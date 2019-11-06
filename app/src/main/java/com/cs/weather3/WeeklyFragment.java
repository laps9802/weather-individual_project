package com.cs.weather3;

import android.graphics.Color;
import android.graphics.Paint;
import android.location.Address;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.cs.weather3.model.midtermprospect.AcceptProspect;
import com.cs.weather3.model.weeklylandforecast.AcceptLand;
import com.cs.weather3.model.weeklylandforecast.Body;
import com.cs.weather3.model.weeklylandforecast.item;
import com.cs.weather3.model.weeklylandforecast.items;
import com.cs.weather3.model.weeklytempforecast.AcceptTemp;
import com.cs.weather3.network.WeatherRetrofitSetup;
import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class WeeklyFragment extends Fragment implements MainActivity.OnLocationUpdatedListener, View.OnClickListener{
    //상수, 전역 사용
    private final String TAG = "WeeklyFrag-";
    private MainActivity mainActivity;

    private boolean boolPermission;
    private boolean boolExistingLocation;
    private boolean boolUpdatedLocation;
    private boolean boolAnnouncingTime;
    private boolean boolUpdatedAnnouncingTime;
    private boolean boolUpdatedReverseGeocode;

    private String midTermLandCode;
    private String midTermTempCode;
    private String midTermWeatherProspectCode;

    //화면
    TextView weekly_text_currentLocation;
    Button weekly_b_refresh;
    TextView weekly_text_midTermProspect;

    //그래프
    private CandleStickChart weekly_chart_temp;
    private ArrayList<CandleEntry> entries;
    private CandleDataSet set1;
    private CandleData data;

    private RecyclerView weekly_recycler_dateSkyRainPercent;

    //위치
    private double doubleLat;
    private double doubleLng;
    private String province;
    private String locality;

    //시간
    private String announcing_time;

    public WeeklyFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if(isNull(bundle)) return;
        boolExistingLocation = bundle.getBoolean("boolExistingLocation");
        doubleLat = bundle.getDouble("doubleLat");
        doubleLng = bundle.getDouble("doubleLng");
        boolAnnouncingTime = bundle.getBoolean("boolAnnouncingTime");
        announcing_time = bundle.getString("announcing_time");

        Log.e(TAG+"초기화", "Weekly번들 초기화");
    }









    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_weekly, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        weekly_text_currentLocation = v.findViewById(R.id.weekly_text_currentLocation);
        weekly_b_refresh = v.findViewById(R.id.weekly_b_refresh);
        weekly_chart_temp = v.findViewById(R.id.weekly_chart_temp);
        weekly_text_midTermProspect = v.findViewById(R.id.weekly_text_midTermProspect);
        weekly_recycler_dateSkyRainPercent = v.findViewById(R.id.weekly_recycler_dateSkyRainPercent);

        //스크롤바 무브먼트 설정
        weekly_text_midTermProspect.setMovementMethod(new ScrollingMovementMethod());
