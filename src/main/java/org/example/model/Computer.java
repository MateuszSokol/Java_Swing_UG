package org.example.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Computer
{

    private long id;
    private BigDecimal usdPrice;
    private BigDecimal plnPrice;
    private String computerName;
    private LocalDate bookingDate;


    public Computer()
    {

    }

    public Computer(long id, BigDecimal usdPrice, BigDecimal plnPrice, String computerName, LocalDate bookingDate) {
        this.id = id;
        this.usdPrice = usdPrice;
        this.plnPrice = plnPrice;
        this.computerName = computerName;
        this.bookingDate = bookingDate;
    }
    private LocalDate exchangeRateDate;

    public LocalDate getExchangeRateDate() {
        return exchangeRateDate;
    }

    public void setExchangeRateDate(LocalDate exchangeRateDate) {
        this.exchangeRateDate = exchangeRateDate;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public BigDecimal getUsdPrice() {
        return usdPrice;
    }

    public void setUsdPrice(BigDecimal usdPrice) {
        this.usdPrice = usdPrice;
    }

    public BigDecimal getPlnPrice() {
        return plnPrice;
    }

    public void setPlnPrice(BigDecimal plnPrice) {
        this.plnPrice = plnPrice;
    }

    public String getComputerName() {
        return computerName;
    }

    public void setComputerName(String computerName) {
        this.computerName = computerName;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }
}
