package cn.flyaudio.weather.util;

import android.content.ContentResolver;
import android.text.TextUtils;
import android.util.Log;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.List;


import cn.flyaudio.Weather.R;
import cn.flyaudio.weather.data.WeatherWidgetApplication;
import cn.flyaudio.weather.objectInfo.FullWeatherInfo;
import cn.flyaudio.weather.view.SkinResource;
import okhttp3.MediaType;
import okhttp3.RequestBody;


/**
 * Created by lan on 16-12-22.
 */

public class GetWeatherUtil {

    private final static String TAG = "GetWeatherUtil";

    private final static String RETURN_CODE = "return_code";//服务区返回码
    private final static String RETURN_MSG = "return_msg";//返回的信息
    private final static String RETURN_HIG = "highestTemperature";//返回当天最高温度
    private final static String RETURN_LOW = "lowestTemperature";//返回当天最低温度
    private final static String CURRENT_TEMP = "temperature";//当前温度
    private final static String CURRENT_DATA_DATE2 = "dateTimeOfCurWeather";//当前时间段
    private final static String WIND_DIRECTION2 = "windDirection";//风向
    private final static String WIND_SPEED2 = "windSpeed";//风速
    private final static String WEATHER_PHENOMENA2 = "weatherPhenomena";//天气现象编码
    private final static String WEATHER_FORECAST = "mWeatherForecast";//天气现象编码

    private final static String APP_KEY = "flyaudioWeather";

    private static GetWeatherUtil mWeatherUtil = null;


    GetWeather_SQL getWeatherSql = new GetWeather_SQL();


