package org.example.gui;

import org.example.api.ComputerService;
import org.example.dao.ComputerDAOImpl;
import org.example.model.Computer;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.concurrent.ExecutionException;

public class ComputerAddPanel extends JPanel {

    private JTextField computerNameTF;
    private JTextField computerBookingDateTF;
    private JTextField computerUsdPriceTF;

    private JLabel computerNameLabel;
    private JLabel computerBookingDateLabel;
    private JLabel computerUsdPriceLabel;

    private RoundButton confirmFormJButton;
    private final RoundButton backButton = new RoundButton("Powrót do menu");
    private final ComputerService computerService;

    public ComputerAddPanel(Runnable onBack) {
        backButton.addActionListener(e -> onBack.run());
        computerService = new ComputerService(new ComputerDAOImpl());

        initializeLayout();
        initializeTextFields();
        initializeLabels();
        initializeButtons();
        addComponentsToPanel();
    }

    private void initializeLayout() {
        setLayout(new GridBagLayout());
        setBackground(new Color(241, 245, 249));
    }

    private void initializeTextFields() {
        computerNameTF = new JTextField(20);
        computerBookingDateTF = new JTextField(20);
        computerUsdPriceTF = new JTextField(20);
    }

    private void initializeLabels() {
        computerNameLabel = new JLabel("Nazwa komputera");
        computerBookingDateLabel =
                new JLabel("Data księgowania (RRRR-MM-DD)");
        computerUsdPriceLabel = new JLabel("Cena w USD");
    }

    private void initializeButtons() {
        confirmFormJButton = new RoundButton("Zatwierdź formularz");
        confirmFormJButton.addActionListener(e -> submitComputer());
    }

    private void addComponentsToPanel() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        add(computerNameLabel, gbc);

        gbc.gridy = 1;
        add(computerNameTF, gbc);

        gbc.gridy = 2;
        add(computerBookingDateLabel, gbc);

        gbc.gridy = 3;
        add(computerBookingDateTF, gbc);

        gbc.gridy = 4;
        add(computerUsdPriceLabel, gbc);

        gbc.gridy = 5;
        add(computerUsdPriceTF, gbc);

        gbc.gridy = 6;
        gbc.insets = new Insets(20, 5, 5, 5);
        add(confirmFormJButton, gbc);

        gbc.gridy = 7;
        gbc.insets = new Insets(10, 5, 5, 5);
        add(backButton, gbc);
    }

    private void submitComputer() {
        final String name = computerNameTF.getText().trim();
        final LocalDate date;
        final BigDecimal usdPrice;

        if (name.isBlank()) {
            JOptionPane.showMessageDialog(this, "Podaj nazwę komputera.");
            return;
        }

        try {
            date = LocalDate.parse(
                    computerBookingDateTF.getText().trim()
            );

            usdPrice = new BigDecimal(
                    computerUsdPriceTF.getText().trim().replace(',', '.')
            );

            if (usdPrice.signum() <= 0) {
                JOptionPane.showMessageDialog(
                        this, "Cena musi być dodatnia."
                );
                return;
            }
        } catch (DateTimeParseException | NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Podaj datę w formacie RRRR-MM-DD "
                            + "i poprawną kwotę USD."
            );
            return;
        }

        confirmFormJButton.setEnabled(false);
        backButton.setEnabled(false);

        SwingWorker<Computer, Void> worker = new SwingWorker<>() {

            @Override
            protected Computer doInBackground() {

                return computerService.addComputer(name, date, usdPrice);
            }

            @Override
            protected void done() {

                try {
                    Computer savedComputer = get();

                    LocalDate rateDate = savedComputer.getExchangeRateDate();

                    String message =
                            "Zapisano komputer: " + savedComputer.getComputerName()
                                    + "\nCena USD: " + savedComputer.getUsdPrice()
                                    + "\nCena PLN: " + savedComputer.getPlnPrice()
                                    + "\nData księgowania: " + savedComputer.getBookingDate()
                                    + "\nData kursu NBP: "
                                    + (rateDate == null ? "brak informacji" : rateDate)
                                    + "\nZaktualizowano raport XML.";

                    if (rateDate != null
                            && !savedComputer.getBookingDate().equals(rateDate)) {
                        message += "\n\nBrak notowania dla wskazanej daty."
                                + "\nZastosowano ostatni dostępny wcześniejszy kurs.";
                    }

                    JOptionPane.showMessageDialog(
                            ComputerAddPanel.this,
                            message,
                            "Zapis zakończony",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    JOptionPane.showMessageDialog(
                            ComputerAddPanel.this,
                            "Operacja nie powiodła się: "
                                    + e.getCause().getMessage(),
                            "Błąd",
                            JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    confirmFormJButton.setEnabled(true);
                    backButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }
}
