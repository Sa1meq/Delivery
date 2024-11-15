package com.example.delivery.model;

public class User {
    public String id;
    public String name;
    public String email;
    public String password;
    public String balance;
    public boolean isAdmin;

    public User(String id, String name, String email, String password, String balance, boolean isAdmin) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.balance = balance;
        this.isAdmin = isAdmin;
    }

    public User() {
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }
}