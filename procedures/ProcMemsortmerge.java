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
import java.util.LinkedList;
import java.util.Vector;
import java.util.TreeMap;
import java.util.Iterator;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.utilities.VectorStringComparatorWithMd;
import ADaMSoft.utilities.VectorStringComparatorNoC;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;

/**
* This is the procedure that sorts and merge two datasets in memory
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcMemsortmerge implements RunStep
{
	boolean ascending;
	/**
	* Starts the execution of Proc Sortmerge and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean noconversion=false;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.var, Keywords.dict+"a", Keywords.dict+"b"};
		String [] optionalparameters=new String[] {Keywords.descending, Keywords.force, Keywords.condition, Keywords.noconversion, Keywords.replace};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		boolean force=(parameters.get(Keywords.force)!=null);
		ascending    =(parameters.get(Keywords.descending)==null);
		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		DictionaryReader dicta = (DictionaryReader)parameters.get(Keywords.dict+"a");
		String vartemp=(String)parameters.get(Keywords.var.toLowerCase());
		VariableUtilities varu=new VariableUtilities(dicta, null, vartemp, null, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);
		String[] mergevar=varu.getanalysisvar();

		noconversion=(parameters.get(Keywords.noconversion)!=null);

		String condition=(String)parameters.get(Keywords.condition.toLowerCase());
		int valec=0;
		if (condition!=null)
		{
			String[] conditions=new String[] {Keywords.ifdicta, Keywords.ifdictb, Keywords.both};
			valec=steputilities.CheckOption(conditions, condition);
			if (valec==0)
				return new Result("%1775% "+Keywords.condition.toUpperCase()+"<br>\n"+steputilities.getMessage(), false, null);
		}

		DictionaryReader dictb = (DictionaryReader)parameters.get(Keywords.dict+"b");
		VariableUtilities varub=new VariableUtilities(dictb, null, vartemp, null, null, null);
		if (varub.geterror())
			return new Result(varub.getmessage(), false, null);

		String replace=(String)parameters.get(Keywords.replace);

		String keyword="MemSortMerge "+dicta.getkeyword()+" "+dictb.getkeyword();
		String description="MemSortMerge "+dicta.getdescription()+" "+dictb.getdescription();
		String author=(String)parameters.get(Keywords.client_host.toLowerCase());

		/*Check for variables*/
		Vector<String> remaininga=new Vector<String>();
		int totalvara=dicta.gettotalvar();
		for (int i=0; i<totalvara; i++)
		{
			boolean remain=true;
			String tempname=dicta.getvarname(i);
			for (int j=0; j<mergevar.length; j++)
			{
				if (tempname.equalsIgnoreCase(mergevar[j]))
					remain=false;
			}
			if (remain)
				remaininga.add(tempname);
		}
		Vector<String> remainingb=new Vector<String>();
		int totalvarb=dictb.gettotalvar();
		for (int i=0; i<totalvarb; i++)
		{
			boolean remain=true;
			String tempname=dictb. getvarname(i);
			for (int j=0; j<mergevar.length; j++)
			{
				if (tempname.equalsIgnoreCase(mergevar[j]))
					remain=false;
			}
			if (remain)
				remainingb.add(tempname);
		}
		if (!force)
		{
			boolean check=false;
			for (int i=0; i<remaininga.size(); i++)
			{
				String vara=remaininga.get(i);
				for (int j=0; j<remainingb.size(); j++)
				{
					String varb=remainingb.get(j);
					if (vara.equalsIgnoreCase(varb))
						check=true;
				}
			}
			if (check)
				return new Result("%535%<br>\n", false, null);
		}

		Vector<Hashtable<String, String>> fixedvariableinfoa=dicta.getfixedvariableinfo();
		Vector<Hashtable<String, String>> fixedvariableinfob=dictb.getfixedvariableinfo();

		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);

		Vector<String> remvar=new Vector<String>();

		String forcevarname="";
		for (int j=0; j<mergevar.length; j++)
		{
			for (int i=0; i<fixedvariableinfoa.size(); i++)
			{
				Hashtable<String, String> tempvar=fixedvariableinfoa.get(i);
				String varname=(tempvar.get(Keywords.VariableName.toLowerCase())).trim();
				if (varname.equalsIgnoreCase(mergevar[j].trim()))
				{
					String nvarname=varname;
					if (force)
					{
						nvarname="common_"+varname;
						forcevarname=forcevarname+nvarname+" ";
					}
					dsu.addnewvarfromolddict(dicta, varname, null, null, nvarname);
				}
			}
		}

		for (int i=0; i<fixedvariableinfoa.size(); i++)
		{
			Hashtable<String, String> tempvar=fixedvariableinfoa.get(i);
			String varname=(tempvar.get(Keywords.VariableName.toLowerCase())).trim();
			boolean issel=false;
			for (int j=0; j<mergevar.length; j++)
			{
				if (varname.equalsIgnoreCase(mergevar[j].trim()))
					issel=true;
			}
			if (!issel)
				remvar.add(varname);
		}

		for (int i=0; i<remvar.size(); i++)
		{
			String varname=remvar.get(i);
			String nvarname=varname;
			if (force)
				nvarname="dicta_"+nvarname;
			dsu.addnewvarfromolddict(dicta, varname, null, null, nvarname);
		}
		remvar.clear();

		for (int i=0; i<fixedvariableinfob.size(); i++)
		{
			Hashtable<String, String> tempvar=fixedvariableinfob.get(i);
			String varname=(tempvar.get(Keywords.VariableName.toLowerCase())).trim();
			boolean issel=false;
			for (int j=0; j<mergevar.length; j++)
			{
				if (varname.equalsIgnoreCase(mergevar[j].trim()))
					issel=true;
			}
			if (!issel)
			{
				String nvarname=varname;
				if (force)
					nvarname="dictb_"+nvarname;
				dsu.addnewvarfromolddict(dictb, varname, null, null, nvarname);
			}
		}

		int sortav=mergevar.length;
		int remva=remaininga.size();
		int remvb=remainingb.size();

		int[] replacerulea=new int[totalvara];
		int[] replaceruleb=new int[totalvarb];
		if (replace==null)
		{
			for (int j=0; j<totalvara; j++)
			{
				replacerulea[j]=0;
			}
			for (int j=0; j<totalvarb; j++)
			{
				replaceruleb[j]=0;
			}
		}
		else if (replace.equalsIgnoreCase(Keywords.replaceall))
		{
			for (int j=0; j<totalvara; j++)
			{
				replacerulea[j]=1;
			}
			for (int j=0; j<totalvarb; j++)
			{
				replaceruleb[j]=1;
			}
			dsu.setempycodelabels();
			dsu.setempymissingdata();
		}
		else if (replace.equalsIgnoreCase(Keywords.replaceformat))
		{
			for (int j=0; j<totalvara; j++)
			{
				replacerulea[j]=2;
			}
			for (int j=0; j<totalvarb; j++)
			{
				replaceruleb[j]=2;
			}
			dsu.setempycodelabels();
		}
		else if (replace.equalsIgnoreCase(Keywords.replacemissing))
		{
			for (int j=0; j<totalvara; j++)
			{
				replacerulea[j]=3;
			}
			for (int j=0; j<totalvarb; j++)
			{
				replaceruleb[j]=3;
			}
			dsu.setempymissingdata();
		}
		else
		{
			for (int j=0; j<totalvara; j++)
			{
				replacerulea[j]=0;
			}
			for (int j=0; j<totalvarb; j++)
			{
				replaceruleb[j]=0;
			}
		}
		String[] vara=new String[totalvara];
		String[] varb=new String[totalvarb];
		for (int i=0; i<mergevar.length; i++)
		{
			vara[i]=mergevar[i];
		}
		for (int i=0; i<remaininga.size(); i++)
		{
			String name=remaininga.get(i);
			vara[i+mergevar.length]=name;
		}
		for (int i=0; i<mergevar.length; i++)
		{
			varb[i]=mergevar[i];
		}
		for (int i=0; i<remainingb.size(); i++)
		{
			String name=remainingb.get(i);
			varb[i+mergevar.length]=name;
		}

		TreeMap<Vector<String>, RemainningInfo> values_def=null;

		if (!noconversion)
		{
			values_def=new TreeMap<Vector<String>, RemainningInfo>(new VectorStringComparatorWithMd());
		}
		else
		{
			values_def=new TreeMap<Vector<String>, RemainningInfo>(new VectorStringComparatorNoC());
		}

		DataReader dataa = new DataReader(dicta);
		if (!dataa.open(vara, replacerulea, false))
			return new Result(dataa.getmessage(), false, null);
		String[] values=null;
		while (!dataa.isLast())
		{
			values = dataa.getRecord();
			if (values!=null)
			{
				Vector<String> temps=new Vector<String>();
				for (int i=0; i<sortav; i++)
				{
					temps.add(values[i]);
				}
				Vector<String> tempv=new Vector<String>();
				for (int i=0; i<remva; i++)
				{
					tempv.add(values[i+sortav]);
				}
				if (values_def.containsKey(temps))
				{
					RemainningInfo tempvr=values_def.get(temps);
					tempvr.addA(tempv);
					values_def.put(temps, tempvr);
				}
				else
				{
					RemainningInfo tempvr=new RemainningInfo();
					tempvr.addA(tempv);
					values_def.put(temps, tempvr);
				}
			}
		}
		dataa.close();
		DataReader datab = new DataReader(dictb);
		if (!datab.open(varb, replaceruleb, false))
			return new Result(datab.getmessage(), false, null);
		while (!datab.isLast())
		{
			values = datab.getRecord();
			if (values!=null)
			{
				Vector<String> temps=new Vector<String>();
				for (int i=0; i<sortav; i++)
				{
					temps.add(values[i]);
				}
				Vector<String> tempv=new Vector<String>();
				for (int i=0; i<remvb; i++)
				{
					tempv.add(values[i+sortav]);
				}
				if (values_def.containsKey(temps))
				{
					RemainningInfo tempvr=values_def.get(temps);
					tempvr.addB(tempv);
					values_def.put(temps, tempvr);
				}
				else
				{
					RemainningInfo tempvr=new RemainningInfo();
					tempvr.addB(tempv);
					values_def.put(temps, tempvr);
				}
			}
		}
		datab.close();

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
		{
			values_def=null;
			return new Result(dw.getmessage(), false, null);
		}

		String[] out_values=new String[sortav+remvb+remva];
		Vector<String> tempss=null;
		Vector<String> tempsp=null;
		boolean confirmwrite=false;
		int notvalid=0;
		Vector<Integer> na=new Vector<Integer>();
		Vector<Integer> nb=new Vector<Integer>();
		int repa=0;
		int repb=0;
		Vector<Vector<String>> va=new Vector<Vector<String>>();
		Vector<Vector<String>> vb=new Vector<Vector<String>>();
		for (Iterator<Vector<String>> ite = values_def.keySet().iterator(); ite.hasNext();)
		{
			confirmwrite=true;
			Vector<String> sortv=ite.next();
			RemainningInfo r=values_def.get(sortv);
			if (r==null) notvalid++;
			else
			{
				va=r.get_a();
				vb=r.get_b();
				na=r.get_repetition_a();
				nb=r.get_repetition_b();
				for (int i=0; i<sortv.size(); i++)
				{
					out_values[i]=sortv.get(i);
				}
				for (int i=sortav; i<out_values.length; i++)
				{
					out_values[i]="";
				}
				if (va.size()==0 && vb.size()!=0)
				{
					if (valec==1) confirmwrite=false;
					if (valec==3) confirmwrite=false;
					if (confirmwrite)
					{
						for (int i=0; i<vb.size(); i++)
						{
							repb=(nb.get(i)).intValue();
							tempss=vb.get(i);
							for (int rep=0; rep<repb; rep++)
							{
								for (int j=0; j<tempss.size(); j++)
								{
									out_values[sortav+remva+j]=tempss.get(j);
								}
								dw.write(out_values);
							}
						}
					}
				}
				else if (vb.size()==0 && va.size()!=0)
				{
					if (valec==2) confirmwrite=false;
					if (valec==3) confirmwrite=false;
					if (confirmwrite)
					{
						for (int i=0; i<va.size(); i++)
						{
							repa=(na.get(i)).intValue();
							tempss=va.get(i);
							for (int rep=0; rep<repa; rep++)
							{
								for (int j=0; j<tempss.size(); j++)
								{
									out_values[sortav+j]=tempss.get(j);
								}
								dw.write(out_values);
							}
						}
					}
				}
				else
				{
					for (int i=0; i<va.size(); i++)
					{
						repa=(na.get(i)).intValue();
						tempss=va.get(i);
						for (int j=0; j<tempss.size(); j++)
						{
							out_values[sortav+j]=tempss.get(j);
						}
						for (int j=0; j<vb.size(); j++)
						{
							repb=(nb.get(j)).intValue();
							tempsp=vb.get(j);
							if (tempss.size()==0 && tempsp.size()==0)
							{
								if (repa>repb)
								{
									for (int k=0; k<repa; k++)
									{
										dw.write(out_values);
									}
								}
								else if (repb>repa)
								{
									for (int k=0; k<repb; k++)
									{
										dw.write(out_values);
									}
								}
								else
								{
									for (int k=0; k<repa; k++)
									{
										dw.write(out_values);
									}
								}
							}
							else if (tempss.size()!=0 && tempsp.size()==0)
							{
								for (int k=0; k<repa; k++)
								{
									dw.write(out_values);
								}
							}
							else if (tempss.size()==0 && tempsp.size()!=0)
							{
								for (int k=0; k<tempsp.size(); k++)
								{
									out_values[sortav+remva+k]=tempsp.get(k);
								}
								for (int rep=0; rep<repb; rep++)
								{
									dw.write(out_values);
								}
							}
							else
							{
								for (int k=0; k<tempsp.size(); k++)
								{
									out_values[sortav+remva+k]=tempsp.get(k);
								}
								for (int rep=0; rep<repb*repa; rep++)
								{
									dw.write(out_values);
								}
							}
						}
					}
				}
			}
		}
		values_def=null;
		Hashtable<String, String> othertableinfo=new Hashtable<String, String>();
		if (force)
			vartemp=forcevarname.trim();
		othertableinfo.put(Keywords.SORTED.toLowerCase(), vartemp);
		Vector<StepResult> result = new Vector<StepResult>();
		if (notvalid>0)
			result.add(new LocalMessageGetter("%3409% (%3410%: "+String.valueOf(notvalid)+")\n"));
		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
		Hashtable<String, String> datatableinfo=dw.getTableInfo();
		result.add(new LocalDictionaryWriter(dw.getdictpath(), keyword, description, author, dw.gettabletype(),
		datatableinfo, dsu.getfinalvarinfo(), tablevariableinfo, dsu.getfinalcl(), dsu.getfinalmd(), othertableinfo));
		return new Result("", true, result);
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"a=", "dict", true, 536, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"b=", "dict", true, 537, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 440, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[2];
		dep[0]="dicta";
		dep[1]="dictb";
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=common", true, 538, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.force, "checkbox", false, 1794, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.noconversion,"checkbox",false,2245,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.condition, "listsingle=2246_NULL,2247_"+Keywords.ifdicta+",2248_"+Keywords.ifdictb+",2249_"+Keywords.both,false, 2250, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="4162";
		retprocinfo[1]="3405";
		return retprocinfo;
	}
}

