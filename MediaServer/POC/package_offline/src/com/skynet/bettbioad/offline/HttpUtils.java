package com.skynet.bettbioad.offline;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class HttpUtils {

	public static String requestContent(String urlStr) throws Exception {
		URL url = new URL(urlStr);

		URLConnection rulConnection = url.openConnection();
		HttpURLConnection httpUrlConnection = (HttpURLConnection) rulConnection;
		
		httpUrlConnection.setDoOutput(false);
		httpUrlConnection.setDoInput(true);
		httpUrlConnection.setUseCaches(false);
//		httpUrlConnection.setRequestProperty("Content-type", "application/json;charset=utf-8");
		httpUrlConnection.setRequestMethod("GET");
		httpUrlConnection.setRequestProperty("Accept","application/json");
		
		InputStream ins = httpUrlConnection.getInputStream();
		ByteArrayOutputStream  bout = new ByteArrayOutputStream();
		byte[] buff = new byte[1024];
		int n;
		while((n = ins.read(buff)) > 0){
//			System.out.println("READ " + n);
			bout.write(buff, 0, n);
			System.out.print(".");
		}
		System.out.println();
		String result = new String(bout.toByteArray(), StandardCharsets.UTF_8);
		System.out.println("GET " + urlStr);
		System.out.println("    " + result.replaceAll("([\\w\\W]{120})", "$1\n   "));
		return result;
	}

	public static void downloadPicture(String  uri, String fileName, File imageFolder) throws Exception {
		if (!imageFolder.exists()){
			if (!imageFolder.mkdirs()){
				throw new Exception("Cannot create folder" + imageFolder.getAbsolutePath());
			}
		}
		File imageFile = new File(imageFolder, fileName);
		if (imageFile.exists()){
			imageFile.delete();
		}
		imageFile.createNewFile();
		
		FileOutputStream fout = new FileOutputStream(imageFile);
//		URL url = new URL("http://ad.bettbio.com:8280/" + uri);
		String urlStr = "http://ad.bettbio.com:8280/" + uri;
		
		URL url = new URL(encodeEntireUrl(urlStr));
		
		URLConnection rulConnection = url.openConnection();
		HttpURLConnection httpUrlConnection = (HttpURLConnection) rulConnection;
		
		httpUrlConnection.setDoOutput(true);
		httpUrlConnection.setDoInput(true);
		httpUrlConnection.setUseCaches(false);
		httpUrlConnection.setRequestMethod("GET");
		
		InputStream ins = httpUrlConnection.getInputStream();
		ByteArrayOutputStream  bout = new ByteArrayOutputStream();
		byte[] buff = new byte[1024];
		int n;
		while((n = ins.read(buff))> 0){
			fout.write(buff, 0, n);
			System.out.print(".");
		}
		System.out.println();
		fout.flush();
		fout.close();
		System.out.println("Save " + uri+" to " + imageFile.getAbsolutePath());
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
