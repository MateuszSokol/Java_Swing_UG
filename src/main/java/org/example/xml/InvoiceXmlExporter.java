package org.example.xml;

import org.example.model.Computer;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class InvoiceXmlExporter {

    public void export(List<Computer> computers, Path file)
            throws IOException, XMLStreamException {

        Path parent = file.toAbsolutePath().getParent();
        Files.createDirectories(parent);

        XMLOutputFactory factory = XMLOutputFactory.newFactory();

        try (OutputStream output = Files.newOutputStream(file)) {
            XMLStreamWriter writer =
                    factory.createXMLStreamWriter(output, "UTF-8");

            try {
                writer.writeStartDocument("UTF-8", "1.0");
                writer.writeCharacters("\n");
                writer.writeStartElement("faktura");

                for (Computer computer : computers) {
                    writer.writeCharacters("\n  ");
                    writer.writeStartElement("komputer");

                    writeElement(writer, "nazwa",
                            computer.getComputerName());

                    writeElement(writer, "data_ksiegowania",
                            computer.getBookingDate().toString());

                    writeElement(writer, "koszt_USD",
                            formatAmount(computer.getUsdPrice()));

                    writeElement(writer, "koszt_PLN",
                            formatAmount(computer.getPlnPrice()));

                    writer.writeCharacters("\n  ");
                    writer.writeEndElement(); // komputer
                }

                writer.writeCharacters("\n");
                writer.writeEndElement(); // faktura
                writer.writeEndDocument();
                writer.flush();
            } finally {
                writer.close();
            }
        }
    }

    private void writeElement(
            XMLStreamWriter writer, String name, String value)
            throws XMLStreamException {

        writer.writeCharacters("\n    ");
        writer.writeStartElement(name);
        writer.writeCharacters(value);
        writer.writeEndElement();
    }

    private String formatAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}