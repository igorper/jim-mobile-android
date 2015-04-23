package com.trainerjim.android.network;

import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by igor on 23.04.15.
 */
public interface  TrainerJimService {

    @GET("/mapi/users/authenticate")
    Boolean checkCredentials(@Query("email") String email, @Query("password") String password);
}
