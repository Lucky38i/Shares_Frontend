package ntu.n0696066.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

import java.util.HashSet;
import java.util.Set;

public class User {
    private String username;

    private final Set<Shares> ownedShares = new HashSet<>();
    private final ObservableList<SharesRecursive> sharesRecursivesList = FXCollections.observableArrayList();

    public User() {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String userName) {
        this.username = userName;
    }

    public Set<Shares> getOwnedShares() {
        return ownedShares;
    }

    @JsonIgnore
    public ObservableList<SharesRecursive> getSharesRecursivesList() {
        return sharesRecursivesList;
    }
}
