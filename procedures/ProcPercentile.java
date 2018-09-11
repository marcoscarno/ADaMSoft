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

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import ADaMSoft.algorithms.VarGroupModalities;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.GroupedTempDataSet;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.ValuesParser;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that evaluates the percentile of the variable
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcPercentile extends ObjectTransformer implements RunStep
{
	private double percentile;
	private GroupedTempDataSet gtds;
	private int analisysVarCount;
	private int divisions;

	/**
	* Starts the execution of Proc Percentile and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean novgconvert=false;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.var, Keywords.parts};
		String [] optionalparameters=new String[] {Keywords.vargroup, Keywords.weight, Keywords.where, Keywords.replace, Keywords.useinterpol, Keywords.novgconvert};
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

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		String vartemp=(String)parameters.get(Keywords.var.toLowerCase());
		String vargroup=(String)parameters.get(Keywords.vargroup.toLowerCase());
		String weight=(String)parameters.get(Keywords.weight.toLowerCase());
		String perc=(String)parameters.get(Keywords.parts.toLowerCase());
		boolean interpola=(parameters.get(Keywords.useinterpol)!=null);

		try
		{
			divisions = Integer.parseInt(perc);
			percentile=100/divisions;
		}
		catch(NumberFormatException e)
		{
			return new Result("%1867% (" +perc+")<br>\n", false, null);
		}

		VariableUtilities varu=new VariableUtilities(dict, vargroup, vartemp, weight, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);
		VarGroupModalities vgm=new VarGroupModalities();

		String[] var=varu.getanalysisvar();
		String[] varg=varu.getgroupvar();
		String[] totalvar=varu.getreqvar();
		analisysVarCount= var.length;

		String replace=(String)parameters.get(Keywords.replace);
		int[] replacerule=varu.getreplaceruleforsel(replace);

		String keyword="Percentil "+dict.getkeyword();
		String description="Percentil "+dict.getdescription();

		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		DataReader data = new DataReader(dict);

		if (!data.open(totalvar, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		int[] utilvarstype=varu.getnormalruleforsel();

		ValuesParser vp=new ValuesParser(utilvarstype, null, null, null, null, null);
		String tempdir=(String)parameters.get(Keywords.WorkDir);
		gtds = new GroupedTempDataSet(tempdir,var.length+1);

		String[] values;
		Vector<String> vargroupvalues=new Vector<String>();
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				if (novgconvert)
					vargroupvalues=vp.getorigvargroup(values);
				else
					vargroupvalues=vp.getvargroup(values);
				String[] varvalues=vp.getanalysisvar(values);
				double weightvalue=vp.getweight(values);
				if (vp.vargroupisnotmissing(vargroupvalues))
				{
					vgm.updateModalities(vargroupvalues);
					String[] row = new String[var.length+1];
					for(int i=0;i<varvalues.length;i++){
						row[i]=varvalues[i];
					}
					row[row.length-1]=String.valueOf(weightvalue);
					gtds.write(vargroupvalues, row);
				}
			}
		}
		data.close();
		gtds.finalizeWriteAll();
		vgm.calculate();
		int totalgroupmodalities=vgm.getTotal();
		if (totalgroupmodalities==0)
			totalgroupmodalities=1;

		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);

		Vector<Hashtable<String, String>> groupcodelabels=vgm.getgroupcodelabels();
		Hashtable<String, String> tempmd=new Hashtable<String, String>();
		int count=0;
		for (int j=0; j<varg.length; j++)
		{
			dsu.addnewvarfromolddict(dict, varg[j], groupcodelabels.get(j), tempmd, "g_"+varg[j]);
		}
		dsu.addnewvar("v"+count++, "%1868%", Keywords.TEXTSuffix, tempmd, tempmd);
		for (int i=0; i<analisysVarCount; i++)
		{
			dsu.addnewvar("v"+var[i], dict.getvarlabelfromname(var[i]), Keywords.NUMSuffix, tempmd, tempmd);
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		for (int i=0; i<totalgroupmodalities; i++)
		{
			Vector<String> rifmodgroup=vgm.getvectormodalities(i);
			String[] valuesToWrite= new String[varg.length+1+analisysVarCount];
			String[][] percentil=new String[divisions-1][analisysVarCount];
			for(int j=0;j<analisysVarCount;j++)
			{
				gtds.sortdataset(rifmodgroup, j);
				String[] percentile;
				if(interpola)
					percentile=calculatePercentileI(rifmodgroup, j);
				else
					percentile=calculatePercentileM(rifmodgroup, j);
				if(percentile==null)
				{
					if ((analisysVarCount==1) && (totalgroupmodalities==1))
					{
						gtds.deletetempdataAll();
						dw.deletetmp();
						return new Result("%1869%\n", false, null);
					}
					percentile= new String[divisions-1];
					for (int k=0; k<percentile.length; k++)
					{
						percentile[k]="";
					}
				}
				gtds.finalizeWrite(rifmodgroup);
				for (int k=0; k<percentile.length; k++)
				{
					percentil[k][j]=percentile[k];
				}
			}
			int validValues = rifmodgroup.get(0)==null?0:rifmodgroup.size();
			for(int k=0;k<validValues;k++)
			{
				valuesToWrite[k]= rifmodgroup.get(k);
			}
			for(int k=0;k<percentil.length;k++)
			{
				valuesToWrite[validValues]= String.valueOf(k+1);
				for (int j=0; j<percentil[0].length; j++)
				{
					valuesToWrite[validValues+1+j]= percentil[k][j];
				}
				dw.write(valuesToWrite);
			}
		}
		gtds.deletetempdataAll();

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
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 440, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", true, 641, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.weight, "var=all", false, 656, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.parts, "text", true, 1871, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.useinterpol, "checkbox", false, 1872, dep, "", 2));
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
		retprocinfo[0]="678";
		retprocinfo[1]="1870";
		return retprocinfo;
	}

	private String[]  calculatePercentileI(Vector<String> rifmodgroup, int rifvar)
	{
		String[] result= new String[divisions-1];
		int weightIndex = analisysVarCount;
		double count = gtds.getweight(rifmodgroup, analisysVarCount, rifvar);
		if(count<divisions)
		{
			return null;
		}
		String value[];
		while((value=gtds.read(rifmodgroup))[rifvar].equals(""));
		double read=Double.parseDouble(value[weightIndex]);
		String oldRead="0";
		boolean cont;
		for(int i=1;i<result.length+1;i++)
		{
			cont=true;
			while(cont)
			{
				if(read>=(count+1)*i*percentile/100)
				{
					if(read==(count+1)*i*percentile/100)
					{
						oldRead=value[rifvar];
					}
					double v = interpola(oldRead,value[rifvar],(count+1)*i*percentile/100);
					result[i-1]=""+v;
					cont=false;
				}
				try
				{
					oldRead=value[rifvar];
					value=gtds.read(rifmodgroup);
					read+=Double.parseDouble(value[weightIndex]);
				}
				catch (Exception eee)
				{
					cont=false;
				}
			}
		}
		return result;
	}
	private String[]  calculatePercentileM(Vector<String> rifmodgroup, int rifvar)
	{

		String[] result= new String[divisions-1];
		int weightIndex = analisysVarCount;

		double count = gtds.getweight(rifmodgroup, analisysVarCount, rifvar);
		if(count<divisions)
		{
			gtds.endread(rifmodgroup);
			return null;
		}
		String value[];
		while((value=gtds.read(rifmodgroup))[rifvar].equals(""));
		double read=Double.parseDouble(value[weightIndex]);
		String oldRead="0";
		boolean cont;
		for(int i=1;i<result.length+1;i++){
			cont=true;
			while(cont){
				if(read>=(count+1)*i*percentile/100){
					if(read==(count+1)*i*percentile/100){
						oldRead=value[rifvar];
					}
					double v = media(oldRead,value[rifvar]);
					result[i-1]=""+v;
					cont=false;
				}
				oldRead=value[rifvar];
				value=gtds.read(rifmodgroup);
				try
				{
					read+=Double.parseDouble(value[weightIndex]);
				}
				catch (Exception eee)
				{
					cont=false;
				}
			}
		}
		gtds.endread(rifmodgroup);
		return result;
	}

	private double interpola(String a, String b, double posa){

		double av= Double.parseDouble(a);
		double bv= Double.parseDouble(b);

		return av+(bv-av)*(posa-(int)posa);
	}

	private double media(String a, String b){

		double av= Double.parseDouble(a);
		double bv= Double.parseDouble(b);

		return (av+bv)/2;
	}
}