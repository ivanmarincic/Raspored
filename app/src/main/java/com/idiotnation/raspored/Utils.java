package com.idiotnation.raspored;

public class Utils {

    public static String getGDiskId(String url) {

        String output = "NN";
        int slash = 0;
        if (url.length() >= 32) {
            for (int i = 32; i < url.length(); i++) {
                if (url.substring(i, (i + 1)).equals("/")) {
                    slash = i;
                    break;
                }
            }
            output = url.substring(32, slash);
        }
        return output;
    }
}
