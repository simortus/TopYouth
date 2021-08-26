package com.example.topyouth.utility_classes;

import android.app.Activity;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;
import com.example.topyouth.R;

public final class Toaster extends Toast {
    private static final String TAG = "Toaster";
    private final Activity context;

    public Toaster(Activity context) {
        super(context);
        this.context = context;
    }

    public void displayNoConnectionMessage() {
        final Toast toast = Toast.makeText(context, null, Toast.LENGTH_SHORT);
        final View view1 = context.getLayoutInflater().inflate(R.layout.offline_layout, null);
        WindowManager.LayoutParams wlp = context.getWindow().getAttributes();
        wlp.windowAnimations = R.style.Widget_AppCompat_Light_Spinner_DropDown_ActionBar;
        context.getWindow().setAttributes(wlp);
        toast.setView(view1);
        toast.setGravity(Gravity.CENTER,wlp.x,wlp.y);
        toast.show();
    }


}
