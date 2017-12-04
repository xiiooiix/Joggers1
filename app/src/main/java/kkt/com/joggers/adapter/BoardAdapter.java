package kkt.com.joggers.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

import kkt.com.joggers.R;
import kkt.com.joggers.model.Board;

public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.ViewHolder> {
    private Context context;
    private ArrayList<Board> boards;

    public BoardAdapter(Context context, ArrayList<Board> boards) {
        this.context = context;
        this.boards = boards;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 부모 Context 로부터 inflater 가져오기....
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        // Inflate layout to View
        View view = inflater.inflate(R.layout.item_board, parent, false);

        // View를 담은 ViewHolder 생성
        ViewHolder holder = new ViewHolder(view);
        holder.b_btn.setOnClickListener(new OnClickBtn(holder)); //'좋아요' 버튼 onClickListener 설정

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Board board = boards.get(position);

        holder.b_id.setText(board.getId());
        holder.b_time.setText(board.getTime());
        holder.b_content.setText(board.getContent());
        holder.b_heartNum.setText(String.valueOf(board.getHeartNum()));

        // image_url로 FirebaseStorage 에 저장된 이미지를 가져온다
        if (board.getImageUrl() != null) {
            FirebaseStorage.getInstance().getReferenceFromUrl(board.getImageUrl())
                    .getBytes(Long.MAX_VALUE)
                    .addOnSuccessListener(new OnSuccessGetImage(holder.b_img));
        }
        holder.b_btn.setBackground(context.getResources().getDrawable(R.drawable.heart_empty));
    }

    @Override
    public int getItemCount() {
        return boards.size();
    }

    public void addItem(Board board) {
        boards.add(0, board);
    }

    public void removeItem(Board board) {
        boards.remove(board);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView b_id, b_time, b_content, b_heartNum;
        private ImageView b_img;
        private Button b_btn;

        private ViewHolder(View itemVivew) {
            super(itemVivew);
            b_id = itemVivew.findViewById(R.id.board_id);
            b_time = itemVivew.findViewById(R.id.board_time);
            b_content = itemVivew.findViewById(R.id.board_content);
            b_heartNum = itemVivew.findViewById(R.id.board_heartNum);
            b_img = itemVivew.findViewById(R.id.board_imageView);
            b_btn = itemVivew.findViewById(R.id.board_heart);
        }
    }

    class OnClickBtn implements View.OnClickListener {
        private ViewHolder holder;

        private OnClickBtn(ViewHolder holder) {
            this.holder = holder;
        }

        @Override
        public void onClick(View v) {
            int position = holder.getAdapterPosition();
            int num = boards.get(position).getHeartNum();
            Board board = boards.get(position);

            // TODO 변경된 '좋아요' 값을 DB에 저장할 것!
            if (!board.isHeart()) {
                num++;
                board.setHeartNum(num);
                board.setHeart(true);
                holder.b_btn.setBackground(context.getResources().getDrawable(R.drawable.heart_full));
                holder.b_heartNum.setText(String.valueOf(boards.get(position).getHeartNum()));
            } else {
                num--;
                board.setHeartNum(num);
                board.setHeart(false);
                holder.b_btn.setBackground(context.getResources().getDrawable(R.drawable.heart_empty));
                holder.b_heartNum.setText(String.valueOf(boards.get(position).getHeartNum()));
            }
        }
    }

    class OnSuccessGetImage implements OnSuccessListener<byte[]> {
        private ImageView b_img;

        private OnSuccessGetImage(ImageView b_img) {
            this.b_img = b_img;
        }

        @Override
        public void onSuccess(byte[] bytes) {
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            b_img.setImageBitmap(bmp);
        }
    }

}
