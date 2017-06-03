package com.idiotnation.raspored.Tasks;


import android.content.Context;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.idiotnation.raspored.Helpers.BackgroundTask;
import com.idiotnation.raspored.Models.LessonCell;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
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

public class HTMLDownloadTask extends BackgroundTask<List<List<LessonCell>>> {

    Context context;
    String url;

    public HTMLDownloadTask(Context context, String url) {
        this.context = context;
        this.url = url;
    }

    @Override
    protected void onCreate() {

    }

    @Override
    protected List<List<LessonCell>> onExecute() {
        try {
            listener.onFinish(parseRaspored(url));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public List<List<LessonCell>> parseRaspored(String url) throws IOException {
        List<List<LessonCell>> columns = new ArrayList<>();
        DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("yyy-MM-dd,HH:mm:ss");
        try {
            Document doc = Jsoup.connect(url).timeout(16000).get();
            Elements dayss = doc.select("#WeekTablee1 tbody tr");
            if (dayss.size() > 0) {
                Element days = dayss.get(0);
                Elements head = doc.select("head").get(0).select("script");
                List<String[]> properties = getElementsArray(head.get(head.size() - 1).html());
                if (properties != null) {
                    for (int i = 0; i < properties.size(); i++) {
                        LessonCell lessonCell = new LessonCell();
                        String id = properties.get(i)[2].split("=")[1].replaceAll("\"", "").replaceAll(" ", "");
                        DateTime startDate = timeFormatter.parseDateTime(properties.get(i)[7].split("=")[1].replaceAll("\"", "").replaceAll(" ", "")),
                                endDate = timeFormatter.parseDateTime(properties.get(i)[6].split("=")[1].replaceAll("\"", "").replaceAll(" ", ""));
                        float start = (float) ((float) startDate.getHourOfDay() - 7.0 + (startDate.getMinuteOfHour() * (1.0 / 60.0))),
                                end = (float) ((float) endDate.getHourOfDay() - 7.0 + (endDate.getMinuteOfHour() * (1.0 / 60.0))),
                                height = end - start;
                        int width = Integer.parseInt(properties.get(i)[4].split("=")[1].replaceAll("\"", "").replaceAll(" ", "")),
                                colCount = Integer.parseInt(properties.get(i)[3].split("=")[1].replaceAll("\"", "").replaceAll(" ", "")),
                                left = Integer.parseInt(properties.get(i)[5].split("=")[1].replaceAll("\"", "").replaceAll(" ", "")),
                                day = Integer.parseInt(properties.get(i)[8].split("=")[1].replaceAll("\"", "").replaceAll(" ", ""));
                        lessonCell.setWidth(width);
                        lessonCell.setHeight(height);
                        lessonCell.setLeft(left);
                        lessonCell.setTop(start);
                        lessonCell.setColCount(colCount);
                        lessonCell.setText(days.child(day).select("[id=\"" + id + "\"]").get(0).select("span").get(0).text());
                        lessonCell.setStart(startDate);
                        lessonCell.setEnd(endDate);
                        while (columns.size() < day + 1) {
                            columns.add(new ArrayList<LessonCell>());
                        }
                        columns.get(day).add(lessonCell);
                    }
                }
            }
            while (columns.size() < 6) {
                columns.add(new ArrayList<LessonCell>());
            }
            saveColumnsToJson(columns);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return columns;
    }

    public void saveColumnsToJson(List<List<LessonCell>> columns) {
        try {
            new File(context.getFilesDir() + "/raspored.json").delete();
            FileOutputStream fos = context.openFileOutput("raspored.json", MODE_PRIVATE);
            Gson gson = Converters.registerDateTime(new GsonBuilder()).create();
            fos.write(gson.toJson(columns).getBytes());
            fos.close();
        } catch (Exception ignored) {
        }
    }

    public List<String[]> getElementsArray(String js) {
        String start = "FInAppointment = false;\r\n}\r\n}\r\n\r\n";
        String end = "\r\n\r\nvar PosFurtherUp";
        int indexOfStart = js.indexOf(start), indexOfEnd = js.indexOf(end);
        if (indexOfStart == -1 || indexOfEnd == -1) {
            return null;
        }
        String javaScript = js.substring(indexOfStart + start.length(), indexOfEnd);
        String[] list = javaScript.split("\r\n\r\n");
        List<String[]> strings = new ArrayList<>();
        for (String item : list) {
            strings.add(item.split(";"));
        }
        return strings;
    }

}
