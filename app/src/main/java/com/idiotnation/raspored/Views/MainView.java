package com.idiotnation.raspored.Views;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Vibrator;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.idiotnation.raspored.Contracts.MainContract;
import com.idiotnation.raspored.Dialogs.FiltersDialog;
import com.idiotnation.raspored.Dialogs.SettingsDialog;
import com.idiotnation.raspored.Modules.TableCell;
import com.idiotnation.raspored.Presenters.MainPresenter;
import com.idiotnation.raspored.R;
import com.idiotnation.raspored.Utils;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.idiotnation.raspored.Utils.ERROR_INTERNAL;
import static com.idiotnation.raspored.Utils.ERROR_INTERNET;
import static com.idiotnation.raspored.Utils.ERROR_UNAVAILABLE;
import static com.idiotnation.raspored.Utils.INFO_FINISHED;
import static com.idiotnation.raspored.Utils.INFO_MESSAGE;

public class MainView extends AppCompatActivity implements MainContract.View {

    // Variables

    MyPagerAdapter mAdapter;
    SharedPreferences prefs;
    MainPresenter presenter;
    float pageWidth = 1;

    // Initialization

    @BindView(R.id.easter_egg_bg)
    FrameLayout easterEgg;

    @BindView(R.id.sati)
    RelativeLayout hoursView;

    @BindView(R.id.pager)
    ViewPager mPager;

    @BindView(R.id.dayTabs)
    TabLayout mTabs;

    @BindView(R.id.info_msg)
    TextView infoMessage;

    @BindView(R.id.refresh_layout)
    CustomSwipeToRefresh mRefresh;

    @BindView(R.id.action_bar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        presenter = new MainPresenter();
        presenter.start(this, getApplicationContext());
    }

