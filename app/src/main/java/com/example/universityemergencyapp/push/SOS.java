package com.example.universityemergencyapp.push;

public class SOS {

    String reporter_UID, assing_person, status ;
    double lati, longi ;
    public SOS() {
        // Empty needed
    }

    public SOS(String reporter_UID, double longi, double lati, String assing_person, String status, String issue) {
        this.reporter_UID = reporter_UID ;
        this.longi = longi ;
        this.lati = lati ;
        this.assing_person = assing_person ;
        this.status = status ;

    }

    // set method

    public void setReporter_UID(String UID) {
        this.reporter_UID = reporter_UID ;
    }

    public void setLongitude(double longi) {
        this.longi = longi ;
    }

    public void setLatitude(double lati) {
        this.lati  = lati ;
    }

    public void setAssing_person(String assing_person) {
        this.assing_person = assing_person ;
    }

    public void setStatus(String status) { this.status = status ;  }


    //get method


    public String getReporter_UID() { return reporter_UID ; }
    public double getLongitiude() {return longi ; }
    public double getLatitude() {return lati ; }
    public String getAssing_person() { return assing_person; }
    public String getStatus() { return status; }

}
