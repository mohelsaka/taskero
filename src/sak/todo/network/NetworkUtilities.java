package sak.todo.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.text.TextUtils;
import android.util.Log;

public class NetworkUtilities {

	private static final String TAG = "Network";

	private static final String SERVER = "http://10.0.2.2:3000/login";
	private static final String SYNC_URL = SERVER + "/sync";
	private static final String AUTH_URL = SERVER + "/auth";

	private static final String PARAM_USERNAME = "name";
	private static final String PARAM_EMAIL = "email";
	private static final String PARAM_API_KEY = "api_token";
	private static final String PARAM_DIRTY_DATA = "dirty";

	private static final int HTTP_REQUEST_TIMEOUT_MS = 30 * 1000;

	
    /** POST parameter name for the user's password */
    public static final String PARAM_PASSWORD = "password";
    
    /**
     * Connects to the SampleSync test server, authenticates the provided
     * username, password and email
     *
     * @param username The server account username
     * @param password The server account password
     * @param email the server account email
     * @return String The authentication token returned by the server (or null)
     */
	public static String authenticate(String username, String password, String emial) {
		final HttpResponse resp;
		final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(PARAM_USERNAME, username));
		params.add(new BasicNameValuePair(PARAM_PASSWORD, password));
		params.add(new BasicNameValuePair(PARAM_EMAIL, emial));
		final HttpEntity entity;
		try {
			entity = new UrlEncodedFormEntity(params);
		} catch (final UnsupportedEncodingException e) {
			// this should never happen.
			throw new IllegalStateException(e);
		}
		Log.i(TAG, "Authenticating to: " + AUTH_URL);
		final HttpPost post = new HttpPost(AUTH_URL);
		post.addHeader(entity.getContentType());
		post.setEntity(entity);
		try {
			resp = getHttpClient().execute(post);
			String authToken = null;
			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				InputStream istream = (resp.getEntity() != null) ? resp
						.getEntity().getContent() : null;
				if (istream != null) {
					BufferedReader ireader = new BufferedReader(
							new InputStreamReader(istream));
					authToken = ireader.readLine().trim();
				}
			}
			if (!TextUtils.isEmpty(authToken)) {
				Log.v(TAG, "Successful authentication");
				return authToken;
			} else {
				Log.e(TAG, "Error authenticating" + resp.getStatusLine());
				return null;
			}
		} catch (final IOException e) {
			Log.e(TAG, "IOException when getting authtoken", e);
			return null;
		} finally {
			Log.v(TAG, "getAuthtoken completing");
		}
	}

	public static JSONObject sendSyncData(JSONObject dirtyData,
			Account account, String api_key) throws AuthenticationException,
			IOException, JSONException {

		// Prepare our POST data
		final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(PARAM_USERNAME, account.name));
		params.add(new BasicNameValuePair(PARAM_API_KEY, api_key));
		params.add(new BasicNameValuePair(PARAM_DIRTY_DATA, dirtyData
				.toString()));

		Log.i(TAG, params.toString());
		HttpEntity entity = new UrlEncodedFormEntity(params);

		// Send the dirty data to the server
		final HttpPost post = new HttpPost(SYNC_URL);
		post.addHeader(entity.getContentType());
		post.setEntity(entity);

		// sending the request to the server
		final HttpResponse resp = getHttpClient().execute(post);

		// handling the response
		final String response = EntityUtils.toString(resp.getEntity());
		if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			// Our request to the server was successful
			final JSONObject serverUpdates = new JSONObject(response);
			Log.d(TAG, response);
			return serverUpdates;
		} else {
			if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) { // 401
				Log.e(TAG, "Authentication exception in sending dirty data");
				throw new AuthenticationException();
			} else {
				Log.e(TAG,
						"Server error in sending dirty data: "
								+ resp.getStatusLine());
				throw new IOException();
			}
		}
	}

	/**
	 * Configures the httpClient to connect to the URL provided.
	 */
	public static HttpClient getHttpClient() {
		HttpClient httpClient = new DefaultHttpClient();
		final HttpParams params = httpClient.getParams();
		HttpConnectionParams.setConnectionTimeout(params,
				HTTP_REQUEST_TIMEOUT_MS);
		HttpConnectionParams.setSoTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
		ConnManagerParams.setTimeout(params, HTTP_REQUEST_TIMEOUT_MS);
		return httpClient;
	}
}
