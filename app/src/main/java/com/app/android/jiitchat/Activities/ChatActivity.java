package com.app.android.jiitchat.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.android.jiitchat.Adapters.MessageAdapter;
import com.app.android.jiitchat.HelperClasses.GetTimeAgo;
import com.app.android.jiitchat.HelperClasses.Messages;
import com.app.android.jiitchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;


public class ChatActivity extends AppCompatActivity {

    private String mChatUser;
    private String userName;
    private Toolbar mChatToolbar;
    private FirebaseAuth mAuth;


    private TextView mTitleView, mLastSeenView;
    private CircleImageView mProfileImage;

    private String mCurrentUserId;

    private static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    private static final int GALLERY_PICK = 77;

    private DatabaseReference mRootRef;
    private StorageReference mImageStorage;


    private ImageButton mChatAddBtn;
    private ImageButton mChatSendButton;
    private EditText mChatMessageView;

    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mRefreshLayout;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;

    private int itemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";
    private String mFirstKey = "";
    private static final int TOTAL_ITEMS_TO_LOAD = 20;
    private int mCurrentPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat2);

        mChatToolbar = (Toolbar) findViewById(R.id.chat_app_bar);
        setSupportActionBar(mChatToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mRootRef = FirebaseDatabase.getInstance().getReference();
        mRootRef.keepSynced(true);

        mChatUser = getIntent().getStringExtra("user-id");
        userName = getIntent().getStringExtra("user-name");

        mCurrentUserId = mAuth.getCurrentUser().getUid();

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar,null);

        actionBar.setCustomView(action_bar_view);

        // Custom actionbar items
        mTitleView = (TextView) findViewById(R.id.name_custom_bar);
        mLastSeenView = (TextView) findViewById(R.id.last_seen_custom_bar);
        mProfileImage = (CircleImageView) findViewById(R.id.custom_bar_image);
        mTitleView.setText(userName);


        mRootRef.child("Users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String online = dataSnapshot.child("online").getValue().toString();
                final String image = dataSnapshot.child("Image").getValue().toString();

                Picasso.with(ChatActivity.this).load(image)
                        .placeholder(R.drawable.male)
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(mProfileImage, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {
                                Picasso.with(ChatActivity.this).load(image)
                                        .placeholder(R.drawable.male).into(mProfileImage);
                            }
                        });

                if (online.equals("true")){
                    mLastSeenView.setText("Online");
                } else {

                    GetTimeAgo getTimeAgoObj = new GetTimeAgo();
                    long lastTime = Long.parseLong(online);

                    String lastSennTime = getTimeAgoObj.getTimeAgo(lastTime,getApplicationContext());

                    mLastSeenView.setText(lastSennTime);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chat").child(mCurrentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.hasChild(mChatUser)){

                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("timestamp",ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUserId + "/" + mChatUser,chatAddMap);
                    chatUserMap.put("Chat/" + mChatUser + "/" + mCurrentUserId,chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError!=null){
                                Log.i("CHAT_LOG",databaseError.getMessage().toString());
                            }

                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mChatAddBtn = (ImageButton) findViewById(R.id.photoPickerButton);
        mChatSendButton = (ImageButton) findViewById(R.id.sendButton);
        mChatMessageView = (EditText) findViewById(R.id.messageEditText);

        mAdapter = new MessageAdapter(messagesList);
        mMessagesList = (RecyclerView) findViewById(R.id.messages_list);
        mLinearLayout = new LinearLayoutManager(this);
        mMessagesList.setHasFixedSize(true);
        mRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.messageSwipeLayout);
        //mLinearLayout.setStackFromEnd(true);
        mMessagesList.setLayoutManager(mLinearLayout);
        mMessagesList.setAdapter(mAdapter);

        loadMessages();

        mImageStorage = FirebaseStorage.getInstance().getReference();

        mMessagesList.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                if (i3 < i7){
                    mMessagesList.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (messagesList.size() > 0){
                                mMessagesList.scrollToPosition(messagesList.size()-1);
                            }
                        }
                    },0);
                }
            }
        });

        mChatAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

                startActivityForResult(Intent.createChooser(galleryIntent,"Select Image"),GALLERY_PICK);
            }
        });


        mChatMessageView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});
        mChatMessageView.setInputType(EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE);
        mChatMessageView.setSingleLine(false);

        mChatSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mCurrentPage++;
                itemPos = 0;
                loadMoreMessages();

            }
        });



    }

    private void loadMoreMessages(){

        Log.i("TEST_KEYS","FIRST KEY LOAD MESSAGES : "+mFirstKey + "  LAST KEY LOAD MESSAGES : "+ mLastKey);
        if (mLastKey.equals(mFirstKey)){
            mCurrentPage = -1;

            mRefreshLayout.setRefreshing(false);
            return;

        }

        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(mCurrentPage*TOTAL_ITEMS_TO_LOAD);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages messages = dataSnapshot.getValue(Messages.class);
                String messageKey = dataSnapshot.getKey();

                if (!mPrevKey.equals(messageKey)){
                    messagesList.add(itemPos++,messages);
                } else {
                    mPrevKey = mLastKey;
                }

                if (itemPos == 1){

                    mLastKey = messageKey;
                    Log.i("KEY","MLASTKEY " + mLastKey);

                }


                mAdapter.notifyDataSetChanged();

                mRefreshLayout.setRefreshing(false);

                mLinearLayout.scrollToPositionWithOffset(mCurrentPage*TOTAL_ITEMS_TO_LOAD,0);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadMessages() {


        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

        Query getFirstQuery = messageRef.limitToFirst(1);
        getFirstQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                mFirstKey = dataSnapshot.getKey();
                Log.i("KEYS","MFIRSTKEY " +mFirstKey);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        Query messageQuery = messageRef.limitToLast(mCurrentPage*TOTAL_ITEMS_TO_LOAD);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Messages messages = dataSnapshot.getValue(Messages.class);
                Log.i("KEYS",dataSnapshot.getKey());

                itemPos++;

                if (itemPos == 1){

                    String messageKey = dataSnapshot.getKey();

                    mLastKey = messageKey;
                    mPrevKey = messageKey;

                }

                messagesList.add(messages);
                mAdapter.notifyDataSetChanged();

                mMessagesList.scrollToPosition(messagesList.size()-1);

                mRefreshLayout.setRefreshing(false);


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            Log.i("IMAGE-LOG", "REQUEST OK");

            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                Uri resultUri = result.getUri();

                File thumb_filePath = new File(resultUri.getPath());

                final String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
                final String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

                DatabaseReference user_message_push = mRootRef.child("messages")
                        .child(mCurrentUserId).child(mChatUser).push();

                final String push_id = user_message_push.getKey();

                StorageReference filepath = mImageStorage.child("message_images").child("main_images").child(push_id + ".jpg");
                final StorageReference thumb_file = mImageStorage.child("message_images").child("thumbnails").child(push_id + ".jpg");

                Log.i("IMAGE-LOG", "STORAGE OK , THUMB STORAGE OK");
                Log.i("IMAGE-LOG", thumb_filePath.toString());

                try {
                    Bitmap thumb_bitmap = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(60)
                            .compressToBitmap(thumb_filePath);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
                    final byte[] thumb_byte = baos.toByteArray();

                    Log.i("IMAGE-LOG", "BYTE[] OK");

                    thumb_file.putBytes(thumb_byte).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                            Log.i("IMAGE-LOG", "LOADING THUMB");

                            String thumbDownloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                            if (thumb_task.isSuccessful()) {

                                Map messageMap = new HashMap();
                                messageMap.put("message", "");
                                messageMap.put("seen", false);
                                messageMap.put("type", "image");
                                messageMap.put("time", ServerValue.TIMESTAMP);
                                messageMap.put("from", mCurrentUserId);
                                messageMap.put("image", "");
                                messageMap.put("thumb", thumbDownloadUrl);

                                Map messageUserMap = new HashMap();
                                messageUserMap.put(current_user_ref + "/" + push_id, messageMap);
                                messageUserMap.put(chat_user_ref + "/" + push_id, messageMap);

                                mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if (databaseError != null) {
                                            Log.d("CHAT_LOG", databaseError.getMessage().toString());
                                        } else {
                                            Log.i("IMAGE-LOG", "SENT");
                                        }
                                    }
                                });

                            } else {
                                Toast.makeText(ChatActivity.this, "Error in uploading thumbnail.", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

                    filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                Log.i("IMAGE-LOG", "TASK OK");
                                final String downloadUrl = task.getResult().getDownloadUrl().toString();

                                mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).child(push_id).child("image").setValue(downloadUrl);
                                mRootRef.child("messages").child(mChatUser).child(mCurrentUserId).child(push_id).child("image").setValue(downloadUrl);

                            } else {
                                Toast.makeText(ChatActivity.this, "Error in uploading.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

        }

    }

    private void sendMessage(){
        String message = mChatMessageView.getText().toString();

        if (!TextUtils.isEmpty(message)){

            String current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
            String chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId;

            DatabaseReference userMessagePush = mRootRef.child("messages")
                    .child(mCurrentUserId).child(mChatUser).push();
            String pushId = userMessagePush.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message",message);
            messageMap.put("seen",false);
            messageMap.put("type","text");
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",mCurrentUserId);
            messageMap.put("image","");
            messageMap.put("thumb","");

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + pushId,messageMap);
            messageUserMap.put(chat_user_ref + "/" + pushId,messageMap);

            mChatMessageView.setText("");

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError!=null){
                        Log.d("CHAT_ LOG",databaseError.getMessage().toString());
                    }
                }
            });

            Log.i("TEST_KEYS","FIRST KEY : "+mFirstKey + "  LAST KEY : "+ mLastKey);

        }
    }

}
