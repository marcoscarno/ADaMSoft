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

import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;

import ADaMSoft.keywords.Keywords;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import java.util.Iterator;
import java.util.TreeMap;
import ADaMSoft.utilities.StepUtilities;


/**
* This is the procedure that creates virtual docs by merging the frequencies of already existent docs
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class ProcAddvirtualdocs extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Addvirtualdocs
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		Vector<StepResult> result = new Vector<StepResult>();
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.virtualdoc};
		String [] optionalparameters=new String[] {Keywords.where, Keywords.vartokeep, Keywords.replace};
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		Keywords.percentage_total=3;

		String replace =(String)parameters.get(Keywords.replace);
		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
		{
			Keywords.percentage_total=0;
			return new Result(dw.getmessage(), false, null);
		}
		String vartokeep =(String)parameters.get(Keywords.vartokeep);
		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String virtualdoc=(String)parameters.get(Keywords.virtualdoc);
		String[] virtualdocs=virtualdoc.split(";");
		TreeMap<String, int[]> posnewdocs=new TreeMap<String, int[]>();
		try
		{
			for (int i=0; i<virtualdocs.length; i++)
			{
				String[] temp=virtualdocs[i].split("=");
				if (temp.length!=2)
				{
					Keywords.percentage_total=0;
					return new Result("%3640% ("+virtualdocs[i]+")<br>\n", false, null);
				}
				String docname=temp[0];
				String tempdv=replacevarnames(temp[1]);
				String[] docvars=(tempdv.trim()).split(" ");
				int[] posd=new int[docvars.length];
				String vne="";
				for (int j=0; j<dict.gettotalvar(); j++)
				{
					if (docname.equalsIgnoreCase(dict.getvarname(j)))
					{
						Keywords.percentage_total=0;
						return new Result("%3641% ("+docname+")<br>\n", false, null);
					}
				}
				for (int k=0; k<docvars.length; k++)
				{
					boolean nev=false;
					for (int j=0; j<dict.gettotalvar(); j++)
					{
						if (docvars[k].equalsIgnoreCase(dict.getvarname(j)))
						{
							nev=true;
							posd[k]=j;
						}
					}
					if (!nev) vne=vne+docvars[k]+" ";
				}
				if (!vne.equals(""))
				{
					Keywords.percentage_total=0;
					return new Result("%3642% ("+vne+")<br>\n", false, null);
				}
				posnewdocs.put(docname, posd);
			}
		}
		catch (Exception evd)
		{
			Keywords.percentage_total=0;
			return new Result("%3640%<br>\n", false, null);
		}
		String[] temp_vartokeep=new String[0];
		int[] pos_vnk=new int[0];
		if (vartokeep!=null)
		{
			temp_vartokeep=vartokeep.split(" ");
			pos_vnk=new int[temp_vartokeep.length];
			String vnex="";
			boolean fvne=true;
			for (int i=0; i<temp_vartokeep.length; i++)
			{
				fvne=false;
				for (int j=0; j<dict.gettotalvar(); j++)
				{
					if (temp_vartokeep[i].equalsIgnoreCase(dict.getvarname(j)))
					{
						pos_vnk[i]=j;
						fvne=true;
					}
				}
				if (!fvne) vnex=vnex+temp_vartokeep[i]+" ";
			}
			if (!vnex.equals(""))
			{
				Keywords.percentage_total=0;
				return new Result("%3768% ("+vnex.trim()+")<br>\n", false, null);
			}
		}
		Keywords.percentage_done=1;

		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);
		Hashtable<String, String> temph=new Hashtable<String, String>();
		if (vartokeep==null) dsu.defineolddict(dict);
		else
		{
			dsu.defineolddictwithvar(dict, temp_vartokeep);
			dsu.setpointerfornewvar(temp_vartokeep.length);
		}
		Iterator<String> et = posnewdocs.keySet().iterator();
		while (et.hasNext())
		{
			String two = et.next();
			dsu.addnewvartoolddict(two, two, Keywords.NUMSuffix+Keywords.INTSuffix, temph, temph);
		}
		int newvariables=posnewdocs.size();

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
		{
			Keywords.percentage_done=0;
			Keywords.percentage_total=0;
			return new Result(dw.getmessage(), false, null);
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

		DataReader data = new DataReader(dict);

		if (!data.open(null, rifrep, false))
		{
			Keywords.percentage_done=0;
			Keywords.percentage_total=0;
			return new Result(data.getmessage(), false, null);
		}
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where))
			{
				Keywords.percentage_done=0;
				Keywords.percentage_total=0;
				return new Result(data.getmessage(), false, null);
			}
		}
		String[] values=null;
		int refpos=0;
		double freqs=0;
		double tempv=0;
		Keywords.percentage_done=2;
		String[] wvalues=null;
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				String[] newvalues=new String[newvariables];
				Iterator<String> ett = posnewdocs.keySet().iterator();
				refpos=0;
				while (ett.hasNext())
				{
					newvalues[refpos]="";
					String two = ett.next();
					int[] positions=posnewdocs.get(two);
					freqs=0;
					for (int i=0; i<positions.length; i++)
					{
						if (!values[positions[i]].equals(""))
						{
							tempv=Double.NaN;
							try
							{
								tempv=Double.parseDouble(values[positions[i]]);
							}
							catch (Exception ef){}
							if (!Double.isNaN(tempv)) freqs=freqs+tempv;
						}
					}
					newvalues[refpos]=String.valueOf(freqs);
					refpos++;
				}
				if (vartokeep==null) wvalues=dsu.getnewvalues(values, newvalues);
				else wvalues=dsu.getoldnewvalues(values, newvalues);
				dw.write(wvalues);
			}
		}
		data.close();
		Keywords.percentage_done=0;
		Keywords.percentage_total=0;

		String keyword="Addvirtualdocs "+dict.getkeyword();
		String description="Addvirtualdocs "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
		Hashtable<String, String> datatableinfo=dw.getTableInfo();
		result.add(new LocalDictionaryWriter(dw.getdictpath(), keyword, description, author, dw.gettabletype(),
		datatableinfo, dsu.getfinalvarinfo(), tablevariableinfo, dsu.getfinalcl(), dsu.getfinalmd(), null));
		return new Result("", true, result);
	}
	private String replacevarnames(String varnames)
	{
		varnames=varnames.trim();
		varnames=varnames.toUpperCase();
		String iniVar, endVar, FirstVar, LastVar, suffixIni, suffixEnd;
		int inivalue, endvalue;
		while ((varnames!=null) && ((varnames.indexOf("-"))>0))
		{
			iniVar=varnames.substring(0,varnames.indexOf("-"));
			iniVar=iniVar.trim();
			try
			{
				FirstVar=iniVar.substring(iniVar.lastIndexOf(" "));
			}
			catch (Exception ex)
			{
				FirstVar=iniVar;
			}
			endVar=varnames.substring(varnames.indexOf("-")+1);
			endVar=endVar.trim();
			try
			{
				LastVar=endVar.substring(0,endVar.indexOf(" "));
			}
			catch (Exception ex)
			{
				LastVar=endVar;
			}
			FirstVar=FirstVar.trim();
			LastVar=LastVar.trim();
			inivalue=-1;
			endvalue=0;
			int posinival=0;
			int posendval=0;
			for (int i=0; i<FirstVar.length(); i++)
			{
				if (posinival==0)
				{
					try
					{
						inivalue=Integer.parseInt(FirstVar.substring(i));
						posinival=i;
					}
					catch (Exception ex)
					{
						inivalue=-1;
						posinival=0;
					}
				}
			}
			suffixIni=FirstVar.substring(0,posinival);
			for (int i=0; i<LastVar.length(); i++)
			{
				if (posendval==0)
				{
					try
					{
						endvalue=Integer.parseInt(LastVar.substring(i));
						posendval=i;
					}
					catch (Exception ex)
					{
						endvalue=0;
						posendval=0;
					}
				}
			}
			suffixEnd=LastVar.substring(0,posendval);
			if (!suffixEnd.equals(suffixIni))
			{
				varnames=null;
			}
			else
			{
				if (endvalue==0) varnames=null;
				else if (inivalue==-1) varnames=null;
				else
				{
					if (inivalue<endvalue)
					{
						for (int j=inivalue+1; j<endvalue; j++)
						{
							iniVar=iniVar+" "+suffixIni+j;
						}
						varnames=iniVar+" "+endVar;
					}
					else varnames=null;
				}
			}
		}
		return varnames;
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 3635, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 3636, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.virtualdoc, "longtext", true, 3637, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 3638, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 3639, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vartokeep,"text", false, 3766,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 3767, dep, "", 2));
		dep = new String[0];
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
		retprocinfo[0]="4176";
		retprocinfo[1]="3643";
		return retprocinfo;
	}
}
