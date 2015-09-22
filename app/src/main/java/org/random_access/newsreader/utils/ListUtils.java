package org.random_access.newsreader.utils;

import java.util.ArrayList;

/**
 * <b>Project:</b> NewsReader for Android <br>
 * <b>Date:</b> 22.09.15 <br>
 * <b>Author:</b> Monika Schrenk <br>
 * <b>E-Mail:</b> software@random-access.org <br>
 */
public class ListUtils<T> {

    /**
     * Convert all elements of type T in the given ArrayList to Strings (depending on the toString()
     * implementation of type T) and build an Array holding these elements.
     * @param list an ArrayList with elements of type T
     * @return an array with the toString() representation of all elements
     * in the given ArrayList
     */
    public String[] convertArrayListToStringArray(ArrayList<T> list) {
        String [] stringArray = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            stringArray[i] = (list.get(i)).toString();
        }
        return stringArray;
    }

}
