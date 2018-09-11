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
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import cern.jet.math.Arithmetic;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;

import ADaMSoft.algorithms.BinDivider;
import ADaMSoft.algorithms.MaxEvaluator;
import ADaMSoft.algorithms.MeanEvaluator;
import ADaMSoft.algorithms.MinEvaluator;
import ADaMSoft.algorithms.VarGroupModalities;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.gui.GraphViewer;
import ADaMSoft.keywords.Keywords;

import ADaMSoft.algorithms.NEvaluator;
import ADaMSoft.algorithms.STDEvaluator;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.VariableUtilities;

/**
* This is the procedure that evaluates the frequency for each bin for a numerical variables and prints the corresponding graph
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class ProcDisthist extends ObjectTransformer implements RunStep
{
	boolean cumulative;
	private int bins;
	private double binMin, binMax;
	/**
	* Starts the execution of Proc Dishist and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		boolean novgconvert=false;
		boolean orderclbycode=false;
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		boolean percent=false;

		String [] requiredparameters=new String[] {Keywords.dict, Keywords.var};
		String [] optionalparameters=new String[] {Keywords.bins, Keywords.where, Keywords.vargroup, Keywords.weight, Keywords.percent,
		Keywords.binmin, Keywords.binmax, Keywords.imgwidth, Keywords.imgheight, Keywords.title,
		Keywords.labelx, Keywords.labely, Keywords.vertical,	Keywords.cumulative,
		Keywords.replace, Keywords.novgconvert, Keywords.orderclbycode, Keywords.userule};
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}

		String rule=(String)parameters.get(Keywords.userule.toLowerCase());
		int selectrule=-1;
		if (rule!=null)
		{
			String[] rules=new String[] {Keywords.sturges, Keywords.scott};
			selectrule=steputilities.CheckOption(rules, rule);
			if (selectrule==0)
				return new Result("%1775% "+Keywords.userule.toUpperCase()+"<br>\n"+steputilities.getMessage(), false, null);
		}

		cumulative =(parameters.get(Keywords.cumulative)!=null);
		percent =(parameters.get(Keywords.percent)!=null);

		boolean onegraph=(parameters.get(Keywords.onegraph)!=null);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		novgconvert =(parameters.get(Keywords.novgconvert)!=null);
		orderclbycode =(parameters.get(Keywords.orderclbycode)!=null);

		String vartemp=(String)parameters.get(Keywords.var.toLowerCase());
		String vargroup=(String)parameters.get(Keywords.vargroup.toLowerCase());
		String weight=(String)parameters.get(Keywords.weight.toLowerCase());
		String tmpBins=(String)parameters.get(Keywords.bins.toLowerCase());
		String tmpBinMax = (String)parameters.get(Keywords.binmax);
		String tmpBinMin = (String)parameters.get(Keywords.binmin);

		if (tmpBins==null)
		{
			tmpBins="3";
			if (rule==null)
				return new Result("%2238%<br>\n", false, null);
		}

		if (vargroup==null)
			onegraph=false;

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

		boolean vertical =(parameters.get(Keywords.vertical)!=null);

		int eight=400;
		int width=400;

		String sWidth = (String)parameters.get(Keywords.imgwidth);
		String sEight = (String)parameters.get(Keywords.imgheight);
		String outjpg = (String)parameters.get(Keywords.outjpg);
		if (outjpg !=null)
		{
			if (!outjpg.toLowerCase().endsWith(".jpg"))
				outjpg=outjpg+".jpg";
		}

		if(sEight!=null)
		{
			try
			{
				eight = Integer.parseInt(sEight);
			}
			catch (Exception e) {}
		}
		if(sWidth!=null)
		{
			try
			{
				width = Integer.parseInt(sWidth);
			}
			catch (Exception e) {}
		}

		String title = (String)parameters.get(Keywords.title);
		String labelx = (String)parameters.get(Keywords.labelx);
		String labely = (String)parameters.get(Keywords.labely);
		String legend = (String)parameters.get(Keywords.nolegend);

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
		if ((varg.length==0) && (orderclbycode))
		{
			result.add(new LocalMessageGetter("%2232%<br>\n"));
		}

		if(var.length>1)
			return new Result("%529%<br>\n", false, null);

		String replace=(String)parameters.get(Keywords.replace);
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

		MeanEvaluator emean=new MeanEvaluator();

		NEvaluator en=new NEvaluator();
		STDEvaluator estd=new STDEvaluator(true);

		MaxEvaluator emax=null;
		if(tmpBinMax==null)
            emax=new MaxEvaluator();

		MinEvaluator emin=null;
		if(tmpBinMin==null)
			emin=new MinEvaluator();

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
				if (!Double.isNaN(weightvalue))
				{
					if (vp.vargroupisnotmissing(vargroupvalues))
					{
						validgroup++;
						vgm.updateModalities(vargroupvalues);
						emean.setValue(vargroupvalues, varvalues, weightvalue);
						if (selectrule>0)
						{
							en.setValue(vargroupvalues, varvalues, weightvalue);
							estd.setValue(vargroupvalues, varvalues, weightvalue);
						}
						if(tmpBinMax==null) emax.setValue(vargroupvalues, varvalues);
						if(tmpBinMin==null) emin.setValue(vargroupvalues, varvalues);
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
		if (selectrule>0)
			estd.calculate();
		vgm.calculate();

		Hashtable<Vector<String>, double[]> mean=emean.getresult();
		Hashtable<Vector<String>, double[]> max=null;
		if(tmpBinMax==null) max=emax.getresult();
		Hashtable<Vector<String>, double[]> min=null;
		if(tmpBinMin==null)
			min=emin.getresult();

		Hashtable<Vector<String>, double[]> std=null;
		Hashtable<Vector<String>, double[]> n=null;
		if (selectrule>0)
		{
			std=estd.getresult();
			n=en.getresult();
		}

		Hashtable<Vector<String>,BinDivider> bucket = new Hashtable<Vector<String>,BinDivider>();
		Iterator<Vector<String>> itMean = mean.keySet().iterator();
		Hashtable<Vector<String>, Integer> nbin= new Hashtable<Vector<String>,Integer>();

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
			if (!Double.isNaN(weightvalue))
			{
				BinDivider bin = bucket.get(vargroupvalues);
				if(bin!=null)
					bin.addValue(varvalues[0],weightvalue);
			}
		}
		data.close();

		int totalgroupmodalities=vgm.getTotal();
		if (totalgroupmodalities==0)
			totalgroupmodalities=1;

		Vector<JFreeChart> results = new Vector<JFreeChart>();

		if (!onegraph)
		{
			for (int v=0; v<totalgroupmodalities; v++)
			{
				Vector<String> vt=vgm.getvectormodalities(v);
				String realtitle=title;
				String tempname=dict.getvarlabelfromname(var[0]);
				if (realtitle==null)
					realtitle=dict.getvarlabelfromname(var[0]);
				if(varg.length!=0)
				{
					realtitle=realtitle+" (";
					tempname=tempname+" (";
					for(int i=0;i<vt.size();i++)
					{
						realtitle=realtitle+dict.getvarlabelfromname(varg[i])+"="+vt.get(i);
						tempname=tempname+dict.getvarlabelfromname(varg[i])+"="+vt.get(i);
						if (i<(vt.size()-1))
						{
							realtitle=realtitle+"; ";
							tempname=tempname+"; ";
						}
					}
					tempname=tempname+")";
					realtitle=realtitle+")";
				}
				XYIntervalSeries tempxyintervalseries=new XYIntervalSeries(tempname);
				BinDivider bin = bucket.get(vt);
				double va=0;
				int lbin=(nbin.get(vt)).intValue();
				for(int j=0;j<lbin;j++)
				{
					if (!cumulative)
					{
						if (!percent)
							va=bin.getBinValue(j);
						else
							va=100*bin.getBinValuePercent(j);
					}
					else
					{
						if (!percent)
							va+=bin.getBinValue(j);
						else
							va+=100*bin.getBinValuePercent(j);
					}
					tempxyintervalseries.add((bin.getBinLowerEdge(j)+bin.getBinUpperEdge(j))/2,
					bin.getBinLowerEdge(j), bin.getBinUpperEdge(j), va, va, va);
				}
				XYIntervalSeriesCollection xyseriescollection = new XYIntervalSeriesCollection();
				xyseriescollection.addSeries(tempxyintervalseries);
				JFreeChart chart = ChartFactory.createXYBarChart
				(realtitle,
				labelx!=null?labelx:"",
				false,
				labely!=null?labely:"",
				xyseriescollection,
				vertical?PlotOrientation.VERTICAL:PlotOrientation.HORIZONTAL,
				(legend==null),
				true,
				false);
				results.add(chart);
			}
		}
		else
		{
			XYIntervalSeriesCollection xyseriescollection = new XYIntervalSeriesCollection();
			String realtitle=title;
			if (realtitle==null)
				realtitle=dict.getvarlabelfromname(var[0]);
			for (int v=0; v<totalgroupmodalities; v++)
			{
				Vector<String> vt=vgm.getvectormodalities(v);
				String tempname=dict.getvarlabelfromname(var[0]);
				if(varg.length!=0)
				{
					realtitle=realtitle+" (";
					tempname=tempname+" (";
					for(int i=0;i<vt.size();i++)
					{
						realtitle=realtitle+dict.getvarlabelfromname(varg[i])+"="+vt.get(i);
						tempname=tempname+dict.getvarlabelfromname(varg[i])+"="+vt.get(i);
						if (i<(vt.size()-1))
						{
							realtitle=realtitle+"; ";
							tempname=tempname+"; ";
						}
					}
					realtitle=realtitle+")";
					tempname=tempname+")";
				}
				XYIntervalSeries tempxyintervalseries=new XYIntervalSeries(tempname);
				BinDivider bin = bucket.get(vt);
				double va=0;
				int lbin=(nbin.get(vt)).intValue();
				for(int j=0;j<lbin;j++)
				{
					if (!cumulative)
					{
						if (!percent)
							va=bin.getBinValue(j);
						else
							va=100*bin.getBinValuePercent(j);
					}
					else
					{
						if (!percent)
							va+=bin.getBinValue(j);
						else
							va+=100*bin.getBinValuePercent(j);
					}
					tempxyintervalseries.add((bin.getBinLowerEdge(j)+bin.getBinUpperEdge(j))/2,
					bin.getBinLowerEdge(j), bin.getBinUpperEdge(j), va, va, va);
				}
				xyseriescollection.addSeries(tempxyintervalseries);
			}
			JFreeChart chart = ChartFactory.createXYBarChart
			(realtitle,
			labelx!=null?labelx:"",
			false,
			labely!=null?labely:"",
			xyseriescollection,
			vertical?PlotOrientation.VERTICAL:PlotOrientation.HORIZONTAL,
			(legend==null),
			true,
			false);
			results.add(chart);
		}
		String workdirg=(String)parameters.get(Keywords.WorkDir);
		Iterator<JFreeChart> itg = results.iterator();
		int i=0;
		while(itg.hasNext())
		{
			if(outjpg==null)
			{
				try
				{
					new GraphViewer(itg.next());
				}
				catch (Exception ee){}
			}
			else
			{
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try
				{
					ChartUtilities.saveChartAsJPEG(new File(workdirg+"temp.jpg"), itg.next(), width,eight);
					FileInputStream fin = new FileInputStream(new File(workdirg+"temp.jpg"));
					byte buffer[] = new byte[4096];
					int read = 0;
					do
					{
						read = fin.read(buffer);
						if(read != -1)
						{
							baos.write(buffer, 0, read);
						}
					} while(read != -1);
					fin.close();
				}
				catch (IOException e)
				{
					return new Result("%794%<br>\n", false, null);
				}
				String path=outjpg;
				if(results.size()>1)
				{
					if(outjpg.indexOf('.')!=0)
					{
						path= outjpg.replace(".",i++ +".");
					}
					else
					{
						return new Result("%795%<br>\n",false,null);
					}
				}
				result.add(new LocalFileSave(path,baos.toByteArray()));
				(new File(workdirg+"temp.jpg")).delete();
			}
		}
		return new Result("",true,result);
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 721, dep, "", 1));
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

		parameters.add(new GetRequiredParameters(Keywords.imgwidth,"text",false,798,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.imgheight,"text",false,799,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.title,"text",false,800,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.labelx,"text",false,1533,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.labely,"text",false,1534,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.vertical, "checkbox", false, 809, dep, "", 2));

		parameters.add(new GetRequiredParameters(Keywords.outjpg, "filesave=.jpeg", false, 802, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.nolegend, "checkbox", false, 803, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.percent, "checkbox", false, 1538, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.cumulative, "checkbox", false, 1541, dep, "", 2));

		parameters.add(new GetRequiredParameters(Keywords.onegraph, "checkbox", false, 1854, dep, "", 2));

		parameters.add(new GetRequiredParameters(Keywords.novgconvert, "checkbox", false, 2227, dep, "", 2));
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
		retprocinfo[0]="804";
		retprocinfo[1]="1539";
		return retprocinfo;
	}
}

