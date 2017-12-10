package kkt.com.joggers.model;

public class Comment {
    private String id;
    private String time;
    private String content;

    // DataSnapshot.getValue(Comment.class)를 위한 기본생성자
    public Comment() {
    }

    public Comment(String id, String time, String content) {
        this.id = id;
        this.time = time;
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

    public String getContent() {
        return content;
    }

    public void setCotent(String comment) {
        this.content = comment;
    }
}
