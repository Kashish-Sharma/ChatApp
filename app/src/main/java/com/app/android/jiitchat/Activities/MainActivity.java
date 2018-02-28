package com.app.android.jiitchat.Activities;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.app.android.jiitchat.Adapters.SectionPagerAdapter;
import com.app.android.jiitchat.Fragments.ChatFragment;
import com.app.android.jiitchat.Fragments.FriendsFragment;
import com.app.android.jiitchat.Fragments.BlogFragment;
import com.app.android.jiitchat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

public class MainActivity extends AppCompatActivity {

    //toolbar
    private Toolbar mToolbar;

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;

    private ViewPager mViewPager;
    private SectionPagerAdapter mSectionPagerAdapter;
    private TabLayout mTabLayout;


    //Fragments
    BlogFragment blogFragment;
    ChatFragment chatFragment;
    FriendsFragment friendsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar)findViewById(R.id.chatPageToolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("MyApp");


        //Tabs
        mViewPager = (ViewPager) findViewById(R.id.main_tabPager);
        mViewPager.setOffscreenPageLimit(3);

        mTabLayout = (TabLayout) findViewById(R.id.mainTabs);
        mTabLayout.setupWithViewPager(mViewPager);
        setupViewPager(mViewPager);

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser()!=null){
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(mAuth.getCurrentUser().getUid());
        }


    }

    public void onStart() {
        super.onStart();
        checkAuthUpdate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser!=null){
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        }
    }

    //if not signed-in goto login/signup page
    private void checkAuthUpdate(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null){
            Intent gotoLoginPage = new Intent(MainActivity.this,LoginActivity.class);
            gotoLoginPage.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(gotoLoginPage);
            finish();
        } else {

            mUserRef.child("online").setValue("true");
        }
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

        int itemId = item.getItemId();

        switch (itemId){
            case R.id.main_logout_button:
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser!=null){
                    mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
                }
                FirebaseAuth.getInstance().signOut();
                checkAuthUpdate();
                break;
            case R.id.main_account_settings:
                Intent gotoSettings = new Intent(MainActivity.this,SettingsActivity.class);
                startActivity(gotoSettings);
                break;
            case R.id.all_users:
                Intent gotoAllUsers = new Intent(MainActivity.this,UsersActivity.class);
                startActivity(gotoAllUsers);
        }
        return true;
    }

    private void setupViewPager(ViewPager viewPager){

        mSectionPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager());
        blogFragment = new BlogFragment();
        chatFragment = new ChatFragment();
        friendsFragment = new FriendsFragment();

        mSectionPagerAdapter.addFragment(blogFragment,"BLOG");
        mSectionPagerAdapter.addFragment(chatFragment,"CHAT");
        mSectionPagerAdapter.addFragment(friendsFragment,"FRIENDS");
        viewPager.setAdapter(mSectionPagerAdapter);
        viewPager.setCurrentItem(1);
    }

}
