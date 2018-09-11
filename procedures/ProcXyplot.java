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


import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.general.SeriesException;
import org.jfree.data.xy.XYSeries;
import org.jfree.chart.ChartUtilities;
import org.jfree.data.xy.XYSeriesCollection;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.gui.GraphViewer;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.VariableUtilities;

/**
* This is the procedure that creates a xy chart for a variable, optionally using a weight and a grouping variable
v*/
public class ProcXyplot implements RunStep
{
	private String wTitle="";

	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean isxspec=true;
		String [] requiredparameters=new String[] {Keywords.dict,Keywords.vary};
		String [] optionalparameters=new String[] { Keywords.varx, Keywords.imgwidth,Keywords.imgheight,
													Keywords.labely,Keywords.labelx,Keywords.title,
													Keywords.outjpg,Keywords.nolegend, Keywords.where};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		String varx=(String)parameters.get(Keywords.varx);
		if (varx==null)
			isxspec=false;
		String vary=(String)parameters.get(Keywords.vary);
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

		if(sEight!=null) eight = Integer.parseInt(sEight);
		if(sEight!=null) width = Integer.parseInt(sWidth);


		String title = (String)parameters.get(Keywords.title);
		String labelx = (String)parameters.get(Keywords.labelx);
		String labely = (String)parameters.get(Keywords.labely);
		String legend = (String)parameters.get(Keywords.nolegend);

		VariableUtilities varu=new VariableUtilities(dict, null, null, null, varx, vary);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String[] varcol=varu.getcolvar();

		String[] reqvar=varu.getreqvar();

		DataReader data = new DataReader(dict);
		if (!data.open(reqvar, null, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		int[] x=new int[0];
		int[] y=varu.getcolruleforsel();

		if (isxspec)
		{
			x=varu.getrowruleforsel();
			wTitle+= dict.getvarlabelfromname(varx)+" - ";
		}
		else
			wTitle="Progressive number- ";

		XYSeriesCollection dataset=new XYSeriesCollection();
		for(int i=0;i<varcol.length;i++)
		{
			String label = dict.getvarlabelfromname(varcol[i]);
			XYSeries tempxys=(new XYSeries(label,true,false));
			dataset.addSeries(tempxys);
			wTitle+= label;
			if (i<(varcol.length-1))
				wTitle+=", ";
		}

		ValuesParser vp=new ValuesParser(null, null, null, x, y, null);
		double prog=1;
		String[] xValues=new String[0];
		double yv=Double.NaN;
		while (!data.isLast())
		{
			String[] values = data.getRecord();
			if (values!=null)
			{
				String[] yValues = vp.getcolvar(values);
				double xv=prog;
				if (isxspec)
				{
					xValues = vp.getrowvar(values);
					xv = Double.parseDouble(xValues[0]);
				}
				prog++;
				for(int i=0;i<yValues.length;i++)
				{
					yv=Double.NaN;
					try
					{
						yv = Double.parseDouble(yValues[i]);
					}
					catch (Exception eeee) {}
					if (!Double.isNaN(yv))
					{
						XYSeries tempxys=dataset.getSeries(i);
						try
						{
							tempxys.add(xv, yv);
						}
						catch(SeriesException e)
						{
							return new Result("%839% ("+ xv +")<br>\n", false, null);
						}
					}
				}
			}
		}
		data.close();

		JFreeChart chart = ChartFactory.createXYLineChart(
				title!=null?title:wTitle, // chart title
				labelx!=null?labelx:"",   // range axis label
			    labely!=null?labely:"",   // domain axis label
			    dataset,                  // data
				vertical?PlotOrientation.HORIZONTAL:PlotOrientation.VERTICAL, // orientation
			    (legend==null),           // include legend
			    true,                     // tooltips?
			    false                     // URLs?
		);


		Vector<JFreeChart> results = new Vector<JFreeChart>();
		results.add(chart);

		Vector<StepResult> result = new Vector<StepResult>();
		String workdirg=(String)parameters.get(Keywords.WorkDir);
		Iterator<JFreeChart> it = results.iterator();
		int i=0;
		while(it.hasNext()){
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
		parameters.add(new GetRequiredParameters(Keywords.varx, "var=all", false, 813, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 907, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vary, "vars=all", true, 814, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.imgwidth,"text",false, 798,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.imgheight,"text",false, 799,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.title,"text",false, 800,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.labelx,"text",false,815,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.labely,"text",false,816,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));

		parameters.add(new GetRequiredParameters(Keywords.outjpg, "filesave=.jpeg", false, 802, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.nolegend, "checkbox", false, 803, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="804";
		retprocinfo[1]="824";
		return retprocinfo;
	}
}