class RemainningInfo
{
	Vector<Vector<String>> info_a;
	Vector<Vector<String>> info_b;
	Vector<Integer> repetition_a;
	Vector<Integer> repetition_b;
	int ref_pos=0;
	int ref_val=0;
	public RemainningInfo()
	{
		info_a=new Vector<Vector<String>>();
		info_b=new Vector<Vector<String>>();
		repetition_a=new Vector<Integer>();
		repetition_b=new Vector<Integer>();
	}
	public void addA(Vector<String> av)
	{
		if (!info_a.contains(av))
		{
			info_a.add(av);
			repetition_a.add(new Integer(1));
		}
		else
		{
			ref_pos=info_a.indexOf(av);
			ref_val=(repetition_a.get(ref_pos)).intValue();
			repetition_a.set(ref_pos, new Integer(1+ref_val));
		}
	}
	public void addB(Vector<String> bv)
	{
		if (!info_b.contains(bv))
		{
			info_b.add(bv);
			repetition_b.add(new Integer(1));
		}
		else
		{
			ref_pos=info_b.indexOf(bv);
			ref_val=(repetition_b.get(ref_pos)).intValue();
			repetition_b.set(ref_pos, new Integer(1+ref_val));
		}
	}
	public Vector<Vector<String>> get_a()
	{
		return info_a;
	}
	public Vector<Vector<String>> get_b()
	{
		return info_b;
	}
	public Vector<Integer> get_repetition_a()
	{
		return repetition_a;
	}
	public Vector<Integer> get_repetition_b()
	{
		return repetition_b;
	}
}