package com.app.android.jiitchat.Activities;

import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.android.jiitchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private TextView mProfileName, mProfileStatus, mProfileEmail, friendsStar;
    private CircleImageView mProfileImageView;
    private Button mProfileSendRequestButton;
    private Button mProfileDeclineRequestButton;

    private DatabaseReference mNotificationDatabase;
    private DatabaseReference mUserReference;
    private DatabaseReference mFriendReqDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mRootRef;
    private FirebaseUser mCurrentUser;
    private FirebaseUser mOtherUser;

    private static final int NOT_FRIENDS = 0;
    private static final int FRIENDS_REQUEST_SENT = 1;
    private static final int FRIEND_REQUEST_RECEIVED = 2;
    private static final int FRIENDS = 3;


    private int mCurrentState;

    private LinearLayout linearLayout;
    private AnimationDrawable animationDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String userId = getIntent().getStringExtra("user-id");

        mProfileImageView = (CircleImageView) findViewById(R.id.profile_image);
        mProfileName = (TextView) findViewById(R.id.profile_displayName);
        mProfileStatus = (TextView) findViewById(R.id.profile_status);
        mProfileEmail = (TextView)findViewById(R.id.profile_email);
        friendsStar = (TextView) findViewById(R.id.friendsStar);
        //mProfileFriendsCount = (TextView) findViewById(R.id.totalfriends);
        mProfileSendRequestButton = (Button) findViewById(R.id.send_req_button);
        mProfileDeclineRequestButton = (Button) findViewById(R.id.decline_req_button);
        mProfileDeclineRequestButton.setVisibility(View.INVISIBLE);
        mProfileDeclineRequestButton.setEnabled(false);

        linearLayout = (LinearLayout) findViewById(R.id.myLinearLayout);
        animationDrawable = (AnimationDrawable) linearLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2500);
        animationDrawable.setExitFadeDuration(2500);
        animationDrawable.start();


        mCurrentState = 0;

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();


        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");

        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("friends");
        mFriendDatabase.keepSynced(true);

        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("friend_req");
        mFriendReqDatabase.keepSynced(true);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUserReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        mUserReference.keepSynced(true);
        mUserReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String displayName = dataSnapshot.child("Name").getValue().toString();
                String displayStatus = dataSnapshot.child("Status").getValue().toString();
                final String image = dataSnapshot.child("Image").getValue().toString();

                mProfileName.setText(displayName);
                mProfileStatus.setText(displayStatus);

                Picasso.with(ProfileActivity.this).load(image)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.male).into(mProfileImageView, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(ProfileActivity.this).load(image)
                                .placeholder(R.drawable.male).into(mProfileImageView);
                    }
                });


                // -----------------FRIEND LIST / REQUEST FEATURE -------------------
                mFriendReqDatabase.child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.hasChild(userId)){
                            String request_type = dataSnapshot.child(userId).child("request_type").getValue().toString();
                            if (request_type.equals("received")){
                                mCurrentState = FRIEND_REQUEST_RECEIVED;
                                mProfileSendRequestButton.setText("Accept");
                                mProfileSendRequestButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_sentiment_satisfied_white_24dp,0,0,0);
                                mProfileSendRequestButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.green)));
                                mProfileDeclineRequestButton.setVisibility(View.VISIBLE);
                                mProfileDeclineRequestButton.setEnabled(true);

                            } else if (request_type.equals("sent")){
                                mCurrentState = FRIENDS_REQUEST_SENT;
                                mProfileSendRequestButton.setText("Cancel Request");
                                mProfileSendRequestButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.clearwhite,0,0,0);
                                mProfileSendRequestButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                                mProfileDeclineRequestButton.setVisibility(View.INVISIBLE);
                                mProfileDeclineRequestButton.setEnabled(false);

                            }
                        } else {
                            mFriendDatabase.child(mCurrentUser.getUid()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(userId)){
                                        mCurrentState = FRIENDS;
                                        mProfileSendRequestButton.setText("Unfriend");
                                        mProfileSendRequestButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.sentiment_neutral_white,0,0,0);
                                        mProfileSendRequestButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));

                                        mProfileDeclineRequestButton.setVisibility(View.INVISIBLE);
                                        mProfileDeclineRequestButton.setEnabled(false);

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                        //Dismiss dialog here
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfileSendRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProfileSendRequestButton.setEnabled(false);

                // - -----------NOT FRIENDS STATE --------------

                if (mCurrentState == NOT_FRIENDS){

                    DatabaseReference newNotificationRef = mRootRef.child("notifications")
                            .child(userId).push();

                    String notificationId = newNotificationRef.getKey();

                     HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("FROM",mCurrentUser.getUid());
                    notificationData.put("TYPE","REQUEST");

                    Map requestMap = new HashMap();
                    requestMap.put("friend_req/" + mCurrentUser.getUid() + "/" + userId + "/request_type","sent");
                    requestMap.put("friend_req/" + userId + "/" + mCurrentUser.getUid() + "/request_type","received");
                    requestMap.put("notifications/" + userId + "/" + notificationId, notificationData);


                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError!=null){
                                Toast.makeText(ProfileActivity.this,"There was an error in sending request",Toast.LENGTH_SHORT).show();
                            } else {
                                friendsStar.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_half_black_24dp,0,0,0);
                                mCurrentState = FRIENDS_REQUEST_SENT;
                                mProfileSendRequestButton.setText("Cancel Request");
                                mProfileSendRequestButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                            }
                            mProfileSendRequestButton.setEnabled(true);

                            mProfileDeclineRequestButton.setVisibility(View.INVISIBLE);
                            mProfileDeclineRequestButton.setEnabled(false);
                        }
                    });

                }
                // ---------------------------NOT FRIENDS STATE COMPLETED -------------------------------------

                // - -------------------CANCEL FRIEND STATE --------------------
                else if (mCurrentState == FRIENDS_REQUEST_SENT){

                    mFriendReqDatabase.child(mCurrentUser.getUid()).child(userId).removeValue().
                    addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful())
                            {
                                mFriendReqDatabase.child(userId).child(mCurrentUser.getUid()).removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mProfileSendRequestButton.setEnabled(true);
                                                friendsStar.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_border_black_24dp,0,0,0);
                                                mCurrentState = NOT_FRIENDS;
                                                mProfileSendRequestButton.setText("Add Friend");
                                                mProfileSendRequestButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.person_add_white,0,0,0);
                                                mProfileSendRequestButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue)));
                                                mProfileSendRequestButton.setEnabled(true);
                                                mProfileDeclineRequestButton.setVisibility(View.INVISIBLE);
                                                mProfileDeclineRequestButton.setEnabled(false);

                                            }
                                        });
                            } else {

                            }
                        }
                    });
                }
                //-------------------------------------CANCEL FRIEND STATE COMPLETED---------------------------------------------

                // -----------------------REQUEST RECEIVED STATE --------------------
                else if (mCurrentState == FRIEND_REQUEST_RECEIVED){
                    friendsStar.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_half_black_24dp,0,0,0);
                    final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("friends/" + mCurrentUser.getUid() + "/" + userId + "/date",currentDate);
                    friendsMap.put("friends/" + userId + "/" + mCurrentUser.getUid() + "/date",currentDate);

                    friendsMap.put("friend_req/" + mCurrentUser.getUid() + "/" + userId,null);
                    friendsMap.put("friend_req/" + userId + "/" + mCurrentUser.getUid(),null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null){
                                mProfileSendRequestButton.setEnabled(true);
                                mCurrentState = FRIENDS;
                                friendsStar.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_full_black_24dp,0,0,0);
                                mProfileSendRequestButton.setText("Unfriend");
                                mProfileSendRequestButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.sentiment_neutral_white,0,0,0);
                                mProfileSendRequestButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));

                                mProfileDeclineRequestButton.setVisibility(View.INVISIBLE);
                                mProfileDeclineRequestButton.setEnabled(false);
                            } else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                }
                // -------------------------------------REQUEST RECEIVED STATE COMPLETED-----------------------------------------------

                // ----------------------------------FRIENDS STATE--------------------------------------------------
                else if (mCurrentState == FRIENDS){
                    friendsStar.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_full_black_24dp,0,0,0);

                    Map unfriendMap = new HashMap();
                    unfriendMap.put("friends/" + mCurrentUser.getUid() + "/" + userId,null);
                    unfriendMap.put("friends/" + userId + "/" + mCurrentUser.getUid(),null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null){
                                mCurrentState = NOT_FRIENDS;
                                friendsStar.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_star_full_black_24dp,0,0,0);
                                mProfileSendRequestButton.setText("Add Friend");
                                mProfileSendRequestButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.person_add_white,0,0,0);
                                mProfileSendRequestButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue)));

                                mProfileDeclineRequestButton.setVisibility(View.INVISIBLE);
                                mProfileDeclineRequestButton.setEnabled(false);
                            } else {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();
                            }
                            mProfileSendRequestButton.setEnabled(true);
                        }
                    });

                }
                // ------------------------------------------FRIENDS STATE COMPLETED---------------------------------------------

        }

        });

        //-----------------------------DECLINE REQUEST------------------------------------------
        mProfileDeclineRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map declineMap = new HashMap();
                declineMap.put("friend_req/" + mCurrentUser.getUid() + "/" + userId,null);
                declineMap.put("friend_req/" + userId + "/" + mCurrentUser.getUid(),null);

                mRootRef.updateChildren(declineMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError == null){
                            mProfileSendRequestButton.setEnabled(true);
                            mCurrentState = NOT_FRIENDS;
                            mProfileSendRequestButton.setText("Add Friend");
                            mProfileSendRequestButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.person_add_white,0,0,0);
                            mProfileSendRequestButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.blue)));

                            mProfileDeclineRequestButton.setVisibility(View.INVISIBLE);
                            mProfileDeclineRequestButton.setEnabled(false);
                        } else {
                            String error = databaseError.getMessage();
                            Toast.makeText(ProfileActivity.this,error,Toast.LENGTH_SHORT).show();
                        }
                        mProfileSendRequestButton.setEnabled(true);
                    }
                });

            }
        });
        //--------------------------------------DECLINE REQUEST COMPLETED ----------------------------------------------------


    }

}
