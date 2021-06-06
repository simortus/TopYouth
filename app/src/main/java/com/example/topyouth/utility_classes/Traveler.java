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

public class Traveler extends Activity {
    private static final String TAG = "Traveler";

    public void gotoWithFlags(@NonNull Context context, @NonNull Class<?extends Activity> cls) {
        startActivity(new Intent(context, cls)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
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
