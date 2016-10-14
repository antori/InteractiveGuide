package opendial.modules.neo4j;

import java.util.ArrayList;
import org.apache.commons.lang.WordUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SynQueryExecutor extends QueryExecutor {

	@Override
	public ArrayList<String> executeQuery(String term, String type, String semanticField) {
		
		ArrayList<String> results = new ArrayList<String>();

		try {
			term = term.replaceAll("\t", "").replaceAll("\n", "").replaceAll(" ","");
			semanticField = semanticField.replaceAll("\t", "").replaceAll("\n", "").replaceAll(" ","");
			String query = "MATCH(n:"+type.toUpperCase()+"{word:\\\""
			               + term +"\\\"})-[:BELONGS_TO]->(m:SYNSET)<-[:BELONGS_TO]-(k:"
			               +type.toUpperCase()+"),(m)-[:BELONGS_TO]->(j:SEMANTIC_FIELD{name:\\\""
					       +WordUtils.capitalize(semanticField)+"\\\"}) RETURN distinct k.word";
					
			String res = executePost(query);
			JSONObject json = new JSONObject(res);
			JSONArray data = (JSONArray) json.get("results");

			json = (JSONObject) data.get(0);
			data = (JSONArray) json.get("data");

			for (int i = 0; i < data.length(); i++) {
				JSONObject temp = (JSONObject) data.get(i);
				JSONArray row = (JSONArray)temp.get("row");
				results.add(row.getString(0));
			}

			return results;

		} catch (JSONException e) {
			System.out.println("JSON Parser Exception");
			return null;
		}
	}

}
