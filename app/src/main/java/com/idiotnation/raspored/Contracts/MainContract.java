package com.idiotnation.raspored.Contracts;

import android.content.Context;
import android.graphics.Bitmap;

import com.idiotnation.raspored.TableColumn;

import java.util.List;

public class MainContract {

    public interface View {
        void initialize();
        void checkContent();
        void refreshPages();
        void setRaspored(Bitmap raspored, int pageCount, List<TableColumn> columns);
        void nextPage(int newPageNumber);
        void previousPage(int newPageNumber);
        void startAnimation();
        void stopAnimation(int visibility);
        void update(String date, String id);
    }

    public interface Presenter {
        void start(View view, Context context);
        void nextPage(int pageNumber, int pageCount);
        void previousPage(int pageNumber, int pageCount);
        void getRaspored(Context context, int pageNumber);
        void download(String url, Context context);
        void refresh(int idNumber, int pageNumber);
    }

}
