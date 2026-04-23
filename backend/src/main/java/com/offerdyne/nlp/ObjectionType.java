package com.offerdyne.nlp;

/** Borrower objection taxonomy. */
public enum ObjectionType {
    AFFORDABILITY,   // "can't afford", "too much"
    JOB_LOSS,        // "lost my job", "unemployed"
    WILLINGNESS,     // "want to pay but...", "partial"
    TIMING,          // "not this month", "next week"
    DISPUTE,         // "not mine", "wrong amount"
    ACCEPTANCE,      // "okay", "agreed"
    NONE
}
