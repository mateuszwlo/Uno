package com.mateusz.uno.ui.singleplayer;

import com.mateusz.uno.data.Card;
import com.mateusz.uno.data.UserData;

public interface SinglePlayerMvpView {
  void player1AddCardView(Card c);
  void gameDrawDialog();
  void removeCardView(int id);
  void changeCurrentCardView(int id);
  void showPlayerWinDialog(int player);
  int getPlayer1CardCount();
  void changeTurnText(int to);
  void changeColour(Card.Colour col);
  void showColourPickerDialog();
  void adjustPlayerCardViews(int id, int size);
  void showWildCardColourPickerDialog();
  void setupPlayerData(int player, UserData data);
  int getAvatarResource(int id);
}
