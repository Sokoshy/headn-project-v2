package com.bibliotheque.model.preparation;

public record SelectableBook(
        Long id,
        String title,
        String author
) {}