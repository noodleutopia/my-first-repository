package pku.ss.zyf.bean;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pku.ss.zyf.myweatherforecast.R;

/**
 * Created by zyf at 11:09,2015/5/14.
 */
public class MyFilter extends BaseAdapter{

    private List<City> cityList = new ArrayList<>();
    private Context ct;
    private LayoutInflater inflater;

    public MyFilter(Context ct, List<City> cities){
        this.cityList = cities;
        this.ct = ct;
        inflater = (LayoutInflater) ct.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {

        return cityList.size();
    }

    @Override
    public City getItem(int position) {
        return cityList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        City city = cityList.get(position);
        if (convertView == null){
            convertView = inflater.inflate(R.layout.m_list_item,null);
        }
        TextView cityTv = (TextView) convertView.findViewById(R.id.item_city_name);
        TextView provinceTv = (TextView) convertView.findViewById(R.id.item_province_name);
        cityTv.setText(city.getCity());
        provinceTv.setText(city.getProvince());
        return convertView;
    }
}
