package ntu.n0696066.model;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.LongProperty;
import javafx.beans.property.StringProperty;

public class Shares extends RecursiveTreeObject<Shares> {
    private StringProperty companyName;
    private StringProperty companySymbol;
    private LongProperty ownedShares;

    private SharePrice sharePrice;

    public StringProperty getCompanyName() {
        return companyName;
    }

    public void setCompanyName(StringProperty companyName) {
        this.companyName = companyName;
    }

    public StringProperty getCompanySymbol() {
        return companySymbol;
    }

    public void setCompanySymbol(StringProperty companySymbol) {
        this.companySymbol = companySymbol;
    }

    public LongProperty getOwnedShares() {
        return ownedShares;
    }

    public void setOwnedShares(LongProperty sharesAmount) {
        this.ownedShares = sharesAmount;
    }

    public SharePrice getSharePrice() {
        return sharePrice;
    }

    public void setSharePrice(SharePrice sharePrice) {
        this.sharePrice = sharePrice;
    }
}
