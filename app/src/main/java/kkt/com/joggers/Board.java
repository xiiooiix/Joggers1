package kkt.com.joggers;

import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.sql.Time;
import java.util.ArrayList;

/**
 * Created by youngjae on 2017-11-11.
 */

public class Board {
    private String id;
    private String time;
    private Drawable image;
    private String context;
    private boolean hart = false;
    private int hartNum =0;

    Board(String id, String time, Drawable image, String context){
        this.id = id;
        this.time = time;
        this.image = image;
        this.context = context;
    }

    static ArrayList<Board> createContacts(int size) {
        ArrayList<Board> boards = new ArrayList<>();
        Drawable img = null;
        for(int i=0; i< size; i++){

            boards.add(new Board("id","11/11", img, "context"));
        }
        return boards;
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

    public Drawable getImage() {
        return image;
    }

    public void setImage(Drawable image) {
        this.image = image;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public boolean isHart() {
        return hart;
    }

    public void setHart(boolean hart) {
        this.hart = hart;
    }

    public int getHartNum() {
        return hartNum;
    }

    public void setHartNum(int hartNum) {
        this.hartNum = hartNum;
    }
}
