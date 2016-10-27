package com.idiotnation.raspored.Presenters;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

import com.idiotnation.raspored.Contracts.MainContract;
import com.idiotnation.raspored.DegreeLoader;
import com.idiotnation.raspored.PDFConverter;
import com.idiotnation.raspored.TableColumn;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import static android.content.Context.MODE_PRIVATE;
import static com.idiotnation.raspored.Utils.getFinalURL;
import static com.idiotnation.raspored.Utils.getGDiskId;

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
    public void nextPage(int currnetPage, int pageCount) {
        if (currnetPage < pageCount) {
            currnetPage++;
        } else {
            currnetPage = 1;
        }
        view.nextPage(currnetPage);
    }

    @Override
    public void previousPage(int currnetPage, int pageCount) {
        if (currnetPage > 1) {
            currnetPage--;
        } else {
            currnetPage = pageCount;
        }
        view.previousPage(currnetPage);
    }

    @Override
    public void getRaspored(Context context, int pageNumber) {
        try {
            PDFConverter pdfConverterf = new PDFConverter(context, pageNumber);
            pdfConverterf.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            pdfConverterf.setFinishListener(new PDFConverter.PDFConverterListener() {
                @Override
                public void onFinish(final Bitmap result, int pageCount, List<TableColumn> rasporedColumns) {
                    view.setRaspored(result, pageCount, rasporedColumns);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            view.stopAnimation(View.VISIBLE);
        }
    }

    @Override
    public void download(String downloadUrl, Context context) {
        try {
            if (new File(context.getFilesDir().getAbsolutePath() + "/raspored.pdf").exists()) {
                new File(context.getFilesDir().getAbsolutePath() + "/raspored.pdf").delete();
            }

            String fileName = "raspored.pdf";

            // download pdf file.
            URL url = new URL(getFinalURL(downloadUrl));
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(false);
            c.connect();
            File file = context.getFilesDir();
            file.mkdirs();
            FileOutputStream fos = context.openFileOutput(fileName.toString(), MODE_PRIVATE);
            InputStream is = c.getInputStream();
            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len1);
            }
            fos.close();
            is.close();
            System.gc();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    @Override
    public void refresh(final int idNumber, final int pageNumber) {
        view.startAnimation();
        DegreeLoader degreeLoader = new DegreeLoader(context);
        degreeLoader.setOnFinishListener(new DegreeLoader.onFinihListener() {
            @Override
            public void onFinish(List list) {
                if (list != null) {
                    downloadTask task = new downloadTask(idNumber, pageNumber, list);
                    task.execute();
                }
            }
        });
        degreeLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public class downloadTask extends AsyncTask<Void, Void, Void> {

        boolean executePost;
        String result;
        int idNumber, pageNumber;
        List<String> rasporedUrls = new ArrayList<>(10);

        public downloadTask(int idNumber, int pageNumber, List<String> rasporedUrls){
            this.rasporedUrls = rasporedUrls;
            this.idNumber = idNumber;
            this.pageNumber = pageNumber;
        }

        @Override
        protected void onPreExecute() {
            view.startAnimation();
        }

        @Override
        protected Void doInBackground(Void... params) {
            executePost = true;
            result = "";
            if (rasporedUrls.size() >= 10) {
                if (!(context.getSharedPreferences("com.idiotnation.raspored", MODE_PRIVATE).getString("CurrentRasporedId", "NN").equals(getGDiskId(rasporedUrls.get(idNumber))) && new File(context.getFilesDir().getAbsolutePath() + "/raspored.pdf").exists())) {
                    download("https://docs.google.com/uc?export=download&id=" + getGDiskId(rasporedUrls.get(idNumber)), context);
                    result = "Preuzimanje dovršeno";
                } else {
                    executePost = false;
                    result = "Raspored nije izmjenjen";
                }
            } else {
                result = "Pokušajte ponovno doslo je do greške";
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (executePost) {
                getRaspored(context, pageNumber);
                view.update(new SimpleDateFormat("dd.MM.yyyy").format(new Date()), getGDiskId(rasporedUrls.get(idNumber)));
            }
            view.stopAnimation(View.INVISIBLE);
            Toast.makeText(context, result, Toast.LENGTH_SHORT).show();
        }
    }
}
