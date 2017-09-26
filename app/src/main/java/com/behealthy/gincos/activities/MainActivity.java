package com.behealthy.gincos.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.behealthy.gincos.BuildConfig;
import com.behealthy.gincos.R;
import com.behealthy.gincos.firebase_utils.DatabaseUtils;
import com.behealthy.gincos.fragments.CalendarFragment;
import com.behealthy.gincos.fragments.MainFragment;
import com.behealthy.gincos.notifications.NotificationReceiver;
import com.bumptech.glide.Glide;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import de.hdodenhof.circleimageview.CircleImageView;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Contains the drawer and the main functionality of BeHealthy.
 */
public class MainActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {
    
    public DrawerLayout drawerLayout;
    public NavigationView navView;
    
    public static final String ROTATION_KEY = "Device Rotated";
    
    public static final String CURRENT_FRAGMENT_KEY = "CurrentFragment";
    public static final String FRAGMENT_MAIN = "MainFragment";
    public static final String FRAGMENT_CALENDAR = "CalendarFragment";
    public static String currentFragment = "";
    
    //Firebase variables
    private FirebaseAuth mAuth;
    public FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    
    private String TAG = "MainActivity";
    
    // Arbitrary request codes
    private static final int RC_SIGN_IN = 123;
    private static final int NOTIFICATION_RC = 654;
    
    //Settings
    String notificationTimeString;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        //Get user's settings
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        notificationTimeString = prefs.getString(getString(R.string.settings_notification_time_key),
                getString(R.string.settings_notification_time_default));
        
        setupNotifications(false);
        
        mAuth = FirebaseAuth.getInstance();
        
        //Get database
        if (mFirebaseDatabase == null) {
            mFirebaseDatabase = DatabaseUtils.getDatabase();
        }
        
        final Context context = this;
        
        //Drawer Views
        drawerLayout = findViewById(R.id.drawer_layout);
        navView = findViewById(R.id.navview);
        
        //Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ViewCompat.setElevation(toolbar, 8);
        
        if (drawerLayout != null) {
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            
        }
    
        Intent intent = getIntent();
    
        String pastFragment = "";
        
        if(savedInstanceState!=null){
            String savedFragment = savedInstanceState.getString(CURRENT_FRAGMENT_KEY);
            if(savedFragment!=null){
                pastFragment = savedFragment;
            }
        }
    
        setupAuthListener(context, pastFragment);
        
