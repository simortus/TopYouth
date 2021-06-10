package com.example.topyouth.utility_classes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.topyouth.home.MainActivity;

public class Traveler  {
    private static final String TAG = "Traveler";

    private final Intent mIntent = new Intent();

    public void gotoWithFlags(@NonNull Context current_activity, @NonNull Class<?extends Activity> destinationClass) {
        mIntent.setClass(current_activity, destinationClass);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        current_activity.startActivity(mIntent);
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
        fragmentManager.popBackStack();
        fragmentManager.beginTransaction().commit();
    }
}
