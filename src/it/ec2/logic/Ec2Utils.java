package it.ec2.logic;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import it.ec2.model.*;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RebootInstancesRequest;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesResult;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesResult;
import com.amazonaws.util.Base64;

public class Ec2Utils {

    /**
     * Public constructor.
     * @throws Exception
     */

    /**
     * The only information needed to create a client are security credentials
     * consisting of the AWS Access Key ID and Secret Access Key. All other
     * configuration, such as the service endpoints, are performed
     * automatically. Client parameters, such as proxies, can be specified in an
     * optional ClientConfiguration object when constructing a client.
     *
     * @see com.amazonaws.auth.BasicAWSCredentials
     * @see com.amazonaws.auth.PropertiesCredentials
     * @see com.amazonaws.ClientConfiguration
     */
    protected static AmazonEC2 init() throws Exception {
    	AmazonEC2 ec2;
        /*
         * Necessary to 1) set credentials
         * 2) Set start-slave.sh folder and spark master ip/port
         * 3) Set InstanceRequest Parameters correctly
         */
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider("default").getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. ",
                    e);
        }

        ec2 = new AmazonEC2Client(credentials);
        Region usWest2 = Region.getRegion(Regions.EU_CENTRAL_1);
        ec2.setRegion(usWest2);
        return ec2;
    }
    
    private static String setECSuserData() {
        String userData = "#!/bin/bash" + "\n";
        userData = userData + "/path/to/start-slave.sh spark://master_ip:port \n";
        String base64UserData = null;
        try {
            base64UserData = new String( Base64.encode( userData.getBytes( "UTF-8" )), "UTF-8" );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return base64UserData;
    }

    public static ArrayList<Machine> createInstance(AmazonEC2 ec2, int requestedCores) {
    	String machineType = "t2.small";
    	int numInstances = 1;
    	if(requestedCores > 4)
    	{
    		machineType = "m4.xlarge";
    		numInstances = requestedCores / 4;
    	}
    	switch(requestedCores){
    	case 0: 
    		return null;
    	case 1: case 2: 
    		machineType = "m4.large";
    		break;
    	case 3: case 4: default: 
    		machineType = "m4.xlarge";
    		break;
    	}
    	
    	//To be set
		System.out.println("Adding "+numInstances+" with type "+machineType);
    	RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
    	runInstancesRequest.withImageId("ami")
    	.withInstanceType(machineType)
    	.withMinCount(numInstances)
    	.withMaxCount(numInstances)
    	.withUserData(setECSuserData())
    	.withDisableApiTermination(false)
    	.withSecurityGroupIds("sg")
    	.withKeyName("key")
    	.withSecurityGroups("security");

    	try {
			TimeUnit.SECONDS.sleep(5);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	RunInstancesResult runInstancesResult = ec2.runInstances(runInstancesRequest);
    	List<Instance> resCreatedList = runInstancesResult.getReservation().getInstances();
    	ArrayList<Machine> machinesCreated = new ArrayList<Machine>();

    	for (Instance k : resCreatedList) {
    		CreateTagsRequest createTagsRequest = new CreateTagsRequest();
    		createTagsRequest.withResources(k.getInstanceId()) 
    		.withTags(new Tag("Name", "added")); //"added" implies the machine was dynamically created
    		ec2.createTags(createTagsRequest);

    		System.out.println("Istanza creata: "+ 
    				" / Tipo: " + k.getInstanceType() + 
    				" / IP: " + k.getPrivateIpAddress() +
    				" / ID: " + k.getInstanceId() +
    				" / Stato Attuale: " + k.getState().getName());

    		Machine m = new Machine(k.getPrivateIpAddress(), "added", k.getInstanceType(), k.getInstanceId(),System.currentTimeMillis());
    		machinesCreated.add(m);
    	}
    	return machinesCreated;
    }
    
    public static ArrayList<Machine> checkClusterStatus(AmazonEC2 ec2, Evaluator t) {
	try
	{
		ArrayList<Machine> currentAddedMachines = new ArrayList<Machine>();
		DescribeInstancesResult descReq = ec2.describeInstances();
		List<Reservation> reservations = descReq.getReservations();
        Set<Instance> instances = new HashSet<Instance>();
        int cores = 0;
        int memory = 0;
        int runningInstances = 0;
        
        for (Reservation reservation : reservations) {
        	for(Instance k : reservation.getInstances())
        	{
        		if(k.getState().getName() == null)
        			continue;
        		if (k.getState().getName().equals("running") && (k.getTags().get(0).getValue().equals("added") ||
        				k.getTags().get(0).getValue().equals("worker")))
        		{
	        		switch(k.getInstanceType()){
        				case "t2.micro": cores++; memory++;
        					break;
	        			case "t2.small": cores++; memory+=2;
	        				break;
	        			case "t2.medium": cores+=2; memory+=4;
	    					break;
	        			case "t2.large": cores+=2; memory+=8;
	    					break;
	        			case "m4.large": cores+=2; memory+=8;
	    					break;
	        			case "m4.xlarge": cores+=4; memory+=16;
	    					break;
	        			case "c4.2xlarge": cores+=8; memory+=15;
	    					break;
	        		}
	        		runningInstances++;
	                System.out.println("Instance found: Gruppo: " + k.getTags().get(0).getValue() + 
	                		" / Tipo: " + k.getInstanceType() + 
	                		" / IP: " + k.getPrivateIpAddress() +
	                		" / ID: " + k.getInstanceId() +
	                		" / Stato Attuale: " + k.getState().getName());
        		}
        		instances.add(k);
        		if(!k.getTags().isEmpty() && k.getTags().get(0).getValue().equals("added") && k.getState().getName().equals("running"))
        		{
        			currentAddedMachines.add(new Machine(k.getPrivateIpAddress(), k.getTags().get(0).getValue(),
        					k.getInstanceType(), k.getInstanceId(), k.getLaunchTime().getTime()));
        		}
        	}
        }

        System.out.println("You have " + instances.size() + " Amazon EC2 instance(s) existing.");
        System.out.println("You have " + runningInstances + " Worker Amazon EC2 instance(s) running.");
        t.setCurrentCores(cores);
        t.setCurrentMemory(memory);
        return currentAddedMachines;
	} catch (AmazonServiceException e) {
	    // Print out the error.
	    System.out.println("Error when calling describeSpotInstances");
	    System.out.println("Caught Exception: " + e.getMessage());
	    System.out.println("Reponse Status Code: " + e.getStatusCode());
	    System.out.println("Error Code: " + e.getErrorCode());
	    System.out.println("Request ID: " + e.getRequestId());

		}
	return null;
    }
    
    /**
     * @param instanceId
     * @param templateBody
     * @param notificationARNs
     * @param creationTimeout
     * @param enableRollback
     * @param ec2
     * @param buildLogger
     * @return String
     * @throws AmazonServiceException
     * @throws AmazonClientException
     * @throws InterruptedException
     * @throws Exception
     */
    public static void startInstance(final String instanceId, AmazonEC2 ec2)
            throws AmazonServiceException, AmazonClientException, InterruptedException
    {
        // Stop the instance
        StartInstancesRequest startRequest = new StartInstancesRequest().withInstanceIds(instanceId);
        StartInstancesResult startResult = ec2.startInstances(startRequest);
        //List<InstanceStateChange> stateChangeList = startResult.getStartingInstances();
        System.out.println("Starting instance :" + instanceId);
    }

    /**
     * @param instanceId
     * @param doForce
     * @param ec2
     * @param buildLogger
     * @return String
     * @throws AmazonServiceException
     * @throws AmazonClientException
     * @throws InterruptedException
     * @throws Exception
     */
    public static void stopInstance(final String instanceId, final Boolean forceStop, AmazonEC2 ec2
            ) throws AmazonServiceException, AmazonClientException, InterruptedException
    {
        // Stop the instance
        StopInstancesRequest stopRequest = new StopInstancesRequest().withInstanceIds(instanceId).withForce(forceStop);
        StopInstancesResult startResult = ec2.stopInstances(stopRequest);
        //List<InstanceStateChange> stateChangeList = startResult.getStoppingInstances();
        System.out.println("Stopping instance: " + instanceId);
    }
    
    public static void terminateInstance(final String instanceId, AmazonEC2 ec2
            ) throws AmazonServiceException, AmazonClientException, InterruptedException
    {
        // Stop the instance
        TerminateInstancesRequest terminateRequest = new TerminateInstancesRequest().withInstanceIds(instanceId);
        TerminateInstancesResult terminateResult = ec2.terminateInstances(terminateRequest);
        //List<InstanceStateChange> stateChangeList = terminateResult.getTerminatingInstances();
        System.out.println("Terminating instance :" + instanceId);
    }

    /**
     * @param instanceId
     * @param ec2
     * @return String
     * @throws AmazonServiceException
     * @throws AmazonClientException
     * @throws Exception
     */
    public void rebootInstance(final String instanceId, AmazonEC2 ec2)
            throws AmazonServiceException, AmazonClientException
    {
        // Reboot the instance
        RebootInstancesRequest rebootRequest = new RebootInstancesRequest().withInstanceIds(instanceId);
        System.out.println("Rebooting instance :" + instanceId);
        ec2.rebootInstances(rebootRequest);
    }

}
