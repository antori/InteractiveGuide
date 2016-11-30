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
    private String authorQuery;
    private String locationQuery;

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
                            w.setAuthor(createAuthor(value.asResource().toString()));
                            break;
                        case WorkEntity.SUBJECT_FIELD:
                            w.setSubjects(value.asResource().toString());
                            break;
                        case WorkEntity.LOCATION_FIELD:
                            w.setLocation(createLocation(value.asResource().toString()));
                            break;
                        case WorkEntity.CITY_FIELD:
                            w.setCity(value.asResource().toString());
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
                            w.setAuthor(createAuthor(value.asLiteral().getString()));
                            break;
                        case WorkEntity.SUBJECT_FIELD:
                            w.setSubjects(value.asLiteral().getString());
                            break;
                        case WorkEntity.LOCATION_FIELD:
                            w.setLocation(createLocation(value.asLiteral().getString()));
                            break;
                        case WorkEntity.CITY_FIELD:
                            w.setCity(value.asLiteral().getString());
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

    private String createLocation(String name) {

        locationQuery = "SELECT ?value WHERE {\n"
                + " <" + name + "> <http://www.w3.org/2000/01/rdf-schema#label> ?value.\n"
                + "}";

        QueryExecution qe = QueryExecutionFactory.sparqlService(sparqlEndpoint, locationQuery);
        ResultSet rs = qe.execSelect();

        AuthorEntity w = new AuthorEntity();

        while (rs.hasNext()) {

            QuerySolution s = rs.next();
            RDFNode value = s.get("?value");
            if (value.isResource()) {
                if(value.asResource().toString().contains("Louvre")){
                    return "Museo del Louvre";
                }
                return value.asResource().toString();
            } else {
                if(value.asLiteral().getString().contains("Louvre")){
                    return "Museo del Louvre";
                }
                return value.asLiteral().getString();
            }
        }

        return null;
    }

    private AuthorEntity createAuthor(String name) {

        authorQuery = "SELECT ?property ?value WHERE {\n"
                + " <" + name + "> ?property ?value.\n"
                + "}";

        QueryExecution qe = QueryExecutionFactory.sparqlService(sparqlEndpoint, authorQuery);
        ResultSet rs = qe.execSelect();

        AuthorEntity w = new AuthorEntity();

        while (rs.hasNext()) {

            QuerySolution s = rs.next();
            //RDFNode work = s.get("?work");
            RDFNode property = s.get("?property");
            RDFNode value = s.get("?value");
            if (value.isResource()) {
                switch (property.toString()) {
                    case AuthorEntity.LABEL_FIELD:
                        w.setLabel(value.asResource().toString());
                        break;
                    case AuthorEntity.NAME_FIELD:
                        w.setName(value.asResource().toString());
                        break;
                    case AuthorEntity.SUBJECT_FIELD:
                        w.setSubjects(value.asResource().toString());
                        break;
                    case AuthorEntity.BIRTHPLACE_FIELD:
                        w.setBirthplace(value.asResource().toString());
                        break;
                    case AuthorEntity.DEATHPLACE_FIELD:
                        w.setDeathplace(value.asResource().toString());
                        break;
                    case AuthorEntity.BIRTHYEAR_FIELD:
                        w.setBirthyear(value.asResource().toString());
                        break;
                    case AuthorEntity.DEATHYEAR_FIELD:
                        w.setDeathyear(value.asResource().toString());
                        break;
                    case AuthorEntity.DESCRIPTION_FIELD:
                        w.setDescription(value.asResource().toString());
                        break;
                    case AuthorEntity.GENDER_FIELD:
                        w.setGender(value.asResource().toString());
                        break;
                    case AuthorEntity.SURNAME_FIELD:
                        w.setSurname(value.asResource().toString());
                        break;
                    case AuthorEntity.NATIONALITY_FIELD:
                        w.setNationality(value.asResource().toString());
                        break;
                }
            } else {
                switch (property.toString()) {
                    case AuthorEntity.LABEL_FIELD:
                        w.setLabel(value.asLiteral().getString());
                        break;
                    case AuthorEntity.NAME_FIELD:
                        w.setName(value.asLiteral().getString());
                        break;
                    case AuthorEntity.SUBJECT_FIELD:
                        w.setSubjects(value.asLiteral().getString());
                        break;
                    case AuthorEntity.BIRTHPLACE_FIELD:
                        w.setBirthplace(value.asLiteral().getString());
                        break;
                    case AuthorEntity.DEATHPLACE_FIELD:
                        w.setDeathplace(value.asLiteral().getString());
                        break;
                    case AuthorEntity.BIRTHYEAR_FIELD:
                        w.setBirthyear(value.asLiteral().getString());
                        break;
                    case AuthorEntity.DEATHYEAR_FIELD:
                        w.setDeathyear(value.asLiteral().getString());
                        break;
                    case AuthorEntity.DESCRIPTION_FIELD:
                        w.setDescription(value.asLiteral().getString());
                        break;
                    case AuthorEntity.GENDER_FIELD:
                        w.setGender(value.asLiteral().getString());
                        break;
                    case AuthorEntity.SURNAME_FIELD:
                        w.setSurname(value.asLiteral().getString());
                        break;
                    case AuthorEntity.NATIONALITY_FIELD:
                        w.setNationality(value.asLiteral().getString());
                        break;
                }
            }
        }

        return w;
    }

}
