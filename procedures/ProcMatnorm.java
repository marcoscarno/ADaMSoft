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
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that evaluate different kind of norms for a matrix
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcMatnorm implements RunStep
{
	/**
	* Starts the execution of Proc Matnorm and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.normtype};
		String [] optionalparameters=new String[] {Keywords.var, Keywords.where, Keywords.replace};
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
		String normtype=(String)parameters.get(Keywords.normtype.toLowerCase());
		VariableUtilities varu=new VariableUtilities(dict, null, vartemp, null, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);
		String[] var=varu.getanalysisvar();

		String replace=(String)parameters.get(Keywords.replace);
		if (normtype==null)
			normtype=Keywords.maxabscolsum;
		boolean checknorm=false;
		if (normtype.equalsIgnoreCase(Keywords.maxabscolsum))
			checknorm=true;
		if (normtype.equalsIgnoreCase(Keywords.euclidean))
			checknorm=true;
		if (normtype.equalsIgnoreCase(Keywords.frobenius))
			checknorm=true;
		if (normtype.equalsIgnoreCase(Keywords.infinity))
			checknorm=true;
		if (!checknorm)
			normtype=Keywords.maxabscolsum;

		String keyword="Matnorm "+normtype+dict.getkeyword();
		String description="Matnorm "+normtype+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		int totalvar=var.length;
		int[] replacerule=varu.getreplaceruleforsel(replace);

		DataReader data = new DataReader(dict);
		int records=data.getRecords();

		DoubleMatrix2D matA=null;
		try
		{
			matA=DoubleFactory2D.dense.make(records, totalvar);
		}
		catch (Exception e)
		{
			matA=null;
			String error=e.toString();
			if (error.startsWith("java.lang.IllegalArgumentException"))
				error="Error "+error.substring("java.lang.IllegalArgumentException".length());
			System.gc();
			error=error+"\n";
			return new Result("%579%<br>\n"+error+"<br>\n", false, null);
		}

		if (!data.open(var, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		int pointer=0;
		String[] values = null;
		double[] valdouble=null;
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				valdouble=new double[values.length];
				for (int i=0; i<values.length; i++)
				{
					if (values[i].equals(""))
					{
						matA=null;
						System.gc();
						return new Result("%580%<br>\n", false, null);
					}
					try
					{
						valdouble[i]=Double.parseDouble(values[i]);
					}
					catch (Exception nonnumber)
					{
						matA=null;
						System.gc();
						return new Result("%581%<br>\n", false, null);
					}
					matA.set(pointer, i, valdouble[i]);
				}
				pointer ++;
			}
		}
		data.close();
		if ((pointer==0) && (where!=null))
			return new Result("%2804%<br>\n", false, null);
		if ((pointer==0) && (where==null))
			return new Result("%666%<br>\n", false, null);
		double norm=0;
		try
		{
			Algebra algebra=new Algebra();
			if (normtype.equalsIgnoreCase(Keywords.maxabscolsum))
				norm=algebra.norm1(matA);
			if (normtype.equalsIgnoreCase(Keywords.euclidean))
				norm=algebra.norm2(matA);
			if (normtype.equalsIgnoreCase(Keywords.frobenius))
				norm=algebra.normF(matA);
			if (normtype.equalsIgnoreCase(Keywords.infinity))
				norm=algebra.normInfinity(matA);
		}
		catch (Exception e)
		{
			matA=null;
			System.gc();
			String error=e.toString();
			if (error.startsWith("java.lang.IllegalArgumentException"))
				error="Error "+error.substring("java.lang.IllegalArgumentException".length());
			error=error+"\n";
			return new Result("%596%<br>\n"+error+"<br>\n", false, null);
		}

		DataSetUtilities dsu=new DataSetUtilities();

		Hashtable<String, String> temph=new Hashtable<String, String>();

		dsu.addnewvar("v0", "%1162%", Keywords.NUMSuffix, temph, temph);

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		String[] valcond=new String[1];
		valcond[0]=String.valueOf(norm);
		dw.write(valcond);

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
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 585, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 440, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", false, 586, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.normtype, "listsingle=598_"+Keywords.maxabscolsum+",599_"+Keywords.euclidean+",600_"+Keywords.frobenius+",601_"+Keywords.infinity,true, 602, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="583";
		retprocinfo[1]="597";
		return retprocinfo;
	}
}
