package cn.flyaudio.weather;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import cn.flyaudio.time.AlarmDetails;
import cn.flyaudio.weather.activity.WeatherDetailsActivity;
import cn.flyaudio.weather.data.WeatherWidgetApplication;
import cn.flyaudio.weather.service.WeatherService;
import cn.flyaudio.weather.util.Constant;
import cn.flyaudio.weather.util.SPUtils;
import cn.flyaudio.weather.util.UtilsTools;
import cn.flyaudio.weather.view.SkinResource;

import android.app.Application;
import android.app.Notification;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.os.SystemProperties;

import static android.content.Context.MODE_PRIVATE;

public class WeatherWidget extends AppWidgetProvider {
    private final static String TAG = "WeatherWidget";
    private final static Boolean DEBUG_FLAG = Constant.DEBUG_FLAG;
    public static String now_date;
    public static String sh_date;
    public static SharedPreferences shared;
    public View viewRoots;
    public RemoteViews views = null;
    private Context context;
    String supportDayNight = SkinResource.getSkinStringByName("skin_support_day_night_mode");
    Intent detailIntent;
    PendingIntent pending;
    final static String ACTION_TIMEBROADCAST = "android.intent.action.timebroadcast";
    public BroadcastReceiver alarmReceiver;
    public Notification notify;
    private SharedPreferences preference0;
    final static String MediaCompleted = "Flyaudio3_TimeService.intent.action.Completed";
    final static String MediaPrepared = "Flyaudio3_TimeService.intent.action.MediaPrepared";
    final static String MediaAlarm = "Flyaudio3_TimeService.intent.action.MediaAlarm";
    private int RingSound = 0;
    public Application application;
    private Bitmap bg;
    private Typeface mTypeface;

