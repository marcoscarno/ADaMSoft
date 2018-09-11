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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import java.io.Serializable;
import java.text.DecimalFormatSymbols;
import java.text.DecimalFormat;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.AdamsCompliant;
import ADaMSoft.utilities.GetSettingParameters;
/**
* This class writes the values into a delimited text file
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
public class Write_dlmfile extends AdamsCompliant implements Serializable, DataTableWriter
{
	private static final long serialVersionUID = 1L;
	String separator;
	String message;
	BufferedWriter outfile;
	Hashtable<String, String> tabinfo;
	Vector<Hashtable<String, String>> varinfo;
	String newfile;
	String oldfile;
	boolean writingerror;
	int totalrecord;
	String thousanddlm;
	String decimaldlm;
	DecimalFormatSymbols dfs;
	DecimalFormat df;
	double tv;
	/**
	*Returns the parameter required to create a data table in a tab delimited file.<p>
	*These are DATA and DICT.
	*/
	public LinkedList<GetSettingParameters> initialize()
	{
		LinkedList<GetSettingParameters> tabpar=new LinkedList<GetSettingParameters>();
		tabpar.add(new GetSettingParameters(Keywords.DATA, false, 379));
		tabpar.add(new GetSettingParameters(Keywords.dict, false, 405));
		tabpar.add(new GetSettingParameters(Keywords.DLM, false, 380));
		tabpar.add(new GetSettingParameters(Keywords.LABEL, false, 381));
		tabpar.add(new GetSettingParameters(Keywords.codethousanddlm.toUpperCase(), false, 2142));
		tabpar.add(new GetSettingParameters(Keywords.codedecimaldlm.toUpperCase(), false, 2143));
		return tabpar;
	}
	/**
	*Opens the data file and returns false for the error (in this case with getmessage() it is possibile to see the error
	*/
	public boolean open(Hashtable<String, String> tableinfo, Vector<Hashtable<String, String>> tempvarinfo)
	{
		thousanddlm="";
		decimaldlm="";
		writingerror=false;
		totalrecord=0;
		tabinfo=new Hashtable<String, String>();
		String dlm="";
		boolean uselabels=false;
		String label="";
		if (tableinfo.size()==0)
		{
			int charSeparator=9;
			separator=new Character((char)charSeparator).toString();
			dlm="09";
		}
		else
		{
			dlm=tableinfo.get(Keywords.DLM.toLowerCase());
			label=tableinfo.get(Keywords.LABEL.toLowerCase());
			if (label!=null)
			{
				if (label.equalsIgnoreCase("ON"))
				{
					uselabels=true;
					tabinfo.put(Keywords.LABEL.toLowerCase(), "ON");
				}
			}
			if (dlm==null)
				dlm="09";
			int charSeparator;
			try
			{
				charSeparator = Integer.parseInt(dlm);
			}
			catch (Exception e)
			{
				charSeparator=9;
			}
			separator=new Character((char)charSeparator).toString();
			if (tableinfo.get(Keywords.codethousanddlm.toLowerCase())!=null)
			{
				try
				{
					thousanddlm=tableinfo.get(Keywords.codethousanddlm.toLowerCase());
					if (!thousanddlm.equals(""))
					{
						int codethousanddlm = Integer.parseInt(thousanddlm);
						thousanddlm = new Character((char)codethousanddlm).toString();
						tabinfo.put(Keywords.codethousanddlm.toLowerCase(), tableinfo.get(Keywords.codethousanddlm.toLowerCase()));
					}
				}
				catch (Exception e)
				{
					thousanddlm="";
				}
			}
			if (tableinfo.get(Keywords.codedecimaldlm.toLowerCase())!=null)
			{
				try
				{
					decimaldlm=tableinfo.get(Keywords.codedecimaldlm.toLowerCase());
					if (!decimaldlm.equals(""))
					{
						int codedecimaldlm = Integer.parseInt(decimaldlm);
						decimaldlm = new Character((char)codedecimaldlm).toString();
						tabinfo.put(Keywords.codedecimaldlm.toLowerCase(), tableinfo.get(Keywords.codedecimaldlm.toLowerCase()));
					}
				}
				catch (Exception e)
				{
					decimaldlm="";
				}
			}
			if ( (!thousanddlm.equals("")) || (!decimaldlm.equals("")) )
			{
				dfs=new DecimalFormatSymbols();
				if (!thousanddlm.equals(""))
					dfs.setGroupingSeparator(thousanddlm.charAt(0));
				if (!decimaldlm.equals(""))
					dfs.setDecimalSeparator(decimaldlm.charAt(0));
				df=new DecimalFormat();
				df.setDecimalFormatSymbols(dfs);
			}
		}
		tabinfo.put(Keywords.DLM.toLowerCase(), dlm);
		String file=tableinfo.get(Keywords.DATA.toLowerCase());
		if (file==null)
			file=tableinfo.get(Keywords.WorkDir);
		file=file+tableinfo.get(Keywords.tablename);
		String filetocreate=tableinfo.get(Keywords.tablename);
		if (!file.toLowerCase().endsWith(".txt"))
			file=file+".txt";
		file= toAdamsFormat(file);
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
		for (int i=0; i<tempvarinfo.size(); i++)
		{
			Hashtable<String, String> tempv=new Hashtable<String, String>();
			varinfo.add(tempv);
		}
		try
		{
			outfile = new BufferedWriter(new FileWriter(file, true));
			if (uselabels)
			{
				for (int i=0; i<tempvarinfo.size(); i++)
				{
					Hashtable<String, String> currentvar=tempvarinfo.get(i);
					String templabel=currentvar.get(Keywords.VariableName.toLowerCase());
					if (templabel.indexOf(separator)>=0)
						templabel="\""+templabel+"\"";
					outfile.write(templabel);
					if (i<(tempvarinfo.size()-1))
						outfile.write(separator);
				}
				outfile.write("\n");
				outfile.flush();
			}
		}
		catch (Exception e)
		{
			message="%389% ("+file+")<br>\n";
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
			totalrecord++;
			for(int i=0;i<values.length;i++)
			{
				if (values[i].indexOf(separator)>=0)
					values[i]="\""+values[i]+"\"";
				outfile.write(values[i].trim());
				if (i<(values.length-1))
					outfile.write(separator);
			}
			outfile.write("\n");
			outfile.flush();
			return true;
		}
		catch (Exception e)
		{
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
		try
		{
			outfile.close();
			(new File(oldfile)).delete();
			return true;
		}
		catch (Exception e) {}
		return false;
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
				outfile.close();
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
			outfile.flush();
			if (totalrecord==0)
			{
				outfile.close();
				message="%786%<br>\n";
				(new File(oldfile)).delete();
				return false;
			}
			outfile.close();
			File file = new File(oldfile);
			File file2 = new File(newfile);
			boolean exists = file2.exists();
			if (exists)
			{
				boolean deletefile=false;
				try
				{
					deletefile=file2.delete();
				}
				catch (SecurityException edel)
				{
					message="%391% ("+edel.getMessage()+")<br>\n";
					return false;
				}
				catch (Exception edel)
				{
					message="%391% ("+edel.getMessage()+")<br>\n";
					return false;
				}
				if (!deletefile)
				{
					message="%391%<br>\n";
					return false;
				}
			}
			boolean success = file.renameTo(file2);
			if (!success)
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
