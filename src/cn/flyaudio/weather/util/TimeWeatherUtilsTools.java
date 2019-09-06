package cn.flyaudio.weather.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.flyaudio.weather.data.WeatherWidgetApplication;
import cn.flyaudio.weather.objectInfo.FullWeatherInfo;
import cn.flyaudio.weather.view.SkinResource;

/**
 * @author:
 * @company:flyaudio
 * @version:2.0
 * @createdDate:20160531
 */
public class TimeWeatherUtilsTools {
    private final static String TAG = Constant.TAG;
    private final static Boolean DEBUG_FLAG = Constant.DEBUG_FLAG;

    public static void setConvertViewWithShared(Context context, View v,
                                                String city) {

    }

    public static void sendBoardcast2SystemUI(Context context, FullWeatherInfo info,
                                              String city, String cityPinYin) {
        Intent intent = new Intent("cn.flyaudio.weather.WEATHERINFO");
        intent.putExtra("condition_text", info.getCondition_text());
        intent.putExtra("condition_code", info.getCondition_code());
        intent.putExtra("condition_temp", info.getCondition_temp());
        intent.putExtra("condition_date", info.getCondition_date());
        intent.putExtra("city_name", city);
        intent.putExtra("city_pinyin", cityPinYin);
        if (DEBUG_FLAG)
            Log.d(TAG, "[UtilsTools] sendBoardcast2SystemUI()" + " " + info.getCondition_text()
                    + "$" + info.getCondition_code() + "$" + info.getCondition_temp()
                    + "$" + info.getCondition_date() + "$" + city + "$" + cityPinYin);
        context.sendBroadcast(intent);
    }






    private static void setTextView(TextView textView, String text) {
        textView.setText(text);
    }


