package com.example.universityemergencyapp.push;

public class Econtact {

    String name , relation, phone;

    public Econtact() {
        //Empty
    }

    public Econtact(String name , String relation, String phone) {

        this.name = name ;
        this.relation = relation ;
        this.phone = phone ;

    }

    //set method

    public void setName(String name) {
        this.name = name ;
    }
    public void setRelation(String relation) {
        this.relation = relation ;
    }
    public void setPhone(String phone) {
        this.phone = phone ;
    }



    // get method

    public String getName() {
        return name ;
    }
    public String getRelation() {
        return relation ;
    }

    public String getPhone() {
        return phone ;
    }


}