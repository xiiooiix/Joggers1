package kkt.com.joggers.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import kkt.com.joggers.R;
import kkt.com.joggers.model.Comment;

/**
 * Created by youngjae on 2017-11-25.
 */

public class CommentAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Comment> comments;

    public CommentAdapter(Context context, ArrayList<Comment> comments){
        this.context = context;
        this.comments = comments;
    }

    @Override
    public int getCount() {
        return comments.size();
    }

    @Override
    public Object getItem(int position) {
        return comments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if(view == null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.comment_listview, parent, false);
        }

        Comment comment = (Comment)getItem(position);
        ((TextView)view.findViewById(R.id.c_id)).setText(comment.getId());
        ((TextView)view.findViewById(R.id.c_time)).setText(comment.getTime());
        ((TextView)view.findViewById(R.id.c_comment)).setText(comment.getContent());

        return view;
    }

    public void setItems(ArrayList<Comment> comments){
        this.comments =comments;
    }

    public void addItem(Comment comment) {
        comments.add(0, comment);
    }

    public void removeItem(Comment comment) {
        comments.remove(comment);
    }

}
