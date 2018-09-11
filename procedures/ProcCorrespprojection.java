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
* This is the procedure that project the observation and the variables in the correspondence space
* @author marco.scarno@gmail.com
* @version 1.0.0, rev.: 13/02/17 by marco
*/
public class ProcCorrespprojection extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Correspprojection
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean novgconvert=false;
		boolean novar=false;
		boolean noobs=false;
		boolean scatter=false;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict+"c", Keywords.dict};
		String [] optionalparameters=new String[] {Keywords.replace, Keywords.where, Keywords.varid, Keywords.ncomp, Keywords.novar, Keywords.noobs, Keywords.novgconvert, Keywords.onlyproj};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		novar =(parameters.get(Keywords.novar)!=null);
		noobs =(parameters.get(Keywords.noobs)!=null);
		scatter= (parameters.get(Keywords.scatter)!=null);
		novgconvert =(parameters.get(Keywords.novgconvert)!=null);
		boolean onlyproj=(parameters.get(Keywords.onlyproj)!=null);

		String replace =(String)parameters.get(Keywords.replace);
		String varid=(String)parameters.get(Keywords.varid.toLowerCase());

		if ((novar) && (noobs))
			return new Result("%959%<br>\n", false, null);

		if (varid!=null)
		{
			String[] testvarid=varid.split(" ");
			if (testvarid.length>1)
				return new Result("%1634%<br>\n", false, null);
		}

		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		DictionaryReader dictc = (DictionaryReader)parameters.get(Keywords.dict+"c");
		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		int numvarc=dictc.gettotalvar();
		int totalcomponent=0;
		String nameofvarsforc="";
		boolean iscorrectc=true;
		for (int i=0; i<numvarc; i++)
		{
			String tempname=dictc.getvarname(i);
			if ((tempname.toLowerCase()).startsWith("c"))
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

		Hashtable<String, String> listofvar=dictc.getcodelabelfromname("type");
		if (listofvar.size()==0)
			return new Result("%958%<br>\n", false, null);

		String usedvarnames="";
		for (Enumeration<String> e = listofvar.keys() ; e.hasMoreElements() ;)
		{
			String temp= e.nextElement();
			if ( (!temp.equals("1")) && (!temp.equals("2")) )
				usedvarnames=usedvarnames+temp+" ";
		}
		usedvarnames=usedvarnames.trim();

		String[] activevar=usedvarnames.split(" ");

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

		int ncomp=string2int(tempncomp);
		if (ncomp==0)
			return new Result("%756%<br>\n", false, null);

		if (totalcomponent<ncomp)
			return new Result("%757%<br>\n", false, null);

		if (ncomp==0)
			totalcomponent=ncomp;

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

		Hashtable<Vector<String>, double[]> eigenval=new Hashtable<Vector<String>, double[]>();
		Hashtable<Hashtable<Vector<String>, String>, double[]> eigenvec=new Hashtable<Hashtable<Vector<String>, String>, double[]>();
		String[] values =null;
		while (!datac.isLast())
		{
			values = datac.getRecord();
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
				eigenval.put(groupval, compval);
			}
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
				t.put(groupval, type.toLowerCase());
				eigenvec.put(t, compval);
			}
		}
		datac.close();

		String keyword="Correspprojection "+dict.getkeyword();
		String description="Correspprojection "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		DataSetUtilities dsu=new DataSetUtilities();

		dsu.setreplace(replace);

		Hashtable<String, String> temph=new Hashtable<String, String>();

		for (int j=0; j<groupnameforc.length; j++)
		{
			dsu.addnewvarfromolddict(dict, groupnameforc[j], dict.getcodelabelfromname(groupnameforc[j]), temph, "g_"+groupnameforc[j]);
		}

		Hashtable<String, String> clvar=new Hashtable<String, String>();
		clvar.put("1", "%306%");
		clvar.put("2", "%1100%");

		dsu.addnewvar("Type", "%1102%", Keywords.TEXTSuffix, clvar, temph);

		Hashtable<String, String> clname=new Hashtable<String, String>();
		for (int i=0; i<activevar.length; i++)
		{
			clname.put(activevar[i], dict.getvarlabelfromname(activevar[i]));
		}
		if (varid!=null)
		{
			Hashtable<String, String> clnameobs=dict.getcodelabelfromname(varid);
			if (clnameobs!=null)
			{
				for (Enumeration<String> en=clnameobs.keys(); en.hasMoreElements();)
				{
					String ccode=en.nextElement();
					String cvalu=clnameobs.get(ccode);
					clname.put(ccode, cvalu);
				}
			}
		}
		dsu.addnewvar("Name", "%1103%", Keywords.TEXTSuffix, clname, temph);

		for (int i=0; i<ncomp; i++)
		{
			dsu.addnewvar("Component_"+(String.valueOf(i)), "%1104% "+(String.valueOf(i+1)), Keywords.NUMSuffix, temph, temph);
		}
		if (!onlyproj)
		{
			for (int i=0; i<ncomp; i++)
			{
				dsu.addnewvar("Relc_"+(String.valueOf(i)), "%1105% "+(String.valueOf(i+1)), Keywords.NUMSuffix, temph, temph);
			}
			for (int i=0; i<ncomp; i++)
			{
				dsu.addnewvar("Absc_"+(String.valueOf(i)), "%1106% "+(String.valueOf(i+1)), Keywords.NUMSuffix, temph, temph);
			}
		}
		if (scatter)
			dsu.addnewvar(Keywords.varlabelinfo, "%1107%", Keywords.TEXTSuffix, temph, temph);

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

		VariableUtilities varu=new VariableUtilities(dict, vargroup, null, null, usedvarnames, varid);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String[] reqvar=varu.getreqvar();

		int[] replacerule=varu.getreplaceruleforsel(replace);
		DataReader data = new DataReader(dict);

		if (!data.open(reqvar, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		int[] grouprule=varu.getgroupruleforsel();
		int[] rowrule=varu.getrowruleforsel();
		int[] colrule=new int[0];

		ValuesParser vp=null;

		if (varid!=null)
		{
			colrule=varu.getcolruleforsel();
			vp=new ValuesParser(null, grouprule, null, rowrule, colrule, null);
		}
		else
			vp=new ValuesParser(null, grouprule, null, rowrule, null, null);

		int validgroup=0;
		Hashtable<Vector<String>, Double> total=new Hashtable<Vector<String>, Double>();
		Hashtable<Vector<String>, double[]> coltotal=new Hashtable<Vector<String>, double[]>();
		Vector<String> vargroupvalues=new Vector<String>();
		double[] varrowvalues=null;
		String[] varcolvalues=new String[0];
		double temptotal=0;
		double[] tempcoltotal=null;
		boolean ismd=false;
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				if (novgconvert)
					vargroupvalues=vp.getorigvargroup(values);
				else
					vargroupvalues=vp.getvargroup(values);
				varrowvalues=vp.getrowvarasdouble(values);
				if (vp.vargroupisnotmissing(vargroupvalues))
				{
					if (total.get(vargroupvalues)==null)
					{
						temptotal=0;
						tempcoltotal=new double[varrowvalues.length];
						ismd=false;
						for (int i=0; i<varrowvalues.length; i++)
						{
							tempcoltotal[i]=varrowvalues[i];
							temptotal +=varrowvalues[i];
							if (Double.isNaN(varrowvalues[i]))
								ismd=true;
						}
						if (!ismd)
						{
							total.put(vargroupvalues, new Double(temptotal));
							coltotal.put(vargroupvalues, tempcoltotal);
							validgroup++;
						}
					}
					else
					{
						temptotal=(total.get(vargroupvalues)).doubleValue();
						tempcoltotal=coltotal.get(vargroupvalues);
						ismd=false;
						for (int i=0; i<varrowvalues.length; i++)
						{
							tempcoltotal[i] +=varrowvalues[i];
							temptotal +=varrowvalues[i];
							if (Double.isNaN(varrowvalues[i]))
								ismd=true;
						}
						if (!ismd)
						{
							total.put(vargroupvalues, new Double(temptotal));
							coltotal.put(vargroupvalues, tempcoltotal);
							validgroup++;
						}
					}
				}
			}
		}
		data.close();
		if ((validgroup==0) && (where!=null))
			return new Result("%2804%<br>\n", false, null);
		if ((validgroup==0) && (where==null))
			return new Result("%666%<br>\n", false, null);

		boolean checkgroup=true;
		double[] tempeigenval=null;
		for (Enumeration<Vector<String>> e = coltotal.keys() ; e.hasMoreElements() ;)
		{
			Vector<String> groupvalues= e.nextElement();
			tempeigenval=eigenval.get(groupvalues);
			if (tempeigenval==null)
				checkgroup=false;
		}
		if (!checkgroup)
			return new Result("%960%<br>\n", false, null);

		int outdim=0;
		if (!onlyproj) outdim=groupnameforc.length+2+ncomp*3;
		else outdim=groupnameforc.length+2+ncomp;
		if (scatter)
			outdim=outdim+1;

		if (!novar)
		{
			String[] newvalues=new String[outdim];
			Vector<String> groupvalues=new Vector<String>();
			Vector<String> exgroupc=new Vector<String>();
			String grouptosearch=null;
			Hashtable<Vector<String>, String> tt=new Hashtable<Vector<String>, String>();
			double[] compvalue=null;
			double allprojection=0;
			double projection=0;
			for (Enumeration<Vector<String>> e = coltotal.keys() ; e.hasMoreElements() ;)
			{
				groupvalues= e.nextElement();
				temptotal=(total.get(groupvalues)).doubleValue();
				tempcoltotal=coltotal.get(groupvalues);
				tempeigenval=eigenval.get(groupvalues);
				exgroupc.clear();
				for (int i=0; i<groupvalues.size(); i++)
				{
					if (groupvalues.get(i)!=null)
					{
						grouptosearch=(groupvalues.get(i)).trim();
						newvalues[i]=grouptosearch;
						exgroupc.add(grouptosearch);
					}
					else
						exgroupc.add(null);
				}
				newvalues[groupnameforc.length]="1";
				for (int i=0; i<tempcoltotal.length; i++)
				{
					for (int j=groupnameforc.length+1; j<outdim; j++)
					{
						newvalues[j]="";
					}
					try
					{
						tt.clear();
						newvalues[groupnameforc.length+1]=activevar[i];
						tt.put(exgroupc, activevar[i]);
						compvalue=eigenvec.get(tt);
						allprojection=0;
						for (int j=0; j<totalcomponent; j++)
						{
							allprojection +=Math.pow((Math.sqrt(temptotal/tempcoltotal[i])*Math.sqrt(tempeigenval[j])*compvalue[j]),2);
						}
						for (int j=0; j<ncomp; j++)
						{
							newvalues[groupnameforc.length+2+j]="";
							projection=Math.sqrt(temptotal/tempcoltotal[i])*Math.sqrt(tempeigenval[j])*compvalue[j];
							newvalues[groupnameforc.length+2+j]=double2String(projection);
						}
						if (!onlyproj)
						{
							for (int j=0; j<ncomp; j++)
							{
								newvalues[groupnameforc.length+2+j+ncomp]="";
								projection=Math.sqrt(temptotal/tempcoltotal[i])*Math.sqrt(tempeigenval[j])*compvalue[j];
								projection=Math.pow(projection, 2)/allprojection;
								newvalues[groupnameforc.length+2+j+ncomp]=double2String(projection);
							}
							for (int j=0; j<ncomp; j++)
							{
								newvalues[groupnameforc.length+2+j+ncomp*2]="";
								projection=Math.sqrt(temptotal/tempcoltotal[i])*Math.sqrt(tempeigenval[j])*compvalue[j];
								projection=(tempcoltotal[i]/temptotal)*Math.pow(projection, 2)/tempeigenval[j];
								newvalues[groupnameforc.length+2+j+ncomp*2]=double2String(projection);
							}
						}
						if (scatter)
							newvalues[outdim-1]="red";
					}
					catch (Exception enumber) {}
					dw.write(newvalues);
				}
			}
		}

		if (!noobs)
		{
			if (!data.open(reqvar, replacerule, false))
				return new Result(data.getmessage(), false, null);
			int currentobs=0;
			String[] newvalues=new String[outdim];
			Vector<String> exgroupc=new Vector<String>();
			String grouptosearch=null;
			double sumrow=0;
			double[] projection=null;
			Hashtable<Vector<String>, String> tt=new Hashtable<Vector<String>, String>();
			double[] compvalue=null;
			double sumofp=0;
			double rel=0;
			while (!data.isLast())
			{
				currentobs++;
				for (int i=0; i<outdim; i++)
				{
					newvalues[i]="";
				}
				values = data.getRecord();
				if (novgconvert)
					vargroupvalues=vp.getorigvargroup(values);
				else
					vargroupvalues=vp.getvargroup(values);
				varrowvalues=vp.getrowvarasdouble(values);
				if (varid!=null)
					varcolvalues=vp.getcolvar(values);
				if (vp.vargroupisnotmissing(vargroupvalues))
				{
					temptotal=(total.get(vargroupvalues)).doubleValue();
					tempcoltotal=coltotal.get(vargroupvalues);
					tempeigenval=eigenval.get(vargroupvalues);
					exgroupc.clear();
					for (int i=0; i<vargroupvalues.size(); i++)
					{
						if (vargroupvalues.get(i)!=null)
						{
							grouptosearch=(vargroupvalues.get(i)).trim();
							newvalues[i]=grouptosearch;
							exgroupc.add(grouptosearch);
						}
						else
							exgroupc.add(null);
					}
					newvalues[groupnameforc.length]="2";
					for (int j=groupnameforc.length+1; j<outdim; j++)
					{
						newvalues[j]="";
					}
					try
					{
						sumrow=0;
						ismd=false;
						for (int i=0; i<varrowvalues.length; i++)
						{
							sumrow +=varrowvalues[i];
							if (Double.isNaN(varrowvalues[i]))
								ismd=true;
						}
						if (!ismd)
						{
							for (int i=0; i<varrowvalues.length; i++)
							{
								varrowvalues[i]=(varrowvalues[i]/sumrow)*Math.sqrt(temptotal/tempcoltotal[i]);
							}
							projection=new double[totalcomponent];
							for (int i=0; i<totalcomponent; i++)
							{
								projection[i]=0;
							}
							for (int i=0; i<tempcoltotal.length; i++)
							{
								tt.clear();
								tt.put(exgroupc, activevar[i]);
								compvalue=eigenvec.get(tt);
								for (int j=0; j<totalcomponent; j++)
								{
									projection[j] +=varrowvalues[i]*compvalue[j];
								}
							}
							sumofp=0;
							for (int i=0; i<totalcomponent; i++)
							{
								sumofp +=Math.pow(projection[i],2);
							}
							if (varid==null)
								newvalues[groupnameforc.length+1]=String.valueOf(currentobs);
							else if (!varcolvalues[0].equals(""))
								newvalues[groupnameforc.length+1]=varcolvalues[0];
							else
								newvalues[groupnameforc.length+1]=String.valueOf(currentobs);
							for (int i=0; i<ncomp; i++)
							{
								newvalues[groupnameforc.length+2+i]=double2String(projection[i]);
							}
							if (!onlyproj)
							{
								for (int i=0; i<ncomp; i++)
								{
									rel=Math.pow(projection[i], 2)/sumofp;
									newvalues[groupnameforc.length+2+i+ncomp]=double2String(rel);
								}
								for (int i=0; i<ncomp; i++)
								{
									rel=((sumrow/temptotal)*Math.pow(projection[i],2))/tempeigenval[i];
									newvalues[groupnameforc.length+2+i+ncomp*2]=double2String(rel);
								}
							}
						}
						else
						{
							if (varid==null)
								newvalues[groupnameforc.length+1]=String.valueOf(currentobs);
							else if (!varcolvalues[0].equals(""))
								newvalues[groupnameforc.length+1]=varcolvalues[0];
							else
								newvalues[groupnameforc.length+1]=String.valueOf(currentobs);
							for (int i=0; i<ncomp; i++)
							{
								newvalues[groupnameforc.length+2+i]="";
							}
							if (!onlyproj)
							{
								for (int i=0; i<ncomp; i++)
								{
									newvalues[groupnameforc.length+2+i+ncomp]="";
								}
								for (int i=0; i<ncomp; i++)
								{
									newvalues[groupnameforc.length+2+i+ncomp*2]="";
								}
							}
						}
						if (scatter)
							newvalues[outdim-1]="blue";
					}
					catch (Exception enumber) {}
					dw.write(newvalues);
				}
			}
			data.close();
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
		parameters.add(new GetRequiredParameters(Keywords.dict+"c=", "dict", true, 952, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 747, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.varid, "vars=all", false, 1635, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2809,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.ncomp, "text", false, 954, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.novar, "checkbox", false, 955, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.noobs, "checkbox", false, 956, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.scatter, "checkbox", false, 964, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.novgconvert, "checkbox", false, 2227, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.onlyproj, "checkbox", false, 3408, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="950";
		retprocinfo[1]="953";
		return retprocinfo;
	}
}
