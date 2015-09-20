package com.example.bhatvig.andriodlogin;

import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.RFC2109Spec;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class CustomHttpClient {
    /** The time it takes for our client to timeout */
    public static final int HTTP_TIMEOUT = 30 * 1000; // milliseconds

    /** Single instance of our HttpClient */
    private static HttpClient mHttpClient;

    /**
     * Get our single instance of our HttpClient object
     * @return an HttpClient object with connection parameters set
     */
    private static HttpClient getHttpClient() {
        if (mHttpClient == null) {
            mHttpClient = new DefaultHttpClient();
            final HttpParams params = mHttpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(params, HTTP_TIMEOUT);
            HttpConnectionParams.setSoTimeout(params, HTTP_TIMEOUT);
            ConnManagerParams.setTimeout(params, HTTP_TIMEOUT);
        }
        return mHttpClient;
    }
    /**
     * Performs an HTTP Post request to the specified url with the
     * specified parameters.
     * @param url The web address to post the request to
     * @param postParameters The parameters to send via the request
     * @return The result of the request
     * @throws Exception
     */
    public static String executeHttpPost(String url, ArrayList<NameValuePair> postParameters) throws Exception {
        BufferedReader in = null;
        try {
            HttpClient client = getHttpClient();
            syncCookiesFromAppCookieManager(url, (DefaultHttpClient)client);
            HttpPost request = new HttpPost(url);
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(postParameters);
            request.setEntity(formEntity);
            HttpResponse response = client.execute(request);
            response.getEntity().consumeContent();
      /*      in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            StringBuffer sb = new StringBuffer("");
            String line = "";
            String NL = System.getProperty("line.separator");
            while ((line = in.readLine()) != null) {
                sb.append(line + NL);
            }
            in.close();*/

            String result = String.valueOf(response.getStatusLine().getStatusCode());
            syncCookiesToAppCookieManager(url, (DefaultHttpClient)client);
            return result;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Performs an HTTP GET request to the specified url.
     *
     * @param url The web address to post the request to
     * @return The result of the request
     * @throws Exception
     */
    public static String executeHttpGet(String url) throws Exception {
        BufferedReader in = null;
        try {
            DefaultHttpClient client = new DefaultHttpClient();
            syncCookiesFromAppCookieManager(url, client);
            HttpGet request = new HttpGet();
            request.setURI(new URI(url));
            HttpResponse response = client.execute(request);
            in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

            StringBuffer sb = new StringBuffer("");
            String line = "";
            String NL = System.getProperty("line.separator");
            while ((line = in.readLine()) != null) {
                sb.append(line + NL);
            }
            in.close();

            String result = sb.toString();
            syncCookiesToAppCookieManager(url, client);
            return result;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void syncCookiesFromAppCookieManager(String url, DefaultHttpClient httpClient) {

        BasicCookieStore cookieStore = new BasicCookieStore();
        httpClient.setCookieStore(cookieStore);

        CookieManager cookieManager = CookieManager.getInstance();
        if (cookieManager == null) return;

        RFC2109Spec cookieSpec = new RFC2109Spec();
        String rawCookieHeader = null;
        try {
            URL parsedURL = new URL(url);

            //Extract Set-Cookie header value from Android app CookieManager for this URL
            rawCookieHeader = cookieManager.getCookie(parsedURL.getHost());
            if (rawCookieHeader == null) return;

            //Convert Set-Cookie header value into Cookies w/in HttpClient CookieManager
            int port = parsedURL.getPort() == -1 ?
                    parsedURL.getDefaultPort() : parsedURL.getPort();

            CookieOrigin cookieOrigin = new CookieOrigin( parsedURL.getHost(),
                    port,
                    "/",
                    false);
            List<Cookie> appCookies = cookieSpec.parse(
                    new BasicHeader("set-cookie", rawCookieHeader),
                    cookieOrigin);

            cookieStore.addCookies(appCookies.toArray(new Cookie[appCookies.size()]));
        } catch (MalformedURLException e) {
            // Handle Error
        } catch (MalformedCookieException e) {
            // Handle Error
        }
    }

    public static void syncCookiesToAppCookieManager(String url, DefaultHttpClient httpClient) {

        CookieStore clientCookieStore = httpClient.getCookieStore();
        List<Cookie> cookies  = clientCookieStore.getCookies();
        if (cookies.size() < 1) return;
        CookieSyncManager syncManager = CookieSyncManager.getInstance();
        CookieManager appCookieManager = CookieManager.getInstance();
        if (appCookieManager == null) return;

        //Extract any stored cookies for HttpClient CookieStore
        // Store this cookie header in Android app CookieManager
        for (Cookie cookie:cookies) {
            //HACK: Work around weird version-only cookies from cookie formatter.
            if (cookie.getName() == "$Version") break;

            String setCookieHeader = cookie.getName()+"="+cookie.getValue()+
                    "; Domain="+cookie.getDomain();
            appCookieManager.setCookie(url, setCookieHeader);
            CookieSyncManager.getInstance().sync();
        }

        //Sync CookieManager to disk if we added any cookies
    }
}

