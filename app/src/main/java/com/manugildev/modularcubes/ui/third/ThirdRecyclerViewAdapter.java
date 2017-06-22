package com.manugildev.modularcubes.ui.third;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.manugildev.modularcubes.R;
import com.manugildev.modularcubes.data.models.Player;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.ArrayList;

public class ThirdRecyclerViewAdapter extends RecyclerView.Adapter<ThirdRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Player> playerData = new ArrayList<>();
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;

    // data is passed into the constructor
    public ThirdRecyclerViewAdapter(Context context, ArrayList<Player> data) {
        this.mInflater = LayoutInflater.from(context);
        this.playerData = data;
        this.context = context;
    }

    // inflates the cell layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.fragment_third_player_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Player player = playerData.get(position);
        holder.numberTextView.setText(player.getName());
        holder.progressBar1.setColor(Color.parseColor(playerData.get(position).getColor()));
        holder.progressBar2.setBackgroundColor(ColorUtils.setAlphaComponent(Color.parseColor(playerData.get(position).getColor()), 60));
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return playerData.size();
    }

    public void addItem(Player cube) {
        playerData.add(cube);
        notifyItemInserted(getItemCount() - 1);
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView numberTextView;
        public CardView tile1;
        public CardView tile2;
        public CircularProgressBar progressBar1;
        public CircularProgressBar progressBar2;

        public ViewHolder(View itemView) {
            super(itemView);
            numberTextView = (TextView) itemView.findViewById(R.id.numberTextView);
            tile1 = (CardView) itemView.findViewById(R.id.tile1);
            tile2 = (CardView) itemView.findViewById(R.id.tile2);
            progressBar1 = (CircularProgressBar) tile1.findViewById(R.id.cubeProgressBar);
            progressBar2 = (CircularProgressBar) tile2.findViewById(R.id.cubeProgressBar);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public Player getItem(int id) {
        return playerData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}