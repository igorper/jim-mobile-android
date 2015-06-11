package com.trainerjim.mobile.android.network;

/**
 * Created by igor on 09.06.15.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.trainerjim.mobile.android.storage.PermanentSettings;

import retrofit.RequestInterceptor;

public class ApiHeaders implements RequestInterceptor {
    private static class Holder {
        static final ApiHeaders INSTANCE = new ApiHeaders();
    }

    public static ApiHeaders getInstance(Context context) {
        Holder.INSTANCE.mSettings = PermanentSettings.create(PreferenceManager
                        .getDefaultSharedPreferences(context));
        return Holder.INSTANCE;
    }

    private PermanentSettings mSettings;

    public void setSessionId(String sessionId) {
        mSettings.saveSessionCookie(sessionId);
    }

    public void clearSessionId() {
        mSettings.saveSessionCookie("");
    }

    @Override public void intercept(RequestInterceptor.RequestFacade request) {
        String sessionCookie = mSettings.getSessionCookie();
        if (!sessionCookie.equals("")) {
            request.addHeader("Cookie",sessionCookie);
        }
    }
}