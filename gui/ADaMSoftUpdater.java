/**
* Copyright (c) 2016 MS
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

package ADaMSoft.gui;

import java.awt.BorderLayout;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.net.*;
import java.net.*;
import java.io.InputStreamReader;

import java.security.Security.*;

import javax.swing.ImageIcon;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import ADaMSoft.keywords.Keywords;

/**
* This class is used to download the ADaMSoft update
* @author marco.scarno@gmail.com
* @date 19/02/2015
*/
public class ADaMSoftUpdater implements Runnable
{
	private int updatesize;
	private URLConnection urlConn;
	private volatile Thread proc=null;
	JProgressBar progressBar;
	JInternalFrame progressdown;
	String localfile;
	String server;
	public ADaMSoftUpdater()
	{
		try
		{
			server=System.getProperty("server_update");
			if (!server.startsWith("http://")) server="http://"+server;

			URL urladamsoft = new URL(System.getProperty("server_update"));
			BufferedReader buffReaderServer = new BufferedReader(new InputStreamReader(urladamsoft.openStream()));
			String tempserver= buffReaderServer.readLine();
			buffReaderServer.close();
			server=tempserver;
			String javaversion=System.getProperty("java.version").toString();
			if (javaversion.startsWith("1.9")) server=server+"UpdateADaMSoft_JRE9.jar";
			if (javaversion.startsWith("1.8")) server=server+"UpdateADaMSoft_JRE8.jar";
			if (javaversion.startsWith("1.7")) server=server+"UpdateADaMSoft_JRE7.jar";
			if (javaversion.startsWith("1.6")) server=server+"UpdateADaMSoft_JRE6.jar";
			if (javaversion.startsWith("9")) server=server+"UpdateADaMSoft_JRE9.jar";
			if (javaversion.startsWith("8")) server=server+"UpdateADaMSoft_JRE8.jar";
			if (javaversion.startsWith("7")) server=server+"UpdateADaMSoft_JRE7.jar";
			if (javaversion.startsWith("6")) server=server+"UpdateADaMSoft_JRE6.jar";
			server=server+"?use_mirror=autoselect";

			localfile=System.getProperty("main_directory")+"UpdateADaMSoft.jar";
			progressdown = new JInternalFrame(Keywords.Language.getMessage(1873), false, false, false,true);
			java.net.URL urlprogress = MainGUI.class.getResource(Keywords.simpleicon);
			ImageIcon iconImageprogress = new ImageIcon(urlprogress);
			progressdown.setFrameIcon(iconImageprogress);
			progressBar = new JProgressBar();
			progressBar.setStringPainted(true);
			JPanel panelprogress = new JPanel();
			panelprogress.add(progressBar);
			MainGUI.desktop.add(progressdown);
			proc=new Thread(this);
			urladamsoft = new URL(server);
			HttpURLConnection httpurlConn = (HttpURLConnection)urladamsoft.openConnection();
			httpurlConn.setDoInput(true);
			httpurlConn.setUseCaches(false);
			httpurlConn.setInstanceFollowRedirects(true);
			httpurlConn.setFollowRedirects(true);
			int status = httpurlConn.getResponseCode();
			boolean redirect=false;
			if (status != HttpURLConnection.HTTP_OK)
			{
				if (status == HttpURLConnection.HTTP_MOVED_TEMP
					|| status == HttpURLConnection.HTTP_MOVED_PERM
					|| status == HttpURLConnection.HTTP_SEE_OTHER)
				redirect = true;
			}
			String newUrl = httpurlConn.getHeaderField("Location");
			httpurlConn.disconnect();
			urlConn = new URL(newUrl).openConnection();
			try
			{
				Thread.sleep(10000);
			}
			catch (Exception e){}
			urlConn.setDoInput(true);
			urlConn.setUseCaches(false);
			updatesize=urlConn.getContentLength()+1;
			progressBar.setMaximum(updatesize);
			progressBar.setValue(0);
			progressBar.setStringPainted(true);
			progressdown.getContentPane().add(panelprogress,BorderLayout.CENTER);
			progressdown.pack();
			progressdown.setVisible(true);
		}
		catch (Exception e)
		{
			String text=Keywords.Language.getMessage(1712) + "<br>\n"+e.toString()+"<br>\n";
			try
			{
				Keywords.semwritelog.acquire();
				File filelog = new File(System.getProperty("out_logfile"));
		        BufferedWriter logwriter = new BufferedWriter(new FileWriter(filelog,true));
		        logwriter.write(text);
		        logwriter.close();
				Keywords.semwritelog.release();
			}
			catch (Exception eee) {}
			progressdown.dispose();
			return;
		}
		proc.start();
	}
	public void run()
	{
		DownloadUpdate();
	}
	public void DownloadUpdate()
	{
		try
		{
			DataInputStream indata = new DataInputStream(urlConn.getInputStream());
			FileOutputStream outdata=new FileOutputStream(localfile);
			int accessed=0;
			int total=1;
			byte[] buffer=new byte[1024];
			while((accessed=indata.read(buffer))!=-1)
			{
				total+=accessed;
				progressBar.setValue(total);
				outdata.write(buffer,0 , accessed);
			}
			indata.close();
			outdata.close();
			progressdown.dispose();
			if (updatesize!=total)
			{
				(new File(localfile)).delete();
				try
				{
					String text="<br><b><i><font color=red>"+Keywords.Language.getMessage(2141) +" ("+ server + ")</font></i></b><br><br>\n";
					Keywords.semwritelog.acquire();
					File filelog = new File(System.getProperty("out_logfile"));
			        BufferedWriter logwriter = new BufferedWriter(new FileWriter(filelog,true));
			        logwriter.write(text);
			        logwriter.close();
					Keywords.semwritelog.release();
				}
				catch (Exception eee) {}
			}
			else
			{
				String text="<br><b><i><font color=blue>"+Keywords.Language.getMessage(1715) +" "+ server + "<br>\n"+Keywords.Language.getMessage(1713) + "</font></i></b><br><br>\n";
				try
				{
					Keywords.semwritelog.acquire();
					File filelog = new File(System.getProperty("out_logfile"));
			        BufferedWriter logwriter = new BufferedWriter(new FileWriter(filelog,true));
			        logwriter.write(text);
			        logwriter.close();
					Keywords.semwritelog.release();
				}
				catch (Exception eee) {}
			}
			MainGUI.updatetoinstall=localfile;
		}
		catch (Exception e)
		{
			(new File(localfile)).delete();
			String text="<br><b><i><font color=red>"+Keywords.Language.getMessage(1712) + "<br>\n"+e.toString()+"</font></i></b><br>\n<br><br>\n";
			try
			{
				Keywords.semwritelog.acquire();
				File filelog = new File(System.getProperty("out_logfile"));
		        BufferedWriter logwriter = new BufferedWriter(new FileWriter(filelog,true));
		        logwriter.write(text);
		        logwriter.close();
				Keywords.semwritelog.release();
			}
			catch (Exception eee) {}
		}
		progressdown.dispose();
	}
}
