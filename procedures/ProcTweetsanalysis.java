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
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.ObjectTransformer;

import ADaMSoft.keywords.Keywords;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

/**
* This is the procedure that implements the possibility to analize a series of tweets
* @author marco.scarno@gmail.com
* @date 19/02/2017
*/
public class ProcTweetsanalysis extends ObjectTransformer implements RunStep
{
	/**
	* Starts the execution of Proc Tweetsanalysis
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		String [] requiredparameters=new String[] {Keywords.dict, Keywords.vartweets};
		String [] optionalparameters=new String[] {Keywords.dict+"hashtags", Keywords.varhashtags,
		Keywords.OUT.toLowerCase()+"freqhashtags", Keywords.OUT.toLowerCase()+"hashtagswords",
		Keywords.OUT.toLowerCase()+"hashtags", Keywords.OUT.toLowerCase()+"users", Keywords.OUT.toLowerCase()+"frequsers",
		Keywords.OUT.toLowerCase()+"usershashtags", Keywords.OUT.toLowerCase()+"mathashtagswords",
		Keywords.minfreqhashtags, Keywords.minfrequsers, Keywords.noretwett, Keywords.where};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		String minfreqhashtags = (String) parameters.get(Keywords.minfreqhashtags);
		int mfh=10;
		if (minfreqhashtags!=null)
		{
			mfh=-1;
			try
			{
				mfh=Integer.parseInt(minfreqhashtags);
			}
			catch (Exception e)
			{
				mfh=-1;
			}
			if (mfh<1)
			{
				Keywords.procedure_error=true;
				return new Result("%3855% ("+minfreqhashtags+")<br>\n", false, null);
			}
		}
		String minfrequsers = (String) parameters.get(Keywords.minfrequsers);
		int mfu=10;
		if (minfrequsers!=null)
		{
			mfu=-1;
			try
			{
				mfu=Integer.parseInt(minfrequsers);
			}
			catch (Exception e)
			{
				mfu=-1;
			}
			if (mfu<1)
			{
				Keywords.procedure_error=true;
				return new Result("%3861% ("+minfrequsers+")<br>\n", false, null);
			}
		}

		boolean noretwett=(parameters.get(Keywords.noretwett)!=null);

		boolean outhashtags=false;
		boolean outfreqhashtags=false;
		boolean outusers=false;
		boolean outfrequsers=false;
		boolean outusers_hashtags=false;
		boolean outhashtags_words=false;
		boolean outmathashtags_words=false;
		if (parameters.get(Keywords.OUT.toLowerCase()+"hashtags")!=null)
			outhashtags=true;
		if (parameters.get(Keywords.OUT.toLowerCase()+"freqhashtags")!=null)
			outfreqhashtags=true;
		if (parameters.get(Keywords.OUT.toLowerCase()+"users")!=null)
			outusers=true;
		if (parameters.get(Keywords.OUT.toLowerCase()+"frequsers")!=null)
			outfrequsers=true;
		if (parameters.get(Keywords.OUT.toLowerCase()+"usershashtags")!=null)
			outusers_hashtags=true;
		if (parameters.get(Keywords.OUT.toLowerCase()+"hashtagswords")!=null)
			outhashtags_words=true;
		if (parameters.get(Keywords.OUT.toLowerCase()+"mathashtagswords")!=null)
			outmathashtags_words=true;
		if (!outhashtags && !outfreqhashtags && !outusers && !outfrequsers && !outusers_hashtags && !outhashtags_words || !outmathashtags_words)
			return new Result("%3859%<br>\n", false, null);

		DataWriter dwht=null;
		if (outhashtags)
		{
			dwht=new DataWriter(parameters, Keywords.OUT.toLowerCase()+"hashtags");
			if (!dwht.getmessage().equals(""))
				return new Result(dwht.getmessage(), false, null);
		}

		DataWriter dwus=null;
		if (outusers)
		{
			dwus=new DataWriter(parameters, Keywords.OUT.toLowerCase()+"users");
			if (!dwus.getmessage().equals(""))
				return new Result(dwus.getmessage(), false, null);
		}

		DataWriter dwhtus=null;
		if (outusers_hashtags)
		{
			dwhtus=new DataWriter(parameters, Keywords.OUT.toLowerCase()+"usershashtags");
			if (!dwhtus.getmessage().equals(""))
				return new Result(dwhtus.getmessage(), false, null);
		}

		DataWriter dwfht=null;
		if (outfreqhashtags)
		{
			dwfht=new DataWriter(parameters, Keywords.OUT.toLowerCase()+"freqhashtags");
			if (!dwfht.getmessage().equals(""))
				return new Result(dwfht.getmessage(), false, null);
		}

		DataWriter dwfus=null;
		if (outfrequsers)
		{
			dwfus=new DataWriter(parameters, Keywords.OUT.toLowerCase()+"frequsers");
			if (!dwfus.getmessage().equals(""))
				return new Result(dwfus.getmessage(), false, null);
		}

		DataWriter dwwht=null;
		if (outhashtags_words)
		{
			dwwht=new DataWriter(parameters, Keywords.OUT.toLowerCase()+"hashtagswords");
			if (!dwwht.getmessage().equals(""))
				return new Result(dwwht.getmessage(), false, null);
		}

		DataWriter dwmwht=null;
		if (outmathashtags_words)
		{
			dwmwht=new DataWriter(parameters, Keywords.OUT.toLowerCase()+"mathashtagswords");
			if (!dwmwht.getmessage().equals(""))
				return new Result(dwmwht.getmessage(), false, null);
		}
		DataSetUtilities dsu1=null;
		DataSetUtilities dsu2=null;
		DataSetUtilities dsu3=null;
		DataSetUtilities dsu4=null;
		DataSetUtilities dsu5=null;
		DataSetUtilities dsu6=null;
		DataSetUtilities dsu7=null;

		Vector<String> listh=new Vector<String>();
		if (parameters.get(Keywords.dict+"hashtags")!=null)
		{
			if (minfreqhashtags!=null)
				return new Result("%3881%<br>\n", false, null);
			DictionaryReader dicth = (DictionaryReader)parameters.get(Keywords.dict+"hashtags");
			String varhashtags=(String)parameters.get(Keywords.varhashtags.toLowerCase());
			String[] testvh=varhashtags.split(" ");
			if (testvh.length!=1)
				return new Result("%3882%<br>\n", false, null);
			int position_ht=-1;
			for (int i=0; i<dicth.gettotalvar(); i++)
			{
				if (varhashtags.equalsIgnoreCase(dicth.getvarname(i))) position_ht=i;
			}
			if (position_ht==-1)
				return new Result("%3883% ("+varhashtags+")<br>\n", false, null);
			DataReader datah = new DataReader(dicth);
			String[] valuesh=null;
			if (!datah.open(testvh, 0, false))
				return new Result(datah.getmessage(), false, null);
			while (!datah.isLast())
			{
				valuesh = datah.getRecord();
				if (valuesh!=null)
				{
					if (!listh.contains(valuesh[0].toLowerCase())) listh.add(valuesh[0].toLowerCase());
				}
			}
			datah.close();
		}

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String vartweets=(String)parameters.get(Keywords.vartweets.toLowerCase());

		String[] testv=vartweets.split(" ");
		if (testv.length!=1)
			return new Result("%3844%<br>\n", false, null);

		int position_tweets=-1;

		for (int i=0; i<dict.gettotalvar(); i++)
		{
			if (vartweets.equalsIgnoreCase(dict.getvarname(i))) position_tweets=i;
		}
		if (position_tweets==-1)
			return new Result("%3845% ("+vartweets+")<br>\n", false, null);

		DataReader data = new DataReader(dict);

		String[] values=null;

		String dw_keyword="Tweet analysis "+dict.getkeyword();
		String dw_description="Tweet analysis "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());
		TreeMap<String, Integer> ashtags=new TreeMap<String, Integer>();
		TreeMap<String, Integer> users=new TreeMap<String, Integer>();
		TreeMap<String, Hashtable<String, Integer>> matoutput=new TreeMap<String, Hashtable<String, Integer>>();
		String patternHashtags = "(?:\\s|\\A)[##]+([A-Za-z0-9-_]+)";
		String patternUsers = "(?:\\s|\\A)[@]+([A-Za-z0-9-_]+)";
		Pattern patternHT = Pattern.compile(patternHashtags);
		Pattern patternUS = Pattern.compile(patternUsers);
		Matcher matcherHT;
		Matcher matcherUS;
		Vector<String> tempht=new Vector<String>();
		String tempvalue="";
		int r = 0;
		int index = 0;
		int[] combination=new int[2];
		String actualtweet="";
		int indexOfHttp=0;
		int endPoint=0;
		String part_eliminate="";
		String[] parts_eliminate;
		String start_h="";
		String end_h="";
		Vector<String> rh=new Vector<String>();
		int totindex=0;
		if (outhashtags || outfreqhashtags || outhashtags_words || outusers_hashtags)
		{
			if (!data.open(testv, 0, false))
				return new Result(data.getmessage(), false, null);
			String where=(String)parameters.get(Keywords.where.toLowerCase());
			if (where!=null)
			{
				if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
			}
			Hashtable<TreeSet<String>, Integer> cashtags=new Hashtable<TreeSet<String>, Integer>();
			TreeMap<String, Vector<String>> ashtags_words=new TreeMap<String, Vector<String>>();
			while (!data.isLast())
			{
				values = data.getRecord();
				if (values!=null)
				{
					actualtweet=values[0].toLowerCase();
					if (noretwett && actualtweet.startsWith("rt ")) actualtweet="";
					if (!actualtweet.equals(""))
					{
						try
						{
							while (actualtweet.contains("http:"))
							{
								indexOfHttp = actualtweet.indexOf("http:");
								endPoint = (actualtweet.indexOf(" ", indexOfHttp) != -1) ? actualtweet.indexOf(" ", indexOfHttp) : actualtweet.length();
								start_h="";
								end_h="";
								if (indexOfHttp-5>0) start_h=actualtweet.substring(0, indexOfHttp-5);
								if (endPoint<actualtweet.length()) end_h=actualtweet.substring(endPoint);
								actualtweet=start_h+" "+end_h;
							}
							while (actualtweet.contains("https:"))
							{
								indexOfHttp = actualtweet.indexOf("https:");
								endPoint = (actualtweet.indexOf(" ", indexOfHttp) != -1) ? actualtweet.indexOf(" ", indexOfHttp) : actualtweet.length();
								start_h="";
								end_h="";
								if (indexOfHttp-6>0) start_h=actualtweet.substring(0, indexOfHttp-6);
								if (endPoint<actualtweet.length()) end_h=actualtweet.substring(endPoint);
								actualtweet=start_h+" "+end_h;
							}
						}
						catch (Exception ep){}
						actualtweet=actualtweet.replaceAll("\\s+"," ");
						tempht.clear();
						part_eliminate="";
						matcherHT = patternHT.matcher(actualtweet);
						while (matcherHT.find())
						{
							tempvalue = matcherHT.group();
							tempvalue = tempvalue.replace(" ", "");
							part_eliminate=part_eliminate+tempvalue+" ";
						}
						if (!part_eliminate.equals(""))
						{
							part_eliminate=part_eliminate.replaceAll("\\s+"," ");
							parts_eliminate=part_eliminate.split(" ");
							rh.clear();
							for (int j=0; j<parts_eliminate.length; j++)
							{
								if (!rh.contains(parts_eliminate[j])) rh.add(parts_eliminate[j]);
							}
							parts_eliminate=new String[rh.size()];
							for (int j=0; j<rh.size(); j++)
							{
								parts_eliminate[j]=rh.get(j);
							}
							rh.clear();
							for (int j=0; j<parts_eliminate.length; j++)
							{
								if (!tempht.contains(parts_eliminate[j]))
									tempht.add(parts_eliminate[j]);
								if (ashtags.get(parts_eliminate[j])==null)
									ashtags.put(parts_eliminate[j], new Integer(1));
								else
								{
									index=((Integer)ashtags.get(parts_eliminate[j])).intValue();
									ashtags.put(parts_eliminate[j], new Integer(index+1));
								}
								actualtweet=actualtweet.replaceAll(parts_eliminate[j]," ");
							}
							actualtweet=actualtweet.replaceAll("\\s+"," ");
						}
						if (outhashtags_words)
						{
							part_eliminate="";
							matcherUS = patternUS.matcher(actualtweet);
							while (matcherUS.find())
							{
								tempvalue = matcherUS.group();
								tempvalue = tempvalue.replace(" ", "");
								part_eliminate=part_eliminate+tempvalue+" ";
							}
							if (!part_eliminate.equals(""))
							{
								part_eliminate=part_eliminate.replaceAll("\\s+"," ");
								parts_eliminate=part_eliminate.split(" ");
								rh.clear();
								for (int j=0; j<parts_eliminate.length; j++)
								{
									if (!rh.contains(parts_eliminate[j])) rh.add(parts_eliminate[j]);
								}
								parts_eliminate=new String[rh.size()];
								for (int j=0; j<rh.size(); j++)
								{
									parts_eliminate[j]=rh.get(j);
								}
								rh.clear();
								for (int j=0; j<parts_eliminate.length; j++)
								{
									actualtweet=actualtweet.replaceAll(parts_eliminate[j]," ");
								}
								actualtweet=actualtweet.replaceAll("\\s+"," ");
							}
							actualtweet=onlyAscii(actualtweet);
							actualtweet=actualtweet.replaceAll("\\s+"," ");
							actualtweet.trim();
							if (!actualtweet.equals("") && tempht.size()>0)
							{
								for (int i=0; i<tempht.size(); i++)
								{
									if (ashtags_words.get(tempht.get(i))!=null)
									{
										Vector<String> thw=ashtags_words.get(tempht.get(i));
										if (!thw.contains(actualtweet)) thw.add(actualtweet);
										ashtags_words.put(tempht.get(i), thw);
									}
									else
									{
										Vector<String> thw=new Vector<String>();
										thw.add(actualtweet);
										ashtags_words.put(tempht.get(i), thw);
									}
								}
								if (outmathashtags_words)
								{
									parts_eliminate=actualtweet.split(" ");
									for (int i=0; i<parts_eliminate.length; i++)
									{
										if (matoutput.get(parts_eliminate[i])==null)
										{
											Hashtable<String, Integer> tmh=new Hashtable<String, Integer>();
											for (int j=0; j<tempht.size(); j++)
											{
												tmh.put(tempht.get(j), new Integer(1));
											}
											matoutput.put(parts_eliminate[i], tmh);
										}
										else
										{
											Hashtable<String, Integer> tmh=matoutput.get(parts_eliminate[i]);
											for (int j=0; j<tempht.size(); j++)
											{
												if (tmh.get(tempht.get(j))!=null)
													index=((Integer)tmh.get(tempht.get(j))).intValue();
												else
													index=0;
												tmh.put(tempht.get(j), new Integer(1+index));
											}
											matoutput.put(parts_eliminate[i], tmh);
										}
									}
								}
							}
						}
						if (tempht.size()>0 && outhashtags)
						{
							Vector<TreeSet<String>> temphtt=new Vector<TreeSet<String>>();
							if (tempht.size()==1)
							{
								TreeSet<String> temphttt=new TreeSet<String>();
								temphttt.add(tempht.get(0));
								temphtt.add(temphttt);
							}
							else
							{
								r = 0;
								index = 0;
								while(r >= 0)
								{
									if(index <= (tempht.size() + (r - 2)))
									{
										combination[r] = index;
										if(r == 1)
										{
											TreeSet<String> temphttt=new TreeSet<String>();
											temphttt.add(tempht.get(combination[0]));
											temphttt.add(tempht.get(combination[1]));
											if (!temphtt.contains(temphttt))
												temphtt.add(temphttt);
											index++;
										}
										else
										{
											index = combination[r]+1;
											r++;
										}
									}
									else
									{
										r--;
										if(r > 0)
											index = combination[r]+1;
										else
											index = combination[0]+1;
									}
								}
							}
							for (int i=0; i<temphtt.size(); i++)
							{
								TreeSet<String> temphttt=temphtt.get(i);
								if (cashtags.get(temphttt)==null)
								{
									cashtags.put(temphttt, new Integer(1));
								}
								else
								{
									index=((Integer)cashtags.get(temphttt)).intValue();
									cashtags.put(temphttt, new Integer(index+1));
								}
							}
						}
					}
				}
			}
			data.close();
			if (outfreqhashtags)
			{
				if (ashtags.size()>0)
				{
					dsu1=new DataSetUtilities();
					Hashtable<String, String> tempmd=new Hashtable<String, String>();
					dsu1.addnewvar("hashtag", "%3853%", Keywords.TEXTSuffix, tempmd, tempmd);
					dsu1.addnewvar("frequency", "%3862%", Keywords.NUMSuffix, tempmd, tempmd);
					String[] outv1=new String[2];
					if (!dwfht.opendatatable(dsu1.getfinalvarinfo()))
					{
						return new Result(dwfht.getmessage(), false, null);
					}
					for (Iterator<String> itr = ashtags.keySet().iterator(); itr.hasNext();)
					{
						outv1[0]=itr.next();
						index=((Integer)ashtags.get(outv1[0])).intValue();
						outv1[1]=String.valueOf(index);
						dwfht.write(outv1);
					}
				}
				else
				{
					outfreqhashtags=false;
					result.add(new LocalMessageGetter("%3863%<br>\n"));
				}
			}
			if (outhashtags)
			{
				if (ashtags.size()>0)
				{
					dsu2=new DataSetUtilities();
					Hashtable<String, String> tempmd=new Hashtable<String, String>();
					dsu2.addnewvar("hashtag", "%3852%", Keywords.TEXTSuffix, tempmd, tempmd);
					int pointer=1;
					Vector<String> refht=new Vector<String>();
					String cht="";
					for (Iterator<String> itr = ashtags.keySet().iterator(); itr.hasNext();)
					{
						cht=itr.next();
						index=((Integer)ashtags.get(cht)).intValue();
						if (listh.size()==0)
						{
							if (index>=mfh)
							{
								refht.add(cht);
								dsu2.addnewvar("hashtag_"+pointer, "%3853%: "+cht, Keywords.NUMSuffix, tempmd, tempmd);
								pointer++;
							}
						}
						else
						{
							if (listh.contains(cht))
							{
								refht.add(cht);
								dsu2.addnewvar("hashtag_"+pointer, "%3853%: "+cht, Keywords.NUMSuffix, tempmd, tempmd);
								pointer++;
							}
						}
					}
					if (refht.size()>0)
					{
						if (!dwht.opendatatable(dsu2.getfinalvarinfo()))
						{
							return new Result(dwht.getmessage(), false, null);
						}
						String[] outvalues=new String[pointer];
						TreeSet<String> temp_usable=new TreeSet<String>();
						for (int i=0; i<refht.size(); i++)
						{
							outvalues[0]=refht.get(i);
							for (int j=0; j<refht.size(); j++)
							{
								if (i==j)
								{
									temp_usable.clear();
									temp_usable.add(outvalues[0]);
									if (cashtags.get(temp_usable)!=null)
									{
										index=((Integer)cashtags.get(temp_usable)).intValue();
										outvalues[j+1]=String.valueOf(index);
									}
									else
										outvalues[j+1]="0";
								}
								else
								{
									temp_usable.clear();
									temp_usable.add(outvalues[0]);
									temp_usable.add(refht.get(j));
									if (cashtags.get(temp_usable)!=null)
									{
										index=((Integer)cashtags.get(temp_usable)).intValue();
										outvalues[j+1]=String.valueOf(index);
									}
									else
										outvalues[j+1]="0";
								}
							}
							dwht.write(outvalues);
						}
						refht.clear();
						refht=null;
					}
					else
					{
						outhashtags=false;
						result.add(new LocalMessageGetter("%3864%<br>\n"));
					}
				}
				else
				{
					outhashtags=false;
					result.add(new LocalMessageGetter("%3864%<br>\n"));
				}
			}
			if (outhashtags_words)
			{
				if (ashtags_words.size()>0)
				{
					dsu3=new DataSetUtilities();
					Hashtable<String, String> tempmd=new Hashtable<String, String>();
					dsu3.addnewvar("hashtag", "%3853%", Keywords.TEXTSuffix, tempmd, tempmd);
					dsu3.addnewvar("terms", "%3866%", Keywords.TEXTSuffix, tempmd, tempmd);
					String[] outv3=new String[2];
					if (!dwwht.opendatatable(dsu3.getfinalvarinfo()))
					{
						return new Result(dwwht.getmessage(), false, null);
					}
					for (Iterator<String> itr = ashtags_words.keySet().iterator(); itr.hasNext();)
					{
						outv3[0]=itr.next();
						Vector<String> thw=ashtags_words.get(outv3[0]);
						outv3[1]="";
						for (int i=0; i<thw.size(); i++)
						{
							outv3[1]=thw.get(i);
							if (!outv3[1].trim().equals(""))
								dwwht.write(outv3);
						}
					}
				}
				else
				{
					outhashtags_words=false;
					result.add(new LocalMessageGetter("%3865%<br>\n"));
				}
			}
			if (outmathashtags_words)
			{
				if (ashtags_words.size()>0)
				{
					dsu7=new DataSetUtilities();
					Hashtable<String, String> tempmd=new Hashtable<String, String>();
					dsu7.addnewvar("word", "%3878%", Keywords.TEXTSuffix, tempmd, tempmd);
					Vector<String> refht=new Vector<String>();
					String cht="";
					int pointer=1;
					for (Iterator<String> itr = ashtags.keySet().iterator(); itr.hasNext();)
					{
						cht=itr.next();
						index=((Integer)ashtags.get(cht)).intValue();
						if (listh.size()==0)
						{
							if (index>=mfh)
							{
								refht.add(cht);
								dsu7.addnewvar("hashtag_"+pointer, "%3853%: "+cht, Keywords.NUMSuffix, tempmd, tempmd);
								pointer++;
							}
						}
						else
						{
							if (listh.contains(cht))
							{
								refht.add(cht);
								dsu7.addnewvar("hashtag_"+pointer, "%3853%: "+cht, Keywords.NUMSuffix, tempmd, tempmd);
								pointer++;
							}
						}
					}
					if (refht.size()>0)
					{
						if (!dwmwht.opendatatable(dsu7.getfinalvarinfo()))
						{
							return new Result(dwmwht.getmessage(), false, null);
						}
						String[] outv7=new String[pointer];
						for (Iterator<String> itr = matoutput.keySet().iterator(); itr.hasNext();)
						{
							outv7[0]=itr.next();
							Hashtable<String, Integer> tmh=matoutput.get(outv7[0]);
							totindex=0;
							for (int i=0; i<refht.size(); i++)
							{
								if (tmh.get(refht.get(i))!=null)
								{
									index=(tmh.get(refht.get(i))).intValue();
									totindex+=index;
									outv7[i+1]=String.valueOf(index);
								}
								else
									outv7[i+1]="0";
							}
							if (totindex>0) dwmwht.write(outv7);
						}
					}
					else
					{
						outmathashtags_words=false;
						result.add(new LocalMessageGetter("%3880%<br>\n"));
					}
				}
				else
				{
					outhashtags_words=false;
					result.add(new LocalMessageGetter("%3880%<br>\n"));
				}
			}
			cashtags.clear();
			ashtags_words.clear();
			cashtags=null;
			ashtags_words=null;
			System.gc();
		}
		Keywords.percentage_done=33;
		if (outfrequsers || outusers || outusers_hashtags)
		{
			if (!data.open(testv, 0, false))
				return new Result(data.getmessage(), false, null);
			String where=(String)parameters.get(Keywords.where.toLowerCase());
			if (where!=null)
			{
				if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
			}
			Hashtable<TreeSet<String>, Integer> cusers=new Hashtable<TreeSet<String>, Integer>();
			while (!data.isLast())
			{
				values = data.getRecord();
				if (values!=null)
				{
					actualtweet=values[0].toLowerCase();
					if (noretwett && actualtweet.startsWith("rt ")) actualtweet="";
					if (!actualtweet.equals(""))
					{
						try
						{
							while (actualtweet.contains("http:"))
							{
								indexOfHttp = actualtweet.indexOf("http:");
								endPoint = (actualtweet.indexOf(" ", indexOfHttp) != -1) ? actualtweet.indexOf(" ", indexOfHttp) : actualtweet.length();
								start_h="";
								end_h="";
								if (indexOfHttp-5>0) start_h=actualtweet.substring(0, indexOfHttp-5);
								if (endPoint<actualtweet.length()) end_h=actualtweet.substring(endPoint);
								actualtweet=start_h+" "+end_h;
							}
							while (actualtweet.contains("https:"))
							{
								indexOfHttp = actualtweet.indexOf("https:");
								endPoint = (actualtweet.indexOf(" ", indexOfHttp) != -1) ? actualtweet.indexOf(" ", indexOfHttp) : actualtweet.length();
								start_h="";
								end_h="";
								if (indexOfHttp-6>0) start_h=actualtweet.substring(0, indexOfHttp-6);
								if (endPoint<actualtweet.length()) end_h=actualtweet.substring(endPoint);
								actualtweet=start_h+" "+end_h;
							}
						}
						catch (Exception ep){}
						actualtweet=actualtweet.replaceAll("\\s+"," ");
						tempht.clear();
						part_eliminate="";
						matcherUS = patternUS.matcher(actualtweet);
						while (matcherUS.find())
						{
							tempvalue = matcherUS.group();
							tempvalue = tempvalue.replace(" ", "");
							part_eliminate=part_eliminate+tempvalue+" ";
						}
						if (!part_eliminate.equals(""))
						{
							part_eliminate=part_eliminate.replaceAll("\\s+"," ");
							parts_eliminate=part_eliminate.split(" ");
							rh.clear();
							for (int j=0; j<parts_eliminate.length; j++)
							{
								if (!rh.contains(parts_eliminate[j])) rh.add(parts_eliminate[j]);
							}
							parts_eliminate=new String[rh.size()];
							for (int j=0; j<rh.size(); j++)
							{
								parts_eliminate[j]=rh.get(j);
							}
							rh.clear();
							for (int j=0; j<parts_eliminate.length; j++)
							{
								if (!tempht.contains(parts_eliminate[j]))
									tempht.add(parts_eliminate[j]);
								if (users.get(parts_eliminate[j])==null)
									users.put(parts_eliminate[j], new Integer(1));
								else
								{
									index=((Integer)users.get(parts_eliminate[j])).intValue();
									users.put(parts_eliminate[j], new Integer(index+1));
								}
								actualtweet=actualtweet.replaceAll(parts_eliminate[j]," ");
							}
							actualtweet=actualtweet.replaceAll("\\s+"," ");
						}
						if (tempht.size()>0 && outusers)
						{
							Vector<TreeSet<String>> temphtt=new Vector<TreeSet<String>>();
							if (tempht.size()==1)
							{
								TreeSet<String> temphttt=new TreeSet<String>();
								temphttt.add(tempht.get(0));
								temphtt.add(temphttt);
							}
							else
							{
								r = 0;
								index = 0;
								while(r >= 0)
								{
									if(index <= (tempht.size() + (r - 2)))
									{
										combination[r] = index;
										if(r == 1)
										{
											TreeSet<String> temphttt=new TreeSet<String>();
											temphttt.add(tempht.get(combination[0]));
											temphttt.add(tempht.get(combination[1]));
											if (!temphtt.contains(temphttt))
												temphtt.add(temphttt);
											index++;
										}
										else
										{
											index = combination[r]+1;
											r++;
										}
									}
									else
									{
										r--;
										if(r > 0)
											index = combination[r]+1;
										else
											index = combination[0]+1;
									}
								}
							}
							for (int i=0; i<temphtt.size(); i++)
							{
								TreeSet<String> temphttt=temphtt.get(i);
								if (cusers.get(temphttt)==null)
								{
									cusers.put(temphttt, new Integer(1));
								}
								else
								{
									index=((Integer)cusers.get(temphttt)).intValue();
									cusers.put(temphttt, new Integer(index+1));
								}
							}
						}
					}
				}
			}
			data.close();
			if (outfrequsers)
			{
				if (users.size()>0)
				{
					dsu4=new DataSetUtilities();
					Hashtable<String, String> tempmd=new Hashtable<String, String>();
					dsu4.addnewvar("user", "%3853%", Keywords.TEXTSuffix, tempmd, tempmd);
					dsu4.addnewvar("frequency", "%3867%", Keywords.NUMSuffix, tempmd, tempmd);
					String[] outv4=new String[2];
					if (!dwfus.opendatatable(dsu4.getfinalvarinfo()))
					{
						return new Result(dwfus.getmessage(), false, null);
					}
					for (Iterator<String> itr = users.keySet().iterator(); itr.hasNext();)
					{
						outv4[0]=itr.next();
						index=((Integer)users.get(outv4[0])).intValue();
						outv4[1]=String.valueOf(index);
						dwfus.write(outv4);
					}
				}
				else
				{
					outfrequsers=false;
					result.add(new LocalMessageGetter("%3868%<br>\n"));
				}
			}
			if (outusers)
			{
				if (users.size()>0)
				{
					dsu5=new DataSetUtilities();
					Hashtable<String, String> tempmd=new Hashtable<String, String>();
					dsu5.addnewvar("user", "%3869%", Keywords.TEXTSuffix, tempmd, tempmd);
					int pointer=1;
					Vector<String> refht=new Vector<String>();
					String cht="";
					for (Iterator<String> itr = users.keySet().iterator(); itr.hasNext();)
					{
						cht=itr.next();
						index=((Integer)users.get(cht)).intValue();
						if (index>=mfu)
						{
							refht.add(cht);
							dsu5.addnewvar("user"+pointer, "%3870%: "+cht, Keywords.NUMSuffix, tempmd, tempmd);
							pointer++;
						}
					}
					if (refht.size()>0)
					{
						if (!dwus.opendatatable(dsu5.getfinalvarinfo()))
						{
							return new Result(dwus.getmessage(), false, null);
						}
						String[] outvalues=new String[pointer];
						TreeSet<String> temp_usable=new TreeSet<String>();
						for (int i=0; i<refht.size(); i++)
						{
							outvalues[0]=refht.get(i);
							for (int j=0; j<refht.size(); j++)
							{
								if (i==j)
								{
									temp_usable.clear();
									temp_usable.add(outvalues[0]);
									if (cusers.get(temp_usable)!=null)
									{
										index=((Integer)cusers.get(temp_usable)).intValue();
										outvalues[j+1]=String.valueOf(index);
									}
									else
										outvalues[j+1]="0";
								}
								else
								{
									temp_usable.clear();
									temp_usable.add(outvalues[0]);
									temp_usable.add(refht.get(j));
									if (cusers.get(temp_usable)!=null)
									{
										index=((Integer)cusers.get(temp_usable)).intValue();
										outvalues[j+1]=String.valueOf(index);
									}
									else
										outvalues[j+1]="0";
								}
							}
							dwus.write(outvalues);
						}
						refht.clear();
						refht=null;
					}
					else
					{
						outusers=false;
						result.add(new LocalMessageGetter("%3871%<br>\n"));
					}

				}
				else
				{
					outusers=false;
					result.add(new LocalMessageGetter("%3871%<br>\n"));
				}
			}
			cusers.clear();
			cusers=null;
			System.gc();
		}
		Keywords.percentage_done=66;
		if (outusers_hashtags)
		{
			String cht="";
			Vector<String> real_hashtags=new Vector<String>();
			Vector<String> real_users=new Vector<String>();
			for (Iterator<String> itr = ashtags.keySet().iterator(); itr.hasNext();)
			{
				cht=itr.next();
				index=((Integer)ashtags.get(cht)).intValue();
				if (listh.size()==0)
				{
					if (index>=mfh) real_hashtags.add(cht);
				}
				else if (listh.contains(cht))
					real_hashtags.add(cht);
			}
			for (Iterator<String> itr = users.keySet().iterator(); itr.hasNext();)
			{
				cht=itr.next();
				index=((Integer)users.get(cht)).intValue();
				if (index>=mfu) real_users.add(cht);
			}
			if (real_hashtags.size()==0 || real_users.size()==0)
			{
				outusers_hashtags=false;
				result.add(new LocalMessageGetter("%3872%<br>\n"));
			}
			else
			{
				if (!data.open(testv, 0, false))
					return new Result(data.getmessage(), false, null);
				String where=(String)parameters.get(Keywords.where.toLowerCase());
				if (where!=null)
				{
					if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
				}
				Hashtable<TreeSet<String>, Integer> chtus=new Hashtable<TreeSet<String>, Integer>();
				String part_user="";
				String part_hashtag="";
				String[] parts_user;
				String[] parts_hashtag;
				while (!data.isLast())
				{
					values = data.getRecord();
					if (values!=null)
					{
						actualtweet=values[0].toLowerCase();
						if (noretwett && actualtweet.startsWith("rt ")) actualtweet="";
						if (!actualtweet.equals(""))
						{
							try
							{
								while (actualtweet.contains("http:"))
								{
									indexOfHttp = actualtweet.indexOf("http:");
									endPoint = (actualtweet.indexOf(" ", indexOfHttp) != -1) ? actualtweet.indexOf(" ", indexOfHttp) : actualtweet.length();
									start_h="";
									end_h="";
									if (indexOfHttp-5>0) start_h=actualtweet.substring(0, indexOfHttp-5);
									if (endPoint<actualtweet.length()) end_h=actualtweet.substring(endPoint);
									actualtweet=start_h+" "+end_h;
								}
								while (actualtweet.contains("https:"))
								{
									indexOfHttp = actualtweet.indexOf("https:");
									endPoint = (actualtweet.indexOf(" ", indexOfHttp) != -1) ? actualtweet.indexOf(" ", indexOfHttp) : actualtweet.length();
									start_h="";
									end_h="";
									if (indexOfHttp-6>0) start_h=actualtweet.substring(0, indexOfHttp-6);
									if (endPoint<actualtweet.length()) end_h=actualtweet.substring(endPoint);
									actualtweet=start_h+" "+end_h;
								}
							}
							catch (Exception ep){}
							actualtweet=actualtweet.replaceAll("\\s+"," ");
							tempht.clear();
							part_user="";
							part_hashtag="";
							matcherUS = patternUS.matcher(actualtweet);
							while (matcherUS.find())
							{
								tempvalue = matcherUS.group();
								tempvalue = tempvalue.replace(" ", "");
								part_user=part_user+tempvalue+" ";
							}
							matcherHT = patternHT.matcher(actualtweet);
							while (matcherHT.find())
							{
								tempvalue = matcherHT.group();
								tempvalue = tempvalue.replace(" ", "");
								part_hashtag=part_hashtag+tempvalue+" ";
							}
							if (!part_user.equals("") && !part_hashtag.equals(""))
							{
								part_user=part_user.replaceAll("\\s+"," ");
								part_hashtag=part_hashtag.replaceAll("\\s+"," ");
								parts_user=part_user.split(" ");
								parts_hashtag=part_hashtag.split(" ");
								rh.clear();
								for (int j=0; j<parts_user.length; j++)
								{
									if (!rh.contains(parts_user[j])) rh.add(parts_user[j]);
								}
								parts_user=new String[rh.size()];
								for (int j=0; j<rh.size(); j++)
								{
									parts_user[j]=rh.get(j);
								}
								rh.clear();
								for (int j=0; j<parts_hashtag.length; j++)
								{
									if (!rh.contains(parts_hashtag[j])) rh.add(parts_hashtag[j]);
								}
								parts_hashtag=new String[rh.size()];
								for (int j=0; j<rh.size(); j++)
								{
									parts_hashtag[j]=rh.get(j);
								}
								rh.clear();
								for (int i=0; i<parts_user.length; i++)
								{
									for (int j=0; j<parts_hashtag.length; j++)
									{
										if (real_users.contains(parts_user[i]) && real_hashtags.contains(parts_hashtag[j]))
										{
											TreeSet<String> temphttt=new TreeSet<String>();
											temphttt.add(parts_user[i]);
											temphttt.add(parts_hashtag[j]);
											if (chtus.get(temphttt)==null)
											{
												chtus.put(temphttt, new Integer(1));
											}
											else
											{
												index=((Integer)chtus.get(temphttt)).intValue();
												chtus.put(temphttt, new Integer(index+1));
											}
										}
									}
								}
							}
						}
					}
				}
				data.close();
				if (real_hashtags.size()>real_users.size())
				{
					dsu6=new DataSetUtilities();
					Hashtable<String, String> tempmd=new Hashtable<String, String>();
					dsu6.addnewvar("hashtag", "%3852%", Keywords.TEXTSuffix, tempmd, tempmd);
					int pointer=1;
					cht="";
					for (int i=0; i<real_users.size(); i++)
					{
						cht=real_users.get(i);
						dsu6.addnewvar("user"+pointer, "%3870%: "+cht, Keywords.NUMSuffix, tempmd, tempmd);
						pointer++;
					}
					if (!dwhtus.opendatatable(dsu6.getfinalvarinfo()))
					{
						return new Result(dwhtus.getmessage(), false, null);
					}
					String[] outvalues=new String[pointer];
					TreeSet<String> temp_usable=new TreeSet<String>();
					for (int i=0; i<real_hashtags.size(); i++)
					{
						outvalues[0]=real_hashtags.get(i);
						for (int j=0; j<real_users.size(); j++)
						{
							temp_usable.clear();
							temp_usable.add(real_users.get(j));
							temp_usable.add(outvalues[0]);
							if (chtus.get(temp_usable)!=null)
							{
								index=((Integer)chtus.get(temp_usable)).intValue();
								outvalues[j+1]=String.valueOf(index);
							}
							else
								outvalues[j+1]="0";
						}
						dwhtus.write(outvalues);
					}
				}
				else
				{
					dsu6=new DataSetUtilities();
					Hashtable<String, String> tempmd=new Hashtable<String, String>();
					dsu6.addnewvar("user", "%3869%", Keywords.TEXTSuffix, tempmd, tempmd);
					int pointer=1;
					cht="";
					for (int i=0; i<real_hashtags.size(); i++)
					{
						cht=real_hashtags.get(i);
						dsu6.addnewvar("hashtag_"+pointer, "%3853%: "+cht, Keywords.NUMSuffix, tempmd, tempmd);
						pointer++;
					}
					if (!dwhtus.opendatatable(dsu6.getfinalvarinfo()))
					{
						return new Result(dwhtus.getmessage(), false, null);
					}
					String[] outvalues=new String[pointer];
					TreeSet<String> temp_usable=new TreeSet<String>();
					for (int i=0; i<real_users.size(); i++)
					{
						outvalues[0]=real_users.get(i);
						for (int j=0; j<real_hashtags.size(); j++)
						{
							temp_usable.clear();
							temp_usable.add(outvalues[0]);
							temp_usable.add(real_hashtags.get(j));
							if (chtus.get(temp_usable)!=null)
							{
								index=((Integer)chtus.get(temp_usable)).intValue();
								outvalues[j+1]=String.valueOf(index);
							}
							else
								outvalues[j+1]="0";
						}
						dwhtus.write(outvalues);
					}
				}
				chtus.clear();
				chtus=null;
			}
		}
		if (outhashtags)
		{
			boolean resclose=dwht.close();
			if (!resclose)
				return new Result(dwht.getmessage(), false, null);
			Vector<Hashtable<String, String>> t1=dwht.getVarInfo();
			Hashtable<String, String> d1=dwht.getTableInfo();
			result.add(new LocalDictionaryWriter(dwht.getdictpath(), dw_keyword, dw_description, author, dwht.gettabletype(),
			d1, dsu2.getfinalvarinfo(), t1, dsu2.getfinalcl(), dsu2.getfinalmd(), null));
		}
		if (outusers)
		{
			boolean resclose=dwus.close();
			if (!resclose)
				return new Result(dwus.getmessage(), false, null);
			Vector<Hashtable<String, String>> t2=dwus.getVarInfo();
			Hashtable<String, String> d2=dwus.getTableInfo();
			result.add(new LocalDictionaryWriter(dwus.getdictpath(), dw_keyword, dw_description, author, dwus.gettabletype(),
			d2, dsu5.getfinalvarinfo(), t2, dsu5.getfinalcl(), dsu5.getfinalmd(), null));
		}
		if (outusers_hashtags)
		{
			boolean resclose=dwhtus.close();
			if (!resclose)
				return new Result(dwhtus.getmessage(), false, null);
			Vector<Hashtable<String, String>> t3=dwhtus.getVarInfo();
			Hashtable<String, String> d3=dwhtus.getTableInfo();
			result.add(new LocalDictionaryWriter(dwhtus.getdictpath(), dw_keyword, dw_description, author, dwhtus.gettabletype(),
			d3, dsu6.getfinalvarinfo(), t3, dsu6.getfinalcl(), dsu6.getfinalmd(), null));
		}
		if (outfreqhashtags)
		{
			boolean resclose=dwfht.close();
			if (!resclose)
				return new Result(dwfht.getmessage(), false, null);
			Vector<Hashtable<String, String>> t4=dwfht.getVarInfo();
			Hashtable<String, String> d4=dwfht.getTableInfo();
			result.add(new LocalDictionaryWriter(dwfht.getdictpath(), dw_keyword, dw_description, author, dwfht.gettabletype(),
			d4, dsu1.getfinalvarinfo(), t4, dsu1.getfinalcl(), dsu1.getfinalmd(), null));
		}
		if (outfrequsers)
		{
			boolean resclose=dwfus.close();
			if (!resclose)
				return new Result(dwfus.getmessage(), false, null);
			Vector<Hashtable<String, String>> t5=dwfus.getVarInfo();
			Hashtable<String, String> d5=dwfus.getTableInfo();
			result.add(new LocalDictionaryWriter(dwfus.getdictpath(), dw_keyword, dw_description, author, dwfus.gettabletype(),
			d5, dsu4.getfinalvarinfo(), t5, dsu4.getfinalcl(), dsu4.getfinalmd(), null));
		}
		if (outhashtags_words)
		{
			boolean resclose=dwwht.close();
			if (!resclose)
				return new Result(dwwht.getmessage(), false, null);
			Vector<Hashtable<String, String>> t6=dwwht.getVarInfo();
			Hashtable<String, String> d6=dwwht.getTableInfo();
			result.add(new LocalDictionaryWriter(dwwht.getdictpath(), dw_keyword, dw_description, author, dwwht.gettabletype(),
			d6, dsu3.getfinalvarinfo(), t6, dsu3.getfinalcl(), dsu3.getfinalmd(), null));
		}
		if (outmathashtags_words)
		{
			boolean resclose=dwmwht.close();
			if (!resclose)
				return new Result(dwmwht.getmessage(), false, null);
			Vector<Hashtable<String, String>> t7=dwmwht.getVarInfo();
			Hashtable<String, String> d7=dwmwht.getTableInfo();
			result.add(new LocalDictionaryWriter(dwmwht.getdictpath(), dw_keyword, dw_description, author, dwmwht.gettabletype(),
			d7, dsu7.getfinalvarinfo(), t7, dsu7.getfinalcl(), dsu7.getfinalmd(), null));
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
		String[] deph ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 3847, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"hashtags=", "dict", false, 3875, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"freqhashtags=", "setting=out", false, 3856, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"hashtags=", "setting=out", false, 3848, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"frequsers=", "setting=out", false, 3857, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"users=", "setting=out", false, 3849, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"usershashtags=", "setting=out", false, 3850, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"hashtagswords=", "setting=out", false, 3858, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"mathashtagswords=", "setting=out", false, 3874, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		deph = new String[1];
		deph[0]=Keywords.dict+"hashtags";
		parameters.add(new GetRequiredParameters(Keywords.vartweets, "var=all", true, 3851, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varhashtags, "var=all", false, 3876, deph, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.minfreqhashtags, "text", false, 3854, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 3879, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.minfrequsers, "text", false, 3860, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.noretwett,"checkbox", false, 3877,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 3344, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 3878, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="4180";
		retprocinfo[1]="3846";
		return retprocinfo;
	}
	private String onlyAscii(String intext)
	{
		StringBuilder start=new StringBuilder();
		start.append(intext);
		StringBuilder end=new StringBuilder();
		int sz = start.length();
		for (int i = 0; i < sz; i++)
		{
			if (start.charAt(i) >= 48 && start.charAt(i) <= 57) end.append(start.charAt(i));
			else if (start.charAt(i) >= 65 && start.charAt(i) <= 90) end.append(start.charAt(i));
			else if (start.charAt(i) >= 97 && start.charAt(i) <= 122) end.append(start.charAt(i));
			else if (start.charAt(i) >= 192 && start.charAt(i) <= 197) end.append(start.charAt(i));
			else if (start.charAt(i) >= 200 && start.charAt(i) <= 207) end.append(start.charAt(i));
			else if (start.charAt(i) >= 210 && start.charAt(i) <= 214) end.append(start.charAt(i));
			else if (start.charAt(i) >= 216 && start.charAt(i) <= 221) end.append(start.charAt(i));
			else if (start.charAt(i) >= 224 && start.charAt(i) <= 246) end.append(start.charAt(i));
			else if (start.charAt(i) >= 249 && start.charAt(i) <= 255) end.append(start.charAt(i));
			else end.append(" ");
		}
		intext=end.toString();
		start=null;
		intext=intext.replaceAll("\\s+"," ");
		intext=intext.trim();
		return intext;
	}
}
