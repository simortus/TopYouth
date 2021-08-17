package com.example.topyouth.home;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.topyouth.R;
import com.example.topyouth.molde.Likes;
import com.example.topyouth.molde.PostModel;
import com.example.topyouth.molde.TopUser;
import com.example.topyouth.utility_classes.DBSingleton;
import com.example.topyouth.view_utils.BottomNavigationHandler;
import com.example.topyouth.utility_classes.FirebaseAuthSingleton;
import com.example.topyouth.view_utils.RecyclerViewAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    //view
    private RelativeLayout notApprovedLayout, userLayout;
    private BottomNavigationView bottomNavigationView;
    private RecyclerViewAdapter viewAdapter;
    private RecyclerView recyclerViewer;
    private FrameLayout frameLayout;

    //firebase
    private FirebaseAuthSingleton authSingleton;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase database;
    private DBSingleton dbSingleton;
    private final ExecutorService executorService = Executors.newCachedThreadPool();


    // context
    private Context mContext;
    private BottomNavigationHandler bottomNavigationHandler;

    //vars
    private final List<PostModel> postList = new ArrayList<>();
    private final List<TopUser> postOwnerList = new ArrayList<>();
    private final List<Likes> postLikes = new ArrayList<>();
    private final List<PostModel> currentUserPosts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();
        findWidgets();
        connectFirebase();
    }

    private void findWidgets() {
        notApprovedLayout = findViewById(R.id.not_approved_layout);
        frameLayout = findViewById(R.id.container);
        userLayout = findViewById(R.id.userLayout);
        bottomNavigationView = findViewById(R.id.bottomNavigationBar);
        bottomNavigationHandler = new BottomNavigationHandler(this, bottomNavigationView);
        bottomNavigationHandler.navigation();
        recyclerViewer = findViewById(R.id.recyclerViewer);
        viewAdapter = new RecyclerViewAdapter(this, postList, postOwnerList);
        recyclerViewer.setAdapter(viewAdapter);

        recyclerViewer.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });


    }

    private void connectFirebase() {
        authSingleton = FirebaseAuthSingleton.getInst(this);
        mAuth = authSingleton.mAuth();
        mUser = authSingleton.getCurrentUser();
        mAuthStateListener = firebaseAuth -> {
            mAuth.addAuthStateListener(this.mAuthStateListener);
        };
        dbSingleton = DBSingleton.getInstance();
        database = dbSingleton.getDbInstance();
        bottomNavigationHandler.isAdminApproved(mUser, notApprovedLayout, userLayout);
        MenuItem homeItem = bottomNavigationView.getMenu().getItem(0);
        homeItem.setChecked(false);
        homeItem.getIcon().setTint(getResources().getColor(R.color.green));

        Runnable one = this::readPosts;
        Runnable two = this::setUpRecyclerView;

        executorService.execute(one);
        executorService.execute(two);
    }

    private void setUpRecyclerView() {
        recyclerViewer.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        recyclerViewer.setLayoutManager(mLayoutManager);
    }

    private void getLikes(final String postId) {
        final DatabaseReference userRef = dbSingleton.getLikesRef();
        final Query query = userRef.child(postId);
        query.limitToFirst(100);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    Log.d(TAG, "getLikes onDataChange: dataSnapshot.exists: " + dataSnapshot.exists());
                    final Likes like = dataSnapshot.getValue(Likes.class);
                    postLikes.add(like);
                    Log.d(TAG, "getLikes onDataChange: LikeList.size: " + postLikes.size());
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    Log.d(TAG, "getLikes onDataChange: dataSnapshot.exists: " + dataSnapshot.exists());
                    final Likes like = dataSnapshot.getValue(Likes.class);
                    postLikes.add(like);
                    Log.d(TAG, "getLikes onDataChange: LikeList.size: " + postLikes.size());
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d(TAG, "getLikes onDataChange: dataSnapshot.exists: " + dataSnapshot.exists());
                    final Likes like = dataSnapshot.getValue(Likes.class);
                    postLikes.add(like);
                    Log.d(TAG, "getLikes onDataChange: LikeList.size: " + postLikes.size());
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                if (dataSnapshot.exists()) {
                    Log.d(TAG, "getLikes onDataChange: dataSnapshot.exists: " + dataSnapshot.exists());
                    final Likes like = dataSnapshot.getValue(Likes.class);
                    postLikes.add(like);
                    Log.d(TAG, "getLikes onDataChange: LikeList.size: " + postLikes.size());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled : dataSnapshot.exists: " + databaseError.getMessage());
            }
        });

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d(TAG, "getLikes onDataChange: dataSnapshot.exists: " + dataSnapshot.exists());
                    final Likes like = dataSnapshot.getValue(Likes.class);
                    postLikes.add(like);
                    Log.d(TAG, "getLikes onDataChange: LikeList.size: " + postLikes.size());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: databaseError: " + databaseError.getMessage());
            }
        });
//        query.addChildEventListener(new ChildEventListener() {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                if (dataSnapshot.exists()){
//                    final String userId = mUser.getUid();
//                    if (!dataSnapshot.getKey().equals(userId)){
//                        Toast.makeText(mContext,"",Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                if (dataSnapshot.exists()){
//                    Log.d(TAG, "onChildAdded: String: "+s);
//                }
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
//                if (dataSnapshot.exists()){
//                    Log.d(TAG, "onChildAdded: String: "+dataSnapshot.getKey());
//                }
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
//                if (dataSnapshot.exists()){
//                    Log.d(TAG, "onChildAdded: String: "+dataSnapshot.getKey());
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                Log.d(TAG, "onCancelled: databaseError: "+databaseError.getMessage());
//            }
//        });

    }

    private void postOwner(final String user_id) {
        Runnable getPostOwnerDetails = () -> {
            final DatabaseReference userRef = dbSingleton.getUsers_ref();
            final Query query = userRef.child(user_id);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        Log.d(TAG, "onDataChange: exists");
                        Log.d(TAG, "onDataChange: node-key: " + dataSnapshot.getKey());
                        TopUser user = dataSnapshot.getValue(TopUser.class);
                        Log.d(TAG, "onDataChange: PostOwnerID: " + user_id);
                        postOwnerList.add(user);
                    }
                    viewAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, "onCancelled: Database error: " + databaseError.getMessage());
                }
            });
        };

        executorService.execute(getPostOwnerDetails);

    }

    private void readPosts() {
        final String user_id = mUser.getUid();
        final DatabaseReference postRef = dbSingleton.getPostsRef();
        final Query query = postRef.getRef();
        //todo need to remove the event listener and add child event listener  to avoid the database errror
        query.addListenerForSingleValueEvent(new ValueEventListener() {
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

                                //filter the posts
                                if (user_id.equals(post.getPostOwnerID())){
                                    currentUserPosts.add(post);
                                    getLikes(post.getPostId());
                                }
                                postList.add(post);
                                Log.d(TAG, "onDataChange: postListSIze: " + postList.size());
                                postOwner(post.getPostOwnerID());

                            }
                            viewAdapter.notifyDataSetChanged();
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

}
