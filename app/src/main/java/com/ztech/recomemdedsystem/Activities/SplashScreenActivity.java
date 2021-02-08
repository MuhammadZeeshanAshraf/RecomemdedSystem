package com.ztech.recomemdedsystem.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ztech.recomemdedsystem.OnBoarding.OnBoardingActivity;
import com.ztech.recomemdedsystem.R;
import com.ztech.recomemdedsystem.databinding.ActivitySplashScreenBinding;

public class SplashScreenActivity extends AppCompatActivity {

    ActivitySplashScreenBinding binding;
    Animation sideAnim, bottomAnim, anime;
    SharedPreferences onBoardingScreen;
    private static int SPLASH_TIMER = 5000;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        //Animations
        sideAnim = AnimationUtils.loadAnimation(this, R.anim.side_anim);
        bottomAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_anim);
        sideAnim.setDuration(500);
        bottomAnim.setDuration(500);

        moveIcon(binding.animationView);
        binding.appName.setAnimation(sideAnim);
        binding.description.setAnimation(bottomAnim);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                onBoardingScreen = getSharedPreferences("onBoardingScreen", MODE_PRIVATE);
                boolean isFirstTime = onBoardingScreen.getBoolean("firstTime" , true);

                if(isFirstTime)
                {
                    SharedPreferences.Editor editor = onBoardingScreen.edit();
                    editor.putBoolean("firstTime", false);
                    editor.commit();

                    Intent intent = new Intent(getApplicationContext() , OnBoardingActivity.class);
                    startActivity(intent);
                    finish();
                }else
                {
                    if(firebaseUser != null)
                    {
                        Intent intent = new Intent(getApplicationContext() , HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }else
                    {
                        Intent intent = new Intent(getApplicationContext() , LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }

                }

            }
        },SPLASH_TIMER);



    }


    private void moveIcon(View view) {
        int[] originalPos = new int[2];
        view.getLocationOnScreen(originalPos);
        anime = new TranslateAnimation(0, 0, 0, originalPos[1] + 100);
        anime.setDuration(1000);
        anime.setFillAfter(true);
        view.startAnimation(anime);
    }

}