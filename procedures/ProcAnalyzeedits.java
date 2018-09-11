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
import java.util.HashSet;
import java.util.Iterator;

import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that analyzes the edits
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class ProcAnalyzeedits extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Analyzeedits
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		String [] requiredparameters=new String[] {Keywords.dict};
		String [] optionalparameters=new String[] {Keywords.where};
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		int refvar=dict.gettotalvar()-2;
		String where=(String)parameters.get(Keywords.where.toLowerCase());

		String[] varref=new String[refvar];
		String[] vartoread=new String[dict.gettotalvar()];
		int[] replacerule=new int[dict.gettotalvar()];

		int posv=0;

		String listvar="";

		for (int i=0; i<dict.gettotalvar(); i++)
		{
			String tv=dict.getvarname(i);
			if ( (!tv.equals("_sign_")) && (!tv.equals("_b_")) )
			{
				String[] ttv=tv.split("_");
				String realvname="";
				for (int rv=1; rv<ttv.length; rv++)
				{
					realvname=realvname+ttv[rv];
					if (rv<(ttv.length-1))
						realvname=realvname+"_";
				}
				varref[posv]=realvname.trim();
				listvar=listvar+" "+varref[posv];
				vartoread[posv]=tv;
				replacerule[posv]=1;
				posv++;
			}
		}
		vartoread[posv]="_b_";
		replacerule[posv]=1;
		vartoread[posv+1]="_sign_";
		replacerule[posv+1]=1;
		listvar=listvar.trim();

		HashSet<Vector<Double>> coeffeq=new HashSet<Vector<Double>>();
		HashSet<Vector<Double>> coeffiq=new HashSet<Vector<Double>>();

		DataReader data = new DataReader(dict);
		if (!data.open(vartoread, replacerule, false))
		{
			return new Result(data.getmessage(), false, null);
		}
		if (where!=null)
		{
			if (!data.setcondition(where))
			{
				return new Result(data.getmessage(), false, null);
			}
		}

		Keywords.percentage_total=3;
		int ncoeff=vartoread.length-2;

		String[] values=null;
		int validgroup=0;
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				validgroup++;
				if (values[values.length-1].equals(">="))
				{
					double[] te=new double[vartoread.length-2];
					for (int i=0; i<vartoread.length-2; i++)
					{
						values[i]=values[i].trim();
						try
						{
							te[i]=Double.parseDouble(values[i]);
						}
						catch (Exception exn)
						{
							te[i]=0.0;
						}
					}
					Vector<Double> tempcoeff=new Vector<Double>();
					for (int i=0; i<te.length; i++)
					{
						tempcoeff.add(new Double(te[i]));
					}
					coeffiq.add(tempcoeff);
				}
				else
				{
					double[] te=new double[vartoread.length-2];
					for (int i=0; i<vartoread.length-2; i++)
					{
						values[i]=values[i].trim();
						try
						{
							te[i]=Double.parseDouble(values[i]);
						}
						catch (Exception exn)
						{
							te[i]=0.0;
						}
					}
					Vector<Double> tempcoeff=new Vector<Double>();
					for (int i=0; i<te.length; i++)
					{
						tempcoeff.add(new Double(te[i]));
					}
					coeffeq.add(tempcoeff);
				}
			}
		}
		data.close();
		if (validgroup==0)
		{
			Keywords.percentage_total=0;
			return new Result("%2807%<br>\n", false, null);
		}
		Keywords.percentage_done=1;
		int numeiq=coeffiq.size();
		int numeeq=coeffeq.size();
		Vector<StepResult> result = new Vector<StepResult>();
		result.add(new LocalMessageGetter("%2615%: "+String.valueOf(numeiq)+"<br>\n"));
		result.add(new LocalMessageGetter("%2616%: "+String.valueOf(numeeq)+"<br>\n"));
		result.add(new LocalMessageGetter("%2617%: "+String.valueOf(ncoeff)+" ("+listvar+")<br>\n"));

		double[][] coefficientsiq=new double[coeffiq.size()][ncoeff];
		double[][] coefficientseq=null;
		posv=0;
		Iterator<Vector<Double>> itr = coeffiq.iterator();
		while(itr.hasNext())
		{
			Vector<Double> tm=itr.next();
			for (int i=0; i<tm.size(); i++)
			{
				coefficientsiq[posv][i]=(tm.get(i)).doubleValue();
			}
			posv++;
		}
		if (numeeq>0)
		{
			coefficientseq=new double[coeffeq.size()][ncoeff];
			posv=0;
			itr = coeffeq.iterator();
			while(itr.hasNext())
			{
				Vector<Double> tm=itr.next();
				for (int i=0; i<tm.size(); i++)
				{
					coefficientseq[posv][i]=(tm.get(i)).doubleValue();
				}
				posv++;
			}
		}

		boolean implicit=false;
		for (int i=0; i<ncoeff; i++)
		{
			for (int j=0; j<coeffiq.size()-1; j++)
			{
				for (int h=j+1; h<coeffiq.size(); h++)
				{
					if (coefficientsiq[j][i]*coefficientsiq[h][i]<0)
					{
						implicit=true;
						break;
					}
				}
			}
		}
		if (implicit)
			result.add(new LocalMessageGetter("%2618%<br>\n"));
		if (!implicit)
			result.add(new LocalMessageGetter("%2619%<br>\n"));
		Keywords.percentage_done=2;

		for (int i=0; i<ncoeff; i++)
		{
			result.add(new LocalMessageGetter("\n%2620% "+varref[i].toUpperCase()+":<br>\n"));
			int timeinvolved=0;
			HashSet<Integer> refivar=new HashSet<Integer>();
			for (int j=0; j<coefficientsiq.length; j++)
			{
				if (coefficientsiq[j][i]!=0)
				{
					timeinvolved++;
					for (int k=0; k<coefficientsiq[0].length; k++)
					{
						if ((k!=i) && (coefficientsiq[j][k]!=0))
							refivar.add(new Integer(k));
					}
				}
			}
			if (numeeq>0)
			{
				for (int j=0; j<coefficientseq.length; j++)
				{
					if (coefficientseq[j][i]!=0)
					{
						timeinvolved++;
					}
				}
			}
			result.add(new LocalMessageGetter("%2621%: "+String.valueOf(timeinvolved)+"<br>\n"));
			String otherv="";
			Iterator<Integer> itri = refivar.iterator();
			while(itri.hasNext())
			{
				posv=(itri.next()).intValue();
				otherv=otherv+varref[posv].toUpperCase()+" ";
			}
			otherv=otherv.trim();
			if (!otherv.equals(""))
				result.add(new LocalMessageGetter("%2622%: "+String.valueOf(refivar.size())+" ("+otherv+")<br>\n"));
			else
				result.add(new LocalMessageGetter("%2622%: "+String.valueOf(refivar.size())+"<br>\n"));
			if (refivar.size()>0)
			{
				itri = refivar.iterator();
				HashSet<Integer> totvar=new HashSet<Integer>();
				while(itri.hasNext())
				{
					posv=(itri.next()).intValue();
					for (int j=0; j<coefficientsiq.length; j++)
					{
						if (coefficientsiq[j][posv]!=0)
						{
							for (int k=0; k<coefficientsiq[0].length; k++)
							{
								if ((k!=i) && (coefficientsiq[j][k]!=0))
									totvar.add(new Integer(k));
							}
						}
					}
				}
				otherv="";
				itri = totvar.iterator();
				while(itri.hasNext())
				{
					posv=(itri.next()).intValue();
					otherv=otherv+varref[posv].toUpperCase()+" ";
				}
				otherv=otherv.trim();
				if (!otherv.equals(""))
					result.add(new LocalMessageGetter("%2623%: "+String.valueOf(totvar.size())+" ("+otherv+")<br>\n"));
			}
			if (numeeq>0)
			{
				refivar.clear();
				for (int j=0; j<coefficientseq.length; j++)
				{
					if (coefficientseq[j][i]!=0)
					{
						for (int k=0; k<coefficientseq[0].length; k++)
						{
							if ((k!=i) && (coefficientseq[j][k]!=0))
								refivar.add(new Integer(k));
						}
					}
				}
				if (refivar.size()>0)
				{
					itri = refivar.iterator();
					HashSet<Integer> totvar=new HashSet<Integer>();
					while(itri.hasNext())
					{
						posv=(itri.next()).intValue();
						for (int j=0; j<coefficientseq.length; j++)
						{
							if (coefficientseq[j][posv]!=0)
							{
								for (int k=0; k<coefficientseq[0].length; k++)
								{
									if ((k!=i) && (coefficientseq[j][k]!=0))
										totvar.add(new Integer(k));
								}
							}
						}
					}
					otherv="";
					itri = totvar.iterator();
					while(itri.hasNext())
					{
						posv=(itri.next()).intValue();
						otherv=otherv+varref[posv].toUpperCase()+" ";
					}
					otherv=otherv.trim();
					if (!otherv.equals(""))
						result.add(new LocalMessageGetter("%2624%: "+String.valueOf(totvar.size())+" ("+otherv+")<br>\n"));
				}
			}
		}
		Keywords.percentage_done=0;
		Keywords.percentage_total=0;
		return new Result("", true, result);
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 2475, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2)); 		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="2439";
		retprocinfo[1]="2614";
		return retprocinfo;
	}
}
