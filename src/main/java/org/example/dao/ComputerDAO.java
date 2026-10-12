package org.example.dao;

import org.example.model.Computer;

import java.time.LocalDate;
import java.util.List;

public interface ComputerDAO {
    Computer saveComputertoDB(Computer computer);

    List<Computer> findComputerByName(String nameFragment);

    List<Computer> findComputerByBookingDate(LocalDate bookingDate);
}