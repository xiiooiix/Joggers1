package kkt.com.joggers;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;

public class BoardFragment extends Fragment {
    Context a;
    RecyclerView list;
    BoardAdapter adapter;
    Button btn;

    /*보드 라이트 */
    static private String context, id, time;
    static Drawable img;
    static private boolean write=false;


    public BoardFragment() { }
    public void setContext(String context){
        this.context = context;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setImg(Drawable img) {
        if(img==null) { //null일때 뭔가를 넣어야하는데 뭘 넣어야할 지 모르겟다.
            getActivity();
            Log.i("ASDF", "aaa");
        }
        else {
            this.img = img;
            Log.i("ASDF", "width: " + img.getIntrinsicWidth() +" // height: "+img.getIntrinsicHeight());

        }
    }

    public void setWrite(Boolean write){
        this.write = write;
        Log.i("ASDD", "write 바꾸기 - " +write.toString());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_board, container, false);
        list = view.findViewById(R.id.rcView);
        LinearLayoutManager lm = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        list.setLayoutManager(lm);

        a = getActivity();

        ArrayList<Board> boards = Board.createContacts(3);

        adapter = new BoardAdapter(getContext(), boards);


        list.setAdapter(adapter);

        btn = (Button)view.findViewById(R.id.btn_write);    //작성하기 버튼

        btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Log.i("ASD", "글작성 ㄱㄱ");
                FragmentManager fragmentManager = getFragmentManager();

                fragmentManager.beginTransaction().replace(R.id.content_main, new Board_WriteFragment(),"BoardFragment").addToBackStack("BoardFragment").commit();
            }
        });

        RecyclerView.ItemDecoration itemDecoration = new MarginItemDecoration(20);
        list.addItemDecoration(itemDecoration);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();



        if(write){     //새로운 게시글 추가 됐을 때
            Log.i("ASDD", "wrtie exit");
            if(img == null){    //이미지 없을
                Log.i("ASDF", "img null:  " + a.toString());
                img = ContextCompat.getDrawable(a, R.drawable.hart1);
            }
            Log.i("ASDF", "img exit: ");
            adapter.addItem(id, time, img, context);


        }
        else
            Log.i("ASDD", "wrtie null");

    }
}
