package com.elitecrm.rcclient.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.elitecrm.rcclient.R;
import com.elitecrm.rcclient.util.Constants;
import com.elitecrm.rcclient.util.MessageUtils;

import io.rong.imkit.RongIM;
import io.rong.imlib.model.Conversation;
import io.rong.message.InformationNotificationMessage;

/**
 * Created by Loriling on 2017/2/20.
 *
 * 满意：1
 * 不满意：0
 * 相关值需要和服务端rating表中的id值匹配
 */

public class RatingFragment extends DialogFragment {

    public static RatingFragment newInstance() {
        RatingFragment f = new RatingFragment();
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ViewGroup ratingVG = (ViewGroup) getActivity().findViewById(R.id.rating);
        final View layout = getActivity().getLayoutInflater().inflate(R.layout.rating_dialog, ratingVG);

        //切换到不满意时候显示备注输入框
        RadioGroup rg = (RadioGroup) layout.findViewById(R.id.rating_rg);
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                EditText et = (EditText) layout.findViewById(R.id.comments);
                if(checkedId == R.id.satisfied){
                    et.setVisibility(View.INVISIBLE);
                } else {
                    et.setVisibility(View.VISIBLE);
                }
            }
        });
        //初始备注框不显示
        EditText et = (EditText) layout.findViewById(R.id.comments);
        et.setVisibility(View.INVISIBLE);

        return new AlertDialog.Builder(getActivity()).setTitle("满意度评价").setView(layout).setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                RadioGroup rg = (RadioGroup) layout.findViewById(R.id.rating_rg);
                int checkedId = rg.getCheckedRadioButtonId();
                String ratingName= "不满意";
                int ratingId = 0;
                if(checkedId == R.id.satisfied){
                    ratingName = "满意";
                    ratingId = 1;
                }
                EditText et = (EditText) layout.findViewById(R.id.comments);
                MessageUtils.sendRating(ratingId, et.getText().toString());

                //显示评价结果到聊天界面
                InformationNotificationMessage informationMessage = InformationNotificationMessage.obtain("您的评价是【" + ratingName + "】");
                RongIM.getInstance().insertMessage(Conversation.ConversationType.PRIVATE, Constants.CHAT_TARGET_ID, null, informationMessage, null);
            }
        }).create();

    }
}
