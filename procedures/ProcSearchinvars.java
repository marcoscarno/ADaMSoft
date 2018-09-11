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
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;

import ADaMSoft.keywords.Keywords;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;


/**
* This is the procedure that search a string into one or more string variables and adds a new variables with search score
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcSearchinvars extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Searchinvars and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.var, Keywords.words};
		String [] optionalparameters=new String[] {Keywords.exact, Keywords.where, Keywords.ignorecase, Keywords.divby, Keywords.replace};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		boolean exact =(parameters.get(Keywords.exact)!=null);
		boolean ignorecase =(parameters.get(Keywords.ignorecase)!=null);
		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		String divby=(String)parameters.get(Keywords.divby.toLowerCase());
		if (divby==null)
			divby=Keywords.lenvlenw;

		String[] ratio=new String[] {Keywords.lenvlenw, Keywords.lenwlenv, Keywords.lenwords, Keywords.lenvars};
		int selectstyle=steputilities.CheckOption(ratio, divby);
		if (selectstyle==0)
			return new Result("%1775% "+Keywords.divby.toUpperCase()+"<br>\n"+steputilities.getMessage(), false, null);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String vartemp=(String)parameters.get(Keywords.var.toLowerCase());
		String[] var=vartemp.split(" ");

		String wordstemp=(String)parameters.get(Keywords.words);
		String[] words=wordstemp.split(" ");
		double lw=0;
		for (int i=0; i<words.length; i++)
		{
			lw=lw+words[i].length();
		}

		String replace=(String)parameters.get(Keywords.replace);

		String keyword="Searchinvars "+dict.getkeyword();
		String description="Searchinvars "+dict.getdescription();
		String author=dict.getauthor();

		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);
		dsu.defineolddict(dict);
		Hashtable<String, String> temp=new Hashtable<String, String>();
		dsu.addnewvartoolddict("searchscore", "%1542%", Keywords.NUMSuffix, temp, temp);

		int[] replacerule=new int[dict.gettotalvar()];
		int rep=0;
		if (replace!=null)
		{
			if (replace.equalsIgnoreCase(Keywords.replaceall))
			{
				dsu.setempycodelabels();
				dsu.setempymissingdata();
				rep=1;
			}
			else if (replace.equalsIgnoreCase(Keywords.replaceformat))
			{
				rep=2;
				dsu.setempycodelabels();
			}
			else if (replace.equalsIgnoreCase(Keywords.replacemissing))
			{
				rep=3;
				dsu.setempymissingdata();
			}
		}
		int[] numvar=new int[var.length];
		for (int i=0; i<dict.gettotalvar(); i++)
		{
			replacerule[i]=rep;
		}

		int checkvar=0;
		for (int j=0; j<var.length; j++)
		{
			for (int i=0; i<dict.gettotalvar(); i++)
			{
				String t=dict.getvarname(i);
				if (var[j].equalsIgnoreCase(t))
				{
					checkvar++;
					numvar[j]=i;
					break;
				}
			}
		}
		if (checkvar!=var.length)
			return new Result("%2401%<br>\n", false, null);

		DataReader data = new DataReader(dict);

		if (!data.open(null, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		String[] values=null;
		String text="";
		double res=0;
		String[] t=new String[0];
		String[] newvalues=new String[1];
		String[] wvalues=new String[0];
		double lb=0;
		double den=0;
		while (!data.isLast())
		{
			text="";
			res=0;
			values = data.getRecord();
			if (values!=null)
			{
				for (int i=0; i<numvar.length; i++)
				{
					text=text+" "+values[numvar[i]];
				}
				text=text.trim();
				t=text.split(" ");
				lb=0;
				for (int i=0; i<t.length; i++)
				{
					lb=lb+t[i].length();
				}
				if (!exact)
				{
					for (int i=0; i<words.length; i++)
					{
						if (ignorecase)
						{
							if (text.toUpperCase().indexOf(words[i].toUpperCase())>=0)
								res++;
						}
						else
						{
							if (text.indexOf(words[i])>=0)
								res++;
						}
					}
				}
				else
				{
					for (int i=0; i<words.length; i++)
					{
						double c=0;
						for (int j=0; j<t.length; j++)
						{
							if (ignorecase)
							{
								if (words[i].equalsIgnoreCase(t[j]))
									c=1;
							}
							else
							{
								if (words[i].equals(t[j]))
									c=1;
							}
						}
						res=res+c;
					}
				}
				den=lb/lw;
				if (selectstyle==2)
					den=lw/lb;
				if (selectstyle==3)
					den=words.length;
				if (selectstyle==4)
					den=t.length;

				newvalues[0]=double2String(res/den);
				wvalues=dsu.getnewvalues(values, newvalues);
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
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 541, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 440, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", true, 1544, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.words, "text", true, 1545, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.exact, "checkbox", false, 1546, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.ignorecase, "checkbox", false, 1547, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.divby, "listsingle=1548_"+Keywords.lenvlenw+",1549_"+Keywords.lenwlenv+",1550_"+Keywords.lenwords+",1551_"+Keywords.lenvars, false, 1552, dep, "", 2));
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
		retprocinfo[0]="4175";
		retprocinfo[1]="1543";
		return retprocinfo;
	}
}
