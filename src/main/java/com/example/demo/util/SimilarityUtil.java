package com.example.demo.util;

import org.apache.commons.text.similarity.JaroWinklerSimilarity;

public class SimilarityUtil {
    private static final JaroWinklerSimilarity similarity = new JaroWinklerSimilarity();

    public static double similarityScore(String a, String b) {
        return similarity.apply(a, b);
    }
}

