package com.elitecrm.rcclient.robot;

import android.annotation.SuppressLint;
import android.text.style.URLSpan;
import android.view.View;

/**
 * Created by Loriling on 2018/7/3.
 */

@SuppressLint("ParcelCreator")
public class RobotMessageClickURLSpan extends URLSpan {
    private OnClickListener mOnClickListener;

    public RobotMessageClickURLSpan(String url) {
        super(url);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        mOnClickListener = onClickListener;
    }

    @Override
    public void onClick(View widget) {
        if (mOnClickListener == null) {
            super.onClick(widget);
        } else {
            mOnClickListener.onClick(widget, getURL());
        }
    }

    public interface OnClickListener {
        void onClick(View view, String url);
    }
}
