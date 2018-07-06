package com.elitecrm.rcclient.robot;

import android.text.Spannable;
import android.text.Spanned;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Loriling on 2018/7/3.
 */

public class RobotUtils {
    private static final Pattern URL_PATTERN = Pattern.compile("【转人工】");

    public static void linkifyUrl(
            Spannable spannable, TransferManualClickURLSpan.OnClickListener onClickListener) {
        Matcher m = URL_PATTERN.matcher(spannable);
        while (m.find()) {
            String url = spannable.toString().substring(m.start(), m.end());
            TransferManualClickURLSpan urlSpan = new TransferManualClickURLSpan(url);
            urlSpan.setOnClickListener(onClickListener);
            spannable.setSpan(urlSpan, m.start(), m.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}
