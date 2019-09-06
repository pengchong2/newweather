package cn.flyaudio.weather.objectInfo;


import java.io.Serializable;

/**
 * @author:zengyuke
 * @company:flyaudio
 * @version:1.0
 * @createdDate:2014-5-5下午3:18:07
 */
public class WeatherInfo_OneDay implements Serializable {

	private String day;    //星期
	private String date;   //日期
	private String low;    //最低温
	private String high;   //最高温
	private String curtemp;   //当前温
	private String windDirection;   //风向
	private String windSpeed;   //风速
	private String weatherPhenomena;   //天气现象编码
	private String cityname;   //


    private String text;   //天气类型
    private String code;   //天气代号
	public String getDay() {
		return day;
	}
	public void setDay(String day) {
		this.day = day;
	}
	public String getDate() {
		return date;
	}

	public String getCurtemp() {
		return curtemp;
	}

	public void setCurtemp(String curtemp) {
		this.curtemp = curtemp;
	}

	public String getCityname() {
		return cityname;
	}

	public void setCityname(String cityname) {
		this.cityname = cityname;
	}

	public String getWeatherPhenomena() {
		return weatherPhenomena;
	}

	public void setWeatherPhenomena(String weatherPhenomena) {
		this.weatherPhenomena = weatherPhenomena;
	}

	public String getWindDirection() {
		return windDirection;
	}

	public void setWindDirection(String windDirection) {
		this.windDirection = windDirection;
	}

	public String getWindSpeed() {
		return windSpeed;
	}

	public void setWindSpeed(String windSpeed) {
		this.windSpeed = windSpeed;
	}

	public void setDate(String date) {
		this.date = date;
	}
	public String getLow() {
		return low;
	}
	public void setLow(String low) {
		this.low = low;
	}
	public String getHigh() {
		return high;
	}
	public void setHigh(String high) {
		this.high = high;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}

	
}
