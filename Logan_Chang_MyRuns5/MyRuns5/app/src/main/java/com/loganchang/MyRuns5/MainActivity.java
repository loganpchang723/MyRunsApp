package com.loganchang.MyRuns5;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    //tab labels
    public static final String START_TAB_LABEL = "START";
    public static final String HISTORY_TAB_LABEL = "HISTORY";
    public static final String SETTINGS_TAB_LABEL = "SETTINGS";

    //fragments
    private StartFragment mStartFragment;
    private HistoryFragment mHistoryFragment;
    private SettingsFragment mSettingsFragment;

    //widgets
    private ViewPager2 mViewPager;
    private TabLayout mTabLayout;
    private TabsViewPageAdapter mTabsViewPageAdapter;
    private ArrayList<Fragment> fragments;
    public static ExerciseEntryDbHelper DBhelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //save widget instances
        mViewPager = (ViewPager2) findViewById(R.id.view_pager);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);

        //instantiate fragments and put them in ArrayList
        mStartFragment = new StartFragment();
        mHistoryFragment = new HistoryFragment();
        mSettingsFragment = new SettingsFragment();
        fragments = new ArrayList<>();
        fragments.add(mStartFragment);
        fragments.add(mHistoryFragment);
        fragments.add(mSettingsFragment);

        //bind ArrayList to adapter and TabView
        mTabsViewPageAdapter = new TabsViewPageAdapter(this, fragments);
        mViewPager.setAdapter(mTabsViewPageAdapter);

        //create tab labels
        TabLayoutMediator.TabConfigurationStrategy tabConfigurationStrategy = new
                TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(TabLayout.Tab tab, int position) {
                        if(position == 0) tab.setText(START_TAB_LABEL);
                        else if(position == 1) tab.setText(HISTORY_TAB_LABEL);
                        else if(position == 2) tab.setText(SETTINGS_TAB_LABEL);
                    }
                };
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(mTabLayout, mViewPager, tabConfigurationStrategy);
        tabLayoutMediator.attach();

        //instantiate the db helper
//        if(savedInstanceState == null){
//            DBhelper = new ExerciseEntryDbHelper(this);
//        }
//        Log.d("DBHelper", "is db helper null: "+(DBhelper == null));
    }


//    /**
//     * Close the DBHelper when activity is paused
//     */
//    @Override
//    protected void onPause() {
//        if(DBhelper != null) DBhelper.close();
//        super.onPause();
//    }
//
//    /**
//     * Reopen the DB helper
//     */
//    public void onResume(){
//        if(DBhelper == null) DBhelper = new ExerciseEntryDbHelper(this);
//        super.onResume();
//    }
}