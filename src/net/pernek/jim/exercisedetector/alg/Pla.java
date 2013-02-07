package net.pernek.jim.exercisedetector.alg;

import java.io.IOException;
import java.util.List;

public interface Pla {
	
	void process(SensorValue newValue);
	
	void setOutputFile(String outputPath) throws IOException;
	
	void closeOutputFile();
	
	List<SensorValue> getKeyValues();
}
