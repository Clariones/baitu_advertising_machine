package com.skynet.adplayer.utils;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

public class HttpUtils {




    public static class RequestResult {
        protected boolean networkError = false;
        protected boolean serverError = false;
        protected Exception exception = null;
        protected String responseBody = null;
        protected Map<String, List<String>> responseHeaders = null;

        public boolean isNetworkError() {
            return networkError;
        }

        public void setNetworkError(boolean networkError) {
            this.networkError = networkError;
        }

        public boolean isServerError() {
            return serverError;
        }

        public void setServerError(boolean serverError) {
            this.serverError = serverError;
        }

        public Exception getException() {
            return exception;
        }

        public void setException(Exception exception) {
            this.exception = exception;
        }

        public String getResponseBody() {
            return responseBody;
        }

        public void setResponseBody(String responseBody) {
            this.responseBody = responseBody;
        }

        public Map<String, List<String>> getResponseHeaders() {
            return responseHeaders;
        }

        public void setResponseHeaders(Map<String, List<String>> responseHeaders) {
            this.responseHeaders = responseHeaders;
        }
    }

    public static RequestResult get(String url) {
        RequestResult result = new RequestResult();

        HttpURLConnection urlConnection = null;
        URL requestUrl = null;
        InputStream in = null;
        try {
            requestUrl = new URL(url);
        } catch (MalformedURLException e) {
            //e.printStackTrace();
            result.setException(e);
            return result; // error 1
        }

        try {
            urlConnection = (HttpURLConnection) requestUrl.openConnection();
            urlConnection.setConnectTimeout(3000);
            urlConnection.setReadTimeout(1000);
            in = urlConnection.getInputStream();
        } catch (ConnectException e){
            // connection fail
            //e.printStackTrace();
            result.setException(e);
            result.setNetworkError(true);
            return result;
        } catch (IOException e) {
            // need check is network error, or server not started
            //e.printStackTrace();
            result.setException(e);
            result.setServerError(true);
            return result;
        }

        InputStreamReader isw = new InputStreamReader(in);
        StringBuilder sb = new StringBuilder();
        try {
            int data = isw.read();
            while (data != -1) {
                char current = (char) data;
                data = isw.read();
                sb.append(current);
            }
            result.setResponseBody(sb.toString());
            result.setResponseHeaders(urlConnection.getHeaderFields());
        }catch (IOException e){
            // need check is network error, or server not started
            //e.printStackTrace();
            result.setException(e);
            result.setServerError(true);
        }finally {
            try {
                isw.close();
            } catch (IOException e) {
            }
        }
        return result;
    }

    public static String getRequest(String url) throws Exception {
        return requestWithGetMethod(url, null);
    }
    private static String requestWithGetMethod(String url, String userAgent) throws Exception{
        HttpURLConnection urlConnection = null;
        URL requestUrl = null;
        InputStream in = null;
        InputStreamReader isw = null;
        try {
            requestUrl = new URL(url);
            urlConnection = (HttpURLConnection) requestUrl.openConnection();
            urlConnection.setConnectTimeout(3000);
            urlConnection.setReadTimeout(1000);
            if (userAgent != null){
                urlConnection.setRequestProperty("User-agent", userAgent);
            }
            in = urlConnection.getInputStream();
            isw = new InputStreamReader(in);
            StringBuilder sb = new StringBuilder();
            int data = isw.read();
            while (data != -1) {
                char current = (char) data;
                data = isw.read();
                sb.append(current);
            }
            return sb.toString();
        } finally {
            try {
                isw.close();
            } catch (IOException e) {
            }
        }
    }
    public static String getRequestWithUseAgent(String url) throws Exception{
        return requestWithGetMethod(url, SystemPropertyUtils.getDeviceUserAgentString());
    }

    public static void saveResponseToFile(String url, File file) throws Exception{
        InputStream in = null;
        FileOutputStream fout = null;
        Log.i("=SAVE PICTURE=", "file " + file.getAbsolutePath()+", url=" + url);
        try {
            if (!file.exists()){
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            URL requestUrl = new URL(encodeEntireUrl(url));
            HttpURLConnection urlConnection = (HttpURLConnection) requestUrl.openConnection();
            urlConnection.setConnectTimeout(30000); // 30 seconds timeout
            urlConnection.setReadTimeout(30000);
            in = urlConnection.getInputStream();
            fout = new FileOutputStream(file);
            byte[] buff = new byte[1024];
            int n = 0;
            while( (n=in.read(buff)) > 0){
                fout.write(buff, 0, n);
            }
            in.close();
            fout.flush();
        } finally {
            if (fout != null){
                fout.close();
            }
        }
    }

    public static String encodeEntireUrl(String urlStr) throws Exception {
        int pos = urlStr.indexOf('?');
        String queryStr = "";
        String uri = urlStr;
        if (pos >= 0){
            queryStr = urlStr.substring(pos+1);
            uri = urlStr.substring(0, pos);
        }
        String[] uriPieces = uri.split("(?<=[^/])/(?=[^/])");
        StringBuilder sbUrl = new StringBuilder();
        for(String piece: uriPieces){
            if (piece.isEmpty()){
                continue;
            }
            if (piece.toLowerCase().contains("://")){
                sbUrl.append(piece);
            }else{
                sbUrl.append("/").append(URLEncoder.encode(piece, "utf-8"));
            }
        }
        if (!queryStr.isEmpty()){
            String[] queryPieces = queryStr.split("\\&");
            boolean first = true;
            for(String piece: queryPieces){
                int pos2 = piece.indexOf('=');
                String name = "";
                String value = "";
                if (pos2 > 0){
                    name=URLEncoder.encode(piece.substring(0, pos2),"utf-8");
                    value=URLEncoder.encode(piece.substring(pos2+1),"utf-8"); ;
                }else{
                    name=URLEncoder.encode(piece,"utf-8");
                    value = null;
                }
                if(first){
                    sbUrl.append('?').append(name);
                    first = false;
                }else{
                    sbUrl.append('&').append(name);
                }
                if (value != null){
                    sbUrl.append('=').append(value);
                }
            }
        }
        System.out.println("Convert " + urlStr);
        System.out.println("     to " + sbUrl.toString());
        return sbUrl.toString();
    }
}