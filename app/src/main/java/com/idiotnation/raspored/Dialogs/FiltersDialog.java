package com.idiotnation.raspored.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.widget.CompoundButton;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.idiotnation.raspored.Modules.FilterOption;
import com.idiotnation.raspored.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Context.MODE_PRIVATE;


public class FiltersDialog extends Dialog {

    SharedPreferences prefs;
    Context context;
    List<FilterOption> filterOptions;
    boolean refreshFilters = false;
    onFinishListner finishListner;

    @BindView(R.id.filter_group_option_1)
    AppCompatCheckBox option1;

    @BindView(R.id.filter_group_option_2)
    AppCompatCheckBox option2;

    @BindView(R.id.filter_group_option_3)
    AppCompatCheckBox option3;

    @BindView(R.id.filter_group_option_a)
    AppCompatCheckBox optionA;

    @BindView(R.id.filter_group_option_b)
    AppCompatCheckBox optionB;

    public FiltersDialog(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filters_layout);
        ButterKnife.bind(this);
        init();
        properties();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finishListner.onFinish(refreshFilters);
    }

    public void init() {
        prefs = context.getSharedPreferences("com.idiotnation.raspored", MODE_PRIVATE);
        filterOptions = new ArrayList();
        loadOptionsList();
    }

    public void properties() {
        option1.setChecked(filterOptions.get(0).isValue());
        option1.setOnCheckedChangeListener(checkedChangeListener);
        option2.setChecked(filterOptions.get(1).isValue());
        option2.setOnCheckedChangeListener(checkedChangeListener);
        option3.setChecked(filterOptions.get(2).isValue());
        option3.setOnCheckedChangeListener(checkedChangeListener);
        optionA.setChecked(filterOptions.get(3).isValue());
        optionA.setOnCheckedChangeListener(checkedChangeListener);
        optionB.setChecked(filterOptions.get(4).isValue());
        optionB.setOnCheckedChangeListener(checkedChangeListener);
    }

    CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            String id = compoundButton.getTag().toString();
            FilterOption option = new FilterOption(id, b);
            filterOptions.set(getListIndexByName(id), option);
            saveOptionsList();
        }
    };

    public void loadOptionsList() {
        filterOptions = (List<FilterOption>) new Gson().fromJson(prefs.getString("FilterOptions", ""), new TypeToken<List<FilterOption>>() {
        }.getType());
        if (filterOptions == null || filterOptions.size() < 5) {
            filterOptions = new ArrayList(Arrays.asList(new FilterOption("1", false), new FilterOption("2", false), new FilterOption("3", false), new FilterOption("a", false), new FilterOption("b", false)));
        }
    }

    public void saveOptionsList() {
        refreshFilters = true;
        prefs.edit().putString("FilterOptions", new Gson().toJson(filterOptions).toString()).apply();
    }

    private int getListIndexByName(String name) {
        switch (name) {
            case "1":
                return 0;
            case "2":
                return 1;
            case "3":
                return 2;
            case "a":
                return 3;
            case "b":
                return 4;
        }
        return -1;
    }

    public void setOnFinishListener(onFinishListner finishListener) {
        this.finishListner = finishListener;
    }

    public interface onFinishListner {
        void onFinish(boolean refreshFilters);
    }

}
