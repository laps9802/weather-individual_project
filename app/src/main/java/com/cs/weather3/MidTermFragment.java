package com.cs.weather3;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cs.weather3.model.weather.AcceptClass;
import com.cs.weather3.model.weather.Body;
import com.cs.weather3.model.weather.HourWeather;
import com.cs.weather3.model.weather.items;
import com.cs.weather3.network.WeatherRetrofitSetup;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.widget.ListPopupWindow.MATCH_PARENT;


public class MidTermFragment extends Fragment implements MainActivity.OnLocationUpdatedListener, View.OnClickListener {
    //상수, 전역 사용
    private final String TAG = "MidTermFrag-";
    private MainActivity mainActivity;

    private boolean boolPermission;
    private boolean boolExistingLocation;
    private boolean boolUpdatedLocation;
    private boolean boolTime;

    //화면 - 리사이클러 뷰
    TextView threeHour_text_currentLocation;
    Button threeHour_b_refresh;
    RecyclerView threeHour_recycler_timeSkyRainPercent;
    TextView threeHour_text_naverWeatherHome, threeHour_text_naverBroadcast, threeHour_text_naverNationalWeather,
            threeHour_text_weatherNewsWeatherHome, threeHour_text_weatherNewsBroadcast, threeHour_text_weatherNewsLivingIndex;

    LineChart threeHour_lineChart_temp;

    //위치
    private Location targetLocation;
    private LocationManager manager;
    private int intGridX;
    private int intGridY;
    private String province;
    private String locality;

    //시간
    private String base_date;
    private String base_time;

    //날씨 정보 아이템 리스트
    private ArrayList<HashMap<String, Double>> threeHourWeatherList;

    public MidTermFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if(isNull(bundle)) return;
        ThreeHourBundle threeHourBundle = bundle.getParcelable("threeHourBundle");
        if(isNull(threeHourBundle)) return;

        this.intGridX = threeHourBundle.intGridX;
        this.intGridY = threeHourBundle.intGridY;
        this.base_date = threeHourBundle.base_date;
        this.base_time = threeHourBundle.base_time;

        this.boolPermission = threeHourBundle.boolPermission;
        this.boolExistingLocation = threeHourBundle.boolExistingLocation;
        this.boolUpdatedLocation = threeHourBundle.boolUpdatedLocation;
        this.boolTime = threeHourBundle.boolTime;

        Log.e(TAG+"초기화", "ThreeHour번들 초기화");
    }





    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mid_term, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        threeHour_text_currentLocation = v.findViewById(R.id.threeHour_text_currentLocation);
        threeHour_b_refresh = v.findViewById(R.id.threeHour_b_refresh);
        threeHour_recycler_timeSkyRainPercent = v.findViewById(R.id.threeHour_recyclerView);
        threeHour_text_naverWeatherHome = v.findViewById(R.id.threeHour_text_naverWeatherHome);
        threeHour_text_naverBroadcast = v.findViewById(R.id.threeHour_text_naverBroadcast);
        threeHour_text_naverNationalWeather = v.findViewById(R.id.threeHour_text_naverNationalWeather);
        threeHour_text_weatherNewsWeatherHome = v.findViewById(R.id.threeHour_text_weatherNewsWeatherHome);
        threeHour_text_weatherNewsBroadcast = v.findViewById(R.id.threeHour_text_weatherNewsBroadcast);
        threeHour_text_weatherNewsLivingIndex = v.findViewById(R.id.threeHour_text_weatherNewsLivingIndex);
        threeHour_lineChart_temp = v.findViewById(R.id.threeHour_lineChart_temp);

        /**
         * 스크롤 리스너
         */
//        threeHour_recycler_timeSkyRainPercent.setOnScrollChangeListener(new RecyclerView.OnScrollChangeListener(){
//            @Override
//            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//                threeHour_lineChart_temp.enableScroll();
//                threeHour_lineChart_temp.scrollBy(scrollX, scrollY);
//            }
//        });


