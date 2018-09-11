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
import java.util.TreeMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.chart.ChartUtilities;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.gui.GraphViewer;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.VectorStringComparator;
import ADaMSoft.utilities.StringComparator;

/**
* This is the procedure that create a pie chart for a variable, optionally using a weight and a grouping variable
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcPie implements RunStep
{
	private String wTitle;
	/**
	* Starts the execution of Proc Freq
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.dict,Keywords.var};
		String [] optionalparameters=new String[] {Keywords.percent,Keywords.where,Keywords.replace,Keywords.weight,Keywords.vargroup,Keywords.imgwidth,
													Keywords.imgheight,Keywords.title,Keywords.outjpg,Keywords.nolegend};
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
		String tempweight=(String)parameters.get(Keywords.weight.toLowerCase());

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
		String legend = (String)parameters.get(Keywords.nolegend);

		String replace =(String)parameters.get(Keywords.replace);

		VariableUtilities varu=new VariableUtilities(dict, tempvarg, null, tempweight, tempvar, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String[] var=varu.getrowvar();
		String[] varg=varu.getgroupvar();

		String[] reqvar=varu.getreqvar();

		int[] replacerule=varu.getreplaceruleforsel(replace);

		int[] grouprule=varu.getgroupruleforsel();
		int[] rowrule=varu.getrowruleforsel();
		int[] weightrule=varu.getweightruleforsel();

		if(var.length>1){
			return new Result("%793%<br>\n", false, null);
		}
		if(varg.length>1){
			return new Result("%793%<br>\n", false, null);
		}

		DataReader data = new DataReader(dict);
		if (!data.open(reqvar, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		ValuesParser vp=new ValuesParser(null, grouprule, null, rowrule, null, weightrule);

		String[] varRowLabels = new String[var.length];

		for(int i= 0;i<var.length;i++)
		{
			varRowLabels[i]= dict.getvarlabelfromname(var[i]);
		}
		TreeMap<Vector<String>, Vector<TreeMap<String, Double>>> freqval=new TreeMap<Vector<String>, Vector<TreeMap<String, Double>>>(new VectorStringComparator());

		boolean perc =(parameters.get(Keywords.percent)!=null);

		int validgroup=0;
		while (!data.isLast())
		{
			String[] values = data.getRecord();
			if (values!=null)
			{
				Vector<String> vargroupvalues=vp.getvargroup(values);
				String[] varrowvalues = vp.getrowvar(values);
				double weightvalue=vp.getweight(values);
				if (!Double.isNaN(weightvalue))
				{
					if (vp.vargroupisnotmissing(vargroupvalues))
					{
						validgroup++;
						Vector<TreeMap<String, Double>> temp=null;
						if (!freqval.containsKey(vargroupvalues))
						{
							temp=new Vector<TreeMap<String, Double>>();
							for (int i=0; i<var.length; i++)
							{
								if (!varrowvalues[i].equals(""))
								{
									TreeMap<String, Double> tempf=new TreeMap<String, Double>(new StringComparator());
									tempf.put(varrowvalues[i], new Double(weightvalue));
									temp.add(tempf);
								}
							}
							freqval.put(vargroupvalues, temp);
						}
						else
						{
							temp=freqval.get(vargroupvalues);
							for (int i=0; i<var.length; i++)
							{
								if (!varrowvalues[i].equals(""))
								{
									TreeMap<String, Double> tempf=temp.get(i);
									if (tempf.get(varrowvalues[i])==null)
										tempf.put(varrowvalues[i], new Double(weightvalue));
									else
									{
										double tempval=(tempf.get(varrowvalues[i])).doubleValue()+weightvalue;
										tempf.put(varrowvalues[i], new Double(tempval));
									}
									temp.set(i, tempf);
								}
							}
							freqval.put(vargroupvalues, temp);
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
		DefaultPieDataset dataset = new DefaultPieDataset();
		JFreeChart chart;

		if (perc)
		{
			Iterator<Vector<String>> e = freqval.keySet().iterator();
			while(e.hasNext())
			{
				Vector<String> tempgroup=e.next();
				Vector<TreeMap<String, Double>> temp=freqval.get(tempgroup);
				for (int i=0; i<temp.size(); i++)
				{
					TreeMap<String, Double> tempf=temp.get(i);
					double sum=0;
					Iterator<String> ef = tempf.keySet().iterator();
					while(ef.hasNext())
					{
						String tempval=ef.next();
						double tempfre=(tempf.get(tempval)).doubleValue();
						sum+=tempfre;
					}
					ef = tempf.keySet().iterator();
					while(ef.hasNext())
					{
						String tempval=ef.next();
						double tempfre=100*(tempf.get(tempval)).doubleValue()/sum;
						tempf.put(tempval, new Double(tempfre));
					}
				}
			}
		}

		NumberFormat formatter = new DecimalFormat("#.#");

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
		Iterator<Vector<String>> e = freqval.keySet().iterator();
		while(e.hasNext())
		{
			wTitle="";
			Vector<String> tempgroup=e.next();
			Vector<TreeMap<String, Double>> temp=freqval.get(tempgroup);
			int p=0;
			for (int i=0; i<temp.size(); i++)
			{
				wTitle+=varRowLabels[i];
				if (p<(temp.size()-1))
					wTitle+=" - ";
				p++;
				TreeMap<String, Double> tempf=temp.get(i);
				Iterator<String> ef = tempf.keySet().iterator();
				while(ef.hasNext())
				{
					String tempval=ef.next();
					double tempfre=(tempf.get(tempval)).doubleValue();
					String sfreq = formatter.format(tempfre);
					if (perc)
						sfreq=sfreq+"%";
					if (varg.length==0)
						dataset.setValue(tempval+" ("+sfreq+")", tempfre);
					else
					{
						String grouprif="";
						for (int j=0; j<tempgroup.size(); j++)
						{
							grouprif+=tempgroup.get(j);
							if (j<(tempgroup.size()-1))
								grouprif+="; ";
						}
						dataset.setValue(tempval+" ("+grouprif+"), "+sfreq, tempfre);
					}
				}
			}
			wTitle=wTitle.trim()+gTitle;
	        chart = ChartFactory.createPieChart(
	            title!=null?title:wTitle,         // chart title
	            dataset,                  // data
	            (legend==null),           // include legend
	            true,                     // tooltips?
	            false                     // URLs?
	        );
	        results.add(chart);
			dataset = new DefaultPieDataset();
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
				} catch (IOException ee) {
					return new Result("%794%\n", false, null);
				}
				String path=outjpg;
				if(results.size()>1){
					if(outjpg.indexOf('.')!=0){
						path= outjpg.replace(".",i++ +".");
					}
					else{
						return new Result("%795%\n",false,null);
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
		parameters.add(new GetRequiredParameters(Keywords.var, "var=all", true, 796, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "var=all", false, 655, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.weight, "var=all", false, 797, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.imgwidth,"text",false, 798,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.imgheight,"text",false, 799,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.title,"text",false, 800,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.percent, "checkbox", false, 801, dep, "", 2));
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
		retprocinfo[1]="805";
		return retprocinfo;
	}
}
