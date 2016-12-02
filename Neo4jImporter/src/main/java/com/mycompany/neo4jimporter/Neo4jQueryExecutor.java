/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.neo4jimporter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
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
            builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(fileName));
            Node root = doc.getFirstChild();
            createNode(root, 0);
            String res1 = executePost("MATCH (n) REMOVE n.id");
            System.out.println(res1);

        } catch (ParserConfigurationException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (SAXException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public int createNode(Node n, int parentId) throws UnsupportedEncodingException {

        DbpediaSparql q = new DbpediaSparql();
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

            WorkEntity w = q.execQuery(n.getPreviousSibling().getPreviousSibling().getTextContent());
            AuthorEntity a = w.getAuthor();
            System.out.println(w.getName() + " " + a.getLabel());

            if (EntityRepository.authors.get(a.getLabel()) == null) {
                EntityRepository.authors.put(a.getLabel(), true);

                String birthy = (a.getBirthyear() == null) ? "null" : a.getBirthyear();
                String deathy = (a.getDeathyear() == null) ? "null" : a.getDeathyear();
                String desc = a.getDescription();
                String nat = (a.getNationality() == null) ? "null" : a.getNationality();
                String gender = (a.getGender() == null) ? "null" : a.getGender();

                //(a > b) ? a : b;
                String query = "CREATE (n:PERSON { "
                        + "name:\\\"" + a.getLabel().replace("à", "a'")
                        .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                        .replace("ò", "o'").replace("é", "e'").replace("É", "E")
                        + "\\\","
                        + "birth_year:\\\"" + birthy.replace("à", "a'")
                        .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                        .replace("ò", "o'").replace("é", "e'").replace("É", "E")
                        + "\\\","
                        + "death_year:\\\"" + deathy.replace("à", "a'")
                        .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                        .replace("ò", "o'").replace("é", "e'").replace("É", "E")
                        + "\\\","
                        + "gender:\\\"" + gender.replace("à", "a'")
                        .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                        .replace("ò", "o'").replace("é", "e'").replace("É", "E")
                        + "\\\",";

                if (desc != null) {
                    query += "description:\\\"" + desc.replace("ç", "c").replace("\"", "").replace("à", "a'")
                            .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                            .replace("ò", "o'").replace("é", "e'").replace("É", "E").replace("ì", "i'")
                            .replace("í", "i").replace("«", "").replace("»", "").replace("”", "").replace("È", "E'")
                            .replace("“", "").replace("(", "").replace(")", "").replace("’", "'").replace("–", "-")
                            + "\\\",";
                }

                query += "nationality:\\\"" + nat.replace("à", "a'")
                        .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                        .replace("ò", "o'").replace("é", "e'").replace("É", "E")
                        + "\\\"})";

                System.out.println(query);
                String res = executePost(query);
                System.out.println(res);

                ArrayList<String> subjects = a.getSubjects();
                for (String s : subjects) {

                    if (EntityRepository.subjects.get(s) == null) {
                        EntityRepository.subjects.put(s, true);
                        String querySubject = " MATCH(a:PERSON{name:\\\""
                                + a.getLabel().replace("à", "a'")
                                .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                                .replace("ò", "o'").replace("é", "e'").replace("É", "E")
                                + "\\\"})"
                                + " CREATE (n:SUBJECT{name:\\\""
                                + s.replace("\"", "").replace("à", "a'")
                                .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                                .replace("ò", "o'").replace("é", "e'").replace("É", "E").replace("ì", "i'")
                                .replace("í", "i").replace("«", "").replace("»", "").replace("”", "").replace("È", "E'")
                                .replace("“", "").replace("(", "").replace(")", "").replace("’", "'").replace("–", "-")
                                .replace("Categoria:", "").replace("http://it.dbpedia.org/resource/", "").replace("_", " ")
                                + "\\\"})"
                                + " CREATE (n)-[:IS_SUBJECT_OF]->(a)";
                        System.out.println(querySubject);
                        String resS = executePost(querySubject);
                        System.out.println(resS);
                    } else {

                        String querySubject = "MATCH (n:SUBJECT{name:\\\""
                                + s.replace("\"", "").replace("à", "a'")
                                .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                                .replace("ò", "o'").replace("é", "e'").replace("É", "E").replace("ì", "i'")
                                .replace("í", "i").replace("«", "").replace("»", "").replace("”", "").replace("È", "E'")
                                .replace("“", "").replace("(", "").replace(")", "").replace("’", "'").replace("–", "-")
                                .replace("Categoria:", "").replace("http://it.dbpedia.org/resource/", "").replace("_", " ")
                                + "\\\"}) MATCH(a:PERSON{name:\\\""
                                + a.getLabel().replace("à", "a'")
                                .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                                .replace("ò", "o'").replace("é", "e'").replace("É", "E")
                                + "\\\"}) CREATE (n)-[:IS_SUBJECT_OF]->(a)";
                        System.out.println(querySubject);
                        String resS = executePost(querySubject);
                        System.out.println(resS);
                    }
                }
                if (a.getBirthplace() != null) {

                    if (EntityRepository.cities.get(a.getBirthplace().replace("à", "a'")
                            .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                            .replace("ò", "o'").replace("é", "e'").replace("ü", "u")
                            .replace("ö", "o").replace("http://it.dbpedia.org/resource/", "")
                            .replace("_", " ")) == null) {

                        EntityRepository.cities.put(a.getBirthplace().replace("à", "a'")
                                .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                                .replace("ò", "o'").replace("é", "e'").replace("ü", "u")
                                .replace("ö", "o").replace("http://it.dbpedia.org/resource/", "")
                                .replace("_", " "), true);

                        String queryc = " MATCH(a:PERSON{name:\\\""
                                + a.getLabel().replace("à", "a'")
                                .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                                .replace("ò", "o'").replace("é", "e'").replace("É", "E")
                                + "\\\"}) CREATE (n:CITY { "
                                + "name:\\\"" + a.getBirthplace().replace("à", "a'")
                                .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                                .replace("ò", "o'").replace("é", "e'").replace("ü", "u")
                                .replace("ö", "o").replace("http://it.dbpedia.org/resource/", "")
                                .replace("_", " ")
                                + "\\\"}) CREATE (a)-[:BORN_IN]->(n)";
                        System.out.println(queryc);
                        String res1 = executePost(queryc);
                        System.out.println(res1);
                    } else {
                        String queryc = " MATCH(a:PERSON{name:\\\""
                                + a.getLabel().replace("à", "a'")
                                .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                                .replace("ò", "o'").replace("é", "e'").replace("É", "E")
                                + "\\\"}) MATCH(n:CITY { "
                                + "name:\\\"" + a.getBirthplace().replace("à", "a'")
                                .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                                .replace("ò", "o'").replace("é", "e'").replace("ü", "u")
                                .replace("ö", "o").replace("http://it.dbpedia.org/resource/", "")
                                .replace("_", " ")
                                + "\\\"}) CREATE (a)-[:BORN_IN]->(n)";
                        System.out.println(queryc);
                        String res1 = executePost(queryc);
                        System.out.println(res1);
                    }
                }
                if (a.getDeathplace() != null) {

                    if (EntityRepository.cities.get(a.getDeathplace().replace("à", "a'")
                            .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                            .replace("ò", "o'").replace("é", "e'").replace("ü", "u")
                            .replace("ö", "o").replace("http://it.dbpedia.org/resource/", "")
                            .replace("_", " ")) == null) {

                        EntityRepository.cities.put(a.getDeathplace().replace("à", "a'")
                                .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                                .replace("ò", "o'").replace("é", "e'").replace("ü", "u")
                                .replace("ö", "o").replace("http://it.dbpedia.org/resource/", "")
                                .replace("_", " "), true);

                        String queryc = " MATCH(a:PERSON{name:\\\""
                                + a.getLabel().replace("à", "a'")
                                .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                                .replace("ò", "o'").replace("é", "e'").replace("É", "E")
                                + "\\\"}) CREATE (n:CITY { "
                                + "name:\\\"" + a.getDeathplace().replace("à", "a'")
                                .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                                .replace("ò", "o'").replace("é", "e'").replace("ü", "u")
                                .replace("ö", "o").replace("http://it.dbpedia.org/resource/", "")
                                .replace("_", " ")
                                + "\\\"}) CREATE (a)-[:DEAD_IN]->(n)";
                        System.out.println(queryc);
                        String res1 = executePost(queryc);
                        System.out.println(res1);
                    } else {

                        String queryc = " MATCH(a:PERSON{name:\\\""
                                + a.getLabel().replace("à", "a'")
                                .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                                .replace("ò", "o'").replace("é", "e'").replace("É", "E")
                                + "\\\"}) MATCH(n:CITY { "
                                + "name:\\\"" + a.getDeathplace().replace("à", "a'")
                                .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                                .replace("ò", "o'").replace("é", "e'").replace("ü", "u")
                                .replace("ö", "o").replace("http://it.dbpedia.org/resource/", "")
                                .replace("_", " ")
                                + "\\\"}) CREATE (a)-[:DEAD_IN]->(n)";
                        System.out.println(queryc);
                        String res1 = executePost(queryc);
                        System.out.println(res1);
                    }
                }
            }
            boolean city = false;
            String c = "";

            if (EntityRepository.cities.get(w.getCity().replace("à", "a'")
                    .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                    .replace("ò", "o'").replace("é", "e'").replace("ü", "u")
                    .replace("ö", "o").replace("http://it.dbpedia.org/resource/", "")
                    .replace("_", " ")) == null) {

                EntityRepository.cities.put(w.getCity().replace("à", "a'")
                        .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                        .replace("ò", "o'").replace("é", "e'").replace("ü", "u")
                        .replace("ö", "o").replace("http://it.dbpedia.org/resource/", "")
                        .replace("_", " "), true);

                String query = "CREATE (n:CITY { "
                        + "name:\\\"" + w.getCity().replace("à", "a'")
                        .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                        .replace("ò", "o'").replace("é", "e'").replace("ü", "u")
                        .replace("ö", "o").replace("http://it.dbpedia.org/resource/", "")
                        .replace("_", " ")
                        + "\\\"})";
                System.out.println(query);
                String res = executePost(query);
                System.out.println(res);
                city = true;
            }
            boolean checkLocation = EntityRepository.locations.get(w.getLocation().replace("à", "a'")
                    .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                    .replace("ò", "o'").replace("é", "e'").replace("ü", "u")
                    .replace("ö", "o").replace("http://it.dbpedia.org/resource/", "")
                    .replace("_", " ")) == null;

            if (checkLocation || city) {

                if (!checkLocation) {

                    c = " di " + w.getCity().replace("à", "a'")
                            .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                            .replace("ò", "o'").replace("é", "e'").replace("ü", "u")
                            .replace("ö", "o").replace("http://it.dbpedia.org/resource/", "")
                            .replace("_", " ");
                }
                EntityRepository.locations.put(w.getLocation().replace("à", "a'")
                        .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                        .replace("ò", "o'").replace("é", "e'").replace("ü", "u")
                        .replace("ö", "o").replace("http://it.dbpedia.org/resource/", "")
                        .replace("_", " "), true);

                String query = "MATCH(c:CITY{name:\\\"" + w.getCity().replace("à", "a'")
                        .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                        .replace("ò", "o'").replace("é", "e'").replace("ü", "u")
                        .replace("ö", "o").replace("http://it.dbpedia.org/resource/", "")
                        .replace("_", " ")
                        + "\\\"}) CREATE (n:LOCATION { "
                        + "name:\\\"" + w.getLocation().replace("à", "a'")
                        .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                        .replace("ò", "o'").replace("é", "e'").replace("ü", "u")
                        .replace("ö", "o").replace("http://it.dbpedia.org/resource/", "")
                        .replace("_", " ") + c
                        + "\\\"}) CREATE (n)-[:LOCATED_IN]->(c)";

                System.out.println(query);
                String res = executePost(query);
                System.out.println(res);
            }
            System.out.println(w.toString());

            String sml = n.getFirstChild().getTextContent().replace("<prosody rate= \"0.9\" pitch= \"+60%\">", "").replace("</prosody>", "").replace("\n", "")
                    .replace("\"", "").replace("à", "a'")
                    .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                    .replace("ò", "o'").replace("é", "e'").toLowerCase();

            String query = "MATCH(l:LOCATION{name:\\\"" + w.getLocation().replace("à", "a'")
                    .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                    .replace("ò", "o'").replace("é", "e'").replace("ü", "u")
                    .replace("ö", "o").replace("http://it.dbpedia.org/resource/", "")
                    .replace("_", " ") + c
                    + "\\\"}) "
                    + "MATCH(a:PERSON{name:\\\"" + a.getLabel().replace("à", "a'")
                    .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                    .replace("ò", "o'").replace("é", "e'").replace("É", "E")
                    + "\\\"}) CREATE(n:INFO:PAINTING"
                    + "{id:'"
                    + id
                    + "', name:\\\"" + w.getName()
                    .replace("à", "a'")
                    .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                    .replace("ò", "o'").replace("é", "e'").replace("@it'", "")
                    + "\\\", date:\\\"" + w.getDate()
                    .replace("à", "a'")
                    .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                    .replace("ò", "o'").replace("é", "e'").replace("http://it.dbpedia.org/resource/", "")
                    .replace("_", " ")
                    + "\\\", height:\\\"" + w.getHeight()
                    .replace("à", "a'")
                    .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                    .replace("ò", "o'").replace("é", "e'").replace("http://it.dbpedia.org/resource/", "")
                    .replace("_", " ")
                    + "\\\", width:\\\"" + w.getWidth()
                    .replace("à", "a'")
                    .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                    .replace("ò", "o'").replace("é", "e'").replace("http://it.dbpedia.org/resource/", "")
                    .replace("_", " ")
                    + "\\\", tecnique:\\\"" + w.getTecnique()
                    .replace("à", "a'")
                    .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                    .replace("ò", "o'").replace("é", "e'").replace("http://it.dbpedia.org/resource/", "")
                    .replace("_", " ").replace("@it'", "")
                    + "\\\", sml:\\\"" + sml + "\\\"}) CREATE (a)-[:PAINTED]->(n)-[:HOSTED_IN]->(l)";

            System.out.println(query);
            String res = executePost(query);
            System.out.println(res);
            inserted = true;

            ArrayList<String> subjects = w.getSubjects();

            for (String s : subjects) {

                if (EntityRepository.subjects.get(s) == null) {

                    EntityRepository.subjects.put(s, true);
                    String querySubject = " MATCH(a:PAINTING{name:\\\""
                            + w.getName().replace("à", "a'")
                            .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                            .replace("ò", "o'").replace("é", "e'").replace("É", "E")
                            + "\\\"})"
                            + " CREATE (n:SUBJECT{name:\\\""
                            + s.replace("\"", "").replace("à", "a'")
                            .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                            .replace("ò", "o'").replace("é", "e'").replace("É", "E").replace("ì", "i'")
                            .replace("í", "i").replace("«", "").replace("»", "").replace("”", "").replace("È", "E'")
                            .replace("“", "").replace("(", "").replace(")", "").replace("’", "'").replace("–", "-")
                            .replace("Categoria:", "").replace("http://it.dbpedia.org/resource/", "").replace("_", " ")
                            .replace("ö", "o").replace("ü", "u")
                            + "\\\"})"
                            + " CREATE (n)-[:IS_SUBJECT_OF]->(a)";
                    System.out.println(querySubject);
                    String resS = executePost(querySubject);
                    System.out.println(resS);
                } else {

                    String querySubject = "MATCH (n:SUBJECT{name:\\\""
                            + s.replace("\"", "").replace("à", "a'")
                            .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                            .replace("ò", "o'").replace("é", "e'").replace("É", "E").replace("ì", "i'")
                            .replace("í", "i").replace("«", "").replace("»", "").replace("”", "").replace("È", "E'")
                            .replace("“", "").replace("(", "").replace(")", "").replace("’", "'").replace("–", "-")
                            .replace("Categoria:", "").replace("http://it.dbpedia.org/resource/", "").replace("_", " ")
                            + "\\\"}) MATCH(a:PAINTING{name:\\\""
                            + w.getName().replace("à", "a'")
                            .replace("è", "e'").replace("ù", "u'").replace("á", "a'")
                            .replace("ò", "o'").replace("é", "e'").replace("É", "E")
                            + "\\\"}) CREATE (n)-[:IS_SUBJECT_OF]->(a)";
                    System.out.println(querySubject);
                    String resS = executePost(querySubject);
                    System.out.println(resS);
                }
            }
        } else if (n.getNodeName()
                .equals("style") || n.getNodeName().equals("iconography") || n.getNodeName().equals("author")) {

            String query = "CREATE (n:ABSTRACT{id:'" + id + "', type:\\\"" + n.getNodeName() + "\\\"})";
            String res = executePost(query);
            System.out.println(res);
            inserted = true;
        } else if (n.getNodeName()
                .equals("node")) {

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
                    + "CREATE (a)-[:BELONGS_TO]->(b)";
            String res = executePost(query);
            System.out.println(res);
        }

        for (int i = 0; i < childsId.size(); i++) {

            if (i + 1 < childsId.size()) {

                String query = "MATCH (a{id:'" + childsId.get(i) + "'}),(b{id:'" + childsId.get(i + 1) + "'})"
                        + "CREATE (a)-[:SHARE_TOPIC_WITH]->(b)";
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
