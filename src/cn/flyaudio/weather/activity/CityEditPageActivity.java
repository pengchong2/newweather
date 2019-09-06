package cn.flyaudio.weather.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.GridView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cn.flyaudio.base.BaseActivity;
import cn.flyaudio.base.MvpBasePresenter;
import cn.flyaudio.weather.activity.presenter.AddCityPresenter;
import cn.flyaudio.weather.activity.view.IAddCityView;
import cn.flyaudio.weather.adapter.CityAdapter;
import cn.flyaudio.weather.data.WeatherWidgetApplication;
import cn.flyaudio.weather.objectInfo.FullWeatherInfo;
import cn.flyaudio.weather.util.Constant;
import cn.flyaudio.weather.util.SPUtils;
import cn.flyaudio.weather.view.SkinResource;


public class CityEditPageActivity extends BaseActivity implements IAddCityView {
    private GridView gridview;
    public List<String> mCityList;// 城市名称
    public List<String> mCityEnList;// 城市名称
    public List<String> mCityCodeList;// 城市代码
    CityAdapter cityAdapter;
    private UpdateItenUIReceiver mItemUIReceiver;
    private AddCityPresenter addCityPresenter;
    private MyHandler mHandler;
    private List<FullWeatherInfo> mForecastyweathers;
    private Animation mAnim;
    private Button freshButton;

    @Override
    protected int getlayoutId() {
        int initPreloadViewsID = this.getResources().getIdentifier("cityedit_activity_layout", "layout", this.getPackageName());
        return initPreloadViewsID;
    }

    @Override
    protected void initView() {
        init();
        Button addButton = (Button) this.findViewById(SkinResource.getSkinResourceId("addbutton", "id"));
        freshButton = (Button) this.findViewById(SkinResource.getSkinResourceId("freshbutton", "id"));
        mAnim = AnimationUtils.loadAnimation(SkinResource.getSkinContext(), SkinResource.getSkinResourceId("weather_loading", "anim"));
        mAnim.setInterpolator(new LinearInterpolator());

        addButton.setOnClickListener(new AddClickListener());
        mHandler = new MyHandler(this);
        cityAdapter = new CityAdapter(this, mHandler);
        gridview = (GridView) this.findViewById(SkinResource.getSkinResourceId("gridview", "id"));
        gridview.setNumColumns(1);
        gridview.setAdapter(cityAdapter);

        mCityList = new ArrayList<>();
        mCityCodeList = new ArrayList<String>();
        mCityEnList = new ArrayList<>();
        mForecastyweathers = new ArrayList<>();
        addCityPresenter.getCityLiAndCityCodeLiFormSpPresenter();

        String weatherJson = SPUtils.getInstance().getString("weather_json", "");
        mForecastyweathers = new Gson().fromJson(weatherJson, new TypeToken<List<FullWeatherInfo>>() {
        }.getType());
        cityAdapter.setData(WeatherWidgetApplication.isCNLanguage ? mForecastyweathers : mForecastyweathers);

    }

    @Override
    public void showData(List<String> mStringArray, String[] cityname) {

    }

    @Override
    public int getPager() {
        return 0;
    }

    @Override
    public void getCityCodeList(List<String> mCityList, List<String> mCityCodeList, List<String> mCityEnList) {
        this.mCityCodeList.clear();
        this.mCityEnList.clear();
        this.mCityList.clear();

        this.mCityList.addAll(mCityList);
        this.mCityCodeList.addAll(mCityCodeList);
        this.mCityEnList.addAll(mCityEnList);
        //绑定适配器
//        cityAdapter = new CityAdapter(CityEditPageActivity.this, mHandler);
        cityAdapter.setCodeListData(WeatherWidgetApplication.isCNLanguage ? mCityList : mCityEnList);
        cityAdapter.setData(mForecastyweathers);


    }


    private class UpdateItenUIReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            freshButton.clearAnimation();
            freshButton.setVisibility(View.GONE);
            if (intent.getAction().equals(Constant.ACTION_UPITEMUI)) {
                mForecastyweathers.clear();
                String weatherJson = SPUtils.getInstance().getString("weather_json", "");
                mForecastyweathers = new Gson().fromJson(weatherJson, new TypeToken<List<FullWeatherInfo>>() {
                }.getType());
                addCityPresenter.getCityLiAndCityCodeLiFormSpPresenter();
            }
            if (intent.getStringExtra("errmsg")!=null&&intent.getStringExtra("errmsg").length()>0){
                showToast(intent.getStringExtra("errmsg"));
            }

        }

    }

    private static class MyHandler extends Handler {
        private final WeakReference<CityEditPageActivity> mActivity;

        public MyHandler(CityEditPageActivity activity) {
            mActivity = new WeakReference<CityEditPageActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            CityEditPageActivity activity = mActivity.get();
            if (activity != null) {
                if (msg.arg2 == 0x002) {
                    activity.mCityList.remove(msg.arg1);
                    activity.mCityEnList.remove(msg.arg1);
                    activity.mCityCodeList.remove(msg.arg1);
                    activity.mForecastyweathers.remove(msg.arg1);
                }
                activity.cityAdapter.setCodeListData(WeatherWidgetApplication.isCNLanguage?activity.mCityList: activity.mCityEnList);
                activity.cityAdapter.setData(activity.mForecastyweathers);
            }
        }
    }


    private void registerIntentFilter() {
        mItemUIReceiver = new UpdateItenUIReceiver();
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(Constant.ACTION_UPITEMUI);
        registerReceiver(mItemUIReceiver, iFilter);

    }

    @Override
    protected MvpBasePresenter getPresenter() {
        addCityPresenter = new AddCityPresenter();
        addCityPresenter.attach(this);
        return addCityPresenter;
    }

    private void init() {


    }


    @Override
    protected void onStop() {
        unregisterReceiver(mItemUIReceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerIntentFilter();
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {//点击的是返回键
            if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {//按键的按下事件
                CityEditPageActivity.this.finish();
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private class AddClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            cancelToast();
            if (mForecastyweathers.size() < 4) {
                Intent intent = new Intent(CityEditPageActivity.this, AddCityActivity.class);
                startActivityForResult(intent,500);
            } else {
                showToast(SkinResource.getSkinStringByName("citynum_limit"));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==resultCode){
            freshButton.startAnimation(mAnim);
            freshButton.setVisibility(View.VISIBLE);
        }
    }
}