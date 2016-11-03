package com.idiotnation.raspored;


import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class HTMLConverter extends AsyncTask<Void, Void, Void> {

    Context context;
    List<Integer> xs, ys;
    HTMLConverterListener htmlConverterListener;
    String url;

    public HTMLConverter(Context context, String url) {
        this.context = context;
        this.url = url;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            htmlConverterListener.onFinish(parseRaspored(url));
        } catch (Exception e) {
            e.printStackTrace();
            htmlConverterListener.onFinish(null);
        }
        return null;
    }

    public List<List<TableColumn>> parseRaspored(String url) throws IOException {
        List<List<TableColumn>> columns = new ArrayList<>();
        Document doc = Jsoup.connect(url).timeout(0).get();
        Element days = doc.select("#WeekTablee1 tbody tr").get(0);
        for (int i = 0; i < days.children().size(); i++) {
            Elements lessons = days.child(i).select(".appointment");
            List<TableColumn> tableLessons = new ArrayList<>();
            for (Element lesson : lessons) {
                TableColumn tableColumn = new TableColumn();
                String[] styles = lesson.attr("style").split(";");
                for (String style : styles) {
                    style = style.replaceAll(" ", "");
                    String[] attrs = style.split(":");
                    if (attrs[0].equals("top")) {
                        tableColumn.setTop((int) ((Integer.parseInt(attrs[1].replace("px", "")) / 24.0) - 6.5));
                    }
                    if (attrs[0].equals("height")) {
                        tableColumn.setHeight((int) ((Integer.parseInt(attrs[1].replace("px", "")) / 24.0) + 0.5));
                        System.out.println();
                    }
                }
                tableColumn.setText(lesson.select("span").get(0).text());
                tableLessons.add(tableColumn);
            }
            columns.add(tableLessons);
        }
        saveColumnsToJson(columns);
        return columns;
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

    public void setFinishListener(HTMLConverterListener htmlConverterListener) {
        this.htmlConverterListener = htmlConverterListener;
    }

    public interface HTMLConverterListener {
        void onFinish(List<List<TableColumn>> columns);
    }

}
