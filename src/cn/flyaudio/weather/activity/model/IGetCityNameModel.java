package cn.flyaudio.weather.activity.model;

import java.util.List;

/**
 * Created by 叶兴运 on
 * 18-8-14.上午9:53
 */
public interface IGetCityNameModel {
    void getCityCodeList( CallBackCodeListData callBackData);

    void getCurrentPageData(int currentpage, CallBackData callBackData);

    interface CallBackData {
        void onSuccess(List<String> mStringArray, String[] mCityNameCopy);
        void onFail();
    }
    interface CallBackCodeListData{
        void GetCodeList(List<String> mCityList, List<String> mCityCodeList, List mCityEnList);
    }
}
