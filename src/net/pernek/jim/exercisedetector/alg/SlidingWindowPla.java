package net.pernek.jim.exercisedetector.alg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SlidingWindowPla implements Pla {
	
	private double mMaxError;
	private PrintWriter mOutputWriter;
	
	private List<Float> mBufferX = new ArrayList<Float>();
	private List<Float> mBufferY = new ArrayList<Float>();
	private List<Float> mBufferZ = new ArrayList<Float>();
	
	private List<SensorValue> mKeyValues = new ArrayList<SensorValue>();
	
	private SensorValue mLastSensorValue;
	
	private SlidingWindowPla() {}
	
	public static SlidingWindowPla create(double maxError){
		SlidingWindowPla retVal = new SlidingWindowPla();
		retVal.mMaxError = maxError;
				
		return retVal;
	}
	
	// while calculating PLA perform saving the values to a CSV (this CSV will be sent to a server)
	// create a field with the current file name in the preferences as soon as sampling is started
	// (when the app starts we first check if there is any value in preferences -> if there is we know
	// the app was sampling and we should continue sampling and saving in this file)
	// when the session is finished and the stop button is clicked the file name (flag) is deleted from preferences
	// and added to the database where all activities that have to be uploaded will be stored.
	@Override
	public void process(SensorValue newValue) {
		// first element should always be a key point
		if(mKeyValues.size() == 0){		
			mKeyValues.add(SensorValue.create(newValue.getSensorType(), 
					new Float[] {newValue.getValues()[0],
							newValue.getValues()[1],
							newValue.getValues()[2]}, 
							newValue.getTimestamp()));
		}
		
		// add values to buffer
		mBufferX.add(newValue.getValues()[0]);
		mBufferY.add(newValue.getValues()[1]);
		mBufferZ.add(newValue.getValues()[2]);
		
		// get regression error for buffer values
		double errorX = calculateSegmentError(mBufferX);
		double errorY = calculateSegmentError(mBufferY);
		double errorZ = calculateSegmentError(mBufferZ);
		
		boolean addX = false;
		boolean addY = false;
		boolean addZ = false;
		if(errorX > mMaxError){
			addX = true;

			mBufferX.clear();
			mBufferX.add(newValue.getValues()[0]);
		}
		
		if(errorY > mMaxError){
			addY = true;
		
			mBufferY.clear();
			mBufferY.add(newValue.getValues()[1]);
		}
		
		if(errorZ > mMaxError){
			addZ = true;
			
			mBufferZ.clear();
			mBufferZ.add(newValue.getValues()[2]);
		}
		
		// if a segment was created for any of the axis
		if(addX || addY || addZ){			
			SensorValue lastKeyPoint = mKeyValues.get(mKeyValues.size() - 1);
			SensorValue lastValue = mLastSensorValue;// mHistory.get(mHistory.size() - 1);
			boolean wasCorrected = false;
			
			// if the previous segment end element was already added in the previous step just update it
			if(lastKeyPoint.getTimestamp() == lastValue.getTimestamp()){
				Float[] values = new Float[] {
					(lastKeyPoint.getValues()[0] != null) ? lastKeyPoint.getValues()[0] : 
						(addX ? lastValue.getValues()[0] : null),
					(lastKeyPoint.getValues()[1] != null) ? lastKeyPoint.getValues()[1] : 
						(addY ? lastValue.getValues()[1] : null),
					(lastKeyPoint.getValues()[2] != null) ? lastKeyPoint.getValues()[2] : 
						(addZ ? lastValue.getValues()[2] : null)
				};
				wasCorrected = true;
				// updating is done by removing the element and creating a new one (immutable object)
				mKeyValues.remove(lastKeyPoint);
				mKeyValues.add(SensorValue.create(lastKeyPoint.getSensorType(), values, lastKeyPoint.getTimestamp()));
			} else {
				// otherwise add new end of the previous segment element
				mKeyValues.add(SensorValue.create(lastValue.getSensorType(), 
					new Float[] {addX ? lastValue.getValues()[0] : null,
								 addY ? lastValue.getValues()[1] : null,
								 addZ ? lastValue.getValues()[2] : null}, 
								lastValue.getTimestamp()));
			}
			
			// add element for the start of the new segment
			mKeyValues.add(SensorValue.create(newValue.getSensorType(), 
					new Float[] {addX ? newValue.getValues()[0] : null,
								 addY ? newValue.getValues()[1] : null,
								 addZ ? newValue.getValues()[2] : null}, 
									newValue.getTimestamp()));
			
			if(mOutputWriter != null){
				// add those two entries that for sure won't be changed anymore
				if(!wasCorrected) {
					// add this only if the last value was not edited
					// (maybe all this can be done a bit more elegantly)
					mOutputWriter.println(mKeyValues.get(mKeyValues.size() - 3).getCsvString());
				}
				mOutputWriter.println(mKeyValues.get(mKeyValues.size() - 2).getCsvString());
			}
		}
		
		mLastSensorValue = newValue;
	}
		
	private static double calculateSegmentError(List<Float> data){
		int n = data.size();
		double[] x = new double[n];
		double[] y = new double[n];
		
		double sumx = 0.0, sumy = 0.0;
        for(int i=0; i < n; i++) {
            x[i] = i;
            y[i] = data.get(i);
            sumx  += x[i];
            sumy  += y[i];
        }
        double xbar = sumx / n;
        double ybar = sumy / n;

        // second pass: compute summary statistics
        double xxbar = 0.0, xybar = 0.0;
        for (int i = 0; i < n; i++) {
            xxbar += (x[i] - xbar) * (x[i] - xbar);
            xybar += (x[i] - xbar) * (y[i] - ybar);
        }
        double beta1 = xybar / xxbar;
        double beta0 = ybar - beta1 * xbar;

        double rss = 0.0;      // residual sum of squares
        for (int i = 0; i < n; i++) {
            double fit = beta1*x[i] + beta0;
            rss += (fit - y[i]) * (fit - y[i]);
        }
        
        return rss;
	}

	@Override
	public List<SensorValue> getKeyValues() {		
		return Collections.unmodifiableList(mKeyValues);
	}

	@Override
	public void setOutputFile(String outputPath) throws IOException {
		mOutputWriter = new PrintWriter(new BufferedWriter(new FileWriter(outputPath, true)));  	
	}

	@Override
	public void closeOutputFile() {
		if(mOutputWriter != null){
			// as we are always saving the forelast elements we have to write 
			// the last element in the end 
			
			if(mKeyValues.size() > 1){
				mOutputWriter.println(mKeyValues.get(mKeyValues.size() - 1).getCsvString());
			}
			
			mOutputWriter.close();
			mOutputWriter = null;
		}
		
	}
}
