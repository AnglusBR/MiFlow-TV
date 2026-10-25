package com.miflow.tv;

import java.util.Locale;

final class CategoryClassifier {
    private CategoryClassifier() {}

    static AppCategory classify(String label, String packageName) {
        String text = (label + " " + packageName).toLowerCase(Locale.ROOT);
        if (containsAny(text, "netflix", "youtube", "prime", "disney", "hbo", "max", "globoplay",
                "spotify", "plex", "jellyfin", "twitch", "video", "tv", "stream", "media")) {
            return AppCategory.STREAMING;
        }
        if (containsAny(text, "game", "jogo", "retro", "steam", "geforce", "xbox", "play games",
                "emulator", "arcade", "moonlight")) {
            return AppCategory.GAMES;
        }
        return AppCategory.TOOLS;
    }

    private static boolean containsAny(String source, String... needles) {
        for (String needle : needles) {
            if (source.contains(needle)) return true;
        }
        return false;
    }
}
