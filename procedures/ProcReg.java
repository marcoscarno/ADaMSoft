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

import ADaMSoft.algorithms.RegEvaluator;
import ADaMSoft.algorithms.VarGroupModalities;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
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

import cern.jet.stat.Probability;

/**
* This is the procedure that implements a linear regression model
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcReg extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Reg
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		boolean novgconvert=false;
		boolean noclforvg=false;
		boolean orderclbycode=false;
		boolean noint=false;
		boolean pairwise=false;
		boolean isoutc=false;
		String [] requiredparameters=new String[] {Keywords.OUTE.toLowerCase(), Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.varx, Keywords.vary};
		String [] optionalparameters=new String[] {Keywords.OUTC.toLowerCase(), Keywords.vargroup, Keywords.where, Keywords.weight, Keywords.noint, Keywords.replace, Keywords.novgconvert, Keywords.noclforvg, Keywords.orderclbycode};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		noint =(parameters.get(Keywords.noint)!=null);
		pairwise =false;
		isoutc =(parameters.get(Keywords.OUTC.toLowerCase())!=null);

		String replace =(String)parameters.get(Keywords.replace);
		DataWriter dw=new DataWriter(parameters, Keywords.OUTE.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		novgconvert =(parameters.get(Keywords.novgconvert)!=null);
		orderclbycode =(parameters.get(Keywords.orderclbycode)!=null);
		noclforvg =(parameters.get(Keywords.noclforvg)!=null);

		DataWriter dwi=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dwi.getmessage().equals(""))
			return new Result(dwi.getmessage(), false, null);

		DataWriter dwc=null;
		if (isoutc)
		{
			dwc=new DataWriter(parameters, Keywords.OUTC.toLowerCase());
			if (!dwc.getmessage().equals(""))
				return new Result(dwc.getmessage(), false, null);
		}

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String tempvarx=(String)parameters.get(Keywords.varx.toLowerCase());
		String tempvary=(String)parameters.get(Keywords.vary.toLowerCase());
		String tempvargroup=(String)parameters.get(Keywords.vargroup.toLowerCase());
		String weight=(String)parameters.get(Keywords.weight.toLowerCase());

		String[] testdepvar=tempvary.split(" ");
		if (testdepvar.length>1)
			return new Result("%1125%<br>\n", false, null);

		VariableUtilities varu=new VariableUtilities(dict, tempvargroup, null, weight, tempvarx, tempvary);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String keyword="Reg "+dict.getkeyword();
		String description="Reg "+dict.getdescription();
		String idescription="Reg: information "+dict.getdescription();
		String cdescription="Reg: covb "+dict.getdescription();
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
		RegEvaluator ereg=new RegEvaluator(noint, pairwise);
		if (varg.length>0)
		{
			vgm.setvarnames(varg);
			vgm.setdictionary(dict);
			if (orderclbycode)
				vgm.setorderbycode();
			if (novgconvert)
				vgm.noconversion();
		}

		int validgroup=0;
		String[] values=null;
		Vector<String> vargroupvalues=new Vector<String>();
		double[] varxvalues=null;
		double[] varyvalues=null;
		int validx=0;
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
				varyvalues=vp.getcolvarasdouble(values);
				weightvalue=vp.getweight(values);
				if ((vp.vargroupisnotmissing(vargroupvalues)) && (!Double.isNaN(varyvalues[0])) && (!Double.isNaN(weightvalue)))
				{
					validx=0;
					for (int i=0; i<varxvalues.length; i++)
					{
						if (Double.isNaN(varxvalues[i])) validx++;
					}
					if (validx<varxvalues.length)
					{
						validgroup++;
						vgm.updateModalities(vargroupvalues);
						ereg.setValue(vargroupvalues, varxvalues, varyvalues, weightvalue);
					}
				}
			}
		}
		data.close();
		if ((validgroup==0) && (where!=null))
			return new Result("%2804%<br>\n", false, null);
		if ((validgroup==0) && (where==null))
			return new Result("%666%<br>\n", false, null);

		ereg.estimate();

		Hashtable<Vector<String>, String> emsg=ereg.geterrormsgs();

		if (ereg.getError()==1)
		{
			if (ereg.getnumg()==1)
			{
				for (Enumeration<Vector<String>> esg = emsg.keys() ; esg.hasMoreElements() ;)
				{
					Vector<String> gv= esg.nextElement();
					String msg=emsg.get(gv);
					return new Result(msg, false, null);
				}
			}
			else
			{
				String vargref="";
				for (Enumeration<Vector<String>> esg = emsg.keys() ; esg.hasMoreElements() ;)
				{
					Vector<String> gv= esg.nextElement();
					String msg=emsg.get(gv);
					for (int i=0; i<gv.size(); i++)
					{
						vargref=vargref+gv.get(i);
						if (i<(gv.size()-1))
							vargref=vargref+"-";
					}
					vargref=vargref+"\n "+msg+"\n";
				}
				return new Result("%1633%<br>\n"+vargref, false, null);
			}
		}

		else if (ereg.getError()==0)
		{
			for (Enumeration<Vector<String>> esg = emsg.keys() ; esg.hasMoreElements() ;)
			{
				Vector<String> gv= esg.nextElement();
				String msg=emsg.get(gv);
				String vargref="";
				for (int i=0; i<gv.size(); i++)
				{
					vargref=vargref+gv.get(i);
					if (i<(gv.size()-1))
						vargref=vargref+"-";
				}
				result.add(new LocalMessageGetter(vargref+" "+msg));
			}
		}

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
			varyvalues=vp.getcolvarasdouble(values);
			weightvalue=vp.getweight(values);
			if ((vp.vargroupisnotmissing(vargroupvalues)) && (!Double.isNaN(varyvalues[0])) && (!Double.isNaN(weightvalue)))
			{
				validgroup++;
				vgm.updateModalities(vargroupvalues);
				ereg.regstat(vargroupvalues, varxvalues, varyvalues, weightvalue);
			}
		}
		data.close();
		ereg.evaluatestat(varx.length);

		vgm.calculate();

		Vector<Hashtable<String, String>> groupcodelabels=vgm.getgroupcodelabels();

		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);

		Hashtable<String, String> tempmd=new Hashtable<String, String>();

		for (int j=0; j<varg.length; j++)
		{
			if (!noclforvg)
				dsu.addnewvarfromolddict(dict, varg[j], groupcodelabels.get(j), tempmd, "g_"+varg[j]);
			else
				dsu.addnewvarfromolddict(dict, varg[j], tempmd, tempmd, "g_"+varg[j]);
		}

		Hashtable<String, String> clvar=new Hashtable<String, String>();
		for (int j=0; j<varx.length; j++)
		{
			clvar.put(varx[j], dict.getvarlabelfromname(varx[j]));
		}
		if (!noint)
			clvar.put("0","%1108%");
		dsu.addnewvar("varx", "%1109%", Keywords.TEXTSuffix, clvar, tempmd);
		dsu.addnewvar("value", "%1110%", Keywords.NUMSuffix, tempmd, tempmd);
		dsu.addnewvar("stderr", "%1111%", Keywords.NUMSuffix, tempmd, tempmd);
		dsu.addnewvar("tval", "%1112%", Keywords.NUMSuffix, tempmd, tempmd);
		dsu.addnewvar("ptval", "%1113%", Keywords.NUMSuffix, tempmd, tempmd);

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		int totalgroupmodalities=vgm.getTotal();

		if (totalgroupmodalities==0)
			totalgroupmodalities=1;

		DataSetUtilities cdsu=new DataSetUtilities();
		cdsu.setreplace(replace);
		if (isoutc)
		{
			Hashtable<Vector<String>, double[][]> covb=ereg.getcovb();
			for (int j=0; j<varg.length; j++)
			{
				if (!noclforvg)
					cdsu.addnewvarfromolddict(dict, varg[j], groupcodelabels.get(j), tempmd, "g_"+varg[j]);
				else
					cdsu.addnewvarfromolddict(dict, varg[j], tempmd, tempmd, "g_"+varg[j]);
			}
			Hashtable<String, String> clcovb=new Hashtable<String, String>();
			for (int j=0; j<varx.length; j++)
			{
				clcovb.put(varx[j], dict.getvarlabelfromname(varx[j]));
			}
			if (!noint)
				clcovb.put("0","%1108%");
			cdsu.addnewvar("varx", "%1109%", Keywords.TEXTSuffix, clcovb, tempmd);
			for (int i=0; i<varx.length; i++)
			{
				cdsu.addnewvar("v_"+varx[i], dict.getvarlabelfromname(varx[i]), Keywords.NUMSuffix, tempmd, tempmd);
			}
			if (!noint)
				cdsu.addnewvar("v_0", "%1108%", Keywords.NUMSuffix, tempmd, tempmd);

			if (!dwc.opendatatable(cdsu.getfinalvarinfo()))
				return new Result(dwc.getmessage(), false, null);

			for (int i=0; i<totalgroupmodalities; i++)
			{
				Vector<String> rifmodgroup=vgm.getvectormodalities(i);
				if (emsg.get(rifmodgroup)==null)
				{
					double[][] tempcovb=covb.get(rifmodgroup);
					String[] valuestowrite=new String[varg.length+1+tempcovb[0].length];
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
					for (int k=0; k<tempcovb.length; k++)
					{
						if (!noint)
						{
							if (k<tempcovb.length-1)
								valuestowrite[varg.length]=varx[k];
							else
								valuestowrite[varg.length]="0";
						}
						else
							valuestowrite[varg.length]=varx[k];
						for (int j=0; j<tempcovb[0].length; j++)
						{
							valuestowrite[varg.length+1+j]=double2String(tempcovb[k][j]);;
						}
						dwc.write(valuestowrite);
					}
				}
			}

		}

		DataSetUtilities idsu=new DataSetUtilities();
		idsu.setreplace(replace);
		for (int j=0; j<varg.length; j++)
		{
			if (!noclforvg)
				idsu.addnewvarfromolddict(dict, varg[j], groupcodelabels.get(j), tempmd, "g_"+varg[j]);
			else
				idsu.addnewvarfromolddict(dict, varg[j], tempmd, tempmd, "g_"+varg[j]);
		}

		Hashtable<String, String> clt=new Hashtable<String, String>();
		clt.put("0", "%1114%");
		clt.put("1", "%1115%");
		clt.put("2", "%1116%");
		clt.put("3", "%1117%");
		clt.put("4", "%1118%");
		clt.put("5", "%1119%");
		clt.put("6", "%1120%");
		clt.put("7", "%1121%");
		clt.put("8", "%1122%");
		idsu.addnewvar("Info", "%1123%", Keywords.TEXTSuffix, clt, tempmd);
		idsu.addnewvar("value", "%1124%", Keywords.NUMSuffix, tempmd, tempmd);

		if (!dwi.opendatatable(idsu.getfinalvarinfo()))
			return new Result(dwi.getmessage(), false, null);

		Hashtable<Vector<String>, double[]> coeff=ereg.getresult();
		Hashtable<Vector<String>, double[]> stderr=ereg.getsdterr();
		Hashtable<Vector<String>, Double> n=ereg.getn();
		Hashtable<Vector<String>, Double> depmean=ereg.getymean();
		Hashtable<Vector<String>, Double> sse=ereg.getsse();
		Hashtable<Vector<String>, Double> modelss=ereg.getmodelss();

		for (int i=0; i<totalgroupmodalities; i++)
		{
			String[] valuestowrite=new String[varg.length+5];
			Vector<String> rifmodgroup=vgm.getvectormodalities(i);
			if (emsg.get(rifmodgroup)==null)
			{
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
				double[] tempcoeff=coeff.get(rifmodgroup);
				double[] tempstde=stderr.get(rifmodgroup);
				double num=(n.get(rifmodgroup)).doubleValue();
				for (int j=0; j<tempcoeff.length; j++)
				{
					if (noint)
						valuestowrite[varg.length]=varx[j];
					else
					{
						if (j<tempcoeff.length-1)
							valuestowrite[varg.length]=varx[j];
						else
							valuestowrite[varg.length]="0";
					}
					valuestowrite[varg.length+1]=double2String(tempcoeff[j]);
					valuestowrite[varg.length+2]=double2String(tempstde[j]);
					if (!Double.isNaN(tempcoeff[j]/tempstde[j]))
						valuestowrite[varg.length+3]=double2String(tempcoeff[j]/tempstde[j]);
					else
						valuestowrite[varg.length+3]="";
					try
					{
						double tempval=(1-(Probability.studentT(num-tempcoeff.length,Math.abs(tempcoeff[j]/tempstde[j]))))*2;
						valuestowrite[varg.length+4]=double2String(tempval);
					}
					catch (Exception ex)
					{
						valuestowrite[varg.length+4]="";
					}
					dw.write(valuestowrite);
				}
			}
		}


		for (int i=0; i<totalgroupmodalities; i++)
		{
			String[] valuestowrite=new String[varg.length+2];
			Vector<String> rifmodgroup=vgm.getvectormodalities(i);
			if (emsg.get(rifmodgroup)==null)
			{
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
				double num=(n.get(rifmodgroup)).doubleValue();
				double depsse=(sse.get(rifmodgroup)).doubleValue();
				double depss=(modelss.get(rifmodgroup)).doubleValue();
				double ymean=(depmean.get(rifmodgroup)).doubleValue();
				valuestowrite[varg.length]="0";
				valuestowrite[varg.length+1]=double2String(num);
				dwi.write(valuestowrite);
				valuestowrite[varg.length]="1";
				valuestowrite[varg.length+1]=double2String(varx.length);
				dwi.write(valuestowrite);
				valuestowrite[varg.length]="2";
				valuestowrite[varg.length+1]=double2String(num-varx.length-1);
				dwi.write(valuestowrite);
				valuestowrite[varg.length]="3";
				valuestowrite[varg.length+1]=double2String(depsse);
				dwi.write(valuestowrite);
				valuestowrite[varg.length]="4";
				valuestowrite[varg.length+1]=double2String(depss);
				dwi.write(valuestowrite);
				double fisher=(depss/varx.length)/(depsse/(num-varx.length-1));
				valuestowrite[varg.length]="5";
				valuestowrite[varg.length+1]=double2String(fisher);
				dwi.write(valuestowrite);

				valuestowrite[varg.length]="6";
				valuestowrite[varg.length+1]="";
				try
				{
					double a=varx.length;
					double b=((Double)(num-varx.length-1)).doubleValue();
					double valf=1/(1+(a/b)*fisher);
					double testf=Probability.betaComplemented(a/2, b/2, valf);
					valuestowrite[varg.length+1]=double2String(testf);
				}
				catch (Exception ee){}
				dwi.write(valuestowrite);

				valuestowrite[varg.length]="7";
				valuestowrite[varg.length+1]=double2String(ymean);
				dwi.write(valuestowrite);
				double rsquare=depss/(depss+depsse);
				valuestowrite[varg.length]="8";
				valuestowrite[varg.length+1]=double2String(rsquare);
				dwi.write(valuestowrite);
			}
		}

		Hashtable<String, String> othertableinfo=new Hashtable<String, String>();
		othertableinfo.put(Keywords.vary, tempvary);

		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		resclose=dwi.close();
		if (!resclose)
			return new Result(dwi.getmessage(), false, null);
		if (isoutc)
		{
			resclose=dwc.close();
			if (!resclose)
				return new Result(dwc.getmessage(), false, null);
			Vector<Hashtable<String, String>> ctablevariableinfo=dwc.getVarInfo();
			Hashtable<String, String> cdatatableinfo=dwc.getTableInfo();
			result.add(new LocalDictionaryWriter(dwc.getdictpath(), keyword, cdescription, author, dwc.gettabletype(),
			cdatatableinfo, cdsu.getfinalvarinfo(), ctablevariableinfo, cdsu.getfinalcl(), cdsu.getfinalmd(), null));
		}

		Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
		Hashtable<String, String> datatableinfo=dw.getTableInfo();
		Vector<Hashtable<String, String>> itablevariableinfo=dwi.getVarInfo();
		Hashtable<String, String> idatatableinfo=dwi.getTableInfo();

		result.add(new LocalDictionaryWriter(dw.getdictpath(), keyword, description, author, dw.gettabletype(),
		datatableinfo, dsu.getfinalvarinfo(), tablevariableinfo, dsu.getfinalcl(), dsu.getfinalmd(), othertableinfo));
		result.add(new LocalDictionaryWriter(dwi.getdictpath(), keyword, idescription, author, dwi.gettabletype(),
		idatatableinfo, idsu.getfinalvarinfo(), itablevariableinfo, idsu.getfinalcl(), idsu.getfinalmd(), null));
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
		parameters.add(new GetRequiredParameters(Keywords.OUTE.toLowerCase()+"=", "setting=out", true, 890, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 896, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUTC.toLowerCase()+"=", "setting=out", false, 897, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.varx, "vars=all", true, 888, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vary, "var=all", true, 889, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.weight, "vars=all", false, 656, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.noint, "checkbox", false, 892, dep, "", 2));
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
		retprocinfo[0]="886";
		retprocinfo[1]="887";
		return retprocinfo;
	}
}
