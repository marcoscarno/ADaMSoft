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

import ADaMSoft.algorithms.VarGroupModalities;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;

import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.keywords.Keywords;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

/**
* This is the procedure that evaluates several indicators related to the result of one or more methods that try to approximate a function
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcEvalfuncapprox extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Evalfuncapprox
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		boolean novgconvert=false;
		boolean noclforvg=false;
		boolean orderclbycode=false;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.varpred, Keywords.vary};
		String [] optionalparameters=new String[] {Keywords.vargroup, Keywords.where, Keywords.weight, Keywords.replace, Keywords.novgconvert, Keywords.noclforvg, Keywords.orderclbycode};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String tempvarx=(String)parameters.get(Keywords.varpred.toLowerCase());
		String tempvary=(String)parameters.get(Keywords.vary.toLowerCase());
		String tempvargroup=(String)parameters.get(Keywords.vargroup.toLowerCase());
		String weight=(String)parameters.get(Keywords.weight.toLowerCase());

		novgconvert =(parameters.get(Keywords.novgconvert)!=null);
		orderclbycode =(parameters.get(Keywords.orderclbycode)!=null);
		noclforvg =(parameters.get(Keywords.noclforvg)!=null);

		String[] testdepvar=tempvary.split(" ");
		if (testdepvar.length>1)
			return new Result("%2006%<br>\n", false, null);

		VariableUtilities varu=new VariableUtilities(dict, tempvargroup, null, weight, tempvarx, tempvary);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String keyword="Evalfuncapprox "+dict.getkeyword();
		String description="Evalfuncapprox "+dict.getdescription();
		String author=(String)parameters.get(Keywords.client_host.toLowerCase());

		String[] varx=varu.getrowvar();
		String[] varg=varu.getgroupvar();

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

		String[] reqvar=varu.getreqvar();
		
		int[] replacerule=varu.getreplaceruleforsel(tempvarx);

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
		int[] colrule=varu.getcolruleforsel();
		int[] weightrule=varu.getweightruleforsel();

		ValuesParser vp=new ValuesParser(null, grouprule, null, rowrule, colrule, weightrule);

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

		Hashtable<Vector<String>, double[]> numdep=new Hashtable<Vector<String>, double[]>();
		Hashtable<Vector<String>, double[]> meandep=new Hashtable<Vector<String>, double[]>();
		Hashtable<Vector<String>, double[]> meanpred=new Hashtable<Vector<String>, double[]>();
		Hashtable<Vector<String>, double[]> stddep=new Hashtable<Vector<String>, double[]>();
		Hashtable<Vector<String>, double[]> stdpred=new Hashtable<Vector<String>, double[]>();
		Hashtable<Vector<String>, double[]> meandif=new Hashtable<Vector<String>, double[]>();
		Hashtable<Vector<String>, double[]> meanadif=new Hashtable<Vector<String>, double[]>();
		Hashtable<Vector<String>, double[]> meanrmse=new Hashtable<Vector<String>, double[]>();
		Hashtable<Vector<String>, double[]> stddif=new Hashtable<Vector<String>, double[]>();
		Hashtable<Vector<String>, double[]> stdadif=new Hashtable<Vector<String>, double[]>();
		Hashtable<Vector<String>, double[]> stdrmse=new Hashtable<Vector<String>, double[]>();
		Hashtable<Vector<String>, double[]> covar=new Hashtable<Vector<String>, double[]>();
		Hashtable<Vector<String>, double[]> bestres=new Hashtable<Vector<String>, double[]>();

		int validgroup=0;
		String[] values=null;
		Vector<String> vargroupvalues=new Vector<String>();
		double[] varxvalues=null;
		double[] varyvalues=null;
		double weightvalue=1;
		double dif=0;
		double adif=0;
		double rmse=0;
		int bres=0;
		double bestres2=0;
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				if (novgconvert)
					vargroupvalues=vp.getorigvargroup(values);
				else
					vargroupvalues=vp.getvargroup(values);
				varxvalues=vp.getrowvarasdouble(values);
				varyvalues=vp.getcolvarasdouble(values);
				weightvalue=vp.getweight(values);
				if ((vp.vargroupisnotmissing(vargroupvalues)) && (!Double.isNaN(weightvalue)))
				{
					bres=-1;
					bestres2=Double.MAX_VALUE;
					for (int i=0; i<varxvalues.length; i++)
					{
						if ((!Double.isNaN(varyvalues[0])) && (!Double.isNaN(varxvalues[i])))
						{
							validgroup++;
							if (numdep.get(vargroupvalues)==null)
							{
								double[] tnumdep=new double[varxvalues.length];
								double[] tmeandep=new double[varxvalues.length];
								double[] tmeanpred=new double[varxvalues.length];
								double[] tstddep=new double[varxvalues.length];
								double[] tstdpred=new double[varxvalues.length];
								double[] tmeandif=new double[varxvalues.length];
								double[] tmeanadif=new double[varxvalues.length];
								double[] tmeanrmse=new double[varxvalues.length];
								double[] tstddif=new double[varxvalues.length];
								double[] tstdadif=new double[varxvalues.length];
								double[] tstdrmse=new double[varxvalues.length];
								double[] tcovar=new double[varxvalues.length];
								double[] tbestres=new double[varxvalues.length];
								for (int j=0; j<varxvalues.length; j++)
								{
									tnumdep[j]=0;
									tmeandep[j]=0;
									tmeanpred[j]=0;
									tstddep[j]=0;
									tstdpred[j]=0;
									tmeandif[j]=0;
									tmeanadif[j]=0;
									tmeanrmse[j]=0;
									tstddif[j]=0;
									tstdadif[j]=0;
									tstdrmse[j]=0;
									tcovar[j]=0;
									tbestres[j]=0;
								}
								numdep.put(vargroupvalues, tnumdep);
								meandep.put(vargroupvalues, tmeandep);
								meanpred.put(vargroupvalues, tmeanpred);
								stddep.put(vargroupvalues, tstddep);
								stdpred.put(vargroupvalues, tstdpred);
								meandif.put(vargroupvalues, tmeandif);
								meanadif.put(vargroupvalues, tmeanadif);
								meanrmse.put(vargroupvalues, tmeanrmse);
								stddif.put(vargroupvalues, tstddif);
								stdadif.put(vargroupvalues, tstdadif);
								stdrmse.put(vargroupvalues, tstdrmse);
								covar.put(vargroupvalues, tcovar);
								bestres.put(vargroupvalues, tbestres);
							}
							double[] ttnumdep=numdep.get(vargroupvalues);
							double[] ttmeandep=meandep.get(vargroupvalues);
							double[] ttmeanpred=meanpred.get(vargroupvalues);
							double[] ttstddep=stddep.get(vargroupvalues);
							double[] ttstdpred=stdpred.get(vargroupvalues);
							double[] ttmeandif=meandif.get(vargroupvalues);
							double[] ttmeanadif=meanadif.get(vargroupvalues);
							double[] ttmeanrmse=meanrmse.get(vargroupvalues);
							double[] ttstddif=stddif.get(vargroupvalues);
							double[] ttstdadif=stdadif.get(vargroupvalues);
							double[] ttstdrmse=stdrmse.get(vargroupvalues);
							double[] ttcovar=covar.get(vargroupvalues);
							ttnumdep[i]=ttnumdep[i]+weightvalue;
							dif=(varyvalues[0]-varxvalues[i]);
							adif=(Math.abs(varyvalues[0]-varxvalues[i]));
							rmse=Math.pow((varyvalues[0]-varxvalues[i]),2);
							if (rmse<bestres2)
							{
								bestres2=rmse;
								bres=i;
							}
							ttmeandep[i]=ttmeandep[i]+weightvalue*varyvalues[0];
							ttmeanpred[i]=ttmeanpred[i]+weightvalue*varxvalues[i];
							ttstddep[i]=ttstddep[i]+weightvalue*varyvalues[0]*varyvalues[0];
							ttstdpred[i]=ttstdpred[i]+weightvalue*varxvalues[i]*varxvalues[i];
							ttmeandif[i]=ttmeandif[i]+weightvalue*dif;
							ttmeanadif[i]=ttmeanadif[i]+weightvalue*adif;
							ttmeanrmse[i]=ttmeanrmse[i]+weightvalue*rmse;

							double tempvali=weightvalue*(adif/((varyvalues[0]+varxvalues[i])/2));
							if (!Double.isNaN(tempvali))
							{
								if (!Double.isInfinite(tempvali))
									ttstddif[i]=ttstddif[i]+tempvali;
							}

							ttstdadif[i]=ttstdadif[i]+weightvalue*adif*adif;
							ttstdrmse[i]=ttstdrmse[i]+weightvalue*rmse*rmse;
							ttcovar[i]=ttcovar[i]+weightvalue*varyvalues[0]*varxvalues[i];
						}
					}
					double[] ttbestres=bestres.get(vargroupvalues);
					if (bres!=-1)
					{
						ttbestres[bres]=ttbestres[bres]+1;
					}
					vgm.updateModalities(vargroupvalues);
				}
			}
		}
		data.close();
		if ((validgroup==0) && (where!=null))
			return new Result("%2804%<br>\n", false, null);
		if ((validgroup==0) && (where==null))
			return new Result("%666%<br>\n", false, null);
		vgm.calculate();

		int totalgroupmodalities=vgm.getTotal();
		if (totalgroupmodalities==0)
			totalgroupmodalities=1;

		DataSetUtilities dsu=new DataSetUtilities();
		String replace="";
		dsu.setreplace(replace);

		Vector<Hashtable<String, String>> groupcodelabels=vgm.getgroupcodelabels();

		Hashtable<String, String> tempmd=new Hashtable<String, String>();
		for (int j=0; j<varg.length; j++)
		{
			if (!noclforvg)
				dsu.addnewvarfromolddict(dict, varg[j], groupcodelabels.get(j), tempmd, "g_"+varg[j]);
			else
				dsu.addnewvarfromolddict(dict, varg[j], tempmd, tempmd, "g_"+varg[j]);
		}

		Hashtable<String, String> clvar=new Hashtable<String, String>();
		clvar.put("0","%2007%");
		clvar.put("1","%2008%");
		clvar.put("2","%2009%");
		clvar.put("3","%2010%");
		clvar.put("4","%2011%");
		clvar.put("5","%2012%");
		clvar.put("6","%2013%");
		clvar.put("7","%2014%");
		clvar.put("8","%2016%");
		clvar.put("9","%2017%");
		clvar.put("10","%2018%");
		clvar.put("11","%2019%");
		clvar.put("12","%2021%");
		clvar.put("13","%2015%");

		dsu.addnewvar("indica", "%2020%", Keywords.TEXTSuffix, clvar, tempmd);

		for (int i=0; i<varx.length; i++)
		{
			dsu.addnewvar("v_"+varx[i], dict.getvarlabelfromname(varx[i]), Keywords.NUMSuffix, tempmd, tempmd);
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		String[] valuestowrite=new String[varg.length+1+varx.length];
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
			valuestowrite[varg.length]="0";
			double[] ttnumdep=numdep.get(rifmodgroup);
			for (int r=0; r<varx.length; r++)
			{
				valuestowrite[varg.length+r+1]=double2String(ttnumdep[r]);
			}
			dw.write(valuestowrite);
			valuestowrite[varg.length]="1";
			double[] ttmeandep=meandep.get(rifmodgroup);
			for (int r=0; r<varx.length; r++)
			{
				valuestowrite[varg.length+r+1]=double2String(ttmeandep[r]/ttnumdep[r]);
			}
			dw.write(valuestowrite);
			valuestowrite[varg.length]="2";
			double[] ttmeanpred=meanpred.get(rifmodgroup);
			for (int r=0; r<varx.length; r++)
			{
				valuestowrite[varg.length+r+1]=double2String(ttmeanpred[r]/ttnumdep[r]);
			}
			dw.write(valuestowrite);
			valuestowrite[varg.length]="3";
			double[] ttstddep=stddep.get(rifmodgroup);
			for (int r=0; r<varx.length; r++)
			{
				valuestowrite[varg.length+r+1]=double2String(Math.sqrt((ttstddep[r]/ttnumdep[r])-(ttmeandep[r]/ttnumdep[r])*(ttmeandep[r]/ttnumdep[r])));
			}
			dw.write(valuestowrite);
			valuestowrite[varg.length]="4";
			double[] ttstdpred=stdpred.get(rifmodgroup);
			for (int r=0; r<varx.length; r++)
			{
				valuestowrite[varg.length+r+1]=double2String(Math.sqrt((ttstdpred[r]/ttnumdep[r])-(ttmeanpred[r]/ttnumdep[r])*(ttmeanpred[r]/ttnumdep[r])));
			}
			dw.write(valuestowrite);
			valuestowrite[varg.length]="5";
			double[] ttmeandif=meandif.get(rifmodgroup);
			for (int r=0; r<varx.length; r++)
			{
				valuestowrite[varg.length+r+1]=double2String(ttmeandif[r]/ttnumdep[r]);
			}
			dw.write(valuestowrite);
			valuestowrite[varg.length]="6";
			double[] ttmeanadif=meanadif.get(rifmodgroup);
			for (int r=0; r<varx.length; r++)
			{
				valuestowrite[varg.length+r+1]=double2String(ttmeanadif[r]/ttnumdep[r]);
			}
			dw.write(valuestowrite);
			valuestowrite[varg.length]="7";
			double[] ttmeanrmse=meanrmse.get(rifmodgroup);
			for (int r=0; r<varx.length; r++)
			{
				valuestowrite[varg.length+r+1]=double2String(Math.sqrt(ttmeanrmse[r]/ttnumdep[r]));
			}
			dw.write(valuestowrite);
			valuestowrite[varg.length]="8";
			double[] ttstdadif=stdadif.get(rifmodgroup);
			for (int r=0; r<varx.length; r++)
			{
				valuestowrite[varg.length+r+1]=double2String(Math.sqrt((ttstdadif[r]/ttnumdep[r])-(ttmeanadif[r]/ttnumdep[r])*(ttmeanadif[r]/ttnumdep[r])));
			}
			dw.write(valuestowrite);
			valuestowrite[varg.length]="9";
			double[] ttstdrmse=stdrmse.get(rifmodgroup);
			for (int r=0; r<varx.length; r++)
			{
				valuestowrite[varg.length+r+1]=double2String(Math.sqrt((ttstdrmse[r]/ttnumdep[r])-(ttmeanrmse[r]/ttnumdep[r])*(ttmeanrmse[r]/ttnumdep[r])));
			}
			dw.write(valuestowrite);
			valuestowrite[varg.length]="10";
			double[] ttcovar=covar.get(rifmodgroup);
			for (int r=0; r<varx.length; r++)
			{
				double num=ttcovar[r]/ttnumdep[r]-((ttmeandep[r]/ttnumdep[r])*(ttmeanpred[r]/ttnumdep[r]));
				double den1=Math.sqrt((ttstddep[r]/ttnumdep[r])-(ttmeandep[r]/ttnumdep[r])*(ttmeandep[r]/ttnumdep[r]));
				double den2=Math.sqrt((ttstdpred[r]/ttnumdep[r])-(ttmeanpred[r]/ttnumdep[r])*(ttmeanpred[r]/ttnumdep[r]));
				valuestowrite[varg.length+r+1]=double2String(num/(den1*den2));
			}
			dw.write(valuestowrite);
			valuestowrite[varg.length]="11";
			for (int r=0; r<varx.length; r++)
			{
				double num=ttcovar[r]/ttnumdep[r]-((ttmeandep[r]/ttnumdep[r])*(ttmeanpred[r]/ttnumdep[r]));
				double den=(ttstdpred[r]/ttnumdep[r])-(ttmeanpred[r]/ttnumdep[r])*(ttmeanpred[r]/ttnumdep[r]);
				valuestowrite[varg.length+r+1]=double2String(num/den);
			}
			dw.write(valuestowrite);
			valuestowrite[varg.length]="12";
			double[] ttbestres=bestres.get(rifmodgroup);
			double sumnumdep=0;
			for (int r=0; r<varx.length; r++)
			{
				sumnumdep=sumnumdep+ttbestres[r];
			}
			for (int r=0; r<varx.length; r++)
			{
				valuestowrite[varg.length+r+1]=double2String(100*ttbestres[r]/sumnumdep);
			}
			dw.write(valuestowrite);
			valuestowrite[varg.length]="13";

			double[] ttstddif=stddif.get(rifmodgroup);

			for (int r=0; r<varx.length; r++)
			{
				valuestowrite[varg.length+r+1]=double2String(ttstddif[r]*100);
			}
			dw.write(valuestowrite);
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
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 2002, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 1843, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.vary, "var=all", true, 2003, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varpred, "vars=all", true, 2004, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.weight, "vars=all", false, 656, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
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
		retprocinfo[0]="2000";
		retprocinfo[1]="2001";
		return retprocinfo;
	}
}
