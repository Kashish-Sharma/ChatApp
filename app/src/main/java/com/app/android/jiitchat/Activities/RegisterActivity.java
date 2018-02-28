package com.app.android.jiitchat.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.app.android.jiitchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "REGISTER_ACTIVITY";

    //registration-fields
    private TextInputLayout mDisplayName;
    private TextInputLayout mDisplayEmail;
    private TextInputLayout mPassword;
    private Button mRegisterButton;

    //toolbar
    private Toolbar mToolbar;

    //progressDialog
    private ProgressDialog mRegProgress;

    //firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.RegisterTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mToolbar = (Toolbar) findViewById(R.id.registerPageToolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mRegProgress = new ProgressDialog(this);

        //firebase
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();


        //registration-fields
        mDisplayName = (TextInputLayout) findViewById(R.id.register_display_name);
        mDisplayEmail = (TextInputLayout) findViewById(R.id.register_email);
        mPassword = (TextInputLayout) findViewById(R.id.register_password);
        mRegisterButton = (Button) findViewById(R.id.register_create_account_button);

        mRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String displayName = mDisplayName.getEditText().getText().toString();
                String displayEmail = mDisplayEmail.getEditText().getText().toString();
                String password = mPassword.getEditText().getText().toString();
                if(displayName.equals("") || displayEmail.equals("") || password.equals(""))
                {
                    Toast.makeText(RegisterActivity.this,"Please fill up all the fields",Toast.LENGTH_SHORT).show();
                } else
                {
                    mRegProgress.setTitle("Registering User");
                    mRegProgress.setMessage("Please Wait while we setup your account");
                    mRegProgress.setCanceledOnTouchOutside(false);
                    mRegProgress.show();
                    registerUser(displayName,displayEmail,password);
                }
            }
        });

    }

    private void registerUser(final String displayName, String email, String password){

        //final String maleImageUrl = "https://firebasestorage.googleapis.com/v0/b/chatapp-b749d.appspot.com/o/male.png?alt=media&token=622916b6-5397-4606-b323-ecdf5eed8a2f";
        //final String femaleImageUrl = "https://firebasestorage.googleapis.com/v0/b/chatapp-b749d.appspot.com/o/female.png?alt=media&token=3bdaffcf-2c73-4b1c-ad09-d273411dad0d";

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                            String uid = currentUser.getUid();

                            mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                            String device_token = FirebaseInstanceId.getInstance().getToken();

                            HashMap<String,String> userMap = new HashMap<>();
                            userMap.put("Name",displayName);
                            userMap.put("Status","Hi there, I'm using myApp.");
                            userMap.put("Image","default");
                            userMap.put("Thumb","default");
                            userMap.put("device_token",device_token);

                            mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        mRegProgress.dismiss();
                                        Log.d(TAG, "createUserWithEmail:success");
                                        Intent gotoChatPage = new Intent(RegisterActivity.this,MainActivity.class);
                                        gotoChatPage.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(gotoChatPage);
                                        finish();
                                    }
                                }
                            });

                        } else {
                            mRegProgress.hide();
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed, Please try Again.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });

    }

}
