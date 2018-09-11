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
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.SortRequestedVar;
import ADaMSoft.algorithms.LinearCombinationEvaluator;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that evaluates the predicted value for a linear discriminant problem
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcLdaeval extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Ldaeval
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		boolean novgconvert=false;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.dict+"e"};
		String [] optionalparameters=new String[] {Keywords.replace, Keywords.where, Keywords.novgconvert, Keywords.limitvalue, Keywords.minvalue, Keywords.maxvalue};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}

		String replace =(String)parameters.get(Keywords.replace);

		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		DictionaryReader dicte = (DictionaryReader)parameters.get(Keywords.dict+"e");
		boolean limitvalue=(parameters.get(Keywords.limitvalue)!=null);
		String tmin=(String)parameters.get(Keywords.minvalue);
		String tmax=(String)parameters.get(Keywords.maxvalue);
		novgconvert =(parameters.get(Keywords.novgconvert)!=null);

		if (tmin==null && tmax!=null)
			return new Result("%3444%<br>\n", false, null);
		if (tmin!=null && tmax==null)
			return new Result("%3444%<br>\n", false, null);

		if (tmin!=null && limitvalue)
			return new Result("%3445%<br>\n", false, null);

		double minv=Double.NaN;
		double maxv=Double.NaN;
		if (tmin!=null)
		{
			try
			{
				minv=Double.parseDouble(tmin);
			}
			catch (Exception e) {}
			if (Double.isNaN(minv)) return new Result("%3446%<br>\n", false, null);
		}
		if (tmax!=null)
		{
			try
			{
				maxv=Double.parseDouble(tmax);
			}
			catch (Exception e) {}
			if (Double.isNaN(maxv)) return new Result("%3447%<br>\n", false, null);
		}
		if (!Double.isNaN(minv) && !Double.isNaN(maxv) && maxv<=minv) return new Result("%3448%<br>\n", false, null);

		int numvar=dicte.gettotalvar();
		String var="";
		boolean iscorrect=true;
		for (int i=0; i<numvar; i++)
		{
			String tempname=dicte.getvarname(i);
			if ((tempname.toLowerCase()).startsWith("g_"))
			{
				try
				{
					String tc=tempname.substring(2);
					var=var+" "+tc;
				}
				catch (Exception ec)
				{
					iscorrect=false;
				}
			}
		}
		if (!iscorrect)
			return new Result("%1931%<br>\n", false, null);

		String groupname=var.trim();

		var=var+" varx value";
		var=var.trim();

		String[] groupvar=new String[0];
		if (!groupname.equals(""))
			groupvar=groupname.split(" ");

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		LinearCombinationEvaluator lce=new LinearCombinationEvaluator(dict);

		String[] alldsvars=new String[dict.gettotalvar()];
		for (int i=0; i<dict.gettotalvar(); i++)
		{
			alldsvars[i]=dict.getvarname(i);
		}
		groupvar=SortRequestedVar.getreqsorted(groupvar, alldsvars);

		String[] vartoread=new String[groupvar.length+2];
		for (int i=0; i<groupvar.length; i++)
		{
			vartoread[i]="g_"+groupvar[i];
		}
		vartoread[groupvar.length]="varx";
		vartoread[groupvar.length+1]="value";

		int[] replacerule=new int[vartoread.length];
		for (int i=0; i<vartoread.length; i++)
		{
			replacerule[i]=0;
		}

		DataReader datae = new DataReader(dicte);
		if (!datae.open(vartoread, replacerule, false))
			return new Result(datae.getmessage(), false, null);

		Hashtable<String, Integer> tempvarname=new Hashtable<String, Integer>();

		boolean errorinpara=false;

		lce.setparametersinfo(groupvar.length+1, groupvar.length);

		while (!datae.isLast())
		{
			String[] values = datae.getRecord();
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
			try
			{
				lce.addparameters(groupval, values);
				String tempname=values[groupvar.length].toLowerCase();
				if (tempvarname.get(tempname)==null)
					tempvarname.put(tempname, new Integer(tempvarname.size()));
			}
			catch (Exception e)
			{
				errorinpara=true;
			}
		}
		datae.close();
		if (errorinpara)
			return new Result("%1931%<br>\n", false, null);

		String[] usedvarnames=new String[tempvarname.size()];
		for (Enumeration<String> es = tempvarname.keys() ; es.hasMoreElements() ;)
		{
			String tempvname=es.nextElement();
			int varposition=(tempvarname.get(tempvname)).intValue();
			usedvarnames[varposition]=tempvname;
		}

		String varx="";
		for (int i=0; i<usedvarnames.length; i++)
		{
			varx=varx+" "+usedvarnames[i];
		}
		varx=varx.trim();

		String keyword="LDAEval "+dict.getkeyword();
		String description="LDAEval "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);
		Hashtable<String, String> temph=new Hashtable<String, String>();

		dsu.defineolddict(dict);

		dsu.addnewvartoolddict("pred", "%1935%", Keywords.NUMSuffix, temph, temph);

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

		VariableUtilities varu=new VariableUtilities(dict, groupname, varx, null, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String[] totalvar=varu.getallvar();

		int[] replacer=varu.getreplaceruleforall(replace);

		DataReader data = new DataReader(dict);
		if (!data.open(totalvar, replacer, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		int[] allvarstype=varu.getnormalruleforall();

		ValuesParser vp=new ValuesParser(allvarstype, null, null, null, null, null);

		int validgroup=0;
		boolean noterror=true;
		double predictedval=Double.NaN;
		Vector<String> vargroupvalues=new Vector<String>();
		Hashtable<Vector<String>, double[]> min_max=new Hashtable<Vector<String>, double[]>();
		if (limitvalue)
		{
			while ((!data.isLast()) && (noterror))
			{
				String[] values = data.getRecord();
				Vector<String> vg=new Vector<String>();
				if (values!=null)
				{
					if (novgconvert)
						vg=vp.getorigvargroup(values);
					else
						vg=vp.getvargroup(values);
					if (vp.vargroupisnotmissing(vg))
					{
						predictedval=lce.evalcombination(vg, values);
						if (!Double.isNaN(predictedval))
						{
							if (min_max.get(vg)==null)
							{
								double[] tmima=new double[2];
								tmima[0]=predictedval;
								tmima[1]=predictedval;
								min_max.put(vg, tmima);
							}
							else
							{
								double[] tmima=min_max.get(vg);
								if (tmima[0]>predictedval) tmima[0]=predictedval;
								if (tmima[1]<predictedval) tmima[1]=predictedval;
								min_max.put(vg, tmima);
							}
						}
					}
				}
			}
			data.close();
			if (!data.open(totalvar, replacer, false))
				return new Result(data.getmessage(), false, null);
			where=(String)parameters.get(Keywords.where.toLowerCase());
			if (where!=null)
			{
				if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
			}
			result.add(new LocalMessageGetter("%3449%<br>\n"));
			if (groupvar.length>0)
			{
				String gref="";
				for (int i=0; i<groupvar.length; i++)
				{
					gref=gref+dict.getvarlabelfromname(groupvar[i]);
					if (i<groupvar.length-1) gref=gref+",";
				}
				result.add(new LocalMessageGetter("%3450%: "+gref+"<br>\n"));
			}
			for (Enumeration<Vector<String>> emm = min_max.keys() ; emm.hasMoreElements() ;)
			{
				Vector<String> vg= emm.nextElement();
				String gref="";
				if (groupvar.length>0)
				{
					for (int i=0; i<vg.size(); i++)
					{
						gref=gref+vg.get(i);
						if (i<vg.size()-1) gref=gref+",";
					}
					gref=gref+"; ";
				}
				double[] tmm=min_max.get(vg);
				gref=gref+"%3451%: "+String.valueOf(tmm[0])+", "+String.valueOf(tmm[1]);
				result.add(new LocalMessageGetter(gref+"<br>\n"));
			}
		}

		while ((!data.isLast()) && (noterror))
		{
			String[] values = data.getRecord();
			if (values!=null)
			{
				if (novgconvert)
					vargroupvalues=vp.getorigvargroup(values);
				else
					vargroupvalues=vp.getvargroup(values);
				String[] newvalues=new String[1];
				newvalues[0]="";
				if (vp.vargroupisnotmissing(vargroupvalues))
				{
					validgroup++;
					predictedval=lce.evalcombination(vargroupvalues, values);
					if (!Double.isNaN(minv) && !Double.isNaN(maxv))
					{
						predictedval=(predictedval-minv)/(maxv-minv);
						if (predictedval<0) predictedval=0.0;
						if (predictedval>1) predictedval=1.0;
					}
					if (limitvalue)
					{
						double[] tmin_max=min_max.get(vargroupvalues);
						predictedval=(predictedval-tmin_max[0])/(tmin_max[1]-tmin_max[0]);
						if (predictedval<0) predictedval=0.0;
						if (predictedval>1) predictedval=1.0;
					}
					try
					{
						newvalues[0]=double2String(predictedval);
					}
					catch (Exception e) {}
				}
				String[] wvalues=dsu.getnewvalues(values, newvalues);
				dw.write(wvalues);
			}
		}
		data.close();
		if ((validgroup==0) && (where!=null))
		{
			dw.deletetmp();
			return new Result("%2804%<br>\n", false, null);
		}
		if ((validgroup==0) && (where==null))
		{
			dw.deletetmp();
			return new Result("%666%<br>\n", false, null);
		}

		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
		Hashtable<String, String> datatableinfo=dw.getTableInfo();
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
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 1937, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"e=", "dict", true, 1938, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 1939, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.limitvalue,"checkbox", false, 3441,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.minvalue,"text", false, 3442,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.maxvalue,"text", false, 3443,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2809,dep,"",2));
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
		retprocinfo[0]="1929";
		retprocinfo[1]="1936";
		return retprocinfo;
	}
}
