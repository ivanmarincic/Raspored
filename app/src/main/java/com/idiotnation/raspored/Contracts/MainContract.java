package com.idiotnation.raspored.Contracts;

import android.content.Context;
import android.widget.RelativeLayout;

import com.idiotnation.raspored.Modules.TableCell;

import java.util.List;

public class MainContract {

    public interface View {
        void initialize();
        void checkContent();
        void refreshPages();
        void setRaspored(List<List<TableCell>> columns);
        void startAnimation();
        void stopAnimation();
        void showMessage(int visibility, int type);
    }

    public interface Presenter {
        void start(View view, Context context);
        void download(String url);
        List<List<TableCell>> getRaspored();
        void refresh(int idNumber);
        void refreshNotifications();
        void refreshFilters();
        void populateHours(RelativeLayout layout, Context context);
        String getRasporedUrl(int index);
    }

}
