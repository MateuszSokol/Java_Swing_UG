package org.example.dao;

import org.example.model.Computer;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ComputerDAOImpl implements ComputerDAO{

    @Override
    public Computer saveComputertoDB(Computer computer) {
        String sql = """
            INSERT INTO computers
                (usd_price, pln_price, computer_name, booking_date)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setBigDecimal(1, computer.getUsdPrice());
            statement.setBigDecimal(2, computer.getPlnPrice());
            statement.setString(3, computer.getComputerName());
            statement.setObject(4, computer.getBookingDate());

            if (statement.executeUpdate() != 1) {
                throw new SQLException("Nie zapisano jednego rekordu komputera");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    computer.setId(keys.getLong(1));
                } else {
                    throw new SQLException("Nie otrzymano ID zapisanego komputera");
                }
            }

            return computer;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Błąd zapisu komputera lub odczytu jego ID", e);
        }
    }
    @Override
    public List<Computer> findComputerByName(String nameFragment) {
        String sql = """
            SELECT id, usd_price, pln_price, computer_name, booking_date
            FROM computers
            WHERE computer_name LIKE ? ESCAPE '!'
            ORDER BY computer_name, booking_date, id
            """;

        List<Computer> computers = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {


            String escapedFragment = nameFragment
                    .replace("!", "!!")
                    .replace("%", "!%")
                    .replace("_", "!_");

            statement.setString(1, "%" + escapedFragment + "%");

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    computers.add(mapComputer(result));
                }
            }

            return computers;

        } catch (SQLException e) {
            throw new RuntimeException("Błąd wyszukiwania po nazwie", e);
        }
    }

    @Override
    public List<Computer> findComputerByBookingDate(LocalDate bookingDate) {
        String sql = """
            SELECT id, usd_price, pln_price, computer_name, booking_date
            FROM computers
            WHERE booking_date = ?
            ORDER BY computer_name, id
            """;

        List<Computer> computers = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setObject(1, bookingDate);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    computers.add(mapComputer(result));
                }
            }

            return computers;

        } catch (SQLException e) {
            throw new RuntimeException("Błąd wyszukiwania po dacie", e);
        }
    }

    private Computer mapComputer(ResultSet result) throws SQLException {
        Computer computer = new Computer();

        computer.setId(result.getLong("id"));
        computer.setUsdPrice(result.getBigDecimal("usd_price"));
        computer.setPlnPrice(result.getBigDecimal("pln_price"));
        computer.setComputerName(result.getString("computer_name"));
        computer.setBookingDate(
                result.getObject("booking_date", LocalDate.class)
        );

        return computer;
    }


}
