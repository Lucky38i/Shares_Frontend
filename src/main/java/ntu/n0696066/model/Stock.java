package ntu.n0696066.model;


import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Stock {

    private String currency;
    private Float value;
    private Long currentShares;
    private LocalDate lastUpdate;
    private String shareSymbol;

    private final List<Shares> userShares = new ArrayList<>();

    public Stock(){}

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Float getValue() {
        return value;
    }

    public void setValue(Float value) {
        this.value = value;
    }

    public LocalDate getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDate lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Long getCurrentShares() {
        return currentShares;
    }

    public void setCurrentShares(Long currentShares) {
        this.currentShares = currentShares;
    }

    @JsonIgnore
    public List<Shares> getUserShares() {
        return userShares;
    }

    public String getShareSymbol() {
        return shareSymbol;
    }

    public void setShareSymbol(String shareSymbol) {
        this.shareSymbol = shareSymbol;
    }
}