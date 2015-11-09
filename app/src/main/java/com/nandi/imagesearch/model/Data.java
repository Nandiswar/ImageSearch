package com.nandi.imagesearch.model;

import java.io.Serializable;

/**
 * Created by nandi_000 on 08-11-2015.
 * Each image attributes - link: image link, title: image title
 */
public class Data implements Serializable {

    public String title;
    public String link;

    /*@SuppressWarnings("serial")
    public static class List extends ArrayList<Data> {
    }*/
    //public List<Data> data;
}
