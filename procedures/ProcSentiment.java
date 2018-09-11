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

import java.util.TreeMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
import java.util.TreeSet;

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.StepUtilities;


/**
* This is the procedure that evaluates the sentiment
* @author marco.scarno@gmail.com
* @version 1.0.0, rev.: 19/06/17 by marco
*/
public class ProcSentiment implements RunStep
{
	/**
	*Evaluate the sentiment
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		String[] requiredparameters = new String[] {Keywords.dict, Keywords.dict+"sentiment", Keywords.varphrase, Keywords.varrefterm, Keywords.varrefweight, Keywords.varrefpolarity, Keywords.OUT.toLowerCase()};
		String[] optionalparameters = new String[] {Keywords.negations, Keywords.term_multiplier};
		Keywords.percentage_total=3;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		String replace =(String)parameters.get(Keywords.replace);
		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
		{
			return new Result(dw.getmessage(), false, null);
		}
		DictionaryReader dictsent = (DictionaryReader)parameters.get(Keywords.dict+"sentiment");
		String varrefterm=(String)parameters.get(Keywords.varrefterm);
		String varrefweight=(String)parameters.get(Keywords.varrefweight);
		String varrefpolarity=(String)parameters.get(Keywords.varrefpolarity);

		String temp_multiplier=(String)parameters.get(Keywords.term_multiplier);
		Vector<String[]> term_multiplier=new Vector<String[]>();
		if (temp_multiplier!=null)
		{
			String[] tm=temp_multiplier.split(";");
			for (int i=0; i<tm.length; i++)
			{
				String[] info_tm=new String[4];
				String[] p1=tm[i].split("=");
				if (p1.length!=3)
				{
					return new Result("%4160% ("+tm[i]+")<br>\n", false, null);
				}
				info_tm[0]=p1[0].trim().toLowerCase();
				info_tm[1]=p1[1].trim();
				try
				{
					Double.parseDouble(info_tm[1]);
				}
				catch (Exception ec)
				{
					return new Result("%4161% ("+tm[i]+")<br>\n", false, null);
				}
				String[] p2=p1[2].split(":");
				if (p2.length!=2)
				{
					return new Result("%4158% ("+tm[i]+")<br>\n", false, null);
				}
				info_tm[2]=p2[0].trim();
				info_tm[3]=p2[1].trim();
				try
				{
					Integer.parseInt(info_tm[2]);
					Integer.parseInt(info_tm[3]);
				}
				catch (Exception ec)
				{
					return new Result("%4158% ("+tm[i]+")<br>\n", false, null);
				}
				term_multiplier.add(info_tm);
			}
		}

		if (varrefterm.indexOf(" ")>=0)
		{
			return new Result("%4107%<br>\n", false, null);
		}
		if (varrefweight.indexOf(" ")>=0)
		{
			return new Result("%4108%<br>\n", false, null);
		}
		if (varrefpolarity.indexOf(" ")>=0)
		{
			return new Result("%4109%<br>\n", false, null);
		}
		int pos_varrefterm=-1;
		int pos_varrefweight=-1;
		int pos_varrefpolarity=-1;
		String tempname="";
		for (int i=0; i<dictsent.gettotalvar(); i++)
		{
			tempname=dictsent.getvarname(i);
			tempname=tempname.toLowerCase();
			if (tempname.equalsIgnoreCase(varrefterm)) pos_varrefterm=i;
			if (tempname.equalsIgnoreCase(varrefweight)) pos_varrefweight=i;
			if (tempname.equalsIgnoreCase(varrefpolarity)) pos_varrefpolarity=i;
		}
		if (pos_varrefterm<0) return new Result("%4110%<br>\n", false, null);
		if (pos_varrefweight<0) return new Result("%4111%<br>\n", false, null);
		if (pos_varrefpolarity<0) return new Result("%4112%<br>\n", false, null);
		Hashtable<String, Hashtable<String, Double>> ref_terms=new Hashtable<String, Hashtable<String, Double>>();
		TreeSet<String> ref_polarity=new TreeSet<String>();
		int repcond[]=new int[3];
		String vartoask[]=new String[3];
		vartoask[0]=varrefterm;
		vartoask[1]=varrefweight;
		vartoask[2]=varrefpolarity;
		repcond[0]=0;
		repcond[1]=0;
		repcond[2]=0;
		String[] values=null;
		double tval=0;
		int num_tval=0;
		DataReader datasent = new DataReader(dictsent);
		if (!datasent.open(vartoask, repcond, false))
			return new Result(datasent.getmessage(), false, null);
		while (!datasent.isLast())
		{
			values = datasent.getRecord();
			if (values!=null)
			{
				ref_polarity.add(values[2]);
				try
				{
					tval=Double.parseDouble(values[1]);
				}
				catch (Exception et)
				{
					num_tval++;
					tval=1;
				}
				if (ref_terms.get(values[2])==null)
				{
					Hashtable<String, Double> temp_terms=new Hashtable<String, Double>();
					temp_terms.put(values[0].toLowerCase(), new Double(tval));
					ref_terms.put(values[2], temp_terms);
				}
				else
				{
					Hashtable<String, Double> temp_terms=ref_terms.get(values[2]);
					temp_terms.put(values[0].toLowerCase(), new Double(tval));
					ref_terms.put(values[2], temp_terms);
				}
			}
		}
		datasent.close();
		if (ref_terms.size()==0) return new Result("%4113%<br>\n", false, null);
		if (num_tval>0) result.add(new LocalMessageGetter("%4114% ("+String.valueOf(num_tval)+")<br>\n"));
		Keywords.percentage_done=1;
		String temp_negations=(String)parameters.get(Keywords.negations);
		String[] negations=new String[0];
		if (temp_negations!=null) negations=temp_negations.split(" ");
		String varphrase=(String)parameters.get(Keywords.varphrase);
		DictionaryReader dict= (DictionaryReader)parameters.get(Keywords.dict);
		int pos_varphrase=-1;
		for (int i=0; i<dict.gettotalvar(); i++)
		{
			tempname=dict.getvarname(i);
			tempname=tempname.toLowerCase();
			if (tempname.equalsIgnoreCase(varphrase)) pos_varphrase=i;
		}
		if (pos_varphrase==-1) return new Result("%4115%<br>\n", false, null);
		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(null);
		Hashtable<String, String> temph=new Hashtable<String, String>();
		dsu.defineolddict(dict);
		Iterator<String> itr=ref_polarity.iterator();
		int num_pol=0;
		while(itr.hasNext())
		{
			num_pol++;
			String ref_pol=itr.next();
			dsu.addnewvartoolddict("polarity_"+String.valueOf(num_pol), ref_pol, Keywords.NUMSuffix, temph, temph);
		}
		num_pol=0;
		itr=ref_polarity.iterator();
		while(itr.hasNext())
		{
			num_pol++;
			String ref_pol=itr.next();
			dsu.addnewvartoolddict("polarity_"+String.valueOf(num_pol)+"_neg", ref_pol+" (negations)", Keywords.NUMSuffix, temph, temph);
		}
		Keywords.percentage_done++;
		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);
		DataReader data = new DataReader(dict);
		if (!data.open(null, 0, false))
			return new Result(data.getmessage(), false, null);
		String[] newvalues=new String[ref_polarity.size()*2];
		String[] temp_parts=null;
		int[] neg_parts=null;
		double[] temp_sent=new double[newvalues.length];
		String[] wvalues=null;
		boolean invert_polarity=false;
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				values[pos_varphrase]=values[pos_varphrase].replaceAll("\\s+"," ");
				temp_parts=values[pos_varphrase].split(" ");
				neg_parts=new int[temp_parts.length];
				for (int i=0; i<temp_parts.length; i++)
				{
					neg_parts[i]=1;
					if (negations.length>0)
					{
						for (int j=0; j<negations.length; j++)
						{
							if (temp_parts[i].equalsIgnoreCase(negations[j])) neg_parts[i]=-1;
						}
					}
				}
				for (int i=0; i<temp_sent.length; i++)
				{
					temp_sent[i]=0;
				}
				for (int i=0; i<temp_parts.length; i++)
				{
					if (!temp_parts[i].equals(""))
					{
						num_pol=0;
						itr=ref_polarity.iterator();
						while(itr.hasNext())
						{
							Hashtable<String, Double> temp_terms=ref_terms.get(itr.next());
							if (temp_terms.get(temp_parts[i])!=null)
							{
								tval=(temp_terms.get(temp_parts[i])).doubleValue();
								invert_polarity=false;
								if (i>=1 && negations.length>0)
								{
									if (neg_parts[i-1]==-1)
									{
										invert_polarity=!invert_polarity;
										neg_parts[i-1]=1;
									}
									if (i>=2 && neg_parts[i-2]==-1)
									{
										invert_polarity=!invert_polarity;
										neg_parts[i-2]=1;
									}
									if (i>=3 && neg_parts[i-3]==-1)
									{
										invert_polarity=!invert_polarity;
										neg_parts[i-3]=1;
									}
								}
								if (term_multiplier.size()>0)
								{
									for (int j=0; j<term_multiplier.size(); j++)
									{
										String[] tmm=term_multiplier.get(j);
										int ps=Integer.parseInt(tmm[2]);
										int pe=Integer.parseInt(tmm[3]);
										ps=i-ps;
										if (ps<0) ps=0;
										pe=pe+i+1;
										if (pe>temp_parts.length) pe=temp_parts.length;
										for (int k=ps; k<pe; k++)
										{
											if (temp_parts[k].equalsIgnoreCase(tmm[0]))
											{
												if (!invert_polarity) tval=tval*Double.parseDouble(tmm[1]);
												if (invert_polarity) tval=tval/Double.parseDouble(tmm[1]);
												temp_parts[k]="";
											}
										}
									}
								}
								if (tval>=0)
								{
									if (!invert_polarity) temp_sent[num_pol]=temp_sent[num_pol]+tval;
									if (invert_polarity) temp_sent[num_pol+ref_polarity.size()]=temp_sent[num_pol+ref_polarity.size()]+tval;
								}
								else
								{
									if (invert_polarity) temp_sent[num_pol]=temp_sent[num_pol]+Math.abs(tval);
									if (!invert_polarity) temp_sent[num_pol+ref_polarity.size()]=temp_sent[num_pol+ref_polarity.size()]+Math.abs(tval);
								}
							}
							num_pol++;
						}
					}
				}
				for (int i=0; i<temp_sent.length; i++)
				{
					newvalues[i]=String.valueOf(temp_sent[i]);
				}
				wvalues=dsu.getnewvalues(values, newvalues);
				dw.write(wvalues);
			}
		}
		data.close();
		Keywords.percentage_done++;
		String keyword="Sentiment "+dict.getkeyword();
		String description="Sentiment "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());
		boolean resclose = dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfo = dw.getVarInfo();
		Hashtable<String, String> datatableinfo = dw.getTableInfo();
		result.add(new LocalDictionaryWriter(dw.getdictpath(), keyword,description, author,
		dw.gettabletype(), datatableinfo,dsu.getfinalvarinfo(), tablevariableinfo, dsu.getfinalcl(),dsu.getfinalmd(), null));
		return new Result("", true, result);
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		String[] depn ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 4116, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"sentiment=", "dict", true, 4117, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 4118, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.varphrase, "var=all", true, 4119, dep, "", 2));
		depn[0]=Keywords.dict+"sentiment";
		parameters.add(new GetRequiredParameters(Keywords.varrefterm, "var=all", true, 4121, depn, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varrefweight, "vars=all", true, 4122, depn, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varrefpolarity, "vars=all", true, 4123, depn, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.negations, "text", false, 4124, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.term_multiplier, "multipletext", false, 4152, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 4153, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 4154, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 4155, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 4156, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 4157, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 4159, dep, "", 2));

		return parameters;
	}
	/**
	*Return the name of the procedure and the group to which belongs
	*/
	public String[] getstepinfo() {
		String[] info=new String[2];
		info[0]="4177";
		info[1]="4120";
		return info;
	}
}
