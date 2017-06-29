package com.skynet.mediaserver.mediasaver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.skynet.mediaserver.MediaData;
import com.skynet.mediaserver.MedieResourceSaver;
import com.skynet.mediaserver.utils.FSMediaUtils;

public class SimpleFileSystemMediaSaver implements MedieResourceSaver{
	protected String repoistoryFolder = "/tmp";
	
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
	

}
