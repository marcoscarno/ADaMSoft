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

import java.util.Hashtable;
import java.util.Vector;
import java.util.LinkedList;
import java.io.Serializable;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetSettingParameters;
/**
* This class writes the values into a table in memory
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
public class Write_memtable implements Serializable, DataTableWriter
{
	private static final long serialVersionUID = 1L;
	String message;
	Hashtable<String, String> tabinfo;
	Vector<Hashtable<String, String>> varinfo;
	String newtable;
	boolean writingerror;
	Vector<MemoryValue[]> tempvalues;
	int totalrecord;
	boolean[] ftmtext;
	double tv;
	/**
	*Returns the parameter required to create a data table in the memory.<p>
	*This is only DICT.
	*/
	public LinkedList<GetSettingParameters> initialize()
	{
		LinkedList<GetSettingParameters> tabpar=new LinkedList<GetSettingParameters>();
		tabpar.add(new GetSettingParameters(Keywords.dict, false, 405));
		return tabpar;
	}
	/**
	*Opens the data file and returns false for the error (in this case with getmessage() it is possibile to see the error
	*/
	public boolean open(Hashtable<String, String> tableinfo, Vector<Hashtable<String, String>> tempvarinfo)
	{
		ftmtext=new boolean[tempvarinfo.size()];
		totalrecord=0;
		writingerror=false;
		tabinfo=new Hashtable<String, String>();
		newtable=tableinfo.get(Keywords.tablename).toLowerCase();
		tabinfo.put(Keywords.tablename, newtable);
		varinfo=new Vector<Hashtable<String, String>>();
		for (int i=0; i<tempvarinfo.size(); i++)
		{
			String wfmt=(tempvarinfo.get(i)).get(Keywords.VariableFormat.toLowerCase());
			if ((wfmt.length()==Keywords.NUMSuffix.length()) && (wfmt.toUpperCase().startsWith(Keywords.NUMSuffix.toUpperCase())))
				ftmtext[i]=false;
			else if ((wfmt.length()>Keywords.NUMSuffix.length()) && (wfmt.toUpperCase().startsWith(Keywords.NUMSuffix.toUpperCase())))
			{
				String num=wfmt.substring(Keywords.NUMSuffix.length());
				if ((num.toUpperCase()).startsWith(Keywords.DTSuffix))
					ftmtext[i]=true;
				else if ((num.toUpperCase()).startsWith(Keywords.DATESuffix))
					ftmtext[i]=true;
				else if ((num.toUpperCase()).startsWith(Keywords.TIMESuffix))
					ftmtext[i]=true;
			}
			else
				ftmtext[i]=true;
			Hashtable<String, String> tempv=new Hashtable<String, String>();
			varinfo.add(tempv);
		}
		tempvalues=new Vector<MemoryValue[]>();
		return true;
	}
	/**
	*Write the values and returns false in case of error
	*/
	public boolean writevalues(String[] values)
	{
		if (writingerror)
			return false;
		try
		{
			int length=values.length;
			MemoryValue[] memvals= new MemoryValue[length];
			for(int i=0;i<length;i++)
			{
				if (!ftmtext[i])
				{
					try
					{
						tv=Double.parseDouble(values[i]);
						memvals[i]=new MemoryValue(tv);
					}
					catch (Exception e)
					{
						memvals[i]=new MemoryValue(Double.NaN);
					}
				}
				else
					memvals[i]=new MemoryValue(values[i]);
			}
			tempvalues.add(memvals);
			totalrecord++;
			return true;
		}
		catch (Exception e)
		{
			tempvalues.clear();
			tempvalues=null;
			message="%390%<br>\n";
			writingerror=true;
			return false;
		}
	}
	/**
	*Delete the temporary file
	*/
	public boolean deletetmp()
	{
		tempvalues.clear();
		tempvalues=null;
		return true;
	}
	/**
	*Close the file and returns false in case of error
	*/
	public boolean close()
	{
		if (writingerror)
		{
			tempvalues.clear();
			tempvalues=null;
			return false;
		}
		if (totalrecord==0)
		{
			message="%786%<br>\n";
			tempvalues.clear();
			tempvalues=null;
			return false;
		}
		Keywords.MemoriesDatasets.put(newtable, tempvalues);
		return true;
	}
	/**
	*Returns the error message
	*/
	public String getmessage()
	{
		return message;
	}
	/**
	*Returns the information on the data table that will be inserted in the new dictionary
	*/
	public Hashtable<String, String> getTableInfo ()
	{
		return tabinfo;
	}
	/**
	*Returns the information on the variables, specific for this kind of data file
	*/
	public Vector<Hashtable<String, String>> getVariablesInfo()
	{
		return varinfo;
	}
}
