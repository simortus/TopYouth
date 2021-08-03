package com.example.topyouth.utility_classes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.transition.Fade;
import android.transition.Transition;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.topyouth.R;
import com.example.topyouth.home.MainActivity;

import static androidx.core.content.ContextCompat.getSystemService;

public class Traveler  {
    private static final String TAG = "Traveler";

    private final Intent mIntent = new Intent();

    public void gotoWithFlags(@NonNull Activity current_activity, @NonNull Class<?extends Activity> destinationClass) {
        mIntent.setClass(current_activity, destinationClass);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        current_activity.startActivity(mIntent,null); // added null to deactivate any transition from the destination activity
        current_activity.overridePendingTransition(R.anim.push_left_in,R.anim.push_left_out);
    }

    public void goFragment(@NonNull FragmentManager fragmentManager, Fragment fragment, @NonNull int id) {
        Fragment fragmentToUse = fragmentManager.findFragmentById(id);
        if (fragmentToUse == null) {
            fragmentToUse = fragment;
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.addToBackStack(String.valueOf(fragmentToUse));
            fragmentTransaction.add(id, fragmentToUse).commit();
        }
    }


    public void removeFromStack(FragmentManager fragmentManager) {
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
            fragmentManager.beginTransaction().commit();
        }
    }

    public void hideKeyboard(View view, Context context){
        // Check if no view has focus:
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
