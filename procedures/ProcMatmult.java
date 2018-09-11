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
import cern.colt.matrix.linalg.Algebra;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that evaluate the product of two matrices
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcMatmult implements RunStep
{
	/**
	* Starts the execution of Proc Matmult and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict+"a", Keywords.dict+"b"};
		String [] optionalparameters=new String[] {Keywords.var+"a", Keywords.var+"b",Keywords.replace+"b", Keywords.replace+"a"};
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

		DictionaryReader dicta = (DictionaryReader)parameters.get(Keywords.dict+"a");
		String vartempa=(String)parameters.get((Keywords.var+"a").toLowerCase());
		VariableUtilities varua=new VariableUtilities(dicta, null, vartempa, null, null, null);
		if (varua.geterror())
			return new Result(varua.getmessage(), false, null);
		String[] vara=varua.getanalysisvar();

		DictionaryReader dictb = (DictionaryReader)parameters.get(Keywords.dict+"b");
		String vartempb=(String)parameters.get((Keywords.var+"b").toLowerCase());
		VariableUtilities varub=new VariableUtilities(dictb, null, vartempb, null, null, null);
		if (varub.geterror())
			return new Result(varub.getmessage(), false, null);
		String[] varb=varub.getanalysisvar();

		String replacea=(String)parameters.get(Keywords.replace+"a");
		String replaceb=(String)parameters.get(Keywords.replace+"b");

		String keyword="Matmult "+dicta.getkeyword()+" "+dictb.getkeyword();
		String description="Matmult "+dicta.getdescription()+" "+dicta.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		int totalvara=vara.length;
		int[] replacerulea=varua.getreplaceruleforsel(replacea);

		int totalvarb=varb.length;
		int[] replaceruleb=varub.getreplaceruleforsel(replaceb);

		DataReader dataa = new DataReader(dicta);
		int recordsa=dataa.getRecords();

		DoubleMatrix2D matA=null;
		double scalar=0;
		String[] values = null;
		double[] valdouble=null;
		if ((recordsa==1) && (totalvara==1))
		{
			if (!dataa.open(vara, replacerulea, false))
				return new Result(dataa.getmessage(), false, null);
			while (!dataa.isLast())
			{
				values = dataa.getRecord();
				try
				{
					scalar=Double.parseDouble(values[0]);
				}
				catch (Exception nonnumber)
				{
					System.gc();
					return new Result("%581%<br>\n", false, null);
				}
			}
			dataa.close();
		}
		else
		{
			try
			{
				matA=DoubleFactory2D.dense.make(recordsa, totalvara);
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

			if (!dataa.open(vara, replacerulea, false))
				return new Result(dataa.getmessage(), false, null);

			int pointer=0;
			while (!dataa.isLast())
			{
				values = dataa.getRecord();
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
			dataa.close();
		}

		DataReader datab = new DataReader(dictb);
		int recordsb=datab.getRecords();

		DoubleMatrix2D matB=null;
		try
		{
			matB=DoubleFactory2D.dense.make(recordsb, totalvarb);
		}
		catch (Exception e)
		{
			matA=null;
			matB=null;
			String error=e.toString();
			if (error.startsWith("java.lang.IllegalArgumentException"))
				error="Error "+error.substring("java.lang.IllegalArgumentException".length());
			System.gc();
			error=error+"\n";
			return new Result("%579%<br>\n"+error+"<br>\n", false, null);
		}

		if (!datab.open(varb, replaceruleb, false))
			return new Result(datab.getmessage(), false, null);

		int pointer=0;
		while (!datab.isLast())
		{
			values = datab.getRecord();
			valdouble=new double[values.length];
			for (int i=0; i<values.length; i++)
			{
				if (values[i].equals(""))
				{
					matB=null;
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
					matB=null;
					System.gc();
					return new Result("%581%<br>\n", false, null);
				}
				matB.set(pointer, i, valdouble[i]);
			}
			pointer ++;
		}
		datab.close();

		DoubleMatrix2D matRES=null;
		int rowinv=0;
		int colinv=0;
		try
		{
			if ((recordsa==1) && (totalvara==1))
			{
				rowinv=recordsb;
				colinv=totalvarb;
			}
			else
			{
				Algebra algebra=new Algebra();
				matRES=algebra.mult(matA, matB);
				DoubleMatrix1D row=matRES.viewColumn(0);
				DoubleMatrix1D col=matRES.viewRow(0);
				double []rowd=row.toArray();
				double []cold=col.toArray();
				rowinv=rowd.length;
				colinv=cold.length;
				row=null;
				col=null;
				rowd=new double[0];
				cold=new double[0];
			}
		}
		catch (Exception e)
		{
			matA=null;
			matB=null;
			System.gc();
			String error=e.toString();
			if (error.startsWith("java.lang.IllegalArgumentException"))
				error="Error "+error.substring("java.lang.IllegalArgumentException".length());
			error=error+"\n";
			return new Result("%617%<br>\n"+error+"<br>\n", false, null);
		}

		DataSetUtilities dsu=new DataSetUtilities();

		Hashtable<String, String> temph=new Hashtable<String, String>();

		for (int i=0; i<colinv; i++)
		{
			dsu.addnewvar("v"+(String.valueOf(i)), "v"+(String.valueOf(i)), Keywords.NUMSuffix, temph, temph);
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		for (int i=0; i<rowinv; i++)
		{
			values=new String[colinv];
			if ((recordsa==1) && (totalvara==1))
			{
				for (int j=0; j<colinv; j++)
				{
					values[j]=String.valueOf(scalar*matB.get(i,j));
				}
			}
			else
			{
				for (int j=0; j<colinv; j++)
				{
					values[j]=String.valueOf(matRES.get(i,j));
				}
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
		parameters.add(new GetRequiredParameters(Keywords.dict+"a=", "dict", true, 619, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"b=", "dict", true, 620, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 440, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict+"a";
		parameters.add(new GetRequiredParameters(Keywords.var+"a", "vars=all", false, 621, dep, "", 2));
		dep = new String[1];
		dep[0]=Keywords.dict+"b";
		parameters.add(new GetRequiredParameters(Keywords.var+"b", "vars=all", false, 622, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.replace+"a", "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 623, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace+"b", "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 624, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="583";
		retprocinfo[1]="618";
		return retprocinfo;
	}
}
