package com.skynet.mediaserver.mediasaver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.skynet.mediaserver.MediaConstants;
import com.skynet.mediaserver.MediaData;
import com.skynet.mediaserver.utils.FSMediaUtils;
import com.skynet.mediaserver.utils.ImageUtils;
import com.skynet.mediaserver.utils.ParameterUtils;

public class SimpleFileSystemMediaSaver implements MedieResourceProcessor{
	protected String repoistoryFolder = "/tmp";
	protected Logger logger = Logger.getLogger(SimpleFileSystemMediaSaver.class.getName());
	protected Map<String, String> appOperationTokens;
	
	public Map<String, String> getAppOperationTokens() {
		return appOperationTokens;
	}

	public void setAppOperationTokens(Map<String, String> appOperationTokens) {
		this.appOperationTokens = appOperationTokens;
	}

	public String getRepoistoryFolder() {
		return repoistoryFolder;
	}

	public void setRepoistoryFolder(String repoistoryFolder) {
		this.repoistoryFolder = repoistoryFolder;
	}

	public String storeMedia(String appKey, MediaData media) throws Exception {
		String key = FSMediaUtils.makeKey(appKey, media);
		File baseFolder = new File(repoistoryFolder);
		File tgtFile = new File(baseFolder, key);
		
		ensureFileExisted(tgtFile);
		
		if (media.getContentStream() != null) {
			FileOutputStream fout = new FileOutputStream(tgtFile);
			byte[] buf = new byte[1024 * 1024];
			int n;
			while ((n = media.getContentStream().read(buf)) > 0) {
				fout.write(buf, 0, n);
			}
			fout.close();
		}else if (media.getContentFile() != null){
			tgtFile.delete();
			media.getContentFile().renameTo(tgtFile);
		}else{
			return null;
		}
		return key;
	}

	private void ensureFileExisted(File tgtFile) throws IOException {
		if (tgtFile.exists()){
			return;
		}
		tgtFile.getParentFile().mkdirs();
		tgtFile.createNewFile();
	}

	public String getBaseFolder() {
		return repoistoryFolder;
	}
	
