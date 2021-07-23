/*
 *
 * 	StreamWIDE (Team on The Run)
 *
 * @createdBy  AndroidTeam on ven., 10 avr. 2020 11:06:25 +0100
 * @copyright  Copyright (c) 2020 StreamWIDE UK Ltd (Team on the Run)
 * @email      support@teamontherun.com
 *
 * 	© Copyright 2020 StreamWIDE UK Ltd (Team on the Run). StreamWIDE is the copyright holder
 * 	of all code contained in this file. Do not redistribute or
 *  	re-use without permission.
 *
 * @lastModifiedOn ven., 10 avr. 2020 11:06:17 +0100
 */

package com.stw.protorype.ui.activity.mybusiness;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.stw.protorype.R;


public class TemplateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template);
        setTitle("Templates");

        ViewPager viewPager = findViewById(R.id.activity_template_viewpager);
        TabLayout tabLayout = findViewById(R.id.activity_template_tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setAdapter(new PageAdapter(getSupportFragmentManager()) {});
    }

    public class PageAdapter extends FragmentPagerAdapter {

        PageAdapter(FragmentManager mgr) {
            super(mgr,FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public int getCount() {
            return(3);
        }

        @Override
        @NonNull
        public Fragment getItem(int position) {
            switch (position){
                case 2 :
                    return new FavoritesFragment();
                case 1 :
                    return new RecentFragment();
                case 0 :default :
                    return new TemplateListFragment();
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position){
                case 2 :
                    return "Favorites";
                case 1 :
                    return "Recent";
                case 0 :default :
                    return "Template";
            }
        }

    }

}
