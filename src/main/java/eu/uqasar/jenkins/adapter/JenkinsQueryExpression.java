package eu.uqasar.jenkins.adapter;

import eu.uqasar.adapter.query.QueryExpression;

public class JenkinsQueryExpression extends QueryExpression {

    String query;

    public JenkinsQueryExpression(String query) {
        super(query);
        this.query = query;
    }

    public String getQuery() {
        return query;
    }
}
