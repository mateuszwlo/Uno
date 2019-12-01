package com.mateusz.uno.ui.singleplayer.PlayerCardView;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.mateusz.uno.R;

public class AIPlayerTopCardView extends AIPlayerCardView{

    public AIPlayerTopCardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public LinearLayout.LayoutParams getParams() {
        //ImageView Params
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                (int) (100 * getResources().getDisplayMetrics().density),
                LinearLayout.LayoutParams.MATCH_PARENT);

        //Setting left margins for all cards
        int margin = (int) (-65 * getResources().getDisplayMetrics().density);

        if(this.getChildCount() >= 8){
            margin = (int) (-85 * getResources().getDisplayMetrics().density);
        }
        else if(this.getChildCount() >= 5){
            margin = (int) ((-65 -((this.getChildCount() - 4) * 5)) * getResources().getDisplayMetrics().density);
        }

        cardParams.leftMargin = margin;
        cardParams.weight = 0;

        //Setting margins for each card in the layout
        for(int i = 0; i < this.getChildCount(); i++){
            if(this.getChildAt(i).getId() != R.id.placeholderCard) this.getChildAt(i).setLayoutParams(cardParams);
        }

        //Setting width of placeholder card
        LinearLayout.LayoutParams placeholderParams = new LinearLayout.LayoutParams(
                margin * -1,
                LinearLayout.LayoutParams.MATCH_PARENT);

        if(this.findViewById(R.id.placeholderCard) == null) return cardParams;

        this.findViewById(R.id.placeholderCard).setLayoutParams(placeholderParams);
        return cardParams;
    }
}
