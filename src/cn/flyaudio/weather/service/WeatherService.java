package cn.flyaudio.weather.service;

import cn.flyaudio.weather.GetWeatherMsgRec;
import cn.flyaudio.weather.WeatherWidget;
import cn.flyaudio.time.AlarmDetails;
import cn.flyaudio.weather.activity.WeatherDetailsActivity;
import cn.flyaudio.weather.data.WeatherWidgetApplication;
import cn.flyaudio.weather.objectInfo.FullWeatherInfo;
import cn.flyaudio.weather.util.Constant;
import cn.flyaudio.weather.util.GetWeatherForCityNameUtil;
import cn.flyaudio.weather.util.GetWeatherUtil;
import cn.flyaudio.weather.util.SPUtils;
import cn.flyaudio.weather.util.UtilsTools;
import cn.flyaudio.weather.view.SkinResource;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.google.gson.Gson;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static cn.flyaudio.weather.data.WeatherWidgetApplication.application;
import static cn.flyaudio.weather.util.Constant.ACTION_APP_GETWEATHER;


public class WeatherService extends Service implements Runnable, AMapLocationListener {
    private final String TAG = "WeatherService";
    private final Boolean DEBUG_FLAG = Constant.DEBUG_FLAG;

    private static boolean sThreadRunning = false;
    public static long updateTime = 0;
    private static long _1hours = 3 * 60 * 60 * 1000;
    private static long _1minute = 60 * 1000;
    private int mCityCount;
    private SharedPreferences preference, preference1, preference2,
            preference3, preference4;
    //	public static boolean backups[] = { false, false, false, false };
    public static boolean backup = false;

    private List<FullWeatherInfo> citiesWeatherList = null;
    private ArrayList<String> mCities = null;


    final static String ACTION_TIMEBROADCAST = "android.intent.action.timebroadcast";

    public BroadcastReceiver alarmReceiver;
    private SharedPreferences preference0;
//    private MediaPlayer mMediaPlayer_2, mMediaPlayer_1;
    final static String MediaAlarm = "Flyaudio3_TimeService.intent.action.MediaAlarm";
    private int RingSound = 0;
    private Typeface mTypeface;

