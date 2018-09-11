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

package ADaMSoft.procedures;

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.VariableUtilities;

import ADaMSoft.keywords.Keywords;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
* This is the procedure that export a data table in excel or in a tab delimited file
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcExportdatatable implements RunStep
{
	/**
	* Starts the execution of Proc Exportdatatable and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.dict, Keywords.filetype, Keywords.filename};
		String [] optionalparameters=new String[] {Keywords.var, Keywords.replace, Keywords.where};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String vartemp=(String)parameters.get(Keywords.var.toLowerCase());
		String filetype=(String)parameters.get(Keywords.filetype.toLowerCase());
		String filename=(String)parameters.get(Keywords.filename.toLowerCase());
		VariableUtilities varu=new VariableUtilities(dict, null, vartemp, null, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);
		String[] var=varu.getanalysisvar();
		String[] ftype=new String[] {Keywords.excel, Keywords.tabdlmfile};
		int typefile=steputilities.CheckOption(ftype, filetype);
		if (typefile==0)
			return new Result("%3394%<br>\n", false, null);

		if (typefile==1 && var.length>255)
		{
			return new Result("%4020%<br>\n", false, null);
		}

		if ((typefile==1) && (!filename.toLowerCase().endsWith(".xls")))
			filename=filename+".xls";
		if ((typefile==2) && (!filename.toLowerCase().endsWith(".txt")))
			filename=filename+".txt";

		boolean exist=(new File(filename)).exists();
		if (exist)
		{
			boolean success = (new File(filename)).delete();
			if (!success)
				return new Result("%2882%<br>\n", false, null);
		}

		String tempfile=(String)parameters.get(Keywords.WorkDir);
		if (typefile==1) tempfile=tempfile+"temp.xls";
		if (typefile==2) tempfile=tempfile+"temp.txt";
		tempfile=filename;

		String replace=(String)parameters.get(Keywords.replace);

		int[] replacerule=varu.getreplaceruleforsel(replace);
		String[] values=null;
		DataReader data = new DataReader(dict);
		if (!data.open(var, replacerule, true))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}
		if (typefile==1)
		{
			try
			{
				String sheetname=dict.getdescription();
				if (sheetname==null)
					sheetname="ADaMS";
				if (sheetname.equals(""))
					sheetname="ADaMS";
				if (sheetname.length()>10) sheetname=sheetname.substring(0, 10);
				sheetname=sheetname.replaceAll(" ","_");
				WritableWorkbook workbook=Workbook.createWorkbook(new File(tempfile));
				WritableSheet sheet=workbook.createSheet(sheetname, 0);
				int row=0;
				for (int j=0; j<var.length; j++)
				{
					String labname="";
					labname=dict.getvarlabelfromname(var[j]);
					Label lab = new Label(j, row, labname);
					sheet.addCell(lab);
				}
				row++;
				while (!data.isLast())
				{
					values = data.getRecord();
					for (int j=0; j<values.length; j++)
					{
						if (!dict.getvarformatfromname(var[j]).toUpperCase().startsWith(Keywords.TEXTSuffix.toUpperCase()))
						{
							try
							{
								double val = Double.parseDouble(values[j]);
								if (!Double.isNaN(val))
								{
									jxl.write.Number number = new jxl.write.Number(j, row, val);
									sheet.addCell(number);
								}
								else
								{
									Label label = new Label(j, row, "");
									sheet.addCell(label);
								}
							}
							catch(NumberFormatException nfe)
							{
								Label label = new Label(j, row,values[j]);
								sheet.addCell(label);
							}
						}
						else
						{
							Label label = new Label(j, row,values[j]);
							sheet.addCell(label);
						}
					}
					row++;
				}
				data.close();
				workbook.write();
				workbook.close();
			}
			catch (Exception e)
			{
				return new Result("%2886%<br>\n"+e.toString()+"<br>\n", false, null);
			}
		}

		if (typefile==2)
		{
			try
			{
				BufferedWriter fileout = new BufferedWriter(new FileWriter(tempfile, true));
				for (int j=0; j<var.length; j++)
				{
					String labname="";
					labname=dict.getvarlabelfromname(var[j]);
					fileout.write(labname);
					if (j<(var.length-1))
						fileout.write("\t");
				}
				fileout.write("\n");
				while (!data.isLast())
				{
					values = data.getRecord();
					for (int j=0; j<values.length; j++)
					{
						fileout.write(values[j]);
						if (j<(var.length-1))
							fileout.write("\t");
					}
					fileout.write("\n");
				}
				data.close();
				fileout.close();
			}
			catch (Exception e)
			{
				return new Result("%2887%<br>\n"+e.toString()+"<br>\n", false, null);
			}
		}

		if (typefile==1) return new Result("%2888% ("+filename+")<br>\n", true, null);
		else return new Result("%2889% ("+filename+")<br>\n", true, null);
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 2874, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", false, 2891, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.filetype, "listsingle=2875_"+Keywords.excel+",2876_"+Keywords.tabdlmfile, true, 2878, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.filename, "filesave=.all", false, 2881, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="4166";
		retprocinfo[1]="2873";
		return retprocinfo;
	}
}
