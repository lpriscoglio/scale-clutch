package it.spark.model;

public class SparkAttempt {
	private String startTime;
	private String endTime;
	private String sparkUser;
	private boolean completed;
	public String getStartTime() {
		return startTime;
	}
	public String getEndTime() {
		return endTime;
	}
	public String getSparkUser() {
		return sparkUser;
	}
	public boolean isCompleted() {
		return completed;
	}
}
