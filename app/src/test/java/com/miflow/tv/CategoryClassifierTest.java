package com.miflow.tv;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CategoryClassifierTest {
    @Test public void classifiesStreamingApps() {
        assertEquals(AppCategory.STREAMING, CategoryClassifier.classify("Netflix", "com.netflix.ninja"));
        assertEquals(AppCategory.STREAMING, CategoryClassifier.classify("YouTube", "com.google.android.youtube.tv"));
    }

    @Test public void classifiesGames() {
        assertEquals(AppCategory.GAMES, CategoryClassifier.classify("Moonlight", "com.limelight"));
    }

    @Test public void fallsBackToTools() {
        assertEquals(AppCategory.TOOLS, CategoryClassifier.classify("Files", "com.example.files"));
    }
}
