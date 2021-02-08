package com.ztech.recomemdedsystem.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.github.ybq.android.spinkit.style.Wave;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.ztech.recomemdedsystem.R;
import com.ztech.recomemdedsystem.Utils.CommonFunctions;
import com.ztech.recomemdedsystem.databinding.ActivityLoginBinding;

import es.dmoral.toasty.Toasty;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    FirebaseAuth firebaseAuth;
    DatabaseReference rootRef;
    DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        setListener();
    }

    private void init() {
        firebaseAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        Wave wave = new Wave();
        binding.LoadingBar.setIndeterminateDrawable(wave);
        binding.LoadingBar.setVisibility(View.INVISIBLE);
    }

    private void setListener() {
        binding.UserSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckField();
            }
        });

        binding.UserLoginGoToSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void CheckField() {
        try {
            final String email = binding.UserLoginEmail.getText().toString();
            final String password = binding.UserLoginPassword.getText().toString();

            if (TextUtils.isEmpty(email)) {
                CommonFunctions.setError(binding.UserLoginEmail, "Email Required");

            } else if (TextUtils.isEmpty(password)) {
                CommonFunctions.setError(binding.UserLoginPassword, "Password Required");
            } else {
                if (CommonFunctions.isNetworkAvailable(this)) {
                    AllowUserToLogin(email, password);
                } else {
                    CommonFunctions.showShortToastInfo(this, "Check Your Internet ! Make Sure Your are Connected to Internet ");
                }


            }
        } catch (Exception e) {
            Toast.makeText(LoginActivity.this, "Try Again, Something wrong occur while Logging in.", Toast.LENGTH_SHORT).show();
        }
    }

    private void AllowUserToLogin(final String email, final String password) {

        binding.LoadingBar.setVisibility(View.VISIBLE);
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    final String currentUserId = firebaseAuth.getCurrentUser().getUid();
                    FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {

                            if (task.isSuccessful()) {
                                String devicetoken = task.getResult().getToken();
                                userRef.child(currentUserId).child("DeviceToken").setValue(devicetoken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {
                                            binding.LoadingBar.setVisibility(View.INVISIBLE);
                                            Intent home_intent = new Intent(LoginActivity.this, HomeActivity.class);
                                            home_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(home_intent);
                                            finish();
                                            Toasty.success(LoginActivity.this, "Login Successfully", Toasty.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(LoginActivity.this, "Try Again, Something wrong occur while Logging in.", Toast.LENGTH_SHORT).show();
                                binding.LoadingBar.setVisibility(View.INVISIBLE);
                            }

                        }
                    });

                } else {
                    String error_message = task.getException().toString();
                    Toast.makeText(LoginActivity.this, "Try Again, Something wrong occur while Logging in.", Toast.LENGTH_SHORT).show();
                    binding.LoadingBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }
}