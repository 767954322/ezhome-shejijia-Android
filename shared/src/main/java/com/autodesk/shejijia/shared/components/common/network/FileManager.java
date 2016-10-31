package com.autodesk.shejijia.shared.components.common.network;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by t_panya on 16/10/28.
 */

public class FileManager {
    private int handlerState;
    private String file_id;
    private String file_url;
    private String file_name;
    private Handler handler;
    private FileManager(){

    }

    private static class FileManagerHolder{
        private static final FileManager INSTANCE = new FileManager();
    }

    public FileManager getInstance(){
        return FileManagerHolder.INSTANCE;
    }

    public void getUploadServer(final File file, int handlerState){
        JSONObject jsonObject = new JSONObject();
        this.handlerState = handlerState;
        String url = "https://api.acgcn.autodesk.com/api/v2/server/upload";
        OkJsonRequest okRequest = new OkJsonRequest(Request.Method.GET, url, jsonObject, new OkJsonRequest.OKResponseCallback() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }

            @Override
            public void onResponse(JSONObject jsonObject) {
                String server;
                try {
                    server = jsonObject.getString("server");
                    uploadFile(file,server);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }



    private void uploadFile(final File file,final String server){
        final String url = "http://" + server + "/api/v2/files/upload";
        Callback callback = new Callback() {
            @Override
            public void onFailure(com.squareup.okhttp.Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                String result = response.body().toString();
                if(result != null){
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        JSONArray files = jsonObject.getJSONArray("files");
                        JSONObject subObject = (JSONObject) files.get(0);

                        file_id = subObject.getString("file_id");
                        file_name = subObject.getString("file_name");
                        file_url = subObject.getString("file_url");

                        Message message = handler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putString("file_id",file_id);
                        bundle.putString("file_name",file_name);
                        bundle.putString("file_url",file_url);
                        bundle.putInt("handlerState",handlerState);
                        message.setData(bundle);
                        handler.sendMessage(message);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

    }

    public void sendFileByPost(String server, File saveFile, Callback callback, String tag){

    }

    public void sendMultiFiles(String server, Map<String,Object> map,Callback callback){
        MultipartBuilder builder = new MultipartBuilder().type(MultipartBuilder.FORM);
        if(null != map){
            for(Map.Entry<String,Object> entry : map.entrySet()){
                if(entry.getValue() != null){
                    if(entry.getValue() instanceof File){
                        File file = (File) entry.getValue();
                        builder.addFormDataPart(entry.getKey(),file.getName(),RequestBody.create(MediaType.parse("image/*"),file));
                    }
                }
            }
        }
        RequestBody requestBody = builder.build();
        com.squareup.okhttp.Request request = new com.squareup.okhttp.Request
                                                .Builder()
                                                .url(server)
                        // TODO: 16/10/28  add Header
                        //                .addHeader()
                        //                .addHeader()
                                                .post(requestBody)
                                                .build();
        OkHttpClient okHttpClient1 = new OkHttpClient();
        Call call = okHttpClient1.newCall(request);
        call.enqueue(callback);


    }


    public void setHandler(Handler handler){
        this.handler = handler;
    }

}
