package com.dy.travel.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import android.util.Log;
import android.util.SparseArray;

/**
 * @author chh
 */
public class HttpBase {



	private Map<String, String> fileMap = null;
	private Map<String, String> paramMap = null;
	private Map<String, String> handerMap = null;
	private Map<String, List<String>> cookieMap = null;
	private String postUrl = null;
	private String charset = null;
	private int responseCode = 0;
	private String responseText = null;
	private DownListener listen = null;// 下载监听
	private boolean read = true;// 是否在读取中
	private boolean cancel = false;// 是否取消本次请求
	private boolean followRedirects = true;// 是否302自动跳转
	private final int TIMEOUT = 30000;// 超时时间30秒

	public HttpBase(String postUrl) {
		fileMap = new HashMap<String, String>();
		paramMap = new HashMap<String, String>();
		handerMap = new HashMap<String, String>();
		cookieMap = new HashMap<String, List<String>>();
		this.postUrl = postUrl;
		this.charset = "GBK";
	}

	public DownListener getListen() {
		return listen;
	}

	public void setListen(DownListener listen) {
		this.listen = listen;
	}

	public void addFile(String key, String fileName) {
		fileMap.put(key, fileName);
	}

	public void addParam(String key, String value) {
		paramMap.put(key, value);
	}

	public void addHander(String key, String value) {
		handerMap.put(key, value);
	}

	public void clearHander() {
		handerMap.clear();
	}

	public void clearParam() {
		paramMap.clear();
	}

	public void clearFile() {
		fileMap.clear();
	}

	public void setFollowRedirects(boolean followRedirects) {
		this.followRedirects = followRedirects;
	}

	public void reset() {
		clearHander();
		clearParam();
		clearFile();
	}

	public Map<String, List<String>> getCookies() {
		return cookieMap;
	}

	public List<String> getCookie(String key) {
		List<String> values = null;
		if (cookieMap != null) {
			values = cookieMap.get(key);
		}
		return values;
	}

	public void setCookie(Map<String, List<String>> cookieMap) {
		this.cookieMap = cookieMap;
	}

	/**
	 * 用get方式获取数据
	 */
	public void get() {
		get(TIMEOUT);
	}

	public void get(int timeout) {
		try {
			responseCode = 0;
			responseText = null;

			StringBuffer sbuf = new StringBuffer(postUrl);
			if (paramMap.size() > 0) {
				sbuf.append("?");
				Set<String> set = paramMap.keySet();
				Iterator<String> it = set.iterator();
				String key = null;
				String value = null;
				int count = 1;
				while (it.hasNext()) {
					key = it.next();
					value = paramMap.get(key);
					if (key != null && value != null) {
						sbuf.append(key).append("=").append(value);
						if (count < set.size()) {
							sbuf.append("&");
						}
						count++;
					}
				}
			}

			URL url = new URL(sbuf.toString());
			// 分隔标记符号Mozilla/4.0
			HttpURLConnection.setFollowRedirects(followRedirects);
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setConnectTimeout(timeout);
			httpConn.setReadTimeout(timeout);
			if (httpConn != null) {
				httpConn.setRequestProperty("Accept", "*/*");
				httpConn.setRequestProperty("Accept-Language", "zh-cn");
				httpConn.setRequestProperty("User-Agent",
						"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0; Trident/4.0; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; .NET CLR 3.5.30729; .NET CLR 3.0.30729)");
				// httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary="+boundary);
				if (url.getPort() == 80 || url.getPort() == -1) {
					httpConn.setRequestProperty("Host", url.getHost());
				} else {
					httpConn.setRequestProperty("Host", url.getHost() + ":" + url.getPort());
				}
				httpConn.setRequestProperty("Pragma", "no-cache");
				if (handerMap.size() > 0) {
					Iterator<String> it = handerMap.keySet().iterator();
					while (it.hasNext()) {
						String key = it.next();
						String value = handerMap.get(key);
						httpConn.setRequestProperty(key, value);
					}
				}
				// System.out.println(url.getPort());

				// /////////////////////设置cookie///////////////////////////////
				if (cookieMap != null && cookieMap.size() > 0) {
					Iterator<String> it = cookieMap.keySet().iterator();
					String key = null;
					sbuf = new StringBuffer();
					while (it.hasNext()) {
						key = it.next();
						sbuf.append(key).append("=").append(cookieMap.get(key).get(0)).append(";");
					}
					// 删掉最后一个分号
					if (sbuf.length() > 0) {
						sbuf.deleteCharAt(sbuf.length() - 1);
					}
					httpConn.addRequestProperty("Cookie", sbuf.toString());
				}
				// //////////////////////////////////////////////////////////////

				httpConn.setRequestMethod("GET");
				// httpConn.setDoOutput(true);
				httpConn.setDoInput(true);
				// 流写入表单数据

				responseCode = httpConn.getResponseCode();
				BufferedReader br = null;
				if (responseCode == 200) {
					br = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), getCharset()));
				} else if (responseCode != 302) {
					br = new BufferedReader(new InputStreamReader(httpConn.getErrorStream(), getCharset()));
				}
				if (br != null) {
					sbuf = new StringBuffer();
					String tmp = br.readLine();
					while (tmp != null) {
						sbuf.append(tmp);
						sbuf.append("\n");
						tmp = br.readLine();
					}
					responseText = sbuf.toString();
				}

