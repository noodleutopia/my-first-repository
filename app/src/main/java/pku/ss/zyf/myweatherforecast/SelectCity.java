package pku.ss.zyf.myweatherforecast;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import pku.ss.zyf.app.MyApplication;
import pku.ss.zyf.bean.City;

/**
 * User: ZhangYafei(261957725@qq.com)
 * Date: 2015-03-27
 * Time: 14:22
 */
public class SelectCity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private ImageView mBackBtn;
    private ListView cityListView;
    private List<City> cityList = new ArrayList<>();
    private  List<String> cityNameList;

    @Override
    protected  void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.select_city);

        initView();
    }

    private void initView(){
        mBackBtn = (ImageView) findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);
        cityListView = (ListView) findViewById(R.id.city_list);
        MyApplication myApp = (MyApplication) this.getApplicationContext();
        cityListView.setOnItemClickListener(this);

        cityNameList = new ArrayList<>();
        for (City city : myApp.getCityList()){
            cityList.add(city);
            cityNameList.add(city.getProvince() + "-" + city.getCity());
        }
        ArrayAdapter adapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1,cityNameList);
        cityListView.setAdapter(adapter);
    }
    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.title_back:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {

        String cityNumber = cityList.get(position).getNumber();
//        Toast.makeText(this,cityNumber,Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this,MainActivity.class);
        intent.putExtra("cityNo",cityNumber);
        setResult(RESULT_OK,intent);
        finish();
    }
}
