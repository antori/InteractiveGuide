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
public class AuthorEntity {
    public static final String LABEL_FIELD = "http://www.w3.org/2000/01/rdf-schema#label";
    public static final String NAME_FIELD = "http://it.dbpedia.org/property/nome";
    public static final String SURNAME_FIELD = "http://it.dbpedia.org/property/cognome";
    public static final String GENDER_FIELD = "http://it.dbpedia.org/property/sesso";
    public static final String BIRTHPLACE_FIELD = "http://it.dbpedia.org/property/luogonascita";
    public static final String DEATHPLACE_FIELD = "http://it.dbpedia.org/property/luogomorte";
    public static final String BIRTHYEAR_FIELD = "http://it.dbpedia.org/property/annonascita";
    public static final String DEATHYEAR_FIELD = "http://it.dbpedia.org/property/annomorte";
    public static final String JOB_FIELD = "http://it.dbpedia.org/property/attività";
    public static final String NATIONALITY_FIELD = "http://it.dbpedia.org/property/nazionalità";
    public static final String EPOQUE_FIELD = "http://it.dbpedia.org/property/epoca";
    public static final String SUBJECT_FIELD = "http://purl.org/dc/terms/subject";
    public static final String DESCRIPTION_FIELD = "http://dbpedia.org/ontology/abstract";
    public static final String REDIRECT_FIELD = "http://dbpedia.org/ontology/wikiPageRedirects";
    
    private String label;
    private String name;
    private String surname;
    private String gender;
    private String birthplace;
    private String deathplace;
    private String deathyear;
    private String birthyear;
    private String job;
    private String nationality;
    private String epoque;
    private ArrayList<String> subjects = new ArrayList<String>();
    private String description;

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
     * @return the surname
     */
    public String getSurname() {
        return surname;
    }

    /**
     * @param surname the surname to set
     */
    public void setSurname(String surname) {
        this.surname = surname;
    }

    /**
     * @return the gender
     */
    public String getGender() {
        return gender;
    }

    /**
     * @param gender the gender to set
     */
    public void setGender(String gender) {
        this.gender = gender;
    }

    /**
     * @return the birthplace
     */
    public String getBirthplace() {
        return birthplace;
    }

    /**
     * @param birthplace the birthplace to set
     */
    public void setBirthplace(String birthplace) {
        this.birthplace = birthplace;
    }

    /**
     * @return the deathplace
     */
    public String getDeathplace() {
        return deathplace;
    }

    /**
     * @param deathplace the deathplace to set
     */
    public void setDeathplace(String deathplace) {
        this.deathplace = deathplace;
    }

    /**
     * @return the deathyear
     */
    public String getDeathyear() {
        return deathyear;
    }

    /**
     * @param deathyear the deathyear to set
     */
    public void setDeathyear(String deathyear) {
        this.deathyear = deathyear;
    }

    /**
     * @return the birthyear
     */
    public String getBirthyear() {
        return birthyear;
    }

    /**
     * @param birthyear the birthyear to set
     */
    public void setBirthyear(String birthyear) {
        this.birthyear = birthyear;
    }

    /**
     * @return the job
     */
    public String getJob() {
        return job;
    }

    /**
     * @param job the job to set
     */
    public void setJob(String job) {
        this.job = job;
    }

    /**
     * @return the nationality
     */
    public String getNationality() {
        return nationality;
    }

    /**
     * @param nationality the nationality to set
     */
    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    /**
     * @return the epoque
     */
    public String getEpoque() {
        return epoque;
    }

    /**
     * @param epoque the epoque to set
     */
    public void setEpoque(String epoque) {
        this.epoque = epoque;
    }

    /**
     * @return the subjects
     */
    public ArrayList<String> getSubjects() {
        return subjects;
    }

    /**
     * @param subject the subjects to set
     */
    public void setSubjects(String subject) {
        this.subjects.add(subject);
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }
     
}