//        threeHour_lineChart_temp.setOnScrollChangeListener(new LineChart.OnScrollChangeListener(){
//            @Override
//            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//                threeHour_recycler_timeSkyRainPercent.scrollTo(scrollX, scrollY);
//                threeHour_recycler_timeSkyRainPercent.scrollToPosition(scrollX);
//                threeHour_recycler_timeSkyRainPercent.smoothScrollToPosition(scrollX);
//            }
//        });
        threeHour_b_refresh.setOnClickListener(this);

        threeHour_text_naverWeatherHome.setOnClickListener(this);
        threeHour_text_naverBroadcast.setOnClickListener(this);
        threeHour_text_naverNationalWeather.setOnClickListener(this);
        threeHour_text_weatherNewsWeatherHome.setOnClickListener(this);
        threeHour_text_weatherNewsBroadcast.setOnClickListener(this);
        threeHour_text_weatherNewsLivingIndex.setOnClickListener(this);

        //위치 업데이트
        mainActivity.getUpdatedLocation();

        //기존 위치로 API 통신 요청
        if(!boolExistingLocation || !boolTime) {
            if (!boolExistingLocation) Log.e(TAG+"동네예보요청", "기존 위치 null");
            if (!boolTime) Log.e(TAG+"동네예보요청", "기존 시간 null");
            return;
        }
        Log.e(TAG+"동네예보요청", "동네예보요청-위치:기존");
        requestThreeHourWeather();       // 통신 요청 시 쓰레드 분기함. onCreate 제어는 별개로 계속 명령어 실행
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.threeHour_b_refresh:
                mainActivity.getUpdatedLocation();
                break;
            case R.id.threeHour_text_naverWeatherHome:
                Log.e(TAG+"뉴스보러가기", "네이버-날씨홈");
                Intent intentNaverWeatherHome = new Intent(Intent.ACTION_VIEW, Uri.parse("https://m.weather.naver.com/m/main.nhn"));
                if(intentNaverWeatherHome.resolveActivity(mainActivity.getPackageManager()) != null) startActivity(intentNaverWeatherHome);
                break;
            case R.id.threeHour_text_naverBroadcast:
                Log.e(TAG+"뉴스보러가기", "네이버-방송보기");
                Intent intentNaverBroadcast = new Intent(Intent.ACTION_VIEW, Uri.parse("https://m.weather.naver.com/m/news.nhn"));
                if(intentNaverBroadcast.resolveActivity(mainActivity.getPackageManager()) != null) startActivity(intentNaverBroadcast);
                break;
            case R.id.threeHour_text_naverNationalWeather:
                Log.e(TAG+"뉴스보러가기", "네이버-전국날씨");
                Intent intentNaverNationalWeather = new Intent(Intent.ACTION_VIEW, Uri.parse("https://m.weather.naver.com/m/nation.nhn"));
                if(intentNaverNationalWeather.resolveActivity(mainActivity.getPackageManager()) != null) startActivity(intentNaverNationalWeather);
                break;
            case R.id.threeHour_text_weatherNewsWeatherHome:
                Log.e(TAG+"뉴스보러가기", "웨더뉴스-날씨홈");
                Intent intentWeatherNewsWeatherHome = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.kr-weathernews.com/mweb/html/main2.html"));
                if(intentWeatherNewsWeatherHome.resolveActivity(mainActivity.getPackageManager()) != null) startActivity(intentWeatherNewsWeatherHome);
                break;
            case R.id.threeHour_text_weatherNewsBroadcast:
                Log.e(TAG+"뉴스보러가기", "웨더뉴스-방송보기");
                Intent intentWeatherNewsBroadcast = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.kr-weathernews.com/mweb/html/movie/movie2.html"));
                if(intentWeatherNewsBroadcast.resolveActivity(mainActivity.getPackageManager()) != null) startActivity(intentWeatherNewsBroadcast);
                break;
            case R.id.threeHour_text_weatherNewsLivingIndex:
                Log.e(TAG+"뉴스보러가기", "웨더뉴스-생활지수");
                Intent intentWeatherNewsLivingIndex = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.kr-weathernews.com/mweb/html/life/life.html?region="));
                if(intentWeatherNewsLivingIndex.resolveActivity(mainActivity.getPackageManager()) != null) startActivity(intentWeatherNewsLivingIndex);
                break;
        }
    }




    //***통신 작업 - 대기 시간 있기 때문에 함수 연쇄호출함
    private void requestThreeHourWeather() {
        Log.e(TAG+"동네예보:API요청", "***********************************동네예보요청 시작");
        new WeatherRetrofitSetup()
                .getRetrofitInterface()
                .getThreeHourWeather(AppConstants.WEATHER_API_KEY, base_date, base_time, intGridX, intGridY, "json", "1", "500")
                .enqueue(new Callback<AcceptClass>() {
                    @Override
                    public void onResponse(Call<AcceptClass> call, Response<AcceptClass> response) {
                        Log.e(TAG+"동네예보:API요청", "***********************************응답성공  " + response.toString());

                        //** JSON 객체 참조
                        AcceptClass acceptClass = response.body();                                  //c: AcceptClass / m: Response
                        com.cs.weather3.model.weather.Response response1 = acceptClass.getResponse();       //c: Response / m: Body
                        Body body = response1.getBody();                                            //c: Body / m: items
                        if (isNull(body)) {
                            Log.e(TAG+"동네예보:API요청", "body 널값 - 요청파라미터 검사필요");
                            return;
                        }
                        items items = body.getItems();                                          //c: items / m: ArrayList item
                        if(!(items.getItem() instanceof ArrayList)) {
                            Log.e(TAG+"동네예보:API요청", "item null String값 - base_time 검사필요");
                            return;
                        }
                        ArrayList<HourWeather> threeHourItemList = items.getItem();             // 실제데이터: HourWeather 담긴 ArrayList item
                        if (isNull(threeHourItemList)) {
                            Log.e(TAG+"동네예보:API요청", "item 리스트 널값 - base_time 검사필요");
                            return;
                        }
                        parseThreeHour(threeHourItemList);
                    }

                    @Override
                    public void onFailure(Call<AcceptClass> call, Throwable t) {
                        Log.e(TAG+"동네예보:API요청", "***********************************응답실패");
                        Log.e(TAG+"동네예보:API요청", call.request().toString());
                        Log.e(TAG+"동네예보:API요청", t.getLocalizedMessage());
                        Log.e(TAG+"동네예보:API요청", t.getMessage());
                    }
                });
    }

    private void parseThreeHour(ArrayList<HourWeather> threeHourItemList) {
        Log.e(TAG+"동네예보:데이터처리", "***********************************threeHourItemList.get(0) = " + threeHourItemList.get(0));
        int len = threeHourItemList.size();
        threeHourWeatherList = new ArrayList<>();
        ArrayList<Integer> timeList = new ArrayList<>();

        if (len > 0) {
            for (int i = 0; i < len; i++) {
                HourWeather hourWeather = threeHourItemList.get(i);

                String fcstDate = hourWeather.getFcstDate();
                fcstDate = fcstDate.substring(4);
                String fcstTime = hourWeather.getFcstTime();
                fcstTime = fcstTime.substring(0,2);
                int fcstDateTime =  Integer.parseInt(fcstDate + fcstTime);

                String category = hourWeather.getCategory();
                double fcstValue = hourWeather.getFcstValue();

                if(!timeList.contains(fcstDateTime)){
                    timeList.add(fcstDateTime);
                    HashMap<String, Double> weatherItem = new HashMap<>();

                    weatherItem.put("fcstDateTime", (double)fcstDateTime);
                    weatherItem.put(category, fcstValue);
                    threeHourWeatherList.add(weatherItem);
                }
                else{
                    HashMap<String, Double> weatherItem=null;
                    for(int j=0; j<threeHourWeatherList.size(); j++)
                    {
                        if(threeHourWeatherList.get(j).get("fcstDateTime") == (double)fcstDateTime) {
                            weatherItem = threeHourWeatherList.get(j);
                            break;
                        }
                    }
                    weatherItem.put(category, fcstValue);
                }

                Log.e(TAG+"동네예보:데이터처리", "3시간데이터: fcstDateTime = " + fcstDateTime);
                Log.e(TAG+"동네예보:데이터처리", "3시간데이터: category + fcstValue = " + category + " + " + fcstValue);
            }
        }


        Iterator iterator = threeHourWeatherList.get(0).keySet().iterator();
        while (iterator.hasNext()) Log.e(TAG+"동네예보:데이터처리", "첫번째 시간 Keysets = " + (String) iterator.next());

        if (threeHourWeatherList.get(0).keySet().size() > 0) {
            Log.e(TAG+"동네예보:데이터처리", "날씨 데이터 얻음");

            Log.e(TAG+"동네예보:뷰적용", "***********************************뷰적용 시작");
            threeHourAdapter threeHourAdapter = new threeHourAdapter(threeHourWeatherList);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
            threeHour_recycler_timeSkyRainPercent.setLayoutManager(layoutManager);
            threeHour_recycler_timeSkyRainPercent.setAdapter(threeHourAdapter);

            setupLineChartTemp(threeHourWeatherList);
        }
        else
            Log.e(TAG+"동네예보:데이터처리", "날씨 데이터 없음");
    }

    private void setupLineChartTemp(ArrayList<HashMap<String, Double>> threeHourWeatherList) {
        ArrayList<Entry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        for (int i = 0; i < threeHourWeatherList.size(); i++) {
            entries.add(new Entry((float)(double)(threeHourWeatherList.get(i).get("T3H")), i+1));
            labels.add("0");
        }

        //기타 설정
        XAxis x = threeHour_lineChart_temp.getXAxis();
        x.setDrawAxisLine(false);
        x.setDrawGridLines(false);
        x.setDrawLabels(false);
//        x.setSpaceBetweenLabels(10);
//        x.setLabelsToSkip(3);
        YAxis y = threeHour_lineChart_temp.getAxisLeft();
        y.setDrawAxisLine(false);
        y.setDrawGridLines(false);
        y.setDrawLabels(false);
        YAxis yRight = threeHour_lineChart_temp.getAxisRight();
        yRight.setDrawAxisLine(false);
        yRight.setDrawGridLines(false);
        yRight.setDrawLabels(false);
        Legend legend = threeHour_lineChart_temp.getLegend();
        legend.setEnabled(false);

        //데이터셋
        LineDataSet lineDataSet = new LineDataSet(entries, "0");
        lineDataSet.setLineWidth(2.5f);

        lineDataSet.setDrawCircles(true);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setCircleRadius(8f);

        lineDataSet.setDrawValues(true);
        lineDataSet.setValueTextSize(15f);
        lineDataSet.setValueTextColor(Color.WHITE);

        //데이터
        LineData lineData = new LineData(labels, lineDataSet);
        lineData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                return (int)value + "℃";
            }
        });

        //차트
        threeHour_lineChart_temp.setDescription(null);
        threeHour_lineChart_temp.setDoubleTapToZoomEnabled(false);
        threeHour_lineChart_temp.setClickable(false);
        threeHour_lineChart_temp.setDragEnabled(true);
        //touchEnabled(false) : 모든 터치이벤트를 막음
        threeHour_lineChart_temp.setScaleEnabled(false);
        threeHour_lineChart_temp.setPinchZoom(false);
