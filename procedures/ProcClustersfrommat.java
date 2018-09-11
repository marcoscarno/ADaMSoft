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

import ADaMSoft.algorithms.ClustersEvaluator;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;

import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;

import ADaMSoft.keywords.Keywords;

import java.util.LinkedList;
import java.util.Hashtable;
import java.util.Vector;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that creates clusters of variables
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class ProcClustersfrommat extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Clustersfrommat
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict};
		String [] optionalparameters=new String[] {Keywords.var, Keywords.where, Keywords.associatewithmax, Keywords.tolerance};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		boolean associatewithmax =(parameters.get(Keywords.associatewithmax)!=null);
		boolean type=true;
		if (associatewithmax)
			type=false;
		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String tempvar=(String)parameters.get(Keywords.var.toLowerCase());

		String temptol=(String)parameters.get(Keywords.tolerance.toLowerCase());
		double tolerance=0.00001;
		if (temptol!=null)
		{
			try
			{
				tolerance=Double.parseDouble(temptol);
			}
			catch (Exception enumtol)
			{
				return new Result("%2785%<br>\n", false, null);
			}
			if (Double.isNaN(tolerance))
				return new Result("%2785%<br>\n", false, null);
		}

		String keyword="Clustersfrommat "+dict.getkeyword();
		String description="Clustersfrommat "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		String[] reqvar=new String[0];
		int[] replacerule=new int[0];
		String[] labelvar=new String[0];

		if (tempvar!=null)
		{
			reqvar=tempvar.split(" ");
			replacerule=new int[reqvar.length];
			labelvar=new String[reqvar.length];
			for (int j=0; j<reqvar.length; j++)
			{
				boolean existv=false;
				replacerule[j]=3;
				for (int i=0; i<dict.gettotalvar(); i++)
				{
					String tv=dict.getvarname(i);
					if (reqvar[j].equalsIgnoreCase(tv))
					{
						existv=true;
						labelvar[j]=dict.getvarlabel(i);
					}
				}
				if (!existv)
					return new Result("%2549% ("+reqvar[j]+")<br>\n", false, null);
			}
		}
		else
		{
			reqvar=new String[dict.gettotalvar()];
			replacerule=new int[dict.gettotalvar()];
			labelvar=new String[dict.gettotalvar()];
			for (int i=0; i<dict.gettotalvar(); i++)
			{
				reqvar[i]=dict.getvarname(i);
				replacerule[i]=3;
				labelvar[i]=dict.getvarlabel(i);
			}
		}

		DataReader data = new DataReader(dict);

		if (!data.open(reqvar, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}

		double[][] phimatrix=new double[reqvar.length][reqvar.length];

		int validgroup=0;
		int numobs=0;
		while (!data.isLast())
		{
			String[] values = data.getRecord();
			if (values!=null)
			{
				for (int i=0; i<values.length; i++)
				{
					try
					{
						phimatrix[numobs][i]=Double.parseDouble(values[i]);
					}
					catch (Exception ee)
					{
						data.close();
						return new Result("%2551% "+String.valueOf(numobs)+"-"+String.valueOf(i)+"<br>\n", false, null);
					}
					if (Double.isNaN(phimatrix[numobs][i]))
					{
						data.close();
						return new Result("%2551% "+String.valueOf(numobs)+"-"+String.valueOf(i)+"<br>\n", false, null);
					}
				}
				validgroup++;
				numobs++;
				if (numobs>values.length)
				{
					data.close();
					return new Result("%2552%<br>\n", false, null);
				}
			}
		}
		data.close();
		if (validgroup==0)
			return new Result("%2807%<br>\n", false, null);

		for (int i=0; i<phimatrix.length; i++)
		{
			for (int j=i+1; j<phimatrix.length; j++)
			{
				if (!Double.isNaN(phimatrix[j][i]))
				{
					if (Math.abs(phimatrix[i][j]-phimatrix[j][i])>tolerance)
					{
						return new Result("%2554% ("+String.valueOf(i)+","+String.valueOf(j)+"="+phimatrix[i][j]+"; "+String.valueOf(j)+","+String.valueOf(i)+"="+phimatrix[j][i]+")<br>\n", false, null);
					}
				}
				if (Double.isNaN(phimatrix[i][j]))
					return new Result("%2710% "+String.valueOf(i)+","+String.valueOf(j)+"<br>\n", false, null);
			}
		}

		ClustersEvaluator ce=new ClustersEvaluator(phimatrix, type);
		Vector<String[]>res=ce.estimateclusters(reqvar);

		DataSetUtilities dsu=new DataSetUtilities();

		Hashtable<String, String> tempmd=new Hashtable<String, String>();

		Hashtable<String, String> clvar=new Hashtable<String, String>();
		for (int j=0; j<reqvar.length; j++)
		{
			clvar.put("v_"+reqvar[j], labelvar[j]);
			if (j<(reqvar.length-1))
				clvar.put("CL"+String.valueOf(j+reqvar.length+1), "Cluster: "+String.valueOf(j+reqvar.length+1));
		}
		dsu.addnewvar("step", "%1070%", Keywords.NUMSuffix, tempmd, tempmd);
		dsu.addnewvar("first", "%1071%", Keywords.TEXTSuffix, clvar, tempmd);
		dsu.addnewvar("second", "%1072%", Keywords.TEXTSuffix, clvar, tempmd);
		dsu.addnewvar("newcluster", "%1073%", Keywords.TEXTSuffix, clvar, tempmd);
		dsu.addnewvar("numvars", "%2392%", Keywords.TEXTSuffix, clvar, tempmd);
		dsu.addnewvar("distance", "%1476%", Keywords.NUMSuffix, tempmd, tempmd);

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		String[] valuestowrite=new String[6];
		double lastdist=0;
		for (int j=0; j<res.size(); j++)
		{
			String[] tv=res.get(j);
			valuestowrite[0]=String.valueOf(j+1);
			valuestowrite[1]=tv[0];
			valuestowrite[2]=tv[1];
			if ((tv[0].startsWith("CL")) && (!tv[1].startsWith("CL")))
			{
				valuestowrite[1]=tv[1];
				valuestowrite[2]=tv[0];
			}
			if ((tv[0].startsWith("CL")) && (tv[1].startsWith("CL")))
			{
				double f1=Double.valueOf(tv[0].replaceAll("CL",""));
				double f2=Double.valueOf(tv[1].replaceAll("CL",""));
				if (f1>f2)
				{
					valuestowrite[1]=tv[1];
					valuestowrite[2]=tv[0];
				}
			}
			valuestowrite[3]=tv[4];
			valuestowrite[4]=tv[3];
			double dist=Double.parseDouble(tv[2]);
			lastdist+=dist;
			valuestowrite[5]=String.valueOf(lastdist);
			dw.write(valuestowrite);
		}
		ce=null;

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
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 721, dep, "", 1));
		parameters.add(new GetRequiredParameters("", "note", false, 2550, dep, "", 1));
		parameters.add(new GetRequiredParameters("", "note", false, 2553, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 1468, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", false, 2547, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.associatewithmax, "checkbox", false, 2557, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 2548, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.tolerance, "text", false, 2784, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="1089";
		retprocinfo[1]="2546";
		return retprocinfo;
	}
}
