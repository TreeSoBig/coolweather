package com.example.coolweather.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    public static final String CREATE_PROVINCE="create table Province("
            +"id integer primary key autoincrement,"
            +"provinceName text,"
            +"provinceCode integer)";
    public static final String CREATE_CITY="create table City("
            +"id integer primary key autoincrement,"
            +"cityName text,"
            +"cityCode integer,"
            +"provinceId integer)";
    public static final String CREATE_COUNTRY="create table Country("
            +"id integer primary key autoincrement,"
            +"countryName text,"
            +"weatherId text,"
            +"cityId integer)";
    private Context mContext;
    public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,int version){
        super(context,name,factory,version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_PROVINCE);
        Toast.makeText(mContext,"Create province succeeded",Toast.LENGTH_SHORT).show();
        sqLiteDatabase.execSQL(CREATE_CITY);
        Toast.makeText(mContext,"Create city succeeded",Toast.LENGTH_SHORT).show();
        sqLiteDatabase.execSQL(CREATE_COUNTRY);
        Toast.makeText(mContext,"Create country succeeded",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table if exists Province");
        sqLiteDatabase.execSQL("drop table if exists City");
        sqLiteDatabase.execSQL("drop table if exists Country");
        onCreate(sqLiteDatabase);
    }
}
