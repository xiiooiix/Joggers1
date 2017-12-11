package kkt.com.joggers.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public abstract class DataLoadLimitAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ValueEventListener {
    final Context context;
    int loadLimit = 10;

    DataLoadLimitAdapter(Context context) {
        this.context = context;
    }

    protected abstract void update();

    public void increaseLoadLimit() {
        loadLimit += 10;
        update();
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
    }
}