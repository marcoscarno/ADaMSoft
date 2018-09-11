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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;


/**
* Export one or more data sets in ax Excel file
* @author marco.scarno@gmail.com
* @date 04/09/2015
*/
public class ReportXls implements RunStep
{
	/**
	*Export a data set into an Excel file
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Object tempinfo=parameters.get("reportinfo");
		@SuppressWarnings("rawtypes")
		Hashtable reportinfo=(Hashtable)tempinfo;

		int maxds=0;
		for (@SuppressWarnings("rawtypes")
		Enumeration e = reportinfo.keys() ; e.hasMoreElements() ;)
		{
			int rifds = ((Integer)e.nextElement()).intValue();
			if (rifds>maxds)
				maxds=rifds;
		}
		String reportname=(String)parameters.get("reportname");
		String reportpath=(String)parameters.get("reportpath");
		if (reportpath==null)
			reportpath="";

		Object generalinfo=reportinfo.get(new Integer(0));
		@SuppressWarnings("rawtypes")
		Hashtable general=(Hashtable)generalinfo;

		if (reportpath.equals(""))
			reportpath=(String)parameters.get(Keywords.WorkDir);

		String outreport=reportpath+reportname+".xls";
		boolean exist=(new File(outreport)).exists();
		if (exist)
		{
			boolean success = (new File(outreport)).delete();
			if (!success)
				return new Result("%482%<br>\n", false, null);
		}
		WritableWorkbook workbook;
		String onesheetfords="";
		try
		{
			onesheetfords=(String)general.get(Keywords.onesheetfords);
			if (onesheetfords==null)
				onesheetfords="";
		}
		catch (Exception exe)
		{
			onesheetfords="";
		}
		try
		{
			workbook = Workbook.createWorkbook(new File(outreport));
		}
		catch (Exception e)
		{
			return new Result("%2149% ("+outreport+")<br>\n", false, null);
		}

		try
		{
			WritableSheet onesheet=null;
			int col=0;
			int row=0;
			if (onesheetfords.equalsIgnoreCase("YES"))
				onesheet = workbook.createSheet(Keywords.SoftwareName, 0);
			for (int i=1; i<=maxds; i++)
			{
				WritableSheet sheet=null;
				if (!onesheetfords.equalsIgnoreCase("YES"))
				{
					sheet = workbook.createSheet(Keywords.SoftwareName+"_"+String.valueOf(i), i-1);
					col=0;
					row=0;
				}

				Object tempdsinfo=reportinfo.get(new Integer(i));
				@SuppressWarnings("rawtypes")
				Hashtable dsinfo=(Hashtable)tempdsinfo;
				DictionaryReader dr=(DictionaryReader)dsinfo.get(Keywords.dict);
				String[] selectedvar=(String[])dsinfo.get(Keywords.var);
				String replace=(String)dsinfo.get(Keywords.replace);
				String title=(String)dsinfo.get(Keywords.title);
				if (title==null)
					title="";

				DataReader data=new DataReader(dr);
				int totalvar=0;
				if (selectedvar==null)
					totalvar=dr.gettotalvar();
				else
					totalvar=selectedvar.length;
				int[] replacerule=new int[totalvar];
				if (replace==null)
				{
					for (int j=0; j<totalvar; j++)
					{
						replacerule[j]=1;
					}
				}
				else if (replace.equalsIgnoreCase(Keywords.replaceall))
				{
					for (int j=0; j<totalvar; j++)
					{
						replacerule[j]=1;
					}
				}
				else if (replace.equalsIgnoreCase(Keywords.replaceformat))
				{
					for (int j=0; j<totalvar; j++)
					{
						replacerule[j]=2;
					}
				}
				else if (replace.equalsIgnoreCase(Keywords.replacemissing))
				{
					for (int j=0; j<totalvar; j++)
					{
						replacerule[j]=3;
					}
				}
				else
				{
					for (int j=0; j<totalvar; j++)
					{
						replacerule[j]=1;
					}
				}
				if (!data.open(selectedvar, replacerule, true))
					return new Result(data.getmessage(), false, null);

				if (title.equalsIgnoreCase("YES"))
				{
					Label lab = new Label(col, row, dr.getdescription());
					row++;
					if (onesheetfords.equalsIgnoreCase("YES"))
						onesheet.addCell(lab);
					else
						sheet.addCell(lab);
				}
				if (selectedvar==null)
				{
					selectedvar=new String[totalvar];
					for (int j=0; j<totalvar; j++)
					{
						selectedvar[j]=dr.getvarname(j);
					}
				}

				for (int j=0; j<totalvar; j++)
				{
					String labname="";
					labname=dr.getvarlabelfromname(selectedvar[j]);
					Label lab = new Label(j, row, labname);
					if (onesheetfords.equalsIgnoreCase("YES"))
						onesheet.addCell(lab);
					else
						sheet.addCell(lab);
				}
				row++;
				while (!data.isLast())
				{
					String[] values=data.getRecord();
					if (values==null)
						return new Result(data.getmessage(), false, null);
					for (int j=0; j<values.length; j++)
					{
						if (!dr.getvarformatfromname(selectedvar[j]).toUpperCase().startsWith(Keywords.TEXTSuffix.toUpperCase()))
						{
							try
							{
								double val = Double.parseDouble(values[j]);
								if (!Double.isNaN(val))
								{
									jxl.write.Number number = new jxl.write.Number(j, row, val);
									if (onesheetfords.equalsIgnoreCase("YES"))
										onesheet.addCell(number);
									else
										sheet.addCell(number);
								}
								else
								{
									Label label = new Label(j, row, "");
									if (onesheetfords.equalsIgnoreCase("YES"))
										onesheet.addCell(label);
									else
										sheet.addCell(label);
								}
							}
							catch(NumberFormatException nfe)
							{
								Label label = new Label(j, row,values[j]);
								if (onesheetfords.equalsIgnoreCase("YES"))
									onesheet.addCell(label);
								else
									sheet.addCell(label);
							}
						}
						else
						{
							Label label = new Label(j, row,values[j]);
							if (onesheetfords.equalsIgnoreCase("YES"))
								onesheet.addCell(label);
							else
								sheet.addCell(label);
						}
					}
					row++;
				}
				data.close();
				row++;
			}
			workbook.write();
			workbook.close();
		}
		catch (Exception e)
		{
			try
			{
				workbook.close();
			}
			catch (Exception ew) {}
			return new Result("%2150%\n"+e.toString()+"<br>\n", false, null);
		}

		return new Result("%2151% ("+outreport+")<br>\n", true, null);

	}
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.outreport+"=", "outdictreport", true, 450, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.xlslayout+"=" , "setting=xlslayout", false, 451, dep, "", 1));
		return parameters;
	}
	public String[] getstepinfo()
	{
		String[] info=new String[2];
		info[0]="2152";
		info[1]="2152";
		return info;
	}
}

