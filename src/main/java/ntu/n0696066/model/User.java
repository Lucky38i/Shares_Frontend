package ntu.n0696066.model;

import java.util.HashSet;
import java.util.Set;

public class User {
    private String username;
    private String password;

    private final Set<Shares> ownedShares = new HashSet<>();

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String userName) {
        this.username = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String role) {
        this.password = role;
    }

    public Set<Shares> getOwnedShares() {
        return ownedShares;
    }
}
