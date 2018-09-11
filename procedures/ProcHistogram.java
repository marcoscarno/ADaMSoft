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
import org.jfree.chart.ChartUtilities;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
import java.util.TreeMap;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.xy.XYBarDataset;
import org.jfree.data.xy.XYIntervalSeriesCollection;

import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.gui.GraphViewer;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.VectorStringComparator;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that evaluate the histogram for a variable, optionally by considering a weight and a grouping variable
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcHistogram implements RunStep
{
	/**
	* Starts the execution of Proc Histogram
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.dict, Keywords.vary};
		String [] optionalparameters=new String[] {Keywords.var, Keywords.varmin, Keywords.where, Keywords.varmax,Keywords.weight,Keywords.replace,Keywords.vertical,
													Keywords.vargroup,Keywords.imgwidth,Keywords.imgheight,Keywords.title,
													Keywords.labelx,Keywords.labely,Keywords.outjpg,Keywords.nolegend, Keywords.barwidth};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		double bw=1;

		String tempvar=(String)parameters.get(Keywords.var.toLowerCase());
		String tempvarmin=(String)parameters.get(Keywords.varmin.toLowerCase());
		String tempvarmax=(String)parameters.get(Keywords.varmax.toLowerCase());
		String tempvary=(String)parameters.get(Keywords.vary.toLowerCase());

		String tempbw=(String)parameters.get(Keywords.barwidth);

		if ((tempvar!=null) && (tempvarmin!=null))
			return new Result("%1526%<br>\n", false, null);

		if ((tempvar!=null) && (tempvarmax!=null))
			return new Result("%1526%<br>\n", false, null);

		if ((tempvarmin!=null) && (tempvarmax==null))
			return new Result("%1527%<br>\n", false, null);

		if ((tempvarmin==null) && (tempvarmax!=null))
			return new Result("%1527%<br>\n", false, null);

		String[] t=new String[0];
		if (tempvar!=null)
		{
			t=tempvar.split(" ");
			if (t.length!=1)
				return new Result("%1528%<br>\n", false, null);
		}
		if (tempvarmin!=null)
		{
			t=tempvarmin.split(" ");
			if (t.length!=1)
				return new Result("%1529%<br>\n", false, null);
		}
		if (tempvarmax!=null)
		{
			t=tempvarmax.split(" ");
			if (t.length!=1)
				return new Result("%1530%<br>\n", false, null);
		}
		if (tempvary!=null)
		{
			t=tempvary.split(" ");
			if (t.length!=1)
				return new Result("%1531%<br>\n", false, null);
		}

		String tempvarg=(String)parameters.get(Keywords.vargroup.toLowerCase());
		String tempweight=(String)parameters.get(Keywords.weight.toLowerCase());
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

		if(tempbw!=null)
		{
			try
			{
				bw= Double.parseDouble(tempbw);
			}
			catch (Exception e)
			{
				return new Result("%1537%<br>\n", false, null);
			}
		}

		String title = (String)parameters.get(Keywords.title);
		String labelx = (String)parameters.get(Keywords.labelx);
		String labely = (String)parameters.get(Keywords.labely);
		String legend = (String)parameters.get(Keywords.nolegend);

		boolean interval=false;

		if (tempvar==null)
		{
			tempvar=tempvarmin+" "+tempvarmax;
			interval=true;
		}

		tempvar=tempvar+" "+tempvary;

		TreeMap<Vector<String>, XYSeries> xyseries=new TreeMap<Vector<String>, XYSeries>(new VectorStringComparator());
		TreeMap<Vector<String>, XYIntervalSeries> xyintervalseries=new TreeMap<Vector<String>, XYIntervalSeries>(new VectorStringComparator());

		String replace =(String)parameters.get(Keywords.replace);

		VariableUtilities varu=new VariableUtilities(dict, tempvarg, null, tempweight, tempvar, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String[] var=varu.getrowvar();
		String[] varg=varu.getgroupvar();

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
		int[] weightrule=varu.getweightruleforsel();

		ValuesParser vp=new ValuesParser(null, grouprule, null, rowrule, null, weightrule);

		boolean errorinval=false;

		String gTitle="";
		if (varg.length>0)
		{
			for (int j=0; j<varg.length; j++)
			{
				gTitle+=dict.getvarlabelfromname(varg[j]);
				if (j<(varg.length-1))
					gTitle+="; ";
			}
			gTitle=" ("+gTitle+")";
		}

		int validgroup=0;
		while (!data.isLast())
		{
			String[] values = data.getRecord();
			if (values!=null)
			{
				Vector<String> vargroupvalues=vp.getvargroup(values);
				double[] varrowvalues = vp.getrowvarasdouble(values);
				double weightvalue=vp.getweight(values);
				if ((vp.vargroupisnotmissing(vargroupvalues)) && (!Double.isNaN(weightvalue)))
				{
					if (!interval)
					{
						if ((!Double.isNaN(varrowvalues[0])) && (!Double.isNaN(varrowvalues[1])))
						{
							validgroup++;
							if (xyseries.containsKey(vargroupvalues))
							{
								XYSeries tempxyseries=xyseries.get(vargroupvalues);
								tempxyseries.add(varrowvalues[0], varrowvalues[1]*weightvalue);
								xyseries.put(vargroupvalues, tempxyseries);
							}
							else
							{
								String series=dict.getvarlabelfromname(var[1]);
								String groupvalue="";
								for (int j=0; j<vargroupvalues.size(); j++)
								{
									String tempgroupvalue=vargroupvalues.get(j);
									if (tempgroupvalue!=null)
									{
										groupvalue=groupvalue+" "+tempgroupvalue;
										if (j<(vargroupvalues.size()-1))
											groupvalue=groupvalue+"; ";
									}
								}
								if (!groupvalue.equals(""))
									series=series+" ("+groupvalue+")";
								XYSeries tempxyseries=new XYSeries(series);
								tempxyseries.add(varrowvalues[0], varrowvalues[1]*weightvalue);
								xyseries.put(vargroupvalues, tempxyseries);
							}
						}
					}
					else
					{
						if ((!Double.isNaN(varrowvalues[0])) && (!Double.isNaN(varrowvalues[1])) && (!Double.isNaN(varrowvalues[2])))
						{
							if (varrowvalues[0]>varrowvalues[1])
								errorinval=true;
							validgroup++;
							if (xyintervalseries.containsKey(vargroupvalues))
							{
								XYIntervalSeries tempxyintervalseries=xyintervalseries.get(vargroupvalues);
								tempxyintervalseries.add((varrowvalues[0]+varrowvalues[1])/2, varrowvalues[0], varrowvalues[1], varrowvalues[2], varrowvalues[2], varrowvalues[2]);
								xyintervalseries.put(vargroupvalues, tempxyintervalseries);
							}
							else
							{
								String series=dict.getvarlabelfromname(var[2]);
								String groupvalue="";
								for (int j=0; j<vargroupvalues.size(); j++)
								{
									String tempgroupvalue=vargroupvalues.get(j);
									if (tempgroupvalue!=null)
									{
										groupvalue=groupvalue+" "+tempgroupvalue;
										if (j<(vargroupvalues.size()-1))
											groupvalue=groupvalue+"; ";
									}
								}
								if (!groupvalue.equals(""))
									series=series+" ("+groupvalue+")";
								XYIntervalSeries tempxyintervalseries=new XYIntervalSeries(series);
								tempxyintervalseries.add((varrowvalues[0]+varrowvalues[1])/2, varrowvalues[0], varrowvalues[1], varrowvalues[2], varrowvalues[2], varrowvalues[2]);
								xyintervalseries.put(vargroupvalues, tempxyintervalseries);
							}
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
		if (errorinval)
			return new Result("%1532%<br>\n", false, null);

		Vector<JFreeChart> results = new Vector<JFreeChart>();
		Vector<StepResult> result = new Vector<StepResult>();
		if (!interval)
		{
			Iterator<Vector<String>> en = xyseries.keySet().iterator();
			while(en.hasNext())
			{
				Vector<String> vargroupvalues=en.next();
				String realtitle=title;
				if (realtitle==null)
					realtitle=dict.getvarlabelfromname(var[1]);
				String groupvalue="";
				for (int j=0; j<vargroupvalues.size(); j++)
				{
					String tempgroupvalue=vargroupvalues.get(j);
					if (tempgroupvalue!=null)
					{
						groupvalue=groupvalue+" "+tempgroupvalue;
						if (j<(vargroupvalues.size()-1))
							groupvalue=groupvalue+"; ";
					}
				}
				if (!groupvalue.equals(""))
					realtitle=realtitle+" ("+groupvalue+")";
				XYSeries tempxyseries=xyseries.get(vargroupvalues);
				XYSeriesCollection xyseriescollection = new XYSeriesCollection();
				xyseriescollection.addSeries(tempxyseries);
				XYBarDataset xybar=new XYBarDataset(xyseriescollection, bw);
				JFreeChart chart = ChartFactory.createXYBarChart
				(realtitle,
				labelx!=null?labelx:"",
				false,
				labely!=null?labely:"",
				xybar,
				vertical?PlotOrientation.VERTICAL:PlotOrientation.HORIZONTAL,
				(legend==null),
				true,
				false);
				results.add(chart);
			}
		}
		else
		{
			Iterator<Vector<String>> en = xyintervalseries.keySet().iterator();
			while(en.hasNext())
			{
				Vector<String> vargroupvalues=en.next();
				String realtitle=title;
				if (realtitle==null)
					realtitle=dict.getvarlabelfromname(var[2]);
				String groupvalue="";
				for (int j=0; j<vargroupvalues.size(); j++)
				{
					String tempgroupvalue=vargroupvalues.get(j);
					if (tempgroupvalue!=null)
					{
						groupvalue=groupvalue+" "+tempgroupvalue;
						if (j<(vargroupvalues.size()-1))
							groupvalue=groupvalue+"; ";
					}
				}
				if (!groupvalue.equals(""))
					realtitle=realtitle+" ("+groupvalue+")";
				XYIntervalSeries tempxyintervalseries=xyintervalseries.get(vargroupvalues);
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

		String workdirg=(String)parameters.get(Keywords.WorkDir);
		Iterator<JFreeChart> it = results.iterator();
		int i=0;
		while(it.hasNext())
		{
			if(outjpg==null)
			{
				new GraphViewer(it.next());
			}
			else
			{
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try
				{
					ChartUtilities.saveChartAsJPEG(new File(workdirg+"temp.jpg"), it.next(), width,eight);
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
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", false, 1521, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varmin, "vars=all", false, 1522, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varmax, "vars=all", false, 1523, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 1520, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vary, "vars=all", true, 1524, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.weight, "var=all", false, 797, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 1525, dep, "", 2));

		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.imgwidth,"text",false,798,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.imgheight,"text",false,799,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.title,"text",false,800,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.labelx,"text",false,1533,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.labely,"text",false,1534,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.barwidth,"text",false,1535,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 1536, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vertical, "checkbox", false, 809, dep, "", 2));

		parameters.add(new GetRequiredParameters(Keywords.outjpg, "filesave=.jpeg", false, 802, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.nolegend, "checkbox", false, 803, dep, "", 2));
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
		retprocinfo[1]="1519";
		return retprocinfo;
	}
}
