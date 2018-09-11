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
import ADaMSoft.utilities.StringSplitter;

/**
* This class reads the values contained into a delimited text file
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
public class Read_dlmfile extends DataTableReader
{
	StringSplitter sp;
	boolean recordisread;
	/**
	*This is the buffer
	*/
	BufferedReader in;
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
	*This is the string array of the next record
	*/
	String[] waitingrecord;
	/**
	*This is the message that will be returned in case of error
	*/
	String message="";
	/**
	*This is the char used as a separator
	*/
	String separator;
	/**
	*Opens the delimited text file and return false in case of errors
	*/
	String tablepath;
	String str;
	Vector<int[]> trimchars;
	String chartoremove;
	int[] tintchar;
	public boolean open (Hashtable<String, String> tableinfo, Vector<Hashtable<String, String>> fixedvariableinfo,
	Vector<Hashtable<String, String>> tablevariableinfo)
	{
		sp=new StringSplitter();
		try
		{
			String dlm=tableinfo.get(Keywords.DLM.toLowerCase());
			int charSeparator = Integer.parseInt(dlm);
			separator = new Character((char)charSeparator).toString();
			sp.setdlm(separator);
		}
		catch (Exception e)
		{
			message="%355%<br>\n";
			return false;
		}
		boolean avoidquotationmarks=(tableinfo.get(Keywords.avoidquotationmarks)!=null);
		sp.setQuotationMarks(avoidquotationmarks);
		String trimchar=tableinfo.get(Keywords.trimchars.toLowerCase());
		trimchars=new Vector<int[]>();
		if (trimchar!=null)
		{
			if (!trimchar.equals(""))
			{
				String[] temptrimchars=(trimchar.trim()).split(";");
				for (int i=0; i<temptrimchars.length; i++)
				{
					try
					{
						String[] temptemptrimchars=(temptrimchars[i].trim()).split(" ");
						int[] tempintchars=new int[temptemptrimchars.length];
						for (int j=0; j<temptemptrimchars.length; j++)
						{
							tempintchars[j]=-1;
							try
							{
								tempintchars[j]=Integer.parseInt(temptemptrimchars[j].trim());
							}
							catch (Exception eit){}
						}
						trimchars.add(tempintchars);
					}
					catch (Exception eit){}
				}
			}
		}
		boolean label=(tableinfo.get(Keywords.label)!=null);
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
			if (label)
				in.readLine();
		}
		catch (Exception e)
		{
			message="%356%<br>\n";
			checklast=true;
			return false;
		}
		try
		{
			if (tableinfo.get(Keywords.codethousanddlm.toLowerCase())!=null)
			{
				String thousanddlm=tableinfo.get(Keywords.codethousanddlm.toLowerCase());
				if (!thousanddlm.equals(""))
				{
					int codethousanddlm = Integer.parseInt(thousanddlm);
					thousanddlm = new Character((char)codethousanddlm).toString();
					sp.setthousanddlm(thousanddlm);
				}
			}
		}
		catch (Exception e)
		{
			message="%2147%<br>\n";
			return false;
		}
		try
		{
			if (tableinfo.get(Keywords.codedecimaldlm.toLowerCase())!=null)
			{
				String decimaldlm=tableinfo.get(Keywords.codedecimaldlm.toLowerCase());
				if (!decimaldlm.equals(""))
				{
					int codedecimaldlm = Integer.parseInt(decimaldlm);
					decimaldlm = new Character((char)codedecimaldlm).toString();
					sp.setdecimaldlm(decimaldlm);
				}
			}
		}
		catch (Exception e)
		{
			message="%2148%<br>\n";
			return false;
		}
		waitingrecord=new String[fixedvariableinfo.size()];
		try
		{
			getNextRecord();
		}
		catch (Exception e)
		{
			message="%357%<br>\n";
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
			in = null;
			System.gc();
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
			boolean label=(tableinfo.get(Keywords.label)!=null);
			java.net.URL fileUrlr;
			if((filenamer.toLowerCase()).startsWith("http"))
				fileUrlr =  new java.net.URL(filenamer);
			else
			{
				File filer=new File(filenamer);
				fileUrlr = filer.toURI().toURL();
			}
			BufferedReader inr = new BufferedReader(new InputStreamReader(fileUrlr.openStream()));
			if (label)
				inr.readLine();
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
				if (str.trim().equals("")) checklast=true;
				else
				{
					try
					{
						str=str.replaceAll("\"\"","'");
						if (trimchars.size()>0)
						{
							for (int t=0; t<trimchars.size(); t++)
							{
								chartoremove="";
								tintchar=trimchars.get(t);
								for (int c=0; c<tintchar.length; c++)
								{
									if (tintchar[c]>-1) chartoremove=chartoremove+"\\x"+Integer.toHexString(tintchar[c]);
								}
								if (!chartoremove.equals("")) str=str.replaceAll(chartoremove, "");
							}
						}
					}
					catch (Exception e) {}
					sp.settext(str);
					waitingrecord=sp.getvals();
				}
			}
		}
		catch (Exception e)
		{
			message="%358%<br>\n";
			checklast=true;
		}
	}
}
