package mx.grupohi.acarreos.Oauth;


import mx.grupohi.acarreos.Oauth.Token;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ErpClient {
    @Headers("Accept: application/json")
    @POST("oauth/token")
    @FormUrlEncoded
    Call<Token> getToken(
            @Field("client_id") String clientId,
            @Field("client_secret") String clientSecret,
            @Field("code") String Code,
            @Field("grant_type") String grant,
            @Field("redirect_uri") String redirect
    );
}
