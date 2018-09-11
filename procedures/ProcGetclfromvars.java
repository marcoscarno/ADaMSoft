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
import java.util.Vector;
import java.util.LinkedList;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.utilities.StepUtilities;

/**
* This procedure adds code labels from vars
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcGetclfromvars implements RunStep
{
	/**
	* Creates a dictionary for a delimited text file
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.dict, Keywords.dictcl, Keywords.outdict, Keywords.var, Keywords.varcode, Keywords.varlabel};
		String [] optionalparameters=new String[] {Keywords.overwrite, Keywords.where, Keywords.replace};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		boolean overwrite=(parameters.get(Keywords.overwrite)!=null);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		DictionaryReader dictcl = (DictionaryReader)parameters.get(Keywords.dictcl);
		String path       = (String) parameters.get(Keywords.outdict);
		String replace =(String)parameters.get(Keywords.replace);

		String tempvar =(String)parameters.get(Keywords.var);
		String tempvarcode =(String)parameters.get(Keywords.varcode);
		String tempvarlabel=(String)parameters.get(Keywords.varlabel);

		String[] var=tempvar.split(" ");
		String[] varcode=tempvarcode.split(" ");
		String[] varlabel=tempvarlabel.split(" ");

		if (var.length!=varcode.length)
			return new Result("%1783%<br>\n", false, null);

		if (var.length!=varlabel.length)
			return new Result("%1784%<br>\n", false, null);

		String vartemp=tempvarcode.trim()+" "+tempvarlabel.trim();

		VariableUtilities varu=new VariableUtilities(dictcl, null, vartemp, null, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);
		String[] varcl=varu.getanalysisvar();
		int[] replacerule=varu.getreplaceruleforsel(replace);

		int[] pointer=new int[var.length];
		for (int j=0; j<var.length; j++)
		{
			for (int i=0; i<dict.gettotalvar(); i++)
			{
				if (dict.getvarname(i).equalsIgnoreCase(var[j].trim()))
					pointer[j]=i;
			}
		}

		DataReader data = new DataReader(dictcl);
		if (!data.open(varcl, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		String keyword=dict.getkeyword();
		String description=dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());
		String datatabletype=dict.getdatatabletype();
		Hashtable<String, String> datatableinfo=dict.getdatatableinfo();

		Vector<Hashtable<String, String>> fixedvariableinfo=dict.getfixedvariableinfo();
		Vector<Hashtable<String, String>> tablevariableinfo=dict.gettablevariableinfo();
		Vector<Hashtable<String, String>> codelabel=dict.getcodelabel();
		Vector<Hashtable<String, String>> missingdata=dict.getmissingdata();

		String[] values=null;
		int validgroup=0;
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				validgroup++;
				for (int i=0; i<var.length; i++)
				{
					String code=values[i];
					String label=values[i+var.length];
					if ((!code.equals("")) && (!label.equals("")))
					{
						Hashtable<String, String> tempcodelabel=codelabel.get(pointer[i]);
						if (!overwrite)
						{
							if (tempcodelabel.get(code)==null)
								tempcodelabel.put(code, label);
						}
						else
							tempcodelabel.put(code, label);
					}
				}
			}
		}
		data.close();
		if (validgroup==0)
			return new Result("%2807%<br>\n", false, null);

		Object viewclass=dict.getviewclass();

		Vector<StepResult> result = new Vector<StepResult>();
		LocalDictionaryWriter ldw=new LocalDictionaryWriter(path, keyword, description, author, datatabletype,
		datatableinfo, fixedvariableinfo, tablevariableinfo, codelabel, missingdata, null);
		if (viewclass!=null)
			ldw.setviewclass(viewclass);

		result.add(ldw);

		return new Result("", true, result);
	}
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={Keywords.dict};
		String[] depcl ={Keywords.dictcl};
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 1785, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dictcl+"=", "dict", true, 1786, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.outdict+"=", "outdictreport", true, 1792, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", true, 1787, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 1788, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varcode, "vars=all", true, 1789, depcl, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varlabel, "vars=all", true, 1790, depcl, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.overwrite, "checkbox", false, 1791, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));

		return parameters;
	}
	public String[] getstepinfo()
	{
		String[] retstepinfo=new String[2];
		retstepinfo[0]="4166";
		retstepinfo[1]="1782";
		return retstepinfo;
	}
}