				/* 读取cookie */
				Map<String, List<String>> headers = httpConn.getHeaderFields();
				Iterator<String> it = headers.keySet().iterator();
				String key = null;
				String location = null;
				while (it.hasNext()) {
					key = it.next();
					if (key != null && key.toLowerCase(Locale.CHINA).equals("set-cookie")) {
						List<String> cookieList = headers.get(key);
						if (cookieList != null && cookieList.size() > 0) {
							for (String cookie : cookieList) {
								if (cookie != null && cookie.length() > 0) {
									String args[] = cookie.split(";");
									if (args != null) {
										for (String arg : args) {
											if (arg != null && arg.length() > 0) {
												String cookies[] = arg.split("=");
												if (cookies != null && cookies.length == 2) {
													List<String> values = cookieMap.get(cookies[0]);
													if (values == null) {
														values = new ArrayList<String>();
													}
													values.add(cookies[1]);
													// Log.d("ucapp", cookies[0]+"="+cookies[1]);
													cookieMap.put(cookies[0], values);
												}
											}
										}
									}
								}
							}
						}
					} else if (key != null && key.toLowerCase(Locale.CHINA).equals("location")) {
						List<String> ls = headers.get(key);
						if (ls != null && ls.size() > 0) {
							location = ls.get(0);
						}
					}
				}

				httpConn.disconnect();

