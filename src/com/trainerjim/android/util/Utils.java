package com.trainerjim.android.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.os.Environment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Utils {

    /**
     * Object for (de)seralization of json data. Date format is set to include timezone information.
     */
    private static Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();

	public static String getApplicationTag() {
		return "MainActivity";
	}

	public static String getDataFolder() {
		return "jimdata";
	}

	public static File getDataFolderFile(Context context) {
		File dataFolder = new File(context.getFilesDir(),
				Utils.getDataFolder());
		dataFolder.mkdirs();
		return dataFolder;
	}

	public static File getAccelerationFile(String outputFile, Context context) {
		return new File(getDataFolderFile(context), outputFile);
	}

	public static File getInterpolatedAccelerationFile(String outputFile, Context context) {
		return new File(getDataFolderFile(context),
				getInterpolationFileName(outputFile));
	}

	public static File getTrainingManifestFile(String outputFile, Context context) {
		return new File(getDataFolderFile(context),
				getTrainingManifestFileName(outputFile));
	}
	
	public static File getTimestampsFile(String outputFile, Context context) {
		return new File(getDataFolderFile(context),
				getTimestampsFileName(outputFile));
	}

	public static String getTrainingManifestFileName(String outputFile) {
		return outputFile + "_exercises";
	}

	public static String getInterpolationFileName(String outputFile) {
		return outputFile + "i";
	}

	public static String getTimestampsFileName(String outputFile){
		return outputFile + "_tstmps";
	}
	
	public static String generateFileName() {
		return new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar
				.getInstance().getTime());
	}

    /**
     * Returns the json (de)serialization object that can be used throught the application. The object
     * is initialized so that the whole application uses the object with the same settings.
     * @return
     */
    public static Gson getGsonObject(){
        return mGson;
    }
}
