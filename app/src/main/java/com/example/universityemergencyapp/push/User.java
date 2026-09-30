package com.example.universityemergencyapp.push;

public class User {
    private String name, id, email, phone,gender, department, hostel;

    // 1. Empty constructor (REQUIRED by Firebase)
    public User() {
    }

    // 2. Constructor with parameters
    public User(String name, String id, String email, String phone, String gender, String department, String hostel) {
        this.name = name;
        this.id = id;
        this.email = email;
        this.phone = phone;
        this.gender = gender;
        this.department = department ;
        this.hostel = hostel ;
    }

    // 3. Getters
    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getGender() {
        return gender;
    }

    public String getDepartment(){return department ;}

    public String getHostel() {
        return hostel;
    }

    // 4. Setters (recommended)
    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setDepartment(String department) {
        this.department = department ;
    }

    public void setHostel() {this.hostel = hostel; }



}