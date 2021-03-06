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

import java.util.TreeMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import java.util.HashSet;

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.dataaccess.FastTempDataSet;


/**
* This is the procedure that writes the words in more records into a variable with many words
* @author marco.scarno@gmail.com
* @date 03/04/2017
*/
public class ProcRecords2sentences implements RunStep
{
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean noout=false;
		Vector<StepResult> result = new Vector<StepResult>();
		String[] requiredparameters = new String[] {Keywords.dict, Keywords.OUT.toLowerCase(), Keywords.var, Keywords.vardescriptor};
		String[] optionalparameters = new String[] {Keywords.minlength, Keywords.maxlength};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		DataWriter dw = new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);
		String minval = (String) parameters.get(Keywords.minlength);
		String maxval = (String) parameters.get(Keywords.maxlength);
		int minvalue = 0;
		int maxvalue = 100;
		try
		{
			if (minval != null)
			{
				minvalue = Integer.parseInt(minval);
			}
			if (maxval != null)
			{
				maxvalue = Integer.parseInt(maxval);
			}
		}
		catch (Exception e)
		{
			return new Result("%3325%<br>\n", false, null);
		}
		if (minvalue<0) return new Result("%3325%<br>\n", false, null);
		if (maxvalue<0) return new Result("%3325%<br>\n", false, null);
		if (minvalue>maxvalue) return new Result("%3325%<br>\n", false, null);
		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String vartemp=(String)parameters.get(Keywords.var.toLowerCase());
		String vardescriptor=(String)parameters.get(Keywords.vardescriptor.toLowerCase());
		if (vartemp.indexOf(" ")>0) return new Result("%4080%<br>\n", false, null);
		if (vardescriptor.indexOf(" ")>0) return new Result("%4081%<br>\n", false, null);
		VariableUtilities varu=new VariableUtilities(dict, vardescriptor, vartemp, null, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);
		String[] totalvar=new String[2];
		totalvar[0]=vardescriptor;
		totalvar[1]=vartemp;
		int[] replacerule=new int[2];
		for (int i=0; i<2; i++)
		{
			replacerule[i]=0;
		}
		DataReader data = new DataReader(dict);
		if (!data.open(totalvar, replacerule, false))
			return new Result(data.getmessage(), false, null);
		DataSetUtilities dsu=new DataSetUtilities();
		String keyword = "Records2sentences "+dict.getkeyword();
		String description = "Records2sentences " + dict.getdescription();
		String author = (String)parameters.get(Keywords.client_host.toLowerCase());
		Hashtable<String, String> tempmd1=new Hashtable<String, String>();
		Hashtable<String, String> tempmd2=new Hashtable<String, String>();
		Hashtable<String, String> tempmd3=new Hashtable<String, String>();
		dsu.addnewvar("vardescriptor", "%4082%", Keywords.TEXTSuffix, tempmd1, tempmd1);
		dsu.addnewvar("sentence", "%4125%", Keywords.TEXTSuffix, tempmd2, tempmd2);
		if (!dw.opendatatable(dsu.getfinalvarinfo())) return new Result(dw.getmessage(), false, null);
		String[] values=new String[2];
		String[] ref_values=new String[3];
		String[] outvalues=new String[2];
		int validgroup=0;
		int current_record=0;
		boolean write_record=true;

		String tempdir=(String)parameters.get(Keywords.WorkDir);
		FastTempDataSet tempftd=new FastTempDataSet(tempdir);

		while (!data.isLast())
		{
			values = data.getRecord();
			current_record++;
			if (values!=null)
			{
				write_record=true;
				if (minvalue>0 && values[1].length()<minvalue) write_record=false;
				if (maxvalue>0 && values[1].length()>maxvalue) write_record=false;
				if (values[0].equals("")) values[0]="-";
				if (write_record)
				{
					ref_values[0]=values[0].toLowerCase();
					ref_values[1]=String.valueOf(current_record);
					ref_values[2]=values[1].toLowerCase();
					validgroup++;
					tempftd.write(ref_values);
				}
			}
		}
		data.close();
		if (validgroup==0)
		{
			tempftd.forceClose();
			return new Result("%666%<br>\n", false, null);
		}
		tempftd.endwrite();
		tempftd.first_sort_num();
		tempftd.sortwith(1,1,3);
		tempftd.openSortedFile();
		boolean itera_records=true;
		Object[] current=new Object[2];
		String old_ref="";
		String curr_ref="";
		String curr_value="";
		while (itera_records)
		{
			current=tempftd.readSortedRecord();
			if (current==null) itera_records=false;
			else
			{
				curr_ref=current[0].toString();
				if (old_ref.equals("")) old_ref=curr_ref;
				if (old_ref.equals(curr_ref)) curr_value=curr_value+current[2].toString()+" ";
				if (!old_ref.equals(curr_ref))
				{
					outvalues[0]=old_ref;
					outvalues[1]=curr_value;
					dw.write(outvalues);
					old_ref=curr_ref;
					curr_value=current[2].toString()+" ";
				}
			}
		}
		tempftd.closeSortedFile();
		tempftd.deletefile();
		outvalues[0]=curr_ref;
		outvalues[1]=curr_value;
		dw.write(outvalues);
		boolean resclose = dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfo = dw.getVarInfo();
		Hashtable<String, String> datatableinfo = dw.getTableInfo();
		result.add(new LocalDictionaryWriter(dw.getdictpath(), keyword,description, author,
		dw.gettabletype(), datatableinfo,dsu.getfinalvarinfo(), tablevariableinfo, dsu.getfinalcl(),dsu.getfinalmd(), null));
		return new Result("", true, result);
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 4084, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 440, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", true, 4085, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vardescriptor, "vars=all", true, 4086, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.minlength, "text", false, 480, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.maxlength, "text", false, 481, dep, "", 2));
		return parameters;
	}
	/**
	*Return the name of the procedure and the group to which belongs
	*/
	public String[] getstepinfo() {
		String[] info=new String[2];
		info[0]="4178";
		info[1]="4087";
		return info;
	}
}
