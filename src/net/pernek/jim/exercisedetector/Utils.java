package net.pernek.jim.exercisedetector;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Utils {
	
	public static String getApplicationTag(){
		return "MainActivity";
	}
	
	public static String getDataFolder(){
		return "jimdata";
	}
	
	public static String getUploadDataFolder(){
		return "jimdata/upload";
	}
	
	public static String generateFileName(){
		return new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
	}

}
