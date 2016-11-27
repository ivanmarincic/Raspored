package com.idiotnation.raspored.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.idiotnation.raspored.R;
import com.idiotnation.raspored.Utils;
import com.idiotnation.raspored.Views.ColorSetupView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Context.MODE_PRIVATE;

public class SettingsDialog extends Dialog {

    Activity activity;
    SharedPreferences prefs;
    onEggsterListener eggsterListener;
    onFinishListner finishListner;
    int egg = 0, newPosition;
    boolean refreshNotifications;

    @BindView(R.id.spinner)
    Spinner godineSpinner;

    @BindView(R.id.updateOnBoot)
    SwitchCompat updateOnBoot;

    @BindView(R.id.notifications)
    SwitchCompat notificationsEnabled;

    @BindView(R.id.gitButton)
    ImageView gitHub;

    @BindView(R.id.easter_egg)
    TextView easterEgg;

    @BindView(R.id.updateOnBoot_text)
    TextView updateOnBootText;

    @BindView(R.id.notifications_text)
    TextView notificationsEnabledText;

    @BindView(R.id.setup_colors_text)
    TextView colorsText;

    @BindView(R.id.setup_colors)
    ImageView colorsImage;

    @BindView(R.id.setup_colors_container)
    RelativeLayout setupColors;

    @BindView(R.id.settings_dialog_bg)
    LinearLayout rootView;

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

    @Override
    protected void onStop() {
        super.onStop();
        finishListner.onFinish(newPosition, refreshNotifications);
    }

    public void init() {
        ButterKnife.bind(this);
        prefs = activity.getSharedPreferences("com.idiotnation.raspored", MODE_PRIVATE);
        setColors();
    }

    public void properties() {
        updateOnBoot.setChecked(prefs.getBoolean("UpdateOnBoot", false));
        notificationsEnabled.setChecked(prefs.getBoolean("NotificationsEnabled", false));
        updateOnBoot.setVisibility(View.VISIBLE);
        updateOnBoot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                prefs.edit().putBoolean("UpdateOnBoot", b).apply();
            }
        });
        notificationsEnabled.setVisibility(View.VISIBLE);
        notificationsEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                prefs.edit().putBoolean("NotificationsEnabled", b).apply();
                refreshNotifications = true;
            }
        });
        setupColors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.startActivity(new Intent(activity.getApplicationContext(), ColorSetupView.class));
            }
        });
        final ArrayAdapter<String> dataAdapter = new SpinnerArrayAdapter(activity.getApplicationContext(), R.layout.spinner_selected_item, activity.getResources().getStringArray(R.array.godine_array));
        godineSpinner.setAdapter(dataAdapter);
        godineSpinner.setSelection(prefs.getInt("SpinnerDefault", 0) + setNewPositionOffset(prefs.getInt("SpinnerDefault", 0)));
        godineSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                newPosition = position - getNewPositionOffset(position);
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

    public void setColors(){
        int[][] states = new int[][]{
                new int[]{-android.R.attr.state_checked},
                new int[]{android.R.attr.state_checked}
        };
        int[] colorsThumb= new int[]{
                Utils.getColor(R.color.textColorPrimary, activity),
                Utils.getColor(R.color.colorAccent, activity)
        };
        int[] colorsTrack = new int[]{
                Utils.manipulateAlpha(Utils.getColor(R.color.textColorPrimary, activity), 0.75f),
                Utils.manipulateAlpha(Utils.getColor(R.color.colorAccent, activity), 0.75f)
        };
        rootView.setBackgroundColor(Utils.getColor(R.color.windowBackgroundColor, activity));
        updateOnBoot.setThumbTintList(new ColorStateList(states, colorsThumb));
        updateOnBoot.setTrackTintList(new ColorStateList(states, colorsTrack));
        notificationsEnabled.setThumbTintList(new ColorStateList(states, colorsThumb));
        notificationsEnabled.setTrackTintList(new ColorStateList(states, colorsTrack));
        Drawable colorsDrawable = colorsImage.getDrawable();
        if (colorsDrawable != null) {
            colorsDrawable.mutate();
            colorsDrawable.setColorFilter(Utils.getColor(R.color.textColorPrimary, activity), PorterDuff.Mode.SRC_ATOP);
        }
        Drawable gitDrawable = gitHub.getDrawable();
        if (gitDrawable != null) {
            gitDrawable.mutate();
            gitDrawable.setColorFilter(Utils.getColor(R.color.textColorPrimary, activity), PorterDuff.Mode.SRC_ATOP);
        }
        easterEgg.setTextColor(Utils.getColor(R.color.textColorPrimary, activity));
        updateOnBootText.setTextColor(Utils.getColor(R.color.textColorPrimary, activity));
        notificationsEnabledText.setTextColor(Utils.getColor(R.color.textColorPrimary, activity));
        colorsText.setTextColor(Utils.getColor(R.color.textColorPrimary, activity));
    }

    public int getNewPositionOffset(int position){
        if(position<12){
            return 1;
        }else if(position<20){
            return 2;
        }else {
            return 3;
        }
    }

    public int setNewPositionOffset(int position){
        if(position<=10){
            return 1;
        }else if(position<=17){
            return 2;
        }else {
            return 3;
        }
    }

    public class SpinnerArrayAdapter extends ArrayAdapter<String> {

        String[] items;
        List headers;

        public SpinnerArrayAdapter(Context context, int resource, String[] items) {
            super(context, resource, items);
            this.items = items;
            headers = new ArrayList(Arrays.asList(0, 12, 20));
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_selected_item, null);
            }
            parent.setBackgroundColor(Utils.getColor(R.color.windowBackgroundColor, getContext()));
            TextView tv = (TextView) convertView.findViewById(R.id.ssi_item);
            tv.setText(getHeader(position) + items[position]);
            tv.setTextColor(Utils.getColor(R.color.textColorPrimary, getContext()));
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
            parent.setBackgroundColor(Utils.getColor(R.color.windowBackgroundColor, getContext()));
            TextView tv = (TextView) convertView.findViewById(R.id.si_item);
            tv.setText(items[position]);
            tv.setTextColor(Utils.getColor(R.color.textColorPrimary, getContext()));
            return convertView;
        }

        public String getHeader(int position) {
            if (position < 12) {
                return "S. ";
            } else if (position < 20) {
                return "R. ";
            } else {
                return "E. ";
            }
        }
    }

    public void setOnEggListener(onEggsterListener eggsterListener) {
        this.eggsterListener = eggsterListener;
    }

    public void setOnFinishListener(onFinishListner finishListener) {
        this.finishListner = finishListener;
    }

    public interface onEggsterListener {
        void onEgg();
    }

    public interface onFinishListner {
        void onFinish(int spinnerItem, boolean refreshNotifications);
    }
}
