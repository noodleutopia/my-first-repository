package pku.ss.zyf.myweatherforecast;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pku.ss.zyf.app.MyApplication;
import pku.ss.zyf.bean.City;

/**
 * User: ZhangYafei(261957725@qq.com)
 * Date: 2015-03-27
 * Time: 14:22
 */
public class SelectCity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener, TextWatcher {

    private List<City> cityList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private  List<String> cityNameList;
    private  ListView cityListView;
    private SimpleAdapter simpleAdapter;
    private List<Map<String, String>> cityMapList;

    @Override
    protected  void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        setContentView(R.layout.select_city);

        initView();
    }

    private void initView(){
        ImageView mBackBtn = (ImageView) findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(this);
        cityListView = (ListView) findViewById(R.id.city_list);
        MyApplication myApp = (MyApplication) this.getApplicationContext();
        cityListView.setOnItemClickListener(this);
        EditText searchEdit = (EditText) findViewById(R.id.search_edit);
        searchEdit.addTextChangedListener(this);
        cityNameList = new ArrayList<>();
        cityMapList = new ArrayList<Map<String, String>>();

        for (City city : myApp.getCityList()){
            cityList.add(city);
            cityNameList.add(city.getProvince() + "-" + city.getCity());
            Map<String, String> cityMap = new HashMap<String, String>();
            cityMap.put("province", city.getProvince());
            cityMap.put("city",city.getCity());
            cityMap.put("cityNumber",city.getNumber());
            cityMapList.add(cityMap);
        }
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, cityNameList);
        String str[] = {"city","province"};
        int id[] = {android.R.id.text1,android.R.id.text2};
        simpleAdapter = new SimpleAdapter(this,cityMapList,android.R.layout.simple_list_item_2,str,id);
//        cityListView.setAdapter(adapter);
        cityListView.setAdapter(simpleAdapter);
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
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

//        String cityNumber = cityList.get(position).getNumber();
        HashMap tempCityMap = (HashMap) simpleAdapter.getItem(position);

        String cityNumber = (String) tempCityMap.get("cityNumber");

//        Toast.makeText(this,cityNumber,Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this,MainActivity.class);
        intent.putExtra("cityNo",cityNumber);
        setResult(RESULT_OK,intent);
        finish();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
//        List<String> subCityNameList = new ArrayList<>();
////        cityListView.getit
//        for (String cityName : cityNameList){
//            if (cityName.contains(s)){
//                subCityNameList.add(cityName);
//
//            }
//        }
//        Log.d("MyAPP",subCityNameList.toString());
//        cityListView.setAdapter(new ArrayAdapter<String>(this,
//                android.R.layout.simple_list_item_2, subCityNameList));

//        adapter.notifyDataSetChanged();
        simpleAdapter.getFilter().filter(s);
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
}
