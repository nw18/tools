package com.newind.android.views;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.TextView;

import com.newind.android.R;

/**
 * Created by newind on 17-2-9.
 */

public class DialogProcessing extends Dialog {
    public DialogProcessing(Context context) {
        super(context, R.style.DialogProcessing);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView textView = new TextView(getContext());
        textView.setTextColor(0xFFFFFFFF);
        textView.setBackgroundColor(0xFF000000);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        textView.setPadding(20, 10, 20, 10);
        textView.setText(R.string.please_wait);
        setContentView(textView);
    }

    @Override
    public void show() {
        super.show();
        setCancelable(false);
        setCanceledOnTouchOutside(false);
    }
}
