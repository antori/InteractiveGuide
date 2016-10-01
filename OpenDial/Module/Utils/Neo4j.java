package opendial.modules;

import java.util.Collection;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;
import java.net.URLEncoder;
import java.net.UnknownHostException;

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.DataOutputStream;
import java.nio.charset.*;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

import opendial.DialogueState;
import opendial.DialogueSystem;
import opendial.bn.values.ValueFactory;
import opendial.bn.values.SetVal;
import opendial.bn.values.StringVal;
import opendial.bn.values.Value;

import java.sql.*;
import java.util.Properties;

public class Neo4j implements Module {
	boolean paused = true;
	DialogueSystem dsys;
	private String neo4jIP;
	private int neo4jPort;

	public Neo4j(DialogueSystem system) throws IOException{
		this.dsys = system;
		readConfig("./ARGOS.cfg");
	}

	public boolean isRunning() {
		return !paused;
	}

	public void pause(boolean toPause) {
		paused = toPause;
	}

	public void start() {
		paused = false;
	}

	private void readConfig(String file) throws IOException {
	    BufferedReader br = new BufferedReader(new FileReader(file));
	    try {
	        String line = br.readLine();
	        
	        while (line != null) {
	            String[] vars= line.split("=");
	            
	            switch(vars[0]) {
	            	case "NEO4JIP": 	neo4jIP= vars[1];
          					break;
	            	case "NEO4JPORT": 	neo4jPort= Integer.parseInt(vars[1]);
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
			//Create connection
			URL url = new URL("http://" + neo4jIP + ":" + neo4jPort + "/db/data/transaction/commit");
			connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");

			String urlParameters= "{\"statements\" : [ { \"statement\" : \"" + query + "\" } ] }";
			connection.setRequestProperty("Content-Length", Integer.toString(urlParameters.getBytes().length));

			connection.setUseCaches(false);
			connection.setDoOutput(true);

			//Send request
			DataOutputStream wr = new DataOutputStream (
			connection.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.close();

			//Get Response  
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			StringBuilder response = new StringBuilder(); // or StringBuffer if not Java 5+ 
			String line;
			while((line = rd.readLine()) != null) {
				response.append(line);
				response.append('\r');
			}
			rd.close();
			return response.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if(connection != null) {
				connection.disconnect(); 
			}
		}
	}

	public void trigger(DialogueState state, Collection<String> updatedVars) {
		ResultSet rs;
		String cypherResult= "";
		String buf = null;

		if (updatedVars.contains("node") && !paused) {

			try {
				if (!state.queryProb("node").getBest().toString().equals("empty")) {

					System.out.println(state.queryProb("node").getBest().toString());

					//Find Continue node and save audio
					String query= "MATCH (a)-[:SHARES_TOPIC]->(b) WHERE a.id='" + state.queryProb("node").getBest().toString() + "' AND NOT b.id IN [" + state.queryProb("visited").getBest().toString().replaceAll("X","'") + "] RETURN b.speech, b.id, b.xi, b.yi, b.xf, b.yf, b.x, b.y UNION MATCH (a)-[:DEEPENS*]->()-[:SHARES_TOPIC]->(b) WHERE a.id='" + state.queryProb("node").getBest().toString() + "' AND NOT b.id IN [" + state.queryProb("visited").getBest().toString().replaceAll("X","'") + "] RETURN b.speech, b.id, b.xi, b.yi, b.xf, b.yf, b.x, b.y LIMIT 1";
					Charset.forName("UTF-8").encode(query);

					String res= executePost(query);
					JSONParser parser = new JSONParser();

					JSONObject json = (JSONObject) parser.parse(res);
					JSONArray data= (JSONArray) json.get("results");
					json= (JSONObject) data.get(0);
					data= (JSONArray) json.get("data");

					String nodeID= state.queryProb("node").getBest().toString();

					if (data.size() > 0) {
						json= (JSONObject) data.get(0);

						data= (JSONArray) json.get("row");
						dsys.addContent("continue_node", data.get(1).toString());
						if (data.get(2) != null) {
							dsys.addContent("continue_node_xi", data.get(2).toString());
							dsys.addContent("continue_node_yi", data.get(3).toString());
							dsys.addContent("continue_node_xf", data.get(4).toString());
							dsys.addContent("continue_node_yf", data.get(5).toString());
						}
						else {
							dsys.addContent("continue_node_xi", "empty");
							dsys.addContent("continue_node_yi", "empty");
							dsys.addContent("continue_node_xf", "empty");
							dsys.addContent("continue_node_yf", "empty");
						}
						if (data.get(6) != null) {
							dsys.addContent("continue_node_x", data.get(6).toString());
							dsys.addContent("continue_node_y", data.get(7).toString());
						}
						else {
							dsys.addContent("continue_node_x", "empty");
							dsys.addContent("continue_node_y", "empty");
						}
						new Thread(new PrefetchProcess("continue.wav", data.get(0).toString())).start();
					}
					else {
						query= "MATCH (a)-[:DEEPENS*]->()-[:DESCRIBES]->()<-[:DESCRIBES]-()<-[:DEEPENS]-(b) WHERE a.id='" + nodeID + "' AND NOT b.id IN [" + state.queryProb("visited").getBest().toString().replaceAll("X","'") + "] RETURN b.speech, b.id, b.xi, b.yi, b.xf, b.yf, b.x, b.y LIMIT 1";
						Charset.forName("UTF-8").encode(query);

						res= executePost(query);
						parser = new JSONParser();

						json = (JSONObject) parser.parse(res);
						data= (JSONArray) json.get("results");
						json= (JSONObject) data.get(0);
						data= (JSONArray) json.get("data");
						if (data.size() > 0) {					
							json= (JSONObject) data.get(0);
							data= (JSONArray) json.get("row");
							dsys.addContent("continue_node", data.get(1).toString());
							if (data.get(2) != null) {
								dsys.addContent("continue_node_xi", data.get(2).toString());
								dsys.addContent("continue_node_yi", data.get(3).toString());
								dsys.addContent("continue_node_xf", data.get(4).toString());
								dsys.addContent("continue_node_yf", data.get(5).toString());
							}
							else {
								dsys.addContent("continue_node_xi", "empty");
								dsys.addContent("continue_node_yi", "empty");
								dsys.addContent("continue_node_xf", "empty");
								dsys.addContent("continue_node_yf", "empty");
							}
							if (data.get(6) != null) {
								dsys.addContent("continue_node_x", data.get(6).toString());
								dsys.addContent("continue_node_y", data.get(7).toString());
							}
							else {
								dsys.addContent("continue_node_x", "empty");
								dsys.addContent("continue_node_y", "empty");
							}
							new Thread(new PrefetchProcess("continue.wav", data.get(0).toString())).start();
						}
						else {
							dsys.addContent("continue_node", "empty");
							dsys.addContent("continue_node_xi", "empty");
							dsys.addContent("continue_node_yi", "empty");
							dsys.addContent("continue_node_xf", "empty");
							dsys.addContent("continue_node_yf", "empty");
							dsys.addContent("continue_node_x", "empty");
							dsys.addContent("continue_node_y", "empty");
						}
					}

					//Find Deepen node and save audio
					String roots= state.queryProb("roots").getBest().toString();

					if (!roots.equals("empty")) {
						if (roots.indexOf(nodeID) > 0) {
							parser = new JSONParser();

							String queryStyle= "MATCH (b)-[:DEEPENS]->(c)-[:DESCRIBES]->(a) WHERE a.id='" + nodeID + "' AND NOT b.id IN [" + state.queryProb("visited").getBest().toString().replaceAll("X","'") + "] AND c.desc='style' RETURN b.speech, b.id, b.xi, b.yi, b.xf, b.yf, b.x, b.y LIMIT 1";
							res= executePost(queryStyle);

							json = (JSONObject) parser.parse(res);
							data= (JSONArray) json.get("results");
							if (data.size() > 0) {
								json= (JSONObject) data.get(0);
								data= (JSONArray) json.get("data");
								if (data.size() > 0) {
									json= (JSONObject) data.get(0);
									data= (JSONArray) json.get("row");
									new Thread(new PrefetchProcess("Style.wav", data.get(0).toString())).start();
									
									dsys.addContent("style_node", data.get(1).toString());
									if (data.get(2) != null) {
										dsys.addContent("deepen_node_xi", data.get(2).toString());
										dsys.addContent("deepen_node_yi", data.get(3).toString());
										dsys.addContent("deepen_node_xf", data.get(4).toString());
										dsys.addContent("deepen_node_yf", data.get(5).toString());
									}
									else {
										dsys.addContent("deepen_node_xi", "empty");
										dsys.addContent("deepen_node_yi", "empty");
										dsys.addContent("deepen_node_xf", "empty");
										dsys.addContent("deepen_node_yf", "empty");
									}
									if (data.get(6) != null) {
										dsys.addContent("deepen_node_x", data.get(6).toString());
										dsys.addContent("deepen_node_y", data.get(7).toString());
									}
									else {
										dsys.addContent("deepen_node_x", "empty");
										dsys.addContent("deepen_node_y", "empty");
									}
								}
								else {
									dsys.addContent("style_node", "empty");
								}
							}
							else {
								dsys.addContent("style_node", "empty");
							}

							String queryIconography= "MATCH (b)-[:DEEPENS]->(c)-[:DESCRIBES]->(a) WHERE a.id='" + nodeID + "' AND NOT b.id IN [" + state.queryProb("visited").getBest().toString().replaceAll("X","'") + "] AND c.desc='iconography' RETURN b.speech, b.id, b.xi, b.yi, b.xf, b.yf, b.x, b.y LIMIT 1";
							res= executePost(queryIconography);

							json = (JSONObject) parser.parse(res);
							data= (JSONArray) json.get("results");
							if (data.size() > 0) {
								json= (JSONObject) data.get(0);
								data= (JSONArray) json.get("data");
								if (data.size() > 0) {
									json= (JSONObject) data.get(0);
									data= (JSONArray) json.get("row");
									new Thread(new PrefetchProcess("Iconography.wav", data.get(0).toString())).start();
									dsys.addContent("iconography_node", data.get(1).toString());
									if (data.get(2) != null) {
										dsys.addContent("deepen_node_xi", data.get(2).toString());
										dsys.addContent("deepen_node_yi", data.get(3).toString());
										dsys.addContent("deepen_node_xf", data.get(4).toString());
										dsys.addContent("deepen_node_yf", data.get(5).toString());
									}
									else {
										dsys.addContent("deepen_node_xi", "empty");
										dsys.addContent("deepen_node_yi", "empty");
										dsys.addContent("deepen_node_xf", "empty");
										dsys.addContent("deepen_node_yf", "empty");
									}
									if (data.get(6) != null) {
										dsys.addContent("deepen_node_x", data.get(6).toString());
										dsys.addContent("deepen_node_y", data.get(7).toString());
									}
									else {
										dsys.addContent("deepen_node_x", "empty");
										dsys.addContent("deepen_node_y", "empty");
									}
								}
								else {
									dsys.addContent("iconography_node", "empty");
								}
							}
							else {
								dsys.addContent("iconography_node", "empty");
							}

							String queryAuthor= "MATCH (b)-[:DEEPENS]->(c)-[:DESCRIBES]->(a) WHERE a.id='" + nodeID + "' AND NOT b.id IN [" + state.queryProb("visited").getBest().toString().replaceAll("X","'") + "] AND c.desc='author' RETURN b.speech, b.id, b.xi, b.yi, b.xf, b.yf, b.x, b.y LIMIT 1";
							res= executePost(queryAuthor);

							json = (JSONObject) parser.parse(res);
							data= (JSONArray) json.get("results");
							if (data.size() > 0) {
								json= (JSONObject) data.get(0);
								data= (JSONArray) json.get("data");
								if (data.size() > 0) {
									json= (JSONObject) data.get(0);
									data= (JSONArray) json.get("row");
									new Thread(new PrefetchProcess("Author.wav", data.get(0).toString())).start();
									dsys.addContent("author_node", data.get(1).toString());
									if (data.get(2) != null) {
										dsys.addContent("deepen_node_xi", data.get(2).toString());
										dsys.addContent("deepen_node_yi", data.get(3).toString());
										dsys.addContent("deepen_node_xf", data.get(4).toString());
										dsys.addContent("deepen_node_yf", data.get(5).toString());
									}
									else {
										dsys.addContent("deepen_node_xi", "empty");
										dsys.addContent("deepen_node_yi", "empty");
										dsys.addContent("deepen_node_xf", "empty");
										dsys.addContent("deepen_node_yf", "empty");
									}
									if (data.get(6) != null) {
										dsys.addContent("deepen_node_x", data.get(6).toString());
										dsys.addContent("deepen_node_y", data.get(7).toString());
									}
									else {
										dsys.addContent("deepen_node_x", "empty");
										dsys.addContent("deepen_node_y", "empty");
									}
								}
								else {
									dsys.addContent("author_node", "empty");
								}
							}
							else {
								dsys.addContent("author_node", "empty");
							}
						}
						else {
							query= "MATCH (b)-[:DEEPENS]->(a) WHERE a.id='" + nodeID + "' AND NOT b.id IN [" + state.queryProb("visited").getBest().toString().replaceAll("X","'") + "] RETURN b.speech, b.id, b.xi, b.yi, b.xf, b.yf, b.x, b.y LIMIT 1";
							Charset.forName("UTF-8").encode(query);

							res= executePost(query);
							parser = new JSONParser();

							json = (JSONObject) parser.parse(res);
							data= (JSONArray) json.get("results");
							if (data.size() > 0) {
								json= (JSONObject) data.get(0);
								data= (JSONArray) json.get("data");
								if (data.size() > 0) {
									json= (JSONObject) data.get(0);
									data= (JSONArray) json.get("row");
									dsys.addContent("deepen_node", data.get(1).toString().replaceAll("null","empty"));
									if (data.get(2) != null) {
										dsys.addContent("deepen_node_xi", data.get(2).toString());
										dsys.addContent("deepen_node_yi", data.get(3).toString());
										dsys.addContent("deepen_node_xf", data.get(4).toString());
										dsys.addContent("deepen_node_yf", data.get(5).toString());
									}
									else {
										dsys.addContent("deepen_node_xi", "empty");
										dsys.addContent("deepen_node_yi", "empty");
										dsys.addContent("deepen_node_xf", "empty");
										dsys.addContent("deepen_node_yf", "empty");
									}
									if (data.get(6) != null) {
										dsys.addContent("deepen_node_x", data.get(6).toString());
										dsys.addContent("deepen_node_y", data.get(7).toString());
									}
									else {
										dsys.addContent("deepen_node_x", "empty");
										dsys.addContent("deepen_node_y", "empty");
									}
									new Thread(new PrefetchProcess("deepen.wav", data.get(0).toString())).start();
								}
								else {
									dsys.addContent("deepen_node", "empty");
									dsys.addContent("deepen_node_xi", "empty");
									dsys.addContent("deepen_node_yi", "empty");
									dsys.addContent("deepen_node_xf", "empty");
									dsys.addContent("deepen_node_yf", "empty");
									dsys.addContent("deepen_node_x", "empty");
									dsys.addContent("deepen_node_y", "empty");
								}
							}
							else {
								dsys.addContent("deepen_node", "empty");
								dsys.addContent("deepen_node_xi", "empty");
								dsys.addContent("deepen_node_yi", "empty");
								dsys.addContent("deepen_node_xf", "empty");
								dsys.addContent("deepen_node_yf", "empty");
								dsys.addContent("deepen_node_x", "empty");
								dsys.addContent("deepen_node_y", "empty");
							}
						}
					}
				}
			} catch(ParseException pe){
				System.out.println("JSON Parser Exception");
			}
		}

		if (updatedVars.contains("category") && !paused) {
			
			try {
				if (!state.queryProb("category").getBest().toString().equals("R001")) {
					String query= "MATCH p= (b)-[:BELONGS_TO]->(a) WHERE a.id='" + state.queryProb("category").getBest().toString() + "' RETURN b.id, b.media, b.speech, b.name, a.name";
					Charset.forName("UTF-8").encode(query);

					String res= executePost(query);

					JSONParser parser = new JSONParser();

					JSONObject json = (JSONObject) parser.parse(res);

					JSONArray data= (JSONArray) json.get("results");
					json= (JSONObject) data.get(0);
					data= (JSONArray) json.get("data");

					List<Value> roots= new ArrayList<Value>();
					ValueFactory rootsFactory= new ValueFactory();
					List<Value> titles= new ArrayList<Value>();
					String videoMessage= "";
					JSONArray videoMessageItem= null;
					for (int i= 0; i < data.size(); i++) {
						json= (JSONObject) data.get(i);
						videoMessageItem= (JSONArray) json.get("row");
						roots.add(new StringVal(videoMessageItem.get(0).toString()));
						titles.add(new StringVal(videoMessageItem.get(0).toString() + "_" + videoMessageItem.get(3).toString()));
						videoMessage= videoMessage + videoMessageItem.get(0) + "," + videoMessageItem.get(1) + "," + videoMessageItem.get(3) + ",";
					}
					videoMessage= videoMessage + videoMessageItem.get(4).toString();
					dsys.addContent("videoMessage", videoMessage);
					SetVal rootsVal= rootsFactory.create(roots);
					dsys.addContent("roots", rootsVal);
					SetVal titlesVal= rootsFactory.create(titles);
					dsys.addContent("titles", titlesVal);

					for (int i= 0; i < data.size(); i++) {
						json= (JSONObject) data.get(i);
						JSONArray item= (JSONArray) json.get("row");
						new Thread(new PrefetchProcess(item.get(0)+ ".wav", item.get(2).toString())).start();
					}
				}
				else {
					dsys.addContent("videoMessage", "R001,Empty");
					dsys.addContent("roots", "empty");
					dsys.addContent("titles", "empty");
				}

			} catch(ParseException pe){
				System.out.println("JSON Parser Exception");
			}
		}
	}

	class PrefetchProcess implements Runnable {

		String outFileName;
		String localSsml;

		public PrefetchProcess(String fileName, String smml) {
			outFileName= fileName;
			localSsml= smml;
		}

		public void run() {

			try {
				File file = new File("./wav/" + outFileName);
				if (file.exists()) {
					file.delete();
				}
				file.createNewFile();

				String request= "http://inst0213.tts.mivoq.it/say?input%5Btype%5D=SSML&input%5Bcontent%5D=%3C%3Fxml+version%3D%221.0%22%3F%3E%3Cspeak+version%3D%221.1%22+xmlns%3D%22http%3A%2F%2Fwww.w3.org%2F2001%2F10%2Fsynthesis%22+xml%3Alang%3D%22it%22%3E%3Cprosody+rate%3D%220.9%22%3E";
				request+= URLEncoder.encode(localSsml,"UTF-8");
				request+= "</prosody></speak>" + "&input[locale]=it&output[type]=AUDIO&output[format]=WAVE_FILE&voice[gender]=male&voice[name]=roberto-hsmm&voice[age]=35&voice%5Bvariant%5D=1&utterance[style]=&utterance[effects]=";

				System.out.println("Generating file: " + outFileName);
				URL u = new URL(request);
				HttpURLConnection c = (HttpURLConnection) u.openConnection();

				String userpass = "casper" + ":" + "mycasper19";
				String basicAuth = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(userpass.getBytes());

				c.setRequestProperty ("Authorization", basicAuth);

				c.setRequestMethod("GET");
				c.setDoOutput(true);
				c.connect();

				FileOutputStream f = new FileOutputStream(new File("./wav/" + outFileName));
				InputStream in = c.getInputStream();
				byte[] buffer = new byte[1024];
				int len = 0;

				while ((len= in.read(buffer)) > 0) {
					f.write(buffer, 0, len);
				}
				f.close();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}