//        threeHour_lineChart_temp.setScaleXEnabled(true);
//        threeHour_lineChart_temp.setScaleYEnabled(false);
//        threeHour_lineChart_temp.setScaleX(1);
//        threeHour_lineChart_temp.setScaleY(1);

//        threeHour_lineChart_temp.enableScroll();
//        threeHour_lineChart_temp.setHorizontalScrollBarEnabled(true);
//        threeHour_lineChart_temp.setVerticalScrollBarEnabled(false);
//        threeHour_lineChart_temp.setScrollContainer(true);


//        threeHour_lineChart_temp.setScaleMinima(lineData.getXValCount() / 5f, 1f);
        threeHour_lineChart_temp.setVisibleXRangeMaximum(5);
        threeHour_lineChart_temp.moveViewToX(0);

        threeHour_lineChart_temp.setData(lineData);
        threeHour_lineChart_temp.invalidate();

//        threeHour_recycler_timeSkyRainPercent.setOnScrollListener(new RecyclerView.OnScrollListener(){
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                threeHour_lineChart_temp.scrollBy(dx, dy);
//
//                ViewPortHandler vph = threeHour_lineChart_temp.getViewPortHandler();
//                Matrix transformation = vph.getMatrixTouch();
//                transformation.postTranslate(-dx, dy);
//                vph.refresh(transformation, threeHour_lineChart_temp, true);
//
//                Log.e(TAG+"recycler", "dx = " + dx + "  " + "dy = " + dy);
//            }
//        });

        /**
         * 스크롤 리스너
         */
        threeHour_recycler_timeSkyRainPercent.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                threeHour_lineChart_temp.enableScroll();
                threeHour_lineChart_temp.scrollBy(dx, dy);
            }
        });
