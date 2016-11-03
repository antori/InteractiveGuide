package opendial.modules.neo4j;

public class PersistenceManager {

	private static final String NEO4J = "neo4j";

	public QueryExecutor getQueryExecutor(String queryType) {

		if (queryType.equals(NEO4J)) {
			return new Neo4jQueryExecutor();
		}

		throw new RuntimeException("[Neo4j] Can not find QueryExecutor!");
	}

}
