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

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.StringComparator;

import ADaMSoft.keywords.Keywords;

import java.util.Hashtable;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.LinkedList;
import java.util.Vector;
import java.util.Iterator;


/**
* This is the procedure that projects the original values on the decomposition obtained by considering the ST Mean
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class ProcDfam1projectobs extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Dfam1projectobs
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean samplevariance=false;
		String [] requiredparameters=new String[] {Keywords.dict, Keywords.dicti, Keywords.dictstmd, Keywords.OUT.toLowerCase()};
		String [] optionalparameters=new String[] {Keywords.replace, Keywords.where};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		String replace =(String)parameters.get(Keywords.replace);
		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);
		DictionaryReader dicti = (DictionaryReader)parameters.get(Keywords.dicti);
		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		DataReader datai = new DataReader(dicti);
		String[] varininfo=new String[2];
		varininfo[0]="ref";
		varininfo[1]="value";
		int[] repininfo=new int[2];
		repininfo[0]=0;
		repininfo[1]=0;
		if (!datai.open(varininfo, repininfo, false))
			return new Result(datai.getmessage(), false, null);
		String transform=null;
		String tempvar=null;
		String tempvarobs=null;
		String tempvartime=null;
		String weight=null;
		String[] values=null;
		try
		{
			while (!datai.isLast())
			{
				values = datai.getRecord();
				if (values[0].equals("1"))
					tempvartime=values[1].trim();
				else if (values[0].equals("2"))
					tempvarobs=values[1].trim();
				else if (values[0].equals("3"))
					tempvar=values[1].trim();
				else if (values[0].equals("14"))
					weight=values[1].trim();
				else if (values[0].equals("4"))
					transform=values[1].trim();
			}
		}
		catch (Exception e)
		{
			datai.close();
			return new Result("%2057%<br>\n", false, null);
		}
		datai.close();
		if (transform==null)
			transform=Keywords.notransform;

		String[] transformation=new String[] {Keywords.notransform, Keywords.standardize, Keywords.divformax,
		Keywords.normalize01, Keywords.meannormalize, Keywords.sumnormalize,};
		int selectedoption=steputilities.CheckOption(transformation, transform);
		if (selectedoption==0)
			return new Result(steputilities.getMessage(), false, null);

		if ( (tempvar==null) || (tempvarobs==null) || (tempvartime==null) )
			return new Result("%2058%<br>\n", false, null);

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

		DictionaryReader dictstmd = (DictionaryReader)parameters.get(Keywords.dictstmd);
		DataReader datastmd = new DataReader(dictstmd);
		String[] varinistmd=new String[tempvarname.length+1];
		int[] repinistmd=new int[varinistmd.length];
		varinistmd[0]="type";
		repinistmd[0]=0;
		for (int i=0; i<tempvarname.length; i++)
		{
			varinistmd[i+1]="c_"+String.valueOf(i+1);
			repinistmd[i+1]=0;
		}
		if (!datastmd.open(varinistmd, repinistmd, false))
			return new Result(datastmd.getmessage(), false, null);
		double[][] matvec=new double[tempvarname.length][tempvarname.length];
		try
		{
			while (!datastmd.isLast())
			{
				values = datastmd.getRecord();
				if (values[0].toLowerCase().startsWith("evec_"))
				{
					String refvar=values[0].substring(5);
					int posrefvar=0;
					for (int i=0; i<tempvarname.length; i++)
					{
						if (refvar.equalsIgnoreCase(tempvarname[i]))
						{
							posrefvar=i;
							break;
						}
					}
					for (int i=1; i<values.length; i++)
					{
						double tempmatvec=Double.parseDouble(values[i]);
						matvec[posrefvar][i-1]=tempmatvec;
					}
				}
			}
		}
		catch (Exception e)
		{
			datastmd.close();
			return new Result("%2060%<br>\n", false, null);
		}
		datastmd.close();

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

		TreeSet<String> timen=new TreeSet<String>(new StringComparator());
		TreeSet<String> obsn=new TreeSet<String>(new StringComparator());
		HashSet<String[]> testobsfortime=new HashSet<String[]>();
		Hashtable<String, double[]> refsums=new Hashtable<String, double[]>();
		Hashtable<String, double[]> refnums=new Hashtable<String, double[]>();

		double nvalid=0;
		String obs="";
		double obsw=1;
		boolean ismissing=false;
		double currentvalue=Double.NaN;
		double time=Double.NaN;
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
					if (refsums.get(values[0])==null)
					{
						double[] temprefsums=new double[lastcol-2];
						double[] temprefnums=new double[lastcol-2];
						for (int i=0; i<lastcol-2; i++)
						{
							temprefsums[i]=0;
							temprefnums[i]=0;
						}
						refsums.put(values[0], temprefsums);
						refnums.put(values[0], temprefnums);
					}
					double[] trefsums=refsums.get(values[0]);
					double[] trefnums=refnums.get(values[0]);
					for (int i=2; i<lastcol; i++)
					{
						currentvalue=Double.parseDouble(values[i]);
						trefsums[i-2]+=currentvalue*obsw;
						trefnums[i-2]+=obsw;
						means[i-2]+=currentvalue*obsw;
						sum[i-2]+=currentvalue*obsw;
						sumsq[i-2]+=currentvalue*currentvalue*obsw;
						if (currentvalue<min[i-2])
							min[i-2]=currentvalue;
						if (currentvalue>max[i-2])
							max[i-2]=currentvalue;
					}
					refsums.put(values[0], trefsums);
					refnums.put(values[0], trefnums);
					nvalid+=obsw;
				}
			}
		}
		data.close();
		if ((nvalid==0) && (where!=null))
			return new Result("%2804%<br>\n", false, null);
		if ((nvalid==0) && (where==null))
			return new Result("%1338%<br>\n", false, null);

		for (int i=0; i<testvar.length-2; i++)
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
			String[] testot=new String[2];
			testot[0]=itt.next();
			while(ito.hasNext())
			{
				testot[1]=ito.next();
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
		Hashtable<String, String> temph=new Hashtable<String, String>();
		dsu.defineolddict(dict);
		for (int i=0; i<tempvarname.length; i++)
		{
			dsu.addnewvartoolddict("p_"+String.valueOf(i+1), "%2066% "+String.valueOf(i), Keywords.NUMSuffix, temph, temph);
		}

		if (replace!=null)
		{
			if (replace.equalsIgnoreCase(Keywords.replaceall))
			{
				dsu.setempycodelabels();
				dsu.setempymissingdata();
			}
			else if (replace.equalsIgnoreCase(Keywords.replaceformat))
				dsu.setempycodelabels();
			else if (replace.equalsIgnoreCase(Keywords.replacemissing))
				dsu.setempymissingdata();
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		String[] newvalues=new String[tempvarname.length];

		if (!data.open(testvar, replacerule, false))
			return new Result(data.getmessage(), false, null);

		double[] proj=new double[tempvarname.length];
		double[] valuerow=new double[tempvarname.length];

		while (!data.isLast())
		{
			for (int i=0; i<tempvarname.length; i++)
			{
				proj[i]=0;
				newvalues[i]="";
			}
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
				double[] trefsums=refsums.get(values[0]);
				double[] trefnums=refnums.get(values[0]);
				for (int i=2; i<lastcol; i++)
				{
					valuerow[i-2]=Double.parseDouble(values[i]);
					double newcenter=(trefsums[i-2]/trefnums[i-2]);
					if(selectedoption==2)
					{
						valuerow[i-2]=(valuerow[i-2]-means[i-2])/Math.pow(sumsq[i-2],0.5);
						newcenter=(trefsums[i-2]/trefnums[i-2])/Math.pow(sumsq[i-2],0.5);
					}
					if(selectedoption==3)
					{
						valuerow[i-2]=valuerow[i-2]/max[i-2];
						newcenter=(trefsums[i-2]/trefnums[i-2])/max[i-2];
					}
					if(selectedoption==4)
					{
						valuerow[i-2]=(valuerow[i-2]-min[i-2])/(max[i-2]-min[i-2]);
						newcenter=((trefsums[i-2]/trefnums[i-2])-min[i-2])/(max[i-2]-min[i-2]);
					}
					if(selectedoption==5)
					{
						valuerow[i-2]=valuerow[i-2]/means[i-2];
						newcenter=(trefsums[i-2]/trefnums[i-2])/means[i-2];
					}
					if(selectedoption==6)
					{
						valuerow[i-2]=valuerow[i-2]/sum[i-2];
						newcenter=(trefsums[i-2]/trefnums[i-2])/sum[i-2];
					}
					valuerow[i-2]=valuerow[i-2]-newcenter;
				}
				for (int i=0; i<tempvarname.length; i++)
				{
					for (int j=0; j<tempvarname.length; j++)
					{
						proj[i]=proj[i]+valuerow[j]*matvec[j][i];
					}
					newvalues[i]=double2String(proj[i]);
				}
			}
			String[] wvalues=dsu.getnewvalues(values, newvalues);
			dw.write(wvalues);
		}
		data.close();

		Vector<StepResult> result = new Vector<StepResult>();
		boolean resclo2=dw.close();
		if (!resclo2)
			return new Result(dw.getmessage(), false, null);
		Vector<Hashtable<String, String>> ctablevariableinfo2=dw.getVarInfo();
		Hashtable<String, String> cdatatableinfo2=dw.getTableInfo();
		result.add(new LocalDictionaryWriter(dw.getdictpath(), "DfaModel1 obs projection "+dict.getkeyword(), "DfaModel1 obs projection  "+dict.getdescription(), author, dw.gettabletype(),
		cdatatableinfo2, dsu.getfinalvarinfo(), ctablevariableinfo2, dsu.getfinalcl(), dsu.getfinalmd(), null));
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
		parameters.add(new GetRequiredParameters(Keywords.dicti+"=", "dict", true, 2055, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dictstmd+"=", "dict", true, 2056, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2809,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 2054, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
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
		retprocinfo[1]="2053";
		return retprocinfo;
	}
}
