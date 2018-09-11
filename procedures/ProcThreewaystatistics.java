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

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
import java.util.TreeSet;
import java.util.TreeMap;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.StringComparator;

/**
* This is the procedure that evaluates several statistics for each way of the array
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcThreewaystatistics extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Threewaystatistics
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean samplevariance=false;
		String [] requiredparameters=new String[] {Keywords.dict, Keywords.var, Keywords.varobs, Keywords.vartime, Keywords.OUT.toLowerCase()};
		String [] optionalparameters=new String[] {Keywords.weight, Keywords.where, Keywords.samplevariance, Keywords.replace};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		samplevariance =(parameters.get(Keywords.samplevariance)!=null);

		String replace =(String)parameters.get(Keywords.replace);
		DataWriter dwm=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dwm.getmessage().equals(""))
			return new Result(dwm.getmessage(), false, null);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String tempvar=(String)parameters.get(Keywords.var.toLowerCase());
		String tempvarobs=(String)parameters.get(Keywords.varobs.toLowerCase());
		String tempvartime=(String)parameters.get(Keywords.vartime.toLowerCase());
		String weight=(String)parameters.get(Keywords.weight.toLowerCase());

		String[] tempvarname=tempvar.split(" ");

		String[] testvar=tempvarobs.split(" ");
		if (testvar.length>1)
			return new Result("%1336%<br>\n", false, null);

		testvar=tempvartime.split(" ");
		if (testvar.length>1)
			return new Result("%1337%<br>\n", false, null);

		String allvar=tempvartime+" "+tempvarobs+" "+tempvar;

		boolean invarisobs=false;
		boolean invaristime=false;
		boolean invarisweight=false;
		for (int i=0; i<tempvarname.length; i++)
		{
			if ((tempvarname[i].trim()).equalsIgnoreCase(tempvarobs.trim()))
				invarisobs=true;
			if ((tempvarname[i].trim()).equalsIgnoreCase(tempvartime.trim()))
				invarisobs=true;
			if (weight!=null)
			{
				if ((tempvarname[i].trim()).equalsIgnoreCase(weight.trim()))
					invarisweight=true;
			}
		}
		if (invarisobs)
			return new Result("%2024%<br>\n", false, null);
		if (invaristime)
			return new Result("%2025%<br>\n", false, null);
		if (invarisweight)
			return new Result("%2026%<br>\n", false, null);

		if (weight!=null)
			allvar=allvar+" "+weight;

		VariableUtilities varu=new VariableUtilities(dict, null, null, null, allvar, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String keyword="Threeway statistics "+dict.getkeyword();
		String description="Threeway statistics "+dict.getdescription();
		String author=(String)parameters.get(Keywords.client_host.toLowerCase());

		testvar=allvar.split(" ");
		int lastcol=testvar.length;
		int[] replacerule=new int[lastcol];
		int rifrep=0;
		if (replace==null)
			rifrep=0;
		else if (replace.equalsIgnoreCase(Keywords.replaceall))
			rifrep=1;
		else if (replace.equalsIgnoreCase(Keywords.replaceformat))
			rifrep=2;
		else if (replace.equalsIgnoreCase(Keywords.replacemissing))
			rifrep=3;
		for (int i=0; i<lastcol; i++)
		{
			replacerule[i]=rifrep;
		}

		DataReader data = new DataReader(dict);

		if (!data.open(testvar, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		if (weight!=null)
			lastcol=lastcol-1;
		double[] means=new double[lastcol-2];
		double[] sum=new double[lastcol-2];
		double[] max=new double[lastcol-2];
		double[] min=new double[lastcol-2];
		double[] sumsq=new double[lastcol-2];
		for (int i=0; i<lastcol-2; i++)
		{
			means[i]=0;
			sum[i]=0;
			max[i]=-1.7976931348623157E308;
			min[i]=Double.MAX_VALUE;
			sumsq[i]=0;
		}
		double nvalid=0;
		TreeMap<Double, double[]> timemeans=new TreeMap<Double, double[]>();
		TreeMap<Double, double[]> timesum=new TreeMap<Double, double[]>();
		TreeMap<Double, double[]> timemax=new TreeMap<Double, double[]>();
		TreeMap<Double, double[]> timemin=new TreeMap<Double, double[]>();
		TreeMap<Double, double[]> timesumsq=new TreeMap<Double, double[]>();

		TreeMap<String, double[]> obsmeans=new TreeMap<String, double[]>(new StringComparator());
		TreeMap<String, double[]> obssum=new TreeMap<String, double[]>(new StringComparator());
		TreeMap<String, double[]> obsmax=new TreeMap<String, double[]>(new StringComparator());
		TreeMap<String, double[]> obsmin=new TreeMap<String, double[]>(new StringComparator());
		TreeMap<String, double[]> obssumsq=new TreeMap<String, double[]>(new StringComparator());

		TreeSet<String> timen=new TreeSet<String>(new StringComparator());
		TreeSet<String> obsn=new TreeSet<String>(new StringComparator());
		HashSet<Vector<String>> testobsfortime=new HashSet<Vector<String>>();

		String[] values=new String[0];
		double obsw=1;
		boolean ismissing=false;
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				obsw=1;
				ismissing=false;
				if (weight!=null)
				{
					try
					{
						obsw=Double.parseDouble(values[values.length-1]);
					}
					catch (Exception ee)
					{
						ismissing=true;
					}
					if (Double.isNaN(obsw))
						ismissing=true;
				}
				double time=Double.NaN;
				try
				{
					time=Double.parseDouble(values[0]);
				}
				catch (Exception eee) {}
				if (Double.isNaN(time))
					ismissing=true;
				for (int i=2; i<lastcol; i++)
				{
					try
					{
						double currentvalue=Double.parseDouble(values[i]);
						if (Double.isNaN(currentvalue))
							ismissing=true;
					}
					catch (Exception ee)
					{
						ismissing=true;
					}
				}
				String obs=values[1];
				if (obs.equals(""))
					ismissing=true;
				if (!ismissing)
				{
					Vector<String> rifobs=new Vector<String>();
					rifobs.add(String.valueOf(time));
					rifobs.add(obs);
					testobsfortime.add(rifobs);
					timen.add(String.valueOf(time));
					obsn.add(obs);
					double[] ttimemeans=timemeans.get(new Double(time));
					double[] ttimesum=timesum.get(new Double(time));
					double[] ttimemax=timemax.get(new Double(time));
					double[] ttimemin=timemin.get(new Double(time));
					double[] ttimesumsq=timesumsq.get(new Double(time));
					if (ttimemeans==null)
					{
						ttimemeans=new double[lastcol-2];
						ttimesum=new double[lastcol-2];
						ttimemax=new double[lastcol-2];
						ttimemin=new double[lastcol-2];
						ttimesumsq=new double[lastcol-2];
						for (int i=0; i<lastcol-2; i++)
						{
							ttimemeans[i]=0;
							ttimesum[i]=0;
							ttimemax[i]=-1.7976931348623157E308;
							ttimemin[i]=Double.MAX_VALUE;
							ttimesumsq[i]=0;
						}
					}

					double[] tobsmeans=obsmeans.get(obs);
					double[] tobssum=obssum.get(obs);
					double[] tobsmax=obsmax.get(obs);
					double[] tobsmin=obsmin.get(obs);
					double[] tobssumsq=obssumsq.get(obs);
					if (tobsmeans==null)
					{
						tobsmeans=new double[lastcol-2];
						tobssum=new double[lastcol-2];
						tobsmax=new double[lastcol-2];
						tobsmin=new double[lastcol-2];
						tobssumsq=new double[lastcol-2];
						for (int i=0; i<lastcol-2; i++)
						{
							tobsmeans[i]=0;
							tobssum[i]=0;
							tobsmax[i]=-1.7976931348623157E308;
							tobsmin[i]=Double.MAX_VALUE;
							tobssumsq[i]=0;
						}
					}
					if (!ismissing)
					{
						nvalid++;
						for (int i=2; i<lastcol; i++)
						{
							double currentvalue=Double.parseDouble(values[i]);
							means[i-2]+=obsw;
							sum[i-2]+=currentvalue*obsw;
							sumsq[i-2]+=currentvalue*currentvalue*obsw;
							if (currentvalue<min[i-2])
								min[i-2]=currentvalue;
							if (currentvalue>max[i-2])
								max[i-2]=currentvalue;

							ttimemeans[i-2]+=obsw;
							ttimesum[i-2]+=currentvalue*obsw;
							ttimesumsq[i-2]+=currentvalue*currentvalue*obsw;
							if (currentvalue<ttimemin[i-2])
								ttimemin[i-2]=currentvalue;
							if (currentvalue>ttimemax[i-2])
								ttimemax[i-2]=currentvalue;

							tobsmeans[i-2]+=obsw;
							tobssum[i-2]+=currentvalue*obsw;
							tobssumsq[i-2]+=currentvalue*currentvalue*obsw;
							if (currentvalue<tobsmin[i-2])
								tobsmin[i-2]=currentvalue;
							if (currentvalue>tobsmax[i-2])
								tobsmax[i-2]=currentvalue;
						}
						timemeans.put(new Double(time), ttimemeans);
						timesum.put(new Double(time), ttimesum);
						timemax.put(new Double(time), ttimemax);
						timemin.put(new Double(time), ttimemin);
						timesumsq.put(new Double(time), ttimesumsq);

						obsmeans.put(obs, tobsmeans);
						obssum.put(obs, tobssum);
						obsmax.put(obs, tobsmax);
						obsmin.put(obs, tobsmin);
						obssumsq.put(obs, tobssumsq);
					}

				}
			}
		}
		data.close();
		if ((nvalid==0) && (where!=null))
			return new Result("%2804%<br>\n", false, null);
		if ((nvalid==0) && (where==null))
			return new Result("%666%<br>\n", false, null);
		for (int i=0; i<lastcol-2; i++)
		{
			sumsq[i]=sumsq[i]/means[i]-Math.pow(sum[i]/means[i],2);
			if (!samplevariance)
				sumsq[i]=sumsq[i]*means[i]/(means[i]-1);
			means[i]=sum[i]/means[i];
			sumsq[i]=Math.sqrt(sumsq[i]);
		}

		boolean testifallobs=true;
		Iterator<String> itt = timen.iterator();
		while(itt.hasNext())
		{
			Iterator<String> ito = obsn.iterator();
			Vector<String> testot=new Vector<String>();
			testot.add(itt.next());
			while(ito.hasNext())
			{
				if (testot.size()>1)
					testot.set(1, ito.next());
				else
					testot.add(ito.next());
				if (!testobsfortime.contains(testot))
					testifallobs=false;
			}
		}
		if (!testifallobs)
			return new Result("%1339%<br>\n", false, null);

		testobsfortime.clear();
		testobsfortime=null;

		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);

		Hashtable<String, String> tempmd=new Hashtable<String, String>();
		Hashtable<String, String> clvar=new Hashtable<String, String>();
		clvar.put("0", "%1341%");
		int rifcl=1;
		itt = timen.iterator();
		Hashtable<String, String> cltime=new Hashtable<String, String>();
		while(itt.hasNext())
		{
			String currenttime= itt.next();
			clvar.put(String.valueOf(rifcl), "%1342% "+currenttime);
			cltime.put(currenttime, String.valueOf(rifcl));
			rifcl++;
		}
		Hashtable<String, String> clobs=new Hashtable<String, String>();
		Iterator<String> ito = obsn.iterator();
		while(ito.hasNext())
		{
			String currentobs= ito.next();
			clvar.put(String.valueOf(rifcl), "%1343% "+currentobs);
			clobs.put(currentobs, String.valueOf(rifcl));
			rifcl++;
		}

		Hashtable<String, String> clstat=new Hashtable<String, String>();
		clstat.put("0", "%681%");
		clstat.put("1", "%687%");
		clstat.put("2", "%686%");
		clstat.put("3", "%682%");
		clstat.put("4", "%680%");
		dsu.addnewvar("rifv", "%1340%", Keywords.TEXTSuffix, clvar, tempmd);
		dsu.addnewvar("rifs", "%1366%", Keywords.TEXTSuffix, clstat, tempmd);

		for (int j=0; j<tempvarname.length; j++)
		{
			dsu.addnewvar("v"+String.valueOf(j), dict.getvarlabelfromname(tempvarname[j]), Keywords.NUMSuffix, tempmd, tempmd);
		}
		if (!dwm.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dwm.getmessage(), false, null);

		String[] valuestowrite=new String[tempvarname.length+2];
		valuestowrite[0]="0";
		valuestowrite[1]="0";
		for (int i=0; i<tempvarname.length; i++)
		{
			valuestowrite[i+2]=double2String(means[i]);
		}
		dwm.write(valuestowrite);
		valuestowrite[1]="1";
		for (int i=0; i<tempvarname.length; i++)
		{
			valuestowrite[i+2]=double2String(sum[i]);
		}
		dwm.write(valuestowrite);
		valuestowrite[1]="2";
		for (int i=0; i<tempvarname.length; i++)
		{
			valuestowrite[i+2]=double2String(sumsq[i]);
		}
		dwm.write(valuestowrite);
		valuestowrite[1]="3";
		for (int i=0; i<tempvarname.length; i++)
		{
			valuestowrite[i+2]=double2String(min[i]);
		}
		dwm.write(valuestowrite);
		valuestowrite[1]="4";
		for (int i=0; i<tempvarname.length; i++)
		{
			valuestowrite[i+2]=double2String(max[i]);
		}
		dwm.write(valuestowrite);

		ito = obsn.iterator();
		while(ito.hasNext())
		{
			String currentobs= ito.next();

			double[] tobsmeans=obsmeans.get(currentobs);
			double[] tobssum=obssum.get(currentobs);
			double[] tobsmax=obsmax.get(currentobs);
			double[] tobsmin=obsmin.get(currentobs);
			double[] tobssumsq=obssumsq.get(currentobs);

			for (int i=0; i<tempvarname.length; i++)
			{
				tobssumsq[i]=tobssumsq[i]/tobsmeans[i]-Math.pow(tobssum[i]/tobsmeans[i],2);
				if (!samplevariance)
					tobssumsq[i]=tobssumsq[i]*tobsmeans[i]/(tobsmeans[i]-1);
				tobsmeans[i]=tobssum[i]/tobsmeans[i];
				tobssumsq[i]=Math.sqrt(tobssumsq[i]);
			}

			valuestowrite[0]=clobs.get(currentobs);
			valuestowrite[1]="0";
			for (int i=0; i<tempvarname.length; i++)
			{
				valuestowrite[i+2]=double2String(tobsmeans[i]);
			}
			dwm.write(valuestowrite);
			valuestowrite[1]="1";
			for (int i=0; i<tempvarname.length; i++)
			{
				valuestowrite[i+2]=double2String(tobssum[i]);
			}
			dwm.write(valuestowrite);
			valuestowrite[1]="2";
			for (int i=0; i<tempvarname.length; i++)
			{
				valuestowrite[i+2]=double2String(tobssumsq[i]);
			}
			dwm.write(valuestowrite);
			valuestowrite[1]="3";
			for (int i=0; i<tempvarname.length; i++)
			{
				valuestowrite[i+2]=double2String(tobsmin[i]);
			}
			dwm.write(valuestowrite);
			valuestowrite[1]="4";
			for (int i=0; i<tempvarname.length; i++)
			{
				valuestowrite[i+2]=double2String(tobsmax[i]);
			}
			dwm.write(valuestowrite);
		}

		/*Starting to write the statistics for the times*/
		itt = timen.iterator();
		while(itt.hasNext())
		{
			String currenttime= itt.next();
			double[] ttimemeans=timemeans.get(new Double(Double.parseDouble(currenttime)));
			double[] ttimesum=timesum.get(new Double(Double.parseDouble(currenttime)));
			double[] ttimemax=timemax.get(new Double(Double.parseDouble(currenttime)));
			double[] ttimemin=timemin.get(new Double(Double.parseDouble(currenttime)));
			double[] ttimesumsq=timesumsq.get(new Double(Double.parseDouble(currenttime)));
			valuestowrite[0]=cltime.get(currenttime);
			valuestowrite[1]="0";

			for (int i=0; i<tempvarname.length; i++)
			{
				ttimesumsq[i]=ttimesumsq[i]/ttimemeans[i]-Math.pow(ttimesum[i]/ttimemeans[i],2);
				if (!samplevariance)
					ttimesumsq[i]=ttimesumsq[i]*ttimemeans[i]/(ttimemeans[i]-1);
				ttimemeans[i]=ttimesum[i]/ttimemeans[i];
				ttimesumsq[i]=Math.sqrt(ttimesumsq[i]);
			}

			for (int i=0; i<tempvarname.length; i++)
			{
				valuestowrite[i+2]=double2String(ttimemeans[i]);
			}
			dwm.write(valuestowrite);
			valuestowrite[1]="1";
			for (int i=0; i<tempvarname.length; i++)
			{
				valuestowrite[i+2]=double2String(ttimesum[i]);
			}
			dwm.write(valuestowrite);
			valuestowrite[1]="2";
			for (int i=0; i<tempvarname.length; i++)
			{
				valuestowrite[i+2]=double2String(ttimesumsq[i]);
			}
			dwm.write(valuestowrite);
			valuestowrite[1]="3";
			for (int i=0; i<tempvarname.length; i++)
			{
				valuestowrite[i+2]=double2String(ttimemin[i]);
			}
			dwm.write(valuestowrite);
			valuestowrite[1]="4";
			for (int i=0; i<tempvarname.length; i++)
			{
				valuestowrite[i+2]=double2String(ttimemax[i]);
			}
			dwm.write(valuestowrite);
		}

		Vector<StepResult> result = new Vector<StepResult>();
		boolean resclosem=dwm.close();
		if (!resclosem)
			return new Result(dwm.getmessage(), false, null);
		Vector<Hashtable<String, String>> mtablevariableinfo=dwm.getVarInfo();
		Hashtable<String, String> mdatatableinfo=dwm.getTableInfo();
		result.add(new LocalDictionaryWriter(dwm.getdictpath(), keyword, description, author, dwm.gettabletype(),
		mdatatableinfo, dsu.getfinalvarinfo(), mtablevariableinfo, dsu.getfinalcl(), dsu.getfinalmd(), null));
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
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", false, 1346, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", true, 1347, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varobs, "var=all", true, 1348, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vartime, "vars=all", true, 1349, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.weight, "vars=all", false, 656, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.samplevariance, "checkbox", false, 861, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="1344";
		retprocinfo[1]="1359";
		return retprocinfo;
	}
}
