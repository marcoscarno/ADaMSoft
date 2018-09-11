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

import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.keywords.Keywords;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

/**
* This is the procedure that evaluates several indicators related to the result of one or more classification methods
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcEvalclass extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Evalclass
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

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String tempvarx=(String)parameters.get(Keywords.varpred.toLowerCase());
		String tempvary=(String)parameters.get(Keywords.vary.toLowerCase());
		String tempvargroup=(String)parameters.get(Keywords.vargroup.toLowerCase());
		String weight=(String)parameters.get(Keywords.weight.toLowerCase());

		novgconvert =(parameters.get(Keywords.novgconvert)!=null);
		orderclbycode =(parameters.get(Keywords.orderclbycode)!=null);
		noclforvg =(parameters.get(Keywords.noclforvg)!=null);

		String[] testdepvar=tempvary.split(" ");
		if (testdepvar.length>1)
			return new Result("%1812%\n", false, null);

		VariableUtilities varu=new VariableUtilities(dict, tempvargroup, null, weight, tempvarx, tempvary);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String keyword="Evalclass "+dict.getkeyword();
		String description="Evalclass "+dict.getdescription();
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

		Hashtable<Vector<String>, double[][][]> confusionmat=new Hashtable<Vector<String>, double[][][]>();

		int validgroup=0;
		String[] values=null;
		Vector<String> vargroupvalues=new Vector<String>();
		String[] varxvalues=null;
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
				varxvalues=vp.getrowvar(values);
				varyvalues=vp.getcolvar(values);
				weightvalue=vp.getweight(values);
				if ((vp.vargroupisnotmissing(vargroupvalues)) && (!varyvalues[0].equals("")) && (!Double.isNaN(weightvalue)))
				{
					if (confusionmat.get(vargroupvalues)==null)
					{
						double[][][] tempdist=new double[varx.length][2][2];
						for (int l=0; l<varxvalues.length; l++)
						{
							tempdist[l][0][0]=0;
							tempdist[l][0][1]=0;
							tempdist[l][1][0]=0;
							tempdist[l][1][1]=0;
						}
						confusionmat.put(vargroupvalues, tempdist);
					}
					double[][][] tdist=confusionmat.get(vargroupvalues);
					validgroup++;
					for (int i=0; i<varxvalues.length; i++)
					{
						if (!varxvalues[i].equals(""))
						{
							if ((varxvalues[i].equals("1")) && (varyvalues[0].equals("1")))
								tdist[i][1][1]=tdist[i][1][1]+weightvalue;
							if ((varxvalues[i].equals("0")) && (varyvalues[0].equals("1")))
								tdist[i][1][0]=tdist[i][1][0]+weightvalue;
							if ((varxvalues[i].equals("1")) && (varyvalues[0].equals("0")))
								tdist[i][0][1]=tdist[i][0][1]+weightvalue;
							if ((varxvalues[i].equals("0")) && (varyvalues[0].equals("0")))
								tdist[i][0][0]=tdist[i][0][0]+weightvalue;

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
			return new Result("%666%<br>\n", false, null);
		vgm.calculate();

		int totalgroupmodalities=vgm.getTotal();
		if (totalgroupmodalities==0)
			totalgroupmodalities=1;

		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);

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
		clvar.put("0","%1829%");
		clvar.put("1","%1830%");
		clvar.put("2","%1831%");
		clvar.put("3","%1832%");
		clvar.put("4","%1833%");
		clvar.put("5","%1834%");
		clvar.put("6","%1835%");
		clvar.put("7","%1836%");
		clvar.put("8","%1837%");
		clvar.put("9","%1838%");
		clvar.put("10","%1839%");
		clvar.put("11","%1840%");
		clvar.put("12","%1841%");
		clvar.put("13","%1842%");
		clvar.put("14","%1015%"); //Chi Quadro
		clvar.put("15","%1017%"); //Phi Quadro
		clvar.put("16","%1018%"); //Cramer V
		clvar.put("17","%1859%"); //Risk ratio
		clvar.put("18","%1860%"); //Odds ratio
		clvar.put("19","%1861%"); //Gamma
		clvar.put("20","%1862%"); //Risk difference

		dsu.addnewvar("indica", "%1828%", Keywords.TEXTSuffix, clvar, tempmd);

		for (int i=0; i<varx.length; i++)
		{
			dsu.addnewvar("v_"+varx[i], dict.getvarlabelfromname(varx[i]), Keywords.NUMSuffix, tempmd, tempmd);
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		String[] valuestowrite=new String[varg.length+1+varx.length];
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
			double[][][] tempv=confusionmat.get(rifmodgroup);
			for (int j=0; j<21; j++)
			{
				valuestowrite[varg.length]=String.valueOf(j);
				if (j==0)
				{
					for (int r=0; r<varx.length; r++)
					{
						valuestowrite[varg.length+r+1]=double2String(tempv[r][0][0]);
					}
				}
				else if (j==1)
				{
					for (int r=0; r<varx.length; r++)
					{
						valuestowrite[varg.length+r+1]=double2String(tempv[r][1][1]);
					}
				}
				else if (j==2)
				{
					for (int r=0; r<varx.length; r++)
					{
						valuestowrite[varg.length+r+1]=double2String(tempv[r][0][1]);
					}
				}
				else if (j==3)
				{
					for (int r=0; r<varx.length; r++)
					{
						valuestowrite[varg.length+r+1]=double2String(tempv[r][1][0]);
					}
				}
				else if (j==4)
				{
					for (int r=0; r<varx.length; r++)
					{
						if (tempv[r][0][0]+tempv[r][0][1]+tempv[r][1][0]+tempv[r][1][1]>0)
							valuestowrite[varg.length+r+1]=double2String((tempv[r][0][0]+tempv[r][1][1])/(tempv[r][0][0]+tempv[r][0][1]+tempv[r][1][0]+tempv[r][1][1]));
						else
							valuestowrite[varg.length+r+1]=double2String(0.0);
					}
				}
				else if (j==5)
				{
					for (int r=0; r<varx.length; r++)
					{
						if (tempv[r][0][0]+tempv[r][0][1]+tempv[r][1][0]+tempv[r][1][1]>0)
							valuestowrite[varg.length+r+1]=double2String((tempv[r][1][0]+tempv[r][0][1])/(tempv[r][0][0]+tempv[r][0][1]+tempv[r][1][0]+tempv[r][1][1]));
						else
							valuestowrite[varg.length+r+1]=double2String(0.0);
					}
				}
				else if (j==6)
				{
					for (int r=0; r<varx.length; r++)
					{
						if (tempv[r][1][1]+tempv[r][1][0]>0)
							valuestowrite[varg.length+r+1]=double2String(tempv[r][1][1]/(tempv[r][1][1]+tempv[r][1][0]));
						else
							valuestowrite[varg.length+r+1]=double2String(0.0);
					}
				}
				else if (j==7)
				{
					for (int r=0; r<varx.length; r++)
					{
						if (tempv[r][0][0]+tempv[r][0][1]>0)
							valuestowrite[varg.length+r+1]=double2String(tempv[r][0][1]/(tempv[r][0][0]+tempv[r][0][1]));
						else
							valuestowrite[varg.length+r+1]=double2String(0.0);
					}
				}
				else if (j==8)
				{
					for (int r=0; r<varx.length; r++)
					{
						if (tempv[r][0][0]+tempv[r][0][1]>0)
							valuestowrite[varg.length+r+1]=double2String(tempv[r][0][0]/(tempv[r][0][0]+tempv[r][0][1]));
						else
							valuestowrite[varg.length+r+1]=double2String(0.0);
					}
				}
				else if (j==9)
				{
					for (int r=0; r<varx.length; r++)
					{
						if (tempv[r][0][1]+tempv[r][1][1]>0)
							valuestowrite[varg.length+r+1]=double2String(tempv[r][1][1]/(tempv[r][0][1]+tempv[r][1][1]));
						else
							valuestowrite[varg.length+r+1]=double2String(0.0);
					}
				}
				else if (j==10)
				{
					for (int r=0; r<varx.length; r++)
					{
						if (tempv[r][0][0]+tempv[r][1][0]>0)
							valuestowrite[varg.length+r+1]=double2String(tempv[r][0][0]/(tempv[r][0][0]+tempv[r][1][0]));
						else
							valuestowrite[varg.length+r+1]=double2String(0.0);
					}
				}
				else if (j==11)
				{
					for (int r=0; r<varx.length; r++)
					{
						if (tempv[r][0][1]+tempv[r][1][1]>0)
							valuestowrite[varg.length+r+1]=double2String(tempv[r][0][1]/(tempv[r][0][1]+tempv[r][1][1]));
						else
							valuestowrite[varg.length+r+1]=double2String(0.0);
					}
				}
				else if (j==12)
				{
					for (int r=0; r<varx.length; r++)
					{
						double num=tempv[r][1][1]*tempv[r][0][0]-tempv[r][1][0]*tempv[r][0][1];
						double den=(tempv[r][0][0]+tempv[r][0][1])*(tempv[r][1][0]+tempv[r][1][1]);
						den=den*(tempv[r][0][0]+tempv[r][1][0])*(tempv[r][0][1]+tempv[r][1][1]);
						if (!Double.isNaN(den) && den!=0)
							valuestowrite[varg.length+r+1]=double2String(num/Math.sqrt(den));
						else
							valuestowrite[varg.length+r+1]=double2String(0.0);
					}
				}
				else if (j==13)
				{
					for (int r=0; r<varx.length; r++)
					{
						double num=tempv[r][0][0]/(tempv[r][0][0]+tempv[r][0][1]);
						num=num*tempv[r][1][1]/(tempv[r][1][1]+tempv[r][1][0]);
						double den=tempv[r][0][1]/(tempv[r][0][0]+tempv[r][0][1]);
						den=den*tempv[r][1][0]/(tempv[r][1][1]+tempv[r][1][0]);
						if (!Double.isNaN(den) && den!=0)
							valuestowrite[varg.length+r+1]=double2String(Math.abs(num-den));
						else
							valuestowrite[varg.length+r+1]=double2String(0.0);
					}
				}
				else if (j==14)
				{
					for (int r=0; r<varx.length; r++)
					{
						double totobs=tempv[r][0][0]+tempv[r][1][1]+tempv[r][1][0]+tempv[r][0][1];
						double t00=(tempv[r][0][0]+tempv[r][0][1])*(tempv[r][0][0]+tempv[r][1][0])/totobs;
						double t01=(tempv[r][0][0]+tempv[r][0][1])*(tempv[r][0][1]+tempv[r][1][1])/totobs;
						double t10=(tempv[r][0][0]+tempv[r][1][0])*(tempv[r][1][0]+tempv[r][1][1])/totobs;
						double t11=(tempv[r][1][0]+tempv[r][1][1])*(tempv[r][0][1]+tempv[r][1][1])/totobs;
						double chi=(Math.pow(tempv[r][0][0]-t00,2))/t00;
						chi=chi+(Math.pow(tempv[r][1][1]-t11,2))/t11;
						chi=chi+(Math.pow(tempv[r][1][0]-t10,2))/t10;
						chi=chi+(Math.pow(tempv[r][0][1]-t01,2))/t01;
						valuestowrite[varg.length+r+1]=double2String(chi);
					}
				}
				else if (j==15)
				{
					for (int r=0; r<varx.length; r++)
					{
						double totobs=tempv[r][0][0]+tempv[r][1][1]+tempv[r][1][0]+tempv[r][0][1];
						double t00=(tempv[r][0][0]+tempv[r][0][1])*(tempv[r][0][0]+tempv[r][1][0])/totobs;
						double t01=(tempv[r][0][0]+tempv[r][0][1])*(tempv[r][0][1]+tempv[r][1][1])/totobs;
						double t10=(tempv[r][0][0]+tempv[r][1][0])*(tempv[r][1][0]+tempv[r][1][1])/totobs;
						double t11=(tempv[r][1][0]+tempv[r][1][1])*(tempv[r][0][1]+tempv[r][1][1])/totobs;
						double chi=(Math.pow(tempv[r][0][0]-t00,2))/t00;
						chi=chi+(Math.pow(tempv[r][1][1]-t11,2))/t11;
						chi=chi+(Math.pow(tempv[r][1][0]-t10,2))/t10;
						chi=chi+(Math.pow(tempv[r][0][1]-t01,2))/t01;
						valuestowrite[varg.length+r+1]=double2String(chi/totobs);
					}
				}
				else if (j==16)
				{
					for (int r=0; r<varx.length; r++)
					{
						double totobs=tempv[r][0][0]+tempv[r][1][1]+tempv[r][1][0]+tempv[r][0][1];
						double t00=(tempv[r][0][0]+tempv[r][0][1])*(tempv[r][0][0]+tempv[r][1][0])/totobs;
						double t01=(tempv[r][0][0]+tempv[r][0][1])*(tempv[r][0][1]+tempv[r][1][1])/totobs;
						double t10=(tempv[r][0][0]+tempv[r][1][0])*(tempv[r][1][0]+tempv[r][1][1])/totobs;
						double t11=(tempv[r][1][0]+tempv[r][1][1])*(tempv[r][0][1]+tempv[r][1][1])/totobs;
						double chi=(Math.pow(tempv[r][0][0]-t00,2))/t00;
						chi=chi+(Math.pow(tempv[r][1][1]-t11,2))/t11;
						chi=chi+(Math.pow(tempv[r][1][0]-t10,2))/t10;
						chi=chi+(Math.pow(tempv[r][0][1]-t01,2))/t01;
						valuestowrite[varg.length+r+1]=double2String(Math.sqrt(chi/totobs));
					}
				}
				else if (j==17)
				{
					for (int r=0; r<varx.length; r++)
					{
						double rr=(tempv[r][1][1]/(tempv[r][1][1]+tempv[r][1][0]))/(1-tempv[r][0][0]/(tempv[r][0][0]+tempv[r][0][1]));
						valuestowrite[varg.length+r+1]=double2String(rr);
					}
				}
				else if (j==18)
				{
					for (int r=0; r<varx.length; r++)
					{
						double num=(tempv[r][1][1]/(tempv[r][1][1]+tempv[r][1][0]))*(tempv[r][0][0]/(tempv[r][0][0]+tempv[r][0][1]));
						double den=(1-tempv[r][1][1]/(tempv[r][1][1]+tempv[r][1][0]))*(1-tempv[r][0][0]/(tempv[r][0][0]+tempv[r][0][1]));
						valuestowrite[varg.length+r+1]=double2String(num/den);
					}
				}
				else if (j==19)
				{
					for (int r=0; r<varx.length; r++)
					{
						double num=(tempv[r][1][1]/(tempv[r][1][1]+tempv[r][1][0]))*(tempv[r][0][0]/(tempv[r][0][0]+tempv[r][0][1]));
						double den=(1-tempv[r][1][1]/(tempv[r][1][1]+tempv[r][1][0]))*(1-tempv[r][0][0]/(tempv[r][0][0]+tempv[r][0][1]));
						double gamma=((num/den)-1)/((num/den)+1);
						if (gamma<0)
							gamma=0;
						valuestowrite[varg.length+r+1]=double2String(gamma);
					}
				}
				else if (j==20)
				{
					for (int r=0; r<varx.length; r++)
					{
						double rd=(tempv[r][1][1]/(tempv[r][1][1]+tempv[r][1][0]))+(tempv[r][0][0]/(tempv[r][0][0]+tempv[r][0][1]))-1;
						valuestowrite[varg.length+r+1]=double2String(rd);
					}
				}
				dw.write(valuestowrite);
			}
		}

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
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 1848, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 1843, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.vary, "var=all", true, 1811, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varpred, "vars=all", true, 1827, dep, "", 2));
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
		retprocinfo[1]="1826";
		return retprocinfo;
	}
}