//        weekly_text_midTermProspect.setMaxHeight(10);
//        weekly_text_midTermProspect.setVerticalScrollBarEnabled(true);

        weekly_b_refresh.setOnClickListener(this);


        mainActivity.getUpdatedLocation();

        if(!boolExistingLocation || !boolAnnouncingTime) {
            if (!boolExistingLocation) Log.e(TAG+"한글주소요청", "기존 위치 null");
            if (!boolAnnouncingTime) Log.e(TAG+"한글주소요청", "기존 시간 null");
            return;
        }
        mainActivity.requestReverseGeocode(doubleLat, doubleLng);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.weekly_b_refresh:
                mainActivity.getUpdatedLocation();
                break;
        }
    }
















    private void requestWeeklyLandWeather() {
        Log.e(TAG+"Land", "***********************************통신시작");
        new WeatherRetrofitSetup()
                .getRetrofitInterface()
                .getWeeklyLandWeather(AppConstants.WEATHER_API_KEY, midTermLandCode, announcing_time, "json", "1", "500")
                .enqueue(new Callback<AcceptLand>() {
                    @Override
                    public void onResponse(Call<AcceptLand> call, Response<AcceptLand> response) {
                        Log.e(TAG+"Land:API요청", "***********************************응답성공  " + response.toString());

                        AcceptLand acceptLand = response.body();
                        com.cs.weather3.model.weeklylandforecast.Response response1 = acceptLand.getResponse();
                        Body body = response1.getBody();
                        if(isNull(body)) {
                            Log.e(TAG+"Land:API요청", "body 널값 - 요청파라미터 검사 필요");
                            return;
                        }
                        items items = body.getItems();
                        if(!(items.getItem() instanceof item)) {
                            Log.e(TAG+"Land:API요청", "item null String값 - 시간파라미터 검사필요");
                            return;
                        }
                        item item = items.getItem();
                        if(isNull(item)) {
                            Log.e(TAG+"Land:API요청", "item 널값 - 시간파라미터 검사필요");
                            return;
                        }
                        Log.e(TAG+"Land:API요청", "***********************************데이터 클래스 획득");
                        parseWeeklyLandWeather(item);
                    }

                    @Override
                    public void onFailure(Call<AcceptLand> call, Throwable t) {
                        Log.e(TAG+"Land:API요청", "***********************************응답실패");
                        Log.e(TAG+"Land:API요청", call.request().toString());
                        Log.e(TAG+"Land:API요청", t.getLocalizedMessage());
                        Log.e(TAG+"Land:API요청", t.getMessage());
                    }
                });
    }

    private void parseWeeklyLandWeather(item item) {
        Log.e(TAG+"Land:데이터처리", "***********************************데이터 처리 시작");

        Date dateInstance = new Date();
        ArrayList<String> dateList = new ArrayList();

        dateInstance.setTime(dateInstance.getTime() + 1000*60*60*24*3);
        dateList.add(dateInstance.getDate()+"일");
        dateInstance.setTime(dateInstance.getTime() + 1000*60*60*24);
        dateList.add(dateInstance.getDate()+"일");
        dateInstance.setTime(dateInstance.getTime() + 1000*60*60*24);
        dateList.add(dateInstance.getDate()+"일");
        dateInstance.setTime(dateInstance.getTime() + 1000*60*60*24);
        dateList.add(dateInstance.getDate()+"일");
        dateInstance.setTime(dateInstance.getTime() + 1000*60*60*24);
        dateList.add(dateInstance.getDate()+"일");
        dateInstance.setTime(dateInstance.getTime() + 1000*60*60*24);
        dateList.add(dateInstance.getDate()+"일");
        dateInstance.setTime(dateInstance.getTime() + 1000*60*60*24);
        dateList.add(dateInstance.getDate()+"일");
        dateInstance.setTime(dateInstance.getTime() + 1000*60*60*24);
        dateList.add(dateInstance.getDate()+"일");

        ArrayList<HashMap<String, String>> landList = new ArrayList<>();

        HashMap<String, String> landMap3 = new HashMap<>();
        landMap3.put("date", dateList.get(0));
        landMap3.put("rainPercent", item.getRnSt3Am()+"");
        landMap3.put("pmRainPercent", item.getRnSt3Pm()+"");
        landMap3.put("sky", item.getWf3Am()+"");
        landMap3.put("pmSky", item.getWf3Pm()+"");
        landList.add(landMap3);

        HashMap<String, String> landMap4 = new HashMap<>();
        landMap4.put("date", dateList.get(1));
        landMap4.put("rainPercent", item.getRnSt4Am()+"");
        landMap4.put("pmRainPercent", item.getRnSt4Pm()+"");
        landMap4.put("sky", item.getWf4Am()+"");
        landMap4.put("pmSky", item.getWf4Pm()+"");
        landList.add(landMap4);

        HashMap<String, String> landMap5 = new HashMap<>();
        landMap5.put("date", dateList.get(2));
        landMap5.put("rainPercent", item.getRnSt5Am()+"");
        landMap5.put("pmRainPercent", item.getRnSt5Pm()+"");
        landMap5.put("sky", item.getWf5Am()+"");
        landMap5.put("pmSky", item.getWf5Pm()+"");
        landList.add(landMap5);

        HashMap<String, String> landMap6 = new HashMap<>();
        landMap6.put("date", dateList.get(3));
        landMap6.put("rainPercent", item.getRnSt6Am()+"");
        landMap6.put("pmRainPercent", item.getRnSt6Pm()+"");
        landMap6.put("sky", item.getWf6Am()+"");
        landMap6.put("pmSky", item.getWf6Pm()+"");
        landList.add(landMap6);

        HashMap<String, String> landMap7 = new HashMap<>();
        landMap7.put("date", dateList.get(4));
        landMap7.put("rainPercent", item.getRnSt7Am()+"");
        landMap7.put("pmRainPercent", item.getRnSt7Pm()+"");
        landMap7.put("sky", item.getWf7Am()+"");
        landMap7.put("pmSky", item.getWf7Pm()+"");
        landList.add(landMap7);

        HashMap<String, String> landMap8 = new HashMap<>();
        landMap8.put("date", dateList.get(5));
        landMap8.put("rainPercent", item.getRnSt8()+"");
        landMap8.put("sky", item.getWf8()+"");
        landList.add(landMap8);

        HashMap<String, String> landMap9 = new HashMap<>();
        landMap9.put("date", dateList.get(6));
        landMap9.put("rainPercent", item.getRnSt9()+"");
        landMap9.put("sky", item.getWf9()+"");
        landList.add(landMap9);

        HashMap<String, String> landMap10 = new HashMap<>();
        landMap10.put("date", dateList.get(7));
        landMap10.put("rainPercent", item.getRnSt10()+"");
        landMap10.put("sky", item.getWf10()+"");
        landList.add(landMap10);

        WeeklyRecyclerAdapter adapter = new WeeklyRecyclerAdapter(landList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        weekly_recycler_dateSkyRainPercent.setAdapter(adapter);
        weekly_recycler_dateSkyRainPercent.setLayoutManager(layoutManager);
        weekly_recycler_dateSkyRainPercent.setOverScrollMode(View.OVER_SCROLL_NEVER);
    }

    class WeeklyRecyclerAdapter extends RecyclerView.Adapter<WeeklyRecyclerAdapter.ViewHolder>{
        ArrayList<HashMap<String, String>> itemList;

        public WeeklyRecyclerAdapter(ArrayList<HashMap<String, String>> itemList) {
            this.itemList = itemList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.weekly_item, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
            HashMap<String, String> itemMap = itemList.get(position);

            //초기화
            viewHolder.weeklyItem_text_pmRainPercent.setText("");
            viewHolder.weeklyItem_img_pmSky.setImageResource(android.R.color.transparent);


            viewHolder.weeklyItem_text_date.setText(itemMap.get("date"));
            viewHolder.weeklyItem_text_amRainPercent.setText(itemMap.get("rainPercent")+"%");
            String amSky = itemMap.get("sky");
            if(amSky.equals("맑음")){
                viewHolder.weeklyItem_img_amSky.setImageResource(R.drawable.sunny);
            }else if(amSky.equals("구름많음")){
                viewHolder.weeklyItem_img_amSky.setImageResource(R.drawable.weeklyitem_cloud);
            }else if(amSky.startsWith("구")){
                if(amSky.endsWith("비/눈")){
                    viewHolder.weeklyItem_img_amSky.setImageResource(R.drawable.weeklyitem_sleet);
                }else if(amSky.endsWith("눈/비")){
                    viewHolder.weeklyItem_img_amSky.setImageResource(R.drawable.weeklyitem_sleet);
                }else if(amSky.endsWith("비")){
                    viewHolder.weeklyItem_img_amSky.setImageResource(R.drawable.weeklyitem_rain);
                }else if(amSky.endsWith("눈")){
                    viewHolder.weeklyItem_img_amSky.setImageResource(R.drawable.weeklyitem_snow);
                }
            }else if(amSky.equals("흐림")){
                viewHolder.weeklyItem_img_amSky.setImageResource(R.drawable.weeklyitem_gray);
            }else if(amSky.startsWith("흐")){
                if(amSky.endsWith("비/눈")){
                    viewHolder.weeklyItem_img_amSky.setImageResource(R.drawable.weeklyitem_graysleet);
                }else if(amSky.endsWith("눈/비")){
                    viewHolder.weeklyItem_img_amSky.setImageResource(R.drawable.weeklyitem_graysleet);
                }else if(amSky.endsWith("비")){
                    viewHolder.weeklyItem_img_amSky.setImageResource(R.drawable.weeklyitem_grayrain);
                }else if(amSky.endsWith("눈")){
                    viewHolder.weeklyItem_img_amSky.setImageResource(R.drawable.weeklyitem_graysnow);
                }
            }

            //7일 후 까진 오전/오후 데이터 있음
            if(position < 5) {
                viewHolder.weeklyItem_text_pmRainPercent.setText(itemMap.get("pmRainPercent")+"%");

                String pmSky = itemMap.get("pmSky");
                if (pmSky.equals("맑음")) {
                    viewHolder.weeklyItem_img_pmSky.setImageResource(R.drawable.sunny);
                } else if (pmSky.equals("구름많음")) {
                    viewHolder.weeklyItem_img_pmSky.setImageResource(R.drawable.weeklyitem_cloud);
                } else if (pmSky.startsWith("구")) {
                    if (pmSky.endsWith("비/눈")) {
                        viewHolder.weeklyItem_img_pmSky.setImageResource(R.drawable.weeklyitem_sleet);
                    } else if (pmSky.endsWith("눈/비")) {
                        viewHolder.weeklyItem_img_pmSky.setImageResource(R.drawable.weeklyitem_sleet);
                    } else if (pmSky.endsWith("비")) {
                        viewHolder.weeklyItem_img_pmSky.setImageResource(R.drawable.weeklyitem_rain);
                    } else if (pmSky.endsWith("눈")) {
                        viewHolder.weeklyItem_img_pmSky.setImageResource(R.drawable.weeklyitem_snow);
                    }
                } else if (pmSky.equals("흐림")) {
                    viewHolder.weeklyItem_img_pmSky.setImageResource(R.drawable.weeklyitem_gray);
                } else if (pmSky.startsWith("흐")) {
                    if (pmSky.endsWith("비/눈")) {
                        viewHolder.weeklyItem_img_pmSky.setImageResource(R.drawable.weeklyitem_graysleet);
                    } else if (pmSky.endsWith("눈/비")) {
                        viewHolder.weeklyItem_img_pmSky.setImageResource(R.drawable.weeklyitem_graysleet);
                    } else if (pmSky.endsWith("비")) {
                        viewHolder.weeklyItem_img_pmSky.setImageResource(R.drawable.weeklyitem_grayrain);
                    } else if (pmSky.endsWith("눈")) {
                        viewHolder.weeklyItem_img_pmSky.setImageResource(R.drawable.weeklyitem_graysnow);
                    }
                }
            }
        }

        @Override
        public int getItemCount() {
            return itemList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder{
            TextView weeklyItem_text_date;
            ImageView weeklyItem_img_amSky, weeklyItem_img_pmSky;
            TextView weeklyItem_text_amRainPercent, weeklyItem_text_pmRainPercent;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                weeklyItem_text_date = itemView.findViewById(R.id.weeklyItem_text_date);
                weeklyItem_img_amSky = itemView.findViewById(R.id.weeklyItem_img_amSky);
                weeklyItem_img_pmSky = itemView.findViewById(R.id.weeklyItem_img_pmSky);
                weeklyItem_text_amRainPercent = itemView.findViewById(R.id.weeklyItem_text_amRainPercent);
                weeklyItem_text_pmRainPercent = itemView.findViewById(R.id.weeklyItem_text_pmRainPercent);
            }
        }
    }



    private void requestWeeklyTempWeather() {
        Log.e(TAG+"Temp:API요청", "***********************************통신시작");
        new WeatherRetrofitSetup()
                .getRetrofitInterface()
                .getWeeklyTempWeather(AppConstants.WEATHER_API_KEY, midTermTempCode, announcing_time, "json", "1", "500")
                .enqueue(new Callback<AcceptTemp>() {
                    @Override
                    public void onResponse(Call<AcceptTemp> call, Response<AcceptTemp> response) {
                        Log.e(TAG+"Temp:API요청", "***********************************응답성공  " + response.toString());

                        AcceptTemp acceptTemp = response.body();
                        com.cs.weather3.model.weeklytempforecast.Response response1 = acceptTemp.getResponse();
                        com.cs.weather3.model.weeklytempforecast.Body body = response1.getBody();
                        if(isNull(body)) {
                            Log.e(TAG+"Temp:API요청", "body 널값 - 요청파라미터 검사필요");
                            return;
                        }
                        com.cs.weather3.model.weeklytempforecast.items items = body.getItems();
                        if(!(items.getItem() instanceof com.cs.weather3.model.weeklytempforecast.item)){
                            Log.e(TAG+"Temp:API요청", "item null String값 - 시간파라미터 검사필요");
                            return;
                        }
                        com.cs.weather3.model.weeklytempforecast.item item = items.getItem();
                        if(isNull(item)) {
                            Log.e(TAG+"Temp:API요청", "item 널값 - 시간파라미터 검사필요");
                            return;
                        }
                        parseWeeklyTempWeather(item);
                    }

                    @Override
                    public void onFailure(Call<AcceptTemp> call, Throwable t) {
                        Log.e(TAG+"Temp:API요청", "***********************************응답실패");
                        Log.e(TAG+"Temp:API요청", call.request().toString());
                        Log.e(TAG+"Temp:API요청", t.getLocalizedMessage());
                        Log.e(TAG+"Temp:API요청", t.getMessage());
                    }
                });
    }

    private void parseWeeklyTempWeather(com.cs.weather3.model.weeklytempforecast.item item) {
        Log.e(TAG+"Temp:데이터처리", "***********************************");
        Log.e(TAG+"Temp:뷰적용", "***********************************");

        Log.e(TAG+"Temp", "item.getTaMax3() = " + item.getTaMax3());
        Log.e(TAG+"Temp", "item.getTaMax4() = " + item.getTaMax4());
        Log.e(TAG+"Temp", "item.getTaMax5() = " + item.getTaMax5());
        Log.e(TAG+"Temp", "item.getTaMax6() = " + item.getTaMax6());
        Log.e(TAG+"Temp", "item.getTaMax7() = " + item.getTaMax7());
        Log.e(TAG+"Temp", "item.getTaMax8() = " + item.getTaMax8());
        Log.e(TAG+"Temp", "item.getTaMax9() = " + item.getTaMax9());
        Log.e(TAG+"Temp", "item.getTaMax10() = " + item.getTaMax10());

        Date date = new Date();
        ArrayList<String> day = new ArrayList();
        // Entry
        entries = new ArrayList<>();
        date.setTime(date.getTime() + 1000*60*60*24*3);
        day.add(date.getDate()+"일");
        entries.add(new CandleEntry(0, item.getTaMax3(), item.getTaMin3(), item.getTaMin3(), item.getTaMax3()));       //shadow: 막대그래프 관통선 범위, open-close: 막대그래프 범위
        date.setTime(date.getTime() + 1000*60*60*24);
        day.add(date.getDate()+"일");
        entries.add(new CandleEntry(1, item.getTaMax4(), item.getTaMin4(), item.getTaMin4(), item.getTaMax4()));
        date.setTime(date.getTime() + 1000*60*60*24);
        day.add(date.getDate()+"일");
        entries.add(new CandleEntry(2, item.getTaMax5(), item.getTaMin5(), item.getTaMin5(), item.getTaMax5()));
        date.setTime(date.getTime() + 1000*60*60*24);
        day.add(date.getDate()+"일");
        entries.add(new CandleEntry(3, item.getTaMax6(), item.getTaMin6(), item.getTaMin6(), item.getTaMax6()));
        date.setTime(date.getTime() + 1000*60*60*24);
        day.add(date.getDate()+"일");
        entries.add(new CandleEntry(4, item.getTaMax7(), item.getTaMin7(), item.getTaMin7(), item.getTaMax7()));
        date.setTime(date.getTime() + 1000*60*60*24);
        day.add(date.getDate()+"일");
        entries.add(new CandleEntry(5, item.getTaMax8(), item.getTaMin8(), item.getTaMin8(), item.getTaMax8()));
        date.setTime(date.getTime() + 1000*60*60*24);
        day.add(date.getDate()+"일");
        entries.add(new CandleEntry(6, item.getTaMax9(), item.getTaMin9(), item.getTaMin9(), item.getTaMax9()));
        date.setTime(date.getTime() + 1000*60*60*24);
        day.add(date.getDate()+"일");
        entries.add(new CandleEntry(7, item.getTaMax10(), item.getTaMin10(), item.getTaMin10(), item.getTaMax10()));

        ArrayList<Integer> tempMaxList = new ArrayList<>();
        tempMaxList.add(item.getTaMax3());
        tempMaxList.add(item.getTaMax4());
        tempMaxList.add(item.getTaMax5());
        tempMaxList.add(item.getTaMax6());
        tempMaxList.add(item.getTaMax7());
        tempMaxList.add(item.getTaMax8());
        tempMaxList.add(item.getTaMax9());
        tempMaxList.add(item.getTaMax10());

        ArrayList<Integer> tempMinList = new ArrayList<>();
        tempMinList.add(item.getTaMin3());
        tempMinList.add(item.getTaMin4());
        tempMinList.add(item.getTaMin5());
        tempMinList.add(item.getTaMin6());
        tempMinList.add(item.getTaMin7());
        tempMinList.add(item.getTaMin8());
        tempMinList.add(item.getTaMin9());
        tempMinList.add(item.getTaMin10());

        int tempMax = tempMaxList.get(0);
        for(int i=0; i<tempMaxList.size(); i++) {
            if (tempMaxList.get(i) > tempMax) tempMax = tempMaxList.get(i);
        }

        int tempMin = tempMinList.get(0);
        for(int i=0; i<tempMinList.size(); i++) {
            if (tempMinList.get(i) > tempMin) tempMin = tempMinList.get(i);
        }

        // x,y 축
        XAxis x = weekly_chart_temp.getXAxis();
        x.setDrawAxisLine(false);
        x.setDrawGridLines(false);
        x.setDrawLabels(false);
//        x.setTextSize(20);
//        x.setTextColor(Color.WHITE);
//        x.setLabelsToSkip(0);

        YAxis y = weekly_chart_temp.getAxisLeft();
        y.setAxisMaxValue(tempMax+2);
        y.setAxisMinValue(tempMin);
        y.setDrawLabels(true);
        y.setDrawGridLines(false);
        y.setDrawTopYLabelEntry(true);
        y.setTextSize(15);
        y.setTextColor(Color.WHITE);

        YAxis yRight = weekly_chart_temp.getAxisRight();
        yRight.setDrawAxisLine(false);
        yRight.setDrawLabels(false);
        yRight.setDrawGridLines(false);

        Legend legend = weekly_chart_temp.getLegend();
        legend.setEnabled(false);

        //DataSet
        set1 = new CandleDataSet(entries, "TempData");
        set1.setAxisDependency(YAxis.AxisDependency.LEFT);

//        set1.setColor(R.color.graph_color);
//        set1.setDecreasingColor(R.color.graph_color);
        set1.setDecreasingPaintStyle(Paint.Style.FILL);
//        set1.setIncreasingColor(R.color.graph_color);
        set1.setIncreasingPaintStyle(Paint.Style.FILL);
//        set1.setNeutralColor(R.color.graph_color);

        set1.setBarSpace(0.40f);

        set1.setDrawValues(true);
        set1.setValueTextSize(15);
        set1.setValueTextColor(Color.WHITE);

        set1.setVisible(true);
        set1.setShowCandleBar(true);
        set1.setLabel(null);

        CandleEntry entry = set1.getEntryForXIndex(0);      //x인덱스로 엔트리 얻기

        //Data
        data = new CandleData(day, set1);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return (int)value + "℃";
            }
        });

