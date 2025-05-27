package com.plss.model;

public class User {
    private long id;
    private String phone;
    private String passkey;

    public User(String phone, String passkey) {
        this.phone = phone;
        this.passkey = passkey;
    }

    // Getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPasskey() { return passkey; }
    public void setPasskey(String passkey) { this.passkey = passkey; }
}
