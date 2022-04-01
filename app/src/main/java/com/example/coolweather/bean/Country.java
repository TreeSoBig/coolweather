package com.example.coolweather.bean;

public class Country {
    private int id;
    private String countryName;
    private String weatherId;
    private int cityId;

    public Country() {
    }

    public Country(int id, String countryName, String weatherId, int cityId) {
        this.id = id;
        this.countryName = countryName;
        this.weatherId = weatherId;
        this.cityId = cityId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
}
