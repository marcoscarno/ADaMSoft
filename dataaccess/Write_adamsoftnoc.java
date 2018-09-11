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

import java.io.BufferedOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.File;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.AdamsCompliant;
import ADaMSoft.utilities.GetSettingParameters;
/**
* This class writes the values into the not compressed ADaMSoft file
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
public class Write_adamsoftnoc extends AdamsCompliant implements Serializable, DataTableWriter
{
	BufferedOutputStream bisr;
	private static final long serialVersionUID = 1L;
	ObjectOutputStream oos=null;
	String message;
	Hashtable<String, String> tabinfo;
	Vector<Hashtable<String, String>> varinfo;
	String newfile;
	String oldfile;
	boolean writingerror;
	int numberofvars, totalrecord;
	int tempnumberofvars;
	/**
	*Returns the parameter required to create a data table in the compressed ADaMSoft form.<p>
	*These are DATA and DICT.
	*/
	public LinkedList<GetSettingParameters> initialize()
	{
		LinkedList<GetSettingParameters> tabpar=new LinkedList<GetSettingParameters>();
		tabpar.add(new GetSettingParameters(Keywords.DATA, false, 2814));
		tabpar.add(new GetSettingParameters(Keywords.dict, false, 405));
		return tabpar;
	}
	/**
	*Opens the data file and returns false for the error (in this case with getmessage() it is possibile to see the error
	*/
	public boolean open(Hashtable<String, String> tableinfo, Vector<Hashtable<String, String>> tempvarinfo)
	{
		writingerror=false;
		totalrecord=0;
		tabinfo=new Hashtable<String, String>();
		String file=tableinfo.get(Keywords.DATA.toLowerCase());
		if (file==null)
			file=tableinfo.get(Keywords.WorkDir);
		file=file+tableinfo.get(Keywords.tablename);
		String filetocreate=tableinfo.get(Keywords.tablename);
		if (!file.toLowerCase().endsWith(Keywords.DataExtensionNoc))
			file=file+Keywords.DataExtensionNoc;
		file=toAdamsFormat(file);
		newfile=file;
		tabinfo.put(Keywords.DATA.toLowerCase(), newfile);
		file=file+".tmp";
		oldfile=file;
		boolean iswriting=(new File(oldfile)).exists();
		int cyclesverify=0;
		while (iswriting)
		{
			try
			{
				Thread.sleep(1000);
			}
			catch (Exception exes) {}
			cyclesverify++;
			iswriting=(new File(file)).exists();
			if (cyclesverify==10)
				iswriting=false;
		}
		if (cyclesverify==10)
		{
			message="%2800% ("+filetocreate+")<br>\n";
			return false;
		}
		varinfo=new Vector<Hashtable<String, String>>();
		numberofvars=tempvarinfo.size();
		tempnumberofvars=numberofvars;
		for (int i=0; i<tempvarinfo.size(); i++)
		{
			Hashtable<String, String> tempv=new Hashtable<String, String>();
			varinfo.add(tempv);
		}
		try
		{
			bisr = new BufferedOutputStream(new FileOutputStream(oldfile));
			oos = new ObjectOutputStream(bisr);
		}
		catch (Exception e)
		{
			message="%2813% ("+file+"), "+e.toString()+"<br>\n";
			return false;
		}
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
			oos.writeObject(values);
			oos.flush();
			bisr.flush();
			oos.reset();
			totalrecord++;
			return true;
		}
		catch (Exception e)
		{
			try
			{
				oos.flush();
				bisr.flush();
				oos.close();
				bisr.close();
			}
			catch (Exception ed) {}
			message="%390%<br>\n";
			writingerror=true;
			return false;
		}
	}
	/**
	*Close the file and returns false in case of error
	*/
	public boolean close()
	{
		if (writingerror)
		{
			message="%390%<br>\n";
			try
			{
				oos.flush();
				oos.close();
				bisr.close();
			}
			catch (Exception e) {}
			try
			{
				(new File(oldfile)).delete();
			}
			catch (Exception e) {}
			return false;
		}
		try
		{
			oos.flush();
			oos.close();
			bisr.close();
			if (totalrecord==0)
			{
				message="%786%<br>\n";
				(new File(oldfile)).delete();
				return false;
			}
			File file = new File(oldfile);
			File file2 = new File(newfile);
			if (file2.exists())
			{
				if (!file2.delete())
				{
					message="%391%<br>\n";
					return false;
				}
			}
			if (!file.renameTo(file2))
			{
				message="%391%<br>\n";
				return false;
			}
			return true;
		}
		catch (Exception e)
		{
			message="%391%<br>\n";
		}
		return false;
	}
	/**
	*Delete the temporary file
	*/
	public boolean deletetmp()
	{
		try
		{
			oos.flush();
			oos.close();
			bisr.close();
			(new File(oldfile)).delete();
			return true;
		}
		catch (Exception e) {}
		return false;
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