	// uri could be: public/a/b/c/1/3/4/5/image.jpg?w=100&h=200&scale=fill
	public File getFile(String resourceUri, Map<String, Object> parameters) {
		File orgFile = new File(getBaseFolder(), resourceUri);
		// if original file not existed, nothing can do
		if (!orgFile.exists() || !orgFile.isFile()){
			return null;
		}
		
		// rule 1: if no resizing, just return the orginal file
		String paramW = ParameterUtils.getString(parameters, MediaConstants.PARAM_SIZE_WIDTH, null);
		String paramH = ParameterUtils.getString(parameters, MediaConstants.PARAM_SIZE_HEIGHT, null);
		String paramScale = ParameterUtils.getString(parameters, MediaConstants.PARAM_SIZE_SCALE_TYPE, MediaConstants.RESIZE_METHOD_SCALE_TO_FIT);
		if (paramW == null && paramH == null){
			return orgFile;
		}
		// rule 2: if want resizing, check if the resized file already existed
		File resizedFile = calcResizedFileName(orgFile, paramW, paramH, paramScale);
		if (resizedFile == null){
			return null;
		}
		if (resizedFile.exists() && resizedFile.isFile()){
			return resizedFile;
		}
		// rule 3: if not existed, check if this is an image file
		ImageFileInfo fileInfo = ImageUtils.getImageFileInfo(orgFile);
		if (fileInfo == null){
			return null;
		}
		int resizeW,resizeH;
		try{
			resizeW = paramW==null?0:(paramW.isEmpty()?0:Integer.parseInt(paramW));
			resizeH = paramH==null?0:(paramH.isEmpty()?0:Integer.parseInt(paramH));
			if (resizeW == 0 && resizeH == 0){
				return orgFile; // if no resize value, return original file
			}
		}catch(Exception e){
			return null; // w or h not valid integer
		}
		try {
			ImageUtils.createResizedFile(orgFile, resizedFile, resizeW, resizeH, paramScale);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		logger.warning("Cannot found file " + orgFile.getAbsolutePath());
		return resizedFile;
	}

	

	private File calcResizedFileName(File orgFile, String paramW, String paramH, String paramScale) {
		String fileName = orgFile.getName();
		int pos = fileName.lastIndexOf('.');
		if (pos < 0){
			return null;
		}
		fileName = String.format("%s%sx%s_%s", 
				MediaConstants.RESIZE_METHOD_SCALE_TO_FILL.equals(paramScale)?"f":"s",
				paramW==null?"0":paramW, paramH==null?"0":paramH, fileName);
		return new File(orgFile.getParentFile(), fileName);
	}

	public List<MediaData> search(Map<String, Object> parameters) throws Exception {
		boolean needAuth = ParameterUtils.getBoolean(parameters, MediaConstants.PARAM_NEED_AUTH, false);
		String appKey = ParameterUtils.getString(parameters, MediaConstants.PARAM_APPKEY, null);
		List<String> categories = (List<String>) parameters.get(MediaConstants.PARAM_CATEGORY);
		
		List<File> searchFrom = new ArrayList<File>();
		if (categories == null || categories.isEmpty()){
			searchFrom.add(FSMediaUtils.calcSearchFrom(new File(getBaseFolder()), needAuth, appKey, null));
		}else{
			boolean hasValue = false;
			for(String ctgName : categories){
				if (ctgName == null || ctgName.isEmpty()){
					continue;
				}
				File file = FSMediaUtils.calcSearchFrom(new File(getBaseFolder()), needAuth, appKey, ctgName);
				if (searchFrom.contains(file)){
					continue;
				}
				searchFrom.add(file);
				hasValue = true;
			}
			if (!hasValue){
				searchFrom.add(FSMediaUtils.calcSearchFrom(new File(getBaseFolder()), needAuth, appKey, null));
			}
		}
		
		String searchKey = ParameterUtils.getString(parameters, MediaConstants.PARAM_SEARCH_KEY, "");
		List<File> files = new ArrayList<File>();
		List<MediaData> results = new ArrayList();
		for(File folder : searchFrom) {
			searchFilesByKey(files, searchKey, folder);
			for(File file : files){
				MediaData fileInfo = FSMediaUtils.parseFileInfo(file, new File(getBaseFolder()));
				if (fileInfo == null){
					continue;
				}
				results.add(fileInfo);
			}
		}
		
		return results;
	}
	


	private void searchFilesByKey(List<File> files, String searchKey, File file) {
		if (file.isFile()){
			if (file.getAbsolutePath().toLowerCase().contains(searchKey)){
				if (!files.contains(file)){
					files.add(file);
				}
			}
			return;
		}
		File[] subFiles = file.listFiles();
		if (subFiles == null){
			return;
		}
		for(File subFile: subFiles){
			searchFilesByKey(files, searchKey, subFile);
		}
	}

	public int delete(String reqUri, Map<String, Object> parameters) throws Exception {
		String paramDeleteScope = ParameterUtils.getString(parameters, MediaConstants.PARAM_DELETE_SCOPE, MediaConstants.DELETE_SCOPE_ALL);
		String paramDeleteAppKey = ParameterUtils.getString(parameters, MediaConstants.PARAM_APPKEY, MediaConstants.DEFAULT_PARAM_APPKEY);
		String paramDeleteToken = ParameterUtils.getString(parameters, MediaConstants.PARAM_TOKEN, null);
		
		File tgtFile = new File(getBaseFolder(), reqUri);
		if (!tgtFile.exists() || !tgtFile.isFile()){
			return 0; // cannot found any thing to delete
		}
		
		String validToken = appOperationTokens.get(paramDeleteAppKey);
		if (validToken == null){
			return 0;
		}
		if ("example".equals(paramDeleteAppKey)){
			paramDeleteToken = validToken; // example zone can be delete by any body
		}
		if (!validToken.equals(paramDeleteToken)){
			// verify token
			return 0;
		}
		//verify appkey
		MediaData info = FSMediaUtils.parseFileInfo(tgtFile, new File(getBaseFolder()));
		if (info == null){
			return 0;
		}
		if (!paramDeleteAppKey.equals(info.getAppKey())){
			return 0;
		}
		if (MediaConstants.DELETE_SCOPE_SELF.equals(paramDeleteScope)){
			tgtFile.delete();
			return 1;
		}
		File[] files = tgtFile.getParentFile().listFiles();
		int cnt = 0;
		String fileNamePattern = "[fs]\\d+x\\d+_.*";
		String tgtFileName = tgtFile.getName();
		for(File file : files){
			String fileName = file.getName();
			if (fileName.equals(tgtFileName)){
				file.delete();
				cnt++;
				continue;
			}
			if (!fileName.matches(fileNamePattern)){
				continue;
			}
			if (!fileName.endsWith(tgtFileName)){
				continue;
			}
			file.delete();
			cnt++;
		}
		return cnt;
	}
	
	
}
