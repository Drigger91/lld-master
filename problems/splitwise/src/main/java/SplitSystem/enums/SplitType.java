package SplitSystem.enums;

public enum SplitType {
    CUSTOM(), EQUAL(50);
    SplitType() {
    }
    SplitType(int splitPercent) {
        this.splitPercent = splitPercent;
    }
    private int splitPercent;
    public int getSplitPercent() {
        return this.splitPercent;
    }
    public void setSplitPercent(int splitPercent) {
        this.splitPercent = splitPercent;
    }
}
