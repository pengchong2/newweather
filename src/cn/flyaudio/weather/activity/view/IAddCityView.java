package cn.flyaudio.weather.activity.view;

import java.util.List;

import cn.flyaudio.base.MvpBaseView;

/**
 * Created by 叶兴运 on
 * 18-8-14.上午10:37
 */
public interface IAddCityView extends MvpBaseView{
    void showData(List<String> mStringArray,String[] cityname);
    int getPager();
    void getCityCodeList(List<String> mCityList, List<String> mCityCodeList, List<String> mCityEnList);
}
