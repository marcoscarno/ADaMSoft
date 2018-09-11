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
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.SortRequestedVar;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that evaluates the predicted value for a variable using a linear regression model
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcFuzzyregeval extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Fuzzyregeval
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean novgconvert=false;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.dict+"e"};
		String [] optionalparameters=new String[] {Keywords.replace, Keywords.where, Keywords.novgconvert};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		String replace =(String)parameters.get(Keywords.replace);

		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		DictionaryReader dicte = (DictionaryReader)parameters.get(Keywords.dict+"e");
		novgconvert =(parameters.get(Keywords.novgconvert)!=null);

		int numvar=dicte.gettotalvar();
		String var="";
		boolean iscorrect=true;
		for (int i=0; i<numvar; i++)
		{
			String tempname=dicte.getvarname(i);
			if ((tempname.toLowerCase()).startsWith("g_"))
			{
				try
				{
					String tc=tempname.substring(2);
					var=var+" "+tc;
				}
				catch (Exception ec)
				{
					iscorrect=false;
				}
			}
		}
		if (!iscorrect)
			return new Result("%1328%<br>\n", false, null);

		String groupname=var.trim();

		var=var+" ref value";
		var=var.trim();

		String[] groupvar=new String[0];
		if (!groupname.equals(""))
			groupvar=groupname.split(" ");

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		String[] alldsvars=new String[dict.gettotalvar()];
		for (int i=0; i<dict.gettotalvar(); i++)
		{
			alldsvars[i]=dict.getvarname(i);
		}

		groupvar=SortRequestedVar.getreqsorted(groupvar, alldsvars);

		String[] vartoread=new String[groupvar.length+2];
		for (int i=0; i<groupvar.length; i++)
		{
			vartoread[i]="g_"+groupvar[i];
		}
		vartoread[groupvar.length]="ref";
		vartoread[groupvar.length+1]="value";

		int[] replacerule=new int[vartoread.length];
		for (int i=0; i<vartoread.length; i++)
		{
			replacerule[i]=0;
		}

		DataReader datae = new DataReader(dicte);
		if (!datae.open(vartoread, replacerule, false))
			return new Result(datae.getmessage(), false, null);

		Hashtable<Vector<String>, Hashtable<String, Double>> tempparameters=new Hashtable<Vector<String>, Hashtable<String, Double>>();
		Hashtable<Vector<String>, Double> par1=new Hashtable<Vector<String>, Double>();
		Hashtable<Vector<String>, Double> par2=new Hashtable<Vector<String>, Double>();
		Hashtable<Vector<String>, Double> par3=new Hashtable<Vector<String>, Double>();
		Hashtable<Vector<String>, Double> par4=new Hashtable<Vector<String>, Double>();
		Hashtable<Vector<String>, Double> par5=new Hashtable<Vector<String>, Double>();
		Hashtable<Vector<String>, double[]> parameter=new Hashtable<Vector<String>, double[]>();

		Hashtable<String, Integer> tempvarname=new Hashtable<String, Integer>();

		while (!datae.isLast())
		{
			String[] values = datae.getRecord();
			Vector<String> groupval=new Vector<String>();
			if (groupvar.length==0)
				groupval.add(null);
			else
			{
				for (int i=0; i<groupvar.length; i++)
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
			try
			{
				String tempname=values[groupvar.length].toLowerCase();
				if (tempname.equalsIgnoreCase("0"))
				{
					try
					{
						double temppara=Double.valueOf(values[groupvar.length+1]);
						par1.put(groupval, new Double(temppara));
					}
					catch (Exception e)
					{
						iscorrect=false;
					}
				}
				else if (tempname.equalsIgnoreCase("1"))
				{
					try
					{
						double temppara=Double.valueOf(values[groupvar.length+1]);
						par2.put(groupval, new Double(temppara));
					}
					catch (Exception e)
					{
						iscorrect=false;
					}
				}
				else if (tempname.equalsIgnoreCase("2"))
				{
					try
					{
						double temppara=Double.valueOf(values[groupvar.length+1]);
						par3.put(groupval, new Double(temppara));
					}
					catch (Exception e)
					{
						iscorrect=false;
					}
				}
				else if (tempname.equalsIgnoreCase("3"))
				{
					try
					{
						double temppara=Double.valueOf(values[groupvar.length+1]);
						par4.put(groupval, new Double(temppara));
					}
					catch (Exception e)
					{
						iscorrect=false;
					}
				}
				else if (tempname.equalsIgnoreCase("4"))
				{
					try
					{
						double temppara=Double.valueOf(values[groupvar.length+1]);
						par5.put(groupval, new Double(temppara));
					}
					catch (Exception e)
					{
						iscorrect=false;
					}
				}
				else
				{
					Hashtable<String, Double> tempp=tempparameters.get(groupval);
					if (tempp==null)
						tempp=new Hashtable<String, Double>();
					try
					{
						String[] tempn=tempname.split("_");
						double temppara=Double.valueOf(values[groupvar.length+1]);
						tempp.put(tempn[1].toLowerCase(), new Double(temppara));
						tempparameters.put(groupval, tempp);
						tempvarname.put(tempn[1], new Integer(tempvarname.size()));
					}
					catch (Exception e)
					{
						iscorrect=false;
					}

				}
			}
			catch (Exception e)
			{
				iscorrect=true;
			}
		}
		datae.close();
		if (!iscorrect)
			return new Result("%1328%<br>\n", false, null);

		int coeffsize=tempvarname.size();

		String[] usedvarnames=new String[tempvarname.size()];
		int inlength=0;
		for (Enumeration<String> es = tempvarname.keys() ; es.hasMoreElements() ;)
		{
			String tempvname=es.nextElement();
			int varposition=(tempvarname.get(tempvname)).intValue();
			usedvarnames[varposition]=tempvname;
			inlength++;
		}

		String varx="";
		for (int i=0; i<usedvarnames.length; i++)
		{
			varx=varx+" "+usedvarnames[i];
		}
		varx=varx.trim();

		for (Enumeration<Vector<String>> e = tempparameters.keys() ; e.hasMoreElements() ;)
		{
			Vector<String> gv= e.nextElement();
			Hashtable<String, Double> actualpar=tempparameters.get(gv);
			int rif=0;
			double[] par=new double[coeffsize];
			for (int i=0; i<usedvarnames.length; i++)
			{
				double temppar=(actualpar.get(usedvarnames[i].toLowerCase())).doubleValue();
				par[rif]=temppar;
				rif++;
			}
			parameter.put(gv, par);
		}

		String keyword="FuzzyRegEval "+dict.getkeyword();
		String description="FuzzyRegEval "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		DataSetUtilities dsu=new DataSetUtilities();

		dsu.setreplace(replace);

		Hashtable<String, String> temph=new Hashtable<String, String>();

		dsu.defineolddict(dict);

		numvar=dict.gettotalvar();

		dsu.addnewvartoolddict("esc", "%1329%", Keywords.NUMSuffix, temph, temph);
		dsu.addnewvartoolddict("esls", "%1330%", Keywords.NUMSuffix, temph, temph);
		dsu.addnewvartoolddict("esrs", "%1331%", Keywords.NUMSuffix, temph, temph);

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

		if (groupname.equals(""))
			groupname=null;

		int outlength=3;
		VariableUtilities varu=new VariableUtilities(dict, groupname, varx, null, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String[] totalvar=varu.getallvar();

		int[] replacer=varu.getreplaceruleforall(replace);

		DataReader data = new DataReader(dict);
		if (!data.open(totalvar, replacer, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		int[] allvarstype=varu.getnormalruleforall();

		ValuesParser vp=new ValuesParser(allvarstype, null, null, null, null, null);

		int validgroup=0;
		boolean noterror=true;
		String[] values = null;
		Vector<String> vargroupvalues=new Vector<String>();
		double[] realval=null;
		String[] newvalues=new String[outlength];

		double[] par=null;
		double intercept=Double.NaN;
		double bv=Double.NaN;
		double dv=Double.NaN;
		double gv=Double.NaN;
		double hv=Double.NaN;
		double center=0;
		double left=0;
		double right=0;
		int outpointer=0;
		boolean isnull=false;
		String[] wvalues=null;

		while ((!data.isLast()) && (noterror))
		{
			values = data.getRecord();
			if (values!=null)
			{
				if (novgconvert)
					vargroupvalues=vp.getorigvargroup(values);
				else
					vargroupvalues=vp.getvargroup(values);
				realval=vp.getanalysisvarasdouble(values);
				for (int i=0; i<newvalues.length; i++)
				{
					newvalues[i]="";
				}
				if (vp.vargroupisnotmissing(vargroupvalues))
				{
					validgroup++;
					par=parameter.get(vargroupvalues);
					intercept=(par1.get(vargroupvalues)).doubleValue();
					bv=(par2.get(vargroupvalues)).doubleValue();
					dv=(par3.get(vargroupvalues)).doubleValue();
					gv=(par4.get(vargroupvalues)).doubleValue();
					hv=(par5.get(vargroupvalues)).doubleValue();
					center=0;
					left=0;
					right=0;
					outpointer=0;
					isnull=false;
					for (int i=0; i<inlength; i++)
					{
						if (!Double.isNaN(realval[i]))
							center=center+realval[i]*par[i];
						else
							isnull=true;
					}
					center=center+intercept;
					if(Double.isNaN(center))
						isnull=true;
					if (!isnull)
					{
						left=center*bv+dv;
						right=center*gv+hv;
						try
						{
							newvalues[outpointer]=double2String(center);
							outpointer++;
						}
						catch (Exception e) {}
						try
						{
							newvalues[outpointer]=double2String(left);
							outpointer++;
						}
						catch (Exception e) {}
						try
						{
							newvalues[outpointer]=double2String(right);
						}
						catch (Exception e) {}
					}
				}
				wvalues=dsu.getnewvalues(values, newvalues);
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
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 1332, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"e=", "dict", true, 1333, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 1334, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
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
		retprocinfo[0]="1312";
		retprocinfo[1]="1327";
		return retprocinfo;
	}
}
