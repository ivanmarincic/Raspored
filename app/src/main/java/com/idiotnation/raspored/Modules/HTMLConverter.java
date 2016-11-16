package com.idiotnation.raspored.Modules;


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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class HTMLConverter extends AsyncTask<Void, Void, Void> {

    Context context;
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

    public List<List<TableColumn>> parseRaspored(String url) throws IOException, ParseException {
        List<List<TableColumn>> columns = new ArrayList<>();
        Document doc = Jsoup.connect(url).timeout(0).get();
        Element days = doc.select("#WeekTablee1 tbody tr").get(0);
        Elements head = doc.select("head").get(0).select("script");
        List<String[]> properties = getElementsArray(head.get(head.size() - 1).html());
        for (int i = 0; i < properties.size(); i++) {
            TableColumn tableColumn = new TableColumn();
            String id = properties.get(i)[2].split("=")[1].replaceAll("\"", "").replaceAll(" ", "");
            Date startDate = new SimpleDateFormat("yyy-MM-dd,HH:mm:ss").parse(properties.get(i)[7].split("=")[1].replaceAll("\"", "").replaceAll(" ", "")),
                    endDate = new SimpleDateFormat("yyy-MM-dd,HH:mm:ss").parse(properties.get(i)[6].split("=")[1].replaceAll("\"", "").replaceAll(" ", ""));
            float start = (float) ((float)startDate.getHours() - 7.0 + (startDate.getMinutes() * (1.0 / 60.0))),
                    end = (float) ((float)endDate.getHours() - 7.0 + (endDate.getMinutes() * (1.0 / 60.0))),
                    height = end - start;
            int width = Integer.parseInt(properties.get(i)[4].split("=")[1].replaceAll("\"", "").replaceAll(" ", "")),
                    colCount = Integer.parseInt(properties.get(i)[3].split("=")[1].replaceAll("\"", "").replaceAll(" ", "")),
                    left = Integer.parseInt(properties.get(i)[5].split("=")[1].replaceAll("\"", "").replaceAll(" ", "")),
                    day = Integer.parseInt(properties.get(i)[8].split("=")[1].replaceAll("\"", "").replaceAll(" ", ""));
            tableColumn.setWidth(width);
            tableColumn.setHeight(height);
            tableColumn.setLeft(left);
            tableColumn.setTop(start);
            tableColumn.setColCount(colCount);
            tableColumn.setText(days.child(day).select("[id=\""+id+"\"]").get(0).select("span").get(0).text());
            tableColumn.setStart(startDate);
            tableColumn.setEnd(endDate);
            tableColumn.setVisibility(true);
            if (columns.size()<=day){
                columns.add(new ArrayList<TableColumn>());
            }
            columns.get(day).add(tableColumn);
        }
        while (columns.size()<6){
            columns.add(new ArrayList<TableColumn>());
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

    public List<String[]> getElementsArray(String js) {
        String start = "FInAppointment = false;\r\n}\r\n}\r\n\r\n";
        String end = "\r\n\r\nvar PosFurtherUp";
        String javaScript = js.substring(js.indexOf(start) + start.length(), js.indexOf(end));
        String[] list = javaScript.split("\r\n\r\n");
        List<String[]> strings = new ArrayList<>();
        for (String item : list) {
            strings.add(item.split(";"));
        }
        return strings;
    }

    public void setFinishListener(HTMLConverterListener htmlConverterListener) {
        this.htmlConverterListener = htmlConverterListener;
    }

    public interface HTMLConverterListener {
        void onFinish(List<List<TableColumn>> columns);
    }

}
