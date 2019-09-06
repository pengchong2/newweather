package cn.flyaudio.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import cn.flyaudio.base.MvpBaseActivity;
import cn.flyaudio.base.MvpBasePresenter;
import cn.flyaudio.base.MvpBaseView;

/**
 * Created by 叶兴运 on
 * 18-8-13.下午3:08
 */
public abstract class BaseActivity<V extends MvpBaseView, P extends MvpBasePresenter<V>> extends MvpBaseActivity<V, P> {
    private Toast mToast;

    @Override
    protected void initLayout(Bundle savedInstanceState) {
        setContentView(getlayoutId());
    }

    protected abstract int getlayoutId();

    @Override
    protected void initView(Bundle savedInstanceState) {
        initView();
    }

    protected abstract void initView();

    public void showToast(String text) {
        mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        mToast.show();
    }

    public void cancelToast() {
        if (mToast != null) {
            mToast.cancel();
        }
    }

    /**
     * 通过Class跳转界面
     **/
    public void startActivity(Class<?> cls) {
        startActivity(cls, null);
    }


    /**
     * 含有Bundle通过Class跳转界面
     **/
    public void startActivity(Class<?> cls, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(this, cls);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }
}
