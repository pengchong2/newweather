package cn.flyaudio.weather.adapter;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.flyaudio.weather.data.WeatherWidgetApplication;
import cn.flyaudio.weather.objectInfo.FullWeatherInfo;
import cn.flyaudio.weather.util.Constant;
import cn.flyaudio.weather.util.SPUtils;
import cn.flyaudio.weather.view.SkinResource;

import static cn.flyaudio.weather.util.UtilsTools.parseSmartBgBycode;
import static cn.flyaudio.weather.util.UtilsTools.parseWeatherBgBycode;


/**
 * @author:zengyuke
 * @company:flyaudio
 * @version:1.0
 * @createdDate:2014-5-5下午3:16:19
 */
public class ViewFlowAdapter extends BaseAdapter {

    private final String TAG = Constant.TAG;
    private final Boolean DEBUG_FLAG = Constant.DEBUG_FLAG;
    private ArrayList<String> mCityList;
    private LayoutInflater mInflater;
    private Context mContext;
    private Animation mAnim;
    private List<FullWeatherInfo> fullWeatherInfos;

    public ViewFlowAdapter(Context context) {
        if (fullWeatherInfos==null){
            String weatherJson = SPUtils.getInstance().getString("weather_json", "");

           this.fullWeatherInfos = new Gson().fromJson(weatherJson, new TypeToken<List<FullWeatherInfo>>() {
            }.getType());
        }

        mCityList = new ArrayList<>();
        mContext = context;
        mInflater = (LayoutInflater) SkinResource.getSkinContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (int i = 1; i <= 4; i++) {
            String city = SPUtils.getInstance().getString(String.valueOf(i), null);
            if (city != null) {
                mCityList.add(city);
            }
        }
    }
    public void setData(List<FullWeatherInfo> fullWeatherInfos){
        this.fullWeatherInfos = fullWeatherInfos;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 4;// WeatherDetails.view_count-1
    }

    @Override
    public int getCount() {
        return fullWeatherInfos==null?0:fullWeatherInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        FullWeatherInfo mForecastyweathers = fullWeatherInfos.get(position);
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(SkinResource.getSkinLayoutIdByName("viewflow_layout"), null);
            //更新于20161223，新接口没有日出日落所以隐藏切换按钮
            //convertView.findViewById(SkinResource.getSkinResourceId("city_button", "id")).setOnClickListener(clickListener);
            convertView.findViewById(SkinResource.getSkinResourceId("city_button", "id")).setVisibility(View.GONE);
            mAnim = AnimationUtils.loadAnimation(SkinResource.getSkinContext(), SkinResource.getSkinResourceId("weather_loading", "anim"));
            mAnim.setInterpolator(new LinearInterpolator());
            viewHolder.cityName = (TextView) convertView.findViewById(SkinResource.getSkinResourceId("weathercity", "id"));
            viewHolder.date = (TextView) convertView.findViewById(SkinResource.getSkinResourceId("date", "id"));
            convertView.findViewById(SkinResource.getSkinResourceId("curTemp_parent", "id")).setVisibility(View.VISIBLE);
            convertView.findViewById(SkinResource.getSkinResourceId("layout_low_hight_temperature", "id")).setVisibility(View.VISIBLE);
            viewHolder.cutTemp = (TextView) convertView.findViewById(SkinResource.getSkinResourceId("curTemp", "id"));
            viewHolder.txtCurWeatherCondition = (TextView) convertView.findViewById(SkinResource.getSkinResourceId("txt_cur_weather_condition", "id"));

            viewHolder.curLowTemperature = (TextView) convertView.findViewById(SkinResource.getSkinResourceId("txt_cur_low_temperature", "id"));
            viewHolder.curHightTemperature = (TextView) convertView.findViewById(SkinResource.getSkinResourceId("txt_cur_hight_temperature", "id"));

            viewHolder.curLowTemperature.setText(mForecastyweathers.yweathers[0].getLow());
            viewHolder.curHightTemperature.setText(mForecastyweathers.yweathers[0].getHigh());

            viewHolder.imgBg = (ImageView) convertView.findViewById(SkinResource.getSkinResourceId("set", "id"));


            //图标和背景分离时修改：set改为img_weather_icon（新增的图标id）
            viewHolder.imgWeatherIcon = (ImageView) convertView.findViewById(SkinResource.getSkinResourceId("img_weather_icon", "id"));


            viewHolder.lvLastFewDaysWeather = (GridView) convertView.findViewById(SkinResource.getSkinResourceId("lv_last_few_days_weather", "id"));


            viewHolder.txtWindSpeed = (TextView) convertView.findViewById(SkinResource.getSkinResourceId("txt_wind_speed", "id"));
            viewHolder.txtWindDirection = (TextView) convertView.findViewById(SkinResource.getSkinResourceId("txt_wind_direction", "id"));
            viewHolder.viewWindSpeed = (TextView) convertView.findViewById(SkinResource.getSkinResourceId("weather_time_view_windSpeed", "id"));
            viewHolder.viewWindDirection = (TextView) convertView.findViewById(SkinResource.getSkinResourceId("weather_time_view_windDirection", "id"));


//			UtilsTools.setConvertViewWithShared(mContext, convertView, mCityList.get(position));
            convertView.setTag(viewHolder);
        }
        viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.txtWindSpeed.setText(mForecastyweathers.getWindspeed());
        viewHolder.cutTemp.setText(mForecastyweathers.getCondition_temp());
        viewHolder.txtWindDirection.setText(mForecastyweathers.getWinddirection());
        viewHolder.viewWindSpeed.setText(mForecastyweathers.getWindspeed());
        viewHolder.viewWindDirection.setText(mForecastyweathers.getWinddirection());
        if (WeatherWidgetApplication.isCNLanguage) {
            viewHolder.cityName.setText(mForecastyweathers.getCity());
        } else {
            viewHolder.cityName.setText(mForecastyweathers.getCityPinyin());
        }
        viewHolder.date.setText(getSystemDate(SkinResource.getSkinContext().getString(SkinResource.getSkinResourceId("weather_timeformat", "string"))));
        LastFewDaysWeatherViewAdapter adapter = new LastFewDaysWeatherViewAdapter(mContext, mForecastyweathers);
        viewHolder.lvLastFewDaysWeather.setAdapter(adapter);
        viewHolder.lvLastFewDaysWeather.setEnabled(false);


        viewHolder.imgWeatherIcon.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(),
                parseSmartBgBycode(mForecastyweathers.getCondition_code(),
                        mForecastyweathers.getSunrise(), mForecastyweathers.getSunset())));
        viewHolder.imgBg.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(),
                parseWeatherBgBycode(mForecastyweathers.getCondition_code(),
                        mForecastyweathers.getSunrise(), mForecastyweathers.getSunset())));
        return convertView;
    }

    class ViewHolder {

        public TextView cityName;
        public TextView date;
        public TextView temp;
        public TextView lowTemp;
        public TextView heightTemp;
        public ImageView imgWeatherIcon;
        public TextView txtWindSpeed;
        public TextView txtWindDirection;
        public TextView cutTemp;
        public TextView txtCurWeatherCondition;
        public TextView curLowTemperature;
        public TextView curHightTemperature;
        public ImageView imgBg;
        public GridView lvLastFewDaysWeather;
        public TextView viewWindSpeed;
        public TextView viewWindDirection;
    }


    private static String getSystemDate(String dateformat) {
        SimpleDateFormat format = new SimpleDateFormat(dateformat);
        Date date = new Date(System.currentTimeMillis());
        return format.format(date).toUpperCase();
    }


}