package com.behealthy.gincos.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.behealthy.gincos.BuildConfig;
import com.behealthy.gincos.R;
import com.behealthy.gincos.firebase_utils.DatabaseContract;
import com.behealthy.gincos.firebase_utils.DatabaseUtils;
import com.behealthy.gincos.fragments.MainFragment;
import com.behealthy.gincos.utils.MyTextView;
import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.Arrays;

public class ProfileActivity extends AppCompatActivity {

    //Firebase variables
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;

    private String TAG = "ProfileActivity";

    //Views
    private CircleImageView profileImage;
    private MyTextView ageTV;
    private MyTextView userNameTV;
    private ProgressBar levelProgressBar;
    private MyTextView levelTV;
    private MyTextView remainderXpTV;

    private long XP = 0;

    // Choose an arbitrary request code value
    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = DatabaseUtils.getDatabase();

        //Assign the views
        profileImage = findViewById(R.id.profile_image);
        ageTV = findViewById(R.id.profile_years);
        userNameTV = findViewById(R.id.profile_name);
        levelProgressBar = findViewById(R.id.profile_level_progress);
        levelTV = findViewById(R.id.profile_level);
        remainderXpTV = findViewById(R.id.profile_current_experience);

        user = mAuth.getCurrentUser();

        //Logout Button
        View logoutBtn = findViewById(R.id.profile_logout_btn);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                finish();
            }
        });

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser newUser = firebaseAuth.getCurrentUser();
                if (newUser != null) {
                    // User is signed in
                    user = newUser;
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                                    .setAvailableProviders(
                                            Arrays.asList(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build(),
                                                    new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER).build()))
                                    .setLogo(R.drawable.banner_bayer_login)
                                    .setTheme(R.style.AppTheme)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };

        DatabaseReference xpReference = mFirebaseDatabase.getReference().child(user.getUid()).child(DatabaseContract.XP);
        xpReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot!=null){
                    XP = (long) dataSnapshot.getValue();
                    setupViews();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (user!=null){
            setupViews();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Handle logout to send user to MainActivity
        if(requestCode == RC_SIGN_IN){
            if (resultCode == RESULT_OK){
                Toast.makeText(ProfileActivity.this,R.string.login_signed_in,Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
            } else if(resultCode == RESULT_CANCELED){
                finish();
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mAuth.removeAuthStateListener(mAuthListener);
    }

    private void setupViews(){
        Uri imageUri = user.getPhotoUrl();
        if(imageUri==null){
            profileImage.setImageResource(R.drawable.profile_picture_placeholder);
        }else {
            Glide.with(this).asBitmap().load(user.getPhotoUrl()).into(profileImage);
        }

        userNameTV.setText(user.getDisplayName());
        levelTV.setText(String.valueOf(MainFragment.calculateLevel(XP)));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            levelProgressBar.setProgress(((int)XP%1000)/10,true);
        }else {
            levelProgressBar.setProgress(((int)XP%1000)/10);
        }
        remainderXpTV.setText(String.valueOf((int)XP%1000)+"/1000");
    }
}
