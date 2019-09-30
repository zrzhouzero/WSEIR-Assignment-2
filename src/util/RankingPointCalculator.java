package util;

public class RankingPointCalculator {

    private static final double k1 = 1.2;
    private static final double b = 0.75;

    public static double BM25(double N, double ft, double fdt, double Ld, double AL) {
        double K = k1 * ((1 - b) + b * Ld / AL);
        double inLog = (N - ft + 0.5) / (ft + 0.5);
        double left = Math.log(inLog);
        double right = ((k1 + 1) * fdt) / (K + fdt);
        return left * right;
    }

}
