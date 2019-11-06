package com.cs.weather3;

import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cs.weather3.model.weather.AcceptClass;
import com.cs.weather3.model.weather.Body;
import com.cs.weather3.model.weather.HourWeather;
import com.cs.weather3.model.weather.items;
import com.cs.weather3.network.WeatherRetrofitSetup;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainFragment extends android.support.v4.app.Fragment implements MainActivity.OnLocationUpdatedListener, View.OnClickListener {
    //상수, 전역 사용
    private String TAG = "MainFrag-";
    private MainActivity mainActivity;

    private boolean boolPermission;
    private boolean boolExistingLocation;
    private boolean boolUpdatedLocation;
    private boolean boolTime;

    //화면
    private TextView text_currentLocation;
    private Button b_refresh;

    private ImageView img_mainWeather, img_thunder;
    private TextView text_mainWeather;

    private TextView text_currentHour, text_currentTemp, text_currentRain;
    private TextView text_nextHour, text_nextTemp, text_nextRain;

    private TextView text_currentCloud, text_currentRainType, text_currentThunder,
            text_currentHumidity, text_currentWindDirection, text_currentWindSpeed, text_currentHorizontalWind, text_currentVerticalWind;
    private TextView text_nextCloud, text_nextRainType, text_nextThunder,
            text_nextHumidity, text_nextWindDirection, text_nextWindSpeed, text_nextHorizontalWind, text_nextVerticalWind;

    //위치
    private Location targetLocation;
    private LocationManager manager;
    private int intGridX;
    private int intGridY;
    private String province;
    private String locality;

    //시간
    private String currentTime;
    private String base_date;
    private String base_time;
    private int currentHour;
    private int nextHour;

    //날씨
    private HashMap<String, Double> currentHourWeather;
    private HashMap<String, Double> nextHourWeather;


    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if(isNull(bundle)) return;
        MainBundle mainBundle = bundle.getParcelable("MainBundle");
        if(mainBundle == null) return;

        this.intGridX = mainBundle.intGridX;
        this.intGridY = mainBundle.intGridY;
        this.base_date = mainBundle.base_date;
        this.base_time = mainBundle.base_time;
        this.currentTime = mainBundle.currentTime;
        this.currentHour = mainBundle.currentHour;
        this.nextHour = mainBundle.nextHour;

        this.boolPermission = mainBundle.boolPermission;
        this.boolExistingLocation = mainBundle.boolExistingLocation;
        this.boolUpdatedLocation = mainBundle.boolUpdatedLocation;
        this.boolTime = mainBundle.boolTime;

        Log.e(TAG+"초기화", "Main번들로 초기화");
    }









    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //화면
        text_currentLocation = view.findViewById(R.id.text_currentLocation);
        b_refresh = view.findViewById(R.id.b_refresh);

        img_mainWeather = view.findViewById(R.id.img_mainWeather);
        img_thunder = view.findViewById(R.id.img_thunder);
        text_mainWeather = view.findViewById(R.id.text_mainWeather);

        text_currentHour = view.findViewById(R.id.main_text_currentHour);
        text_currentTemp = view.findViewById(R.id.text_currentTemp);
        text_currentRain = view.findViewById(R.id.text_currentRain);

        text_nextHour = view.findViewById(R.id.text_nextHour);
        text_nextTemp = view.findViewById(R.id.text_nextTemp);
        text_nextRain = view.findViewById(R.id.text_nextRain);

        text_currentCloud = view.findViewById(R.id.text_currentCloud);
        text_currentRainType = view.findViewById(R.id.text_currentRainType);
        text_currentThunder = view.findViewById(R.id.text_currentThunder);
        text_currentHumidity = view.findViewById(R.id.text_currentHumidity);
        text_currentWindDirection = view.findViewById(R.id.text_currentWindDirection);
        text_currentWindSpeed = view.findViewById(R.id.text_currentWindSpeed);
        text_currentHorizontalWind = view.findViewById(R.id.text_currentHorizontalWind);
        text_currentVerticalWind = view.findViewById(R.id.text_currentVerticalWind);

        text_nextCloud = view.findViewById(R.id.text_nextCloud);
        text_nextRainType = view.findViewById(R.id.text_nextRainType);
        text_nextThunder = view.findViewById(R.id.text_nextThunder);
        text_nextHumidity = view.findViewById(R.id.text_nextHumidity);
        text_nextWindDirection = view.findViewById(R.id.text_nextWindDirection);
        text_nextWindSpeed = view.findViewById(R.id.text_nextWindSpeed);
        text_nextHorizontalWind = view.findViewById(R.id.text_nextHorizontalWind);
        text_nextVerticalWind = view.findViewById(R.id.text_nextVerticalWind);

        b_refresh.setOnClickListener(this);

        mainActivity.getUpdatedLocation();

        Log.e("boolExistingLocation", ""+boolExistingLocation);
        Log.e("boolUpdatedLocation", ""+boolUpdatedLocation);
        Log.e("boolTime", ""+boolTime);

        //기존 위치로 API 통신 요청
        if(!boolExistingLocation || !boolTime) {
            if (!boolExistingLocation) Log.e(TAG+"동네예보요청", "기존 위치 null");
            if (!boolTime) Log.e(TAG+"동네예보요청", "기존 시간 null");
            return;
        }
        Log.e(TAG+"동네예보요청", "동네예보요청-위치:기존");
        requestHourWeather();       // 통신 요청 시 쓰레드 분기함. onCreate 제어는 별개로 계속 명령어 실행
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.b_refresh:
                mainActivity.getUpdatedLocation();
                break;
        }
    }




    //***통신 작업 - 대기 시간 있기 때문에 함수 연쇄호출함
    private void requestHourWeather() {
        Log.e(TAG+"초단기예보:API요청", "***********************************초단기예보요청 시작");
        new WeatherRetrofitSetup()
                .getRetrofitInterface()
                .getHourWeather(AppConstants.WEATHER_API_KEY, base_date, base_time, intGridX, intGridY, "json", "1", "500")
                .enqueue(new Callback<AcceptClass>() {
                    @Override
                    public void onResponse(Call<AcceptClass> call, Response<AcceptClass> response) {
                        Log.e(TAG+"초단기예보:API요청", "***********************************응답성공  "+response.toString());

                        //** JSON 객체 참조
                        AcceptClass acceptClass = response.body();                              //c: AcceptClass / m: Response
                        com.cs.weather3.model.weather.Response response1 = acceptClass.getResponse();    //c: Response / m: Body
                        Body body = response1.getBody();                                        //c: Body / m: items
                        if(isNull(body)) {
                            Log.e(TAG+"초단기예보:API요청", "body 널값 - 요청파라미터 검사필요 ");
                            return;
                        }
                        items items = body.getItems();                                          //c: items / m: ArrayList item
                        if(!(items.getItem() instanceof ArrayList)) {
                            Log.e(TAG+"초단기예보:API요청", "item null String값 - 시간파라미터 검사필요");
                            return;
                        }
                        ArrayList<HourWeather> ultraShort = items.getItem();                    // 실제데이터: HourWeather 담긴 ArrayList item
                        if (isNull(ultraShort)) {
                            Log.e(TAG+"초단기예보:API요청", "item 널값 - 시간파라미터 검사필요");
                            return;
                        }
                        parseUltraShort(ultraShort);
                    }

                    @Override
                    public void onFailure(Call<AcceptClass> call, Throwable t) {
                        Log.e(TAG+"초단기예보:API요청", "***********************************");
                        Log.e(TAG+"초단기예보:API요청", call.request().toString());
                        Log.e(TAG+"초단기예보:API요청", t.getLocalizedMessage());
                        Log.e(TAG+"초단기예보:API요청", t.getMessage());
                    }
                });
    }

    private void parseUltraShort(ArrayList<HourWeather> ultraShort ) {
        Log.e(TAG+"초단기예보:API데이터처리", "***********************************ultraShort.get(0) = " + ultraShort.get(0));
        int len = ultraShort.size();
        currentHourWeather = new HashMap<>();
        nextHourWeather = new HashMap<>();

        if(len > 0){
            for(int i=0; i<len; i++)
            {
                HourWeather hourWeather = ultraShort.get(i);
                String fcstTime = hourWeather.getFcstTime();
                int fcstHour = Integer.parseInt(fcstTime.substring(0,2));
                if(fcstHour == currentHour){
                    String category = hourWeather.getCategory();
                    double fcstValue = hourWeather.getFcstValue();

                    if(category.equals("T1H")) currentHourWeather.put("temp", fcstValue);
                    else if(category.equals("SKY")) currentHourWeather.put("cloudType", fcstValue);
                    else if(category.equals("PTY")) currentHourWeather.put("rainType", fcstValue);
                    else if(category.equals("RN1")) currentHourWeather.put("rainAmount", fcstValue);
                    else currentHourWeather.put(category, fcstValue);

                    Log.e(TAG+"초단기예보:API데이터처리", "현재시간: category = " + category);
                    Log.e(TAG+"초단기예보:API데이터처리", "현재시간: fcstValue = " + fcstValue);
                }
                else if(fcstHour == nextHour){
                    String category = hourWeather.getCategory();
                    double fcstValue = hourWeather.getFcstValue();

                    if(category.equals("T1H")) nextHourWeather.put("temp", fcstValue);
                    else if(category.equals("SKY")) nextHourWeather.put("cloudType", fcstValue);
                    else if(category.equals("PTY")) nextHourWeather.put("rainType", fcstValue);
                    else if(category.equals("RN1")) nextHourWeather.put("rainAmount", fcstValue);
                    else nextHourWeather.put(category, fcstValue);

                    Log.e(TAG+"초단기예보:API데이터처리", "다음시간: category = " + category);
                    Log.e(TAG+"초단기예보:API데이터처리", "다음시간: fcstValue = " + fcstValue);
                }
                else
                    continue;
            }
        }


        Iterator iterator = currentHourWeather.keySet().iterator();
        while(iterator.hasNext())
            Log.e("Keysets", (String)iterator.next());
        if(currentHourWeather.keySet().size() > 0 && nextHourWeather.keySet().size() > 0) {
            Log.e(TAG+"초단기예보:날씨데이터", "날씨 데이터 얻음");
            viewUltraShort();
        }
        else{
            Log.e(TAG+"초단기예보:날씨데이터", "날씨 데이터 없음");
        }
    }

    //초단기예보
    //View
    private void viewUltraShort(){
        Log.e(TAG+"초단기예보:API뷰적용", "***********************************프래그먼트 컴포넌트에 데이터 적용");
        text_currentHour.setText(currentHour+"시");
        text_nextHour.setText(nextHour+"시");
        Log.e(TAG+"viewHour", "currentHour = " + currentHour);
        Log.e(TAG+"viewHour", "nextHour = " + nextHour);

        //날씨 개요 아이콘 및 텍스트 세팅
        double mainWeather_temp = (double)currentHourWeather.get("temp");
        int mainWeather_rainAmount = (int)(double)currentHourWeather.get("rainAmount");
        int mainWeather_rainType = (int)(double)currentHourWeather.get("rainType");
        int mainWeather_cloudType = (int)(double)currentHourWeather.get("cloudType");
        int mainWeather_thunder = (int)(double)currentHourWeather.get("LGT");
        text_mainWeather.setText("");
        text_mainWeather.append("현재 기온 " + mainWeather_temp + "℃.  ");

        switch (mainWeather_rainType){
            case 0:
                //하늘상태 세팅
                if(mainWeather_cloudType == 1) {
                    img_mainWeather.setImageResource(R.drawable.sunny);          //맑음
                    text_mainWeather.append("하늘 맑음.  ");
                }
                else if(mainWeather_cloudType == 3) {
                    img_mainWeather.setImageResource(R.drawable.cloudy);     //구름 많음
                    text_mainWeather.append("하늘 구름 많음.  ");
                }
                else if(mainWeather_cloudType == 4) {
                    img_mainWeather.setImageResource(R.drawable.cloudy_gray);     //흐림
                    text_mainWeather.append("하늘 흐림.  ");
                }
                else {
                    img_mainWeather.setEnabled(false);
                    img_mainWeather.setContentDescription("기상청 데이터 없음");
                }
                break;
            case 1:
                img_mainWeather.setImageResource(R.drawable.rain);     //비
                text_mainWeather.append("강수 확률 있음.  ");
                break;
            case 2:
                img_mainWeather.setImageResource(R.drawable.sleety);     //비/눈
                text_mainWeather.append("진눈 깨비.  ");
                break;
            case 3:
                img_mainWeather.setImageResource(R.drawable.snow);     //눈
                text_mainWeather.append("강설 확률 있음.  ");
                break;
            case 4:
                img_mainWeather.setImageResource(R.drawable.shower);     //소나기
                text_mainWeather.append("소나기.  ");
                break;
            default:
                img_mainWeather.setEnabled(false);
                img_mainWeather.setContentDescription("기상청 데이터 없음");
                break;
        }
        //낙뢰는 있을 경우에만
        if(mainWeather_thunder == 1) {
            img_thunder.setImageResource(R.drawable.thunder);
            text_mainWeather.append("낮은확률의 낙뢰  ");
        }
        else if(mainWeather_thunder == 2) {
            text_mainWeather.append("보통확률의 낙뢰  ");
        }
        else if(mainWeather_thunder == 3) {
            text_mainWeather.append("높은확률의 낙뢰  ");
        }
        else if(mainWeather_thunder < -900 && mainWeather_thunder > 900) {
            img_mainWeather.setEnabled(false);
            img_mainWeather.setContentDescription("기상청 데이터 없음");
        }


        for (String s : currentHourWeather.keySet()) {
            switch (s){
                case "temp":
                    double currentHourTemp = currentHourWeather.get(s);
                    if(currentHourTemp > -900 && currentHourTemp < 900) text_currentTemp.setText("기온: " + currentHourTemp + "℃");
                    else text_currentTemp.setText("N/A");
                    break;
                case "cloudType":
                    double currentHourCloudType = currentHourWeather.get(s);
                    if(currentHourCloudType > -900 && currentHourCloudType < 900) {
                        if(currentHourCloudType == 1.0) text_currentCloud.setText("하늘상태: 맑음");
                        else if(currentHourCloudType == 3.0) text_currentCloud.setText("하늘상태:구름많음");
                        else if(currentHourCloudType == 4.0) text_currentCloud.setText("하늘상태: 흐림");
                    }
                    else text_currentCloud.setText("N/A");
                    break;
                case "rainType":
                    double currentHourRainType = currentHourWeather.get(s);
                    if(currentHourRainType > -900 && currentHourRainType < 900) {
                        if(currentHourRainType == 0.0) text_currentRainType.setText("강수형태: 없음");
                        else if(currentHourRainType == 1.0) text_currentRainType.setText("강수형태: 비");
                        else if(currentHourRainType == 2.0) text_currentRainType.setText("강수형태: 비/눈");
                        else if(currentHourRainType == 3.0) text_currentRainType.setText("강수형태: 눈");
                        else if(currentHourRainType == 4.0) text_currentRainType.setText("강수형태: 소나기");
                    }
                    else text_currentRainType.setText("N/A");
                    break;
                case "rainAmount":
                    double currentHourRainAmount = currentHourWeather.get(s);
                    if(currentHourRainAmount > -900 && currentHourRainAmount < 900) {
                        if(currentHourRainAmount == 0) text_currentRain.setText("강수량: 없음");
                        else if(currentHourRainAmount > 0 && currentHourRainAmount <= 1) text_currentRain.setText("강수량:1mm미만");
                        else if(currentHourRainAmount > 1 && currentHourRainAmount <= 5) text_currentRain.setText("강수량:1~4mm");
                        else if(currentHourRainAmount > 5 && currentHourRainAmount <= 10) text_currentRain.setText("강수량:5~9mm");
                        else if(currentHourRainAmount > 10 && currentHourRainAmount <= 20) text_currentRain.setText("강수량:10~19mm");
                        else if(currentHourRainAmount > 20 && currentHourRainAmount <= 40) text_currentRain.setText("강수량:20~39mm");
                        else if(currentHourRainAmount > 40 && currentHourRainAmount <= 70) text_currentRain.setText("강수량:40~69mm");
                        else if(currentHourRainAmount > 70 && currentHourRainAmount <= 100) text_currentRain.setText("강수량:70~100mm");
                        else if(currentHourRainAmount > 100) text_currentRain.setText("강수량:100mm이상");
                    }
                    else text_currentRain.setText("N/A");
                    break;
                case "LGT":
                    double currentHourThunder = currentHourWeather.get(s);
                    if(currentHourThunder > -900 && currentHourThunder < 900) {
                        if(currentHourThunder == 0.0) text_currentThunder.setText("낙뢰: 확률없음");
                        else if(currentHourThunder == 1.0) text_currentThunder.setText("낙뢰: 낮음");
                        else if(currentHourThunder == 2.0) text_currentThunder.setText("낙뢰: 보통");
                        else if(currentHourThunder == 3.0) text_currentThunder.setText("낙뢰: 높음");
                    }
                    else text_currentThunder.setText("N/A");
                    break;
                case "REH":
                    double currentHourHumidity = currentHourWeather.get(s);
                    if(currentHourHumidity > -900 && currentHourHumidity < 900) text_currentHumidity.setText("습도: " + currentHourHumidity + "%");
                    else text_currentHumidity.setText("N/A");
                    break;
                case "VEC":
                    double currentHourWindDirection = currentHourWeather.get(s);
                    if(currentHourWindDirection > -900 && currentHourWindDirection < 900){
                        if(currentHourWindDirection >= 0.0 && currentHourWindDirection < 45.0) text_currentWindDirection.setText("풍향: N-NE");
                        else if(currentHourWindDirection >= 45.0 && currentHourWindDirection < 90.0) text_currentWindDirection.setText("풍향: NE-E");
                        else if(currentHourWindDirection >= 90.0 && currentHourWindDirection < 135.0) text_currentWindDirection.setText("풍향: E-SE");
                        else if(currentHourWindDirection >= 135.0 && currentHourWindDirection < 180.0) text_currentWindDirection.setText("풍향: SE-S");
                        else if(currentHourWindDirection >= 180.0 && currentHourWindDirection < 225.0) text_currentWindDirection.setText("풍향: S-SW");
                        else if(currentHourWindDirection >= 225.0 && currentHourWindDirection < 270.0) text_currentWindDirection.setText("풍향: SW-W");
                        else if(currentHourWindDirection >= 270.0 && currentHourWindDirection < 315.0) text_currentWindDirection.setText("풍향: W-NW");
                        else if(currentHourWindDirection >= 315.0 && currentHourWindDirection <= 360.0) text_currentWindDirection.setText("풍향: NW-N");
                    }
                    else text_currentWindDirection.setText("N/A");
                    break;
                case "WSD":
                    double currentHourWindSpeed = currentHourWeather.get(s);
                    if(currentHourWindSpeed > -900 && currentHourWindSpeed < 900) text_currentWindSpeed.setText("풍속: " + currentHourWindSpeed);
                    else text_currentWindSpeed.setText("N/A");
                    break;
                case "UUU":
                    double currentHourHorizontalWind = currentHourWeather.get(s);
                    if(currentHourHorizontalWind > -900 && currentHourHorizontalWind < 900) {
                        if(currentHourHorizontalWind > 0) text_currentHorizontalWind.setText("수평바람: 동쪽 " + currentHourHorizontalWind + "㎧");
                        else if(currentHourHorizontalWind < 0) text_currentHorizontalWind.setText("수평바람: 서쪽 " + (-currentHourHorizontalWind) + "㎧");
                        else text_currentHorizontalWind.setText("수평바람: 평형");
                    }
                    else text_currentHorizontalWind.setText("N/A");
                    break;
                case "VVV":
                    double currentHourVerticalWind = currentHourWeather.get(s);
                    if(currentHourVerticalWind > -900 && currentHourVerticalWind < 900) {
                        if(currentHourVerticalWind > 0) text_currentVerticalWind.setText("수직바람: 북쪽 " + currentHourVerticalWind + "㎧");
                        else if(currentHourVerticalWind < 0) text_currentVerticalWind.setText("수직바람: 남쪽 " + (-currentHourVerticalWind) + "㎧");
                        else text_currentVerticalWind.setText("수직바람: 평형");
                    }
                    else text_currentVerticalWind.setText("N/A");
                    break;
            }
        }

        for (String s : nextHourWeather.keySet()) {
            switch (s){
                case "temp":
                    double nextHourTemp = nextHourWeather.get(s);
                    if(nextHourTemp > -900 && nextHourTemp < 900) text_nextTemp.setText("기온: " + nextHourTemp + "℃");
                    else text_nextTemp.setText("N/A");
                    break;
                case "cloudType":
                    double nextHourCloudType = nextHourWeather.get(s);
                    if(nextHourCloudType > -900 && nextHourCloudType < 900) {
                        if(nextHourCloudType == 1.0) text_nextCloud.setText("하늘상태: 맑음");
                        else if(nextHourCloudType == 3.0) text_nextCloud.setText("하늘상태:구름많음");
                        else if(nextHourCloudType == 4.0) text_nextCloud.setText("하늘상태: 흐림");
                    }
                    else text_nextCloud.setText("N/A");
                    break;
                case "rainType":
                    double nextHourRainType = nextHourWeather.get(s);
                    if(nextHourRainType > -900 && nextHourRainType < 900) {
                        if(nextHourRainType == 0.0) text_nextRainType.setText("강수형태: 없음");
                        else if(nextHourRainType == 1.0) text_nextRainType.setText("강수형태: 비");
                        else if(nextHourRainType == 2.0) text_nextRainType.setText("강수형태: 비/눈");
                        else if(nextHourRainType == 3.0) text_nextRainType.setText("강수형태: 눈");
                        else if(nextHourRainType == 4.0) text_nextRainType.setText("강수형태: 소나기");
                    }
                    else text_nextRainType.setText("N/A");
                    break;
                case "rainAmount":
                    double nextHourRainAmount = nextHourWeather.get(s);
                    if(nextHourRainAmount > -900 && nextHourRainAmount < 900) {
                        if(nextHourRainAmount == 0.0) text_nextRain.setText("강수량: 없음");
                        else if(nextHourRainAmount > 0 && nextHourRainAmount <= 1) text_nextRain.setText("강수량:1mm미만");
                        else if(nextHourRainAmount > 1 && nextHourRainAmount <= 5) text_nextRain.setText("강수량:1~4mm");
                        else if(nextHourRainAmount > 5 && nextHourRainAmount <= 10) text_nextRain.setText("강수량:5~9mm");
                        else if(nextHourRainAmount > 10 && nextHourRainAmount <= 20) text_nextRain.setText("강수량:10~19mm");
                        else if(nextHourRainAmount > 20 && nextHourRainAmount <= 40) text_nextRain.setText("강수량:20~39mm");
                        else if(nextHourRainAmount > 40 && nextHourRainAmount <= 70) text_nextRain.setText("강수량:40~69mm");
                        else if(nextHourRainAmount > 70 && nextHourRainAmount <= 100) text_nextRain.setText("강수량:70~100mm");
                        else if(nextHourRainAmount > 100) text_nextRain.setText("강수량:100mm이상");
                    }
                    else text_nextRain.setText("N/A");
                    break;
                case "LGT":
                    double nextHourThunder = nextHourWeather.get(s);
                    if(nextHourThunder > -900 && nextHourThunder < 900) {
                        if(nextHourThunder == 0.0) text_nextThunder.setText("낙뢰: 확률없음");
                        else if(nextHourThunder == 1.0) text_nextThunder.setText("낙뢰: 낮음");
                        else if(nextHourThunder == 2.0) text_nextThunder.setText("낙뢰: 보통");
                        else if(nextHourThunder == 3.0) text_nextThunder.setText("낙뢰: 높음");
                    }
                    else text_nextThunder.setText("N/A");
                    break;
                case "REH":
                    double nextHourHumidity = nextHourWeather.get(s);
                    if(nextHourHumidity > -900 && nextHourHumidity < 900) text_nextHumidity.setText("습도: " + nextHourHumidity + "%");
                    else text_nextHumidity.setText("N/A");
                    break;
                case "VEC":
                    double nextHourWindDirection = nextHourWeather.get(s);
                    if(nextHourWindDirection > -900 && nextHourWindDirection < 900){
                        if(nextHourWindDirection >= 0.0 && nextHourWindDirection < 45.0) text_nextWindDirection.setText("풍향: N-NE");
                        else if(nextHourWindDirection >= 45.0 && nextHourWindDirection < 90.0) text_nextWindDirection.setText("풍향: NE-E");
                        else if(nextHourWindDirection >= 90.0 && nextHourWindDirection < 135.0) text_nextWindDirection.setText("풍향: E-SE");
                        else if(nextHourWindDirection >= 135.0 && nextHourWindDirection < 180.0) text_nextWindDirection.setText("풍향: SE-S");
                        else if(nextHourWindDirection >= 180.0 && nextHourWindDirection < 225.0) text_nextWindDirection.setText("풍향: S-SW");
                        else if(nextHourWindDirection >= 225.0 && nextHourWindDirection < 270.0) text_nextWindDirection.setText("풍향: SW-W");
                        else if(nextHourWindDirection >= 270.0 && nextHourWindDirection < 315.0) text_nextWindDirection.setText("풍향: W-NW");
                        else if(nextHourWindDirection >= 315.0 && nextHourWindDirection <= 360.0) text_nextWindDirection.setText("풍향: NW-N");
                    }
                    else text_nextWindDirection.setText("N/A");
                    break;
                case "WSD":
                    double nextHourWindSpeed = nextHourWeather.get(s);
                    if(nextHourWindSpeed > -900 && nextHourWindSpeed < 900) text_nextWindSpeed.setText("풍속: " + nextHourWindSpeed);
                    else text_nextWindSpeed.setText("N/A");
                    break;
                case "UUU":
                    double nextHourHorizontalWind = nextHourWeather.get(s);
                    if(nextHourHorizontalWind > -900 && nextHourHorizontalWind < 900) {
                        if(nextHourHorizontalWind > 0) text_nextHorizontalWind.setText("수평바람: 동쪽 " + nextHourHorizontalWind + "㎧");
                        else if(nextHourHorizontalWind < 0) text_nextHorizontalWind.setText("수평바람: 서쪽 " + (-nextHourHorizontalWind) + "㎧");
                        else text_nextHorizontalWind.setText("수평바람: 평형");
                    }
                    else text_nextHorizontalWind.setText("N/A");
                    break;
                case "VVV":
                    double nextHourVerticalWind = nextHourWeather.get(s);
                    if(nextHourVerticalWind > -900 && nextHourVerticalWind < 900) {
                        if(nextHourVerticalWind > 0) text_nextVerticalWind.setText("수직바람: 북쪽 " + nextHourVerticalWind + "㎧");
                        else if(nextHourVerticalWind < 0) text_nextVerticalWind.setText("수직바람: 남쪽 " + (-nextHourVerticalWind) + "㎧");
                        else text_nextVerticalWind.setText("수직바람: 평형");
                    }
                    else text_nextVerticalWind.setText("N/A");
                    break;
            }
        }
    }








    //etc
    private boolean isNull(Object obj){
        if(obj==null) return true;
        else return false;
    }





    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    /**
     * public interface인 리스너를 메인액티비티에 구현, 재정의 (static아니어도 가능, 범용성)
     * 메인액티비티의 context를 프래그먼트에 전달 (메인의 onAttachFragment / 프래그먼트의 onAttach 이용)
     * 프래그먼트에서 'callback'으로 onMainFragmentInteraction을 사용하면
     * 메인액티비티가 파라미터값을 전달받고, 재정의한 함수를 실행하는 효과 -> 데이터 전달 & 로직 처리
     */
    public void setMainActivity(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    @Override
    public void locationUpdated() {
        Log.e(TAG+"위치업데이트", "시작");
        Bundle bundle = getArguments();
        if (isNull(bundle)) {
            Log.e(TAG + "위치업데이트", "번들 널값");
            return;
        }
        boolUpdatedLocation = bundle.getBoolean("boolUpdatedLocation");
        intGridX = bundle.getInt("updatedIntGridX");
        intGridY = bundle.getInt("updatedIntGridY");
        boolTime = bundle.getBoolean("updatedBoolTime");
        base_date = bundle.getString("updatedBaseDate");
        base_time = bundle.getString("updatedBaseTime");
        if(boolTime) {
            requestHourWeather();       // 통신 요청 시 쓰레드 분기함. onCreate 제어는 별개로 계속 명령어 실행
            Log.e(TAG+"초단기예보요청", "초단기예보요청-위치업데이트");
        }
        Log.e(TAG+"위치업데이트", "끝");
    }

    @Override
    public void setKorLocation() {
        Bundle bundle = getArguments();
        if(isNull(bundle)) {
            Log.e(TAG+"주소전달", "번들 널값");
            return;
        }

        province = bundle.getString("province");
        locality = bundle.getString("locality");
        Log.e(TAG+"한글주소저장", province + " " + locality);
        if(isNull(province) || isNull(locality)) {
            Log.e(TAG+"주소전달", "한글 주소 데이터 전달값 null");
            return;
        }
        StringTokenizer st = new StringTokenizer(locality);
        if(st.hasMoreTokens()) text_currentLocation.setText(st.nextToken());
    }

}