//        weekly_chart_temp.setOnScrollChangeListener(new CandleStickChart.OnScrollChangeListener(){
//            @Override
//            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//
//            }
//        });

        //View
        weekly_chart_temp.setAutoScaleMinMaxEnabled(true);
        weekly_chart_temp.setFitsSystemWindows(false);
//        weekly_chart_temp.setScaleXEnabled(false);
//        weekly_chart_temp.setScaleYEnabled(false);

        weekly_chart_temp.setDescription(null);
        weekly_chart_temp.setDoubleTapToZoomEnabled(false);
        weekly_chart_temp.setClickable(false);

        weekly_chart_temp.setData(data);
        weekly_chart_temp.invalidate();
    }




    private void requestMidTermProspectWeather(){
        Log.e(TAG+"Prospect:요청", "***********************************통신시작");
        new WeatherRetrofitSetup()
                .getRetrofitInterface()
                .getMidTermProspectWeather(AppConstants.WEATHER_API_KEY, midTermWeatherProspectCode, announcing_time, "json", "1", "500")
                .enqueue(new Callback<AcceptProspect>() {
                    @Override
                    public void onResponse(Call<AcceptProspect> call, Response<AcceptProspect> response) {
                        Log.e(TAG+"Prospect:요청", "***********************************응답성공  " + response.toString());

                        AcceptProspect acceptProspect = response.body();
                        com.cs.weather3.model.midtermprospect.Response response1 = acceptProspect.getResponse();
                        com.cs.weather3.model.midtermprospect.Body body = response1.getBody();
                        if(isNull(body)) {
                            Log.e(TAG+"Prospect:요청", "body 널값 - 요청파라미터 검사필요");
                            return;
                        }
                        com.cs.weather3.model.midtermprospect.items items = body.getItems();
                        if(!(items.getItem() instanceof com.cs.weather3.model.midtermprospect.item)){
                            Log.e(TAG+"Prospect:요청", "item null String값 - 시간파라미터 검사필요");
                            return;
                        }
                        com.cs.weather3.model.midtermprospect.item item = items.getItem();
                        if(isNull(item)){
                            Log.e(TAG+"Prospect:요청", "item 널값 - 시간파라미터 검사필요");
                            return;
                        }
                        parseMidTermProspectWeather(item);
                    }

                    @Override
                    public void onFailure(Call<AcceptProspect> call, Throwable t) {
                        Log.e(TAG+"Prospect:요청", "***********************************응답실패");
                        Log.e(TAG+"Prospect:요청", call.request().toString());
                        Log.e(TAG+"Prospect:요청", t.getLocalizedMessage());
                        Log.e(TAG+"Prospect:요청", t.getMessage());
                    }
                });
    }

    private void parseMidTermProspectWeather(com.cs.weather3.model.midtermprospect.item item) {
        Log.e(TAG+"Prospect", "***********************************데이터 처리 시작");
        Log.e(TAG+"Prospect", "***********************************뷰적용 시작");

        weekly_text_midTermProspect.setText(item.getWfSv());
    }













    //etc
    private boolean isNull(Object obj) {
        if (obj == null) return true;
        else return false;
    }
















    public void setMainActivity(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }

    @Override
    public void locationUpdated() {
        Bundle bundle = getArguments();
        if (isNull(bundle)) {
            Log.e(TAG + "위치업데이트", "번들 널값");
            return;
        }
        boolUpdatedLocation = bundle.getBoolean("boolUpdatedLocation");
        boolAnnouncingTime = bundle.getBoolean("boolUpdatedAnnouncingTime");
        announcing_time = bundle.getString("updatedAnnouncingTime");
    }

    @Override
    public void setKorLocation() {
        Bundle bundle = getArguments();
        if(isNull(bundle)) {
            Log.e(TAG+"주소전달", "한글 주소 데이터 전달값 null");
            return;
        }
        province = bundle.getString("province");
        locality = bundle.getString("locality");
        boolUpdatedReverseGeocode = bundle.getBoolean("boolUpdatedReverseGeocode");
        Log.e(TAG+"한글주소저장", province + " " + locality);
        if(isNull(province) || isNull(locality)) {
            Log.e(TAG+"주소전달", "한글 주소 데이터 전달값 null");
            return;
        }
        StringTokenizer st = new StringTokenizer(locality);
        if(st.hasMoreTokens()) weekly_text_currentLocation.setText(st.nextToken());

        parseKorLocation(province, locality);
    }

    private void parseKorLocation(String province, String locality) {
        Log.e(TAG+"예보별코드설정", "***********************************예보 별 코드설정 시작");
        if (province.length() >= 5) province = province.substring(0,2);
        locality = locality.substring(0, locality.length()-1);

        //중기 육상 예보 코드 설정
        switch (province){
            case "서울":
            case "인천":
            case "경기도":
                midTermLandCode = "11B00000";
                break;
            case "강원도":
                midTermLandCode = "11D10000";
            case "대전":
            case "세종":
            case "충청남도":
                midTermLandCode = "11C20000";
                break;
            case "충청북도":
                midTermLandCode = "11C10000";
                break;
            case "광주":
            case "전라남도":
                midTermLandCode = "11F20000";
                break;
            case "전라북도":
                midTermLandCode = "11F10000";
                break;
            case "대구":
            case "경상북도":
                midTermLandCode = "11H10000";
                break;
            case "부산":
            case "울산":
            case "경상남도":
                midTermLandCode = "11H20000";
                break;
            case "제주도":
                midTermLandCode = "11G00000";
                break;
            default:
                Log.e(TAG+"중기육상예보", "코드 에러");
        }

        //중기 기온 예보 코드 설정
        /*
        특수 행정 구역 - province
        일반 도시 - locality
         */
        if (!locality.contains(" ")) {      //특별시는 locality에 단어 하나
            switch (province){
                case "서울": midTermTempCode="11B10101"; break;
                case "인천": midTermTempCode="11B20201"; break;
                case "대전": midTermTempCode="11C20401"; break;
                case "세종": midTermTempCode="11C20404"; break;
                case "대구": midTermTempCode="11H10701"; break;
                case "부산": midTermTempCode="11H20201"; break;
                case "울산": midTermTempCode="11H20101"; break;
                case "광주": midTermTempCode="11F20501"; break;
                default: Log.e(TAG+"중기기온예보", "코드 에러");
            }
        } else {
            switch (locality){
                case "수원": midTermTempCode="11B20601"; break;
                case "성남": midTermTempCode="11B20605"; break;
                case "안양": midTermTempCode="11B20602"; break;
                case "광명": midTermTempCode="11B10103"; break;
                case "과천": midTermTempCode="11B10102"; break;
                case "평택": midTermTempCode="11B20606"; break;
                case "오산": midTermTempCode="11B20603"; break;
                case "의왕": midTermTempCode="11B20609"; break;
                case "용인": midTermTempCode="11B20612"; break;
                case "군포": midTermTempCode="11B20610"; break;
                case "안성": midTermTempCode="11B20611"; break;
                case "화성": midTermTempCode="11B20604"; break;
                case "양평": midTermTempCode="11B20503"; break;
                case "구리": midTermTempCode="11B20501"; break;
                case "남양주": midTermTempCode="11B20502"; break;
                case "하남": midTermTempCode="11B20504"; break;
                case "이천": midTermTempCode="11B20701"; break;
                case "여주": midTermTempCode="11B20703"; break;
                case "광주": midTermTempCode="11B20702"; break;
                case "의정부": midTermTempCode="11B20301"; break;
                case "고양": midTermTempCode="11B20302"; break;
                case "파주": midTermTempCode="11B20305"; break;
                case "양주": midTermTempCode="11B20304"; break;
                case "동두천": midTermTempCode="11B20401"; break;
                case "연천": midTermTempCode="11B20402"; break;
                case "포천": midTermTempCode="11B20403"; break;
                case "가평": midTermTempCode="11B20404"; break;
                case "강화": midTermTempCode="11B20101"; break;
                case "김포": midTermTempCode="11B20102"; break;
                case "시흥": midTermTempCode="11B20202"; break;
                case "부천": midTermTempCode="11B20204"; break;
                case "안산": midTermTempCode="11B20203"; break;
                case "백령도": midTermTempCode="11A00101"; break;
                case "김해": midTermTempCode="11H20304"; break;
                case "양산": midTermTempCode="11H20102"; break;
                case "창원": midTermTempCode="11H20301"; break;
                case "밀양": midTermTempCode="11H20601"; break;
                case "함안": midTermTempCode="11H20603"; break;
                case "창녕": midTermTempCode="11H20604"; break;
                case "의령\n": midTermTempCode="11H20602\n"; break;
                case "진주\n": midTermTempCode="11H20701\n"; break;
                case "하동\n": midTermTempCode="11H20704\n"; break;
                case "사천\n": midTermTempCode="11H20402\n"; break;
                case "거창\n": midTermTempCode="11H20502\n"; break;
                case "합천\n": midTermTempCode="11H20503\n"; break;
                case "산청\n": midTermTempCode="11H20703\n"; break;
                case "함양\n": midTermTempCode="11H20501\n"; break;
                case "통영\n": midTermTempCode="11H20401\n"; break;
                case "거제\n": midTermTempCode="11H20403\n"; break;
                case "고성\n":                                            //고성시 중복
                    if(province.equals("경상남도")) midTermTempCode="11H20404\n";
                    else midTermTempCode="11D20402";
                    break;
                case "남해\n": midTermTempCode="11H20405\n"; break;
                case "영천\n": midTermTempCode="11H10702\n"; break;
                case "경산\n": midTermTempCode="11H10703\n"; break;
                case "청도\n": midTermTempCode="11H10704\n"; break;
                case "칠곡\n": midTermTempCode="11H10705\n"; break;
                case "김천\n": midTermTempCode="11H10601\n"; break;
                case "구미\n": midTermTempCode="11H10602\n"; break;
                case "군위\n": midTermTempCode="11H10603\n"; break;
                case "고령\n": midTermTempCode="11H10604\n"; break;
                case "성주\n": midTermTempCode="11H10605\n"; break;
                case "안동\n": midTermTempCode="11H10501\n"; break;
                case "의성\n": midTermTempCode="11H10502\n"; break;
                case "청송\n": midTermTempCode="11H10503\n"; break;
                case "상주\n": midTermTempCode="11H10302\n"; break;
                case "문경\n": midTermTempCode="11H10301\n"; break;
                case "예천\n": midTermTempCode="11H10303\n"; break;
                case "영주\n": midTermTempCode="11H10401\n"; break;
                case "봉화\n": midTermTempCode="11H10402\n"; break;
                case "영양\n": midTermTempCode="11H10403\n"; break;
                case "울진\n": midTermTempCode="11H10101\n"; break;
                case "영덕\n": midTermTempCode="11H10102\n"; break;
                case "포항\n": midTermTempCode="11H10201\n"; break;
                case "경주\n": midTermTempCode="11H10202\n"; break;
                case "울릉도\n": midTermTempCode="11E00101\n"; break;
                case "독도\n": midTermTempCode="11E00102\n"; break;
                case "나주\n": midTermTempCode="11F20503\n"; break;
                case "장성\n": midTermTempCode="11F20502\n"; break;
                case "담양\n": midTermTempCode="11F20504\n"; break;
                case "화순\n": midTermTempCode="11F20505\n"; break;
                case "영광\n": midTermTempCode="21F20102\n"; break;
                case "함평\n": midTermTempCode="21F20101\n"; break;
                case "목포\n": midTermTempCode="21F20801\n"; break;
                case "무안\n": midTermTempCode="21F20804\n"; break;
                case "영암\n": midTermTempCode="21F20802\n"; break;
                case "진도\n": midTermTempCode="21F20201\n"; break;
                case "신안\n": midTermTempCode="21F20803\n"; break;
                case "흑산도\n": midTermTempCode="11F20701\n"; break;
                case "순천\n": midTermTempCode="11F20603\n"; break;
                case "광양\n": midTermTempCode="11F20402\n"; break;
                case "구례\n": midTermTempCode="11F20601\n"; break;
                case "곡성\n": midTermTempCode="11F20602\n"; break;
                case "완도\n": midTermTempCode="11F20301\n"; break;
                case "강진\n": midTermTempCode="11F20303\n"; break;
                case "장흥\n": midTermTempCode="11F20304\n"; break;
                case "해남\n": midTermTempCode="11F20302\n"; break;
                case "여수\n": midTermTempCode="11F20401\n"; break;
                case "고흥\n": midTermTempCode="11F20403\n"; break;
                case "보성\n": midTermTempCode="11F20404\n"; break;
                case "전주\n": midTermTempCode="11F10201\n"; break;
                case "익산\n": midTermTempCode="11F10202\n"; break;
                case "군산\n": midTermTempCode="21F10501\n"; break;
                case "정읍\n": midTermTempCode="11F10203\n"; break;
                case "김제\n": midTermTempCode="21F10502\n"; break;
                case "남원\n": midTermTempCode="11F10401\n"; break;
                case "고창\n": midTermTempCode="21F10601\n"; break;
                case "무주\n": midTermTempCode="11F10302\n"; break;
                case "부안\n": midTermTempCode="21F10602\n"; break;
                case "순창\n": midTermTempCode="11F10403\n"; break;
                case "완주\n": midTermTempCode="11F10204\n"; break;
                case "임실\n": midTermTempCode="11F10402\n"; break;
                case "장수\n": midTermTempCode="11F10301\n"; break;
                case "진안\n": midTermTempCode="11F10303\n"; break;
                case "세종\n": midTermTempCode="11C20404\n"; break;
                case "공주\n": midTermTempCode="11C20402\n"; break;
                case "논산\n": midTermTempCode="11C20602\n"; break;
                case "계룡\n": midTermTempCode="11C20403\n"; break;
                case "금산\n": midTermTempCode="11C20601\n"; break;
                case "천안\n": midTermTempCode="11C20301\n"; break;
                case "아산\n": midTermTempCode="11C20302\n"; break;
                case "예산\n": midTermTempCode="11C20303\n"; break;
                case "서산\n": midTermTempCode="11C20101\n"; break;
                case "태안\n": midTermTempCode="11C20102\n"; break;
                case "당진\n": midTermTempCode="11C20103\n"; break;
                case "홍성\n": midTermTempCode="11C20104\n"; break;
                case "보령\n": midTermTempCode="11C20201\n"; break;
                case "서천\n": midTermTempCode="11C20202\n"; break;
                case "청양\n": midTermTempCode="11C20502\n"; break;
                case "부여\n": midTermTempCode="11C20501\n"; break;
                case "청주\n": midTermTempCode="11C10301\n"; break;
                case "증평\n": midTermTempCode="11C10304\n"; break;
                case "괴산\n": midTermTempCode="11C10303\n"; break;
                case "진천\n": midTermTempCode="11C10102\n"; break;
                case "충주\n": midTermTempCode="11C10101\n"; break;
                case "음성\n": midTermTempCode="11C10103\n"; break;
                case "제천\n": midTermTempCode="11C10201\n"; break;
                case "단양\n": midTermTempCode="11C10202\n"; break;
                case "보은\n": midTermTempCode="11C10302\n"; break;
                case "옥천\n": midTermTempCode="11C10403\n"; break;
                case "영동\n": midTermTempCode="11C10402\n"; break;
                case "추풍령\n": midTermTempCode="11C10401\n"; break;
                case "철원\n": midTermTempCode="11D10101\n"; break;
                case "화천\n": midTermTempCode="11D10102\n"; break;
                case "인제\n": midTermTempCode="11D10201\n"; break;
                case "양구\n": midTermTempCode="11D10202\n"; break;
                case "춘천\n": midTermTempCode="11D10301\n"; break;
                case "홍천\n": midTermTempCode="11D10302\n"; break;
                case "원주\n": midTermTempCode="11D10401\n"; break;
                case "횡성\n": midTermTempCode="11D10402\n"; break;
                case "영월\n": midTermTempCode="11D10501\n"; break;
                case "정선\n": midTermTempCode="11D10502\n"; break;
                case "평창\n": midTermTempCode="11D10503\n"; break;
                case "대관령\n": midTermTempCode="11D20201\n"; break;
                case "속초\n": midTermTempCode="11D20401\n"; break;
                case "양양\n": midTermTempCode="11D20403\n"; break;
                case "강릉\n": midTermTempCode="11D20501\n"; break;
                case "동해\n": midTermTempCode="11D20601\n"; break;
                case "삼척\n": midTermTempCode="11D20602\n"; break;
                case "태백\n": midTermTempCode="11D20301\n"; break;
                case "제주\n": midTermTempCode="11G00201\n"; break;
                case "서귀포\n": midTermTempCode="11G00401\n"; break;
                case "성산\n": midTermTempCode="11G00101\n"; break;
                case "고산\n": midTermTempCode="11G00501\n"; break;
                case "성판악\n": midTermTempCode="11G00302\n"; break;
                case "이어도\n": midTermTempCode="11G00601\n"; break;
                case "추자도\n": midTermTempCode="11G00800\n"; break;
                default: Log.e(TAG+"중기기온예보", "코드 에러");
            }
        }

        //중기 기상 전망 코드 설정
        switch (province){
            case "강원도": midTermWeatherProspectCode="105"; break;
            case "서울":
            case "인천":
            case "경기도": midTermWeatherProspectCode="109"; break;
            case "충청북도": midTermWeatherProspectCode="131"; break;
            case "대전":
            case "세종":
            case "충청남도": midTermWeatherProspectCode="133"; break;
            case "전라북도": midTermWeatherProspectCode="146"; break;
            case "광주":
            case "전라남도": midTermWeatherProspectCode="156"; break;
            case "대구":
            case "경상북도": midTermWeatherProspectCode="143"; break;
            case "부산":
            case "울산":
            case "경상남도": midTermWeatherProspectCode="159"; break;
            case "제주도": midTermWeatherProspectCode="184"; break;
            default: Log.e(TAG+"중기기상전망", "코드 에러");
        }

        Log.e(TAG+"코드세팅", "**************************************리퀘스트 시도" + boolUpdatedLocation + "  " + boolAnnouncingTime + "  " + boolUpdatedAnnouncingTime);
        if(boolUpdatedLocation){
            if(boolAnnouncingTime || boolUpdatedAnnouncingTime) {
                requestWeeklyLandWeather();
                requestWeeklyTempWeather();
                requestMidTermProspectWeather();
            }
        }
    }

}






































