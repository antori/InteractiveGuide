package opendial.modules.utils;

import java.util.ArrayList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import java.io.FileReader;
import java.io.DataOutputStream;
import org.json.*;

import opendial.DialogueSystem;


public class Neo4j {
	boolean paused = true;
	DialogueSystem dsys;
	private String neo4jIP = "143.225.85.137";
	private int neo4jPort = 7474;

	private void readConfig(String file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			String line = br.readLine();

			while (line != null) {
				String[] vars = line.split("=");

				switch (vars[0]) {
				case "NEO4JIP":
					neo4jIP = vars[1];
					break;
				case "NEO4JPORT":
					neo4jPort = Integer.parseInt(vars[1]);
					break;
				}

				line = br.readLine();
			}
		} finally {
			br.close();
		}
	}

	private String executePost(String query) {

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
			StringBuilder response = new StringBuilder(); // or StringBuffer if
															// not Java 5+
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

	public ArrayList<String> getSynonimus(String term, String type, String semanticField) {

		ArrayList<String> results = new ArrayList<String>();

		try {
			term = term.replaceAll("\t", "").replaceAll("\n", "").replaceAll(" ","");
			semanticField = semanticField.replaceAll("\t", "").replaceAll("\n", "").replaceAll(" ","");
			String query = "MATCH(n)-[:BELONGS_TO]->(m:SYNSET)<-[:BELONGS_TO]-(k),(m)-[:BELONGS_TO]->(j:SEMANTIC_FIELD)" + "WHERE n.word = \\\"" + term + "\\\" and j.name= \\\""
					+ semanticField + "\\\" RETURN k.word";
					
			System.out.println(query);
			String res = executePost(query);
			System.out.println(res);
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
