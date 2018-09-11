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
import ADaMSoft.dataaccess.DataWriter;

import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;

import ADaMSoft.keywords.Keywords;

import ADaMSoft.algorithms.NLFitting.ErrorMinimisator;
import ADaMSoft.algorithms.NLFitting.NormalFitting;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

/**
* This is the procedure that minimize a function
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcFitting extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Fitting and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.function, Keywords.depvar, Keywords.parameter};
		String [] optionalparameters=new String[] {Keywords.startvalue, Keywords.stepvalue, Keywords.constrain, Keywords.iterations, Keywords.tolerance, Keywords.simplexreflectioncoeff, Keywords.simplexextensioncoeff, Keywords.simplexcontractioncoeff, Keywords.replace};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		String replace =(String)parameters.get(Keywords.replace);

		double[][] constrainvalues=new double[0][0];

		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		double ftol = 1e-8;

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		String par=(String)parameters.get(Keywords.parameter);
		String depvar=(String)parameters.get(Keywords.depvar);
		String simplexreflectioncoeff=(String)parameters.get(Keywords.simplexreflectioncoeff);
		String simplexextensioncoeff=(String)parameters.get(Keywords.simplexextensioncoeff);
		String simplexcontractioncoeff=(String)parameters.get(Keywords.simplexcontractioncoeff);
		double simplexreflectioncoeffval=1;
		double simplexextensioncoeffval=2;
		double simplexcontractioncoeffval=0.5;
		if (simplexreflectioncoeff!=null)
		{
			simplexreflectioncoeffval=string2double(simplexreflectioncoeff);
			if (Double.isNaN(simplexreflectioncoeffval))
				return new Result("%1683%<br>\n", false, null);
		}
		if (simplexextensioncoeff!=null)
		{
			simplexextensioncoeffval=string2double(simplexextensioncoeff);
			if (Double.isNaN(simplexextensioncoeffval))
				return new Result("%1684%<br>\n", false, null);
		}
		if (simplexcontractioncoeff!=null)
		{
			simplexcontractioncoeffval=string2double(simplexcontractioncoeff);
			if (Double.isNaN(simplexcontractioncoeffval))
				return new Result("%1685%<br>\n", false, null);
		}

		String[] testdepvar=depvar.split(" ");
		if (testdepvar.length>1)
			return new Result("%1125%<br>\n", false, null);

		int niter=1000;
		String maxiter=(String)parameters.get(Keywords.iterations);
		if (maxiter!=null)
		{
			niter=string2int(maxiter);
			if (niter<0)
				return new Result("%1461%<br>\n", false, null);
		}
		String tolerance=(String)parameters.get(Keywords.tolerance);
		if (tolerance!=null)
		{
			ftol=string2int(tolerance);
			if (Double.isNaN(ftol))
				return new Result("%1652%<br>\n", false, null);
		}

		String[] parameterList=par.split(" ");

		String constrainparts =(String)parameters.get(Keywords.constrain);
		if (constrainparts!=null)
		{
			String[] constrain=constrainparts.split(";");
			constrainvalues=new double[constrain.length][3];
			for (int i=0; i<constrain.length; i++)
			{
				constrain[i]=constrain[i].trim();
				if (constrain[i].indexOf(" ")>0)
				{
					String[] tcon=constrain[i].split(" ");
					constrain[i]="";
					for (int j=0; j<tcon.length; j++)
					{
						constrain[i]=constrain[i]+tcon[j];
					}
				}
				if (constrain[i].indexOf(">")>0)
				{
					String[] temprifc=constrain[i].split(">");
					if (temprifc.length!=2)
						return new Result("%1677% ("+constrain[i]+")<br>\n", false, null);
					constrainvalues[i][0]=-1;
					for (int j=0; j<parameterList.length; j++)
					{
						if (parameterList[j].equalsIgnoreCase(temprifc[0]))
						{
							constrainvalues[i][0]=j;
							constrainvalues[i][1]=-1;
							try
							{
								constrainvalues[i][2]=Double.valueOf(temprifc[1]);
							}
							catch (Exception e)
							{
								return new Result("%1677% ("+constrain[i]+")<br>\n", false, null);
							}
						}
					}
					if (constrainvalues[i][0]==-1)
						return new Result("%1677% ("+constrain[i]+")<br>\n", false, null);
				}
				else if (constrain[i].indexOf("<")>0)
				{
					String[] temprifc=constrain[i].split("<");
					if (temprifc.length!=2)
						return new Result("%1677% ("+constrain[i]+")<br>\n", false, null);
					constrainvalues[i][0]=-1;
					for (int j=0; j<parameterList.length; j++)
					{
						if (parameterList[j].equalsIgnoreCase(temprifc[0]))
						{
							constrainvalues[i][0]=j;
							constrainvalues[i][1]=1;
							try
							{
								constrainvalues[i][2]=Double.valueOf(temprifc[1]);
							}
							catch (Exception e)
							{
								return new Result("%1677% ("+constrain[i]+")<br>\n", false, null);
							}
						}
					}
					if (constrainvalues[i][0]==-1)
						return new Result("%1677% ("+constrain[i]+")<br>\n", false, null);
				}
				else
					return new Result("%1677% ("+constrain[i]+")<br>\n", false, null);
			}
		}

		boolean parameterisvar=false;
		for (int i=0; i<dict.gettotalvar(); i++)
		{
			for (int j=0; j<parameterList.length; j++)
			{
				parameterList[j]=parameterList[j].trim();
				if (dict.getvarname(i).equalsIgnoreCase(parameterList[j]))
					parameterisvar=true;
			}
		}
		if (parameterisvar)
			return new Result("%1650%<br>\n", false, null);

		String function=(String)parameters.get(Keywords.function);
		String startparvalue=(String)parameters.get(Keywords.startvalue);
		String stepvalue=(String)parameters.get(Keywords.stepvalue);
		double[] start=new double[parameterList.length];
		for (int i=0; i<start.length; i++)
		{
			start[i]=1;
		}
		double[] step=new double[parameterList.length];
		for (int i=0; i<step.length; i++)
		{
			step[i]=.5;
		}
		if (startparvalue!=null)
		{
			startparvalue=startparvalue.trim();
			String[] tstartparvalue=startparvalue.split(" ");
			if (tstartparvalue.length!=parameterList.length)
				return new Result("%1653%<br>\n", false, null);
			boolean errortpar=false;
			for (int i=0; i<tstartparvalue.length; i++)
			{
				start[i]=string2double(tstartparvalue[i]);
				if (Double.isNaN(start[i]))
					errortpar=true;
			}
			if (errortpar)
				return new Result("%1654%<br>\n", false, null);
		}
		if (stepvalue!=null)
		{
			stepvalue=stepvalue.trim();
			String[] tstepvalue=stepvalue.split(" ");
			if (tstepvalue.length!=parameterList.length)
				return new Result("%1655%<br>\n", false, null);
			boolean errortpar=false;
			for (int i=0; i<tstepvalue.length; i++)
			{
				step[i]=string2double(tstepvalue[i]);
				if (Double.isNaN(step[i]))
					errortpar=true;
				if (step[i]==0)
					errortpar=true;
			}
			if (errortpar)
				return new Result("%1656%<br>\n", false, null);
		}

		String workdir=(String)parameters.get(Keywords.WorkDir);
		Vector<Hashtable<String, String>> infovar=dict.getfixedvariableinfo();

		NormalFitting nf=new NormalFitting(function, workdir, infovar, parameterList, depvar);

		if (nf.geterror())
		{
			String rm=nf.getretmess();
			nf.clearmem();
			nf=null;
			return new Result(rm, false, null);
		}

		nf.setdict(dict);
		nf.setstart(start);

		if (!nf.compilefunc(false))
		{
			String rm=nf.getretmess();
			nf.clearmem();
			nf=null;
			return new Result(rm, false, null);
		}

		ErrorMinimisator emin= new ErrorMinimisator(dict);

		if (simplexreflectioncoeff!=null)
		{
			emin.setNMreflect(simplexreflectioncoeffval);
		}

		if (simplexextensioncoeff!=null)
		{
			emin.setNMextend(simplexextensioncoeffval);
		}

		if (simplexcontractioncoeff!=null)
		{
			emin.setNMcontract(simplexcontractioncoeffval);
		}

		if (constrainvalues.length>0)
		{
			for (int i=0; i<constrainvalues.length; i++)
			{
				emin.addConstraint((int)constrainvalues[i][0], (int)constrainvalues[i][1], constrainvalues[i][2]);
			}
		}

		emin.setNmax(niter);

		emin.nelderMead(nf.getEF(), start, step, ftol);

		Vector<StepResult> result = new Vector<StepResult>();

		String resuemin=emin.getMessage();
		double funmin=emin.getMinimum();
		resuemin=resuemin+"%1676%: "+funmin;

		result.add(new LocalMessageGetter(resuemin));

		double[] actualparam=emin.getParamValues();

		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);
		Hashtable<String, String> tempmd=new Hashtable<String, String>();
		dsu.addnewvar("parameter", "%1665%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("value", "%1666%", Keywords.NUMSuffix, tempmd, tempmd);
		dsu.addnewvar(Keywords.startvalue, "%1667%", Keywords.NUMSuffix, tempmd, tempmd);
		dsu.addnewvar(Keywords.stepvalue, "%1668%", Keywords.NUMSuffix, tempmd, tempmd);

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		String[] valuestowrite=new String[4];
		for (int i=0; i<actualparam.length; i++)
		{
			valuestowrite[0]="";
			valuestowrite[1]="";
			valuestowrite[2]="";
			valuestowrite[3]="";
			valuestowrite[0]=parameterList[i];
			valuestowrite[1]=double2String(actualparam[i]);
			valuestowrite[2]=double2String(start[i]);
			valuestowrite[3]=double2String(step[i]);
			dw.write(valuestowrite);
		}

		nf.clearmem();
		nf=null;

		String keyword="Fitting "+dict.getkeyword();
		String description="Fitting "+dict.getdescription();
		String author=(String)parameters.get(Keywords.client_host.toLowerCase());

		Hashtable<String, String> othertableinfo=new Hashtable<String, String>();

		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
		Hashtable<String, String> datatableinfo=dw.getTableInfo();
		result.add(new LocalDictionaryWriter(dw.getdictpath(), keyword, description, author, dw.gettabletype(),
		datatableinfo, dsu.getfinalvarinfo(), tablevariableinfo, dsu.getfinalcl(), dsu.getfinalmd(), othertableinfo));
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
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 1640, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.depvar, "var=all", true, 889, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.function, "text", true, 1641, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.parameter, "text", true, 1642, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 1649, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.constrain, "multipletext", false, 1678, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 1679, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.startvalue, "text", false, 1657, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.stepvalue, "text", false, 1658, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.iterations, "text", false, 1643, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.tolerance, "text", false, 1675, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.simplexreflectioncoeff, "text", false, 1680, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.simplexextensioncoeff, "text", false, 1681, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.simplexcontractioncoeff, "text", false, 1682, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="1638";
		retprocinfo[1]="1639";
		return retprocinfo;
	}
}
