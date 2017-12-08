package kkt.com.joggers.model;

public class Comment {
    int num;
    String time;
    String id;
    String content;

    // DataSnapshot.getValue(Comment.class)를 위한 기본생성자
    public Comment() {
    }

    public Comment(int num, String id, String content, String time) {
        this.num = num;
        this.id = id;
        this.content = content;
        this.time = time;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setCotent(String comment) {
        this.content = comment;
    }
}
