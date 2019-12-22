package com.mateusz.uno.ui.singleplayer.PlayerCardView;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.mateusz.uno.R;
import com.mateusz.uno.data.Card;

public class AIPlayerLeftCardView extends AIPlayerCardView{

    public AIPlayerLeftCardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void addCard(int id){

        ImageView iv = new ImageView(this.getContext());
        iv.setImageResource(R.drawable.c108);
        iv.setId(id);
        iv.setRotation(90);

        this.addView(iv, getParams());
    }

    @Override
    public LinearLayout.LayoutParams getParams() {
        //ImageView Params
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) (100 * getResources().getDisplayMetrics().density));

        //Setting left margins for all cards
        int margin = (int) (-65 * getResources().getDisplayMetrics().density);

        if(this.getChildCount() >= 8){
            margin = (int) (-85 * getResources().getDisplayMetrics().density);
        }
        else if(this.getChildCount() >= 5){
            margin = (int) ((-65 -((this.getChildCount() - 4) * 5)) * getResources().getDisplayMetrics().density);
        }

        cardParams.topMargin = margin;
        cardParams.weight = 0;

        //Setting margins for each card in the layout
        for(int i = 0; i < this.getChildCount(); i++){
            if(this.getChildAt(i).getId() != R.id.placeholderCard) this.getChildAt(i).setLayoutParams(cardParams);
        }

        //Setting width of placeholder card
        LinearLayout.LayoutParams placeholderParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                margin * -1);

        this.findViewById(R.id.placeholderCard).setLayoutParams(placeholderParams);
        return cardParams;
    }
}
