package com.example.nedadlipovac.lappitchat;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
private android.support.v7.widget.Toolbar mToolBar;
private ViewPager mViewPager;
private SectionsPagerAdapter mSectionsPagerAdapter;
private TabLayout mTabLayOut;
private DatabaseReference mUsersRef;
    public MainActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
       mToolBar=(android.support.v7.widget.Toolbar) findViewById(R.id.mainPageToolBar);
       setSupportActionBar(mToolBar);
       getSupportActionBar().setTitle("Lappit Chat");
       //Tabs
        mViewPager=(ViewPager) findViewById(R.id.mainPager);
        mSectionsPagerAdapter=new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTabLayOut=(TabLayout)findViewById(R.id.mainTabs);
        mTabLayOut.setupWithViewPager(mViewPager);

        if(mAuth.getCurrentUser()!=null){
            mUsersRef=FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        // updateUI(currentUser);
        if (currentUser == null) {
            sendToStert();
        }
        else {
            mUsersRef.child("online").setValue("true");
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser!=null){
            mUsersRef.child("online").setValue(ServerValue.TIMESTAMP);


        }

    }

    private void sendToStert() {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);
         getMenuInflater().inflate(R.menu.main_menu,menu);
         return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
         if(item.getItemId()==R.id.mainLogOutBtn){
             FirebaseAuth.getInstance().signOut();
             sendToStert();
         }
         if(item.getItemId()==R.id.mainAccSettingsBtn)
         {
             Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
             startActivity(settingsIntent);
         }
        if(item.getItemId()==R.id.mainAllUsersBtn)
        {
            Intent startIntent = new Intent(MainActivity.this, UsersActivity.class);
            startActivity(startIntent);
        }
         return true;
    }
}
