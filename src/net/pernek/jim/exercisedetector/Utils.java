package net.pernek.jim.exercisedetector;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.os.Environment;

public class Utils {

	public static String getApplicationTag() {
		return "MainActivity";
	}

	public static String getDataFolder() {
		return "jimdata";
	}

	public static File getFullDataFolder() {
		return new File(Environment.getExternalStorageDirectory(),
				Utils.getDataFolder());
	}

	public static String getUploadDataFolder() {
		return "jimdata/upload";
	}
	
	public static String getTrainingManifestFileName(String outputFileName){
		return outputFileName + "_exercises";
	}

	public static String generateFileName() {
		return new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar
				.getInstance().getTime());
	}

}
