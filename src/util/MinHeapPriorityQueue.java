package util;

import java.util.ArrayList;
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
        ArrayList<DocumentRankingPoints> printList = new ArrayList<>();
        int minHeapSize = minHeap.size();
        for (int i = 0; i < minHeapSize; i++) {
            printList.add(minHeap.poll());
        }
        for (int i = minHeapSize - 1; i >= 0; i--) {
            System.out.println(printList.get(i));
        }
    }

}
