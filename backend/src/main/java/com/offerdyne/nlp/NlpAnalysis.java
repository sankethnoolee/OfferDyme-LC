package com.offerdyne.nlp;

import java.math.BigDecimal;

/** Output of NLP analysis of a single borrower utterance. */
public class NlpAnalysis {
    private final ObjectionType objectionType;
    private final Sentiment sentiment;
    private final BigDecimal sentimentScore;
    private final boolean capacitySignalConfirmed;
    private final boolean willingnessSignal;

    public NlpAnalysis(ObjectionType o, Sentiment s, BigDecimal score,
                       boolean capacityConfirmed, boolean willingness) {
        this.objectionType = o;
        this.sentiment = s;
        this.sentimentScore = score;
        this.capacitySignalConfirmed = capacityConfirmed;
        this.willingnessSignal = willingness;
    }
    public ObjectionType getObjectionType() { return objectionType; }
    public Sentiment getSentiment() { return sentiment; }
    public BigDecimal getSentimentScore() { return sentimentScore; }
    public boolean isCapacitySignalConfirmed() { return capacitySignalConfirmed; }
    public boolean isWillingnessSignal() { return willingnessSignal; }
}
