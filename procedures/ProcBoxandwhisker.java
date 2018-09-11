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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.chart.ChartUtilities;

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
* This is the procedure that creates a box and whisker plot
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class ProcBoxandwhisker implements RunStep
{
	String wTitle;
	/**
	* Starts the execution of Proc Boxandwhisker
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		wTitle="";
		String [] requiredparameters=new String[] {Keywords.dict,Keywords.var};
		String [] optionalparameters=new String[] {Keywords.replace, Keywords.vargroup,
					Keywords.imgwidth,Keywords.imgheight,Keywords.title,
					Keywords.labelx,Keywords.labely,Keywords.outjpg,Keywords.cross,Keywords.nolegend, Keywords.weight, Keywords.where};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String tempvar=(String)parameters.get(Keywords.var.toLowerCase());
		String tempvarg=(String)parameters.get(Keywords.vargroup.toLowerCase());
		boolean cross =(parameters.get(Keywords.cross)!=null);
		String weight=(String)parameters.get(Keywords.weight.toLowerCase());

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
		if(sEight!=null)
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

		VariableUtilities varu=new VariableUtilities(dict, tempvarg, null, weight, tempvar, null);
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

		String[] varRowLabels = new String[var.length];

		for(int i= 0;i<var.length;i++)
		{
			varRowLabels[i]= dict.getvarlabelfromname(var[i]);
			wTitle+=varRowLabels[i];
			if (i<(var.length-1))
				wTitle+=" - ";
		}

		int validgroup=0;
		TreeMap<Vector<String>,Vector<ArrayList<Double>>> lists = new TreeMap<Vector<String>,Vector<ArrayList<Double>>>(new VectorStringComparator());
		while (!data.isLast())
		{
			String[] values = data.getRecord();
			if (values!=null)
			{
				Vector<String> vargroupvalues=vp.getvargroup(values);
				String[] rowVars=vp.getrowvar(values);
				double weightvalue=vp.getweight(values);
				if (!Double.isNaN(weightvalue))
				{
					validgroup++;
					if (vp.vargroupisnotmissing(vargroupvalues))
					{
						Vector<ArrayList<Double>> element=null;
						if (!lists.containsKey(vargroupvalues))
						{
							element = new Vector<ArrayList<Double>>();
							for(int j=0;j<rowVars.length;j++)
							{
								element.add(new ArrayList<Double>());
							}
						}
						else
							element= lists.get(vargroupvalues);
						for (int i=0; i<rowVars.length; i++)
						{
							if (!rowVars[i].equals(""))
							{
								try
								{
									double tv=Double.parseDouble(rowVars[i]);
									element.get(i).add(new Double(tv*weightvalue));
								}
								catch (NumberFormatException e) {}
							}
						}
						lists.put(vargroupvalues, element);
					}
				}
			}
		}
		data.close();
		if (validgroup==0)
			return new Result("%2807%<br>\n", false, null);

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

		wTitle=wTitle.trim()+gTitle;

		Vector<JFreeChart> results = new Vector<JFreeChart>();
		DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset();
		JFreeChart chart;
		Iterator<Vector<String>> iter = lists.keySet().iterator();
		while(iter.hasNext())
		{
			Vector<String> el = iter.next();
			Vector<ArrayList<Double>> value = lists.get(el);
			for (int i=0; i<value.size(); i++)
			{
				if (varg.length==0)
					dataset.add(value.get(i), varRowLabels[i], "");
				else
				{
					String categoryLabel=el.get(0);
					for(int k=1;k<el.size()-1;k++)
					{
						categoryLabel+=el.get(i)+"; ";
					}
					if(el.size()>1)
					{
						categoryLabel+=el.get(el.size()-1);
					}
					dataset.add(value.get(i), varRowLabels[i]+gTitle, "("+categoryLabel+")");
				}
			}
			if (!cross)
			{
				chart = ChartFactory.createBoxAndWhiskerChart(
				title!=null?title:wTitle,         // chart title
				labelx!=null?labelx:"",               // domain axis label
				labely!=null?labely:"",                  // range axis label
				dataset,                  // data
				(legend==null)                     // include legend
		        );
		        chart.setBackgroundPaint(java.awt.Color.white);
				results.add(chart);
				dataset = new DefaultBoxAndWhiskerCategoryDataset();
			}
		}
		if (cross)
		{
			chart = ChartFactory.createBoxAndWhiskerChart(
			title!=null?title:wTitle,         // chart title
			labelx!=null?labelx:"",               // domain axis label
			labely!=null?labely:"",                  // range axis label
			dataset,                  // data
			(legend==null)                     // include legend
	        );
	        results.add(chart);
		}

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
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", true, 1458, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.weight, "vars=all", false, 656, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.imgwidth,"text",false,798,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.imgheight,"text",false,799,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.title,"text",false,800,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.labelx,"text",false,807,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.labely,"text",false,1459,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));

		parameters.add(new GetRequiredParameters(Keywords.outjpg, "filesave=.jpeg", false, 802, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.cross, "checkbox", false, 810, dep, "", 2));
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
		retprocinfo[1]="1460";
		return retprocinfo;
	}
}
