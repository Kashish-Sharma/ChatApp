package com.app.android.jiitchat.Fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.android.jiitchat.Activities.ChatActivity;
import com.app.android.jiitchat.HelperClasses.Conv;
import com.app.android.jiitchat.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private RecyclerView mConvList;

    private DatabaseReference mConvDatabase;
    private DatabaseReference mChatRef;
    private DatabaseReference mMessageDatabase;
    private DatabaseReference mUsersDatabase;

    private FirebaseAuth mAuth;

    private String mCurrent_user_id;

    private View mMainView;

    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_chat,container,false);
        mConvList = (RecyclerView) mMainView.findViewById(R.id.conv_list);
        mAuth = FirebaseAuth.getInstance();

        mCurrent_user_id = mAuth.getCurrentUser().getUid();

        mConvDatabase = FirebaseDatabase.getInstance().getReference().child("Chat").child(mCurrent_user_id);
        mConvDatabase.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);
        mUsersDatabase.keepSynced(true);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        mConvList.setHasFixedSize(true);
        mConvList.setLayoutManager(linearLayoutManager);

        return mMainView;

    }

    @Override
    public void onStart() {
        super.onStart();

        Query conversationQuery = mConvDatabase.orderByChild("timestamp");

        FirebaseRecyclerOptions<Conv> options =
                new FirebaseRecyclerOptions.Builder<Conv>()
                        .setQuery(conversationQuery, Conv.class)
                        .build();


        FirebaseRecyclerAdapter<Conv,ConvViewHolder> firebaseConvAdapter = new FirebaseRecyclerAdapter<Conv, ConvViewHolder>(options) {
            @Override
            public ConvViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.users_single_layout,parent,false);
                return new ConvViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final ConvViewHolder holder, int position, @NonNull final Conv model) {

                final String list_user_id = getRef(position).getKey();

                Query lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(1);

                lastMessageQuery.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        try {
                            String data = dataSnapshot.child("message").getValue().toString();
                            if (!data.equals("")){
                                if (!model.isSeen()){
                                    holder.statusTextView.setText(data);
                                    holder.statusTextView.setTypeface(holder.statusTextView.getTypeface(),R.font.latobold);
                                    holder.statusTextView.setTextColor(getResources().getColor(R.color.colorPrimaryDark));

                                } else {
                                    holder.statusTextView.setText(data);
                                }
                            } else {
                                if (!model.isSeen()){
                                    holder.statusTextView.setText("image");
                                    holder.statusTextView.setTypeface(holder.statusTextView.getTypeface(),R.font.latobold);
                                    holder.statusTextView.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                                } else {
                                    holder.statusTextView.setText("image");
                                }
                            }
                        } catch (Exception e){

                            Log.e("CHATFRAG_ERROR","SEE THE ERROR"+e.getMessage());

                        }



                        mChatRef = FirebaseDatabase.getInstance().getReference().child("Chat");
                        mChatRef.child(list_user_id).child(mCurrent_user_id).child("seen").setValue(true);
                        mChatRef.child(mCurrent_user_id).child(list_user_id).child("seen").setValue(true);

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

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String userName = dataSnapshot.child("Name").getValue().toString();
                        final String userThumb = dataSnapshot.child("Thumb").getValue().toString();

                        if(dataSnapshot.hasChild("online")) {

                            String userOnlineData = dataSnapshot.child("online").getValue().toString();

                            if (userOnlineData.equals("true")){
                                holder.userOnline.setVisibility(View.VISIBLE);
                            } else {
                                holder.userOnline.setVisibility(View.INVISIBLE);
                            }

                        }

                        holder.nameTextView.setText(userName);

                        Picasso.with(getContext()).load(userThumb)
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.male)
                                .into(holder.mProfileImageView, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {
                                        Picasso.with(getContext()).load(userThumb).placeholder(R.drawable.male).into(holder.mProfileImageView);
                                    }
                                });

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                chatIntent.putExtra("user-id", list_user_id);
                                chatIntent.putExtra("user-name", userName);
                                startActivity(chatIntent);
                            }
                        });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        };

        firebaseConvAdapter.startListening();
        mConvList.setAdapter(firebaseConvAdapter);

    }

    public static class ConvViewHolder extends RecyclerView.ViewHolder {

        View mView;
        TextView statusTextView, nameTextView;
        CircleImageView mProfileImageView;
        ImageView userOnline;

        public ConvViewHolder(View itemView) {
            super(itemView);

            statusTextView = (TextView) itemView.findViewById(R.id.userSingleStatus);
            nameTextView = (TextView) itemView.findViewById(R.id.userSingleName);
            mView = itemView;
            userOnline = (ImageView) itemView.findViewById(R.id.user_single_online_icon);
            mProfileImageView = (CircleImageView) itemView.findViewById(R.id.userSingleImage);

        }
    }
}
