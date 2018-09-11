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

package ADaMSoft.dataaccess;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import java.util.LinkedList;

import ADaMSoft.keywords.Keywords;

/**
* This class reads the values contained into the compressed adams data table
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class Read_adamsoft extends DataTableReader
{
	/**
	*This is the object input stream that represents the values
	*/
	ObjectInputStream ois;
	BufferedInputStream bis;
	/**
	*This is the compressed input stream
	*/
	ZipInputStream indata;
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
	*Contains the number of variables that are in the data table
	*/
	int numberofvar;
	/**
	*Contains the number of the columns of the requested variables
	*/
	int[] rifvar;

	String tablepath;
	Object[] tempvalues;

	LinkedList<RecordAccessed> rqueue = new LinkedList<RecordAccessed>();

	int release;
	private int BLOCKSIZE=1048576;
	/**
	*Opens the delimited text file and return false in case of errors
	*/
	public boolean open (Hashtable<String, String> tableinfo, Vector<Hashtable<String, String>> fixedvariableinfo,
	Vector<Hashtable<String, String>> tablevariableinfo)
	{
		try
		{
			BLOCKSIZE=Integer.parseInt(System.getProperty(Keywords.FileBufferDim));
		}
		catch (Exception exbuf) {}
		release=1;
		tablepath="";
		try
		{
			String filename=tableinfo.get(Keywords.DATA.toLowerCase());
			tablepath=filename;
			java.net.URL fileUrl;
			toAdamsFormat(filename);
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
			urlConn.setUseCaches(true);
			indata= new ZipInputStream(urlConn.getInputStream());
			ZipEntry entry;
			while ((entry = indata.getNextEntry()) != null)
			{
				String nameFile = entry.getName();
				if (nameFile.equalsIgnoreCase("InfoVar"))
				{
					BufferedReader filebuffered= new BufferedReader(new InputStreamReader(indata));
					String line=filebuffered.readLine();
					numberofvar=Integer.parseInt(line.trim());
				}
				if (nameFile.equalsIgnoreCase("Release"))
				{
					BufferedReader filebuffered= new BufferedReader(new InputStreamReader(indata));
					String temprelease=filebuffered.readLine();
					release=Integer.parseInt(temprelease.trim());
				}
	        }
			indata.close();
			if (release!=2)
			{
				message="%2699%<br>\n";
				checklast=true;
				return false;
			}
			urlConn = fileUrl.openConnection();
			urlConn.setDoInput(true);
			urlConn.setUseCaches(true);
			indata= new ZipInputStream(urlConn.getInputStream());
			while ((entry = indata.getNextEntry()) != null)
			{
				String nameFile = entry.getName();
				if (nameFile.equalsIgnoreCase("Values"))
				{
					bis=new BufferedInputStream(indata, BLOCKSIZE);
					ois= new ObjectInputStream(bis);
					getNextRecord();
					return true;
				}
	        }
		}
		catch (Exception e)
		{
			message="%448%<br>\n";
			checklast=true;
			return false;
		}
		return false;
	}
	/**
	*Delete the data table
	*/
	public boolean deletetable()
	{
		try
		{
			ois.close();
			bis.close();
			indata.close();
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
			tempvalues=(Object[])ois.readObject();
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
			ois.close();
			bis.close();
			indata.close();
			return true;
		}
		catch (Exception e)
		{
			message="%359%<br>\n";
			return false;
		}
	}
	/**
	*Returns an int with the number of records contained in the delimited file
	*/
	public int getRecords(Hashtable<String, String> tableinfo)
	{
		int totrecords=0;
		try
		{
			String filenamer=tableinfo.get(Keywords.DATA.toLowerCase());
			java.net.URL fileUrlr;
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
			ZipInputStream indatar= new ZipInputStream(urlConnr.getInputStream());
			ZipEntry entryr;
			while ((entryr = indatar.getNextEntry()) != null)
			{
				String nameFile = entryr.getName();
				if (nameFile.equalsIgnoreCase("InfoRecord"))
				{
					BufferedReader filebuffered= new BufferedReader(new InputStreamReader(indatar));
					String line=filebuffered.readLine();
					totrecords=Integer.parseInt(line.trim());
				}
	        }
			indatar.close();
		}
		catch (Exception e)
		{
			message="%358%<br>\n";
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