    private String strTimeFormat_12_24 = "";
    public static int color = Color.RED;
    public static boolean enableColorTheme = true;
    Drawable whiteBg = null;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, " -----onCreate ");
        preference = getSharedPreferences("weather", MODE_PRIVATE);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK); // 时间的流逝，以分钟为单位
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED); // 时间被改变，人为设置时间
        intentFilter.addAction("action.flyaudio.updateTimeByGPS");
        if (enableColorTheme) {
            whiteBg = ((WeatherWidgetApplication) getApplicationContext()).
                    getShareResources().getDrawable(((WeatherWidgetApplication)
                    application).getResId("white_bg", "drawable"));
            intentFilter.addAction("action.flyaudio.colortheme");
            String c = SystemProperties.get("persist.fly.colortheme", "red");

            try {
                color = Integer.valueOf(c);
            } catch (Exception e) {
                // TODO: handle exception
                color = Color.RED;
            }
        }
        ContentResolver mProvider = WeatherWidgetApplication.getContext().getContentResolver();
        strTimeFormat_12_24 = android.provider.Settings.System.getString(mProvider,
                android.provider.Settings.System.TIME_12_24);
        if (strTimeFormat_12_24 == null) {
            strTimeFormat_12_24 = "";
        }
        Log.d(TAG, "onCreate >>>strTimeFormat_12_24==" + strTimeFormat_12_24);

        registerReceiver(boroadcastReceiver, intentFilter);
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(MediaAlarm);
        registerReceiver(alarmReceiver, iFilter);

        preference0 = getSharedPreferences("time", MODE_PRIVATE);
        if (!preference0.contains("alarmOrNot")) {
            SharedPreferences.Editor editor = preference0.edit();
            editor.putString("alarmOrNot", "no");
            editor.commit();
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "--onStartCommdd()--intent=" + intent);
        updateUI(color); // 开始服务前先刷新一次UI
        Log.d(TAG, "[WeatherService] onStart()  sThreadRunning == " + sThreadRunning);

        if (!sThreadRunning) {
            sThreadRunning = true;
            new Thread(this).start();
        }
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Flyaudio3_TimeService--onDestroy()--");
        unregisterReceiver(boroadcastReceiver);
        this.startService(new Intent(this, WeatherService.class));
    }


    // 用于监听系统时间变化Intent.ACTION_TIME_TICK的BroadcastReceiver，此BroadcastReceiver须为动态注册
    public BroadcastReceiver boroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context acontext, Intent intent) {
            Log.d(TAG, "Flyaudio3_TimeService--监听系统时间变化广播--");
            Log.d(TAG, intent.getAction());
            if (intent.getAction().equals("action.flyaudio.colortheme")) {
                int rgb = intent.getIntExtra("rgb", -1);
                Log.d(TAG, "Flyaudio3_TimeService  onReceive:" + rgb);
                color = rgb;
                updateUI(color);
            } else if (intent.getAction().equals("action.flyaudio.updateTimeByGPS")) {
                Log.d(TAG, " receive action.flyaudio.updateTimeByGPS action .");
                if (SystemProperties.get("persist.fly.usegps", "no").equals("yes")) {
                    Log.d(TAG, "new gps : " + SystemProperties.get("persist.fly.usegps", "no").toString());
                    updateUI(color);
                }
            } else if (intent.getAction().equals("android.intent.action.TIME_SET")) {
                ContentResolver mProvider = WeatherWidgetApplication.getContext().getContentResolver();
                strTimeFormat_12_24 = android.provider.Settings.System.getString(mProvider,
                        android.provider.Settings.System.TIME_12_24);
                if (strTimeFormat_12_24 == null) {
                    strTimeFormat_12_24 = "";
                }
                updateUI(color);
            } else {
                Log.d(TAG, "receive other action");
               // updateUI(color);
            }

        }
    };


    // 根据当前时间设置小部件相应的数字图片
    private void updateUI(int color) {
        /*
        Log.d(TAG, "Flyaudio3_TimeService--->updateUI");
        RemoteViews remoteViews = null;
        remoteViews = new RemoteViews(((WeatherWidgetApplication) application).getSharePackageName(),
                ((WeatherWidgetApplication) application).getResId("widget_layout", "layout"));


        Date currentDateTime = Calendar.getInstance().getTime();
        SimpleDateFormat AM_PM_hourFormat = new SimpleDateFormat("HH", Locale.getDefault());
        int hournum = Integer.parseInt(AM_PM_hourFormat.format(currentDateTime));
        String amPmStr = null;
        SimpleDateFormat hourFormat = null;
        Log.d(TAG, " updateUI：strTimeFormat_12_24 " + strTimeFormat_12_24);
        if (strTimeFormat_12_24.equals("12")) {
            hourFormat = new SimpleDateFormat("hh", Locale.getDefault());
            if (0 <= hournum && hournum < 12)
                amPmStr = "AM";
            else
                amPmStr = "PM";
            remoteViews.setViewVisibility(((WeatherWidgetApplication) application).getResId("time_ampm_text", "id"), View.VISIBLE);
            remoteViews.setTextViewText(((WeatherWidgetApplication) application).getResId("time_ampm_text", "id"), amPmStr);
        } else {
            hourFormat = new SimpleDateFormat("HH", Locale.getDefault());
            remoteViews.setViewVisibility(((WeatherWidgetApplication) application).getResId("time_ampm_text", "id"), View.GONE);
        }
        SimpleDateFormat minFormat = new SimpleDateFormat("mm", Locale.getDefault());
        SimpleDateFormat secFormat = new SimpleDateFormat("ss", Locale.getDefault());

        Log.d(TAG, "获取到系统当前的时间是 ：strTimeFormat_12_24 " + strTimeFormat_12_24 + "," + hourFormat.format(currentDateTime) + " : " + minFormat.format(currentDateTime) + " : " + secFormat.format(currentDateTime));

        if (getEnableTimeFont()) {
            remoteViews.setImageViewBitmap(((WeatherWidgetApplication) application).getResId("time_hour_imageview", "id"),
                    createTimeBitmap(hourFormat.format(currentDateTime)));
            remoteViews.setImageViewBitmap(((WeatherWidgetApplication) application).getResId("time_min_imageview", "id"), createTimeBitmap(minFormat.format(currentDateTime)));
        } else {
            remoteViews.setTextViewText(((WeatherWidgetApplication) application).getResId("time_hour_text", "id"), hourFormat.format(currentDateTime));
            remoteViews.setTextViewText(((WeatherWidgetApplication) application).getResId("time_min_text", "id"), minFormat.format(currentDateTime));
        }

        if (enableColorTheme) {
            remoteViews.setTextColor(((WeatherWidgetApplication) application).getResId("time_ampm_text", "id"), color);
        }
        preference0 = getSharedPreferences("time", MODE_PRIVATE);
        if (preference0.getString("alarmOrNot", "no").equals("yes")) {
            Bitmap bmp = null;
            bmp = BitmapFactory.decodeResource(((WeatherWidgetApplication) application).getShareResources(),
                    ((WeatherWidgetApplication) application).getResId("time_clockflag", "drawable"));

            remoteViews.setImageViewBitmap(((WeatherWidgetApplication) application).getResId("timelogo", "id"), bmp);
        } else {
            Bitmap bmp = BitmapFactory.decodeResource(((WeatherWidgetApplication) application).getShareResources(),
                    ((WeatherWidgetApplication) application).getResId("time_clockflag_bg", "drawable"));
        }
//        if (minFormat.format(currentDateTime).equals("00") && preference0.getString("alarmOrNot", "no").equals("yes")
//                && secFormat.format(currentDateTime).equals("00")) {
//            if (getResources().getConfiguration().locale.getCountry().equals("CN")) {// 中文版声音
//                for (int i = 0; i < 24; i++) {
//                    String hour = AM_PM_hourFormat.format(currentDateTime);
//                    if (hour.equals(i < 10 ? ("0" + i) : (i + ""))) {
//                        RingSound = ((WeatherWidgetApplication) application).getResId("time_b" + i, "raw");
//                        break;
//                    }
//                }
//            } else {// 英文版声音
//                for (int i = 0; i < 24; i++) {
//                    String hour = AM_PM_hourFormat.format(currentDateTime);
//                    if (hour.equals(i < 10 ? ("0" + i) : (i + ""))) {
//                        RingSound = ((WeatherWidgetApplication) application).getResId("time_e" + i, "raw");
//                        break;
//                    }
//                }
//            }
//
//            Intent intent = new Intent(ACTION_TIMEBROADCAST);
//            intent.putExtra("TIMEBROADCAST", "play");
//            sendBroadcast(intent);
//            Log.d(TAG, "Flyaudio3_TimeService--sendBroadcast" + ACTION_TIMEBROADCAST + "---play");
//
//            InputStream is1 = ((WeatherWidgetApplication) application).getShareResources().openRawResource(((WeatherWidgetApplication) application).getResId("time_gg_big", "raw"));
//            InputStream is2 = ((WeatherWidgetApplication) application).getShareResources().openRawResource(RingSound);
//
//            String tempPath1 = "";
//            String tempPath2 = "";
//            try {
//                File temp1 = File.createTempFile("mediaplayertmp1", "temp1");
//                File temp2 = File.createTempFile("mediaplayertmp2", "temp2");
//                tempPath1 = temp1.getAbsolutePath();
//                tempPath2 = temp2.getAbsolutePath();
//                writeTempFile(is1, temp1);
//                writeTempFile(is2, temp2);
//                Log.d(TAG, "Flyaudio3_TimeService--临时文件1路径：=" + tempPath1);
//                Log.d(TAG, "Flyaudio3_TimeService--临时文件2路径：=" + tempPath2);
//            } catch (Exception e2) {
//                Log.d(TAG, "Flyaudio3_TimeService--创建临时文件出错！!!" + e2);
//            }
//            mMediaPlayer_1.reset();
//            mMediaPlayer_2.reset();
//            try {
//                mMediaPlayer_1.setDataSource(tempPath1);
//                mMediaPlayer_2.setDataSource(tempPath2);
//                mMediaPlayer_1.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
//                mMediaPlayer_2.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
//                mMediaPlayer_1.prepare();
//                mMediaPlayer_2.prepare();
//            } catch (IllegalArgumentException e1) {
//                e1.printStackTrace();
//            } catch (SecurityException e1) {
//                e1.printStackTrace();
//            } catch (IllegalStateException e1) {
//                e1.printStackTrace();
//            } catch (IOException e1) {
//                e1.printStackTrace();
//            }
//            new Handler().postDelayed(new Runnable() {
//                public void run() {
//                    try {
//                        Log.d(TAG, "开始播放第一段");
//                        mMediaPlayer_1.start();
//                    } catch (Exception e) {
//                        // TODO: handle exception
//                        Log.d(TAG, "播放第一段异常：" + e.toString());
//                    }
//                }
//
//            }, 500);
//
//            mMediaPlayer_1.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mp) {
//                    Log.d(TAG, "第一段播放结束");
//                    try {
//                        Log.d(TAG, "开始播放第二段");
//                        mMediaPlayer_2.start();
//                    } catch (Exception e) {
//                        // TODO: handle exception
//                        Log.d(TAG, "播放第二段异常：" + e.toString());
//                    }
//
//                }
//            });
//            mMediaPlayer_2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mp) {
//                    mMediaPlayer_1.release();
//                    Log.d(TAG, "第二段播放结束");
//                    Intent intent = new Intent(ACTION_TIMEBROADCAST);
//                    intent.putExtra("TIMEBROADCAST", "stop");
//                    Log.d(TAG, "Flyaudio3_TimeService--sendBroadcast" + ACTION_TIMEBROADCAST + "---stop");
//                    sendBroadcast(intent);
//                }
//            });
//
//        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());//国内版日期显示格式

        if (WeatherWidgetApplication.shareResources.getString(((WeatherWidgetApplication) application).getResId("need_change_time_formate", "string")).equals("yes")) {
            Log.d("QQ", " need_change_time_formate ");
            String format = WeatherWidgetApplication.shareResources.getString(((WeatherWidgetApplication) application).getResId("weather_dateformat", "string"));
//            Log.i("AAA", "format:" + format);

            dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        }

        remoteViews.setTextViewText(((WeatherWidgetApplication) application).getResId("date_text", "id")
                , dateFormat.format(currentDateTime));
        String weekStr = null;
        weekStr = getWeek(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
        remoteViews.setTextViewText(((WeatherWidgetApplication) application).getResId("week_text", "id")
                , weekStr);
//        Log.e("AAA", "format:" + weekStr);
//        Log.e("AAA", "format:" + Calendar.getInstance().get(Calendar.DAY_OF_WEEK));

        Intent detailIntent1 = new Intent(this, AlarmDetails.class);
        PendingIntent pending1 = PendingIntent.getActivity(this, 0, detailIntent1, 0);
        remoteViews.setOnClickPendingIntent(((WeatherWidgetApplication) application).getResId("time_main", "id"),
                pending1);

        // 将AppWidgetProvider的子类包装成ComponentName对象
        ComponentName componentName = new ComponentName(this, WeatherWidget.class);
        // 调用AppWidgetManager将remoteViews添加到ComponentName中
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        if (appWidgetManager == null) {
            // appWidgetManager= TimeApplication.appWidgetManager;
            Log.d(TAG, "appWidgetManager == null");
            WeatherService.this.sendBroadcast(new Intent("test"));
            return;
        }
        appWidgetManager.updateAppWidget(componentName, remoteViews);
        Log.d("WeatherWidget","updateUI action");
        sendBroadcast(new Intent(Constant.ACTION_APPWIDGET_UPDATE));

        */
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


//    private MediaPlayer create(Context context, InputStream stream) {
//        MediaPlayer mediaplayer = null;
//        try {
//            File temp = File.createTempFile("mediaplayertmp", "temp");
//            String tempPath = temp.getAbsolutePath();
//            FileOutputStream out = new FileOutputStream(temp);
//            // 用BufferdOutputStream速度快
//            BufferedOutputStream bis = new BufferedOutputStream(out);
//            byte buf[] = new byte[128];
//            do {
//                int numread = stream.read(buf);
//                if (numread <= 0)
//                    break;
//                bis.write(buf, 0, numread);
//            } while (true);
//            MediaPlayer mp = new MediaPlayer();
//            mp.setDataSource(tempPath);
//            mp.prepare();
//            mediaplayer = mp;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return mediaplayer;
//    }
//

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


    @Override
    public void run() {
        //获取天气数据。定义给一个天气对象的对象数组
        mCities = new ArrayList<String>();

        ArrayList<String> mCitieNames = new ArrayList<String>();
        ArrayList<String> cityNameEnLists = new ArrayList<String>();
        mCityCount = 0;
        //sp获取选中的四个城市，如果大于1就添加到城市id 列表中，
        for (int i = 1; i < 5; i++) {
            String city = preference.getString("" + i, null);
            String cityen = preference.getString("" + i * 10, null);
            if (!TextUtils.isEmpty(city)) {
                mCitieNames.add(city);
                cityNameEnLists.add(cityen);
            }
        }
        final String city1 = preference.getString("city1", null);
        if (!TextUtils.isEmpty(city1)) {
            mCityCount++;
            mCities.add(city1);
        }

        final String city2 = preference.getString("city2", null);
        if (!TextUtils.isEmpty(city2)) {
            mCityCount++;
            mCities.add(city2);
        }

        final String city3 = preference.getString("city3", null);
        if (!TextUtils.isEmpty(city3)) {
            mCityCount++;
            mCities.add(city3);
        }

        final String city4 = preference.getString("city4", null);
        if (!TextUtils.isEmpty(city4)) {
            mCityCount++;
            mCities.add(city4);
        }

        Log.d(TAG, "mCityCount = " + mCityCount + ",mCities.size = " + mCities.size());
        //get all city weather info
        if (mCityCount != 0 && !backup) {
            backup = true;
            GetWeatherUtil.Instance().getAllWeatherContentForOKGO(mCities, new GetWeatherForCityNameUtil.CallBackWeatherList() {
                @Override
                public void onSuccess(List<FullWeatherInfo> fullWeatherInfoList) {
                    if (fullWeatherInfoList != null && fullWeatherInfoList.size() > 0) {
                        FullWeatherInfo fullWeatherInfo = fullWeatherInfoList.get(0);
                        SPUtils.getInstance().put("condition_temp", fullWeatherInfo.getCondition_temp());
                        SPUtils.getInstance().put("condition_code", fullWeatherInfo.getCondition_code());
                        String json = new Gson().toJson(fullWeatherInfoList);
                        SPUtils.getInstance().put("weather_json", json);
                        sThreadRunning = false;
                        notifyForUpdateUI(0);
                        notifyForUpdateItemUI("");
                        backup = false;
                        Log.d(TAG,"getAllWeatherContent====full=="+fullWeatherInfoList.toString());
                    }
                    getlocation();
                }

                @Override
                public void onError(String errmsg) {
                    notifyForUpdateUI(0);
                    notifyForUpdateItemUI(errmsg);
                }
            }, mCitieNames, cityNameEnLists);

        }
        updateTime = getUpdateTime();

        Intent updateIntent = new Intent(Constant.ACTION_UPDATE_ALL);

        updateIntent.setClass(this, WeatherService.class);
        //后台服务
        PendingIntent pending = PendingIntent.getService(this, 0, updateIntent, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Time time = new Time();
        long nowMillis = System.currentTimeMillis();
        time.set(nowMillis + updateTime);
        long updateTimes = time.toMillis(true);
        alarm.set(AlarmManager.RTC_WAKEUP, updateTimes, pending);
        sThreadRunning = false;

        Log.d(TAG, "run()====   sThreadRunning ==  " + sThreadRunning);
        if (DEBUG_FLAG)
            Log.d(TAG, "[WeatherService] run()  request next updateTime at"
                    + updateTime);

    }

    private void getlocation() {
        GetWeatherForCityNameUtil.GetInstance().GetLocation(new GetWeatherForCityNameUtil.LocationCallBack() {
            @Override
            public void onSuccess(String toStr, String toString, String city, String address) {
                Log.d("yexingyun", "getweathermsg: tostr" + toStr);
                Log.d("yexingyun", "getweathermsg: toString" + toString);
                Log.d("yexingyun", "getweathermsg: city" + city);
                Log.d("yexingyun", "getweathermsg: address" + address);

                SPUtils.getInstance().put("city", city);
                SPUtils.getInstance().put("toStr", toStr);
                SPUtils.getInstance().put("toString", toString);
                SPUtils.getInstance().put("address", address);
                if (city.contains("市")) {
                    String substring = city.substring(0, city.length() - 1);
                    Log.d("yexingyun", "city======" + substring);
                    initWeatherData(substring);
//                    initWeatherData(substring);
                } else {
                    Log.d("yexingyun", "city======" + city);
                    initWeatherData(city);

                }
//                Toast.makeText(WeatherDetailsActivity.this,city+"--"+address,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String errmsg) {
                Intent intent = new Intent(Constant.ACTION_APP_GETWEATHERCALLBACK);
                intent.putExtra("errmsg", errmsg);
//                Toast.makeText(WeatherDetailsActivity.this,"errmsg"+"--"+errmsg,Toast.LENGTH_SHORT).show();

                sendBroadcast(intent);
            }
        });
    }

    private void initWeatherData(String substring) {
        GetWeatherForCityNameUtil.GetInstance().GetWeatherForCityName(WeatherService.this, substring, new GetWeatherForCityNameUtil.CallBackWeatherList() {
            @Override
            public void onSuccess(List<FullWeatherInfo> fullWeatherInfoList) {
                if (fullWeatherInfoList != null && fullWeatherInfoList.size() > 0) {
                    String json = new Gson().toJson(fullWeatherInfoList);
                    SPUtils.getInstance().put("weather_json", json);
                    sThreadRunning = false;
                    notifyForUpdateUI(0);
                    notifyForUpdateItemUI("");
                    backup = false;
//                    Log.d("yexingyun","initweather====location=="+fullWeatherInfoList.get(0).toString());
                    Log.d("yexingyun","initweather====location=="+fullWeatherInfoList.get(0).getYweathers().toString());

                }

            }

            @Override
            public void onError(String errmsg) {
                notifyForUpdateUI(0);
                notifyForUpdateItemUI(errmsg);
            }
        });
    }


    private void notifyForUpdateUI(int current) {
        Log.d("WeatherWidget","notifyForUpdateUI action ");
        this.sendBroadcast(new Intent(Constant.ACTION_STOP_FRESH));
        this.sendBroadcast(new Intent(Constant.ACTION_APPWIDGET_UPDATE));
        Intent intent = new Intent(Constant.ACTION_UPDATEUI_VIEWFLOW);
        intent.putExtra("current", current);
        this.sendBroadcast(intent);
    }

    private void notifyForUpdateItemUI(String errmsg) {
        Intent intent = new Intent(Constant.ACTION_UPITEMUI);
        intent.putExtra("errmsg", errmsg);
        this.sendBroadcast(intent);
    }


    private long getUpdateTime() {

        long systemMillis = System.currentTimeMillis();
        if (UtilsTools.isConnect(WeatherService.this)) {
            for (int i = 0; i < mCityCount; i++) {
                if (!backup) {
                    Log.d(TAG, "[WeatherService] getUpdateTime()  12s");
                    return 12000;
                }
            }
        }

        ((WeatherWidgetApplication) getApplicationContext()).setServiceRunning(false);
        sendBroadcast(new Intent(Constant.ACTION_STOP_FRESH));
        Log.d(TAG, "[WeatherService] getUpdateTime()  sendBroadcast Constant.ACTION_STOP_FRESH");
        for (int i = 0; i < 4; i++) {
            backup = false;
            if (DEBUG_FLAG)
                Log.d(TAG, "[WeatherService] getUpdateTime()  backup" + (i + 1) + " = " + backup);
        }
        if (DEBUG_FLAG)
            Log.d(TAG, "[WeatherService] " + "() _1hours - ((systemMillis) % (_1hours))="
                    + (_1hours - ((systemMillis) % (_1hours))));
        // udatetime in next 1hour_1minute 10:01 11:01
        return _1hours - ((systemMillis) % (_1hours)) + _1minute;
    }

    public static boolean isServiceRunning() {
        return updateTime == 12000;
    }


    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {

    }
}