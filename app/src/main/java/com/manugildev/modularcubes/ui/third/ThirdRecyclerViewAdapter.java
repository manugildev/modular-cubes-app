package com.manugildev.modularcubes.ui.third;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.manugildev.modularcubes.R;
import com.manugildev.modularcubes.data.models.ModularCube;
import com.manugildev.modularcubes.data.models.Player;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ThirdRecyclerViewAdapter extends RecyclerView.Adapter<ThirdRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Player> playerData = new ArrayList<>();
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context context;

    // data is passed into the constructor
    public ThirdRecyclerViewAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
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
        int color = Color.parseColor(playerData.get(position).getColor());
        holder.numberTextView.setText(String.valueOf(player.getNumber()));
        holder.numberTextView.setTextColor(color);
        holder.progressBar1.setColor(color);
        holder.progressBar1.setBackgroundColor(ColorUtils.setAlphaComponent(color, 60));
        holder.progressBar2.setColor(color);
        holder.progressBar2.setBackgroundColor(ColorUtils.setAlphaComponent(color, 60));
        holder.progressBar1.setProgressWithAnimation(player.getProgress(), 800);
        holder.progressBar2.setProgressWithAnimation(player.getProgress(), 800);
        holder.playerFrame.setBackgroundColor(color);
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return playerData.size();
    }

    public void addItem(Player cube) {
        playerData.add(cube);
        notifyItemInserted(playerData.size() - 1);
    }

    public void removeAll() {
        if (playerData.size() > 0)
            for (int i = 0; i < playerData.size(); i++) {
                playerData.remove(i);
                notifyItemRemoved(i);
            }
    }

    public ArrayList<Player> getPlayers() {
        return playerData;
    }

    public Player getPlayerByCube(ModularCube cube) {
        for (Player p : playerData) {
            if (cube.getDeviceId() == p.getCube1().getDeviceId()) {
                p.getCube1().setCurrentOrientation(cube.getCurrentOrientation());
                return p;
            }
            if (cube.getDeviceId() == p.getCube2().getDeviceId()) {
                p.getCube2().setCurrentOrientation(cube.getCurrentOrientation());
                return p;
            }
        }
        return null;
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.numberTextView)
        public TextView numberTextView;
        @BindView(R.id.tile1)
        public CardView tile1;
        @BindView(R.id.tile2)
        public CardView tile2;
        public CircularProgressBar progressBar1;
        public CircularProgressBar progressBar2;
        @BindView(R.id.playerFrameLayout)
        public FrameLayout playerFrame;
        @BindView(R.id.playerTextView)
        public TextView playerTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            progressBar1 = (CircularProgressBar) tile1.findViewById(R.id.cubeProgressBar);
            progressBar2 = (CircularProgressBar) tile2.findViewById(R.id.cubeProgressBar);
            playerFrame.setAlpha(0);
            playerTextView.setAlpha(0);
            itemView.setOnClickListener(this);

        }

        @OnClick(R.id.tile1)
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