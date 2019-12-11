package ntu.n0696066.model;


import java.time.LocalDate;

public class SharePrice {

    private String currency;
    private float value;
    private long currentShares;
    private LocalDate lastUpdate;

    public SharePrice(){};

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public LocalDate getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDate lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public long getCurrentShares() {
        return currentShares;
    }

    public void setCurrentShares(long currentShares) {
        this.currentShares = currentShares;
    }
}