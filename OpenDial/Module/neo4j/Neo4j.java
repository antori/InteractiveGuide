package opendial.modules.neo4j;

public class Neo4j {
	
	private static final String SYN = "syn";
	private static final String HAS_PART = "has_part";
	

	public QueryExecutor getQueryExecutor(String queryType) {
		
		if(queryType.equals(SYN)){
			return new SynQueryExecutor();
		}
		else if(queryType.equals(HAS_PART)){
			return new HasPartQueryExecutor();
		}
		
		throw new RuntimeException("[Neo4j] Can not find QueryExecutor!");
	}

}
