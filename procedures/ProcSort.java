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

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataSorter;
import ADaMSoft.dataaccess.DataWriter;

import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;

import ADaMSoft.keywords.Keywords;

/**
* This is the procedure that sorts a dataset
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcSort implements RunStep
{
	boolean ascending;
	boolean nodupkey;
	/**
	* Starts the execution of Proc Sort and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean noconversion=false;
		boolean[] varascending;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.var, Keywords.dict};
		String [] optionalparameters=new String[] {Keywords.descending, Keywords.noconversion, Keywords.replace, Keywords.addfirstvars, Keywords.addlastvars, Keywords.vardescending, Keywords.nodupkey, Keywords.where};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		ascending    =(parameters.get(Keywords.descending)==null);
		nodupkey    =(parameters.get(Keywords.nodupkey)!=null);
		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String vartemp=(String)parameters.get(Keywords.var.toLowerCase());
		VariableUtilities varu=new VariableUtilities(dict, null, vartemp, null, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String[] var=varu.getanalysisvar();
		varascending=new boolean[var.length];
		for (int j=0; j<var.length; j++)
		{
			varascending[j]=ascending;
		}
		String tempvardes=(String)parameters.get(Keywords.vardescending);
		String[] vardes=new String[0];
		if (tempvardes!=null)
		{
			if (!ascending)
				return new Result("%2676%<br>\n", false, null);
			vardes=tempvardes.split(" ");
			String mvar="";
			boolean nf=false;
			for (int i=0; i<vardes.length; i++)
			{
				nf=false;
				for (int j=0; j<var.length; j++)
				{
					if (var[j].equalsIgnoreCase(vardes[i]))
					{
						nf=true;
						varascending[j]=false;
					}
				}
				if (!nf)
					mvar=mvar+vardes[i]+" ";
			}
			mvar=mvar.trim();
			if (!mvar.equals(""))
				return new Result("%2674% ("+mvar+")<br>\n", false, null);
		}

		int mervar=var.length;

		String replace=(String)parameters.get(Keywords.replace);

		noconversion=(parameters.get(Keywords.noconversion)!=null);

		boolean addfirstvars=(parameters.get(Keywords.addfirstvars)!=null);
		boolean addlastvars=(parameters.get(Keywords.addlastvars)!=null);

		String keyword="Sort "+dict.getkeyword();
		String description="Sort "+dict.getdescription();
		String author=dict.getauthor();

		Vector<Hashtable<String, String>> fixedvariableinfo=dict.getfixedvariableinfo();

		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);

		Vector<String> remvar=new Vector<String>();

		for (int j=0; j<var.length; j++)
		{
			for (int i=0; i<fixedvariableinfo.size(); i++)
			{
				Hashtable<String, String> tempvar=fixedvariableinfo.get(i);
				String varname=(tempvar.get(Keywords.VariableName.toLowerCase())).trim();
				if (varname.equalsIgnoreCase(var[j].trim()))
					dsu.addnewvarfromolddict(dict, varname, null, null, varname);
			}
		}

		for (int i=0; i<fixedvariableinfo.size(); i++)
		{
			Hashtable<String, String> tempvar=fixedvariableinfo.get(i);
			String varname=(tempvar.get(Keywords.VariableName.toLowerCase())).trim();
			boolean issel=false;
			for (int j=0; j<var.length; j++)
			{
				if (varname.equalsIgnoreCase(var[j].trim()))
					issel=true;
			}
			if (!issel)
				remvar.add(varname);
		}

		for (int i=0; i<remvar.size(); i++)
		{
			String varname=remvar.get(i);
			dsu.addnewvarfromolddict(dict, varname, null, null, varname);
		}

		int reftotvar=fixedvariableinfo.size();

		dsu.setnewvarposition(reftotvar);

		if (addfirstvars)
		{
			reftotvar=reftotvar+var.length;
			for (int j=0; j<var.length; j++)
			{
				Hashtable<String, String> tempcl=new Hashtable<String, String>();
				tempcl.put("0", "%2407%");
				tempcl.put("1", "%2408%");
				Hashtable<String, String> tempmd=new Hashtable<String, String>();
				dsu.addnewvartoolddict("first_"+var[j], "%2406% "+dict.getvarlabelfromname(var[j]), Keywords.TEXTSuffix, tempcl, tempmd);
			}
		}

		if (addlastvars)
		{
			reftotvar=reftotvar+var.length;
			for (int j=0; j<var.length; j++)
			{
				Hashtable<String, String> tempcl=new Hashtable<String, String>();
				tempcl.put("0", "%2407%");
				tempcl.put("1", "%2408%");
				Hashtable<String, String> tempmd=new Hashtable<String, String>();
				dsu.addnewvartoolddict("last_"+var[j], "%2409% "+dict.getvarlabelfromname(var[j]), Keywords.TEXTSuffix, tempcl, tempmd);
			}
		}

		String[] newvalues=new String[reftotvar];

		int totalvar=dsu.gettotalvarnum();

		int[] replacerule=new int[totalvar];
		if (replace==null)
		{
			for (int j=0; j<totalvar; j++)
			{
				replacerule[j]=0;
			}
		}
		else if (replace.equalsIgnoreCase(Keywords.replaceall))
		{
			for (int j=0; j<totalvar; j++)
			{
				replacerule[j]=1;
			}
			dsu.setempycodelabels();
			dsu.setempymissingdata();
		}
		else if (replace.equalsIgnoreCase(Keywords.replaceformat))
		{
			for (int j=0; j<totalvar; j++)
			{
				replacerule[j]=2;
			}
			dsu.setempycodelabels();
		}
		else if (replace.equalsIgnoreCase(Keywords.replacemissing))
		{
			for (int j=0; j<totalvar; j++)
			{
				replacerule[j]=3;
			}
			dsu.setempymissingdata();
		}
		else
		{
			for (int j=0; j<totalvar; j++)
			{
				replacerule[j]=0;
			}
		}

		String tempdir=(String)parameters.get(Keywords.WorkDir);

		String where=(String)parameters.get(Keywords.where.toLowerCase());

		DataSorter datasorter=new DataSorter(dict, var, varascending, tempdir, replacerule, where);
		datasorter.setconversion(noconversion);
		if (datasorter.geterror())
			return new Result(datasorter.getmessage(), false, null);

		datasorter.sortdata();
		if (datasorter.geterror())
		{
			datasorter.deletefile();
			return new Result(datasorter.getmessage(), false, null);
		}
		if (!dw.opendatatable(dsu.getfinalvarinfo()))
		{
			datasorter.deletefile();
			return new Result(dw.getmessage(), false, null);
		}
		datasorter.openFinalFile();
		if (datasorter.geterror())
		{
			datasorter.deletefile();
			dw.deletetmp();
			return new Result(datasorter.getmessage(), false, null);
		}

		int lastlength=fixedvariableinfo.size();
		int newp=0;
		if (addfirstvars)
			newp=mervar;
		Vector <String[]> rwait=new Vector <String[]>();
		String[] firstvar=new String[mervar];
		int isequal=0;
		int duprecord=0;
		int totrecordord=datasorter.getTotalRecords();
		String[] firstvalues=null;
		String[] secondvalues=null;
		String[] oldfirstvalue=null;
		for (int i=0; i<totrecordord; i++)
		{
			Object[] Values=datasorter.readFinalRecord();
			if (Values!=null)
			{
				if (Values.length>0)
				{
					String [] values=new String[Values.length];
					for (int j=0; j<Values.length; j++)
					{
						try
						{
							values[j]=Values[j].toString();
						}
						catch (Exception ee)
						{
							values[j]="";
						}
					}
					rwait.add(values);
					if (datasorter.geterror())
					{
						datasorter.deletefile();
						dw.deletetmp();
						return new Result(datasorter.getmessage(), false, null);
					}
				}
			}
			if (rwait.size()==2)
			{
				firstvalues=rwait.get(0);
				secondvalues=rwait.get(1);
				for (int k=0; k<firstvalues.length; k++)
				{
					newvalues[k]=firstvalues[k];
				}
				if (addfirstvars)
				{
					if (i==1)
					{
						for (int f=0; f<mervar; f++)
						{
							newvalues[lastlength+f]="1";
							firstvar[f]=firstvalues[f];
						}
					}
					else
					{
						for (int f=0; f<mervar; f++)
						{
							if (firstvar[f].equals(firstvalues[f]))
								newvalues[lastlength+f]="0";
							else
								newvalues[lastlength+f]="1";
							firstvar[f]=firstvalues[f];
						}
					}
				}
				if (addlastvars)
				{
					if (i<(totrecordord))
					{
						for (int f=0; f<mervar; f++)
						{
							if (firstvalues[f].equals(secondvalues[f]))
								newvalues[newp+lastlength+f]="0";
							else
								newvalues[newp+lastlength+f]="1";
						}
					}
					if (nodupkey)
					{
						for (int f=0; f<mervar; f++)
						{
							newvalues[newp+lastlength+f]="1";
						}
					}
				}
				isequal=0;
				if ((nodupkey) && (i>1))
				{
					for (int f=0; f<mervar; f++)
					{
						if (firstvalues[f].equals(oldfirstvalue[f]))
							isequal++;
					}
				}
				if (isequal<mervar)
				{
					String[] wvalues=dsu.getnewvalues(secondvalues, newvalues);
					dw.write(wvalues);
					duprecord++;
				}
				oldfirstvalue=new String[firstvalues.length];
				for (int f=0; f<firstvalues.length; f++)
				{
					oldfirstvalue[f]=firstvalues[f];
				}
				rwait.remove(0);
			}
		}
		firstvalues=rwait.get(0);
		for (int k=0; k<firstvalues.length; k++)
		{
			newvalues[k]=firstvalues[k];
		}
		if (addfirstvars)
		{
			if (totrecordord==1)
			{
				for (int f=0; f<mervar; f++)
				{
					newvalues[lastlength+f]="1";
				}
			}
			else
			{
				for (int f=0; f<mervar; f++)
				{
					if (firstvar[f].equals(firstvalues[f]))
						newvalues[lastlength+f]="0";
					else
						newvalues[lastlength+f]="1";
				}
			}
		}
		if (addlastvars)
		{
			for (int f=0; f<mervar; f++)
			{
				newvalues[newp+lastlength+f]="1";
			}
		}
		if (totrecordord>1)
		{
			isequal=0;
			if (nodupkey)
			{
				if (totrecordord>1)
				{
					for (int f=0; f<mervar; f++)
					{
						if (firstvalues[f].equals(oldfirstvalue[f]))
							isequal++;
					}
				}
			}
			if (isequal<mervar)
			{
				String[] wvalues=dsu.getnewvalues(firstvalues, newvalues);
				dw.write(wvalues);
				duprecord++;
			}
		}
		else
		{
			String[] wvalues=dsu.getnewvalues(firstvalues, newvalues);
			dw.write(wvalues);
			duprecord++;
		}
		datasorter.closeFinalFile();
		Hashtable<String, String> othertableinfo=new Hashtable<String, String>();
		othertableinfo.put(Keywords.SORTED.toLowerCase(), vartemp);
		Vector<StepResult> result = new Vector<StepResult>();
		result.add(new LocalMessageGetter("%2678% "+String.valueOf(totrecordord)+"<br>\n"));
		if (duprecord<totrecordord)
		{
			result.add(new LocalMessageGetter("%2679% "+String.valueOf(totrecordord-duprecord)+"<br>\n"));
			result.add(new LocalMessageGetter("%2680% "+String.valueOf(duprecord)+"<br>\n"));
		}
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
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 522, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 440, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", true, 523, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vardescending, "vars=all", false, 2675, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.noconversion,"checkbox",false,2245,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.descending,"checkbox",false,524,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2681, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.nodupkey,"checkbox",false,2677,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.addfirstvars,"checkbox",false,2404,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.addlastvars,"checkbox",false,2405,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
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
		retprocinfo[0]="4162";
		retprocinfo[1]="521";
		return retprocinfo;
	}
}
