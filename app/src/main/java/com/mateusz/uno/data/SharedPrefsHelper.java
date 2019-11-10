package com.mateusz.uno.data;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsHelper {

    private String tag;
    private Context ctx;

    public SharedPrefsHelper(Context ctx){
        this.ctx = ctx;
    }

    public UserData getUserData(){
        tag = "com.mateusz.uno.userData";
        int defaultAvatar = ctx.getResources().getIdentifier("avatar_1", "drawable", ctx.getPackageName());

        SharedPreferences sharedPreferences = ctx.getSharedPreferences(tag, Context.MODE_PRIVATE);
        int id = sharedPreferences.getInt("avatarId", defaultAvatar);
        String userName = sharedPreferences.getString("userName", "");

        return new UserData(id, userName);
    }

    public boolean setUserData(UserData data){
        tag = "com.mateusz.uno.userData";

        SharedPreferences sharedPreferences = ctx.getSharedPreferences(tag, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("avatarId", data.getId());
        editor.putString("userName", data.getName());
        return editor.commit();
    }
}
