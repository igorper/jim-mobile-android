package net.pernek.jim.exercisedetector.entities;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.google.gson.Gson;

public class Measurement {

	public int training_id;

	public Date start_time;

	public Date end_time;

	public int rating;

	public String comment;

	public List<SeriesExecution> series_executions;
}
