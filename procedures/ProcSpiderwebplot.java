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
import java.util.TreeMap;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.title.TextTitle;

import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.gui.GraphViewer;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that evaluate the Spider web plot for a variable that contains the name of the axis,
* one or more variables for the series and a weight value
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcSpiderwebplot implements RunStep
{
	String wTitle;
	/**
	* Starts the execution of Proc Spiderwebplot
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		wTitle="";
		String [] requiredparameters=new String[] {Keywords.dict, Keywords.varitems};
		String [] optionalparameters=new String[] {Keywords.varseries, Keywords.where, Keywords.percent,Keywords.weight, Keywords.replace,
													Keywords.imgwidth,Keywords.imgheight,Keywords.title,
													Keywords.fillseries,Keywords.outjpg,Keywords.nolegend};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		boolean fillseries =(parameters.get(Keywords.fillseries)!=null);
		boolean percent =(parameters.get(Keywords.percent)!=null);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String tempvaritems=(String)parameters.get(Keywords.varitems.toLowerCase());
		String tempvarseries=(String)parameters.get(Keywords.varseries.toLowerCase());
		String tempweight=(String)parameters.get(Keywords.weight.toLowerCase());

		String[] testuvar=tempvaritems.trim().split(" ");
		Vector<String> reqvar=new Vector<String>();
		if (testuvar.length>1)
			return new Result("%2696%<br>\n", false, null);
		reqvar.add(tempvaritems.trim());

		boolean isseries=false;
		boolean isweight=false;
		int numseries=0;

		String labelitem=dict.getvarlabelfromname(tempvaritems.trim());
		String[] labelseries=new String[0];

		if (tempvarseries!=null)
		{
			isseries=true;
			testuvar=tempvarseries.trim().split(" ");
			numseries=testuvar.length;
			labelseries=new String[testuvar.length];
			for (int i=0; i<testuvar.length; i++)
			{
				labelseries[i]=dict.getvarlabelfromname(testuvar[i].trim());
				reqvar.add(testuvar[i].trim());
				if (testuvar[i].trim().equalsIgnoreCase(tempvaritems.trim()))
					return new Result("%2697% ("+testuvar[i].trim()+")<br>\n", false, null);
			}
		}

		if (tempweight!=null)
		{
			isweight=true;
			testuvar=tempweight.trim().split(" ");
			for (int i=0; i<reqvar.size(); i++)
			{
				if (reqvar.get(i).equalsIgnoreCase(tempweight.trim()))
					return new Result("%636%<br>\n", false, null);
			}
			reqvar.add(tempweight.trim());
			if (testuvar.length>1)
				return new Result("%1895%<br>\n", false, null);
		}

		String vnexist="";
		boolean vn=false;
		for (int j=0; j<reqvar.size(); j++)
		{
			vn=false;
			for (int i=0; i<dict.gettotalvar(); i++)
			{
				if (reqvar.get(j).equalsIgnoreCase(dict.getvarname(i)))
					vn=true;
			}
			if (!vn)
				vnexist=vnexist+" "+reqvar.get(j);
		}
		if (!vnexist.equals(""))
			return new Result("%1632% ("+vnexist.trim()+")<br>\n", false, null);

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

		int[] replacerule=new int[reqvar.size()];
		String[] requested=new String[reqvar.size()];

		for (int i=0; i<requested.length; i++)
		{
			requested[i]=reqvar.get(i).trim();
		}

		DataReader data = new DataReader(dict);
		if (!data.open(requested, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		TreeMap<String, double[]> freqval=new TreeMap<String, double[]>();

		int validgroup=0;
		boolean valid=true;
		while (!data.isLast())
		{
			valid=true;
			String[] values = data.getRecord();
			if (values!=null)
			{
				String groupname=values[0];
				double w=1;
				if (isweight)
				{
					try
					{
						w=Double.parseDouble(values[values.length-1]);
					}
					catch (Exception e)
					{
						valid=false;
					}
				}
				if ((!isseries) && (valid))
				{
					if (freqval.containsKey(groupname))
					{
						double[] tv=freqval.get(groupname);
						tv[0]=tv[0]+w;
						freqval.put(groupname, tv);
					}
					else
					{
						double[] varv=new double[1];
						varv[0]=w;
						freqval.put(groupname, varv);
					}
					validgroup++;
				}
				if ((isseries) && (valid))
				{
					double[] varv=new double[numseries];
					int checkvalds=0;
					for (int i=0; i<numseries; i++)
					{
						try
						{
							varv[i]=Double.parseDouble(values[1+i]);
							varv[i]=varv[i]*w;
							checkvalds++;
						}
						catch (Exception e){}
					}
					if (checkvalds>0)
					{
						validgroup++;
						if (freqval.containsKey(groupname))
						{
							double[] tv=freqval.get(groupname);
							for (int i=0; i<numseries; i++)
							{
								if ((!Double.isNaN(varv[i])) && (!Double.isNaN(tv[i])))
									tv[i]=tv[i]+varv[i];
								else if ((!Double.isNaN(varv[i])) && (Double.isNaN(tv[i])))
									tv[i]=varv[i];
							}
							freqval.put(groupname, tv);
						}
						else
							freqval.put(groupname, varv);
					}
				}
			}
		}
		data.close();
		if ((validgroup==0) && (where!=null))
			return new Result("%2804%<br>\n", false, null);
		if ((validgroup==0) && (where==null))
			return new Result("%666%<br>\n", false, null);

		Hashtable<String, Double> maxvalues=new Hashtable<String, Double>();
		double tm=-1.7976931348623157E308;
		if (percent)
		{
			Iterator<String> igm = freqval.keySet().iterator();
			while(igm.hasNext())
			{
				String tempgroup=igm.next();
				double[] tempval=freqval.get(tempgroup);
				tm=-1.7976931348623157E308;
				for (int i=0; i<tempval.length; i++)
				{
					if (!Double.isNaN(tempval[i]))
					{
						if (tempval[i]>tm)
							tm=tempval[i];
					}
				}
				maxvalues.put(tempgroup, tm);
			}
		}

		Vector<JFreeChart> results = new Vector<JFreeChart>();
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		Iterator<String> igv = freqval.keySet().iterator();
		while(igv.hasNext())
		{
			String tempgroup=igv.next();
			double[] tempval=freqval.get(tempgroup);
			if (!isseries)
			{
				if (!percent)
					dataset.addValue(tempval[0], labelitem, tempgroup);
				else
				{
					tm=(maxvalues.get(tempgroup)).doubleValue();
					dataset.addValue(100.0*tempval[0]/tm, labelitem, tempgroup);
				}
			}
			else
			{
				for (int i=0; i<tempval.length; i++)
				{
					if (!percent)
					{
						if (!Double.isNaN(tempval[i]))
							dataset.addValue(tempval[i], labelseries[i], tempgroup);
					}
					else
					{
						if (!Double.isNaN(tempval[i]))
						{
							tm=(maxvalues.get(tempgroup)).doubleValue();
							dataset.addValue(100.0*tempval[i]/tm, labelseries[i], tempgroup);
						}
					}
				}
			}
		}
		String gtitle=labelitem;
		SpiderWebPlot plot=new SpiderWebPlot(dataset);
		if (fillseries)
			plot.setWebFilled(true);
		else
			plot.setWebFilled(false);
		JFreeChart chart = new JFreeChart
		(
			title!=null?title:gtitle,
			TextTitle.DEFAULT_FONT,
			plot,
			true
		);

        results.add(chart);
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
		parameters.add(new GetRequiredParameters(Keywords.varitems, "vars=all", true, 2692, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varseries, "vars=all", false, 2693, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.weight, "var=all", false, 797, dep, "", 2));

		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.imgwidth,"text",false,798,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.imgheight,"text",false,799,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.title,"text",false,800,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.fillseries,"checkbox",false,2694,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.percent, "checkbox", false, 2698, dep, "", 2));
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
		retprocinfo[1]="2695";
		return retprocinfo;
	}
}
