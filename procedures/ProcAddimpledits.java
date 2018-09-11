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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.ObjectTransformer;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that adds the implied edits
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class ProcAddimpledits extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Addimpledits
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		String [] requiredparameters=new String[] {Keywords.dict, Keywords.filewedits, Keywords.iterations};
		String [] optionalparameters=new String[] {Keywords.replacefile, Keywords.where};
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String where=(String)parameters.get(Keywords.where.toLowerCase());

		String outedit=(String)parameters.get(Keywords.filewedits);
		if (!outedit.toLowerCase().endsWith(".txt"))
			outedit=outedit+".txt";

		boolean replacefile =(parameters.get(Keywords.replacefile)!=null);

		String iter=(String)parameters.get(Keywords.iterations.toLowerCase());

		int maxitera=string2int(iter);
		if (maxitera<0)
		{
			return new Result("%2610%<br>\n", false, null);
		}

		if (maxitera==0) maxitera=1000000;

		int refvar=dict.gettotalvar()-2;

		String[] varref=new String[refvar];
		String[] vartoread=new String[dict.gettotalvar()];
		int[] replacerule=new int[dict.gettotalvar()];

		int posv=0;

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
				vartoread[posv]=tv;
				replacerule[posv]=1;
				posv++;
			}
		}
		vartoread[posv]="_b_";
		replacerule[posv]=1;
		vartoread[posv+1]="_sign_";
		replacerule[posv+1]=1;

		HashSet<Vector<Double>> coeff=new HashSet<Vector<Double>>();
		Vector<String> oldeditsiq=new Vector<String>();
		Vector<String> oldeditseq=new Vector<String>();
		Vector<String> newedits=new Vector<String>();

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

		DecimalFormatSymbols formsep =new DecimalFormatSymbols();
		formsep.setDecimalSeparator('.');
		DecimalFormat formatter = new DecimalFormat("###.########", formsep);
		formatter.setGroupingSize(0);

		int ncoeff=vartoread.length-1;

		String[] values=null;
		double cf=0;
		String tempedit="";
		int validgroup=0;
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				validgroup++;
				tempedit="";
				if (values[values.length-1].equals(">="))
				{
					double[] te=new double[vartoread.length-1];
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
						if (te[i]!=0)
						{
							if (values[i].startsWith("-"))
								tempedit=tempedit+formatter.format(te[i])+"*"+varref[i];
							else
								tempedit=tempedit+"+"+formatter.format(te[i])+"*"+varref[i];
						}
					}
					try
					{
						te[vartoread.length-2]=Double.parseDouble(values[vartoread.length-2]);
					}
					catch (Exception exn)
					{
						te[vartoread.length-2]=0;
					}
					if (te[vartoread.length-2]!=0)
					{
						if (values[vartoread.length-2].startsWith("-"))
							tempedit=tempedit+formatter.format(te[vartoread.length-2]);
						else
							tempedit=tempedit+"+"+formatter.format(te[vartoread.length-2]);
					}
					tempedit=tempedit+values[vartoread.length-1]+"0";
					Vector<Double> tempcoeff=new Vector<Double>();
					for (int i=0; i<te.length; i++)
					{
						tempcoeff.add(new Double(te[i]));
					}
					coeff.add(tempcoeff);
					oldeditsiq.add(tempedit);
				}
				else
				{
					for (int i=0; i<vartoread.length-2; i++)
					{
						values[i]=values[i].trim();
						try
						{
							cf=Double.parseDouble(values[i]);
						}
						catch (Exception exn)
						{
							cf=0.0;
						}
						if (cf!=0)
						{
							if (values[i].startsWith("-"))
								tempedit=tempedit+formatter.format(cf)+"*"+varref[i];
							else
								tempedit=tempedit+"+"+formatter.format(cf)+"*"+varref[i];
						}
					}
					try
					{
						cf=Double.parseDouble(values[vartoread.length-2]);
					}
					catch (Exception exn)
					{
						cf=0.0;
					}
					if (cf!=0)
					{
						if (values[vartoread.length-2].startsWith("-"))
							tempedit=tempedit+formatter.format(cf);
						else
							tempedit=tempedit+"+"+formatter.format(cf);
					}
					tempedit=tempedit+values[vartoread.length-1]+"0";
					oldeditseq.add(tempedit);
				}
			}
		}
		data.close();
		if (validgroup==0)
		{
			return new Result("%2807%<br>\n", false, null);
		}

		double[][] coefficientsiq=new double[coeff.size()][ncoeff];
		posv=0;
		Iterator<Vector<Double>> itr = coeff.iterator();
		while(itr.hasNext())
		{
			Vector<Double> tm=itr.next();
			for (int i=0; i<tm.size(); i++)
			{
				coefficientsiq[posv][i]=(tm.get(i)).doubleValue();
			}
			posv++;
		}

		boolean implicit=false;

		for (int i=0; i<ncoeff-1; i++)
		{
			for (int j=0; j<coeff.size()-1; j++)
			{
				for (int h=j+1; h<coeff.size(); h++)
				{
					if (coefficientsiq[j][i]*coefficientsiq[h][i]<0)
					{
						implicit=true;
						break;
					}
				}
			}
		}
		if (!implicit)
		{
			return new Result("%2492%<br>\n", true, null);
		}

		double tempc=0;

		int numorige=coeff.size();

		boolean almostonevar=false;

		int toteditadded=0;

			int itera=0;
			int editadded=0;
			Keywords.percentage_total=maxitera*(ncoeff-1);
			while (itera<maxitera)
			{
				itera++;
				for (int r=0; r<ncoeff-1; r++)
				{
					Keywords.percentage_done=r*itera;
					for (int s=0; s<numorige-1; s++)
					{
						for (int t=s+1; t<numorige; t++)
						{
							if (coefficientsiq[s][r]*coefficientsiq[t][r]<0)
							{
								tempedit="";
								Vector<Double> testexist=new Vector<Double>();
								almostonevar=false;
								for (int i=0; i<ncoeff; i++)
								{
									if (i!=r)
									{
										tempc=(Math.abs(coefficientsiq[s][r]))*coefficientsiq[t][i]+(Math.abs(coefficientsiq[t][r]))*coefficientsiq[s][i];
										testexist.add(new Double(tempc));
										if (tempc!=0)
										{
											if (tempc>=0)
												tempedit=tempedit+"+";
											tempedit=tempedit+formatter.format(tempc);
											if (i<ncoeff-1)
											{
												tempedit=tempedit+"*"+varref[i];
												almostonevar=true;
											}
										}
									}
									else
										testexist.add(new Double(0.0));
								}
								if ((!coeff.contains(testexist)) && (almostonevar))
								{
									newedits.add(tempedit+">=0;");
									coeff.add(testexist);
								}
							}
						}
					}
				}
				toteditadded=newedits.size();
				if (toteditadded==editadded) itera=maxitera;
				else
				{
					coefficientsiq=new double[coeff.size()][ncoeff];
					posv=0;
					itr = coeff.iterator();
					while(itr.hasNext())
					{
						Vector<Double> tm=itr.next();
						for (int i=0; i<tm.size(); i++)
						{
							coefficientsiq[posv][i]=(tm.get(i)).doubleValue();
						}
						posv++;
					}
					editadded=newedits.size();
				}
			}
			toteditadded=newedits.size();
			if (newedits.size()==0)
			{
				Keywords.percentage_done=0;
				Keywords.percentage_total=0;
				return new Result("%2503%<br>\n", true, null);
			}
		BufferedWriter fileoutedit=null;
		boolean exist=(new File(outedit)).exists();
		if ((exist) && (!replacefile))
		{
			return new Result("%2484%<br>\n", false, null);
		}
		if (exist)
		{
			exist=(new File(outedit)).delete();
			if (!exist)
			{
				return new Result("%2494% ("+outedit+")<br>\n", false, null);
			}
		}
		try
		{
			fileoutedit = new BufferedWriter(new FileWriter(outedit, true));
		}
		catch (Exception e)
		{
			return new Result("%2485% ("+outedit+")<br>\n", false, null);
		}

		String content="";
		for (int i=0; i<oldeditseq.size(); i++)
		{
			content=content+oldeditseq.get(i)+";\n";
		}
		content=content+"\n";
		for (int i=0; i<oldeditsiq.size(); i++)
		{
			content=content+oldeditsiq.get(i)+";\n";
		}
		content=content+"\n";
		for (int i=0; i<newedits.size(); i++)
		{
			content=content+newedits.get(i)+"\n";
		}

		try
		{
			fileoutedit.write(content);
			content="";
			fileoutedit.close();
			return new Result("%2611% "+String.valueOf(toteditadded)+"<br>\n"+"%2486% ("+outedit+")<br>\n", true, null);
		}
		catch (Exception e)
		{
			return new Result("%2487% ("+outedit+")<br>\n", false, null);
		}
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 2475, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.filewedits, "filesave=all", true, 2483, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.iterations, "text", true, 2609, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replacefile, "checkbox", false, 2493, dep, "", 2));
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
		retprocinfo[1]="2482";
		return retprocinfo;
	}
}
