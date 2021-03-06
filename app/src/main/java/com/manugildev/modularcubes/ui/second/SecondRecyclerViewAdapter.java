package com.manugildev.modularcubes.ui.second;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manugildev.modularcubes.R;
import com.manugildev.modularcubes.data.models.ModularCube;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.ArrayList;

public class SecondRecyclerViewAdapter extends RecyclerView.Adapter<SecondRecyclerViewAdapter.ViewHolder> {

    private ArrayList<ModularCube> mData = new ArrayList<>();
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;

    public SecondRecyclerViewAdapter(Context context, ArrayList<ModularCube> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.moto_tile_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        long id = mData.get(position).getDeviceId();
        holder.myTextView.setText(String.valueOf(id));
        holder.cubeProgressBar.setColor(Color.parseColor(mData.get(position).getColor()));
        holder.cubeProgressBar.setBackgroundColor(ColorUtils.setAlphaComponent(Color.parseColor(mData.get(position).getColor()), 60));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void addItem(ModularCube cube) {
        mData.add(cube);
        notifyItemInserted(getItemCount() - 1);
    }

    public void removeItem(ModularCube cube) {
        int i = getCubeIndexById(cube.getDeviceId());
        mData.remove(i);
        notifyItemRemoved(i);
    }

    public int getCubeIndexById(long cubeId) {
        for (int i = 0; i < mData.size(); i++) {
            if (cubeId == mData.get(i).getDeviceId()) {
                return i;
            }
        }
        return -1;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView myTextView;
        public CircularProgressBar cubeProgressBar;

        public ViewHolder(View itemView) {
            super(itemView);
            myTextView = (TextView) itemView.findViewById(R.id.info_text);
            cubeProgressBar = (CircularProgressBar) itemView.findViewById(R.id.cubeProgressBar);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    public ModularCube getItem(int id) {
        return mData.get(id);
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}