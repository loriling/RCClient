package com.elitecrm.rcclient.message;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.elitecrm.rcclient.R;
import com.elitecrm.rcclient.message.RobotMessage;
import com.elitecrm.rcclient.robot.RobotAutoLinkTextView;

import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.ILinkClickListener;
import io.rong.imkit.widget.LinkTextViewMovementMethod;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imlib.model.Message;

/**
 * Created by Loriling on 2018/7/11.
 */
@ProviderTag(messageContent = RobotMessage.class)
public class RobotMessageItemProvider extends IContainerItemProvider.MessageProvider<RobotMessage> {
    class ViewHolder {
        RobotAutoLinkTextView message;
    }

    @Override
    public void bindView(final View v, int i, RobotMessage robotMessage, final UIMessage data) {
        ViewHolder holder = (ViewHolder) v.getTag();

        if (data.getMessageDirection() == Message.MessageDirection.SEND) {//消息方向，自己发送的
            holder.message.setBackgroundResource(io.rong.imkit.R.drawable.rc_ic_bubble_right);
        } else {
            holder.message.setBackgroundResource(io.rong.imkit.R.drawable.rc_ic_bubble_left);
        }
        holder.message.setText(robotMessage.getMessage());

        final RobotAutoLinkTextView textView = holder.message;
        if(data.getTextMessageContent() != null) {
            int len = data.getTextMessageContent().length();
            if(v.getHandler() != null && len > 500) {
                v.getHandler().postDelayed(new Runnable() {
                    public void run() {
                        textView.setText(data.getTextMessageContent());
                    }
                }, 50L);
            } else {
                textView.setText(data.getTextMessageContent());
            }
        }

        holder.message.setMovementMethod(new LinkTextViewMovementMethod(new ILinkClickListener() {
            public boolean onLinkClick(String link) {
                boolean result = false;
//                RongIM.ConversationClickListener clickListener = RongContext.getInstance().getConversationClickListener();
//                boolean result = clickListener.onMessageLinkClick(v.getContext(), link, data.getMessage());

                return result;
            }
        }));
    }

    @Override
    public Spannable getContentSummary(RobotMessage robotMessage) {
        return new SpannableString(robotMessage.getMessage());
    }

    @Override
    public void onItemClick(View view, int i, RobotMessage robotMessage, UIMessage uiMessage) {

    }

    @Override
    public View newView(Context context, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_robot_message, null);
        ViewHolder holder = new ViewHolder();
        holder.message = view.findViewById(android.R.id.text1);
        view.setTag(holder);
        return view;
    }
}
