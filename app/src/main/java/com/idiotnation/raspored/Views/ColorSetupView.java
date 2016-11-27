package com.idiotnation.raspored.Views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.idiotnation.raspored.Contracts.ColorSetupContract;
import com.idiotnation.raspored.Dialogs.FiltersDialog;
import com.idiotnation.raspored.Dialogs.SettingsDialog;
import com.idiotnation.raspored.Presenters.ColorSetupPresenter;
import com.idiotnation.raspored.R;
import com.idiotnation.raspored.RasporedApplication;
import com.idiotnation.raspored.Utils;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.priyesh.chroma.ChromaDialog;
import me.priyesh.chroma.ColorMode;


public class ColorSetupView extends AppCompatActivity implements ColorSetupContract.View {


    ColorsListAdapter colorsListAdapter;

    // Initialization

    @Inject
    ColorSetupPresenter presenter;

    @BindView(R.id.color_setup_action_bar)
    Toolbar mToolbar;

    @BindView(R.id.color_setup_color_list)
    ListView colorsList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((RasporedApplication) getApplication()).component().inject(this);
        setContentView(R.layout.color_setup_layout);
        ButterKnife.bind(this);
        presenter = new ColorSetupPresenter();
        presenter.start(this, ColorSetupView.this);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.color_setup_menu, menu);
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
            case R.id.ab_save:
                ActivityCompat.finishAffinity(this);
                presenter.save();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        colorsListAdapter = new ColorsListAdapter(getApplicationContext(), R.layout.colors_list_item, Arrays.asList(new String[]{"Prozor", "Alatna traka", "Dani", "Sati", "Predmeti"}));
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
                    return new Integer[]{R.color.hoursBackgroundColor, R.color.hoursBackgroundStrokeColor, R.color.hoursTextColorPrimary};
                case "Predmeti":
                    return new Integer[]{R.color.lessonsBackgroundColor, R.color.lessonsBackgroundStrokeColor, R.color.lessonsTextColorPrimary};
            }
            return null;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View rootView = convertView;

            if (rootView == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(getContext());
                rootView = layoutInflater.inflate(R.layout.colors_list_item, null);
            }

            String text = getItem(position);

            if (text != null) {
                TextView textView = (TextView) rootView.findViewById(R.id.color_setup_list_item_color_text);
                textView.setText(text);
                textView.setTextColor(Utils.getColor(R.color.textColorPrimary, getApplicationContext()));
                LinearLayout colorsContainer = (LinearLayout) rootView.findViewById(R.id.color_setup_list_item_colors);
                presenter.populateColorsContainer(colorsContainer, getItemColors(text), getSupportFragmentManager());
            }

            return rootView;
        }

    }

}