    /**
     * 自1970年1月1日 0点0分0秒以来的秒数
     * 毫秒级，需要转换成秒(10位数字)
     */
    public static String getCurrentMinTime() {
        String time = "";
        try {
            long l = System.currentTimeMillis();
            Log.d("yexingyun", "l==" + l + "");
            long mtime = (long) (l / 1000); //mtime 为秒
            Log.d("yexingyun", "mtime===" + mtime + "");

            time = String.valueOf(mtime);
            Log.d("yexingyun", "time===" + time + "");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return time;
    }

    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    public String getMD5Nonce(String stamp) {
        if (TextUtils.isEmpty(stamp)) {
//            log.error("getMD5Nonce---stamp="+stamp);
            return null;
        }
        StringBuffer string1 = new StringBuffer();
        string1.append(APP_KEY);

        string1.append("||");
        string1.append(stamp);
        String signature = "";

        String stringA = string1.toString();
//        log.info("getMD5Nonce---stringA="+stringA);
        try {

            MessageDigest crypt = MessageDigest.getInstance("MD5");
            crypt.reset();
            crypt.update(stringA.getBytes("UTF-8"));
            signature = byteToHex(crypt.digest());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return signature;
    }


    public static GetWeatherUtil Instance() {
        if (mWeatherUtil == null) {
            synchronized (GetWeatherUtil.class) {
                mWeatherUtil = new GetWeatherUtil();
            }
        }
        return mWeatherUtil;
    }


    public static void LOG(String tag, String msg) {  //信息太长,分段打印
        //因为String的length是字符数量不是字节数量所以为了防止中文字符过多，
        //  把4*1024的MAX字节打印长度改为2001字符数
        int max_str_length = 2001 - tag.length();
        //大于4000时
        while (msg.length() > max_str_length) {
            Log.i(tag, msg.substring(0, max_str_length));
            msg = msg.substring(max_str_length);
        }
        //剩余部分
        Log.i(tag, msg);
    }


    public void getAllWeatherContentForOKGO(final ArrayList<String> citiesNum, final GetWeatherForCityNameUtil.CallBackWeatherList callBackWeatherList, final ArrayList<String> cityNameLists, final AbstractList<String> cityNameEnLists) {
        ArrayList<String> cityNumToCityId = null;//根据城市num查询数据库返回来的城市id的数组集合
        //获取当前时间戳
        String mtime = getCurrentMinTime();
        //进行MD5加密
        String md5String = getMD5Nonce(mtime);
        int citiesCount = citiesNum.size();
        if (citiesCount == 0) {
            callBackWeatherList.onError("没有对应的城市,城市数量为0,citiesCount==0");
            return;
        }
        //根据城市num查询本地数据库文件，返回城市id的数组
        cityNumToCityId = getWeatherSql.queryCitiseId(citiesNum);
        if (cityNumToCityId == null) {
            return;
        }
        //添加城市id
        StringBuffer sb = new StringBuffer();
        sb.append("area=");
        for (int i = 0; i < citiesCount - 1; i++) {
            sb.append(cityNumToCityId.get(i));
            sb.append("|");
        }
        sb.append(cityNumToCityId.get(citiesCount - 1));
        sb.append("&");
        sb.append("stamp=" + mtime);
        sb.append("&");
        sb.append("nonce=" + md5String);
        RequestBody body = RequestBody.create(MediaType.parse("charset=utf-8"), sb.toString());
        final ArrayList<String> finalCityNumToCityId = cityNumToCityId;
        OkGo.<String>post(cn.flyaudio.weather.util.Constant.REQUEST_URL)
                .upRequestBody(body).execute(new StringCallback() {
            private List<FullWeatherInfo> fullWeatherInfoList;

            @Override
            public void onSuccess(com.lzy.okgo.model.Response<String> response) {
                JSONObject returnData = null;
                try {
                    returnData = new JSONObject(response.body());
                    String returnCode = returnData.getString(RETURN_CODE);
                    String returnMsg = returnData.getString(RETURN_MSG);

                    LOG("yexingyun", "returnData====" + returnData.toString());
                    Log.d("yexingyun", "returnMsg====" + returnMsg);
                    Log.d("yexingyun", "returnCode====" + returnCode);
                    if (returnCode.equals("FAIL")) {
                        if (returnMsg.equals("对不起，你请求时间戳不合法")) {
                            returnMsg = WeatherWidgetApplication.shareContext.getResources().getString(R.string.errortime);
                        }
                        if (returnMsg.equals("请求过于频繁")) {
                            return;
                        }

                        callBackWeatherList.onError(returnMsg);
                    } else {
                        fullWeatherInfoList = ParsJson(returnMsg, citiesNum, finalCityNumToCityId, cityNameLists, cityNameEnLists);
                        if (fullWeatherInfoList != null && fullWeatherInfoList.size() > 0) {
                            Log.d("yexingyun", fullWeatherInfoList.size() + "");
                            Log.d("yexingyun", fullWeatherInfoList.toString() + "");
                            Log.d("yexingyun", fullWeatherInfoList.get(0).toString() + "");
                            callBackWeatherList.onSuccess(fullWeatherInfoList);
                        } else {

                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(com.lzy.okgo.model.Response<String> response) {
                super.onError(response);
                Log.d("yexingyun", "returnMsg==" + response.message());
                Log.d("yexingyun", "returnCode==" + response.code());
                callBackWeatherList.onError(WeatherWidgetApplication.shareResources.getResourceName(R.string.neworkconnect));
            }
        });
    }

    private List<FullWeatherInfo> ParsJson(String returnmsg, ArrayList<String> citiesNum, ArrayList<String> finalCityNumToCityId, ArrayList<String> cityNameList, AbstractList<String> cityNameEnLists) {
        String cityId = null;
        String weatherMsg = null;
        String weatherForecast = null;

        SimpleDateFormat sdf = null;
        String preDay = null;
        String curDay = null;
        String windSpeed = null;
        String windDirection = null;
        String weatherPhenomena = null;
        String temperature = null;
        String dateTimeOfCurWeather = null;
        String strTimeFormat_12_24 = null;
        String hourOfDay;
        int curHourOfDay = 0;
        int dayFlag = 0;
        String highestTemperature = null;
        String lowestTemperature = null;
        List<FullWeatherInfo> fullWeatherInfoList = new ArrayList<>();
        FullWeatherInfo latestWeathersokgo;
        try {
            JSONArray array = new JSONArray(returnmsg);
            cityId = array.getString(0);
            JSONObject object = new JSONObject(cityId);

            for (int x = 0; x < citiesNum.size(); x++) {
                weatherMsg = object.getString(finalCityNumToCityId.get(x));
                Log.d(TAG, "----weatherMsg------" + weatherMsg);
                JSONArray array2 = new JSONArray(weatherMsg);
                dayFlag = 0;
                latestWeathersokgo = new FullWeatherInfo();

                for (int j = 0; j < array2.length(); j++) {
                    JSONObject object2 = array2.getJSONObject(j);
                    weatherForecast = object2.getString(WEATHER_FORECAST);
                    JSONObject object3 = new JSONObject(weatherForecast);

                    weatherPhenomena = object3.getString(WEATHER_PHENOMENA2);
                    windDirection = object3.getString(WIND_DIRECTION2);
                    temperature = object3.getString(CURRENT_TEMP);
                    windSpeed = object3.getString(WIND_SPEED2);
                    highestTemperature = object3.getString(RETURN_HIG);
                    lowestTemperature = object3.getString(RETURN_LOW);
                    dateTimeOfCurWeather = object3.getString(CURRENT_DATA_DATE2);
                    //截取年月日
                    curDay = dateTimeOfCurWeather.substring(0, 8);
                    hourOfDay = dateTimeOfCurWeather.substring(8, 10);
                    //当前获取的日期和上一次的不同时说明当前获取的是下一天的天气，
                    if (!curDay.equals(preDay) && dayFlag != 0) {
                        dayFlag++;
                        preDay = curDay;

                    }
                    String dateformart = dateTimeOfCurWeather.substring(0, 4) + "-" + dateTimeOfCurWeather.substring(4, 6) + "-" + dateTimeOfCurWeather.substring(6, 8) + " " +
                            dateTimeOfCurWeather.substring(8, 10) + ":" + dateTimeOfCurWeather.substring(10, 12);
                    switch (dayFlag) {
                        case 0:
                            dayFlag++;
                            preDay = curDay;
                            Calendar calendar = Calendar.getInstance();
                            //获取当前的时间(24小时制)
                            curHourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
                        case 1:
                            //将天气现象id改为对应的编码
                            latestWeathersokgo.yweathers[0].setCode(getWeatherSql.queryWeatherPhenomena(weatherPhenomena));
                            latestWeathersokgo.yweathers[0].setLow(lowestTemperature);
                            Log.d("yexingyun", "low=== " + lowestTemperature);
                            latestWeathersokgo.yweathers[0].setHigh(highestTemperature);
                            Log.d("yexingyun", "hight=== " + highestTemperature);
                            latestWeathersokgo.yweathers[0].setText(UtilsTools.getSmartWeatherByNum(WeatherWidgetApplication.getContext(), latestWeathersokgo.yweathers[0].getCode()));
                            latestWeathersokgo.yweathers[0].setCurtemp(temperature);

                            latestWeathersokgo.yweathers[0].setWindDirection(TimeWeatherUtilsTools.getSmartWindDirection(SkinResource.getSkinContext(), windDirection));
                            latestWeathersokgo.yweathers[0].setWindSpeed(TimeWeatherUtilsTools.getSmartWindSpeed(SkinResource.getSkinContext(), windSpeed));
                            latestWeathersokgo.yweathers[0].setWeatherPhenomena(weatherPhenomena);
                            latestWeathersokgo.yweathers[0].setDate(dateformart);
//                                latestWeathersokgo.yweathers[0].setDate(dateTimeOfCurWeather);
                            latestWeathersokgo.yweathers[0].setCityname(cityNameList.get(x));
                            latestWeathersokgo.yweathers[0].setDay(dateToWeek(dateformart));


                            latestWeathersokgo.setCityPinyin(cityNameEnLists.get(x));
                            latestWeathersokgo.setWindspeed(TimeWeatherUtilsTools.getSmartWindSpeed(SkinResource.getSkinContext(), windSpeed));
                            latestWeathersokgo.setWinddirection(TimeWeatherUtilsTools.getSmartWindDirection(SkinResource.getSkinContext(), windDirection));
                            latestWeathersokgo.setCondition_temp(temperature);
                            latestWeathersokgo.setWeatherPhenomena(weatherPhenomena);
                            latestWeathersokgo.setCity(cityNameList.get(x));

                            latestWeathersokgo.setCondition_code(latestWeathersokgo.yweathers[0].getCode());
                            latestWeathersokgo.setCondition_text(UtilsTools.getSmartWeatherByNum(WeatherWidgetApplication.getContext(), latestWeathersokgo.yweathers[0].getCode()));

                            break;
                        case 2:
                            if ("14".equals(hourOfDay))//以下午14点的天气现象作为明天的预报天气现象 latestWeathers.yweathers[1].setCode(getWeatherSql.queryWeatherPhenomena(weatherPhenomena));
                                latestWeathersokgo.yweathers[1].setLow(lowestTemperature);
                            latestWeathersokgo.yweathers[1].setHigh(highestTemperature);
                            latestWeathersokgo.yweathers[1].setCode(getWeatherSql.queryWeatherPhenomena(weatherPhenomena));
                            latestWeathersokgo.yweathers[1].setText(UtilsTools.getSmartWeatherByNum(WeatherWidgetApplication.getContext(), latestWeathersokgo.yweathers[1].getCode()));
                            latestWeathersokgo.yweathers[1].setCurtemp(temperature);

                            latestWeathersokgo.yweathers[1].setWindDirection(TimeWeatherUtilsTools.getSmartWindDirection(SkinResource.getSkinContext(), windDirection));
                            latestWeathersokgo.yweathers[1].setWindSpeed(TimeWeatherUtilsTools.getSmartWindSpeed(SkinResource.getSkinContext(), windSpeed));
                            latestWeathersokgo.yweathers[1].setWeatherPhenomena(weatherPhenomena);
                            latestWeathersokgo.yweathers[1].setDate(dateformart);
//                                latestWeathersokgo.yweathers[0].setDate(dateTimeOfCurWeather);
                            latestWeathersokgo.yweathers[1].setCityname(cityNameList.get(x));
                            latestWeathersokgo.yweathers[1].setDay(dateToWeek(dateformart));

                            break;
                        case 3:
                            if ("14".equals(hourOfDay))//以下午14点的天气现象作为后天的预报天气现象
                                latestWeathersokgo.yweathers[2].setCode(getWeatherSql.queryWeatherPhenomena(weatherPhenomena));
                            latestWeathersokgo.yweathers[2].setLow(lowestTemperature);
                            latestWeathersokgo.yweathers[2].setHigh(highestTemperature);
                            latestWeathersokgo.yweathers[2].setCurtemp(temperature);
                            latestWeathersokgo.yweathers[2].setText(UtilsTools.getSmartWeatherByNum(WeatherWidgetApplication.getContext(), latestWeathersokgo.yweathers[2].getCode()));

                            latestWeathersokgo.yweathers[2].setWindDirection(TimeWeatherUtilsTools.getSmartWindDirection(SkinResource.getSkinContext(), windDirection));
                            latestWeathersokgo.yweathers[2].setWindSpeed(TimeWeatherUtilsTools.getSmartWindSpeed(SkinResource.getSkinContext(), windSpeed));
                            latestWeathersokgo.yweathers[2].setWeatherPhenomena(weatherPhenomena);

                            latestWeathersokgo.yweathers[2].setDate(dateformart);
//                                latestWeathersokgo.yweathers[0].setDate(dateTimeOfCurWeather);
                            latestWeathersokgo.yweathers[2].setCityname(cityNameList.get(x));
                            latestWeathersokgo.yweathers[2].setDay(dateToWeek(dateformart));
                            break;

                        default:
                            break;
                    }
                }

                ContentResolver contentResolver = WeatherWidgetApplication.getContext().getContentResolver();
                strTimeFormat_12_24 = android.provider.Settings.System.getString(contentResolver,
                        android.provider.Settings.System.TIME_12_24);
                if ("12".equals(strTimeFormat_12_24)) {
                    //24小时制
                    sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm");
                } else {
                    sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                }

                Date curDate = new Date(System.currentTimeMillis());
                String curDateTime = sdf.format(curDate);
                latestWeathersokgo.setCondition_date(curDateTime);
                latestWeathersokgo.setDataflag(true);
                fullWeatherInfoList.add(latestWeathersokgo);
            }
            return fullWeatherInfoList;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return fullWeatherInfoList;
    }

    public static String dateToWeek(String dateformart) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date parse = null;
        try {
            parse = sf.parse(dateformart);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        String[] weekDaysEN = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        Calendar cal = Calendar.getInstance(); // 获得一个日历
//        Date datet =new Date();

        cal.setTime(parse);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1; // 指示一个星期中的某天。
        if (w < 0)
            w = 0;
        return WeatherWidgetApplication.isCNLanguage ? weekDays[w] : weekDaysEN[w];
    }

}
