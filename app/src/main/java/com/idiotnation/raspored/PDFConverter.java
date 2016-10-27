package com.idiotnation.raspored;


import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.idiotnation.raspored.Utils.getColumns;
import static com.idiotnation.raspored.Utils.lineColor;

public class PDFConverter extends AsyncTask<Void, Void, Void> {

    Context context;
    List<Integer> xs, ys;
    PDFConverterListener pdfConverterListener;
    int pageNum, pageCount;

    public PDFConverter(Context context, int pageNum) {
        this.context = context;
        this.pageNum = pageNum - 1;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            PdfiumCore pdfiumCore = new PdfiumCore(context);
            PdfDocument pdfDocument = pdfiumCore.newDocument(getSeekableFileDescriptor(context.getFilesDir().getAbsolutePath() + "/raspored.pdf"));
            pageCount = pdfiumCore.getPageCount(pdfDocument);
            pageNum = pageNum >= pageCount ? 0 : pageNum;

            pdfiumCore.openPage(pdfDocument, pageNum);

            int width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNum) * 3;
            int height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNum) * 3;

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

            pdfiumCore.renderPageBitmap(pdfDocument, bitmap, pageNum, 0, 0, width, height);

            Bitmap cpy = bitmap.copy(Bitmap.Config.RGB_565, false);
            bitmap.recycle();
            bitmap = cpy;

            pdfiumCore.closeDocument(pdfDocument);
            getTableLines(bitmap);
            List<TableColumn> columns = getColumns(bitmap, xs, ys);
            pdfConverterListener.onFinish(Utils.createInvertedBitmap(bitmap, context.getSharedPreferences("com.idiotnation.raspored", MODE_PRIVATE).getBoolean("DarkMode", false)), pageCount, columns);
            return null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected ParcelFileDescriptor getSeekableFileDescriptor(String path) throws IOException {
        ParcelFileDescriptor pfd;

        File pdfCopy = new File(path);
        if (pdfCopy.exists()) {
            pfd = ParcelFileDescriptor.open(pdfCopy, ParcelFileDescriptor.MODE_READ_ONLY);
            return pfd;
        }

        if (!path.contains("://")) {
            path = String.format("file://%s", path);
        }

        Uri uri = Uri.parse(path);
        pfd = context.getContentResolver().openFileDescriptor(uri, "r");

        if (pfd == null) {
            throw new IOException("Cannot get FileDescriptor for " + path);
        }

        return pfd;
    }

    // TODO: Make better algorithm that is aware of both sides at once

    public void getTableLines(Bitmap rasporedBitmap) {
        xs = new ArrayList<>();
        ys = new ArrayList<>();
        if (rasporedBitmap != null) {
            ArrayList<Integer> wCache = new ArrayList<>();
            wCache.clear();
            for (int i = 0; i < rasporedBitmap.getWidth(); i++) {
                if (Integer.toHexString(rasporedBitmap.getPixel(i, rasporedBitmap.getHeight() / 2)).equals(lineColor)) {
                    for (int j = 0; j < rasporedBitmap.getHeight() / 2; j++) {
                        if (Integer.toHexString(rasporedBitmap.getPixel(i, rasporedBitmap.getHeight() / 2 + j)).equals(lineColor)) {
                            wCache.add(i);
                        } else {
                            wCache.clear();
                            break;
                        }
                        if (wCache.size() > 100) {
                            xs.add(i);
                            while (Integer.toHexString(rasporedBitmap.getPixel(i, rasporedBitmap.getHeight() / 2 + j)).equals(lineColor)) {
                                i++;
                            }
                            wCache.clear();
                            break;
                        }
                    }
                }
            }
            wCache.clear();
            for (int i = 0; i < rasporedBitmap.getHeight(); i++) {
                if (Integer.toHexString(rasporedBitmap.getPixel(xs.get(0) + 20, i)).equals(lineColor)) {
                    for (int j = 0; j < rasporedBitmap.getWidth() / 2; j++) {
                        if (Integer.toHexString(rasporedBitmap.getPixel(xs.get(0) + j, i)).equals(lineColor)) {
                            wCache.add(i);
                        } else {
                            wCache.clear();
                            break;
                        }
                        if (wCache.size() > 100) {
                            ys.add(i);
                            while (Integer.toHexString(rasporedBitmap.getPixel(xs.get(0) + j, i)).equals(lineColor)) {
                                i++;
                            }
                            wCache.clear();
                            break;
                        }
                    }
                }
            }
        }
    }

    public void saveColumnsToJson(List<TableColumn> columns) {
        try {
            FileOutputStream fos = context.openFileOutput("raspored.json", MODE_PRIVATE);
            fos.write(new Gson().toJson(columns).getBytes());
            fos.close();
        } catch (Exception e) {
        }
    }

    public List<TableColumn> loadColumnsFromJson() {
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
            return new Gson().fromJson(text.toString(), new TypeToken<ArrayList<TableColumn>>() {
            }.getType());
        }
    }

    public void setFinishListener(PDFConverterListener pdfConverterListener) {
        this.pdfConverterListener = pdfConverterListener;
    }

    public interface PDFConverterListener {
        void onFinish(Bitmap result, int pageCount, List<TableColumn> columns);
    }

}
