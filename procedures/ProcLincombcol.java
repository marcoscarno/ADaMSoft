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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;

/**
* This is the procedure that evaluates a new variable that is a linear combination of one or more variables (the coefficients are on several variables)
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcLincombcol extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Lincombcol
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean novgconvert=false;
		String [] requiredparameters=new String[] {Keywords.varcoeffval, Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.dict+"c"};
		String [] optionalparameters=new String[] {Keywords.vargroup, Keywords.where, Keywords.replace, Keywords.novgconvert};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		novgconvert =(parameters.get(Keywords.novgconvert)!=null);

		String replace =(String)parameters.get(Keywords.replace);

		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		DictionaryReader dicte = (DictionaryReader)parameters.get(Keywords.dict+"c");

		String vartempval=(String)parameters.get(Keywords.varcoeffval.toLowerCase());
		String vargroup=(String)parameters.get(Keywords.vargroup.toLowerCase());

		String[] testvarval=vartempval.split(" ");

		String[] testvargroup=null;
		int ng=0;
		if (vargroup!=null)
		{
			testvargroup=vargroup.split(" ");
			ng=testvargroup.length;
		}

		Hashtable<Vector<String>, Hashtable<Integer, Double>> coeffval=new Hashtable<Vector<String>, Hashtable<Integer, Double>>();

		String[] vartoread=new String[testvarval.length+ng];
		int[] replacerule=new int[testvarval.length+ng];

		if (testvargroup!=null)
		{
			for (int i=0; i<ng; i++)
			{
				vartoread[i]=testvargroup[i];
				replacerule[i]=1;
			}
		}
		for (int i=0; i<testvarval.length; i++)
		{
			vartoread[ng+i]=testvarval[i];
			replacerule[ng+i]=0;
		}

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		int totaldictvars=dict.gettotalvar();

		int rifrep=0;
		if (replace==null)
			rifrep=0;
		else if (replace.equalsIgnoreCase(Keywords.replaceall))
			rifrep=1;
		else if (replace.equalsIgnoreCase(Keywords.replaceformat))
			rifrep=2;
		else if (replace.equalsIgnoreCase(Keywords.replacemissing))
			rifrep=3;

		String[] alldsvars=new String[totaldictvars];
		int[] replaceruledict=new int[totaldictvars];
		int[] vgpos=null;
		if (ng!=0)
		{
			vgpos=new int[ng];
			for (int j=0; j<testvargroup.length; j++)
			{
				for (int i=0; i<totaldictvars; i++)
				{
					if (testvargroup[j].equalsIgnoreCase(dict.getvarname(i)))
						vgpos[j]=i;
				}
			}
		}

		for (int i=0; i<totaldictvars; i++)
		{
			alldsvars[i]=dict.getvarname(i);
			replaceruledict[i]=rifrep;
			if (ng!=0)
			{
				for (int j=0; j<testvargroup.length; j++)
				{
					if (alldsvars[i].equalsIgnoreCase(testvargroup[j]))
					{
						replaceruledict[i]=1;
						break;
					}
				}
			}
		}

		DataReader datae = new DataReader(dicte);
		if (!datae.open(vartoread, replacerule, false))
			return new Result(datae.getmessage(), false, null);

		while (!datae.isLast())
		{
			String[] values = datae.getRecord();
			Vector<String> groupval=new Vector<String>();
			if (ng==0)
				groupval.add(null);
			else
			{
				for (int i=0; i<ng; i++)
				{
					String realvgval=values[i].trim();
					if (!novgconvert)
					{
						try
						{
							double realnumvgval=Double.parseDouble(realvgval);
							if (!Double.isNaN(realnumvgval))
								realvgval=String.valueOf(realnumvgval);
						}
						catch (Exception e) {}
					}
					groupval.add(realvgval);
				}
			}
			for (int k=0; k<testvarval.length; k++)
			{
				Hashtable<Integer, Double> actcoeff=coeffval.get(groupval);
				if (actcoeff==null)
				{
					actcoeff=new Hashtable<Integer, Double>();
				}
				double temppara=Double.NaN;
				try
				{
					temppara=Double.valueOf(values[ng+k]);
				}
				catch(Exception e) {}
				int rifvarname=0;
				for (int i=0; i<totaldictvars; i++)
				{
					if (testvarval[k].equalsIgnoreCase(alldsvars[i]))
					{
						rifvarname=i;
						break;
					}
				}
				actcoeff.put(new Integer(rifvarname), new Double(temppara));
				coeffval.put(groupval, actcoeff);
			}
		}
		datae.close();

		String keyword="Lincombcol "+dict.getkeyword();
		String description="Lincombcol "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);
		Hashtable<String, String> temph=new Hashtable<String, String>();

		dsu.defineolddict(dict);

		dsu.addnewvartoolddict("lincomb", "%1949%", Keywords.NUMSuffix, temph, temph);

		if (replace!=null)
		{
			if (replace.equalsIgnoreCase(Keywords.replaceall))
			{
				dsu.setempycodelabels();
				dsu.setempymissingdata();
			}
			else if (replace.equalsIgnoreCase(Keywords.replaceformat))
				dsu.setempycodelabels();
			else if (replace.equalsIgnoreCase(Keywords.replacemissing))
				dsu.setempymissingdata();
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);


		DataReader data = new DataReader(dict);
		if (!data.open(alldsvars, replaceruledict, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		boolean vgmiss=false;
		double predictedval=0;
		Vector<String> groupval=new Vector<String>();
		int validgroup=0;
		while (!data.isLast())
		{
			groupval.clear();
			String[] values = data.getRecord();
			if (values!=null)
			{
				validgroup++;
				String[] newvalues=new String[1];
				newvalues[0]="";
				vgmiss=false;
				predictedval=Double.NaN;
				if (ng==0)
					groupval.add(null);
				else
				{
					for (int i=0; i<ng; i++)
					{
						String realvgval=values[vgpos[i]].trim();
						if (realvgval.equals(""))
							vgmiss=true;
						if (!novgconvert)
						{
							try
							{
								double realnumvgval=Double.parseDouble(realvgval);
								if (!Double.isNaN(realnumvgval))
									realvgval=String.valueOf(realnumvgval);
							}
							catch (Exception e) {}
						}
						groupval.add(realvgval);
					}
				}
				if (!vgmiss)
				{
					Hashtable<Integer, Double> actcoeff=coeffval.get(groupval);
					if (actcoeff!=null)
					{
						predictedval=0;
						for (Enumeration<Integer> e = actcoeff.keys() ; e.hasMoreElements() ;)
						{
							Integer ai=e.nextElement();
							int posref=ai.intValue();
							double coeffv=(actcoeff.get(ai)).doubleValue();
							try
							{
								predictedval=predictedval+Double.parseDouble(values[posref])*coeffv;
							}
							catch (Exception ex)
							{
								predictedval=Double.NaN;
							}
						}
					}
				}
				try
				{
					newvalues[0]=double2String(predictedval);
				}
				catch (Exception e) {}
				String[] wvalues=dsu.getnewvalues(values, newvalues);
				dw.write(wvalues);
			}
		}
		data.close();
		if ((validgroup==0) && (where!=null))
		{
			dw.deletetmp();
			return new Result("%2804%<br>\n", false, null);
		}
		if ((validgroup==0) && (where==null))
		{
			dw.deletetmp();
			return new Result("%666%<br>\n", false, null);
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
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 1941, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"c=", "dict", true, 1951, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 1943, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict+"c";
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varcoeffval, "vars=all", true, 1952, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.novgconvert, "checkbox", false, 2227, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="4163";
		retprocinfo[1]="1950";
		return retprocinfo;
	}
}
