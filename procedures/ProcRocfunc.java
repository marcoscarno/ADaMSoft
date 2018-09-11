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

import ADaMSoft.algorithms.VarGroupModalities;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.GroupedTempDataSet;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;

import ADaMSoft.keywords.Keywords;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.Vector;

/**
* This is the procedure that evaluates the error function for the result of a classification model
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcRocfunc extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Rocfunc
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		boolean novgconvert=false;
		boolean noclforvg=false;
		boolean orderclbycode=false;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.varpred, Keywords.vary};
		String [] optionalparameters=new String[] {Keywords.vargroup, Keywords.where, Keywords.weight, Keywords.replace, Keywords.novgconvert, Keywords.noclforvg, Keywords.orderclbycode};
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

		novgconvert =(parameters.get(Keywords.novgconvert)!=null);
		orderclbycode =(parameters.get(Keywords.orderclbycode)!=null);
		noclforvg =(parameters.get(Keywords.noclforvg)!=null);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String tempvarx=(String)parameters.get(Keywords.varpred.toLowerCase());
		String tempvary=(String)parameters.get(Keywords.vary.toLowerCase());
		String tempvargroup=(String)parameters.get(Keywords.vargroup.toLowerCase());
		String weight=(String)parameters.get(Keywords.weight.toLowerCase());

		String[] testdepvar=tempvary.split(" ");
		if (testdepvar.length>1)
			return new Result("%1812%<br>\n", false, null);

		VariableUtilities varu=new VariableUtilities(dict, tempvargroup, null, weight, tempvarx, tempvary);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String keyword="Rocfunc "+dict.getkeyword();
		String description="Rocfunc "+dict.getdescription();
		String author=(String)parameters.get(Keywords.client_host.toLowerCase());

		String[] varx=varu.getrowvar();
		String[] varg=varu.getgroupvar();

		if ((varg.length==0) && (novgconvert))
		{
			result.add(new LocalMessageGetter("%2228%<br>\n"));
		}
		if ((varg.length==0) && (noclforvg))
		{
			result.add(new LocalMessageGetter("%2230%<br>\n"));
		}
		if ((varg.length==0) && (orderclbycode))
		{
			result.add(new LocalMessageGetter("%2232%<br>\n"));
		}

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
		int[] colrule=varu.getcolruleforsel();
		int[] weightrule=varu.getweightruleforsel();

		ValuesParser vp=new ValuesParser(null, grouprule, null, rowrule, colrule, weightrule);

		VarGroupModalities vgm=new VarGroupModalities();

		if (varg.length>0)
		{
			vgm.setvarnames(varg);
			vgm.setdictionary(dict);
			if (orderclbycode)
				vgm.setorderbycode();
			if (novgconvert)
				vgm.noconversion();
		}

		Hashtable<String, Integer> checkclasses=new Hashtable<String, Integer>();
		double[][] dist=new double[varx.length][2];
		double[][] num=new double[varx.length][2];
		for (int i=0; i<varx.length; i++)
		{
			dist[i][0]=0;
			dist[i][1]=0;
			num[i][0]=0;
			num[i][1]=0;
		}

		int validgroup=0;
		String[] values=null;
		Vector<String> vargroupvalues=new Vector<String>();
		double[] varxvalues=null;
		String[] varyvalues=null;
		double weightvalue=1;
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				if (novgconvert)
					vargroupvalues=vp.getorigvargroup(values);
				else
					vargroupvalues=vp.getvargroup(values);
				varxvalues=vp.getrowvarasdouble(values);
				varyvalues=vp.getcolvar(values);
				weightvalue=vp.getweight(values);
				if ((vp.vargroupisnotmissing(vargroupvalues)) && (!varyvalues[0].equals("")) && (!Double.isNaN(weightvalue)))
				{
					validgroup++;
					if (checkclasses.get(varyvalues[0])==null)
						checkclasses.put(varyvalues[0], new Integer(checkclasses.size()));
					if (checkclasses.size()>2)
					{
						data.close();
						return new Result("%1813%<br>\n", false, null);
					}
					for (int i=0; i<varxvalues.length; i++)
					{
						if (!Double.isNaN(varxvalues[i]))
						{
							if ((varxvalues[i]>1) || (varxvalues[i]<0))
							{
								data.close();
								return new Result("%1819% ("+varx[i]+")<br>\n", false, null);
							}
							int rif=(checkclasses.get(varyvalues[0])).intValue();
							dist[i][rif]=dist[i][rif]+varxvalues[i]*weightvalue;
							num[i][rif]=num[i][rif]+weightvalue;
						}
					}
					vgm.updateModalities(vargroupvalues);
				}
			}
		}
		data.close();
		if ((validgroup==0) && (where!=null))
			return new Result("%2804%<br>\n", false, null);
		if ((validgroup==0) && (where==null))
			return new Result("%894%<br>\n", false, null);
		vgm.calculate();
		boolean[] order=new boolean[varx.length];
		for (int i=0; i<varx.length; i++)
		{
			order[i]=true;
			dist[i][0]=dist[i][0]/num[i][0];
			dist[i][1]=dist[i][1]/num[i][1];
			if (dist[i][1]<dist[i][0])
				order[i]=false;
		}

		String tempdir=(String)parameters.get(Keywords.WorkDir);

		int totalgroupmodalities=vgm.getTotal();
		if (totalgroupmodalities==0)
			totalgroupmodalities=1;

		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);

		String[] rife=new String[2];

		for (Enumeration<String> en=checkclasses.keys(); en.hasMoreElements();)
		{
			String temprif=(String)en.nextElement();
			int r=(checkclasses.get(temprif)).intValue();
			rife[r]=temprif;
		}

		Vector<Hashtable<String, String>> groupcodelabels=vgm.getgroupcodelabels();

		Hashtable<String, String> tempmd=new Hashtable<String, String>();
		for (int j=0; j<varg.length; j++)
		{
			if (!noclforvg)
				dsu.addnewvarfromolddict(dict, varg[j], groupcodelabels.get(j), tempmd, "g_"+varg[j]);
			else
				dsu.addnewvarfromolddict(dict, varg[j], tempmd, tempmd, "g_"+varg[j]);
		}

		Hashtable<String, String> clvar=new Hashtable<String, String>();
		for (int i=0; i<varx.length; i++)
		{
			String namerif="";
			if (order[i])
				namerif=dict.getvarlabelfromname(varx[i])+" (0="+rife[0]+"; 1="+rife[1]+")";
			else
				namerif=dict.getvarlabelfromname(varx[i])+" (0="+rife[1]+"; 1="+rife[0]+")";
			clvar.put(varx[i], namerif);
		}

		dsu.addnewvar("v"+String.valueOf(varg.length), "%1815%", Keywords.TEXTSuffix, clvar, tempmd);
		dsu.addnewvar("v"+String.valueOf(varg.length+1), "%1818%", Keywords.NUMSuffix, tempmd, tempmd);
		dsu.addnewvar("v"+String.valueOf(varg.length+2), "%1835%", Keywords.NUMSuffix, tempmd, tempmd);
		dsu.addnewvar("v"+String.valueOf(varg.length+3), "%1845%", Keywords.NUMSuffix, tempmd, tempmd);

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		String[] valuestowrite=new String[varg.length+4];
		GroupedTempDataSet defgtds=new GroupedTempDataSet(tempdir, 4);
		for (int r=0; r<varx.length; r++)
		{
			String[] valtowrite=new String[3];
			String[] uvaltowrite=new String[1];
			GroupedTempDataSet gtds=new GroupedTempDataSet(tempdir, 3);
			GroupedTempDataSet ugtds=new GroupedTempDataSet(tempdir, 1);

			data = new DataReader(dict);
			if (!data.open(reqvar, replacerule, false))
				return new Result(data.getmessage(), false, null);
			while (!data.isLast())
			{
				values = data.getRecord();
				if (novgconvert)
					vargroupvalues=vp.getorigvargroup(values);
				else
					vargroupvalues=vp.getvargroup(values);
				varxvalues=vp.getrowvarasdouble(values);
				varyvalues=vp.getcolvar(values);
				weightvalue=vp.getweight(values);
				if ((vp.vargroupisnotmissing(vargroupvalues)) && (!varyvalues[0].equals("")) && (!Double.isNaN(weightvalue)))
				{
					if (!Double.isNaN(varxvalues[r]))
					{
						int rifr=(checkclasses.get(varyvalues[0])).intValue();
						uvaltowrite[0]=String.valueOf(varxvalues[r]);
						valtowrite[0]=String.valueOf(varxvalues[r]);
						valtowrite[1]="";
						valtowrite[2]=String.valueOf(weightvalue);
						if (order[r])
							valtowrite[1]=String.valueOf(rifr);
						else
							valtowrite[1]=String.valueOf(Math.abs(1-rifr));
						ugtds.write(vargroupvalues, uvaltowrite);
						gtds.write(vargroupvalues, valtowrite);
					}
				}
			}
			data.close();
			gtds.finalizeWriteAll();
			ugtds.finalizeWriteAll();
			if (gtds.geterrors())
			{
				gtds.deletetempdataAll();
					return new Result("%1814%\n"+gtds.getMessage()+"<br>\n", false, null);
			}
			if (ugtds.geterrors())
			{
				ugtds.deletetempdataAll();
					return new Result("%1814%\n"+ugtds.getMessage()+"<br>\n", false, null);
			}
			ugtds.sortAll(0);
			for (int i=0; i<totalgroupmodalities; i++)
			{
				Vector<String> rifmodgroup=vgm.getvectormodalities(i);
				for (int j=0; j<rifmodgroup.size(); j++)
				{
					String groupvalue=rifmodgroup.get(j);
					if (groupvalue!=null)
					{
						if (!noclforvg)
							valuestowrite[j]=vgm.getcode(j, groupvalue);
						else
							valuestowrite[j]=groupvalue;
					}
				}
				valuestowrite[varg.length]=varx[r];
				double actualval=Double.NaN;
				String[] uvalread=new String[1];
				String[] valread=new String[0];
				for (int k=0; k<ugtds.getRows(rifmodgroup); k++)
				{
					uvalread=ugtds.read(rifmodgroup);
					actualval=string2double(uvalread[0]);
					if (!Double.isNaN(actualval))
					{
						double n00=0;
						double n01=0;
						double n10=0;
						double n11=0;
						double valpred=Double.NaN;
						double classval=Double.NaN;
						double numobs=Double.NaN;
						for (int p=0; p<gtds.getRows(rifmodgroup); p++)
						{
							valread=gtds.read(rifmodgroup);
							valpred=string2double(valread[0]);
							classval=string2double(valread[1]);
							numobs=string2double(valread[2]);
							if ((!Double.isNaN(numobs)) && (!Double.isNaN(classval)) && (!Double.isNaN(valpred)))
							{
								if (valpred>actualval)
									valpred=1;
								else
									valpred=0;
								if ((classval==1) && (valpred==1))
									n11=n11+numobs;
								if ((classval==0) && (valpred==0))
									n00=n00+numobs;
								if ((classval==1) && (valpred==0))
									n10=n10+numobs;
								if ((classval==0) && (valpred==1))
									n01=n01+numobs;
							}
						}
						valuestowrite[varg.length+1]=double2String(actualval);
						double sensitivity=n11/(n11+n10);
						double specificity=n00/(n01+n00);
						valuestowrite[varg.length+2]=double2String(sensitivity);
						valuestowrite[varg.length+3]=double2String(1-specificity);
						defgtds.write(rifmodgroup, valuestowrite);
						gtds.endread(rifmodgroup);
					}
				}
				ugtds.endread(rifmodgroup);
			}
			gtds.deletetempdataAll();
			ugtds.deletetempdataAll();
		}
		defgtds.finalizeWriteAll();
		for (int i=0; i<totalgroupmodalities; i++)
		{
			Vector<String> rifmodgroup=vgm.getvectormodalities(i);
			for (int j=0; j<rifmodgroup.size(); j++)
			{
				String groupvalue=rifmodgroup.get(j);
				if (groupvalue!=null)
				{
					if (!noclforvg)
						valuestowrite[j]=vgm.getcode(j, groupvalue);
					else
						valuestowrite[j]=groupvalue;
				}
			}
			for (int p=0; p<defgtds.getRows(rifmodgroup); p++)
			{
				String[] valread=defgtds.read(rifmodgroup);
				valuestowrite[varg.length]=valread[0];
				valuestowrite[varg.length+1]=valread[1];
				valuestowrite[varg.length+2]=valread[2];
				valuestowrite[varg.length+3]=valread[3];
				dw.write(valuestowrite);
			}
			defgtds.endread(rifmodgroup);
		}
		defgtds.deletetempdataAll();

		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
		Hashtable<String, String> datatableinfo=dw.getTableInfo();
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
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 1809, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 1849, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.varpred, "vars=all", true, 1810, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vary, "var=all", true, 1811, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.weight, "vars=all", false, 656, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.novgconvert, "checkbox", false, 2227, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.noclforvg, "checkbox", false, 2229, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.orderclbycode, "checkbox", false, 2231, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="1806";
		retprocinfo[1]="1844";
		return retprocinfo;
	}
}
