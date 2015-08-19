package eu.uqasar.gl.adapter;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import eu.uqasar.adapter.exception.uQasarException;
import eu.uqasar.adapter.model.Measurement;
import eu.uqasar.jenkins.adapter.JenkinsAdapter;

/**
 * 
 * @author Feetu Nyrhinen <nyrhinen@atb-bremen.de>
 *
 */
public class GitlabAdapterTest {

	private JenkinsAdapter gitlabAdapter;
    private String boundSystemURL;
    private String credentials;
    private String queryExpression;

    
    @Before
    public void setUp() {
    	gitlabAdapter = new JenkinsAdapter();
    	boundSystemURL = "https://gitlab.com";
    	credentials = "user:pass";
    }
    
    
    @Test
    public void testGetCommits() {
    	queryExpression = "GIT_COMMITS";
    	try {
			List<Measurement> measurements = gitlabAdapter.query(boundSystemURL, credentials, queryExpression);
			Assert.assertNotNull(measurements);
			gitlabAdapter.printMeasurements(measurements);
		} catch (uQasarException e) {
			e.printStackTrace();
		}
    }
    
    @Test
    public void testGetProjects() {
    	queryExpression = "GIT_PROJECTS";
    	try {
			List<Measurement> measurements = gitlabAdapter.query(boundSystemURL, credentials, queryExpression);
			Assert.assertNotNull(measurements);
			gitlabAdapter.printMeasurements(measurements);
		} catch (Exception e) {
			e.printStackTrace();
		}    	
    }
    
    
}
