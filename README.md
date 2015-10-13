# JenkinsAdapter

JenkinsAdapter is a component enabling data exchange with the Jenkins continuous integration platform (https://jenkins-ci.org/) implementing the uQasarAdapter interface and overriding the following methods:

<strong><pre>public  List<Measurement> query(BindedSystem bindedSystem, User user, QueryExpression queryExpression) throws uQasarException;</pre></strong>
<strong><pre>public List<Measurement> query(String bindedSystemURL, String credentials,	String queryExpression) throws uQasarException;</pre></strong>


