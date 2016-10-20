package com.idiotnation.raspored;

import android.annotation.SuppressLint;
import android.app.job.JobScheduler;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static com.idiotnation.raspored.Utils.getGDiskId;

public class MainActivity extends AppCompatActivity {

    static Bitmap bmp; // Bitmap (image) used for mPager
    static FrameLayout mFrame; // Landscape mode layout
    static ImageView sati; // ImageView with clock values
    TextView errorMessage, infoMessage;
    ViewPager mPager; // Pager
    MyPagerAdapter mAdapter; // Adapeter for mPager
    List<String> rasporedUrls = new ArrayList<>(10); // List of URLs for downloading PDF files
    static List<Integer> xs, ys; // Coordinates for table rows and columnd used for detemining width and height of image placed in mPager
    String[] godinaStudija;  // List for spinner
    static SharedPreferences prefs; // Shared preferneces
    int pdfCount = 1; // Amount of pages in PDF file
    int currentPDFPage = 1;
    static String lineColor = "ff000000";
    String taskResult;
    Tracker mTracker;
    CustomSwipeToRefresh mRefresh;
    Toolbar mToolbar;
    FloatingActionButton nextPage, prevPage;
    FrameLayout easterEgg;
    Handler mHandler;
    static ArrayList<tableColumn> columns;

