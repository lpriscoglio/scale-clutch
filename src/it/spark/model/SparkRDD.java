package it.spark.model;

public class SparkRDD {
	private int id;
	private String name;
	private int numPartitions;
	private int numCachedPartitions;
	private String storageLevel;
	private long memoryUsed;
	private int diskUsed;
	public long getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public int getNumPartitions() {
		return numPartitions;
	}
	public int getNumCachedPartitions() {
		return numCachedPartitions;
	}
	public String getStorageLevel() {
		return storageLevel;
	}
	public long getMemoryUsed() {
		return memoryUsed;
	}
	public long getDiskUsed() {
		return diskUsed;
	}
}
