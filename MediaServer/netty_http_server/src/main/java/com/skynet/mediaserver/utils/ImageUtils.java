package com.skynet.mediaserver.utils;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.skynet.mediaserver.MediaConstants;
import com.skynet.mediaserver.mediasaver.ImageFileInfo;

public class ImageUtils {
	protected static final Map<String, String> imageFilePostfixs = new HashMap<String,String>();
	static {
		imageFilePostfixs.put("bmp","bmp");
		imageFilePostfixs.put("dib", "bmp");
		imageFilePostfixs.put("gif", "gif");
		imageFilePostfixs.put("jfif","jpeg");
		imageFilePostfixs.put("jpg","jpeg");
		imageFilePostfixs.put("jpeg","jpeg");
		imageFilePostfixs.put("png","png");
		imageFilePostfixs.put("tif","bmp");
		imageFilePostfixs.put("tiff","bmp");
		imageFilePostfixs.put("ico","png");
	}

	public static ImageFileInfo getImageFileInfo(File orgFile) {
		// first, check the file postfix, if not an image file name, fail it.
		String fileName = orgFile.getName();
		int pos = fileName.lastIndexOf('.');
		if (pos <= 0) {
			return null;
		}
		String filePosfix = fileName.substring(pos + 1).toLowerCase();
		if (!imageFilePostfixs.containsKey(filePosfix)) {
			return null;
		}

		// and then, check the file should has width and height data.
		BufferedImage img = null;
		try {
			img = ImageIO.read(orgFile);
			if (img == null || img.getWidth(null) <= 0 || img.getHeight(null) <= 0) {
				return null;
			}
			ImageFileInfo result = new ImageFileInfo();
			result.setHeight(img.getHeight(null));
			result.setWidth(img.getWidth(null));
			return result;
		} catch (Exception e) {
			return null;
		} finally {
			img = null;
		}
	}

	public static void createResizedFile(File orgFile, File resizedFile, int resizeW, int resizeH, String scaleType) throws Exception{
		BufferedImage orgImage;
		Image newImage;
		BufferedImage bufferedImage;
		try{
			orgImage = ImageIO.read(orgFile);
			int orgW = orgImage.getWidth(null);
			int orgH = orgImage.getHeight(null);
			// calc the resized result width and height
			double orgScale = orgW * 1.0 / orgH;
			if (MediaConstants.RESIZE_METHOD_SCALE_TO_FILL.equalsIgnoreCase(scaleType)){
				if (resizeW == 0){
					resizeW=(int) (resizeH*orgScale);
				}else if (resizeH == 0){
					resizeH = (int) (resizeW/orgScale);
				}else {
				}
			}else{
				if (resizeW == 0){
					resizeW=(int) (resizeH*orgScale);
				}else if (resizeH == 0){
					resizeH = (int) (resizeW/orgScale);
				}else {
					resizeW = (int) Math.min(resizeW, resizeH*orgScale);
					resizeH = (int) Math.min(resizeH, resizeW/orgScale);
				}
			}
			
			newImage = orgImage.getScaledInstance(resizeW, resizeH, BufferedImage.SCALE_SMOOTH);
			bufferedImage= new BufferedImage(newImage.getWidth(null), newImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
			bufferedImage.getGraphics().drawImage(newImage, 0, 0, null);
			
			String fileName = orgFile.getName();
			int pos = fileName.lastIndexOf('.');
			String filePosfix = fileName.substring(pos + 1).toLowerCase();
			String imageType = imageFilePostfixs.get(filePosfix);
			ImageIO.write(bufferedImage, imageType, resizedFile);
		}finally{
			orgImage = null;
			newImage = null;
			bufferedImage= null;
		}

	}

}
