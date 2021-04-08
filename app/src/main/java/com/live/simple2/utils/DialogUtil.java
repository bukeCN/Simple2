package com.live.simple2.utils;

import android.app.Dialog;
import android.content.Context;
import android.view.WindowManager;

import androidx.fragment.app.DialogFragment;

public class DialogUtil {

    public static Dialog createDialog(Context context, int resId){
        Dialog dialog = new Dialog(context);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.MATCH_PARENT);
        dialog.setContentView(resId);
        return dialog;
    }

}
