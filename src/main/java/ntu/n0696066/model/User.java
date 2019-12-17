package ntu.n0696066.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

public class User {
    private String username;

    private final ObservableList<Shares> ownedShares = FXCollections.observableArrayList();

    public User() {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String userName) {
        this.username = userName;
    }

    public ObservableList<Shares> getOwnedShares() {
        return ownedShares;
    }
}
