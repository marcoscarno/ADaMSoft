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

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math3.linear.*;

/**
* This is the procedure that implements the Ordinary Least Square linear regression model
* @author marco.scarno@gmail.com
* @date 16/10/2017
*/
public class ProcOlsmultiplelinearregression extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Olsmultiplelinearregression
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
		String [] optionalparameters=new String[] {Keywords.vargroup, Keywords.where, Keywords.replace, Keywords.novgconvert, Keywords.noclforvg, Keywords.orderclbycode};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		pairwise =false;

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

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String tempvarx=(String)parameters.get(Keywords.varx.toLowerCase());
		String tempvary=(String)parameters.get(Keywords.vary.toLowerCase());
		String tempvargroup=(String)parameters.get(Keywords.vargroup.toLowerCase());
		String weight=null;

		String[] testdepvar=tempvary.split(" ");
		if (testdepvar.length>1)
			return new Result("%1125%<br>\n", false, null);

		VariableUtilities varu=new VariableUtilities(dict, tempvargroup, null, weight, tempvarx, tempvary);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String keyword="OlsMultiLinReg "+dict.getkeyword();
		String description="OlsMultiLinReg "+dict.getdescription();
		String idescription="OlsMultiLinReg: information "+dict.getdescription();
		String cdescription="OlsMultiLinReg: covb "+dict.getdescription();
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

		int validgroup=0;
		String[] values=null;
		Vector<String> vargroupvalues=new Vector<String>();
		double[] varxvalues=null;
		double[] varyvalues=null;
		int validx=0;
		Hashtable<Vector<String>, Vector<Double>> mat_y=new Hashtable<Vector<String>, Vector<Double>>();
		Hashtable<Vector<String>, Vector<double[]>> mat_x=new Hashtable<Vector<String>, Vector<double[]>>();
		Hashtable<Vector<String>, Double> valid_obs=new Hashtable<Vector<String>, Double>();
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
				if ((vp.vargroupisnotmissing(vargroupvalues)) && (!Double.isNaN(varyvalues[0])))
				{
					validx=0;
					for (int i=0; i<varxvalues.length; i++)
					{
						if (Double.isNaN(varxvalues[i])) validx++;
					}
					if (validx==0)
					{
						validgroup++;
						vgm.updateModalities(vargroupvalues);
						Vector<String> temp_vargroupvalues=new Vector<String>();
						for (int i=0; i<vargroupvalues.size(); i++)
						{
							temp_vargroupvalues.add(vargroupvalues.get(i));
						}
						if (mat_y.get(temp_vargroupvalues)!=null)
						{
							Vector<Double> ty=mat_y.get(temp_vargroupvalues);
							double tty=varyvalues[0];
							ty.add(new Double(tty));
							double[] ttx=new double[varxvalues.length];
							for (int i=0; i<ttx.length; i++)
							{
								ttx[i]=varxvalues[i];
							}
							Vector<double[]> tx=mat_x.get(temp_vargroupvalues);
							tx.add(ttx);
							mat_y.put(temp_vargroupvalues, ty);
							mat_x.put(temp_vargroupvalues, tx);
							double curtty=((Double)valid_obs.get(temp_vargroupvalues)).doubleValue();
							valid_obs.put(temp_vargroupvalues, new Double(curtty+1));
						}
						if (mat_y.get(temp_vargroupvalues)==null)
						{
							Vector<Double> ty=new Vector<Double>();
							double tty=varyvalues[0];
							ty.add(new Double(tty));
							double[] ttx=new double[varxvalues.length];
							for (int i=0; i<ttx.length; i++)
							{
								ttx[i]=varxvalues[i];
							}
							Vector<double[]> tx=new Vector<double[]>();
							tx.add(ttx);
							mat_y.put(temp_vargroupvalues, ty);
							mat_x.put(temp_vargroupvalues, tx);
							valid_obs.put(temp_vargroupvalues, new Double(1));
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
		clvar.put("0","%1108%");
		for (int j=0; j<varx.length; j++)
		{
			clvar.put(varx[j], dict.getvarlabelfromname(varx[j]));
		}
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
		clt.put("8", "%1122%");
		idsu.addnewvar("Info", "%1123%", Keywords.TEXTSuffix, clt, tempmd);
		idsu.addnewvar("value", "%1124%", Keywords.NUMSuffix, tempmd, tempmd);

		if (!dwi.opendatatable(idsu.getfinalvarinfo()))
			return new Result(dwi.getmessage(), false, null);

		double[] txx=new double[varx.length];
		try
		{
			for (int i=0; i<totalgroupmodalities; i++)
			{
				String[] valuestowrite1=new String[varg.length+5];
				String[] valuestowrite2=new String[varg.length+2];
				Vector<String> rifmodgroup=vgm.getvectormodalities(i);
				for (int j=0; j<rifmodgroup.size(); j++)
				{
					String groupvalue=rifmodgroup.get(j);
					if (groupvalue!=null)
					{
						if (!noclforvg)
						{
							valuestowrite1[j]=vgm.getcode(j, groupvalue);
							valuestowrite2[j]=vgm.getcode(j, groupvalue);
						}
						else
						{
							valuestowrite1[j]=groupvalue;
							valuestowrite2[j]=groupvalue;
						}
					}
				}
				Vector<Double> temp_y=mat_y.get(rifmodgroup);
				double[] dep=new double[temp_y.size()];
				double[][] indep=new double[temp_y.size()][varx.length];
				Vector<double[]> temp_x=mat_x.get(rifmodgroup);
				for (int j=0; j<temp_y.size(); j++)
				{
					dep[j]=((Double)temp_y.get(j)).doubleValue();
					txx=temp_x.get(j);
					for (int k=0; k<txx.length; k++)
					{
						indep[j][k]=txx[k];
					}
				}
				OLSMultipleLinearRegression regression = new OLSMultipleLinearRegression();
				regression.newSampleData(dep, indep);
				double[] beta = regression.estimateRegressionParameters();
				double[] std_err=regression.estimateRegressionParametersStandardErrors();
				double rSquared = regression.calculateRSquared();
				double num_obs=((Double)valid_obs.get(rifmodgroup)).doubleValue();
 				for (int j=0; j<beta.length; j++)
				{
					if (j==0) valuestowrite1[varg.length]="0";
					if (j>0) valuestowrite1[varg.length]=varx[j-1];
					valuestowrite1[varg.length+1]=double2String(beta[j]);
					double tempstde=Math.sqrt(std_err[j]);
					valuestowrite1[varg.length+2]=double2String(tempstde);
					valuestowrite1[varg.length+3]=double2String(beta[j]/tempstde);
					double tempval=(1-(Probability.studentT(num_obs-beta.length,Math.abs(beta[j]/tempstde))))*2;
					valuestowrite1[varg.length+4]=double2String(tempval);
					dw.write(valuestowrite1);
				}
				valuestowrite2[varg.length]="0";
				valuestowrite2[varg.length+1]=double2String(num_obs);
				dwi.write(valuestowrite2);
				valuestowrite2[varg.length]="8";
				valuestowrite2[varg.length+1]=double2String(rSquared);
				dwi.write(valuestowrite2);
			}
		}
		catch (Exception varex)
		{
			dw.deletetmp();
			dwi.deletetmp();
			return new Result(varex.toString()+"<br>\n", false, null);
		}

		Hashtable<String, String> othertableinfo=new Hashtable<String, String>();
		othertableinfo.put(Keywords.vary, tempvary);

		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		resclose=dwi.close();
		if (!resclose)
			return new Result(dwi.getmessage(), false, null);

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
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.varx, "vars=all", true, 888, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vary, "var=all", true, 889, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, dep, "", 2));
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
		retprocinfo[0]="886";
		retprocinfo[1]="4203";
		return retprocinfo;
	}
}
