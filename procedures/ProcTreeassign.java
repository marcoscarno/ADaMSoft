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

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Vector;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;

/**
* This is the procedure that assign a record to a class using an evaluated tree
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcTreeassign extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Treeassign
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict+"t", Keywords.dict};
		String [] optionalparameters=new String[] {Keywords.assignonlytomax, Keywords.where, Keywords.forceequals, Keywords.useprob, Keywords.replace, };
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		String replace =(String)parameters.get(Keywords.replace);

		boolean forceequals =(parameters.get(Keywords.forceequals)!=null);
		boolean useprob =(parameters.get(Keywords.useprob)!=null);
		boolean assignonlytomax=(parameters.get(Keywords.assignonlytomax)!=null);

		int rifrep=0;
		if (replace==null)
			rifrep=0;
		else if (replace.equalsIgnoreCase(Keywords.replaceall))
			rifrep=1;
		else if (replace.equalsIgnoreCase(Keywords.replaceformat))
			rifrep=2;
		else if (replace.equalsIgnoreCase(Keywords.replacemissing))
			rifrep=3;

		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		DictionaryReader dictt = (DictionaryReader)parameters.get(Keywords.dict+"t");
		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		int numvarc=dictt.gettotalvar();
		int totalnodes=0;
		int totaldepvalues=0;
		Vector<String> depvalues=new Vector<String>();
		boolean iscorrect=true;
		for (int i=0; i<numvarc; i++)
		{
			String tempname=dictt.getvarname(i);
			if ((tempname.toLowerCase()).startsWith("node_"))
			{
				try
				{
					String tc=tempname.substring(5);
					int actualn=Integer.parseInt(tc);
					if (actualn>totalnodes)
						totalnodes=actualn;
				}
				catch (Exception ec)
				{
					iscorrect=false;
				}
			}
			if (tempname.toLowerCase().startsWith("freq_"))
			{
				try
				{
					String[] tc=tempname.split("_");
					depvalues.add(tc[2]);
				}
				catch (Exception ec)
				{
					iscorrect=false;
				}
			}
		}
		if (!iscorrect)
			return new Result("%1905%<br>\n", false, null);
		totaldepvalues=depvalues.size();

		int totalvartoread=(totalnodes+1)*2+1+totaldepvalues;

		Vector<String[]> nodes=new Vector<String[]>();
		Vector<String[]> nodevalues=new Vector<String[]>();
		Vector<double[]> freqd=new Vector<double[]>();
		Vector<String> decision=new Vector<String>();

		String[] vartoreadint=new String[totalvartoread];
		int[] reprulet=new int[totalvartoread];
		for (int i=0; i<(totalnodes+1); i++)
		{
			reprulet[i]=0;
			vartoreadint[i]="node_"+String.valueOf(i);
		}
		int rifdepval=0;
		for (int i=(totalnodes+1); i<(totalnodes+1)+(totalnodes+1); i++)
		{
			reprulet[i]=rifrep;
			vartoreadint[i]="val_"+String.valueOf(rifdepval);
			rifdepval++;
		}
		rifdepval=0;
		for (int i=(totalnodes+1)*2; i<(totalnodes+1)*2+totaldepvalues; i++)
		{
			reprulet[i]=0;
			vartoreadint[i]="freq_"+String.valueOf(rifdepval)+"_"+depvalues.get(rifdepval);
			rifdepval++;
		}
		vartoreadint[vartoreadint.length-1]="decision";
		reprulet[vartoreadint.length-1]=rifrep;

		DataReader datat = new DataReader(dictt);
		if (!datat.open(vartoreadint, reprulet, false))
			return new Result(datat.getmessage(), false, null);

		while (!datat.isLast())
		{
			String[] values = datat.getRecord();
			String[] tempnodes=new String[totalnodes+1];
			String[] tempnodevalues=new String[totalnodes+1];
			for (int i=0; i<totalnodes+1; i++)
			{
				tempnodes[i]=values[i];
				tempnodevalues[i]=values[i+totalnodes+1];
			}
			double[] freqdepvar=new double[totaldepvalues];
			for (int i=0; i<totaldepvalues; i++)
			{
				try
				{
					freqdepvar[i]=Double.NaN;
					if (!values[i+(totalnodes+1)*2].equals(""))
					{
						double temp=Double.parseDouble(values[i+(totalnodes+1)*2]);
						freqdepvar[i]=temp;
					}
				}
				catch (Exception e)
				{
					iscorrect=false;
				}
			}
			decision.add(values[values.length-1]);
			nodes.add(tempnodes);
			nodevalues.add(tempnodevalues);
			freqd.add(freqdepvar);
		}
		datat.close();
		if (!iscorrect)
			return new Result("%1905%<br>\n", false, null);

		String keyword="Treeassign "+dict.getkeyword();
		String description="Treeassign "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		DataSetUtilities dsu=new DataSetUtilities();

		dsu.setreplace(replace);

		Hashtable<String, String> temph=new Hashtable<String, String>();

		dsu.defineolddict(dict);

		dsu.addnewvartoolddict("Treedecision", "%1912%", Keywords.TEXTSuffix, temph, temph);

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

		Hashtable<String, Integer> posvarindict=new Hashtable<String, Integer>();
		for (int i=0; i<nodes.size(); i++)
		{
			String[] tempnodes=nodes.get(i);
			for (int j=0; j<tempnodes.length; j++)
			{
				tempnodes[j]=tempnodes[j].trim();
				if (!tempnodes[j].equals(""))
					posvarindict.put(tempnodes[j], new Integer(0));
			}
		}

		int numvar=dict.gettotalvar();

		for (Enumeration<String> e = posvarindict.keys() ; e.hasMoreElements() ;)
		{
			String tvarname = ((String)e.nextElement());
			for (int i=0; i<numvar; i++)
			{
				String tempname=dict.getvarname(i);
				if (tempname.equalsIgnoreCase(tvarname))
					posvarindict.put(tvarname, new Integer(i));
			}
		}

		String[] vtor=new String[numvar];
		int[] reptor=new int[numvar];
		for (int i=0; i<numvar; i++)
		{
			vtor[i]=dict.getvarname(i);
			reptor[i]=rifrep;
		}


		DataReader data = new DataReader(dict);
		if (!data.open(vtor, reptor, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		double maxindex=0;
		int maxref=0;
		while (!data.isLast())
		{
			String[] values = data.getRecord();
			if (values!=null)
			{
				String[] newvalues=new String[1];
				newvalues[0]="";
				maxindex=0;
				maxref=-1;
				for (int i=0; i<nodes.size(); i++)
				{
					double actualindex=0;
					double depth=0;
					String[] tempnodes=nodes.get(i);
					String[] tempnodevalues=nodevalues.get(i);
					for (int j=0; j<tempnodes.length; j++)
					{
						try
						{
							double tdouble=Double.parseDouble(tempnodevalues[j]);
							if (!Double.isNaN(tdouble))
								tempnodevalues[j]=String.valueOf(tdouble);
						}
						catch (Exception e) {}
						if (!tempnodes[j].trim().equals(""))
						{
							depth++;
							int posvar=(posvarindict.get(tempnodes[j])).intValue();
							try
							{
								double ttdouble=Double.parseDouble(values[posvar]);
								if (!Double.isNaN(ttdouble))
									values[posvar]=String.valueOf(ttdouble);
							}
							catch (Exception e) {}
							if (values[posvar].equals(tempnodevalues[j]))
								actualindex++;
						}
					}
					actualindex=actualindex/depth;
					if (actualindex>maxindex)
					{
						maxindex=actualindex;
						maxref=i;
					}
				}
				if ((maxindex<1) && (assignonlytomax))
				{
					maxref=-1;
				}
				String refdec="-";
				if (maxref>-1)
				{
					refdec=(decision.get(maxref)).trim();
					if (useprob)
					{
						double[] tempfreqd=freqd.get(maxref);
						double sumfreq=0;
						for (int i=0; i<tempfreqd.length; i++)
						{
							sumfreq=sumfreq+tempfreqd[i];
						}
						double[] ttempfreqd=new double[tempfreqd.length];
						for (int i=0; i<tempfreqd.length; i++)
						{
							ttempfreqd[i]=tempfreqd[i]/sumfreq;
						}
						double casual=Math.random();
						double tempclass=0;
						int refnewclass=0;
						for (int i=0; i<tempfreqd.length; i++)
						{
							tempclass=tempclass+ttempfreqd[i];
							if (casual<=tempclass)
							{
								refnewclass=i;
								break;
							}
						}
						refdec=depvalues.get(refnewclass);
					}
					else if ((refdec.equals("-")) && (forceequals))
					{
						double[] tempfreqd=freqd.get(maxref);
						double sumfreq=0;
						for (int i=0; i<tempfreqd.length; i++)
						{
							sumfreq=sumfreq+tempfreqd[i];
						}
						double[] ttempfreqd=new double[tempfreqd.length];
						for (int i=0; i<tempfreqd.length; i++)
						{
							ttempfreqd[i]=tempfreqd[i]/sumfreq;
						}
						double casual=Math.random();
						double tempclass=0;
						int refnewclass=0;
						for (int i=0; i<tempfreqd.length; i++)
						{
							tempclass=tempclass+ttempfreqd[i];
							if (casual<=tempclass)
							{
								refnewclass=i;
								break;
							}
						}
						refdec=depvalues.get(refnewclass);
					}
				}
				newvalues[0]=refdec;
				String[] wvalues=dsu.getnewvalues(values, newvalues);
				dw.write(wvalues);
			}
		}
		data.close();

		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
		Hashtable<String, String> datatableinfo=dw.getTableInfo();
		Vector<StepResult> result = new Vector<StepResult>();
		result.add(new LocalDictionaryWriter(dw.getdictpath(), keyword, description, author, dw.gettabletype(),
		datatableinfo, dsu.getfinalvarinfo(), tablevariableinfo, dsu.getfinalcl(), dsu.getfinalmd(), null));
		return new Result("", true, result);
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 1906, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"t=", "dict", true, 1907, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 1908, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.assignonlytomax, "checkbox", false, 1913, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.forceequals, "checkbox", false, 1909, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.useprob, "checkbox", false, 1910, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2809,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="1462";
		retprocinfo[1]="1911";
		return retprocinfo;
	}
}
