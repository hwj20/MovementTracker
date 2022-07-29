package com.example.movementtracker;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class UploadUtils {
    private static final String TAG = "uploadFile";
    private static final int TIME_OUT = 30 * 1000; // 超时时间
    private static final String CHARSET = "utf-8"; // 设置编码
    private static final String PREFIX = "--", LINE_END = "\r\n";
    /**
     * Android上传message到服务端 (POST)
     * @param message 需要上传的string
     * @param RequestURL 请求的rul
     * @param port 请求的端口
     * @return 返回响应的内容
     */
    public static String uploadMessage(String message, String RequestURL, int port) {
        String result = "upload failed";
        String CONTENT_TYPE = "application/www-form-ulrencoded";
        try {
            URL targetUrl = new URL(RequestURL);
            URL url = new URL(targetUrl.getProtocol(),targetUrl.getHost(), port, targetUrl.getFile());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIME_OUT);
            conn.setConnectTimeout(TIME_OUT);
            conn.setDoInput(true); // 允许输入流
            conn.setRequestMethod("POST"); // 请求方式
            conn.setRequestProperty("Charset", CHARSET); // 设置编码
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE);
            DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
            dos.write(message.getBytes());

            dos.flush();

            int res = conn.getResponseCode();
            Log.d(TAG, "response code:" + res);
            if(res==200) {
                result = "success upload";
                Log.d(TAG, "request success");
            }
            else {
                Log.e(TAG, "request error");
            }
        }
        catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        return result;
    }
    /**
     * Android上传文件到服务端 (POST)
     *
     * @param file 需要上传的文件
     * @param RequestURL 请求的rul
     * @return 返回响应的内容
     */
    public static String uploadFile(File file, String RequestURL, int port) {
        String result = null;
        String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成
        String CONTENT_TYPE = "multipart/form-data"; // 内容类型
        try {
            URL targetUrl = new URL(RequestURL);
            URL url = new URL(targetUrl.getProtocol(),targetUrl.getHost(), port, targetUrl.getFile());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIME_OUT);
            conn.setConnectTimeout(TIME_OUT);
            conn.setDoInput(true); // 允许输入流
            conn.setDoOutput(true); // 允许输出流
            conn.setUseCaches(false); // 不允许使用缓存
            conn.setRequestMethod("POST"); // 请求方式
            conn.setRequestProperty("Charset", CHARSET); // 设置编码
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);
            if (file != null) {
                /*
                 * 当文件不为空，把文件包装并且上传
                 */
                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                String sb = PREFIX +
                        BOUNDARY +
                        LINE_END +
                        /*
                         * 这里重点注意： name里面的值为服务端需要key 只有这个key 才可以得到对应的文件
                         * filename是文件的名字，包含后缀名的 比如:abc.png
                         */
                        "Content-Disposition: form-data; name=\"uploadfile\"; filename=\""
                        + file.getName() + "\"" + LINE_END +
                        "Content-Type: application/octet-stream; charset=" + CHARSET + LINE_END +
                        LINE_END;
                dos.write(sb.getBytes());
                InputStream is = new FileInputStream(file);
                byte[] bytes = new byte[1024];
                int len = 0;
                while ((len = is.read(bytes)) != -1) {
                    Log.d(TAG,"data  "+ new String(bytes));
                    dos.write(bytes, 0, len);
                }
                is.close();
                dos.write(LINE_END.getBytes());
                byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END).getBytes();
                dos.write(end_data);
                dos.flush();
                /*
                  获取响应码 200=成功 当响应成功，获取响应的流
                 */
                int res = conn.getResponseCode();
                Log.e(TAG, "response code:" + res);
                if(res==200) {
                    Log.d(TAG, "request success");
                    InputStream input = conn.getInputStream();
                    StringBuilder sb1 = new StringBuilder();
                    int ss;
                    while ((ss = input.read()) != -1) {
                        sb1.append((char) ss);
                    }
                    result = sb1.toString();
                    Log.d(TAG, "result : " + result);
                }
                else{
                    Log.e(TAG, "request error");
                }
            }
        } catch (IOException e) {
            Log.w(TAG, e.toString());
        }
        return result;
    }
}
