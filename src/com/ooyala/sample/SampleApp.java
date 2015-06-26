package com.ooyala.sample;

import com.ooyala.api.OoyalaAPI;
import com.ooyala.api.HttpStatusCodeException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;

public class SampleApp {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	/**
	 * 
	 * @param arg[0] = Api Key
	 * @param arg[1] = Api Secret
	 * @param arg[2] = File Name
	 * @param arg[3] = Action (upload|replace) // Optional - Default upload
	 * @param arg[4] = Embed Code // Optional - 
	 * @throws ClientProtocolException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws HttpStatusCodeException
	 */
	public static void main(String arg[]) { //throws ClientProtocolException, NoSuchAlgorithmException, IOException, HttpStatusCodeException {
		Log log = new Log();
		SampleApp fileUpload = new SampleApp();
        File file = new File (arg[2]); //Video file location
		OoyalaAPI api = new OoyalaAPI(arg[0], arg[1]);	// Create a new OoyalaAPI object using API KEY and API SECRET

		try {
			// Creation Parameters
			HashMap<String, Object> cParams = new HashMap<String, Object>();
			String name = java.util.UUID.randomUUID().toString();
			cParams.put("name", name);
			cParams.put("file_name", file.getName());
			cParams.put("asset_type", "video");
			cParams.put("file_size", file.length());
			
			log.d("Creation Parameters: " + cParams.toString());
			
			// Execute and receive embed_code
			LinkedHashMap<String, LinkedList<HashMap<String, String>>> createResponse;
			createResponse = (LinkedHashMap<String, LinkedList<HashMap<String, String>>>) api.postRequest("assets", cParams);
			log.d(createResponse.toString());
			
			String embed_code = "";
			Set entrySet = createResponse.entrySet();
			Iterator it = entrySet.iterator();
			while (it.hasNext()) {
				Entry map = (Entry) it.next();
				if(map.getKey().equals("embed_code"))
					embed_code = map.getValue().toString();
			}
			
			log.d("Embed Code: " + embed_code);
			// Get Upload URLS
			LinkedList<HashMap<String, String>> uploadingUrls;
			uploadingUrls = (LinkedList<HashMap<String, String>>) api.getRequest("assets/" + embed_code + "/uploading_urls");
			
			Iterator it2 = uploadingUrls.iterator();
			log.i("Uploading");
			while(it2.hasNext()) {
				String uploadUrl = (String) it2.next();
				log.d("Upload URL: " + uploadUrl);
				String response = fileUpload.executeUploadRequest(uploadUrl, file);
				log.i(response);
				if(response.isEmpty()) {
					HashMap<String, Object> uParams = new HashMap<String, Object>();
					uParams.put("status", "uploaded");
					LinkedHashMap<String, LinkedList<HashMap<String, String>>> updateStatus;
					updateStatus = (LinkedHashMap<String, LinkedList<HashMap<String, String>>>) api.putRequest("assets/" + embed_code + "/upload_status", uParams);
					log.d("Final Response: " + updateStatus.toString());
				}
			}
			
	        //
			
		} catch (NoSuchAlgorithmException | IOException | HttpStatusCodeException e) {
			// TODO Auto-generated catch block
			log.e(e.getMessage());
		}
	}

	/**
	 * Method that will post the file/part of file
	 * 
	 * @param urlString = the urlString to which the file needs to be uploaded
	 * @param file = the actual file instance that needs to be uploaded
	 * @return server response as <code>String</code>
	 */
	public String executeUploadRequest(String urlString, File file) {

		String responseString = "";
		HttpPost requestBase = new HttpPost(urlString);
		MultipartEntity multiPartEntity = new MultipartEntity();
		FileBody fileBody = new FileBody(file, "application/octect-stream");
		multiPartEntity.addPart("attachment", fileBody);
		requestBase.setEntity(multiPartEntity);

		InputStream responseStream = null;
		HttpClient client = new DefaultHttpClient();
		try {
			HttpResponse response = client.execute(requestBase);
			if (response != null) {
				HttpEntity responseEntity = response.getEntity();
				if (responseEntity != null) {
					responseStream = responseEntity.getContent();
					if (responseStream != null) {
						BufferedReader br = new BufferedReader(new InputStreamReader(responseStream));
						String responseLine = br.readLine();
						String tempResponseString = "";
						while (responseLine != null) {
							tempResponseString = tempResponseString + responseLine + System.getProperty("line.separator");
							responseLine = br.readLine();
						}
						br.close();
						if (tempResponseString.length() > 0) {
							responseString = tempResponseString;
						}
					}
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (responseStream != null) {
				try { responseStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		client.getConnectionManager().shutdown();
		return responseString;
	}
}