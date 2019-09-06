package cn.flyaudio.weather.objectInfo;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author:zengyuke
 * @company:flyaudio
 * @version:1.0
 * @createdDate:2014-5-5下午3:17:28
 */
@SuppressLint("ParcelCreator")
public class FullWeatherInfo implements Parcelable {
    private String return_code;
    private String return_msg;
    private String condition_text;  //当前天气类型名称
    private String condition_code;  //当前天气类型标号
    private String condition_temp;  //当前气温
    private String condition_date;  //发布时间
    private String city;            //城市名
    private String cityPinyin;      //城市拼音
    private String windchill;       //风寒
    private String winddirection;   //风向
    private String windspeed;       //风速
    private String humidity;        //湿度
    private String pressure;        //压力
    private String sunrise;
    private String weatherPhenomena;//天气编码


    protected FullWeatherInfo(Parcel in) {
        return_code = in.readString();
        return_msg = in.readString();
        condition_text = in.readString();
        condition_code = in.readString();
        condition_temp = in.readString();
        condition_date = in.readString();
        city = in.readString();
        cityPinyin = in.readString();
        windchill = in.readString();
        winddirection = in.readString();
        windspeed = in.readString();
        humidity = in.readString();
        pressure = in.readString();
        sunrise = in.readString();
        weatherPhenomena = in.readString();
        feelslike = in.readString();
        visibility = in.readString();
        uvindex = in.readString();
        chanceOfRain = in.readString();
        xcindex = in.readString();
        cyindex = in.readString();
        dwinddirection = in.readString();
        dwindspeed = in.readString();
        nwinddirection = in.readString();
        nwindspeed = in.readString();
        byte tmpDataflag = in.readByte();
        dataflag = tmpDataflag == 0 ? null : tmpDataflag == 1;
        sunset = in.readString();
    }

    public static final Creator<FullWeatherInfo> CREATOR = new Creator<FullWeatherInfo>() {
        @Override
        public FullWeatherInfo createFromParcel(Parcel in) {
            return new FullWeatherInfo(in);
        }

        @Override
        public FullWeatherInfo[] newArray(int size) {
            return new FullWeatherInfo[size];
        }
    };

    public String getReturn_code() {
        return return_code;
    }

    public void setReturn_code(String return_code) {
        this.return_code = return_code;
    }

    public String getReturn_msg() {
        return return_msg;
    }

    public void setReturn_msg(String return_msg) {
        this.return_msg = return_msg;
    }
    public String getXcindex() {
        return xcindex;
    }

    public void setXcindex(String xcindex) {
        this.xcindex = xcindex;
    }

    public String getCyindex() {
        return cyindex;
    }

    public void setCyindex(String cyindex) {
        this.cyindex = cyindex;
    }

    public String getDwinddirection() {
        return dwinddirection;
    }

    public void setDwinddirection(String dwinddirection) {
        this.dwinddirection = dwinddirection;
    }

    public String getDwindspeed() {
        return dwindspeed;
    }

    public void setDwindspeed(String dwindspeed) {
        this.dwindspeed = dwindspeed;
    }

    public String getNwinddirection() {
        return nwinddirection;
    }

    public void setNwinddirection(String nwinddirection) {
        this.nwinddirection = nwinddirection;
    }

    public String getNwindspeed() {
        return nwindspeed;
    }

    public void setNwindspeed(String nwindspeed) {
        this.nwindspeed = nwindspeed;
    }

    private String feelslike;       //体感
    private String visibility;      //能见度
    private String uvindex;        // 紫外线
    private String chanceOfRain;   //下雨可能性

    private String xcindex;
    private String cyindex;
    private String dwinddirection;
    private String dwindspeed;
    private String nwinddirection;
    private String nwindspeed;
    private Boolean dataflag;

    public WeatherInfo_OneDay[] yweathers = new WeatherInfo_OneDay[5];

    public Boolean getDataflag() {
        return dataflag;
    }

    public void setDataflag(Boolean dataflag) {
        this.dataflag = dataflag;
    }


    public String getFeelslike() {
        return feelslike;
    }

    public void setFeelslike(String feelslike) {
        this.feelslike = feelslike;
    }

    public WeatherInfo_OneDay[] getYweathers() {
        return yweathers;
    }

    public void setYweathers(WeatherInfo_OneDay[] yweathers) {
        this.yweathers = yweathers;
    }

    public String getUvindex() {
        return uvindex;
    }

    public void setUvindex(String uvindex) {
        this.uvindex = uvindex;
    }

    public String getChanceOfRain() {
        return chanceOfRain;
    }

    public void setChanceOfRain(String chanceOfRain) {
        this.chanceOfRain = chanceOfRain;
    }

    public String getWindchill() {
        return windchill;
    }

    public void setWindchill(String windchill) {
        this.windchill = windchill;
    }

    public String getWinddirection() {
        return winddirection;
    }

    public void setWinddirection(String winddirection) {
        this.winddirection = winddirection;
    }

    public String getWindspeed() {
        return windspeed;
    }

    public void setWindspeed(String windspeed) {
        this.windspeed = windspeed;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public String getSunrise() {
        return sunrise;
    }

    public void setSunrise(String sunrise) {
        this.sunrise = sunrise;
    }

    public String getSunset() {
        return sunset;
    }

    public void setSunset(String sunset) {
        this.sunset = sunset;
    }

    private String sunset;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCityPinyin() {
        return cityPinyin;
    }

    public void setCityPinyin(String cityPinyin) {
        this.cityPinyin = cityPinyin;
    }


    public FullWeatherInfo() {
        // TODO Auto-generated constructor stub
        for (int i = 0; i < 5; i++) {
            yweathers[i] = new WeatherInfo_OneDay();
        }
    }

    public String getCondition_text() {
        return condition_text;
    }

    public void setCondition_text(String condition_text) {
        this.condition_text = condition_text;
    }

    public String getCondition_code() {
        return condition_code;
    }

    public void setCondition_code(String condition_code) {
        this.condition_code = condition_code;
    }

    public String getCondition_temp() {
        return condition_temp;
    }

    public void setCondition_temp(String condition_temp) {
        this.condition_temp = condition_temp;
    }

    public String getCondition_date() {
        return condition_date;
    }

    public void setCondition_date(String condition_date) {
        this.condition_date = condition_date;
    }

    public String getWeatherPhenomena() {
        return weatherPhenomena;
    }

    public void setWeatherPhenomena(String weatherPhenomena) {
        this.weatherPhenomena = weatherPhenomena;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(return_code);
        dest.writeString(return_msg);
        dest.writeString(condition_text);
        dest.writeString(condition_code);
        dest.writeString(condition_temp);
        dest.writeString(condition_date);
        dest.writeString(city);
        dest.writeString(cityPinyin);
        dest.writeString(windchill);
        dest.writeString(winddirection);
        dest.writeString(windspeed);
        dest.writeString(humidity);
        dest.writeString(pressure);
        dest.writeString(sunrise);
        dest.writeString(weatherPhenomena);
        dest.writeString(feelslike);
        dest.writeString(visibility);
        dest.writeString(uvindex);
        dest.writeString(chanceOfRain);
        dest.writeString(xcindex);
        dest.writeString(cyindex);
        dest.writeString(dwinddirection);
        dest.writeString(dwindspeed);
        dest.writeString(nwinddirection);
        dest.writeString(nwindspeed);
        dest.writeByte((byte) (dataflag == null ? 0 : dataflag ? 1 : 2));
        dest.writeString(sunset);
    }
}
