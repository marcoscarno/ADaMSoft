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
import java.util.TreeSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import ADaMSoft.algorithms.CorrelationsEvaluator;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.StringComparator;
import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.VariableUtilities;

/**
* This is the procedure that evaluates the correlations for a three way array
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcThreewaycorr extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Threewaycorr
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean samplevariance=false;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.var, Keywords.varobs, Keywords.vartime};
		String [] optionalparameters=new String[] {Keywords.weight, Keywords.where, Keywords.samplevariance, Keywords.replace, Keywords.transform};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		samplevariance =(parameters.get(Keywords.samplevariance)!=null);

		String transform=(String)parameters.get(Keywords.transform.toLowerCase());
		if (transform==null)
			transform=Keywords.notransform;
		String[] transformation=new String[] {Keywords.notransform, Keywords.standardize, Keywords.divformax,
		Keywords.normalize01, Keywords.meannormalize, Keywords.sumnormalize,};
		int selectedoption=steputilities.CheckOption(transformation, transform);
		if (selectedoption==0)
			return new Result("%1775% "+Keywords.transform.toUpperCase()+"<br>\n"+steputilities.getMessage(), false, null);

		String replace =(String)parameters.get(Keywords.replace);
		DataWriter dwc=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dwc.getmessage().equals(""))
			return new Result(dwc.getmessage(), false, null);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String tempvar=(String)parameters.get(Keywords.var.toLowerCase());
		String tempvarobs=(String)parameters.get(Keywords.varobs.toLowerCase());
		String tempvartime=(String)parameters.get(Keywords.vartime.toLowerCase());
		String weight=(String)parameters.get(Keywords.weight.toLowerCase());

		String[] tempvarname=tempvar.split(" ");
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

		String[] testvar=tempvarobs.split(" ");
		if (testvar.length>1)
			return new Result("%1336%<br>\n", false, null);

		testvar=tempvartime.split(" ");
		if (testvar.length>1)
			return new Result("%1337%<br>\n", false, null);

		String allvar=tempvartime+" "+tempvarobs+" "+tempvar;

		if (weight!=null)
			allvar=allvar+" "+weight;

		VariableUtilities varu=new VariableUtilities(dict, null, null, null, allvar, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String keyword="Threeway corr "+dict.getkeyword();
		String description="Threeway corr "+dict.getdescription();
		String author=(String)parameters.get(Keywords.client_host.toLowerCase());

		testvar=allvar.split(" ");
		int lastcol=testvar.length;
		int[] replacerule=new int[testvar.length];
		int rifrep=0;
		if (replace==null)
			rifrep=0;
		else if (replace.equalsIgnoreCase(Keywords.replaceall))
			rifrep=1;
		else if (replace.equalsIgnoreCase(Keywords.replaceformat))
			rifrep=2;
		else if (replace.equalsIgnoreCase(Keywords.replacemissing))
			rifrep=3;
		for (int i=0; i<testvar.length; i++)
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

		TreeSet<String> timen=new TreeSet<String>(new StringComparator());
		TreeSet<String> obsn=new TreeSet<String>(new StringComparator());
		HashSet<Vector<String>> testobsfortime=new HashSet<Vector<String>>();
		String[] values=new String[0];
		double obsw=1;
		boolean ismissing=false;
		double time=Double.NaN;
		double currentvalue=Double.NaN;
		String obs="";
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				ismissing=false;
				obsw=1;
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
				time=Double.NaN;
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
						currentvalue=Double.parseDouble(values[i]);
						if (Double.isNaN(currentvalue))
							ismissing=true;
					}
					catch (Exception ee)
					{
						ismissing=true;
					}
				}
				obs=values[1];
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
					for (int i=2; i<lastcol; i++)
					{
						currentvalue=Double.parseDouble(values[i]);
						means[i-2]+=currentvalue*obsw;
						sum[i-2]+=currentvalue*obsw;
						sumsq[i-2]+=currentvalue*currentvalue*obsw;
						if (currentvalue<min[i-2])
							min[i-2]=currentvalue;
						if (currentvalue>max[i-2])
							max[i-2]=currentvalue;
					}
					nvalid+=obsw;
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
			means[i]=means[i]/nvalid;
			sumsq[i]=sumsq[i]/nvalid-Math.pow(means[i],2);
			if (!samplevariance)
				sumsq[i]=sumsq[i]*nvalid/(nvalid-1);
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

		CorrelationsEvaluator covtot=new CorrelationsEvaluator(true, true);
		CorrelationsEvaluator covtime=new CorrelationsEvaluator(true, true);
		CorrelationsEvaluator covobs=new CorrelationsEvaluator(true, true);

		double[] valuerow=new double[means.length];

		data = new DataReader(dict);

		if (!data.open(testvar, replacerule, false))
			return new Result(data.getmessage(), false, null);

		while (!data.isLast())
		{
			values = data.getRecord();
			ismissing=false;
			obsw=1;
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
			time=Double.NaN;
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
					currentvalue=Double.parseDouble(values[i]);
					if (Double.isNaN(currentvalue))
						ismissing=true;
				}
				catch (Exception ee)
				{
					ismissing=true;
				}
			}
			obs=values[1];
			if (obs.equals(""))
				ismissing=true;
			if (!ismissing)
			{
				for (int i=2; i<lastcol; i++)
				{
					valuerow[i-2]=Double.parseDouble(values[i]);
					if(selectedoption==2)
						valuerow[i-2]=(valuerow[i-2]-means[i-2])/Math.pow(sumsq[i-2],0.5);
					if(selectedoption==3)
						valuerow[i-2]=valuerow[i-2]/max[i-2];
					if(selectedoption==4)
						valuerow[i-2]=(valuerow[i-2]-min[i-2])/(max[i-2]-min[i-2]);
					if(selectedoption==5)
						valuerow[i-2]=valuerow[i-2]/means[i-2];
					if(selectedoption==6)
						valuerow[i-2]=valuerow[i-2]/sum[i-2];
				}
				Vector<String> vtime=new Vector<String>();
				Vector<String> vobs=new Vector<String>();
				Vector<String> all=new Vector<String>();
				vtime.add(String.valueOf(time));
				vobs.add(obs);
				all.add(null);
				covtot.setValue(all, valuerow, valuerow, obsw);
				covtime.setValue(vtime, valuerow, valuerow, obsw);
				covobs.setValue(vobs, valuerow, valuerow, obsw);
			}
		}
		data.close();
		covtot.calculate();
		covtime.calculate();
		covobs.calculate();

		Hashtable<Vector<String>, double[][]> codef=covtot.getresult();
		Hashtable<Vector<String>, double[][]> cotime=covtime.getresult();
		Hashtable<Vector<String>, double[][]> coobs=covobs.getresult();

		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);

		Hashtable<String, String> tempmd=new Hashtable<String, String>();
		Hashtable<String, String> clvar=new Hashtable<String, String>();
		clvar.put("0", "%1352%");
		int rifcl=1;

		itt = timen.iterator();
		Hashtable<String, String> cltime=new Hashtable<String, String>();
		while(itt.hasNext())
		{
			String currenttime= itt.next();
			clvar.put(String.valueOf(rifcl), "%1353%: "+currenttime);
			cltime.put(currenttime, String.valueOf(rifcl));
			rifcl++;
		}
		Iterator<String> ito = obsn.iterator();
		Hashtable<String, String> clobs=new Hashtable<String, String>();
		while(ito.hasNext())
		{
			String currentobs=ito.next();
			clvar.put(String.valueOf(rifcl), "%1354%: "+currentobs);
			clobs.put(currentobs, String.valueOf(rifcl));
			rifcl++;
		}
		dsu.addnewvar("rif", "%1351%", Keywords.TEXTSuffix, clvar, tempmd);
		Hashtable<String, String> clvarn=new Hashtable<String, String>();
		for (int j=0; j<tempvarname.length; j++)
		{
			clvarn.put(tempvarname[j], dict.getvarlabelfromname(tempvarname[j]));
		}
		dsu.addnewvar("var", "%1355%", Keywords.TEXTSuffix, clvarn, tempmd);
		for (int j=0; j<tempvarname.length; j++)
		{
			dsu.addnewvar("v"+String.valueOf(j), dict.getvarlabelfromname(tempvarname[j]), Keywords.NUMSuffix, tempmd, tempmd);
		}
		if (!dwc.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dwc.getmessage(), false, null);

		Vector<String> all=new Vector<String>();
		all.add(null);
		double[][] allc=codef.get(all);

		String[] valuestowrite=new String[tempvarname.length+2];
		valuestowrite[0]="0";
		for (int i=0; i<tempvarname.length; i++)
		{
			valuestowrite[1]=tempvarname[i];
			for (int j=0; j<tempvarname.length; j++)
			{
				valuestowrite[j+2]=double2String(allc[i][j]);
			}
			dwc.write(valuestowrite);
		}

		itt = timen.iterator();
		while(itt.hasNext())
		{
			String currenttime=itt.next();
			Vector<String> vtime=new Vector<String>();
			vtime.add(currenttime);
			allc=cotime.get(vtime);
			valuestowrite[0]=cltime.get(currenttime);
			for (int i=0; i<tempvarname.length; i++)
			{
				valuestowrite[1]=tempvarname[i];
				for (int j=0; j<tempvarname.length; j++)
				{
					valuestowrite[j+2]=double2String(allc[i][j]);
				}
				dwc.write(valuestowrite);
			}
		}
		ito = obsn.iterator();
		while(ito.hasNext())
		{
			String currentobs=ito.next();
			Vector<String> vobs=new Vector<String>();
			vobs.add(currentobs);
			allc=coobs.get(vobs);
			valuestowrite[0]=clobs.get(currentobs);
			for (int i=0; i<tempvarname.length; i++)
			{
				valuestowrite[1]=tempvarname[i];
				for (int j=0; j<tempvarname.length; j++)
				{
					valuestowrite[j+2]=double2String(allc[i][j]);
				}
				dwc.write(valuestowrite);
			}
		}

		Vector<StepResult> result = new Vector<StepResult>();
		boolean resclosem=dwc.close();
		if (!resclosem)
			return new Result(dwc.getmessage(), false, null);
		Vector<Hashtable<String, String>> ctablevariableinfo=dwc.getVarInfo();
		Hashtable<String, String> cdatatableinfo=dwc.getTableInfo();
		result.add(new LocalDictionaryWriter(dwc.getdictpath(), keyword, description, author, dwc.gettabletype(),
		cdatatableinfo, dsu.getfinalvarinfo(), ctablevariableinfo, dsu.getfinalcl(), dsu.getfinalmd(), null));
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
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 1350, dep, "", 1));
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
		parameters.add(new GetRequiredParameters(Keywords.transform, "listsingle=1357_"+Keywords.notransform+",647_"+Keywords.standardize+",648_"+Keywords.divformax+",649_"+Keywords.normalize01
		+",650_"+Keywords.meannormalize+",651_"+Keywords.sumnormalize,false, 1367, dep, "", 2));
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
		retprocinfo[1]="1345";
		return retprocinfo;
	}
}
