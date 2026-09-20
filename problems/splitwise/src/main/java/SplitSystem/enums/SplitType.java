package SplitSystem.enums;

/**
 * How an expense is divided among its participants.
 * EQUAL   - every participant owes the same share.
 * EXACT   - caller supplies the exact amount each participant owes; must add up to the total.
 * PERCENT - caller supplies a percentage per participant; must add up to 100.
 */
public enum SplitType {
    EQUAL,
    EXACT,
    PERCENT
}
