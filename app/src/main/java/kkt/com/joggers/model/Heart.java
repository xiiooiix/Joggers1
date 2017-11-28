package kkt.com.joggers.model;

public class Heart {
    private String id;
    private boolean like;
    private int num;

    public Heart(){}

    public Heart(String id, boolean like, int num){
        this.id = id;
        this.like = like;
        this.num = num;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public boolean isLike() {
        return like;
    }

    public void setLike(boolean like) {
        this.like = like;
    }
}
