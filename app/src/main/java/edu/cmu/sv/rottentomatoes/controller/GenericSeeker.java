package edu.cmu.sv.rottentomatoes.controller;

import java.util.ArrayList;

import edu.cmu.sv.rottentomatoes.utils.HttpRetriever;
import edu.cmu.sv.rottentomatoes.utils.jsonParser;

/**
 * Created by xingwei on 12/7/15.
 */
public abstract class GenericSeeker<E> {

    protected HttpRetriever httpRetriever = new HttpRetriever();
    protected edu.cmu.sv.rottentomatoes.utils.jsonParser jsonParser = new jsonParser();

    public abstract ArrayList<E> find(String query);
    public abstract ArrayList<E> find(String query, int maxResults);

    public abstract String retrieveSearchMethodPath();

    public ArrayList<E> retrieveFirstResults(ArrayList<E> list, int maxResults) {
        ArrayList<E> newList = new ArrayList<E>();
        int count = Math.min(list.size(), maxResults);
        for (int i=0; i<count; i++) {
            newList.add(list.get(i));
        }
        return newList;
    }

}