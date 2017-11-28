package kkt.com.joggers.model;

public class User {
    private String id;
    private int board[];
    private int heart[];

    public User(){}

    public User(String id, int board[], int heart[]){
        this.id = id;
        this.board = board;
        this.heart = heart;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int[] getBoard() {
        return board;
    }

    public void setBoard(int[] board) {
        this.board = board;
    }

    public int[] getHeart() {
        return heart;
    }

    public void setHeart(int[] heart) {
        this.heart = heart;
    }
}
