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

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Vector;

import ADaMSoft.algorithms.CovariancesEvaluator;
import ADaMSoft.algorithms.clusters.EvaluateDistance;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.SortRequestedVar;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that replace a missing data value with the value of the seed that is near to the unit
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcMdfuzzykmeans extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Mdfuzzykmeans
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean novgconvert=false;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict+"c", Keywords.dict, Keywords.fuzzycoeff};
		String [] optionalparameters=new String[] {Keywords.weight, Keywords.where, Keywords.replace, Keywords.novgconvert};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		boolean wwp=(parameters.get(Keywords.weight)!=null);
		String replace =(String)parameters.get(Keywords.replace);
		String fuzzycoeff=(String)parameters.get(Keywords.fuzzycoeff.toLowerCase());
		double fcoeff=0;
		try
		{
			fcoeff=Double.parseDouble(fuzzycoeff);
		}
		catch (NumberFormatException en) {}
		if (fcoeff<1)
			return new Result("%932%\n", false, null);

		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);
		novgconvert =(parameters.get(Keywords.novgconvert)!=null);

		DictionaryReader dictc = (DictionaryReader)parameters.get(Keywords.dict+"c");
		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		Hashtable<String, String> tinfo=dictc.getdatatableinfo();
		boolean distancek=false;
		String distanceType=null;
		for (Enumeration<String> e = tinfo.keys() ; e.hasMoreElements() ;)
		{
			String par = (String) e.nextElement();
			String val = tinfo.get(par);
			if (par.equalsIgnoreCase(Keywords.Distance.toLowerCase()))
			{
				distancek=true;
				distanceType=val;
			}
		}
		if (!distancek)
			return new Result("%1136%<br>\n", false, null);

		String[] dtype=new String[] {Keywords.EuclideanDistance, Keywords.SquaredEuclideanDistance, Keywords.ManhattanDistance, Keywords.ChebyshevDistance, Keywords.MahalanobisDistance};
		int vdt=steputilities.CheckOption(dtype, distanceType);
		if (vdt==0)
			return new Result("%1775% "+Keywords.Distance.toUpperCase()+"<br>\n"+steputilities.getMessage(), false, null);

		EvaluateDistance ed=new EvaluateDistance(vdt);

		int numvarc=dictc.gettotalvar();
		String groupname="";
		String varlist="";
		boolean iscorrectc=true;
		for (int i=0; i<numvarc; i++)
		{
			String tempname=dictc.getvarname(i);
			if ((tempname.toLowerCase()).startsWith("g_"))
			{
				try
				{
					String tc=tempname.substring(2);
					groupname=groupname+" "+tc;
				}
				catch (Exception ec)
				{
					iscorrectc=false;
				}
			}
			if (tempname.toLowerCase().startsWith("v_"))
			{
				try
				{
					String tc=tempname.substring(2);
					varlist=varlist+" "+tc;
				}
				catch (Exception ec)
				{
					iscorrectc=false;
				}
			}
		}
		if (!iscorrectc)
			return new Result("%881%<br>\n", false, null);

		groupname=groupname.trim();
		varlist=varlist.trim();

		String[] groupvar=new String[0];
		if (!groupname.equals(""))
			groupvar=groupname.split(" ");

		String[] alldsvars=new String[dict.gettotalvar()];
		for (int i=0; i<dict.gettotalvar(); i++)
		{
			alldsvars[i]=dict.getvarname(i);
		}
		groupvar=SortRequestedVar.getreqsorted(groupvar, alldsvars);

		String[] usedvar=varlist.split(" ");
		usedvar=SortRequestedVar.getreqsorted(usedvar, alldsvars);

		String[] vartoreadinc=new String[groupvar.length+1+usedvar.length];
		for (int i=0; i<groupvar.length; i++)
		{
			vartoreadinc[i]="g_"+groupvar[i];
		}
		vartoreadinc[groupvar.length]="group";
		for (int i=0; i<usedvar.length; i++)
		{
			vartoreadinc[groupvar.length+1+i]="v_"+usedvar[i];
		}
		int[] replaceruleforc=new int[vartoreadinc.length];
		for (int i=0; i<vartoreadinc.length; i++)
		{
			replaceruleforc[i]=0;
		}

		DataReader datac = new DataReader(dictc);
		if (!datac.open(vartoreadinc, replaceruleforc, false))
			return new Result(datac.getmessage(), false, null);

		Hashtable<Vector<String>, Vector<Hashtable<String, Double>>> seeds=new Hashtable<Vector<String>, Vector<Hashtable<String, Double>>>();

		boolean errorinseeds=false;

		int totalgroup=0;

		String[] values=null;
		String groupnum="";
		int rifgroup=-1;
		while (!datac.isLast())
		{
			values = datac.getRecord();
			Vector<String> groupval=new Vector<String>();
			if (groupvar.length==0)
				groupval.add(null);
			else
			{
				for (int i=0; i<groupvar.length; i++)
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
			groupnum=values[groupvar.length];
			rifgroup=-1;
			try
			{
				rifgroup=Integer.parseInt(groupnum);
				if (rifgroup>totalgroup)
					totalgroup=rifgroup;
			}
			catch (Exception e)
			{
				errorinseeds=true;
			}
			double[] seedsval=new double[values.length-groupvar.length-1];
			for (int i=groupvar.length+1; i<values.length; i++)
			{
				try
				{
					seedsval[i-groupvar.length-1]=Double.parseDouble(values[i]);
				}
				catch (Exception nfe)
				{
					errorinseeds=true;
				}
			}
			Hashtable<String, Double> tempnameval=new Hashtable<String, Double>();
			for (int v=0; v<seedsval.length; v++)
			{
				tempnameval.put(usedvar[v], new Double(seedsval[v]));
			}
			Vector<Hashtable<String, Double>> tempseed=seeds.get(groupval);
			if (tempseed==null)
				tempseed=new Vector<Hashtable<String, Double>>();
			tempseed.add(tempnameval);
			seeds.put(groupval, tempseed);
		}
		datac.close();
		if (errorinseeds)
			return new Result("%881%<br>\n", false, null);

		String keyword="MdFuzzyKMeans "+dict.getkeyword();
		String description="MdFuzzyKMeans "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		DataSetUtilities dsu=new DataSetUtilities();

		dsu.setreplace(replace);

		totalgroup++;

		dsu.defineolddict(dict);

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

		if (groupname.equals(""))
			groupname=null;

		VariableUtilities varu=new VariableUtilities(dict, groupname, varlist, null, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String[] totalvar=varu.getallvar();

		int[] replacerule=varu.getreplaceruleforall(replace);

		DataReader data = new DataReader(dict);
		if (!data.open(totalvar, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		int[] allvarstype=varu.getnormalruleforall();

		ValuesParser vp=new ValuesParser(allvarstype, null, null, null, null, null);

		CovariancesEvaluator cove=new CovariancesEvaluator(true, true);

		Vector<String> vargroupvalues=new Vector<String>();

		if (vdt==5)
		{
			while (!data.isLast())
			{
				values = data.getRecord();
				if (values!=null)
				{
					if (novgconvert)
						vargroupvalues=vp.getorigvargroup(values);
					else
						vargroupvalues=vp.getvargroup(values);
					double[] realval=vp.getanalysisvarasdouble(values);
					boolean ismd=false;
					for (int i=0; i<realval.length; i++)
					{
						if (Double.isNaN(realval[i]))
							ismd=true;
					}
					if ((vp.vargroupisnotmissing(vargroupvalues)) && (!ismd))
					{
						cove.setValue(vargroupvalues, realval, realval, 1);
					}
				}
			}
			data.close();
			cove.calculate();
			ed.setweights(cove.getresult());
			if (!data.open(totalvar, replacerule, false))
				return new Result(data.getmessage(), false, null);
		}

		boolean noterror=true;
		double[] realval=null;
		int nummd=0;
		double[] sumu=null;
		double[] valseeds=null;
		double min=0;
		int pointermin=-1;
		double[] possibilities=null;
		String[] newval=null;
		double testmd=Double.NaN;
		double vals=0;
		double sumvals=0;
		while ((!data.isLast()) && (noterror))
		{
			values = data.getRecord();
			if (values!=null)
			{
				if (novgconvert)
					vargroupvalues=vp.getorigvargroup(values);
				else
					vargroupvalues=vp.getvargroup(values);
				realval=vp.getanalysisvarasdouble(values);
				nummd=0;
				for (int i=0; i<realval.length; i++)
				{
					if (Double.isNaN(realval[i]))
						nummd++;
				}
				if ((nummd==0) || (nummd==realval.length))
					dw.write(values);
				else
				{
					Vector<Hashtable<String, Double>> tempseed=seeds.get(vargroupvalues);
					if (tempseed==null)
						noterror=false;
					else
					{
						int nseeds=tempseed.size();
						sumu = new double[nseeds];
						for (int i=0; i<usedvar.length; i++)
						{
							for (int j=0; j<totalvar.length; j++)
							{
								if (usedvar[i].equalsIgnoreCase(totalvar[j]))
								{
									realval[i]=Double.NaN;
									if (!values[j].equals(""))
									{
										try
										{
											realval[i]=Double.parseDouble(values[j].trim());
										}
										catch (Exception nfe){}
									}
								}
							}
						}
						for(int i=0; i<nseeds; i++)
						{
							valseeds=new double[usedvar.length];
							Hashtable<String, Double> valse=tempseed.get(i);
							for(int j=0; j<usedvar.length; j++)
							{
								valseeds[j]=(valse.get(usedvar[j])).doubleValue();
							}
							if (vdt==5)
								ed.setGroup(vargroupvalues);
							sumu[i] = ed.getdistance(valseeds, realval);
						}
						possibilities=new double[nseeds];
						for(int i = 0; i<nseeds; i++)
						{
							double tempden=0;
							for (int l=0; l<nseeds; l++)
							{
								tempden+=Math.pow(sumu[i]/sumu[l], 2.0/(fcoeff-1.0));
							}
							possibilities[i]=1/tempden;
						}
						min=0;
						pointermin=-1;
						for(int i = 0; i<nseeds; i++)
						{
							if (possibilities[i]>min)
							{
								min=possibilities[i];
								pointermin=i;
							}
						}
						if (pointermin==-1)
							dw.write(values);
						else
						{
							newval=new String[values.length];
							for (int i=0; i<totalvar.length; i++)
							{
								newval[i]=values[i];
								for (int j=0; j<usedvar.length; j++)
								{
									if (totalvar[i].equals(usedvar[j]))
									{
										testmd=Double.NaN;
										if (!values[i].equals(""))
										{
											try
											{
												testmd=Double.parseDouble(values[i].trim());
											}
											catch (Exception nfe){}
										}
										if (Double.isNaN(testmd))
										{
											if (!wwp)
											{
												Hashtable<String, Double> valse=tempseed.get(pointermin);
												newval[i]=double2String((valse.get(usedvar[j])).doubleValue());
											}
											else
											{
												vals=0;
												sumvals=0;
												for (int k=0; k<tempseed.size(); k++)
												{
													Hashtable<String, Double> valse=tempseed.get(k);
													vals+=((valse.get(usedvar[j])).doubleValue())*possibilities[k];
													sumvals+=possibilities[k];
												}
												vals=vals/sumvals;
												newval[i]=double2String(vals);
											}
										}
									}
								}
							}
							dw.write(newval);
						}
					}
				}
			}
		}
		data.close();
		if (!noterror)
			return new Result("%882%<br>\n", false, null);

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
		parameters.add(new GetRequiredParameters(Keywords.dict+"c=", "dict", true, 885, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 884, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.fuzzycoeff,"text", true, 930,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.weight, "checkbox", false, 1231, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.novgconvert, "checkbox", false, 2227, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="1228";
		retprocinfo[1]="1229";
		return retprocinfo;
	}
}
