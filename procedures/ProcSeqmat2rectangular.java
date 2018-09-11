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
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;

import ADaMSoft.keywords.Keywords;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;


/**
* This is the procedure that transform a matrix contained in a data set with row column and value into a rectangular matrix
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcSeqmat2rectangular implements RunStep
{
	/**
	* Starts the execution of Proc Seqmat2rectangular and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.varrow, Keywords.varcol, Keywords.varval};
		String [] optionalparameters=new String[] {Keywords.issquare, Keywords.where, Keywords.setmdzero, Keywords.replace};
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

		boolean issquare =(parameters.get(Keywords.issquare)!=null);
		boolean setmdzero =(parameters.get(Keywords.setmdzero)!=null);

		String varrow=(String)parameters.get(Keywords.varrow.toLowerCase());
		String varcol=(String)parameters.get(Keywords.varcol.toLowerCase());
		String varval=(String)parameters.get(Keywords.varval.toLowerCase());

		String vartemp=varrow.trim()+" "+varcol.trim()+" "+varval.trim();

		VariableUtilities varu=new VariableUtilities(dict, null, vartemp, null, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);
		String[] var=varu.getanalysisvar();
		if (var.length!=3)
			return new Result("%2573%<br>\n", false, null);

		String replace=(String)parameters.get(Keywords.replace);

		String keyword="Seqmat2rectangular "+dict.getkeyword();
		String description="Seqmat2rectangular "+dict.getdescription();
		String author=dict.getauthor();

		int[] replacerule=varu.getreplaceruleforsel(replace);
		DataReader data = new DataReader(dict);

		if (!data.open(var, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		int maxrow=0;
		int maxcol=0;
		boolean recok=true;
		int pointer=0;
		int crow=0;
		int ccol=0;
		String[] values=null;
		double tvalref=0;
		while (!data.isLast())
		{
			recok=true;
			values = data.getRecord();
			if (values!=null)
			{
				pointer++;
				try
				{
					tvalref=Double.parseDouble(values[0]);
					crow=(int)tvalref;
					if (crow>maxrow)
						maxrow=crow;
					if (crow<1) recok=false;
				}
				catch (Exception en)
				{
					recok=false;
				}
				try
				{
					tvalref=Double.parseDouble(values[1]);
					ccol=(int)tvalref;
					if (ccol>maxcol)
						maxcol=ccol;
					if (ccol<1) recok=false;
				}
				catch (Exception en)
				{
					recok=false;
				}
				if (!recok)
				{
					data.close();
					return new Result("%2574% ("+String.valueOf(pointer)+")<br>\n", false, null);
				}
			}
		}
		data.close();
		if (issquare)
		{
			if (maxrow>maxcol)
				maxcol=maxrow;
			else
				maxrow=maxcol;
		}

		double[][] matval=new double[maxrow][maxcol];
		for (int i=0; i<maxrow; i++)
		{
			for (int j=0; j<maxcol; j++)
			{
				matval[i][j]=Double.NaN;
			}
		}
		double tempval=0;
		if (!data.open(var, replacerule, false))
			return new Result(data.getmessage(), false, null);
		while (!data.isLast())
		{
			values = data.getRecord();
			try
			{
				tvalref=Double.parseDouble(values[0]);
				crow=(int)tvalref;
			}
			catch (Exception en){}
			try
			{
				tvalref=Double.parseDouble(values[1]);
				ccol=(int)tvalref;
			}
			catch (Exception en){}
			try
			{
				tempval=Double.parseDouble(values[2]);
				matval[crow-1][ccol-1]=tempval;
			}
			catch (Exception en){}
		}
		data.close();

		DataSetUtilities dsu=new DataSetUtilities();
		Hashtable<String, String> temph=new Hashtable<String, String>();
		for (int i=0; i<maxcol; i++)
		{
			dsu.addnewvar("col_"+String.valueOf(i+1), "%2575% "+String.valueOf(i+1), Keywords.NUMSuffix, temph, temph);
		}
		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		values=new String[maxcol];

		for (int i=0; i<maxrow; i++)
		{
			for (int j=0; j<maxcol; j++)
			{
				values[j]="";
				if (!Double.isNaN(matval[i][j]))
					values[j]=String.valueOf(matval[i][j]);
				if ((setmdzero) && (Double.isNaN(matval[i][j])))
					values[j]="0";
			}
			dw.write(values);
		}

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
		parameters.add(new GetRequiredParameters(Keywords.varrow, "var=all", true, 2568, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varcol, "var=all", true, 2569, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varval, "var=all", true, 2570, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.issquare, "checkbox", false, 2571, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.setmdzero, "checkbox", false, 2572, dep, "", 2));
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
		retprocinfo[0]="4166";
		retprocinfo[1]="2567";
		return retprocinfo;
	}
}
