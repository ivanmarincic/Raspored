package com.idiotnation.raspored.Views;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.idiotnation.raspored.Contracts.AppearanceContract;
import com.idiotnation.raspored.Presenters.AppearancePresenter;
import com.idiotnation.raspored.R;
import com.idiotnation.raspored.Utils;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AppearanceView extends AppCompatActivity implements AppearanceContract.View {


    AppearancePresenter presenter;
    ColorsListAdapter colorsListAdapter;

    // Initialization

    @BindView(R.id.color_setup_action_bar)
    Toolbar mToolbar;

    @BindView(R.id.color_setup_color_list)
    ListView colorsList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.color_setup_layout);
        ButterKnife.bind(this);
        presenter = new AppearancePresenter();
        presenter.start(this, AppearanceView.this);
    }

    @Override
    public void initialize() {
        setActionBarProperties();
        setColorsListProperties();
        setColors();
    }

    @Override
    public void refreshList() {
        if(colorsListAdapter!=null){
            colorsListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBackPressed() {
        ActivityCompat.finishAffinity(this);
        presenter.save();
    }

    public void setActionBarProperties() {
        mToolbar.setTitleTextColor(Utils.getColor(R.color.actionBarTextColorPrimary, getApplicationContext()));
        mToolbar.setBackgroundColor(Utils.getColor(R.color.actionBarBackgroundColor, getApplicationContext()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Utils.getColor(R.color.actionBarBackgroundColor, getApplicationContext())-592396);
        }
        setSupportActionBar(mToolbar);
    }

    public void setColorsListProperties() {
        colorsListAdapter = new ColorsListAdapter(getApplicationContext(), R.layout.colors_list_item, Arrays.asList("Prozor", "Alatna traka", "Dani", "Sati", "Predmeti", "Widget"));
        colorsList.setAdapter(colorsListAdapter);
    }

    public void setColors() {
        mToolbar.getRootView().setBackgroundColor(Utils.getColor(R.color.windowBackgroundColor, getApplicationContext()));
        colorsList.setDivider(null);
    }

    public class ColorsListAdapter extends ArrayAdapter<String>{

        public ColorsListAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
        }

        private Integer[] getItemColors(String text){
            switch (text){
                case "Prozor":
                    return new Integer[]{R.color.windowBackgroundColor, R.color.textColorPrimary, R.color.colorAccent};
                case "Alatna traka":
                    return new Integer[]{R.color.actionBarBackgroundColor, R.color.actionBarTextColorPrimary};
                case "Dani":
                    return new Integer[]{R.color.tabsBarBackgroundColor, R.color.tabsBarTextColorPrimary};
                case "Sati":
                    return new Integer[]{R.color.hoursBackgroundColor, R.color.hoursTextColorPrimary, R.color.hoursBackgroundStrokeColor};
                case "Predmeti":
                    return new Integer[]{R.color.lessonsBackgroundColor, R.color.lessonsTextColorPrimary, R.color.lessonsBackgroundStrokeColor};
                case "Widget":
                    return new Integer[]{R.color.widgetBackgroundColor, R.color.widgetTextColorPrimary};
            }
            return null;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View rootView = convertView;
            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            if (rootView == null) {
                rootView = layoutInflater.inflate(R.layout.colors_list_item, null);
            }

            String text = getItem(position);

            if (text != null) {
                TextView textView = (TextView) rootView.findViewById(R.id.color_setup_list_item_color_text);
                textView.setText(text);
                textView.setTextColor(Utils.getColor(R.color.textColorPrimary, getApplicationContext()));
                LinearLayout colorsContainer = (LinearLayout) rootView.findViewById(R.id.color_setup_list_item_colors);
                presenter.populateColorsContainer(layoutInflater, colorsContainer, getItemColors(text));
            }

            return rootView;
        }

    }

}
