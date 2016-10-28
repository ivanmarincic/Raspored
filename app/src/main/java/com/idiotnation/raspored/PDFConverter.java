package com.idiotnation.raspored;


import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;

import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
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

    public void getTableLines(Bitmap rasporedBitmap) {
        if(rasporedBitmap==null)
            return;

        xs = new ArrayList<>();
        ys = new ArrayList<>();
        int range = rasporedBitmap.getWidth()>rasporedBitmap.getHeight()?rasporedBitmap.getWidth():rasporedBitmap.getHeight(), currentPosition = 0;
        int xCache=0, yCache=0, xStart = rasporedBitmap.getWidth()/2, yStart = rasporedBitmap.getHeight()/2;
        int xOffset=0, yOffset=0;
        while (currentPosition<range){
            if(currentPosition+yOffset<rasporedBitmap.getHeight()){
                if(Integer.toHexString(rasporedBitmap.getPixel(xStart, currentPosition+yOffset)).equals(lineColor)){
                    for (int i=0; i<150;i++){
                        if(Integer.toHexString(rasporedBitmap.getPixel(xStart+i, currentPosition+yOffset)).equals(lineColor)){
                            yCache++;
                            if(yCache>100){
                                yCache=0;
                                ys.add(currentPosition+yOffset);
                                if(ys.size()==1){
                                    yStart = ys.get(0)+20;
                                }
                                while (Integer.toHexString(rasporedBitmap.getPixel(xStart+i, currentPosition+yOffset)).equals(lineColor)) {
                                    yOffset++;
                                }
                                break;
                            }
                        }else {
                            yCache = 0;
                            break;

                        }
                    }
                }
            }
            if(currentPosition+xOffset<rasporedBitmap.getWidth()){
                if(Integer.toHexString(rasporedBitmap.getPixel(currentPosition+xOffset, yStart)).equals(lineColor)){
                    for (int i=0; i<150;i++){
                        if(Integer.toHexString(rasporedBitmap.getPixel(currentPosition+xOffset, yStart+i)).equals(lineColor)){
                            xCache++;
                            if(xCache>100){
                                xCache=0;
                                xs.add(currentPosition+xOffset);
                                if(xs.size()==1){
                                    xStart = xs.get(0)+20;
                                }
                                while (Integer.toHexString(rasporedBitmap.getPixel(currentPosition+xOffset, yStart+i)).equals(lineColor)) {
                                    xOffset++;
                                }
                                break;
                            }
                        }else {
                            xCache = 0;
                            break;

                        }
                    }
                }
            }
            currentPosition++;
        }
    }

    public void setFinishListener(PDFConverterListener pdfConverterListener) {
        this.pdfConverterListener = pdfConverterListener;
    }

    public interface PDFConverterListener {
        void onFinish(Bitmap result, int pageCount, List<TableColumn> columns);
    }

}
