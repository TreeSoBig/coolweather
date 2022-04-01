package com.example.coolweather;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.coolweather.application.MyApplication;
import com.example.coolweather.bean.City;
import com.example.coolweather.bean.Country;
import com.example.coolweather.bean.Province;
import com.example.coolweather.db.MyDatabaseHelper;
import com.example.coolweather.util.HttpUtil;
import com.example.coolweather.util.Utility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChooseAreaFragment extends Fragment {
    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTRY = 2;
    private ProgressDialog progressDialog;
    private TextView textView;
    private Button button;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();

    //省市县数据列表
    private List<Province> provinceList;
    private List<City> cityList;
    private List<Country> countryList;

    //选中的省市
    private Province selectedProvince;
    private City selectedCity;

    //当前选中的级别
    private int currentLevel;

    //数据库版本号
    private static int VERSION = 8;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        textView = (TextView) view.findViewById(R.id.title_text);
        button = (Button) view.findViewById(R.id.back_button);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(i);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(i);
                    queryCountries();
                }else if(currentLevel == LEVEL_COUNTRY){
                        String weatherId = countryList.get(i).getWeatherId();
                        if(getActivity() instanceof MainActivity){
                            Intent intent = new Intent(getActivity(), WeatherActivity.class);
                            intent.putExtra("weather_id", weatherId);
                            startActivity(intent);
                            getActivity().finish();
                        }else if(getActivity() instanceof WeatherActivity){
                            WeatherActivity activity = (WeatherActivity) getActivity();
                            activity.drawerLayout.closeDrawers();
                            activity.swipeRefresh.setRefreshing(true);
                            activity.requestWeather(weatherId);
                        }

                }
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentLevel == LEVEL_COUNTRY) {
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    queryProvinces();
                }
            }
        });
        queryProvinces();
    }

    @SuppressLint("Range")
    //查询所有的省  优先从数据库查询  没有查询到再去服务器查询
    private void queryProvinces() {
        //设置标题栏
        textView.setText("中国");

        //设置返回按钮不可见
        button.setVisibility(View.GONE);
        //从数据库中得到省份列表
        MyDatabaseHelper dbHelper = new MyDatabaseHelper(MyApplication.getContext(), "Province.db", null, VERSION);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from Province", null);
        Province province = null;
        provinceList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String provinceName = cursor.getString(cursor.getColumnIndex("provinceName"));
                int provinceCode = cursor.getInt(cursor.getColumnIndex("provinceCode"));
                province = new Province(id, provinceName, provinceCode);
                provinceList.add(province);
            } while (cursor.moveToNext());
        }
        cursor.close();
        if (provinceList.size() > 0) {
            dataList.clear();
            for (int i = 0; i < provinceList.size(); i++) {
                dataList.add(provinceList.get(i).getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            String address = "http://guolin.tech/api/china";
            queryFromServer(address, "province");
        }
    }

    @SuppressLint("Range")
    //查询所有的市  优先从数据库查询  没有查询到再去服务器查询
    private void queryCities() {
        //设置标题栏
        textView.setText(selectedProvince.getProvinceName());
        //设置返回按钮可见
        button.setVisibility(View.VISIBLE);
        //从数据库中得到市列表
        MyDatabaseHelper dbHelper = new MyDatabaseHelper(MyApplication.getContext(), "City.db", null, VERSION);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] args = {String.valueOf(selectedProvince.getProvinceCode())};
        //说明能在数据库中查到数据
        Cursor cursor = db.rawQuery("select * from City where provinceId=?", args);

        cityList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String cityName = cursor.getString(cursor.getColumnIndex("cityName"));
                int cityCode = cursor.getInt(cursor.getColumnIndex("cityCode"));
                int provinceId = cursor.getInt(cursor.getColumnIndex("provinceId"));
                City city = new City(id, cityName, cityCode, provinceId);
                cityList.add(city);
            }

        }
        cursor.close();
        if (cityList != null && cityList.size() > 0) {
            dataList.clear();
            for (City c : cityList) {
                dataList.add(c.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            String address = "http://guolin.tech/api/china/" + provinceCode;
            queryFromServer(address, "city");
        }
    }

    @SuppressLint("Range")
    //查询所有的县 优先从数据库查询  没有查询到再去服务器查询
    private void queryCountries() {
        //设置标题栏
        textView.setText(selectedCity.getCityName());
        //设置返回按钮可见
        button.setVisibility(View.VISIBLE);
        //从数据库中得到县列表
        MyDatabaseHelper dbHelper = new MyDatabaseHelper(MyApplication.getContext(), "Country.db", null, VERSION);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String[] args = {String.valueOf(selectedCity.getCityCode())};
        Cursor cursor = db.rawQuery("select * from Country where cityId=?", args);
        Log.d("bugcatch", "queryCities:" + (cursor == null));
        Country country = null;
        countryList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String countryName = cursor.getString(cursor.getColumnIndex("countryName"));
                String weatherId = cursor.getString(cursor.getColumnIndex("weatherId"));
                int cityId = cursor.getInt(cursor.getColumnIndex("cityId"));
                country = new Country(id, countryName, weatherId, cityId);
                countryList.add(country);
            } while (cursor.moveToNext());
        }
        cursor.close();

        if (countryList != null && countryList.size() > 0) {
            dataList.clear();
            for (Country c : countryList) {
                dataList.add(c.getCountryName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTRY;
        } else {
            int provinceCode = selectedProvince.getProvinceCode();
            int cityCode = selectedCity.getCityCode();
            String address = "http://guolin.tech/api/china/" + provinceCode + "/" + cityCode;
            queryFromServer(address, "country");
        }
    }

    @SuppressLint("Range")
    //根据传入的地址和类型从服务器上查询省市县数据
    private void queryFromServer(String address, final String type) {
        showProgressDialog();
        HttpUtil.sendOKHttpRequest(address, new Callback() {

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String responseData = response.body().string();
                boolean result = false;
                if ("province".equals(type)) {
                    result = Utility.handleProvinceResponse(responseData);
                } else if ("city".equals(type)) {
                    result = Utility.handleCityResponse(responseData, selectedProvince.getProvinceCode());
                } else if ("country".equals(type)) {
                    result = Utility.handleCountyResponse(responseData, selectedCity.getCityCode());
                }
                //切换到主线程 更改UI
                if (result) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)) {
                                queryProvinces();
                            } else if ("city".equals(type)) {
                                queryCities();
                            } else if ("country".equals(type)) {
                                queryCountries();
                                //Thread.sleep(3000);

                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeProgressDialog();
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    //显示进度对话框
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载...");
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    //关闭进度对话框
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}

