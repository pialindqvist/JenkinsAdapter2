package eu.uqasar.jenkins.adapter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Job;
import com.offbytwo.jenkins.model.JobWithDetails;

import eu.uqasar.adapter.SystemAdapter;
import eu.uqasar.adapter.exception.uQasarException;
import eu.uqasar.adapter.model.BindedSystem;
import eu.uqasar.adapter.model.Measurement;
import eu.uqasar.adapter.model.User;
import eu.uqasar.adapter.model.uQasarMetric;
import eu.uqasar.adapter.query.QueryExpression;
public class JenkinsAdapter implements SystemAdapter {

/************************************** QUERY METHOD ADAPTER - JENKINS  **************************************/
    @Override
    public List<Measurement> query(BindedSystem boundSystem, User user, 
    		QueryExpression queryExpression) throws uQasarException {

    	/* A linked list variable for storing the measurements */
    	LinkedList<Measurement> measurements = new LinkedList<Measurement>();

        try {

        	/* Saving the system url */  
    		String long_url = boundSystem.getUri();
    		String url = "";

    		/* Saving the project name */
    		String project = "";

    		/* Testing weather the url is valid and removing the end of it to get an url for authentication */
    		String[] parts = long_url.split("/");
    		    			
		//************************ Splitting the URL to URL & project name ************************//
			
			for(int i = 0; i < parts.length; i++) {
				if(parts[i].compareTo("job") == 0 && parts.length-1 > i){
					project = parts[i+1];
					i = parts.length;
					}
				else if (i == 3){
					url = url + parts[i];
				}
				else{
					url = url + parts[i] + "/";
					}
			}
			
			
            /* Initialize new Jenkins server */
            JenkinsServer jenkins = null;            

            /* Use the Jenkins-client library to open the server connection */
				
        //********************************* Connecting to server *********************************//
				
			jenkins = new JenkinsServer(new URI(url), user.getUsername(), user.getPassword());
		
            /* This comes from the U-QASAR interface */
            String query = queryExpression.getQuery();
            		            
		//******************************* Implementing data mapping *******************************//
		// TODO: Is better error handling needed?

            if (query.equalsIgnoreCase(uQasarMetric.JENKINS_LATEST_BUILD_SUCCESS.name())) {
            	
            	// Here the status of the latest build is fetched
            	// Only status "Stable", "Unstable" and "Failed" are noticed.
            	// Should all the statuses be noticed?

          	  	JobWithDetails job  = null;
          	  	String status = "";
            	
      	  		job = jenkins.getJobs().get(project).details();
      	  		
				if(job.getLastBuild().getNumber() == job.getLastStableBuild().getNumber()){
				 
					status = "Stable";
				}
				else if (job.getLastBuild().getNumber() == job.getLastUnstableBuild().getNumber()) {
				  
					status = "Unstable";
				}
				else if (job.getLastBuild().getNumber() == job.getLastFailedBuild().getNumber()) {
					status = "Broken";
				}
				else
					status = "Unknown";
          	  	
          	  	measurements.add(new Measurement(uQasarMetric.JENKINS_LATEST_BUILD_SUCCESS, status));
                
            }
            
            if (query.equalsIgnoreCase(uQasarMetric.JENKINS_BUILD_HISTORY.name())) {
            	
            	// Here the status of the maximum 100 latest builds are fetched
            	// Only status "Stable", "Unstable" and "Failed" are noticed.
            	// Should all the statuses be noticed?
            	
            	JobWithDetails job  = null;  
            	JSONArray measurementResultJSONArray = new JSONArray();
            	
            	job = jenkins.getJobs().get(project).details();
            	
	      		for(int i=0; i< Math.min(100, job.getBuilds().size()); i++ ) {
	      			
	      			JSONObject jObj = new JSONObject();
	      			jObj.put("BuildNumber", Integer.toString(job.getBuilds().get(i).details().getNumber()));
	      			
	      			if(job.getBuilds().get(i).details().getResult().name() != "STABLE") {
	      				jObj.put("BuildStatus", "Stable");
	      			}
	      			else if(job.getBuilds().get(i).details().getResult().name() != "UNSTABLE") {
	      				jObj.put("BuildStatus", "Unstable");
	      			}
	      			else if(job.getBuilds().get(i).details().getResult().name() != "FAILED") {
	      				jObj.put("BuildStatus", "Broken");
	      			}
	      			else {
	      				jObj.put("BuildStatus", "Other");
	      			}
	      			
	      			measurementResultJSONArray.put(jObj);
	      		}
            	
            	measurements.add(new Measurement(uQasarMetric.JENKINS_BUILD_HISTORY, measurementResultJSONArray.toString()));
	      		
            }
            
            if (query.equalsIgnoreCase(uQasarMetric.JENKINS_PROJECTS.name())) {
            	
            	// Here all the projects in the Jenkins instance are fetched
            	// Name, url, last build.
            	// Is something else needed?
            
            	JobWithDetails job  = null;
            	Map<String, Job> jobs = null;
  			  	JSONArray measurementResultJSONArray = new JSONArray();
  			  	
		  		job = jenkins.getJobs().get(project).details();
		  		jobs = jenkins.getJobs();
		  	
  			  	for (Map.Entry entry : jobs.entrySet()) {
  			  		JSONObject jObj = new JSONObject();
  				  
  			  		Job j = (Job) entry.getValue();
  			  		jObj.put("name", j.getName());
  			  		jObj.put("url", j.getUrl());
  			
  			  		if(j.details().getLastBuild() != null) {
  			  			jObj.put("last_build", Integer.toString(j.details().getLastBuild().getNumber()));
  			  		}
  			  		else {
  			  			jObj.put("last_build", "no_builds");
  			  		}
  			
  			  		measurementResultJSONArray.put(jObj);
  			  	}
  			  	
  			  	measurements.add(new Measurement(uQasarMetric.JENKINS_PROJECTS, measurementResultJSONArray.toString()));
            	
            }
	            
            return measurements;

        } catch (Exception e) {
        	e.printStackTrace();
			return measurements;
		}
        
//    	catch(NullPointerException e) {
//    		throw new uQasarException(String.format("There was an exception"));
//		}
				
//		catch (URISyntaxException e) {
//			e.printStackTrace();
//			return measurements;
//		}
        
    }

/************************************** QUERY METHOD U-QASAR - ADAPTER  **************************************/
    
