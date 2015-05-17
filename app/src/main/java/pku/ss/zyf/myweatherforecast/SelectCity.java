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
        Pattern p;
        List<City> resCityList = new ArrayList<>();
        List<City> firstCityList = new ArrayList<>();   //Ê××ÖÆ¥Åä
        List<City> secondCityList = new ArrayList<>();  //µÚ¶þ×ÖÆ¥Åä
        List<City> thirdCityList = new ArrayList<>();   //µÚÈý×ÖÆ¥Åä
        for(int i=0;i<cityList.size();i++){
            City city = cityList.get(i);
            p = Pattern.compile("^" + aa);
            Matcher matcher = p.matcher(city.getCity());
            if(matcher.find()){
                firstCityList.add(city);
            }else {
                p = Pattern.compile("^/w{1}" + aa);
                matcher = p.matcher(city.getCity());
                if(matcher.find()){
                    secondCityList.add(city);
                }else{
                    p = Pattern.compile("^/w{2}" + aa);
                    matcher = p.matcher(city.getCity());
                    if(matcher.find()){
                        thirdCityList.add(city);
                    }
                    else{
                        p = Pattern.compile(aa);
                        matcher = p.matcher(city.getCity());
                        if(matcher.find()){
                            thirdCityList.add(city);
                        }
                    }
                }
            }




        }
        resCityList.addAll(firstCityList);
        resCityList.addAll(secondCityList);
        resCityList.addAll(thirdCityList);
        filterAdapter = new MyFilter(this, resCityList);
        cityListView.setAdapter(filterAdapter);
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
}
