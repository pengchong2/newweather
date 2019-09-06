package cn.flyaudio.weather.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import cn.flyaudio.weather.WeatherWidget;
import cn.flyaudio.weather.activity.CityEditPageActivity;
import cn.flyaudio.weather.activity.WeatherDetailsActivity;
import cn.flyaudio.weather.data.WeatherWidgetApplication;
import cn.flyaudio.weather.objectInfo.FullWeatherInfo;
import cn.flyaudio.weather.service.WeatherService;
import cn.flyaudio.weather.util.Constant;
import cn.flyaudio.weather.util.SPUtils;
import cn.flyaudio.weather.util.TimeWeatherUtilsTools;
//import cn.flyaudio.weather.util.UtilsTools;
import cn.flyaudio.weather.view.SkinResource;

import static cn.flyaudio.weather.util.TimeWeatherUtilsTools.getSmartWindDirection;
import static cn.flyaudio.weather.util.TimeWeatherUtilsTools.getSmartWindSpeed;
import static cn.flyaudio.weather.util.TimeWeatherUtilsTools.parseSmartBgBycode;

/**
 * @author:zengyuke
 * @company:flyaudio
 * @version:1.0
 * @createdDate:2014-5-5下午3:16:08
 */
public class CityAdapter extends BaseAdapter {

    private final String TAG = Constant.TAG;
    private final Boolean DEBUG_FLAG = Constant.DEBUG_FLAG;
    private final Handler mHandler;
    private Context mContext;
    private List<String> mCityList = null;
    private List<FullWeatherInfo> mForecastyweathers = null;

    public CityAdapter(Context context, Handler mHandler) {
        mCityList = new ArrayList<>();
        this.mForecastyweathers = new ArrayList<>();
        mContext = context;
        this.mHandler = mHandler;


    }

    public void setData(List<FullWeatherInfo> mForecastyweathers) {
        if (this.mForecastyweathers != null && this.mForecastyweathers.size() > 0) {
            this.mForecastyweathers.clear();
        }
        this.mForecastyweathers.addAll(mForecastyweathers);
        notifyDataSetChanged();
    }

    public void setCodeListData(List<String> mCityList) {
        this.mCityList.clear();
        this.mCityList .addAll(mCityList);
//        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


    @Override
    public int getCount() {
        return mForecastyweathers == null ? 0 : mForecastyweathers.size();
    }

    @Override
    public Object getItem(int position) {
        return mForecastyweathers.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(SkinResource.getSkinLayoutIdByName("weather_city_item"), null);
            //删除按钮
            viewHolder.deleteBtn = (Button) convertView.findViewById(SkinResource.getSkinResourceId("weather_time_delete", "id"));
            viewHolder.relativeLayout = (RelativeLayout) convertView.findViewById(SkinResource.getSkinResourceId("weather_time_relativelayout", "id"));
            viewHolder.cityName = (TextView) convertView.findViewById(SkinResource.getSkinResourceId("weather_time_cityname", "id"));
            viewHolder.temp = (TextView) convertView.findViewById(SkinResource.getSkinResourceId("weather_time_temp", "id"));
            viewHolder.lowTemp = (TextView) convertView.findViewById(SkinResource.getSkinResourceId("weather_time_low_temperature", "id"));
            viewHolder.heightTemp = (TextView) convertView.findViewById(SkinResource.getSkinResourceId("weather_time_hight_temperature", "id"));
            viewHolder.imgWeatherIcon = (ImageView) convertView.findViewById(SkinResource.getSkinResourceId("weather_time_weather_icon", "id"));
            viewHolder.txtWindSpeed = (TextView) convertView.findViewById(SkinResource.getSkinResourceId("weather_time_windSpeed", "id"));
            viewHolder.txtWindDirection = (TextView) convertView.findViewById(SkinResource.getSkinResourceId("weather_time_windDirection", "id"));

            convertView.setTag(viewHolder);
        }
        viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.relativeLayout.setBackgroundResource(SkinResource.getSkinDrawableIdByName("weather_select_u"));


        final SPUtils shared = SPUtils.getInstance();

        final String currentCity = mCityList.get(position);

        final FullWeatherInfo fullWeatherInfo = mForecastyweathers.get(position);


        viewHolder.txtWindSpeed.setText(fullWeatherInfo.getWindspeed());
        viewHolder.txtWindDirection.setText(fullWeatherInfo.getWinddirection());
        if (WeatherWidgetApplication.isCNLanguage) {
            viewHolder.cityName.setText(fullWeatherInfo.getCity());
        } else {
            String cityName = fullWeatherInfo.getCityPinyin();
            String s = WeatherWidgetApplication.toUpperCaseFirstOne(cityName);
            viewHolder.cityName.setText(s);
        }
        if (fullWeatherInfo.getDataflag()) {
            viewHolder.lowTemp.setText(fullWeatherInfo.yweathers[0].getLow());
            viewHolder.temp.setText(fullWeatherInfo.getCondition_temp());
            viewHolder.heightTemp.setText(fullWeatherInfo.yweathers[0].getHigh());
        }
        //图标和背景分离时修改：set改为img_weather_icon（新增的图标id）
//        ImageView img = (ImageView) convertView.findViewById(SkinResource.getSkinResourceId("weather_time_weather_icon", "id"));

        viewHolder.imgWeatherIcon.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(),
                parseSmartBgBycode(fullWeatherInfo.getCondition_code(),
                        fullWeatherInfo.getSunrise(), fullWeatherInfo.getSunset())));
        if (TextUtils.isEmpty(fullWeatherInfo.getWinddirection())) {
            mContext.sendBroadcast(new Intent(Constant.ACTION_UPDATE_DATA));
        }

        viewHolder.relativeLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                int current = 1;
                for (int i = 1; i < 5; i++) {
                    if (WeatherWidgetApplication.isCNLanguage) {
                        if (shared.getString(String.valueOf(i), "").equals(
                                currentCity))
                            current = i;
                    } else {

                        if (shared.getString(String.valueOf(i * 10), "").equals(
                                currentCity))
                            current = i;
                    }
                }
                shared.put("current", current);
                shared.put("condition_temp",fullWeatherInfo.getCondition_temp());
                shared.put("condition_code",fullWeatherInfo.getCondition_code());

                Intent intent = new Intent(Constant.ACTION_APPWIDGET_UPDATE);
                intent.putExtra("current", current);
