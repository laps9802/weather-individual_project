package com.cs.weather3;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.cs.weather3.model.geocode.AcceptGeocode;
import com.cs.weather3.model.geocode.Area1;
import com.cs.weather3.model.geocode.Area2;
import com.cs.weather3.model.geocode.Orders;
import com.cs.weather3.model.geocode.Region;
import com.cs.weather3.network.NaverMapRetrofitSetup;
import com.cs.weather3.util.GridUtil;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements AutoPermissionsListener {
    //상수, 전역 사용
    private String TAG = "MainActi-";

    private FragmentManager fm;

    private boolean boolPermission = false;
    private boolean boolExistingLocation = false;
    private boolean boolUpdatedLocation = false;
    private boolean boolTime = false;
    private boolean boolAnnouncingTime=false;
    private boolean boolUpdatedAnnouncingTime=false;
    private boolean boolUpdatedReverseGeocode=false;

    //프래그먼트
    private OnLocationUpdatedListener callback;
    private Fragment mainFragment, midTermFragment, weeklyFragment;

    //위치
    private Location targetLocation;
    private LocationManager manager;
    private LocationListener locatingListener;
    private int intGridX;
    private int intGridY;

    //시간
    private String currentTime;
    private String base_date;
    private String base_time;
    private int currentHour;
    private int nextHour;

    private String announcing_time;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        boolPermission = getIntent().getBooleanExtra("boolPermission", false);



        //BottomNavigation, Fragment 초기화
        fm = getSupportFragmentManager();
        mainFragment = new MainFragment();
        midTermFragment = new MidTermFragment();
        weeklyFragment = new WeeklyFragment();

        fm.beginTransaction().replace(R.id.fragment_container, mainFragment).commit();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                FragmentTransaction tran = fm.beginTransaction();
                switch (menuItem.getItemId()) {
                    case R.id.bottomNav_main:
                        tran.replace(R.id.fragment_container, mainFragment).commit();
                        return true;
                    case R.id.bottomNav_midTerm:
                        tran.replace(R.id.fragment_container, midTermFragment).commit();
                        return true;
                    case R.id.bottomNav_monthTerm:
                        tran.replace(R.id.fragment_container, weeklyFragment).commit();
                        return true;
                }
                return false;
            }
        });
    }



    //리스너 연결
    @Override
    public void onAttachFragment(Fragment fragment) {
        //위치 기존
        getExistingLocation();
        if (boolExistingLocation) {
            Map<String, Double> gridMap = GridUtil.getGrid(targetLocation.getLatitude(), targetLocation.getLongitude());
            intGridX = (int) (double) gridMap.get("x");
            intGridY = (int) (double) gridMap.get("y");
            Log.e(TAG+"격자:기존", intGridX + "   " + intGridY);
        }else {
            Log.e(TAG+"위치:기존", "위치값을 얻지 못했습니다");
        }

        //시간 기존 / 번들 전달
        if (fragment instanceof MainFragment) {
            getTime();
            if(!isNull(base_date) && !isNull(base_time))
                boolTime = true;
            else
                Log.e(TAG+"시간:기존", "시간값을 얻지 못했습니다");


            MainFragment attachedMainFragment = (MainFragment) fragment;
            this.callback = attachedMainFragment;
            attachedMainFragment.setMainActivity(this);

            Bundle mainParcelBundle = new Bundle();
            MainBundle mainBundle = mainBundleInit();
            mainParcelBundle.putParcelable("MainBundle", mainBundle);
            fragment.setArguments(mainParcelBundle);
        } else if (fragment instanceof MidTermFragment) {
            getTimeThreeHour();
            if(!isNull(base_date) && !isNull(base_time))
                boolTime = true;
            else
                Log.e(TAG+"시간:기존", "시간값을 얻지 못했습니다");

            MidTermFragment attachedMidTermFragment = (MidTermFragment) fragment;
            this.callback = attachedMidTermFragment;
            attachedMidTermFragment.setMainActivity(this);

            Bundle threeHourParcelBundle = new Bundle();
            ThreeHourBundle threeHourBundle = threeHourBundleInit();
            threeHourParcelBundle.putParcelable("threeHourBundle", threeHourBundle);
            fragment.setArguments(threeHourParcelBundle);
        } else if (fragment instanceof WeeklyFragment) {
            getTimeWeekly();
            if(!isNull(announcing_time))
                boolAnnouncingTime = true;
            else
                Log.e(TAG+"시간:기존", "시간값을 얻지 못했습니다");

            WeeklyFragment attachedWeeklyFragment = (WeeklyFragment) fragment;
            this.callback = attachedWeeklyFragment;
            attachedWeeklyFragment.setMainActivity(this);

            Bundle weeklyParcelBundle = new Bundle();
            weeklyParcelBundle.putBoolean("boolExistingLocation", boolExistingLocation);
            weeklyParcelBundle.putDouble("doubleLat", targetLocation.getLatitude());
            weeklyParcelBundle.putDouble("doubleLng", targetLocation.getLongitude());
            weeklyParcelBundle.putBoolean("boolAnnouncingTime", boolAnnouncingTime);
            weeklyParcelBundle.putString("announcing_time", announcing_time);
            fragment.setArguments(weeklyParcelBundle);
        }

        //불값 초기화
        boolExistingLocation = false;
        boolUpdatedLocation = false;
        boolTime = false;
        boolAnnouncingTime = false;
    }







    private MainBundle mainBundleInit(){
        MainBundle mainBundle = new MainBundle();

        mainBundle.intGridX = this.intGridX;
        mainBundle.intGridY = this.intGridY;
        mainBundle.base_date = this.base_date;
        mainBundle.base_time = this.base_time;
        mainBundle.currentTime = this.currentTime;
        mainBundle.currentHour = this.currentHour;
        mainBundle.nextHour = this.nextHour;

        mainBundle.boolPermission = this.boolPermission;
        mainBundle.boolExistingLocation = this.boolExistingLocation;
        mainBundle.boolUpdatedLocation = this.boolUpdatedLocation;
        mainBundle.boolTime = this.boolTime;

        return mainBundle;
    }

    private ThreeHourBundle threeHourBundleInit(){
        ThreeHourBundle threeHourBundle = new ThreeHourBundle();

        threeHourBundle.intGridX = this.intGridX;
        threeHourBundle.intGridY = this.intGridY;
        threeHourBundle.base_date = this.base_date;
        threeHourBundle.base_time = this.base_time;

        threeHourBundle.boolPermission = this.boolPermission;
        threeHourBundle.boolExistingLocation = this.boolExistingLocation;
        threeHourBundle.boolUpdatedLocation = this.boolUpdatedLocation;
        threeHourBundle.boolTime = this.boolTime;

        return threeHourBundle;
    }







    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AutoPermissions.Companion.parsePermissions(this, requestCode, permissions, this);
    }
    @Override
    public void onDenied(int i, String[] strings) {
        Log.e(TAG+"권한", "거절");
        boolPermission = true;
    }
    @Override
    public void onGranted(int i, String[] strings) {
        Log.e(TAG+"권한", "승인");
        boolPermission = true;
    }




    //***위치
    private void getExistingLocation() {
        if(isNull(manager)) manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //권한
            AutoPermissions.Companion.loadSelectedPermissions(this, 101,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
            return;
        }

        Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Log.e(TAG+"기존위치", location.getLatitude() + "  " + location.getLongitude());
        if (!isNull(location)) {
            targetLocation = location;
            boolExistingLocation = true;
            Log.e(TAG+"기존위경도", targetLocation.getLatitude() + "  " + targetLocation.getLongitude());
        }
    }

    public void getUpdatedLocation() {
        if(isNull(manager)) manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //권한
            AutoPermissions.Companion.loadSelectedPermissions(this, 101,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
            return;
        }

        locatingListener = new LocatingListener();
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100000L, 100000F, locatingListener);
        Log.e(TAG+"위치업데이트요청", "위치 업데이트 요청 이후");
    }

    class LocatingListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            //#위치 업데이트 시
            Log.e(TAG+"위치업데이트요청", "onLocationChanged 시작");
            if(isNull(location)) {
                Log.e(TAG+"위치업데이트요청", "location 널값");
                boolUpdatedLocation = false;
                return;
            }

            //업데이트된 위치 전역변수에 전달 & API 요청
            targetLocation = location;
            boolUpdatedLocation = true;
            Log.e(TAG+"업데이트위경도", targetLocation.getLatitude() + "  " + targetLocation.getLongitude());



            Map<String, Double> gridMap = GridUtil.getGrid(targetLocation.getLatitude(), targetLocation.getLongitude());
            Bundle bundle = new Bundle();
            bundle.putInt("updatedIntGridX", (int)(double)gridMap.get("x"));
            bundle.putInt("updatedIntGridY", (int)(double)gridMap.get("y"));
            bundle.putBoolean("boolUpdatedLocation", boolUpdatedLocation);

            if (callback instanceof MainFragment) {
                getTime();
                if(!isNull(base_date) && !isNull(base_time))
                    boolTime = true;
                else
                    Log.e(TAG+"시간업데이트:MainFrag", "시간값을 얻지 못했습니다");

                bundle.putString("updatedBaseDate", base_date);
                bundle.putString("updatedBaseTime", base_time);
                bundle.putBoolean("updatedBoolTime", boolTime);
                mainFragment.setArguments(bundle);
            } else if (callback instanceof MidTermFragment) {
                getTimeThreeHour();
                if(!isNull(base_date) && !isNull(base_time))
                    boolTime = true;
                else
                    Log.e(TAG+"시간업데이트:MidTermFrag", "시간값을 얻지 못했습니다");

                bundle.putString("updatedBaseDate", base_date);
                bundle.putString("updatedBaseTime", base_time);
                bundle.putBoolean("updatedBoolTime", boolTime);
                midTermFragment.setArguments(bundle);
            } else if (callback instanceof WeeklyFragment) {
                getTimeWeekly();
                if(!isNull(announcing_time))
                    boolUpdatedAnnouncingTime = true;
                else
                    Log.e(TAG+"시간업데이트:WeeklyFrag", "시간값을 얻지 못했습니다");

                Bundle weeklyBundle = new Bundle();
                weeklyBundle.putBoolean("boolUpdatedLocation", boolUpdatedLocation);
                weeklyBundle.putString("updatedAnnouncingTime", announcing_time);
                weeklyBundle.putBoolean("boolUpdatedAnnouncingTime", boolUpdatedAnnouncingTime);
                weeklyFragment.setArguments(weeklyBundle);
            }

            requestReverseGeocode(location.getLatitude(), location.getLongitude());
            callback.locationUpdated();     //다형성
            manager.removeUpdates(locatingListener);    //한번만 업데이트 (수동)
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onProviderDisabled(String provider) {}
    }


    public void requestReverseGeocode(double lat, double lng){
        Log.e(TAG+"한글주소요청", "****************************************주소 API 요청 시작");

        new NaverMapRetrofitSetup()
                .getRetrofitInterface()
                .getReverseGeocode("coordsToaddr", lng+","+lat, "addr", "json")
                .enqueue(new Callback<AcceptGeocode>() {
                    @Override
                    public void onResponse(Call<AcceptGeocode> call, Response<AcceptGeocode> response) {
                        Log.e(TAG+"한글주소요청", "****************************************주소 API 요청 성공  " + response.toString());

                        AcceptGeocode acceptGeocode = response.body();
                        ArrayList<Orders> results = acceptGeocode.getResults();
                        Orders orders=null;
                        for (int i = 0; i < results.size(); i++)
                            if(results.get(i).getName().equals("addr")) orders = results.get(i);
                        if(isNull(orders)) {
                            Log.e(TAG+"한글주소요청", "주소 요청 리스트 얻기 실패");
                            return;
                        }
                        Region region = orders.getRegion();
                        Area1 area1 = region.getArea1();
                        Area2 area2 = region.getArea2();
                        if(isNull(area1.getName()) || isNull(area2.getName())) {
                            Log.e(TAG+"한글주소요청", "주소 요청 리스트 아이템 내용 없음");
                            return;
                        }

                        sendReverseGeocode(area1.getName(), area2.getName());
                    }

                    @Override
                    public void onFailure(Call<AcceptGeocode> call, Throwable t) {
                        Log.e(TAG+"한글주소요청", "****************************************주소 API 요청 실패");
                        Log.e(TAG+"한글주소요청", "****************************************"+call.request().toString());
                        Log.e(TAG+"한글주소요청", "****************************************"+t.getLocalizedMessage());
                        Log.e(TAG+"한글주소요청", "****************************************"+t.getMessage());
                    }
                });
    }

    private void sendReverseGeocode(String province, String locality) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("boolUpdatedReverseGeocode", boolUpdatedReverseGeocode);
        bundle.putString("province", province);
        bundle.putString("locality", locality);

        if (callback instanceof MainFragment)
            mainFragment.setArguments(bundle);
        else if (callback instanceof MidTermFragment)
            midTermFragment.setArguments(bundle);
        else if (callback instanceof WeeklyFragment)
            weeklyFragment.setArguments(bundle);

        callback.setKorLocation();      //다형성
    }















    //시간
    private void getTime() {
        Date standardDate = new Date();
        SimpleDateFormat simpleForm = new SimpleDateFormat("HHmm");
        int currentMinutes = standardDate.getMinutes();
        currentTime = simpleForm.format(standardDate);


        if (currentMinutes < 55) standardDate.setTime(standardDate.getTime() - 1000*60*60);
        long standardTime = standardDate.getTime();

        simpleForm.applyPattern("yyyyMMdd");
        base_date = simpleForm.format(standardDate);
        simpleForm.applyPattern("HHmm");
        standardDate.setMinutes(30);
        base_time = simpleForm.format(standardDate);

        standardDate.setTime(standardTime + 1000*60*60);
        currentHour = standardDate.getHours();
        standardDate.setTime(standardTime + 1000*60*60*2);
        nextHour = standardDate.getHours();

        //시간 디버깅
        Log.e("시간", "currentHour = " + currentHour);
        Log.e("시간", "nextHour = " + nextHour);
        Log.e("시간", "currentTime = " + currentTime);
        Log.e("시간", "base_date = " + base_date);
        Log.e("시간", "base_time = " + base_time);
    }

    private void getTimeThreeHour(){
        Date date = new Date();
        int hourOnly = date.getHours();
        long standard_time = date.getTime();

        if(hourOnly >= 0 && hourOnly < 3){
            date.setTime(standard_time - 1000*60*60*4);     // 전날로 바꾸고
            date.setHours(23);                              // 23시로
        }else if(hourOnly >=3 && hourOnly < 6){
            date.setHours(2);
        }else if(hourOnly >=6 && hourOnly < 9){
            date.setHours(5);
        }else if(hourOnly >=9 && hourOnly < 12){
            date.setHours(8);
        }else if(hourOnly >=12 && hourOnly < 15){
            date.setHours(11);
        }else if(hourOnly >=15 && hourOnly < 18){
            date.setHours(14);
        }else if(hourOnly >=18 && hourOnly < 21){
            date.setHours(17);
        }else if(hourOnly >=21 && hourOnly < 24){
            date.setHours(20);
        }
        date.setMinutes(30);

        SimpleDateFormat simpleForm = new SimpleDateFormat("yyyyMMdd");
        base_date = simpleForm.format(date);

        simpleForm.applyPattern("HHmm");
        base_time = simpleForm.format(date);
    }

    private void getTimeWeekly() {
        Date date = new Date();
        long standard_time = date.getTime();
        int hourOnly = date.getHours();

        if(hourOnly >= 0 && hourOnly < 7) {
            date.setTime(standard_time - 1000*60*60*24);
            date.setHours(18);
        }
        else if(hourOnly >= 7 && hourOnly < 19) date.setHours(6);
        else if(hourOnly >= 19 && hourOnly <= 23) date.setHours(18);
        date.setMinutes(0);

        announcing_time = new SimpleDateFormat("yyyyMMddHHmm").format(date);
    }






    public interface OnLocationUpdatedListener {
        void locationUpdated();
        void setKorLocation();
    }

    private boolean isNull(Object obj){
        if(obj==null) return true;
        else return false;
    }


}


