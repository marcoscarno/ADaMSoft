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

import ADaMSoft.algorithms.MaxEvaluator;
import ADaMSoft.algorithms.MinEvaluator;
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
* This is the procedure that evaluates the Threshold of the estimate probabilities in order to minimize the classification error
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcNumvardivide extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Numvardivide
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		boolean novgconvert=false;
		boolean noclforvg=false;
		boolean orderclbycode=false;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.varnum, Keywords.varref};
		String [] optionalparameters=new String[] {Keywords.function, Keywords.vargroup, Keywords.where, Keywords.weight, Keywords.errortype, Keywords.replace, Keywords.OUTC.toLowerCase(), Keywords.novgconvert, Keywords.noclforvg, Keywords.orderclbycode};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		boolean isoutc =(parameters.get(Keywords.OUTC.toLowerCase())!=null);

		String usefunction=(String)parameters.get(Keywords.function.toLowerCase());
		if (usefunction==null)
			usefunction=Keywords.roc;

		novgconvert =(parameters.get(Keywords.novgconvert)!=null);
		orderclbycode =(parameters.get(Keywords.orderclbycode)!=null);
		noclforvg =(parameters.get(Keywords.noclforvg)!=null);

		String[] typefunc=new String[] {Keywords.roc, Keywords.roe};
		int valefunc=steputilities.CheckOption(typefunc, usefunction);
		if (valefunc==0)
			return new Result("%1775% "+Keywords.errortype.toUpperCase()+"<br>\n"+steputilities.getMessage(), false, null);

		String errortype=(String)parameters.get(Keywords.errortype.toLowerCase());
		if (errortype==null)
			errortype=Keywords.absvalue;
		int valef=0;
		if (valefunc==2)
		{
			String[] errors=new String[] {Keywords.absvalue, Keywords.quadratic, Keywords.logaritmic};
			valef=steputilities.CheckOption(errors, errortype);
			if (valef==0)
				return new Result("%1775% "+Keywords.errortype.toUpperCase()+"<br>\n"+steputilities.getMessage(), false, null);
		}

		String replace =(String)parameters.get(Keywords.replace);

		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		DataWriter dwc=null;
		if (isoutc)
		{
			dwc=new DataWriter(parameters, Keywords.OUTC.toLowerCase());
			if (!dwc.getmessage().equals(""))
				return new Result(dwc.getmessage(), false, null);
		}

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String tempvarx=(String)parameters.get(Keywords.varnum.toLowerCase());
		String tempvary=(String)parameters.get(Keywords.varref.toLowerCase());
		String tempvargroup=(String)parameters.get(Keywords.vargroup.toLowerCase());
		String weight=(String)parameters.get(Keywords.weight.toLowerCase());

		String[] testdepvar=tempvary.split(" ");
		if (testdepvar.length>1)
			return new Result("%1812%<br>\n", false, null);

		VariableUtilities varu=new VariableUtilities(dict, tempvargroup, null, weight, tempvarx, tempvary);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String keyword="Numvardivide "+dict.getkeyword();
		String description="Numvardivide "+dict.getdescription();
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
		MaxEvaluator emax=new MaxEvaluator();
		MinEvaluator emin=new MinEvaluator();

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
						return new Result("%1813%\n", false, null);
					}
					for (int i=0; i<varxvalues.length; i++)
					{
						if (!Double.isNaN(varxvalues[i]))
						{
							int rif=(checkclasses.get(varyvalues[0])).intValue();
							dist[i][rif]=dist[i][rif]+varxvalues[i]*weightvalue;
							num[i][rif]=num[i][rif]+weightvalue;
						}
					}
					emax.setValue(vargroupvalues, varxvalues);
					emin.setValue(vargroupvalues, varxvalues);
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

		Hashtable<Vector<String>, double[]> max=emax.getresult();
		Hashtable<Vector<String>, double[]> mini=emin.getresult();

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
		Hashtable<Vector<String>, double[]> thresholdvalues=new Hashtable<Vector<String>, double[]>();
		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);

		if (valefunc==2)
		{
			Hashtable<Vector<String>, double[][]> numclasses=new Hashtable<Vector<String>, double[][]>();
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
					double[] tmax=max.get(vargroupvalues);
					double[] tmin=mini.get(vargroupvalues);
					int rif=(checkclasses.get(varyvalues[0])).intValue();
					if (numclasses.get(vargroupvalues)==null)
					{
						double[][] tempdist=new double[varx.length][3];
						for (int l=0; l<varx.length; l++)
						{
							tempdist[l][0]=0;
							tempdist[l][1]=0;
							tempdist[l][2]=0;
						}
						numclasses.put(vargroupvalues, tempdist);
					}
					double[][] tdist=numclasses.get(vargroupvalues);
					for (int i=0; i<varxvalues.length; i++)
					{
						if (!Double.isNaN(varxvalues[i]))
						{
							if (order[i])
							{
								if (valef==1)
								{
									tdist[i][0]=tdist[i][0]+(varxvalues[i]-tmin[i])/(tmax[i]-tmin[i])*weightvalue;
									tdist[i][1]=tdist[i][1]+(varxvalues[i]-tmin[i])/(tmax[i]-tmin[i])*weightvalue*rif;
									tdist[i][2]=tdist[i][2]+rif;
								}
								else if (valef==2)
								{
									tdist[i][0]=tdist[i][0]+Math.pow((varxvalues[i]-tmin[i])/(tmax[i]-tmin[i]),2)*weightvalue;
									tdist[i][1]=tdist[i][1]+Math.pow((varxvalues[i]-tmin[i])/(tmax[i]-tmin[i]),2)*weightvalue*rif;
									tdist[i][2]=tdist[i][2]+rif;
								}
								else
								{
									tdist[i][0]=tdist[i][0]+Math.log(1+(varxvalues[i]-tmin[i])/(tmax[i]-tmin[i]))*weightvalue;
									tdist[i][1]=tdist[i][1]+Math.log(1+(varxvalues[i]-tmin[i])/(tmax[i]-tmin[i]))*weightvalue*rif;
									tdist[i][2]=tdist[i][2]+rif;
								}
							}
							else
							{
								if (valef==1)
								{
									tdist[i][0]=tdist[i][0]+(varxvalues[i]-tmin[i])/(tmax[i]-tmin[i])*weightvalue;
									tdist[i][1]=tdist[i][1]+(varxvalues[i]-tmin[i])/(tmax[i]-tmin[i])*weightvalue*(1-rif);
									tdist[i][2]=tdist[i][2]+(1-rif);
								}
								else if (valef==2)
								{
									tdist[i][0]=tdist[i][0]+Math.pow((varxvalues[i]-tmin[i])/(tmax[i]-tmin[i]),2)*weightvalue;
									tdist[i][1]=tdist[i][1]+Math.pow((varxvalues[i]-tmin[i])/(tmax[i]-tmin[i]),2)*weightvalue*(1-rif);
									tdist[i][2]=tdist[i][2]+(1-rif);
								}
								else
								{
									tdist[i][0]=tdist[i][0]+Math.log(1+(varxvalues[i]-tmin[i])/(tmax[i]-tmin[i]))*weightvalue;
									tdist[i][1]=tdist[i][1]+Math.log(1+(varxvalues[i]-tmin[i])/(tmax[i]-tmin[i]))*weightvalue*(1-rif);
									tdist[i][2]=tdist[i][2]+(1-rif);
								}
							}
						}
					}
					numclasses.put(vargroupvalues, tdist);
				}
			}
			data.close();

			int totalgroupmodalities=vgm.getTotal();
			if (totalgroupmodalities==0)
				totalgroupmodalities=1;

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

			dsu.addnewvar("v"+String.valueOf(varg.length), "%1901%", Keywords.TEXTSuffix, clvar, tempmd);
			dsu.addnewvar("v"+String.valueOf(varg.length+1), "%1822%", Keywords.NUMSuffix, tempmd, tempmd);

			if (!dw.opendatatable(dsu.getfinalvarinfo()))
				return new Result(dw.getmessage(), false, null);

			String[] valuestowrite=new String[varg.length+2];
			double v0=0;
			double v1=0;
			boolean donethresholdvalues=false;
			for (int r=0; r<varx.length; r++)
			{
				String[] valtowrite=new String[4];
				GroupedTempDataSet gtds=new GroupedTempDataSet(tempdir, 4);

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
						double[] tmax=max.get(vargroupvalues);
						double[] tmin=mini.get(vargroupvalues);
						if (isoutc)
						{
							if (!donethresholdvalues)
							{
								if (thresholdvalues.get(vargroupvalues)==null)
								{
									double[] thresholdval=new double[varx.length];
									for (int t=0; t<varx.length; t++)
									{
										thresholdval[t]=Double.NaN;
									}
									donethresholdvalues=true;
									thresholdvalues.put(vargroupvalues, thresholdval);
								}
							}
						}
						if (!Double.isNaN(varxvalues[r]))
						{
							varxvalues[r]=(varxvalues[r]-tmin[r])/(tmax[r]-tmin[r]);
							double[][] tdist=numclasses.get(vargroupvalues);
							int rif=(checkclasses.get(varyvalues[0])).intValue();
							valtowrite[0]=String.valueOf(varxvalues[r]);
							valtowrite[1]="";
							valtowrite[2]="";
							valtowrite[3]=String.valueOf(weightvalue);
							v0=Double.NaN;
							v1=Double.NaN;
							if (order[r])
							{
								if (valef==1)
								{
									v0=((varxvalues[r]-tmin[r])/(tmax[r]-tmin[r])-varxvalues[r]*rif)/(tdist[r][0]-tdist[r][1]);
									v1=(rif-varxvalues[r]*rif)/(tdist[r][2]-tdist[r][1]);
								}
								else if (valef==2)
								{
									v0=(Math.pow(varxvalues[r],2)-Math.pow(varxvalues[r],2)*rif)/(tdist[r][0]-tdist[r][1]);
									v1=(rif-Math.pow(varxvalues[r],2)*rif)/(tdist[r][2]-tdist[r][1]);
								}
								else
								{
									v0=(Math.log(1+varxvalues[r])-Math.log(1+varxvalues[r])*rif)/(tdist[r][0]-tdist[r][1]);
									v1=(rif-Math.log(1+varxvalues[r])*rif)/(tdist[r][2]-tdist[r][1]);
								}
							}
							else
							{
								if (valef==1)
								{
									v0=(varxvalues[r]-varxvalues[r]*(1-rif))/(tdist[r][0]-tdist[r][1]);
									v1=((1-rif)-varxvalues[r]*(1-rif))/(tdist[r][2]-tdist[r][1]);
								}
								else if (valef==2)
								{
									v0=(Math.pow(varxvalues[r],2)-Math.pow(varxvalues[r],2)*(1-rif))/(tdist[r][0]-tdist[r][1]);
									v1=((1-rif)-Math.pow(varxvalues[r],2)*(1-rif))/(tdist[r][2]-tdist[r][1]);
								}
								else
								{
									v0=(Math.log(1+varxvalues[r])-Math.log(1+varxvalues[r])*(1-rif))/(tdist[r][0]-tdist[r][1]);
									v1=((1-rif)-Math.log(1+varxvalues[r])*(1-rif))/(tdist[r][2]-tdist[r][1]);
								}
							}
							if (!Double.isNaN(v0))
								valtowrite[1]=String.valueOf(v0);
							if (!Double.isNaN(v1))
								valtowrite[2]=String.valueOf(v1);
							gtds.write(vargroupvalues, valtowrite);
						}
					}
				}
				data.close();
				gtds.finalizeWriteAll();
				if (gtds.geterrors())
				{
					gtds.deletetempdataAll();
						return new Result("%1814%<br>\n"+gtds.getMessage()+"<br>\n", false, null);
				}
				gtds.sortAll(0);
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
					double memvalpred=0;
					double memvalv0=0;
					double memvalv1=0;
					double tmemvalv0=0;
					double tmemvalv1=0;
					double mindiff=1;
					double rifmindiff=0;
					double actualdiff=0;

					double[] tmax=max.get(rifmodgroup);
					double[] tmin=mini.get(rifmodgroup);
					for (int k=0; k<gtds.getRows(rifmodgroup); k++)
					{
						String[] valread=gtds.read(rifmodgroup);
						double valpred=string2double(valread[0]);
						double valv0=string2double(valread[1]);
						double valv1=string2double(valread[2]);
						double numobs=string2double(valread[3]);
						if (k==0)
						{
							rifmindiff=valpred;
							mindiff=Math.abs(1-valv0*numobs-valv1*numobs);
							actualdiff=mindiff;
						}
						else
						{
							if (memvalpred!=valpred)
							{
								tmemvalv0=1-valv0*numobs-memvalv0;
								tmemvalv1=valv1*numobs+memvalv1;
								if (tmemvalv0<0.00000001)
									tmemvalv0=0;
								if (tmemvalv0>1)
									tmemvalv0=1;
								if (tmemvalv1<0.00000001)
									tmemvalv1=0;
								if (tmemvalv1>1)
									tmemvalv1=1;
								actualdiff=Math.abs(tmemvalv0-tmemvalv1);
							}
						}
						if (actualdiff<mindiff)
						{
							mindiff=actualdiff;
							rifmindiff=valpred;
						}
						memvalpred=valpred;
						memvalv0=memvalv0+valv0*numobs;
						memvalv1=memvalv1+valv1*numobs;
					}
					if (isoutc)
					{
						double[] thresholdval=thresholdvalues.get(vargroupvalues);
						thresholdval[r]=rifmindiff*(tmax[r]-tmin[r])+tmin[r];
						thresholdvalues.put(vargroupvalues, thresholdval);
					}
					valuestowrite[varg.length+1]=double2String(rifmindiff*(tmax[r]-tmin[r])+tmin[r]);
					dw.write(valuestowrite);
					gtds.endread(rifmodgroup);
				}
				gtds.deletetempdataAll();
			}
		}
		else
		{
			int totalgroupmodalities=vgm.getTotal();
			if (totalgroupmodalities==0)
				totalgroupmodalities=1;

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

			dsu.addnewvar("v"+String.valueOf(varg.length), "%1901%", Keywords.TEXTSuffix, clvar, tempmd);
			dsu.addnewvar("v"+String.valueOf(varg.length+1), "%1847%", Keywords.NUMSuffix, tempmd, tempmd);

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
						double[] tmax=max.get(vargroupvalues);
						double[] tmin=mini.get(vargroupvalues);
						if (!Double.isNaN(varxvalues[r]))
						{
							int rifr=(checkclasses.get(varyvalues[0])).intValue();
							varxvalues[r]=(varxvalues[r]-tmin[r])/(tmax[r]-tmin[r]);
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

			boolean donethresholdvalues=false;

			valuestowrite=new String[varg.length+2];

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
				if (isoutc)
				{
					if (!donethresholdvalues)
					{
						if (thresholdvalues.get(rifmodgroup)==null)
						{
							double[] thresholdval=new double[varx.length];
							for (int t=0; t<varx.length; t++)
							{
								thresholdval[t]=Double.NaN;
							}
							donethresholdvalues=true;
							thresholdvalues.put(vargroupvalues, thresholdval);
						}
					}
				}
				String oldvarx="";
				String actualvarx="";
				double maxhreshold=1;
				double rifmaxhreshold=0;
				int rifvarx=0;
				double score=Double.NaN;
				double sensi=Double.NaN;
				double speci=Double.NaN;
				double distance=Double.NaN;
				double[] tmax=max.get(rifmodgroup);
				double[] tmin=mini.get(rifmodgroup);
				for (int p=0; p<defgtds.getRows(rifmodgroup); p++)
				{
					score=Double.NaN;
					sensi=Double.NaN;
					speci=Double.NaN;
					String[] valread=defgtds.read(rifmodgroup);
					actualvarx=valread[0];
					if (oldvarx.equals(""))
						oldvarx=actualvarx;
					if (!actualvarx.equals(oldvarx))
					{
						valuestowrite[varg.length]=oldvarx;
						valuestowrite[varg.length+1]=double2String(rifmaxhreshold);
						if (isoutc)
						{
							double[] thresholdval=thresholdvalues.get(rifmodgroup);
							thresholdval[rifvarx]=rifmaxhreshold*(tmax[rifvarx]-tmin[rifvarx])+tmin[rifvarx];
							thresholdvalues.put(rifmodgroup, thresholdval);
						}
						rifvarx++;
						dw.write(valuestowrite);
						oldvarx=actualvarx;
						maxhreshold=1;
					}
					score=string2double(valread[1]);
					sensi=string2double(valread[2]);
					speci=string2double(valread[3]);
					if ((!Double.isNaN(score)) && (!Double.isNaN(sensi)) && (!Double.isNaN(speci)))
					{
						distance=Math.sqrt(Math.pow(speci-0,2)+Math.pow(sensi-1,2));
						if (distance<maxhreshold)
						{
							rifmaxhreshold=score;
							maxhreshold=distance;
						}
					}
				}
				valuestowrite[varg.length]=oldvarx;
				valuestowrite[varg.length+1]=double2String(rifmaxhreshold*(tmax[rifvarx]-tmin[rifvarx])+tmin[rifvarx]);
				if (isoutc)
				{
					double[] thresholdval=thresholdvalues.get(rifmodgroup);
					thresholdval[rifvarx]=rifmaxhreshold*(tmax[rifvarx]-tmin[rifvarx])+tmin[rifvarx];
					thresholdvalues.put(rifmodgroup, thresholdval);
				}
				dw.write(valuestowrite);
				defgtds.endread(rifmodgroup);
			}
			defgtds.deletetempdataAll();
		}
		DataSetUtilities dsuc=new DataSetUtilities();
		if (isoutc)
		{
			int tvar=dict.gettotalvar();
			int[] ordervg=new int[varg.length];
			int[] ordera=new int[varx.length];
			int[] replacevar=new int[tvar];
			int rifrep=0;
			if (replace==null)
				rifrep=0;
			else if (replace.equalsIgnoreCase(Keywords.replaceall))
				rifrep=1;
			else if (replace.equalsIgnoreCase(Keywords.replaceformat))
				rifrep=2;
			else if (replace.equalsIgnoreCase(Keywords.replacemissing))
				rifrep=3;
			for (int i=0; i<tvar; i++)
			{
				replacevar[i]=rifrep;
			}
			for (int i=0; i<varg.length; i++)
			{
				for (int j=0; j<tvar; j++)
				{
					if (varg[i].equalsIgnoreCase(dict.getvarname(j)))
					{
						ordervg[i]=j;
						replacevar[j]=1;
					}
				}
			}
			for (int i=0; i<varx.length; i++)
			{
				for (int j=0; j<tvar; j++)
				{
					if (varx[i].equalsIgnoreCase(dict.getvarname(j)))
						ordera[i]=j;
				}
			}
			int posy=0;
			for (int j=0; j<tvar; j++)
			{
				if (tempvary.trim().equalsIgnoreCase(dict.getvarname(j)))
					posy=j;
			}
			dsuc.setreplace(replace);
			Hashtable<String, String> temph=new Hashtable<String, String>();
			dsuc.defineolddict(dict);
			for (int i=0; i<varx.length; i++)
			{
				dsuc.addnewvartoolddict("real_value_"+tempvary.trim()+"_"+varx[i].trim(), "%1824% ("+dict.getvarlabelfromname(tempvary)+"-"+dict.getvarlabelfromname(varx[i])+")", Keywords.NUMSuffix, temph, temph);
				dsuc.addnewvartoolddict("pred_value_"+varx[i].trim(), "%1902% ("+dict.getvarlabelfromname(varx[i])+")", Keywords.NUMSuffix, temph, temph);
			}
			if (!dwc.opendatatable(dsuc.getfinalvarinfo()))
				return new Result(dwc.getmessage(), false, null);

			boolean vgmiss=false;

			if (!data.open(null, replacevar, false))
				return new Result(data.getmessage(), false, null);
			double realnumvg=Double.NaN;
			String realvgval="";
			while (!data.isLast())
			{
				vgmiss=false;
				values = data.getRecord();
				Vector<String> vg=new Vector<String>();
				if (varg.length==0)
					vg.add(null);
				else
				{
					realnumvg=Double.NaN;
					for (int i=0; i<ordervg.length; i++)
					{
						realvgval=values[ordervg[i]].trim();
						if (!novgconvert)
						{
							try
							{
								realnumvg=Double.parseDouble(realvgval);
								if (!Double.isNaN(realnumvg))
									realvgval=String.valueOf(realnumvg);
							}
							catch (Exception e) {}
						}
						if (realvgval.equals(""))
							vgmiss=true;
						vg.add(realvgval);
					}
				}
				String[] newvalues=new String[varx.length*2];
				for (int i=0; i<varx.length*2; i++)
				{
					newvalues[i]="";
				}
				if (!vgmiss)
				{
					if (checkclasses.get(values[posy])!=null)
					{
						double[] thresholdval=thresholdvalues.get(vg);
						int rif=(checkclasses.get(values[posy])).intValue();
						for (int i=0; i<varx.length; i++)
						{
							double aval=Double.NaN;
							try
							{
								aval=Double.parseDouble(values[ordera[i]]);
							}
							catch (Exception e) {}
							if (!Double.isNaN(aval))
							{
								if (aval>thresholdval[i])
									newvalues[i*2+1]="1";
								else
									newvalues[i*2+1]="0";
							}
							if (order[i])
								newvalues[i*2]=String.valueOf(rif);
							else
								newvalues[i*2]=String.valueOf(Math.abs(rif-1));
						}
					}

				}
				String[] wvalues=dsuc.getnewvalues(values, newvalues);
				dwc.write(wvalues);
			}
			data.close();
		}

		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
		Hashtable<String, String> datatableinfo=dw.getTableInfo();
		result.add(new LocalDictionaryWriter(dw.getdictpath(), keyword, description, author, dw.gettabletype(),
		datatableinfo, dsu.getfinalvarinfo(), tablevariableinfo, dsu.getfinalcl(), dsu.getfinalmd(), null));
		if (isoutc)
		{
			resclose=dwc.close();
			if (!resclose)
				return new Result(dwc.getmessage(), false, null);
			Vector<Hashtable<String, String>> tablevariableinfoc=dwc.getVarInfo();
			Hashtable<String, String> datatableinfoc=dwc.getTableInfo();
			result.add(new LocalDictionaryWriter(dwc.getdictpath(), keyword, description, author, dwc.gettabletype(),
			datatableinfoc, dsuc.getfinalvarinfo(), tablevariableinfoc, dsuc.getfinalcl(), dsuc.getfinalmd(), null));
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
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 1809, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 1821, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUTC.toLowerCase()+"=", "setting=out", false, 1823, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.varnum, "vars=all", true, 1890, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varref, "var=all", true, 1891, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.weight, "vars=all", false, 656, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.function, "listsingle=1886_"+Keywords.roc+",1887_"+Keywords.roe, false, 1888, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.errortype, "listsingle=1864_"+Keywords.absvalue+",1865_"+Keywords.quadratic+",1866_"+Keywords.logaritmic, false, 1863, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 1889, dep, "", 2));
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
		retprocinfo[0]="4163";
		retprocinfo[1]="1885";
		return retprocinfo;
	}
}
