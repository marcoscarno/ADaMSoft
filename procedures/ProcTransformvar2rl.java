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

import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that implements the cross products of two data sets for record linkage
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcTransformvar2rl extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Transformvar2rl
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict};
		String [] optionalparameters=new String[] {Keywords.vartodelaccents, Keywords.vartotrim, Keywords.vartoreplace, Keywords.vartoreplacenonascii, Keywords.vartoupcase, Keywords.vartolowcase, Keywords.vartotrimfromright, Keywords.vartotrimfromleft, Keywords.vartodeletewords, Keywords.codecharstoreplace, Keywords.wordstodelete, Keywords.replace};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		String replace =(String)parameters.get(Keywords.replace);
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

		int operations=0;
		String vartotrim=(String)parameters.get(Keywords.vartotrim);
		String vartoreplace=(String)parameters.get(Keywords.vartoreplace);
		String vartoupcase=(String)parameters.get(Keywords.vartoupcase);
		String vartolowcase=(String)parameters.get(Keywords.vartolowcase);
		String vartotrimfromright=(String)parameters.get(Keywords.vartotrimfromright);
		String vartotrimfromleft=(String)parameters.get(Keywords.vartotrimfromleft);
		String vartoreplacenonascii=(String)parameters.get(Keywords.vartoreplacenonascii);
		String codecharstoreplace=(String)parameters.get(Keywords.codecharstoreplace);
		String vartodelaccents=(String)parameters.get(Keywords.vartodelaccents);
		String vartodeletewords=(String)parameters.get(Keywords.vartodeletewords);

		String wordstodelete=(String)parameters.get(Keywords.wordstodelete);

		if (vartotrim!=null) operations++;
		if (vartoreplace!=null) operations++;
		if (vartoupcase!=null) operations++;
		if (vartolowcase!=null) operations++;
		if (vartotrimfromright!=null) operations++;
		if (vartotrimfromleft!=null) operations++;
		if (vartoreplacenonascii!=null) operations++;
		if (vartodelaccents!=null) operations++;
		if (vartodeletewords!=null) operations++;
		if (operations==0)
			return new Result("%2995%<br>\n", false, null);
		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		String keyword="Transformvar2rl "+dict.getkeyword();
		String description="Transformvar2rl "+dict.getdescription();
		String author=dict.getauthor();

		if ((vartoreplace!=null) && (codecharstoreplace==null))
			return new Result("%2995%<br>\n", false, null);

		if ((vartodeletewords!=null) && (wordstodelete==null))
			return new Result("%4044%<br>\n", false, null);

		String[] temptrimchars=null;
		if (codecharstoreplace!=null) temptrimchars=(codecharstoreplace.trim()).split(" ");

		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);
		dsu.defineolddict(dict);
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
		DataReader data = new DataReader(dict);
		Vector<Integer> posvartotrim=new Vector<Integer>();
		Vector<Integer> posvartoreplace=new Vector<Integer>();
		Vector<Integer> posvarvartoupcase=new Vector<Integer>();
		Vector<Integer> posvartolowcase=new Vector<Integer>();
		Vector<Integer> posvartotrimfromright=new Vector<Integer>();
		Vector<Integer> posvartotrimfromleft=new Vector<Integer>();
		Vector<Integer> posvartoreplacenonascii=new Vector<Integer>();
		Vector<Integer> posvartodelaccents=new Vector<Integer>();
		Vector<Integer> posvartodeletewords=new Vector<Integer>();
		String[] tempvartotrim=null;
		String[] tempvartoreplace=null;
		String[] tempvartoupcase=null;
		String[] tempvartolowcase=null;
		String[] tempvartotrimfromright=null;
		String[] tempvartotrimfromleft=null;
		String[] tempvartoreplacenonascii=null;
		String[] tempvartodelaccents=null;
		String[] tempvartodeletewords=null;
		if (vartotrim!=null) tempvartotrim=vartotrim.split(" ");
		if (vartoreplace!=null) tempvartoreplace=vartoreplace.split(" ");
		if (vartoupcase!=null) tempvartoupcase=vartoupcase.split(" ");
		if (vartolowcase!=null) tempvartolowcase=vartolowcase.split(" ");
		if (vartotrimfromright!=null) tempvartotrimfromright=vartotrimfromright.split(" ");
		if (vartotrimfromleft!=null) tempvartotrimfromleft=vartotrimfromleft.split(" ");
		if (vartoreplacenonascii!=null) tempvartoreplacenonascii=vartoreplacenonascii.split(" ");
		if (vartodelaccents!=null) tempvartodelaccents=vartodelaccents.split(" ");
		if (vartodeletewords!=null) tempvartodeletewords=vartodeletewords.split(" ");
		String errorvars="";
		boolean foundvar=false;
		String vn="";
		if (vartotrim!=null)
		{
			for (int i=0; i<tempvartotrim.length; i++)
			{
				foundvar=false;
				for (int j=0; j<dict.gettotalvar(); j++)
				{
					vn=dict.getvarname(j);
					if (vn.equalsIgnoreCase(tempvartotrim[i]))
					{
						posvartotrim.add(new Integer(j));
						foundvar=true;
					}
				}
				if (!foundvar) errorvars=errorvars+tempvartotrim[i]+" ";
			}
		}
		if (vartoreplace!=null)
		{
			for (int i=0; i<tempvartoreplace.length; i++)
			{
				foundvar=false;
				for (int j=0; j<dict.gettotalvar(); j++)
				{
					vn=dict.getvarname(j);
					if (vn.equalsIgnoreCase(tempvartoreplace[i]))
					{
						posvartoreplace.add(new Integer(j));
						foundvar=true;
					}
				}
				if (!foundvar) errorvars=errorvars+tempvartoreplace[i]+" ";
			}
		}
		if (vartoupcase!=null)
		{
			for (int i=0; i<tempvartoupcase.length; i++)
			{
				foundvar=false;
				for (int j=0; j<dict.gettotalvar(); j++)
				{
					vn=dict.getvarname(j);
					if (vn.equalsIgnoreCase(tempvartoupcase[i]))
					{
						posvarvartoupcase.add(new Integer(j));
						foundvar=true;
					}
				}
				if (!foundvar) errorvars=errorvars+tempvartoreplace[i]+" ";
			}
		}
		if (vartolowcase!=null)
		{
			for (int i=0; i<tempvartolowcase.length; i++)
			{
				foundvar=false;
				for (int j=0; j<dict.gettotalvar(); j++)
				{
					vn=dict.getvarname(j);
					if (vn.equalsIgnoreCase(tempvartolowcase[i]))
					{
						posvartolowcase.add(new Integer(j));
						foundvar=true;
					}
				}
				if (!foundvar) errorvars=errorvars+tempvartolowcase[i]+" ";
			}
		}
		if (vartotrimfromright!=null)
		{
			for (int i=0; i<tempvartotrimfromright.length; i++)
			{
				foundvar=false;
				for (int j=0; j<dict.gettotalvar(); j++)
				{
					vn=dict.getvarname(j);
					if (vn.equalsIgnoreCase(tempvartotrimfromright[i]))
					{
						posvartotrimfromright.add(new Integer(j));
						foundvar=true;
					}
				}
				if (!foundvar) errorvars=errorvars+tempvartotrimfromright[i]+" ";
			}
		}
		if (vartotrimfromleft!=null)
		{
			for (int i=0; i<tempvartotrimfromleft.length; i++)
			{
				foundvar=false;
				for (int j=0; j<dict.gettotalvar(); j++)
				{
					vn=dict.getvarname(j);
					if (vn.equalsIgnoreCase(tempvartotrimfromleft[i]))
					{
						posvartotrimfromleft.add(new Integer(j));
						foundvar=true;
					}
				}
				if (!foundvar) errorvars=errorvars+tempvartotrimfromleft[i]+" ";
			}
		}
		if (vartoreplacenonascii!=null)
		{
			for (int i=0; i<tempvartoreplacenonascii.length; i++)
			{
				foundvar=false;
				for (int j=0; j<dict.gettotalvar(); j++)
				{
					vn=dict.getvarname(j);
					if (vn.equalsIgnoreCase(tempvartoreplacenonascii[i]))
					{
						posvartoreplacenonascii.add(new Integer(j));
						foundvar=true;
					}
				}
				if (!foundvar) errorvars=errorvars+tempvartoreplacenonascii[i]+" ";
			}
		}
		if (vartodelaccents!=null)
		{
			for (int i=0; i<tempvartodelaccents.length; i++)
			{
				foundvar=false;
				for (int j=0; j<dict.gettotalvar(); j++)
				{
					vn=dict.getvarname(j);
					if (vn.equalsIgnoreCase(tempvartodelaccents[i]))
					{
						posvartodelaccents.add(new Integer(j));
						foundvar=true;
					}
				}
				if (!foundvar) errorvars=errorvars+tempvartodelaccents[i]+" ";
			}
		}
		if (vartodeletewords!=null)
		{
			for (int i=0; i<tempvartodeletewords.length; i++)
			{
				foundvar=false;
				for (int j=0; j<dict.gettotalvar(); j++)
				{
					vn=dict.getvarname(j);
					if (vn.equalsIgnoreCase(tempvartodeletewords[i]))
					{
						posvartodeletewords.add(new Integer(j));
						foundvar=true;
					}
				}
				if (!foundvar) errorvars=errorvars+tempvartodeletewords[i]+" ";
			}
		}
		if (!errorvars.equals(""))
			return new Result("%2996%: "+errorvars.trim().toUpperCase()+"<br>\n", false, null);

		if (!data.open(null, rifrep, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);
		String[] values=null;
		int position=0;
		int codec=0;
		String scode="";
		String[] pwr=new String[0];
		if (wordstodelete!=null)
		{
			pwr=wordstodelete.split(" ");
		}
		while (!data.isLast())
		{
			values = data.getRecord();
			for (int i=0; i<posvartoreplacenonascii.size(); i++)
			{
				position=(posvartoreplacenonascii.get(i)).intValue();
				try
				{
					values[position]=values[position].replaceAll("[^\\p{ASCII}]", " ");
				}
				catch (Exception et) {}
			}
			for (int i=0; i<posvartoreplace.size(); i++)
			{
				position=(posvartoreplace.get(i)).intValue();
				for (int j=0; j<temptrimchars.length; j++)
				{
					try
					{
						codec=Integer.parseInt(temptrimchars[j]);
						scode = new Character((char)codec).toString();
						values[position]=values[position].replaceAll(scode, "");
					}
					catch (Exception et) {}
				}
			}
			for (int i=0; i<posvartodelaccents.size(); i++)
			{
				position=(posvartodelaccents.get(i)).intValue();
				values[position] = values[position].replaceAll("[èéêë]","e");
				values[position] = values[position].replaceAll("[ûù]","u");
				values[position] = values[position].replaceAll("[ïî]","i");
				values[position] = values[position].replaceAll("[àâ]","a");
				values[position] = values[position].replaceAll("Ô","o");
				values[position] = values[position].replaceAll("[ÈÉÊË]","E");
				values[position] = values[position].replaceAll("[ÛÙ]","U");
				values[position] = values[position].replaceAll("[ÏÎ]","I");
				values[position] = values[position].replaceAll("[ÀÂ]","A");
				values[position] = values[position].replaceAll("Ô","O");
			}
			for (int i=0; i<posvartodeletewords.size(); i++)
			{
				position=(posvartodeletewords.get(i)).intValue();
				for (int j=0; j<pwr.length; j++)
				{
					values[position] = values[position].replaceAll("(?i)"+pwr[j],"");
				}
			}
			for (int i=0; i<posvarvartoupcase.size(); i++)
			{
				position=(posvarvartoupcase.get(i)).intValue();
				try
				{
					values[position]=values[position].toUpperCase();
				}
				catch (Exception et) {}
			}
			for (int i=0; i<posvartolowcase.size(); i++)
			{
				position=(posvartolowcase.get(i)).intValue();
				try
				{
					values[position]=values[position].toLowerCase();
				}
				catch (Exception et) {}
			}
			for (int i=0; i<posvartotrimfromright.size(); i++)
			{
				position=(posvartotrimfromright.get(i)).intValue();
				try
				{
					if (values[position].length()>1)
						values[position]=values[position].substring(0, values[position].length()-1);
				}
				catch (Exception et) {}
			}
			for (int i=0; i<posvartotrimfromleft.size(); i++)
			{
				position=(posvartotrimfromleft.get(i)).intValue();
				try
				{
					if (values[position].length()>1)
						values[position]=values[position].substring(1);
				}
				catch (Exception et) {}
			}
			for (int i=0; i<posvartotrim.size(); i++)
			{
				position=(posvartotrim.get(i)).intValue();
				try
				{
					values[position]=values[position].replaceAll("\t"," ");
					values[position]=values[position].replaceAll("\n"," ");
					values[position]=values[position].replaceAll("^ +| +$|( )+", "$1");
				}
				catch (Exception et) {}
			}
			dw.write(values);
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
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 2998, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 2999, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.vartoreplacenonascii, "vars=all", false, 3003, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vartoreplace, "vars=all", false, 3002, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vartodelaccents, "vars=all", false, 3000, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vartoupcase, "vars=all", false, 3004, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vartolowcase, "vars=all", false, 3005, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vartotrimfromright, "vars=all", false, 3006, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vartotrimfromleft, "vars=all", false, 3007, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vartotrim, "vars=all", false, 3001, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vartodeletewords, "vars=all", false, 4052, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.codecharstoreplace,"text", false, 3008,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.wordstodelete,"text", false, 4043,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="2919";
		retprocinfo[1]="2994";
		return retprocinfo;
	}
}
