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

import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.algorithms.NLFitting.MLP;
import ADaMSoft.algorithms.NLFitting.MLPMinimisator;

/**
* This is the procedure that implements a neural network based on a MLP
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcMlplearn extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc MLP
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean testdefined=false;
		boolean alreadylearn=false;
		String [] requiredparameters=new String[] {Keywords.OUTHIST.toLowerCase(), Keywords.dict};
		String [] optionalparameters=new String[] {Keywords.var, Keywords.hidden, Keywords.depvar, Keywords.outfunction, Keywords.hidfunction, Keywords.dicttest, Keywords.dictnet, Keywords.OUTNET.toLowerCase(), Keywords.epoch, Keywords.ihweightint, Keywords.howeightint, Keywords.tolerance, Keywords.simplexreflectioncoeff, Keywords.simplexextensioncoeff, Keywords.simplexcontractioncoeff, Keywords.testtimes, Keywords.replace};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		testdefined =(parameters.get(Keywords.dicttest)!=null);
		DataWriter dw=new DataWriter(parameters, Keywords.OUTHIST.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		String outnet=(String)parameters.get(Keywords.OUTNET.toLowerCase());
		DataWriter dwn=null;
		if (outnet!=null)
		{
			dwn=new DataWriter(parameters, Keywords.OUTNET.toLowerCase());
			if (!dwn.getmessage().equals(""))
				return new Result(dwn.getmessage(), false, null);
		}

		String outtype =null;
		String hidtype =null;
		int nhidden=1;
		String vartemp=null;
		String vary=null;
		String[] testy=null;
		String[] vartouse=null;
		double[] start=null;

		if (parameters.get(Keywords.dictnet)==null)
		{
			vartemp=(String)parameters.get(Keywords.var.toLowerCase());
			if (vartemp==null)
				return new Result("%918%<br>\n", false, null);
			vary=(String)parameters.get(Keywords.depvar.toLowerCase());
			if (vary==null)
				return new Result("%919%<br>\n", false, null);
			outtype =(String)parameters.get(Keywords.outfunction);
			hidtype =(String)parameters.get(Keywords.hidfunction);
			if ((outtype==null) || (hidtype==null))
				return new Result("%920%<br>\n", false, null);
			String[] types=new String[] {Keywords.logistic, Keywords.tanh, Keywords.linear};
			if (!steputilities.CheckOptions(types, outtype))
				return new Result("%1775% "+Keywords.outfunction.toUpperCase()+"<br>\n"+steputilities.getMessage(), false, null);
			if (!steputilities.CheckOptions(types, hidtype))
				return new Result("%1775% "+Keywords.hidfunction.toUpperCase()+"<br>\n"+steputilities.getMessage(), false, null);
			if (parameters.get(Keywords.hidden)==null)
				return new Result("%973%\n", false, null);
			try
			{
				nhidden=Integer.parseInt((String)parameters.get(Keywords.hidden));
			}
			catch (Exception e)
			{
				return new Result("%826%<br>\n", false, null);
			}
			testy=(vary.trim()).split(" ");
			if (testy.length!=1)
				return new Result("%832%<br>\n", false, null);
			vartouse=vartemp.split(" ");
		}
		else
		{
			vartemp=(String)parameters.get(Keywords.var.toLowerCase());
			if (vartemp!=null)
				return new Result("%976% ("+Keywords.var.toLowerCase()+")<br>\n", false, null);
			vary=(String)parameters.get(Keywords.depvar.toLowerCase());
			if (vary!=null)
				return new Result("%976% ("+Keywords.depvar.toLowerCase()+")<br>\n", false, null);
			outtype=(String)parameters.get(Keywords.outfunction.toLowerCase());
			if (outtype!=null)
				return new Result("%976% ("+Keywords.outfunction.toLowerCase()+")<br>\n", false, null);
			hidtype=(String)parameters.get(Keywords.hidfunction.toLowerCase());
			if (hidtype!=null)
				return new Result("%976% ("+Keywords.hidfunction.toLowerCase()+"<br>\n", false, null);
			if (parameters.get(Keywords.hidden.toLowerCase())!=null)
				return new Result("%976% ("+Keywords.hidden.toLowerCase()+")<br>\n", false, null);

			DictionaryReader dictnet = (DictionaryReader)parameters.get(Keywords.dictnet);
			String[] netinfo=new String[2];
			netinfo[0]="net";
			netinfo[1]="info";
			int[] netreplacerule=new int[] {0, 0};
			DataReader netdata = new DataReader(dictnet);
			if (!netdata.open(netinfo, netreplacerule, false))
				return new Result(netdata.getmessage(), false, null);

			while (!netdata.isLast())
			{
				String[] values = netdata.getRecord();
				if (values[0].equals("1"))
					vartemp=values[1].trim();
				if (values[0].equals("2"))
					vary=values[1].trim();
				else if (values[0].equals("3"))
				{
					try
					{
						nhidden=Integer.parseInt(values[1]);
					}
					catch (Exception e)
					{
						netdata.close();
						return new Result("%863%<br>\n%826%<br>\n", false, null);
					}
				}
				else if (values[0].equals("5"))
				{
					parameters.put(Keywords.outfunction, values[1]);
					outtype=values[1];
				}
				else if (values[0].equals("4"))
				{
					parameters.put(Keywords.hidfunction, values[1]);
					hidtype=values[1];
				}
			}
			netdata.close();
			vartouse=vartemp.split(" ");
			double[][] w=new double[nhidden][vartouse.length];
			double[] s=new double[nhidden];
			double[] v=new double[nhidden];
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
			alreadylearn=true;
			start=new double[vartouse.length*nhidden+2*nhidden+1];
			int pointerw=0;
			for (int i=0; i<nhidden; i++)
			{
				for (int j=0; j<vartouse.length; j++)
				{
					start[pointerw]=w[i][j];
					pointerw++;
				}
			}
			for (int i=0; i<nhidden; i++)
			{
				start[pointerw]=s[i];
				pointerw++;
			}
			for (int i=0; i<nhidden; i++)
			{
				start[pointerw]=v[i];
				pointerw++;
			}
			start[pointerw]=os;
		}

		int outfunction=0;
		if (outtype.equalsIgnoreCase(Keywords.tanh))
			outfunction=2;
		if (outtype.equalsIgnoreCase(Keywords.logistic))
			outfunction=1;

		int hidfunction=0;
		if (hidtype.equalsIgnoreCase(Keywords.tanh))
			hidfunction=2;
		if (hidtype.equalsIgnoreCase(Keywords.logistic))
			hidfunction=1;

		int ntimestest=100;
		String mntimestest=(String)parameters.get(Keywords.testtimes);
		if (mntimestest!=null)
		{
			ntimestest=string2int(mntimestest);
			if (ntimestest<0)
				return new Result("%827%<br>\n", false, null);
		}

		int nepoch=3000;
		String maxiter=(String)parameters.get(Keywords.epoch);
		if (maxiter!=null)
		{
			nepoch=string2int(maxiter);
			if (nepoch<0)
				return new Result("%827%<br>\n", false, null);
		}

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
		double ftol = 1e-8;
		String tolerance=(String)parameters.get(Keywords.tolerance);
		if (tolerance!=null)
		{
			ftol=string2int(tolerance);
			if (Double.isNaN(ftol))
				return new Result("%1652%<br>\n", false, null);
		}
		double ihwint=0.1;
		try
		{
			if ((String)parameters.get(Keywords.ihweightint)==null)
				ihwint=1;
			else
				ihwint=Double.parseDouble((String)parameters.get(Keywords.ihweightint));
		}
		catch (Exception e)
		{
			return new Result("%912%<br>\n", false, null);
		}
		double howint=0.01;
		try
		{
			if ((String)parameters.get(Keywords.howeightint)==null)
				howint=1;
			else
				howint=Double.parseDouble((String)parameters.get(Keywords.howeightint));
		}
		catch (Exception e)
		{
			return new Result("%916%<br>\n", false, null);
		}

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		DictionaryReader dicttest = null;
		if (testdefined)
		{
			dicttest=(DictionaryReader)parameters.get(Keywords.dicttest);
			String trainname=dict.getDictPath();
			String testname=dicttest.getDictPath();
			if (testname.equalsIgnoreCase(trainname))
				testdefined=false;
		}

		vartouse=vartemp.split(" ");

		VariableUtilities varu=new VariableUtilities(dict, null, vartemp.trim()+" "+vary.trim(), null, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		VariableUtilities varutest=null;
		if (testdefined)
		{
			varutest=new VariableUtilities(dicttest, null, vartemp.trim()+" "+vary.trim(), null, null, null);
			if (varutest.geterror())
				return new Result(varutest.getmessage(), false, null);
		}

		String workdir=(String)parameters.get(Keywords.WorkDir);
		Vector<Hashtable<String, String>> infovar=dict.getfixedvariableinfo();

		MLP mlp=new MLP(workdir, infovar, vary.trim());
		mlp.sethiddenfuntype(hidfunction);
		mlp.setoutputfuntype(outfunction);
		mlp.sethidden(nhidden);

		if (!mlp.createnettolearn(vartouse))
		{
			String rm=mlp.getretmess();
			mlp.clearmem();
			mlp=null;
			return new Result(rm, false, null);
		}
		mlp.setdict(dict);
		if (!mlp.compilefunc(false))
		{
			String rm=mlp.getretmess();
			mlp.clearmem();
			mlp=null;
			return new Result(rm, false, null);
		}

		MLP mlptest=null;
		if (testdefined)
		{
			mlptest=new MLP(workdir, infovar, vary.trim());
			mlptest.sethiddenfuntype(hidfunction);
			mlptest.setoutputfuntype(outfunction);
			mlptest.sethidden(nhidden);
			if (!mlptest.createnettolearn(vartouse))
			{
				String rm=mlptest.getretmess();
				mlptest.clearmem();
				mlptest=null;
				mlp.clearmem();
				mlp=null;
				return new Result(rm, false, null);
			}
			mlptest.setdict(dicttest);
			if (!mlptest.compilefunc(false))
			{
				String rm=mlptest.getretmess();
				mlptest.clearmem();
				mlptest=null;
				mlp.clearmem();
				mlp=null;
				return new Result(rm, false, null);
			}
		}

		MLPMinimisator emlp= new MLPMinimisator(dict, dicttest);

		if (simplexreflectioncoeff!=null)
		{
			emlp.setNMreflect(simplexreflectioncoeffval);
		}

		if (simplexextensioncoeff!=null)
		{
			emlp.setNMextend(simplexextensioncoeffval);
		}

		if (simplexcontractioncoeff!=null)
		{
			emlp.setNMcontract(simplexcontractioncoeffval);
		}

		if (testdefined)
		{
			emlp.setntimestest(ntimestest);
			emlp.settestEF(mlptest.getEF());
		}

		if (!alreadylearn)
		{
			start=new double[vartouse.length*nhidden+2*nhidden+1];
			int pointerw=0;
			for (int i=0; i<nhidden; i++)
			{
				for (int j=0; j<vartouse.length; j++)
				{
					start[pointerw]=Math.random()*2*ihwint-ihwint;
					pointerw++;
				}
			}
			for (int i=0; i<nhidden; i++)
			{
				start[pointerw]=0;
				pointerw++;
			}
			for (int i=0; i<nhidden; i++)
			{
				start[pointerw]=Math.random()*2*howint-howint;
				pointerw++;
			}
			start[pointerw]=0;
		}

		emlp.nelderMead(mlp.getEF(), start, ftol, nepoch);

		int convergence=emlp.getconvergencestatus();

		if (convergence==-1)
		{
			mlp.clearmem();
			mlp=null;
			if (testdefined)
			{
				mlptest.clearmem();
				mlptest=null;
			}
			return new Result("%844%<br>\n", false, null);
		}

		String keyword="MLP "+dict.getkeyword();
		String description="MLP "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		Vector<StepResult> result = new Vector<StepResult>();

		if ((convergence==1) && (testdefined))
		{
			LocalMessageGetter lmg=new LocalMessageGetter("%913%<br>\n");
			result.add(lmg);
		}

		if (!emlp.getConvStatus())
		{
			LocalMessageGetter lmg=new LocalMessageGetter("%829%<br>\n");
			result.add(lmg);
		}
		else
		{
			LocalMessageGetter lmg=new LocalMessageGetter("%837%<br>\n");
			result.add(lmg);
		}

		DataSetUtilities dsu=new DataSetUtilities();

		Hashtable<String, String> temph=new Hashtable<String, String>();

		dsu.addnewvar("epoch", "%978%", Keywords.NUMSuffix, temph, temph);
		dsu.addnewvar("rmse", "%979%", Keywords.NUMSuffix, temph, temph);
		if (testdefined)
			dsu.addnewvar("rmse_test", "%980%", Keywords.NUMSuffix, temph, temph);

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		int outvar=2;
		Vector<Double> history=emlp.getRMSE();
		Vector<Double> historytest=null;
		if (testdefined)
		{
			outvar=3;
			historytest=emlp.getRMSEtest();
		}

		for (int i=0; i<history.size(); i++)
		{
			String [] values=new String[outvar];
			double rmse=(history.get(i)).doubleValue();
			values[0]=String.valueOf(i);
			if (!Double.isNaN(rmse))
				values[1]=String.valueOf(rmse);
			else
				values[1]="";
			if (testdefined)
			{
				double rmsetest=(historytest.get(i)).doubleValue();
				if (!Double.isNaN(rmsetest))
					values[2]=String.valueOf(rmsetest);
				else
					values[2]="";
			}
			dw.write(values);
		}

		if (outnet!=null)
		{
			double[] estpar=emlp.getParamValues();
			String netkeyword="MLP Structure: "+dict.getkeyword();
			String netdescription="MLP Structure: "+dict.getdescription();
			String netauthor=(String) parameters.get(Keywords.client_host.toLowerCase());
			DataSetUtilities netdsu=new DataSetUtilities();
			Hashtable<String, String> tempcodelabel=new Hashtable<String, String>();
			tempcodelabel.put("1","%981%");
			tempcodelabel.put("2","%982%");
			tempcodelabel.put("3","%983%");
			tempcodelabel.put("4","%1067%");
			tempcodelabel.put("5","%984%");
			int pointerw=0;
			for (int i=0; i<nhidden; i++)
			{
				for (int j=0; j<vartouse.length; j++)
				{
					tempcodelabel.put("w"+String.valueOf(i+1)+"_"+vartouse[j],"%985%: "+String.valueOf(i+1)+"-"+"%987%: "+dict.getvarlabelfromname(vartouse[j]));
				}
			}
			for (int i=0; i<nhidden; i++)
			{
				tempcodelabel.put("s"+String.valueOf(i+1),"%988%: "+String.valueOf(i+1));
			}
			for (int i=0; i<nhidden; i++)
			{
				tempcodelabel.put("v"+String.valueOf(i+1),"%985%: "+String.valueOf(i+1)+"-"+"%986%");
			}
			tempcodelabel.put("os","%2095%");
			netdsu.addnewvar("net", "%989%", Keywords.TEXTSuffix, tempcodelabel, temph);
			Hashtable<String, String> tcl=new Hashtable<String, String>();
			String labivar="";
			for (int i=0; i<vartouse.length; i++)
			{
				labivar=labivar+dict.getvarlabelfromname(vartouse[i])+"- ";
			}
			labivar=labivar.trim();
			tcl.put((vartemp.trim()).toLowerCase(), labivar);
			tcl.put((vary.trim()).toLowerCase(), dict.getvarlabelfromname(vary));
			netdsu.addnewvar("info", "%991%", Keywords.TEXTSuffix, tcl, temph);

			if (!dwn.opendatatable(netdsu.getfinalvarinfo()))
				return new Result(dwn.getmessage(), false, null);

			String [] values=new String[2];
			values[0]="1";
			values[1]=(vartemp.trim()).toLowerCase();
			dwn.write(values);

			values[0]="2";
			values[1]=(vary.trim()).toLowerCase();
			dwn.write(values);

			values[0]="3";
			values[1]=String.valueOf(nhidden);
			dwn.write(values);

			values[0]="4";
			values[1]=(String)parameters.get(Keywords.hidfunction);
			dwn.write(values);

			values[0]="5";
			values[1]=(String)parameters.get(Keywords.outfunction);
			dwn.write(values);

			pointerw=0;
			for (int i=0; i<nhidden; i++)
			{
				for (int j=0; j<vartouse.length; j++)
				{
					values[0]="w"+String.valueOf(i+1)+"_"+vartouse[j];
					values[1]=String.valueOf(estpar[pointerw]);
					pointerw++;
					dwn.write(values);
				}
			}
			for (int i=0; i<nhidden; i++)
			{
				values[0]="s"+String.valueOf(i+1);
				values[1]=String.valueOf(estpar[pointerw]);
				pointerw++;
				dwn.write(values);
			}
			for (int i=0; i<nhidden; i++)
			{
				values[0]="v"+String.valueOf(i+1);
				values[1]=String.valueOf(estpar[pointerw]);
				pointerw++;
				dwn.write(values);
			}
			values[0]="os";
			values[1]=String.valueOf(estpar[pointerw]);
			dwn.write(values);

			boolean resclose=dwn.close();
			if (!resclose)
				return new Result(dwn.getmessage(), false, null);
			Vector<Hashtable<String, String>> nettablevariableinfo=dwn.getVarInfo();
			Hashtable<String, String> netdatatableinfo=dwn.getTableInfo();
			result.add(new LocalDictionaryWriter(dwn.getdictpath(), netkeyword, netdescription, netauthor, dwn.gettabletype(),
			netdatatableinfo, netdsu.getfinalvarinfo(), nettablevariableinfo, netdsu.getfinalcl(), netdsu.getfinalmd(), null));
		}

		mlp.clearmem();
		mlp=null;
		if (testdefined)
		{
			mlptest.clearmem();
			mlptest=null;
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
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 721, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dicttest+"=", "dict", false, 872, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dictnet+"=", "dict", false, 865, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUTHIST.toLowerCase()+"=", "setting=out", true, 841, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUTNET.toLowerCase()+"=", "setting=out", false, 840, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", false, 830, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 975, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.depvar, "vars=all", false, 831, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 975, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.epoch, "text", false, 835, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.hidden, "text", false, 836, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 975, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.outfunction, "listsingle=1064_"+Keywords.linear+",1065_"+Keywords.tanh+",1066_"+Keywords.logistic, false, 842, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 975, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.hidfunction, "listsingle=1064_"+Keywords.linear+",1065_"+Keywords.tanh+",1066_"+Keywords.logistic, false, 1063, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 975, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.ihweightint,"text",false,911,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.howeightint,"text",false,915,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.tolerance, "text", false, 1675, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.simplexreflectioncoeff, "text", false, 1680, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.simplexextensioncoeff, "text", false, 1681, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.simplexcontractioncoeff, "text", false, 1682, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.testtimes, "text", false, 828, dep, "", 2));
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
		retprocinfo[1]="834";
		return retprocinfo;
	}
}
