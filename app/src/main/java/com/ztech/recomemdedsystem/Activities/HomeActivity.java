package com.ztech.recomemdedsystem.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ztech.recomemdedsystem.Fragments.AddLocationFragment;
import com.ztech.recomemdedsystem.Fragments.MapsFragment;
import com.ztech.recomemdedsystem.R;
import com.ztech.recomemdedsystem.databinding.ActivityHomeBinding;
import com.ztech.recomemdedsystem.databinding.ActivityLoginBinding;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    ActivityHomeBinding binding;

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    Toolbar toolbar;
    NavigationView sideNavigationView;
    LinearLayout profile;

    static HomeActivity instance;

    public static String POSITION = "POSITION";
    //Header
    CircleImageView headerProfileImage;
    TextView headerUsername, headerEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(
                getSupportFragmentManager(), FragmentPagerItems.with(this)
                .add("Map", MapsFragment.class)
                .add("Add Location", AddLocationFragment.class)
                .add("Fill Form", AddLocationFragment.class)
                .add("Quality Form", AddLocationFragment.class)
                .create());

        ViewPager viewPager = (ViewPager) findViewById(R.id.tabPager);
        viewPager.setAdapter(adapter);

        SmartTabLayout viewPagerTab = (SmartTabLayout) findViewById(R.id.viewpapertab);
        viewPagerTab.setViewPager(viewPager);

        sideNavigationView = findViewById(R.id.nav_bar);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawable_layout);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        toggle.syncState();
        sideNavigationView.setNavigationItemSelectedListener(this);
        View header = sideNavigationView.getHeaderView(0);
        profile = header.findViewById(R.id.profile);
        profile.setOnClickListener(this);

        headerUsername = sideNavigationView.getHeaderView(0).findViewById(R.id.header_profilename);
        headerEmail = sideNavigationView.getHeaderView(0).findViewById(R.id.header_profilemail);
        headerProfileImage = sideNavigationView.getHeaderView(0).findViewById(R.id.header_profile_image);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        super.onOptionsItemSelected(item);

        int selected_option = item.getItemId();

        switch (selected_option)
        {


        }
        return true;
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }
}