package com.sun.supplierpoc.models.opera;

public class Reservation {
    private String id;
    private int room;
    private int guestId;
    private String name;
    private int personsNumber;
    private int childrenNumber;
    private String fromDate;
    private String toDate;
    private String website;
    private String email;
    private String phone;
    private String nationality;
    private String lastRoom;
    private String lastVist;
    private String Arr;
    private String Nts;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getRoom() {
        return room;
    }

    public void setRoom(int room) {
        this.room = room;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGuestId() {
        return guestId;
    }

    public void setGuestId(int guestId) {
        this.guestId = guestId;
    }

    public int getPersonsNumber() {
        return personsNumber;
    }

    public void setPersonsNumber(int personsNumber) {
        this.personsNumber = personsNumber;
    }

    public int getChildrenNumber() {
        return childrenNumber;
    }

    public void setChildrenNumber(int childrenNumber) {
        this.childrenNumber = childrenNumber;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getLastRoom() {
        return lastRoom;
    }

    public void setLastRoom(String lastRoom) {
        this.lastRoom = lastRoom;
    }

    public String getLastVist() {
        return lastVist;
    }

    public void setLastVist(String lastVist) {
        this.lastVist = lastVist;
    }

    public String getArr() {
        return Arr;
    }

    public void setArr(String arr) {
        Arr = arr;
    }

    public String getNts() {
        return Nts;
    }

    public void setNts(String nts) {
        Nts = nts;
    }
}
