package eu.uqasar.gl.adapter;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gitlab.api.GitlabAPI;
import org.gitlab.api.models.GitlabCommit;
import org.gitlab.api.models.GitlabProject;
import org.gitlab.api.models.GitlabSession;

import eu.uqasar.adapter.SystemAdapter;
import eu.uqasar.adapter.exception.uQasarException;
import eu.uqasar.adapter.model.BindedSystem;
import eu.uqasar.adapter.model.Measurement;
import eu.uqasar.adapter.model.User;
import eu.uqasar.adapter.model.uQasarMetric;
import eu.uqasar.adapter.query.QueryExpression;

public class GitlabAdapter implements SystemAdapter {

	private final static Logger LOGGER = 
			Logger.getLogger(GitlabAdapter.class.getName()); 
	
	// Constructor
    public GitlabAdapter() {
    	LOGGER.setLevel(Level.INFO);
    }

    @Override
    public List<Measurement> query(BindedSystem boundSystem, User user, 
    		QueryExpression queryExpression) throws uQasarException {

    	// For storing the measurements 
    	LinkedList<Measurement> measurements = new LinkedList<Measurement>();

        try {

        	// URL of the Gitlab instance
        	String url = boundSystem.getUri();
        	
        	// Init a Gitlab session
            GitlabSession session = GitlabAPI.connect(url, user.getUsername(), 
            		user.getPassword());
            
            // Obtain a private token from the session by authenticating
            String privateToken = session.getPrivateToken();
            
            LOGGER.info("PrivateToken: " +privateToken);
            
            // Connect to GitlabAPI
            GitlabAPI api = GitlabAPI.connect(url, privateToken);
            
            String query = queryExpression.getQuery();
            
            if (query.equalsIgnoreCase(uQasarMetric.GIT_COMMITS.name())){
            	LOGGER.info("COMMITS...");
            	List<GitlabProject> projects = api.getProjects();
            	Integer commitsCount = null;
                for (GitlabProject gitlabProject : projects) {
                	LOGGER.info("Project: " +gitlabProject.getName());
    				List<GitlabCommit> listOfCommits = 
    						api.getAllCommits(gitlabProject.getId());
    				commitsCount = listOfCommits.size();
    				String commitsCountStr = Integer.toString(commitsCount);
    				
    	            for (GitlabCommit commit : listOfCommits) {
    	            	LOGGER.info("[ id: "+commit.getId() 
    	            			+", author: " +commit.getAuthorName() 
    	            			+", title: " +commit.getTitle()
    	            			+", created: " +commit.getCreatedAt()
    	            			+" ]");
    				}				

    	            LOGGER.info("Number of commits: " +commitsCount);
    	            measurements.add(new Measurement(uQasarMetric.GIT_COMMITS, 
    	            		commitsCountStr));            	
                }
            } 
            
            if (query.equalsIgnoreCase(uQasarMetric.GIT_PROJECTS.name())) {
            	LOGGER.info("PROJECTS...");
            	List<GitlabProject> projects = api.getProjects();
            	Integer projectsCount = projects.size();
            	LOGGER.info("Number of projects: " +projectsCount);
            	String projectsCountStr = Integer.toString(projectsCount);
            	
            	for (GitlabProject gitlabProject : projects) {
					LOGGER.info("[ id: " +gitlabProject.getId() +", "
							+ "name: " +gitlabProject.getName() +", "
							+ "ssh-url: " +gitlabProject.getSshUrl()
							+" ]");
				}
            	measurements.add(new Measurement(uQasarMetric.GIT_PROJECTS, 
            			projectsCountStr));
            }
            
            return measurements;
            
        }   catch (Exception e) {
			e.printStackTrace();
			return measurements;
		}
    }

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

        GitlabQueryExpression gitlabQueryExpression = 
        		new GitlabQueryExpression(queryExpression);
        GitlabAdapter gitlabAdapter = new GitlabAdapter();
        // Get the measurements
        measurements = gitlabAdapter.query(boundSystem, user, 
        		gitlabQueryExpression);

        return measurements;
    }

    public void printMeasurements(List<Measurement> measurements){
        String newLine = System.getProperty("line.separator");
        for (Measurement measurement : measurements) {
            System.out.println("----------TEST metric: "
            		+measurement.getMetric()+" ----------" +newLine);
            System.out.println(measurement.getMeasurement() +newLine +newLine);
            System.out.println();

        }
    }

	//in order to invoke main from outside jar
	//mvn exec:java -Dexec.mainClass="eu.uqasar.gitlab.adapter.GitlabAdapter" 
    //-Dexec.args="https://gitlab.com user:pass"

	
	public static void main(String[] args) {
		
		
	    List<Measurement> measurements;
	    BindedSystem boundSystem = new BindedSystem();
	    boundSystem.setUri(args[0]);
	
	    // User
	    User user = new User();
	    
	    String[] credentials = args[1].split(":");
	    user.setUsername(credentials[0]);
	    user.setPassword(credentials[1]);
	
	    
	    try {
	    	GitlabAdapter gitlabAdapter = new GitlabAdapter();
	    	GitlabQueryExpression gitlabQueryExpression = 
	    			new GitlabQueryExpression(args[2]);    	
	        measurements = gitlabAdapter.query(boundSystem, user, 
	        		gitlabQueryExpression);
	        gitlabAdapter.printMeasurements(measurements);
	        
	    } catch (uQasarException e) {
	        e.printStackTrace();
	    }    
	}
}