    int day;
    boolean executePost = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        prefs = getSharedPreferences("com.idiotnation.raspored", MODE_PRIVATE);
        setTheme(prefs.getInt("CurrentTheme", R.style.AppTheme_Light));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        additionalProperties();
        godinaStudija = getResources().getStringArray(R.array.godine_array);
        try {
            addCustomActionBar();
            addErrorMeassagePreview();
            addInfoMeassagePreview();
            addPager();
            addFrameView();
            onConfigurationChanged(getResources().getConfiguration());
            if (prefs.getBoolean("FirstRun", false) && prefs.getInt("SpinnerDefault", -1) != -1) {
                checkIfHasContent();
            } else {
                listAvailableUrls(false);
                prefs.edit().putBoolean("FirstRun", true).apply();
                infoMessage.setVisibility(View.VISIBLE);
            }
            if (prefs.getBoolean("AutoUpdate", false)) {
                startService(new Intent(this, AutoUpdateService.class));
            } else {
                stopService(new Intent(this, AutoUpdateService.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mFrame.setVisibility(View.VISIBLE);
            mPager.setVisibility(View.GONE);
            mPager.setClickable(false);
            reloadRaspored();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mFrame.setVisibility(View.GONE);
            mPager.setVisibility(View.VISIBLE);
            mPager.setClickable(true);
            reloadRaspored();
        }
    }

    // Initialazing mPager
    public void addPager() {
        setDayOfWeek();
        sati = (ImageView) findViewById(R.id.sati);
        mPager = (ViewPager) findViewById(R.id.pager);
        mAdapter = new MyPagerAdapter(getSupportFragmentManager());
        if (mAdapter != null) {
            mPager.setAdapter(mAdapter);
        }
        mPager.setOffscreenPageLimit(6);
        mPager.setCurrentItem(day);
    }

    public void addErrorMeassagePreview() {
        errorMessage = (TextView) findViewById(R.id.err_msg);
    }

    public void addInfoMeassagePreview() {
        infoMessage = (TextView) findViewById(R.id.info_msg);
    }

    // Initialize async task
    class downloadTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            startAnimation();
        }

        @Override
        protected Void doInBackground(Void... params) {
            startDownload();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (executePost) {
                getPdfImg(executePost);
                mToolbar.setTitle(prefs.getString("UpdateDate", "ažuriraj"));
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("Action")
                        .setAction("Refresh")
                        .build());
            } else {
                if (!new File(getFilesDir().getAbsolutePath() + "/raspored.json").exists() && bmp != null) {
                    getColumns();
                }
                stopAnimation(View.INVISIBLE);
            }
            Toast.makeText(getApplicationContext(), taskResult, Toast.LENGTH_SHORT).show();
        }
    }

    // Initialazing mFrame
    public void addFrameView() {
        mFrame = (FrameLayout) findViewById(R.id.land_view);
    }

    // Opens Menu when you press menu key
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    // Selection handler for menu
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.ab_settings:
                item.setEnabled(false);
                SettingsDialog settingsDialog = new SettingsDialog(MainActivity.this, rasporedUrls);
                settingsDialog.show();
                settingsDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        item.setEnabled(true);
                        if (SettingsDialog.doRefresh) {
                            SettingsDialog.doRefresh = false;
                            checkDownload();
                        }
                    }
                });
                settingsDialog.setOnEggListener(new SettingsDialog.onEggsterListener() {
                    @Override
                    public void onEgg() {
                        easterEgg = (FrameLayout) findViewById(R.id.easter_egg_bg);
                        easterEgg.setVisibility(View.VISIBLE);
                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                        v.vibrate(1000);
                        Runnable clickList = new Runnable() {
                            @Override
                            public void run() {
                                easterEgg.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        easterEgg.setVisibility(View.GONE);
                                    }
                                });
                            }
                        };
                        mHandler.postDelayed(clickList, 5000);
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    // Options menu next
    public void OptonsNext() {
        if (currentPDFPage < pdfCount) {
            currentPDFPage++;
        } else {
            currentPDFPage = 1;
        }
        prefs.edit().putInt("currentPDFPage", currentPDFPage).apply();
        if (pdfCount > 1) {
            getPdfImg(false);
        }
    }

    // Options menu previous
    public void OptonsPrevious() {
        if (currentPDFPage > 1) {
            currentPDFPage--;
        } else {
            currentPDFPage = pdfCount;
        }
        prefs.edit().putInt("currentPDFPage", currentPDFPage).apply();
        if (pdfCount > 1) {
            getPdfImg(false);
        }
    }

    public void internetErrorMessage() {
        if (!new File(getFilesDir().getAbsolutePath() + "/raspored.pdf").exists()) {
            stopAnimation(View.VISIBLE);
        } else {
            stopAnimation(View.INVISIBLE);
            Toast.makeText(getApplicationContext(), "Niste povezani s internetom", Toast.LENGTH_SHORT).show();
        }
    }

    // Lists all avaliable URLs from Web Page
    public void listAvailableUrls(final boolean checkDownload) {
        startAnimation();
        ConnectionHelper connectionHelper = new ConnectionHelper(getApplicationContext());
        connectionHelper.setFinishListener(new ConnectionHelper.finishListener() {
            @Override
            public void onFinish(boolean isConnectionEstablished) {
                if (isConnectionEstablished) {
                    try {
                        rasporedUrls = new DegreeLoader().getDegrees();
                        if(checkDownload){
                            checkDownload();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            stopAnimation(View.INVISIBLE);
                            if (!new File(getFilesDir().getAbsolutePath() + "/raspored.pdf").exists()) {
                                infoMessage.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            internetErrorMessage();
                        }
                    });
                }
            }
        });
        connectionHelper.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    // Sets page for mPager by day of week
    public void setDayOfWeek() {
        day = Calendar.getInstance(TimeZone.getTimeZone("Europe/Sarajevo")).get(Calendar.DAY_OF_WEEK) - 2;
        if (day == -1) {
            day = 5;
        }
        if (day == -2) {
            day = 6;
        }
    }

    public boolean checkForUpdate(String id) {
        if (prefs.getString("CurrentRasporedId", "NN").equals(id) && new File(getFilesDir().getAbsolutePath() + "/raspored.pdf").exists()) {
            return false;
        } else {
            return true;
        }
    }

    // Used for checking wether PDF is downloaded or not
    public void checkIfHasContent() {
        if (rasporedUrls.size() < 10) {
            listAvailableUrls(true);
        }
        if (new File(getFilesDir().getAbsolutePath() + "/raspored.pdf").exists()) {
            getPdfImg(!new File(getFilesDir().getAbsolutePath() + "/raspored.json").exists());
        }
    }

    public void checkDownload() {
        startAnimation();
        ConnectionHelper connectionHelper = new ConnectionHelper(getApplicationContext());
        connectionHelper.setFinishListener(new ConnectionHelper.finishListener() {
            @Override
            public void onFinish(boolean isConnectionEstablished) {
                if (isConnectionEstablished) {
                    new downloadTask().execute();
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            internetErrorMessage();
                        }
                    });
                }
            }
        });
        connectionHelper.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void stopAnimation(int visibility) {
        errorMessage.setVisibility(visibility);
        mRefresh.post(new Runnable() {
            @Override
            public void run() {
                mRefresh.setRefreshing(false);
            }
        });
    }

    public void startAnimation() {
        if (!mRefresh.isRefreshing()) {
            mRefresh.post(new Runnable() {
                @Override
                public void run() {
                    mRefresh.setRefreshing(true);
                }
            });
        }
    }

    // Some additional properties needed for some classes
    public void additionalProperties() {
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName(Build.MANUFACTURER + " " + Build.MODEL);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        mHandler = new Handler();
    }

    public void startDownload() {
        executePost = true;
        taskResult = "";
        if (rasporedUrls.size() >= 10) {
            if (checkForUpdate(getGDiskId(rasporedUrls.get(prefs.getInt("SpinnerDefault", 0))))) {
                downloadPdfContent("https://docs.google.com/uc?export=download&id=" + getGDiskId(rasporedUrls.get(prefs.getInt("SpinnerDefault", 0))));
                prefs.edit().putString("UpdateDate", new SimpleDateFormat("dd.MM.yyyy").format(new Date())).apply();
                prefs.edit().putString("CurrentRasporedId", getGDiskId(rasporedUrls.get(prefs.getInt("SpinnerDefault", 0)))).apply();
                taskResult = "Preuzimanje dovršeno";
            } else {
                executePost = false;
                taskResult = "Raspored nije izmjenjen";
            }
        } else {
            taskResult = "Pokušajte ponovno doslo je do greške";
        }
    }

    // Initialazing custom Actionbar view and addind elements to it
    public void addCustomActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.action_bar);
        mToolbar.setTitleTextColor(getResources().getColor(android.R.color.white));
        mToolbar.setTitle(prefs.getString("UpdateDate", "Raspored"));
        setSupportActionBar(mToolbar);
        mRefresh = (CustomSwipeToRefresh) findViewById(R.id.refresh_layout);
        mRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (prefs.getInt("SpinnerDefault", -1) != -1) {
                    listAvailableUrls(true);
                } else {
                    Toast.makeText(MainActivity.this, "Odaberite godinu studija", Toast.LENGTH_SHORT).show();
                    stopAnimation(errorMessage.getVisibility());
                }
            }
        });
        mRefresh.setColorScheme(R.color.blue, R.color.green, R.color.red, R.color.orange);
        nextPage = (FloatingActionButton) findViewById(R.id.menu_next);
        nextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OptonsNext();
            }
        });
        prevPage = (FloatingActionButton) findViewById(R.id.menu_prev);
        prevPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OptonsPrevious();
            }
        });
    }

    // Used for redirecting URL
    public static String getFinalURL(String url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setInstanceFollowRedirects(false);
        con.connect();
        con.getInputStream();

        if (con.getResponseCode() == HttpURLConnection.HTTP_MOVED_PERM || con.getResponseCode() == HttpURLConnection.HTTP_MOVED_TEMP) {
            String redirectUrl = con.getHeaderField("Location");
            return getFinalURL(redirectUrl);
        }
        return url;
    }

    // Checking if PDF file is downloaded
    public void checkIfPdfExists() {
        if (new File(getFilesDir().getAbsolutePath() + "/raspored.pdf").exists()) {
            new File(getFilesDir().getAbsolutePath() + "/raspored.pdf").delete();
        }
    }

    // Method for downloading PDF content
    public void downloadPdfContent(String urlToDownload) {
        try {
            checkIfPdfExists();

            String fileName = "raspored.pdf";

            // download pdf file.
            URL url = new URL(getFinalURL(urlToDownload));
            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(false);
            c.connect();
            File file = getFilesDir();
            file.mkdirs();
            FileOutputStream fos = openFileOutput(fileName.toString(), MODE_PRIVATE);
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

    // Adapter for mPager
    public class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Fragment getItem(int i) {
            return new ImageFragment(i);
        }

        @Override
        public int getCount() {
            return 6;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "OBJECT " + (position + 1);
        }
    }

    // Reloads images in mPager
    public void reloadRaspored() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    // Converts PDF to image (.png)
    public void getPdfImg(final boolean getLines) {
        try {
            PDF pdf = new PDF(getApplicationContext(), prefs.getInt("currentPDFPage", 1));
            pdf.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            pdf.setFinishListener(new finishListener() {
                @Override
                public void onFinish(Bitmap result) {
                    bmp = createInvertedBitmap(result);
                    if (getLines) {
                        getColumns();
                    }
                    loadJson();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            mFrame.setBackgroundDrawable(new BitmapDrawable(Bitmap.createBitmap(bmp, columns.get(0).getX(), columns.get(0).getY(), columns.get(columns.size() - 1).getX() - columns.get(0).getX() + columns.get(columns.size() - 1).getWidth(), columns.get(columns.size() - 1).getY() - columns.get(0).getY() + columns.get(columns.size() - 1).getHeight())));
                            reloadRaspored();
                            stopAnimation(View.INVISIBLE);
                            infoMessage.setVisibility(View.INVISIBLE);
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            stopAnimation(View.VISIBLE);
        }
    }

    public void getColumns() {
        if (bmp != null) {
            getTableLines();
            columns = new ArrayList<>();
            for (int i = 0; i < 7; i++) {
                Point topLeftCorner = new Point(xs.get(i), ys.get(0));
                int wThickness = 0;
                if (i != 0) {
                    wThickness = getLineThickness(new Point(xs.get(i + 1), ys.get(0) + 20), true);
                }
                int hThickness = getLineThickness(new Point(xs.get(0) + 20, ys.get(ys.size() - 1)), false);
                int columnWidth = (xs.get(i + 1) - xs.get(i)) + wThickness;
                int columnHeight = (ys.get(ys.size() - 1) - ys.get(0)) + hThickness;
                columns.add(new tableColumn(topLeftCorner.x, topLeftCorner.y, columnWidth, columnHeight));
            }
            saveJson();
        }
    }

    /*public boolean validateBitmap(Bitmap bmp) throws Exception {
        if (bmp != null) {
            List<Integer> colorCache = new ArrayList<>();
            List<Integer> cordCache = new ArrayList<>();
            for (int i = 0; i < bmp.getWidth(); i++) {
                if (Integer.toHexString(bmp.getPixel(i, bmp.getHeight() / 2)).equals("ff000000")) {
                    for (int j = 0; j < bmp.getHeight() / 2; j++) {
                        if (Integer.toHexString(bmp.getPixel(i, bmp.getHeight() / 2 + j)).equals("ff000000")) {
                            colorCache.add(i);
                        } else {
                            colorCache.clear();
                            break;
                        }
                        if (colorCache.size() > 100) {
                            cordCache.add(i);
                            while (Integer.toHexString(bmp.getPixel(i, bmp.getHeight() / 2 + j)).equals("ff000000")) {
                                i++;
                            }
                            colorCache.clear();
                            break;
                        }
                    }
                }
            }
            if (cordCache.size() >= 8) {
                return false;
            }
        } else {
            prefs.edit().putInt("currentPDFPage", 1).apply();
        }
        return true;
    }*/

    public void getTableLines() {
        xs = new ArrayList<>();
        ys = new ArrayList<>();
        if (bmp != null) {
            ArrayList<Integer> wCache = new ArrayList<>();
            wCache.clear();
            for (int i = 0; i < bmp.getWidth(); i++) {
                if (Integer.toHexString(bmp.getPixel(i, bmp.getHeight() / 2)).equals(lineColor)) {
                    for (int j = 0; j < bmp.getHeight() / 2; j++) {
                        if (Integer.toHexString(bmp.getPixel(i, bmp.getHeight() / 2 + j)).equals(lineColor)) {
                            wCache.add(i);
                        } else {
                            wCache.clear();
                            break;
                        }
                        if (wCache.size() > 100) {
                            xs.add(i);
                            while (Integer.toHexString(bmp.getPixel(i, bmp.getHeight() / 2 + j)).equals(lineColor)) {
                                i++;
                            }
                            wCache.clear();
                            break;
                        }
                    }
                }
            }
            wCache.clear();
            for (int i = 0; i < bmp.getHeight(); i++) {
                if (Integer.toHexString(bmp.getPixel(xs.get(0) + 20, i)).equals(lineColor)) {
                    for (int j = 0; j < bmp.getWidth() / 2; j++) {
                        if (Integer.toHexString(bmp.getPixel(xs.get(0) + j, i)).equals(lineColor)) {
                            wCache.add(i);
                        } else {
                            wCache.clear();
                            break;
                        }
                        if (wCache.size() > 100) {
                            ys.add(i);
                            while (Integer.toHexString(bmp.getPixel(xs.get(0) + j, i)).equals(lineColor)) {
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

    public int getLineThickness(Point coord, boolean ori) {
        int lineOffset = 0;
        if ((Integer.toHexString(bmp.getPixel(coord.x - 1, coord.y)).equals(lineColor) && Integer.toHexString(bmp.getPixel(coord.x + 10, coord.y)).equals(lineColor)) || Integer.toHexString(bmp.getPixel(coord.x, coord.y - 1)).equals(lineColor)) {
            lineOffset = 10;
        } else {
            lineOffset = 0;
        }
        for (int i = 0; i <= 20; i++) {
            if (ori) {
                if (!Integer.toHexString(bmp.getPixel(coord.x + i, coord.y + lineOffset)).equals(lineColor)) {
                    return i;
                }
            } else {
                if (!Integer.toHexString(bmp.getPixel(coord.x + lineOffset, coord.y + i)).equals(lineColor)) {
                    return i;
                }
            }
        }
        return 3;
    }

    public void saveJson() {
        try {
            FileOutputStream fos = openFileOutput("raspored.json", MODE_PRIVATE);
            fos.write(new Gson().toJson(columns).getBytes());
            fos.close();
        } catch (Exception e) {
        }
    }

    public void loadJson() {
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(getFilesDir(), "raspored.json")));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        } catch (IOException e) {
        }
        columns = new Gson().fromJson(text.toString(), new TypeToken<ArrayList<tableColumn>>() {
        }.getType());
    }

    private static Bitmap createInvertedBitmap(Bitmap src) {
        if (prefs.getBoolean("NightMode", false)) {
            lineColor = "ffffffff";
            ColorMatrix colorMatrix_Inverted =
                    new ColorMatrix(new float[]{
                            -1, 0, 0, 0, 255,
                            0, -1, 0, 0, 255,
                            0, 0, -1, 0, 255,
                            0, 0, 0, 1, 0});
            ColorFilter ColorFilter_Sepia = new ColorMatrixColorFilter(
                    colorMatrix_Inverted);
            Bitmap bitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            paint.setColorFilter(ColorFilter_Sepia);
            canvas.drawBitmap(src, 0, 0, paint);
            return bitmap;
        } else {
            lineColor = "ff000000";
            return src;
        }
    }

    // Image fragment which is used in mPager for holding week days
    public static class ImageFragment extends Fragment {
        int b;

        public ImageFragment() {
        }

        @SuppressLint("ValidFragment")
        public ImageFragment(int i) {
            b = i + 1;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(
                    R.layout.fragment_layout, container, false);
            ImageView imageView = (ImageView) rootView.findViewById(R.id.column);
            if (columns != null && bmp != null) {
                if (b == 1) {
                    sati.setImageBitmap(Bitmap.createBitmap(bmp, columns.get(0).getX(), columns.get(0).getY(), columns.get(0).getWidth(), columns.get(0).getHeight()));
                }
                imageView.setImageBitmap(Bitmap.createBitmap(bmp, columns.get(b).getX(), columns.get(b).getY(), columns.get(b).getWidth(), columns.get(b).getHeight()));
            }
            return rootView;
        }
    }

    class PDF extends AsyncTask<Void, Void, Void> {

        Context context;
        finishListener finishListener;
        int pageNum;

        public PDF(Context context, int pageNum) {
            this.context = context;
            this.pageNum = pageNum - 1;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                PdfiumCore pdfiumCore = new PdfiumCore(context);
                PdfDocument pdfDocument = pdfiumCore.newDocument(getSeekableFileDescriptor(getFilesDir().getAbsolutePath() + "/raspored.pdf"));
                pdfCount = pdfiumCore.getPageCount(pdfDocument);
                pageNum = pageNum >= pdfCount ? 0 : pageNum;

                pdfiumCore.openPage(pdfDocument, pageNum);

                int width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNum) * 3;
                int height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNum) * 3;

                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                pdfiumCore.renderPageBitmap(pdfDocument, bitmap, pageNum, 0, 0, width, height);

                Bitmap cpy = bitmap.copy(Bitmap.Config.RGB_565, false);
                bitmap.recycle();
                bitmap = cpy;

                pdfiumCore.closeDocument(pdfDocument);
                finishListener.onFinish(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        public void setFinishListener(finishListener finishListener) {
            this.finishListener = finishListener;
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

    }

    public interface finishListener {
        void onFinish(Bitmap result);
    }

    public class tableColumn {
        int x, y;
        int width, height;

        public tableColumn(int x, int y, int width, int height) {
            this.height = height;
            this.x = x;
            this.y = y;
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        @Override
        public String toString() {
            return "tableColumn[x=" + x + ",y=" + y + ",width=" + width + ",height=" + height + "]";
        }
    }

    public class tableCell {

    }
}
