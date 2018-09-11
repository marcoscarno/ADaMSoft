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

import ADaMSoft.algorithms.FuzzyRegEvaluator;
import ADaMSoft.algorithms.VarGroupModalities;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;

import ADaMSoft.dataaccess.GroupedMatrix2Dfile;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;

import ADaMSoft.keywords.Keywords;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

/**
* This is the procedure that implements a fuzzy linear regression model
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcFuzzyreg extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc FuzzyReg
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		boolean novgconvert=false;
		boolean noclforvg=false;
		boolean orderclbycode=false;
		String [] requiredparameters=new String[] {Keywords.OUTE.toLowerCase(), Keywords.dict, Keywords.varx, Keywords.varm, Keywords.varr, Keywords.varl, Keywords.varrmf, Keywords.varlmf, Keywords.iterations};
		String [] optionalparameters=new String[] {Keywords.OUTHIST.toLowerCase(), Keywords.where, Keywords.vargroup, Keywords.accuracy, Keywords.replace, Keywords.novgconvert, Keywords.noclforvg, Keywords.orderclbycode};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}

		String replace =(String)parameters.get(Keywords.replace);
		String accuracy =(String)parameters.get(Keywords.accuracy);
		String iter=(String)parameters.get(Keywords.iterations.toLowerCase());
		DataWriter dw=new DataWriter(parameters, Keywords.OUTE.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		novgconvert =(parameters.get(Keywords.novgconvert)!=null);
		orderclbycode =(parameters.get(Keywords.orderclbycode)!=null);
		noclforvg =(parameters.get(Keywords.noclforvg)!=null);

		DataWriter dwhist=null;
		if (parameters.get(Keywords.OUTHIST.toLowerCase())!=null)
		{
			dwhist=new DataWriter(parameters, Keywords.OUTHIST.toLowerCase());
			if (!dwhist.getmessage().equals(""))
				return new Result(dwhist.getmessage(), false, null);
		}

		int niter=string2int(iter);
		if (niter==0)
			return new Result("%1301%<br>\n", false, null);

		double ac=0.000001;
		if (accuracy!=null)
		{
			ac=string2double(accuracy);
			if (ac==0)
				return new Result("%1302%<br>\n", false, null);
		}

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String tempvarx=(String)parameters.get(Keywords.varx.toLowerCase());
		String tempvarm=(String)parameters.get(Keywords.varm.toLowerCase());
		String tempvarr=(String)parameters.get(Keywords.varr.toLowerCase());
		String tempvarl=(String)parameters.get(Keywords.varl.toLowerCase());
		String tempvarrmf=(String)parameters.get(Keywords.varrmf.toLowerCase());
		String tempvarlmf=(String)parameters.get(Keywords.varlmf.toLowerCase());

		String tempvargroup=(String)parameters.get(Keywords.vargroup.toLowerCase());

		String[] testv=tempvarm.split(" ");
		if (testv.length>1)
			return new Result("%1303%<br>\n", false, null);

		testv=tempvarr.split(" ");
		if (testv.length>1)
			return new Result("%1304%<br>\n", false, null);

		testv=tempvarl.split(" ");
		if (testv.length>1)
			return new Result("%1305%<br>\n", false, null);

		testv=tempvarrmf.split(" ");
		if (testv.length>1)
			return new Result("%1306%<br>\n", false, null);

		testv=tempvarlmf.split(" ");
		if (testv.length>1)
			return new Result("%1307%<br>\n", false, null);

		testv=tempvarx.split(" ");
		int nvarx=testv.length;

		String tempdir=(String)parameters.get(Keywords.WorkDir);

		FuzzyRegEvaluator fre=new FuzzyRegEvaluator(niter, nvarx+1, tempdir, ac);

		String variables=tempvarx+" "+tempvarm+" "+tempvarl+" "+tempvarr+" "+tempvarlmf+" "+tempvarrmf;

		VariableUtilities varu=new VariableUtilities(dict, tempvargroup, variables, null, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String[] varg=varu.getgroupvar();
		String[] varx=tempvarx.split(" ");

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

		GroupedMatrix2Dfile x=new GroupedMatrix2Dfile(tempdir, nvarx+1);
		GroupedMatrix2Dfile m=new GroupedMatrix2Dfile(tempdir, 1);
		GroupedMatrix2Dfile l=new GroupedMatrix2Dfile(tempdir, 1);
		GroupedMatrix2Dfile r=new GroupedMatrix2Dfile(tempdir, 1);
		GroupedMatrix2Dfile lam=new GroupedMatrix2Dfile(tempdir, 1);
		GroupedMatrix2Dfile rho=new GroupedMatrix2Dfile(tempdir, 1);

		String keyword="FuzzyReg "+dict.getkeyword();
		String description="FuzzyReg "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		String[] reqvar=varu.getreqvar();
		int[] replacerule=varu.getreplaceruleforsel(replace);

		int[] parsingrule=varu.getnormalruleforsel();

		DataReader data = new DataReader(dict);
		if (!data.open(reqvar, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		ValuesParser vp=new ValuesParser(parsingrule, null, null, null, null, null);

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

		boolean mfok=true;

		int validgroup=0;
		String[] values=null;
		Vector<String> vargroupvalues=new Vector<String>();
		double[] varxvalues=null;
		boolean ismd=false;
		double[] xval=null;
		double[] mval=new double[1];
		double[] lval=new double[1];
		double[] rval=new double[1];
		double[] lmfval=new double[1];
		double[] rmfval=new double[1];
		while (!data.isLast())
		{
			values= data.getRecord();
			if (values!=null)
			{
				if (novgconvert)
					vargroupvalues=vp.getorigvargroup(values);
				else
					vargroupvalues=vp.getvargroup(values);
				varxvalues=vp.getanalysisvarasdouble(values);
				if (vp.vargroupisnotmissing(vargroupvalues))
				{
					ismd=false;
					for (int i=0; i<varxvalues.length; i++)
					{
						if (Double.isNaN(varxvalues[i]))
							ismd=true;
					}
					if (!ismd)
					{
						xval=new double[nvarx+1];
						for (int i=0; i<nvarx; i++)
						{
							xval[i]=varxvalues[i];
						}
						xval[nvarx]=1;
						mval[0]=varxvalues[nvarx];
						lval[0]=varxvalues[nvarx+1];
						rval[0]=varxvalues[nvarx+2];
						lmfval[0]=varxvalues[nvarx+3];
						if ((varxvalues[nvarx+3]>=1) || (varxvalues[nvarx+3]<0))
							mfok=false;
						rmfval[0]=varxvalues[nvarx+4];
						if ((varxvalues[nvarx+4]>=1) || (varxvalues[nvarx+4]<0))
							mfok=false;
						x.write(vargroupvalues, xval);
						m.write(vargroupvalues, mval);
						l.write(vargroupvalues, lval);
						r.write(vargroupvalues, rval);
						lam.write(vargroupvalues, lmfval);
						rho.write(vargroupvalues, rmfval);
						validgroup++;
						vgm.updateModalities(vargroupvalues);
					}
				}
			}
		}
		data.close();
		if ((validgroup==0) && (where!=null))
		{
			x.closeAll();
			m.closeAll();
			r.closeAll();
			l.closeAll();
			rho.closeAll();
			lam.closeAll();
			return new Result("%2804%<br>\n", false, null);
		}
		if ((validgroup==0) && (where==null))
		{
			x.closeAll();
			m.closeAll();
			r.closeAll();
			l.closeAll();
			rho.closeAll();
			lam.closeAll();
			return new Result("%1308%<br>\n", false, null);
		}
		if (!mfok)
		{
			x.closeAll();
			m.closeAll();
			r.closeAll();
			l.closeAll();
			rho.closeAll();
			lam.closeAll();
			return new Result("%1326%<br>\n", false, null);
		}

		vgm.calculate();

		fre.setparam(vgm, x, m, l, r, lam, rho);

		fre.evaluate();

		x.closeAll();
		m.closeAll();
		r.closeAll();
		l.closeAll();
		rho.closeAll();
		lam.closeAll();

		boolean state=fre.getError();
		if (state)
		{
			String ermsg="%1309%<br>\n"+fre.getErrorMsg();
			return new Result(ermsg+"<br>\n", false, null);
		}

		int totalgroupmodalities=vgm.getTotal();

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
			clvar.put("v_"+varx[j], dict.getvarlabelfromname(varx[j]));
		}
		clvar.put("0","%1108%");
		clvar.put("1","%1320%");
		clvar.put("2","%1321%");
		clvar.put("3","%1322%");
		clvar.put("4","%1323%");
		dsu.addnewvar("ref", "%1319%", Keywords.TEXTSuffix, clvar, tempmd);
		dsu.addnewvar("value", "%1310%", Keywords.NUMSuffix, tempmd, tempmd);

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		if (totalgroupmodalities==0)
			totalgroupmodalities=1;

		Hashtable<Vector<String>, double[]> coeff=fre.getcoeff();
		Hashtable<Vector<String>, Double> bv=fre.getbv();
		Hashtable<Vector<String>, Double> dv=fre.getdv();
		Hashtable<Vector<String>, Double> gv=fre.getgv();
		Hashtable<Vector<String>, Double> hv=fre.gethv();

		String[] valuestowrite=new String[varg.length+2];

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
			double[] tempcoeff=coeff.get(rifmodgroup);
			for (int j=0; j<tempcoeff.length; j++)
			{
				if (j<tempcoeff.length-1)
					valuestowrite[varg.length]="v_"+varx[j];
				else
					valuestowrite[varg.length]="0";

				valuestowrite[varg.length+1]=double2String(tempcoeff[j]);

				dw.write(valuestowrite);
			}
			valuestowrite[varg.length]="1";
			double tempv=(bv.get(rifmodgroup)).doubleValue();
			valuestowrite[varg.length+1]=double2String(tempv);
			dw.write(valuestowrite);

			valuestowrite[varg.length]="2";
			tempv=(dv.get(rifmodgroup)).doubleValue();
			valuestowrite[varg.length+1]=double2String(tempv);
			dw.write(valuestowrite);

			valuestowrite[varg.length]="3";
			tempv=(gv.get(rifmodgroup)).doubleValue();
			valuestowrite[varg.length+1]=double2String(tempv);
			dw.write(valuestowrite);

			valuestowrite[varg.length]="4";
			tempv=(hv.get(rifmodgroup)).doubleValue();
			valuestowrite[varg.length+1]=double2String(tempv);
			dw.write(valuestowrite);
		}
		DataSetUtilities dsuh=null;
		if (dwhist!=null)
		{
			dsuh=new DataSetUtilities();
			for (int j=0; j<varg.length; j++)
			{
				if (!noclforvg)
					dsuh.addnewvarfromolddict(dict, varg[j], groupcodelabels.get(j), tempmd, "g_"+varg[j]);
				else
					dsuh.addnewvarfromolddict(dict, varg[j], tempmd, tempmd, "g_"+varg[j]);
			}
			for (int i=0; i<2; i++)
			{
				String lab="%978%_";
				if (i==1)
					lab="%1325%";
				dsuh.addnewvar("v"+(String.valueOf(i)), lab, Keywords.NUMSuffix, tempmd, tempmd);
			}
			if (!dwhist.opendatatable(dsuh.getfinalvarinfo()))
				return new Result(dwhist.getmessage(), false, null);
			Hashtable<Vector<String>, Vector<Double>> hist=fre.gethistory();
			for (int i=0; i<totalgroupmodalities; i++)
			{
				valuestowrite=new String[varg.length+2];
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
				Vector<Double> tempsse=hist.get(rifmodgroup);
				for (int j=0; j<tempsse.size(); j++)
				{
					valuestowrite[varg.length]=String.valueOf(j);
					valuestowrite[varg.length+1]="";
					try
					{
						double temphist=(tempsse.get(j)).doubleValue();
						if (!Double.isNaN(temphist))
							valuestowrite[varg.length+1]=String.valueOf(temphist);

					}
					catch (Exception e){}
					dwhist.write(valuestowrite);
				}
			}
		}

		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		if (dwhist!=null)
		{
			resclose=dwhist.close();
			if (!resclose)
				return new Result(dwhist.getmessage(), false, null);
			Vector<Hashtable<String, String>> tablevariableinfoh=dwhist.getVarInfo();
			Hashtable<String, String> datatableinfoh=dwhist.getTableInfo();
			result.add(new LocalDictionaryWriter(dwhist.getdictpath(), keyword, description, author, dwhist.gettabletype(),
			datatableinfoh, dsuh.getfinalvarinfo(), tablevariableinfoh, dsuh.getfinalcl(), dsuh.getfinalmd(), null));
		}

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
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 721, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUTE.toLowerCase()+"=", "setting=out", true, 890, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUTHIST.toLowerCase()+"=", "setting=out", false, 1324, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.varx, "vars=all", true, 888, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varm, "var=all", true, 1313, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varl, "var=all", true, 1315, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varr, "var=all", true, 1314, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varlmf, "var=all", true, 1317, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varrmf, "var=all", true, 1316, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.iterations, "text", true, 878, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.accuracy,"text", false, 1318,dep,"",2));
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
		retprocinfo[0]="1312";
		retprocinfo[1]="1311";
		return retprocinfo;
	}
}