				if (location != null) {// 有重定向
					postUrl = location;
					reset();
					get();

				}
			}
		} catch (MalformedURLException ex) {
			Log.e(HttpBase.class.getName(), "MalformedURLException", ex);
		} catch (IOException ex) {
			responseCode = -1;
			Log.e(HttpBase.class.getName(), "IOException", ex);
		} catch (Exception ex) {
			responseCode = -1;
			Log.e(HttpBase.class.getName(), "Exception", ex);
		}
	}

	/**
	 * post方式提交
	 */
	public void post() {
		post(TIMEOUT);
	}

	public void post(int timeout) {
		try {
			responseCode = 0;
			responseText = null;
			URL url = new URL(postUrl);
			// 分隔标记符号
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setConnectTimeout(timeout);
			httpConn.setReadTimeout(timeout);
			if (httpConn != null) {
				httpConn.setRequestProperty("Accept", "*/*");
				httpConn.setRequestProperty("Accept-Language", "zh-cn");
				httpConn.setRequestProperty("User-Agent",
						"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0; Trident/4.0; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; .NET CLR 3.5.30729; .NET CLR 3.0.30729)");
				httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=" + getCharset());
				if (url.getPort() == 80 || url.getPort() == -1) {
					httpConn.setRequestProperty("Host", url.getHost());
				} else {
					httpConn.setRequestProperty("Host", url.getHost() + ":" + url.getPort());
				}
				httpConn.setRequestProperty("Pragma", "no-cache");
				if (handerMap.size() > 0) {
					Iterator<String> it = handerMap.keySet().iterator();
					while (it.hasNext()) {
						String key = it.next();
						String value = handerMap.get(key);
						httpConn.setRequestProperty(key, value);
					}
				}
				// System.out.println(url.getPort());

				// /////////////////////设置cookie///////////////////////////////
				if (cookieMap != null && cookieMap.size() > 0) {
					Iterator<String> it = cookieMap.keySet().iterator();
					String key = null;
					StringBuffer sbuf = new StringBuffer();
					while (it.hasNext()) {
						key = it.next();
						sbuf.append(key).append("=").append(cookieMap.get(key).get(0)).append(";");
					}
					// 删掉最后一个分号
					if (sbuf.length() > 0) {
						sbuf.deleteCharAt(sbuf.length() - 1);
					}
					httpConn.addRequestProperty("Cookie", sbuf.toString());
				}
				// //////////////////////////////////////////////////////////////

				httpConn.setRequestMethod("POST");
				httpConn.setDoOutput(true);
				httpConn.setDoInput(true);
				// 流写入表单数据
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(httpConn.getOutputStream(), getCharset()));

				// 先写入表单参数
				if (paramMap.size() > 0) {
					Set<String> set = paramMap.keySet();
					Iterator<String> it = set.iterator();
					String key = null;
					String value = null;
					int count = 1;
					while (it.hasNext()) {
						key = it.next();
						value = paramMap.get(key);
						if (key != null && value != null) {
							Log.e("POST", key + "=" + value);
							bw.write(key);
							bw.write("=");
							bw.write(value.replace("%", "%25"));
							if (count < set.size()) {
								bw.write("&");
							}
							count++;
						}
					}
				}

				bw.flush();
				bw.close();

				responseCode = httpConn.getResponseCode();
				BufferedReader br = null;
				if (responseCode == 200) {
					br = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), getCharset()));
				} else {
					br = new BufferedReader(new InputStreamReader(httpConn.getErrorStream(), getCharset()));
				}
				StringBuffer sbuf = new StringBuffer();
				String tmp = br.readLine();
				while (tmp != null) {
					sbuf.append(tmp);
					tmp = br.readLine();
					if (tmp != null) {
						sbuf.append("\n");
					}
				}
				responseText = sbuf.toString();

				/* 读取cookie */
				Map<String, List<String>> headers = httpConn.getHeaderFields();
				Iterator<String> it = headers.keySet().iterator();
				while (it.hasNext()) {
					String key = it.next();
					if (key != null && key.toLowerCase(Locale.CHINA).equals("set-cookie")) {
						List<String> cookieList = headers.get(key);
						if (cookieList != null && cookieList.size() > 0) {
							for (String cookie : cookieList) {
								if (cookie != null && cookie.length() > 0) {
									String args[] = cookie.split(";");
									if (args != null) {
										for (String arg : args) {
											if (arg != null && arg.length() > 0) {
												String cookies[] = arg.split("=");
												if (cookies != null && cookies.length == 2) {
													List<String> values = cookieMap.get(cookies[0]);
													if (values == null) {
														values = new ArrayList<String>();
													}
													values.add(cookies[1]);
													// Log.d("ucapp", cookies[0]+"="+cookies[1]);
													cookieMap.put(cookies[0], values);
												}
											}
										}
									}
								}
							}
						}
					}
				}

				httpConn.disconnect();
			}
		} catch (MalformedURLException ex) {
			Log.e(HttpBase.class.getName(), "MalformedURLException", ex);
		} catch (IOException ex) {
			responseCode = -1;
			Log.e(HttpBase.class.getName(), "IOException", ex);
		} catch (Exception ex) {
			responseCode = -1;
			Log.e(HttpBase.class.getName(), "Exception", ex);
		}
	}

	public void upload() {
		try {
			responseCode = 0;
			responseText = null;
			URL url = new URL(postUrl);
			// 分隔标记符号
			String boundary = "---------------------------7da2a82d1806d6";
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			if (httpConn != null) {
				httpConn.setRequestProperty(
						"Accept",
						" image/gif, image/jpeg, image/pjpeg, application/x-ms-application, application/vnd.ms-xpsdocument, application/xaml+xml, application/x-ms-xbap, application/x-shockwave-flash, application/msword, application/vnd.ms-excel, application/vnd.ms-powerpoint, */*");
				httpConn.setRequestProperty("Accept-Language", "zh-cn");
				httpConn.setRequestProperty("User-Agent",
						"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0; Trident/4.0; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; .NET CLR 3.5.30729; .NET CLR 3.0.30729)");
				httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
				if (url.getPort() == 80) {
					httpConn.setRequestProperty("Host", url.getHost());
				} else {
					httpConn.setRequestProperty("Host", url.getHost() + ":" + url.getPort());
				}
				httpConn.setRequestProperty("Pragma", "no-cache");
				if (handerMap.size() > 0) {
					Iterator<String> it = handerMap.keySet().iterator();
					while (it.hasNext()) {
						String key = it.next();
						String value = handerMap.get(key);
						httpConn.setRequestProperty(key, value);
					}
				}

				httpConn.setRequestMethod("POST");
				httpConn.setDoOutput(true);
				httpConn.setDoInput(true);
				// 生成一个临时流写入表单数据
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(baos, getCharset()));
				// 因为写文件要用字节流，所以要使用bos
				BufferedOutputStream bos = new BufferedOutputStream(httpConn.getOutputStream());

				// 先写入表单参数
				Iterator<String> it = paramMap.keySet().iterator();
				String key = null;
				String value = null;
				while (it.hasNext()) {
					key = it.next();
					value = paramMap.get(key);
					if (key != null && value != null) {
						// 要在分隔标志符号前加--以及后面加\r\n
						bw.write("--" + boundary + "\r\n");
						bw.write("Content-Disposition: form-data; name=\"" + key + "\"\r\n\r\n");
						bw.write(value);
						bw.write("\r\n");
					}
				}
				bw.flush();
				bos.write(baos.toByteArray());

				// 写入文件
				byte[] buf = new byte[2048];// 读入流缓存
				int len = 0;
				// int fileCount = 0;
				it = fileMap.keySet().iterator();
				String fileName = null;
				while (it.hasNext()) {
					key = it.next();
					value = fileMap.get(key);
					try {
						File file = new File(value);
						if (file.exists() && file.isFile()) {
							BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
							len = bis.read(buf);
							fileName = file.getName();
							baos.reset();
							// 要在分隔标志符号前加--以及后面加\r\n
							bw.write("--" + boundary + "\r\n");
							bw.write("Content-Disposition: form-data; name=\"" + key + "\"; filename=\"" + fileName + "\"\r\n");
							bw.write("Content-Type: ");
							if (fileName.toLowerCase(Locale.CHINA).endsWith(".jpg")) {
								bw.write("image/pjpeg\r\n\r\n");
							} else {
								bw.write("image/gif\r\n\r\n");
							}
							bw.flush();
							bos.write(baos.toByteArray());
							while (len > 0) {
								bos.write(buf, 0, len);
								len = bis.read(buf);
							}
							bis.close();
							bos.write(13);// r
							bos.write(10);// n
						} else {
							Log.i(HttpBase.class.getName(), "无法读取信息");
						}
					} catch (IOException ie) {
						Log.e(HttpBase.class.getName(), "上传文件时，发生错误！", ie);
					}
					// fileCount ++;
				}
				baos.reset();
				// 结束要在分隔标志符号前加--以及后面加--
				bw.write("--" + boundary + "--");
				bw.flush();
				bos.write(baos.toByteArray());
				bos.flush();
				bos.close();
				// 提交数据完成

				responseCode = httpConn.getResponseCode();
				BufferedReader br = null;
				if (responseCode == 200) {
					br = new BufferedReader(new InputStreamReader(httpConn.getInputStream(), getCharset()));
				} else {
					br = new BufferedReader(new InputStreamReader(httpConn.getErrorStream(), getCharset()));
				}
				StringBuffer sbuf = new StringBuffer();
				String tmp = br.readLine();
				while (tmp != null) {
					sbuf.append(tmp);
					tmp = br.readLine();
				}
				responseText = sbuf.toString();

				httpConn.disconnect();
			}
		} catch (MalformedURLException ex) {
			Log.e(HttpBase.class.getName(), "MalformedURLException", ex);
		} catch (IOException ex) {
			responseCode = -1;
			Log.e(HttpBase.class.getName(), "IOException", ex);
		} catch (Exception ex) {
			responseCode = -1;
			Log.e(HttpBase.class.getName(), "Exception", ex);
		}
	}

	/**
	 * 取消下载,配合getFileAndSave使用。
	 *
	 * @param cancel
	 */
	public void cancelDownFile(boolean cancel) {
		this.cancel = cancel;
	}

	public String getFileAndSave(String path, long startByte) {
		// 设置默认超时时间为15秒
		return getFileAndSave(path, startByte, TIMEOUT);
	}

	/**
	 * 下载并保存
	 */
	public String getFileAndSave(String path, long startByte, int timeout) {

		File tempFile = new File(path + ".ucf");// 临时文件
		File downFile = new File(path);// 正式文件
		try {
			cancel = false;
			responseCode = 0;
			responseText = null;
			final SparseArray<Long> byteMap = new SparseArray<Long>();
			long totalByte = 0;
			long downByte = 0;
			byteMap.put(0, downByte);
			byteMap.put(1, totalByte);

			StringBuffer sbuf = new StringBuffer(postUrl);
			if (paramMap.size() > 0) {
				sbuf.append("?");
				Set<String> set = paramMap.keySet();
				Iterator<String> it = set.iterator();
				String key = null;
				String value = null;
				int count = 1;
				while (it.hasNext()) {
					key = it.next();
					value = paramMap.get(key);
					if (key != null && value != null) {
						sbuf.append(key).append("=").append(value);
						if (count < set.size()) {
							sbuf.append("&");
						}
						count++;
					}
				}
			}

			URL url = new URL(sbuf.toString());
			// 分隔标记符号
			HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
			httpConn.setConnectTimeout(timeout);
			httpConn.setReadTimeout(timeout);
			httpConn.setRequestProperty("Accept", "*/*");
			httpConn.setRequestProperty("Accept-Language", "zh-cn");
			httpConn.setRequestProperty("User-Agent",
					"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0; Trident/4.0; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; .NET CLR 3.5.30729; .NET CLR 3.0.30729)");
			if (startByte > 0) {// 是否断点续传
				httpConn.setRequestProperty("RANGE", "bytes=" + startByte + "-");
			}
			httpConn.setRequestProperty("Host", url.getHost());
			httpConn.setRequestProperty("Pragma", "no-cache");
			if (handerMap.size() > 0) {
				Iterator<String> it = handerMap.keySet().iterator();
				while (it.hasNext()) {
					String key = it.next();
					String value = handerMap.get(key);
					httpConn.setRequestProperty(key, value);
				}
			}

			httpConn.setRequestMethod("GET");
			// httpConn.setDoOutput(true);
			httpConn.setDoInput(true);
			// 流写入表单数据
			totalByte = httpConn.getContentLength();
			if (totalByte > 0) {
				byteMap.put(1, totalByte);
				Log.d("ucapp", "down file size=" + totalByte);
			}
			responseCode = httpConn.getResponseCode();
			read = true;
			if (responseCode == 200 || responseCode == 206) {
				if (responseCode == 206) {
					// 断点续传
					downByte = startByte;
					byteMap.put(0, downByte);
					// raf.seek(downByte);
					totalByte += downByte;
					byteMap.put(1, totalByte);
				}
				// 使用文件映射提高性能
				long memSize = totalByte - downByte;
				BufferedInputStream bis = new BufferedInputStream(httpConn.getInputStream());
				byte[] buf = new byte[8192];
				int len = bis.read(buf);
				// 启动下载监听线程
				if (len > 0 && listen != null) {
					new Thread() {
						public void run() {
							long downByte = byteMap.get(0);
							long totalByte = byteMap.get(1);
							while (read && !cancel) {
								listen.progress(totalByte, downByte);
								try {
									Thread.sleep(1000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								downByte = byteMap.get(0);
								totalByte = byteMap.get(1);
							}
						}
					}.start();
				}
				// //////////////////监听结束/////////////////////////////////
				if (memSize > 0) {// 有固定长度的,用映射文件
					RandomAccessFile raf = new RandomAccessFile(tempFile, "rw");
					FileChannel fco = raf.getChannel();
					MappedByteBuffer mbuf = fco.map(FileChannel.MapMode.READ_WRITE, downByte, memSize);
					while (len > 0 && !cancel) {
						// raf.write(buf, 0, len);
						mbuf.put(buf, 0, len);
						downByte = downByte + len;
						byteMap.put(0, downByte);
						len = bis.read(buf);
					}
					fco.close();
					raf.close();
				} else {
					// 无固定长度,不支持断点下载,直接用输出流
					BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tempFile));
					while (len > 0 && !cancel) {
						bos.write(buf, 0, len);
						downByte = downByte + len;
						byteMap.put(0, downByte);
						len = bis.read(buf);
					}
					bos.close();
				}
				bis.close();
				Log.d("ucapp", "download file downByte=" + downByte);
				// 结束了,并且正常下载完
				if (!cancel) {
					// 原文件存在,先删除
					if (downFile.exists()) {
						downFile.delete();
					}
					tempFile.renameTo(downFile);
				}
				// 如果有监听,通知
				if (listen != null) {
					listen.progress(totalByte, downByte);
					if (!cancel) {// 如果不是中止的,为正常完成
						listen.onComplete();
					} else {// 如果中止了的
						listen.onCancel();
					}
				}
			} else {
				if (startByte == 0 && tempFile.exists()) {// 如果不是断点续传,需要删除建立的文件
					tempFile.delete();
				}
				Log.d("ucapp", "HttpBase.getFileAndSave(),responseCode=" + responseCode);
				if (listen != null) {
					listen.onError(-1);
				}
			}
			httpConn.disconnect();
		} catch (MalformedURLException ex) {
			Log.e(HttpBase.class.getName(), "MalformedURLException", ex);
			if (listen != null) {
				listen.onError(-1);
			}
		} catch (IOException ex) {
			Log.e(HttpBase.class.getName(), "IOException", ex);
			if (listen != null) {
				listen.onError(-1);
			}
		}
		read = false;
		return downFile.getPath();
	}

	// public byte[] getFileAndSave(String path){
	//
	// ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
	//
	// try {
	// responseCode = 0;
	// responseText = null;
	//
	// StringBuffer sbuf = new StringBuffer(postUrl);
	// if(paramMap.size()>0){
	// sbuf.append("?");
	// Set<String> set = paramMap.keySet();
	// Iterator<String> it = set.iterator();
	// String key = null;
	// String value = null;
	// int count = 1;
	// while(it.hasNext()){
	// key = it.next();
	// value = paramMap.get(key);
	// if(key!=null && value!=null){
	// sbuf.append(key).append("=").append(value);
	// if(count<set.size()){
	// sbuf.append("&");
	// }
	// count ++;
	// }
	// }
	// }
	//
	// URL url = new URL(sbuf.toString());
	// //分隔标记符号
	// HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
	// httpConn.setRequestProperty("Accept", "*/*");
	// httpConn.setRequestProperty("Accept-Language","zh-cn");
	// httpConn.setRequestProperty("User-Agent","Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0; Trident/4.0; SLCC1; .NET CLR 2.0.50727; Media Center PC 5.0; .NET CLR 3.5.30729; .NET CLR 3.0.30729)");
	// httpConn.setRequestProperty("Host", url.getHost());
	// httpConn.setRequestProperty("Pragma","no-cache");
	//
	// httpConn.setRequestMethod("GET");
	// // httpConn.setDoOutput(true);
	// httpConn.setDoInput(true);
	// //流写入表单数据
	//
	// responseCode = httpConn.getResponseCode();
	//
	// BufferedInputStream bis = null;
	// if(responseCode==200){
	// bis = new BufferedInputStream(httpConn.getInputStream());
	// }
	// byte[] buf = new byte[4096];
	// int len = bis.read(buf);
	// while(len>0){
	// byteOut.write(buf, 0, len);
	// len = bis.read(buf);
	// }
	// bis.close();
	//
	// httpConn.disconnect();
	// } catch (MalformedURLException ex) {
	// Log.e(HttpBase.class.getName(), "MalformedURLException", ex);
	// } catch (IOException ex) {
	// Log.e(HttpBase.class.getName(), "IOException", ex);
	// }
	//
	// return byteOut.toByteArray();
	// }

	/**
	 * @return the responseCode
	 */
	public int getResponseCode() {
		return responseCode;
	}

	/**
	 * @param responseCode
	 *            the responseCode to set
	 */
	public void setResponseCode(int responseCode) {
		this.responseCode = responseCode;
	}

	/**
	 * @return the responseText
	 */
	public String getResponseText() {
		return responseText;
	}

	/**
	 * @param responseText
	 *            the responseText to set
	 */
	public void setResponseText(String responseText) {
		this.responseText = responseText;
	}

	/**
	 * @return the charset
	 */
	public String getCharset() {
		return charset;
	}

	/**
	 * @param charset
	 *            the charset to set
	 */
	public void setCharset(String charset) {
		this.charset = charset;
	}

	public void saveFile(byte[] buf, String fileName) {
		if (buf != null && buf.length > 0 && fileName != null && fileName.length() > 0) {
			try {
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(fileName));
				bos.write(buf);
				bos.close();
			} catch (FileNotFoundException ex) {
				Log.e(HttpBase.class.getName(), "FileNotFoundException", ex);
			} catch (IOException ex) {
				Log.e(HttpBase.class.getName(), "IOException", ex);
			}
		}
	}

	/**
	 * 下载监听
	 *
	 * @author chh
	 */
	public interface DownListener {
		/**
		 * @param downByte
		 */
		public void progress(long totalByte, long downByte);

		/**
		 * 下载完成
		 */
		public void onComplete();

		/**
		 * 取消下载时
		 */
		public void onCancel();

		/**
		 * 发生错误时
		 *
		 * @param err
		 */
		public void onError(int err);
	};

	// public static void main(String args[]){
	// // Properties prop = System.getProperties();
	// // //设置http访问要使用的代理服务器的地址
	// // prop.setProperty("http.proxyHost", "127.0.0.1");
	// // //设置http访问要使用的代理服务器的端口
	// // prop.setProperty("http.proxyPort", "8800");
	// //http://192.168.1.122:8084/jttz/servlet/UpLoadFileServlet
	// HttpBase httpUpload = new HttpBase("http://www.gx12580.net/images/upload/merchantimages/142905.jpg");
	// httpUpload.setCharset("UTF-8");
	// byte[] buf = httpUpload.getFile();
	// try {
	// BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("d:\\142905.jpg"));
	// bos.write(buf);
	// bos.close();
	// // httpUpload.addFile("file1","e:\\pp.jpg");
	// // httpUpload.addFile("file1","d:\\ccc.jpg");
	// // httpUpload.addParam("inp", "titleImg");
	// // httpUpload.upload();
	// } catch (FileNotFoundException ex) {
	// Logger.getLogger(HttpBase.class.getName()).log(Level.SEVERE, null, ex);
	// } catch (IOException ex) {
	// Logger.getLogger(HttpBase.class.getName()).log(Level.SEVERE, null, ex);
	// }
	// // httpUpload.addFile("file1","e:\\pp.jpg");
	// // httpUpload.addFile("file1","d:\\ccc.jpg");
	// // httpUpload.addParam("inp", "titleImg");
	// // httpUpload.upload();
	//
	// System.out.println(httpUpload.getResponseCode());
	// System.out.println(httpUpload.getResponseText());
	// }


}