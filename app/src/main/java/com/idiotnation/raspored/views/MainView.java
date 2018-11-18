package com.idiotnation.raspored.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.idiotnation.raspored.R;
import com.idiotnation.raspored.adapters.AppointmentsListAdapter;
import com.idiotnation.raspored.contracts.MainContract;
import com.idiotnation.raspored.custom.CustomSwipeToRefresh;
import com.idiotnation.raspored.custom.HeaderItemDecoration;
import com.idiotnation.raspored.dialogs.AppointmentInfoDialog;
import com.idiotnation.raspored.helpers.Utils;
import com.idiotnation.raspored.models.dto.AppointmentDto;
import com.idiotnation.raspored.presenters.MainPresenter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainView extends AppCompatActivity implements MainContract.View {

    @BindView(R.id.main_view_toolbar)
    Toolbar toolbar;

    @BindView(R.id.main_view_appointments_list)
    RecyclerView list;

    @BindView(R.id.main_view_appointments_refresh)
    CustomSwipeToRefresh swipeToRefresh;

    MainContract.Presenter presenter;
    AppointmentsListAdapter listAdapter;
    private boolean isRefreshing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_view);
        ButterKnife.bind(this);
        presenter = new MainPresenter();
        try {
            presenter.start(this, getApplicationContext());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        ActivityCompat.finishAffinity(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == Utils.SETTINGS_RESULT_CODE && data.getExtras() != null) {
                presenter.checkSettingsResultFlags(data.getExtras().getInt(Utils.SETTINGS_RESULT_EXTRAS, -1));
            }
        }
    }

    @Override
    public void initialize() {
        setSupportActionBar(toolbar);
        swipeToRefresh.setColorSchemeColors(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
        swipeToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                presenter.syncAppointments();
            }
        });
        listAdapter = new AppointmentsListAdapter(getApplicationContext(), new ArrayList<AppointmentDto>());
        listAdapter.setItemOnSelectListener(new AppointmentsListAdapter.ItemOnSelectListener() {
            @Override
            public void onSelect(AppointmentDto item) {
                AppointmentInfoDialog dialog = new AppointmentInfoDialog(MainView.this, item);
                dialog.setOnItemBlockedListener(new AppointmentInfoDialog.OnItemBlocked() {
                    @Override
                    public void onBlock(AppointmentDto appointmentDto) {
                        presenter.blockAppointment(appointmentDto);
                    }
                });
                dialog.show();
            }
        });
        list.setAdapter(listAdapter);
        list.addItemDecoration(new HeaderItemDecoration(list, listAdapter));
        list.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        if (presenter.checkIfCourseIsSelected()) {
            presenter.getAppointments();
        }
    }

    @Override
    public void loadList(List<AppointmentDto> appointments) {
        listAdapter.setList(appointments);
        listAdapter.notifyDataSetChanged();
        list.setVisibility(View.VISIBLE);
        list.scrollToPosition(listAdapter.getIndexOfNow());
        setRefreshing(false);
    }

    @Override
    public void scrollToNow() {
        RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(getApplicationContext()) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };
        smoothScroller.setTargetPosition(listAdapter.getIndexOfNow());
        RecyclerView.LayoutManager layoutManager = list.getLayoutManager();
        if (layoutManager != null) {
            layoutManager.startSmoothScroll(smoothScroller);
        }
    }

    @Override
    public void setRefreshing(final boolean refreshing) {
        this.isRefreshing = refreshing;
        swipeToRefresh.post(new Runnable() {
            @Override
            public void run() {
                if (refreshing == isRefreshing) {
                    swipeToRefresh.setRefreshing(refreshing);
                } else {
                    swipeToRefresh.setRefreshing(false);
                }
            }
        });

    }

    @Override
    public void startFirstTimeConfiguration() {
        list.setVisibility(View.INVISIBLE);
        startActivityForResult(new Intent(getApplicationContext(), SettingsView.class), Utils.SETTINGS_RESULT_CODE);
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
            case R.id.main_view_menu_settings:
                item.setEnabled(true);
                startActivityForResult(new Intent(getApplicationContext(), SettingsView.class), Utils.SETTINGS_RESULT_CODE);
                return true;
            case R.id.main_view_menu_now:
                item.setEnabled(true);
                scrollToNow();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
