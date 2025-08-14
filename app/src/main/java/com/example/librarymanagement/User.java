package com.example.librarymanagement;
public class User {

    public String uid;

    public String name;
    public String email;
    public int enroll;
    public int card;
    public int type;
    public String password;

    public User() { } // Needed for Firebase

    public User(String name, String email, int enroll, int card, int type, String password) {
        this.name = name;
        this.email = email;
        this.enroll = enroll;
        this.card = card;
        this.type = type;
        this.password = password;
    }

    // Getter & Setter for UID
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
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

    public int getEnroll() {
        return enroll;
    }

    public void setEnroll(int enroll) {
        this.enroll = enroll;
    }

    public int getCard() {
        return card;
    }

    public void setCard(int card) {
        this.card = card;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
