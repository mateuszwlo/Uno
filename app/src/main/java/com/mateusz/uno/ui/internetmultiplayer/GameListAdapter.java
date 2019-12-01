package com.mateusz.uno.ui.internetmultiplayer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mateusz.uno.R;
import com.mateusz.uno.data.InternetGameData;

import java.util.ArrayList;

public class GameListAdapter extends RecyclerView.Adapter<GameListAdapter.GameViewHolder> {

    private ArrayList<InternetGameData> games;

    public GameListAdapter(ArrayList<InternetGameData> games) {
        this.games = games;
    }

    @NonNull
    @Override
    public GameListAdapter.GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.game_list_item, parent, false);

        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameListAdapter.GameViewHolder holder, int position) {
        holder.bindGame(games.get(position));
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    public class GameViewHolder extends RecyclerView.ViewHolder{
        private TextView gameNameTv, gameAvailability;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            gameNameTv = itemView.findViewById(R.id.gameNameTv);
            gameAvailability = itemView.findViewById(R.id.gameAvailability);
        }

        public void bindGame(InternetGameData game){
            //Get name of first player
            gameNameTv.setText(game.getName());

            String availability = game.getPlayerList().size() + "/" + game.getPlayerCount();
            gameAvailability.setText(availability);
        }
    }
}
