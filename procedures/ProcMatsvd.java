/**
* Copyright (C) 2017 MS
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
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.keywords.Keywords;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.SingularValueDecomposition;

/**
* This is the procedure that returns a new singular value decomposition object
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcMatsvd implements RunStep
{
	/**
	* Starts the execution of Proc Matsvd and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.OUTU.toLowerCase(), Keywords.OUTV.toLowerCase(), Keywords.dict, Keywords.OUTS.toLowerCase()};
		String [] optionalparameters=new String[] {Keywords.var, Keywords.where, Keywords.replace};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		DataWriter dwu=new DataWriter(parameters, Keywords.OUTU.toLowerCase());
		if (!dwu.getmessage().equals(""))
			return new Result(dwu.getmessage(), false, null);

		DataWriter dws=new DataWriter(parameters, Keywords.OUTS.toLowerCase());
			if (!dws.getmessage().equals(""))
				return new Result(dws.getmessage(), false, null);

		DataWriter dwv=new DataWriter(parameters, Keywords.OUTV.toLowerCase());
		if (!dwv.getmessage().equals(""))
			return new Result(dwv.getmessage(), false, null);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String vartemp=(String)parameters.get(Keywords.var.toLowerCase());
		VariableUtilities varu=new VariableUtilities(dict, null, vartemp, null, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);
		String[] var=varu.getanalysisvar();

		String replace=(String)parameters.get(Keywords.replace);

		String ukeyword="Left singular Eigenvectors "+dict.getkeyword();
		String udescription="Left singular Eigenvectors "+dict.getdescription();
		String vkeyword="Right singular Eigenvectors "+dict.getkeyword();
		String vdescription="Right singular Eigenvectors "+dict.getdescription();
		String skeyword="Singular values "+dict.getkeyword();
		String sdescription="Singular values "+dict.getdescription();
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
						data.close();
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
						data.close();
						matA=null;
						System.gc();
						return new Result("%581%<br>\n", false, null);
					}
					matA.set(pointer,i,valdouble[i]);
				}
				pointer ++;
			}
		}
		data.close();
		if ((pointer==0) && (where!=null))
			return new Result("%2804%<br>\n", false, null);
		if ((pointer==0) && (where==null))
			return new Result("%666%<br>\n", false, null);

		int rowv=0;
		int colv=0;
		int rowu=0;
		int colu=0;
		int rows=0;
		int cols=0;
		DoubleMatrix2D matU=null;
		DoubleMatrix2D matV=null;
		DoubleMatrix2D matS=null;
		try
		{
			SingularValueDecomposition svd=new SingularValueDecomposition(matA);

			matV=svd.getV();
			matU=svd.getU();
			matS=svd.getS();

			DoubleMatrix1D row=matV.viewColumn(0);
			DoubleMatrix1D col=matV.viewRow(0);
			double []rowd=row.toArray();
			double []cold=col.toArray();
			rowv=rowd.length;
			colv=cold.length;

			row=matU.viewColumn(0);
			col=matU.viewRow(0);
			rowd=row.toArray();
			cold=col.toArray();
			rowu=rowd.length;
			colu=cold.length;

			row=matS.viewColumn(0);
			col=matS.viewRow(0);
			rowd=row.toArray();
			cold=col.toArray();
			rows=rowd.length;
			cols=cold.length;
		}
		catch (Exception e)
		{
			matA=null;
			System.gc();
			String error=e.toString();
			if (error.startsWith("java.lang.IllegalArgumentException"))
				error="Error "+error.substring("java.lang.IllegalArgumentException".length());
			error=error+"\n";
			return new Result("%612%<br>\n"+error+"<br>\n", false, null);
		}

		DataSetUtilities udsu=new DataSetUtilities();

		Hashtable<String, String> temph=new Hashtable<String, String>();

		for (int i=0; i<colu; i++)
		{
			udsu.addnewvar("v"+(String.valueOf(i)), "v"+(String.valueOf(i)), Keywords.NUMSuffix, temph, temph);
		}

		if (!dwu.opendatatable(udsu.getfinalvarinfo()))
			return new Result(dwu.getmessage(), false, null);

		for (int i=0; i<rowu; i++)
		{
			values=new String[colu];
			for (int j=0; j<colu; j++)
			{
				values[j]=String.valueOf(matU.get(i,j));
			}
			dwu.write(values);
		}

		DataSetUtilities vdsu=new DataSetUtilities();

		for (int i=0; i<colv; i++)
		{
			vdsu.addnewvar("v"+(String.valueOf(i)), "v"+(String.valueOf(i)), Keywords.NUMSuffix, temph, temph);
		}

		if (!dwv.opendatatable(vdsu.getfinalvarinfo()))
			return new Result(dwv.getmessage(), false, null);

		values=new String[colv];
		for (int i=0; i<rowv; i++)
		{
			for (int j=0; j<colv; j++)
			{
				values[j]=String.valueOf(matV.get(i,j));
			}
			dwv.write(values);
		}

		DataSetUtilities sdsu=new DataSetUtilities();

		for (int i=0; i<cols; i++)
		{
			sdsu.addnewvar("v"+(String.valueOf(i)), "v"+(String.valueOf(i)), Keywords.NUMSuffix, temph, temph);
		}

		if (!dws.opendatatable(sdsu.getfinalvarinfo()))
			return new Result(dws.getmessage(), false, null);

		values=new String[cols];
		for (int i=0; i<rows; i++)
		{
			for (int j=0; j<cols; j++)
			{
				values[j]=String.valueOf(matS.get(i,j));
			}
			dws.write(values);
		}

		boolean resclose=dwv.close();
		if (!resclose)
			return new Result(dwv.getmessage(), false, null);
		resclose=dws.close();
		if (!resclose)
			return new Result(dws.getmessage(), false, null);
		resclose=dwu.close();
		if (!resclose)
			return new Result(dwu.getmessage(), false, null);

		Vector<Hashtable<String, String>> vtablevariableinfo=dwv.getVarInfo();
		Hashtable<String, String> vdatatableinfo=dwv.getTableInfo();
		Vector<StepResult> result = new Vector<StepResult>();
		result.add(new LocalDictionaryWriter(dwv.getdictpath(), vkeyword, vdescription, author, dwv.gettabletype(),
		vdatatableinfo, vdsu.getfinalvarinfo(), vtablevariableinfo, vdsu.getfinalcl(), vdsu.getfinalmd(), null));

		Vector<Hashtable<String, String>> utablevariableinfo=dwu.getVarInfo();
		Hashtable<String, String> udatatableinfo=dwu.getTableInfo();
		result.add(new LocalDictionaryWriter(dwu.getdictpath(), ukeyword, udescription, author, dwu.gettabletype(),
		udatatableinfo, udsu.getfinalvarinfo(), utablevariableinfo, udsu.getfinalcl(), udsu.getfinalmd(), null));

		Vector<Hashtable<String, String>> stablevariableinfo=dws.getVarInfo();
		Hashtable<String, String> sdatatableinfo=dws.getTableInfo();
		result.add(new LocalDictionaryWriter(dws.getdictpath(), skeyword, sdescription, author, dws.gettabletype(),
		sdatatableinfo, sdsu.getfinalvarinfo(), stablevariableinfo, sdsu.getfinalcl(), sdsu.getfinalmd(), null));

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
		parameters.add(new GetRequiredParameters(Keywords.OUTV.toLowerCase()+"=", "setting=out", true, 614, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUTU.toLowerCase()+"=", "setting=out", true, 615, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUTS.toLowerCase()+"=", "setting=out", true, 616, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", false, 586, dep, "", 2));
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
		retprocinfo[0]="583";
		retprocinfo[1]="613";
		return retprocinfo;
	}
}
