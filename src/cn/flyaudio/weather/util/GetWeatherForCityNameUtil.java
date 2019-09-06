package cn.flyaudio.weather.util;

import android.content.Context;
import android.util.Log;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import cn.flyaudio.weather.activity.model.IGetCityNameModel;
import cn.flyaudio.weather.data.WeatherWidgetApplication;
import cn.flyaudio.weather.objectInfo.CityResult;
import cn.flyaudio.weather.objectInfo.FullWeatherInfo;
import cn.flyaudio.weather.objectInfo.WeatherInfo_OneDay;

/**
 * Created by 叶兴运 on
 * 18-8-16.上午9:02
 */
public class GetWeatherForCityNameUtil {
    private static GetWeatherForCityNameUtil mWeatherUtilFName = null;
    //声明定位回调监听器
    public AMapLocationClient mLocationClient = null;
    //声明AMapLocationClient类对象
    public AMapLocationListener mLocationListener = null;
    private LocationCallBack mLocationCallBack;
    private int temp = 0;

    private GetWeatherForCityNameUtil() {
    }

    public static GetWeatherForCityNameUtil GetInstance() {
        if (mWeatherUtilFName == null) {
            synchronized (GetWeatherForCityNameUtil.class) {
                mWeatherUtilFName = new GetWeatherForCityNameUtil();
            }
        }
        return mWeatherUtilFName;
    }

    //根据城市名获取天气信息，使用回调。
    public void GetWeatherForCityName(Context context, String cityName, final CallBackWeatherList callBackWeatherList) {
        Smart_GetCity_SQL smartGetCitySql =  Smart_GetCity_SQL.getHelper(context);
        List<CityResult> cityResults = smartGetCitySql.query(cityName);
        ArrayList<String> areaidLists = new ArrayList<>();
        ArrayList<String> cityNameLists = new ArrayList<>();
        AbstractList<String> cityNameEnLists = new ArrayList();

        if (cityResults != null && cityResults.size() > 0) {
            for (CityResult cityResult : cityResults) {
                String areaid = cityResult.getAreaid();
                areaidLists.add(areaid);
                cityNameLists.add(cityResult.getCityName());
                cityNameEnLists.add(cityResult.getCityname_pinyin());
            }
            GetWeatherUtil.Instance().getAllWeatherContentForOKGO(areaidLists, callBackWeatherList, cityNameLists, cityNameEnLists);

        }


    }

    //返回单个城市的天气信息，默认是返回第一个。
    public void GetOneCityWeatherForCityName(Context context, String cityName, final CallBackOneCityWeather callBackOneCityWeather) {
        GetWeatherForCityName(context, cityName, new CallBackWeatherList() {
            @Override
            public void onSuccess(List<FullWeatherInfo> fullWeatherInfoList) {
                if (fullWeatherInfoList != null && fullWeatherInfoList.size() > 0) {
                    FullWeatherInfo fullWeatherInfo = fullWeatherInfoList.get(0);
                    callBackOneCityWeather.onSuccess(fullWeatherInfo);
                }
            }

            @Override
            public void onError(String errMsg) {
                callBackOneCityWeather.onError(errMsg);
            }
        });
    }

    public void GetLocation(final LocationCallBack locationCallBack) {
        this.mLocationCallBack = locationCallBack;
        temp = 0;
        mLocationListener = new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation aMapLocation) {
                String toStr = aMapLocation.toStr();
                String toString = aMapLocation.toString();
                String city = aMapLocation.getCity();
                String address = aMapLocation.getAddress();
                temp = temp + 1;
                mLocationCallBack.onSuccess(toStr, toString, city, address);
//                Log.d("yexingyun", "errorinfo====ss=== " + aMapLocation.getErrorInfo() + "");
                Log.d("yexingyun", "temp g====ss=== " + temp + "");
                if (!aMapLocation.getErrorInfo().equals("success") && aMapLocation.getErrorCode() == 0) {
                    mLocationCallBack.onError(aMapLocation.getErrorInfo().toString() + "");
                }
            }
        };
        //初始化定位
        mLocationClient = new AMapLocationClient(WeatherWidgetApplication.application);
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        //给定位客户端对象设置定位参数
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        mLocationOption.setGpsFirst(true);
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setLocationCacheEnable(true);
        //获取一次定位结果：
        //该方法默认为false。
        //单次定位
        mLocationOption.setOnceLocation(true);
//        mLocationOption.setHttpTimeOut(15);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();
    }

    public interface CallBackOneCityWeather {
        void onSuccess(FullWeatherInfo fullWeatherInfo);

        void onError(String errMsg);
    }

    //返回单个城市最近三天的天气信息
    public void GetThreesDayWeatherForCityName(Context context, String cityName, final CallBackThreeDaysWeather backThreeDaysWeather) {
        GetWeatherForCityName(context, cityName, new CallBackWeatherList() {
            @Override
            public void onSuccess(List<FullWeatherInfo> fullWeatherInfoList) {
                if (fullWeatherInfoList != null && fullWeatherInfoList.size() > 0) {
                    FullWeatherInfo fullWeatherInfo = fullWeatherInfoList.get(0);
                    WeatherInfo_OneDay weatherInfo_oneDay = null;
                    ArrayList<WeatherInfo_OneDay> weatherInfo_oneDayList = new ArrayList<>();
                    for (int i = 0; i < 3; i++) {
                        weatherInfo_oneDay = new WeatherInfo_OneDay();
                        weatherInfo_oneDay.setCode(fullWeatherInfo.yweathers[i].getCode());
                        weatherInfo_oneDay.setDate(fullWeatherInfo.yweathers[i].getDate());
                        weatherInfo_oneDay.setDay(fullWeatherInfo.yweathers[i].getDay());
                        weatherInfo_oneDay.setHigh(fullWeatherInfo.yweathers[i].getHigh());
                        weatherInfo_oneDay.setLow(fullWeatherInfo.yweathers[i].getLow());
                        weatherInfo_oneDay.setText(fullWeatherInfo.yweathers[i].getText());
                        weatherInfo_oneDayList.add(weatherInfo_oneDay);
                    }
                    backThreeDaysWeather.onSuccess(weatherInfo_oneDayList);
                }
            }

            @Override
            public void onError(String errMsg) {
                backThreeDaysWeather.onError(errMsg);
            }
        });


    }


    public interface CallBackThreeDaysWeather {
        void onSuccess(ArrayList<WeatherInfo_OneDay> weaThreeDayAryList);

        void onError(String errMsg);
    }

    public interface CallBackWeatherList {
        void onSuccess(List<FullWeatherInfo> fullWeatherInfoList);

        void onError(String errmsg);
    }

    public interface LocationCallBack {
        void onSuccess(String toStr, String toString, String city, String address);

        void onError(String errmsg);
    }
}
