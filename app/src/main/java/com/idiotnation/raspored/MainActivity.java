package com.idiotnation.raspored;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.Vibrator;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.idiotnation.raspored.Contracts.MainContract;
import com.idiotnation.raspored.Presenters.MainPresenter;

import java.io.File;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements MainContract.View {


    // Variables

    Bitmap rasporedBitmap;
    MyPagerAdapter mAdapter;

    SharedPreferences prefs;
    String taskResult;
    Handler mHandler;
    List<TableColumn> columns;
    boolean executePost = false, refresh = false;
    int pageCount = 1, currentPageNumber = 1;

    // Initialization

    @Inject
    MainPresenter presenter;

    @BindView(R.id.land_view)
    ImageView landView;

    @BindView(R.id.easter_egg_bg)
    FrameLayout easterEgg;

    @BindView(R.id.sati)
    ImageView hoursView;

    @BindView(R.id.pager)
    ViewPager mPager;

    @BindView(R.id.err_msg)
    TextView errorMessage;

    @BindView(R.id.info_msg)
    TextView infoMessage;

    @BindView(R.id.refresh_layout)
    CustomSwipeToRefresh mRefresh;

    @BindView(R.id.action_bar)
    Toolbar mToolbar;

    @OnClick(R.id.menu_next)
    public void nextRasporedPage() {
        presenter.nextPage(currentPageNumber, pageCount);
    }

    @OnClick(R.id.menu_prev)
    public void previousRasporedPage() {
        presenter.previousPage(currentPageNumber, pageCount);
    }

    // TODO: Error handling ( Internet and first run)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences("com.idiotnation.raspored", MODE_PRIVATE);
        setTheme(prefs.getInt("CurrentTheme", R.style.AppTheme_Light));
        super.onCreate(savedInstanceState);
        ((RasporedApplication) getApplication()).component().inject(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        presenter = new MainPresenter();
        presenter.start(this, getApplicationContext());
    }

    @Override
    public void initialize() {
        setPagerProperties();
        setThreadPolicy();
        setActionBarProperties();
        setRefreshLayoutProperties();
        onConfigurationChanged(getResources().getConfiguration());
        checkContent();
    }

    @Override
    public void checkContent() {
        startAnimation();
        if (new File(getFilesDir().getAbsolutePath() + "/raspored.pdf").exists()) {
            presenter.getRaspored(getApplicationContext(), prefs.getInt("curentPageNumber", 1));
        }
        presenter.refresh(prefs.getInt("SpinnerDefault", -1), prefs.getInt("curentPageNumber", 1));
    }

    @Override
    public void refreshPages() {
        mAdapter.notifyDataSetChanged();
        mRefresh.requestLayout();
    }

    @Override
    public void nextPage(int newPageNumber) {
        prefs.edit().putInt("curentPageNumber", newPageNumber).apply();
        if (pageCount > 1) {
            presenter.getRaspored(getApplicationContext(), newPageNumber);
        }
    }

    @Override
    public void previousPage(int newPageNumber) {
        prefs.edit().putInt("curentPageNumber", newPageNumber).apply();
        if (pageCount > 1) {
            presenter.getRaspored(getApplicationContext(), newPageNumber);
        }
    }

    @Override
    public void setRaspored(Bitmap raspored, int pageCounts, List<TableColumn> rasporedColumns) {
        pageCount = pageCounts;
        rasporedBitmap = raspored;
        columns = rasporedColumns;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hoursView.setImageBitmap(Bitmap.createBitmap(rasporedBitmap, columns.get(0).getX(), columns.get(0).getY(), columns.get(0).getWidth(), columns.get(0).getHeight()));
                landView.setImageBitmap(Bitmap.createBitmap(rasporedBitmap, columns.get(0).getX(), columns.get(0).getY(), columns.get(columns.size() - 1).getX() - columns.get(0).getX() + columns.get(columns.size() - 1).getWidth(), columns.get(columns.size() - 1).getY() - columns.get(0).getY() + columns.get(columns.size() - 1).getHeight()));
                refreshPages();
            }
        });
    }

    @Override
    public void stopAnimation(int visibility) {
        errorMessage.setVisibility(visibility);
        mRefresh.post(new Runnable() {
            @Override
            public void run() {
                mRefresh.setRefreshing(false);
            }
        });
    }

    @Override
    public void startAnimation() {
        if (!mRefresh.isRefreshing()) {
            mRefresh.post(new Runnable() {
                @Override
                public void run() {
                    mRefresh.setRefreshing(true);
                }
            });
        }
    }

    @Override
    public void update(String date, String id) {
        prefs.edit().putString("UpdateDate", date).apply();
        prefs.edit().putString("CurrentRasporedId", id).apply();
        mToolbar.setTitle(date);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            landView.setVisibility(View.VISIBLE);
            mPager.setVisibility(View.GONE);
            mPager.setClickable(false);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            landView.setVisibility(View.GONE);
            mPager.setVisibility(View.VISIBLE);
            mPager.setClickable(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ab_settings:
                item.setEnabled(false);
                SettingsDialog settingsDialog = new SettingsDialog(MainActivity.this);
                settingsDialog.show();
                settingsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        item.setEnabled(true);
                        if (refresh) {
                            presenter.refresh(prefs.getInt("SpinnerDefault", -1), prefs.getInt("curentPageNumber", 1));
                        }
                    }
                });
                settingsDialog.setOnFinishListener(new SettingsDialog.onFinishListner() {
                    @Override
                    public void onFinish(int spinnerItem) {
                        if(spinnerItem!=prefs.getInt("SpinnerDefault", -1)){
                            refresh = true;
                            prefs.edit().putInt("SpinnerDefault", spinnerItem).apply();
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Properties

    public void setPagerProperties() {
        mAdapter = new MyPagerAdapter(getSupportFragmentManager());
        if (mAdapter != null) {
            mPager.setAdapter(mAdapter);
        }
        mPager.setOffscreenPageLimit(6);
        setPagerActivePage();
    }

    public void setPagerActivePage() {
        int day = Calendar.getInstance(TimeZone.getTimeZone("Europe/Sarajevo")).get(Calendar.DAY_OF_WEEK) - 2;
        if (day == -1) {
            day = 5;
        }
        if (day == -2) {
            day = 6;
        }
        mPager.setCurrentItem(day);
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
        mToolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        mToolbar.setTitle(prefs.getString("UpdateDate", "Raspored"));
        setSupportActionBar(mToolbar);
    }

    public void setRefreshLayoutProperties() {
        mRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (prefs.getInt("SpinnerDefault", -1) != -1) {
                    presenter.refresh(prefs.getInt("SpinnerDefault", -1), prefs.getInt("curentPageNumber", 1));
                } else {
                    Toast.makeText(MainActivity.this, "Odaberite godinu studija", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mRefresh.setColorScheme(R.color.blue, R.color.green, R.color.red, R.color.orange);
    }

    // Inner classes

    public class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int i) {
            ImageFragment fragment = new ImageFragment();
            fragment.setParams(i, rasporedBitmap, columns);
            return fragment;
        }

        @Override
        public int getCount() {
            return 6;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "DAY- " + (position + 1);
        }
    }
}
