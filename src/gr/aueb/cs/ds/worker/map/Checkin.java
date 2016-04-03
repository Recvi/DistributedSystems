package gr.aueb.cs.ds.worker.map;

public class Checkin {
	
	private String POI;
	private String POI_name;
	private String POI_category;
	private double latitude;
	private double longitude;
	private String time;
	private String photos;
	
	
	public Checkin (String poi, String poi_name, String poi_category, double lat, double lon, String time, String photos) {
		this.POI = poi;
		this.POI_name = poi_name;
		this.POI_category = poi_category;
		this.latitude = lat;
		this.longitude = lon;
		this.time = time;
		this.photos = photos;
	}


	public String getPOI() {
		return POI;
	}


	public void setPOI(String pOI) {
		POI = pOI;
	}


	public String getPOI_name() {
		return POI_name;
	}


	public void setPOI_name(String pOI_name) {
		POI_name = pOI_name;
	}


	public String getPOI_category() {
		return POI_category;
	}


	public void setPOI_category(String pOI_category) {
		POI_category = pOI_category;
	}


	public double getLatitude() {
		return latitude;
	}


	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}


	public double getLongitude() {
		return longitude;
	}


	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}


	public String getTime() {
		return time;
	}


	public void setTime(String time) {
		this.time = time;
	}


	public String getPhotos() {
		return photos;
	}


	public void setPhotos(String photos) {
		this.photos = photos;
	}

}