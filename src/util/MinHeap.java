package util;

import java.util.ArrayList;

public interface MinHeap {

    void addElement(DocumentRankingPoints pair);

    void printRank();

    ArrayList<DocumentRankingPoints> outputRank();

}