//                intent.putExtra("current", 1);
                for (int i = 0; i < 4; i++) {
                    WeatherService.backup = false;
                }
                mContext.sendBroadcast(intent);
                Intent mIntentHome = new Intent(mContext,
                        WeatherDetailsActivity.class);
                mContext.startActivity(mIntentHome);
                ((CityEditPageActivity) mContext).finish();

            }
        });
        viewHolder.deleteBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mForecastyweathers.size() > 1) {
                    mCityList.remove(position);
                    notifyDataSetChanged();
                    Message msg = new Message();
                    msg.arg1 = position;
                    msg.arg2 = 0x002;
                    mHandler.sendMessage(msg);
                    updateWeatherShared(position,currentCity);
                }


            }
        });

        if (WeatherWidgetApplication.isCNLanguage) {
            if (shared.getString(String.valueOf(shared.getInt("current", -1)), "").equals(currentCity)) {
                viewHolder.relativeLayout.setBackgroundResource(SkinResource.getSkinDrawableIdByName("weather_select_o"));
            }
        } else {
            if (shared.getString(String.valueOf(shared.getInt("current", -1) * 10), "").equals(currentCity)) {

                viewHolder.relativeLayout.setBackgroundResource(SkinResource.getSkinDrawableIdByName("weather_select_o"));
            }
        }


        return convertView;
    }

    class ViewHolder {
        public Button deleteBtn;
        public RelativeLayout relativeLayout;
        public TextView cityName;
        public TextView temp;
        public TextView lowTemp;
        public TextView heightTemp;
        public ImageView imgWeatherIcon;
        public TextView txtWindSpeed;
        public TextView txtWindDirection;
    }


    protected void updateWeatherShared(int position, String currentCity) {
        SPUtils shared = SPUtils.getInstance();
        int index = 1;
        // for get delete index 根据当前城市名得到sp中对应的索引
        for (int i = 1; i <= 4; i++) {
            String city = WeatherWidgetApplication.isCNLanguage ?
                    shared.getString(String.valueOf(i), null)
                    : shared.getString(String.valueOf(i * 10), null);

            if (city != null && city.equals(currentCity)) {
                index = i;
            }
        }
//
        // remove info city 移除索引对应的数据
        shared.remove(String.valueOf(index));
        shared.remove(String.valueOf(index * 10));
        shared.remove(String.valueOf("city" + index));

        mForecastyweathers.remove(position);
        SPUtils.getInstance().put("weather_json", new Gson().toJson(mForecastyweathers));

        int current = shared.getInt("current", 1);
        if (current == index) {
            for (int i = 0; i < 4; i++) {
                String city = shared.getString(String.valueOf(i + 1), null);
                if (city != null) {
                    current = i + 1;
                    break;
                }
            }
            shared.put("current", current);
            Log.d("WeatherWidget","updateWeatherShared action");
            mContext.sendBroadcast(new Intent(Constant.ACTION_APPWIDGET_UPDATE));
        }
    }

}
