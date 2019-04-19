package com.lyc.callerinfo.http;

import java.io.File;

/**
 * @author: Jason
 * @date: 2016/6/12.
 * @desc: 文件下载回调接口
 */
public interface HttpFileCallBack {

	/**
	 * 下载过程中的进度回调方法
	 * @param currentSize
	 * @param totalSize
	 * @param progress
	 * @param networkSpeed
	 */
	public void inProgress(long currentSize, long totalSize, float progress, long networkSpeed);
	
	/**
	 * 下载成功的文件回调方法
	 * @param file
	 */
	public void onSuccess(File file);
	
	/**
	 * 下载失败的异常搜集回调方法
	 * @param e
	 */
	public void onError(Exception e);
}
