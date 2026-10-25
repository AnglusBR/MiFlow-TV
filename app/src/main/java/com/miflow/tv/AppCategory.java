package com.miflow.tv;

enum AppCategory {
    ALL("Todos"),
    FAVORITES("Favoritos"),
    STREAMING("Streaming"),
    GAMES("Jogos"),
    TOOLS("Utilitários");

    final String title;

    AppCategory(String title) {
        this.title = title;
    }
}
