package com.idiotnation.raspored.Contracts;

import android.content.Context;
import android.widget.RelativeLayout;

import com.idiotnation.raspored.Modules.FilterOption;
import com.idiotnation.raspored.Modules.TableColumn;

import java.util.List;

public class MainContract {

    public interface View {
        void initialize();
        void checkContent();
        void refreshPages();
        void setRaspored(List<List<TableColumn>> columns);
        void startAnimation();
        void stopAnimation();
        void showMessage(int visibility, int type);
    }

    public interface Presenter {
        void start(View view, Context context);
        void download(String url);
        List<List<TableColumn>> getRaspored();
        void refresh(int idNumber);
        void refreshNotifications();
        void refreshFilters();
        void populateHours(RelativeLayout layout, Context context);
    }

}
