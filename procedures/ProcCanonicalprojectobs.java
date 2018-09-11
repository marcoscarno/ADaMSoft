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
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.SortRequestedVar;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that project the observation into the canonical space
* @author marco.scarno@gmail.com
* @version 1.0.0, rev.: 13/02/17 by marco
*/
public class ProcCanonicalprojectobs extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Canonicalprojectobs
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean usecov=false;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict+"c", Keywords.dict+"s", Keywords.dict};
		String [] optionalparameters=new String[] {Keywords.replace, Keywords.ncomp, Keywords.novgconvert, Keywords.where};
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

		boolean novgconvert=false;
		novgconvert =(parameters.get(Keywords.novgconvert)!=null);

		DictionaryReader dicts = (DictionaryReader)parameters.get(Keywords.dict+"s");
		DictionaryReader dictc = (DictionaryReader)parameters.get(Keywords.dict+"c");
		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		int numvarc=dictc.gettotalvar();
		int totalcomponent=0;
		String nameofvarsforc="";
		boolean iscorrectc=true;
		int ngroupc=0;
		for (int i=0; i<numvarc; i++)
		{
			String tempname=dictc.getvarname(i);
			if ((tempname.toLowerCase()).startsWith("c_"))
			{
				try
				{
					String[] tc=tempname.split("_");
					int rif=Integer.parseInt(tc[1]);
					if (rif>totalcomponent)
						totalcomponent=rif;
				}
				catch (Exception ec)
				{
					iscorrectc=false;
				}
			}
			else if (!tempname.equalsIgnoreCase("Type"))
			{
				if (tempname.toLowerCase().startsWith("g_"))
				{
					try
					{
						tempname=tempname.substring(2);
						nameofvarsforc=nameofvarsforc+tempname.toLowerCase()+" ";
						ngroupc++;
					}
					catch (Exception ec)
					{
						iscorrectc=false;
					}
				}
				else
					iscorrectc=false;
			}
		}
		if (!iscorrectc)
			return new Result("%752%<br>\n", false, null);

		nameofvarsforc=nameofvarsforc.trim();
		String[] groupnameforc=new String[0];
		if (!nameofvarsforc.equals(""))
			groupnameforc=nameofvarsforc.split(" ");

		String[] alldsvars=new String[dict.gettotalvar()];
		for (int i=0; i<dict.gettotalvar(); i++)
		{
			alldsvars[i]=dict.getvarname(i);
		}
		groupnameforc=SortRequestedVar.getreqsorted(groupnameforc, alldsvars);

		if (totalcomponent==0)
			return new Result("%752%<br>\n", false, null);

		String tempncomp=(String)parameters.get(Keywords.ncomp);
		if (tempncomp==null)
			tempncomp=String.valueOf(totalcomponent);

		int ncomp=0;
		try
		{
			ncomp=Integer.parseInt(tempncomp);
		}
		catch (Exception e)
		{
			return new Result("%756%<br>\n", false, null);
		}
		if (totalcomponent<ncomp)
			return new Result("%757%<br>\n", false, null);

		totalcomponent=ncomp;

		int numvars=dicts.gettotalvar();
		int ngroups=0;
		boolean iscorrects=true;
		boolean isstat=false;
		Hashtable<String, String> hasgroupnamefors=new Hashtable<String, String>();
		String usedvarnamesx="";
		String usedvarnamesy="";
		for (int i=0; i<numvars; i++)
		{
			String tempname=dicts.getvarname(i);
			if ((tempname.toLowerCase()).startsWith("g_"))
			{
				try
				{
					tempname=tempname.substring(2);
					hasgroupnamefors.put(tempname.toLowerCase(),"");
					ngroups++;
				}
				catch (Exception ec)
				{
					iscorrects=false;
				}
			}
			else if (!tempname.equalsIgnoreCase("Statistic"))
			{
				if (tempname.startsWith("vx_"))
				{
					try
					{
						tempname=tempname.substring(3);
						usedvarnamesx=usedvarnamesx+tempname+" ";
					}
					catch (Exception ec)
					{
						iscorrects=false;
					}
				}
				if (tempname.startsWith("vy_"))
				{
					try
					{
						tempname=tempname.substring(3);
						usedvarnamesy=usedvarnamesy+tempname+" ";
					}
					catch (Exception ec)
					{
						iscorrects=false;
					}
				}
			}
			else if (tempname.equalsIgnoreCase("Statistic"))
				isstat=true;
		}
		if (!iscorrects)
			return new Result("%753%<br>\n", false, null);

		if (!isstat)
			return new Result("%753%<br>\n", false, null);

		usedvarnamesx=usedvarnamesx.trim();
		usedvarnamesy=usedvarnamesy.trim();

		if (ngroups!=ngroupc)
			return new Result("%758%<br>\n", false, null);

		boolean vgexist=true;
		for (int i=0; i<groupnameforc.length; i++)
		{
			String test=hasgroupnamefors.get(groupnameforc[i]);
			if (test==null)
				vgexist=false;
		}
		if (!vgexist)
			return new Result("%758%<br>\n", false, null);

		String[] usedvarx=usedvarnamesx.split(" ");
		String[] usedvary=usedvarnamesy.split(" ");
		String[] vartoreadins=new String[groupnameforc.length+1+usedvarx.length+usedvary.length];
		for (int i=0; i<groupnameforc.length; i++)
		{
			vartoreadins[i]="g_"+groupnameforc[i];
		}
		vartoreadins[groupnameforc.length]="statistic";
		for (int i=0; i<usedvarx.length; i++)
		{
			vartoreadins[groupnameforc.length+1+i]="vx_"+usedvarx[i];
		}
		for (int i=0; i<usedvary.length; i++)
		{
			vartoreadins[groupnameforc.length+1+i+usedvarx.length]="vy_"+usedvary[i];
		}
		int[] replacerulefors=new int[vartoreadins.length];
		for (int i=0; i<vartoreadins.length; i++)
		{
			replacerulefors[i]=0;
		}

		DataReader datas = new DataReader(dicts);
		if (!datas.open(vartoreadins, replacerulefors, false))
			return new Result(datas.getmessage(), false, null);

		Hashtable<Vector<String>, double[]> meanx=new Hashtable<Vector<String>, double[]>();
		Hashtable<Vector<String>, double[]> stdx=new Hashtable<Vector<String>, double[]>();
		Hashtable<Vector<String>, double[]> meany=new Hashtable<Vector<String>, double[]>();
		Hashtable<Vector<String>, double[]> stdy=new Hashtable<Vector<String>, double[]>();
		int validgroup=0;
		while (!datas.isLast())
		{
			String[] values = datas.getRecord();
			Vector<String> groupval=new Vector<String>();
			if (groupnameforc.length==0)
				groupval.add(null);
			else
			{
				for (int i=0; i<groupnameforc.length; i++)
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
			String type=values[groupnameforc.length];
			if (type.equals("1"))
			{
				double[] meanvalx=new double[values.length-groupnameforc.length-1-usedvary.length];
				double[] meanvaly=new double[values.length-groupnameforc.length-1-usedvarx.length];
				for (int i=groupnameforc.length+1; i<(values.length-usedvary.length); i++)
				{
					meanvalx[i-groupnameforc.length-1]=Double.NaN;
					try
					{
						meanvalx[i-groupnameforc.length-1]=Double.parseDouble(values[i]);
					}
					catch (Exception nfe)
					{
						meanvalx[i-groupnameforc.length-1]=Double.NaN;
					}
				}
				meanx.put(groupval, meanvalx);
				for (int i=groupnameforc.length+1+usedvarx.length; i<values.length; i++)
				{
					meanvaly[i-groupnameforc.length-1-usedvarx.length]=Double.NaN;
					try
					{
						meanvaly[i-groupnameforc.length-1-usedvarx.length]=Double.parseDouble(values[i]);
					}
					catch (Exception nfe)
					{
						meanvaly[i-groupnameforc.length-1-usedvarx.length]=Double.NaN;
					}
				}
				meany.put(groupval, meanvalx);
			}
			if (type.equals("2"))
			{
				double[] stdvalx=new double[values.length-groupnameforc.length-1-usedvary.length];
				double[] stdvaly=new double[values.length-groupnameforc.length-1-usedvarx.length];
				for (int i=groupnameforc.length+1; i<(values.length-usedvary.length); i++)
				{
					stdvalx[i-groupnameforc.length-1]=Double.NaN;
					try
					{
						stdvalx[i-groupnameforc.length-1]=Double.parseDouble(values[i]);
					}
					catch (Exception nfe)
					{
						stdvalx[i-groupnameforc.length-1]=Double.NaN;
					}
				}
				stdx.put(groupval, stdvalx);
				for (int i=groupnameforc.length+1+usedvarx.length; i<values.length; i++)
				{
					stdvaly[i-groupnameforc.length-1-usedvarx.length]=Double.NaN;
					try
					{
						stdvaly[i-groupnameforc.length-1-usedvarx.length]=Double.parseDouble(values[i]);
					}
					catch (Exception nfe)
					{
						stdvaly[i-groupnameforc.length-1-usedvarx.length]=Double.NaN;
					}
				}
				stdy.put(groupval, stdvaly);
			}
		}
		datas.close();

		Hashtable<String, String> tinfo=dictc.getdatatableinfo();
		boolean dsok=false;
		for (Enumeration<String> e = tinfo.keys() ; e.hasMoreElements() ;)
		{
			String par = e.nextElement();
			String val = tinfo.get(par);
			if (par.equalsIgnoreCase("matrix"))
			{
				dsok=true;
				if (val.equalsIgnoreCase("corr"))
					usecov=false;
			}
		}
		if (!dsok)
			return new Result("%752%<br>\n", false, null);

		String[] vartoreadinc=new String[groupnameforc.length+1+totalcomponent];
		for (int i=0; i<groupnameforc.length; i++)
		{
			vartoreadinc[i]="g_"+groupnameforc[i];
		}
		vartoreadinc[groupnameforc.length]="type";
		for (int i=0; i<totalcomponent; i++)
		{
			vartoreadinc[groupnameforc.length+1+i]="c_"+String.valueOf(i+1);
		}
		int[] replaceruleforc=new int[vartoreadinc.length];
		for (int i=0; i<vartoreadinc.length; i++)
		{
			replaceruleforc[i]=0;
		}

		DataReader datac = new DataReader(dictc);
		if (!datac.open(vartoreadinc, replaceruleforc, false))
			return new Result(datac.getmessage(), false, null);

		Hashtable<Hashtable<Vector<String>, String>, double[]> eigenvx=new Hashtable<Hashtable<Vector<String>, String>, double[]>();
		Hashtable<Hashtable<Vector<String>, String>, double[]> eigenvy=new Hashtable<Hashtable<Vector<String>, String>, double[]>();
		while (!datac.isLast())
		{
			String[] values = datac.getRecord();
			Vector<String> groupval=new Vector<String>();
			if (groupnameforc.length==0)
				groupval.add(null);
			else
			{
				for (int i=0; i<groupnameforc.length; i++)
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
			String type=values[groupnameforc.length];
			if ((!type.equals("1")) && (!type.equals("2")))
			{
				double[] compval=new double[values.length-groupnameforc.length-1];
				for (int i=groupnameforc.length+1; i<values.length; i++)
				{
					compval[i-groupnameforc.length-1]=Double.NaN;
					try
					{
						compval[i-groupnameforc.length-1]=Double.parseDouble(values[i]);
					}
					catch (Exception nfe)
					{
						compval[i-groupnameforc.length-1]=Double.NaN;
					}
				}
				Hashtable<Vector<String>, String> t=new Hashtable<Vector<String>, String>();
				boolean isvarx=false;
				for (int i=0; i<usedvarx.length; i++)
				{
					if (type.equalsIgnoreCase(usedvarx[i]))
						isvarx=true;
				}
				t.put(groupval, type.toLowerCase());
				if (isvarx)
					eigenvx.put(t, compval);
				else
					eigenvy.put(t, compval);
			}
		}
		datac.close();

		String keyword="CanonicalProjectobs "+dict.getkeyword();
		String description="CanonicalProjectobs "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);
		Hashtable<String, String> temph=new Hashtable<String, String>();

		dsu.defineolddict(dict);

		for (int i=0; i<ncomp; i++)
		{
			dsu.addnewvartoolddict("XComponent_"+(String.valueOf(i)), "%1882% "+(String.valueOf(i+1)), Keywords.NUMSuffix, temph, temph);
		}
		for (int i=0; i<ncomp; i++)
		{
			dsu.addnewvartoolddict("YComponent_"+(String.valueOf(i)), "%1883% "+(String.valueOf(i+1)), Keywords.NUMSuffix, temph, temph);
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		String vargroup="";
		for (int i=0; i<groupnameforc.length; i++)
		{
			vargroup=vargroup+groupnameforc[i]+" ";
		}

		vargroup=vargroup.trim();

		if (vargroup.equals(""))
			vargroup=null;

		VariableUtilities varu=new VariableUtilities(dict, vargroup, usedvarnamesx+" "+usedvarnamesy, null, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String[] var=varu.getanalysisvar();
		String[] totalvar=varu.getallvar();

		int[] replacerule=varu.getreplaceruleforall(replace);

		DataReader data = new DataReader(dict);
		if (!data.open(totalvar, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		int[] allvarstype=varu.getnormalruleforall();

		ValuesParser vp=new ValuesParser(allvarstype, null, null, null, null, null);

		validgroup=0;
		boolean grouperror=false;
		double[] realvalx=new double[usedvarx.length];
		double[] realvaly=new double[usedvary.length];
		Vector<String> vargroupvalues=null;
		while ((!data.isLast()) && (!grouperror))
		{
			String[] values = data.getRecord();
			if (values!=null)
			{
				if (novgconvert)
					vargroupvalues=vp.getorigvargroup(values);
				else
					vargroupvalues=vp.getvargroup(values);
				double[] realval=vp.getanalysisvarasdouble(values);
				double[] projectionx=new double[totalcomponent];
				double[] projectiony=new double[totalcomponent];
				for (int i=0; i<totalcomponent; i++)
				{
					projectionx[i]=Double.NaN;
					projectiony[i]=Double.NaN;
				}
				if (vp.vargroupisnotmissing(vargroupvalues))
				{
					Vector<String> exgroupc=new Vector<String>();
					Vector<String> exgroups=new Vector<String>();
					for (int i=0; i<vargroupvalues.size(); i++)
					{
						if (vargroupvalues.get(i)!=null)
						{
							String grouptosearch=(vargroupvalues.get(i)).trim();
							exgroupc.add(grouptosearch);
							exgroups.add(grouptosearch);
						}
						else
						{
							exgroupc.add(null);
							exgroups.add(null);
						}
					}
					validgroup++;
					grouperror =(meanx.get(exgroups)==null);
					if (!grouperror)
					{
						double[] actualmeanx=meanx.get(exgroups);
						double[] actualstdx=stdx.get(exgroups);

						double[] actualmeany=meany.get(exgroups);
						double[] actualstdy=stdy.get(exgroups);

						boolean ismd=false;
						for (int i=0; i<totalcomponent; i++)
						{
							projectionx[i]=0;
							projectiony[i]=0;
						}
						int pointx=0;
						int pointy=0;
						for (int i=0; i<realval.length; i++)
						{
							boolean isvarx=false;
							for (int j=0; j<usedvarx.length; j++)
							{
								if (var[i].equalsIgnoreCase(usedvarx[j]))
									isvarx=true;
							}
							if (isvarx)
							{
								realvalx[pointx]=realval[i];
								pointx++;
							}
							else
							{
								realvaly[pointy]=realval[i];
								pointy++;
							}
						}
						for (int i=0; i<realvalx.length; i++)
						{
							String namevx="";
							for (int j=0; j<var.length; j++)
							{
								if (usedvarx[i].equalsIgnoreCase(var[j]))
									namevx=usedvarx[i];
							}
							Hashtable<Vector<String>, String> tt=new Hashtable<Vector<String>, String>();
							tt.put(exgroupc, namevx);
							double[] compvalue=eigenvx.get(tt);
							if (Double.isNaN(realvalx[i]))
								ismd=true;
							else
							{
								for (int j=0; j<totalcomponent; j++)
								{
									if (!usecov)
										projectionx[j]=projectionx[j]+((realvalx[i]-actualmeanx[i])/actualstdx[i])*compvalue[j];
									else
										projectionx[j]=projectionx[j]+(realvalx[i]-actualmeanx[i])*compvalue[j];
								}
							}
						}
						for (int i=0; i<realvaly.length; i++)
						{
							String namevy="";
							for (int j=0; j<var.length; j++)
							{
								if (usedvary[i].equalsIgnoreCase(var[j]))
									namevy=usedvary[i];
							}
							Hashtable<Vector<String>, String> tt=new Hashtable<Vector<String>, String>();
							tt.put(exgroupc, namevy);
							double[] compvalue=eigenvy.get(tt);
							if (Double.isNaN(realvaly[i]))
								ismd=true;
							else
							{
								for (int j=0; j<totalcomponent; j++)
								{
									if (!usecov)
										projectiony[j]=projectiony[j]+((realvaly[i]-actualmeany[i])/actualstdy[i])*compvalue[j];
									else
										projectiony[j]=projectiony[j]+(realvaly[i]-actualmeany[i])*compvalue[j];
								}
							}
						}
						if (ismd)
						{
							for (int i=0; i<ncomp; i++)
							{
								projectionx[i]=Double.NaN;
								projectiony[i]=Double.NaN;
							}
						}
					}
				}
				String[] newvalues=new String[2*projectionx.length];
				for (int i=0; i<projectionx.length; i++)
				{
					newvalues[i]=double2String(projectionx[i]);
				}
				for (int i=0; i<projectiony.length; i++)
				{
					newvalues[i+projectionx.length]=double2String(projectiony[i]);
				}
				String[] wvalues=dsu.getnewvalues(values, newvalues);
				dw.write(wvalues);
			}
		}
		data.close();
		if (grouperror)
		{
			dw.deletetmp();
			return new Result("%874%<br>\n", false, null);
		}
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
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 721, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"c=", "dict", true, 749, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"s=", "dict", true, 750, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 1874, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2809,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.ncomp, "text", false, 755, dep, "", 2));
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
		retprocinfo[0]="1877";
		retprocinfo[1]="1881";
		return retprocinfo;
	}
}
