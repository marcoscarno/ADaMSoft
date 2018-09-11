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

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.EigenvalueDecomposition;
import cern.colt.matrix.linalg.Property;

/**
* This is the procedure that returns a new eigenvalue decomposition object
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcMateigen implements RunStep
{
	/**
	* Starts the execution of Proc MatEigenvalueDecomposition and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.OUTRE.toLowerCase(), Keywords.OUTV.toLowerCase(), Keywords.dict};
		String [] optionalparameters=new String[] {Keywords.var, Keywords.where, Keywords.replace, Keywords.OUTIE.toLowerCase()};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}

		boolean issim=false;

		DataWriter dwre=new DataWriter(parameters, Keywords.OUTRE.toLowerCase());
		if (!dwre.getmessage().equals(""))
			return new Result(dwre.getmessage(), false, null);

		String ie=(String)parameters.get(Keywords.OUTIE.toLowerCase());
		DataWriter dwie=null;
		if (ie!=null)
		{
			dwie=new DataWriter(parameters, Keywords.OUTIE.toLowerCase());
			if (!dwie.getmessage().equals(""))
				return new Result(dwie.getmessage(), false, null);
		}

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

		String keyword="Eigenvectors "+dict.getkeyword();
		String description="Eigenvectors "+dict.getdescription();
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
			error=error+"\n";
			System.gc();
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
						return new Result("%580%\n", false, null);
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
						return new Result("%581%\n", false, null);
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

		Property property=new Property(Property.DEFAULT.tolerance());
		try
		{
			issim=property.isSymmetric(matA);
		}
		catch (Exception e) {}

		int rowv=0;
		int colv=0;
		int rowr=0;
		int rowi=0;
		DoubleMatrix2D matV=null;
		DoubleMatrix1D matR=null;
		DoubleMatrix1D matI=null;
		try
		{
			EigenvalueDecomposition ed=new EigenvalueDecomposition(matA);
			matV=ed.getV();
			matR=ed.getRealEigenvalues();
			matI=ed.getImagEigenvalues();

			DoubleMatrix1D row=matV.viewColumn(0);
			DoubleMatrix1D col=matV.viewRow(0);
			double []rowd=row.toArray();
			double []cold=col.toArray();
			rowv=rowd.length;
			colv=cold.length;
			row=null;
			col=null;
			rowd=matR.toArray();
			rowr=rowd.length;
			rowd=matI.toArray();
			rowi=rowd.length;
			rowd=new double[0];
			cold=new double[0];
		}
		catch (Exception e)
		{
			matA=null;
			System.gc();
			String error=e.toString();
			if (error.startsWith("java.lang.IllegalArgumentException"))
				error="Error "+error.substring("java.lang.IllegalArgumentException".length());
			error=error+"\n";
			return new Result("%607%\n"+error+"\n", false, null);
		}

		DataSetUtilities dsu=new DataSetUtilities();

		Hashtable<String, String> temph=new Hashtable<String, String>();

		for (int i=0; i<colv; i++)
		{
			dsu.addnewvar("v"+(String.valueOf(i)), "v"+(String.valueOf(i)), Keywords.NUMSuffix, temph, temph);
		}

		if (!dwv.opendatatable(dsu.getfinalvarinfo()))
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

		String rkeyword="Real eigenvalues "+dict.getkeyword();
		String rdescription="Real eigenvalues "+dict.getdescription();

		DataSetUtilities rdsu=new DataSetUtilities();

		rdsu.addnewvar("v0", "%1159%", Keywords.NUMSuffix, temph, temph);

		if (!dwre.opendatatable(rdsu.getfinalvarinfo()))
			return new Result(dwre.getmessage(), false, null);

		values=new String[1];
		for (int i=0; i<rowr; i++)
		{
			values[0]=String.valueOf(matR.get(i));
			dwre.write(values);
		}

		DataSetUtilities idsu=new DataSetUtilities();

		String ikeyword="Imaginary eigenvalues "+dict.getkeyword();
		String idescription="Imaginary eigenvalues "+dict.getdescription();

		if (!issim)
		{
			idsu.addnewvar("v0", "%1160%", Keywords.NUMSuffix, temph, temph);
			if (ie!=null)
			{
				if (!dwie.opendatatable(idsu.getfinalvarinfo()))
					return new Result(dwie.getmessage(), false, null);
				values=new String[1];
				for (int i=0; i<rowi; i++)
				{
					values[0]=String.valueOf(matI.get(i));
					dwie.write(values);
				}
			}
		}
		boolean resclose=dwv.close();
		if (!resclose)
			return new Result(dwv.getmessage(), false, null);
		resclose=dwre.close();
		if (!resclose)
			return new Result(dwre.getmessage(), false, null);
		if ((ie!=null) && (!issim))
		{
			resclose=dwie.close();
			if (!resclose)
				return new Result(dwie.getmessage(), false, null);
		}
		Vector<Hashtable<String, String>> tablevariableinfo=dwv.getVarInfo();
		Hashtable<String, String> datatableinfo=dwv.getTableInfo();
		Vector<StepResult> result = new Vector<StepResult>();
		result.add(new LocalDictionaryWriter(dwv.getdictpath(), keyword, description, author, dwv.gettabletype(),
		datatableinfo, dsu.getfinalvarinfo(), tablevariableinfo, dsu.getfinalcl(), dsu.getfinalmd(), null));

		Vector<Hashtable<String, String>> rtablevariableinfo=dwre.getVarInfo();
		Hashtable<String, String> rdatatableinfo=dwre.getTableInfo();
		result.add(new LocalDictionaryWriter(dwre.getdictpath(), rkeyword, rdescription, author, dwre.gettabletype(),
		rdatatableinfo, rdsu.getfinalvarinfo(), rtablevariableinfo, rdsu.getfinalcl(), rdsu.getfinalmd(), null));

		if ((ie!=null) && (!issim))
		{
			Vector<Hashtable<String, String>> itablevariableinfo=dwie.getVarInfo();
			Hashtable<String, String> idatatableinfo=dwie.getTableInfo();
			result.add(new LocalDictionaryWriter(dwie.getdictpath(), ikeyword, idescription, author, dwie.gettabletype(),
			idatatableinfo, idsu.getfinalvarinfo(), itablevariableinfo, idsu.getfinalcl(), idsu.getfinalmd(), null));
		}
		else
			result.add(new LocalMessageGetter("%628%<br>\n"));

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
		parameters.add(new GetRequiredParameters(Keywords.OUTV.toLowerCase()+"=", "setting=out", true, 608, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUTRE.toLowerCase()+"=", "setting=out", true, 609, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUTIE.toLowerCase()+"=", "setting=out", false, 610, dep, "", 1));
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
		retprocinfo[1]="611";
		return retprocinfo;
	}
}
