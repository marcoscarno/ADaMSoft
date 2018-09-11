/**
* Copyright (c) 2015 MS
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

package ADaMSoft.dataaccess;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.ObjectInputStream;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.Vector;

import ADaMSoft.keywords.Keywords;

/**
* This class reads the values contained into the compressed adams data table
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
public class Read_adamsoftnoc extends DataTableReader
{
	ObjectInputStream dis;
	BufferedInputStream bis;
	/**
	*This is the vector of the information on each variable that will be read
	*/
	Vector<Hashtable<String, String>> fixedvariableinfo;
	/**
	*This is the vector of the information, specific for each data type, on each variable that will be read
	*/
	Vector<Hashtable<String, String>> tablevariableinfo;
	/**
	*If true, means that the last observation was reached
	*/
	boolean checklast=false;
	/**
	*This is the message that will be returned in case of error
	*/
	String message="";
	/**
	*Contains the number of the columns of the requested variables
	*/
	int[] rifvar;
	String tablepath;
	Object[] tempvalues;
	Object[] temprec;
	/**
	*Opens the delimited text file and return false in case of errors
	*/
	public boolean open (Hashtable<String, String> tableinfo, Vector<Hashtable<String, String>> fixedvariableinfo,
	Vector<Hashtable<String, String>> tablevariableinfo)
	{
		tablepath="";
		try
		{
			String filename=tableinfo.get(Keywords.DATA.toLowerCase());
			tablepath=filename;
			java.net.URL fileUrl;
			filename=toAdamsFormat(filename);
			if((filename.toLowerCase()).startsWith("http"))
				fileUrl =  new java.net.URL(filename);
			else
			{
				File fileadams=new File(filename);
				fileUrl = fileadams.toURI().toURL();
			}
			URLConnection      urlConn;
			urlConn = fileUrl.openConnection();
			urlConn.setDoInput(true);
			urlConn.setUseCaches(false);
			bis = new BufferedInputStream(urlConn.getInputStream());
			dis = new ObjectInputStream(bis);
			checklast=false;
			getNextRecord();
			if (checklast)
			{
				message="%2810%<br>\n";
				if (dis!=null)
				{
					try
					{
						dis.close();
					}
					catch (Exception ee) {}
				}
				if (bis!=null)
				{
					try
					{
						bis.close();
					}
					catch (Exception ee) {}
				}
				return false;
			}
			return true;
		}
		catch (Exception e)
		{
			if (dis!=null)
			{
				try
				{
					dis.close();
				}
				catch (Exception ee) {}
			}
			if (bis!=null)
			{
				try
				{
					bis.close();
				}
				catch (Exception ee) {}
			}
			message="%2810% ("+e.toString()+")<br>\n";
			checklast=true;
			return false;
		}
	}
	/**
	*Delete the data table
	*/
	public boolean deletetable()
	{
		try
		{
			dis.close();
			bis.close();
			toAdamsFormat(tablepath);
			if((tablepath.toLowerCase()).startsWith("http"))
				return false;
			else
			{
				(new File(tablepath)).delete();
				return true;
			}
		}
		catch (Exception e)
		{
			return false;
		}
	}
	/**
	*Returns the current record
	*/
	public String[] getRecord()
	{
		String[] actualrecord=new String[tempvalues.length];
		for (int i=0; i<tempvalues.length; i++)
		{
			actualrecord[i]=(tempvalues[i].toString()).trim();
		}
		getNextRecord();
		return actualrecord;
	}
	/**
	*Read the next record
	*/
	public void getNextRecord()
	{
		try
		{
			tempvalues=(String[])dis.readObject();
		}
		catch (EOFException ex)
		{
			checklast=true;
		}
		catch (Exception e)
		{
			checklast=true;
		}
	}
	/**
	*Returns true if the last observations was reached
	*/
	public boolean isLast()
	{
		return checklast;
	}
	/**
	*Close the file and returns false in case of error
	*/
	public boolean close()
	{
		try
		{
			dis.close();
			bis.close();
			return true;
		}
		catch (Exception e)
		{
			message="%2811%<br>\n";
			return false;
		}
	}
	/**
	*Returns an int with the number of records contained in the delimited file
	*/
	public int getRecords(Hashtable<String, String> tableinfo)
	{
		int totrecords=0;
		BufferedInputStream bisr = null;
		ObjectInputStream disr = null;
		try
		{
			String filenamer=tableinfo.get(Keywords.DATA.toLowerCase());
			java.net.URL fileUrlr;
			toAdamsFormat(filenamer);
			if((filenamer.toLowerCase()).startsWith("http"))
				fileUrlr =  new java.net.URL(filenamer);
			else
			{
				File filer=new File(filenamer);
				fileUrlr = filer.toURI().toURL();
			}
			URLConnection      urlConnr;
			urlConnr = fileUrlr.openConnection();
			urlConnr.setDoInput(true);
			urlConnr.setUseCaches(false);
			bisr = new BufferedInputStream(urlConnr.getInputStream());
			disr = new ObjectInputStream(bisr);
			temprec=null;
			while (disr!=null)
			{
				temprec=(Object[])disr.readObject();
				totrecords++;
			}
			bisr.close();
		}
		catch (EOFException ex)
		{
			if (disr!=null)
			{
				try
				{
					disr.close();
				}
				catch (Exception ee) {}
			}
			if (bisr!=null)
			{
				try
				{
					bisr.close();
				}
				catch (Exception ee) {}
			}
		}
		catch (Exception e)
		{
			if (disr!=null)
			{
				try
				{
					disr.close();
				}
				catch (Exception ee) {}
			}
			if (bisr!=null)
			{
				try
				{
					bisr.close();
				}
				catch (Exception ee) {}
			}
			message="%2812% ("+e.toString()+")<br>\n";
			return 0;
		}
		return totrecords;
	}
	/**
	*Returns the error message
	*/
	public String getMessage()
	{
		return message;
	}
}
