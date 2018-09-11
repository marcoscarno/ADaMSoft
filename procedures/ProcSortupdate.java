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
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.dataaccess.CompareObjects;
import ADaMSoft.dataaccess.DataSorter;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.VariableUtilities;
import ADaMSoft.utilities.DataSetUtilities;

/**
* This is the procedure that sorts and merge two datasets
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcSortupdate implements RunStep
{
	/**
	* Starts the execution of Proc Sortmerge and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		boolean noconversion=false;
		boolean onlynew=false;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.var, Keywords.dict, Keywords.dict+"new"};
		String [] optionalparameters=new String[] {Keywords.OUT.toLowerCase()+"info", Keywords.noconversion, Keywords.onlynew, Keywords.replace};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		onlynew=(parameters.get(Keywords.onlynew)!=null);
		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String vartemp=(String)parameters.get(Keywords.var.toLowerCase());
		VariableUtilities varu=new VariableUtilities(dict, null, vartemp, null, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);
		String[] mergevar=varu.getanalysisvar();

		boolean isoutu =(parameters.get(Keywords.OUT.toLowerCase()+"info")!=null);

		DataWriter dwu=null;
		if (isoutu)
		{
			dwu=new DataWriter(parameters, Keywords.OUT.toLowerCase()+"info");
			if (!dwu.getmessage().equals(""))
				return new Result(dwu.getmessage(), false, null);
		}
		noconversion=(parameters.get(Keywords.noconversion)!=null);

		DictionaryReader dictnew = (DictionaryReader)parameters.get(Keywords.dict+"new");
		VariableUtilities varub=new VariableUtilities(dictnew, null, vartemp, null, null, null);
		if (varub.geterror())
			return new Result(varub.getmessage(), false, null);

		String replace=(String)parameters.get(Keywords.replace);

		String keyword="SortUpdate "+dict.getkeyword()+" "+dictnew.getkeyword();
		String description="SortUpdate "+dict.getdescription()+" "+dictnew.getdescription();
		String author=(String)parameters.get(Keywords.client_host.toLowerCase());

		/*Check for variables*/
		Vector<String> remaininga=new Vector<String>();
		int totalvara=dict.gettotalvar();
		for (int i=0; i<totalvara; i++)
		{
			boolean remain=true;
			String tempname=dict.getvarname(i);
			for (int j=0; j<mergevar.length; j++)
			{
				if (tempname.equalsIgnoreCase(mergevar[j]))
					remain=false;
			}
			if (remain)
				remaininga.add(tempname);
		}
		Vector<String> remainingb=new Vector<String>();
		int totalvarb=dictnew.gettotalvar();
		for (int i=0; i<totalvarb; i++)
		{
			boolean remain=true;
			String tempname=dictnew. getvarname(i);
			for (int j=0; j<mergevar.length; j++)
			{
				if (tempname.equalsIgnoreCase(mergevar[j]))
					remain=false;
			}
			if (remain)
				remainingb.add(tempname);
		}

		boolean check=false;
		int[] posvup=new int[remainingb.size()];
		for (int i=0; i<remainingb.size(); i++)
		{
			check=false;
			String vara=remainingb.get(i);
			for (int j=0; j<remaininga.size(); j++)
			{
				String varb=remaininga.get(j);
				if (vara.equalsIgnoreCase(varb))
				{
					posvup[i]=j;
					check=true;
				}
			}
			if (!check)
			{
				return new Result("%2258% ("+vara+")<br>\n", false, null);
			}
		}

		Vector<Hashtable<String, String>> fixedvariableinfoa=dict.getfixedvariableinfo();
		Vector<Hashtable<String, String>> fixedvariableinfob=dictnew.getfixedvariableinfo();

		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);

		String[] selvar=new String[mergevar.length+remaininga.size()];
		for (int i=0; i<mergevar.length; i++)
		{
			selvar[i]=mergevar[i];
		}
		for (int i=0; i<remaininga.size(); i++)
		{
			selvar[i+mergevar.length]=remaininga.get(i);
		}
		dsu.defineolddictwithvar(dict, selvar);

		DataSetUtilities dsup=new DataSetUtilities();
		dsup.setreplace(replace);
		for (int i=0; i<mergevar.length; i++)
		{
			dsup.addnewvar("var_"+mergevar[i], dict.getvarlabelfromname(mergevar[i]), dict.getvarformatfromname(mergevar[i]), dict.getcodelabelfromname(mergevar[i]), dict.getmissingdatafromname(mergevar[i]));
		}
		for (int i=0; i<remaininga.size(); i++)
		{
			dsup.addnewvar("old_"+remaininga.get(i), "%2270% "+dict.getvarlabelfromname(remaininga.get(i)), dict.getvarformatfromname(remaininga.get(i)), dict.getcodelabelfromname(remaininga.get(i)), dict.getmissingdatafromname(remaininga.get(i)));
		}
		for (int i=0; i<remainingb.size(); i++)
		{
			dsup.addnewvar("new_"+remainingb.get(i), "%2271% "+dictnew.getvarlabelfromname(remainingb.get(i)), dictnew.getvarformatfromname(remainingb.get(i)), dictnew.getcodelabelfromname(remainingb.get(i)), dictnew.getmissingdatafromname(remainingb.get(i)));
		}
		Hashtable<String, String> newclu=new Hashtable<String, String> ();
		Hashtable<String, String> temph=new Hashtable<String, String> ();
		newclu.put("1","%2260%");
		newclu.put("2","%2261%");
		newclu.put("3","%2273%");
		newclu.put("4","%2276%");
		dsup.addnewvar("updateinfo", "%2259%", Keywords.TEXTSuffix, newclu, temph);

		int[] replacerulea=new int[fixedvariableinfoa.size()];
		int[] replaceruleb=new int[fixedvariableinfob.size()];

		int reprule=0;
		if (replace==null)
			reprule=0;
		else if (replace.equalsIgnoreCase(Keywords.replaceall))
			reprule=1;
		else if (replace.equalsIgnoreCase(Keywords.replaceformat))
			reprule=2;
		else if (replace.equalsIgnoreCase(Keywords.replacemissing))
			reprule=3;

		for (int j=0; j<fixedvariableinfoa.size(); j++)
		{
			replacerulea[j]=reprule;
		}
		for (int j=0; j<fixedvariableinfob.size(); j++)
		{
			replaceruleb[j]=reprule;
		}

		String[] vara=new String[fixedvariableinfoa.size()];
		String[] varb=new String[fixedvariableinfob.size()];
		for (int i=0; i<mergevar.length; i++)
		{
			vara[i]=mergevar[i];
			varb[i]=mergevar[i];
		}
		for (int i=0; i<remaininga.size(); i++)
		{
			String name=remaininga.get(i);
			vara[i+mergevar.length]=name;
		}
		for (int i=0; i<remainingb.size(); i++)
		{
			String name=remainingb.get(i);
			varb[i+mergevar.length]=name;
		}

		String tempdir=(String)parameters.get(Keywords.WorkDir);
		DataSorter datasortera=new DataSorter(dict, vara, true, tempdir, replacerulea, null);
		datasortera.setconversion(noconversion);
		if (datasortera.geterror())
			return new Result(datasortera.getmessage(), false, null);

		datasortera.sortdata();
		if (datasortera.geterror())
		{
			datasortera.deletefile();
			return new Result(datasortera.getmessage(), false, null);
		}

		DataSorter datasorterb=new DataSorter(dictnew, varb, true, tempdir, replaceruleb, null);
		datasorterb.setconversion(noconversion);
		if (datasorterb.geterror())
		{
			datasortera.deletefile();
			return new Result(datasorterb.getmessage(), false, null);
		}

		datasorterb.sortdata();
		if (datasorterb.geterror())
		{
			datasortera.deletefile();
			datasorterb.deletefile();
			return new Result(datasorterb.getmessage(), false, null);
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
		{
			datasortera.deletefile();
			datasorterb.deletefile();
			return new Result(dw.getmessage(), false, null);
		}
		if (isoutu)
		{
			if (!dwu.opendatatable(dsup.getfinalvarinfo()))
			{
				datasortera.deletefile();
				datasorterb.deletefile();
				return new Result(dwu.getmessage(), false, null);
			}
		}

		datasortera.openFinalFile();
		if (datasortera.geterror())
		{
			datasortera.deletefile();
			datasorterb.deletefile();
			return new Result(datasortera.getmessage(), false, null);
		}
		datasorterb.openFinalFile();
		if (datasorterb.geterror())
		{
			datasortera.deletefile();
			datasorterb.deletefile();
			return new Result(datasorterb.getmessage(), false, null);
		}

		int totalrecorda=datasortera.getTotalRecords();
		int totalrecordb=datasorterb.getTotalRecords();
		int totalrecord=totalrecorda;
		if (totalrecordb>totalrecord)
			totalrecord=totalrecordb;
		boolean readfromfirst=true;
		boolean readfromsecond=true;
		int firstpointer=0;
		int lastpointer=0;
		Object[] Valuesa=datasortera.readFinalRecord();
		if (datasortera.geterror())
		{
			datasortera.deletefile();
			datasorterb.deletefile();
			return new Result(datasortera.getmessage(), false, null);
		}
		Object[] Valuesb=datasorterb.readFinalRecord();
		if (datasorterb.geterror())
		{
			datasortera.deletefile();
			datasorterb.deletefile();
			return new Result(datasorterb.getmessage(), false, null);
		}
		Object[] oldValuesa=new Object[0];
		Object[] oldValuesb=new Object[0];
		String [] values=new String[Valuesa.length];
		String [] valuesup=new String[Valuesa.length+remainingb.size()+1];
		int compare=0;

		CompareObjects co=new CompareObjects(mergevar.length, true);
		co.setconversion(noconversion);

		int compareTemp=0;
		String newval="";
		String oldval="";
		int recordinu=0;
		boolean writtenu=false;
		boolean checkifnew=false;
		while ((readfromfirst) || (readfromsecond))
		{
			checkifnew=false;
			if ((Valuesa.length>0) && (Valuesb.length>0))
			{
				compare=co.getComparison(Valuesa, Valuesb);
				if (compare==0)
				{
					oldValuesa=new Object[Valuesa.length];
					oldValuesb=new Object[Valuesb.length];
					for (int j=0; j<Valuesb.length; j++)
					{
						oldValuesb[j]=Valuesb[j];
					}
					writtenu=false;
					for (int j=0; j<posvup.length; j++)
					{
						newval=(Valuesb[j+mergevar.length].toString());
						oldval=(Valuesa[mergevar.length+posvup[j]].toString());
						if (!newval.equals(oldval))
						{
							recordinu++;
							if ((isoutu) && (!writtenu))
							{
								for (int k=0; k<valuesup.length; k++)
								{
									valuesup[k]="";
								}
								for (int k=0; k<Valuesa.length; k++)
								{
									valuesup[k]=Valuesa[k].toString();
								}
								for (int k=mergevar.length; k<Valuesb.length; k++)
								{
									valuesup[k+Valuesa.length-mergevar.length]=Valuesb[k].toString();
								}
								valuesup[Valuesa.length+remainingb.size()]="1";
								dwu.write(valuesup);
								writtenu=true;
							}
							Valuesa[mergevar.length+posvup[j]]=newval;
						}
					}
					for (int j=0; j<Valuesa.length; j++)
					{
						try
						{
							values[j]=Valuesa[j].toString();
						}
						catch (Exception exc)
						{
							values[j]="";
						}
						oldValuesa[j]=Valuesa[j];
					}
					dw.write(values);
					firstpointer++;
					lastpointer++;
					if (firstpointer<totalrecorda)
						Valuesa=datasortera.readFinalRecord();
					else
					{
						readfromfirst=false;
						Valuesa=new Object[0];
					}
					if (lastpointer<totalrecordb)
						Valuesb=datasorterb.readFinalRecord();
					else
					{
						readfromsecond=false;
						Valuesb=new Object[0];
					}
				}
				else if (compare<0)
				{
					compareTemp=-1;
					if (oldValuesb.length>0)
						compareTemp=co.getComparison(Valuesa, oldValuesb);
					oldValuesa=new Object[Valuesa.length];
					if (compareTemp==0)
					{
						writtenu=false;
						for (int j=0; j<posvup.length; j++)
						{
							newval=(oldValuesb[j+mergevar.length].toString());
							oldval=(Valuesa[mergevar.length+posvup[j]].toString());
							if (!newval.equals(oldval))
							{
								recordinu++;
								if ((isoutu) && (!writtenu))
								{
									for (int k=0; k<valuesup.length; k++)
									{
										valuesup[k]="";
									}
									for (int k=0; k<Valuesa.length; k++)
									{
										valuesup[k]=Valuesa[k].toString();
									}
									for (int k=mergevar.length; k<oldValuesb.length; k++)
									{
										valuesup[k+Valuesa.length-mergevar.length]=oldValuesb[k].toString();
									}
									valuesup[Valuesa.length+remainingb.size()]="1";
									dwu.write(valuesup);
									writtenu=true;
								}
								Valuesa[mergevar.length+posvup[j]]=newval;
							}
						}
					}
					else
					{
						oldValuesb=new Object[0];
						checkifnew=true;
					}
					for (int j=0; j<Valuesa.length; j++)
					{
						try
						{
							values[j]=Valuesa[j].toString();
						}
						catch (Exception exc)
						{
							values[j]="";
						}
						oldValuesa[j]=Valuesa[j];
					}
					if (!onlynew)
						dw.write(values);
					else
					{
						if (!checkifnew)
							dw.write(values);
						else
						{
							for (int k=0; k<valuesup.length; k++)
							{
								valuesup[k]="";
							}
							for (int k=0; k<Valuesa.length; k++)
							{
								valuesup[k]=Valuesa[k].toString();
							}
							valuesup[Valuesa.length+remainingb.size()]="3";
							dwu.write(valuesup);
						}
					}
					firstpointer++;
					if (firstpointer<totalrecorda)
						Valuesa=datasortera.readFinalRecord();
					else
					{
						readfromfirst=false;
						Valuesa=new Object[0];
					}
				}
				else
				{
					compareTemp=-1;
					for (int j=0; j<values.length; j++)
					{
						values[j]="";
					}
					if (oldValuesa.length>0)
						compareTemp=co.getComparison(oldValuesa, Valuesb);
					if (compareTemp==0)
					{
						for (int j=0; j<values.length; j++)
						{
							try
							{
								values[j]=oldValuesa[j].toString();
							}
							catch (Exception exc)
							{
								values[j]="";
							}
						}
						writtenu=false;
						for (int j=0; j<posvup.length; j++)
						{
							newval=(Valuesb[j+mergevar.length].toString());
							oldval=(oldValuesa[mergevar.length+posvup[j]].toString());
							if (!newval.equals(oldval))
							{
								recordinu++;
								if ((isoutu) && (!writtenu))
								{
									for (int k=0; k<valuesup.length; k++)
									{
										valuesup[k]="";
									}
									for (int k=0; k<oldValuesa.length; k++)
									{
										valuesup[k]=oldValuesa[k].toString();
									}
									for (int k=mergevar.length; k<Valuesb.length; k++)
									{
										valuesup[k+Valuesa.length-mergevar.length]=Valuesb[k].toString();
									}
									valuesup[oldValuesa.length+remainingb.size()]="1";
									dwu.write(valuesup);
									writtenu=true;
								}
								values[mergevar.length+posvup[j]]=newval;
							}
						}
					}
					else
					{
						recordinu++;
						oldValuesa=new Object[0];
						for (int j=0; j<posvup.length; j++)
						{
							newval=(Valuesb[j+mergevar.length].toString());
							values[mergevar.length+posvup[j]]=newval;
						}
						if (isoutu)
						{
							for (int k=0; k<valuesup.length; k++)
							{
								valuesup[k]="";
							}
							for (int j=0; j<posvup.length; j++)
							{
								newval=(Valuesb[j+mergevar.length].toString());
								valuesup[mergevar.length+remaininga.size()+posvup[j]]=newval;
							}
							for (int r=0; r<mergevar.length; r++)
							{
								try
								{
									valuesup[r]=Valuesb[r].toString();
								}
								catch (Exception exc){}
							}
							valuesup[mergevar.length+remainingb.size()+remaininga.size()]="2";
							dwu.write(valuesup);
						}
					}
					oldValuesb=new Object[Valuesb.length];
					for (int r=0; r<mergevar.length; r++)
					{
						try
						{
							values[r]=Valuesb[r].toString();
						}
						catch (Exception exc)
						{
							values[r]="";
						}
						oldValuesb[r]=Valuesb[r];
					}
					for (int j=mergevar.length; j<Valuesb.length; j++)
					{
						oldValuesb[j]=Valuesb[j];
					}
					dw.write(values);
					lastpointer++;
					if (lastpointer<totalrecordb)
						Valuesb=datasorterb.readFinalRecord();
					else
					{
						readfromsecond=false;
						Valuesb=new Object[0];
					}
				}
			}
			else if ((Valuesa.length==0) && (Valuesb.length==0))
			{
				readfromfirst=false;
				readfromsecond=false;
			}
			else if ((Valuesa.length>0) && (Valuesb.length==0))
			{
				compareTemp=-1;
				if (oldValuesb.length>0)
					compareTemp=co.getComparison(Valuesa, oldValuesb);
				oldValuesa=new Object[Valuesa.length];
				for (int j=0; j<Valuesa.length; j++)
				{
					try
					{
						values[j]=Valuesa[j].toString();
					}
					catch (Exception exc)
					{
						values[j]="";
					}
					oldValuesa[j]=Valuesa[j];
				}
				if (compareTemp==0)
				{
					writtenu=false;
					for (int j=0; j<posvup.length; j++)
					{
						newval=(oldValuesb[j+mergevar.length].toString());
						oldval=(Valuesa[mergevar.length+posvup[j]].toString());
						if (!newval.equals(oldval))
						{
							values[mergevar.length+posvup[j]]=newval;
							if ((isoutu) && (!writtenu))
							{
								for (int k=0; k<valuesup.length; k++)
								{
									valuesup[k]="";
								}
								for (int k=0; k<Valuesa.length; k++)
								{
									valuesup[k]=Valuesa[k].toString();
								}
								for (int k=mergevar.length; k<oldValuesb.length; k++)
								{
									valuesup[k+Valuesa.length-mergevar.length]=oldValuesb[k].toString();
								}
								valuesup[oldValuesa.length+remainingb.size()]="1";
								dwu.write(valuesup);
								writtenu=true;
							}
						}
					}
				}
				else
				{
					oldValuesb=new Object[0];
					checkifnew=true;
				}
				if (!onlynew)
					dw.write(values);
				else
				{
					if (!checkifnew)
						dw.write(values);
					else
					{
						for (int k=0; k<valuesup.length; k++)
						{
							valuesup[k]="";
						}
						for (int k=0; k<Valuesa.length; k++)
						{
							valuesup[k]=Valuesa[k].toString();
						}
						valuesup[Valuesa.length+remainingb.size()]="3";
						dwu.write(valuesup);
					}
				}
				firstpointer++;
				if (firstpointer<totalrecorda)
					Valuesa=datasortera.readFinalRecord();
				else
				{
					readfromfirst=false;
					Valuesa=new Object[0];
				}
			}
			else if ((Valuesa.length==0) && (Valuesb.length>0))
			{
				for (int j=0; j<values.length; j++)
				{
					values[j]="";
				}
				compareTemp=-1;
				if (oldValuesa.length>0)
					compareTemp=co.getComparison(oldValuesa, Valuesb);
				oldValuesb=new Object[Valuesb.length];
				for (int r=0; r<mergevar.length; r++)
				{
					try
					{
						values[r]=Valuesb[r].toString();
					}
					catch (Exception exc)
					{
						values[r]="";
					}
					oldValuesb[r]=Valuesb[r];
				}
				if (compareTemp==0)
				{
					writtenu=false;
					for (int j=0; j<posvup.length; j++)
					{
						try
						{
							values[j]=oldValuesa[j].toString();
						}
						catch (Exception exc)
						{
							values[j]="";
						}
						newval=(Valuesb[j+mergevar.length].toString());
						oldval=(oldValuesa[mergevar.length+posvup[j]].toString());
						if (!newval.equals(oldval))
						{
							values[mergevar.length+posvup[j]]=newval;
							if ((isoutu) && (!writtenu))
							{
								for (int k=0; k<valuesup.length; k++)
								{
									valuesup[k]="";
								}
								for (int k=0; k<oldValuesa.length; k++)
								{
									valuesup[k]=oldValuesa[k].toString();
								}
								for (int k=mergevar.length; k<Valuesb.length; k++)
								{
									valuesup[k+Valuesa.length-mergevar.length]=Valuesb[k].toString();
								}
								valuesup[oldValuesa.length+remainingb.size()]="1";
								dwu.write(valuesup);
								writtenu=true;
							}
						}
					}
				}
				else
				{
					for (int j=0; j<posvup.length; j++)
					{
						newval=(Valuesb[j+mergevar.length].toString());
						values[mergevar.length+posvup[j]]=newval;
					}
					oldValuesa=new Object[0];
					if (isoutu)
					{
						for (int k=0; k<valuesup.length; k++)
						{
							valuesup[k]="";
						}
						for (int j=0; j<posvup.length; j++)
						{
							newval=(Valuesb[j+mergevar.length].toString());
							valuesup[mergevar.length+remaininga.size()+posvup[j]]=newval;
						}
						for (int r=0; r<mergevar.length; r++)
						{
							try
							{
								valuesup[r]=Valuesb[r].toString();
							}
							catch (Exception exc){}
						}
						valuesup[mergevar.length+remainingb.size()+remaininga.size()]="4";
						dwu.write(valuesup);
					}
				}
				for (int j=mergevar.length; j<Valuesb.length; j++)
				{
					oldValuesb[j]=Valuesb[j];
				}
				dw.write(values);
				lastpointer++;
				if (lastpointer<totalrecordb)
					Valuesb=datasorterb.readFinalRecord();
				else
				{
					readfromsecond=false;
					Valuesb=new Object[0];
				}
			}
		}
		if (recordinu==0)
		{
			result.add(new LocalMessageGetter("%2262%<br>\n"));
			if (isoutu)
			{
				result.add(new LocalMessageGetter("%2268%<br>\n"));
				dwu.deletetmp();
			}
			isoutu=false;
		}
		else
			result.add(new LocalMessageGetter("%2269% "+String.valueOf(recordinu)+"<br>\n"));

		datasortera.closeFinalFile();
		datasorterb.closeFinalFile();
		Valuesa=new Object[0];
		Valuesb=new Object[0];
		oldValuesa=new Object[0];
		oldValuesb=new Object[0];
		Valuesa=null;
		Valuesb=null;
		oldValuesa=null;
		oldValuesb=null;
		System.gc();
		Hashtable<String, String> othertableinfo=new Hashtable<String, String>();
		othertableinfo.put(Keywords.SORTED.toLowerCase(), vartemp);
		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
		Hashtable<String, String> datatableinfo=dw.getTableInfo();
		result.add(new LocalDictionaryWriter(dw.getdictpath(), keyword, description, author, dw.gettabletype(),
		datatableinfo, dsu.getfinalvarinfo(), tablevariableinfo, dsu.getfinalcl(), dsu.getfinalmd(), othertableinfo));
		if (isoutu)
		{
			resclose=dwu.close();
			if (!resclose)
				return new Result(dwu.getmessage(), false, null);
			Vector<Hashtable<String, String>> ctablevariableinfo=dwu.getVarInfo();
			Hashtable<String, String> cdatatableinfo=dwu.getTableInfo();
			result.add(new LocalDictionaryWriter(dwu.getdictpath(), keyword, description, author, dwu.gettabletype(),
			cdatatableinfo, dsup.getfinalvarinfo(), ctablevariableinfo, dsup.getfinalcl(), dsup.getfinalmd(), null));
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
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 2255, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"new=", "dict", true, 2256, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 440, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"info=", "setting=out", false, 2254, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[2];
		dep[0]="dict";
		dep[1]="dictnew";
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=common", true, 2257, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.noconversion,"checkbox",false,2245,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.onlynew,"checkbox",false,2272,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="4162";
		retprocinfo[1]="2253";
		return retprocinfo;
	}
}
