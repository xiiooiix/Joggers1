package kkt.com.joggers.model;

import java.util.Map;

public class Board {
    private String id;
    private String time;
    private String imageUrl;
    private String content;
    private Map<String, String> heart;
    private Map<String, Comment> comment;

    // DataSnapshot.getValue(Board.class)를 위한 기본생성자
    Board() {
    }

    public Board(String id, String time, String imageUrl, String content) {
        this.id = id;
        this.time = time;
        this.imageUrl = imageUrl;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, String> getHeart() {
        return heart;
    }

    public void setHeart(Map<String, String> heart) {
        this.heart = heart;
    }

    public Map<String, Comment> getComment() {
        return comment;
    }

    public void setComment(Map<String, Comment> comment) {
        this.comment = comment;
    }
}