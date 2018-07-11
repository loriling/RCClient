package com.elitecrm.rcclient.robot;

import android.text.Spannable;
import android.text.Spanned;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Loriling on 2018/7/3.
 */

public class RobotUtils {
    private static final Pattern TRANSFER_MANUAL_PATTERN = Pattern.compile("【转人工】");
    private static final Pattern RECOMMED_PATTERN = Pattern.compile("【[^】]+】");

    public static void linkifyUrl(
            Spannable spannable, RobotMessageClickURLSpan.OnClickListener onClickListener) {
        Matcher m = RECOMMED_PATTERN.matcher(spannable);
        while (m.find()) {
            String url = spannable.toString().substring(m.start(), m.end());
            RobotMessageClickURLSpan urlSpan = new RobotMessageClickURLSpan(url);
            urlSpan.setOnClickListener(onClickListener);
            spannable.setSpan(urlSpan, m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }


}