    @Override
    public List<Measurement> query(String boundSystemURL, String credentials, 
    		String queryExpression) throws uQasarException {
    	
        List<Measurement> measurements = null;

        BindedSystem boundSystem = new BindedSystem();
        boundSystem.setUri(boundSystemURL);
        User user = new User();

        String[] creds = credentials.split(":");

        user.setUsername(creds[0]);
        user.setPassword(creds[1]);

        JenkinsQueryExpression jenkinsQueryExpression = 
        		new JenkinsQueryExpression(queryExpression);
        
        JenkinsAdapter jenkinsAdapter = new JenkinsAdapter();
        // Get the measurements
        measurements = jenkinsAdapter.query(boundSystem, user, 
        		jenkinsQueryExpression);

        return measurements;
    }

/****************************** METHOD FOR PRINTING OUT THE MEASUREMENTS  ******************************/
    
    public void printMeasurements(List<Measurement> measurements){
        String newLine = System.getProperty("line.separator");
        for (Measurement measurement : measurements) {
            System.out.println("----------TEST metric: "
            		+measurement.getMetric()+" ----------" +newLine);
            System.out.println(measurement.getMeasurement() +newLine +newLine);
            System.out.println();

        }
    }
    
/************************************** MAIN METHOD FOR TESTING  **************************************/

    // Execute on command line: 
    // $ java -jar target\GitLabAdapter-1.0-jar http://dev.uqasar.eu/jenkins/job/uqasar/ user:pass QUERY
    // QUERY is JENKINS_PROJECTS or JENKINS_BUILD_HISTORY or JENKINS_LATEST_BUILD_SUCCESS
    
    // TODO: kirjoita uusi testi tälle luokalle!
//	public static void main(String[] args) {
//		
//	    List<Measurement> measurements;
//	    BindedSystem boundSystem = new BindedSystem();
//	    boundSystem.setUri(args[0]);
//	
//	    // User
//	    User user = new User();
//	    
//	    String[] credentials = args[1].split(":");
//	    user.setUsername(credentials[0]);
//	    user.setPassword(credentials[1]);
//	
//	    
//	    try {
//	    	JenkinsAdapter jenkinsAdapter = new JenkinsAdapter();
//	    	JenkinsQueryExpression jenkinsQueryExpression = new JenkinsQueryExpression(args[2]);    	
//	        measurements = jenkinsAdapter.query(boundSystem, user, jenkinsQueryExpression);
//	        jenkinsAdapter.printMeasurements(measurements);
//	        
//	    } catch (uQasarException e) {
//	        e.printStackTrace();
//	    }    
//	}
    
    
    public static void main(String[] args) {
    	
    	List<Measurement> measurements;
 	    BindedSystem boundSystem = new BindedSystem();
 	    boundSystem.setUri("http://dev.uqasar.eu/jenkins/job/uqasar/");
 	
 	    // User
 	    User user = new User();
 	    
 	    //String[] credentials = args[1].split(":");
 	    user.setUsername("jenkins");
 	    user.setPassword("!jenkins!");
 	
 	    
 	    try {
 	    	JenkinsAdapter jenkinsAdapter = new JenkinsAdapter();
 	    	JenkinsQueryExpression jenkinsQueryExpression = new JenkinsQueryExpression("JENKINS_BUILD_HISTORY");    	
 	        measurements = jenkinsAdapter.query(boundSystem, user, jenkinsQueryExpression);
 	        jenkinsAdapter.printMeasurements(measurements);
 	        
 	    } catch (uQasarException e) {
 	        e.printStackTrace();
 	    }
    }
    
}











