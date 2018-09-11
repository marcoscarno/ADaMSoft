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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Vector;

import ADaMSoft.keywords.Keywords;

/**
* This class reads the values contained into a text file with fixed field
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
public class Read_fixedtxt extends DataTableReader
{
	/**
	*This is the buffer
	*/
	BufferedReader in;
	/**
	*This is the vector of the information, specific for each data type, on each variable that will be read
	*/
	Vector<Hashtable<String, String>> tablevariableinfo;
	/**
	*If true, means that the last observation was reached
	*/
	boolean checklast=false;
	/**
	*This is the string array of the next record
	*/
	String[] waitingrecord;
	/**
	*This is the message that will be returned in case of error
	*/
	String message="";
	/**
	*Contains the number of the columns of the requested variables
	*/
	int[] rifvar;

	String tablepath;

	String thousanddlm;
	String decimaldlm;
	String ttev;
	double testisnum;
	String str;
	/**
	*Opens the delimited text file and return false in case of errors
	*/
	public boolean open (Hashtable<String, String> tableinfo, Vector<Hashtable<String, String>> fixedvariableinfo,
	Vector<Hashtable<String, String>> tablevariableinfo)
	{
		this.tablevariableinfo=tablevariableinfo;
		try
		{
			String filename=tableinfo.get(Keywords.DATA.toLowerCase());
			tablepath=filename;
			java.net.URL fileUrl;
			if((filename.toLowerCase()).startsWith("http"))
				fileUrl =  new java.net.URL(filename);
			else
			{
				File file=new File(filename);
				fileUrl = file.toURI().toURL();
			}
			in = new BufferedReader(new InputStreamReader(fileUrl.openStream()));
		}
		catch (Exception e)
		{
			message="%578%<br>\n"+e.toString()+"<br>\n";
			checklast=true;
			return false;
		}
		try
		{
			if (tableinfo.get(Keywords.codethousanddlm.toLowerCase())!=null)
			{
				thousanddlm=tableinfo.get(Keywords.codethousanddlm.toLowerCase());
				if (!thousanddlm.equals(""))
				{
					int codethousanddlm = Integer.parseInt(thousanddlm);
					thousanddlm = new Character((char)codethousanddlm).toString();
				}
			}
		}
		catch (Exception e)
		{
			message="%2147%<br>\n"+e.toString()+"<br>\n";
			return false;
		}
		try
		{
			if (tableinfo.get(Keywords.codedecimaldlm.toLowerCase())!=null)
			{
				decimaldlm=tableinfo.get(Keywords.codedecimaldlm.toLowerCase());
				if (!decimaldlm.equals(""))
				{
					int codedecimaldlm = Integer.parseInt(decimaldlm);
					decimaldlm = new Character((char)codedecimaldlm).toString();
				}
			}
		}
		catch (Exception e)
		{
			message="%2148%<br>\n"+e.toString()+"<br>\n";
			return false;
		}
		waitingrecord=new String[fixedvariableinfo.size()];
		try
		{
			getNextRecord();
		}
		catch (Exception e)
		{
			message="%357%<br>\n"+e.toString()+"<br>\n";
			checklast=true;
			return false;
		}
		return true;
	}
	/**
	*Delete the data table
	*/
	public boolean deletetable()
	{
		try
		{
			in.close();
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
		String[] actualrecord=new String[waitingrecord.length];
		for (int i=0; i<waitingrecord.length; i++)
		{
			actualrecord[i]=waitingrecord[i];
		}
		getNextRecord();
		return actualrecord;
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
			in.close();
			return true;
		}
		catch (Exception e)
		{
			message="%359%<br>\n"+e.toString()+"<br>\n";
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
			BufferedReader inr = new BufferedReader(new InputStreamReader(fileUrlr.openStream()));
			String str="";
			while ((str=inr.readLine()) != null)
			{
				if (!str.trim().equals(""))
					totrecords++;
			}
			inr.close();
		}
		catch (Exception e)
		{
			message="%358%<br>\n"+e.toString()+"<br>\n";
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
	/**
	*Read the next record
	*/
	public void getNextRecord()
	{
		try
		{
			str = in.readLine();
			if (str==null)
			{
				checklast=true;
			}
			else
			{
				for (int i=0; i<tablevariableinfo.size(); i++)
				{
					Hashtable<String, String> t=tablevariableinfo.get(i);
					int varstart=Integer.parseInt(t.get(Keywords.FixedFileVariableStart.toLowerCase()));
					int varend=Integer.parseInt(t.get(Keywords.FixedFileVariableEnd.toLowerCase()));
					int vardec=0;
					if (t.get(Keywords.NumberOfDecimals.toLowerCase())!=null)
					{
						vardec=Integer.parseInt(t.get(Keywords.NumberOfDecimals.toLowerCase()));
					}
					waitingrecord[i]=(str.substring(varstart-1, varend-1)).trim();
					ttev=waitingrecord[i];
					try
					{
						if (!thousanddlm.equals(""))
							ttev=ttev.replace(thousanddlm,"");
					}
					catch (Exception e) {}
					try
					{
						if (!decimaldlm.equals(""))
						{
							if (!decimaldlm.equals("."))
								ttev=ttev.replace(decimaldlm,".");
						}
					}
					catch (Exception e) {}
					try
					{
						testisnum=Double.parseDouble(ttev);
						if (!Double.isNaN(testisnum))
						{
							if (!Double.isInfinite(testisnum))
								waitingrecord[i]=String.valueOf(ttev);
						}
					}
					catch (Exception en){}
					if (vardec>0)
					{
						try
						{
							double tempv=Double.parseDouble(waitingrecord[i]);
							for (int j=0; j<vardec; j++)
							{
								tempv=tempv/10.0;
							}
							waitingrecord[i]=String.valueOf(tempv);
						}
						catch (Exception e) {}
					}
				}
			}
		}
		catch (Exception e)
		{
			message="%358%<br>\n"+e.toString()+"<br>\n";
			checklast=true;
		}
	}
}
