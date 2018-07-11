package com.elitecrm.rcclient.robot;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.AttributeSet;

import io.rong.imkit.widget.AutoLinkTextView;

/**
 * Created by Loriling on 2018/7/3.
 */

public class RobotAutoLinkTextView extends AutoLinkTextView {
    public RobotAutoLinkTextView(Context context) {
        super(context);
    }

    public RobotAutoLinkTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RobotAutoLinkTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RobotAutoLinkTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setAutoLinkMask(0);
        //super.setText(text, type);
        //RongLinkify.addLinks(this, RongLinkify.ALL);

        Spannable spannable = new SpannableString(text);
        RobotUtils.linkifyUrl(spannable, new RobotMessageClickHandler());

        super.setText(spannable, type);
    }
}
