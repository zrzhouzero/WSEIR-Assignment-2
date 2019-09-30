package util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.PriorityQueue;

public class MinHeapPriorityQueue implements MinHeap {

    private PriorityQueue<DocumentRankingPoints> minHeap;
    private int maxSize;

    public MinHeapPriorityQueue(int maxSize) {
        this.minHeap = new PriorityQueue<>();
        this.maxSize = maxSize;
    }

    @Override
    public void addElement(DocumentRankingPoints pair) {
        this.minHeap.add(pair);
        if (this.minHeap.size() > maxSize) {
            this.minHeap.poll();
        }
    }

    @Override
    public void printRank() {
        ArrayList<DocumentRankingPoints> printList = outputRank();
        for (int i = printList.size() - 1; i >= 0; i--) {
            System.out.println(printList.get(i));
        }
    }

    @Override
    public ArrayList<DocumentRankingPoints> outputRank() {
        ArrayList<DocumentRankingPoints> outputList = new ArrayList<>();
        int minHeapSize = minHeap.size();
        for (int i = 0; i < minHeapSize; i++) {
            outputList.add(minHeap.poll());
        }
        Collections.reverse(outputList);
        return outputList;
    }

}
