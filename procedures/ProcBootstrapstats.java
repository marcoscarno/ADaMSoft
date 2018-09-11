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

import ADaMSoft.algorithms.BootstrapErrorEvaluator;
import ADaMSoft.algorithms.VarGroupModalities;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;

import ADaMSoft.dataaccess.GroupedMatrix2Dfile;

import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;

import ADaMSoft.keywords.Keywords;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import cern.jet.stat.Probability;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that evaluates the bootstrap error for one or more variables
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class ProcBootstrapstats extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Bootstraperror
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		boolean todisk=false;
		boolean novgconvert=false;
		boolean noclforvg=false;
		boolean orderclbycode=false;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.var, Keywords.replications, Keywords.typeofstat, Keywords.bootestimate, Keywords.outstyle};
		String [] optionalparameters=new String[] {Keywords.vargroup, Keywords.weight, Keywords.alpha, Keywords.replace, Keywords.todisk, Keywords.novgconvert, Keywords.noclforvg, Keywords.orderclbycode, Keywords.where};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		todisk =(parameters.get(Keywords.todisk)!=null);

		novgconvert =(parameters.get(Keywords.novgconvert)!=null);
		orderclbycode =(parameters.get(Keywords.orderclbycode)!=null);
		noclforvg =(parameters.get(Keywords.noclforvg)!=null);

		String replace =(String)parameters.get(Keywords.replace);
		String alpha =(String)parameters.get(Keywords.alpha);
		String replications=(String)parameters.get(Keywords.replications.toLowerCase());
		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		int rep=string2int(replications);
		if (rep<2)
			return new Result("%1382%<br>\n", false, null);

		if (alpha==null)
			alpha="95";
		double interval=string2double(alpha);
		if (interval==0)
			return new Result("%1386%<br>\n", false, null);
		interval=1-((100-interval)/200);
		if ((interval>1) || (interval<0))
			return new Result("%1386%<br>\n", false, null);
		interval=Probability.normalInverse(interval);

		String typeofstat=(String)parameters.get(Keywords.typeofstat.toLowerCase());
		String[] stat=new String[] {Keywords.mean, Keywords.sum};
		int selectstat=steputilities.CheckOption(stat, typeofstat);
		if (selectstat==0)
			return new Result("%1775% "+Keywords.typeofstat.toUpperCase()+"<br>\n"+steputilities.getMessage(), false, null);

		String typeofboot=(String)parameters.get(Keywords.bootestimate.toLowerCase());
		String[] boot=new String[] {Keywords.standarderror, Keywords.bias, Keywords.confidenceinterval};
		int selectboot=steputilities.CheckOption(boot, typeofboot);
		if (selectboot==0)
			return new Result("%1775% "+Keywords.bootestimate.toUpperCase()+"<br>\n"+steputilities.getMessage(), false, null);

		String outstyle=(String)parameters.get(Keywords.outstyle.toLowerCase());
		String[] outstyles=new String[] {Keywords.varrow, Keywords.varcol};
		int selectstyle=steputilities.CheckOption(outstyles, outstyle);
		if (selectstyle==0)
			return new Result("%1775% "+Keywords.outstyle.toUpperCase()+"<br>\n"+steputilities.getMessage(), false, null);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		String vartemp=(String)parameters.get(Keywords.var.toLowerCase());
		String vargroup=(String)parameters.get(Keywords.vargroup.toLowerCase());
		String weightv=(String)parameters.get(Keywords.weight.toLowerCase());

		VariableUtilities varu=new VariableUtilities(dict, vargroup, vartemp, weightv, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String[] varv=varu.getanalysisvar();
		String[] varg=varu.getgroupvar();
		String[] totalvar=varu.getreqvar();

		if ((varg.length==0) && (novgconvert))
		{
			result.add(new LocalMessageGetter("%2228%<br>\n"));
		}
		if ((varg.length==0) && (noclforvg))
		{
			result.add(new LocalMessageGetter("%2230%<br>\n"));
		}
		if ((varg.length==0) && (orderclbycode))
		{
			result.add(new LocalMessageGetter("%2232%<br>\n"));
		}

		int[] replacerule=varu.getreplaceruleforsel(replace);

		DataReader data = new DataReader(dict);

		if (!data.open(totalvar, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		int[] utilvarstype=varu.getnormalruleforsel();

		ValuesParser vp=new ValuesParser(utilvarstype, null, null, null, null, null);

		String tempdir=(String)parameters.get(Keywords.WorkDir);

		BootstrapErrorEvaluator bee=new BootstrapErrorEvaluator(varv.length, rep, selectstat, todisk);

		GroupedMatrix2Dfile var=new GroupedMatrix2Dfile(tempdir, varv.length+1);

		String keyword="Bootstrap stats "+dict.getkeyword();
		String description="Bootstrap stats "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		VarGroupModalities vgm=new VarGroupModalities();

		if (varg.length>0)
		{
			vgm.setvarnames(varg);
			vgm.setdictionary(dict);
			if (orderclbycode)
				vgm.setorderbycode();
			if (novgconvert)
				vgm.noconversion();
		}
		int validgroup=0;
		String[] values = null;
		Vector<String> vargroupvalues=null;
		double[] varvalues=null;
		double[] vartouse=new double[varv.length+1];
		boolean ismd=false;
		double weightvalue=1;
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				if (novgconvert)
					vargroupvalues=vp.getorigvargroup(values);
				else
					vargroupvalues=vp.getvargroup(values);
				varvalues=vp.getanalysisvarasdouble(values);
				weightvalue=vp.getweight(values);
				if (vp.vargroupisnotmissing(vargroupvalues))
				{
					ismd=false;
					for (int i=0; i<varvalues.length; i++)
					{
						vartouse[i+1]=varvalues[i];
						if (Double.isNaN(varvalues[i]))
							ismd=true;
					}
					if (Double.isNaN(weightvalue))
						ismd=true;
					if (!ismd)
					{
						vartouse[0]=weightvalue;
						validgroup++;
						vgm.updateModalities(vargroupvalues);
						var.write(vargroupvalues, vartouse);
					}
				}
			}
		}
		data.close();
		if ((validgroup==0) && (where!=null))
		{
			var.closeAll();
			return new Result("%2804%<br>\n", false, null);
		}
		if ((validgroup==0) && (where==null))
		{
			var.closeAll();
			return new Result("%666%<br>\n", false, null);
		}

		vgm.calculate();
		bee.evaluate(vgm, var);

		Hashtable<Vector<String>, double[][]> beest=bee.getboot();
		Hashtable<Vector<String>, double[]> besta=bee.getstat();

		var.closeAll();

		int totalgroupmodalities=vgm.getTotal();

		Vector<Hashtable<String, String>> groupcodelabels=vgm.getgroupcodelabels();

		DataSetUtilities dsu=new DataSetUtilities();

		Hashtable<String, String> tempmd=new Hashtable<String, String>();

		int finalvars=0;

		for (int j=0; j<varg.length; j++)
		{
			if (!noclforvg)
				dsu.addnewvarfromolddict(dict, varg[j], groupcodelabels.get(j), tempmd, "g_"+varg[j]);
			else
				dsu.addnewvarfromolddict(dict, varg[j], tempmd, tempmd, "g_"+varg[j]);
			finalvars++;
		}

		if (selectstyle==1)
		{
			Hashtable<String, String> clvar=new Hashtable<String, String>();
			for (int j=0; j<varv.length; j++)
			{
				clvar.put(varv[j], dict.getvarlabelfromname(varv[j]));
			}
			dsu.addnewvar("var", "%306%", Keywords.TEXTSuffix, clvar, tempmd);
			finalvars++;
			String label="%1374%";
			if (selectstat==2)
				label="%1375%";
			dsu.addnewvar("realval", label, Keywords.NUMSuffix, tempmd, tempmd);
			finalvars++;
			label="%1377%";
			if (selectboot==2)
				label="%1378%";
			finalvars++;
			if (selectboot==3)
			{
				dsu.addnewvar("icinf", "%1383%", Keywords.NUMSuffix, tempmd, tempmd);
				finalvars++;
				dsu.addnewvar("icsup", "%1384%", Keywords.NUMSuffix, tempmd, tempmd);
			}
			else
				dsu.addnewvar("bootval", label, Keywords.NUMSuffix, tempmd, tempmd);
		}
		else
		{
			Hashtable<String, String> clvar=new Hashtable<String, String>();
			String label="%1374%";
			if (selectstat==2)
				label="%1375%";
			clvar.put("0",label);
			label="%1377%";
			if (selectboot==2)
				label="%1378%";
			if (selectboot==3)
			{
				clvar.put("1", "%1383%");
				clvar.put("2", "%1384%");
			}
			else
				clvar.put("1", label);
			dsu.addnewvar("ref", "%1385%", Keywords.TEXTSuffix, clvar, tempmd);
			finalvars++;
			for (int j=0; j<varv.length; j++)
			{
				dsu.addnewvar("v_"+varv[j], dict.getvarlabelfromname(varv[j]), Keywords.TEXTSuffix, tempmd, tempmd);
				finalvars++;
			}
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		if (totalgroupmodalities==0)
			totalgroupmodalities=1;

		String[] valuestowrite=new String[finalvars];

		for (int i=0; i<totalgroupmodalities; i++)
		{
			Vector<String> rifmodgroup=vgm.getvectormodalities(i);
			for (int j=0; j<rifmodgroup.size(); j++)
			{
				String groupvalue=rifmodgroup.get(j);
				if (groupvalue!=null)
				{
					if (!noclforvg)
						valuestowrite[j]=vgm.getcode(j, groupvalue);
					else
						valuestowrite[j]=groupvalue;
				}
			}
			double[][] tempb=beest.get(rifmodgroup);
			double[] realstat=besta.get(rifmodgroup);
			if (selectstyle==1)
			{
				for (int j=0; j<varv.length; j++)
				{
					valuestowrite[varg.length]=varv[j];
					valuestowrite[varg.length+1]=double2String(realstat[j]);
					double se=0;
					double mean=0;
					int num=0;
					for (int k=0; k<tempb.length; k++)
					{
						mean+=tempb[k][j];
						num++;
					}
					mean=mean/num;
					for (int k=0; k<tempb.length; k++)
					{
						se+=Math.pow((tempb[k][j]-mean),2);
					}
					se=Math.sqrt(se/(num-1));
					if (selectboot==1)
						valuestowrite[varg.length+2]=double2String(se);
					else if (selectboot==2)
						valuestowrite[varg.length+2]=double2String(mean-realstat[j]);
					else
					{
						valuestowrite[varg.length+2]=double2String(mean-interval*se);
						valuestowrite[varg.length+3]=double2String(mean+interval*se);
					}
					dw.write(valuestowrite);
				}
			}
			else
			{
				valuestowrite[varg.length]="0";
				for (int j=0; j<varv.length; j++)
				{
					valuestowrite[varg.length+j+1]=double2String(realstat[j]);
				}
				dw.write(valuestowrite);
				valuestowrite[varg.length]="1";
				if (selectboot==1)
				{
					for (int j=0; j<varv.length; j++)
					{
						double se=0;
						double mean=0;
						int num=0;
						for (int k=0; k<tempb.length; k++)
						{
							mean+=tempb[k][j];
							num++;
						}
						mean=mean/num;
						for (int k=1; k<tempb.length; k++)
						{
							se+=Math.pow((tempb[k][j]-mean),2);
						}
						se=Math.sqrt(se/(num-1));
						valuestowrite[varg.length+j+1]=double2String(se);
					}
					dw.write(valuestowrite);
				}
				else if (selectboot==2)
				{
					for (int j=0; j<varv.length; j++)
					{
						double mean=0;
						int num=0;
						for (int k=0; k<tempb.length; k++)
						{
							mean+=tempb[k][j];
							num++;
						}
						mean=mean/num;
						valuestowrite[varg.length+j+1]=double2String(mean-realstat[j]);
					}
					dw.write(valuestowrite);
				}
				else
				{
					for (int j=0; j<varv.length; j++)
					{
						double se=0;
						double mean=0;
						int num=0;
						for (int k=0; k<tempb.length; k++)
						{
							mean+=tempb[k][j];
							num++;
						}
						mean=mean/num;
						for (int k=0; k<tempb.length; k++)
						{
							se+=Math.pow((tempb[k][j]-mean),2);
						}
						se=Math.sqrt(se/(num-1));
						valuestowrite[varg.length+j+1]=double2String(mean-interval*se);
					}
					dw.write(valuestowrite);
					valuestowrite[varg.length]="2";
					for (int j=0; j<varv.length; j++)
					{
						double se=0;
						double mean=0;
						int num=0;
						for (int k=0; k<tempb.length; k++)
						{
							mean+=tempb[k][j];
							num++;
						}
						mean=mean/num;
						for (int k=0; k<tempb.length; k++)
						{
							se+=Math.pow((tempb[k][j]-mean),2);
						}
						se=Math.sqrt(se/(num-1));
						valuestowrite[varg.length+j+1]=double2String(mean+interval*se);
					}
					dw.write(valuestowrite);
				}
			}
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
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 721, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 1371, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", true, 641, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.weight, "vars=all", false, 656, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replications, "text", true, 1372, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.typeofstat, "listsingle=687_"+Keywords.sum+",681_"+Keywords.mean, true, 1373, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.bootestimate, "listsingle=1377_"+Keywords.standarderror+",1378_"+Keywords.bias+",1379_"+Keywords.confidenceinterval, true, 1376, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.alpha,"text", false, 1380,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.outstyle, "listsingle=674_"+Keywords.varrow+",675_"+Keywords.varcol, true, 677, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.todisk, "checkbox", false, 1387, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.novgconvert, "checkbox", false, 2227, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.noclforvg, "checkbox", false, 2229, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.orderclbycode, "checkbox", false, 2231, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="678";
		retprocinfo[1]="1381";
		return retprocinfo;
	}
}
