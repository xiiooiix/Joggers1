package kkt.com.joggers.model;

import java.util.List;

public class Board {
    private String id;
    private String time;
    private String imageUrl;
    private String content;
    private boolean heart = false;
    private int heartNum;
    private int num=0;
    private String comment;
    // DataSnapshot.getValue(Board.class)를 위한 기본생성자
    Board() {}

    public Board(String id, String time, String imageUrl, String content, boolean heart, int heartNum, int num) {
        this.id = id;
        this.time = time;
        this.imageUrl = imageUrl;
        this.content = content;
        this.heart = heart;
        this.heartNum = heartNum;
        this.num = num;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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

    void setContent(String content) {
        this.content = content;
    }

    public void setHeart(boolean heart) {
        this.heart = heart;
    }

    public boolean getHeart(){ return heart; }

    public int getHeartNum() {
        return heartNum;
    }

    public void setHeartNum(int heartNum) {
        this.heartNum = heartNum;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }
}
