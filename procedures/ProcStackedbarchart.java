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
import java.text.DecimalFormat;
import java.util.TreeMap;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.StackedBarRenderer3D;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.gui.GraphViewer;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.VectorStringComparator;

/**
* This is the procedure that evaluate the Stackedbarchart for a variable that contains the bar name,
* a variable that contains the stack name, a variable that contains the value and another variable
* that contains the weight
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcStackedbarchart implements RunStep
{
	String wTitle;
	/**
	* Starts the execution of Proc Stackedbarchart
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		wTitle="";
		String [] requiredparameters=new String[] {Keywords.dict, Keywords.varbar};
		String [] optionalparameters=new String[] {Keywords.varstack, Keywords.where, Keywords.varvalue, Keywords.percent,Keywords.weight,Keywords.replace,
													Keywords.imgwidth,Keywords.imgheight,Keywords.title,
													Keywords.labelx,Keywords.labely,Keywords.outjpg,Keywords.nolegend};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String tempvarbar=(String)parameters.get(Keywords.varbar.toLowerCase());
		String tempvarstack=(String)parameters.get(Keywords.varstack.toLowerCase());
		String tempvarvalue=(String)parameters.get(Keywords.varvalue.toLowerCase());
		String tempweight=(String)parameters.get(Keywords.weight.toLowerCase());

		String[] testuvar=tempvarbar.trim().split(" ");
		if (testuvar.length>1)
			return new Result("%1892%<br>\n", false, null);

		if (tempvarstack!=null)
		{
			testuvar=tempvarstack.trim().split(" ");
			if (testuvar.length>1)
				return new Result("%1893%<br>\n", false, null);
		}
		if (tempvarvalue!=null)
		{
			testuvar=tempvarvalue.trim().split(" ");
			if (testuvar.length>1)
				return new Result("%1894%<br>\n", false, null);
		}

		if (tempweight!=null)
		{
			testuvar=tempweight.trim().split(" ");
			if (testuvar.length>1)
				return new Result("%1895%<br>\n", false, null);
		}
		if (tempvarstack==null)
			tempvarstack="";
		tempvarbar=tempvarbar+" "+tempvarstack;
		tempvarbar=tempvarbar.trim();

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

		String replace =(String)parameters.get(Keywords.replace);

		VariableUtilities varu=new VariableUtilities(dict, null, null, tempweight, tempvarbar, tempvarvalue);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String[] var=varu.getrowvar();

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

		int[] rowrule=varu.getrowruleforsel();
		int[] colrule=null;
		if (tempvarvalue!=null)
			colrule=varu.getcolruleforsel();
		int[] weightrule=varu.getweightruleforsel();

		ValuesParser vp=new ValuesParser(null, null, null, rowrule, colrule, weightrule);

		TreeMap<Vector<String>, Double> freqval=new TreeMap<Vector<String>, Double>(new VectorStringComparator());

		boolean perc =(parameters.get(Keywords.percent)!=null);

		int validgroup=0;
		while (!data.isLast())
		{
			String[] values = data.getRecord();
			if (values!=null)
			{
				double[] varcolvalues = null;
				if (tempvarvalue!=null)
					varcolvalues=vp.getcolvarasdouble(values);
				String[] varrowvalues = vp.getrowvar(values);
				double weightvalue=vp.getweight(values);
				if (!Double.isNaN(weightvalue))
				{
					validgroup++;
					Vector<String> aval=new Vector<String>();
					boolean notmissing=true;
					for (int i=0; i<varrowvalues.length; i++)
					{
						aval.add(varrowvalues[i]);
						if (varrowvalues[i].equals(""))
							notmissing=false;
					}
					if (notmissing)
					{
						if (!freqval.containsKey(aval))
						{
							if (tempvarvalue!=null)
							{
								if (!Double.isNaN(varcolvalues[0]))
								{
									weightvalue=weightvalue*varcolvalues[0];
									freqval.put(aval,new Double(weightvalue));
								}
							}
							else
								freqval.put(aval,new Double(weightvalue));
						}
						else
						{
							double tempval=(freqval.get(aval)).doubleValue();
							if (tempvarvalue!=null)
							{
								if (!Double.isNaN(varcolvalues[0]))
								{
									tempval=tempval+varcolvalues[0]*weightvalue;
									freqval.put(aval, new Double(tempval));
								}
							}
							else
							{
								freqval.put(aval, new Double(tempval+weightvalue));
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

		Vector<JFreeChart> results = new Vector<JFreeChart>();
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		JFreeChart chart;

		Iterator<Vector<String>> igv = freqval.keySet().iterator();
		while(igv.hasNext())
		{
			Vector<String> tempgroup=igv.next();
			double tempval=(freqval.get(tempgroup)).doubleValue();
			String seriesname=tempgroup.get(0);
			String valname="";
			if (tempgroup.size()==2)
				valname=tempgroup.get(1);
        	dataset.addValue(tempval, valname, seriesname);
		}
		String gtitle=dict.getvarlabelfromname(var[0]);
		if (var.length>1)
			gtitle=gtitle+" ("+dict.getvarlabelfromname(var[1])+")";

		chart = ChartFactory.createStackedBarChart3D(
		title!=null?title:gtitle,         // chart title
		labelx!=null?labelx:"",               // domain axis label
		labely!=null?labely:"",                  // range axis label
		dataset,                  // data
		PlotOrientation.VERTICAL, // orientation
		(legend==null),                     // include legend
		true,                     // tooltips?
		false                     // URLs?
		);

		if (perc)
		{
			CategoryPlot categoryplot = (CategoryPlot)chart.getPlot();
			NumberAxis numberaxis = (NumberAxis)categoryplot.getRangeAxis();
			numberaxis.setNumberFormatOverride(new DecimalFormat("0.0%"));
        	StackedBarRenderer3D stackedbarrenderer3d = (StackedBarRenderer3D)categoryplot.getRenderer();
        	stackedbarrenderer3d.setRenderAsPercentages(true);
        }

        results.add(chart);
		dataset = new DefaultCategoryDataset();

		Vector<StepResult> result = new Vector<StepResult>();
		String workdirg=(String)parameters.get(Keywords.WorkDir);
		Iterator<JFreeChart> it = results.iterator();
		int i=0;
		while(it.hasNext())
		{
			if(outjpg==null)
			{
				new GraphViewer(it.next());
			}
			else{
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				try {
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
				} catch (IOException e) {
					return new Result("%794%<br>\n", false, null);
				}
				String path=outjpg;
				if(results.size()>1){
					if(outjpg.indexOf('.')!=0){
						path= outjpg.replace(".",i++ +".");
					}
					else{
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
		parameters.add(new GetRequiredParameters(Keywords.varbar, "vars=all", true, 1897, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varstack, "vars=all", false, 1898, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varvalue, "vars=all", false, 1899, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.weight, "var=all", false, 797, dep, "", 2));

		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.imgwidth,"text",false,798,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.imgheight,"text",false,799,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.title,"text",false,800,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.labelx,"text",false,807,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.labely,"text",false,808,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.percent, "checkbox", false, 1900, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));

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
		retprocinfo[1]="1896";
		return retprocinfo;
	}
}
