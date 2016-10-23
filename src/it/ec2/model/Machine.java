package it.ec2.model;

import java.util.ArrayList;

public class Machine {
	private String IP;
	private String groupName;
	private String type;
	private String ID;
	private long createdTime;
	private ArrayList<String> usedInApplications;
	private boolean terminateProtection;
	
	public Machine(String IP, String groupName, String type, String ID, long time)
	{
		this.IP = IP;
		this.ID = ID;
		this.groupName = groupName;
		this.type = type;
		this.setCreatedTime(time);
		usedInApplications = new ArrayList<String>();
		this.terminateProtection = false;
	}

	public String getIP() {
		return IP;
	}

	public void setIP(String iP) {
		IP = iP;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	public long getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(long createdTime) {
		this.createdTime = createdTime;
	}

	public ArrayList<String> getUsedInApplications() {
		return usedInApplications;
	}

	public void addToUsedInApplications(String appId) {
		usedInApplications.add(appId);
	}

	public boolean isTerminateProtection() {
		return terminateProtection;
	}

	public void setTerminateProtection(boolean terminateProtection) {
		this.terminateProtection = terminateProtection;
	}
}
