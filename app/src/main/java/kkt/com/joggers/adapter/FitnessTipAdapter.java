package kkt.com.joggers.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import kkt.com.joggers.R;
import kkt.com.joggers.activity.FitnessTipContentActivity;
import kkt.com.joggers.model.FitnessTipResImgs;

public class FitnessTipAdapter extends RecyclerView.Adapter<FitnessTipAdapter.ViewHolder> implements AdapterView.OnItemClickListener {
    private Context context;

    public FitnessTipAdapter(Context context) {
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 부모 Context로부터 inflater 가져오기
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // inflate R.layout.item_contact to View
        View view = inflater.inflate(R.layout.item_fit_tip_title, parent, false);

        // View를 담은 ViewHolder 반환
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.titleView.setImageDrawable(context.getDrawable(FitnessTipResImgs.tilteImgs[position]));
    }

    @Override
    public int getItemCount() {
        return FitnessTipResImgs.tilteImgs.length;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(context, FitnessTipContentActivity.class);
        intent.putExtra("position", position);
        context.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation((Activity) context).toBundle());
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView titleView;

        private ViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.title_view);
        }
    }
}
