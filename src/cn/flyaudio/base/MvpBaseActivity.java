package cn.flyaudio.base;

import android.app.Activity;
import android.os.Bundle;



/**
 * MVP模式中的View的抽象类，提供了Activity的基础通用功能
 *
 * @author Brook
 * @time 2017/3/16 13:38
 */
public abstract class MvpBaseActivity<V extends MvpBaseView, P extends MvpBasePresenter<V>> extends Activity implements MvpBaseView {

    private P mPresenter;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 初始化Presenter
        mPresenter = getPresenter();
        if (mPresenter != null) {
            mPresenter.attach((V) this);
        }
        initLayout(savedInstanceState);
        initView(savedInstanceState);

    }


    protected abstract P getPresenter();




    /**
     * 在这里设置布局的操作
     * 例如:setContentView
     */
    protected abstract void initLayout(Bundle savedInstanceState);

    /**
     * 在这里做初始化View的操作
     *
     * @param savedInstanceState
     */
    protected abstract void initView(Bundle savedInstanceState);

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.detach();
        }
    }

    @Override
    public void showMessage(String msg) {

    }
}
