package opendial.modules.neo4j;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HasPartQueryExecutor extends QueryExecutor {

	@Override
	public ArrayList<String> executeQuery(String term, String type, String semanticField) {

		ArrayList<String> results = new ArrayList<String>();

		try {
			term = term.replaceAll("\t", "").replaceAll("\n", "").replaceAll(" ", "");
			semanticField = semanticField.replaceAll("\t", "").replaceAll("\n", "").replaceAll(" ", "");
			String query = "MATCH(n:" + type.toUpperCase() + "{word:\\\"" + term
					+ "\\\"})-[:BELONGS_TO]->(m:SYNSET)-[:HAS_PART]->(k:SYNSET)<-[:BELONGS_TO]-(z:" + type.toUpperCase()
					+ ") RETURN distinct z.word";

			String res = executePost(query);
			JSONObject json = new JSONObject(res);
			JSONArray data = (JSONArray) json.get("results");

			json = (JSONObject) data.get(0);
			data = (JSONArray) json.get("data");

			for (int i = 0; i < data.length(); i++) {
				JSONObject temp = (JSONObject) data.get(i);
				JSONArray row = (JSONArray) temp.get("row");
				results.add(row.getString(0));
			}

			return results;

		} catch (JSONException e) {
			System.out.println("JSON Parser Exception");
			return null;
		}
	}

}
