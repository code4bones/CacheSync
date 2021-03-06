package com.code4bones.utils;

import java.io.IOException;
import java.io.StringReader;
import java.net.SocketTimeoutException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.util.Log;

public class XMLParser {

	// constructor
	public XMLParser() {

	}

	/**
	 * Getting XML from URL making HTTP request
	 * @param url string
	 * @throws Exception 
	 */
	
	public String getXmlFromUrl(String url) throws Exception {
		String xml = null;
		try {
	  	    HttpGet request = new HttpGet(url);
			
	  	    // �������� ��������� ����� ����� ��������������� ��
	  	    // ������ �������, � �� ��������
	  	    //  - HttpParams httpParameters = new BasicHttpParams();
		    HttpParams httpParameters = request.getParams();
	  	  
	  	    // TODO: move timouts to something global
		    int timeoutConnection = 5000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			int timeoutSocket = 10000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
			// ������������� ���������
			request.setParams(httpParameters);
		    HttpClient httpClient = new DefaultHttpClient();
		    HttpResponse response = httpClient.execute(request);
		    NetLog.v("HTTP Request executed\n");
		   // NetLog.v("HTTP Request params = params %s\n",request.getParams().toString());
		   // NetLog.v("HTTP Client    params = params %s\n",httpClient.getParams().toString());
		    HttpEntity entity = response.getEntity();
		    xml = EntityUtils.toString(entity);
		    //NetLog.v("*** XML = %s\n",xml);
		} catch ( SocketTimeoutException e ) {
			throw e;
		} catch ( Exception e ) {
			throw e;
		}
		// return XML
		return xml;
	}
	
	/**
	 * Getting XML DOM element
	 * @param XML string
	 * */
	public Document getDomElement(String xml){
		Document doc = null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {

			DocumentBuilder db = dbf.newDocumentBuilder();

			InputSource is = new InputSource();
		        is.setCharacterStream(new StringReader(xml));
		        doc = db.parse(is); 

			} catch (ParserConfigurationException e) {
				Log.e("Error: ", e.getMessage());
				return null;
			} catch (SAXException e) {
				Log.e("Error: ", e.getMessage());
	            return null;
			} catch (IOException e) {
				Log.e("Error: ", e.getMessage());
				return null;
			}

	        return doc;
	}
	
	/** Getting node value
	  * @param elem element
	  */
	 public final String getElementValue( Node elem ) {
	     Node child;
	     if( elem != null){
	         if (elem.hasChildNodes()){
	             for( child = elem.getFirstChild(); child != null; child = child.getNextSibling() ){
	                 if( child.getNodeType() == Node.TEXT_NODE  ){
	                     return child.getNodeValue();
	                 }
	             }
	         }
	     }
	     return "";
	 }
	 
	 /**
	  * Getting node value
	  * @param Element node
	  * @param key string
	  * */
	 public String getValue(Element item, String str) {		
			NodeList n = item.getElementsByTagName(str);		
			return this.getElementValue(n.item(0));
		}
}
