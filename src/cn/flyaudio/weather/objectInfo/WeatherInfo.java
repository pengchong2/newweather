package cn.flyaudio.weather.objectInfo;

import java.util.List;

/**
 * Created by 叶兴运 on
 * 18-8-23.下午1:47
 */
public class WeatherInfo {
    public List<WeatherInfo_OneDay> wInfo;

    public List<WeatherInfo_OneDay> getwInfo() {
        return wInfo;
    }

    public void setwInfo(List<WeatherInfo_OneDay> wInfo) {
        this.wInfo = wInfo;
    }
}
