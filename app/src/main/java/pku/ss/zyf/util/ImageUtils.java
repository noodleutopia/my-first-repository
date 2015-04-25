package pku.ss.zyf.util;

import android.widget.ImageView;

import pku.ss.zyf.myweatherforecast.R;

/**
 * User: ZhangYafei(261957725@qq.com)
 * Date: 2015-04-23
 * Time: 09:52
 */
public class ImageUtils {

    /**
     * 更新图片显示
     *
     * @param pmData pmData
     * @param weatherDetail weatherDetail
     */
    public static void alterImages(String pmData, String weatherDetail, ImageView pmImg, ImageView weatherImg){
        pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
        if (pmData != null){
            int pmInt = Integer.parseInt(pmData);
            if (pmInt <= 50){
                pmImg.setImageResource(R.drawable.biz_plugin_weather_0_50);
            }else if (pmInt <= 100){
                pmImg.setImageResource(R.drawable.biz_plugin_weather_51_100);
            }else if (pmInt <= 150){
                pmImg.setImageResource(R.drawable.biz_plugin_weather_101_150);
            }else if (pmInt <= 200){
                pmImg.setImageResource(R.drawable.biz_plugin_weather_151_200);
            }else if (pmInt <= 300){
                pmImg.setImageResource(R.drawable.biz_plugin_weather_201_300);
            }else {
                pmImg.setImageResource(R.drawable.biz_plugin_weather_greater_300);
            }
        }
        if (weatherDetail != null){
            switch (weatherDetail){
                case "晴":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_qing);
                    break;
                case "多云":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_duoyun);
                    break;
                case "阴":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_yin);
                    break;
                case "小雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoyu);
                    break;
                case "中雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongyu);
                    break;
                case "大雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_dayu);
                    break;
                case "雷阵雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyu);
                    break;
                case "雷阵雨冰雹":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_leizhenyubingbao);
                    break;
                case "小雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_xiaoxue);
                    break;
                case "中雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhongxue);
                    break;
                case "大雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_daxue);
                    break;
                case "暴雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoxue);
                    break;
                case "阵雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenxue);
                    break;
                case "雨夹雪":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_yujiaxue);
                    break;
                case "雾":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_wu);
                    break;
                case "特大暴雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_tedabaoyu);
                    break;
                case "沙尘暴":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_shachenbao);
                    break;
                case "阵雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_zhenyu);
                    break;
                case "暴雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_baoyu);
                    break;
                case "大暴雨":
                    weatherImg.setImageResource(R.drawable.biz_plugin_weather_dabaoyu);
                    break;
                default:
                    break;
            }
        }
    }

}