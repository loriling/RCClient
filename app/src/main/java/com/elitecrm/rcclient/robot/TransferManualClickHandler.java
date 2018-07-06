package com.elitecrm.rcclient.robot;

import android.view.View;

/**
 * Created by Loriling on 2018/7/3.
 */

public class TransferManualClickHandler implements TransferManualClickURLSpan.OnClickListener{
    @Override
    public void onClick(View view, String url) {
        System.out.println("onclick: " + url);
    }
}
