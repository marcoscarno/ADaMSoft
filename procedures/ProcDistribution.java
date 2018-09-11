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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
import cern.jet.math.Arithmetic;
import ADaMSoft.algorithms.BinDivider;
import ADaMSoft.algorithms.MaxEvaluator;
import ADaMSoft.algorithms.MeanEvaluator;
import ADaMSoft.algorithms.MinEvaluator;
import ADaMSoft.algorithms.VarEvaluator;
import ADaMSoft.algorithms.VarGroupModalities;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.VariableUtilities;
import cern.jet.stat.Probability;
import ADaMSoft.algorithms.NEvaluator;
import ADaMSoft.algorithms.STDEvaluator;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that evaluates the frequency for each bin for a numerical variables
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class ProcDistribution extends ObjectTransformer implements RunStep
{
	private int bins;
	private int freedomDegree;
	private double binMin,binMax;
	/**
	* Starts the execution of Proc Distribution and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		boolean novgconvert=false;
		boolean noclforvg=false;
		boolean orderclbycode=false;
		boolean samplevariance=false;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.var};
		String [] optionalparameters=new String[] {Keywords.bins, Keywords.where, Keywords.userule, Keywords.distribution, Keywords.vargroup, Keywords.weight, Keywords.distdegree, Keywords.binmin, Keywords.binmax, Keywords.samplevariance, Keywords.replace, Keywords.novgconvert, Keywords.noclforvg, Keywords.orderclbycode};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		samplevariance =(parameters.get(Keywords.samplevariance)!=null);

		String rule=(String)parameters.get(Keywords.userule.toLowerCase());
		int selectrule=-1;
		if (rule!=null)
		{
			String[] rules=new String[] {Keywords.sturges, Keywords.scott};
			selectrule=steputilities.CheckOption(rules, rule);
			if (selectrule==0)
				return new Result("%1775% "+Keywords.userule.toUpperCase()+"<br>\n"+steputilities.getMessage(), false, null);
		}

		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		novgconvert =(parameters.get(Keywords.novgconvert)!=null);
		orderclbycode =(parameters.get(Keywords.orderclbycode)!=null);
		noclforvg =(parameters.get(Keywords.noclforvg)!=null);

		String vartemp=(String)parameters.get(Keywords.var.toLowerCase());
		String vargroup=(String)parameters.get(Keywords.vargroup.toLowerCase());
		String weight=(String)parameters.get(Keywords.weight.toLowerCase());
		String tmpBins=(String)parameters.get(Keywords.bins.toLowerCase());
		String distribution= (String)parameters.get(Keywords.distribution);
		String tmpFreedomDegree = (String)parameters.get(Keywords.distdegree);
		String tmpBinMax = (String)parameters.get(Keywords.binmax);
		String tmpBinMin = (String)parameters.get(Keywords.binmin);

		String[] t=new String[0];
		t=vartemp.split(" ");
		if (t.length!=1)
			return new Result("%1540%<br>\n", false, null);

		if(tmpBinMax!=null)
		{
			try
			{
				binMax = Double.parseDouble(tmpBinMax);
			}
			catch(NumberFormatException e)
			{
				return new Result("%845% (" + tmpBinMax+")<br>\n", false, null);
			}
		}

		if(tmpBinMin!=null)
		{
			try
			{
				binMin = Double.parseDouble(tmpBinMin);
			}
			catch(NumberFormatException e)
			{
				return new Result("%846% (" + tmpBinMax+")<br>\n", false, null);
			}
		}


		if(tmpFreedomDegree!=null)
		{
			try
			{
				freedomDegree = Integer.parseInt(tmpFreedomDegree);
			}
			catch(NumberFormatException e){
				return new Result("%847% (" + tmpFreedomDegree+")<br>\n", false, null);
			}
		}

		if (tmpBins==null)
		{
			tmpBins="3";
			if (rule==null)
				return new Result("%2238%<br>\n", false, null);
		}

		try
		{
			bins = Integer.parseInt(tmpBins);
		}
		catch(NumberFormatException e)
		{
			return new Result("%848% (" + tmpBins+")<br>\n", false, null);
		}
		if (bins<2)
			return new Result("%1875% (" + tmpBins+")<br>\n", false, null);
		boolean isdist=false;
		String[] thdists=new String[] {Keywords.chisquare, Keywords.normal, Keywords.poisson, Keywords.student};
		int seldist=0;

		if (distribution!=null)
		{
			isdist=true;
			seldist=steputilities.CheckOption(thdists, distribution);
			if (seldist==0)
				return new Result("%1775% "+Keywords.distribution.toUpperCase()+"<br>\n"+steputilities.getMessage(), false, null);
			else
				seldist=seldist-1;
			if ((seldist==0) || (seldist==3))
			{
				if (tmpFreedomDegree==null)
					return new Result("%849%<br>\n", false, null);
			}
		}

		VariableUtilities varu=new VariableUtilities(dict, vargroup, vartemp, weight, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String[] var=varu.getanalysisvar();
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

		if(var.length>1)
			return new Result("%529%<br>\n", false, null);

		String replace=(String)parameters.get(Keywords.replace);
		int[] replacerule=varu.getreplaceruleforsel(replace);

		String keyword="Distribution "+dict.getkeyword();
		String description="Distribution "+dict.getdescription();

		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

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

		MeanEvaluator emean=new MeanEvaluator();
		MaxEvaluator emax=null;
		if(tmpBinMax==null)
            emax=new MaxEvaluator();

		MinEvaluator emin=null;
		if(tmpBinMin==null)
			emin=new MinEvaluator();

		VarEvaluator evar=new VarEvaluator(samplevariance);

		NEvaluator en=new NEvaluator();
		STDEvaluator estd=new STDEvaluator(true);

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
		Vector<String> vargroupvalues=null;
		while (!data.isLast())
		{
			String[] values = data.getRecord();
			if (values!=null)
			{
				if (novgconvert)
					vargroupvalues=vp.getorigvargroup(values);
				else
					vargroupvalues=vp.getvargroup(values);
				double[] varvalues=vp.getanalysisvarasdouble(values);
				double weightvalue=vp.getweight(values);
				if (vp.vargroupisnotmissing(vargroupvalues))
				{
					validgroup++;
					vgm.updateModalities(vargroupvalues);
					emean.setValue(vargroupvalues, varvalues, weightvalue);
					if(tmpBinMax==null) emax.setValue(vargroupvalues, varvalues);
					if(tmpBinMin==null) emin.setValue(vargroupvalues, varvalues);
					evar.setValue(vargroupvalues, varvalues, weightvalue);
					if (selectrule>0)
					{
						en.setValue(vargroupvalues, varvalues, weightvalue);
						estd.setValue(vargroupvalues, varvalues, weightvalue);
					}
				}
			}
		}
		data.close();
		if ((validgroup==0) && (where!=null))
			return new Result("%2804%<br>\n", false, null);
		if ((validgroup==0) && (where==null))
			return new Result("%666%<br>\n", false, null);

		emean.calculate();
		evar.calculate();
		vgm.calculate();
		if (selectrule>0)
			estd.calculate();

		Hashtable<Vector<String>, double[]> mean=emean.getresult();
		Hashtable<Vector<String>, double[]> max=null;
		if(tmpBinMax==null) max=emax.getresult();
		Hashtable<Vector<String>, double[]> min=null;
		if(tmpBinMin==null)
			min=emin.getresult();
		Hashtable<Vector<String>, double[]> vari=evar.getresult();

		Hashtable<Vector<String>, double[]> std=null;
		Hashtable<Vector<String>, double[]> n=null;
		if (selectrule>0)
		{
			std=estd.getresult();
			n=en.getresult();
		}

		Hashtable<Vector<String>, Integer> nbin= new Hashtable<Vector<String>,Integer>();
		Hashtable<Vector<String>,BinDivider> bucket = new Hashtable<Vector<String>,BinDivider>();
		Iterator<Vector<String>> itMean = mean.keySet().iterator();
		while(itMean.hasNext())
		{
			Vector<String> key = itMean.next();
			if (selectrule==1)
			{
				try
				{
					double[] tn=n.get(key);
					bins=bins+(int)Arithmetic.log(2, tn[0]);
				}
				catch (Exception eb)
				{
					bins=1;
				}
			}
			else if (selectrule==2)
			{
				try
				{
					double[] tn=n.get(key);
					double[] ts=std.get(key);
					bins=(int)(3.5*ts[0]*Math.pow(tn[0],-1*(1/3)));
				}
				catch (Exception eb)
				{
					bins=1;
				}
			}
			double minVal = (tmpBinMin==null)?min.get(key)[0]:binMin;
			double maxVal = (tmpBinMax==null)?max.get(key)[0]:binMax;
			bucket.put(key,new BinDivider(bins, minVal, maxVal));
			nbin.put(key, new Integer(bins));
		}

		data = new DataReader(dict);
		if (!data.open(totalvar, replacerule, false))
			return new Result(data.getmessage(), false, null);

		while (!data.isLast())
		{
			String[] values = data.getRecord();
			if (novgconvert)
				vargroupvalues=vp.getorigvargroup(values);
			else
				vargroupvalues=vp.getvargroup(values);
			double[] varvalues=vp.getanalysisvarasdouble(values);
			double weightvalue=vp.getweight(values);
			BinDivider bin = bucket.get(vargroupvalues);
			if(bin!=null)
				bin.addValue(varvalues[0],weightvalue);
		}
		data.close();

		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);

		Hashtable<String, String> tempmd=new Hashtable<String, String>();

		Vector<Hashtable<String, String>> groupcodelabels=vgm.getgroupcodelabels();

		for (int j=0; j<varg.length; j++)
		{
			if (!noclforvg)
				dsu.addnewvarfromolddict(dict, varg[j], groupcodelabels.get(j), tempmd, "g_"+varg[j]);
			else
				dsu.addnewvarfromolddict(dict, varg[j], tempmd, tempmd, "g_"+varg[j]);
		}

		dsu.addnewvar("v"+String.valueOf(varg.length), "%1091%", Keywords.NUMSuffix, tempmd, tempmd);
		dsu.addnewvar("v"+String.valueOf(varg.length+1), "%1092%", Keywords.NUMSuffix, tempmd, tempmd);
		dsu.addnewvar("v"+String.valueOf(varg.length+2), "%1093%", Keywords.NUMSuffix, tempmd, tempmd);
		dsu.addnewvar("v"+String.valueOf(varg.length+3), "%1094%", Keywords.NUMSuffix, tempmd, tempmd);
		if (isdist)
			dsu.addnewvar("v"+String.valueOf(varg.length+4), thdists[seldist]+" %1095%", Keywords.NUMSuffix, tempmd, tempmd);

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		int totalgroupmodalities=vgm.getTotal();
		if (totalgroupmodalities==0)
			totalgroupmodalities=1;

		for (int v=0; v<totalgroupmodalities; v++)
		{
			Vector<String> vt=vgm.getvectormodalities(v);
			String[] buffer = new String[varg.length+(isdist?5:4)];
			BinDivider bin = bucket.get(vt);
			int lbin=(nbin.get(vt)).intValue();
			for(int j=0;j<lbin;j++)
			{
				int pos=0;
				for (int i=0; i<vt.size(); i++)
				{
					String groupvalue=vt.get(i);
					if (groupvalue!=null)
					{
						if (!noclforvg)
							buffer[pos++]=vgm.getcode(i, groupvalue);
						else
							buffer[pos++]=groupvalue;
					}
				}
				buffer[pos++]=String.valueOf(bin.getBinLowerEdge(j));
				buffer[pos++]=String.valueOf(bin.getBinUpperEdge(j));
				buffer[pos++]=String.valueOf(bin.getBinCentre(j));
				buffer[pos++]=String.valueOf(bin.getBinValuePercent(j));
				if (isdist)
				{
					double hfreq=-1;
					try
					{
						if (seldist==1)
						{
							double meanVal= mean.get(vt)[0];
							double varVal= vari.get(vt)[0];
							hfreq= Probability.normal(meanVal, varVal, bin.getBinUpperEdge(j))-Probability.normal(meanVal,varVal,bin.getBinLowerEdge(j));
						}
						else if(seldist==2)
						{
							double meanVal= mean.get(vt)[0];
							hfreq = Probability.poisson((int)Math.round(bin.getBinUpperEdge(j)),meanVal)-Probability.poisson((int)Math.round(bin.getBinLowerEdge(j)),meanVal);
						}
						else if(seldist==3)
						{
							hfreq = Probability.studentT(freedomDegree,bin.getBinUpperEdge(j))-Probability.studentT(freedomDegree,bin.getBinLowerEdge(j));
						}
						else if(seldist==0)
						{
							hfreq = Probability.chiSquare(freedomDegree,bin.getBinUpperEdge(j))-Probability.chiSquare(freedomDegree,bin.getBinLowerEdge(j));
						}
					}
					catch (Exception e) {}
					if (hfreq!=-1)
						buffer[pos++]=double2String(hfreq);
					else
						buffer[pos++]="";
				}
				dw.write(buffer);
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
		parameters.add(new GetRequiredParameters(Keywords.var, "var=all", true, 850, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.weight, "var=all", false, 656, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.bins, "text", false, 851, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 2233, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.userule, "listsingle=2234_NULL,2235_"+Keywords.sturges+",2236_"+Keywords.scott,false, 2237, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.binmin, "text", false, 852, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.binmax, "text", false, 853, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.distribution, "listsingle=891_NULL,855_"+Keywords.chisquare+",856_"+Keywords.normal+",857_"+Keywords.poisson+",858_"+Keywords.student, false, 859, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.distdegree, "text", false, 854, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.samplevariance, "checkbox", false, 861, dep, "", 2));
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
		retprocinfo[1]="860";
		return retprocinfo;
	}
}