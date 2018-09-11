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

import java.io.File;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.AdamsCompliant;
import ADaMSoft.utilities.GetSettingParameters;
/**
* This class writes the values into an Excel file
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
public class Write_xlsfile extends AdamsCompliant implements Serializable, DataTableWriter
{
	private static final long serialVersionUID = 1L;
	WritableWorkbook workbook;
	WritableSheet sheet;
	String message;
	Hashtable<String, String> tabinfo;
	Vector<Hashtable<String, String>> varinfo;
	String newfile;
	String oldfile;
	int currentrow;
	boolean writingerror;
	int totalrecord;
	boolean[] ftmtext;
	double tv;
	/**
	*Returns the parameter required to create a data table in an Excel file.<p>
	*These are DATA and DICT.
	*/
	public LinkedList<GetSettingParameters> initialize()
	{
		LinkedList<GetSettingParameters> tabpar=new LinkedList<GetSettingParameters>();
		tabpar.add(new GetSettingParameters(Keywords.DATA, false, 566));
		tabpar.add(new GetSettingParameters(Keywords.dict, false, 405));
		tabpar.add(new GetSettingParameters(Keywords.rowlabel, false, 567));
		return tabpar;
	}
	/**
	*Opens the data file and returns false for the error (in this case with getmessage() it is possibile to see the error
	*/
	public boolean open(Hashtable<String, String> tableinfo, Vector<Hashtable<String, String>> tempvarinfo)
	{
		ftmtext=new boolean[tempvarinfo.size()];
		writingerror=false;
		totalrecord=0;
		try
		{
			Class.forName("jxl.write.WritableWorkbook");
			Class.forName("jxl.write.WritableSheet");
		}
		catch(Exception ex)
		{
			message="%568%<br>\n";
			return false;
		}
		tabinfo=new Hashtable<String, String>();
		String label="0";
		int rowlabel=0;
		if (tableinfo.size()==0)
			tabinfo.put(Keywords.rowlabel.toLowerCase(), label);
		else
		{
			label=tableinfo.get(Keywords.rowlabel.toLowerCase());
			if (label==null)
			{
				tabinfo.put(Keywords.rowlabel.toLowerCase(), "0");
			}
			else
			{
				try
				{
					rowlabel=Integer.parseInt(label);
					tabinfo.put(Keywords.rowlabel.toLowerCase(), label);
				}
				catch (Exception e)
				{
					tabinfo.put(Keywords.rowlabel.toLowerCase(), "0");
					rowlabel=0;
				}
			}
		}
		String file=tableinfo.get(Keywords.DATA.toLowerCase());
		if (file==null)
			file=tableinfo.get(Keywords.WorkDir);
		file=file+tableinfo.get(Keywords.tablename);
		String filetocreate=tableinfo.get(Keywords.tablename);
		if (!file.toLowerCase().endsWith(".xls"))
			file=file+".xls";
		file = toAdamsFormat(file);
		newfile=file;
		tabinfo.put(Keywords.DATA.toLowerCase(), newfile);
		file=file+".xls";
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
		if (tempvarinfo.size()>256)
		{
			message="%4020%<br>\n";
			return false;
		}
		for (int i=0; i<tempvarinfo.size(); i++)
		{
			Hashtable<String, String> tempv=new Hashtable<String, String>();
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
			tempv.put(Keywords.ExcelVariableColumn.toLowerCase(), String.valueOf(i));
			varinfo.add(tempv);
		}
		tabinfo.put(Keywords.lastcol.toLowerCase(), String.valueOf(tempvarinfo.size()-1));
		try
		{
			workbook = Workbook.createWorkbook(new File(file));
			sheet = workbook.createSheet(Keywords.SoftwareName, 0);
			for (int i=0; i<tempvarinfo.size(); i++)
			{
				Hashtable<String, String> currentvar=tempvarinfo.get(i);
				String templabel=currentvar.get(Keywords.VariableName.toLowerCase());
				Label lab = new Label(i, rowlabel, templabel);
				sheet.addCell(lab);
			}
		}
		catch (Exception e)
		{
			message="%569% ("+file+")<br>\n";
			return false;
		}
		currentrow=rowlabel+1;
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
			if (totalrecord>65536)
			{
				writingerror=true;
				totalrecord=65536;
				return false;
			}
			for(int i=0;i<values.length;i++)
			{
				if (!ftmtext[i])
				{
					try
					{
						tv = Double.parseDouble(values[i]);
						jxl.write.Number number = new jxl.write.Number(i, currentrow, tv);
						sheet.addCell(number);
					}
					catch(NumberFormatException nfe)
					{
						Label label = new Label(i, currentrow,"");
						sheet.addCell(label);
					}
				}
				else
				{
					Label label = new Label(i, currentrow,values[i]);
					sheet.addCell(label);
				}
			}
			currentrow++;
			return true;
		}
		catch (Exception e)
		{
			message="%570%<br>\n";
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
			workbook.close();
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
		if ((writingerror) && (totalrecord<65536))
		{
			message="%390%<br>\n";
			try
			{
				workbook.close();
			}
			catch (Exception e) {}
			try
			{
				(new File(oldfile)).delete();
			}
			catch (Exception e) {}
			return false;
		}
		else if (totalrecord>=65536)
		{
			message="%787%<br>\n";
			try
			{
				workbook.close();
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
			if (totalrecord==0)
			{
				workbook.close();
				message="%786%<br>\n";
				(new File(oldfile)).delete();
				return false;
			}
			workbook.write();
			workbook.close();
			File file = new File(oldfile);
			File file2 = new File(newfile);
			boolean exists = file2.exists();
			if (exists)
			{
				boolean deletefile=file2.delete();
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
		tabinfo.put(Keywords.lastrow.toLowerCase(), String.valueOf(currentrow));
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
