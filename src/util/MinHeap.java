package util;

import java.util.ArrayList;

public interface MinHeap {

    /**
     * add a document ID, document ranking point pair into the min heap
     *
     * @param pair the inserting pair
     */
    void addElement(DocumentRankingPoints pair);

    /**
     * print the ranking result
     */
    void printRank();

    /**
     * output the ranking result as a list
     *
     * @return the ranking result list
     */
    ArrayList<DocumentRankingPoints> outputRank();

}
