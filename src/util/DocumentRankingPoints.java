package util;

public class DocumentRankingPoints implements Comparable<DocumentRankingPoints> {

    private int documentId;
    private double documentPoint;

    public DocumentRankingPoints(int documentId, double documentPoint) {
        this.documentId = documentId;
        this.documentPoint = documentPoint;
    }

    public int getDocumentId() {
        return documentId;
    }

    public double getDocumentPoint() {
        return documentPoint;
    }

    @Override
    public int compareTo(DocumentRankingPoints o) {
        return Double.compare(this.documentPoint, o.documentPoint);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return documentId + " " + documentPoint;
    }
}
