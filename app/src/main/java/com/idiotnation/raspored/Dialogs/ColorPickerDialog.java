package com.idiotnation.raspored.Dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.idiotnation.raspored.R;

public class ColorPickerDialog extends Dialog implements SeekBar.OnSeekBarChangeListener {

    public Activity c;
    public Dialog d;

    View colorView;
    SeekBar redSeekBar, greenSeekBar, blueSeekBar, alphaSeekbar;
    TextView redToolTip, greenToolTip, blueToolTip, alphaToolTip;
    EditText codHex;
    Button select;
    onFinishListener onFinishListener;
    private int red, green, blue, alpha;


    /**
     * Creator of the class. It will initialize the class with black color as default
     *
     * @param a The reference to the activity where the color picker is called
     */
    public ColorPickerDialog(Activity a) {
        super(a);

        this.c = a;
        this.alpha = 0;
        this.red = 0;
        this.green = 0;
        this.blue = 0;
    }


    /**
     * Creator of the class. It will initialize the class with the rgb color passed as default
     *
     * @param activity The reference to the activity where the color picker is called
     * @param a        Alpha value for RGB values (0 - 255)
     * @param r        Red color for RGB values (0 - 255)
     * @param g        Green color for RGB values (0 - 255)
     * @param b        Blue color for RGB values (0 - 255)
     *                 <p>
     *                 If the value of the colors it's not in the right range (0 - 255) it will be place at 0.
     */
    public ColorPickerDialog(Activity activity, int a, int r, int g, int b) {
        super(activity);

        this.c = activity;

        if (0 <= r && r <= 255)
            this.red = r;
        else
            this.red = 0;

        if (0 <= g && g <= 255)
            this.green = g;
        else
            this.green = 0;

        if (0 <= b && b <= 255)
            this.blue = b;
        else
            this.green = 0;

    }

    /**
     * Creator of the class. It will initialize the class with the rgb color passed as default
     *
     * @param a The reference to the activity where the color picker is called
     * @param c Color for RGB values (0 - 255)
     *          <p>
     *          If the value of the colors it's not in the right range (0 - 255) it will be place at 0.
     */
    public ColorPickerDialog(Activity a, int c) {
        super(a);
        this.c = a;

        if (0 <= Color.alpha(c) && Color.alpha(c) <= 255)
            this.alpha = Color.alpha(c);
        else
            this.alpha = 0;

        if (0 <= Color.red(c) && Color.red(c) <= 255)
            this.red = Color.red(c);
        else
            this.red = 0;

        if (0 <= Color.green(c) && Color.green(c) <= 255)
            this.green = Color.green(c);
        else
            this.green = 0;

        if (0 <= Color.blue(c) && Color.blue(c) <= 255)
            this.blue = Color.blue(c);
        else
            this.green = 0;
    }

    /**
     * Simple onCreate function. Here there is the init of the GUI.
     *
     * @param savedInstanceState As usual ...
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.color_picker_dialog);

        select = (Button) findViewById(R.id.okColorButton);

        colorView = findViewById(R.id.colorView);

        alphaSeekbar = (SeekBar) findViewById(R.id.alphaSeekBar);
        redSeekBar = (SeekBar) findViewById(R.id.redSeekBar);
        greenSeekBar = (SeekBar) findViewById(R.id.greenSeekBar);
        blueSeekBar = (SeekBar) findViewById(R.id.blueSeekBar);

        alphaToolTip = (TextView) findViewById(R.id.alphaToolTip);
        redToolTip = (TextView) findViewById(R.id.redToolTip);
        greenToolTip = (TextView) findViewById(R.id.greenToolTip);
        blueToolTip = (TextView) findViewById(R.id.blueToolTip);

        codHex = (EditText) findViewById(R.id.codHex);

        alphaSeekbar.setOnSeekBarChangeListener(this);
        redSeekBar.setOnSeekBarChangeListener(this);
        greenSeekBar.setOnSeekBarChangeListener(this);
        blueSeekBar.setOnSeekBarChangeListener(this);

        alphaSeekbar.setProgress(alpha);
        redSeekBar.setProgress(red);
        greenSeekBar.setProgress(green);
        blueSeekBar.setProgress(blue);

        codHex.setTextColor((0xFFFFFF - Color.argb(alpha, red, green, blue)) | 0xFF000000);
        colorView.setBackgroundColor(Color.argb(alpha, red, green, blue));
        codHex.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                                actionId == EditorInfo.IME_ACTION_DONE ||
                                event.getAction() == KeyEvent.ACTION_DOWN &&
                                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                            updateColorView(v.getText().toString());
                            return true;
                        }
                        return false;
                    }
                });
        codHex.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isColorValid() && s.length() == 8) {
                    updateColorView(s.toString());
                }
                if (s.length() > 8) {
                    codHex.setError("Neispravna vrijednost");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        codHex.setText(String.format("%02x%02x%02x%02x", alpha, red, green, blue));

        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isColorValid()) {
                    dismiss();
                    onFinishListener.onFinish(getColor());
                } else {
                    codHex.setError("Neispravna vrijednost");
                }
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        onFinishListener.onFinish(getColor());
    }


    /**
     * Method that syncrhonize the color between the bars, the view and the HEC code text.
     *
     * @param s HEX Code of the color.
     */
    private void updateColorView(String s) {
        try {
            int color = Color.parseColor("#" + s);
            alpha = Color.alpha(color);
            red = Color.red(color);
            green = Color.green(color);
            blue = Color.blue(color);

            codHex.setTextColor((0xFFFFFF - Color.argb(alpha, red, green, blue)) | 0xFF000000);
            colorView.setBackgroundColor(Color.argb(alpha, red, green, blue));
            alphaSeekbar.setProgress(alpha);
            redSeekBar.setProgress(red);
            greenSeekBar.setProgress(green);
            blueSeekBar.setProgress(blue);
        } catch (IllegalArgumentException e) {
            codHex.setError("Neispravna vrijednost");
        }
    }

