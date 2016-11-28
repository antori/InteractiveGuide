/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.neo4jimporter;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.RDFNode;

/**
 *
 * @author Marco
 */
public class DbpediaSparql {

    private String sparqlEndpoint = "http://it.dbpedia.org/sparql";

    // get expression values for uniprot acc Q16850
    private String sparqlQuery;

    public WorkEntity execQuery(String name) {

        WorkEntity w = new WorkEntity();

        sparqlQuery = "SELECT ?work ?property ?value WHERE {"
                + "  ?work a <http://dbpedia.org/ontology/Artwork> ."
                + "  ?work <http://www.w3.org/2000/01/rdf-schema#label> \"" + name + "\"@it."
                + "  ?work ?property ?value."
                + "}";

        QueryExecution qe = QueryExecutionFactory.sparqlService(sparqlEndpoint, sparqlQuery);
        ResultSet rs = qe.execSelect();

        while (rs.hasNext()) {

            QuerySolution s = rs.next();
            //RDFNode work = s.get("?work");
            RDFNode property = s.get("?property");
            RDFNode value = s.get("?value");
            if (property.toString().contains("property") || property.toString().contains("subject") || property.toString().contains("WikiLink")) {
                if (value.isResource()) {
                    switch (property.toString()) {
                        case WorkEntity.AUTHOR_FIELD:
                            w.setAuthor(value.asResource().toString());
                            break;
                        case WorkEntity.CITY_FIELD:
                            w.setCity(value.asResource().toString());
                            break;
                        case WorkEntity.SUBJECT_FIELD:
                            w.setSubjects(value.asResource().toString());
                            break;
                        case WorkEntity.WIKILINK_FIELD:
                            w.setWikiLinks(value.asResource().toString());
                            break;
                        case WorkEntity.LOCATION_FIELD:
                            w.setLocation(value.asResource().toString());
                            break;
                        case WorkEntity.TECIQUE_FIELD:
                            w.setTecnique(value.asResource().toString());
                            break;
                        case WorkEntity.TYPE_FIELD:
                            w.setType(value.asResource().toString());
                            break;
                        case WorkEntity.HEIGHT_FIELD:
                            w.setHeight(value.asResource().toString());
                            break;
                        case WorkEntity.WIDTH_FIELD:
                            w.setWidth(value.asResource().toString());
                            break;
                        case WorkEntity.DATE_FIELD:
                            w.setDate(value.asResource().toString());
                            break;
                        case WorkEntity.NAME_FIELD:
                            w.setName(value.asResource().toString());
                            break;
                    }
                } else {
                    switch (property.toString()) {
                        case WorkEntity.AUTHOR_FIELD:
                            w.setAuthor(value.asLiteral().getString());
                            break;
                        case WorkEntity.CITY_FIELD:
                            w.setCity(value.asLiteral().getString());
                            break;
                        case WorkEntity.SUBJECT_FIELD:
                            w.setSubjects(value.asLiteral().getString());
                            break;
                        case WorkEntity.WIKILINK_FIELD:
                            w.setWikiLinks(value.asLiteral().getString());
                            break;
                        case WorkEntity.LOCATION_FIELD:
                            w.setLocation(value.asLiteral().getString());
                            break;
                        case WorkEntity.TECIQUE_FIELD:
                            w.setTecnique(value.asLiteral().getString());
                            break;
                        case WorkEntity.TYPE_FIELD:
                            w.setType(value.asLiteral().getString());
                            break;
                        case WorkEntity.HEIGHT_FIELD:
                            w.setHeight(value.asLiteral().getString());
                            break;
                        case WorkEntity.WIDTH_FIELD:
                            w.setWidth(value.asLiteral().getString());
                            break;
                        case WorkEntity.DATE_FIELD:
                            w.setDate(value.asLiteral().getString());
                            break;
                        case WorkEntity.NAME_FIELD:
                            w.setName(value.asLiteral().getString());
                            break;
                    }
                }
            }
        }

        return w;
    }
}
