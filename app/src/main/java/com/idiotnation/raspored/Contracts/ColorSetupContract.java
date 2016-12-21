package com.idiotnation.raspored.Contracts;


import android.app.Activity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

public class ColorSetupContract {

    public interface View {
        void initialize();
        void refreshList();
    }

    public interface Presenter {
        void start(ColorSetupContract.View view,  Activity activity);
        void populateColorsContainer(LayoutInflater layoutInflater, LinearLayout container, Integer[] colorIds);
        void save();
    }

}
