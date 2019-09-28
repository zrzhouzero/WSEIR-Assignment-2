package util;

import java.util.ArrayList;

public class MinHeapArray implements MinHeap {

    private DocumentRankingPoints[] minHeap;
    private int maxSize;
    private int currentSize;

    public MinHeapArray(int maxSize) {
        this.minHeap = new DocumentRankingPoints[maxSize];
        this.maxSize = maxSize;
        this.currentSize = 0;
    }

    @Override
    public void addElement(DocumentRankingPoints pair) {
        if (currentSize < maxSize) {
            currentSize++;
            minHeap[currentSize - 1] = pair;
            reformHeap();
        } else {
            if (pair.compareTo(minHeap[0]) > 0) {
                minHeap[0] = pair;
                reformHeap();
            }
        }
    }

    private DocumentRankingPoints poll() {
        if (this.currentSize == 0) {
            return null;
        } else {
            DocumentRankingPoints out = this.minHeap[0];
            if (currentSize - 1 >= 0) System.arraycopy(minHeap, 1, minHeap, 0, currentSize - 1);
            minHeap[currentSize - 1] = null;
            currentSize--;
            reformHeap();
            return out;
        }
    }

    @Override
    public void printRank() {
        ArrayList<DocumentRankingPoints> printList = new ArrayList<>();
        while (currentSize > 0) {
            printList.add(poll());
        }
        for (int i = printList.size() - 1; i >= 0; i--) {
            System.out.println(printList.get(i));
        }
    }

    private void reformHeap() {
        for (int i = currentSize - 1; i >= 0; i--) {
            reformNode(i);
        }
        for (int i = 0; i < currentSize; i++) {
            reformNode(i);
        }
    }

    private void reformNode(int index) {
        if (index > currentSize - 1) return;
        if (getLeftChildIndex(index) <= currentSize - 1) {
            if (this.minHeap[index].compareTo(this.minHeap[getLeftChildIndex(index)]) > 0) {
                swap(index, getLeftChildIndex(index));
            }
        }
        if (getRightChildIndex(index) <= currentSize - 1) {
            if (this.minHeap[index].compareTo(this.minHeap[getRightChildIndex(index)]) > 0) {
                swap(index, getRightChildIndex(index));
            }
        }
    }

    private void swap(int index1, int index2) {
        DocumentRankingPoints temp = this.minHeap[index1];
        this.minHeap[index1] = this.minHeap[index2];
        this.minHeap[index2] = temp;
    }

    public int getParentIndex(int index) {
        if (index == 0) {
            return 0;
        } else {
            return (index - 1) / 2;
        }
    }

    private int getLeftChildIndex(int index) {
        return 2 * index + 1;
    }

    private int getRightChildIndex(int index) {
        return 2 * index + 2;
    }

}
