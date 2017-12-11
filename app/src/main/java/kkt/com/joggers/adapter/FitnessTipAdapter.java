package kkt.com.joggers.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import kkt.com.joggers.R;
import kkt.com.joggers.activity.FitnessTipContentActivity;
import kkt.com.joggers.model.FitnessTipResImgs;

/**
 * FitnessTipFragment의 전체 팁들을 표시하는 Adapter
 */
public class FitnessTipAdapter extends RecyclerView.Adapter<FitnessTipAdapter.ViewHolder> {
    private final Context context;

    public FitnessTipAdapter(Context context) {
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_fit_tip_title, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.titleView.setImageDrawable(context.getDrawable(FitnessTipResImgs.tilteImgs[position]));
    }

    @Override
    public int getItemCount() {
        return FitnessTipResImgs.tilteImgs.length;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ImageView titleView;

        private ViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.title_view);
            titleView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v == titleView) {
                Intent intent = new Intent(context, FitnessTipContentActivity.class);
                intent.putExtra("position", getAdapterPosition());
                context.startActivity(intent);
            }
        }
    }
}