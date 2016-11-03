package com.idiotnation.raspored.Presenters;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.idiotnation.raspored.Contracts.MainContract;
import com.idiotnation.raspored.DegreeLoader;
import com.idiotnation.raspored.HTMLConverter;
import com.idiotnation.raspored.TableColumn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import static android.content.Context.MODE_PRIVATE;
import static com.idiotnation.raspored.Utils.ERROR_INTERNAL;
import static com.idiotnation.raspored.Utils.ERROR_INTERNET;
import static com.idiotnation.raspored.Utils.ERROR_UNAVAILABLE;
import static com.idiotnation.raspored.Utils.INFO_FINISHED;
import static com.idiotnation.raspored.Utils.INFO_MESSAGE;

public class MainPresenter implements MainContract.Presenter {

    @Inject
    public MainPresenter() {};


    MainContract.View view;
    Context context;

    @Override
    public void start(MainContract.View view, Context context) {
        this.view = view;
        this.context = context;
        view.initialize();
    }

    @Override
    public void download(String url) {
        try {
            if(url!="NN"){
                HTMLConverter htmlConverter = new HTMLConverter(context, url);
                htmlConverter.setFinishListener(new HTMLConverter.HTMLConverterListener() {
                    @Override
                    public void onFinish(List<List<TableColumn>> columns) {
                        if(columns!=null){
                            getRaspored();
                        }else {
                            view.stopAnimation();
                            view.showMessage(View.VISIBLE, ERROR_INTERNAL);
                        }
                    }
                });
                if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
                    htmlConverter.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }else {
                    htmlConverter.execute();
                }
            }else{
                view.showMessage(View.VISIBLE, ERROR_UNAVAILABLE);
                view.stopAnimation();
            }
        } catch (Exception e) {
            e.printStackTrace();
            view.showMessage(View.VISIBLE, ERROR_INTERNAL);
            view.stopAnimation();
        }
    }

    @Override
    public void getRaspored() {
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(context.getFilesDir(), "raspored.json")));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
        }finally {
            view.showMessage(View.VISIBLE, INFO_FINISHED);
            view.setRaspored((List<List<TableColumn>>)new Gson().fromJson(text.toString(), new TypeToken<List<List<TableColumn>>>() {
            }.getType()));
        }
    }

    @Override
    public void refresh(final int idNumber) {
        if (idNumber!=-1){
            view.startAnimation();
            DegreeLoader degreeLoader = new DegreeLoader(context);
            degreeLoader.setOnFinishListener(new DegreeLoader.onFinihListener() {
                @Override
                public void onFinish(List list) {
                    if (list != null) {
                        context.getSharedPreferences("com.idiotnation.raspored", MODE_PRIVATE).edit().putInt("SpinnerDefault", idNumber).apply();
                        download(list.get(idNumber).toString());
                    }else{
                        view.stopAnimation();
                        view.showMessage(View.VISIBLE, ERROR_INTERNET);
                    }
                }
            });
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
                degreeLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }else {
                degreeLoader.execute();
            }
        }else {
            view.showMessage(View.VISIBLE, INFO_MESSAGE);
            view.stopAnimation();
        }

    }
}
