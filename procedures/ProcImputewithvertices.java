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
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.keywords.Keywords;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import java.util.Enumeration;

/**
* This is the procedure that imputes the values of the localized variables
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcImputewithvertices implements RunStep
{
	/**
	* Starts the execution of Proc Imputewithvertices and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict};
		String [] optionalparameters=new String[] {Keywords.where, Keywords.OUTS.toLowerCase(), Keywords.replace};
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
		boolean isouts =(parameters.get(Keywords.OUTS.toLowerCase())!=null);
		DataWriter dws=null;
		if (isouts)
		{
			dws=new DataWriter(parameters, Keywords.OUTS.toLowerCase());
			if (!dws.getmessage().equals(""))
				return new Result(dws.getmessage(), false, null);
		}

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String vartemp=(String)parameters.get(Keywords.var.toLowerCase());
		VariableUtilities varu=new VariableUtilities(dict, null, vartemp, null, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String replace=(String)parameters.get(Keywords.replace);

		String keyword="Impute with vertices "+dict.getkeyword();
		String description="Impute with vertices "+dict.getdescription();
		String author=dict.getauthor();

		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);
		dsu.defineolddict(dict);

		int rifrep=0;
		if (replace==null)
			rifrep=0;
		else if (replace.equalsIgnoreCase(Keywords.replaceall))
		{
			dsu.setempycodelabels();
			dsu.setempymissingdata();
			rifrep=1;
		}
		else if (replace.equalsIgnoreCase(Keywords.replaceformat))
		{
			dsu.setempycodelabels();
			rifrep=2;
		}
		else if (replace.equalsIgnoreCase(Keywords.replacemissing))
		{
			dsu.setempymissingdata();
			rifrep=3;
		}
		Hashtable<String, Integer> varref=new Hashtable<String, Integer>();
		int posloc=-1;
		int possol=-1;
		int postyp=-1;
		int detpos=-1;
		boolean type=false;
		for (int i=0; i<dict.gettotalvar(); i++)
		{
			varref.put((dict.getvarname(i)).toUpperCase(), new Integer(i));
			if (dict.getvarname(i).equalsIgnoreCase("result_localize"))
			{
				posloc=i;
			}
			if (dict.getvarname(i).equalsIgnoreCase("result_localize_all"))
			{
				type=true;
				posloc=i;
			}
			if (dict.getvarname(i).equalsIgnoreCase("solution_localize"))
			{
				possol=i;
			}
			if (dict.getvarname(i).equalsIgnoreCase("solution_type"))
			{
				postyp=i;
			}
			if (dict.getvarname(i).equalsIgnoreCase("deterministic_locvars"))
			{
				detpos=i;
			}
		}
		if (posloc==-1)
			return new Result("%2514%<br>\n", false, null);
		if (possol==-1)
			return new Result("%2603%<br>\n", false, null);
		if (postyp==-1)
			return new Result("%2821%<br>\n", false, null);
		if (detpos==-1)
			return new Result("%2855%<br>\n", false, null);

		Vector<StepResult> results = new Vector<StepResult>();
		if (type)
			results.add(new LocalMessageGetter("%2560%<br>\n"));

		DataReader data = new DataReader(dict);

		if (!data.open(null, rifrep, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		String[] values=null;
		String currloc="";
		String currsol="";
		String curdet="";

		String[] locs=new String[0];
		String[] sols=new String[0];

		String[] clocs=new String[0];
		String[] csols=new String[0];

		String[] clocss=new String[0];
		String[] csolss=new String[0];

		int validgroup=0;
		int numimputed=0;
		Hashtable<String, String> allvalues=new Hashtable<String, String>();
		int existdet=0;
		int refdetvars;
		String varname, tempval;
		double dv=0;
		Hashtable<String, double[]> donoreffects=new Hashtable<String, double[]>();
		while (!data.isLast())
		{
			values = data.getRecord();
			allvalues.clear();
			if (values!=null)
			{
				validgroup++;
				currloc=values[posloc].trim();
				currsol=values[possol].trim();
				curdet=values[detpos].trim();
				try
				{
					curdet=curdet.replaceAll("-","");
					curdet=curdet.replaceAll(";","");
					curdet=curdet.replaceAll(",","");
					if (curdet.length()>0) existdet++;
				}
				catch (Exception ee){}
				if ((!currloc.equals("1")) && (!currloc.equals("9")) && (!currloc.equals("99")) && (!currloc.equals("999")) && (!currloc.equals("9999")) && (!currloc.equals("99999")))
				{
					if (currloc.indexOf(";")>=0)
					{
						locs=currloc.split(";");
						sols=currsol.split(";");
						for (int i=0; i<locs.length; i++)
						{
							if (locs[i].trim().indexOf(",")>=0)
							{
								clocs=locs[i].trim().split(",");
								csols=sols[i].trim().split(",");
								clocss=clocs[0].trim().split(" ");
								csolss=csols[0].trim().split(" ");
								for (int j=0; j<clocss.length; j++)
								{
									dv=Double.parseDouble(csolss[j].trim());
									allvalues.put(clocss[j].trim().toUpperCase(), String.valueOf(dv));
								}
							}
							else
							{
								clocs=locs[i].trim().split(" ");
								csols=sols[i].trim().split(" ");
								for (int j=0; j<clocs.length; j++)
								{
									dv=Double.parseDouble(csols[j].trim());
									allvalues.put(clocs[j].trim().toUpperCase(), String.valueOf(dv));
								}
							}
						}
					}
					else
					{
						if (currloc.trim().indexOf(",")>=0)
						{
							clocs=currloc.trim().split(",");
							csols=currsol.trim().split(",");
							clocss=clocs[0].trim().split(" ");
							csolss=csols[0].trim().split(" ");
							for (int j=0; j<clocss.length; j++)
							{
								dv=Double.parseDouble(csolss[j].trim());
								allvalues.put(clocss[j].trim().toUpperCase(), String.valueOf(dv));
							}
						}
						else
						{
							clocss=currloc.trim().split(" ");
							csolss=currsol.trim().split(" ");
							for (int j=0; j<clocss.length; j++)
							{
								dv=Double.parseDouble(csolss[j].trim());
								allvalues.put(clocss[j].trim().toUpperCase(), String.valueOf(dv));
							}
						}
					}
					if (allvalues.size()>0)
					{
						numimputed++;
						values[postyp]="8";
						values[detpos]="";
						values[posloc]="1";
						values[possol]="";
						for (Enumeration<String> en=allvalues.keys(); en.hasMoreElements();)
						{
							varname=en.nextElement();
							tempval=allvalues.get(varname.toUpperCase());
							refdetvars=(varref.get(varname.toUpperCase())).intValue();
							dv=Double.parseDouble(tempval);
							double[] tempeffects=new double[8];
							if (!values[refdetvars].equals(""))
							{
								tempeffects[0]=1;
								tempeffects[1]=1;
								tempeffects[2]=Double.parseDouble(values[refdetvars]);
								tempeffects[3]=dv;
								tempeffects[4]=Double.parseDouble(values[refdetvars])-dv;
								tempeffects[5]=Math.abs(Double.parseDouble(values[refdetvars])-dv);
								tempeffects[6]=0;
								tempeffects[7]=0;
							}
							else
							{
								tempeffects[0]=1;
								tempeffects[1]=0;
								tempeffects[2]=0;
								tempeffects[3]=0;
								tempeffects[4]=0;
								tempeffects[5]=0;
								tempeffects[6]=1;
								tempeffects[7]=dv;
							}
							values[refdetvars]=String.valueOf(dv);
							if (donoreffects.get(varname.toUpperCase())==null)
							{
								donoreffects.put(varname.toUpperCase(), tempeffects);
							}
							else
							{
								double[] temptempeffects=donoreffects.get(varname.toUpperCase());
								temptempeffects[0]=temptempeffects[0]+tempeffects[0];
								temptempeffects[1]=temptempeffects[1]+tempeffects[1];
								temptempeffects[2]=temptempeffects[2]+tempeffects[2];
								temptempeffects[3]=temptempeffects[3]+tempeffects[3];
								temptempeffects[4]=temptempeffects[4]+tempeffects[4];
								temptempeffects[5]=temptempeffects[5]+tempeffects[5];
								temptempeffects[6]=temptempeffects[6]+tempeffects[6];
								temptempeffects[7]=temptempeffects[7]+tempeffects[7];
								donoreffects.put(varname.toUpperCase(), temptempeffects);
							}
						}
					}
				}
				dw.writenoapprox(values);
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
		Vector<StepResult> result = new Vector<StepResult>();
		if (numimputed==0) result.add(new LocalMessageGetter("%2842%<br>\n"));
		if (numimputed>0) result.add(new LocalMessageGetter("%2843%: "+String.valueOf(numimputed)+"<br>\n"));
		if (existdet>0) result.add(new LocalMessageGetter("%2856%<br>\n"));

		DataSetUtilities dsus=new DataSetUtilities();
		if (isouts)
		{
			Hashtable<String, String> tempmd=new Hashtable<String, String>();
			Hashtable<String, String> varfmt=new Hashtable<String, String>();
			for (Enumeration<String> en=donoreffects.keys(); en.hasMoreElements();)
			{
				String tvname=(String)en.nextElement();
				varfmt.put(tvname, dict.getvarlabelfromname(tvname));
			}
			dsus.addnewvar("refvar", "%3173%", Keywords.TEXTSuffix, varfmt, tempmd);
			dsus.addnewvar("tot_donated", "%3175%", Keywords.NUMSuffix+Keywords.INTSuffix, tempmd, tempmd);
			dsus.addnewvar("tot_don_no_mis", "%3176%", Keywords.NUMSuffix+Keywords.INTSuffix, tempmd, tempmd);
			dsus.addnewvar("orig_sum_no_mis", "%3177%", Keywords.NUMSuffix, tempmd, tempmd);
			dsus.addnewvar("new_sum_no_mis", "%3178%", Keywords.NUMSuffix, tempmd, tempmd);
			dsus.addnewvar("diff_no_mis", "%3179%", Keywords.NUMSuffix, tempmd, tempmd);
			dsus.addnewvar("abs_diff_no_mis", "%3180%", Keywords.NUMSuffix, tempmd, tempmd);
			dsus.addnewvar("av_diff_no_mis", "%3181%", Keywords.NUMSuffix, tempmd, tempmd);
			dsus.addnewvar("av_abs_diff_no_mis", "%3182%", Keywords.NUMSuffix, tempmd, tempmd);
			dsus.addnewvar("tot_don_min", "%3183%", Keywords.NUMSuffix+Keywords.INTSuffix, tempmd, tempmd);
			dsus.addnewvar("sum_mis", "%3184%", Keywords.NUMSuffix, tempmd, tempmd);
			dsus.addnewvar("av_mis", "%3185%", Keywords.NUMSuffix, tempmd, tempmd);
			dsus.addnewvar("tot_sum", "%3186%", Keywords.NUMSuffix, tempmd, tempmd);
			if (!dws.opendatatable(dsus.getfinalvarinfo()))
				return new Result(dws.getmessage(), false, null);
			if (numimputed>0)
			{
				for (Enumeration<String> en=donoreffects.keys(); en.hasMoreElements();)
				{
					String tvname=en.nextElement();
					double[] temptempeffects=donoreffects.get(tvname);
					String[] writeinstat=new String[13];
					writeinstat[0]=tvname;
					writeinstat[1]=String.valueOf(temptempeffects[0]);
					writeinstat[2]=String.valueOf(temptempeffects[1]);
					writeinstat[3]=String.valueOf(temptempeffects[2]);
					writeinstat[4]=String.valueOf(temptempeffects[3]);
					writeinstat[5]=String.valueOf(temptempeffects[4]);
					writeinstat[6]=String.valueOf(temptempeffects[5]);
					writeinstat[7]=String.valueOf(temptempeffects[4]/temptempeffects[1]);
					writeinstat[8]=String.valueOf(temptempeffects[5]/temptempeffects[1]);
					writeinstat[9]=String.valueOf(temptempeffects[6]);
					writeinstat[10]=String.valueOf(temptempeffects[7]);
					writeinstat[11]=String.valueOf(temptempeffects[7]/temptempeffects[6]);
					writeinstat[12]=String.valueOf(temptempeffects[3]+temptempeffects[7]);
					dws.write(writeinstat);
				}
			}
			else
			{
				results.add(new LocalMessageGetter("%3208%<br>\n"));
				dws.deletetmp();
			}
		}

		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		if (isouts && numimputed>0)
		{
			boolean rescloses=dws.close();
			if (!rescloses)
				return new Result(dws.getmessage(), false, null);
		}
		Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
		Hashtable<String, String> datatableinfo=dw.getTableInfo();
		result.add(new LocalDictionaryWriter(dw.getdictpath(), keyword, description, author, dw.gettabletype(),
		datatableinfo, dsu.getfinalvarinfo(), tablevariableinfo, dsu.getfinalcl(), dsu.getfinalmd(), null));
		if (isouts && numimputed>0)
		{
			Vector<Hashtable<String, String>> tablevariableinfos=dws.getVarInfo();
			Hashtable<String, String> datatableinfos=dws.getTableInfo();
			results.add(new LocalDictionaryWriter(dws.getdictpath(), keyword, description, author, dws.gettabletype(),
			datatableinfos, dsus.getfinalvarinfo(), tablevariableinfos, dsus.getfinalcl(), dsus.getfinalmd(), null));
		}
		return new Result("", true, result);
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 541, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 2521, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUTS.toLowerCase()+"=", "setting=out", false, 3209, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
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
		retprocinfo[0]="2439";
		retprocinfo[1]="2520";
		return retprocinfo;
	}
}
