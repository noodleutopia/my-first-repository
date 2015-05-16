package pku.ss.zyf.myweatherforecast;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import pku.ss.zyf.bean.TodayWeather;
import pku.ss.zyf.bean.ViewPagerAdapter;
import pku.ss.zyf.util.ImageUtils;
import pku.ss.zyf.util.NetUtil;


public class MainActivity extends Activity implements View.OnClickListener {

    private static final int UPDATE_TODAY_WEATHER = 1;
    private static final int UPDATE_FAIL = 2;
    private String cityCode;
    //定义控件对象
    private ImageView titleUpdateBtn, weatherImg, pmImg, mCitySelect;
    private TextView cityTv, timeTv, humidityTv, weekTv, pmDataTv,
            pmQualityTv, temperatureTv, climateTv,
            windTv, currentTempTv;
    private ViewPagerAdapter viewPagerAdapter;
    private ViewPager viewPager;
    private List<View> views;
    private ProgressBar pgb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_info);
        titleUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);
        titleUpdateBtn.setOnClickListener(this);

        initView();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);

    }
    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //选择城市后更新
        if ((requestCode == 1) && (resultCode == RESULT_OK)){
            cityCode = data.getStringExtra("cityNo");
            if (cityCode != null){
                queryWeatherCode(cityCode);
                pgb.setVisibility(View.VISIBLE);
                titleUpdateBtn.setVisibility(View.INVISIBLE);
            }
        }
    }



    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.title_update_btn) {
            titleUpdateBtn.setVisibility(View.INVISIBLE);
            pgb.setVisibility(View.VISIBLE);
            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
            //设置默认城市为“北京”
            cityCode = sharedPreferences.getString("main_city_code", "101010100");
            Log.d("myWeather", cityCode);
            int netState = NetUtil.getNetworkState(this);
            if (netState != NetUtil.NETWORK_NONE) {
                Log.d("myWeather", "网络已连接,使用" + netState);
                queryWeatherCode(cityCode);
            } else {
                pgb.setVisibility(View.INVISIBLE);
                titleUpdateBtn.setVisibility(View.VISIBLE);
                Log.d("myWeather", "网络未连接");
                Toast.makeText(MainActivity.this, "网络断开！", Toast.LENGTH_LONG).show();
            }
        }
        //点选择城市按钮
        if (view.getId() == R.id.title_city_manager){
            Intent intent = new Intent(this,SelectCity.class);
            startActivityForResult(intent, 1);
        }
    }

    /**
     * 主线程消息处理机
     */
    private Handler mHandler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
                    break;
                case UPDATE_FAIL:
                    pgb.setVisibility(View.INVISIBLE);
                    titleUpdateBtn.setVisibility(View.VISIBLE);
                    Toast.makeText(MainActivity.this,"获取数据失败！",Toast.LENGTH_SHORT).show();
                default:
                    break;
            }
        }
    };

    /**
     * 初始化控件
     */
    private void initView() {
        cityTv = (TextView) findViewById(R.id.city_name);
        timeTv = (TextView) findViewById(R.id.real_time);
        humidityTv = (TextView) findViewById(R.id.real_humidity);
        weekTv = (TextView) findViewById(R.id.date);
        pmDataTv = (TextView) findViewById(R.id.pm_value);
        pmQualityTv = (TextView) findViewById(R.id.pm_quality);
        pmImg = (ImageView) findViewById(R.id.face);
        temperatureTv = (TextView) findViewById(R.id.temperature);
        climateTv = (TextView) findViewById(R.id.weather_description);
        windTv = (TextView) findViewById(R.id.weather_wind);
        weatherImg = (ImageView) findViewById(R.id.weather_image);
        currentTempTv = (TextView) findViewById(R.id.current_temp);
        mCitySelect = (ImageView) findViewById(R.id.title_city_manager);
        pmImg = (ImageView) findViewById(R.id.face);
        weatherImg = (ImageView) findViewById(R.id.weather_image);
        pgb = (ProgressBar) findViewById(R.id.title_update_progress);
        mCitySelect.setOnClickListener(this);
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidityTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");
        currentTempTv.setText("N/A");

        LayoutInflater inflater = LayoutInflater.from(this);
        views = new ArrayList<>();
        views.add(inflater.inflate(R.layout.m_3days_weather,viewPager));
        views.add(inflater.inflate(R.layout.m_3days_weather_1,viewPager));
        viewPagerAdapter = new ViewPagerAdapter(views, this);
        viewPager = (ViewPager) findViewById(R.id.m_viwepager);
        viewPager.setAdapter(viewPagerAdapter);
    }

    /**
     * 更新今日天气
     *
     * @param todayWeather todayWeather
     */
    private void updateTodayWeather(TodayWeather todayWeather) {
//        Log.d("myUpdate",todayWeather.toString());
        String pmData = todayWeather.getPm25();
        String weatherDetail = todayWeather.getType();
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime() + "发布");
        humidityTv.setText("湿度: " + todayWeather.getShidu());
        pmDataTv.setText(pmData);
        pmQualityTv.setText(todayWeather.getQuality());
        String date = todayWeather.getDate();
        weekTv.setText(date.substring(0,date.indexOf("日")+1) + "  " +todayWeather.getDate().substring(date.indexOf("日")+1));
        temperatureTv.setText(todayWeather.getLow() + "〜" + todayWeather.getHigh());
        climateTv.setText(weatherDetail);
        windTv.setText("风力：" + todayWeather.getFengli());
        currentTempTv.setText("当前:" + todayWeather.getWendu() + "℃");

        //更新图片
        if ((pmData != null) || (weatherDetail != null)){
//            alterImages(pmData, weatherDetail);
            ImageUtils.alterImages(pmData, weatherDetail, pmImg,weatherImg);
        }
        pgb.setVisibility(View.INVISIBLE);
        titleUpdateBtn.setVisibility(View.VISIBLE);
        Toast.makeText(MainActivity.this, "更新成功！", Toast.LENGTH_SHORT).show();
    }

    /**
     * 解析XML文件函数(PULL方式)
     *
     * @param xmlData xmlData
     * @return todayWeather
     */
    private TodayWeather parseXML(String xmlData) {

        //当天天气BEAN
        TodayWeather todayWeather = null;

        //各项目计数
        int fengxiangCount = 0;
        int fengliCount = 0;
        int dateCount = 0;
        int highCount = 0;
        int lowCount = 0;
        int typeCount = 0;

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = factory.newPullParser();
            xmlPullParser.setInput(new StringReader(xmlData));
            int eventType = xmlPullParser.getEventType();
            Log.d("myXML", "parseXML");
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    //若为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    //若为开始标签
                    case XmlPullParser.START_TAG:
                        if (xmlPullParser.getName().equals("resp")) {
                            todayWeather = new TodayWeather();
                        }
                        if (todayWeather != null) {
                            String paramString;
                            if (xmlPullParser.getName().equals("city")) {
                                paramString = xmlPullParser.nextText();
                                Log.d("myXML", "city: " + paramString);
                                todayWeather.setCity(paramString);
                            }else if (xmlPullParser.getName().equals("wendu")) {
                                paramString = xmlPullParser.nextText();
                                Log.d("myXML", "wendu: " + paramString);
                                todayWeather.setWendu(paramString);
                            }else if (xmlPullParser.getName().equals("updatetime")) {
                                paramString = xmlPullParser.nextText();
                                Log.d("myXML", "updateTime: " + paramString);
                                todayWeather.setUpdatetime(paramString);
                            } else if (xmlPullParser.getName().equals("shidu")) {
                                paramString = xmlPullParser.nextText();
                                Log.d("myXML", "shidu: " + paramString);
                                todayWeather.setShidu(paramString);
                            } else if (xmlPullParser.getName().equals("pm25")) {
                                paramString = xmlPullParser.nextText();
                                Log.d("myXML", "pm2.5: " + paramString);
                                todayWeather.setPm25(paramString);
                            } else if (xmlPullParser.getName().equals("quality")) {
                                paramString = xmlPullParser.nextText();
                                Log.d("myXML", "quality: " + paramString);
                                todayWeather.setQuality(paramString);
                            } else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0) {
                                paramString = xmlPullParser.nextText();
                                Log.d("myXML", "fengxiang: " + xmlPullParser.getText());
                                todayWeather.setFengxiang(paramString);
                                fengxiangCount++;
                            } else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0) {
                                paramString = xmlPullParser.nextText();
                                Log.d("myXML", "fengli: " + paramString);
                                todayWeather.setFengli(paramString);
                                fengliCount++;
                            } else if (xmlPullParser.getName().equals("date") && dateCount == 0) {
                                paramString = xmlPullParser.nextText();
                                Log.d("myXML", "date: " + paramString);
                                todayWeather.setDate(paramString);
                                dateCount++;
                            } else if (xmlPullParser.getName().equals("high") && highCount == 0) {
                                paramString = xmlPullParser.nextText().substring(3);
                                Log.d("myXML", "high: " + paramString);
                                todayWeather.setHigh(paramString);
                                highCount++;
                            } else if (xmlPullParser.getName().equals("low") && lowCount == 0) {
                                paramString = xmlPullParser.nextText().substring(3);
                                Log.d("myXML", "low: " + paramString);
                                todayWeather.setLow(paramString);
                                lowCount++;
                            } else if (xmlPullParser.getName().equals("type") && typeCount == 0) {
                                paramString = xmlPullParser.nextText();
                                Log.d("myXML", "type: " + paramString);
                                todayWeather.setType(paramString);
                                typeCount++;
                            }
