package com.idiotnation.raspored;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class SettingsDialog extends Dialog {

    Activity activity;
    TextView easterEgg;
    CheckBox autoUpdate, nightMode;
    ImageView gitHub, tipButton;
    Spinner godineSpinner;
    SharedPreferences prefs;
    onEggsterListener eggsterListener;
    onFinishListner finishListner;
    int egg = 0, newPosition;

    public SettingsDialog(Activity activity) {
        super(activity);
        this.activity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_dialog);
        init();
        properties();
    }

    public void init() {
        godineSpinner = (Spinner) findViewById(R.id.spinner);
        autoUpdate = (CheckBox) findViewById(R.id.auto_update);
        nightMode = (CheckBox) findViewById(R.id.night_mode);
        easterEgg = (TextView) findViewById(R.id.easter_egg);
        gitHub = (ImageView) findViewById(R.id.gitButton);
        tipButton = (ImageView) findViewById(R.id.tipButton);
        prefs = activity.getSharedPreferences("com.idiotnation.raspored", MODE_PRIVATE);
    }

    public void properties() {
        autoUpdate.setChecked(prefs.getBoolean("AutoUpdate", false));
        nightMode.setChecked(prefs.getBoolean("DarkMode", false));
        if (nightMode.isChecked()) {
            gitHub.setImageBitmap(Utils.createInvertedBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.git), true));
            tipButton.setImageBitmap(Utils.createInvertedBitmap(BitmapFactory.decodeResource(activity.getResources(), R.drawable.tip), true));
        }
        autoUpdate.setVisibility(View.VISIBLE);
        nightMode.setVisibility(View.VISIBLE);
        autoUpdate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("AutoUpdate", isChecked).apply();
                activity.startService(new Intent(activity.getApplicationContext(), AutoUpdateService.class));
            }
        });
        nightMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                prefs.edit().putBoolean("DarkMode", isChecked).apply();
                if (isChecked) {
                    prefs.edit().putInt("CurrentTheme", R.style.AppTheme_Dark).apply();
                } else {
                    prefs.edit().putInt("CurrentTheme", R.style.AppTheme_Light).apply();
                }
                activity.finish();
                activity.startActivity(activity.getIntent());
            }
        });
        final ArrayAdapter<String> dataAdapter = new SpinnerArrayAdapter(activity.getApplicationContext(), R.layout.spinner_selected_item, activity.getResources().getStringArray(R.array.godine_array));
        godineSpinner.setAdapter(dataAdapter);
        godineSpinner.setSelection(prefs.getInt("SpinnerDefault", 0) + ((prefs.getInt("SpinnerDefault", 0) / 6) + 1));
        godineSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                newPosition = position - ((position / 6) + 1);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        gitHub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ivanmarincic/raspored"));
                activity.startActivity(browserIntent);
            }
        });
        easterEgg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                egg++;
                if (egg == 8) {
                    eggsterListener.onEgg();
                    dismiss();
                }
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        finishListner.onFinish(newPosition);
    }

    public class SpinnerArrayAdapter extends ArrayAdapter<String> {

        String[] items;
        List headers;

        public SpinnerArrayAdapter(Context context, int resource, String[] items) {
            super(context, resource, items);
            this.items = items;
            headers = new ArrayList(Arrays.asList(0, 6, 12));
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_selected_item, null);
            }
            int[] attrs = {R.attr.textColorPrimary};
            TypedArray ta = activity.getApplicationContext().obtainStyledAttributes(prefs.getInt("CurrentTheme", R.style.AppTheme_Light), attrs);
            TextView tv = (TextView) convertView.findViewById(R.id.ssi_item);
            tv.setText(getHeader(position) + items[position]);
            tv.setTextColor(ta.getColor(0, Color.BLACK));
            return convertView;
        }

        @Override
        public boolean isEnabled(int position) {
            return !headers.contains(position);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_item, null);
            }
            int[] attrs = {headers.contains(position) ? R.attr.textColorSecondary : R.attr.textColorPrimary};
            TypedArray ta = activity.getApplicationContext().obtainStyledAttributes(prefs.getInt("CurrentTheme", R.style.AppTheme_Light), attrs);
            TextView tv = (TextView) convertView.findViewById(R.id.si_item);
            tv.setText(items[position]);
            tv.setTextColor(ta.getColor(0, Color.BLACK));
            return convertView;
        }

        public String getHeader(int position) {
            if (position < 6) {
                return "S. ";
            } else if (position < 12) {
                return "R. ";
            } else if (position < 18) {
                return "E. ";
            }
            return "";
        }
    }

    public void setOnEggListener(onEggsterListener eggsterListener) {
        this.eggsterListener = eggsterListener;
    }

    public void setOnFinishListener(onFinishListner finishListener) {
        this.finishListner = finishListener;
    }

    interface onEggsterListener {
        void onEgg();
    }

    interface onFinishListner {
        void onFinish(int spinnerItem);
    }
}