    private String strTimeFormat_12_24 = "";
    public static int color = Color.RED;
    public static boolean enableColorTheme = true;
    Drawable whiteBg = null;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if(intent != null){
            Log.d(TAG,"WeatherWidget action = "+intent.getAction()+" current value = "+intent.getIntExtra("current", -1));
        }
        this.context = context;
        application = ((WeatherWidgetApplication) WeatherWidgetApplication.application);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK); // 时间的流逝，以分钟为单位
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED); // 时间被改变，人为设置时间
        intentFilter.addAction("action.flyaudio.updateTimeByGPS");
        if (enableColorTheme) {
            whiteBg = ((WeatherWidgetApplication) application).getShareResources().getDrawable(((WeatherWidgetApplication) application).getResId("white_bg", "drawable"));
            intentFilter.addAction("action.flyaudio.colortheme");
            String c = SystemProperties.get("persist.fly.colortheme", "red");
            try {
                color = Integer.valueOf(c);
            } catch (Exception e) {
                // TODO: handle exception
                color = Color.RED;
            }
        }
        //注册日夜模式广播Action
        ContentResolver mProvider = WeatherWidgetApplication.getContext().getContentResolver();
        strTimeFormat_12_24 = android.provider.Settings.System.getString(mProvider,
                android.provider.Settings.System.TIME_12_24);
        if (strTimeFormat_12_24 == null) {
            strTimeFormat_12_24 = "";
        }
        Log.d(TAG, "onCreate >>>strTimeFormat_12_24==" + strTimeFormat_12_24);
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(MediaAlarm);
        preference0 = context.getSharedPreferences("time", MODE_PRIVATE);
        if (!preference0.contains("alarmOrNot")) {
            SharedPreferences.Editor editor = preference0.edit();
            editor.putString("alarmOrNot", "no");
            editor.commit();
        }
        if (intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
            try {
                context.stopService(new Intent(context, WeatherService.class));
            } catch (Exception e) {
                Log.d(TAG,"stop WeatherService exception  = "+e.getMessage());
            }

            if (UtilsTools.isConnect(context)) {
                Log.d(TAG,"start WeatherService");
                context.startService(new Intent(context, WeatherService.class));
                context.sendBroadcast(new Intent(Constant.ACTION_START_FRESH));
            } else {
                Log.d(TAG,"stop WeatherService");
                context.sendBroadcast(new Intent(Constant.ACTION_STOP_FRESH));
            }
        } else if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")
                || intent.getAction().equals("cn.flyaudio.action.ACCON")) {// 时间判断
            shared = context.getSharedPreferences(
                    getCurrentWeather(context, intent.getIntExtra("current", -1)), 0);

            AppWidgetManager appwidgetManager = AppWidgetManager.getInstance(context);
            ComponentName componentname = new ComponentName(context, WeatherWidget.class);
            appwidgetManager.updateAppWidget(componentname, updateNewAppWidget(context, shared));

        } else if (intent.getAction().equals("android.appwidget.action.APPWIDGET_UPDATE")) {

            shared = context.getSharedPreferences(
                    getCurrentWeather(context, intent.getIntExtra("current", -1)), 0);

            AppWidgetManager appwidgetManager = AppWidgetManager.getInstance(context);
            ComponentName componentname = new ComponentName(context, WeatherWidget.class);
            appwidgetManager.updateAppWidget(componentname, updateNewAppWidget(context, shared));

        } else if (intent.getAction().equals("android.intent.action.TIME_SET")) {
            shared = context.getSharedPreferences(
                    getCurrentWeather(context, intent.getIntExtra("current", -1)), 0);

            AppWidgetManager appwidgetManager = AppWidgetManager.getInstance(context);
            ComponentName componentname = new ComponentName(context, WeatherWidget.class);
            appwidgetManager.updateAppWidget(componentname, updateNewAppWidget(context, shared));

        } else if (intent.getAction().equals("action.flyaudio.colortheme")) {
            int rgb = intent.getIntExtra("rgb", -1);
            Log.d(TAG, "weather widget rgb=" + rgb);
            SharedPreferences shared = context.getSharedPreferences("weather", 0);
            SharedPreferences.Editor editor = shared.edit();
            editor.putInt("weather_widget_color", rgb);
            editor.commit();
            shared = context.getSharedPreferences(getCurrentWeather(context, intent.getIntExtra("current", -1)), 0);
            AppWidgetManager appwidgetManager = AppWidgetManager.getInstance(context);
            ComponentName componentname = new ComponentName(context, WeatherWidget.class);
            appwidgetManager.updateAppWidget(componentname, updateNewAppWidget(context, shared));

        } else if (intent.getAction().equals("android.intent.action.LOCALE_CHANGED")) {
            WeatherWidgetApplication.getlanguage();
            shared = context.getSharedPreferences(
                    getCurrentWeather(context, intent.getIntExtra("current", -1)), 0);
            AppWidgetManager appwidgetManager = AppWidgetManager.getInstance(context);
            ComponentName componentname = new ComponentName(context, WeatherWidget.class);
            appwidgetManager.updateAppWidget(componentname, updateNewAppWidget(context, shared));
        }


    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

       Log.d(TAG,"WeatherWidget onUpdate");
        SharedPreferences shared = context.getSharedPreferences("weather", 0);
        SharedPreferences.Editor editor = shared.edit();

        int currentCityNum = shared.getInt("current", 1);
        String currentCityName = "city" + String.valueOf(currentCityNum);
        String city = shared.getString(currentCityName, "");

        if (city.equals("")) {
            editor.putString("city1", Constant.DEFAULT_CITYCODE);
            editor.putString("1", SkinResource.getSkinContext().getString(SkinResource.getSkinResourceId("guangzhou", "string")));
            editor.putString("10", SkinResource.getSkinContext().getString(SkinResource.getSkinResourceId("guangzhou_pinyin", "string")));
            editor.putInt("current", 1);
            editor.commit();
        }
        context.startService(new Intent(context, WeatherService.class));

    }


    /**
     * 定义新的天气布局，保留以前的
     * @param context
     * @param shared
     * @return
     */
    private RemoteViews updateNewAppWidget(Context context, SharedPreferences shared) {
        int passtime = getPastTime(context, shared);
        Log.d(TAG,"updateAppWidget passtime = "+passtime);
        if (passtime > 5 || passtime < 0){
            passtime = 0;
        }

        if(views == null){
            views = new RemoteViews(SkinResource.getSkinContext().getPackageName(),
                    SkinResource.getSkinLayoutIdByName("new_widget_layout"));
        }


        Intent detailIntent0 = new Intent(context, WeatherDetailsActivity.class);
        PendingIntent pending0 = PendingIntent.getActivity(context, 0,
                detailIntent0, 0);
        views.setOnClickPendingIntent(SkinResource.getSkinResourceId("widgetllayout", "id"), pending0);
        //没数据时默认显示的温度
        String noTempData = SkinResource.getSkinStringByName("no_temp_data");

        if (shared.getBoolean("dataflag", true)) {
            Log.d(TAG,"dataflag = true");

           String condition_temp = SPUtils.getInstance().getString("condition_temp", noTempData);
           String condition_code = SPUtils.getInstance().getString("condition_code", "-1");
           if("--".equals(condition_temp)){
               condition_temp = "33";
           }
           if("-1".equals(condition_code)){
               condition_code ="04";
           }
          // condition_temp = "31°";
          //  condition_code = "晴转多云";
            views.setTextViewText(SkinResource.getSkinResourceId("currenttemp_tv", "id"),condition_temp+"°");
            //图标和背景分离时修改：weatherwidget改为img_widget_weather_icon（新增的图标id）

            views.setInt(SkinResource.getSkinResourceId("weather_img", "id"), "setBackgroundResource",
                    UtilsTools.parseSmartBgBycode(condition_code, shared.getString("", shared.getString("sunrise", noTempData)), shared.getString("sunset", noTempData)));

            views.setTextViewText(
                    SkinResource.getSkinResourceId("weatherdescribe_tv", "id"),
                    UtilsTools.getSmartWeatherByNum(context, condition_code));

        } else {
            Log.d(TAG,"dataflag = false");
            String noConditionData = SkinResource.getSkinStringByName("no_weather_data");
            views.setTextViewText(SkinResource.getSkinResourceId("currenttemp_tv", "id"), "33°");
            views.setInt(SkinResource.getSkinResourceId("weather_img", "id"), "setBackgroundResource",
                    SkinResource.getSkinDrawableIdByName("weather_widget_default_icon"));
            views.setTextViewText(SkinResource.getSkinResourceId("weatherdescribe_tv", "id"), "雷阵雨");
        }

        SharedPreferences sharedCity = context.getSharedPreferences("weather", 0);
        int currentCityNum = sharedCity.getInt("current", 1);
        Log.d(TAG,"currentCityNum = "+currentCityNum);
        String city = WeatherWidgetApplication.isCNLanguage ? sharedCity.getString(String.valueOf(currentCityNum), "") :
                sharedCity.getString(String.valueOf(currentCityNum * 10), "");
        Log.d(TAG,"city = "+city);
        if (city.length() >= 7 && city.contains(","))
            city = city.split(",")[1];
        if (WeatherWidgetApplication.isCNLanguage) {
            views.setTextViewText(SkinResource.getSkinResourceId("city_tv", "id"), city);
        } else {
            String cityName = WeatherWidgetApplication.toUpperCaseFirstOne(city);
            Flog.d("WidgetcityName cityName=" + cityName);
            views.setTextViewText(SkinResource.getSkinResourceId("city_tv", "id"), cityName);
        }

        Date dd = new Date();
        //样式为07/29 星期一
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd  EEE");
        String dt = sdf.format(dd);
        views.setTextViewText(SkinResource.getSkinResourceId("dateweek_tv", "id"), dt);
        return views;
    }

    /**
     * 以前的天气布局
     * @param context
     * @param shared
     * @return
     */
    private RemoteViews updateAppWidget(Context context, SharedPreferences shared) {

        int passtime = getPastTime(context, shared);
        Log.d(TAG,"updateAppWidget passtime = "+passtime);
        if (passtime > 5 || passtime < 0){
            passtime = 0;
        }

        views = new RemoteViews(SkinResource.getSkinContext().getPackageName(),
                SkinResource.getSkinLayoutIdByName("widget_layout"));
        Log.d(TAG,"enable color theme = "+WeatherWidgetApplication.getEnableColorTheme());
        if (WeatherWidgetApplication.getEnableColorTheme()) {
            int bgcolor = Integer.parseInt(SystemProperties.get(Constant.PROPERTY_COLORTHEME, "-65536"));
            BitmapDrawable mask = (BitmapDrawable) SkinResource.getSkinContext().getResources().getDrawable(SkinResource.getSkinDrawableIdByName("menu_weather"));
            views.setBitmap(SkinResource.getSkinResourceId("weatherwidgetcolormask", "id"), "setImageBitmap", UtilsTools.getBitmap(bgcolor, mask));
        }

        Intent detailIntent0 = new Intent(context, WeatherDetailsActivity.class);
        PendingIntent pending0 = PendingIntent.getActivity(context, 0,
                detailIntent0, 0);

        views.setOnClickPendingIntent(SkinResource.getSkinResourceId("curt_temp_text", "id"), pending0);

        Intent detailIntent1 = new Intent(context, WeatherDetailsActivity.class);
        PendingIntent pending1 = PendingIntent.getActivity(context, 0,
                detailIntent1, 1);
        views.setOnClickPendingIntent(SkinResource.getSkinResourceId("img_widget_weather_icon", "id"), pending1);

        Intent detailIntent2 = new Intent(context, AlarmDetails.class);
        PendingIntent pending2 = PendingIntent.getActivity(context, 0,
                detailIntent2, 2);
        views.setOnClickPendingIntent(SkinResource.getSkinResourceId("weather_in_time", "id"), pending2);

        Intent detailIntent3 = new Intent(context, WeatherDetailsActivity.class);
        PendingIntent pending3 = PendingIntent.getActivity(context, 0,
                detailIntent3, 3);
        views.setOnClickPendingIntent(SkinResource.getSkinResourceId("city_text", "id"), pending3);


       String lowToHightSymbol = SkinResource.getSkinStringByName("low_to_hight_symbol");
        //没数据时默认显示的温度
        String noTempData = SkinResource.getSkinStringByName("no_temp_data");

        if (shared.getBoolean("dataflag", true)) {
            Log.d(TAG,"dataflag = true");
            views.setTextViewText(SkinResource.getSkinResourceId("txt_widget_low_temperature", "id"), shared.getString("low1", noTempData));
            views.setTextViewText(SkinResource.getSkinResourceId("txt_widget_hight_temperature", "id"), shared.getString("high1", noTempData));

            String condition_temp = SPUtils.getInstance().getString("condition_temp", noTempData);
            String condition_code = SPUtils.getInstance().getString(
                    "condition_code", "-1");
            views.setTextViewText(SkinResource.getSkinResourceId("curt_temp_text", "id"),
                    condition_temp);
            //图标和背景分离时修改：weatherwidget改为img_widget_weather_icon（新增的图标id）
            views.setInt(SkinResource.getSkinResourceId("img_widget_weather_icon", "id"), "setBackgroundResource",
                    UtilsTools.parseSmartBgBycode(condition_code, shared.getString("", shared.getString("sunrise", noTempData)), shared.getString("sunset", noTempData)));
            views.setTextViewText(
                    SkinResource.getSkinResourceId("condition_text", "id"),
                    UtilsTools.getSmartWeatherByNum(context, condition_code));
        } else {
            Log.d(TAG,"dataflag = false");
            String noConditionData = SkinResource.getSkinStringByName("no_weather_data");
            views.setTextViewText(SkinResource.getSkinResourceId("curt_temp_text", "id"), noTempData);
            views.setTextViewText(SkinResource.getSkinResourceId("txt_widget_low_temperature", "id"), noTempData);
            views.setTextViewText(SkinResource.getSkinResourceId("txt_widget_hight_temperature", "id"), noTempData);
            views.setInt(SkinResource.getSkinResourceId("img_widget_weather_icon", "id"), "setBackgroundResource",
                    SkinResource.getSkinDrawableIdByName("weather_widget_default_icon"));
            views.setTextViewText(SkinResource.getSkinResourceId("condition_text", "id"), noConditionData);
        }

        SharedPreferences sharedCity = context.getSharedPreferences("weather", 0);
        int currentCityNum = sharedCity.getInt("current", 1);
        Log.d(TAG,"currentCityNum = "+currentCityNum);
        String city = WeatherWidgetApplication.isCNLanguage ? sharedCity.getString(String.valueOf(currentCityNum), "") :
                sharedCity.getString(String.valueOf(currentCityNum * 10), "");
        Log.d(TAG,"city = "+city);
        if (city.length() >= 7 && city.contains(","))
            city = city.split(",")[1];
        if (WeatherWidgetApplication.isCNLanguage) {
            views.setTextViewText(SkinResource.getSkinResourceId("city_text", "id"), city);
        } else {
            String cityName = WeatherWidgetApplication.toUpperCaseFirstOne(city);
            Flog.d("WidgetcityName cityName=" + cityName);
            views.setTextViewText(SkinResource.getSkinResourceId("city_text", "id"), cityName);
        }


        Date currentDateTime = Calendar.getInstance().getTime();
        SimpleDateFormat AM_PM_hourFormat = new SimpleDateFormat("HH", Locale.getDefault());
        int hournum = Integer.parseInt(AM_PM_hourFormat.format(currentDateTime));
        String amPmStr = null;
        SimpleDateFormat hourFormat = null;
        Log.d(TAG, " updateUI：strTimeFormat_12_24 " + strTimeFormat_12_24);
        if (strTimeFormat_12_24.equals("12")) {
            hourFormat = new SimpleDateFormat("hh", Locale.getDefault());
            if (0 <= hournum && hournum < 12){
                amPmStr = "AM";
            }else{
                amPmStr = "PM";
            }
            views.setViewVisibility(((WeatherWidgetApplication) application).getResId("time_ampm_text", "id"), View.VISIBLE);
            views.setTextViewText(((WeatherWidgetApplication) application).getResId("time_ampm_text", "id"), amPmStr);
        } else {
            hourFormat = new SimpleDateFormat("HH", Locale.getDefault());
            views.setViewVisibility(((WeatherWidgetApplication) application).getResId("time_ampm_text", "id"), View.GONE);
        }
        SimpleDateFormat minFormat = new SimpleDateFormat("mm", Locale.getDefault());
        SimpleDateFormat secFormat = new SimpleDateFormat("ss", Locale.getDefault());
        Log.d(TAG, "获取到系统当前的时间是 ：strTimeFormat_12_24 " + strTimeFormat_12_24 + "," + hourFormat.format(currentDateTime) + " : " + minFormat.format(currentDateTime) + " : " + secFormat.format(currentDateTime));

        if (getEnableTimeFont()) {
            Log.d(TAG,"enable time font = true");
            views.setImageViewBitmap(((WeatherWidgetApplication) application).getResId("time_hour_imageview", "id"),
                    createTimeBitmap(hourFormat.format(currentDateTime)));
            views.setImageViewBitmap(((WeatherWidgetApplication) application).getResId("time_min_imageview", "id"), createTimeBitmap(minFormat.format(currentDateTime)));
        } else {
            Log.d(TAG,"enable time fone = false");
            views.setTextViewText(((WeatherWidgetApplication) application).getResId("time_hour_text", "id"), hourFormat.format(currentDateTime));
            views.setTextViewText(((WeatherWidgetApplication) application).getResId("time_min_text", "id"), minFormat.format(currentDateTime));
        }

        if (enableColorTheme) {
            Log.d(TAG,"enable color theme = true");
            views.setTextColor(((WeatherWidgetApplication) application).getResId("time_ampm_text", "id"), color);
        }
        preference0 = context.getSharedPreferences("time", MODE_PRIVATE);
        //国内版日期显示格式
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
      if (WeatherWidgetApplication.shareResources.getString(((WeatherWidgetApplication) application).getResId("need_change_time_formate", "string")).equals("yes")) {
           Log.d(TAG,"need change time formate");
            String format = WeatherWidgetApplication.shareResources.getString(((WeatherWidgetApplication) application).getResId("weather_dateformat", "string"));
            dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        }
        views.setTextViewText(((WeatherWidgetApplication) application).getResId("date_text", "id")
                , dateFormat.format(currentDateTime));
        String weekStr = null;
        weekStr = getWeek(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
        Log.d(TAG,"weekStr = "+weekStr);
        views.setTextViewText(((WeatherWidgetApplication) application).getResId("week_text", "id")
                , weekStr);
        return views;
    }

    public String getWeek(int week) {
        String weekStr = null;
        switch (week) {
            case 1:
                weekStr = SkinResource.getSkinStringByName("sunday");
                break;
            case 2:
                weekStr = SkinResource.getSkinStringByName("monday");
                break;
            case 3:
                weekStr = SkinResource.getSkinStringByName("tuesday");
                break;
            case 4:
                weekStr = SkinResource.getSkinStringByName("wednesday");
                break;
            case 5:
                weekStr = SkinResource.getSkinStringByName("thursday");
                break;
            case 6:
                weekStr = SkinResource.getSkinStringByName("friday");
                break;
            case 7:
                weekStr = SkinResource.getSkinStringByName("saturday");
                break;
        }
        return weekStr;
    }

    private void writeTempFile(InputStream is, File temp) throws FileNotFoundException, IOException {
        FileOutputStream out = new FileOutputStream(temp);
        BufferedOutputStream bis = new BufferedOutputStream(out);
        byte buf[] = new byte[128];
        do {
            int numread = is.read(buf);
            if (numread <= 0)
                break;
            bis.write(buf, 0, numread);
        } while (true);
        Log.d(TAG, "writeTempFile");
    }

    private MediaPlayer create(Context context, int resid) {
        InputStream stream = ((WeatherWidgetApplication) application).getShareResources().openRawResource(resid);
        Log.d(TAG, "stream : " + stream);
        if (stream != null)
            return create(context, stream);
        else
            return null;
    }

    private MediaPlayer create(Context context, InputStream stream) {
        MediaPlayer mediaplayer = null;
        try {
            File temp = File.createTempFile("mediaplayertmp", "temp");
            String tempPath = temp.getAbsolutePath();
            FileOutputStream out = new FileOutputStream(temp);
            // 用BufferdOutputStream速度快
            BufferedOutputStream bis = new BufferedOutputStream(out);
            byte buf[] = new byte[128];
            do {
                int numread = stream.read(buf);
                if (numread <= 0)
                    break;
                bis.write(buf, 0, numread);
            } while (true);
            MediaPlayer mp = new MediaPlayer();
            mp.setDataSource(tempPath);
            mp.prepare();
            mediaplayer = mp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaplayer;
    }

    /**
     * 释放上一次MediaPlayer资源
     */
//    private void releaseMediaPlayer() {
//        if (mMediaPlayer_2 != null) {
//            if (mMediaPlayer_2.isPlaying()) {
//                mMediaPlayer_2.stop();
//            }
//            mMediaPlayer_2.release();
//            mMediaPlayer_2 = null;
//        }
//    }

    /**
     * 获取是否使用图片改变时间字体
     *
     * @return
     */
    private boolean getEnableTimeFont() {
        if (((WeatherWidgetApplication) application).getFlyProperty() != null) {
            return ((WeatherWidgetApplication) application).getFlyProperty().getBoolenValue("enableTimeFont");
        }
        return false;
    }

    private Bitmap createTimeBitmap(String time) {
        Bitmap bitmap = null;
        Canvas canvas = null;
        Paint paint = null;
        if (mTypeface == null) {
            mTypeface = ((WeatherWidgetApplication) application).getTypeface();
        }
        if (mTypeface != null) {
            bitmap = Bitmap.createBitmap(SkinResource.getIntegerFromSkin("widget_time_bitmap_width"),
                    SkinResource.getIntegerFromSkin("widget_time_bitmap_height"), Bitmap.Config.ARGB_4444);
            canvas = new Canvas(bitmap);
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setAlpha(SkinResource.getIntegerFromSkin("widget_time_bitmap_alpha"));
            paint.setSubpixelText(true);
            paint.setTypeface(mTypeface);
            paint.setStyle(Paint.Style.FILL);

                paint.setColor(SkinResource.getColorFromSkin("widget_time_bitmap_textcolor"));

            paint.setTextSize(SkinResource.getDimenFromSkin("widget_time_bitmap_textsize"));
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(time, SkinResource.getIntegerFromSkin("widget_time_text_beginx"),
                    SkinResource.getIntegerFromSkin("widget_time_text_beginy"), paint);
        }
        return bitmap;
    }


    public static String getCurrentWeather(Context c, int current) {
        if (current <= 0) {
            SharedPreferences shared = c.getSharedPreferences("weather", 0);
            current = shared.getInt("current", 1);

        }
        Log.d(TAG,"getCurrentWeather current = "+current);
        switch (current) {
            case 1: {
                return "FirstWeather";
            }
            case 2: {
                return "SecondWeather";
            }
            case 3: {
                return "ThirdWeather";
            }
            case 4: {
                return "FouthWeather";
            }
            default:
                return "FirstWeather";
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        Log.d(TAG, "TimeWidget--->onDeleted");
        Intent intent = new Intent(context, WeatherService.class);
        context.stopService(intent);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        Log.d(TAG,"onDisabled");
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        Log.d(TAG,"onEnabled");
    }

    private int getPastTime(Context context, SharedPreferences shared) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月dd日");
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy年MM月dd日");
        now_date = sDateFormat.format(new java.util.Date());
        // 20140221 diff同步机器时间显示
        if (shared != null) {
            sh_date = shared.getString("date_y", "无");
            if (sh_date.equals("无")) {
                return 0;
            }
        } else {
            return 0;
        }
        java.util.Date before = null;
        try {
            before = df.parse(now_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        java.util.Date after = null;
        try {
            after = df.parse(sh_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long l = before.getTime() - after.getTime();
        long day = l / (24 * 60 * 60 * 1000);
        return (int) day;
    }
}
