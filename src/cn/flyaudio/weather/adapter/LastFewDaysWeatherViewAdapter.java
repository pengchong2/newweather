package cn.flyaudio.weather.adapter;

import android.content.Context;
import android.content.MutableContextWrapper;
import android.graphics.Bitmap;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import cn.flyaudio.weather.data.WeatherWidgetApplication;
import cn.flyaudio.weather.objectInfo.FullWeatherInfo;
import cn.flyaudio.weather.util.Constant;
import cn.flyaudio.weather.util.TimeWeatherUtilsTools;
import cn.flyaudio.weather.util.UtilsTools;
import cn.flyaudio.weather.view.SkinResource;

public class LastFewDaysWeatherViewAdapter extends BaseAdapter {

    private final static int VIEW_SIZE = 3;
    private FullWeatherInfo fullWeatherInfo;
    private Context context;

    public LastFewDaysWeatherViewAdapter(Context context, FullWeatherInfo info) {
        this.fullWeatherInfo = info;
        this.context = context;
    }

    @Override
    public int getCount() {
        return VIEW_SIZE;
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
        WeatherViewHolder viewHolder = null;
        String mLowTemperature = fullWeatherInfo.yweathers[position].getLow();
//        Log.d("yexingyun","mlowview=="+mLowTemperature);
        String mHightTemperature = fullWeatherInfo.yweathers[position].getHigh();
//        Log.d("yexingyun","mHightview=="+mHightTemperature);

        String mWertherCondition = UtilsTools.getSmartWeatherByNum(context, fullWeatherInfo.yweathers[position].getCode());
        Bitmap mWeatherIcon = UtilsTools.getWeatherIcon(context, fullWeatherInfo.yweathers[position].getCode());

        if (TextUtils.isEmpty(mLowTemperature)) {
            mLowTemperature = SkinResource.getSkinStringByName("no_temp_data");
        }
        if (TextUtils.isEmpty(mHightTemperature)) {
            mHightTemperature = SkinResource.getSkinStringByName("no_temp_data");
        }
        if (convertView == null) {
            viewHolder = new WeatherViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(SkinResource.getSkinLayoutIdByName("last_few_days_weather_item"), parent, false);


            viewHolder.txtDate = (TextView) convertView.findViewById(SkinResource.getSkinResourceId("txt_date", "id"));
            viewHolder.txtWeek = (TextView) convertView.findViewById(SkinResource.getSkinResourceId("txt_week", "id"));
            viewHolder.txtLowTemperature = (TextView) convertView.findViewById(SkinResource.getSkinResourceId("txt_low_temperature", "id"));
            viewHolder.txtHightTemperature = (TextView) convertView.findViewById(SkinResource.getSkinResourceId("txt_hight_temperature", "id"));
            viewHolder.txtWeatherCondition = (TextView) convertView.findViewById(SkinResource.getSkinResourceId("txt_weather_condition", "id"));
            viewHolder.imgWeatherConditionIcon = (ImageView) convertView.findViewById(SkinResource.getSkinResourceId("img_weather_smart_icon", "id"));
            //风向风速
            viewHolder.windDirection = (TextView) convertView.findViewById(SkinResource.getSkinResourceId("weather_time_detail_windDirection", "id"));
            viewHolder.windSpeed = (TextView) convertView.findViewById(SkinResource.getSkinResourceId("weather_time_detail_windSpeed", "id"));
            convertView.setTag(viewHolder);

            viewHolder.txtWeatherCondition.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    SkinResource.getSkinContext().getResources().getDimension(SkinResource.getSkinResourceId("normal_text_size", "dimen")));
            if (WeatherWidgetApplication.isCNLanguage == true) {
                if (mWertherCondition.length() > 4) {
                    viewHolder.txtWeatherCondition.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                            SkinResource.getSkinContext().getResources().getDimension(SkinResource.getSkinResourceId("small_text_size", "dimen")));
                }
            } else {
                if (mWertherCondition.length() > 9) {
                    viewHolder.txtWeatherCondition.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                            SkinResource.getSkinContext().getResources().getDimension(SkinResource.getSkinResourceId("small_text_size", "dimen")));
                }
            }
        }
        viewHolder = (WeatherViewHolder) convertView.getTag();
        String date = fullWeatherInfo.yweathers[position].getDate();
        if (WeatherWidgetApplication.isCNLanguage) {
            viewHolder.txtDate.setText(" " + date.substring(5, 7) + "月" +
                    date.substring(8, 10) + "日 ");
//            viewHolder.txtDate.setText(" " + date.substring(5, 7) + "月" +
//                    date.substring(8, 10) + "日 " +
//                    fullWeatherInfo.yweathers[position].getDay());
            viewHolder.txtWeek.setText(
                    fullWeatherInfo.yweathers[position].getDay());
        } else {
            viewHolder.txtDate.setText(" " + date.substring(5, 7) + "/" +
                    date.substring(8, 10) + "  "
//                    + fullWeatherInfo.yweathers[position].getDay()
            );
            viewHolder.txtWeek.setText(" "+fullWeatherInfo.yweathers[position].getDay());
        }


        viewHolder.txtHightTemperature.setText(mHightTemperature);
        viewHolder.txtLowTemperature.setText(mLowTemperature);
        viewHolder.txtWeatherCondition.setText(mWertherCondition);
        viewHolder.imgWeatherConditionIcon.setImageBitmap(mWeatherIcon);

        viewHolder.windSpeed.setText(fullWeatherInfo.getWindspeed());
        viewHolder.windDirection.setText(fullWeatherInfo.getWinddirection());

        return convertView;
    }

    class WeatherViewHolder {
        public TextView txtDate;

        public TextView txtLowTemperature;
        public TextView txtHightTemperature;
        public TextView txtWeatherCondition;
        public TextView windDirection;
        public TextView windSpeed;
        public ImageView imgWeatherConditionIcon;
        public TextView txtWeek;
    }


}
