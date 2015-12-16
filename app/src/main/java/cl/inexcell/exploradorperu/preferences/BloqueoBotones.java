package cl.inexcell.exploradorperu.preferences;


import android.content.Context;
import android.content.SharedPreferences;

public class BloqueoBotones {
    public static final String NAME = "BLOQUEO_BOTONES_PERU";


    public Context mContext;
    public SharedPreferences sharedPreferences;

    public BloqueoBotones(Context context) {
        super();
        mContext = context;
        sharedPreferences = mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public void setBloqueo(String boton, boolean estado, String msg){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(boton+"P", estado);
        editor.putString(boton+"PMSG", msg);
        editor.apply();

    }
    public void clearAll(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

    }


    public boolean getState(String boton){
        return sharedPreferences.getBoolean(boton+"P", false);
    }

    public String getMsg(String boton){
        return sharedPreferences.getString(boton+"PMSG", "");
    }



}
