package it.ec2.model;

public class Application {
	private String id;
	private String type;
	private int mappedPort;
	
	public Application(String id, String type, int mappedPort)
	{
		this.id = id;
		this.type = type;
		this.mappedPort = mappedPort;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getMappedPort() {
		return mappedPort;
	}

	public void setMappedPort(int mappedPort) {
		this.mappedPort = mappedPort;
	}
}
