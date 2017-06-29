package com.skynet.mediaserver;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpData;
import io.netty.handler.codec.http.multipart.MemoryFileUpload;
import io.netty.handler.codec.http.multipart.MixedFileUpload;

public class SkynetHttpDataFactory extends DefaultHttpDataFactory {

	private boolean myUseDisk;
	private boolean myCheckSize;
	private long myMinSize;
	private long myMaxSize;

	public SkynetHttpDataFactory() {
		super();
		myUseDisk = false;
		myCheckSize = true;
		myMinSize = MINSIZE;
	}

	public SkynetHttpDataFactory(boolean useDisk, Charset charset) {
		super(useDisk, charset);
		myUseDisk = useDisk;
	}

	public SkynetHttpDataFactory(boolean useDisk) {
		super(useDisk);
		myUseDisk = useDisk;
		myCheckSize = false;
	}

	public SkynetHttpDataFactory(Charset charset) {
		super(charset);
		myUseDisk = false;
		myCheckSize = true;
		myMinSize = MINSIZE;
	}

	public SkynetHttpDataFactory(long minSize, Charset charset) {
		super(minSize, charset);
		myUseDisk = false;
		myCheckSize = true;
        myMinSize = minSize;
	}

	public SkynetHttpDataFactory(long minSize) {
		super(minSize);
		myUseDisk = false;
		myCheckSize = true;
        myMinSize = minSize;
	}

	@Override
	public FileUpload createFileUpload(HttpRequest request, String name, String filename, String contentType,
			String contentTransferEncoding, Charset charset, long size) {
		if (myUseDisk) {
			FileUpload fileUpload = createDiskFileUpload(name, filename, contentType, contentTransferEncoding, charset,
					size);
			fileUpload.setMaxSize(myMaxSize);
			doCheckHttpDataSize(fileUpload);
			List<HttpData> fileToDelete = retrieveList(request);
			fileToDelete.add(fileUpload);
			return fileUpload;
		}
		if (myCheckSize) {
			FileUpload fileUpload = createMixFileUpload(name, filename, contentType, contentTransferEncoding, charset,
					size);
			fileUpload.setMaxSize(myMaxSize);
			doCheckHttpDataSize(fileUpload);
			List<HttpData> fileToDelete = retrieveList(request);
			fileToDelete.add(fileUpload);
			return fileUpload;
		}
		MemoryFileUpload fileUpload = new MemoryFileUpload(name, filename, contentType, contentTransferEncoding,
				charset, size);
		fileUpload.setMaxSize(myMaxSize);
		doCheckHttpDataSize(fileUpload);
		return fileUpload;
	}

	private FileUpload createMixFileUpload(String name, String filename, String contentType,
			String contentTransferEncoding, Charset charset, long size) {
		FileUpload fileUpload = new MixedFileUpload(name, filename, contentType, contentTransferEncoding, charset,
				size, myMinSize);
		return fileUpload;
	}

	private FileUpload createDiskFileUpload(String name, String filename, String contentType,
			String contentTransferEncoding, Charset charset, long size) {
		FileUpload fileUpload = new DiskFileUpload(name, filename, contentType, contentTransferEncoding, charset,
				size);
		return fileUpload;
	}

	protected static void doCheckHttpDataSize(HttpData data) {
		try {
			data.checkSize(data.length());
		} catch (IOException ignored) {
			throw new IllegalArgumentException("Attribute bigger than maxSize allowed");
		}
	}

	protected List<HttpData> retrieveList(HttpRequest request) {
		Map<HttpRequest, List<HttpData>> reqFileDeleteMap = (Map<HttpRequest, List<HttpData>>) getField("requestFileDeleteMap");
		List<HttpData> list = reqFileDeleteMap.get(request);
		if (list == null) {
			list = new ArrayList<HttpData>();
			reqFileDeleteMap.put(request, list);
		}
		return list;
	}

	private Object getField(String fieldName) {
		try {
			Field field = this.getClass().getField(fieldName);
			field.setAccessible(true);
			return field.get(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
}
