package com.elitecrm.rcclient.message;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.elitecrm.rcclient.R;

import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.utilities.RongUtils;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imkit.widget.provider.IContainerItemProvider;

@ProviderTag(messageContent = CardMessage.class)
public class CardMessageItemProvider extends IContainerItemProvider.MessageProvider<CardMessage> {

    class ViewHolder {
        TextView title;
        AsyncImageView imageView;
        TextView price;
        TextView priceSub;
        TextView from;

        public ViewHolder() {
        }
    }

    @Override
    public View newView(Context context, ViewGroup group) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_card_message, null);
        ViewHolder holder = new ViewHolder();
        holder.imageView = (AsyncImageView)view.findViewById(R.id.card_img);
        holder.price = (TextView)view.findViewById(R.id.card_price);
        holder.priceSub = (TextView)view.findViewById(R.id.card_price_sub);
        holder.title = (TextView) view.findViewById(R.id.card_title);
        holder.from = (TextView)view.findViewById(R.id.card_from);
        int w = RongUtils.getScreenWidth() - RongUtils.dip2px(32.0F);
        view.setLayoutParams(new ViewGroup.LayoutParams(w, -2));
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, int i, CardMessage cardMessage, UIMessage uiMessage) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.imageView.setResource(cardMessage.getImageUri(), 0);
        holder.price.setText(cardMessage.getPriceMain());
        holder.priceSub.setText(cardMessage.getPriceSub());
        holder.title.setText(cardMessage.getTitle());
        holder.from.setText(cardMessage.getFrom());
    }

    @Override
    public Spannable getContentSummary(CardMessage data) {
        return null;
    }

    @Override
    public void onItemClick(View view, int position, CardMessage content, UIMessage message) {
        String action = "io.rong.imkit.intent.action.webview";
        Intent intent = new Intent(action);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("url", content.getUrl());
        intent.setPackage(view.getContext().getPackageName());
        view.getContext().startActivity(intent);
    }

}