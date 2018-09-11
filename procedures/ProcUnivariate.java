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

import ADaMSoft.algorithms.MaxEvaluator;
import ADaMSoft.algorithms.MeanEvaluator;
import ADaMSoft.algorithms.MinEvaluator;
import ADaMSoft.algorithms.NEvaluator;
import ADaMSoft.algorithms.NMissEvaluator;
import ADaMSoft.algorithms.SQEvaluator;
import ADaMSoft.algorithms.SSQEvaluator;
import ADaMSoft.algorithms.STDEvaluator;
import ADaMSoft.algorithms.SumEvaluator;
import ADaMSoft.algorithms.TTestEvaluator;
import ADaMSoft.algorithms.VarEvaluator;
import ADaMSoft.algorithms.VarGroupModalities;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ValuesParser;

import ADaMSoft.keywords.Keywords;


import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import java.lang.Math;
import cern.jet.stat.Probability;

/**
* This is the procedure that evaluates several univariate statistics on one or more variables, also by considering several grouping variables
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcUnivariate implements RunStep
{
	/**
	* Starts the execution of Proc Copy and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		boolean samplevariance=false;
		boolean novgconvert=false;
		boolean noclforvg=false;
		boolean orderclbycode=false;
		boolean usewritefmt=false;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.var, Keywords.statistic};
		String [] optionalparameters=new String[] {Keywords.vargroup, Keywords.weight, Keywords.replace, Keywords.alpha, Keywords.outstyle, Keywords.samplevariance, Keywords.novgconvert, Keywords.noclforvg, Keywords.orderclbycode, Keywords.usewritefmt};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		samplevariance =(parameters.get(Keywords.samplevariance)!=null);
		novgconvert =(parameters.get(Keywords.novgconvert)!=null);
		orderclbycode =(parameters.get(Keywords.orderclbycode)!=null);
		noclforvg =(parameters.get(Keywords.noclforvg)!=null);
		usewritefmt=(parameters.get(Keywords.usewritefmt)!=null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		String statistic=(String)parameters.get(Keywords.statistic.toLowerCase());
		String proba=(String)parameters.get(Keywords.alpha.toLowerCase());
		if (statistic==null)
			return new Result("%660%<br>\n", false, null);
		String[] statistics=new String[] {Keywords.CLM, Keywords.CSS, Keywords.CV,
		Keywords.MAX, Keywords.MEAN, Keywords.MIN, Keywords.N, Keywords.NMISS,
		Keywords.RANGE, Keywords.STD, Keywords.SUM, Keywords.TTEST1, Keywords.TTEST2, Keywords.PTTEST1,
		Keywords.PTTEST2, Keywords.VARIANCE, Keywords.VARTEST, Keywords.PVARTEST};
		if (!steputilities.CheckOptions(statistics, statistic))
			return new Result("%1775% "+Keywords.statistic.toUpperCase()+"<br>\n"+steputilities.getMessage(), false, null);

		String[] statisticlabels=new String[] {"%663%", "%664%", "%665%",
		"%680%", "%681%", "%682%", "%683%", "%684%",
		"%685%", "%686%", "%687%", "%688%", "%709%", "%689%",
		"%710%", "%690%", "%1778%", "%1779%"};

		int[] selectedoption=steputilities.getSelection();
		boolean dottest=false;
		if (selectedoption[11]==1)
			dottest=true;
		if (selectedoption[12]==1)
			dottest=true;
		if (selectedoption[13]==1)
			dottest=true;
		if (selectedoption[14]==1)
			dottest=true;
		if (selectedoption[16]==1)
			dottest=true;
		if (selectedoption[17]==1)
			dottest=true;
		int totalselectedoption=steputilities.getNumselected();

		String outstyle=(String)parameters.get(Keywords.outstyle.toLowerCase());
		String[] outstyles=new String[] {Keywords.varrow, Keywords.varcol, Keywords.aggreg};
		int selectstyle=steputilities.CheckOption(outstyles, outstyle);
		if (selectstyle==0)
			return new Result("%1775% "+Keywords.outstyle.toUpperCase()+"<br>\n"+steputilities.getMessage(), false, null);

		double alpha=0.05;
		if (proba!=null)
		{
			try
			{
				alpha=Double.parseDouble(proba);
			}
			catch (NumberFormatException en)
			{
				return new Result("%662%<br>\n", false, null);
			}
		}
		if ((alpha>1) || (alpha<0))
			return new Result("%662%<br>\n", false, null);

		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		String vartemp=(String)parameters.get(Keywords.var.toLowerCase());
		String vargroup=(String)parameters.get(Keywords.vargroup.toLowerCase());
		String weight=(String)parameters.get(Keywords.weight.toLowerCase());

		VariableUtilities varu=new VariableUtilities(dict, vargroup, vartemp, weight, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String[] var=varu.getanalysisvar();
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

		String[] totalvar=varu.getreqvar();

		String replace=(String)parameters.get(Keywords.replace);
		int[] replacerule=varu.getreplaceruleforsel(replace);

		String keyword="Univariate "+dict.getkeyword();
		String description="Univariate "+dict.getdescription();

		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		DataReader data = new DataReader(dict);

		if (!data.open(totalvar, replacerule, usewritefmt))
			return new Result(data.getmessage(), false, null);
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		int[] utilvarstype=varu.getnormalruleforsel();

		ValuesParser vp=new ValuesParser(utilvarstype, null, null, null, null, null);

		MeanEvaluator emean=new MeanEvaluator();
		STDEvaluator estd=new STDEvaluator(samplevariance);
		SumEvaluator esum=new SumEvaluator();
		MaxEvaluator emax=new MaxEvaluator();
		MinEvaluator emin=new MinEvaluator();
		NEvaluator en=new NEvaluator();
		NEvaluator ennp=new NEvaluator();
		NMissEvaluator enmiss=new NMissEvaluator();
		SSQEvaluator essq=new SSQEvaluator();
		VarEvaluator evar=new VarEvaluator(samplevariance);
		SQEvaluator esq=new SQEvaluator();
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
		double weightvalue=Double.NaN;
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
					validgroup++;
					vgm.updateModalities(vargroupvalues);
					estd.setValue(vargroupvalues, varvalues, weightvalue);
					emean.setValue(vargroupvalues, varvalues, weightvalue);
					esum.setValue(vargroupvalues, varvalues, weightvalue);
					emax.setValue(vargroupvalues, varvalues);
					emin.setValue(vargroupvalues, varvalues);
					en.setValue(vargroupvalues, varvalues, weightvalue);
					enmiss.setValue(vargroupvalues, varvalues, weightvalue);
					essq.setValue(vargroupvalues, varvalues, weightvalue);
					evar.setValue(vargroupvalues, varvalues, weightvalue);
					esq.setValue(vargroupvalues, varvalues, weightvalue);
					ennp.setValue(vargroupvalues, varvalues, 1);
				}
			}
		}
		data.close();
		if ((validgroup==0) && (where==null))
			return new Result("%666%<br>\n", false, null);
		if ((validgroup==0) && (where!=null))
			return new Result("%2804%<br>\n", false, null);

		emean.calculate();
		estd.calculate();
		essq.calculate();
		evar.calculate();
		vgm.calculate();
		Hashtable<Vector<String>, double[]> mean=emean.getresult();
		Hashtable<Vector<String>, double[]> std=estd.getresult();
		Hashtable<Vector<String>, double[]> sum=esum.getresult();
		Hashtable<Vector<String>, double[]> max=emax.getresult();
		Hashtable<Vector<String>, double[]> mini=emin.getresult();
		Hashtable<Vector<String>, double[]> n=en.getresult();
		Hashtable<Vector<String>, double[]> nmiss=enmiss.getresult();
		Hashtable<Vector<String>, double[]> ssq=essq.getresult();
		Hashtable<Vector<String>, double[]> vari=evar.getresult();
		Hashtable<Vector<String>, double[]> sq=esq.getresult();
		Hashtable<Vector<String>, double[]> nnp=ennp.getresult();

		Hashtable<Vector<String>, double[]> ttest1=new Hashtable<Vector<String>, double[]>();
		Hashtable<Vector<String>, double[]> ttest2=new Hashtable<Vector<String>, double[]>();
		Hashtable<Vector<String>, double[]> pttest1=new Hashtable<Vector<String>, double[]>();
		Hashtable<Vector<String>, double[]> pttest2=new Hashtable<Vector<String>, double[]>();
		Hashtable<Vector<String>, double[]> vartest=new Hashtable<Vector<String>, double[]>();
		Hashtable<Vector<String>, double[]> pvartest=new Hashtable<Vector<String>, double[]>();
		if (dottest)
		{
			TTestEvaluator ettest=new TTestEvaluator(sum, n, sq, samplevariance, nnp);
			ttest1=ettest.gettest1();
			ttest2=ettest.gettest2();
			pttest1=ettest.getptest1();
			pttest2=ettest.getptest2();
			vartest=ettest.getvartest();
			pvartest=ettest.getpvartest();
		}

		int totalgroupmodalities=vgm.getTotal();

		Vector<Hashtable<String, String>> groupcodelabels=vgm.getgroupcodelabels();

		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);

		Hashtable<String, String> tempmd=new Hashtable<String, String>();

		if (selectstyle==1)
		{
			for (int j=0; j<varg.length; j++)
			{
				if (!noclforvg)
					dsu.addnewvarfromolddict(dict, varg[j], groupcodelabels.get(j), tempmd, "g_"+varg[j]);
				else
					dsu.addnewvarfromolddict(dict, varg[j], tempmd, tempmd, "g_"+varg[j]);
			}
			Hashtable<String, String> clvar=new Hashtable<String, String>();
			for (int j=0; j<var.length; j++)
			{
				clvar.put(var[j], dict.getvarlabelfromname(var[j]));
			}
			dsu.addnewvar("var", "%1056%", Keywords.TEXTSuffix, clvar, tempmd);
			for (int i=0; i<statistics.length; i++)
			{
				if (selectedoption[i]==1)
					dsu.addnewvar(statistics[i], statisticlabels[i], Keywords.NUMSuffix, tempmd, tempmd);
			}
		}

		else if (selectstyle==2)
		{
			for (int j=0; j<varg.length; j++)
			{
				if (!noclforvg)
					dsu.addnewvarfromolddict(dict, varg[j], groupcodelabels.get(j), tempmd, "g_"+varg[j]);
				else
					dsu.addnewvarfromolddict(dict, varg[j], tempmd, tempmd, "g_"+varg[j]);
			}
			Hashtable<String, String> codelabelstat=new Hashtable<String, String>();
			for (int i=0; i<statistics.length; i++)
			{
				if (selectedoption[i]==1)
					codelabelstat.put(String.valueOf(i), statisticlabels[i]);
			}
			dsu.addnewvar("statistic", "%1057%", Keywords.TEXTSuffix, codelabelstat, tempmd);
			for (int j=0; j<var.length; j++)
			{
				dsu.addnewvarfromolddict(dict, var[j], tempmd, tempmd, "v_"+var[j]);
			}
		}

		else
		{
			for (int j=0; j<varg.length; j++)
			{
				if (!noclforvg)
					dsu.addnewvarfromolddict(dict, varg[j], groupcodelabels.get(j), tempmd, "g_"+varg[j]);
				else
					dsu.addnewvarfromolddict(dict, varg[j], tempmd, tempmd, "g_"+varg[j]);
			}
			int numvar=0;
			for (int h=0; h<statistics.length; h++)
			{
				if (selectedoption[h]==1)
				{
					for (int j=0; j<var.length; j++)
					{
						String varlabel=statisticlabels[h].toUpperCase()+": "+dict.getvarlabelfromname(var[j]);
						dsu.addnewvar("v"+String.valueOf(numvar), varlabel, Keywords.NUMSuffix, tempmd, tempmd);
						numvar++;
					}
				}
			}
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		if (totalgroupmodalities==0)
			totalgroupmodalities=1;

		if (selectstyle==1)
		{
			String[] valuestowrite=new String[varg.length+1+totalselectedoption];
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
				for (int j=0; j<var.length; j++)
				{
					valuestowrite[varg.length]=var[j];
					int pointer=0;
					if (selectedoption[0]==1)
					{
						double[] tempstd=std.get(rifmodgroup);
						double[] tempn=n.get(rifmodgroup);
						try
						{
							double tempval=tempstd[j]/Math.sqrt(tempn[j]);
							int obs=(new Double(Math.rint(tempn[j]-1))).intValue();
							tempval=Probability.studentTInverse(alpha, obs)*tempval;
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+pointer]=String.valueOf(tempval);
							else
								valuestowrite[varg.length+1+pointer]="";
						}
						catch (Exception e)
						{
							valuestowrite[varg.length+1+pointer]="";
						}
						pointer++;
					}
					if (selectedoption[1]==1)
					{
						double[] tempssq=ssq.get(rifmodgroup);
						try
						{
							double tempval=tempssq[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+pointer]=String.valueOf(tempval);
							else
								valuestowrite[varg.length+1+pointer]="";
						}
						catch (Exception e)
						{
							valuestowrite[varg.length+1+pointer]="";
						}
						pointer++;
					}
					if (selectedoption[2]==1)
					{
						double[] tempmean=mean.get(rifmodgroup);
						double[] tempstd=std.get(rifmodgroup);
						try
						{
							double tempval=100*tempstd[j]/tempmean[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+pointer]=String.valueOf(tempval);
							else
								valuestowrite[varg.length+1+pointer]="";
						}
						catch (Exception e)
						{
							valuestowrite[varg.length+1+pointer]="";
						}
						pointer++;
					}
					if (selectedoption[3]==1)
					{
						double[] tempmax=max.get(rifmodgroup);
						try
						{
							double tempval=tempmax[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+pointer]=String.valueOf(tempval);
							else
								valuestowrite[varg.length+1+pointer]="";
						}
						catch (Exception e)
						{
							valuestowrite[varg.length+1+pointer]="";
						}
						pointer++;
					}
					if (selectedoption[4]==1)
					{
						double[] tempmean=mean.get(rifmodgroup);
						try
						{
							double tempval=tempmean[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+pointer]=String.valueOf(tempval);
							else
								valuestowrite[varg.length+1+pointer]="";
						}
						catch (Exception e)
						{
							valuestowrite[varg.length+1+pointer]="";
						}
						pointer++;
					}
					if (selectedoption[5]==1)
					{
						double[] tempmin=mini.get(rifmodgroup);
						try
						{
							double tempval=tempmin[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+pointer]=String.valueOf(tempval);
							else
								valuestowrite[varg.length+1+pointer]="";
						}
						catch (Exception e)
						{
							valuestowrite[varg.length+1+pointer]="";
						}
						pointer++;
					}
					if (selectedoption[6]==1)
					{
						double[] tempn=n.get(rifmodgroup);
						try
						{
							double tempval=tempn[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+pointer]=String.valueOf(tempval);
							else
								valuestowrite[varg.length+1+pointer]="0";
						}
						catch (Exception e)
						{
							valuestowrite[varg.length+1+pointer]="0";
						}
						pointer++;
					}
					if (selectedoption[7]==1)
					{
						double[] tempnmiss=nmiss.get(rifmodgroup);
						try
						{
							double tempval=tempnmiss[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+pointer]=String.valueOf(tempval);
							else
								valuestowrite[varg.length+1+pointer]="0";
						}
						catch (Exception e)
						{
							valuestowrite[varg.length+1+pointer]="0";
						}
						pointer++;
					}
					if (selectedoption[8]==1)
					{
						double[] tempmax=max.get(rifmodgroup);
						double[] tempmin=mini.get(rifmodgroup);
						try
						{
							double tempval=tempmax[j]-tempmin[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+pointer]=String.valueOf(tempval);
							else
								valuestowrite[varg.length+1+pointer]="0";
						}
						catch (Exception e)
						{
							valuestowrite[varg.length+1+pointer]="0";
						}
						pointer++;
					}
					if (selectedoption[9]==1)
					{
						double[] tempstd=std.get(rifmodgroup);
						try
						{
							double tempval=tempstd[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+pointer]=String.valueOf(tempval);
							else
								valuestowrite[varg.length+1+pointer]="";
						}
						catch (Exception e)
						{
							valuestowrite[varg.length+1+pointer]="";
						}
						pointer++;
					}
					if (selectedoption[10]==1)
					{
						double[] tempsum=sum.get(rifmodgroup);
						try
						{
							double tempval=tempsum[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+pointer]=String.valueOf(tempval);
							else
								valuestowrite[varg.length+1+pointer]="";
						}
						catch (Exception e)
						{
							valuestowrite[varg.length+1+pointer]="";
						}
						pointer++;
					}
					if (selectedoption[11]==1)
					{
						double[] tempttest=ttest1.get(rifmodgroup);
						try
						{
							double tempval=tempttest[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+pointer]=String.valueOf(tempval);
							else
								valuestowrite[varg.length+1+pointer]="";
						}
						catch (Exception e)
						{
							valuestowrite[varg.length+1+pointer]="";
						}
						pointer++;
					}
					if (selectedoption[12]==1)
					{
						double[] tempttest=ttest2.get(rifmodgroup);
						try
						{
							double tempval=tempttest[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+pointer]=String.valueOf(tempval);
							else
								valuestowrite[varg.length+1+pointer]="";
						}
						catch (Exception e)
						{
							valuestowrite[varg.length+1+pointer]="";
						}
						pointer++;
					}
					if (selectedoption[13]==1)
					{
						double[] tempttest=pttest1.get(rifmodgroup);
						try
						{
							double tempval=tempttest[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+pointer]=String.valueOf(tempval);
							else
								valuestowrite[varg.length+1+pointer]="";
						}
						catch (Exception e)
						{
							valuestowrite[varg.length+1+pointer]="";
						}
						pointer++;
					}
					if (selectedoption[14]==1)
					{
						double[] tempttest=pttest2.get(rifmodgroup);
						try
						{
							double tempval=tempttest[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+pointer]=String.valueOf(tempval);
							else
								valuestowrite[varg.length+1+pointer]="";
						}
						catch (Exception e)
						{
							valuestowrite[varg.length+1+pointer]="";
						}
						pointer++;
					}
					if (selectedoption[15]==1)
					{
						double[] temptvar=vari.get(rifmodgroup);
						try
						{
							double tempval=temptvar[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+pointer]=String.valueOf(tempval);
							else
								valuestowrite[varg.length+1+pointer]="";
						}
						catch (Exception e)
						{
							valuestowrite[varg.length+1+pointer]="";
						}
						pointer++;
					}
					if (selectedoption[16]==1)
					{
						double[] tempvartest=vartest.get(rifmodgroup);
						try
						{
							double tempval=tempvartest[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+pointer]=String.valueOf(tempval);
							else
								valuestowrite[varg.length+1+pointer]="";
						}
						catch (Exception e)
						{
							valuestowrite[varg.length+1+pointer]="";
						}
						pointer++;
					}
					if (selectedoption[17]==1)
					{
						double[] temppvartest=pvartest.get(rifmodgroup);
						try
						{
							double tempval=temppvartest[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+pointer]=String.valueOf(tempval);
							else
								valuestowrite[varg.length+1+pointer]="";
						}
						catch (Exception e)
						{
							valuestowrite[varg.length+1+pointer]="";
						}
						pointer++;
					}
					dw.write(valuestowrite);
				}
			}
		}

		if (selectstyle==2)
		{
			String[] valuestowrite=new String[varg.length+1+var.length];
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
				if (selectedoption[0]==1)
				{
					valuestowrite[varg.length]=String.valueOf(0);
					double[] tempstd=std.get(rifmodgroup);
					double[] tempn=n.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+1+j]="";
						try
						{
							double tempval=tempstd[j]/Math.sqrt(tempn[j]);
							int obs=(new Double(Math.rint(tempn[j]-1))).intValue();
							tempval=Probability.studentTInverse(alpha, obs)*tempval;
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					dw.write(valuestowrite);
				}
				if (selectedoption[1]==1)
				{
					valuestowrite[varg.length]=String.valueOf(1);
					double[] tempssq=ssq.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+1+j]="";
						try
						{
							double tempval=tempssq[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					dw.write(valuestowrite);
				}
				if (selectedoption[2]==1)
				{
					valuestowrite[varg.length]=String.valueOf(2);
					double[] tempmean=mean.get(rifmodgroup);
					double[] tempstd=std.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+1+j]="";
						try
						{
							double tempval=100*tempstd[j]/tempmean[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					dw.write(valuestowrite);
				}
				if (selectedoption[3]==1)
				{
					valuestowrite[varg.length]=String.valueOf(3);
					double[] tempmax=max.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+1+j]="";
						try
						{
							double tempval=tempmax[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					dw.write(valuestowrite);
				}
				if (selectedoption[4]==1)
				{
					valuestowrite[varg.length]=String.valueOf(4);
					double[] tempmean=mean.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+1+j]="";
						try
						{
							double tempval=tempmean[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					dw.write(valuestowrite);
				}
				if (selectedoption[5]==1)
				{
					valuestowrite[varg.length]=String.valueOf(5);
					double[] tempmin=mini.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+1+j]="";
						try
						{
							double tempval=tempmin[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					dw.write(valuestowrite);
				}
				if (selectedoption[6]==1)
				{
					valuestowrite[varg.length]=String.valueOf(6);
					double[] tempn=n.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+1+j]="0";
						try
						{
							double tempval=tempn[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					dw.write(valuestowrite);
				}
				if (selectedoption[7]==1)
				{
					valuestowrite[varg.length]=String.valueOf(7);
					double[] tempnmiss=nmiss.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+1+j]="0";
						try
						{
							double tempval=tempnmiss[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					dw.write(valuestowrite);
				}
				if (selectedoption[8]==1)
				{
					valuestowrite[varg.length]=String.valueOf(8);
					double[] tempmax=max.get(rifmodgroup);
					double[] tempmin=mini.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+1+j]="";
						try
						{
							double tempval=tempmax[j]-tempmin[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					dw.write(valuestowrite);
				}
				if (selectedoption[9]==1)
				{
					valuestowrite[varg.length]=String.valueOf(9);
					double[] tempstd=std.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+1+j]="";
						try
						{
							double tempval=tempstd[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					dw.write(valuestowrite);
				}
				if (selectedoption[10]==1)
				{
					valuestowrite[varg.length]=String.valueOf(10);
					double[] tempsum=sum.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+1+j]="";
						try
						{
							double tempval=tempsum[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					dw.write(valuestowrite);
				}
				if (selectedoption[11]==1)
				{
					valuestowrite[varg.length]=String.valueOf(11);
					double[] tempttest=ttest1.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+1+j]="";
						try
						{
							double tempval=tempttest[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					dw.write(valuestowrite);
				}
				if (selectedoption[12]==1)
				{
					valuestowrite[varg.length]=String.valueOf(12);
					double[] tempttest=ttest2.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+1+j]="";
						try
						{
							double tempval=tempttest[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					dw.write(valuestowrite);
				}
				if (selectedoption[13]==1)
				{
					valuestowrite[varg.length]=String.valueOf(13);
					double[] tempttest=pttest1.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+1+j]="";
						try
						{
							double tempval=tempttest[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					dw.write(valuestowrite);
				}
				if (selectedoption[14]==1)
				{
					valuestowrite[varg.length]=String.valueOf(14);
					double[] tempttest=pttest2.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+1+j]="";
						try
						{
							double tempval=tempttest[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					dw.write(valuestowrite);
				}
				if (selectedoption[15]==1)
				{
					valuestowrite[varg.length]=String.valueOf(15);
					double[] temptvar=vari.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+1+j]="";
						try
						{
							double tempval=temptvar[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					dw.write(valuestowrite);
				}
				if (selectedoption[16]==1)
				{
					valuestowrite[varg.length]=String.valueOf(16);
					double[] temptvar=vartest.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+1+j]="";
						try
						{
							double tempval=temptvar[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					dw.write(valuestowrite);
				}
				if (selectedoption[17]==1)
				{
					valuestowrite[varg.length]=String.valueOf(17);
					double[] temptvar=pvartest.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+1+j]="";
						try
						{
							double tempval=temptvar[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+1+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					dw.write(valuestowrite);
				}
			}
		}

		if (selectstyle==3)
		{
			String[] valuestowrite=new String[varg.length+totalselectedoption*var.length];
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
				int pointer=0;
				if (selectedoption[0]==1)
				{
					valuestowrite[varg.length]=String.valueOf(0);
					double[] tempstd=std.get(rifmodgroup);
					double[] tempn=n.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+j]="";
						try
						{
							double tempval=tempstd[j]/Math.sqrt(tempn[j]);
							int obs=(new Double(Math.rint(tempn[j]-1))).intValue();
							tempval=Probability.studentTInverse(alpha, obs)*tempval;
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					pointer=pointer+var.length;
				}
				if (selectedoption[1]==1)
				{
					double[] tempssq=ssq.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+pointer+j]="";
						try
						{
							double tempval=tempssq[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+pointer+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					pointer=pointer+var.length;
				}
				if (selectedoption[2]==1)
				{
					double[] tempmean=mean.get(rifmodgroup);
					double[] tempstd=std.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+pointer+j]="";
						try
						{
							double tempval=100*tempstd[j]/tempmean[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+pointer+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					pointer=pointer+var.length;
				}
				if (selectedoption[3]==1)
				{
					double[] tempmax=max.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+pointer+j]="";
						try
						{
							double tempval=tempmax[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+pointer+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					pointer=pointer+var.length;
				}
				if (selectedoption[4]==1)
				{
					double[] tempmean=mean.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+pointer+j]="";
						try
						{
							double tempval=tempmean[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+pointer+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					pointer=pointer+var.length;
				}
				if (selectedoption[5]==1)
				{
					double[] tempmin=mini.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+pointer+j]="";
						try
						{
							double tempval=tempmin[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+pointer+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					pointer=pointer+var.length;
				}
				if (selectedoption[6]==1)
				{
					double[] tempn=n.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+pointer+j]="0";
						try
						{
							double tempval=tempn[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+pointer+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					pointer=pointer+var.length;
				}
				if (selectedoption[7]==1)
				{
					double[] tempnmiss=nmiss.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+pointer+j]="0";
						try
						{
							double tempval=tempnmiss[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+pointer+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					pointer=pointer+var.length;
				}
				if (selectedoption[8]==1)
				{
					double[] tempmax=max.get(rifmodgroup);
					double[] tempmin=mini.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+pointer+j]="";
						try
						{
							double tempval=tempmax[j]-tempmin[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+pointer+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					pointer=pointer+var.length;
				}
				if (selectedoption[9]==1)
				{
					double[] tempstd=std.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+pointer+j]="";
						try
						{
							double tempval=tempstd[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+pointer+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					pointer=pointer+var.length;
				}
				if (selectedoption[10]==1)
				{
					double[] tempsum=sum.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+pointer+j]="";
						try
						{
							double tempval=tempsum[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+pointer+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					pointer=pointer+var.length;
				}
				if (selectedoption[11]==1)
				{
					double[] tempttest=ttest1.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+pointer+j]="";
						try
						{
							double tempval=tempttest[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+pointer+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					pointer=pointer+var.length;
				}
				if (selectedoption[12]==1)
				{
					double[] tempttest=ttest2.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+pointer+j]="";
						try
						{
							double tempval=tempttest[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+pointer+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					pointer=pointer+var.length;
				}
				if (selectedoption[13]==1)
				{
					double[] tempttest=pttest1.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+pointer+j]="";
						try
						{
							double tempval=tempttest[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+pointer+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					pointer=pointer+var.length;
				}
				if (selectedoption[14]==1)
				{
					double[] tempttest=pttest2.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+pointer+j]="";
						try
						{
							double tempval=tempttest[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+pointer+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					pointer=pointer+var.length;
				}
				if (selectedoption[15]==1)
				{
					double[] temptvar=vari.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+pointer+j]="";
						try
						{
							double tempval=temptvar[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+pointer+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					pointer=pointer+var.length;
				}
				if (selectedoption[16]==1)
				{
					double[] temptvar=vartest.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+pointer+j]="";
						try
						{
							double tempval=temptvar[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+pointer+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					pointer=pointer+var.length;
				}
				if (selectedoption[17]==1)
				{
					double[] temptvar=pvartest.get(rifmodgroup);
					for (int j=0; j<var.length; j++)
					{
						valuestowrite[varg.length+pointer+j]="";
						try
						{
							double tempval=temptvar[j];
							if (!Double.isNaN(tempval))
								valuestowrite[varg.length+pointer+j]=String.valueOf(tempval);
						}
						catch (Exception e) {}
					}
					pointer=pointer+var.length;
				}
				dw.write(valuestowrite);
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
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 440, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", true, 641, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.weight, "vars=all", false, 656, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.statistic, "listmultiple=663_"+Keywords.CLM+",664_"+
		Keywords.CSS+",665_"+Keywords.CV+",680_"+Keywords.MAX+",681_"+Keywords.MEAN+",682_"+Keywords.MIN+
		",683_"+Keywords.N+",684_"+Keywords.NMISS+",685_"+Keywords.RANGE+",686_"+Keywords.STD+
		",687_"+Keywords.SUM+",688_"+Keywords.TTEST1+",709_"+Keywords.TTEST2+",689_"+Keywords.PTTEST1+
		",710_"+Keywords.PTTEST2+",690_"+Keywords.VARIANCE+",1778_"+Keywords.VARTEST+",1779_"+Keywords.PVARTEST, true, 691, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.alpha,"text", false, 673,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.outstyle, "listsingle=674_"+Keywords.varrow+",675_"+Keywords.varcol+",676_"+Keywords.aggreg, true, 677, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.samplevariance, "checkbox", false, 861, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.novgconvert, "checkbox", false, 2227, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.noclforvg, "checkbox", false, 2229, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.orderclbycode, "checkbox", false, 2231, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.usewritefmt, "checkbox", false, 2275, dep, "", 2));
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
		retprocinfo[1]="679";
		return retprocinfo;
	}
}
