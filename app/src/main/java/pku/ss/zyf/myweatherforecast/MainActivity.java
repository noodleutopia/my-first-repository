package pku.ss.zyf.myweatherforecast;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.util.logging.LogRecord;
import java.util.zip.GZIPInputStream;

import pku.ss.zyf.bean.TodayWeather;
import pku.ss.zyf.util.NetUtil;


public class MainActivity extends Activity implements View.OnClickListener {

    private static final int UPDATE_TODAY_WEATHER = 1;
    //定义控件对象
    private ImageView titleUpdateBtn, weatherImg, pmImg;
    private TextView cityTv, timeTv, humidtyTv, weekTv, pmDataTv,
            pmQualityTv, temperatureTv, climateTv, windTv;

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.title_update_btn) {
            SharedPreferences sharedPreferences = getSharedPreferences("config", MODE_PRIVATE);
            //设置默认城市为“北京”
            String cityCode = sharedPreferences.getString("main_city_code", "101010100");
            Log.d("myWeather", cityCode);
            int netState = NetUtil.getNetworkState(this);
            if (netState != NetUtil.NETWORK_NONE){
                Log.d("myWeather","网络已连接,使用"+netState);
                queryWeatherCode(cityCode);
            }else {
                Log.d("myWeather","网络未连接");
                Toast.makeText(MainActivity.this,"网络断开！",Toast.LENGTH_LONG).show();
            }

        }
    }

    /**
     * 主线程消息处理机
     */
     private Handler mHandler = new Handler() {

        public void handleMessage(Message msg){
            switch (msg.what){
                case UPDATE_TODAY_WEATHER:
                    updateTodayWeather((TodayWeather) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };
    /**
     * 初始化控件
     */
    private void initView(){
        cityTv = (TextView) findViewById(R.id.city_name);
        timeTv = (TextView) findViewById(R.id.real_time);
        humidtyTv = (TextView) findViewById(R.id.real_humidity);
        weekTv = (TextView) findViewById(R.id.date);
        pmDataTv = (TextView) findViewById(R.id.pm_value);
        pmQualityTv = (TextView) findViewById(R.id.pm_quality);
        pmImg = (ImageView) findViewById(R.id.face);
        temperatureTv = (TextView) findViewById(R.id.temperature);
        climateTv = (TextView) findViewById(R.id.weather_description);
        windTv = (TextView) findViewById(R.id.weather_wind);
        weatherImg = (ImageView) findViewById(R.id.weather_image);
        cityTv.setText("N/A");
        timeTv.setText("N/A");
        humidtyTv.setText("N/A");
        pmDataTv.setText("N/A");
        pmQualityTv.setText("N/A");
        weekTv.setText("N/A");
        temperatureTv.setText("N/A");
        climateTv.setText("N/A");
        windTv.setText("N/A");
    }

    /**
     * 更新今日天气
     * @param todayWeather
     */
    private void updateTodayWeather(TodayWeather todayWeather){
//        Log.d("myUpdate",todayWeather.toString());
        cityTv.setText(todayWeather.getCity());
        timeTv.setText(todayWeather.getUpdatetime() + "发布");
        humidtyTv.setText("湿度：" + todayWeather.getShidu());
        pmDataTv.setText(todayWeather.getPm25());
        pmQualityTv.setText(todayWeather.getQuality());
        weekTv.setText(todayWeather.getDate());
        temperatureTv.setText(todayWeather.getHigh() + "〜" + todayWeather.getLow());
        climateTv.setText(todayWeather.getType());
        windTv.setText("风力：" + todayWeather.getFengli());
        Toast.makeText(MainActivity.this, "更新成功！", Toast.LENGTH_SHORT).show();
    }

    /**
     * 根据城市编号查询所对应的天气信息
     *
     * @param cityCode
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
                    if(httpResponse.getStatusLine().getStatusCode() == 200){
                        HttpEntity httpEntity = httpResponse.getEntity();

                        InputStream responseStream = httpEntity.getContent();
                        responseStream = new GZIPInputStream(responseStream);

                        BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));
                        StringBuilder response = new StringBuilder();
                        String str;
                        while ((str = reader.readLine()) != null){
                            response.append(str);
                        }
                        String responseInfo = response.toString();
                        Log.d("myWeather",responseInfo);

                        //调用解析函数
                        TodayWeather todayWeather = parseXML(responseInfo);
                        if (todayWeather != null){
//                            Log.d("myWeather",todayWeather.toString());
                            //发送消息，主线程更新UI
                            Message msg = new Message();
                            msg.what = UPDATE_TODAY_WEATHER;
                            msg.obj = todayWeather;
                            mHandler.sendMessage(msg);
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 解析XML文件函数
     * @param xmlData
     */
    private TodayWeather parseXML(String xmlData){

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
            Log.d("myXML","parseXML");
            while(eventType != XmlPullParser.END_DOCUMENT){
                switch (eventType){
                    //若为文档开始事件
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    //若为开始标签
                    case XmlPullParser.START_TAG:
                        if (xmlPullParser.getName().equals("resp")){
                            todayWeather = new TodayWeather();
                        }
                        if(todayWeather != null){
                            String paramString;
                            if (xmlPullParser.getName().equals("city")){
                                eventType = xmlPullParser.next();
                                paramString = xmlPullParser.getText();
                                Log.d("myXML","city: "+ paramString);
                                todayWeather.setCity(paramString);
                            }
                            else if (xmlPullParser.getName().equals("updatetime")){
                                eventType = xmlPullParser.next();
                                paramString = xmlPullParser.getText();
                                Log.d("myXML","updateTime: "+paramString);
                                todayWeather.setUpdatetime(paramString);
                            }
                            else if (xmlPullParser.getName().equals("shidu")){
                                eventType = xmlPullParser.next();
                                paramString = xmlPullParser.getText();
                                Log.d("myXML","shidu: "+xmlPullParser.getText());
                                todayWeather.setShidu(paramString);
                            }
                            else if (xmlPullParser.getName().equals("wendu")){
                                eventType = xmlPullParser.next();
                                paramString = xmlPullParser.getText();
                                Log.d("myXML","wendu: "+xmlPullParser.getText());
                                todayWeather.setWendu(paramString);
                            }
                            else if (xmlPullParser.getName().equals("pm25")){
                                eventType = xmlPullParser.next();
                                paramString = xmlPullParser.getText();
                                Log.d("myXML","pm2.5: "+xmlPullParser.getText());
                                todayWeather.setPm25(paramString);
                            }
                            else if (xmlPullParser.getName().equals("quality")){
                                eventType = xmlPullParser.next();
                                paramString = xmlPullParser.getText();
                                Log.d("myXML","quality: "+xmlPullParser.getText());
                                todayWeather.setQuality(paramString);
                            }
                            else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0){
                                eventType = xmlPullParser.next();
                                paramString = xmlPullParser.getText();
                                Log.d("myXML","fengxiang: "+xmlPullParser.getText());
                                todayWeather.setFengxiang(paramString);
                                fengxiangCount++;
                            }
                            else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0){
                                eventType = xmlPullParser.next();
                                paramString = xmlPullParser.getText();
                                Log.d("myXML","fengli: "+xmlPullParser.getText());
                                todayWeather.setFengli(paramString);
                                fengliCount++;
                            }
                            else if (xmlPullParser.getName().equals("date") && dateCount == 0){
                                eventType = xmlPullParser.next();
                                paramString = xmlPullParser.getText();
                                Log.d("myXML","date: "+xmlPullParser.getText());
                                todayWeather.setDate(paramString);
                                dateCount++;
                            }
                            else if (xmlPullParser.getName().equals("high") && highCount == 0){
                                eventType = xmlPullParser.next();
                                paramString = xmlPullParser.getText().substring(3);
                                Log.d("myXML","high: "+paramString);
                                todayWeather.setHigh(paramString);
                                highCount++;
                            }
                            else if (xmlPullParser.getName().equals("low") && lowCount == 0){
                                eventType = xmlPullParser.next();
                                paramString = xmlPullParser.getText().substring(3);
                                Log.d("myXML","low: "+paramString);
                                todayWeather.setLow(paramString);
                                lowCount++;
                            }
                            else if (xmlPullParser.getName().equals("type") && typeCount == 0){
                                eventType = xmlPullParser.next();
                                paramString = xmlPullParser.getText();
                                Log.d("myXML","type: "+paramString);
                                todayWeather.setType(paramString);
                                typeCount++;
                            }
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

}
