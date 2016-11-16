package com.idiotnation.raspored.Modules;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.idiotnation.raspored.Dialogs.FiltersDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class FiltersLoader extends AsyncTask<Void, Void, Void> {

    Context context;
    List<FilterOption> filterOptions;
    List<List<TableColumn>> columns;
    onFinishListner finishListener;

    public FiltersLoader(Context context, List<List<TableColumn>> columns) {
        this.context = context;
        this.columns = columns;
        filterOptions = (List<FilterOption>) new Gson().fromJson(context.getSharedPreferences("com.idiotnation.raspored", MODE_PRIVATE).getString("FilterOptions", ""), new TypeToken<List<FilterOption>>() {
        }.getType());
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            if (filterOptions.size() > 0) {
                setupFilters();
                finishListener.onFinish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setupFilters() {
        for (int i = 0; i < 6; i++) {
            for (TableColumn tableColumn : columns.get(i)) {
                tableColumn.setVisibility(getVisibilityFilter(tableColumn.getText().toLowerCase()));
            }
        }
        saveColumnsToJson(columns);
    }

    public void saveColumnsToJson(List<List<TableColumn>> columns) {
        try {
            new File(context.getFilesDir() + "/raspored.json").delete();
            FileOutputStream fos = context.openFileOutput("raspored.json", MODE_PRIVATE);
            fos.write(new Gson().toJson(columns).getBytes());
            fos.close();
        } catch (Exception e) {
        }
    }

    public boolean getVisibilityFilter(String text) {
        if (filterOptions == null) {
            return true;
        }
        for (int i = 0; i < filterOptions.size(); i++) {
            FilterOption filterOption = filterOptions.get(i);
            if (filterOption.isValue()) {
                if (!text.contains("grupa")) {
                    return true;
                } else {
                    if (text.contains("grupa " + filterOption.getFilter())) {
                        return true;
                    } else {
                        if (i <= 2 && (text.contains("grupa a") || text.contains("grupa b") || text.contains("grupa c"))) {
                            return true;
                        } else if (i > 2 && (text.contains("grupa 1") || text.contains("grupa 2") || text.contains("grupa 3") || text.contains("grupa 4"))) {
                            return true;
                        }
                        return false;
                    }
                }
            }
        }
        return true;
    }
    public void setOnFinishListener(onFinishListner finishListener) {
        this.finishListener = finishListener;
    }

    public interface onFinishListner {
        void onFinish();
    }
}
