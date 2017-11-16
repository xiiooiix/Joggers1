package kkt.com.joggers.board;

class Board {
    private String id;
    private String time;
    private String imageUrl;
    private String content;
    private boolean heart = false;
    private int heartNum;

    Board() {
    }

    Board(String id, String time, String imageUrl, String content, boolean heart, int heartNum) {
        this.id = id;
        this.time = time;
        this.imageUrl = imageUrl;
        this.content = content;
        this.heart = heart;
        this.heartNum = heartNum;
    }

    String getId() {
        return id;
    }

    void setId(String id) {
        this.id = id;
    }

    String getTime() {
        return time;
    }

    void setTime(String time) {
        this.time = time;
    }

    String getImageUrl() {
        return imageUrl;
    }

    void setImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    String getContent() {
        return content;
    }

    void setContent(String content) {
        this.content = content;
    }

    boolean isHeart() {
        return heart;
    }

    void setHeart(boolean heart) {
        this.heart = heart;
    }

    int getHeartNum() {
        return heartNum;
    }

    void setHeartNum(int heartNum) {
        this.heartNum = heartNum;
    }
}
