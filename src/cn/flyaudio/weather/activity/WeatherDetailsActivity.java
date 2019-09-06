package cn.flyaudio.weather.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Message;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;
import cn.flyaudio.weather.GetWeatherMsgRec;
import cn.flyaudio.base.BaseActivity;
import cn.flyaudio.base.MvpBasePresenter;
import cn.flyaudio.weather.adapter.ViewFlowAdapter;
import cn.flyaudio.weather.data.WeatherWidgetApplication;
import cn.flyaudio.weather.objectInfo.FullWeatherInfo;
import cn.flyaudio.weather.service.WeatherService;
import cn.flyaudio.weather.util.Constant;
import cn.flyaudio.weather.util.GetWeatherForCityNameUtil;
import cn.flyaudio.weather.util.SPUtils;
import cn.flyaudio.weather.util.ToastUtil;
import cn.flyaudio.weather.util.UtilsTools;
import cn.flyaudio.weather.view.SkinResource;
import cn.flyaudio.weather.view.ViewFlow;

import static cn.flyaudio.weather.util.Constant.ACTION_APP_GETWEATHER;


public class WeatherDetailsActivity extends BaseActivity {
    private final String TAG = "WeatherDetailsActivity";
    private final Boolean DEBUG_FLAG = Constant.DEBUG_FLAG;
    private ViewFlow mViewFlow;
    private Button mFreshButton1, mBackButton, mChooseCityButton, mFreshButton;
    private ViewFlowAdapter mViewFlowAdapter;
    private UpdateUIReceiver mUIReceiver;
    private Animation mAnim;
    private View weatherdetailview;
    public static int widthPixels = 0;
    private final int NOTIFYDATACHANGED = 0;


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
//            showToast("天气数据已更新完成");
            mViewFlowAdapter.notifyDataSetChanged();
            if (DEBUG_FLAG)
                Log.d(TAG,
                        "[WeatherDetailsActivity] Notify mViewFlowAdapter Changed...");
        }
    };
    private NetworkChangeReceiver networkChangeReceiver;
    private TextView tvError;


    @Override
    protected MvpBasePresenter getPresenter() {
        return null;
    }

    private void init() {

        weatherdetailview = findViewById(SkinResource.getSkinResourceId("weatherdetail", "id"));
        mViewFlow = (ViewFlow) findViewById(SkinResource.getSkinResourceId("viewflow", "id"));
        tvError= (TextView) findViewById(SkinResource.getSkinResourceId("tv_error", "id"));
        mAnim = AnimationUtils.loadAnimation(SkinResource.getSkinContext(), SkinResource.getSkinResourceId("weather_loading", "anim"));
        mAnim.setInterpolator(new LinearInterpolator());

        mFreshButton1 = (Button) findViewById(SkinResource.getSkinResourceId("freshbutton", "id"));

        mFreshButton = (Button) findViewById(SkinResource.getSkinResourceId("fresh_button", "id"));
        mFreshButton.setOnClickListener(new FreshClickListener());

        mBackButton = (Button) findViewById(SkinResource.getSkinResourceId("back_button1", "id"));
        mBackButton.setOnClickListener(new BackClickListener());

        mChooseCityButton = (Button) findViewById(SkinResource.getSkinResourceId("choosecity_button", "id"));
        mChooseCityButton.setOnClickListener(new ChooseCityListener());
        mViewFlowAdapter = new ViewFlowAdapter(WeatherDetailsActivity.this);

        String weatherJson = SPUtils.getInstance().getString("weather_json", "");
        if (TextUtils.isEmpty(weatherJson)) {
            //第一次打开应用的时候调用
           mViewFlow.setAdapter(mViewFlowAdapter, 0);
           initFisrtWeatherData(SPUtils.getInstance().getString("city", "广州"));
        } else {
            SharedPreferences shared = getSharedPreferences("weather", 0);
            int currentpage = shared.getInt("current", 0) == 0 ? 0 : shared.getInt(
                    "current", 0) - 1;
            for (int i = 0; i < currentpage; i++) {
                if (shared.getString(String.valueOf(i + 1), "").equals("")) {
                    currentpage = currentpage - 1;
                }
            }

            mViewFlow.setAdapter(mViewFlowAdapter, currentpage);
            List<FullWeatherInfo> fullWeatherInfoList = new Gson().fromJson(weatherJson, new TypeToken<List<FullWeatherInfo>>() {
            }.getType());
            mViewFlowAdapter.setData(fullWeatherInfoList);

        }
        registerIntentFilter();
    }

    private void initFisrtWeatherData(String cityName) {
//        showToast("正在初始化天气信息...");
        //首次进入应用的时候更新天气信息
        if(!UtilsTools.isNetworkAvailable(WeatherDetailsActivity.this)){
            tvError.setVisibility(View.VISIBLE);
            return;
        }
        GetWeatherForCityNameUtil.GetInstance().GetWeatherForCityName(WeatherDetailsActivity.this, cityName, new GetWeatherForCityNameUtil.CallBackWeatherList() {
            @Override
            public void onSuccess(List<FullWeatherInfo> fullWeatherInfoList) {
                tvError.setVisibility(View.GONE);
                SPUtils shared = SPUtils.getInstance();
                int currentpage = shared.getInt("current", 0) == 0 ? 0 : shared.getInt(
                        "current", 0) - 1;
                if (DEBUG_FLAG)
                    Log.d(TAG, "[WeatherDetailsActivity]   currentpage---->"
                            + currentpage);

                for (int i = 0; i < currentpage; i++) {
                    if (shared.getString(String.valueOf(i + 1), "").equals("")) {
                        currentpage = currentpage - 1;
                    }
                }
                mViewFlow.setAdapter(mViewFlowAdapter, currentpage);
                List<FullWeatherInfo> fullWeatherInfoListone = new ArrayList<>();
                FullWeatherInfo fullWeatherInfo = fullWeatherInfoList.get(0);
                fullWeatherInfoListone.add(fullWeatherInfo);
                SPUtils spUtils = SPUtils.getInstance();
                Log.d(TAG,"temp = "+fullWeatherInfo.getCondition_temp()+" code = "+fullWeatherInfo.getCondition_code()+"json = "+new Gson().toJson(fullWeatherInfoListone));
                spUtils.put("condition_temp", fullWeatherInfo.getCondition_temp());
                spUtils.put("condition_code", fullWeatherInfo.getCondition_code());
                spUtils.put("weather_json", new Gson().toJson(fullWeatherInfoListone));
                mViewFlowAdapter.setData(fullWeatherInfoListone);
            }

            @Override
            public void onError(String errmsg) {
              //  showToast(errmsg);
                tvError.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"onStop");
        try {
            if (mUIReceiver != null) {
                unregisterReceiver(mUIReceiver);
            }
        } catch (Exception e) {
            Log.d(TAG, "unregisterReceiver:" + e.getMessage());
            e.printStackTrace();
        }
    }

    private void registerIntentFilter() {
        mUIReceiver = new UpdateUIReceiver();
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(Constant.ACTION_UPDATEUI_VIEWFLOW);
        iFilter.addAction(Constant.ACTION_APPWIDGET_UPDATE);
        iFilter.addAction(Constant.ACTION_UPDATEUI);
        iFilter.addAction(Constant.ACTION_START_FRESH);
        iFilter.addAction(Constant.ACTION_STOP_FRESH);
        iFilter.addAction(Constant.ACTION_UPITEMUI);
        iFilter.addAction(Constant.ACTION_REQUEST_WEATHER);
        registerReceiver(mUIReceiver, iFilter);
    }


    @Override
    protected void onResume() {
        Log.d(TAG,"onResume");
        super.onResume();
        init();
        WeatherWidgetApplication.getlanguage();

        if (((WeatherWidgetApplication) getApplicationContext()).isServiceRunning()) {
            if (UtilsTools.isNetworkAvailable(this)) {
                Intent intent = new Intent(this, WeatherService.class);
                startService(intent);
            } else {
                mFreshButton1.setVisibility(View.INVISIBLE);
                mFreshButton.setVisibility(View.VISIBLE);
                Toast.makeText(this,
                        SkinResource.getSkinStringByName("neworkconnect"),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected int getlayoutId() {
        int initPreloadViewsID = this.getResources().getIdentifier("details_activity_layout", "layout", this.getPackageName());

        return initPreloadViewsID;
    }

    @Override
    protected void initView() {
        Log.d(TAG,"initView");

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, intentFilter);

    }



    private class UpdateUIReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub ACTION_APPWIDGET_UPDATE
            if (intent.getAction().equals(Constant.ACTION_UPDATEUI_VIEWFLOW)) {
                Message msg = mHandler.obtainMessage(NOTIFYDATACHANGED);
                mHandler.sendMessage(msg);
            } else if (intent.getAction().equals(Constant.ACTION_STOP_FRESH)) {
                mFreshButton1.clearAnimation();
                mFreshButton1.setVisibility(View.INVISIBLE);
                mFreshButton.setVisibility(View.VISIBLE);
            } else if (intent.getAction().equals(Constant.ACTION_START_FRESH)) {
                mFreshButton1.startAnimation(mAnim);
                mFreshButton1.setVisibility(View.VISIBLE);
                mFreshButton.setVisibility(View.GONE);

            } else if (intent.getAction().equals(Constant.ACTION_APPWIDGET_UPDATE)) {
                tvError.setVisibility(View.GONE);
                mViewFlow.setVisibility(View.VISIBLE);
                String weatherJson = SPUtils.getInstance().getString("weather_json", "");
                Log.d("yexingyun","weather_json==UpdateUIReceiver===="+weatherJson);
                List<FullWeatherInfo> fullWeatherInfoList = new Gson().fromJson(weatherJson, new TypeToken<List<FullWeatherInfo>>() {
                }.getType());
                mViewFlowAdapter.setData(fullWeatherInfoList);
            } else if (intent.getAction().equals(Constant.ACTION_UPITEMUI)) {
                String errmsg = intent.getStringExtra("errmsg");
                Log.d("yexingyun", "errmsg==" + errmsg);
                if (!TextUtils.isEmpty(errmsg)) {
                   // ToastUtil.show(WeatherDetailsActivity.this, errmsg);
                }
            }

        }
    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        Log.d(TAG,"onDestroy");
        super.onDestroy();
//        if (mUIReceiver != null) {
//            unregisterReceiver(mUIReceiver);
//        }
        if (DEBUG_FLAG) {
            Log.d(TAG, "[WeatherDetailsActivity][onDestroy]");
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        unregisterReceiver(networkChangeReceiver);
    }

    private class ChooseCityListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            startActivity(CityEditPageActivity.class);
        }
    }

    private class BackClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            Intent mIntentHome = new Intent(Constant.ACTION_HOME);
            mIntentHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(mIntentHome);
            } catch (Exception e) {
            } finally {
                finish();
            }
        }
    }

    private class FreshClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            FreshData();
