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
import cern.colt.matrix.linalg.Property;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that evaluate several properties of a given matrix
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcMatproperties implements RunStep
{
	/**
	* Starts the execution of Proc Matproperties and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict};
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
		VariableUtilities varu=new VariableUtilities(dict, null, vartemp, null, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);
		String[] var=varu.getanalysisvar();

		String replace=(String)parameters.get(Keywords.replace);

		String keyword="Matproperties "+dict.getkeyword();
		String description="Matproperties "+dict.getdescription();
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
			return new Result("%2804%\n", false, null);
		if ((pointer==0) && (where==null))
			return new Result("%666%\n", false, null);
		double cond=Double.NaN;
		double det=Double.NaN;
		double norm1=Double.NaN;
		double norm2=Double.NaN;
		double normF=Double.NaN;
		double normInfinity=Double.NaN;
		double rank=Double.NaN;
		double trace=Double.NaN;
		double density=Double.NaN;
		String isDiagonal="-";
		String isDiagonallyDominantByColumn="-";
		String isDiagonallyDominantByRow="-";
		String isIdentity="-";
		String isLowerBidiagonal="-";
		String isLowerTriangular="-";
		String isNonNegative="-";
		String isOrthogonal="-";
		String isPositive="-";
		String isSingular="-";
		String isSkewSymmetric="-";
		String isSquare="-";
		String isStrictlyLowerTriangular="-";
		String isStrictlyTriangular="-";
		String isStrictlyUpperTriangular="-";
		String isSymmetric="-";
		String isTriangular="-";
		String isTridiagonal="-";
		String isUnitTriangular="-";
		String isUpperBidiagonal="-";
		String isUpperTriangular="-";
		String isZero="-";

		Algebra algebra=new Algebra();
		Property property=new Property(Property.DEFAULT.tolerance());
		try
		{
			cond=algebra.cond(matA);
		}
		catch (Exception e) {}
		try
		{
			det=algebra.det(matA);
		}
		catch (Exception e) {}
		try
		{
			norm1=algebra.norm1(matA);
		}
		catch (Exception e) {}
		try
		{
			norm2=algebra.norm2(matA);
		}
		catch (Exception e) {}
		try
		{
			normF=algebra.normF(matA);
		}
		catch (Exception e) {}
		try
		{
			normInfinity=algebra.normInfinity(matA);
		}
		catch (Exception e) {}
		try
		{
			rank=algebra.rank(matA);
		}
		catch (Exception e) {}
		try
		{
			trace=algebra.trace(matA);
		}
		catch (Exception e) {}
		try
		{
			density=property.density(matA);
		}
		catch (Exception e) {}
		try
		{
			boolean temp=property.isDiagonal(matA);
			isDiagonal=String.valueOf(temp);
		}
		catch (Exception e) {}
		try
		{
			boolean temp=property.isDiagonallyDominantByColumn(matA);
			isDiagonallyDominantByColumn=String.valueOf(temp);
		}
		catch (Exception e) {}
		try
		{
			boolean temp=property.isDiagonallyDominantByRow(matA);
			isDiagonallyDominantByRow=String.valueOf(temp);
		}
		catch (Exception e) {}
		try
		{
			boolean temp=property.isIdentity(matA);
			isIdentity=String.valueOf(temp);
		}
		catch (Exception e) {}
		try
		{
			boolean temp=property.isLowerBidiagonal(matA);
			isLowerBidiagonal=String.valueOf(temp);
		}
		catch (Exception e) {}
		try
		{
			boolean temp=property.isLowerTriangular(matA);
			isLowerTriangular=String.valueOf(temp);
		}
		catch (Exception e) {}
		try
		{
			boolean temp=property.isNonNegative(matA);
			isNonNegative=String.valueOf(temp);
		}
		catch (Exception e) {}
		try
		{
			boolean temp=property.isOrthogonal(matA);
			isOrthogonal=String.valueOf(temp);
		}
		catch (Exception e) {}
		try
		{
			boolean temp=property.isPositive(matA);
			isPositive=String.valueOf(temp);
		}
		catch (Exception e) {}
		try
		{
			boolean temp=property.isSingular(matA);
			isSingular=String.valueOf(temp);
		}
		catch (Exception e) {}
		try
		{
			boolean temp=property.isSkewSymmetric(matA);
			isSkewSymmetric=String.valueOf(temp);
		}
		catch (Exception e) {}
		try
		{
			boolean temp=property.isSquare(matA);
			isSquare=String.valueOf(temp);
		}
		catch (Exception e) {}
		try
		{
			boolean temp=property.isStrictlyLowerTriangular(matA);
			isStrictlyLowerTriangular=String.valueOf(temp);
		}
		catch (Exception e) {}
		try
		{
			boolean temp=property.isStrictlyTriangular(matA);
			isStrictlyTriangular=String.valueOf(temp);
		}
		catch (Exception e) {}
		try
		{
			boolean temp=property.isStrictlyUpperTriangular(matA);
			isStrictlyUpperTriangular=String.valueOf(temp);
		}
		catch (Exception e) {}
		try
		{
			boolean temp=property.isSymmetric(matA);
			isSymmetric=String.valueOf(temp);
		}
		catch (Exception e) {}
		try
		{
			boolean temp=property.isTriangular(matA);
			isTriangular=String.valueOf(temp);
		}
		catch (Exception e) {}
		try
		{
			boolean temp=property.isTridiagonal(matA);
			isTridiagonal=String.valueOf(temp);
		}
		catch (Exception e) {}
		try
		{
			boolean temp=property.isUnitTriangular(matA);
			isUnitTriangular=String.valueOf(temp);
		}
		catch (Exception e) {}
		try
		{
			boolean temp=property.isUpperBidiagonal(matA);
			isUpperBidiagonal=String.valueOf(temp);
		}
		catch (Exception e) {}
		try
		{
			boolean temp=property.isUpperTriangular(matA);
			isUpperTriangular=String.valueOf(temp);
		}
		catch (Exception e) {}
		try
		{
			boolean temp=property.isZero(matA);
			isZero=String.valueOf(temp);
		}
		catch (Exception e) {}

		DataSetUtilities dsu=new DataSetUtilities();

		Hashtable<String, String> temph=new Hashtable<String, String>();

		for (int i=0; i<2; i++)
		{
			if (i==0)
			{
				Hashtable<String, String> tempcodelabel=new Hashtable<String, String>();
				tempcodelabel.put("0","%1157%");
				tempcodelabel.put("1","%1161%");
				tempcodelabel.put("2","%1163%");
				tempcodelabel.put("3","%1164%");
				tempcodelabel.put("4","%1165%");
				tempcodelabel.put("5","%1166%");
				tempcodelabel.put("6","%1167%");
				tempcodelabel.put("7","%1168%");
				tempcodelabel.put("8","%1169%");
				tempcodelabel.put("9","%1170%");
				tempcodelabel.put("10","%1171%");
				tempcodelabel.put("11","%1172%");
				tempcodelabel.put("12","%1173%");
				tempcodelabel.put("13","%1174%");
				tempcodelabel.put("14","%1175%");
				tempcodelabel.put("15","%1176%");
				tempcodelabel.put("16","%1177%");
				tempcodelabel.put("17","%1178%");
				tempcodelabel.put("18","%1179%");
				tempcodelabel.put("19","%1180%");
				tempcodelabel.put("20","%1181%");
				tempcodelabel.put("21","%1182%");
				tempcodelabel.put("22","%1183%");
				tempcodelabel.put("23","%1184%");
				tempcodelabel.put("24","%1185%");
				tempcodelabel.put("25","%1186%");
				tempcodelabel.put("26","%1187%");
				tempcodelabel.put("27","%1188%");
				tempcodelabel.put("28","%1189%");
				tempcodelabel.put("29","%1190%");
				tempcodelabel.put("30","%1191%");
				dsu.addnewvar("v0", "%1192%", Keywords.TEXTSuffix, tempcodelabel, temph);
			}
			if (i==0)
			{
				Hashtable<String, String> tempcodelabel=new Hashtable<String, String>();
				tempcodelabel.put("-","%1193%");
				tempcodelabel.put("0","%363%");
				tempcodelabel.put("1","%362%");
				dsu.addnewvar("v1", "%368%", Keywords.TEXTSuffix, tempcodelabel, temph);
			}
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		String[] results=new String[2];
		results[0]="0";
		if (!Double.isNaN(cond))
			results[1]=String.valueOf(cond);
		else
			results[1]="-";
		dw.write(results);

		results[0]="1";
		if (!Double.isNaN(det))
			results[1]=String.valueOf(det);
		else
			results[1]="-";
		dw.write(results);

		results[0]="2";
		if (!Double.isNaN(norm1))
			results[1]=String.valueOf(norm1);
		else
			results[1]="-";
		dw.write(results);

		results[0]="3";
		if (!Double.isNaN(norm2))
			results[1]=String.valueOf(norm2);
		else
			results[1]="-";
		dw.write(results);

		results[0]="4";
		if (!Double.isNaN(normF))
			results[1]=String.valueOf(normF);
		else
			results[1]="-";
		dw.write(results);

		results[0]="5";
		if (!Double.isNaN(normInfinity))
			results[1]=String.valueOf(normInfinity);
		else
			results[1]="-";
		dw.write(results);

		results[0]="6";
		if (!Double.isNaN(rank))
			results[1]=String.valueOf(rank);
		else
			results[1]="-";
		dw.write(results);

		results[0]="7";
		if (!Double.isNaN(trace))
			results[1]=String.valueOf(trace);
		else
			results[1]="-";
		dw.write(results);

		results[0]="8";
		if (!Double.isNaN(density))
			results[1]=String.valueOf(density);
		else
			results[1]="-";
		dw.write(results);

		results[0]="9";
		if (isDiagonal.equalsIgnoreCase("True"))
			results[1]="1";
		else if (!isDiagonal.equalsIgnoreCase("-"))
			results[1]="0";
		else
			results[1]="-";
		dw.write(results);

		results[0]="10";
		if (isDiagonallyDominantByColumn.equalsIgnoreCase("True"))
			results[1]="1";
		else if (!isDiagonallyDominantByColumn.equalsIgnoreCase("-"))
			results[1]="0";
		else
			results[1]="-";
		dw.write(results);

		results[0]="11";
		if (isDiagonallyDominantByRow.equalsIgnoreCase("True"))
			results[1]="1";
		else if (!isDiagonallyDominantByRow.equalsIgnoreCase("-"))
			results[1]="0";
		else
			results[1]="-";
		dw.write(results);

		results[0]="12";
		if (isIdentity.equalsIgnoreCase("True"))
			results[1]="1";
		else if (!isIdentity.equalsIgnoreCase("-"))
			results[1]="0";
		else
			results[1]="-";
		dw.write(results);

		results[0]="13";
		if (isLowerBidiagonal.equalsIgnoreCase("True"))
			results[1]="1";
		else if (!isLowerBidiagonal.equalsIgnoreCase("-"))
			results[1]="0";
		else
			results[1]="-";
		dw.write(results);

		results[0]="14";
		if (isLowerTriangular.equalsIgnoreCase("True"))
			results[1]="1";
		else if (!isLowerTriangular.equalsIgnoreCase("-"))
			results[1]="0";
		else
			results[1]="-";
		dw.write(results);

		results[0]="15";
		if (isNonNegative.equalsIgnoreCase("True"))
			results[1]="1";
		else if (!isNonNegative.equalsIgnoreCase("-"))
			results[1]="0";
		else
			results[1]="-";
		dw.write(results);

		results[0]="16";
		if (isOrthogonal.equalsIgnoreCase("True"))
			results[1]="1";
		else if (!isOrthogonal.equalsIgnoreCase("-"))
			results[1]="0";
		else
			results[1]="-";
		dw.write(results);

		results[0]="17";
		if (isPositive.equalsIgnoreCase("True"))
			results[1]="1";
		else if (!isPositive.equalsIgnoreCase("-"))
			results[1]="0";
		else
			results[1]="-";
		dw.write(results);

		results[0]="18";
		if (isSingular.equalsIgnoreCase("True"))
			results[1]="1";
		else if (!isSingular.equalsIgnoreCase("-"))
			results[1]="0";
		else
			results[1]="-";
		dw.write(results);

		results[0]="19";
		if (isSkewSymmetric.equalsIgnoreCase("True"))
			results[1]="1";
		else if (!isSkewSymmetric.equalsIgnoreCase("-"))
			results[1]="0";
		else
			results[1]="-";
		dw.write(results);

		results[0]="20";
		if (isSquare.equalsIgnoreCase("True"))
			results[1]="1";
		else if (!isSquare.equalsIgnoreCase("-"))
			results[1]="0";
		else
			results[1]="-";
		dw.write(results);

		results[0]="21";
		if (isStrictlyLowerTriangular.equalsIgnoreCase("True"))
			results[1]="1";
		else if (!isStrictlyLowerTriangular.equalsIgnoreCase("-"))
			results[1]="0";
		else
			results[1]="-";
		dw.write(results);

		results[0]="22";
		if (isStrictlyTriangular.equalsIgnoreCase("True"))
			results[1]="1";
		else if (!isStrictlyTriangular.equalsIgnoreCase("-"))
			results[1]="0";
		else
			results[1]="-";
		dw.write(results);

		results[0]="23";
		if (isStrictlyUpperTriangular.equalsIgnoreCase("True"))
			results[1]="1";
		else if (!isStrictlyUpperTriangular.equalsIgnoreCase("-"))
			results[1]="0";
		else
			results[1]="-";
		dw.write(results);

		results[0]="24";
		if (isSymmetric.equalsIgnoreCase("True"))
			results[1]="1";
		else if (!isSymmetric.equalsIgnoreCase("-"))
			results[1]="0";
		else
			results[1]="-";
		dw.write(results);

		results[0]="25";
		if (isTriangular.equalsIgnoreCase("True"))
			results[1]="1";
		else if (!isTriangular.equalsIgnoreCase("-"))
			results[1]="0";
		else
			results[1]="-";
		dw.write(results);

		results[0]="26";
		if (isTridiagonal.equalsIgnoreCase("True"))
			results[1]="1";
		else if (!isTridiagonal.equalsIgnoreCase("-"))
			results[1]="0";
		else
			results[1]="-";
		dw.write(results);

		results[0]="27";
		if (isUnitTriangular.equalsIgnoreCase("True"))
			results[1]="1";
		else if (!isUnitTriangular.equalsIgnoreCase("-"))
			results[1]="0";
		else
			results[1]="-";
		dw.write(results);

		results[0]="28";
		if (isUpperBidiagonal.equalsIgnoreCase("True"))
			results[1]="1";
		else if (!isUpperBidiagonal.equalsIgnoreCase("-"))
			results[1]="0";
		else
			results[1]="-";
		dw.write(results);

		results[0]="29";
		if (isUpperTriangular.equalsIgnoreCase("True"))
			results[1]="1";
		else if (!isUpperTriangular.equalsIgnoreCase("-"))
			results[1]="0";
		else
			results[1]="-";
		dw.write(results);

		results[0]="30";
		if (isZero.equalsIgnoreCase("True"))
			results[1]="1";
		else if (!isZero.equalsIgnoreCase("-"))
			results[1]="0";
		else
			results[1]="-";
		dw.write(results);

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
		retprocinfo[1]="633";
		return retprocinfo;
	}
}
