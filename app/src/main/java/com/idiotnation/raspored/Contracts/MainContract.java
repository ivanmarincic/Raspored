package com.idiotnation.raspored.Contracts;

import android.content.Context;

import com.idiotnation.raspored.TableColumn;

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
        void getRaspored();
        void refresh(int idNumber);
    }

}
