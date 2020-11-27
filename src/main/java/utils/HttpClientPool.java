package utils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * @author by laugh on 2016/3/29.
 */
public class HttpClientPool {

    private static volatile HttpClientPool clientInstance;
    private HttpClient httpClient;

    public static HttpClientPool getHttpClient() {
        HttpClientPool tmp = clientInstance;
        if (tmp == null) {
            synchronized (HttpClientPool.class) {
                tmp = clientInstance;
                if (tmp == null) {
                    tmp = new HttpClientPool();
                    clientInstance = tmp;
                }
            }
        }
        return tmp;
    }

    private HttpClientPool() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(100, TimeUnit.SECONDS);
        connectionManager.setMaxTotal(200);// 连接池
        connectionManager.setDefaultMaxPerRoute(100);// 每条通道的并发连接数

        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(2000).setSocketTimeout(2000).build();
        httpClient = HttpClients.custom().setConnectionManager(connectionManager).setDefaultRequestConfig(requestConfig).build();
    }

    public String get(String url) throws Exception {
        HttpGet httpGet = new HttpGet(url);
        return getResponseContent(url,httpGet);
    }

    public String post(String url) throws Exception {
        HttpPost httpPost = new HttpPost(url);
        return getResponseContent(url, httpPost);
    }

    private String getResponseContent(String url, HttpRequestBase request) throws Exception {
        HttpResponse response = null;
        try {
            response = httpClient.execute(request);
            return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new Exception("got an error from HTTP for url : " + URLDecoder.decode(url, "UTF-8"),e);
        } finally {
            if(response != null){
                EntityUtils.consumeQuietly(response.getEntity());
            }
            request.releaseConnection();
        }
    }
}