    public static Bitmap getWeatherIcon(Context context, String pos) {

        Bitmap bm = BitmapFactory.decodeResource(
                context.getResources(),
                TimeWeatherUtilsTools.parseSmartIcon(pos));
        int width = bm.getWidth();
        int height = bm.getHeight();
        int newWidth1 = 110;
        int newHeight1 = 110;
        float scaleWidth = ((float) newWidth1) / width;
        float scaleHeight = ((float) newHeight1) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix,
                true);
        return newbm;

    }



    /* 保存天气信息 */
    public static void saveForecastfweathers(Context context,
                                             SharedPreferences p, FullWeatherInfo mForecastyweathers) {
        SharedPreferences.Editor editor = p.edit();
        editor.putString("condition_text",
                mForecastyweathers.getCondition_text());
        editor.putString("condition_code",
                mForecastyweathers.getCondition_code());
        editor.putString("condition_temp",
                mForecastyweathers.getCondition_temp());
        editor.putString("condition_date",
                mForecastyweathers.getCondition_date());
        editor.putString("humidity", mForecastyweathers.getHumidity());
        editor.putString("visibility", mForecastyweathers.getVisibility());
        editor.putString("direction", mForecastyweathers.getWinddirection());
        editor.putString("speed", mForecastyweathers.getWindspeed());
        editor.putString("feelslike", mForecastyweathers.getFeelslike());
        editor.putString("sunrise", mForecastyweathers.getSunrise());
        editor.putString("sunset", mForecastyweathers.getSunset());
        editor.putBoolean("dataflag", mForecastyweathers.getDataflag());
        int index = 0;
        for (int i = 0; i < 5; i++) {
            index = i + 1;
            editor.putString("code" + index,
                    mForecastyweathers.yweathers[i].getCode());
            editor.putString("text" + index,
                    mForecastyweathers.yweathers[i].getText());
            editor.putString("day" + index,
                    mForecastyweathers.yweathers[i].getDay());
            editor.putString("date" + index,
                    mForecastyweathers.yweathers[i].getDate());
            editor.putString("low" + index,
                    mForecastyweathers.yweathers[i].getLow());
            editor.putString("high" + index,
                    mForecastyweathers.yweathers[i].getHigh());
        }
        editor.commit();
    }


    public static String getSmartWindSpeed(Context context, String value) {
        int sunnyNum = SkinResource.getSkinResourceId("nullstring", "string");
        if (value == null || value.equals("") || value.equals("-1"))
            return "";
        int codeValue = Integer.parseInt(value);
        switch (codeValue) {
            case 0:
                sunnyNum = SkinResource.getSkinResourceId("smart_weather_wind_speed0", "string");
                break;
            case 1:
                sunnyNum = SkinResource.getSkinResourceId("smart_weather_wind_speed1", "string");
                break;
            case 2:
                sunnyNum = SkinResource.getSkinResourceId("smart_weather_wind_speed2", "string");
                break;
            case 3:
                sunnyNum = SkinResource.getSkinResourceId("smart_weather_wind_speed3", "string");
                break;
            case 4:
                sunnyNum = SkinResource.getSkinResourceId("smart_weather_wind_speed4", "string");
                break;
            case 5:
                sunnyNum = SkinResource.getSkinResourceId("smart_weather_wind_speed5", "string");
                break;
            case 6:
                sunnyNum = SkinResource.getSkinResourceId("smart_weather_wind_speed6", "string");
                break;
            case 7:
                sunnyNum = SkinResource.getSkinResourceId("smart_weather_wind_speed7", "string");
                break;
            case 8:
                sunnyNum = SkinResource.getSkinResourceId("smart_weather_wind_speed8", "string");
                break;
            case 9:
                sunnyNum = SkinResource.getSkinResourceId("smart_weather_wind_speed9", "string");
                break;
            default:
                return "";
        }
        return SkinResource.getSkinContext().getResources().getString(sunnyNum);
    }

    public static String getSmartWindDirection(Context context, String value) {
        int sunnyNum = SkinResource.getSkinResourceId("nullstring", "string");
        if (value == null || value.equals("") || value.equals("-1"))
            return "";
        int codeValue = Integer.parseInt(value);
        switch (codeValue) {
            case 0:
                sunnyNum = SkinResource.getSkinResourceId("smart_weather_wind_direct0", "string");
                break;
            case 1:
                sunnyNum = SkinResource.getSkinResourceId("smart_weather_wind_direct1", "string");
                break;
            case 2:
                sunnyNum = SkinResource.getSkinResourceId("smart_weather_wind_direct2", "string");
                break;
            case 3:
                sunnyNum = SkinResource.getSkinResourceId("smart_weather_wind_direct3", "string");
                break;
            case 4:
                sunnyNum = SkinResource.getSkinResourceId("smart_weather_wind_direct4", "string");
                break;
            case 5:
                sunnyNum = SkinResource.getSkinResourceId("smart_weather_wind_direct5", "string");
                break;
            case 6:
                sunnyNum = SkinResource.getSkinResourceId("smart_weather_wind_direct6", "string");
                break;
            case 7:
                sunnyNum = SkinResource.getSkinResourceId("smart_weather_wind_direct7", "string");
                break;
            case 8:
                sunnyNum = SkinResource.getSkinResourceId("smart_weather_wind_direct8", "string");
                break;
            case 9:
                sunnyNum = SkinResource.getSkinResourceId("smart_weather_wind_direct9", "string");
                break;
            default:
                return "";
        }
        return SkinResource.getSkinContext().getResources().getString(sunnyNum);
    }

    private static boolean isDay(String sunrise, String sunset) {
        Date date = new Date(System.currentTimeMillis());
        int hour = date.getHours();
        int minutes = date.getMinutes();
        Boolean isDay = true;
        if (sunrise == "--" && sunset == "--") {
            return isDay;
        } else if (sunrise == "--") {
            isDay = false;
            return isDay;
        }

        try {
            int hour_sunrise = Integer.parseInt(sunrise.substring(0,
                    sunrise.indexOf(":")));
            int minutes_sunrise = Integer.parseInt(sunrise.substring(
                    sunrise.indexOf(":") + 1, sunrise.indexOf(" ")));
            int hour_sunset = Integer.parseInt(sunset.substring(0,
                    sunrise.indexOf(":"))) + 12;
            int minutes_sunset = Integer.parseInt(sunset.substring(
                    sunrise.indexOf(":") + 1, sunrise.indexOf(" ")));

            if ((hour_sunrise < hour || (hour_sunrise == hour && minutes_sunrise <= minutes))
                    && ((hour_sunset > hour || (hour_sunset == hour && minutes_sunset >= minutes)))) {
                isDay = true;
            } else {
                isDay = false;
            }
        } catch (Exception e) {
        }
        return isDay;
    }

    /* 获取天气小图片 解析天气代码 **/
    public static int parseSmartIcon(String strIcon) {
        if (strIcon == null)
            return SkinResource.getSkinDrawableIdByName("weather_sunny_d");
        if ("00".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_sunny_d");// 晴天
        if ("01".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_cloudy_d");// 多云
        if ("02".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_overcast");// 阴天
        if ("03".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_shower_d");// 阵雨
        if ("04".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_thundershower");// 雷阵雨
        if ("05".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_thundershower_with_hail");// 冰雹
        if ("06".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_sleet");// 雨夹雪
        if ("07".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_light_rain");// 小雨
        if ("08".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_moderate_rain");// 中雨
        if ("09".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_heavy_rain");// 大雨
        if ("10".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_storm");// 暴雨
        if ("11".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_heavy_storm");// 大暴雨
        if ("12".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_severe_storm");// 特大暴雨
        if ("13".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_snow_flurry_d");// 阵雪
        if ("14".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_light_snow");// 小雪
        if ("15".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_moderate_snow");// 中雪
        if ("16".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_heavy_snow");// 大雪
        if ("17".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_snow_storm");// 暴雪
        if ("18".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_foggy");// 雾
        if ("19".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_ice_rain");// 冻雨
        if ("20".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_duststorm");// 沙尘暴
        if ("21".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_light_to_moderate_rain");// 小雨-中雨
        if ("22".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_moderate_to_heavy_rain");// 中雨-大雨severe_thunderstorms_d
        if ("23".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_heavy_rain_to_storm");// 大雨-暴雨
        if ("24".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_storm_to_heavy_storm");// 暴雨-大暴雨
        if ("25".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_heavy_to_severe_storm");// 大暴雨-特大暴雨
        if ("26".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_light_to_moderate_snow");// 小雪-中雪
        if ("27".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_moderate_to_heavy_snow");// 中雪-大雪
        if ("28".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_heavy_snow_to_snowstorm");// 大雪-暴雪weather_heavy_snow_d
        if ("29".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_dust");// 浮尘
        if ("30".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_sand");// 扬沙
        if ("31".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_sandstorm");// 强沙尘暴
        if ("53".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_haze");// 霾
        if ("99".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_na");//默认
        //（20161223更新，下面为新增天气，因没有来得及设计新图所以使用类似图代替）
        if ("32".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_foggy");// 浓雾
        if ("49".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_foggy");// 强浓雾
        if ("54".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_haze");// 中度霾
        if ("55".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_haze");// 重度霾
        if ("56".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_haze");// 严重霾
        if ("57".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_foggy");// 大雾
        if ("58".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_foggy");// 特强浓雾
        if ("301".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_light_rain");// 雨
        if ("302".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_light_snow");// 雪
        return SkinResource.getSkinDrawableIdByName("weather_na");
    }

    // 20140308 换肤进度到这里。
    public static int parseSmartBgBycode(String strIcon, String sunrise,
                                         String sunset) {
        if (strIcon == null)
            return SkinResource.getSkinDrawableIdByName("weather_sunny_d");
        if ("00".equals(strIcon)) {
            if (isDay(sunrise, sunset)) {
                return SkinResource.getSkinDrawableIdByName("weather_sunny_d");// 晴天(白天)
            }
            return SkinResource.getSkinDrawableIdByName("weather_sunny_n");// 晴天(夜晚)
        }
        if ("01".equals(strIcon)) {
            if (isDay(sunrise, sunset)) {
                return SkinResource.getSkinDrawableIdByName("weather_cloudy_d");// 多云(白天)
            }
            return SkinResource.getSkinDrawableIdByName("weather_cloudy_n");// 多云(夜晚)
        }
        if ("02".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_overcast");// 阴天
        if ("03".equals(strIcon)) {
            if (isDay(sunrise, sunset)) {
                return SkinResource.getSkinDrawableIdByName("weather_shower_d");// 阵雨(白天)
            }
            return SkinResource.getSkinDrawableIdByName("weather_shower_n");// 阵雨(夜晚)
        }
        if ("04".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_thundershower");// 雷阵雨
        if ("05".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_thundershower_with_hail");// 冰雹
        if ("06".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_sleet");// 雨夹雪
        if ("07".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_light_rain");// 小雨
        if ("08".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_moderate_rain");// 中雨
        if ("09".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_heavy_rain");// 大雨
        if ("10".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_storm");// 暴雨
        if ("11".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_heavy_storm");// 大暴雨
        if ("12".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_severe_storm");// 特大暴雨
        if ("13".equals(strIcon)) {
            if (isDay(sunrise, sunset)) {
                return SkinResource.getSkinDrawableIdByName("weather_snow_flurry_d");// 阵雪（白天）
            }
            return SkinResource.getSkinDrawableIdByName("weather_snow_flurry_n");// 阵雪（黑夜）
        }
        if ("14".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_light_snow");// 小雪
        if ("15".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_moderate_snow");// 中雪
        if ("16".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_heavy_snow");// 大雪
        if ("17".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_snow_storm");// 暴雪
        if ("18".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_foggy");// 雾
        if ("19".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_ice_rain");// 冻雨
        if ("20".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_duststorm");// 沙尘暴
        if ("21".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_light_to_moderate_rain");// 小雨-中雨
        if ("22".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_moderate_to_heavy_rain");// 中雨-大雨
        if ("23".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_heavy_rain_to_storm");// 大雨-暴雨
        if ("24".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_storm_to_heavy_storm");// 暴雨-大暴雨
        if ("25".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_heavy_to_severe_storm");// 大暴雨-特大暴雨
        if ("26".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_light_to_moderate_snow");// 小雪-中雪
        if ("27".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_moderate_to_heavy_snow");// 中雪-大雪
        if ("28".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_heavy_snow_to_snowstorm");// 大雪-暴雪
        if ("29".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_dust");// 浮尘
        if ("30".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_sand");// 扬沙
        if ("31".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_sandstorm");// 强沙尘暴
        if ("53".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_haze");// 霾
        if ("99".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_sunny_d");//默认
        // （20161223更新，下面为新增天气，因没有来得及设计新图所以使用类似图代替）
        if ("32".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_foggy");// 浓雾
        if ("49".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_foggy");// 强浓雾
        if ("54".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_haze");// 中度霾
        if ("55".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_haze");// 重度霾
        if ("56".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_haze");// 严重霾
        if ("57".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_foggy");// 大雾
        if ("58".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_foggy");// 特强浓雾
        if ("301".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_light_rain");// 雨
        if ("302".equals(strIcon))
            return SkinResource.getSkinDrawableIdByName("weather_light_snow");// 雪
        return SkinResource.getSkinDrawableIdByName("weather_sunny_d");
    }

}
