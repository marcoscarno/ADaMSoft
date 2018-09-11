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

import java.util.TreeSet;

import java.util.Hashtable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
import java.util.Enumeration;

import ADaMSoft.algorithms.frequencies.TabulateCreator;
import ADaMSoft.algorithms.frequencies.ResTabulate;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ObjectTransformer;

/**
* This is the procedure that build a table with statistics on couple of variables
* @author mscarno@caspur.it
* @version 1.0.0, rev.: 14/02/2017
*/
public class ProcTabulate extends ObjectTransformer implements RunStep
{
	boolean setprogvarnames;
	/**
	* Starts the execution of Proc Tabulate
	*/
	@SuppressWarnings("unused")
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean orderbyval;
		boolean usewritefmt=false;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.table};
		String [] optionalparameters=new String[] {Keywords.statistics, Keywords.varclass, Keywords.weight,
		Keywords.setprogvarnames, Keywords.replace, Keywords.mdhandling, Keywords.mdsubst,
		Keywords.orderbyval, Keywords.usewritefmt,Keywords.noclforvarrow,
		Keywords.shortvarrowinfo, Keywords.shortvarcolinfo, Keywords.where};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		orderbyval=(parameters.get(Keywords.orderbyval)!=null);
		setprogvarnames =(parameters.get(Keywords.setprogvarnames)!=null);
		String replace =(String)parameters.get(Keywords.replace);
		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		usewritefmt=(parameters.get(Keywords.usewritefmt)!=null);
		boolean noclforvarrow=(parameters.get(Keywords.noclforvarrow)!=null);
		boolean shortvarrowinfo=(parameters.get(Keywords.shortvarrowinfo)!=null);
		boolean shortvarcolinfo=(parameters.get(Keywords.shortvarcolinfo)!=null);

		String table =(String)parameters.get(Keywords.table);
		String statistics =(String)parameters.get(Keywords.statistics);
		String tempvarclass =(String)parameters.get(Keywords.varclass);
		String weight =(String)parameters.get(Keywords.weight);
		String[] varclass=new String[0];
		if (tempvarclass!=null)
		{
			tempvarclass=tempvarclass.trim();
			varclass=tempvarclass.split(" ");
		}
		table=table.trim();
		String[] tableparts=table.split("\\*");
		table="";
		for (int i=0; i<tableparts.length; i++)
		{
			table=table+tableparts[i].trim();
			if (i<tableparts.length-1)
				table=table+"*";
		}
		if (statistics==null)
			statistics="";
		statistics=statistics.trim();
		boolean nocol=false;
		boolean norow=false;
		boolean onedim=false;
		if (table.indexOf(",")<0)
			nocol=true;
		if (table.startsWith(","))
		{	table=table.substring(table.indexOf(",")+1);
			norow=true;
		}

		HashSet<String> dvar=new HashSet<String>();
		Hashtable<Integer, Vector<String>> rowvars=new Hashtable<Integer, Vector<String>>();
		Hashtable<Integer, Vector<String>> colvars=new Hashtable<Integer, Vector<String>>();
		Hashtable<Vector<Integer>, String> stats=new Hashtable<Vector<Integer>, String>();
		String[] vars=new String[0];
		if ((!nocol) && (!norow))
		{
			String[] tempt=table.split(",");
			if (tempt.length!=2)
				return new Result("%2113%<br>\n", false, null);
			for (int p=0; p<tempt.length; p++)
			{
				int refr=0;
				int refc=0;
				String[] part=(tempt[p].trim()).split(" ");
				for (int i=0; i<part.length; i++)
				{
					part[i]=part[i].trim();
					if (!part[i].equals(""))
					{
						Vector<String> tempv=new Vector<String>();
						if ( (part[i].trim().startsWith("*")) || (part[i].trim().endsWith("*")) )
							return new Result("%2114%<br>\n", false, null);
						String[] insidepart=(part[i].trim()).split("\\*");
						for (int j=0; j<insidepart.length; j++)
						{
							insidepart[j]=insidepart[j].trim();
							if (!insidepart[j].equals(""))
							{
								tempv.add(insidepart[j]);
								dvar.add(insidepart[j]);
							}
						}
						if (p==0)
						{
							rowvars.put(new Integer(refr), tempv);
							refr++;
						}
						else
						{
							colvars.put(new Integer(refc), tempv);
							refc++;
						}
					}
				}
			}
		}
		else
		{
			onedim=true;
			int ref=0;
			String[] part=(table.trim()).split(" ");
			for (int i=0; i<part.length; i++)
			{
				part[i]=part[i].trim();
				if (!part[i].equals(""))
				{
					Vector<String> tempv=new Vector<String>();
					if ( (part[i].trim().startsWith("*")) || (part[i].trim().endsWith("*")) )
						return new Result("%2114%<br>\n", false, null);
					String[] insidepart=(part[i].trim()).split("\\*");
					for (int j=0; j<insidepart.length; j++)
					{
						insidepart[j]=insidepart[j].trim();
						if (!insidepart[j].equals(""))
						{
							tempv.add(insidepart[j]);
							dvar.add(insidepart[j]);
						}
					}
					if (norow)
						colvars.put(new Integer(ref), tempv);
					else
						rowvars.put(new Integer(ref), tempv);
					ref++;
				}
			}
		}
		Iterator<String> it=dvar.iterator();
		int pointer=0;
		boolean existone=false;
		while (it.hasNext())
		{
			String checkisone=(it.next()).trim();
			if (!checkisone.equals("1"))
				pointer++;
			else
				existone=true;
		}
		vars=new String[pointer];
		if (pointer==0)
			return new Result("%2161%<br>\n", false, null);
		it=dvar.iterator();
		pointer=0;
		while (it.hasNext())
		{
			String checkisone=(it.next()).trim();
			if (!checkisone.equals("1"))
			{
				vars[pointer]=checkisone;
				pointer++;
			}
		}
		for (int i=0; i<varclass.length; i++)
		{
			boolean varexist=false;
			for (int j=0; j<vars.length; j++)
			{
				if (vars[j].equalsIgnoreCase(varclass[i]))
					varexist=true;
			}
			if (!varexist)
				return new Result("%2121% ("+varclass[i]+")<br>\n", false, null);
		}
		if (existone)
		{
			String[] tvarclass=new String[varclass.length+1];
			for (int i=0; i<varclass.length; i++)
			{
				tvarclass[i]=varclass[i];
			}
			tvarclass[varclass.length]="1";
			varclass=new String[tvarclass.length];
			for (int i=0; i<tvarclass.length; i++)
			{
				varclass[i]=tvarclass[i];
			}
		}
		Vector<String> tempvarsnoclass=new Vector<String>();
		for (int i=0; i<vars.length; i++)
		{
			boolean varisnotnum=false;
			for (int j=0; j<varclass.length; j++)
			{
				if (vars[i].equalsIgnoreCase(varclass[j]))
					varisnotnum=true;
			}
			if (!varisnotnum)
				tempvarsnoclass.add(vars[i]);
		}
		String[] varnotclass=new String[tempvarsnoclass.size()];
		for (int i=0; i<tempvarsnoclass.size(); i++)
		{
			varnotclass[i]=tempvarsnoclass.get(i);
		}
		if (colvars.size()==0)
		{
			for (Enumeration<Integer> en=rowvars.keys(); en.hasMoreElements();)
			{
				Integer keyname=en.nextElement();
				Vector<String> varnames=rowvars.get(keyname);
				boolean isnum=false;
				for (int i=0; i<varnames.size(); i++)
				{
					for (int j=0; j<varnotclass.length; j++)
					{
						if (varnames.get(i).equalsIgnoreCase(varnotclass[j]))
							isnum=true;
					}
				}
				Vector<Integer> stattoput=new Vector<Integer>();
				stattoput.add(keyname);
				if (isnum)
					stats.put(stattoput, Keywords.MEAN);
				else
					stats.put(stattoput, Keywords.simplecounts);
			}
		}
		else if (rowvars.size()==0)
		{
			for (Enumeration<Integer> en=colvars.keys(); en.hasMoreElements();)
			{
				Integer keyname=en.nextElement();
				Vector<String> varnames=colvars.get(keyname);
				boolean isnum=false;
				for (int i=0; i<varnames.size(); i++)
				{
					for (int j=0; j<varnotclass.length; j++)
					{
						if (varnames.get(i).equalsIgnoreCase(varnotclass[j]))
							isnum=true;
					}
				}
				Vector<Integer> stattoput=new Vector<Integer>();
				stattoput.add(keyname);
				if (isnum)
					stats.put(stattoput, Keywords.MEAN);
				else
					stats.put(stattoput, Keywords.simplecounts);
			}
		}
		else
		{
			for (Enumeration<Integer> enr=rowvars.keys(); enr.hasMoreElements();)
			{
				Integer keynamer=enr.nextElement();
				Vector<String> varnamesr=rowvars.get(keynamer);
				boolean isnumrow=false;
				for (int i=0; i<varnamesr.size(); i++)
				{
					for (int j=0; j<varnotclass.length; j++)
					{
						if (varnamesr.get(i).equalsIgnoreCase(varnotclass[j]))
							isnumrow=true;
					}
				}
				for (Enumeration<Integer> enc=colvars.keys(); enc.hasMoreElements();)
				{
					boolean isnumcol=false;
					Integer keynamec=enc.nextElement();
					Vector<String> varnamesc=colvars.get(keynamec);
					for (int i=0; i<varnamesc.size(); i++)
					{
						for (int j=0; j<varnotclass.length; j++)
						{
							if (varnamesc.get(i).equalsIgnoreCase(varnotclass[j]))
								isnumcol=true;
						}
					}
					Vector<Integer> stattoput=new Vector<Integer>();
					stattoput.add(keynamer);
					stattoput.add(keynamec);
					if ((isnumrow) || (isnumcol))
						stats.put(stattoput, Keywords.MEAN);
					else
						stats.put(stattoput, Keywords.simplecounts);
				}
			}
		}

		String[] acceptedstats={Keywords.simplecounts, Keywords.rowfreq, Keywords.rowpercentfreq,
		Keywords.colfreq, Keywords.colpercentfreq, Keywords.relfreq, Keywords.relpercentfreq,
		Keywords.MEAN, Keywords.SUM, Keywords.STD, Keywords.N};

		String[] qualstats={Keywords.simplecounts, Keywords.rowfreq, Keywords.rowpercentfreq,
		Keywords.colfreq, Keywords.colpercentfreq, Keywords.relfreq, Keywords.relpercentfreq};
		String[] numstats={Keywords.MEAN, Keywords.SUM, Keywords.STD, Keywords.N};

		if (!statistics.equals(""))
		{
			try
			{
				String[] statparts=statistics.split(";");
				if (onedim)
				{
					for (int i=0; i<statparts.length; i++)
					{
						statparts[i]=statparts[i].trim();
						String[] parts=statparts[i].split("=");
						int ref=Integer.parseInt(parts[0].trim());
						ref=ref-1;
						boolean accepted=false;
						for (int j=0; j<acceptedstats.length; j++)
						{
							if (acceptedstats[j].equalsIgnoreCase(parts[1].trim()))
								accepted=true;
						}
						parts[1]=parts[1].trim();
						if (!accepted)
						{
							String msg="%2118% ("+parts[1]+")<br>\n%2119%<br>\n";
							for (int j=0; j<acceptedstats.length; j++)
							{
								msg=msg+acceptedstats[j].toUpperCase()+"\n";
							}
							return new Result(msg, false, null);
						}
						Vector<Integer> testval=new Vector<Integer>();
						testval.add(new Integer(ref));
						String testvalstat=stats.get(testval);
						if (testvalstat==null)
							return new Result("%2115% "+parts[0]+"="+parts[1]+"<br>\n", false, null);
						boolean isnum=false;
						if (testvalstat.equals(Keywords.MEAN))
							isnum=true;
						if (isnum)
						{
							boolean rescheck=true;
							for (int j=0; j<qualstats.length; j++)
							{
								if (parts[1].equalsIgnoreCase(qualstats[j]))
									rescheck=false;
							}
							if (!rescheck)
							{
								String msg="%2128% ("+parts[0]+"="+parts[1]+")<br>\n%2119%<br>\n";
								for (int j=0; j<numstats.length; j++)
								{
									msg=msg+numstats[j].toUpperCase()+"<br>\n";
								}
								return new Result(msg, false, null);
							}
						}
						else
						{
							boolean rescheck=true;
							for (int j=0; j<numstats.length; j++)
							{
								if (parts[1].equalsIgnoreCase(numstats[j]))
									rescheck=false;
							}
							if (!rescheck)
							{
								String msg="%2129% ("+parts[0]+"="+parts[1]+")<br>\n%2119%<br>\n";
								for (int j=0; j<qualstats.length; j++)
								{
									msg=msg+qualstats[i].toUpperCase()+"<br>\n";
								}
								return new Result(msg, false, null);
							}
						}
						stats.put(testval, parts[1]);
					}
				}
				else
				{
					for (int i=0; i<statparts.length; i++)
					{
						statparts[i]=statparts[i].trim();
						String[] parts=statparts[i].split("=");
						String[] firstparts=parts[0].split(",");
						int ref1=Integer.parseInt(firstparts[0].trim());
						int ref2=Integer.parseInt(firstparts[1].trim());
						ref1=ref1-1;
						ref2=ref2-1;
						boolean accepted=false;
						for (int j=0; j<acceptedstats.length; j++)
						{
							if (acceptedstats[j].equalsIgnoreCase(parts[1].trim()))
								accepted=true;
						}
						parts[1]=parts[1].trim();
						if (!accepted)
						{
							String msg="%2116% ("+parts[1]+")<br>\n%2119%<br>\n";
							for (int j=0; j<acceptedstats.length; j++)
							{
								msg=msg+acceptedstats[j].toUpperCase()+"<br>\n";
							}
							return new Result(msg, false, null);
						}
						Vector<Integer> testval=new Vector<Integer>();
						testval.add(new Integer(ref1));
						testval.add(new Integer(ref2));
						String testvalstat=stats.get(testval);
						if (testvalstat==null)
							return new Result("%2115% "+parts[0]+"="+parts[1]+"<br>\n", false, null);
						boolean isnum=false;
						if (testvalstat.equals(Keywords.MEAN))
							isnum=true;
						if (isnum)
						{
							boolean rescheck=true;
							for (int j=0; j<qualstats.length; j++)
							{
								if (parts[1].equalsIgnoreCase(qualstats[j]))
									rescheck=false;
							}
							if (!rescheck)
							{
								String msg="%2128% ("+parts[0]+"="+parts[1]+")<br>\n%2119%<br>\n";
								for (int j=0; j<numstats.length; j++)
								{
									msg=msg+numstats[j].toUpperCase()+"<br>\n";
								}
								return new Result(msg, false, null);
							}
						}
						else
						{
							boolean rescheck=true;
							for (int j=0; j<numstats.length; j++)
							{
								if (parts[1].equalsIgnoreCase(numstats[j]))
									rescheck=false;
							}
							if (!rescheck)
							{
								String msg="%2129% ("+parts[0]+"="+parts[1]+")<br>\n%2119%<br>\n";
								for (int j=0; j<qualstats.length; j++)
								{
									msg=msg+qualstats[j].toUpperCase()+"<br>\n";
								}
								return new Result(msg, false, null);
							}
						}
						stats.put(testval, parts[1]);
					}
				}
			}
			catch (Exception ex)
			{
				return new Result("%2115%<br>\n", false, null);
			}
		}

 		if (weight!=null)
		{
			String[] tvars=new String[vars.length+1];
			for (int j=0; j<vars.length; j++)
			{
				tvars[j]=vars[j];
			}
			tvars[vars.length]=weight.trim();
			vars=new String[tvars.length];
			for (int j=0; j<tvars.length; j++)
			{
				vars[j]=tvars[j];
			}
		}
		int rifrep=0;
		if (replace==null)
			rifrep=0;
		else if (replace.equalsIgnoreCase(Keywords.replaceall))
			rifrep=1;
		else if (replace.equalsIgnoreCase(Keywords.replaceformat))
			rifrep=2;
		else if (replace.equalsIgnoreCase(Keywords.replacemissing))
			rifrep=3;
		int[] replacerule=new int[vars.length];
		Hashtable<String, Integer> varposition=new Hashtable<String, Integer>();
		for (int j=0; j<vars.length; j++)
		{
			replacerule[j]=rifrep;
			varposition.put(vars[j].toLowerCase(), new Integer(j));
		}

		String mdsubst=(String)parameters.get(Keywords.mdsubst);
		if (mdsubst==null)
			mdsubst="-";

		String mdh=(String)parameters.get(Keywords.mdhandling);
		if (mdh==null)
			mdh=Keywords.pairwisenomd;
		String[] mdt=new String[] {Keywords.pairwisenomd, Keywords.pairwisewithmd, Keywords.casewise};
		int mdhand=steputilities.CheckOption(mdt, mdh);
		if (mdhand==0)
			return new Result("%1775% "+Keywords.mdhandling.toUpperCase()+"<br>\n"+steputilities.getMessage(), false, null);

		if ((rowvars.size()==0) && (!setprogvarnames))
		{
			TreeSet<String> testdoublevar=new TreeSet<String>();
			for (int c=0; c<colvars.size(); c++)
			{
				Integer keyname=new Integer(c);
				Vector<Integer> stattouse=new Vector<Integer>();
				stattouse.add(keyname);
				String stat=stats.get(stattouse);
				String outvarname=stat+"_";
				Vector<String> varnames=colvars.get(keyname);
				for (int i=0; i<varnames.size(); i++)
				{
					outvarname=outvarname+varnames.get(i);
				}
				outvarname=outvarname.toLowerCase();
				if (testdoublevar.contains(outvarname))
					return new Result("%2137%<br>\n", false, null);
				testdoublevar.add(outvarname);
			}
		}

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		String keyword="Tabulate "+dict.getkeyword();
		String description="Tabulate "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		DataReader data = new DataReader(dict);

		if (!data.open(vars, replacerule, usewritefmt))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		TabulateCreator tc=new TabulateCreator(varclass, varposition);
		tc.setrowvars(rowvars);
		tc.setcolvars(colvars);

		int validgroup=0;
		boolean usecurrent=true;
		double weightvalue=1;
		String[][] values=new String[0][0];
		while (!data.isLast())
		{
			usecurrent=true;
			values = data.getOriginalTransformedRecord();
			if (values!=null)
			{
				if (weight!=null)
				{
					weightvalue=Double.parseDouble(values[values.length-1][1]);
				}
				if (Double.isNaN(weightvalue))
					usecurrent=false;
				if (mdhand==3)
				{
					for (int i=0; i<values.length; i++)
					{
						if (values[i][1].equals(""))
						{
							usecurrent=false;
							break;
						}
					}
				}
				else if (mdhand==2)
				{
					for (int i=0; i<values.length; i++)
					{
						if (values[i][1].equals(""))
							values[i][1]=mdsubst;
					}
				}
				if (usecurrent)
				{
					validgroup++;
					tc.setValues(values, weightvalue);
				}
			}
		}
		data.close();
		if ((validgroup==0) && (where!=null))
			return new Result("%2804%<br>\n", false, null);
		if ((validgroup==0) && (where==null))
			return new Result("%666%<br>\n", false, null);

		tc.calculate(orderbyval);

		if (rowvars.size()==0)
		{
			Hashtable<Integer, LinkedList<ResTabulate>> colres=tc.getcolres();
			DataSetUtilities dsu=new DataSetUtilities();
			Hashtable<String, String> clvar=new Hashtable<String, String> ();
			int totvar=0;
			for (int c=0; c<colvars.size(); c++)
			{
				Integer keyname=new Integer(c);
				Vector<Integer> stattouse=new Vector<Integer>();
				stattouse.add(keyname);
				String stat=stats.get(stattouse);
				boolean isnum=false;
				for (int i=0; i<numstats.length; i++)
				{
					if (stat.equalsIgnoreCase(numstats[i]))
						isnum=true;
				}
				String outvarlabel="";
				String outvarname="";
				Vector<String> varnames=colvars.get(keyname);
				for (int i=0; i<varnames.size(); i++)
				{
					outvarname=outvarname+varnames.get(i);
					if (!varnames.get(i).equals("1"))
						outvarlabel=outvarlabel+dict.getvarlabelfromname(varnames.get(i));
					else
						outvarlabel="%2162%";
					if (i<varnames.size()-1)
					{
						outvarname=outvarname+"_";
						outvarlabel=outvarlabel+"-";
					}
				}
				LinkedList<ResTabulate> tempcolres=colres.get(keyname);
				int ref=1;
				Iterator<ResTabulate> igv = tempcolres.iterator();
				while(igv.hasNext())
				{
					Vector<String> tempgroup=(igv.next()).getVec();
					String vl="";
					for (int i=0; i<tempgroup.size(); i++)
					{
						String tl=tempgroup.get(i);
						if (!tempgroup.equals(""))
							vl=vl+tl;
						if (i<tempgroup.size()-1)
							vl=vl+"-";
					}
					vl=vl.trim();
					if ((vl.endsWith("-")) && (vl.length()>1))
						vl=vl.substring(0, vl.length()-1);
					else if ((vl.endsWith("-")) && (vl.length()==1))
						vl="";
					//if ((vl.startsWith("-")) && (vl.length()>1))
					//	vl=vl.substring(1);
					if ((vl.startsWith("-")) && (vl.length()==1))
						vl="";
					if (!shortvarcolinfo)
						vl="("+vl+")";
					if (vl.equals("()"))
						vl="";
					if ((shortvarcolinfo) && (vl.equals("")))
						vl="-";
					if (!setprogvarnames)
					{
						if (!shortvarcolinfo)
							dsu.addnewvar(stat+"_"+outvarname+"_"+String.valueOf(ref), tc.getcodestat(stat)+" "+outvarlabel+" "+vl, Keywords.NUMSuffix, clvar, clvar);
						else
							dsu.addnewvar(stat+"_"+outvarname+"_"+String.valueOf(ref), vl, Keywords.NUMSuffix, clvar, clvar);
					}
					else
					{
						if (!shortvarcolinfo)
							dsu.addnewvar("v_"+String.valueOf(totvar), tc.getcodestat(stat)+" "+outvarlabel+" "+vl, Keywords.NUMSuffix, clvar, clvar);
						else
							dsu.addnewvar("v_"+String.valueOf(totvar), vl, Keywords.NUMSuffix, clvar, clvar);
					}
					ref++;
					totvar++;
				}
				if (!isnum)
				{
					if (!setprogvarnames)
					{
						if (!shortvarcolinfo)
							dsu.addnewvar("tot_"+outvarname, "%1013% "+outvarlabel, Keywords.NUMSuffix, clvar, clvar);
						else
							dsu.addnewvar("tot_"+outvarname, outvarlabel.toUpperCase(), Keywords.NUMSuffix, clvar, clvar);
					}
					else
					{
						if (!shortvarcolinfo)
							dsu.addnewvar("v_"+String.valueOf(totvar), "%1013% "+outvarlabel, Keywords.NUMSuffix, clvar, clvar);
						else
							dsu.addnewvar("v_"+String.valueOf(totvar), outvarlabel.toUpperCase(), Keywords.NUMSuffix, clvar, clvar);
					}
					totvar++;
				}
			}
			if (!dw.opendatatable(dsu.getfinalvarinfo()))
				return new Result(dw.getmessage(), false, null);
			String[] valuestowrite=new String[totvar];
			int ref=0;
			for (int c=0; c<colvars.size(); c++)
			{
				Integer keyname=new Integer(c);
				Vector<Integer> stattouse=new Vector<Integer>();
				stattouse.add(keyname);
				String stat=stats.get(stattouse);
				boolean isnum=false;
				for (int i=0; i<numstats.length; i++)
				{
					if (stat.equalsIgnoreCase(numstats[i]))
						isnum=true;
				}
				LinkedList<ResTabulate> tempcolres=colres.get(keyname);
				if (tempcolres==null)
					return new Result("%2140%<br>\n", false, null);
				else if (tempcolres.size()==0)
					return new Result("%2140%<br>\n", false, null);
				Iterator<ResTabulate> igv = tempcolres.iterator();
				double tot=0;
				while(igv.hasNext())
				{
					ResTabulate temprestabulate=igv.next();
					double[] tres=temprestabulate.getFre();
					tot=tot+tres[0];
				}
				igv = tempcolres.iterator();
				while(igv.hasNext())
				{
					ResTabulate temprestabulate=igv.next();
					double[] tres=temprestabulate.getFre();
					if (stat.equalsIgnoreCase(Keywords.MEAN))
						valuestowrite[ref]=double2String(tres[1]/tres[0]);
					else if (stat.equalsIgnoreCase(Keywords.STD))
					{
						double tval=(tres[2]/tres[0])-(tres[1]/tres[0])*(tres[1]/tres[0]);
						valuestowrite[ref]=double2String(Math.sqrt(tval));
					}
					else if (stat.equalsIgnoreCase(Keywords.SUM))
						valuestowrite[ref]=double2String(tres[1]);
					else if (stat.equalsIgnoreCase(Keywords.N))
						valuestowrite[ref]=double2String(tres[0]);
					else if (stat.equalsIgnoreCase(Keywords.rowfreq))
						valuestowrite[ref]=double2String(tres[0]/tot);
					else if (stat.equalsIgnoreCase(Keywords.rowpercentfreq))
						valuestowrite[ref]=double2String(100*tres[0]/tot);
					else if (stat.equalsIgnoreCase(Keywords.colfreq))
						valuestowrite[ref]=double2String(1);
					else if (stat.equalsIgnoreCase(Keywords.colpercentfreq))
						valuestowrite[ref]=double2String(100);
					else if (stat.equalsIgnoreCase(Keywords.relfreq))
						valuestowrite[ref]=double2String(tres[0]/tot);
					else if (stat.equalsIgnoreCase(Keywords.relpercentfreq))
						valuestowrite[ref]=double2String(100*tres[0]/tot);
					else
						valuestowrite[ref]=double2String(tres[0]);
					ref++;
				}
				if (!isnum)
				{
					if (stat.equalsIgnoreCase(Keywords.rowfreq))
						valuestowrite[ref]=double2String(1);
					else if (stat.equalsIgnoreCase(Keywords.rowpercentfreq))
						valuestowrite[ref]=double2String(100);
					else if (stat.equalsIgnoreCase(Keywords.colfreq))
						valuestowrite[ref]=double2String(1);
					else if (stat.equalsIgnoreCase(Keywords.colpercentfreq))
						valuestowrite[ref]=double2String(100);
					else if (stat.equalsIgnoreCase(Keywords.relfreq))
						valuestowrite[ref]=double2String(1);
					else if (stat.equalsIgnoreCase(Keywords.relpercentfreq))
						valuestowrite[ref]=double2String(100);
					else
						valuestowrite[ref]=double2String(tot);
					ref++;
				}
			}
			dw.write(valuestowrite);
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
		if (colvars.size()==0)
		{
			Hashtable<Integer, LinkedList<ResTabulate>> rowres=tc.getrowres();
			DataSetUtilities dsu=new DataSetUtilities();
			Hashtable<String, String> clvar=new Hashtable<String, String> ();
			Hashtable<String, String> nocl=new Hashtable<String, String> ();
			Hashtable<Integer, String> clvari=new Hashtable<Integer, String> ();
			int totvar=0;
			for (int c=0; c<rowvars.size(); c++)
			{
				Integer keyname=new Integer(c);
				Vector<Integer> stattouse=new Vector<Integer>();
				stattouse.add(keyname);
				String stat=stats.get(stattouse);
				boolean isnum=false;
				for (int i=0; i<numstats.length; i++)
				{
					if (stat.equalsIgnoreCase(numstats[i]))
						isnum=true;
				}
				String outvarlabel="";
				String outvarname="";
				Vector<String> varnames=rowvars.get(keyname);
				for (int i=0; i<varnames.size(); i++)
				{
					outvarname=outvarname+varnames.get(i);
					if (!varnames.get(i).equals("1"))
						outvarlabel=outvarlabel+dict.getvarlabelfromname(varnames.get(i));
					else
						outvarlabel="%2162%";
					if (i<varnames.size()-1)
					{
						outvarname=outvarname+"_";
						outvarlabel=outvarlabel+"-";
					}
				}
				LinkedList<ResTabulate> temprowres=rowres.get(keyname);
				if (temprowres==null)
					return new Result("%2140%<br>\n", false, null);
				else if (temprowres.size()==0)
					return new Result("%2140%<br>\n", false, null);
				Iterator<ResTabulate> igv = temprowres.iterator();
				while(igv.hasNext())
				{
					Vector<String> tempgroup=igv.next().getVec();
					String vl="";
					for (int i=0; i<tempgroup.size(); i++)
					{
						String tl=tempgroup.get(i);
						if (!tempgroup.equals(""))
							vl=vl+tl;
						if (i<tempgroup.size()-1)
							vl=vl+"-";
					}
					vl=vl.trim();
					if ((vl.endsWith("-")) && (vl.length()>1))
						vl=vl.substring(0, vl.length()-1);
					else if ((vl.endsWith("-")) && (vl.length()==1))
						vl="";
					//if ((vl.startsWith("-")) && (vl.length()>1))
					//	vl=vl.substring(1);
					if ((vl.startsWith("-")) && (vl.length()==1))
						vl="";
					if (!noclforvarrow)
						vl="("+vl+")";
					if (vl.equals("()"))
						vl="";
					if ((noclforvarrow) && (vl.equals("")))
						vl="-";
					if (!noclforvarrow)
					{
						if (!shortvarrowinfo)
							clvar.put(String.valueOf(totvar), tc.getcodestat(stat)+" "+outvarlabel+" "+vl);
						else
							clvar.put(String.valueOf(totvar), vl);
					}
					else
					{
						if (!shortvarrowinfo)
							clvari.put(new Integer(totvar), stat.toUpperCase()+" "+outvarlabel.toUpperCase()+" "+vl);
						else
							clvari.put(new Integer(totvar), vl);
					}
					totvar++;
				}
				if (!isnum)
				{
					if (!noclforvarrow)
					{
						if (!shortvarrowinfo)
							clvar.put(String.valueOf(totvar), "%1013% "+outvarlabel);
						else
							clvar.put(String.valueOf(totvar), outvarlabel.toUpperCase());
					}
					else
					{
						if (!shortvarrowinfo)
							clvari.put(new Integer(totvar), "Total: "+outvarlabel);
						else
							clvari.put(new Integer(totvar), outvarlabel.toUpperCase());
					}
					totvar++;
				}
			}
			if (!noclforvarrow)
				dsu.addnewvar("ref", "%2134%", Keywords.TEXTSuffix, clvar, nocl);
			else
				dsu.addnewvar("ref", "%2134%", Keywords.TEXTSuffix, nocl, nocl);
			dsu.addnewvar("stat", "%2135%", Keywords.NUMSuffix, nocl, nocl);
			if (!dw.opendatatable(dsu.getfinalvarinfo()))
				return new Result(dw.getmessage(), false, null);
			String[] valuestowrite=new String[2];
			String[] resultrow=new String[totvar];
			int ref=0;
			for (int c=0; c<rowvars.size(); c++)
			{
				Integer keyname=new Integer(c);
				Vector<Integer> stattouse=new Vector<Integer>();
				stattouse.add(keyname);
				String stat=stats.get(stattouse);
				boolean isnum=false;
				for (int i=0; i<numstats.length; i++)
				{
					if (stat.equalsIgnoreCase(numstats[i]))
						isnum=true;
				}
				LinkedList<ResTabulate> temprowres=rowres.get(keyname);
				Iterator<ResTabulate> igv = temprowres.iterator();
				double tot=0;
				while(igv.hasNext())
				{
					ResTabulate temprestabulate=igv.next();
					double[] tres=temprestabulate.getFre();
					tot=tot+tres[0];
				}
				igv = temprowres.iterator();
				while(igv.hasNext())
				{
					ResTabulate temprestabulate=igv.next();
					double[] tres=temprestabulate.getFre();
					if (stat.equalsIgnoreCase(Keywords.MEAN))
						resultrow[ref]=double2String(tres[1]/tres[0]);
					else if (stat.equalsIgnoreCase(Keywords.STD))
					{
						double tval=(tres[2]/tres[0])-(tres[1]/tres[0])*(tres[1]/tres[0]);
						resultrow[ref]=double2String(Math.sqrt(tval));
					}
					else if (stat.equalsIgnoreCase(Keywords.SUM))
						resultrow[ref]=double2String(tres[1]);
					else if (stat.equalsIgnoreCase(Keywords.N))
						resultrow[ref]=double2String(tres[0]);
					else if (stat.equalsIgnoreCase(Keywords.rowfreq))
						resultrow[ref]=double2String(1);
					else if (stat.equalsIgnoreCase(Keywords.rowpercentfreq))
						resultrow[ref]=double2String(100);
					else if (stat.equalsIgnoreCase(Keywords.colfreq))
						resultrow[ref]=double2String(tres[0]/tot);
					else if (stat.equalsIgnoreCase(Keywords.colpercentfreq))
						resultrow[ref]=double2String(100*tres[0]/tot);
					else if (stat.equalsIgnoreCase(Keywords.relfreq))
						resultrow[ref]=double2String(tres[0]/tot);
					else if (stat.equalsIgnoreCase(Keywords.relpercentfreq))
						resultrow[ref]=double2String(100*tres[0]/tot);
					else
						resultrow[ref]=double2String(tres[0]);
					ref++;
				}
				if (!isnum)
				{
					if (stat.equalsIgnoreCase(Keywords.rowfreq))
						resultrow[ref]=double2String(1);
					else if (stat.equalsIgnoreCase(Keywords.rowpercentfreq))
						resultrow[ref]=double2String(100);
					else if (stat.equalsIgnoreCase(Keywords.colfreq))
						resultrow[ref]=double2String(1);
					else if (stat.equalsIgnoreCase(Keywords.colpercentfreq))
						resultrow[ref]=double2String(100);
					else if (stat.equalsIgnoreCase(Keywords.relfreq))
						resultrow[ref]=double2String(1);
					else if (stat.equalsIgnoreCase(Keywords.relpercentfreq))
						resultrow[ref]=double2String(100);
					else
						resultrow[ref]=double2String(tot);
					resultrow[ref]=double2String(tot);
					ref++;
				}
			}
			for (int i=0; i<resultrow.length; i++)
			{
				if (!noclforvarrow)
					valuestowrite[0]=String.valueOf(i);
				else
					valuestowrite[0]=clvari.get(new Integer(i));
				valuestowrite[1]=resultrow[i];
				dw.write(valuestowrite);
			}
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
		else
		{
			Hashtable<Vector<Integer>, LinkedList<ResTabulate>> totresult=tc.getresult();
			Hashtable<Integer, LinkedList<Vector<String>>> refrowvar=new Hashtable<Integer, LinkedList<Vector<String>>>();
			Hashtable<Integer, LinkedList<Vector<String>>> refcolvar=new Hashtable<Integer, LinkedList<Vector<String>>>();
			DataSetUtilities dsu=new DataSetUtilities();
			Hashtable<String, String> clvar=new Hashtable<String, String> ();
			Hashtable<Integer, String> clvari=new Hashtable<Integer, String> ();
			Hashtable<String, String> nocl=new Hashtable<String, String> ();
			for (int r=0; r<rowvars.size(); r++)
			{
				Integer keynamer=new Integer(r);
				LinkedList<Vector<String>> temprefrowvar=refrowvar.get(keynamer);
				if (temprefrowvar==null)
					temprefrowvar=new LinkedList<Vector<String>>();
				Vector<String> casenames=rowvars.get(keynamer);
				int nvarrow=casenames.size();
				for (int c=0; c<colvars.size(); c++)
				{
					Integer keynamec=new Integer(c);
					LinkedList<Vector<String>> temprefcolvar=refcolvar.get(keynamec);
					if (temprefcolvar==null)
						temprefcolvar=new LinkedList<Vector<String>>();
					Vector<String> varnames=colvars.get(keynamec);
					int nvarcol=varnames.size();
					Vector<Integer> stattouse=new Vector<Integer>();
					stattouse.add(keynamer);
					stattouse.add(keynamec);

					LinkedList<ResTabulate> tempres=totresult.get(stattouse);
					if (tempres==null)
						return new Result("%2140%<br>\n", false, null);
					else if (tempres.size()==0)
						return new Result("%2140%<br>\n", false, null);

					Iterator<ResTabulate> igv = tempres.iterator();
					while(igv.hasNext())
					{
						Vector<String> tempgroup=igv.next().getVec();
						Vector<String> rrow=new Vector<String>();
						Vector<String> rcol=new Vector<String>();
						for (int i=0; i<nvarrow; i++)
						{
							rrow.add(tempgroup.get(i));
						}
						if (!temprefrowvar.contains(rrow))
							temprefrowvar.add(rrow);
						refrowvar.put(keynamer, temprefrowvar);
						for (int i=0; i<nvarcol; i++)
						{
							rcol.add(tempgroup.get(i+nvarrow));
						}
						if (!temprefcolvar.contains(rcol))
							temprefcolvar.add(rcol);
						refcolvar.put(keynamec, temprefcolvar);
					}
				}
			}
			int totcases=0;
			for (int r=0; r<rowvars.size(); r++)
			{
				Integer keynamer=new Integer(r);
				String stat="";
				boolean isnum=true;
				for (int c=0; c<colvars.size(); c++)
				{
					Integer keynamec=new Integer(c);
					Vector<Integer> stattouse=new Vector<Integer>();
					stattouse.add(keynamer);
					stattouse.add(keynamec);
					stat=stats.get(stattouse);
					for (int i=0; i<qualstats.length; i++)
					{
						if (stat.equalsIgnoreCase(qualstats[i]))
							isnum=false;
					}
				}
				String outvarlabel="";
				String outvarname="";
				Vector<String> varnames=rowvars.get(keynamer);
				for (int i=0; i<varnames.size(); i++)
				{
					outvarname=outvarname+varnames.get(i);
					if (!varnames.get(i).equals("1"))
						outvarlabel=outvarlabel+dict.getvarlabelfromname(varnames.get(i));
					else
						outvarlabel="%2162%";
					if (i<varnames.size()-1)
					{
						outvarname=outvarname+"_";
						outvarlabel=outvarlabel+"-";
					}
				}
				LinkedList<Vector<String>> temprefrowvar=refrowvar.get(keynamer);
				Iterator<Vector<String>> igv = temprefrowvar.iterator();
				while(igv.hasNext())
				{
					Vector<String> tempgroup=igv.next();
					String vl="";
					for (int i=0; i<tempgroup.size(); i++)
					{
						String tl=tempgroup.get(i);
						if (!tempgroup.equals(""))
							vl=vl+tl;
						if (i<tempgroup.size()-1)
							vl=vl+"-";
					}
					vl=vl.trim();
					if ((vl.endsWith("-")) && (vl.length()>1))
						vl=vl.substring(0, vl.length()-1);
					else if ((vl.endsWith("-")) && (vl.length()==1))
						vl="";
					//if ((vl.startsWith("-")) && (vl.length()>1))
					//	vl=vl.substring(1);
					if ((vl.startsWith("-")) && (vl.length()==1))
						vl="";
					if (!shortvarrowinfo)
						vl="("+vl+")";
					if (vl.equals("()"))
						vl="";
					if ((shortvarrowinfo) && (vl.equals("")))
						vl="";
					if (colvars.size()==1)
					{
						if (!noclforvarrow)
						{
							if (!shortvarrowinfo)
								clvar.put(String.valueOf(totcases), tc.getcodestat(stat)+" "+outvarlabel+" "+vl);
							else
								clvar.put(String.valueOf(totcases), vl);
						}
						else
						{
							if (!shortvarrowinfo)
								clvari.put(new Integer(totcases), stat.toUpperCase()+" "+outvarlabel.toUpperCase()+" "+vl);
							else
								clvari.put(new Integer(totcases), vl);
						}
					}
					else
					{
						if (!noclforvarrow)
						{
							if (!shortvarrowinfo)
								clvar.put(String.valueOf(totcases), "%2138% "+outvarlabel+" "+vl);
							else
								clvar.put(String.valueOf(totcases), outvarlabel+" "+vl);
						}
						else
						{
							clvari.put(new Integer(totcases), outvarlabel.toUpperCase()+" "+vl.toUpperCase());
						}
					}
					totcases++;
				}
				if (!isnum)
				{
					if (colvars.size()==1)
					{
						if (!noclforvarrow)
						{
							if (!shortvarrowinfo)
								clvar.put(String.valueOf(totcases), "%1013% "+outvarlabel);
							else
								clvar.put(String.valueOf(totcases), outvarlabel);
						}
						else
							clvari.put(new Integer(totcases), outvarlabel.toUpperCase());
					}
					else
					{
						if (!noclforvarrow)
							clvar.put(String.valueOf(totcases), "%2139% "+outvarlabel);
						else
							clvari.put(new Integer(totcases), outvarlabel.toUpperCase());
					}
					totcases++;
				}
			}
			if (!noclforvarrow)
				dsu.addnewvar("ref", "%2134%", Keywords.TEXTSuffix, clvar, nocl);
			else
				dsu.addnewvar("ref", "%2134%", Keywords.TEXTSuffix, nocl, nocl);
			int totcols=1;
			for (int c=0; c<colvars.size(); c++)
			{
				Integer keynamec=new Integer(c);
				boolean isnum=true;
				String stat="";
				for (int r=0; r<rowvars.size(); r++)
				{
					Integer keynamer=new Integer(r);
					Vector<Integer> stattouse=new Vector<Integer>();
					stattouse.add(keynamer);
					stattouse.add(keynamec);
					stat=stats.get(stattouse);
					for (int i=0; i<qualstats.length; i++)
					{
						if (stat.equalsIgnoreCase(qualstats[i]))
							isnum=false;
					}
				}
				String outvarlabel="";
				String outvarname="";
				Vector<String> varnames=colvars.get(keynamec);
				for (int i=0; i<varnames.size(); i++)
				{
					outvarname=outvarname+varnames.get(i);
					if (!varnames.get(i).equals("1"))
						outvarlabel=outvarlabel+dict.getvarlabelfromname(varnames.get(i));
					else
						outvarlabel="%2162%";
					if (i<varnames.size()-1)
					{
						outvarname=outvarname+"_";
						outvarlabel=outvarlabel+"-";
					}
				}
				LinkedList<Vector<String>> temprefcolvar=refcolvar.get(keynamec);
				Iterator<Vector<String>> igv = temprefcolvar.iterator();
				int ref=1;
				while(igv.hasNext())
				{
					Vector<String> tempgroup=igv.next();
					String vl="";
					for (int i=0; i<tempgroup.size(); i++)
					{
						String tl=tempgroup.get(i);
						if (!tempgroup.equals(""))
							vl=vl+tl;
						if (i<tempgroup.size()-1)
							vl=vl+"-";
					}
					vl=vl.trim();
					if ((vl.endsWith("-")) && (vl.length()>1))
						vl=vl.substring(0, vl.length()-1);
					else if ((vl.endsWith("-")) && (vl.length()==1))
						vl="";
					//if ((vl.startsWith("-")) && (vl.length()>1))
					//	vl=vl.substring(1);
					if ((vl.startsWith("-")) && (vl.length()==1))
						vl="";
					if (!shortvarcolinfo)
						vl="("+vl+")";
					if (vl.equals("()"))
						vl="";
					if ((shortvarcolinfo) && (vl.equals("")))
						vl="-";
					if (rowvars.size()==1)
					{
						if (!setprogvarnames)
						{
							if (!shortvarcolinfo)
								dsu.addnewvar(stat+"_"+outvarname+"_"+String.valueOf(ref), tc.getcodestat(stat)+" "+outvarlabel+" "+vl, Keywords.NUMSuffix, nocl, nocl);
							else
								dsu.addnewvar(stat+"_"+outvarname+"_"+String.valueOf(ref), vl, Keywords.NUMSuffix, nocl, nocl);
						}
						else
						{
							if (!shortvarcolinfo)
								dsu.addnewvar("v_"+String.valueOf(totcols), tc.getcodestat(stat)+" "+outvarlabel+" "+vl, Keywords.NUMSuffix, nocl, nocl);
							else
								dsu.addnewvar("v_"+String.valueOf(totcols), vl, Keywords.NUMSuffix, nocl, nocl);
						}
					}
					else
					{
						if (!setprogvarnames)
						{
							if (!shortvarcolinfo)
								dsu.addnewvar(stat+"_"+outvarname+"_"+String.valueOf(ref), "%2138% "+outvarlabel+" "+vl, Keywords.NUMSuffix, nocl, nocl);
							else
								dsu.addnewvar(stat+"_"+outvarname+"_"+String.valueOf(ref), vl, Keywords.NUMSuffix, nocl, nocl);
						}
						else
						{
							if (!shortvarcolinfo)
								dsu.addnewvar("v_"+String.valueOf(totcols), "%2138% "+outvarlabel+" "+vl, Keywords.NUMSuffix, nocl, nocl);
							else
								dsu.addnewvar("v_"+String.valueOf(totcols), vl, Keywords.NUMSuffix, nocl, nocl);
						}
					}
					totcols++;
					ref++;
				}
				if (!isnum)
				{
					if (!setprogvarnames)
					{
						if (rowvars.size()==1)
						{
							if (!shortvarcolinfo)
								dsu.addnewvar("tot_"+outvarname, "%1013% "+outvarlabel, Keywords.NUMSuffix, nocl, nocl);
							else
								dsu.addnewvar("tot_"+outvarname, outvarlabel.toUpperCase(), Keywords.NUMSuffix, nocl, nocl);
						}
						else
						{
							if (!shortvarcolinfo)
								dsu.addnewvar("tot_"+outvarname, "%2139% "+outvarlabel, Keywords.NUMSuffix, nocl, nocl);
							else
								dsu.addnewvar("tot_"+outvarname, outvarlabel.toUpperCase(), Keywords.NUMSuffix, nocl, nocl);
						}
					}
					else
					{
						if (rowvars.size()==1)
						{
							if (!shortvarcolinfo)
								dsu.addnewvar("v_"+String.valueOf(totcols), "%1013% "+outvarlabel, Keywords.NUMSuffix, nocl, nocl);
							else
								dsu.addnewvar("v_"+String.valueOf(totcols), outvarlabel.toUpperCase(), Keywords.NUMSuffix, nocl, nocl);
						}
						else
						{
							if (!shortvarcolinfo)
								dsu.addnewvar("v_"+String.valueOf(totcols), "%2139% "+outvarlabel, Keywords.NUMSuffix, nocl, nocl);
							else
								dsu.addnewvar("v_"+String.valueOf(totcols), outvarlabel.toUpperCase(), Keywords.NUMSuffix, nocl, nocl);
						}
					}
					totcols++;
				}
			}
			if (!dw.opendatatable(dsu.getfinalvarinfo()))
				return new Result(dw.getmessage(), false, null);
			int pointeroutrow=0;
			String[] valuestowrite=new String[totcols];
			for (int r=0; r<rowvars.size(); r++)
			{
				Integer keynamer=new Integer(r);
				Vector<String> testifrowisnum=rowvars.get(keynamer);
				boolean rowisnum=false;
				for (int i=0; i<testifrowisnum.size(); i++)
				{
					for (int k=0; k<varnotclass.length; k++)
					{
						if (testifrowisnum.get(i).equalsIgnoreCase(varnotclass[k]))
							rowisnum=true;
					}
				}
				String stat="";
				LinkedList<Vector<String>> temprefrowvar=refrowvar.get(keynamer);
				Vector<double[][]> finalresulttable=new Vector<double[][]>();
				for (int c=0; c<colvars.size(); c++)
				{
					boolean isnum=false;
					Integer keynamec=new Integer(c);
					Vector<String> testifcolisnum=colvars.get(keynamec);
					boolean colisnum=false;
					for (int i=0; i<testifcolisnum.size(); i++)
					{
						for (int k=0; k<varnotclass.length; k++)
						{
							if (testifcolisnum.get(i).equalsIgnoreCase(varnotclass[k]))
								colisnum=true;
						}
					}
					Vector<Integer> stattouse=new Vector<Integer>();
					stattouse.add(keynamer);
					stattouse.add(keynamec);
					stat=stats.get(stattouse);
					for (int i=0; i<numstats.length; i++)
					{
						if (stat.equalsIgnoreCase(numstats[i]))
							isnum=true;
					}
					LinkedList<Vector<String>> temprefcolvar=refcolvar.get(keynamec);
					double[][] resultmatrix=new double[temprefrowvar.size()][temprefcolvar.size()];
					Iterator<Vector<String>> igvc = null;
					Vector<String> tempgroupfromrow=null;
					int posrow=0;
					int maxposcol=0;
					Iterator<Vector<String>> igvr = temprefrowvar.iterator();
					double[] reftot=new double[3];
					double[][] reftotrow=new double[temprefrowvar.size()][3];
					double[][] reftotcol=new double[temprefcolvar.size()][3];
					for (int i=0; i<temprefrowvar.size(); i++)
					{
						for (int k=0; k<3; k++)
						{
							reftotrow[i][k]=0;
						}
					}
					for (int i=0; i<temprefcolvar.size(); i++)
					{
						for (int k=0; k<3; k++)
						{
							reftotcol[i][k]=0;
						}
					}
					while(igvr.hasNext())
					{
						tempgroupfromrow=igvr.next();
						Vector<String> tempgroupfromcol=null;
						int poscol=0;
						igvc = temprefcolvar.iterator();
						while(igvc.hasNext())
						{
							tempgroupfromcol=igvc.next();
							Vector<String> valuestosearch=new Vector<String>();
							for (int i=0; i<tempgroupfromrow.size(); i++)
							{
								valuestosearch.add(tempgroupfromrow.get(i));
							}
							for (int i=0; i<tempgroupfromcol.size(); i++)
							{
								valuestosearch.add(tempgroupfromcol.get(i));
							}
							double[] tres=tc.checkandget(stattouse, valuestosearch);
							if (tres==null)
							{
								if (isnum)
									resultmatrix[posrow][poscol]=Double.NaN;
								else
									resultmatrix[posrow][poscol]=0;
							}
							else
							{
								boolean errorismissing=false;
								for (int k=0; k<tres.length; k++)
								{
									if (Double.isNaN(tres[k]))
										errorismissing=true;
								}
								if (!errorismissing)
								{
									for (int k=0; k<tres.length; k++)
									{
										reftot[k]=reftot[k]+tres[k];
										reftotrow[posrow][k]=reftotrow[posrow][k]+tres[k];
										reftotcol[poscol][k]=reftotcol[poscol][k]+tres[k];
									}
								}
								if (stat.equalsIgnoreCase(Keywords.MEAN))
									resultmatrix[posrow][poscol]=tres[1]/tres[0];
								else if (stat.equalsIgnoreCase(Keywords.SUM))
									resultmatrix[posrow][poscol]=tres[1];
								else if (stat.equalsIgnoreCase(Keywords.N))
									resultmatrix[posrow][poscol]=tres[0];
								else if (stat.equalsIgnoreCase(Keywords.STD))
								{
									double tval=(tres[2]/tres[0])-(tres[1]/tres[0])*(tres[1]/tres[0]);
									resultmatrix[posrow][poscol]=Math.sqrt(tval);
								}
								else
									resultmatrix[posrow][poscol]=tres[0];
							}
							poscol++;
							if (maxposcol<poscol)
								maxposcol=poscol;
						}
						posrow++;
					}
					if (!isnum)
					{
						double[][] tempresultmatrix=new double[temprefrowvar.size()+1][temprefcolvar.size()+1];
						for (int i=0; i<resultmatrix.length; i++)
						{
							double tot=0;
							for (int j=0; j<resultmatrix[0].length; j++)
							{
								tempresultmatrix[i][j]=resultmatrix[i][j];
								tot=tot+resultmatrix[i][j];
							}
							tempresultmatrix[i][temprefcolvar.size()]=tot;
						}
						double gentot=0;
						for (int i=0; i<resultmatrix[0].length; i++)
						{
							double tot=0;
							for (int j=0; j<resultmatrix.length; j++)
							{
								tempresultmatrix[j][i]=resultmatrix[j][i];
								tot=tot+resultmatrix[j][i];
							}
							tempresultmatrix[temprefrowvar.size()][i]=tot;
							gentot=gentot+tot;
						}
						tempresultmatrix[tempresultmatrix.length-1][tempresultmatrix[0].length-1]=gentot;
						resultmatrix=new double[tempresultmatrix.length][tempresultmatrix[0].length];
						for (int i=0; i<tempresultmatrix.length; i++)
						{
							for (int j=0; j<tempresultmatrix[0].length; j++)
							{
								resultmatrix[i][j]=tempresultmatrix[i][j];
							}
						}
						if (stat.equalsIgnoreCase(Keywords.rowfreq))
						{
							for (int i=0; i<resultmatrix.length; i++)
							{
								for (int j=0; j<resultmatrix[0].length; j++)
								{
									resultmatrix[i][j]=resultmatrix[i][j]/resultmatrix[i][resultmatrix[0].length-1];
								}
							}
						}
						else if (stat.equalsIgnoreCase(Keywords.rowpercentfreq))
						{
							for (int i=0; i<resultmatrix.length; i++)
							{
								for (int j=0; j<resultmatrix[0].length; j++)
								{
									resultmatrix[i][j]=100*resultmatrix[i][j]/resultmatrix[i][resultmatrix[0].length-1];
								}
							}
						}
						else if (stat.equalsIgnoreCase(Keywords.colfreq))
						{
							for (int i=0; i<resultmatrix.length; i++)
							{
								for (int j=0; j<resultmatrix[0].length; j++)
								{
									resultmatrix[i][j]=resultmatrix[i][j]/resultmatrix[resultmatrix.length-1][j];
								}
							}
						}
						else if (stat.equalsIgnoreCase(Keywords.colpercentfreq))
						{
							for (int i=0; i<resultmatrix.length; i++)
							{
								for (int j=0; j<resultmatrix[0].length; j++)
								{
									resultmatrix[i][j]=100*resultmatrix[i][j]/resultmatrix[resultmatrix.length-1][j];
								}
							}
						}
						else if (stat.equalsIgnoreCase(Keywords.relfreq))
						{
							for (int i=0; i<resultmatrix.length; i++)
							{
								for (int j=0; j<resultmatrix[0].length; j++)
								{
									resultmatrix[i][j]=resultmatrix[i][j]/gentot;
								}
							}
						}
						else if (stat.equalsIgnoreCase(Keywords.relpercentfreq))
						{
							for (int i=0; i<resultmatrix.length; i++)
							{
								for (int j=0; j<resultmatrix[0].length; j++)
								{
									resultmatrix[i][j]=100*resultmatrix[i][j]/gentot;
								}
							}
						}
					}
					else
					{
						boolean addtotalstat=true;
						boolean addgentotalstat=false;
						if ((rowisnum) && (colisnum))
							addtotalstat=false;
						if ((posrow==1) && (!rowisnum))
							posrow=2;
						if (addtotalstat)
						{
							if ((posrow==1) && (maxposcol>1))
							{
								addtotalstat=false;
								for (int ct=0; ct<colvars.size(); ct++)
								{
									Integer keynamect=new Integer(ct);
									Vector<Integer> stattouset=new Vector<Integer>();
									stattouset.add(keynamer);
									stattouset.add(keynamect);
									String statt=stats.get(stattouset);
									for (int i=0; i<qualstats.length; i++)
									{
										if (statt.equalsIgnoreCase(qualstats[i]))
											addtotalstat=true;
									}
								}
								for (int rt=0; rt<rowvars.size(); rt++)
								{
									Integer keynamert=new Integer(rt);
									Vector<Integer> stattouset=new Vector<Integer>();
									stattouset.add(keynamert);
									stattouset.add(keynamec);
									String statt=stats.get(stattouset);
									for (int i=0; i<qualstats.length; i++)
									{
										if (statt.equalsIgnoreCase(qualstats[i]))
											addtotalstat=true;
									}
								}
								if (addtotalstat)
								{
									double[][] tempresultmatrix=new double[temprefrowvar.size()][temprefcolvar.size()+1];
									for (int i=0; i<resultmatrix.length; i++)
									{
										for (int j=0; j<resultmatrix[0].length; j++)
										{
											tempresultmatrix[i][j]=resultmatrix[i][j];
										}
									}
									resultmatrix=new double[tempresultmatrix.length][tempresultmatrix[0].length];
									for (int i=0; i<tempresultmatrix.length; i++)
									{
										for (int j=0; j<tempresultmatrix[0].length; j++)
										{
											resultmatrix[i][j]=tempresultmatrix[i][j];
										}
									}
									if (stat.equalsIgnoreCase(Keywords.SUM))
									{
										resultmatrix[resultmatrix.length-1][resultmatrix[0].length-1]=reftot[1];
									}
									else if (stat.equalsIgnoreCase(Keywords.MEAN))
									{
										resultmatrix[resultmatrix.length-1][resultmatrix[0].length-1]=reftot[1]/reftot[0];
									}
									else if (stat.equalsIgnoreCase(Keywords.N))
									{
										resultmatrix[resultmatrix.length-1][resultmatrix[0].length-1]=reftot[0];
									}
									else if (stat.equalsIgnoreCase(Keywords.STD))
									{
										double tval=(reftot[2]/reftot[0])-(reftot[1]/reftot[0])*(reftot[1]/reftot[0]);
										resultmatrix[resultmatrix.length-1][resultmatrix[0].length-1]=Math.sqrt(tval);
									}
								}
							}
							else if ((posrow>1) && (maxposcol==1))
							{
								addtotalstat=false;
								for (int rt=0; rt<rowvars.size(); rt++)
								{
									Integer keynamert=new Integer(rt);
									Vector<Integer> stattouset=new Vector<Integer>();
									stattouset.add(keynamert);
									stattouset.add(keynamec);
									String statt=stats.get(stattouset);
									for (int i=0; i<qualstats.length; i++)
									{
										if (statt.equalsIgnoreCase(qualstats[i]))
											addtotalstat=true;
									}
								}
								for (int ct=0; ct<colvars.size(); ct++)
								{
									Integer keynamect=new Integer(ct);
									Vector<Integer> stattouset=new Vector<Integer>();
									stattouset.add(keynamer);
									stattouset.add(keynamect);
									String statt=stats.get(stattouset);
									for (int i=0; i<qualstats.length; i++)
									{
										if (statt.equalsIgnoreCase(qualstats[i]))
											addtotalstat=true;
									}
								}
								if (addtotalstat)
								{
									double[][] tempresultmatrix=new double[temprefrowvar.size()+1][temprefcolvar.size()];
									for (int i=0; i<resultmatrix.length; i++)
									{
										for (int j=0; j<resultmatrix[0].length; j++)
										{
											tempresultmatrix[i][j]=resultmatrix[i][j];
										}
									}
									resultmatrix=new double[tempresultmatrix.length][tempresultmatrix[0].length];
									for (int i=0; i<tempresultmatrix.length; i++)
									{
										for (int j=0; j<tempresultmatrix[0].length; j++)
										{
											resultmatrix[i][j]=tempresultmatrix[i][j];
										}
									}
									if (stat.equalsIgnoreCase(Keywords.SUM))
									{
										resultmatrix[resultmatrix.length-1][resultmatrix[0].length-1]=reftot[1];
									}
									else if (stat.equalsIgnoreCase(Keywords.MEAN))
									{
										resultmatrix[resultmatrix.length-1][resultmatrix[0].length-1]=reftot[1]/reftot[0];
									}
									else if (stat.equalsIgnoreCase(Keywords.N))
									{
										resultmatrix[resultmatrix.length-1][resultmatrix[0].length-1]=reftot[0];
									}
									else if (stat.equalsIgnoreCase(Keywords.STD))
									{
										double tval=(reftot[2]/reftot[0])-(reftot[1]/reftot[0])*(reftot[1]/reftot[0]);
										resultmatrix[resultmatrix.length-1][resultmatrix[0].length-1]=Math.sqrt(tval);
									}
								}
							}
							else
							{
								addtotalstat=false;
								for (int rt=0; rt<rowvars.size(); rt++)
								{
									Integer keynamert=new Integer(rt);
									Vector<Integer> stattouset=new Vector<Integer>();
									stattouset.add(keynamert);
									stattouset.add(keynamec);
									String statt=stats.get(stattouset);
									for (int i=0; i<qualstats.length; i++)
									{
										if (statt.equalsIgnoreCase(qualstats[i]))
											addtotalstat=true;
									}
								}
								if (addtotalstat)
								{
									double[][] tempresultmatrix=new double[temprefrowvar.size()][temprefcolvar.size()+1];
									for (int i=0; i<resultmatrix.length; i++)
									{
										for (int j=0; j<resultmatrix[0].length; j++)
										{
											tempresultmatrix[i][j]=resultmatrix[i][j];
										}
									}
									resultmatrix=new double[tempresultmatrix.length][tempresultmatrix[0].length];
									for (int i=0; i<tempresultmatrix.length; i++)
									{
										for (int j=0; j<tempresultmatrix[0].length; j++)
										{
											resultmatrix[i][j]=tempresultmatrix[i][j];
										}
									}
									for (int i=0; i<temprefrowvar.size(); i++)
									{
										if (stat.equalsIgnoreCase(Keywords.SUM))
											resultmatrix[i][temprefcolvar.size()]=reftotrow[i][1];
										if (stat.equalsIgnoreCase(Keywords.MEAN))
											resultmatrix[i][temprefcolvar.size()]=reftotrow[i][1]/reftotrow[i][0];
										if (stat.equalsIgnoreCase(Keywords.N))
											resultmatrix[i][temprefcolvar.size()]=reftotrow[i][0];
										if (stat.equalsIgnoreCase(Keywords.STD))
										{
											double tval=(reftotrow[i][2]/reftotrow[i][0])-(reftotrow[i][1]/reftotrow[i][0])*(reftotrow[i][1]/reftotrow[i][0]);
											resultmatrix[i][temprefcolvar.size()]=tval;
										}
									}
									addgentotalstat=true;
								}
								addtotalstat=false;
								for (int ct=0; ct<colvars.size(); ct++)
								{
									Integer keynamect=new Integer(ct);
									Vector<Integer> stattouset=new Vector<Integer>();
									stattouset.add(keynamer);
									stattouset.add(keynamect);
									String statt=stats.get(stattouset);
									for (int i=0; i<qualstats.length; i++)
									{
										if (statt.equalsIgnoreCase(qualstats[i]))
											addtotalstat=true;
									}
								}
								if (addtotalstat)
								{
									double[][] tempresultmatrix=new double[temprefrowvar.size()+1][temprefcolvar.size()];
									for (int i=0; i<resultmatrix.length; i++)
									{
										for (int j=0; j<resultmatrix[0].length; j++)
										{
											tempresultmatrix[i][j]=resultmatrix[i][j];
										}
									}
									resultmatrix=new double[tempresultmatrix.length][tempresultmatrix[0].length];
									for (int i=0; i<tempresultmatrix.length; i++)
									{
										for (int j=0; j<tempresultmatrix[0].length; j++)
										{
											resultmatrix[i][j]=tempresultmatrix[i][j];
										}
									}
									for (int i=0; i<temprefcolvar.size(); i++)
									{
										if (stat.equalsIgnoreCase(Keywords.SUM))
											resultmatrix[temprefrowvar.size()][i]=reftotcol[i][1];
										if (stat.equalsIgnoreCase(Keywords.MEAN))
											resultmatrix[temprefrowvar.size()][i]=reftotcol[i][1]/reftotcol[i][0];
										if (stat.equalsIgnoreCase(Keywords.N))
											resultmatrix[temprefrowvar.size()][i]=reftotcol[i][0];
										if (stat.equalsIgnoreCase(Keywords.STD))
										{
											double tval=(reftotcol[i][2]/reftotcol[i][0])-(reftotcol[i][1]/reftotcol[i][0])*(reftotcol[i][1]/reftotcol[i][0]);
											resultmatrix[temprefrowvar.size()][i]=tval;
										}
									}
								}
								else
									addgentotalstat=false;
							}
						}
					}
					finalresulttable.add(resultmatrix);
				}
				int totalnumrows=0;
				int totalnumcols=0;
				double[][] tr=finalresulttable.get(0);
				totalnumrows=tr.length;
				for (int i=0; i<finalresulttable.size(); i++)
				{
					double[][] ttr=finalresulttable.get(i);
					totalnumcols=totalnumcols+ttr[0].length;
					if (ttr.length>totalnumrows)
						totalnumrows=ttr.length;
				}
				String[][] finaltable=new String[totalnumrows][totalnumcols];
				int lastpointer=0;
				for (int i=0; i<finalresulttable.size(); i++)
				{
					double[][] ttr=finalresulttable.get(i);
					for (int j=0; j<ttr.length; j++)
					{
						for (int h=0; h<ttr[0].length; h++)
						{
							finaltable[j][h+lastpointer]=double2String(ttr[j][h]);
						}
					}
					lastpointer=lastpointer+ttr[0].length;
				}
				for (int i=0; i<finaltable.length; i++)
				{
					for (int j=0; j<totcols; j++)
					{
						valuestowrite[j]="";
					}
					if (!noclforvarrow)
						valuestowrite[0]=String.valueOf(pointeroutrow);
					else
						valuestowrite[0]=clvari.get(new Integer(pointeroutrow));
					for (int j=0; j<finaltable[0].length; j++)
					{
						valuestowrite[j+1]=finaltable[i][j];
					}
					dw.write(valuestowrite);
					pointeroutrow++;
				}

			}
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
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 721, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 440, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.table, "textvars", true, 2124, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 2125, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.statistics, "multipletext", false, 2126, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 2117, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 2130, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 2131, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 2132, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 2133, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varclass, "vars=all", false, 2127, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.weight, "vars=all", false, 656, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.setprogvarnames, "checkbox", false, 2136, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.mdhandling, "listsingle=1555_"+Keywords.pairwisenomd+",1556_"+Keywords.pairwisewithmd+",1557_"+Keywords.casewise, false, 1554, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.mdsubst, "text", false, 1558, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 1559, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.orderbyval, "checkbox", false, 2251, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.usewritefmt, "checkbox", false, 2275, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.noclforvarrow, "checkbox", false, 2239, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.shortvarrowinfo, "checkbox", false, 2324, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.shortvarcolinfo, "checkbox", false, 2325, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="759";
		retprocinfo[1]="2112";
		return retprocinfo;
	}
}
