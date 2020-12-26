package model;

import java.util.ArrayList;
import java.util.List;

public class User {
    public int id;
    public int totalWrongAnswers;
    public int totalCorrectAnswers;
    public List<Word> answeredWords = new ArrayList<>();
    public transient Word currentWord = null;

    public User(int id) {
        this.id = id;
    }

    public boolean isTranslating() {
        return currentWord != null;
    }
}
