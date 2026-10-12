package org.example.api;

import java.math.BigDecimal;
import java.util.List;

public class NbpResponse
{
    private String table;
    private String currency;
    private String code;
    private List<NbpRate> rates;

    public NbpResponse() {
    }

    public NbpResponse(String table, String code, String currency, List<NbpRate> rates) {
        this.table = table;
        this.code = code;
        this.currency = currency;
        this.rates = rates;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<NbpRate> getRates() {
        return rates;
    }

    public void setRates(List<NbpRate> rates) {
        this.rates = rates;
    }
}
