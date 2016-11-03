package opendial.modules.neo4j;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Neo4jQueryExecutor extends QueryExecutor {
	
	private String neo4jIP = "143.225.85.137";
	private int neo4jPort = 7474;

	protected String executePost(String query) {

		HttpURLConnection connection = null;
		try {
			// Create connection
			URL url = new URL("http://" + neo4jIP + ":" + neo4jPort + "/db/data/transaction/");
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Authorization", "Basic bmVvNGo6TXVsdGlXb3JkTmV0QHVuaW5hMjAxNg==");

			String urlParameters = "{\"statements\" : [ { \"statement\" : \"" + query + "\" } ] }";
			connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));

			connection.setUseCaches(false);
			connection.setDoOutput(true);

			// Send request
			DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.close();

			// Get Response
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			StringBuilder response = new StringBuilder(); 
			String line;
			while ((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
			return response.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	@Override
	public ArrayList<String> executeQuery(String query) {

		ArrayList<String> results = new ArrayList<String>();

		try {
			
			query = query.replace("^", "<");
			System.out.println(query);
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
