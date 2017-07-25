package net.suntrans.bikecharge.api;



import net.suntrans.bikecharge.App;
import net.suntrans.bikecharge.utils.LogUtil;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Looney on 2016/12/15.
 */

public class RetrofitHelper {


    //public static final String BASE_URL = "http://www.suntrans.net:8956";
//    public static final String BASE_URL = "http://q1.suntrans.net:8011/sc_product/ebike.php/";
    public static final String BASE_URL = "http://ebike.suntrans-cloud.com/ebike.php/";

    private static OkHttpClient mOkHttpClient;
    private static OkHttpClient mOkHttpClient2;

    static {
        initOkHttpClient();
    }



    public static Api getLoginApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        return retrofit.create(Api.class);
    }

    public static Api getCookieApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(mOkHttpClient2)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        return retrofit.create(Api.class);
    }

    public static Api getApi() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(mOkHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        return retrofit.create(Api.class);
    }


    private static void initOkHttpClient() {
        Interceptor netInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                String header = App.getSharedPreferences().getString("access_token", "-1");
                header = "Bearer " + header;
                LogUtil.i(header);
                Request original = chain.request();
                Request request = original.newBuilder()
                        .header("Authorization", header)
                        .method(original.method(), original.body())
                        .build();
                Response response = chain.proceed(request);

                return response;
            }
        };


        if (mOkHttpClient == null) {
            synchronized (RetrofitHelper.class) {
                if (mOkHttpClient == null) {
                    mOkHttpClient = new OkHttpClient.Builder()
                            .addInterceptor(netInterceptor)
                            .connectTimeout(10, TimeUnit.SECONDS)
                            .build();
                }
            }
        }
        if (mOkHttpClient2 == null) {
            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
            synchronized (RetrofitHelper.class) {
                if (mOkHttpClient2 == null) {
                    mOkHttpClient2 = new OkHttpClient.Builder()
                            .addInterceptor(netInterceptor)
                            .cookieJar(new JavaNetCookieJar(cookieManager))
                            .connectTimeout(10, TimeUnit.SECONDS)
                            .build();
                }
            }
        }
    }


}