//        threeHour_recycler_timeSkyRainPercent.setOnScrollChangeListener(new View.OnScrollChangeListener() {
//            @Override
//            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//                Log.e(TAG+"recycler", "x : (" + oldScrollX + " -> " + scrollX + ")   y : (" + oldScrollY + " -> " + scrollY + ")");
//            }
//        });


//        threeHour_lineChart_temp.setOnScrollChangeListener(new View.OnScrollChangeListener() {
//            @Override
//            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
////                threeHour_recycler_timeSkyRainPercent.scrollBy(scrollX, scrollY);
////                threeHour_recycler_timeSkyRainPercent.smoothScrollBy(scrollX, scrollY);
//                threeHour_recycler_timeSkyRainPercent.scrollToPosition(scrollX);
//                Log.e(TAG+"lineChart", "x : (" + oldScrollX + " -> " + scrollX + ")   y : (" + oldScrollY + " -> " + scrollY + ")");
//            }
//        });
//        threeHour_lineChart_temp.setOnChartGestureListener(new OnChartGestureListener() {
////            int position=(int)threeHour_lineChart_temp.getTranslationX();
////
////            @Override
////            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
////
////            }
////
////            @Override
////            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
////
////            }
////
////            @Override
////            public void onChartLongPressed(MotionEvent me) {
////
////            }
////
////            @Override
////            public void onChartDoubleTapped(MotionEvent me) {
////
////            }
////
////            @Override
////            public void onChartSingleTapped(MotionEvent me) {
////
////            }
////
////            @Override
////            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {
////            }
////
////            @Override
////            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
////
////            }
////
////            @Override
////            public void onChartTranslate(MotionEvent me, float dX, float dY) {          //x왼쪽: +, x오른쪽: -
//////                int dx = (int)threeHour_lineChart_temp.getX() - position;
//////                int dy = 0;
//////                threeHour_recycler_timeSkyRainPercent.scrollBy(dx, dy);
////
//////                Log.e(TAG+"lineChart", "getX = "+threeHour_lineChart_temp.getX());
//////                Log.e(TAG+"lineChart", "getLeft = "+threeHour_lineChart_temp.getLeft());
//////                Log.e(TAG+"lineChart", "getTranslationX = "+threeHour_lineChart_temp.getTranslationX());
//////                Log.e(TAG+"lineChart", "getScrollX = "+threeHour_lineChart_temp.getScrollX());
//////
////                Log.e(TAG+"lineChart", "dX = " + dX + "  " + "dY = " + dY);
//////                position = dx;
//////                PointD pointD = threeHour_lineChart_temp.getValuesByTouchPoint(dX, dY, YAxis.AxisDependency.LEFT);
//////                Log.e(TAG+"lineChart", "pointD.x = " + pointD.x);
//////                Log.e(TAG+"lineChart", "pointD.y = " + pointD.y);
////                Transformer transformer = threeHour_lineChart_temp.getTransformer(YAxis.AxisDependency.LEFT);
//////                transformer.getPixel
//////                threeHour_lineChart_temp.scrollTo
////            }
////        });
    }


    class threeHourAdapter extends RecyclerView.Adapter<threeHourAdapter.ViewHolder>{
        private ArrayList<HashMap<String, Double>> itemList;

        public threeHourAdapter(ArrayList<HashMap<String, Double>> itemList) {
            this.itemList = itemList;
        }



        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int position) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(R.layout.threehour_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
            HashMap<String, Double> weatherMap = itemList.get(position);

            String fcstDateTime = String.valueOf(weatherMap.get("fcstDateTime"));       //72214
            int startIdxDay=0;
            int startIdxHour=0;
            if(fcstDateTime.length() == 5) {
                //1 3, 3 5
                startIdxDay=1;
                startIdxHour=3;
            } else{
                //2 4, 4 6
                startIdxDay=2;
                startIdxHour=4;
            }
            String fcstDay = fcstDateTime.substring(startIdxDay, startIdxDay+2);
            String fcstHour = fcstDateTime.substring(startIdxHour, startIdxHour+2);
            int intFcstHour = Integer.parseInt(fcstHour);

            viewHolder.threeHourItem_text_date.setText("");
            viewHolder.threeHourItem_view_leftSpace.setLayoutParams(new LinearLayout.LayoutParams(0, MATCH_PARENT));

            Date dateItem = new Date();     //오늘,내일,모레,글피,4일 후
            if(position == 0) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(110, MATCH_PARENT);
                viewHolder.threeHourItem_view_leftSpace.setLayoutParams(params);
                if (fcstHour.equals("00") || fcstHour.equals("03"))
                    viewHolder.threeHourItem_text_date.setText("내일");     // 첫 예측시간이 0시, 3시인 경우에만 날짜 다름
                else viewHolder.threeHourItem_text_date.setText("오늘");
            }
            else{
                if(fcstHour.equals("00")){
                    int intFcstDay = Integer.parseInt(fcstDay);
                    Date dateCompare = new Date();
                    dateCompare.setTime(dateItem.getTime() + 1000*60*60*24);
                    int after1Day = dateCompare.getDate();
                    if (intFcstDay == after1Day) viewHolder.threeHourItem_text_date.setText("내일");
                    else{
                        dateCompare.setTime(dateItem.getTime() + 1000*60*60*24*2);
                        int after2Day = dateCompare.getDate();
                        if(intFcstDay == after2Day) viewHolder.threeHourItem_text_date.setText("모레");
                        else{
                            dateCompare.setTime(dateItem.getTime() + 1000*60*60*24*3);
                            int after3Day = dateCompare.getDate();
                            if(intFcstDay == after3Day) viewHolder.threeHourItem_text_date.setText("글피");
                            else{
                                dateCompare.setTime(dateItem.getTime() + 1000*60*60*24*4);
                                int after4Day = dateCompare.getDate();
                                if(intFcstDay == after4Day) viewHolder.threeHourItem_text_date.setText("4일 후");
                            }
                        }
                    }
                }
            }

            if(intFcstHour >= 0 && intFcstHour < 12) viewHolder.threeHourItem_text_hour.setText("오전 ");
            else viewHolder.threeHourItem_text_hour.setText("오후 ");
            viewHolder.threeHourItem_text_hour.append(intFcstHour + "시");
            viewHolder.threeHourItem_text_rain.setText(weatherMap.get("POP") + "%");
        }

        @Override
        public int getItemCount() {
            return itemList.size();
        }



        class ViewHolder extends RecyclerView.ViewHolder{
            TextView threeHourItem_text_date, threeHourItem_text_hour, threeHourItem_text_rain;
            View threeHourItem_view_leftSpace;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                threeHourItem_text_date = itemView.findViewById(R.id.text_threehour_date);
                threeHourItem_text_hour = itemView.findViewById(R.id.text_threehour_hour);
                threeHourItem_text_rain = itemView.findViewById(R.id.text_threehour_rain);
                threeHourItem_view_leftSpace = itemView.findViewById(R.id.threeHourItem_view_leftSpace);
            }
        }
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
        intGridX = bundle.getInt("updatedIntGridX");
        intGridY = bundle.getInt("updatedIntGridY");
        boolTime = bundle.getBoolean("updatedBoolTime");
        base_date = bundle.getString("updatedBaseDate");
        base_time = bundle.getString("updatedBaseTime");
        if(boolTime) {
            requestThreeHourWeather();       // 통신 요청 시 쓰레드 분기함. onCreate 제어는 별개로 계속 명령어 실행
            Log.e(TAG+"동네예보요청", "동네예보요청-위치업데이트");
        }
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
        Log.e(TAG+"한글주소저장", province + " " + locality);
        if(isNull(province) || isNull(locality)) {
            Log.e(TAG+"주소전달", "한글 주소 데이터 전달값 null");
            return;
        }
        StringTokenizer st = new StringTokenizer(locality);
        if(st.hasMoreTokens()) threeHour_text_currentLocation.setText(st.nextToken());
    }
}
