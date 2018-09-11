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
import ADaMSoft.dataaccess.RandomDataSorter;
import ADaMSoft.dataaccess.DataWriter;

import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.keywords.Keywords;

/**
* This is the procedure that sorts randomly a dataset
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcRandomsort implements RunStep
{
	/**
	* Starts the execution of Proc RandomSort and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict};
		String [] optionalparameters=new String[] {Keywords.replace};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String vartemp=(String)parameters.get(Keywords.var.toLowerCase());
		VariableUtilities varu=new VariableUtilities(dict, null, vartemp, null, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String replace=(String)parameters.get(Keywords.replace);

		String keyword="RandomSort "+dict.getkeyword();
		String description="RandomSort "+dict.getdescription();
		String author=dict.getauthor();


		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);

		dsu.defineolddict(dict);

		int totalvar=dsu. gettotalvarnum();
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
		RandomDataSorter randomdatasorter=new RandomDataSorter(dict, tempdir, replacerule);
		if (randomdatasorter.geterror())
			return new Result(randomdatasorter.getmessage(), false, null);

		randomdatasorter.sortdata();
		if (randomdatasorter.geterror())
		{
			randomdatasorter.deletefile();
			return new Result(randomdatasorter.getmessage(), false, null);
		}
		if (!dw.opendatatable(dsu.getfinalvarinfo()))
		{
			randomdatasorter.deletefile();
			return new Result(dw.getmessage(), false, null);
		}
		randomdatasorter.openFinalFile();
		if (randomdatasorter.geterror())
		{
			randomdatasorter.deletefile();
			return new Result(randomdatasorter.getmessage(), false, null);
		}
		for (int i=0; i<randomdatasorter.getTotalRecords(); i++)
		{
			Object[] Values=randomdatasorter.readFinalRecord();
			if (Values.length>0)
			{
				String [] values=new String[Values.length];
				for (int j=0; j<Values.length; j++)
				{
					values[j]=Values[j].toString();
				}
				if (randomdatasorter.geterror())
				{
					randomdatasorter.deletefile();
					return new Result(randomdatasorter.getmessage(), false, null);
				}
				dw.write(values);
			}
		}
		randomdatasorter.closeFinalFile();
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
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 522, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 440, dep, "", 1));
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
		retprocinfo[0]="4162";
		retprocinfo[1]="922";
		return retprocinfo;
	}
}
