package com.idiotnation.raspored.Contracts;


import android.app.Activity;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.idiotnation.raspored.Modules.TableColumn;

import java.util.List;

public class ColorSetupContract {

    public interface View {
        void initialize();
        void refreshList();
    }

    public interface Presenter {
        void start(ColorSetupContract.View view,  Activity activity);
        void populateColorsContainer(LinearLayout container, Integer[] colorIds, FragmentManager fragmentManager);
        void save();
    }

}
