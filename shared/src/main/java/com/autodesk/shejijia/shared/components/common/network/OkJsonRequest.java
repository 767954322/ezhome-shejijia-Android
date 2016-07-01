package com.autodesk.shejijia.shared.components.common.network;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.autodesk.shejijia.shared.framework.AdskApplication;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * @author yangxuewu.
 * @version v1.0 .
 * @date 2016-6-7 .
 * @file OkJsonRequest.java .
 * @brief JSON对象请求.
 */
public class OkJsonRequest extends JsonObjectRequest {

    /**
     * 请求网络
     *
     * @param method
     * @param url
     * @param jsonRequest
     * @param listener
     * @param errorListener
     */
    public OkJsonRequest(int method, String url, JSONObject jsonRequest, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
        this.setRetryPolicy(new DefaultRetryPolicy(300 * 1000, 0, 1.0f));
    }

    /**
     * @param method
     * @param url
     * @param jsonRequest
     * @param callback
     */
    public OkJsonRequest(int method, String url, JSONObject jsonRequest, OKResponseCallback callback) {
        this(method, url, jsonRequest, callback, callback);
    }



    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> header = new HashMap<>();
        header.put("Accept", "application/json");
        header.put("Content-type", "application/json;charset=UTF-8");
        return header;
    }

    /**
     * 请求网络的回调
     */
    public interface OKResponseCallback extends Response.Listener<JSONObject>, Response
            .ErrorListener {
    }

    @Override
    public Request<?> setCacheEntry(Cache.Entry entry) {
        return super.setCacheEntry(entry);
    }

    /*@Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data,
                    HttpHeaderParser.parseCharset(response.headers));

            JSONObject result = null;

            if (jsonString != null && jsonString.length() > 0)
                result = new JSONObject(jsonString);

            return Response.success(result,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }*/

    /**
     * 取消请求
     *
     * @param tag 请求TAG
     */
    public static void cancelRequest(Object tag) {
        if (AdskApplication.getInstance().queue != null) {
            AdskApplication.getInstance().queue.cancelAll(tag);//从请求队列中取消对应的任务
        }
    }
}