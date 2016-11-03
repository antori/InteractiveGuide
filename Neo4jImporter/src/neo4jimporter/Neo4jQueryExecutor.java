/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neo4jimporter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Marco
 */
public class Neo4jQueryExecutor {

    private String neo4jIP = "localhost";
    private int neo4jPort = 7474;
    private int id = 0;

    protected String executePost(String query) {

        HttpURLConnection connection = null;
        try {
            // Create connection
            URL url = new URL("http://" + neo4jIP + ":" + neo4jPort + "/db/data/transaction/commit");
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Authorization", "Basic bmVvNGo6TmFwb2xpOTE=");

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

    public void createPaintingsDatabase(String fileName) {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            String res = executePost("MATCH (n) DETACH DELETE n");
            System.out.println(res);
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(fileName));
            Node root = doc.getFirstChild();
            createNode(root, 0);
            res = executePost("MATCH (n) REMOVE n.id");
            System.out.println(res);

        } catch (ParserConfigurationException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (SAXException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public int createNode(Node n, int parentId) throws UnsupportedEncodingException {

        NodeList childs = n.getChildNodes();
        boolean inserted = false;
        id++;
        int tempId = id;
        ArrayList<Integer> childsId = new ArrayList<Integer>();

        if (n.getNodeName().equals("cultural_heritages")) {
            String query = "CREATE (n:CULTURAL_HERITAGES{id:'" + id + "',name:'paintings'})";
            String res = executePost(query);
            System.out.println(res);
            inserted = true;
        } else if (n.getNodeName().equals("category")) {
            String query = "CREATE (n:CATEGORY{id:'" + id + "', name:'" + n.getAttributes().getNamedItem("name").getNodeValue() + "'})";
            String res = executePost(query);
            System.out.println(res);
            inserted = true;
        } else if (n.getNodeName().equals("presentation")) {
            String sml = n.getFirstChild().getTextContent().replace("<prosody rate= \"0.9\" pitch= \"+60%\">", "").replace("</prosody>", "").replace("\n", "")
                    .replace("\"", "").replace("à", "a'")
                    .replace("è", "e").replace("ù", "u").replace("á", "a'")
                    .replace("ò", "o'").replace("é", "e'").toLowerCase();
            String query = "CREATE (n:INFO{id:'" + id + "', name:'"+n.getPreviousSibling().getPreviousSibling().getTextContent()+"', sml:\\\"" + sml + "\\\"})";
            String res = executePost(query);
            System.out.println(res);
            inserted = true;
        } else if (n.getNodeName().equals("style") || n.getNodeName().equals("iconography") || n.getNodeName().equals("author")) {
            String query = "CREATE (n:ABSTRACT{id:'" + id + "', type:\\\"" + n.getNodeName() + "\\\"})";
            String res = executePost(query);
            System.out.println(res);
            inserted = true;
        } else if (n.getNodeName().equals("node")) {
            String sml = n.getFirstChild().getTextContent().replace("<prosody rate= \"0.9\" pitch= \"+60%\">", "").replace("</prosody>", "").replace("\n", "")
                    .replace("\"", "").replace("à", "a'").replace("ì", "i'")
                    .replace("è", "e").replace("ù", "u").replace("á", "a'")
                    .replace("ò", "o'").replace("é", "e'").toLowerCase();
            String query = "CREATE (n:INFO{id:'" + id + "', name:'";
            if (n.getAttributes().getNamedItem("id") != null) {
                query += n.getAttributes().getNamedItem("id").getNodeValue();
            }
            if (n.getAttributes().getNamedItem("media") != null) {
                query += "', media:'" + n.getAttributes().getNamedItem("media").getNodeValue();
            }
            if (n.getAttributes().getNamedItem("xf") != null) {
                query += "',xf:'" + n.getAttributes().getNamedItem("xf").getNodeValue();
            }
            if (n.getAttributes().getNamedItem("xi") != null) {
                query += "',xi:'" + n.getAttributes().getNamedItem("xi").getNodeValue();
            }
            if (n.getAttributes().getNamedItem("yf") != null) {
                query += "',yf:'" + n.getAttributes().getNamedItem("yf").getNodeValue();
            }
            if (n.getAttributes().getNamedItem("yi") != null) {
                query += "',yi:'" + n.getAttributes().getNamedItem("yi").getNodeValue();
            }
            query += "', sml:\\\"" + sml + "\\\"})";
            String res = executePost(query);
            System.out.println(res);
            inserted = true;
        }

        for (int i = 0; i < childs.getLength(); i++) {
            Node child = childs.item(i);
            if (inserted) {
                int childId = createNode(child, tempId);
                if (childId > 0) {
                    childsId.add(childId);
                }
            } else {
                createNode(child, parentId);
            }
        }

        if (inserted && parentId > 0) {
            String query = "MATCH (a{id:'" + tempId + "'}),(b{id:'" + parentId + "'})"
                    + "CREATE (a)-[:BELONGS]->(b)";
            String res = executePost(query);
            System.out.println(res);
        }

        for (int i = 0; i < childsId.size(); i++) {
            if (i + 1 < childsId.size()) {
                String query = "MATCH (a{id:'" + childsId.get(i) + "'}),(b{id:'" + childsId.get(i + 1) + "'})"
                        + "CREATE (a)-[:SHARE_TOPIC]->(b)";
                String res = executePost(query);
                System.out.println(res);
            }
        }

        if (n.getNodeName().equals("node")) {
            return tempId;
        }

        return -1;
    }

}
