package com.elitecrm.rcclient.util;

import android.app.Activity;
import android.app.DialogFragment;

import com.elitecrm.rcclient.entity.Chat;
import com.elitecrm.rcclient.fragment.RatingFragment;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by Loriling on 2017/2/20.
 */

public class ActivityUtils {
    /**
     * 显示满意度对话框
     */
    public static void showRatingDialog(Activity activity, long sessionId){
        if (activity != null) {
            DialogFragment ratingFragment = RatingFragment.newInstance(sessionId);
            ratingFragment.show(activity.getFragmentManager(), "dialog");
        }
    }
    public static void showRatingDialog(long sessionId){
        showRatingDialog(getCurrentActivity(), sessionId);
    }

    public static void showRatingDialog(){
        showRatingDialog(getCurrentActivity(), Chat.getInstance().getSession().getId());
    }

    /**
     * 获取当前Activity
     * @return
     */
    public static Activity getCurrentActivity () {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(
                    null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            Map activities = (Map) activitiesField.get(activityThread);
            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    Activity activity = (Activity) activityField.get(activityRecord);
                    return activity;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