//                            eventType = xmlPullParser.next();
                        }

                        break;
                    //若为结束标签
                    case XmlPullParser.END_TAG:
                        break;
                }
                eventType = xmlPullParser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return todayWeather;
    }

    /**
     * 解析json文件
     *
     * @param jsonData jsonData
     * @return todayWeather
     */
    private TodayWeather parseXML2(String jsonData) {
        TodayWeather todayWeather = new TodayWeather();

        ObjectMapper mapper = new ObjectMapper();
        try {
            Map map = mapper.readValue(jsonData, Map.class);
            if (map.containsKey("data") && map.get("data") != null) {
                String key = "data";
                //获取数据
                Map mapData = (Map) map.get(key);
                if (mapData.containsKey("forecast") && mapData.get("forecast") != null) {
                    String key1 = "forecast";
                    //获取近几日天气预报数据
                    List<Map> list = (List<Map>) mapData.get(key1);
                    if (list.get(0) != null) {
                        //获取今日数据
                        Map todayMap = list.get(0);
                        Iterator iteratorToday = todayMap.keySet().iterator();
                        //提取今日数据
                        while (iteratorToday.hasNext()) {
                            String key2 = (String) iteratorToday.next();
                            String result = (String) todayMap.get(key2);
                            switch (key2) {
                                case "fengxiang":
                                    todayWeather.setFengxiang(result);
                                    break;
                                case "fengli":
                                    todayWeather.setFengli(result);
                                    break;
                                case "high":
                                    todayWeather.setHigh(result.substring(2));
                                    break;
                                case "low":
                                    todayWeather.setLow(result.substring(2));
                                    break;
                                case "date":
                                    todayWeather.setDate(result);
                                    break;
                                case "type":
                                    todayWeather.setType(result);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
                if (mapData.containsKey("city") && mapData.get("city") != null) {
                    String key1 = "city";
                    todayWeather.setCity((String) mapData.get(key1));
                }
                if (mapData.containsKey("aqi") && mapData.get("aqi") != null) {
                    String key1 = "aqi";
                    todayWeather.setPm25((String) mapData.get(key1));
                }
                if (mapData.containsKey("wendu") && mapData.get("wendu") != null) {
                    String key1 = "wendu";
                    todayWeather.setPm25((String) mapData.get(key1));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return todayWeather;
    }

    /**
     * 根据城市编号查询所对应的天气信息
     *
     * @param cityCode cityCode
     */
    private void queryWeatherCode(String cityCode) {
        //    XML格式
        final String address = "http://wthrcdn.etouch.cn/WeatherApi?citykey=" + cityCode;
        //    JSON格式
//        final String address = "http://wthrcdn.etouch.cn/weather_mini?citykey=" + cityCode;

        Log.d("myWeather", address);

        //获取网络数据的子线程
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    HttpResponse httpResponse = new DefaultHttpClient()
                            .execute(new HttpGet(address));
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        HttpEntity httpEntity = httpResponse.getEntity();

                        InputStream responseStream = httpEntity.getContent();
                        responseStream = new GZIPInputStream(responseStream);

                        BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));
                        StringBuilder response = new StringBuilder();
                        String str;
                        while ((str = reader.readLine()) != null) {
                            response.append(str);
                        }
                        String responseInfo = response.toString();
                        if (!responseInfo.equals( "")){
                            Log.d("myWeather", responseInfo);
                        }
                        else{
//                            mHandler.sendEmptyMessage(UPDATE_FAIL);
                            Log.d("myWeather", "获取网络数据失败！");
                        }

                        //调用解析函数
                        TodayWeather todayWeather = parseXML(responseInfo);
//                        TodayWeather todayWeather = parseXML2(responseInfo);
                        if (todayWeather != null) {
//                            Log.d("myWeather",todayWeather.toString());
                            //发送消息，主线程更新UI
                            Message msg = new Message();
                            msg.what = UPDATE_TODAY_WEATHER;
                            msg.obj = todayWeather;
                            mHandler.sendMessage(msg);
                        }

                    }
                } catch (IOException e) {
                    mHandler.sendEmptyMessage(UPDATE_FAIL);
                    e.printStackTrace();
                    Log.d("myWeather", "获取网络数据失败！！！");
//                    Message msg = new Message();
//                    msg.what = UPDATE_FAIL;
//                    msg.obj = e;
//                    mHandler.sendMessage(msg);
                }
            }
        }).start();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
