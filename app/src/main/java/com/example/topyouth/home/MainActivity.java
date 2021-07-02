package com.example.topyouth.home;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.topyouth.R;
import com.example.topyouth.login.LoginActivity;
import com.example.topyouth.molde.PostModel;
import com.example.topyouth.molde.TopUser;
import com.example.topyouth.utility_classes.DBSingelton;
import com.example.topyouth.view_utils.BottomNavigationHandler;
import com.example.topyouth.utility_classes.FirebaseAuthSingleton;
import com.example.topyouth.view_utils.RecyclerViewAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private static final String TAG = "MainActivity";
    //view
    private FloatingActionButton logOut;
    private RelativeLayout notApprovedLayout, userLayout;
    private BottomNavigationView bottomNavigationView;
    private RecyclerViewAdapter recyclerViewAdapter;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView recyclerView;


    //firebase
    private FirebaseAuthSingleton authSingleton;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase database;
    private DBSingelton dbSingelton;


    // context
    private Context mContext;
    private BottomNavigationHandler bottomNavigationHandler;

    //vars
    private final List<PostModel> postList = new ArrayList<>();
    private final List<TopUser> postOwnerList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();
        recyclerViewAdapter = new RecyclerViewAdapter(mContext, postList, postOwnerList);


        findWidgets();

        connectFirebase();
        buttonListeners();
        bottomNavigationHandler.isAdminApproved(mUser, notApprovedLayout, userLayout);

    }

    private void findWidgets() {
        logOut = findViewById(R.id.floating_button);
        notApprovedLayout = findViewById(R.id.not_approved_layout);
        userLayout = findViewById(R.id.userLayout);
        bottomNavigationView = findViewById(R.id.bottomNavigationBar);
        bottomNavigationHandler = new BottomNavigationHandler(mContext, bottomNavigationView);
        bottomNavigationHandler.navigation();
        recyclerView = findViewById(R.id.recyclerViewer);


    }

    private void buttonListeners() {
        logOut.setOnClickListener(this);
    }

    private void connectFirebase() {
        authSingleton = FirebaseAuthSingleton.getInst(mContext);
        mAuth = authSingleton.mAuth();
        mUser = authSingleton.getCurrentUser();
        mAuthStateListener = firebaseAuth -> {
            mAuth.addAuthStateListener(this.mAuthStateListener);
        };
        dbSingelton = DBSingelton.getInstance();
        database = dbSingelton.getDbInstance();

        ((Runnable) this::readPosts).run();
        setUpRecyclerView();

    }

    private void setUpRecyclerView() {
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(mLayoutManager);
    }

    private final void postOwner(final String user_id) {
        final DatabaseReference userRef = database.getReference("users");
        Query query = userRef.child(user_id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d(TAG, "onDataChange: exists");
                    Log.d(TAG, "onDataChange: node-key: " + dataSnapshot.getKey());
                    TopUser user = dataSnapshot.getValue(TopUser.class);
                    Log.d(TAG, "onDataChange: PostOwnerID: " + user.getUserId());
                    Log.d(TAG, "onDataChange: PostOwnerID: " + user_id);
                    postOwnerList.add(user);
                    recyclerViewAdapter.notifyDataSetChanged();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Database error: " + databaseError.getMessage());

            }
        });
    }

    private void readPosts() {
        final DatabaseReference postRef = database.getReference("posts");
        Query query = postRef.getRef();
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d(TAG, "onDataChange: post Node exists");
                    if (dataSnapshot.hasChildren()) {
                        Log.d(TAG, "onDataChange: post Node has children");
                        for (DataSnapshot ds : dataSnapshot.getChildren()) {
                            Log.d(TAG, "onDataChange: Post_Owner_ID: " + ds.getKey());
                            for (DataSnapshot dss : ds.getChildren()) {
                                PostModel post = dss.getValue(PostModel.class);
                                Log.d(TAG, "onDataChange: Post ID: " + post.getPostId());
                                postList.add(post);
                                Log.d(TAG, "onDataChange: postListSIze: " + postList.size());
                                postOwner(post.getPostOwnerID());
                                recyclerViewAdapter.notifyDataSetChanged();
                            }

                        }
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: Error: " + databaseError);

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.floating_button:
                signOut();
                Log.d(TAG, "onClick: FloatingActionButton clicked");
                break;
        }
    }


    private void signOut() {
        mAuth.signOut();
        Toast.makeText(mContext, "Logged out", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, LoginActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d(TAG, "onItemSelected: Item clicked: " + view.getId());

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }
}
