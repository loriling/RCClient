package com.elitecrm.rcclient.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.elitecrm.rcclient.R;

public class ConversationListActivity extends FragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversationlist);
    }
}
