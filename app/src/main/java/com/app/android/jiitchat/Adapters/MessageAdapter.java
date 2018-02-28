package com.app.android.jiitchat.Adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.android.jiitchat.Activities.SettingsActivity;
import com.app.android.jiitchat.HelperClasses.GetTimeAgo;
import com.app.android.jiitchat.HelperClasses.Messages;
import com.app.android.jiitchat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Kashish on 24-02-2018.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;
    Context context;

    public MessageAdapter(List<Messages> mMessageList){
        this.mMessageList = mMessageList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout,parent,false);

        return  new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder holder, int position) {

        mAuth = FirebaseAuth.getInstance();
        String current_user_id = mAuth.getCurrentUser().getUid();

        final Messages messages = mMessageList.get(position);

        String from_user = messages.getFrom();
        String message_type = messages.getType();

        GetTimeAgo getTimeAgoObject = new GetTimeAgo();

        if (from_user.equals(current_user_id)){

            holder.leftCard.setVisibility(View.GONE);
            holder.mMessageTextLeft.setVisibility(View.GONE);
            holder.mMessageImageLeft.setVisibility(View.GONE);
            holder.mMessageTimeLeft.setVisibility(View.GONE);

            holder.mMessageTextRight.setVisibility(View.VISIBLE);
            holder.mMessageImageRight.setVisibility(View.VISIBLE);
            holder.mMessageTimeRight.setVisibility(View.VISIBLE);
            holder.rightCard.setVisibility(View.VISIBLE);




            if (message_type.equals("text")){
                long lastTime = messages.getTime();
                String lastSeenTime = getTimeAgoObject.getTimeAgo(lastTime,holder.mMessageTimeRight.getContext());
                holder.mMessageTimeRight.setText(lastSeenTime);
                holder.mMessageTextRight.setText(messages.getMessage());
                holder.rightCard.setVisibility(View.GONE);
                holder.mMessageImageRight.setVisibility(View.GONE);
            } else if (message_type.equals("image")){

                holder.mMessageTextRight.setVisibility(View.GONE);
                holder.mMessageImageLeft.getResources().getColor(R.color.colorPrimary);
                Picasso.with(holder.mMessageImageRight.getContext()).load(messages.getThumb())
                        .fit().centerCrop()
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.color.colorPrimary).into(holder.mMessageImageRight, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(holder.mMessageImageRight.getContext()).load(messages.getThumb())
                                .fit().centerCrop()
                                .placeholder(R.color.colorPrimary).into(holder.mMessageImageRight);
                    }
                });
            }

        } else {

            holder.leftCard.setVisibility(View.VISIBLE);
            holder.mMessageTextLeft.setVisibility(View.VISIBLE);
            holder.mMessageImageLeft.setVisibility(View.VISIBLE);
            holder.mMessageTimeLeft.setVisibility(View.VISIBLE);

            holder.rightCard.setVisibility(View.GONE);
            holder.mMessageTextRight.setVisibility(View.GONE);
            holder.mMessageImageRight.setVisibility(View.GONE);
            holder.mMessageTimeRight.setVisibility(View.GONE);



            if (message_type.equals("text")){
                long lastTime = messages.getTime();
                String lastSeenTime = getTimeAgoObject.getTimeAgo(lastTime,holder.mMessageTimeLeft.getContext());
                holder.mMessageTimeLeft.setText(lastSeenTime);
                holder.mMessageTextLeft.setText(messages.getMessage());
                holder.leftCard.setVisibility(View.GONE);
                holder.mMessageImageLeft.setVisibility(View.GONE);
            } else if (message_type.equals("image")){

                holder.mMessageTextLeft.setVisibility(View.GONE);
                Picasso.with(holder.mMessageImageLeft.getContext()).load(messages.getThumb())
                        .fit().centerCrop()
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.color.colorPrimary).into(holder.mMessageImageLeft, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(holder.mMessageImageLeft.getContext()).load(messages.getThumb())
                                .fit().centerCrop()
                                .placeholder(R.color.colorPrimary).into(holder.mMessageImageLeft);
                    }
                });
            }



        }

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView mMessageTextLeft,mMessageTextRight;
        public CardView rightCard, leftCard;
        public TextView mMessageTimeLeft,mMessageTimeRight;
        public ImageView mMessageImageLeft,mMessageImageRight;

        public MessageViewHolder(View itemView) {
            super(itemView);

            rightCard = (CardView) itemView.findViewById(R.id.rightCard);
            leftCard = (CardView) itemView.findViewById(R.id.leftCard);
            mMessageImageRight = (ImageView) itemView.findViewById(R.id.message_image_right);
            mMessageImageLeft = (ImageView) itemView.findViewById(R.id.message_image_left);
            mMessageTextLeft = (TextView) itemView.findViewById(R.id.message_text_left);
            mMessageTextRight = (TextView) itemView.findViewById(R.id.message_text_right);
            mMessageTimeLeft = (TextView) itemView.findViewById(R.id.message_time_text_left);
            mMessageTimeRight = (TextView) itemView.findViewById(R.id.message_time_text_right);


        }
    }


}
