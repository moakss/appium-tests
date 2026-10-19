package com.mydemoappappium.testdata;

public record User(String username, String password) {

    public User withUsername(String username) {
        return new User(username, password);
    }

    public User withPassword(String password) {
        return new User(username, password);
    }
}
