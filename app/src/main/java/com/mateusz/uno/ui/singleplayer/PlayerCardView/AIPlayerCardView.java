package com.mateusz.uno.ui.singleplayer.PlayerCardView;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.mateusz.uno.R;
import com.mateusz.uno.data.Card;

public class AIPlayerCardView extends LinearLayout {

    public AIPlayerCardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void addCard(int id){

        ImageView iv = new ImageView(this.getContext());
        iv.setImageResource(R.drawable.c108);
        iv.setId(id);

        this.addView(iv, getParams());
    }

    public void removeCard(int id){
        this.removeView(findViewById(id));
        getParams();
    }

    public LinearLayout.LayoutParams getParams(){
        //Dimensions of ImageView
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                (int) (100 * getResources().getDisplayMetrics().density),
                LinearLayout.LayoutParams.MATCH_PARENT);

        cardParams.weight = 0;

        //Decreasing or increasing left margins for all cards
        int margin = (int) (-60 * getResources().getDisplayMetrics().density);

        if(this.getChildCount() > 15){
            margin = (int) (-85 * getResources().getDisplayMetrics().density);
        }
        else if(this.getChildCount() >= 10) {
            margin = (int) (-75 - ((this.getChildCount() - 9) * 2.5) * getResources().getDisplayMetrics().density);
        }
        else if(this.getChildCount() >= 7){
            margin = (int) (-60 - ((this.getChildCount() - 6) * 5) * getResources().getDisplayMetrics().density);
        }

        //Setting margins for each card in the layout
        for(int i = 0; i < this.getChildCount(); i++){
            if(this.getChildAt(i).getId() != R.id.placeholderCard) this.getChildAt(i).setLayoutParams(cardParams);
        }

        //Adjusting width of placeholder for accommodate new left margin
        cardParams.leftMargin = margin;

        LinearLayout.LayoutParams placeholderParams = new LinearLayout.LayoutParams(
                margin * -1,
                LinearLayout.LayoutParams.MATCH_PARENT);

        this.findViewById(R.id.placeholderCard).setLayoutParams(placeholderParams);
        return cardParams;
    }
}

