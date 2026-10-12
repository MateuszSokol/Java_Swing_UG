package org.example.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dao.ComputerDAO;
import org.example.model.Computer;
import org.example.xml.InvoiceXmlExporter;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class ComputerService {

    private static final String TEMPLATE_ENDPOINT =
            "https://api.nbp.pl/api/exchangerates/rates/";

    private static final Path XML_FILE =
            Path.of("reports", "faktura.xml");

    private static final int LOOKBACK_DAYS = 14;

    private final ComputerDAO dao;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final InvoiceXmlExporter xmlExporter = new InvoiceXmlExporter();

    public ComputerService(ComputerDAO dao) {
        this.dao = dao;
    }

    public Computer addComputer(
            String name, LocalDate bookingDate, BigDecimal usdPrice) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Podaj nazwę komputera.");
        }

        if (bookingDate == null || bookingDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Podaj datę księgowania nie późniejszą niż dzisiaj."
            );
        }

        if (usdPrice == null || usdPrice.signum() <= 0) {
            throw new IllegalArgumentException("Cena USD musi być dodatnia.");
        }

        NbpRate rate;

        try {
            rate = fetchRate(bookingDate);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Nie udało się pobrać lub odczytać kursu NBP: "
                            + e.getMessage(),
                    e
            );
        }

        BigDecimal plnPrice = usdPrice
                .multiply(rate.getMid())
                .setScale(2, RoundingMode.HALF_UP);

        Computer computer = createComputer(
                name.trim(), bookingDate, usdPrice, plnPrice
        );

        computer.setExchangeRateDate(
                LocalDate.parse(rate.getEffectiveDate())
        );

        Computer savedComputer = dao.saveComputertoDB(computer);

        try {
            exportAllComputersToXml();
        } catch (IOException | XMLStreamException | RuntimeException e) {
            throw new IllegalStateException(
                    "Komputer zapisano w bazie (ID: "
                            + savedComputer.getId()
                            + "), ale eksport XML nie powiódł się. "
                            + "Nie dodawaj ponownie tego samego zakupu.",
                    e
            );
        }

        return savedComputer;
    }

    private NbpRate fetchRate(LocalDate requestedDate) throws IOException {
        String endpoint = TEMPLATE_ENDPOINT
                + "A/USD/" + requestedDate + "/";

        try {
            String json = ConnectionHandler.fetchData(endpoint);

            return readLatestRate(json, requestedDate, requestedDate);

        } catch (ConnectionHandler.HttpStatusException e) {
            if (e.getStatusCode() != 404) {
                throw e;
            }
        }

        LocalDate startDate = requestedDate.minusDays(LOOKBACK_DAYS);
        LocalDate endDate = requestedDate.minusDays(1);

        String rangeEndpoint = TEMPLATE_ENDPOINT
                + "A/USD/" + startDate + "/" + endDate + "/";

        try {
            String json = ConnectionHandler.fetchData(rangeEndpoint);

            return readLatestRate(json, startDate, endDate);

        } catch (ConnectionHandler.HttpStatusException e) {
            if (e.getStatusCode() == 404) {
                throw new IOException(
                        "Brak kursu USD dla " + requestedDate
                                + " oraz w ciągu "
                                + LOOKBACK_DAYS + " poprzednich dni.",
                        e
                );
            }

            throw e;
        }
    }

    private NbpRate readLatestRate(
            String json, LocalDate startDate, LocalDate endDate)
            throws IOException {

        NbpResponse response =
                objectMapper.readValue(json, NbpResponse.class);

        if (response == null
                || response.getRates() == null
                || response.getRates().isEmpty()) {
            throw new IOException("Brak kursów w odpowiedzi NBP.");
        }

        NbpRate latestRate = null;
        LocalDate latestDate = null;

        for (NbpRate rate : response.getRates()) {
            if (rate == null
                    || rate.getEffectiveDate() == null
                    || rate.getMid() == null
                    || rate.getMid().signum() <= 0) {
                throw new IOException(
                        "Niepoprawne dane kursu w odpowiedzi NBP."
                );
            }

            LocalDate rateDate;

            try {
                rateDate = LocalDate.parse(rate.getEffectiveDate());
            } catch (DateTimeParseException e) {
                throw new IOException(
                        "Niepoprawna data kursu NBP.", e
                );
            }

            if (rateDate.isBefore(startDate) || rateDate.isAfter(endDate)) {
                continue;
            }

            if (latestDate == null || rateDate.isAfter(latestDate)) {
                latestRate = rate;
                latestDate = rateDate;
            }
        }

        if (latestRate == null) {
            throw new IOException("Brak kursu w żądanym przedziale dat.");
        }

        return latestRate;
    }

    public void exportAllComputersToXml()
            throws IOException, XMLStreamException {

        List<Computer> computers = dao.findComputerByName("");
        xmlExporter.export(computers, XML_FILE);
    }

    private Computer createComputer(
            String name,
            LocalDate bookingDate,
            BigDecimal usdPrice,
            BigDecimal plnPrice) {

        Computer computer = new Computer();
        computer.setComputerName(name);
        computer.setBookingDate(bookingDate);
        computer.setUsdPrice(usdPrice);
        computer.setPlnPrice(plnPrice);

        return computer;
    }
}