package pku.ss.zyf.myweatherforecast;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.zip.GZIPInputStream;

import util.NetUtil;


public class MainActivity extends Activity implements View.OnClickListener {

    private ImageView titleUpdateBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_info);
        titleUpdateBtn = (ImageView) findViewById(R.id.title_update_btn);
        titleUpdateBtn.setOnClickListener(this);
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

        //获取网络数据的线程
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
                        parseXML(responseInfo);
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
    private void parseXML(String xmlData){

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
                        if (xmlPullParser.getName().equals("city")){
                            eventType = xmlPullParser.next();
                            Log.d("myXML","city: "+xmlPullParser.getText());
                        }
                        else if (xmlPullParser.getName().equals("updatetime")){
                            eventType = xmlPullParser.next();
                            Log.d("myXML","updateTime: "+xmlPullParser.getText());
                        }
                        else if (xmlPullParser.getName().equals("shidu")){
                            eventType = xmlPullParser.next();
                            Log.d("myXML","shidu: "+xmlPullParser.getText());
                        }
                        else if (xmlPullParser.getName().equals("wendu")){
                            eventType = xmlPullParser.next();
                            Log.d("myXML","wendu: "+xmlPullParser.getText());
                        }
                        else if (xmlPullParser.getName().equals("pm25")){
                            eventType = xmlPullParser.next();
                            Log.d("myXML","pm2.5: "+xmlPullParser.getText());
                        }
                        else if (xmlPullParser.getName().equals("quality")){
                            eventType = xmlPullParser.next();
                            Log.d("myXML","quality: "+xmlPullParser.getText());
                        }
                        else if (xmlPullParser.getName().equals("fengxiang") && fengxiangCount == 0){
                            eventType = xmlPullParser.next();
                            Log.d("myXML","fengxiang: "+xmlPullParser.getText());
                            fengxiangCount++;
                        }
                        else if (xmlPullParser.getName().equals("fengli") && fengliCount == 0){
                            eventType = xmlPullParser.next();
                            Log.d("myXML","fengli: "+xmlPullParser.getText());
                            fengliCount++;
                        }
                        else if (xmlPullParser.getName().equals("date") && dateCount == 0){
                            eventType = xmlPullParser.next();
                            Log.d("myXML","date: "+xmlPullParser.getText());
                            dateCount++;
                        }
                        else if (xmlPullParser.getName().equals("high") && highCount == 0){
                            eventType = xmlPullParser.next();
                            Log.d("myXML","high: "+xmlPullParser.getText().substring(3));
                            highCount++;
                        }
                        else if (xmlPullParser.getName().equals("low") && lowCount == 0){
                            eventType = xmlPullParser.next();
                            Log.d("myXML","low: "+xmlPullParser.getText().substring(3));
                            lowCount++;
                        }
                        else if (xmlPullParser.getName().equals("type") && typeCount == 0){
                            eventType = xmlPullParser.next();
                            Log.d("myXML","type: "+xmlPullParser.getText());
                            typeCount++;
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

    }

}
