package com.idiotnation.raspored;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class DatabaseConfigUtil extends OrmLiteConfigUtil {

    public static void main(String[] args) throws SQLException, IOException {
        File conffile = new File("F:/Workspace/Android/Raspored/app/src/main/res/raw/ormlite_config.txt");
        File searchdir = new File("F:\\Workspace\\Android\\Raspored\\app\\src\\main\\java\\com\\idiotnation\\raspored\\models\\db");
        writeConfigFile(conffile, searchdir);
    }
}