    @Override
    public void onBackPressed() {
        ActivityCompat.finishAffinity(this);
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    @Override
    public void initialize() {
        prefs = getSharedPreferences("com.idiotnation.raspored", MODE_PRIVATE);
        setColors();
        if (prefs.getBoolean("FirstRun", true) || prefs.getInt("SpinnerDefault", -1) == -1) {
            prefs.edit().putBoolean("FirstRun", false).apply();
            showMessage(View.VISIBLE, INFO_MESSAGE);
        }
        setPagerProperties();
        hoursView.post(new Runnable() {
            @Override
            public void run() {
                presenter.populateHours(hoursView, getApplicationContext());
            }
        });
        setThreadPolicy();
        setActionBarProperties();
        setRefreshLayoutProperties();
        onConfigurationChanged(getResources().getConfiguration());
        checkContent();
    }

    @Override
    public void checkContent() {
        if (new File(getFilesDir() + "/raspored.json").exists()) {
            startAnimation();
            setRaspored(presenter.getRaspored());
        }
        if (prefs.getBoolean("UpdateOnBoot", false)) {
            startAnimation();
            presenter.refresh(prefs.getInt("SpinnerDefault", -1));
        }
    }

    @Override
    public void refreshPages() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
                mRefresh.requestLayout();
            }
        });
    }

    @Override
    public void setRaspored(final List<List<TableCell>> rasporedColumns) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.setColumns(rasporedColumns);
                refreshPages();
                stopAnimation();
            }
        });
    }

    @Override
    public void stopAnimation() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRefresh.post(new Runnable() {
                    @Override
                    public void run() {
                        mRefresh.setRefreshing(false);
                    }
                });
            }
        });
    }

    @Override
    public void startAnimation() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!mRefresh.isRefreshing()) {
                    mRefresh.post(new Runnable() {
                        @Override
                        public void run() {
                            mRefresh.setRefreshing(true);
                        }
                    });
                }
            }
        });
    }

    @Override
    public void showMessage(final int visibility, final int type) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (visibility == View.VISIBLE) {
                    String message = "";
                    switch (type) {
                        case ERROR_INTERNAL:
                            message = getResources().getString(R.string.error_msg_e);
                            break;
                        case ERROR_INTERNET:
                            message = getResources().getString(R.string.error_msg_i);
                            break;
                        case ERROR_UNAVAILABLE:
                            message = getResources().getString(R.string.error_msg_u);
                            break;
                        case INFO_MESSAGE:
                            message = getResources().getString(R.string.info_msg);
                            break;
                        case INFO_FINISHED:
                            message = getResources().getString(R.string.info_end);
                            break;
                    }
                    if (new File(getFilesDir() + "/raspored.json").exists()) {
                        Toast.makeText(MainView.this, message, Toast.LENGTH_SHORT).show();
                        infoMessage.setVisibility(View.INVISIBLE);
                    } else {
                        infoMessage.setText(message);
                        infoMessage.setVisibility(View.VISIBLE);
                    }
                } else {
                    infoMessage.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        for (int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(Utils.getColor(R.color.actionBarTextColorPrimary, getApplicationContext()), PorterDuff.Mode.SRC_ATOP);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ab_settings:
                SettingsDialog settingsDialog = new SettingsDialog(MainView.this);
                settingsDialog.show();
                settingsDialog.setOnFinishListener(new SettingsDialog.onFinishListner() {
                    @Override
                    public void onFinish(int spinnerItem, boolean refreshNotifications) {
                        item.setEnabled(true);
                        if (spinnerItem != prefs.getInt("SpinnerDefault", -1)) {
                            presenter.refresh(spinnerItem);
                        }
                        if (refreshNotifications) {
                            presenter.refreshNotifications();
                        }
                    }
                });
                settingsDialog.setOnEggListener(new SettingsDialog.onEggsterListener() {
                    @Override
                    public void onEgg() {
                        easterEgg = (FrameLayout) findViewById(R.id.easter_egg_bg);
                        easterEgg.setVisibility(View.VISIBLE);
                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        v.vibrate(1000);
                        easterEgg.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                easterEgg.setVisibility(View.GONE);
                            }
                        });
                    }
                });
                return true;
            case R.id.ab_filters:
                FiltersDialog filtersDialog = new FiltersDialog(MainView.this);
                filtersDialog.setOnFinishListener(new FiltersDialog.onFinishListner() {
                    @Override
                    public void onFinish(boolean refreshFilters) {
                        if (refreshFilters) {
                            presenter.refreshFilters();
                        }
                    }
                });
                filtersDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Properties

    public void setPagerProperties() {
        mAdapter = new MyPagerAdapter(getSupportFragmentManager());
        int bgColor = Utils.getColor(R.color.tabsBarBackgroundColor, getApplicationContext()),
                textColor = Utils.getColor(R.color.tabsBarTextColorPrimary, getApplicationContext()),
                indicatorColor = Utils.getColor(R.color.colorAccent, getApplicationContext());
        mTabs.setupWithViewPager(mPager);
        mTabs.setBackgroundColor(bgColor);
        mTabs.setSelectedTabIndicatorColor(indicatorColor);
        mTabs.setTabTextColors(Utils.manipulateColor(textColor, 0.75f), textColor);
        if (mAdapter != null) {
            mPager.setAdapter(mAdapter);
        }
        mPager.setOffscreenPageLimit(6);
        mPager.setCurrentItem(Utils.getPagerActivePage());
    }

    public void setThreadPolicy() {
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    public void setActionBarProperties() {
        mToolbar.setTitleTextColor(Utils.getColor(R.color.actionBarTextColorPrimary, getApplicationContext()));
        mToolbar.setBackgroundColor(Utils.getColor(R.color.actionBarBackgroundColor, getApplicationContext()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Utils.manipulateColor(Utils.getColor(R.color.actionBarBackgroundColor, getApplicationContext()), 0.75f));
        }
        setSupportActionBar(mToolbar);
    }

    public void setRefreshLayoutProperties() {
        mRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (prefs.getInt("SpinnerDefault", -1) != -1) {
                    presenter.refresh(prefs.getInt("SpinnerDefault", -1));
                } else {
                    Toast.makeText(MainView.this, "Odaberite godinu studija", Toast.LENGTH_SHORT).show();
                }
            }
        });
        int[] attribute = new int[]{R.attr.colorAccent, R.attr.dialogBackground};
        TypedArray array = getTheme().obtainStyledAttributes(attribute);
        mRefresh.setColorSchemeColors(array.getColor(0, Color.TRANSPARENT));
        mRefresh.setProgressBackgroundColorSchemeColor(array.getColor(1, Color.TRANSPARENT));
        array.recycle();
    }

    public void setColors() {
        mToolbar.getRootView().setBackgroundColor(Utils.getColor(R.color.windowBackgroundColor, getApplicationContext()));
        infoMessage.setTextColor(Utils.getColor(R.color.textColorPrimary, getApplicationContext()));
        infoMessage.setBackgroundColor(Utils.getColor(R.color.windowBackgroundColor, getApplicationContext()));
    }

    // Inner classes

    public class MyPagerAdapter extends FragmentStatePagerAdapter {

        List<List<TableCell>> columns;
        String[] days = new String[]{"Ponedjeljak", "Utorak", "Srijeda", "Četvrtak", "Petak", "Subota"};

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void setColumns(List<List<TableCell>> columns) {
            this.columns = columns;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int i) {
            ImageFragment fragment = new ImageFragment();
            if (columns != null) {
                fragment.setParams(columns.get(i));
            }
            return fragment;
        }

        @Override
        public float getPageWidth(int position) {
            return pageWidth;
        }

        @Override
        public int getCount() {
            return 6;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return days[position];
        }
    }
}
