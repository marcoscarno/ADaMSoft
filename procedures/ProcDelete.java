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

import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.keywords.Keywords;

import java.util.LinkedList;
import java.util.Vector;
import java.util.Hashtable;


/**
* This is the procedure that delete a dataset (or part of it, the dictionary or the data table)
* @author marco.scarno@gmail.com
* @13/02/2017
*/
public class ProcDelete implements RunStep
{
	/**
	* Starts the execution of Proc Delete and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.dict};
		String [] optionalparameters=new String[] {Keywords.onlydict, Keywords.onlytable};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		boolean onlydict =(parameters.get(Keywords.onlydict)!=null);
		boolean onlytable=(parameters.get(Keywords.onlytable)!=null);

		if ((onlydict) && (onlytable))
			return new Result("%1726%<br>\n", false, null);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		Vector<StepResult> result = new Vector<StepResult>();

		if (!onlydict)
		{
			DataReader data = new DataReader(dict);
			if (!data.open(null, null, false))
				return new Result(data.getmessage(), false, null);
			boolean resdel=data.deletetable();
			if (resdel)
				result.add(new LocalMessageGetter("%1729%<br>\n"));
			else
			{
				result.add(new LocalMessageGetter("%1730%<br>\n"));
				return new Result("", false, result);
			}
			if (onlytable)
				return new Result("", true, result);
		}
		result.add(new LocalDictionaryDelete(dict.getDictPath()));
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
		parameters.add(new GetRequiredParameters(Keywords.onlydict, "checkbox", false, 1727, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.onlytable, "checkbox", false, 1728, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="4166";
		retprocinfo[1]="1725";
		return retprocinfo;
	}
}
