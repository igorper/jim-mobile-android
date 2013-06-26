package net.pernek.jim.exercisedetector.entities;


/** Contains series information.
 * @author Igor
 *
 */
public class Series {
	
	/***********************
	 * Fields deserialized from server data;
	 ***********************/
	private int id;
	private int repeat_count;
	private int rest_time;
	private int weight;
	
	/** Gets the planned number of repetitions for this series.
	 * @return
	 */
	public int getNumberRepetitions(){
		return repeat_count;
	}
	
	/** Gets the planned rest time for this series.
	 * @return
	 */
	public int getRestTime(){
		return rest_time;
	}

	/** Gets the planned weight for this series.
	 * @return
	 */
	public int getWeight(){
		return weight;
	}
}
