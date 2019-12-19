package ntu.n0696066.model;

public class Shares {
    private String companyName;
    private String companySymbol;
    private Long ownedShares;

    private Stock stock;

    public Shares() {}

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

    public Long getOwnedShares() {
        return ownedShares;
    }

    public void setOwnedShares(Long ownedShares) {
        this.ownedShares = ownedShares;
    }

    public Stock getStock() {
        return stock;
    }

    public void setStock(Stock stock) {
        this.stock = stock;
    }
}