//            getlocaltion();
        }
    }

    private void FreshData() {
        cancelToast();
        if (UtilsTools.isNetworkAvailable(WeatherDetailsActivity.this)) {
            Log.e(TAG, "----WeatherService.isServiceRunning()--1" + WeatherService.isServiceRunning());
            if (!WeatherService.isServiceRunning()) {
                Intent intent = new Intent(WeatherDetailsActivity.this,WeatherService.class);
                startService(intent);
                Log.e(TAG, "----WeatherService.isServiceRunning()--2" + WeatherService.isServiceRunning());
            }
           // showToast(SkinResource.getSkinStringByName("showToast"));
            mFreshButton1.setVisibility(View.VISIBLE);
            mFreshButton1.startAnimation(mAnim);
            mFreshButton.setVisibility(View.GONE);
        } else {
           // showToast(SkinResource.getSkinStringByName("neworkconnect"));
        }
    }

    private class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"onReceive NetworkChangeReceiver");
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable()) {
                if (!WeatherService.isServiceRunning()) {
                    Intent mintent = new Intent(WeatherDetailsActivity.this,WeatherService.class);
                    startService(mintent);
                    Log.e(TAG, "----WeatherService.isServiceRunning()--2" + WeatherService.isServiceRunning());
                }
                Log.d(TAG,"网络可用,刷新数据");
            }else {
                Log.d(TAG,"网络不可用");
            }
        }
    }
}
