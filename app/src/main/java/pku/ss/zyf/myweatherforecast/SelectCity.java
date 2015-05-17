package pku.ss.zyf.myweatherforecast;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pku.ss.zyf.app.MyApplication;
import pku.ss.zyf.bean.City;
import pku.ss.zyf.bean.MyFilter;

/**
 * User: ZhangYafei(261957725@qq.com)
 * Date: 2015-03-27
 * Time: 14:22
 */
public class SelectCity extends Activity implements View.OnClickListener, AdapterView.OnItemClickListener, TextWatcher {

    private List<City> cityList = new ArrayList<>();
    private  ListView cityListView;
    private MyFilter filterAdapter;

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

        for (City city : myApp.getCityList()){
            cityList.add(city);
        }
        filterAdapter = new MyFilter(this,cityList); //
        cityListView.setAdapter(filterAdapter);
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

        String cityNumber = filterAdapter.getItem(position).getNumber();
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

        String aa = s.toString();
        if (aa.length() > 0){
            List<City> resCityList = new ArrayList<>();
            Pattern p;
            List<City> firstCityList = new ArrayList<>();   //首字匹配
            List<City> secondCityList = new ArrayList<>();  //第二字匹配
            List<City> thirdCityList = new ArrayList<>();   //第三字匹配
            List<City> forthCityList = new ArrayList<>();   //第三字后匹配
            for(int i=0;i<cityList.size();i++){
                City city = cityList.get(i);
                p = Pattern.compile(aa);
                Matcher matcher = p.matcher(city.getCity());
                if(matcher.find()) {
                    p = Pattern.compile("^" + aa);
                    matcher = p.matcher(city.getCity());
                    if (matcher.find()) {
                        firstCityList.add(city);
                    } else {
                        p = Pattern.compile("^[\u4e00-\u9fa5]" + aa);
                        matcher = p.matcher(city.getCity());
                        if (matcher.find()) {
                            secondCityList.add(city);
                        } else {
                            p = Pattern.compile("^[\u4e00-\u9fa5]{2}" + aa);
                            matcher = p.matcher(city.getCity());
                            if (matcher.find()) {
                                thirdCityList.add(city);
                            } else {
                                forthCityList.add(city);
                            }
                        }
                    }
                }
            }//endfor
            resCityList.addAll(firstCityList);
            resCityList.addAll(secondCityList);
            resCityList.addAll(thirdCityList);
            resCityList.addAll(forthCityList);
            filterAdapter = new MyFilter(this, resCityList);
            cityListView.setAdapter(filterAdapter);
        }
        else {
            filterAdapter = new MyFilter(this, cityList);
            cityListView.setAdapter(filterAdapter);
        }


    }

    @Override
    public void afterTextChanged(Editable s) {
    }
}
