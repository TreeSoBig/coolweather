package com.example.coolweather.util;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.example.coolweather.application.MyApplication;
import com.example.coolweather.bean.City;
import com.example.coolweather.bean.Country;
import com.example.coolweather.db.MyDatabaseHelper;
import com.example.coolweather.bean.Province;
import com.example.coolweather.gson.Weather;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utility{
    private static int VERSION = 8;
    /**
     * 解析和处理服务器返回的省级数据
     * */
    public static boolean handleProvinceResponse(String response){
        MyDatabaseHelper dbHelper = new MyDatabaseHelper(MyApplication.getContext(),"Province.db",null,VERSION);
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allprovinces = new JSONArray(response);
                for (int i = 0; i < allprovinces.length(); i++) {
                    JSONObject provinceObject = allprovinces.getJSONObject(i);
                    Province province = new Province();

                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("provinceName",provinceObject.getString("name"));
                    contentValues.put("provinceCode",provinceObject.getInt("id"));
                    db.insert("Province",null,contentValues);
                    contentValues.clear();
                }
                return true;
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }


    /**
     * 解析和处理服务器返回的市级数据
     * */
    public static boolean handleCityResponse(String response,int provinceId){
        MyDatabaseHelper dbHelper = new MyDatabaseHelper(MyApplication.getContext(),"City.db",null,VERSION);
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allCities = new JSONArray(response);
                for (int i = 0; i < allCities.length(); i++) {
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("cityName",cityObject.getString("name"));
                    contentValues.put("cityCode",cityObject.getInt("id"));
                    contentValues.put("provinceId",provinceId);
                    db.insert("City",null,contentValues);
                    contentValues.clear();
                }
                return true;
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 解析和处理服务器返回的县级数据
     * */
    public static boolean handleCountyResponse(String response,int cityId){
        MyDatabaseHelper dbHelper = new MyDatabaseHelper(MyApplication.getContext(),"Country.db",null,VERSION);
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allcountrys = new JSONArray(response);
                for (int i = 0; i < allcountrys.length(); i++) {
                    JSONObject countryObject = allcountrys.getJSONObject(i);
                    Country country = new Country();
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("countryName",countryObject.getString("name"));
                    contentValues.put("weatherId",countryObject.getString("weather_id"));
                    contentValues.put("cityId",cityId);
                    db.insert("Country",null,contentValues);
                    contentValues.clear();
                }
                return true;
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 将返回的JSON数据解析成weather实体类
     * */
    public static Weather handleWeatherResponse(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weatherContent = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,Weather.class);
       }catch(Exception e){
            Log.d("bugcatchs","Weather22222");
            e.printStackTrace();
       }
        return null;
    }

}
