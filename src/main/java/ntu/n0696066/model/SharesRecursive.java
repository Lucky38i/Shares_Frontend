package ntu.n0696066.model;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SharesRecursive extends RecursiveTreeObject<SharesRecursive> {
    private StringProperty companyName;
    private StringProperty companySymbol;
    private LongProperty ownedShares;

    public SharesRecursive() {
        companyName = new SimpleStringProperty();
        companySymbol = new SimpleStringProperty();
        ownedShares = new SimpleLongProperty();
    };

    public long getOwnedShares() {
        return ownedShares.get();
    }

    public LongProperty ownedSharesProperty() {
        return ownedShares;
    }

    public void setOwnedShares(long ownedShares) {
        this.ownedShares.set(ownedShares);
    }

    public String getCompanySymbol() {
        return companySymbol.get();
    }

    public StringProperty companySymbolProperty() {
        return companySymbol;
    }

    public void setCompanySymbol(String companySymbol) {
        this.companySymbol.set(companySymbol);
    }

    public String getCompanyName() {
        return companyName.get();
    }

    public StringProperty companyNameProperty() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName.set(companyName);
    }
}
