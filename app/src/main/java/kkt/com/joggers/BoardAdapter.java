package kkt.com.joggers;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;


/**
 * Created by youngjae on 2017-11-11.
 */

public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.ViewHolder> {
    Context context;
    private ArrayList<Board> boards;

    BoardAdapter(Context context, ArrayList<Board> boards){

        this.boards = boards;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context =parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.item_content, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Board item = boards.get(position);
        final int po =position;
        final ViewHolder ho = holder;
        Drawable drawable_btn = ContextCompat.getDrawable(context,R.drawable.hart1);
        Drawable drawable_img = ContextCompat.getDrawable(context,R.drawable.hart1);

        holder.b_id.setText(item.getId());
        holder.b_time.setText(item.getTime());
        holder.b_context.setText(item.getContext());
        holder.b_hartNum.setText(Integer.toString(item.getHartNum()));

        if(item.getImage() ==null){//게시판 작성할 때 사진 x
            holder.b_img.setBackground(drawable_img);
        }
        else{   //사진 o
            holder.b_img.setBackground(item.getImage());
            Log.i("ASDF", "width: " + item.getImage().getIntrinsicWidth() +" // height: "+item.getImage().getIntrinsicHeight());
            holder.b_img.getLayoutParams().width = item.getImage().getIntrinsicWidth(); //크기    520
            holder.b_img.getLayoutParams().height = item.getImage().getIntrinsicHeight();   //크기 520
        }

        holder.b_btn.setBackground(drawable_btn);
            /*좋아요 기능*/
        holder.b_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                int num = boards.get(po).getHartNum();
                if(boards.get(po).isHart() == false) {
                    num++;
                    boards.get(po).setHartNum(num);
                    Drawable drawable = ContextCompat.getDrawable(context,R.drawable.hart2);
                    ho.b_btn.setBackground(drawable);
                    ho.b_hartNum.setText(Integer.toString(boards.get(po).getHartNum()));
                    boards.get(po).setHart(true);
                }
                else{
                    num--;
                    boards.get(po).setHartNum(num);

                    Drawable drawable = ContextCompat.getDrawable(context,R.drawable.hart1);
                    ho.b_btn.setBackground(drawable);
                    ho.b_hartNum.setText(Integer.toString(boards.get(po).getHartNum()));
                    boards.get(po).setHart(false);
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return boards.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView b_id, b_time, b_context, b_hartNum;
        private ImageView b_img;
        private Button b_btn;
        private ViewHolder(View itemVivew){
            super(itemVivew);
            b_id = (TextView)itemVivew.findViewById(R.id.board_id);
            b_time = (TextView)itemVivew.findViewById(R.id.board_time);
            b_context = (TextView)itemVivew.findViewById(R.id.board_context);
            b_hartNum = (TextView)itemVivew.findViewById(R.id.board_hartNum);
            b_img = (ImageView)itemVivew.findViewById(R.id.board_imageView);
            b_btn=(Button)itemVivew.findViewById(R.id.board_hart);

        }
    }

    void addItem(String id, String time, Drawable image, String context) {
        boards.add(0, new Board(id, time, image, context));
        notifyItemInserted(0);
    }
}
