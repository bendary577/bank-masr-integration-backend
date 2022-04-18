package com.sun.supplierpoc.models.aggregtor.TalabatRest;

public class Address{
    public String customerAddressId;
    public String street;
    public String zip;
    public String city;
    public String area;
    public String block;
    public String floor;
    public String apartment;
    public String building;
    public String buildingName;
    public String entrance;
    public String intercom;
    public String info;
    public double latitude;
    public double longitude;
    public int distance;
    public boolean geocodedManually;
    public String formattedAddress;

    public String getCustomerAddressId() {
        return customerAddressId;
    }

    public void setCustomerAddressId(String customerAddressId) {
        this.customerAddressId = customerAddressId;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(String block) {
        this.block = block;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public String getApartment() {
        return apartment;
    }

    public void setApartment(String apartment) {
        this.apartment = apartment;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getBuildingName() {
        return buildingName;
    }

    public void setBuildingName(String buildingName) {
        this.buildingName = buildingName;
    }

    public String getEntrance() {
        return entrance;
    }

    public void setEntrance(String entrance) {
        this.entrance = entrance;
    }

    public String getIntercom() {
        return intercom;
    }

    public void setIntercom(String intercom) {
        this.intercom = intercom;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public boolean isGeocodedManually() {
        return geocodedManually;
    }

    public void setGeocodedManually(boolean geocodedManually) {
        this.geocodedManually = geocodedManually;
    }

    public String getFormattedAddress() {
        return formattedAddress;
    }

    public void setFormattedAddress(String formattedAddress) {
        this.formattedAddress = formattedAddress;
    }

    @Override
    public String toString() {
        return "Address{" +
                "customerAddressId='" + customerAddressId + '\'' +
                ", street='" + street + '\'' +
                ", zip='" + zip + '\'' +
                ", city='" + city + '\'' +
                ", area='" + area + '\'' +
                ", block='" + block + '\'' +
                ", floor='" + floor + '\'' +
                '}';
    }
}
