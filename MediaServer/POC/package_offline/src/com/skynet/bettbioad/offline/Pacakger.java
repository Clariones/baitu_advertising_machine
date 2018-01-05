package com.skynet.bettbioad.offline;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class Pacakger {

	public static void main(String[] args) throws Exception {
		HttpUtils.encodeEntireUrl("?w=400&h=300&scale=fit");
//		if (args == null || args.length < 3){
//			System.out.println("Please input adMachineModel, adMachineSerialNumber and base folder");
//			return;
//		}
		// url : http://ad.bettbio.com:8180/naf/playListManager/retrievePlayListOfAdMachine/c300/ZHEYDQLJ6TZDTSG6/v2.6.0.30/
		String model = "c300";
		String sn = "ZHEYDQLJ6TZDTSG6";
		String version = "v2.6.0.30";
		String outputFolde = ".";
		String hostPrefix = "http://ad.bettbio.com:8180/naf";
		String url = hostPrefix+"/playListManager/retrievePlayListOfAdMachine/";
		url += model + "/" + sn +"/"+version+"/";
		File baseFolder = new File(outputFolde);
		System.out.println("Packaging contents from " + url+" to " + baseFolder.getAbsolutePath());
		
		String jsonStr = HttpUtils.requestContent(url);
		
		Map<String, Object> data = new Gson().fromJson(jsonStr, Map.class);
		System.out.println("Got data: " + data);
		
		List<Map<String, Object>> list = getPlayListData(data);
		File imageFolder = new File(baseFolder, "output/image");
		for(Map<String, Object> item : list){
			String uri = (String) item.get("imageUri");
			String contentId = (String) item.get("contentId");
			String postfix = getPosfix(uri);
			item.put("postfix", postfix);
			HttpUtils.downloadPicture(uri, contentId+postfix, imageFolder);
		}
		generate(data, baseFolder, "index.html.ftl", "output/index.html");
		copyFiles(baseFolder);
	}

	private static void copyFiles(File baseFolder) {
		String[] files = new String[]{
			"css/play-offline.css",
			"js/jquery-1.10.2.min.js",
			"js/play-offline.js"
		};
		for(String file: files){
			File srcFile = new File(baseFolder, "template/" + file);
			File tgtFile = new File(baseFolder, "output/" + file);
			nioTransferCopy(srcFile, tgtFile);
		}
	}


    private static void nioTransferCopy(File source, File target) {
        FileChannel in = null;  
        FileChannel out = null;  
        FileInputStream inStream = null;  
        FileOutputStream outStream = null;  
        try {  
        	if (!target.exists()){
        		target.getParentFile().mkdirs();
        		target.createNewFile();
        	}
            inStream = new FileInputStream(source);  
            outStream = new FileOutputStream(target);  
            in = inStream.getChannel();  
            out = outStream.getChannel();  
            in.transferTo(0, in.size(), out);  
        } catch (IOException e) {  
            e.printStackTrace();  
        } finally {  
            close(inStream);  
            close(in);  
            close(outStream);  
            close(out);  
        }  
    }  

	private static void close(Closeable resource) {
		try {
			resource.close();
		} catch (IOException e) {
		}
	}

	private static void generate(Map<String, Object> data, File baseFolder, String templName, String outputFileName) throws Exception {
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_25);
		cfg.setDefaultEncoding("UTF-8");
		cfg.setDirectoryForTemplateLoading(new File(baseFolder, "template"));
		Template tmpl = cfg.getTemplate(templName);
		FileWriter fw = new FileWriter(new File(baseFolder, outputFileName), false);
		tmpl.process(data, fw);
		fw.close();
	}

	private static String getPosfix(String uri) {
		int pos1 = uri.lastIndexOf('?');
		if (pos1 > 0){
			uri = uri.substring(0, pos1);
		}
		pos1 = uri.lastIndexOf('.');
		if (pos1 > 0){
			return uri.substring(pos1);
		}
		return ".jpg";
	}

	private static List<Map<String, Object>> getPlayListData(Map<String, Object> data) {
		return (List<Map<String, Object>>) data.get("pages");
	}

}
