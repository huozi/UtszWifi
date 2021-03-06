package com.school.huozi.wifiHelper.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Created by zhan on 2016/7/4.
 */
public class HttpUtil {

    /**
     * 判断是否能够连接上互联网
     */
    public static boolean checkConnect() {
        // 个人觉得使用MIUI这个链接有失效的风险
        final String checkUrl = "http://connect.rom.miui.com/generate_204";
        final int SOCKET_TIMEOUT_MS = 1000;

        HttpURLConnection connection = null;
        try {
            URL url = new URL(checkUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(SOCKET_TIMEOUT_MS);
            connection.setReadTimeout(SOCKET_TIMEOUT_MS);
            connection.setUseCaches(false);
            connection.connect();

            return connection.getResponseCode() == 204;
        } catch (Exception e) {
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }


    /**
     * 发送http post请求
     * @param address 链接url
     * @param headerMap 请求头map 不可为null
     * @param paramsMap 参数map
     */
    public static String sendPostRequest(final String address,
            Map<String, String> headerMap, Map<String, String> paramsMap) {

        HttpURLConnection connection = null;
        StringBuilder response = new StringBuilder();
        try {
            URL url = new URL(address);
            connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(Const.SOCKET_TIMEOUT_MS);
            connection.setReadTimeout(Const.SOCKET_TIMEOUT_MS);

            // 设置特殊请求头
            for (String header_name : headerMap.keySet()) {
                connection.setRequestProperty(header_name,
                        headerMap.get(header_name));
            }
            // 发送参数
            String params  = urlEncode(paramsMap);
            OutputStream out = connection.getOutputStream();
            out.write(params.getBytes());

            // 接收响应内容
            InputStream in = connection.getInputStream();
            String encoding = connection.getContentEncoding();
            // 判断返回的信息是否压缩
            if(!Utils.isBlank(encoding) && encoding.contains("gzip")) {
                in = new GZIPInputStream(in);
            }
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (Exception e) {
            return "";
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return response.toString();
    }


    /**
     * 将参数map转换成url参数形式: k1=v1&k2=v2...
     * @param paramsMap map类型的参数
     */
    private static String urlEncode(Map<String, String> paramsMap) {
        if (paramsMap == null || paramsMap.isEmpty()) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String param : paramsMap.keySet()) {
            builder.append(param);
            builder.append("=");
            builder.append(paramsMap.get(param));
            builder.append("&");
        }
        // 除去最后多余的 &
        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }
}
