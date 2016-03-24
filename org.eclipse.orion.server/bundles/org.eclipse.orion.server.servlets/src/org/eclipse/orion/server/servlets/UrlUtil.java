package org.eclipse.orion.server.servlets;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
/**
 * $Id: $
 * Utility for archive a file with password protection .
 * @author bharathi-1397
 */
public class UrlUtil {

    /**
     * Field LOGGER.
     */
    public static final Logger LOGGER = Logger.getLogger(UrlUtil.class.getName());
    
    static Map<String,String> defaultHeader = new HashMap<String,String>(){
    	{
    		put("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
    	}
    };
    
    /**
     * Field CONNECTION_TIMEOUT - Time limit for retrying connection.
     * (value is 5000)
     */
    public static final int CONNECTION_TIMEOUT = 5000;
    /**
     * Field READ_TIMEOUT. - Data readout time .
     * (value is 60000)
     */
    public static final int READ_TIMEOUT = 60000;
    /**
     * Field BUFFER_SIZE - Used for reading response content as blocks.
     * (value is 1024)
     */
    public static final int BUFFER_SIZE = 1024;

    public static final String ZOHO_DOMAIN = ".csez.zohocorpin.com";

    /**
     * Used for ignoring duplicate certificate error when invoking https url connections .
     */
    private static TrustManager[] trustAllCerts = new TrustManager[] { // trust all certificates if it's expired or duplicate .


    new X509TrustManager() {

	public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	    return null;
	}

	public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
	}

	public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
	}
    } };

    /**
     * Ignoring host name verifying check for when making url connection .
     */
    static HostnameVerifier verifier = new HostnameVerifier() {

	public boolean verify(String hostname, SSLSession session) {
	    // TODO Auto-generated method stub
	    return true;
	}
    };

    static {
	try {
	    SSLContext sslContext = SSLContext.getInstance("SSL");
	    sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
	    HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
	    HttpsURLConnection.setDefaultHostnameVerifier(verifier);
	} catch (Exception e) {
	    LOGGER.log(Level.INFO, "No Such protocol ssl : exception trace {0}", e);
	}
    }

    public static String appendDomain(String host){
    	String hostWithDomain = "";
    	if(host.endsWith(ZOHO_DOMAIN)){
    			hostWithDomain = host;
    	}else{
    		hostWithDomain = host+ZOHO_DOMAIN;
    	}
    	return hostWithDomain;
    }

    public static int getResponseStatus(String url, String data, Map<String, String> headers, String method) throws IOException {
    	HttpURLConnection con = getConnection(url, data, headers, method);
    	int response = -1;
    	if(con != null) {
    		response = con.getResponseCode();
    	}
    	return response;
    }

    public static String getUrlResponse(String url, String data, Map<String, String> headers, String method) throws IOException {
    	HttpURLConnection con = getConnection(url, data, headers, method);
    	InputStream is = null;
    	String response = null;
    	try {
    		if(con != null) {
        		is = con.getInputStream();
        		response = new String(read(is));
        	}
    	}
    	finally {
    		closeStream(is);
    	}
    	return response;
    }

    public static HttpURLConnection getConnection(String url, String data, Map<String, String> headers, String method) throws IOException {
    	OutputStream os = null;
    	HttpURLConnection urlConnection = null;
    	try {
    	    urlConnection = (HttpURLConnection) new URL(url).openConnection();
    	    urlConnection.setDoOutput(true);
    	    //urlConnection.setFollowRedirects(true);
    	    //urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    	    urlConnection.setRequestProperty("Content-Length", "");
    	    urlConnection.setDoInput(true);
    	    urlConnection.setRequestMethod(method);
    	    urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
    	    urlConnection.setReadTimeout(5000);
    	    if(headers != null) {
    		Iterator<String> iterator = headers.keySet().iterator();
    		while(iterator.hasNext()) {
    		    String key = iterator.next();
    		    urlConnection.setRequestProperty(key, headers.get(key));
    		}
    	    }
    	    os = urlConnection.getOutputStream();
    	    if(data != null) {
    		os.write(data.getBytes());
    	    }
    	    os.flush();
    	    os.close();
    	    urlConnection.connect();

    	} catch (SocketTimeoutException timeout) {
    	    LOGGER.log(Level.FINE, "Connection timed out . It may be due to request url is not reachable");
    	    throw new IOException(timeout);
    	} catch (IOException io) {
    	    LOGGER.log(Level.WARNING, "Error occured while making connection , url {0} , reason {1}", new Object[] { url, io });
    	    throw new IOException(io);
    	} finally {
    	    if(os != null) {
    		os.close();
    	    }
    	}
    	return urlConnection;
    }

    /**
     *
     * @param url
     *            - url to make connection . It must be http or https .
     * @param datat
     *            - Data to sent .
     * @param method
     *            - <b>GET</b> , <b>POST</b> , <b>HEAD</b> , <b>DELETE</b>
     * @param headers Map<String,String>
     * @return - outputstream as byte[] * @throws IOException
     * @throws Exception */
    public static byte[] sendRequest(String url, String data, Map<String, String> headers, String method) throws IOException {
	OutputStream os = null;
	try {
	    HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
	    urlConnection.setDoOutput(true);
	    urlConnection.setDoInput(true);
	    urlConnection.setRequestMethod(method);
	    urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
	    urlConnection.setReadTimeout(READ_TIMEOUT);
	    if(headers != null) {
		Iterator<String> iterator = headers.keySet().iterator();
		while(iterator.hasNext()) {
		    String key = iterator.next();
		    urlConnection.setRequestProperty(key, headers.get(key));
		}
	    }
	    os = urlConnection.getOutputStream();
	    if(data != null) {
		os.write(data.getBytes());
	    }
	    os.flush();
	    os.close();
	    urlConnection.connect();
	    return read(urlConnection.getInputStream());

	} catch (SocketTimeoutException timeout) {
	    LOGGER.log(Level.FINE, "Connection timed out . It may be due to request url is not reachable");
	    throw new IOException(timeout);
	} catch (IOException io) {
	    LOGGER.log(Level.WARNING, "Error occured while making connection , url {0} , reason {1}", new Object[] { url, io });
	    throw new IOException(io);
	} finally {
	    if(os != null) {
		os.close();
	    }
	}
    }


    public static String toExternalizeForm(URL url){

    	//precomputing the string buffer size.
    	if("file".equals(url.getProtocol())){
    		return url.toString(); //do nothing if it's file protocol.
    	}

		int len = url.getProtocol().length() + 1;
		if (url.getAuthority() != null && url.getAuthority().length() > 0)
		    len += 2 + url.getAuthority().length();
		if (url.getPath() != null) {
		    len += url.getPath().length();
		}
		if (url.getQuery() != null) {
		    len += 1 + url.getQuery().length();
		}
		if (url.getRef() != null)
		    len += 1 + url.getRef().length();

		StringBuffer result = new StringBuffer(len+UrlUtil.ZOHO_DOMAIN.length());
		result.append(url.getProtocol());
	        result.append(":");
	        if (url.getAuthority() != null && url.getAuthority().length() > 0) {
	            result.append("//");
	            result.append(appendDomain(url.getHost()));
	        }
	        if (url.getPath() != null) {
	            result.append(url.getPath());
	        }
	        if (url.getQuery() != null) {
	            result.append('?');
	            result.append(url.getQuery());
	        }
		if (url.getRef() != null) {
		    result.append("#");
	            result.append(url.getRef());
		}
		return result.toString();
    }


    /**
     * Method read.
     * @param in InputStream
     * @return byte[]
     * @throws IOException
     */
    public static byte[] read(InputStream in) throws IOException {
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	byte[] buffer = new byte[BUFFER_SIZE];
	int read = -1;
	while((read = in.read(buffer)) > 0) {
	    out.write(buffer, 0, read);
	}
	return out.toByteArray();
    }

    /**
     * Returns ticket for the user .
     * @param url - Https only iam server url .
     * @return - IAM Agent ticket. * @throws IOException */
    public static String getTicket(String url) throws IOException {
	Map<String, String> header = new HashMap<String, String>() {
	    {
		put("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
	    }
	};
	String ticket = new String(sendRequest(url, null, header, "POST"));
	int index = ticket.indexOf("TICKET=");
	ticket = ticket.substring(index + 7, index + 39);
	return ticket;
    }


    /**
     * Method clearTicket.
     *  Clear's the given ticket IAM Server .
     * @param ticket String
     * @param iamServerUrl String
     * @throws IOException
     */
    private static void clearTicket(String ticket, String iamServerUrl) throws IOException {
	Map<String, String> header = new HashMap<String, String>() {
	    {
		put("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
	    }
	};
	sendRequest(iamServerUrl + "/logout?ticket=" + ticket, null, header, "POST");
    }

    /**
     * Used for adding a new service after service deployment via zide .
     * @param serviceName - Name of the service to add in IAM Server .
     * @param iamServerUrl - IAM Server url . IAM Server should be run on your local machine .
     * @param homePage - Home page after login .
     * @param serviceUrl - Service url .
     * @throws IOException * @throws NoSuchAlgorithmException */
    public static void addService(String serviceName, String iamServerUrl, String homePage, String serviceUrl) throws IOException,
	    NoSuchAlgorithmException {
	Map<String, String> header = new HashMap<String, String>() {
	    {
		put("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
	    }
	};

	String username = "admin";
	String password = "admin";

	//ticket used for authentication purpose .
	String ticket = getTicket(iamServerUrl + "/login?LOGIN_ID="+username+"&PASSWORD="+password+"&FROM_AGENT=true&servicename=AaaServer");
	header.clear();
	header.put("Content-Type", "application/json, application/json;charset=UTF-8");
	//used for generating unique app ids.
	SecureRandom prng = SecureRandom.getInstance("SHA1PRNG");
	String appId = Integer.toString(prng.nextInt(1000));
	String json = "{\"app_id\":\"" + appId + "\",\"app_name\":\"" + serviceName + "\",\"default_sub_domain\":\"" + serviceUrl
		+ "\",\"display_name\":\"" + serviceName + "\",\"home_page\":\"" + homePage + "\"}";
	sendRequest(iamServerUrl + "/accounts/resource/accounts/App?ticket=" + ticket, json, header, "POST");
	header.clear();
	header.put("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
	//used for clearing cache.
	sendRequest(iamServerUrl + "/internal/clearcache.jsp?ticket=" + ticket, "", header, "POST");
	//clearing generated ticket.
	clearTicket(ticket, iamServerUrl);
    }
    
    
    
    public static byte[] post(String uri , String params) throws IOException{
    	return sendRequest(uri, params,defaultHeader, "POST");
    }
    
    public static byte[] get(String uri , String params , Map<String,String> headers) throws IOException{
    	return sendRequest(uri, params, null, "GET");
    }
    
    private static void closeStream(Closeable obj) {
    	if(obj != null) {
    		try {
				obj.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
    	}
    }
    
}

