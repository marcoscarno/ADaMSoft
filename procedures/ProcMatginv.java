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

import ADaMSoft.algorithms.VarGroupModalities;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.GroupedMatrix2Dfile;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.VariableUtilities;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.SingularValueDecomposition;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that evaluates the generalized inverse of a matrix
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcMatginv implements RunStep
{
	/**
	* Starts the execution of Proc Matginv and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict};
		String [] optionalparameters=new String[] {Keywords.var, Keywords.where, Keywords.todisk, Keywords.replace};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		boolean todisk=(parameters.get(Keywords.todisk)!=null);
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

		String keyword="Matginv "+dict.getkeyword();
		String description="Matginv "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		int totalvar=var.length;
		int[] replacerule=varu.getreplaceruleforsel(replace);

		DataReader data = new DataReader(dict);
		int records=data.getRecords();

		DataSetUtilities dsu=null;
		String where=(String)parameters.get(Keywords.where.toLowerCase());

		if (!todisk)
		{
			try
			{
				DoubleMatrix2D matrix=DoubleFactory2D.dense.make(records, totalvar);
				if (!data.open(var, replacerule, false))
					return new Result(data.getmessage(), false, null);
				if (where!=null)
				{
					if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
				}
				int pointer=0;
				int ncol=0;
				String[] values = null;
				double[] valdouble=null;
				while (!data.isLast())
				{
					values = data.getRecord();
					if (values!=null)
					{
						valdouble=new double[values.length];
						if (pointer==0)
							ncol=values.length;
						for (int i=0; i<values.length; i++)
						{
							if (values[i].equals(""))
							{
								data.close();
								matrix=null;
								System.gc();
								return new Result("%580% ("+pointer+"="+i+")\n", false, null);
							}
							try
							{
								valdouble[i]=Double.parseDouble(values[i]);
							}
							catch (Exception nonnumber)
							{
								data.close();
								matrix=null;
								System.gc();
								return new Result("%581% ("+pointer+"="+i+")\n", false, null);
							}
							matrix.set(pointer,i,valdouble[i]);
						}
						pointer ++;
					}
				}
				data.close();
				if ((pointer==0) && (where!=null))
					return new Result("%2804%<br>\n", false, null);
				if ((pointer==0) && (where==null))
					return new Result("%666%<br>\n", false, null);
				if (ncol!=pointer)
				{
					matrix=null;
					return new Result("%1581%<br>\n", false, null);
				}
				double tempdiff=Double.NaN;
				SingularValueDecomposition svd=new SingularValueDecomposition(matrix);
				DoubleMatrix2D matU=svd.getV();
				DoubleMatrix2D matV=svd.getU();
				DoubleMatrix2D matS=svd.getS();
				for (int i=0; i<ncol; i++)
				{
					if (matS.get(i, i)>0.000001)
					{
						matS.set(i,i, 1/matS.get(i, i));
					}
					else
						matS.set(i,i, 0);
				}
				dsu=new DataSetUtilities();
				Hashtable<String, String> temph=new Hashtable<String, String>();
				for (int i=0; i<ncol; i++)
				{
					dsu.addnewvar("v"+(String.valueOf(i)), "v"+(String.valueOf(i)), Keywords.NUMSuffix, temph, temph);
				}
				if (!dw.opendatatable(dsu.getfinalvarinfo()))
					return new Result(dw.getmessage(), false, null);
				values=new String[ncol];
				double vala=0;
				double valb=0;
				for (int a=0; a<ncol; a++)
				{
					for (int b=0; b<ncol; b++)
					{
						tempdiff=0;
						for (int i=0; i<ncol; i++)
						{
							vala=matV.get(a, i);
							valb=matS.get(b, i);
							tempdiff+=vala*valb;
						}
						matrix.set(a,b,tempdiff);
					}
				}
				matV=null;
				matS=null;
				for (int a=0; a<ncol; a++)
				{
					for (int b=0; b<ncol; b++)
					{
						tempdiff=0;
						for (int i=0; i<ncol; i++)
						{
							vala=matrix.get(a, i);
							valb=matU.get(b, i);
							tempdiff+=vala*valb;
						}
						values[b]=String.valueOf(tempdiff);
					}
					dw.write(values);
				}
				matrix=null;
				matU=null;
			}
			catch (Exception e)
			{
				System.gc();
				String error=e.toString();
				if (error.startsWith("java.lang.IllegalArgumentException"))
					error="Error "+error.substring("java.lang.IllegalArgumentException".length());
				error=error+"\n";
				return new Result("%1582%<br>\n"+error+"<br>\n", false, null);
			}
		}
		else
		{
			try
			{
				String tempdir=(String)parameters.get(Keywords.WorkDir);
				GroupedMatrix2Dfile matrix=new GroupedMatrix2Dfile(tempdir, totalvar);
				VarGroupModalities vgm=new VarGroupModalities();
				if (!data.open(var, replacerule, false))
					return new Result(data.getmessage(), false, null);
				if (where!=null)
				{
					if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
				}
				String[] values = null;
				double[] valdouble=null;
				Vector<String> nv=new Vector<String>();
				nv.add(null);
				vgm.updateModalities(nv);
				matrix.assignbasefile(nv);
				int pointer=0;
				int rifobs=0;
				while (!data.isLast())
				{
					values = data.getRecord();
					if (values!=null)
					{
						valdouble=new double[values.length];
						if (pointer==0)
							rifobs=values.length;
						for (int i=0; i<values.length; i++)
						{
							if (values[i].equals(""))
							{
								data.close();
								matrix.deassignbasefile();
								matrix.closeAll();
								System.gc();
								return new Result("%580% ("+pointer+"="+i+")\n", false, null);
							}
							try
							{
								valdouble[i]=Double.parseDouble(values[i]);
							}
							catch (Exception nonnumber)
							{
								data.close();
								matrix.deassignbasefile();
								matrix.closeAll();
								System.gc();
								return new Result("%581% ("+pointer+"="+i+")\n", false, null);
							}
						}
						matrix.write(nv,valdouble);
						pointer ++;
					}
				}
				data.close();
				if ((pointer==0) && (where!=null))
					return new Result("%2804%<br>\n", false, null);
				if ((pointer==0) && (where==null))
					return new Result("%666%<br>\n", false, null);
				if (rifobs!=pointer)
				{
					matrix.deassignbasefile();
					matrix.closeAll();
					return new Result("%1581%<br>\n", false, null);
				}
				vgm.calculate();
				ADaMSoft.algorithms.Algebra2DFile.SingularValueDecomposition svdf=new ADaMSoft.algorithms.Algebra2DFile.SingularValueDecomposition(tempdir, vgm);
				svdf.evaluate(matrix);
				if (svdf.getState())
				{
					matrix.deassignbasefile();
					matrix.closeAll();
					svdf.closeAll();
					return new Result(svdf.getMess(), false, null);
				}
				dsu=new DataSetUtilities();
				Hashtable<String, String> temph=new Hashtable<String, String>();
				for (int i=0; i<rifobs; i++)
				{
					dsu.addnewvar("v"+(String.valueOf(i)), "v"+(String.valueOf(i)), Keywords.NUMSuffix, temph, temph);
				}
				if (!dw.opendatatable(dsu.getfinalvarinfo()))
					return new Result(dw.getmessage(), false, null);
				GroupedMatrix2Dfile matU=svdf.getV();
				GroupedMatrix2Dfile matV=svdf.getU();
				GroupedMatrix2Dfile matS=svdf.gets();
				matU.assignbasefile(nv);
				matV.assignbasefile(nv);
				matS.assignbasefile(nv);
				double vala=0;
				double valb=0;
				double[] actualv=new double[rifobs];
				double tempdiff=0;
				values=new String[rifobs];
				for (int a=0; a<rifobs; a++)
				{
					for (int b=0; b<rifobs; b++)
					{
						actualv[b]=0;
						for (int i=0; i<rifobs; i++)
						{
							if (b==i)
							{
								vala=matV.read(nv, a, i);
								valb=matS.read(nv, b, 0);
								if (valb>0.000001)
									valb=1/valb;
								actualv[b]=actualv[b]+vala*valb;
							}
						}
					}
					for (int b=0; b<rifobs; b++)
					{
						tempdiff=0;
						for (int i=0; i<rifobs; i++)
						{
							vala=actualv[i];
							valb=matU.read(nv, b, i);
							tempdiff+=vala*valb;
						}
						values[b]=String.valueOf(tempdiff);
					}
					dw.write(values);
				}
				matU.deassignbasefile();
				matV.deassignbasefile();
				matS.deassignbasefile();
				matrix.deassignbasefile();
				matrix.closeAll();
				svdf.closeAll();
			}
			catch (Exception e)
			{
				System.gc();
				String error=e.toString();
				if (error.startsWith("java.lang.IllegalArgumentException"))
					error="Error "+error.substring("java.lang.IllegalArgumentException".length());
				error=error+"\n";
				return new Result("%1582%<br>\n"+error+"<br>\n", false, null);
			}
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
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 585, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 440, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", false, 586, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.todisk, "checkbox", false, 1088, dep, "", 2));
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
		retprocinfo[1]="1580";
		return retprocinfo;
	}
}
