package util;

import java.util.Random;

public class Main {

    public static void run() {
        MinHeap rank = new MinHeapArray(5);
        for (int i = 1; i < 11; i++) {
            double point = new Random().nextDouble();
            DocumentRankingPoints d = new DocumentRankingPoints(i, point);
            System.out.println(d);
            rank.addElement(d);
        }
        rank.printRank();
    }

    public static void main(String[] args) {
        run();
    }

}