    /**
     * Method called when the user change the value of the bars. This sync the colors.
     *
     * @param seekBar  SeekBar that has changed
     * @param progress The new progress value
     * @param fromUser If it come from User
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        if (seekBar.getId() == R.id.alphaSeekBar) {

            alpha = progress;

            alphaToolTip.setX(seekBar.getPaddingLeft());
            alphaToolTip.setText("" + alpha);

        }

        if (seekBar.getId() == R.id.redSeekBar) {

            red = progress;

            redToolTip.setX(seekBar.getPaddingLeft());
            redToolTip.setText("" + red);

        } else if (seekBar.getId() == R.id.greenSeekBar) {

            green = progress;

            greenToolTip.setX(seekBar.getPaddingLeft());
            greenToolTip.setText("" + green);

        } else if (seekBar.getId() == R.id.blueSeekBar) {

            blue = progress;

            blueToolTip.setX(seekBar.getPaddingLeft());
            blueToolTip.setText("" + blue);

        }

        codHex.setTextColor((0xFFFFFF - Color.argb(alpha, red, green, blue)) | 0xFF000000);
        colorView.setBackgroundColor(Color.argb(alpha, red, green, blue));

        codHex.setText(String.format("%02x%02x%02x%02x", alpha, red, green, blue));

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    /**
     * Getter for the Alpha value of the RGB selected color
     *
     * @return Alpha Value Integer (0 - 255)
     */
    public String getAlpha() {
        return Long.toHexString(this.alpha);
    }


    /**
     * Getter for the RED value of the RGB selected color
     *
     * @return RED Value Integer (0 - 255)
     */
    public String getRed() {
        return Long.toHexString(this.red);
    }

    /**
     * Getter for the GREEN value of the RGB selected color
     *
     * @return GREEN Value Integer (0 - 255)
     */
    public String getGreen() {
        return Long.toHexString(this.green);
    }


    /**
     * Getter for the BLUE value of the RGB selected color
     *
     * @return BLUE Value Integer (0 - 255)
     */
    public String getBlue() {
        return Long.toHexString(this.blue);
    }

    /**
     * Getter for the color as Android Color class value.
     * <p>
     * From Android Reference: The Color class defines methods for creating and converting color ints.
     * Colors are represented as packed ints, made up of 4 bytes: alpha, red, green, blue.
     * The values are unpremultiplied, meaning any transparency is stored solely in the alpha
     * component, and not in the color components.
     *
     * @return Selected color as Android Color class value.
     */
    public int getColor() {
        return Color.parseColor("#" + codHex.getText());
    }

    public boolean isColorValid() {
        try {
            Color.parseColor("#" + codHex.getText());
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    public void setOnFinishListener(onFinishListener onFinishListener) {
        this.onFinishListener = onFinishListener;
    }

    public interface onFinishListener {
        void onFinish(int color);
    }

}