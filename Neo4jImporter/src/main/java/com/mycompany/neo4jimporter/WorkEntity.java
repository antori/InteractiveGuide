/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mycompany.neo4jimporter;

import java.util.ArrayList;

/**
 *
 * @author Marco
 */
public class WorkEntity {
    
    public static final String NAME_FIELD = "http://it.dbpedia.org/property/titolo";
    public static final String AUTHOR_FIELD = "http://it.dbpedia.org/property/artista";
    public static final String SUBJECT_FIELD = "http://purl.org/dc/terms/subject";
    public static final String WIKILINK_FIELD = "http://dbpedia.org/ontology/wikiPageWikiLink";
    public static final String TECIQUE_FIELD = "http://it.dbpedia.org/property/tecnica";
    public static final String TYPE_FIELD = "http://it.dbpedia.org/property/opera";
    public static final String DATE_FIELD = "http://it.dbpedia.org/property/data";
    public static final String LOCATION_FIELD = "http://it.dbpedia.org/property/ubicazione";
    public static final String CITY_FIELD = "http://it.dbpedia.org/property/città";
    public static final String HEIGHT_FIELD = "http://it.dbpedia.org/property/altezza";
    public static final String WIDTH_FIELD = "http://it.dbpedia.org/property/larghezza";
    
    private String name;
    private String author;
    private ArrayList<String> subjects = new ArrayList<String>();
    private ArrayList<String> wikiLinks = new ArrayList<String>();
    private String tecnique;
    private String type;
    private String date;
    private String location;
    private String city;
    private String height;
    private String width;

    
    @Override
    public String toString(){
        return "=========================\n"+
                "nome: "+getName()+"\n"+
                "autore: "+getAuthor()+"\n"+
                "=========================\n";
    }

    /**
     * @return the subjects
     */
    public ArrayList<String> getSubjects() {
        return subjects;
    }

    /**
     * @param subjects the subjects to set
     */
    public void setSubjects(String subject) {
       this.subjects.add(subject);
    }

    /**
     * @return the wikiLinks
     */
    public ArrayList<String> getWikiLinks() {
        return wikiLinks;
    }

    /**
     * @param wikiLinks the wikiLinks to set
     */
    public void setWikiLinks(String wikiLink) {
        this.wikiLinks.add(wikiLink);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @param author the author to set
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * @return the tecnique
     */
    public String getTecnique() {
        return tecnique;
    }

    /**
     * @param tecnique the tecnique to set
     */
    public void setTecnique(String tecnique) {
        this.tecnique = tecnique;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * @param city the city to set
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * @return the height
     */
    public String getHeight() {
        return height;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(String height) {
        this.height = height;
    }

    /**
     * @return the width
     */
    public String getWidth() {
        return width;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(String width) {
        this.width = width;
    }
    
}
