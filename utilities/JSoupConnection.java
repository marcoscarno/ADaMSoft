/**
* Copyright (c) 2017 MS
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

package ADaMSoft.utilities;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.util.*;
import java.net.*;
import java.io.*;

/**
* This class permits to use JSOUP to connect to a web site
* @author marco.scarno@gmail.com
* @date 16/02/2017
*/
public class JSoupConnection
{
	Hashtable<String, String> cookies;
	String useragent, verifytype, method, doc_loc, url_address;
	boolean followredirects, ignorecontenttype, ignorehttperrors, timeout, testtype;;
	int timeoutmillis, tdoc_size, returncode;
	Connection.Response jsoupconn;
	java.net.URL fileUrl;
	Document docret;
	URL urlr;
	InputStream istest;
	public JSoupConnection()
	{
		timeout=false;
		cookies=new Hashtable<String, String>();
		useragent="";
		followredirects=true;
		ignorecontenttype=false;
		ignorehttperrors=false;
		method="GET";
		timeoutmillis=3000;
		verifytype="";
		tdoc_size=-1;
		doc_loc="";
		docret=null;
	}
	public void setcookies(Hashtable<String, String> cookies)
	{
		this.cookies=cookies;
	}
	public void setuseragent(String useragent)
	{
		if (useragent==null)
			useragent="";
		//if (useragent==null)
		//	useragent="Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET";
		//if (useragent.equals(""))
		//	useragent="Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.4506.2152; .NET";
		this.useragent=useragent;
	}
	public void setbooleans(boolean followredirects, boolean ignorecontenttype, boolean ignorehttperrors)
	{
		this.followredirects=followredirects;
		this.ignorecontenttype=ignorecontenttype;
		this.ignorehttperrors=ignorehttperrors;
	}
	public void setmethod(String method)
	{
		this.method=method;
		if (this.method==null) this.method="GET";
	}
	public void settimeoutmillis (int timeoutmillis)
	{
		this.timeoutmillis=timeoutmillis;
	}
	public Document simpleGetHtmlDocument(String temp_url_address)
	{
		try
		{
			urlr = new URL(URLEncoder.encode(temp_url_address, "UTF-8"));
			url_address=urlr.toString();
		}
		catch (Exception eeu)
		{
			url_address=temp_url_address;
		}
		try
		{
			if (useragent.equals(""))
			{
				if (method.equalsIgnoreCase("post"))
					docret=Jsoup.connect(url_address).cookies(cookies).timeout(timeoutmillis).ignoreContentType(ignorecontenttype).ignoreHttpErrors(ignorehttperrors).followRedirects(followredirects).post();
				else
					docret=Jsoup.connect(url_address).cookies(cookies).timeout(timeoutmillis).ignoreContentType(ignorecontenttype).ignoreHttpErrors(ignorehttperrors).followRedirects(followredirects).get();
			}
			else
			{
				if (method.equalsIgnoreCase("post"))
					docret=Jsoup.connect(url_address).cookies(cookies).timeout(timeoutmillis).ignoreContentType(ignorecontenttype).ignoreHttpErrors(ignorehttperrors).followRedirects(followredirects).userAgent(useragent).post();
				else
					docret=Jsoup.connect(url_address).cookies(cookies).timeout(timeoutmillis).ignoreContentType(ignorecontenttype).ignoreHttpErrors(ignorehttperrors).followRedirects(followredirects).userAgent(useragent).get();
			}
		}
		catch (SocketTimeoutException set)
		{
			timeout=true;
			return null;
		}
		catch (Exception ed)
		{
			return null;
		}
		return docret;
	}
	public Document getHtmlDocument(String temp_url_address)
	{
		try
		{
			urlr = new URL(URLEncoder.encode(temp_url_address, "UTF-8"));
			url_address=urlr.toString();
		}
		catch (Exception eeu)
		{
			url_address=temp_url_address;
		}
		docret=null;
		doc_loc="";
		verifytype="";
		tdoc_size=-1;
		try
		{
			if (useragent.equals(""))
			{
				if (method.equalsIgnoreCase("post"))
					jsoupconn=Jsoup.connect(url_address).cookies(cookies).timeout(timeoutmillis).ignoreContentType(ignorecontenttype).ignoreHttpErrors(ignorehttperrors).followRedirects(followredirects).maxBodySize(0).method(Connection.Method.POST).execute();
				else if (method.equalsIgnoreCase("put"))
					jsoupconn=Jsoup.connect(url_address).cookies(cookies).timeout(timeoutmillis).ignoreContentType(ignorecontenttype).ignoreHttpErrors(ignorehttperrors).followRedirects(followredirects).maxBodySize(0).method(Connection.Method.PUT).execute();
				else
					jsoupconn=Jsoup.connect(url_address).cookies(cookies).timeout(timeoutmillis).ignoreContentType(ignorecontenttype).ignoreHttpErrors(ignorehttperrors).followRedirects(followredirects).maxBodySize(0).method(Connection.Method.GET).execute();
			}
			else
			{
				if (method.equalsIgnoreCase("post"))
					jsoupconn=Jsoup.connect(url_address).cookies(cookies).timeout(timeoutmillis).ignoreContentType(ignorecontenttype).ignoreHttpErrors(ignorehttperrors).followRedirects(followredirects).userAgent(useragent).maxBodySize(0).method(Connection.Method.POST).execute();
				else if (method.equalsIgnoreCase("put"))
					jsoupconn=Jsoup.connect(url_address).cookies(cookies).timeout(timeoutmillis).ignoreContentType(ignorecontenttype).ignoreHttpErrors(ignorehttperrors).followRedirects(followredirects).userAgent(useragent).maxBodySize(0).method(Connection.Method.PUT).execute();
				else
					jsoupconn=Jsoup.connect(url_address).cookies(cookies).timeout(timeoutmillis).ignoreContentType(ignorecontenttype).ignoreHttpErrors(ignorehttperrors).followRedirects(followredirects).userAgent(useragent).maxBodySize(0).method(Connection.Method.GET).execute();
			}
			verifytype=jsoupconn.contentType();
			returncode=jsoupconn.statusCode();
			tdoc_size=(jsoupconn.body()).length();
			if (verifytype!=null)
			{
				if (!verifytype.toLowerCase().startsWith("text/html"))
				{
					return null;
				}
			}
		}
		catch (Exception et)
		{
			return null;
		}
		timeout=false;
		try
		{
			docret=jsoupconn.parse();
			doc_loc=docret.location();
		}
		catch (SocketTimeoutException set)
		{
			timeout=true;
			return null;
		}
		catch (Exception ed)
		{
			return null;
		}
		return docret;
	}
	public boolean istimeout()
	{
		return timeout;
	}
	public int gettimeoutmillis()
	{
		return timeoutmillis;
	}
	public String getDocType()
	{
		return verifytype;
	}
	public int getDocSize()
	{
		return tdoc_size;
	}
	public String getRealLocation()
	{
		return doc_loc;
	}
	public int getReturncode()
	{
		return returncode;
	}
}
