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

import ADaMSoft.keywords.Keywords;

/**
* This class reads the values contained into a table stored in memory
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
public class Read_memtable extends DataTableReader
{
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
	/**
	*Contains the array of values
	*/
	Vector<MemoryValue[]> values;
	/**
	*Contains the number of the actual record
	*/
	int actual;
	/**
	*Contains the actual record
	*/
	String[] temprecord;
	/**
	*Contains the record that will be returned
	*/
	String[] retrecord;
	/**
	*Contains the total number of records
	*/
	int totrecords;
	String tablename;
	/**
	*Opens the delimited text file and return false in case of errors
	*/
	public boolean open (Hashtable<String, String> tableinfo, Vector<Hashtable<String, String>> fixedvariableinfo,
	Vector<Hashtable<String, String>> tablevariableinfo)
	{
		actual=0;
		tablename=tableinfo.get(Keywords.tablename).toLowerCase();
		if ((values=Keywords.MemoriesDatasets.get(tablename))==null)
		{
			message="%1449%<br>\n";
			return false;
		}
		retrecord=new String[fixedvariableinfo.size()];
		totrecords=values.size();
		return true;
	}
	/**
	*Delete the data table
	*/
	public synchronized boolean deletetable()
	{
		try
		{
			Keywords.MemoriesDatasets.remove(tablename);
			return true;
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
		if (actual>=totrecords)
			checklast=true;
		if(!checklast)
		{
			for (int i=0; i<retrecord.length; i++)
			{
				retrecord[i]=values.get(actual)[i].toString();
			}
			actual++;
		}
		return retrecord;
	}
	/**
	*Returns true if the last observations was reached
	*/
	public boolean isLast()
	{
		if (actual<totrecords)
			return false;
		else
		{
			checklast=true;
			return true;
		}
	}
	/**
	*Close the file and returns false in case of error
	*/
	public boolean close()
	{
		return true;
	}
	/**
	*Returns an int with the number of records contained in the delimited file
	*/
	public int getRecords(Hashtable<String, String> tableinfo)
	{
		String tablename=tableinfo.get(Keywords.tablename).toLowerCase();
		if (Keywords.MemoriesDatasets.get(tablename)==null)
		{
			message="%1449%<br>\n";
			return 0;
		}
		return Keywords.MemoriesDatasets.get(tablename.toLowerCase())==null?0:Keywords.MemoriesDatasets.get(tablename.toLowerCase()).size();
	}
	/**
	*Returns the error message
	*/
	public String getMessage()
	{
		return message;
	}
}
