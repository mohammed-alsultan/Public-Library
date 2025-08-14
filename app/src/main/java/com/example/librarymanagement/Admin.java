package com.example.librarymanagement;
public class Admin {
    public int type;
    public String name;
    public String email;
    public String password;

    public Admin() { } // Needed for Firebase

    public Admin(int type, String name, String email, String password) {
        this.type = type;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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
}
