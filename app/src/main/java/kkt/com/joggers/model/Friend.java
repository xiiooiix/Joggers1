package kkt.com.joggers.model;

/**
 * Created by youngjae on 2017-12-07.
 */

public class Friend {
    private String id;
    private String imageUrl;

    public Friend(){}

    public Friend(String id){
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
