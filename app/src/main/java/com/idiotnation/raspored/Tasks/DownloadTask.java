package com.idiotnation.raspored.Tasks;


import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.idiotnation.raspored.Helpers.BackgroundTask;
import com.idiotnation.raspored.Models.LessonCell;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class DownloadTask extends BackgroundTask<List<List<LessonCell>>> {

    Context context;
    String url;
    int rasporedIndex;
    onFinishListener finishListener;

    public DownloadTask(Context context, String url, int rasporedIndex) {
        this.context = context;
        this.url = url;
        this.rasporedIndex = rasporedIndex;
    }

    @Override
    protected void onCreate() {
        onFinish(finishListener);
    }

    @Override
    protected List<List<LessonCell>> onExecute() {
        try {
            RequestQueue queue = Volley.newRequestQueue(context);
            String apiUrl = "INSERT API URL" + rasporedIndex;

            StringRequest stringRequest = new StringRequest(Request.Method.GET, apiUrl,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            saveResponseToJson(response);
                            listener.onFinish(parseResponse(response));
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        HTMLDownloadTask htmlConverter = new HTMLDownloadTask(context, url);
                        htmlConverter.onFinish(finishListener);
                        htmlConverter.run();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            queue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public void setFinishListener(onFinishListener onFinishListener) {
        this.finishListener = onFinishListener;
    }

    public void saveResponseToJson(String response) {
        try {
            new File(context.getFilesDir() + "/raspored.json").delete();
            FileOutputStream fos = context.openFileOutput("raspored.json", MODE_PRIVATE);
            fos.write(response.getBytes());
            fos.close();
        } catch (Exception ignored) {
        }
    }

    public List<List<LessonCell>> parseResponse(String response) {
        return (List<List<LessonCell>>) new Gson().fromJson(response, new TypeToken<List<List<LessonCell>>>() {
        }.getType());
    }
}
