package cn.flyaudio.weather.activity.model;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

import cn.flyaudio.weather.data.WeatherWidgetApplication;
import cn.flyaudio.weather.util.Constant;
import cn.flyaudio.weather.util.SPUtils;
import cn.flyaudio.weather.view.SkinResource;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by 叶兴运 on
 * 18-8-14.上午9:58
 */
public class GetCityNameModel implements IGetCityNameModel {

    private ArrayList<String> mStringArray;
    private String[] mCityNameCopy;
    private List mCityList ;
    private List mCityEnList ;
    private List mCityCodeList;
    private SPUtils preference;

    @Override
    public void getCityCodeList( CallBackCodeListData callBackData) {
        mCityList = new ArrayList();
        mCityCodeList = new ArrayList();
        mCityEnList = new ArrayList();
        preference = SPUtils.getInstance();
        for (int i = 0; i < 4; i++) {
                String city = preference.getString(String.valueOf(i + 1), null);
                String cityen = preference.getString(String.valueOf(i * 10 + 10), null);
                String code = readSharpPreference(i + 1);
                if (city != null) {
                    mCityList.add(city);
                    mCityEnList.add(cityen);
                    mCityCodeList.add(code);
                }
            }
        callBackData.GetCodeList(mCityList,mCityCodeList,mCityEnList);


    }
    private String readSharpPreference(int index) {
        String city = null;
        switch (index) {
            case 1:
                city = preference.getString("city1", Constant.DEFAULT_CITYCODE);
                break;
            case 2:
                city = preference.getString("city2", "1000");
                break;
            case 3:
                city = preference.getString("city3", "1000");
                break;
            case 4:
                city = preference.getString("city4", "1000");
                break;
            default:
                break;
        }
        return city;
    }



    @Override
    public void getCurrentPageData(int currentpage, CallBackData callBackData) {
        final String[] cityname = {SkinResource.getSkinStringByName("city1"),
                SkinResource.getSkinStringByName("city2"), SkinResource.getSkinStringByName("city3"),
                SkinResource.getSkinStringByName("city4"), SkinResource.getSkinStringByName("city5"),
                SkinResource.getSkinStringByName("city6"), SkinResource.getSkinStringByName("city7"),
                SkinResource.getSkinStringByName("city8"), SkinResource.getSkinStringByName("city9"),
                SkinResource.getSkinStringByName("city10"), SkinResource.getSkinStringByName("city11"),
                SkinResource.getSkinStringByName("city12"), SkinResource.getSkinStringByName("city13"),
                SkinResource.getSkinStringByName("city14"), SkinResource.getSkinStringByName("city15"),
                SkinResource.getSkinStringByName("city16"), SkinResource.getSkinStringByName("city17"),
                SkinResource.getSkinStringByName("city18"), SkinResource.getSkinStringByName("city19"),
                SkinResource.getSkinStringByName("city20"), SkinResource.getSkinStringByName("city21"),
                SkinResource.getSkinStringByName("city22"), SkinResource.getSkinStringByName("city23"),
                SkinResource.getSkinStringByName("city24"), SkinResource.getSkinStringByName("city25"),
                SkinResource.getSkinStringByName("city26"), SkinResource.getSkinStringByName("city27"),
                SkinResource.getSkinStringByName("city28"), SkinResource.getSkinStringByName("city29"),
                SkinResource.getSkinStringByName("city30"), SkinResource.getSkinStringByName("city31"),
                SkinResource.getSkinStringByName("city32"), SkinResource.getSkinStringByName("city33"),
                SkinResource.getSkinStringByName("city34"), SkinResource.getSkinStringByName("city35"),
                SkinResource.getSkinStringByName("city36")};

        mCityNameCopy = cityname.clone();
        mStringArray = new ArrayList<String>();
        getcurrentData(currentpage);
        callBackData.onSuccess(mStringArray,mCityNameCopy);
    }

    public void getcurrentData(int page) {
        int start = page * 12;
        int end = start + 12;
        mStringArray.clear();
        while (start < mCityNameCopy.length && start < end) {
            mStringArray.add(mCityNameCopy[start]);
            start++;
        }
    }


}
