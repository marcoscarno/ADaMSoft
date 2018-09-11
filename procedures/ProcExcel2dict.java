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

import java.io.File;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import jxl.Cell;
import jxl.CellType;
import jxl.Sheet;
import jxl.Workbook;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.CheckVarNames;
import ADaMSoft.utilities.StepUtilities;

/**
* This procedure creates a dictionary for an Excel data sheet
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcExcel2dict implements RunStep
{
	/**
	* Creates a dictionary for an Excel sheet
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.xlsfile, Keywords.outdict};
		String [] optionalparameters=new String[] {Keywords.sheet, Keywords.rowlabel, Keywords.firstrow, Keywords.lastrow,
		Keywords.firstcol, Keywords.lastcol, Keywords.uselabelasvarname};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}

		String xlsfile    = (String) parameters.get(Keywords.xlsfile);
		String path       = (String) parameters.get(Keywords.outdict);
		String sheet      = (String) parameters.get(Keywords.sheet);
		String firstrow   = (String) parameters.get(Keywords.firstrow);
		String lastrow    = (String) parameters.get(Keywords.lastrow);
		String rowlabel   = (String) parameters.get(Keywords.rowlabel);
		String firstcol   = (String) parameters.get(Keywords.firstcol);
		String lastcol    = (String) parameters.get(Keywords.lastcol);
		boolean uselabelasvarname =(parameters.get(Keywords.uselabelasvarname)!=null);
		if ((rowlabel==null) && (uselabelasvarname))
			return new Result("%1594%<br>\n", false, null);

		if (sheet==null)
			sheet="0";
		Workbook workbook = null;
		Sheet sheetfile  = null;
		int indsheet = -1;
		try
		{
			java.net.URL fileUrl;
			if((xlsfile.toLowerCase()).startsWith("http"))
				fileUrl =  new java.net.URL(xlsfile);
			else
			{
				File Excelfile=new File(xlsfile);
				fileUrl = Excelfile.toURI().toURL();
			}
			workbook = Workbook.getWorkbook(fileUrl.openStream());
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
				return new Result("%254%<br>\n", false, null);
			}
		}
		catch(Exception ex)
		{
			return new Result("%254% "+ex.toString()+"<br>\n", false, null);
		}
		int firstrownumber=0;
		int lastrownumber =-1;
		int rowlabelnumber=-1;
		int firstcolnumber=0;
		int lastcolnumber =-1;
		int numvar=0;
		int numrec=0;
		try
		{
			numvar=sheetfile.getColumns();
		}
		catch (Exception e)
		{
			return new Result("%549%<br>\n", false, null);
		}
		Hashtable<String, String> datatableinfo=new Hashtable<String, String>();
		datatableinfo.put(Keywords.DATA.toLowerCase(), xlsfile);
		datatableinfo.put(Keywords.sheet.toLowerCase(), sheet);
		if (rowlabel!=null)
		{
			try
			{
				rowlabelnumber = (new Integer(rowlabel)).intValue();
				datatableinfo.put(Keywords.rowlabel.toLowerCase(), String.valueOf(rowlabelnumber));
			}
			catch (Exception e){}
		}
		if (firstrow!=null)
		{
			try
			{
				firstrownumber = (new Integer(firstrow)).intValue();
				datatableinfo.put(Keywords.firstrow.toLowerCase(), String.valueOf(firstrownumber));
			}
			catch (Exception e)
			{
				if (rowlabelnumber>-1)
				{
					firstrownumber=rowlabelnumber+1;
					datatableinfo.put(Keywords.firstrow.toLowerCase(), String.valueOf(rowlabelnumber+1));
				}
				else
					datatableinfo.put(Keywords.firstrow.toLowerCase(), "0");
			}
		}
		else
		{
			if (rowlabelnumber>-1)
				firstrownumber=rowlabelnumber+1;
		}
		if (firstcol!=null)
		{
			try
			{
				firstcolnumber = Integer.parseInt(firstcol);
				datatableinfo.put(Keywords.firstcol.toLowerCase(), String.valueOf(firstcolnumber));
			}
			catch (Exception e)
			{
				datatableinfo.put(Keywords.firstcol.toLowerCase(), String.valueOf(0));
			}
		}

		if (lastcol!=null)
		{
			try
			{
				lastcolnumber = Integer.parseInt(lastcol);
				lastcolnumber=lastcolnumber+1;
				datatableinfo.put(Keywords.lastcol.toLowerCase(), String.valueOf(lastcolnumber+1));
			}
			catch (Exception e)
			{
				lastcolnumber=numvar;
				datatableinfo.put(Keywords.lastcol.toLowerCase(), String.valueOf(numvar));
			}
		}
		else
		{
			lastcolnumber=numvar;
			datatableinfo.put(Keywords.lastcol.toLowerCase(), String.valueOf(numvar));
		}
		int totalvar=lastcolnumber-firstcolnumber;
		try
		{
			for (int i=0; i<totalvar; i++)
			{
				Cell[] record=sheetfile.getColumn(i+firstcolnumber);
				if (record.length>numrec)
					numrec=record.length;
			}
		}
		catch (Exception e)
		{
			return new Result("%549%<br>\n", false, null);
		}

		if (lastrow!=null)
		{
			try
			{
				lastrownumber = (new Integer(lastrow)).intValue();
				datatableinfo.put(Keywords.lastrow.toLowerCase(), String.valueOf(lastrownumber));
			}
			catch (Exception e)
			{
				lastrownumber = numrec;
				datatableinfo.put(Keywords.lastrow.toLowerCase(), String.valueOf(numrec));
			}
		}
		else
		{
			lastrownumber = numrec;
			datatableinfo.put(Keywords.lastrow.toLowerCase(), String.valueOf(numrec));
		}
		if (firstrownumber>=lastrownumber)
		{
			try
			{
				workbook.close();
			}
			catch (Exception e) {}
			return new Result("%550%<br>\n", false, null);
		}
		if (rowlabelnumber>=firstrownumber)
		{
			try
			{
				workbook.close();
			}
			catch (Exception e) {}
			return new Result("%551%<br>\n", false, null);
		}
		if (firstcolnumber>=lastcolnumber)
		{
			try
			{
				workbook.close();
			}
			catch (Exception e) {}
			return new Result("%552%<br>\n", false, null);
		}
		String[] varname=new String[totalvar];
		String[] varlabel=new String[totalvar];
		String[] varformat=new String[totalvar];
		String[] varposition=new String[totalvar];
		Hashtable<String, String> testvn=new Hashtable<String, String>();
		for (int i=0; i<totalvar; i++)
		{
			varname[i]="v"+(String.valueOf(i));
			varposition[i]=(String.valueOf(i+firstcolnumber));
			varformat[i]="NUM";
			varlabel[i]=varname[i];
		}
		boolean vexist=false;
		try
		{
			if (rowlabelnumber>-1)
			{
				Cell[] testlabel=sheetfile.getRow(rowlabelnumber);
				if ((testlabel.length+firstcolnumber-lastcolnumber)<0)
				{
					try
					{
						workbook.close();
					}
					catch (Exception e) {}
					return new Result("%553%\n", false, null);
				}
				int pointer=0;
				for (int i=firstcolnumber; i<lastcolnumber; i++)
				{
					if (uselabelasvarname)
					{
						String tempvname=testlabel[i].getContents();
						String[] tempvnames=tempvname.split(" ");
						if (tempvnames.length>0)
						{
							tempvname="";
							for (int j=0; j<tempvnames.length; j++)
							{
								tempvname=tempvname+tempvnames[j];
							}
						}
						if (testvn.size()>0)
						{
							if (testvn.get(tempvname)!=null)
								vexist=true;
						}
						testvn.put(tempvname, "");
						varname[pointer]=tempvname;
					}
					varlabel[pointer]=testlabel[i].getContents();
					pointer++;
				}
			}
			for (int i=0; i<totalvar; i++)
			{
				Cell[] record=sheetfile.getColumn(i+firstcolnumber);
				int lastobs=lastrownumber;
				if (lastobs>record.length)
					lastobs=record.length;
				for (int j=(rowlabel!=null?1:0); j<lastobs; j++)
				{
					if(!(record[j].getType()==CellType.NUMBER || record[j].getType()==CellType.NUMBER_FORMULA) && !record[j].getContents().equals(""))
					{
						varformat[i]="TEXT";
						break;
					}
				}
			}
		}
		catch (Exception ex)
		{
			return new Result("%554 "+ex.toString()+"%<br>\n", false, null);
		}
		try
		{
			workbook.close();
		}
		catch (Exception e) {}
		if (vexist)
			return new Result("%1595%<br>\n", false, null);

		String keyword=xlsfile;
		String description=xlsfile;
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());
		String datatabletype=Keywords.xlsfile;

		Vector<Hashtable<String, String>> fixedvariableinfo=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> tablevariableinfo=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> codelabel=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> missingdata=new Vector<Hashtable<String, String>>();
		for (int i=0; i<totalvar; i++)
		{
			varlabel[i]=varlabel[i].trim();
			if (varlabel[i].equals(""))
				return new Result("%2647%<br>\n", false, null);
		}
		for (int i=0; i<totalvar-1; i++)
		{
			for (int j=i+1; j<totalvar; j++)
			{
				if (varlabel[i].equalsIgnoreCase(varlabel[j]))
					return new Result("%2648% ("+varlabel[i]+")<br>\n", false, null);
			}
		}

		for (int i=0; i<totalvar; i++)
		{
			Hashtable<String, String> tempfixedvariableinfo=new Hashtable<String, String>();
			Hashtable<String, String> temptablevariableinfo=new Hashtable<String, String>();
			Hashtable<String, String> tempcodelabel=new Hashtable<String, String>();
			Hashtable<String, String> tempmissingdata=new Hashtable<String, String>();
			if (varname[i].startsWith("0")) varname[i]="_"+varname[i];
			if (varname[i].startsWith("1")) varname[i]="_"+varname[i];
			if (varname[i].startsWith("2")) varname[i]="_"+varname[i];
			if (varname[i].startsWith("3")) varname[i]="_"+varname[i];
			if (varname[i].startsWith("4")) varname[i]="_"+varname[i];
			if (varname[i].startsWith("5")) varname[i]="_"+varname[i];
			if (varname[i].startsWith("6")) varname[i]="_"+varname[i];
			if (varname[i].startsWith("7")) varname[i]="_"+varname[i];
			if (varname[i].startsWith("8")) varname[i]="_"+varname[i];
			if (varname[i].startsWith("9")) varname[i]="_"+varname[i];
				try
				{
					varname[i]=varname[i].replaceAll(" ","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll("#","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll("\\*","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll("\\.","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll("\\+","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll("-","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll("&","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll("%","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll("\\|","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll("!","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll("\\$","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll("/","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll("\\(","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll("\\)","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll("=","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll("\\?","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll("<","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll(">","_");
				}
				catch(Exception ename){}
			tempfixedvariableinfo.put(Keywords.VariableName.toLowerCase(),varname[i]);
			tempfixedvariableinfo.put(Keywords.VariableFormat.toLowerCase(),varformat[i]);
			tempfixedvariableinfo.put(Keywords.LabelOfVariable.toLowerCase(),varlabel[i]);
			temptablevariableinfo.put(Keywords.ExcelVariableColumn.toLowerCase(),varposition[i]);
			fixedvariableinfo.add(tempfixedvariableinfo);
			tablevariableinfo.add(temptablevariableinfo);
			codelabel.add(tempcodelabel);
			missingdata.add(tempmissingdata);
		}

		String workdir=(String)parameters.get(Keywords.WorkDir);
		if (!CheckVarNames.getResultCheck(fixedvariableinfo, workdir).equals(""))
			return new Result(CheckVarNames.getResultCheck(fixedvariableinfo, workdir), false, null);

		Vector<StepResult> result = new Vector<StepResult>();
		result.add(new LocalDictionaryWriter(path, keyword, description, author, datatabletype,
		datatableinfo, fixedvariableinfo, tablevariableinfo, codelabel, missingdata, null));
		return new Result("", true, result);
	}
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.outdict+"=", "outdictreport", true, 249, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.xlsfile, "file=xls", true, 555, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.sheet, "text", false, 556, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.rowlabel, "text", false, 557, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 563, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.firstrow,"text",false,558,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.lastrow,"text",false,559,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.firstcol,"text",false,560,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.lastcol,"text",false,561,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 564, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.uselabelasvarname, "checkbox", false, 1592, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 1593, dep, "", 2));
		return parameters;
	}
	public String[] getstepinfo()
	{
		String[] retstepinfo=new String[2];
		retstepinfo[0]="247";
		retstepinfo[1]="562";
		return retstepinfo;
	}
}
