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
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;

import ADaMSoft.keywords.Keywords;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import ADaMSoft.algorithms.EmAlgorithm;

/**
* This is the procedure that find the scores for the record linkage
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcFindlinkscores implements RunStep
{
	/**
	* Starts the execution of Proc Findlinkscores and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict, Keywords.vardistances, Keywords.limits};
		String [] optionalparameters=new String[] {Keywords.replace, Keywords.where, Keywords.iterations, Keywords.epsilon};
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
		String vardistances=(String)parameters.get(Keywords.vardistances.toLowerCase());
		String limits=(String)parameters.get(Keywords.limits.toLowerCase());

		double epsilon=0.0000001;
		int iterations=5000;

		if ((String)parameters.get(Keywords.iterations.toLowerCase())!=null)
		{
			int it=-1;
			try
			{
				String ti=(String)parameters.get(Keywords.iterations.toLowerCase());
				it=Integer.parseInt(ti);
			}
			catch (Exception eti) {}
			if (it<10)
				return new Result("%2985%<br>\n", false, null);
			iterations=it;
		}
		if ((String)parameters.get(Keywords.epsilon.toLowerCase())!=null)
		{
			double ep=Double.NaN;
			try
			{
				String ti=(String)parameters.get(Keywords.epsilon.toLowerCase());
				ep=Double.parseDouble(ti);
			}
			catch (Exception eti) {}
			if (ep<0)
				return new Result("%2986%<br>\n", false, null);
			if (ep>1)
				return new Result("%2986%<br>\n", false, null);
			epsilon=ep;
		}

		VariableUtilities varu=new VariableUtilities(dict, null, vardistances, null, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		Vector<Integer> posv=new Vector<Integer>();
		Vector<Double> limitv=new Vector<Double>();
		String[] tempvar=null;
		String[] tempnamevars=null;
		double templim=Double.NaN;

		try
		{
			tempvar=vardistances.split(" ");
			tempnamevars=vardistances.split(" ");
			for (int i=0; i<tempvar.length; i++)
			{
				for (int j=0; j<dict.gettotalvar(); j++)
				{
					if (tempvar[i].trim().equalsIgnoreCase(dict.getvarname(j))) posv.add(new Integer(j));
				}
			}
			if (posv.size()<tempvar.length)
				return new Result("%2977%<br>\n", false, null);
		}
		catch (Exception e)
		{
			return new Result("%2978%<br>\n"+e.toString()+"<br>\n", false, null);
		}
		try
		{
			tempvar=limits.split(" ");
			if (tempvar.length!=posv.size())
				return new Result("%2979%<br>\n", false, null);
			for (int i=0; i<tempvar.length; i++)
			{
				templim=Double.NaN;
				try
				{
					templim=Double.parseDouble(tempvar[i]);
				}
				catch (Exception e) {}
				if (Double.isNaN(templim))
					return new Result("%2980%<br>\n", false, null);
				if (templim>1)
					return new Result("%2981%<br>\n", false, null);
				if (templim<0)
					return new Result("%2981%<br>\n", false, null);
				limitv.add(new Double(templim));
			}
		}
		catch (Exception e)
		{
			return new Result("%2982%<br>\n"+e.toString()+"<br>\n", false, null);
		}

		String replace=(String)parameters.get(Keywords.replace);

		String keyword="Findlinkscores "+dict.getkeyword();
		String description="Findlinkscores "+dict.getdescription();
		String author=dict.getauthor();

		DataSetUtilities dsuc=new DataSetUtilities();

		DataReader data = new DataReader(dict);
		int rifrep=0;
		if (replace==null)
			rifrep=0;
		else if (replace.equalsIgnoreCase(Keywords.replaceall))
			rifrep=1;
		else if (replace.equalsIgnoreCase(Keywords.replaceformat))
			rifrep=2;
		else if (replace.equalsIgnoreCase(Keywords.replacemissing))
			rifrep=3;
		if (!data.open(null, rifrep, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}
		Hashtable<Vector<Integer>, Double> combinations=new Hashtable<Vector<Integer>, Double>();
		Vector<Integer> numlimit=new Vector<Integer>();
		for (int i=0; i<tempnamevars.length; i++)
		{
			numlimit.add(new Integer(0));
		}

		String[] values=null;
		double tempval=Double.NaN;
		int refpoint=0;
		double currlimit;
		int reflimit=0;
		while (!data.isLast())
		{
			values = data.getRecord();
			Vector<Integer> tempvect=new Vector<Integer>();
			for (int i=0; i<posv.size(); i++)
			{
				refpoint=(posv.get(i)).intValue();
				tempval=Double.NaN;
				try
				{
					tempval=Double.parseDouble(values[refpoint]);
				}
				catch (Exception t) {}
				if (!Double.isNaN(tempval))
				{
					currlimit=(limitv.get(i)).doubleValue();
					if (tempval>=currlimit)
					{
						tempvect.add(new Integer(1));
						reflimit=(numlimit.get(i)).intValue();
						numlimit.set(i, new Integer(reflimit+1));
					}
					else
						tempvect.add(new Integer(0));
				}
				else tempvect.add(new Integer(0));
			}
			if (combinations.get(tempvect)==null)
			{
				combinations.put(tempvect, new Double(1));
			}
			else
			{
				tempval=(combinations.get(tempvect)).doubleValue();
				combinations.remove(tempvect);
				combinations.put(tempvect, new Double(tempval+1));
			}
		}
		data.close();
		double[][] mat=new double[combinations.size()][posv.size()];
		double[] freqs=new double[combinations.size()];
		refpoint=0;
		int refval=0;
		Vector<Integer> cv=null;
		for (Enumeration<Vector<Integer>> en=combinations.keys(); en.hasMoreElements();)
		{
			cv=en.nextElement();
			for (int i=0; i<cv.size(); i++)
			{
				refval=(cv.get(i)).intValue();
				if (refval==1) mat[refpoint][i]=1.0;
				else mat[refpoint][i]=0.0;
			}
			freqs[refpoint]=(combinations.get(cv)).doubleValue();
			refpoint++;
		}
		combinations.clear();
		combinations=null;
		Hashtable<String, String> tempmd=new Hashtable<String, String>();
		Hashtable<String, String> refvar=new Hashtable<String, String>();
		for (int i=0; i<tempnamevars.length; i++)
		{
			refvar.put(tempnamevars[i], dict.getvarlabelfromname(tempnamevars[i]));
		}

		dsuc.addnewvar("ref_distance_var", "%4045% ", Keywords.TEXTSuffix, refvar, tempmd);
		dsuc.addnewvar("ref_limit", "%4046% ", Keywords.NUMSuffix, tempmd, tempmd);
		dsuc.addnewvar("M", "%2989% ", Keywords.NUMSuffix, tempmd, tempmd);
		dsuc.addnewvar("U", "%2990% ", Keywords.NUMSuffix, tempmd, tempmd);
		dsuc.addnewvar("WA", "%2992% ", Keywords.NUMSuffix, tempmd, tempmd);
		dsuc.addnewvar("WD", "%2991% ", Keywords.NUMSuffix, tempmd, tempmd);
		dsuc.addnewvar("numot", "%3074% ", Keywords.NUMSuffix, tempmd, tempmd);
		if (!dw.opendatatable(dsuc.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		EmAlgorithm em=new EmAlgorithm(epsilon, iterations);
		em.initialize(mat, freqs);
		em.calculate();
		if (em.getstate())
		{
			dw.deletetmp();
			return new Result("%2993%<br>\n", false, null);
		}

		double[] wa=em.getWA();
		double[] wd=em.getWD();
		double[] m=em.getMArray();
		double[] u=em.getUArray();

		String[] outdwc=new String[7];

		for (int i=0; i<tempnamevars.length; i++)
		{
			outdwc[0]=tempnamevars[i];
			outdwc[1]=tempvar[i];
			outdwc[2]="";
			outdwc[3]="";
			outdwc[4]="";
			outdwc[5]="";
			if (!Double.isNaN(m[i])) outdwc[2]=String.valueOf(m[i]);
			if (!Double.isNaN(u[i])) outdwc[3]=String.valueOf(u[i]);
			if (!Double.isNaN(wa[i])) outdwc[4]=String.valueOf(wa[i]);
			if (!Double.isNaN(wd[i])) outdwc[5]=String.valueOf(wd[i]);
			reflimit=(numlimit.get(i)).intValue();
			outdwc[6]=String.valueOf(reflimit);
			dw.write(outdwc);
		}

		Vector<StepResult> result = new Vector<StepResult>();
		boolean resclosec=dw.close();
		if (!resclosec)
			return new Result(dw.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfoc=dw.getVarInfo();
		Hashtable<String, String> datatableinfoc=dw.getTableInfo();
		result.add(new LocalDictionaryWriter(dw.getdictpath(), keyword, description, author, dw.gettabletype(),
		datatableinfoc, dsuc.getfinalvarinfo(), tablevariableinfoc, dsuc.getfinalcl(), dsuc.getfinalmd(), null));
		return new Result("", true, result);
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 2971, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 2987, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.vardistances, "vars=all", true, 2973, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.limits,"text", true, 2974,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2975, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.iterations,"text", false, 2983,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.epsilon,"text", false, 2984,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="2919";
		retprocinfo[1]="2970";
		return retprocinfo;
	}
}
