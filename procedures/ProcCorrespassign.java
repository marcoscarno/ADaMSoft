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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.SortRequestedVar;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that assign each observation to a variable, using the correspondence analysys
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class ProcCorrespassign extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Correspprojection
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean novgconvert=false;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict+"c", Keywords.dict};
		String [] optionalparameters=new String[] {Keywords.replace, Keywords.ncomp, Keywords.novgconvert, Keywords.where};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}

		String replace =(String)parameters.get(Keywords.replace);
		novgconvert =(parameters.get(Keywords.novgconvert)!=null);

		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		DictionaryReader dictc = (DictionaryReader)parameters.get(Keywords.dict+"c");
		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		int numvarc=dictc.gettotalvar();
		int totalcomponent=0;
		String nameofvarsforc="";
		boolean iscorrectc=true;
		for (int i=0; i<numvarc; i++)
		{
			String tempname=dictc.getvarname(i);
			if ((tempname.toLowerCase()).startsWith("c"))
			{
				try
				{
					String[] tc=tempname.split("_");
					int rif=Integer.parseInt(tc[1]);
					if (rif>totalcomponent)
						totalcomponent=rif;
				}
				catch (Exception ec)
				{
					iscorrectc=false;
				}
			}
			else if (!tempname.equalsIgnoreCase("Type"))
			{
				if (tempname.toLowerCase().startsWith("g_"))
				{
					try
					{
						tempname=tempname.substring(2);
						nameofvarsforc=nameofvarsforc+tempname.toLowerCase()+" ";
					}
					catch (Exception ec)
					{
						iscorrectc=false;
					}
				}
				else
					iscorrectc=false;
			}
		}
		if (!iscorrectc)
			return new Result("%752%<br>\n", false, null);

		Hashtable<String, String> listofvar=dictc.getcodelabelfromname("type");
		if (listofvar.size()==0)
			return new Result("%958%<br>\n", false, null);

		String usedvarnames="";
		for (Enumeration<String> e = listofvar.keys() ; e.hasMoreElements() ;)
		{
			String temp= e.nextElement();
			if ( (!temp.equals("1")) && (!temp.equals("2")) )
				usedvarnames=usedvarnames+temp+" ";
		}
		usedvarnames=usedvarnames.trim();

		String[] activevar=usedvarnames.split(" ");

		nameofvarsforc=nameofvarsforc.trim();
		String[] groupnameforc=new String[0];
		if (!nameofvarsforc.equals(""))
			groupnameforc=nameofvarsforc.split(" ");

		String[] alldsvars=new String[dict.gettotalvar()];
		for (int i=0; i<dict.gettotalvar(); i++)
		{
			alldsvars[i]=dict.getvarname(i);
		}

		groupnameforc=SortRequestedVar.getreqsorted(groupnameforc, alldsvars);

		if (totalcomponent==0)
			return new Result("%752%<br>\n", false, null);

		String tempncomp=(String)parameters.get(Keywords.ncomp);
		if (tempncomp==null)
			tempncomp=String.valueOf(totalcomponent);

		int ncomp=0;
		try
		{
			ncomp=Integer.parseInt(tempncomp);
		}
		catch (Exception e)
		{
			return new Result("%756%<br>\n", false, null);
		}
		if (totalcomponent<ncomp)
			return new Result("%757%<br>\n", false, null);

		if (ncomp==0)
			totalcomponent=ncomp;

		String[] vartoreadinc=new String[groupnameforc.length+1+totalcomponent];
		for (int i=0; i<groupnameforc.length; i++)
		{
			vartoreadinc[i]="g_"+groupnameforc[i];
		}
		vartoreadinc[groupnameforc.length]="type";
		for (int i=0; i<totalcomponent; i++)
		{
			vartoreadinc[groupnameforc.length+1+i]="c_"+String.valueOf(i+1);
		}
		int[] replaceruleforc=new int[vartoreadinc.length];
		for (int i=0; i<vartoreadinc.length; i++)
		{
			replaceruleforc[i]=0;
		}

		DataReader datac = new DataReader(dictc);
		if (!datac.open(vartoreadinc, replaceruleforc, false))
			return new Result(datac.getmessage(), false, null);

		Hashtable<Vector<String>, double[]> eigenval=new Hashtable<Vector<String>, double[]>();
		Hashtable<Hashtable<Vector<String>, String>, double[]> eigenvec=new Hashtable<Hashtable<Vector<String>, String>, double[]>();
		String[] values =null;
		while (!datac.isLast())
		{
			values = datac.getRecord();
			Vector<String> groupval=new Vector<String>();
			if (groupnameforc.length==0)
				groupval.add(null);
			else
			{
				for (int i=0; i<groupnameforc.length; i++)
				{
					String realvgval=values[i].trim();
					if (!novgconvert)
					{
						try
						{
							double realnumvgval=Double.parseDouble(realvgval);
							if (!Double.isNaN(realnumvgval))
								realvgval=String.valueOf(realnumvgval);
						}
						catch (Exception e) {}
					}
					groupval.add(realvgval);
				}
			}
			String type=values[groupnameforc.length];
			if (type.equals("1"))
			{
				double[] compval=new double[values.length-groupnameforc.length-1];
				for (int i=groupnameforc.length+1; i<values.length; i++)
				{
					compval[i-groupnameforc.length-1]=Double.NaN;
					try
					{
						compval[i-groupnameforc.length-1]=Double.parseDouble(values[i]);
					}
					catch (Exception nfe)
					{
						compval[i-groupnameforc.length-1]=Double.NaN;
					}
				}
				eigenval.put(groupval, compval);
			}
			if ((!type.equals("1")) && (!type.equals("2")))
			{
				double[] compval=new double[values.length-groupnameforc.length-1];
				for (int i=groupnameforc.length+1; i<values.length; i++)
				{
					compval[i-groupnameforc.length-1]=Double.NaN;
					try
					{
						compval[i-groupnameforc.length-1]=Double.parseDouble(values[i]);
					}
					catch (Exception nfe)
					{
						compval[i-groupnameforc.length-1]=Double.NaN;
					}
				}
				Hashtable<Vector<String>, String> t=new Hashtable<Vector<String>, String>();
				t.put(groupval, type.toLowerCase());
				eigenvec.put(t, compval);
			}
		}
		datac.close();

		DataSetUtilities dsu=new DataSetUtilities();

		dsu.setreplace(replace);

		Hashtable<String, String> temph=new Hashtable<String, String>();

		dsu.defineolddict(dict);

		Hashtable<String, String> clname=new Hashtable<String, String>();
		for (int i=0; i<activevar.length; i++)
		{
			clname.put(activevar[i], dict.getvarlabelfromname(activevar[i]));
		}
		dsu.addnewvartoolddict("AsVar", "%1098%", Keywords.TEXTSuffix, clname, temph);
		dsu.addnewvartoolddict("Sp", "%1099%", Keywords.NUMSuffix, temph, temph);

		if (replace!=null)
		{
			if (replace.equalsIgnoreCase(Keywords.replaceall))
			{
				dsu.setempycodelabels();
				dsu.setempymissingdata();
			}
			else if (replace.equalsIgnoreCase(Keywords.replaceformat))
				dsu.setempycodelabels();
			else if (replace.equalsIgnoreCase(Keywords.replacemissing))
				dsu.setempymissingdata();
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		String keyword="Correspassign "+dict.getkeyword();
		String description="Correspassign "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		String vargroup="";
		for (int i=0; i<groupnameforc.length; i++)
		{
			vargroup=vargroup+groupnameforc[i]+" ";
		}
		vargroup=vargroup.trim();

		if (vargroup.equals(""))
			vargroup=null;

		VariableUtilities varu=new VariableUtilities(dict, vargroup, null, null, usedvarnames, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String[] reqvar=varu.getreqvar();

		int[] replacerule=varu.getreplaceruleforsel(replace);
		DataReader data = new DataReader(dict);

		if (!data.open(reqvar, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		int[] grouprule=varu.getgroupruleforsel();
		int[] rowrule=varu.getrowruleforsel();

		ValuesParser vp=new ValuesParser(null, grouprule, null, rowrule, null, null);

		Hashtable<Vector<String>, Double> total=new Hashtable<Vector<String>, Double>();
		Hashtable<Vector<String>, double[]> coltotal=new Hashtable<Vector<String>, double[]>();

		int validgroup=0;
		Vector<String> vargroupvalues=new Vector<String>();
		double[] varrowvalues=null;
		double temptotal=0;
		double[] tempcoltotal=null;
		boolean ismd=false;
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				if (novgconvert)
					vargroupvalues=vp.getorigvargroup(values);
				else
					vargroupvalues=vp.getvargroup(values);
				varrowvalues=vp.getrowvarasdouble(values);
				if (vp.vargroupisnotmissing(vargroupvalues))
				{
					if (total.get(vargroupvalues)==null)
					{
						temptotal=0;
						tempcoltotal=new double[varrowvalues.length];
						ismd=false;
						for (int i=0; i<varrowvalues.length; i++)
						{
							if (Double.isNaN(varrowvalues[i]))
								ismd=true;
							tempcoltotal[i]=varrowvalues[i];
							temptotal +=varrowvalues[i];
						}
						if (!ismd)
						{
							total.put(vargroupvalues, new Double(temptotal));
							coltotal.put(vargroupvalues, tempcoltotal);
							validgroup++;
						}
					}
					else
					{
						temptotal=(total.get(vargroupvalues)).doubleValue();
						tempcoltotal=coltotal.get(vargroupvalues);
						ismd=false;
						for (int i=0; i<varrowvalues.length; i++)
						{
							if (Double.isNaN(varrowvalues[i]))
								ismd=true;
							else
							{
								tempcoltotal[i] +=varrowvalues[i];
								temptotal +=varrowvalues[i];
							}
						}
						if (!ismd)
						{
							total.put(vargroupvalues, new Double(temptotal));
							coltotal.put(vargroupvalues, tempcoltotal);
							validgroup++;
						}
					}
				}
			}
		}
		data.close();
		if ((validgroup==0) && (where!=null))
			return new Result("%2804%<br>\n", false, null);
		if ((validgroup==0) && (where==null))
			return new Result("%666%<br>\n", false, null);

		Hashtable<Vector<String>, Hashtable<String, double[]>> varprojection=new Hashtable<Vector<String>, Hashtable<String, double[]>>();
		boolean checkgroup=true;
		double[] tempeigenval=null;
		double[] compvalue=null;
		double[] projection=null;
		for (Enumeration<Vector<String>> e = coltotal.keys() ; e.hasMoreElements() ;)
		{
			Hashtable<String, double[]> tempname=new Hashtable<String, double[]>();
			Vector<String> groupvalues= e.nextElement();
			temptotal=(total.get(groupvalues)).doubleValue();
			tempcoltotal=coltotal.get(groupvalues);
			tempeigenval=eigenval.get(groupvalues);
			if (tempeigenval==null)
				checkgroup=false;
			else
			{
				Vector<String> exgroupc=new Vector<String>();
				for (int i=0; i<groupvalues.size(); i++)
				{
					if (groupvalues.get(i)!=null)
					{
						String grouptosearch=(groupvalues.get(i)).trim();
						exgroupc.add(grouptosearch);
					}
					else
						exgroupc.add(null);
				}
				for (int i=0; i<tempcoltotal.length; i++)
				{
					Hashtable<Vector<String>, String> tt=new Hashtable<Vector<String>, String>();
					tt.put(exgroupc, activevar[i]);
					compvalue=eigenvec.get(tt);
					projection=new double[ncomp];
					for (int j=0; j<ncomp; j++)
					{
						projection[j]=Math.sqrt(temptotal/tempcoltotal[i])*Math.sqrt(tempeigenval[j])*compvalue[j];
					}
					tempname.put(activevar[i], projection);
				}
				varprojection.put(groupvalues, tempname);
			}
		}
		if (!checkgroup)
			return new Result("%960%<br>\n", false, null);

		VariableUtilities newvaru=new VariableUtilities(dict, vargroup, usedvarnames, null, null, null);
		if (newvaru.geterror())
			return new Result(varu.getmessage(), false, null);

		String[] totalvar=newvaru.getallvar();

		int[] neworder=SortRequestedVar.getreqorder(newvaru.getanalysisvar(), totalvar);

		activevar=SortRequestedVar.getreqsorted(newvaru.getanalysisvar(), totalvar);

		replacerule=newvaru.getreplaceruleforall(replace);

		data = new DataReader(dict);
		if (!data.open(totalvar, replacerule, false))
			return new Result(data.getmessage(), false, null);

		int[] allvarstype=newvaru.getnormalruleforall();

		ValuesParser newvp=new ValuesParser(allvarstype, null, null, null, null, null);

		Vector<String> exgroupc=new Vector<String>();
		String grouptosearch=null;
		double sumrow=0;
		Hashtable<Vector<String>, String> tt=new Hashtable<Vector<String>, String>();
		double maxdist=Double.NaN;
		Hashtable<String, double[]> varp=new Hashtable<String, double[]>();
		double[] tempp=null;
		double tempd=0;
		double sumrsq=0;
		double sumcsq=0;
		String[] wvalues=null;
		String[] newvalues=new String[2];
		while (!data.isLast())
		{
			newvalues[0]="";
			newvalues[1]="";
			values = data.getRecord();
			if (novgconvert)
				vargroupvalues=vp.getorigvargroup(values);
			else
				vargroupvalues=vp.getvargroup(values);
			varrowvalues=newvp.getanalysisvarasdouble(values);
			try
			{
				if (newvp.vargroupisnotmissing(vargroupvalues))
				{
					temptotal=(total.get(vargroupvalues)).doubleValue();
					tempcoltotal=coltotal.get(vargroupvalues);
					exgroupc.clear();
					for (int i=0; i<vargroupvalues.size(); i++)
					{
						if (vargroupvalues.get(i)!=null)
						{
							grouptosearch=(vargroupvalues.get(i)).trim();
							newvalues[i]=grouptosearch;
							exgroupc.add(grouptosearch);
						}
						else
							exgroupc.add(null);
					}
					sumrow=0;
					ismd=false;
					for (int i=0; i<varrowvalues.length; i++)
					{
						sumrow +=varrowvalues[i];
						if (Double.isNaN(varrowvalues[i]))
							ismd=true;
					}
					if (!ismd)
					{
						for (int i=0; i<varrowvalues.length; i++)
						{
							varrowvalues[i]=(varrowvalues[i]/sumrow)*Math.sqrt(temptotal/tempcoltotal[neworder[i]]);
						}
						projection=new double[ncomp];
						for (int i=0; i<ncomp; i++)
						{
							projection[i]=0;
						}
						for (int i=0; i<tempcoltotal.length; i++)
						{
							tt.clear();
							tt.put(exgroupc, activevar[i]);
							compvalue=eigenvec.get(tt);
							for (int j=0; j<ncomp; j++)
							{
								projection[j] +=varrowvalues[i]*compvalue[j];
							}
						}
						maxdist=Double.NaN;
						varp=varprojection.get(vargroupvalues);
						for (Enumeration<String> e = varp.keys() ; e.hasMoreElements() ;)
						{
							String temp=e.nextElement();
							tempp=varp.get(temp);
							tempd=0;
							sumrsq=0;
							sumcsq=0;
							for (int i=0; i<tempp.length; i++)
							{
								if ((!Double.isNaN(tempp[i])) && (!Double.isNaN(projection[i])))
								{
									sumrsq +=Math.pow(projection[i],2);
									sumcsq +=Math.pow(tempp[i],2);
									tempd +=tempp[i]*projection[i];
								}
							}
							tempd=tempd/(Math.sqrt(sumrsq)*Math.sqrt(sumcsq));
							if (Double.isNaN(maxdist))
								maxdist=tempd;
							if (tempd>=maxdist)
							{
								newvalues[0]=temp;
								newvalues[1]=double2String(tempd);
								maxdist=tempd;
							}
						}
					}
				}
			}
			catch(Exception enumber) {}
			wvalues=dsu.getnewvalues(values, newvalues);
			dw.write(wvalues);
		}
		data.close();

		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
		Hashtable<String, String> datatableinfo=dw.getTableInfo();
		Vector<StepResult> result = new Vector<StepResult>();
		result.add(new LocalDictionaryWriter(dw.getdictpath(), keyword, description, author, dw.gettabletype(),
		datatableinfo, dsu.getfinalvarinfo(), tablevariableinfo, dsu.getfinalcl(), dsu.getfinalmd(), null));
		return new Result("", true, result);
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 721, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"c=", "dict", true, 952, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 3404, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.ncomp, "text", false, 954, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.novgconvert, "checkbox", false, 2227, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2809,dep,"",2));
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
		retprocinfo[0]="950";
		retprocinfo[1]="961";
		return retprocinfo;
	}
}
