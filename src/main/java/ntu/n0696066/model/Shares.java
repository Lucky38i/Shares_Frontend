package ntu.n0696066.model;

public class Shares {
    private String companyName;
    private String companySymbol;
    private long ownedShares;

    private SharePrice sharePrice;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanySymbol() {
        return companySymbol;
    }

    public void setCompanySymbol(String companySymbol) {
        this.companySymbol = companySymbol;
    }

    public long getOwnedShares() {
        return ownedShares;
    }

    public void setOwnedShares(long sharesAmount) {
        this.ownedShares = sharesAmount;
    }

    public SharePrice getSharePrice() {
        return sharePrice;
    }

    public void setSharePrice(SharePrice sharePrice) {
        this.sharePrice = sharePrice;
    }
}
