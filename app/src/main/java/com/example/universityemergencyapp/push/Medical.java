package com.example.universityemergencyapp.push;

public class Medical {

    String blood_group, medical_condition, allergies, medications, health_card ;

    public Medical(String blood_group, String medical_condition, String allergies, String medications, String health_card) {
        this.blood_group = blood_group ;
        this.medical_condition = medical_condition ;
        this.allergies = allergies ;
        this.medications = medications ;
        this.health_card = health_card ;
    }
    // Add this empty constructor (REQUIRED)
    public Medical() {
    }

    public void setBlood_group(String blood_group) {
        this.blood_group = blood_group ;
    }
    public void setMedical_condition(String medical_condition) {
        this.medical_condition = medical_condition ;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies ;
    }

    public void setMedications(String medications) {
        this.medications = medications ;
    }

    public void setHealth_card(String health_card) {
        this.health_card = health_card ;
    }

    /* Get Method */

    public String getBlood_group() {
        return blood_group ;
    }
    public String getMedical_condition() {
        return medical_condition ;
    }

    public String getAllergies() {
        return allergies ;
    }

    public String getMedications() {
        return medications;
    }

    public String getHealth_card(){
        return health_card ;
    }



}