        setupNavigationView(context);
        
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(CURRENT_FRAGMENT_KEY,currentFragment);
        super.onSaveInstanceState(outState);
    }
    
    private void setupNavigationView(final Context context) {
    
        navView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    
                        boolean fragmentTransaction = false;
                        boolean goToSettings = false;
                        Fragment fragment = null;
                        String newFragment = "";
                    
                        switch (menuItem.getItemId()) {
                            case R.id.drawer_home:
                                fragment = new MainFragment();
                                ((MainFragment) fragment).setContext(context);
                                fragmentTransaction = true;
                                newFragment = FRAGMENT_MAIN;
                                break;
                            case R.id.drawer_calendar:
                                fragment = new CalendarFragment();
                                fragmentTransaction = true;
                                newFragment = FRAGMENT_CALENDAR;
                                break;
                            case R.id.drawer_settings:
                                goToSettings = true;
                                break;
                            case R.id.drawer_logout:
                                // Firebase sign out
                                mAuth.signOut();
                                break;
                            default:
                                break;
                        }
                    
                        if (fragmentTransaction) {
                            FragmentTransaction transaction = getSupportFragmentManager()
                                    .beginTransaction();
                            if (currentFragment.equals(FRAGMENT_MAIN)) {
                                transaction.addToBackStack(newFragment);
                            }
                            transaction.replace(R.id.content_frame, fragment).commit();
                        
                            menuItem.setChecked(true);
                            if (menuItem.getItemId() != R.id.drawer_home) {
                                getSupportActionBar().setTitle(menuItem.getTitle());
                            } else {
                                getSupportActionBar().setTitle(R.string.app_name);
                            }
                        } else if (goToSettings) {
                            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                            startActivity(intent);
                        }
                    
                        if (drawerLayout != null) {
                            drawerLayout.closeDrawers();
                        }
                    
                        currentFragment = newFragment;
                    
                        return true;
                    }
                });
    
        navView.setCheckedItem(R.id.drawer_home);
    
        //Header leads to ProfileActivity
        navView.getHeaderView(0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                MainActivity.this.startActivity(intent);
                if (drawerLayout != null) {
                    drawerLayout.closeDrawers();
                }
            }
        });
        
    }
    
    @Override
    public void onBackPressed() {
        //Close drawer if opened
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawers();
        } else if (!currentFragment.equals(FRAGMENT_MAIN)) {
            getSupportFragmentManager().popBackStack();
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.transition_fade,R.anim.transition_slide)
                    .replace(R.id.content_frame, new MainFragment()).commit();
        }else{
            super.onBackPressed();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(MainActivity.this, R.string.login_signed_in, Toast.LENGTH_SHORT)
                        .show();
            } else if (resultCode == RESULT_CANCELED) {
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
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    public static void setDefaultActivityTitle(AppCompatActivity activity) {
        activity.getSupportActionBar().setTitle(R.string.app_name);
    }
    
    private void setupNotifications(boolean isSettingsChanged) {
        
        Calendar c = Calendar.getInstance();
        
        String[] notificationTimeSplit = notificationTimeString.split(":");
        
        c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(notificationTimeSplit[0]));
        c.set(Calendar.MINUTE, Integer.parseInt(notificationTimeSplit[1]));
        
        Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
        PendingIntent pi;
        
        if (isSettingsChanged) {
            //Cancel current reminder time and set a new one
            pi = PendingIntent.getBroadcast(getApplicationContext(), NOTIFICATION_RC, intent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
        } else {
            //Just update the current PI
            pi = PendingIntent.getBroadcast(getApplicationContext(), NOTIFICATION_RC, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
        
        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pi);
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        
        if (key.equals(getString(R.string.settings_notification_time_key))) {
            //Update reminder time
            notificationTimeString = sharedPreferences
                    .getString(getString(R.string.settings_notification_time_key),
                            getString(R.string.settings_notification_time_default));
            setupNotifications(true);
        }
        
    }
    
    private void setupAuthListener(final Context context, final String fragmentName){
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Fragment fragment;
                    boolean setAnimations = false;
                    if(fragmentName == null || fragmentName.equals("")){
                        fragment = new MainFragment();
                    }else{
                        switch (fragmentName){
                            case FRAGMENT_MAIN:
                                fragment = new MainFragment();
                                currentFragment = FRAGMENT_MAIN;
                                break;
                            case FRAGMENT_CALENDAR:
                                fragment = new CalendarFragment();
                                currentFragment = FRAGMENT_CALENDAR;
                                setAnimations = true;
                                break;
                            default:
                                fragment = new MainFragment();
                                currentFragment = FRAGMENT_MAIN;
                                break;
                        }
                    }
                    
                    //Inflate last opened fragment
                    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                    transaction.replace(R.id.content_frame, fragment).commit();
                    
                    //Set up Header for the Drawer
                    View headerView = navView.getHeaderView(0);
                
                    TextView nameTextView = headerView.findViewById(R.id.username);
                    LinearLayout userLayout = headerView.findViewById(R.id.user_layout);
                    CircleImageView profilePhoto = headerView.findViewById(R.id.circle_image);
                
                    nameTextView.setText(user.getDisplayName());
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                    userLayout.setLayoutParams(layoutParams);
                
                    Uri url = user.getPhotoUrl();
                    if (url == null) {
                        profilePhoto.setImageResource(R.drawable.profile_picture_placeholder);
                    } else {
                        Glide.with(context).asBitmap().load(user.getPhotoUrl()).into(profilePhoto);
                    }
                
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                                    .setAvailableProviders(
                                            Arrays.asList(new AuthUI.IdpConfig.Builder(
                                                            AuthUI.EMAIL_PROVIDER).build(),
                                                    new AuthUI.IdpConfig.Builder(
                                                            AuthUI.GOOGLE_PROVIDER).build()))
                                    .setLogo(R.drawable.banner_bayer_login)
                                    .setTheme(R.style.AppTheme)
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };
    }
}
