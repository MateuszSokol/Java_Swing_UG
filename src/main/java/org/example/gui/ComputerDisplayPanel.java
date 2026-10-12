package org.example.gui;

import org.example.dao.ComputerDAO;
import org.example.dao.ComputerDAOImpl;
import org.example.model.Computer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ComputerDisplayPanel extends JPanel {

    private final ComputerDAO dao;
    private final RoundButton backButton = new RoundButton("Powrót do menu");

    private final JTextField nameTF = new JTextField(16);
    private final JTextField dateTF = new JTextField(10);
    private final RoundButton searchButton = new RoundButton("Wyszukaj");
    private final JLabel statusLabel =
            new JLabel("Wpisz kryteria lub wyszukaj wszystkie komputery.");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{
                    "ID", "Nazwa komputera", "Data księgowania",
                    "Cena USD", "Cena PLN"
            },
            0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return switch (columnIndex) {
                case 0 -> Long.class;
                case 2 -> LocalDate.class;
                case 3, 4 -> BigDecimal.class;
                default -> String.class;
            };
        }
    };

    private final JTable table = new JTable(tableModel);

    public ComputerDisplayPanel(Runnable onBack) {
        this(new ComputerDAOImpl(), onBack);
    }

    public ComputerDisplayPanel(ComputerDAO dao, Runnable onBack) {
        backButton.addActionListener(e -> onBack.run());
        this.dao = dao;

        initializeLayout();
        initializeListeners();
    }

    private void initializeLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(241, 245, 249));

        JPanel filtersPanel = new JPanel(new GridBagLayout());
        filtersPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        filtersPanel.add(new JLabel("Fragment nazwy:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        filtersPanel.add(nameTF, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        filtersPanel.add(new JLabel("Data (RRRR-MM-DD):"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        filtersPanel.add(dateTF, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        filtersPanel.add(searchButton, gbc);

        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setRowHeight(25);
        table.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(650, 300));

        add(filtersPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.add(statusLabel, BorderLayout.CENTER);
        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        navigationPanel.setOpaque(false);
        navigationPanel.add(backButton);
        bottomPanel.add(navigationPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void initializeListeners() {
        searchButton.addActionListener(e -> searchComputers());
        nameTF.addActionListener(e -> searchComputers());
        dateTF.addActionListener(e -> searchComputers());
    }

    private void searchComputers() {
        if (!searchButton.isEnabled()) {
            return;
        }

        final String nameFragment = nameTF.getText().trim();
        final String dateText = dateTF.getText().trim();
        final LocalDate bookingDate;

        try {
            bookingDate = dateText.isEmpty()
                    ? null
                    : LocalDate.parse(dateText);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Podaj datę w formacie RRRR-MM-DD, np. 2026-07-03.",
                    "Niepoprawna data",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        searchButton.setEnabled(false);
        backButton.setEnabled(false);
        statusLabel.setText("Wyszukiwanie...");

        SwingWorker<List<Computer>, Void> worker = new SwingWorker<>() {

            @Override
            protected List<Computer> doInBackground() {
                if (nameFragment.isEmpty() && bookingDate != null) {
                    return dao.findComputerByBookingDate(bookingDate);
                }

                List<Computer> computers =
                        dao.findComputerByName(nameFragment);

                if (bookingDate != null) {
                    return computers.stream()
                            .filter(computer -> bookingDate.equals(
                                    computer.getBookingDate()
                            ))
                            .toList();
                }

                return computers;
            }

            @Override
            protected void done() {
                try {
                    List<Computer> computers = get();
                    showComputers(computers);

                    statusLabel.setText(
                            computers.isEmpty()
                                    ? "Brak komputerów spełniających kryteria."
                                    : "Liczba wyników: " + computers.size()
                    );
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    statusLabel.setText("Przerwano odbieranie wyników.");
                } catch (ExecutionException e) {
                    tableModel.setRowCount(0);
                    statusLabel.setText("Wyszukiwanie nie powiodło się.");

                    JOptionPane.showMessageDialog(
                            ComputerDisplayPanel.this,
                            "Nie udało się pobrać komputerów: "
                                    + e.getCause().getMessage(),
                            "Błąd",
                            JOptionPane.ERROR_MESSAGE
                    );
                } finally {
                    searchButton.setEnabled(true);
                    backButton.setEnabled(true);
                }
            }
        };

        worker.execute();
    }

    private void showComputers(List<Computer> computers) {
        tableModel.setRowCount(0);

        for (Computer computer : computers) {
            tableModel.addRow(new Object[]{
                    computer.getId(),
                    computer.getComputerName(),
                    computer.getBookingDate(),
                    computer.getUsdPrice(),
                    computer.getPlnPrice()
            });
        }
    }
}
