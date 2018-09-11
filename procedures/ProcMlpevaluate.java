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

import ADaMSoft.algorithms.NLFitting.MLP;
import ADaMSoft.algorithms.NLFitting.MLPEval;

import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.CheckVarNames;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that evaluates the output of a neural network based on a MLP, by loading the net structure from a previously saved net
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcMlpevaluate implements RunStep
{
	/**
	* Starts the execution of Proc MLPevaluate
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.dictnet};
		String [] optionalparameters=new String[] {Keywords.varoutname, Keywords.replace};
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

		String varoutname=(String)parameters.get(Keywords.varoutname);
		if (varoutname==null)
			varoutname="netout";
		varoutname=varoutname.replaceAll("\\s", "");
		if (!CheckVarNames.getResultCheck(varoutname, (String)parameters.get(Keywords.WorkDir)).equals(""))
			return new Result("%2096%"+" "+varoutname+"<br>\n", false, null);

		DictionaryReader dictnet = (DictionaryReader)parameters.get(Keywords.dictnet);
		String[] netinfo=new String[2];
		netinfo[0]="net";
		netinfo[1]="info";
		int[] netreplacerule=new int[] {0, 0};
		DataReader netdata = new DataReader(dictnet);
		if (!netdata.open(netinfo, netreplacerule, false))
			return new Result(netdata.getmessage(), false, null);

		String hidtype=null;
		String outtype=null;
		int hidden=0;
		String vartemp=null;

		while (!netdata.isLast())
		{
			String[] values = netdata.getRecord();
			if (values[0].equals("1"))
				vartemp=values[1];
			else if (values[0].equals("3"))
			{
				try
				{
					hidden=Integer.parseInt(values[1]);
				}
				catch (Exception e)
				{
					netdata.close();
					return new Result("%863%<br>\n%826%<br>\n", false, null);
				}
			}
			else if (values[0].equals("5"))
				outtype=values[1];
			else if (values[0].equals("4"))
				hidtype=values[1];
		}
		netdata.close();
		String[] vartouse=vartemp.split(" ");
		double[][] w=new double[hidden][vartouse.length];
		double[] s=new double[hidden];
		double[] v=new double[hidden];
		double os=0;
		boolean errorinnet=false;
		if (!netdata.open(netinfo, netreplacerule, false))
			return new Result(netdata.getmessage(), false, null);
		while (!netdata.isLast())
		{
			String[] values = netdata.getRecord();
			if (values[0].startsWith("w"))
			{
				try
				{
					String[] parts=values[0].split("_");
					int posw=Integer.parseInt(parts[0].substring(1));
					for (int i=0; i<vartouse.length; i++)
					{
						if (vartouse[i].equalsIgnoreCase(parts[1]))
							w[posw-1][i]=Double.parseDouble(values[1]);
					}
				}
				catch (Exception e)
				{
					errorinnet=true;
				}
			}
			if (values[0].startsWith("s"))
			{
				try
				{
					int posw=Integer.parseInt(values[0].substring(1));
					s[posw-1]=Double.parseDouble(values[1]);
				}
				catch (Exception e)
				{
					errorinnet=true;
				}
			}
			if (values[0].startsWith("v"))
			{
				try
				{
					int posw=Integer.parseInt(values[0].substring(1));
					v[posw-1]=Double.parseDouble(values[1]);
				}
				catch (Exception e)
				{
					errorinnet=true;
				}
			}
			if (values[0].equalsIgnoreCase("os"))
			{
				try
				{
					os=Double.parseDouble(values[1]);
				}
				catch (Exception e)
				{
					errorinnet=true;
				}
			}
		}
		netdata.close();
		if (errorinnet)
			return new Result("%863%<br>\n", false, null);
		String[] types=new String[] {Keywords.logistic, Keywords.tanh, Keywords.linear};
		if (!steputilities.CheckOptions(types, outtype))
			return new Result(steputilities.getMessage(), false, null);
		int outfunction=0;
		if (outtype.equalsIgnoreCase(Keywords.tanh))
			outfunction=2;
		if (outtype.equalsIgnoreCase(Keywords.logistic))
			outfunction=1;

		if (!steputilities.CheckOptions(types, hidtype))
			return new Result(steputilities.getMessage(), false, null);
		int hidfunction=0;
		if (hidtype.equalsIgnoreCase(Keywords.tanh))
			hidfunction=2;
		if (hidtype.equalsIgnoreCase(Keywords.logistic))
			hidfunction=1;

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		vartemp=vartemp.trim();

		VariableUtilities varu=new VariableUtilities(dict, null, vartemp, null, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String workdir=(String)parameters.get(Keywords.WorkDir);
		Vector<Hashtable<String, String>> infovar=dict.getfixedvariableinfo();

		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);
		Hashtable<String, String> temph=new Hashtable<String, String>();

		dsu.defineolddict(dict);

		dsu.addnewvartoolddict(varoutname, "%974%", Keywords.NUMSuffix, temph, temph);

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		MLP mlp=new MLP(workdir, infovar, null);
		mlp.sethiddenfuntype(hidfunction);
		mlp.setoutputfuntype(outfunction);
		mlp.sethidden(hidden);

		if (!mlp.createnettoevaluate(vartouse))
		{
			String rm=mlp.getretmess();
			mlp.clearmem();
			mlp=null;
			return new Result(rm, false, null);
		}

		if (!mlp.compilefunc(true))
		{
			String rm=mlp.getretmess();
			mlp.clearmem();
			mlp=null;
			return new Result(rm, false, null);
		}

		double[] coeff=new double[vartouse.length*hidden+2*hidden+1];
		int pointerw=0;
		for (int i=0; i<hidden; i++)
		{
			for (int j=0; j<vartouse.length; j++)
			{
				coeff[pointerw]=w[i][j];
				pointerw++;
			}
		}
		for (int i=0; i<hidden; i++)
		{
			coeff[pointerw]=s[i];
			pointerw++;
		}
		for (int i=0; i<hidden; i++)
		{
			coeff[pointerw]=v[i];
			pointerw++;
		}
		coeff[pointerw]=os;

		MLPEval ef=mlp.getFE();
		try
		{
			String evalfunc=ef.evaluate(dsu, dict, dw,coeff);
			if (!evalfunc.equals(""))
			{
				mlp.clearmem();
				mlp=null;
				return new Result(evalfunc, false, null);
			}
		}
		catch (Exception e)
		{
			mlp.clearmem();
			mlp=null;
			return new Result("%1999%<br>\n", false, null);
		}

		mlp.clearmem();
		mlp=null;

		String keyword="MLP evaluate "+dict.getkeyword();
		String description="MLP evaluate "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		Hashtable<String, String> othertableinfo=new Hashtable<String, String>();

		Vector<StepResult> result = new Vector<StepResult>();

		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
		Hashtable<String, String> datatableinfo=dw.getTableInfo();
		result.add(new LocalDictionaryWriter(dw.getdictpath(), keyword, description, author, dw.gettabletype(),
		datatableinfo, dsu.getfinalvarinfo(), tablevariableinfo, dsu.getfinalcl(), dsu.getfinalmd(), othertableinfo));
		return new Result("", true, result);
	}
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 721, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dictnet+"=", "dict", true, 865, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 992, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.varoutname, "text", true, 2005, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="833";
		retprocinfo[1]="871";
		return retprocinfo;
	}
}
