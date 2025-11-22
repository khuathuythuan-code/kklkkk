package com.ExpenseTracker.model;

import java.time.LocalDate;

public class User {
    private int id;
    private String userName;
    private String passWord;
    private String email;
    private int phone;
    private LocalDate DateOfBirth;

    public User(int id, String userName,
                String passWord, String email,
                int phone) {
        this.id = id;
        this.userName = userName;
        this.passWord = passWord;
        this.email = email;
        this.phone = phone;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getPhone() {
        return phone;
    }

    public void setPhone(int phone) {
        this.phone = phone;
    }

    public LocalDate getDateOfBirth() {
        return DateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        DateOfBirth = dateOfBirth;
    }
}
