package kkt.com.joggers.model;

public class Board {
    private String id;
    private String time;
    private String imageUrl;
    private String content;
    private boolean heart = false;
    private int heartNum;

    // DataSnapshot.getValue(Board.class)를 위한 기본생성자
    Board() {
    }

    public Board(String id, String time, String imageUrl, String content, boolean heart, int heartNum) {
        this.id = id;
        this.time = time;
        this.imageUrl = imageUrl;
        this.content = content;
        this.heart = heart;
        this.heartNum = heartNum;
    }

    public String getId() {
        return id;
    }

    void setId(String id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    void setTime(String time) {
        this.time = time;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    void setImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getContent() {
        return content;
    }

    void setContent(String content) {
        this.content = content;
    }

    public boolean isHeart() {
        return heart;
    }

    public void setHeart(boolean heart) {
        this.heart = heart;
    }

    public int getHeartNum() {
        return heartNum;
    }

    public void setHeartNum(int heartNum) {
        this.heartNum = heartNum;
    }
}
