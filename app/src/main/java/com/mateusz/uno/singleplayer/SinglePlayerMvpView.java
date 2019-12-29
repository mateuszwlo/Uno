package com.mateusz.uno.singleplayer;

import com.mateusz.uno.data.Card;
import com.mateusz.uno.data.UserData;

public interface SinglePlayerMvpView {
  //User cards
  void addCardView(int player, Card c);
  void removeCardView(int player, Card c);
  int getPlayer1CardCount();

  //Game
  void changeCurrentCardView(int id);
  void changeColour(Card.Colour col);
  void changeTurnText(String player);
  void setupPlayerData(int player, UserData data);

  //Player cards
  int getAvatarResource(int id);

  //Dialogs
  void showPlayerWinDialog(String player);
  void gameDrawDialog();
  void showColourPickerDialog();
  void showWildCardColourPickerDialog();
}
