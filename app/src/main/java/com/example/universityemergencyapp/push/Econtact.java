package com.example.universityemergencyapp.push;

public class Econtact {

    String name, phone, relation ;

    public Econtact() {
        //Empty
    }

    public Econtact(String name, String relation, String phone) {
        this.name = name ;
        this.relation = relation ;
        this.phone = phone ;
    }

    // set -> function()
    public void setName(String name) {
        this.name = name ;
    }

    public void setPhone(String phone) {
        this.phone = phone ;
    }

    public void setRelation(String relation) {
        this.relation = relation ;
    }

    // get -> fuction()


    public String getName(){return name ;}
    public String getPhone() {return phone; }
    public String getRelation() { return relation ; }

}
