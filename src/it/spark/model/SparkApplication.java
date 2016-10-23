package it.spark.model;

public class SparkApplication {
	private String id;
	private String name;
	private int coresGranted;
	private int maxCores;
	private int memoryPerExecutorMB;
	private SparkAttempt [] attempts;
	public String getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public int getCoresGranted() {
		return coresGranted;
	}
	public int getMaxCores() {
		return maxCores;
	}
	public int getMemoryPerExecutorMB() {
		return memoryPerExecutorMB;
	}
	public SparkAttempt [] getAttempts() {
		return attempts;
	}
}
