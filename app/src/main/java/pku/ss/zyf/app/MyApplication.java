package pku.ss.zyf.app;

import android.app.Application;
import android.os.Environment;
import android.util.Log;
import android.widget.ListView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import pku.ss.zyf.bean.City;
import pku.ss.zyf.db.CityDB;

/**
 * User: ZhangYafei(261957725@qq.com)
 * Date: 2015-03-27
 * Time: 15:15
 */
public class MyApplication extends Application{
    private static final String TAG = "MyAPP";

    private static Application mApplication;
    private CityDB mCityDB;
    private List<City> mCityList;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG,"MyApplication->OnCreate");
        mApplication = this;

        mCityDB = openCityDB();

        initCityList();
    }

    public static Application getInstance(){
        return mApplication;
    }

    /**
     * 初始化数据库
     *
     * @return cityDB
     */
    private CityDB openCityDB(){

        //设置数据库文件存放路径
        String path = "/data"
                + Environment.getDataDirectory().getAbsolutePath()
                + File.separator + this.getPackageName()
                + File.separator + "databases"
                + File.separator + CityDB.CITY_DB_NAME;

        //新建数据库文件
        File db = new File(path);
        Log.d(TAG,path);

        if (!db.exists()){
            Log.i(TAG, "db is not exists!");

            try {
                InputStream is = this.getAssets().open("city.db");
                db.getParentFile().mkdir();
                FileOutputStream fos = new FileOutputStream(db);
                int length = -1;
                byte[] buffer = new byte[1024];
                while ((length = is.read(buffer)) != -1){
                    fos.write(buffer, 0, length);
                    fos.flush();
                }
                fos.close();
                is.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        else {
            Log.d(TAG,"db EXIST !!!The length is : " + db.length());
        }
        return new CityDB(this, path);
    }

    /**
     * 初始化城市列表
     */
    private void initCityList(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                prepareCityList();
            }
        }).start();
    }

    /**
     * 获取城市列表
     *
     */
    private boolean prepareCityList(){
        mCityList = mCityDB.getAllCity();
        for (City city : mCityList){
            String cityName = city.getCity();
            String province = city.getProvince();
//            Log.d(TAG, province + " " + cityName);
        }
        return true;
    }

    public List<City> getCityList(){
        return mCityList;
    }
}
