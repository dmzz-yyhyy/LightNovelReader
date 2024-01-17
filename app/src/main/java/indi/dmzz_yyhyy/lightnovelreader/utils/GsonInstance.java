package indi.dmzz_yyhyy.lightnovelreader.utils;

import com.google.gson.Gson;

public class GsonInstance {

    private static GsonInstance INSTANCE;
    private static Gson gson;

    public static GsonInstance getInstance() {
        if (INSTANCE == null) {
            synchronized (GsonInstance.class) {
                if (INSTANCE == null) {
                    INSTANCE = new GsonInstance();
                }
            }
        }
        return INSTANCE;
    }

    public Gson getGson() {
        if (gson == null) {
            synchronized (GsonInstance.class) {
                if (gson == null) {
                    gson = new Gson();
                }
            }
        }
        return gson;
    }

}