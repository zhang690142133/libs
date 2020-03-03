package xsg.lychee.richalpha.dailog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import xsg.lychee.richalpha.R;

public class BottomDailog extends Dialog {
    private boolean iscancelable;//控制点击dialog外部是否dismiss
    private boolean isBackCancelable;//控制返回键是否dismiss
    private @LayoutRes int layoutResID;
    private Context context;

    //这里的view其实可以替换直接传layout过来的 因为各种原因没传(lan)
    public BottomDailog(Context context, @LayoutRes int layoutResID, boolean isCancelable,boolean isBackCancelable) {
        super(context, R.style.MyDialog);

        this.context = context;
        this.layoutResID = layoutResID;
        this.iscancelable = isCancelable;
        this.isBackCancelable = isBackCancelable;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(layoutResID);//这行一定要写在前面
        setCancelable(iscancelable);//点击外部不可dismiss
        setCanceledOnTouchOutside(isBackCancelable);
        Window window = this.getWindow();
        window.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);
    }
}