class MainBundle implements Parcelable {
    String pcl="pcl1";

    int intGridX;
    int intGridY;

    String base_date;
    String base_time;


    String currentTime;
    int currentHour;
    int nextHour;

    boolean boolPermission;
    boolean boolExistingLocation;
    boolean boolUpdatedLocation ;
    boolean boolTime ;

    public MainBundle() {
    }

    protected MainBundle(Parcel in) {
        String[] data = new String[1];
        in.readStringArray(data);
        this.pcl = data[0];
    }

    public static final Creator<MainBundle> CREATOR = new Creator<MainBundle>() {
        @Override
        public MainBundle createFromParcel(Parcel in) {
            return new MainBundle(in);
        }

        @Override
        public MainBundle[] newArray(int size) {
            return new MainBundle[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] { this.pcl });
    }
}


class ThreeHourBundle implements Parcelable {
    String pcl="pcl1";

    int intGridX;
    int intGridY;

    String base_date;
    String base_time;

    boolean boolPermission;
    boolean boolExistingLocation;
    boolean boolUpdatedLocation ;
    boolean boolTime ;

    public ThreeHourBundle() {
    }

    protected ThreeHourBundle(Parcel in) {
        String[] data = new String[1];
        in.readStringArray(data);
        this.pcl = data[0];
    }

    public static final Creator<MainBundle> CREATOR = new Creator<MainBundle>() {
        @Override
        public MainBundle createFromParcel(Parcel in) {
            return new MainBundle(in);
        }

        @Override
        public MainBundle[] newArray(int size) {
            return new MainBundle[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] { this.pcl });
    }
}