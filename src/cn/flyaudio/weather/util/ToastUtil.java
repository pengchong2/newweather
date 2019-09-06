package cn.flyaudio.weather.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by 叶兴运 on
 * 18-8-13.下午3:36
 */
public class ToastUtil {
    private static ToastUtil toastUtil;
    private static Toast toast;
    private ToastUtil(){
    }
    public static void show(Context context,CharSequence msg){
        if (toast==null){
            toast = Toast.makeText(context,msg,Toast.LENGTH_SHORT);
        }else {
            toast.setText(msg);
        }
        toast.show();
    }
}
