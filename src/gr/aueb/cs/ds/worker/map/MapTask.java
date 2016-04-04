package gr.aueb.cs.ds.worker.map;

public class MapTask {

	public String minLatitude;
	public String maxLatitude;
	public String minLongitude;
	public String maxLongitude;
	public String minTime;
	public String maxTime;
    		
	public MapTask(String minLatitude, String maxLatitude, String minLongitude,
    		String maxLongitude, String minTime, String maxTime) {
		
		this.minLatitude = minLatitude;
		this.maxLatitude = maxLatitude;
		this.minLongitude = minLongitude;
		this.maxLongitude = maxLongitude;
		this.minTime = minTime;
		this.maxTime = maxTime;
	}

}
