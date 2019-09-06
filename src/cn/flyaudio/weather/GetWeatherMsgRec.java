package cn.flyaudio.weather;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.flyaudio.base.BaseActivity;
import cn.flyaudio.weather.objectInfo.CityResult;
import cn.flyaudio.weather.objectInfo.FullWeatherInfo;
import cn.flyaudio.weather.objectInfo.WeatherInfo;
import cn.flyaudio.weather.objectInfo.WeatherInfo_OneDay;
import cn.flyaudio.weather.util.Constant;
import cn.flyaudio.weather.util.GetWeatherForCityNameUtil;
import cn.flyaudio.weather.util.GetWeatherUtil;
import cn.flyaudio.weather.util.Smart_GetCity_SQL;
import cn.flyaudio.weather.util.TimeWeatherUtilsTools;
import cn.flyaudio.weather.util.ToastUtil;
import cn.flyaudio.weather.view.SkinResource;

import static cn.flyaudio.weather.Flog.TAG;

/**
 * Created by 叶兴运 on
 * 18-8-22.下午2:36
 */
public class GetWeatherMsgRec extends BroadcastReceiver {


    private int temp = 0;

    @Override
    public void onReceive(final Context mcontext, final Intent intent) {
        String cityName = intent.getStringExtra("cityname");
        String localtion = intent.getStringExtra("getlocaltion");
//        Toast.makeText(mcontext,"sd广播",Toast.LENGTH_SHORT).show();
        Log.d("yexingyun", "onReceive: ssss收到了来自广播=="+temp);
        if (!TextUtils.isEmpty(cityName)) {
            Log.d(TAG, "onReceive: cityName=="+cityName);
            getcityWeather(mcontext, cityName);
        }else if (!TextUtils.isEmpty(localtion)){
//            ToastUtil.show(mcontext,"localtion=="+localtion);
            GetWeatherForCityNameUtil.GetInstance().GetLocation(new GetWeatherForCityNameUtil.LocationCallBack() {
                @Override
                public void onSuccess(String toStr, String toString, String city, String address) {
//                    Log.d("yexingyun","getweathermsg: tostr"+toStr);
//                    Log.d("yexingyun","getweathermsg: toString"+toString);
                    Log.d("yexingyuns","GetLocation: city onSuccess=="+city);
//                    Log.d("yexingyun","getweathermsg: address"+address);
                    Intent intent = new Intent(Constant.ACTION_APP_GETWEATHERCALLBACK);
                    intent.putExtra("json", toStr);
                    intent.putExtra("string", toString);
                    intent.putExtra("city", city);
                    intent.putExtra("address", address);
                    mcontext.sendBroadcast(intent);
//                    temp =temp+1;
                    Log.d("yexingyun", "sendBroadcast onSuccess: ==");
//                    ToastUtil.show(mcontext,"localtion回调已发送=="+address);
                }

                @Override
                public void onError(String errmsg) {

                    Intent intent = new Intent(Constant.ACTION_APP_GETWEATHERCALLBACK);
                    intent.putExtra("errmsg", errmsg);
                    mcontext.sendBroadcast(intent);
//                    ToastUtil.show(mcontext,"errmsg已发送=="+errmsg);
                }
            });
        }
    }

    private void getcityWeather(final Context mcontext, String cityName) {
        GetWeatherForCityNameUtil.GetInstance().GetWeatherForCityName(mcontext, cityName, new GetWeatherForCityNameUtil.CallBackWeatherList() {
            @Override
            public void onSuccess(List<FullWeatherInfo> fullWeatherInfoList) {
//                    Log.d("yexingyun", "onSuccess: fullWeatherInfoList====" + fullWeatherInfoList.size());
                Intent intent = new Intent(Constant.ACTION_APP_GETWEATHERCALLBACK);
//                    Bundle bundle = new Bundle();
                WeatherInfo weatherInfo = null;
                ArrayList<WeatherInfo> weatherInfoList = null;
                if (fullWeatherInfoList != null && fullWeatherInfoList.size() > 0) {

                    weatherInfoList = new ArrayList<>();
                    ArrayList<WeatherInfo_OneDay> weatherInfo_oneDayList = null;
                    for (int j = 0; j < fullWeatherInfoList.size(); j++) {
                        FullWeatherInfo fullWeatherInfo = fullWeatherInfoList.get(j);
                        WeatherInfo_OneDay weatherInfo_oneDay = null;
                        weatherInfo_oneDayList = new ArrayList<>();

                        weatherInfo = new WeatherInfo();
                        for (int i = 0; i < 3; i++) {
                            //一天的天气信息
                            weatherInfo_oneDay = new WeatherInfo_OneDay();
                            weatherInfo_oneDay.setCode(fullWeatherInfo.yweathers[i].getCode());
                            weatherInfo_oneDay.setDate(fullWeatherInfo.yweathers[i].getDate());

                            weatherInfo_oneDay.setDay(fullWeatherInfo.yweathers[i].getDay());
                            weatherInfo_oneDay.setHigh(fullWeatherInfo.yweathers[i].getHigh());
                            weatherInfo_oneDay.setLow(fullWeatherInfo.yweathers[i].getLow());
                            weatherInfo_oneDay.setText(fullWeatherInfo.yweathers[i].getText());
                            weatherInfo_oneDay.setCurtemp(fullWeatherInfo.yweathers[i].getCurtemp());
                            weatherInfo_oneDay.setWindDirection(fullWeatherInfo.yweathers[i].getWindDirection());
                            weatherInfo_oneDay.setWindSpeed(fullWeatherInfo.yweathers[i].getWindSpeed());
                            weatherInfo_oneDay.setWeatherPhenomena(fullWeatherInfo.yweathers[i].getWeatherPhenomena());
                            weatherInfo_oneDay.setDate(fullWeatherInfo.yweathers[i].getDate());
                            weatherInfo_oneDay.setCityname(fullWeatherInfo.yweathers[i].getCityname());
                            //保存三天的天气信息的数组
                            weatherInfo_oneDayList.add(weatherInfo_oneDay);
                        }
                        weatherInfo.setwInfo(weatherInfo_oneDayList);
                        weatherInfoList.add(weatherInfo);
                    }
                }

//                String json = new Gson().toJson(fullWeatherInfoList);
                String jsoninfo = new Gson().toJson(weatherInfoList);
                intent.putExtra("WeatherJsoninfo", jsoninfo);
//                    intent.putExtra("bundle", bundle);
                mcontext.sendBroadcast(intent);
//                    Toast.makeText(mcontext,"已发送回调广播",Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onError(String errmsg) {
                Intent intent = new Intent(Constant.ACTION_APP_GETWEATHERCALLBACK);
                intent.putExtra("WeatherJsoninfo", errmsg);
//                    intent.putExtra("bundle", bundle);
                mcontext.sendBroadcast(intent);
            }
        });
    }


}
