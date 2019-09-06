package cn.flyaudio.weather.activity.presenter;

import java.lang.ref.WeakReference;
import java.util.List;

import cn.flyaudio.base.MvpBasePresenter;
import cn.flyaudio.weather.activity.AddCityActivity;
import cn.flyaudio.weather.activity.model.GetCityNameModel;
import cn.flyaudio.weather.activity.model.IGetCityNameModel;
import cn.flyaudio.weather.activity.view.IAddCityView;

/**
 * Created by 叶兴运 on
 * 18-8-14.上午10:29
 */
public class AddCityPresenter<T extends IAddCityView> extends MvpBasePresenter<IAddCityView> implements IAddCityPresenter {
    private GetCityNameModel mGetCityNameModel = new GetCityNameModel();
    protected WeakReference<T> mViewRef  ;



    @Override
    public void getPager() {
//        mViewRef.get().getPager();
        int pager =mReference.get().getPager();
        mGetCityNameModel.getCurrentPageData(pager, new IGetCityNameModel.CallBackData() {
            @Override
            public void onSuccess(List<String> mStringArray, String[] mCityNameCopy) {
               mReference.get().showData(mStringArray,mCityNameCopy);
            }
            @Override
            public void onFail() {

            }
        });

    }

    @Override
    public void getCityLiAndCityCodeLiFormSpPresenter() {
        mGetCityNameModel.getCityCodeList(new IGetCityNameModel.CallBackCodeListData() {
            @Override
            public void GetCodeList(List<String> mCityList, List<String> mCityCodeList, List mCityEnList) {
               mReference.get().getCityCodeList(mCityList,mCityCodeList,mCityEnList);
            }
        });
    }



}
