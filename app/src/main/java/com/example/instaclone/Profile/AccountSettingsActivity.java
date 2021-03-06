package com.example.instaclone.Profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.instaclone.R;
import com.example.instaclone.Utils.BottomNavigationViewHelper;
import com.example.instaclone.Utils.FirebaseMethods;
import com.example.instaclone.Utils.SectionsStatePagerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;

public class AccountSettingsActivity extends AppCompatActivity {

    private static final String TAG = "AcntSettingAct/DEBUG";
    // Notes: ProfileActivity is 4
    private static final int ACTIVITY_NUM = 4;
    private Context mContext;


    public SectionsStatePagerAdapter pagerAdapter;
    private ViewPager mViewPager;
    private RelativeLayout mRelativelayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountsettings);
        Log.d(TAG, "onCreate: started");

        mContext = AccountSettingsActivity.this;
        mViewPager = (ViewPager) findViewById(R.id.viewpager_container);
        mRelativelayout = (RelativeLayout) findViewById(R.id.relLayout1);


        // Notes: Set ups
        setupSettingsList();
        setupBottomNavigationView();
        setupFragments();
        getIncomingIntent();


        // Notes: backarrow OnClick for navigating back to ProfileActivity
        ImageView backArrow = (ImageView) findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "\tonClick: navigating back to 'ProfileActivity");
                finish();
            }
        });


        
    }



    private void getIncomingIntent()
    {
        Intent intent = getIntent();


        // Notes: If there is an imageUrl attached as an extra, then it was chosen from the gallery/photo fragment
        if(intent.hasExtra(getString(R.string.selected_image)) || intent.hasExtra(getString(R.string.selected_bitmap)))
        {
            Log.d(TAG, "\tgetIncomingIntent: New incoming imgURL");
            if(intent.getStringExtra(getString(R.string.return_to_fragment)).equals(getString(R.string.edit_profile_fragment)))
            {
                // Notes: Intent from GalleryFragment with imgURL
                if(intent.hasExtra(getString(R.string.selected_image)))
                {
                    // Notes: Set the new profile picture
                    FirebaseMethods firebaseMethods = new FirebaseMethods(mContext);
                    // Notes: TODO - rewrite this line for easier read
                    firebaseMethods.uploadNewPhoto(getString(R.string.profile_photo),
                            null,
                            0,
                            intent.getStringExtra(getString(R.string.selected_image)),
                            null);
                }
                // Notes: Intent from PhotoFragment with bitmap
                else if(intent.hasExtra(getString(R.string.selected_bitmap)))
                {
                    // Notes: Set the new profile picture
                    FirebaseMethods firebaseMethods = new FirebaseMethods(mContext);
                    // Notes: TODO - rewrite this line for easier read
                    firebaseMethods.uploadNewPhoto(getString(R.string.profile_photo),
                            null,
                            0,
                            null,
                            (Bitmap) intent.getParcelableExtra(getString(R.string.selected_bitmap)));
                }


            }
        }








        // Notes: TODO - try to rewrite this for easier understanding
        if(intent.hasExtra(getString(R.string.calling_activity)))
        {
            Log.d(TAG, "getIncomingIntent: received incoming intent from " + getString(R.string.profile_activity));
            setViewPager(pagerAdapter.getFragmentNumber(getString(R.string.edit_profile_fragment)));
        }

    }


    private void setupFragments()
    {
        pagerAdapter = new SectionsStatePagerAdapter(getSupportFragmentManager(), 1);

        // Notes: Fragment 0
        pagerAdapter.addFragment(new EditProfileFragment(), getString(R.string.edit_profile_fragment));
        // Notes: Fragment 1
        pagerAdapter.addFragment(new SignOutFragment(), getString(R.string.sign_out_fragment));
    }


    public void setViewPager(int fragmentNumber)
    {
        /*
            Notes: In activity_accountsettings.xml, the ViewPager is included right above the
                RelativeLayout, therefore we want to hide the RelativeLayout when inflating a fragment
                on the ViewPager
         */
        mRelativelayout.setVisibility(View.GONE);

        Log.d(TAG, "\tsetViewPager: navigating to fragment #: " + fragmentNumber);
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setCurrentItem(fragmentNumber);
    }


    private void setupSettingsList()
    {
        Log.d(TAG, "\tsetupSettingsList: initializing 'Account Settings' list");
        ListView listView = (ListView) findViewById(R.id.lvAccountSettings);

        ArrayList<String> options = new ArrayList<>();
        // Notes" fragment 0
        options.add(getString(R.string.edit_profile_fragment));
        // Notes: fragment 1
        options.add(getString(R.string.sign_out_fragment));


        ArrayAdapter adapter = new ArrayAdapter(mContext, android.R.layout.simple_list_item_1, options);
        listView.setAdapter(adapter);


        // Notes: Navigating to fragments from the list options
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Log.d(TAG, "\tonItemClick: navigating to fragment #: " + position);
                setViewPager(position);
            }
        });
    }


    /**
     * Notes: BottomNavigationView setup
     */
    private void setupBottomNavigationView()
    {
        Log.d(TAG, "\tsetupBottomNavigationView: setting up BottomNavigationView");

        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);

        BottomNavigationViewHelper.enableNavigation(mContext, this, bottomNavigationViewEx);

        // Notes: Highlighting the correct Icon when navigating
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }







}
