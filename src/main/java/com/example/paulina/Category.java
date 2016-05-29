package com.example.paulina;


import java.util.HashMap;
import java.util.Map;


public class Category {

    private String category;
    private String categoryAlias;
    private int numberOfWords;
    private int numberOfGuessedWords;
    private boolean available;
    private static final Map<String, String> allCategories;

    static {
        allCategories = new HashMap<>();
        allCategories.put("countries", "countries");
        allCategories.put("famous", "Famous people");
        allCategories.put("bands", "Famous bands");
        allCategories.put("food", "International food");
        allCategories.put("books", "Bestseller books");
    }

    public String getCategoryAlias() {
        return categoryAlias;
    }

    public void setNumberOfGuessedWords(int numberOfGuessedWords) {
        this.numberOfGuessedWords = numberOfGuessedWords;
    }

    public int getNumberOfWords() {
        return numberOfWords;
    }

    public int getNumberOfGuessedWords() {
        return numberOfGuessedWords;
    }

    public String getCategory() {
        return category;
    }


    public boolean isAvailable() {
        return available;
    }


    public Category(String category, boolean available, int numberOfWords, int numberOfGuessedWords) {
        String catName = category.toLowerCase();
        String catAlias = (allCategories.get(catName) == null) ? catName : allCategories.get(catName);
        this.category = category;
        this.categoryAlias = catAlias;
        this.available = available;
        this.numberOfWords = numberOfWords;
        this.numberOfGuessedWords = numberOfGuessedWords;
    }
}
