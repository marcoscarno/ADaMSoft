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

import jxl.Cell;
import jxl.CellType;
import jxl.Sheet;
import jxl.Workbook;
import java.io.File;
import java.util.Hashtable;
import java.util.Vector;
import java.text.NumberFormat;
import java.util.Locale;


import ADaMSoft.keywords.Keywords;

/**
* This class reads the values contained into an Excel file
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
public class Read_xlsfile extends DataTableReader
{
	/**
	*This is the Excel workbook
	*/
	Workbook workbook;
	/**
	*This is the sheet
	*/
	Sheet sheetfile;
	/**
	*These are the information on the Excel file
	*/
	String sheet;
	int firstrownumber;
	int lastrownumber;
	int firstcolnumber;
	int lastcolnumber;
	int rowlabelnumber;
	int obspointer;
	/**
	*If true, means that the last observation was reached
	*/
	boolean checklast=false;
	/**
	*This is the string array of the actual record
	*/
	String[] actualrecord;
	/**
	*This is the message that will be returned in case of error
	*/
	String message="";
	/**
	*This is the char used as a separator
	*/
	String separator;
	/**
	*Contains the number of the columns of the requested variables
	*/
	int[] rifvar;
	NumberFormat nf;
	String tablepath;
	boolean[] istext;
	/**
	*Opens the delimited text file and return false in case of errors
	*/
	public boolean open (Hashtable<String, String> tableinfo, Vector<Hashtable<String, String>> fixedvariableinfo,
	Vector<Hashtable<String, String>> tablevariableinfo)
	{
		Locale defaultLang = Locale.getDefault();
		nf = NumberFormat.getInstance(defaultLang);
		try
		{
			Class.forName("jxl.Workbook");
		}
		catch (Exception e)
		{
			message="%253%<br>\n";
			return false;
		}
		obspointer=0;
		istext=new boolean[fixedvariableinfo.size()];
		try
		{
			rifvar=new int[fixedvariableinfo.size()];
			for (int i=0; i<fixedvariableinfo.size(); i++)
			{
				Hashtable<String, String> temp=tablevariableinfo.get(i);
				String temprif=temp.get(Keywords.ExcelVariableColumn.toLowerCase());
				rifvar[i]=Integer.parseInt(temprif);
				Hashtable<String, String> tempvar=fixedvariableinfo.get(i);
				String it=tempvar.get(Keywords.VariableFormat.toLowerCase());
				if (it.toLowerCase().startsWith(Keywords.TEXTSuffix.toLowerCase()))
					istext[i]=true;
				else
					istext[i]=false;
			}
		}
		catch (Exception e)
		{
			message="%354%<br>\n";
			return false;
		}
		try
		{
			String temp=tableinfo.get(Keywords.rowlabel.toLowerCase());
			rowlabelnumber = Integer.parseInt(temp);
		}
		catch (Exception e)
		{
			rowlabelnumber=-1;
		}
		try
		{
			String temp=tableinfo.get(Keywords.firstrow.toLowerCase());
			firstrownumber = Integer.parseInt(temp);
		}
		catch (Exception e)
		{
			firstrownumber=0;
		}
		if ((rowlabelnumber>=0) &&(firstrownumber==0))
			firstrownumber=rowlabelnumber+1;
		try
		{
			String temp=tableinfo.get(Keywords.lastrow.toLowerCase());
			lastrownumber = Integer.parseInt(temp);
		}
		catch (Exception e)
		{
			message="%565%<br>\n";
			return false;
		}
		try
		{
			String temp=tableinfo.get(Keywords.firstcol.toLowerCase());
			firstcolnumber = Integer.parseInt(temp);
		}
		catch (Exception e)
		{
			firstcolnumber=0;
		}
		try
		{
			String temp=tableinfo.get(Keywords.lastcol.toLowerCase());
			lastcolnumber = Integer.parseInt(temp);
		}
		catch (Exception e)
		{
			message="%565%<br>\n";
			return false;
		}
		sheet      = tableinfo.get(Keywords.sheet.toLowerCase());
		if (sheet==null)
			sheet="0";
		try
		{
			String xlsfile=tableinfo.get(Keywords.DATA.toLowerCase());
			tablepath=xlsfile;
			java.net.URL fileUrl;
			if((xlsfile.toLowerCase()).startsWith("http"))
				fileUrl =  new java.net.URL(xlsfile);
			else
			{
				File Excelfile=new File(xlsfile);
				fileUrl = Excelfile.toURI().toURL();
			}
			workbook = Workbook.getWorkbook(fileUrl.openStream());
			int indsheet=0;
			try
			{
				indsheet = (new Integer(sheet)).intValue();
			}
			catch(NumberFormatException nfe){}
			if(indsheet>=0)
				sheetfile = workbook.getSheet(indsheet);
			else
				sheetfile = workbook.getSheet(sheet);
			if(sheetfile==null)
			{
				message="%254%<br>\n";
				checklast=true;
				return false;
			}
		}
		catch (Exception e)
		{
			message="%254%<br>\n";
			checklast=true;
			return false;
		}
		return true;
	}
	/**
	*Returns the current record
	*/
	public String[] getRecord()
	{
		try
		{
			Cell[] record=sheetfile.getRow(obspointer+firstrownumber);
			actualrecord=new String[istext.length];
			for (int i=0; i<rifvar.length; i++)
			{
				if (i>=record.length)
					actualrecord[i]="";
				else if (rifvar[i]>=record.length)
					actualrecord[i]="";
				else
				{
					if (!istext[i])
					{
						if(record[rifvar[i]].getType()==CellType.NUMBER || record[rifvar[i]].getType()==CellType.NUMBER_FORMULA)
						{
							try
							{

								double val = (nf.parse(record[rifvar[i]].getContents())).doubleValue();
								actualrecord[i]=String.valueOf(val);
							}
							catch (Exception e)
							{
								actualrecord[i]=record[rifvar[i]].getContents();
								if (actualrecord[i]==null)
									actualrecord[i]="";
							}
						}
						else
						{
							actualrecord[i]=record[rifvar[i]].getContents();
							if (actualrecord[i]==null)
								actualrecord[i]="";
						}
					}
					else
					{
						actualrecord[i]=record[rifvar[i]].getContents();
						{
							if (actualrecord[i]==null)
								actualrecord[i]="";
						}
					}
				}
			}
			obspointer++;
		}
		catch (Exception e)
		{
			message="%358%<br>\n";
			checklast=true;
		}
		return actualrecord;
	}
	/**
	*Returns true if the last observations was reached
	*/
	public boolean isLast()
	{
		if (checklast)
			return true;
		if ((obspointer+firstrownumber)>=(lastrownumber))
			return true;
		else
			return false;
	}
	/**
	*Delete the data table
	*/
	public synchronized boolean deletetable()
	{
		try
		{
			workbook.close();
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
	*Close the file and returns false in case of error
	*/
	public boolean close()
	{
		try
		{
			workbook.close();
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
		try
		{
			Class.forName("jxl.Workbook");
		}
		catch (Exception e)
		{
			message="%253%<br>\n";
			return 0;
		}
		int first=0;
		int last=0;
		int rowl=-1;
		try
		{
			String temp=tableinfo.get(Keywords.rowlabel.toLowerCase());
			rowl = Integer.parseInt(temp);
		}
		catch (Exception e)
		{
			rowl=-1;
		}
		try
		{
			String temp=tableinfo.get(Keywords.firstrow.toLowerCase());
			first = Integer.parseInt(temp);
		}
		catch (Exception e)
		{
			first=0;
		}
		if ((rowl>=0) && (first==0))
			first=rowl+1;
		try
		{
			String temp=tableinfo.get(Keywords.lastrow.toLowerCase());
			last = Integer.parseInt(temp);
		}
		catch (Exception e)
		{
			message="%565%<br>\n";
			return 0;
		}
		return last-first;
	}
	/**
	*Returns the error message
	*/
	public String getMessage()
	{
		return message;
	}
}
