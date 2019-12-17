package ntu.n0696066.model;


import javafx.beans.property.FloatProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDate;

public class SharePrice {

    private StringProperty currency;
    private FloatProperty value;
    private LongProperty currentShares;
    private ObjectProperty<LocalDate> lastUpdate;

    public SharePrice(){}

    public StringProperty getCurrency() {
        return currency;
    }

    public void setCurrency(StringProperty currency) {
        this.currency = currency;
    }

    public FloatProperty getValue() {
        return value;
    }

    public void setValue(FloatProperty value) {
        this.value = value;
    }

    public ObjectProperty<LocalDate> getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(ObjectProperty<LocalDate> lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public LongProperty getCurrentShares() {
        return currentShares;
    }

    public void setCurrentShares(LongProperty currentShares) {
        this.currentShares = currentShares;
    }
}