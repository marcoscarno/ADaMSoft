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
import ADaMSoft.utilities.VectorStringComparatorNoC;

import ADaMSoft.keywords.Keywords;

import java.util.Hashtable;
import java.util.TreeMap;
import java.util.LinkedList;
import java.util.Vector;
import java.util.Iterator;

import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

/**
* This is the procedure that create the data set with the totals ready to be used by the calibration procedures
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class ProcCreatetotalds implements RunStep
{
	/**
	* Starts the execution of Proc Createtotalds and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.dict, Keywords.varaux, Keywords.filename};
		String [] optionalparameters=new String[] {Keywords.vargroup, Keywords.weight, Keywords.dichotomous, Keywords.tablewithfreq, Keywords.where};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		boolean dichotomous=(parameters.get(Keywords.dichotomous)!=null);
		boolean tablewithfreq=(parameters.get(Keywords.tablewithfreq)!=null);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String vartemp=(String)parameters.get(Keywords.varaux.toLowerCase());
		String weight=(String)parameters.get(Keywords.weight.toLowerCase());
		String filename=(String)parameters.get(Keywords.filename.toLowerCase());
		String tempvargroup=(String)parameters.get(Keywords.vargroup.toLowerCase());

		String[] varg=new String[0];
		if (tempvargroup!=null) varg=tempvargroup.split(" ");
		String[] varw=new String[0];
		if (weight!=null) varw=weight.split(" ");
		String[] varaux=vartemp.split(" ");
		if (varw.length>1) return new Result("%595%<br>\n", false, null);
		String[] totalvars=new String[varg.length+varaux.length+varw.length];
		for (int i=0; i<varg.length; i++)
		{
			totalvars[i]=varg[i];
		}
		for (int i=0; i<varaux.length; i++)
		{
			totalvars[i+varg.length]=varaux[i];
		}
		for (int i=0; i<varw.length; i++)
		{
			totalvars[i+varg.length+varaux.length]=varw[i];
		}
		int[] replacerule=new int[totalvars.length];
		for (int i=0; i<totalvars.length; i++)
		{
			replacerule[i]=1;
		}
		String vn="";
		boolean fv=false;
		for (int i=0; i<totalvars.length; i++)
		{
			fv=false;
			for (int j=0; j<dict.gettotalvar(); j++)
			{
				if (totalvars[i].equalsIgnoreCase(dict.getvarname(j))) fv=true;
			}
			if (!fv) vn=vn+totalvars[i]+" ";
		}
		if (!vn.equals("")) return new Result("%1632% ("+vn.trim()+")<br>\n", false, null);

		if (!filename.toLowerCase().endsWith(".xls"))
			filename=filename+".xls";
		boolean exist=(new File(filename)).exists();
		if (exist)
		{
			boolean success = (new File(filename)).delete();
			if (!success)
				return new Result("%2882%<br>\n", false, null);
		}
		String tempfile=(String)parameters.get(Keywords.WorkDir);
		tempfile=tempfile+"temp.xls";
		tempfile=filename;

		DataReader data = new DataReader(dict);
		if (!data.open(totalvars, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		TreeMap<Vector<String>, Vector<TreeMap<Vector<String>, Double>>> totfreq=new TreeMap<Vector<String>, Vector<TreeMap<Vector<String>, Double>>>(new VectorStringComparatorNoC());

		String[][] values=null;
		double weightvalue=Double.NaN;
		double tw=0;
		int done=0;
		boolean errordic=false;
		double testdyc=0;
		boolean checkt=false;
		while (!data.isLast())
		{
			values = data.getOriginalTransformedRecord();
			if (values!=null)
			{
				Vector<String> vg=new Vector<String>();
				if (varg.length>0)
				{
					for (int i=0; i<varg.length; i++)
					{
						vg.add(values[i][0]);
						vg.add(values[i][1]);
					}
				}
				else vg.add(null);
				if (varw.length>0) weightvalue=Double.parseDouble(values[totalvars.length-1][0]);
				else weightvalue=1;
				done++;
				if (totfreq.get(vg)==null)
				{
					Vector<TreeMap<Vector<String>, Double>> temptf=new Vector<TreeMap<Vector<String>, Double>>();
					for (int i=0; i<varaux.length; i++)
					{
						TreeMap<Vector<String>, Double> temptff=new TreeMap<Vector<String>, Double>(new VectorStringComparatorNoC());
						Vector<String> temptfff=new Vector<String>();
						if (dichotomous && tablewithfreq)
						{
							testdyc=Double.NaN;
							try
							{
								testdyc=Double.parseDouble(values[i+varg.length][0]);
							}
							catch (Exception edic) {}
							if (testdyc!=0.0)
							{
								temptfff.add("1.0");
								temptfff.add("1.0");
							}
							else
							{
								temptfff.add("0.0");
								temptfff.add("0.0");
							}
						}
						else
						{
							temptfff.add(values[i+varg.length][0]);
							temptfff.add(values[i+varg.length][1]);
						}
						if (dichotomous)
						{
							checkt=false;
							try
							{
								testdyc=Double.parseDouble(values[i+varg.length][0]);
								if (!tablewithfreq)
								{
									if (testdyc==1.0) checkt=true;
									if (testdyc==0.0) checkt=true;
									temptff.put(temptfff, new Double(weightvalue));
								}
								else
								{
									temptff.put(temptfff, new Double(weightvalue*testdyc));
									checkt=true;
								}
							}
							catch (Exception edic) {}
							if (!checkt) errordic=true;
						}
						else
						{
							temptff.put(temptfff, new Double(weightvalue));
						}
						temptf.add(temptff);
					}
					totfreq.put(vg, temptf);
				}
				else
				{
					Vector<TreeMap<Vector<String>, Double>> temptf=totfreq.get(vg);
					for (int i=0; i<varaux.length; i++)
					{
						TreeMap<Vector<String>, Double> temptff=temptf.get(i);
						Vector<String> temptfff=new Vector<String>();
						if (dichotomous && tablewithfreq)
						{
							testdyc=Double.NaN;
							try
							{
								testdyc=Double.parseDouble(values[i+varg.length][0]);
							}
							catch (Exception edic) {}
							if (testdyc!=0.0)
							{
								temptfff.add("1.0");
								temptfff.add("1.0");
							}
							else
							{
								temptfff.add("0.0");
								temptfff.add("0.0");
							}
						}
						else
						{
							temptfff.add(values[i+varg.length][0]);
							temptfff.add(values[i+varg.length][1]);
						}
						if (temptff.get(temptfff)==null)
						{
							if (dichotomous)
							{
								checkt=false;
								try
								{
									testdyc=Double.parseDouble(values[i+varg.length][0]);
									if (!tablewithfreq)
									{
										if (testdyc==1.0) checkt=true;
										if (testdyc==0.0) checkt=true;
										temptff.put(temptfff, new Double(weightvalue));
									}
									else
									{
										temptff.put(temptfff, new Double(weightvalue*testdyc));
										checkt=true;
									}
								}
								catch (Exception edic) {}
								if (!checkt) errordic=true;
							}
							else
							{
								temptff.put(temptfff, new Double(weightvalue));
							}
							temptf.set(i, temptff);
						}
						else
						{
							tw=(temptff.get(temptfff)).doubleValue();
							if (dichotomous)
							{
								checkt=false;
								try
								{
									testdyc=Double.parseDouble(values[i+varg.length][0]);
									if (!tablewithfreq)
									{
										if (testdyc==1.0) checkt=true;
										if (testdyc==0.0) checkt=true;
										temptff.put(temptfff, new Double(tw+weightvalue));
									}
									else
									{
										temptff.put(temptfff, new Double(tw+weightvalue*testdyc));
										checkt=true;
									}
								}
								catch (Exception edic) {}
								if (!checkt) errordic=true;
							}
							else
							{
								temptff.put(temptfff, new Double(tw+weightvalue));
							}
							temptf.set(i, temptff);
						}
					}
					totfreq.put(vg, temptf);
				}
			}
		}
		data.close();
		if (done==0)
		{
			if (where!=null)
				return new Result("%2804%<br>\n", false, null);
			if (where==null)
				return new Result("%666%<br>\n", false, null);
		}
		if (errordic)
		{
			return new Result("%3438%<br>\n", false, null);
		}
		String ct="";
		try
		{
			WritableWorkbook workbook=Workbook.createWorkbook(new File(tempfile));
			WritableSheet sheet=workbook.createSheet("Sample for totals", 0);
			int row=0;
			int col=0;
			String labname="";
			for (int j=0; j<varg.length; j++)
			{
				labname=varg[j];
				Label lab1 = new Label(col, row, labname);
				sheet.addCell(lab1);
				col++;
				labname=dict.getvarlabelfromname(varg[j]);
				Label lab2 = new Label(col, row, labname);
				col++;
				sheet.addCell(lab2);
			}
			if (!dichotomous)
			{
				Label lab3 = new Label(col, row, "Varname");
				sheet.addCell(lab3);
				Label lab4 = new Label(col+1, row, "Varlabel");
				sheet.addCell(lab4);
				Label lab5 = new Label(col+2, row, "Valuecode");
				sheet.addCell(lab5);
				Label lab6 = new Label(col+3, row, "Valuelabel");
				sheet.addCell(lab6);
				Label lab7 = new Label(col+4, row, "Total");
				sheet.addCell(lab7);
			}
			else
			{
				Label lab3 = new Label(col, row, "Varname");
				sheet.addCell(lab3);
				Label lab4 = new Label(col+1, row, "Varlabel");
				sheet.addCell(lab4);
				Label lab7 = new Label(col+2, row, "Total");
				sheet.addCell(lab7);
			}
			row++;
			Iterator<Vector<String>> it = totfreq.keySet().iterator();
			col=0;
			while (it.hasNext())
			{
				Vector<String> temptf = it.next();
				Vector<TreeMap<Vector<String>, Double>> temptff=totfreq.get(temptf);
				for (int i=0; i<temptff.size(); i++)
				{
					TreeMap<Vector<String>, Double> temptfff=temptff.get(i);
					Iterator<Vector<String>> sit = temptfff.keySet().iterator();
					while (sit.hasNext())
					{
						col=0;
						if (varg.length>0)
						{
							for (int j=0; j<temptf.size(); j++)
							{
								Label actuallab= new Label(j, row, temptf.get(j));
								sheet.addCell(actuallab);
								col++;
							}
						}
						Vector<String> temptffff = sit.next();
						tw=(temptfff.get(temptffff)).doubleValue();
						Label actuallab1= new Label(col, row, varaux[i]);
						sheet.addCell(actuallab1);
						Label actuallab2= new Label(col+1, row, dict.getvarlabelfromname(varaux[i]));
						sheet.addCell(actuallab2);
						if (!dichotomous)
						{
							Label actuallab3= new Label(col+2, row, temptffff.get(0));
							sheet.addCell(actuallab3);
							Label actuallab4= new Label(col+3, row, temptffff.get(1));
							sheet.addCell(actuallab4);
							jxl.write.Number number = new jxl.write.Number(col+4, row, tw);
							sheet.addCell(number);
							row++;
						}
						else
						{
							ct=temptffff.get(0);
							testdyc=Double.NaN;
							try
							{
								testdyc=Double.parseDouble(ct);
							}
							catch (Exception edc) {}
							if (testdyc==1.0)
							{
								jxl.write.Number number = new jxl.write.Number(col+2, row, tw);
								sheet.addCell(number);
								row++;
							}
						}
					}
					col++;
				}
			}
			workbook.write();
			workbook.close();
		}
		catch (Exception e)
		{
			return new Result("%3435%<br>\n"+e.toString()+"<br>\n", false, null);
		}

		return new Result("%3436% ("+filename+")<br>\n", true, null);
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 3432, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.varaux, "vars=all", true, 3433, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.filename, "filesave=.xls", true, 3434, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.weight, "vars=all", false, 656, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.dichotomous, "checkbox", false, 3437, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.tablewithfreq, "checkbox", false, 1577, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="1560";
		retprocinfo[1]="3431";
		return retprocinfo;
	}
}
