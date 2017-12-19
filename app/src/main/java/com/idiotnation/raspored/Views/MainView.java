package com.idiotnation.raspored.Views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.idiotnation.raspored.Contracts.MainContract;
import com.idiotnation.raspored.Dialogs.InfoDialog;
import com.idiotnation.raspored.Dialogs.SettingsDialog;
import com.idiotnation.raspored.Helpers.CustomSwipeToRefresh;
import com.idiotnation.raspored.Models.LessonCell;
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
    ViewPagerAdapter mAdapter;
    SharedPreferences prefs;
    MainPresenter presenter;
    boolean themeChanged = false;

    // Initialization
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 69) {
            recreate();
        }
    }

    @Override
    public void initialize() {
        prefs = getSharedPreferences("com.idiotnation.raspored", MODE_PRIVATE);
        themeChanged = getIntent().getBooleanExtra("ThemeChanged", false);
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
        if (themeChanged) {
            showSettingsDialog();
        }
        presenter.initNotificationChannel();
    }

    @Override
    public void checkContent() {
        if (new File(getFilesDir() + "/raspored.json").exists()) {
            startAnimation();
            setRaspored(presenter.getRaspored());
        }
        if ((prefs.getBoolean("UpdateOnBoot", false) || getIntent().getAction() == "com.idiotnation.raspored.action.Refresh") && !themeChanged) {
            startAnimation();
            presenter.refresh(prefs.getInt("SpinnerDefault", -1));
        }
    }

    @Override
    public void refreshPages() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPager.setCurrentItem(presenter.getPageNumber());
                mAdapter.notifyDataSetChanged();
                mRefresh.requestLayout();
            }
        });
    }

    @Override
    public void setRaspored(final List<List<LessonCell>> rasporedColumns) {
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
    public SharedPreferences getPreferences() {
        return prefs;
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
                item.setEnabled(true);
                showSettingsDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Properties

    public void showSettingsDialog() {
        SettingsDialog settingsDialog = new SettingsDialog(MainView.this);
        settingsDialog.setListeners(new SettingsDialog.Listners() {
            @Override
            public void onFinish(int spinnerItem) {
                if (spinnerItem != prefs.getInt("SpinnerDefault", -1)) {
                    presenter.refresh(spinnerItem);
                }
            }

        });
        settingsDialog.show();
    }

    public void setPagerProperties() {
        mAdapter = new ViewPagerAdapter();
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
        mPager.setOffscreenPageLimit(1);
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
                    stopAnimation();
                }
            }
        });
        int[] attribute = new int[]{R.attr.colorAccent, R.attr.dialogBackground};
        TypedArray array = getTheme().obtainStyledAttributes(attribute);
        mRefresh.setColorSchemeColors(Utils.getColor(R.color.colorAccent, getApplicationContext()));
        mRefresh.setProgressBackgroundColorSchemeColor(Utils.getColor(R.color.windowBackgroundColor, getApplicationContext()));
        array.recycle();
    }

    public void setColors() {
        mToolbar.getRootView().setBackgroundColor(Utils.getColor(R.color.windowBackgroundColor, getApplicationContext()));
        infoMessage.setTextColor(Utils.getColor(R.color.textColorPrimary, getApplicationContext()));
        infoMessage.setBackgroundColor(Utils.getColor(R.color.windowBackgroundColor, getApplicationContext()));
    }

    // Inner classes

    public class ViewPagerAdapter extends PagerAdapter {

        List<List<LessonCell>> allColumns;
        String[] days = new String[]{"Ponedjeljak", "Utorak", "Srijeda", "Četvrtak", "Petak", "Subota"};

        ViewPagerAdapter() {
        }

        void setColumns(List<List<LessonCell>> columns) {
            this.allColumns = columns;
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        public Object instantiateItem(ViewGroup collection, int position) {
            final FrameLayout rootView = new FrameLayout(getApplicationContext());
            rootView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            if (allColumns != null) {
                final List<LessonCell> columns = allColumns.get(position);
                if (columns != null) {
                    rootView.post(new Runnable() {
                        @Override
                        public void run() {
                            float densityScale = getResources().getDisplayMetrics().density;
                            int bgColor = Utils.getColor(R.color.lessonsBackgroundColor, getApplicationContext()),
                                    textColor = Utils.getColor(R.color.lessonsTextColorPrimary, getApplicationContext()),
                                    strokeColor = Utils.getColor(R.color.lessonsBackgroundStrokeColor, getApplicationContext());
                            for (int i = 0; i < columns.size(); i++) {
                                int width = columns.get(i).getWidth();
                                float height = columns.get(i).getHeight();
                                TextView textView = new TextView(getApplicationContext());
                                textView.setGravity(Gravity.CENTER);
                                textView.setTypeface(Typeface.DEFAULT_BOLD);
                                textView.setTextColor(textColor);
                                textView.setMaxLines((int) height);
                                textView.setEllipsize(TextUtils.TruncateAt.END);
                                int padding = columns.get(i).getColCount() > 1 ? (int) (rootView.getWidth() * 0.01f) : (int) (rootView.getWidth() * 0.05f);
                                float paddingScale = height > 1 ? 1 : ((int) height - 1);
                                textView.setPadding(padding, (int) (padding * paddingScale), padding, (int) (padding * paddingScale));
                                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((rootView.getWidth() / columns.get(i).getColCount()) * width, (int) ((rootView.getHeight() / 13) * height));
                                params.topMargin = (int) ((rootView.getHeight() / 13) * columns.get(i).getTop());
                                params.leftMargin = (int) ((rootView.getWidth() / columns.get(i).getColCount()) * (columns.get(i).getLeft() - 1));
                                params.height = (int) ((rootView.getHeight() / 13) * height);
                                params.width = (rootView.getWidth() / columns.get(i).getColCount()) * width;
                                textView.setLayoutParams(params);
                                final int current = i;
                                textView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        InfoDialog infoDialog = new InfoDialog(MainView.this, columns.get(current));
                                        infoDialog.show();
                                    }
                                });
                                textView.setText(columns.get(i).getText());
                                GradientDrawable textViewBg = new GradientDrawable();
                                textViewBg.setShape(GradientDrawable.RECTANGLE);
                                textViewBg.setStroke((int) (1 * densityScale + 0.5f), strokeColor);
                                textViewBg.setColor(bgColor);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    textView.setBackground(textViewBg);
                                } else {
                                    textView.setBackgroundDrawable(textViewBg);
                                }
                                rootView.addView(textView);
                            }
                        }
                    });
                }
            }
            collection.addView(rootView);
            return rootView;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
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
