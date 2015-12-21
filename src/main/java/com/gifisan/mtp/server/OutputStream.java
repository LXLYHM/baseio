package com.gifisan.mtp.server;

import java.io.IOException;

public interface OutputStream {
	/**
	 * 文本类型写入结束后要做flush操作
	 * 
	 * @param b
	 * @throws MTPChannelException 
	 * @throws IOException
	 */
	public abstract void write(byte b) throws IOException;
	
	/**
	 * 文本类型写入结束后要做flush操作
	 * 
	 * @param bytes
	 * @throws MTPChannelException 
	 * @throws IOException
	 */
	public abstract void write(byte[] bytes) throws IOException;

	/**
	 * 文本类型写入结束后要做flush操作
	 * 
	 * @param bytes
	 * @param offset
	 * @param length
	 * @throws IOException
	 */
	public abstract void write(byte[] bytes, int offset, int length) throws IOException;
	
	
}
