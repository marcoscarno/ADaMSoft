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

import java.util.*;

import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.dataaccess.CompareObjects;
import ADaMSoft.dataaccess.DataSorter;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;


/**
* This is the procedure that sorts and merge two datasets
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcSortmerge implements RunStep
{
	boolean ascending;
	/**
	* Starts the execution of Proc Sortmerge and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean noconversion=false;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.var, Keywords.dict+"a", Keywords.dict+"b"};
		String [] optionalparameters=new String[] {Keywords.descending, Keywords.force, Keywords.condition, Keywords.noconversion, Keywords.MaxDataBuffered, Keywords.replace};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		Keywords.percentage_total=3;

		boolean force=(parameters.get(Keywords.force)!=null);
		ascending    =(parameters.get(Keywords.descending)==null);
		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
		{
			return new Result(dw.getmessage(), false, null);
		}

		DictionaryReader dicta = (DictionaryReader)parameters.get(Keywords.dict+"a");
		String vartemp=(String)parameters.get(Keywords.var.toLowerCase());
		VariableUtilities varu=new VariableUtilities(dicta, null, vartemp, null, null, null);
		if (varu.geterror())
		{
			return new Result(varu.getmessage(), false, null);
		}
		String[] mergevar=varu.getanalysisvar();

		noconversion=(parameters.get(Keywords.noconversion)!=null);

		String condition=(String)parameters.get(Keywords.condition.toLowerCase());
		int valec=0;
		if (condition!=null)
		{
			String[] conditions=new String[] {Keywords.ifdicta, Keywords.ifdictb, Keywords.both};
			valec=steputilities.CheckOption(conditions, condition);
			if (valec==0)
			{
				return new Result("%1775% "+Keywords.condition.toUpperCase()+"<br>\n"+steputilities.getMessage(), false, null);
			}
		}

		String MaxDataBuffered=(String)parameters.get(Keywords.MaxDataBuffered.toLowerCase());
		int tmdb=-1;
		if (MaxDataBuffered!=null)
		{
			try
			{
				tmdb=Integer.parseInt(MaxDataBuffered);
			}
			catch (Exception et)
			{
				tmdb=-1;
			}
			if (tmdb<=0) return new Result("%3742%<br>\n", false, null);
		}

		DictionaryReader dictb = (DictionaryReader)parameters.get(Keywords.dict+"b");
		VariableUtilities varub=new VariableUtilities(dictb, null, vartemp, null, null, null);
		if (varub.geterror())
		{
			return new Result(varub.getmessage(), false, null);
		}

		String replace=(String)parameters.get(Keywords.replace);

		String keyword="SortMerge "+dicta.getkeyword()+" "+dictb.getkeyword();
		String description="SortMerge "+dicta.getdescription()+" "+dictb.getdescription();
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
			{
				return new Result("%535%\n", false, null);
			}
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

		int totalvar=remaininga.size()+remainingb.size()+mergevar.length;
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

		String tempdir=(String)parameters.get(Keywords.WorkDir);
		DataSorter datasortera=new DataSorter(dicta, vara, ascending, tempdir, replacerulea, null);
		datasortera.setconversion(noconversion);
		if (datasortera.geterror())
		{
			return new Result(datasortera.getmessage(), false, null);
		}
		Keywords.percentage_done=1;

		DataSorter datasorterb=new DataSorter(dictb, varb, ascending, tempdir, replaceruleb, null);
		datasorterb.setconversion(noconversion);
		if (datasorterb.geterror())
		{
			return new Result(datasorterb.getmessage(), false, null);
		}
		if (tmdb>0)
		{
			datasortera.setmaxdatasorted(tmdb);
			datasorterb.setmaxdatasorted(tmdb);
		}
		SorterThread sth=new SorterThread(datasortera);
		sth.start();

		Keywords.percentage_done=2;

		datasorterb.sortdata();
		if (datasorterb.geterror())
		{
			datasortera.deletefile();
			datasorterb.deletefile();
			Keywords.procedure_error=true;
			return new Result(datasorterb.getmessage(), false, null);
		}
		try
		{
			sth.join();
		}
		catch (Exception e){}

		if (datasortera.geterror())
		{
			datasortera.deletefile();
			datasorterb.deletefile();
			return new Result(datasortera.getmessage(), false, null);
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
		{
			datasortera.deletefile();
			datasorterb.deletefile();
			return new Result(dw.getmessage(), false, null);
		}

		datasortera.openFinalFile();
		if (datasortera.geterror())
		{
			datasortera.deletefile();
			datasorterb.deletefile();
			return new Result(datasortera.getmessage(), false, null);
		}
		datasorterb.openFinalFile();
		if (datasorterb.geterror())
		{
			datasortera.deletefile();
			datasorterb.deletefile();
			return new Result(datasorterb.getmessage(), false, null);
		}

		int totalrecorda=datasortera.getTotalRecords();
		int totalrecordb=datasorterb.getTotalRecords();
		Keywords.percentage_total=3*(totalrecorda+totalrecordb);
		Keywords.percentage_done=2*(totalrecorda+totalrecordb);

		int totalrecord=totalrecorda;
		if (totalrecordb>totalrecord)
			totalrecord=totalrecordb;
		boolean readfromfirst=true;
		boolean readfromsecond=true;
		int firstpointer=0;
		int lastpointer=0;
		Object[] Valuesa=datasortera.readFinalRecord();
		if (datasortera.geterror())
		{
			datasortera.deletefile();
			datasorterb.deletefile();
			return new Result(datasortera.getmessage(), false, null);
		}
		Object[] Valuesb=datasorterb.readFinalRecord();
		if (datasorterb.geterror())
		{
			datasortera.deletefile();
			datasorterb.deletefile();
			return new Result(datasorterb.getmessage(), false, null);
		}
		Object[] oldValuesa=new Object[0];
		Object[] oldValuesb=new Object[0];

		String [] values=new String[totalvar];
		int compare=0;
		CompareObjects co=new CompareObjects(mergevar.length, ascending);
		co.setconversion(noconversion);

		int compareTemp=0;
		boolean writerec=true;

		while ((readfromfirst) || (readfromsecond))
		{
			Keywords.percentage_done++;
			if ((Valuesa.length>0) && (Valuesb.length>0))
			{
				compare=co.getComparison(Valuesa, Valuesb);
				if (compare==0)
				{
					oldValuesa=new Object[Valuesa.length];
					oldValuesb=new Object[Valuesb.length];
					for (int j=0; j<Valuesa.length; j++)
					{
						try
						{
							values[j]=Valuesa[j].toString();
						}
						catch (Exception exc)
						{
							values[j]="";
						}
						oldValuesa[j]=Valuesa[j];
					}
					for (int j=0; j<Valuesb.length; j++)
					{
						if (j>=mergevar.length)
						{
							try
							{
								values[j+Valuesa.length-mergevar.length]=Valuesb[j].toString();
							}
							catch (Exception exc)
							{
								values[j+Valuesa.length-mergevar.length]="";
							}
						}
						oldValuesb[j]=Valuesb[j];
					}
					dw.write(values);
					firstpointer++;
					lastpointer++;
					if (firstpointer<totalrecorda)
						Valuesa=datasortera.readFinalRecord();
					else
					{
						readfromfirst=false;
						Valuesa=new Object[0];
					}
					if (lastpointer<totalrecordb)
						Valuesb=datasorterb.readFinalRecord();
					else
					{
						readfromsecond=false;
						Valuesb=new Object[0];
					}
				}
				else if (compare<0)
				{
					writerec=true;
					compareTemp=-1;
					if (oldValuesb.length>0)
						compareTemp=co.getComparison(Valuesa, oldValuesb);
					oldValuesa=new Object[Valuesa.length];
					for (int j=0; j<Valuesa.length; j++)
					{
						try
						{
							values[j]=Valuesa[j].toString();
						}
						catch (Exception exc)
						{
							values[j]="";
						}
						oldValuesa[j]=Valuesa[j];
					}
					if (compareTemp==0)
					{
						for (int j=mergevar.length; j<oldValuesb.length; j++)
						{
							try
							{
								values[j+Valuesa.length-mergevar.length]=oldValuesb[j].toString();
							}
							catch (Exception exc)
							{
								values[j+Valuesa.length-mergevar.length]="";
							}
						}
					}
					else
					{
						for (int j=Valuesa.length; j<values.length; j++)
						{
							values[j]="";
						}
						oldValuesb=new Object[0];
						if (valec>1)
							writerec=false;
					}
					if (writerec)
						dw.write(values);
					firstpointer++;
					if (firstpointer<totalrecorda)
						Valuesa=datasortera.readFinalRecord();
					else
					{
						readfromfirst=false;
						Valuesa=new Object[0];
					}
				}
				else
				{
					writerec=true;
					compareTemp=-1;
					if (oldValuesa.length>0)
						compareTemp=co.getComparison(oldValuesa, Valuesb);
					if (compareTemp==0)
					{
						for (int j=mergevar.length; j<oldValuesa.length; j++)
						{
							try
							{
								values[j]=oldValuesa[j].toString();
							}
							catch (Exception exc)
							{
								values[j]="";
							}
						}
					}
					else
					{
						for (int j=mergevar.length; j<totalvara; j++)
						{
							values[j]="";
						}
						oldValuesa=new Object[0];
						if (valec==1)
							writerec=false;
						if (valec==3)
							writerec=false;
					}
					oldValuesb=new Object[Valuesb.length];
					for (int r=0; r<mergevar.length; r++)
					{
						try
						{
							values[r]=Valuesb[r].toString();
						}
						catch (Exception exc)
						{
							values[r]="";
						}
						oldValuesb[r]=Valuesb[r];
					}
					for (int j=mergevar.length; j<Valuesb.length; j++)
					{
						try
						{
							values[j+totalvara-mergevar.length]=Valuesb[j].toString();
						}
						catch (Exception exc)
						{
							values[j+totalvara-mergevar.length]="";
						}
						oldValuesb[j]=Valuesb[j];
					}
					if (writerec)
						dw.write(values);
					lastpointer++;
					if (lastpointer<totalrecordb)
						Valuesb=datasorterb.readFinalRecord();
					else
					{
						readfromsecond=false;
						Valuesb=new Object[0];
					}
				}
			}
			else if ((Valuesa.length==0) && (Valuesb.length==0))
			{
				readfromfirst=false;
				readfromsecond=false;
			}
			else if ((Valuesa.length>0) && (Valuesb.length==0))
			{
				writerec=true;
				compareTemp=-1;
				if (oldValuesb.length>0)
					compareTemp=co.getComparison(Valuesa, oldValuesb);
				oldValuesa=new Object[Valuesa.length];
				for (int j=0; j<Valuesa.length; j++)
				{
					try
					{
						values[j]=Valuesa[j].toString();
					}
					catch (Exception exc)
					{
						values[j]="";
					}
					oldValuesa[j]=Valuesa[j];
				}
				if (compareTemp==0)
				{
					for (int j=mergevar.length; j<oldValuesb.length; j++)
					{
						try
						{
							values[j+Valuesa.length-mergevar.length]=oldValuesb[j].toString();
						}
						catch (Exception exc)
						{
							values[j+Valuesa.length-mergevar.length]="";
						}
					}
				}
				else
				{
					for (int j=Valuesa.length; j<values.length; j++)
					{
						values[j]="";
					}
					oldValuesb=new Object[0];
					if (valec>1)
							writerec=false;
				}
				if (writerec)
					dw.write(values);
				firstpointer++;
				if (firstpointer<totalrecorda)
					Valuesa=datasortera.readFinalRecord();
				else
				{
					readfromfirst=false;
					Valuesa=new Object[0];
				}
			}
			else if ((Valuesa.length==0) && (Valuesb.length>0))
			{
				writerec=true;
				compareTemp=-1;
				if (oldValuesa.length>0)
					compareTemp=co.getComparison(oldValuesa, Valuesb);
				oldValuesb=new Object[Valuesb.length];
				for (int r=0; r<mergevar.length; r++)
				{
					try
					{
						values[r]=Valuesb[r].toString();
					}
					catch (Exception exc)
					{
						values[r]="";
					}
					oldValuesb[r]=Valuesb[r];
				}
				if (compareTemp==0)
				{
					for (int j=mergevar.length; j<oldValuesa.length; j++)
					{
						try
						{
							values[j]=oldValuesa[j].toString();
						}
						catch (Exception exc)
						{
							values[j]="";
						}
					}
				}
				else
				{
					for (int j=mergevar.length; j<totalvara; j++)
					{
						values[j]="";
					}
					oldValuesa=new Object[0];
					if (valec==1)
						writerec=false;
					if (valec==3)
						writerec=false;
				}
				for (int j=mergevar.length; j<Valuesb.length; j++)
				{
					try
					{
						values[j+totalvara-mergevar.length]=Valuesb[j].toString();
					}
					catch (Exception exc)
					{
						values[j+totalvara-mergevar.length]="";
					}
					oldValuesb[j]=Valuesb[j];
				}
				if (writerec)
					dw.write(values);
				lastpointer++;
				if (lastpointer<totalrecordb)
					Valuesb=datasorterb.readFinalRecord();
				else
				{
					readfromsecond=false;
					Valuesb=new Object[0];
				}
			}
		}
		datasortera.closeFinalFile();
		datasorterb.closeFinalFile();
		Valuesa=new Object[0];
		Valuesb=new Object[0];
		oldValuesa=new Object[0];
		oldValuesb=new Object[0];
		Valuesa=null;
		Valuesb=null;
		oldValuesa=null;
		oldValuesb=null;
		System.gc();
		Hashtable<String, String> othertableinfo=new Hashtable<String, String>();
		if (force)
			vartemp=forcevarname.trim();
		othertableinfo.put(Keywords.SORTED.toLowerCase(), vartemp);
		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
		Hashtable<String, String> datatableinfo=dw.getTableInfo();
		Vector<StepResult> result = new Vector<StepResult>();
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
		parameters.add(new GetRequiredParameters(Keywords.descending,"checkbox",false,524,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.noconversion,"checkbox",false,2245,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.condition, "listsingle=2246_NULL,2247_"+Keywords.ifdicta+",2248_"+Keywords.ifdictb+",2249_"+Keywords.both,false, 2250, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.MaxDataBuffered,"text", false, 3741,dep,"",2));
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
		retprocinfo[1]="539";
		return retprocinfo;
	}
}

class SorterThread extends Thread
{
	DataSorter dsa;
	SorterThread(DataSorter dsa)
	{
		this.dsa=dsa;
	}
	public void run()
	{
		try
		{
			while (true)
			{
				dsa.sortdata();
				break;
			}
		}
 		catch (Exception e) {}
	}
}