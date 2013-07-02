package net.pernek.jim.exercisedetector.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.os.Environment;

public class Utils {

	public static String getApplicationTag() {
		return "MainActivity";
	}

	public static String getDataFolder() {
		return "jimdata";
	}

	public static File getDataFolderFile() {
		return new File(Environment.getExternalStorageDirectory(),
				Utils.getDataFolder());
	}
	
	public static File getUploadDataFolderFile() {
		return new File(Environment.getExternalStorageDirectory(),
				Utils.getUploadDataFolder());
	}

	public static File getAccelerationFile(String outputFile) {
		return new File(getDataFolderFile(), outputFile);
	}

	public static File getInterpolatedAccelerationFile(String outputFile) {
		return new File(getDataFolderFile(),
				getInterpolationFileName(outputFile));
	}

	public static File getTrainingManifestFile(String outputFile) {
		return new File(getDataFolderFile(),
				getTrainingManifestFileName(outputFile));
	}
	
	public static File getTimestampsFile(String outputFile) {
		return new File(getDataFolderFile(),
				getTimestampsFileName(outputFile));
	}

	public static String getUploadDataFolder() {
		return "jimdata/upload";
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
	
	public static String generateFileName(Date date) {
		return new SimpleDateFormat("yyyyMMddHHmmss").format(date);
	}

}
