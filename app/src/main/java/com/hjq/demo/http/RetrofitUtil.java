package com.hjq.demo.http;


import com.google.gson.Gson;
import com.hjq.demo.other.DebugLogUtil;
import com.hjq.demo.other.MmkvUtil;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.ConnectionSpec;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * 140.95
 * Created by LiJianfei on 2016/8/4.
 */
public class RetrofitUtil {
    public static RetrofitUtil getRetrofitUtil() {
        if (retrofitUtil == null) {
            retrofitUtil = new RetrofitUtil();
        }
        return retrofitUtil;
    }

    static private RetrofitUtil retrofitUtil;


    public static <T> T addUrlApi(final Class<T> service) {
        String baseUrl = "";
        Retrofit.Builder builder = new Retrofit.Builder();
        String token = MmkvUtil.getString("token", "");
        OkhttpIntercaptor interceptor = new OkhttpIntercaptor("", token);
        OkHttpClient.Builder okBuilder = new OkHttpClient.Builder();
        OkHttpClient httpClient = new OkHttpClient();
        okBuilder.addInterceptor(interceptor);
        if (baseUrl.startsWith("https://")) {
            okBuilder.connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS));
        }
        httpClient.sslSocketFactory();
        httpClient = okBuilder.build();
        Retrofit retrofit = builder.baseUrl(baseUrl).
                addConverterFactory(GsonConverterFactory.create())
                .client(httpClient).build();
        return retrofit.create(service);
    }


    static class OkhttpIntercaptor implements Interceptor {

        private String mLanguage;
        private String mToken;

        OkhttpIntercaptor(String language, String token) {
            mToken = token;
            mLanguage = language;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request oldRequest = chain.request();

            // 添加新的参数
            HttpUrl.Builder authorizedUrlBuilder = oldRequest.url()
                    .newBuilder()
                    .scheme(oldRequest.url().scheme())
                    .host(oldRequest.url().host())
                    .addQueryParameter("platform", "2")
                    .addQueryParameter("token", mToken);
            // 新的请求
            Request newRequest = oldRequest.newBuilder()
                    .method(oldRequest.method(), oldRequest.body())
                    .addHeader("Accept-Language", "zh-CN")
                    .addHeader("platform", "2")
                    .url(authorizedUrlBuilder.build())
                    .build();

            Buffer requestBuffer = new Buffer();
            if (oldRequest.body() != null) {
                oldRequest.body().writeTo(requestBuffer);
                String oldBodyStr = requestBuffer.readUtf8();
                DebugLogUtil.getInstance().Verbose(mToken + " ### " + oldBodyStr);
            } else {
                DebugLogUtil.getInstance().Verbose(mToken + " ### ");
            }


            Response response = chain.proceed(newRequest);
            ResponseBody oldResponseBody = response.body();
            String oldResponseBodyStr = null;
            try {
                oldResponseBodyStr = oldResponseBody.string();
            } catch (IOException e) {
                e.printStackTrace();
            }

            oldResponseBody.close();
            //构造新的response
            DebugLogUtil.getInstance().Verbose("返回1--" + oldResponseBodyStr);
            if (null != oldResponseBodyStr && !oldResponseBodyStr.startsWith("<")) {
                Gson gson = new Gson();
                /*try {
                    ReqErr reqErr = gson.fromJson(oldResponseBodyStr, ReqErr.class);
                    if (reqErr != null) {
                        if (reqErr.getCode() == 401 && !Helper.isWechatAuth()) {
                            ToastUtils.show("需要登录啦");
//                            ARouter.getInstance().build(ARouters.Login).navigation();
                        }
                    }
                } catch (Exception e) {

                }*/

            }
            ResponseBody newResponseBody = ResponseBody.create(MediaType.parse("text/plain; charset=utf-8"), oldResponseBodyStr);
            response = response.newBuilder().body(newResponseBody).build();
            response.close();
            return response;
        }

    }

}
