package com.tinyappsdev.forestsupply.rest;

import android.os.Handler;
import android.util.Log;

import com.tinyappsdev.forestsupply.data.ModelHelper;

import java.io.IOException;


public abstract class ApiCallClient {
    public final static String TAG = ApiCallClient.class.getSimpleName();

    protected String mServerUri;
    protected HttpClient mHttpClient = new HttpClient();

    public static class Result<T> {
        public String error;
        public T data;

        private Handler mHandler;
        private HttpClient.HttpRequest mRequest;

        public void cancel() {
            if(mRequest == null) return;
            mRequest.cancel();
            mRequest = null;
        }
    }

    public interface OnResultListener<T> {
        void onResult(Result<T> result);
    }

    public void setServerAddress(String serverAddress) {
        mServerUri = serverAddress;
    }

    public void setOnCookieListener(HttpClient.OnCookieListener onCookieListener) {
        mHttpClient.setOnCookieListener(onCookieListener);
    }

    public abstract String toJson(Object obj);
    public abstract Object fromJson(String str, Class resultType);

    public <T> Result<T> makeCall(String uri, Object body, Class<T> resultType) {
        return makeCall(uri, body, resultType, null);
    }

    public <T> Result<T> makeCall(String uri, Object body,
                                  final Class<T> resultType,
                                  final OnResultListener<T> onResultListener) {

        final Result<T> result = new Result();
        if(onResultListener != null) result.mHandler = new Handler();

        if(mServerUri == null) {
            result.error = "No Server Address";
            if(onResultListener != null) {
                result.mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        onResultListener.onResult(result);
                    }
                });
            }
            return result;
        }

        uri = mServerUri + "/Api" + uri;
        Log.i(TAG, String.format("ApiCallClient -> %s", uri));

        String bodyJson = null;
        if(body != null) {
            bodyJson = toJson(body);
            if(bodyJson == null) {
                result.error = "Json Encode Error";
                if(onResultListener != null) {
                    result.mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onResultListener.onResult(result);
                        }
                    });
                }
                return result;
            }
        }

        if(onResultListener == null) {
            try {
                String response = mHttpClient.makeRequestSync(uri, bodyJson);
                if(resultType == null)
                    result.data = (T)response;
                else if(response != null)
                    result.data = (T)fromJson(response, resultType);

                if(result.data == null)
                    result.error = "Empty Response";

            } catch (IOException e) {
                e.printStackTrace();
                result.error = e.getMessage();
            }

        } else {
            result.mRequest = mHttpClient.makeRequestAsync(uri, bodyJson, new HttpClient.OnResultListener() {
                @Override
                public void onResult(String error, String body) {
                    if(error != null) {
                        result.error = error;
                    } else {
                        if(resultType == null)
                            result.data = (T)body;
                        else if(body != null)
                            result.data = (T)fromJson(body, resultType);

                        if(result.data == null)
                            result.error = "Empty Response";
                    }
                    //Log.i("PKT", String.format(">>>>>>>>>>DUMP OUT(%s, %s)",
                    //        ModelHelper.toJson(result.error),
                    //        ModelHelper.toJson(result.data)
                    //));
                    result.mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(result.mRequest == null) return;
                            result.mRequest = null;
                            onResultListener.onResult(result);
                        }
                    });
                }
            });
        }

        return result;
    }

}
