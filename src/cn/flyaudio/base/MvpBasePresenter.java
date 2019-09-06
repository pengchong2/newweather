package cn.flyaudio.base;

import android.os.Handler;
import android.os.Looper;

import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;


public abstract class MvpBasePresenter<V extends MvpBaseView> {

    public WeakReference<V> mReference;
    private static Handler handler;

    public MvpBasePresenter() {
        handler = new Handler(Looper.getMainLooper());
    }

    @Nullable
    public final V getView() {
        return mReference == null ? null : mReference.get();
    }

    public  final void attach(V view) {
        this.mReference = new WeakReference<V>(view);
    }

    public final void detach() {
        this.mReference.clear();
        this.mReference = null;
    }

    public Handler getHandler() {
        return handler;
    }

}
