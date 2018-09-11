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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.File;

import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.dataaccess.FastTempDataSet;

/**
* This is the procedure that tags the different words resulted by the web scraper by using TreeTagger
* @author marco.scarno@gmail.com
* @date 24/02/2017
*/
public class ProcWebscrapertagger implements RunStep
{
	/**
	*Evaluate the frequencies of each word
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String[] requiredparameters = new String[] {Keywords.dict+"content", Keywords.OUT.toLowerCase(), Keywords.trettaggerexe, Keywords.parameterfileandtagsets};
		String[] optionalparameters = new String[] {Keywords.dict+"tags", Keywords.dict+"sw", Keywords.varsw, Keywords.dict+"gow", Keywords.vargow, Keywords.minfreq, Keywords.minlength, Keywords.maxlength, Keywords.onlyascii,
		Keywords.avoidunknown, Keywords.file_encoding,
		Keywords.limit_reference, Keywords.limit_temp_files, Keywords.add_two_terms, Keywords.add_three_terms,
		Keywords.consider_numbers};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		boolean onlyascii= (parameters.get(Keywords.onlyascii) != null);
		boolean avoidunknown= (parameters.get(Keywords.avoidunknown) != null);
		DictionaryReader dictcontent = (DictionaryReader)parameters.get(Keywords.dict+"content");
		DictionaryReader dicttags = null;
		boolean use_tags=false;
		if (parameters.get(Keywords.dict+"tags") != null) use_tags=true;
		if (use_tags) dicttags=(DictionaryReader)parameters.get(Keywords.dict+"tags");

		boolean consider_numbers=(parameters.get(Keywords.consider_numbers)!=null);
		boolean add_two_terms=(parameters.get(Keywords.add_two_terms)!=null);
		boolean add_three_terms=(parameters.get(Keywords.add_three_terms)!=null);

		boolean spstopwords=false;
		HashSet<String> stopwords=new HashSet<String>();

		spstopwords =(parameters.get(Keywords.dict+"sw")!=null);
		if (spstopwords)
		{
			DictionaryReader dictsw = (DictionaryReader)parameters.get(Keywords.dict+"sw");
			String vartemp=(String)parameters.get(Keywords.varsw.toLowerCase());
			if (vartemp==null)
			{
				return new Result("%3401%<br>\n", false, null);
			}
			String[] vsw=vartemp.split(" ");
			if (vsw.length!=1)
			{
				return new Result("%3402%<br>\n", false, null);
			}
			DataReader datasw = new DataReader(dictsw);
			if (!datasw.open(vsw, 0, true))
			{
				return new Result(datasw.getmessage(), false, null);
			}
			String[] valuessw=null;
			while (!datasw.isLast())
			{
				valuessw = datasw.getRecord();
				stopwords.add(valuessw[0].toLowerCase());
			}
			datasw.close();
		}

		boolean spgowords=false;
		HashSet<String> gowords=new HashSet<String>();

		spgowords =(parameters.get(Keywords.dict+"gow")!=null);
		if (spgowords)
		{
			DictionaryReader dictgow = (DictionaryReader)parameters.get(Keywords.dict+"gow");
			String vartemp=(String)parameters.get(Keywords.vargow.toLowerCase());
			if (vartemp==null)
			{
				return new Result("%3700%<br>\n", false, null);
			}
			String[] vsw=vartemp.split(" ");
			if (vsw.length!=1)
			{
				return new Result("%3700%<br>\n", false, null);
			}
			DataReader datagow = new DataReader(dictgow);
			if (!datagow.open(vsw, 0, true))
			{
				return new Result(datagow.getmessage(), false, null);
			}
			String[] valuesgow=null;
			while (!datagow.isLast())
			{
				valuesgow = datagow.getRecord();
				gowords.add(valuesgow[0].toLowerCase());
			}
			datagow.close();
		}

		String limit_reference = (String) parameters.get(Keywords.limit_reference);

		String minval = (String) parameters.get(Keywords.minlength);
		String maxval = (String) parameters.get(Keywords.maxlength);
		String minfreq = (String) parameters.get(Keywords.minfreq);
		int mnf=-1;
		if (minfreq!=null)
		{
			mnf=0;
			try
			{
				mnf=Integer.parseInt(minfreq);
			}
			catch (Exception e)
			{
				mnf=0;
			}
			if (mnf<1)
			{
				return new Result("%3497% ("+minfreq+")<br>\n", false, null);
			}
		}

		String minfreqf = (String) parameters.get(Keywords.limit_temp_files);
		int minfile=0;
		if (minfreqf!=null)
		{
			minfile=-1;
			try
			{
				minfile=Integer.parseInt(minfreqf);
			}
			catch (Exception e)
			{
				return new Result("%4073% ("+minfreqf+")<br>\n", false, null);
			}
			if (minfile<0) return new Result("%4073% ("+minfreqf+")<br>\n", false, null);
		}

		int minvalue = 0;
		int maxvalue = 100;
		try
		{
			if (minval != null)
			{
				minvalue = Integer.parseInt(minval);
			}
			if (maxval != null)
			{
				maxvalue = Integer.parseInt(maxval);
			}
		}
		catch (Exception e)
		{
			return new Result("%3325%<br>\n", false, null);
		}
		if (minvalue<0)
		{
			return new Result("%3325%<br>\n", false, null);
		}
		if (maxvalue<0)
		{
			return new Result("%3325%<br>\n", false, null);
		}
		if (minvalue>maxvalue)
		{
			return new Result("%3325%<br>\n", false, null);
		}

		DataWriter dw = new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);
		String trettaggerexe = (String) parameters.get(Keywords.trettaggerexe);
		String parameterfileandtagsets = (String) parameters.get(Keywords.parameterfileandtagsets);

		String file_encoding=(String) parameters.get(Keywords.file_encoding);

		String tempdir=(String)parameters.get(Keywords.WorkDir);

		String outfile=tempdir+"outfile.txt";
		String infile=tempdir+"infile";
		String resfile=tempdir+"resfile.txt";

		boolean existfile=new File(trettaggerexe).exists();
		if (!existfile) return new Result("%3748% ("+trettaggerexe+")<br>\n", false, null);

		existfile=new File(outfile).exists();
		boolean deletedfiles=true;
		if (existfile) deletedfiles=new File(outfile).delete();
		if (!deletedfiles)
			return new Result("%3350% ("+outfile+")<br>\n", false, null);

		existfile=new File(resfile).exists();
		if (existfile) deletedfiles=new File(resfile).delete();
		if (!deletedfiles)
			return new Result("%3350% ("+resfile+")<br>\n", false, null);

		String[] tempparameterfiles=parameterfileandtagsets.split(";");
		String[] parameterfiles=new String[tempparameterfiles.length];
		Vector<Vector<String>> tagsets=new Vector<Vector<String>>();
		String[] tempsplit=new String[0];
		for (int i=0; i<tempparameterfiles.length; i++)
		{
			parameterfiles[i]=tempparameterfiles[i];
			Vector<String> temptagsets=new Vector<String>();
			if (parameterfiles[i].indexOf("=")>0)
			{
				try
				{
					tempsplit=parameterfiles[i].split("=");
					parameterfiles[i]=tempsplit[0];
					String temptt=tempsplit[1].replaceAll("\\s+"," ");
					String[] tempt=temptt.split(" ");
					for (int j=0; j<tempt.length; j++)
					{
						temptagsets.add(tempt[j].toLowerCase());
					}
				}
				catch (Exception e)
				{
					return new Result("%3743% ("+tempparameterfiles[i]+")<br>\n", false, null);
				}
			}
			try
			{
				parameterfiles[i]=parameterfiles[i].replaceAll("\\\\","/");
			}
			catch (Exception ee){}
			existfile=new File(parameterfiles[i]).exists();
			if (!existfile) return new Result("%3744% ("+parameterfiles[i]+")<br>\n", false, null);
			tagsets.add(temptagsets);
		}

		int numpfiles=tempparameterfiles.length;

		for (int i=0; i<numpfiles; i++)
		{
			existfile=new File(infile+"_"+String.valueOf(i)+".txt").exists();
			if (existfile) deletedfiles=new File(infile+"_"+String.valueOf(i)+".txt").delete();
			if (!deletedfiles)
				return new Result("%3350% ("+infile+"_"+String.valueOf(i)+".txt"+")<br>\n", false, null);
		}

		boolean multiple_content=false;

		int varok=0;
		for (int i=0; i<dictcontent.gettotalvar(); i++)
		{
			String tv=dictcontent.getvarname(i);
			if (tv.equalsIgnoreCase("content")) varok++;
			if (tv.equalsIgnoreCase("site_descriptor")) varok++;
			if (tv.equalsIgnoreCase("page_reference"))
			{
				multiple_content=true;
				varok++;
			}
		}
		boolean ds_ok=false;
		if (varok==2 && !multiple_content) ds_ok=true;
		if (varok==3 && multiple_content) ds_ok=true;
		if (!ds_ok) return new Result("%3745%<br>\n", false, null);

		if (!multiple_content && limit_reference!=null)
			return new Result("%4071%<br>\n", false, null);

		HashSet<String> tlr=new HashSet<String>();
		if (limit_reference!=null)
		{
			String[] temp=limit_reference.split(";");
			for (int i=0; i<temp.length; i++)
			{
				tlr.add(temp[i].toLowerCase());
			}
		}

		varok=0;
		if (use_tags)
		{
			for (int i=0; i<dicttags.gettotalvar(); i++)
			{
				String tv=dicttags.getvarname(i);
				if (tv.equalsIgnoreCase("value")) varok++;
				if (tv.equalsIgnoreCase("site_descriptor")) varok++;
			}
			if (varok!=2) return new Result("%3746%<br>\n", false, null);
		}

		String[] varfc=new String[2];
		String[] varft=new String[2];
		if (multiple_content)
			varfc=new String[3];

		varfc[0]="site_descriptor";
		varfc[1]="content";
		if (multiple_content)
			varfc[2]="page_reference";

		varft[0]="site_descriptor";
		varft[1]="value";

		DataSetUtilities dsu=new DataSetUtilities();
		Hashtable<String, String> tempmd=new Hashtable<String, String>();
		dsu.addnewvar("site_descriptor", "%3536%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("tagged_content", "%3747%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("freq_terms", "%3777%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("num_terms", "%3769%", Keywords.NUMSuffix+Keywords.INTSuffix, tempmd, tempmd);
		dsu.addnewvar("content_size", "%3770%", Keywords.NUMSuffix+Keywords.INTSuffix, tempmd, tempmd);
		if (!dw.opendatatable(dsu.getfinalvarinfo()))
		{
			return new Result(dw.getmessage(), false, null);
		}

		BufferedWriter bw=null;

		String[] values;
		String[] secvalues;
		String[] outvalues=new String[5];

		Hashtable<String, FastTempDataSet> vftd=new Hashtable<String, FastTempDataSet>();

		String[] maincommandstring=new String[7];
		maincommandstring[0]=trettaggerexe;
		maincommandstring[1]="-token";
		maincommandstring[2]="-quiet";
		maincommandstring[3]="-lemma";
		String inps="";
		String errs="";
		String line="";
		BufferedReader[] bufread=new BufferedReader[numpfiles];
		int usable=0;
		boolean writeread;
		boolean exist_infile=false;
		try
		{
			String[] msc=new String[2];
			msc[0]=trettaggerexe;
			msc[1]="> "+resfile;
			Process p = Runtime.getRuntime().exec(msc);
			BufferedReader brie = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader bree = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			line="";
			inps="";
			errs="";
			while ((line = brie.readLine()) != null)
			{
				inps=inps+line+" ";
			}
			while ((line = bree.readLine()) != null)
			{
				errs=errs+line+" ";
			}
			p.waitFor();
			brie.close();
			bree.close();
		}
		catch (Exception ex)
		{
			return new Result("%3353% "+ex.toString()+"<br>\n", false, null);
		}

		Keywords.percentage_total=102;
		Keywords.percentage_done=1;
		String[] actual_string=new String[1];
		Vector<String> exerrors=new Vector<String>();

		Vector<String> tt=new Vector<String>();

		int param_done=0;

		HashMap<String, Integer> final_freq=new HashMap<String, Integer>();
		int tfreq=0;
		String real_term="";
		String[] types_found;
		String[] lemmas_found;
		String[] term_found;
		int num_terms=0;
		int con_size=0;
		int record_wr=0;
		int sites_treated=0;
		HashSet<String> treated_sites=new HashSet<String>();
		HashSet<String> to_treat_sites=new HashSet<String>();
		boolean consider_site=true;
		int les1sec=0;
		int sites_lemmas=0;
		int tempnrec=0;
		String ref_site;
		double testnumber=0;
		Vector<String> two_terms=new Vector<String>();
		Vector<String> three_terms=new Vector<String>();
		String temp_terms="";
		int total_resulting_sites=0;
		int already_treated_sites=0;
		HashSet<String> tsites=new HashSet<String>();
		while (minfile>=0)
		{
			to_treat_sites.clear();
			vftd.clear();
			final_freq.clear();
			ref_site="";
			if (use_tags)
			{
				DataReader datatags=new DataReader(dicttags);
				if (!datatags.open(varft, 0, false))
					return new Result(datatags.getmessage(), false, null);
				while (!datatags.isLast())
				{
					secvalues = datatags.getRecord();
					if (secvalues!=null)
					{
						if (minfile==0) tsites.add(secvalues[0]);
						consider_site=true;
						if (treated_sites.contains(secvalues[0])) consider_site=false;
						if (to_treat_sites.contains(secvalues[0])) consider_site=true;
						if (consider_site)
						{
							consider_site=true;
							if (minfile>0 && to_treat_sites.size()>minfile) consider_site=false;
							if (consider_site)
							{
								treated_sites.add(secvalues[0]);
								to_treat_sites.add(secvalues[0]);
								secvalues[1]=secvalues[1].replaceAll("\\s+"," ");
								if (!secvalues[1].equals(""))
								{
									types_found=secvalues[1].split(" ");
									for (int i=0; i<types_found.length; i++)
									{
										actual_string[0]=types_found[i];
										if (onlyascii) actual_string[0]=getAscii(actual_string[0]);
										if (minvalue!=0 && actual_string[0].length()<minvalue) actual_string[0]="";
										if (maxvalue!=100 && actual_string[0].length()>maxvalue) actual_string[0]="";
										if (stopwords.size()>0 && stopwords.contains(actual_string[0].toLowerCase())) actual_string[0]="";
										if (!consider_numbers)
										{
											try
											{
												testnumber = Double.parseDouble(actual_string[0]);
												actual_string[0]="";
											}
											catch (Exception en){}
										}
										if (!actual_string[0].equals(""))
										{
											if (vftd.get(secvalues[0])==null)
											{
												FastTempDataSet tempftd=new FastTempDataSet(tempdir);
												tempftd.write(actual_string);
												vftd.put(secvalues[0], tempftd);
											}
											else
											{
												FastTempDataSet tempftd=vftd.get(secvalues[0]);
												tempftd.write(actual_string);
											}
										}
									}
								}
							}
						}
					}
				}
				datatags.close();
			}
			DataReader datacontent = new DataReader(dictcontent);
			if (!datacontent.open(varfc, 0, false))
				return new Result(datacontent.getmessage(), false, null);
			while (!datacontent.isLast())
			{
				values = datacontent.getRecord();
				if (values!=null)
				{
					if (minfile==0) tsites.add(values[0]);
					consider_site=true;
					if (treated_sites.contains(values[0])) consider_site=false;
					if (to_treat_sites.contains(values[0])) consider_site=true;
					if (consider_site)
					{
						consider_site=true;
						if (minfile>0 && to_treat_sites.size()>minfile) consider_site=false;
						if (to_treat_sites.contains(values[0])) consider_site=true;
						if (consider_site)
						{
							treated_sites.add(values[0]);
							to_treat_sites.add(values[0]);
							if (tlr.size()>0)
							{
								if (!tlr.contains(values[2].toLowerCase()))
									values[1]="";
							}
							values[1]=values[1].replaceAll("\\s+"," ");
							if (!values[1].equals(""))
							{
								types_found=values[1].split(" ");
								for (int i=0; i<types_found.length; i++)
								{
									actual_string[0]=types_found[i];
									if (onlyascii) actual_string[0]=getAscii(actual_string[0]);
									if (minvalue!=0 && actual_string[0].length()<minvalue) actual_string[0]="";
									if (maxvalue!=100 && actual_string[0].length()>maxvalue) actual_string[0]="";
									if (stopwords.size()>0 && stopwords.contains(actual_string[0].toLowerCase())) actual_string[0]="";
									if (!consider_numbers)
									{
										try
										{
											testnumber = Double.parseDouble(actual_string[0]);
											actual_string[0]="";
										}
										catch (Exception en){}
									}
									if (!actual_string[0].equals(""))
									{
										if (vftd.get(values[0])==null)
										{
											FastTempDataSet tempftd=new FastTempDataSet(tempdir);
											tempftd.write(actual_string);
											vftd.put(values[0], tempftd);
										}
										else
										{
											FastTempDataSet tempftd=vftd.get(values[0]);
											tempftd.write(actual_string);
										}
									}
								}
							}
						}
					}
				}
			}
			datacontent.close();
			if (minfile==0)
			{
				total_resulting_sites=tsites.size();
				tsites.clear();
			}
			int totalsites=vftd.size();
			if (totalsites>0)
			{
				sites_lemmas=sites_lemmas+totalsites;
				try
				{
					for (Enumeration<String> en=vftd.keys(); en.hasMoreElements();)
					{
						already_treated_sites++;
						Keywords.percentage_done=(int)(100*already_treated_sites/total_resulting_sites);
						if (file_encoding!=null)
							bw=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), file_encoding));
						else
							bw=new BufferedWriter(new FileWriter(outfile));
						ref_site=en.nextElement();
						FastTempDataSet tempftd = vftd.get(ref_site);
						tempftd.endwrite();
						tempftd.opentoread();
						tempnrec=tempftd.getrecords();
						for (int i=0; i<tempnrec; i++)
						{
							actual_string=tempftd.read();
							bw.write(actual_string[0].toLowerCase()+"\n");
						}
						tempftd.endread();
						tempftd.deletefile();
						bw.close();
						param_done=0;
						for (int i=0; i<parameterfiles.length; i++)
						{
							maincommandstring[4]=parameterfiles[i];
							maincommandstring[5]=outfile;
							maincommandstring[6]=infile+"_"+String.valueOf(i)+".txt";
							Process p = Runtime.getRuntime().exec(maincommandstring);
							BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
							BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
							line="";
							inps="";
							errs="";
							while ((line = bri.readLine()) != null)
							{
								inps=inps+line+" ";
							}
							bri.close();
							while ((line = bre.readLine()) != null)
							{
								errs=errs+line+" ";
							}
							bre.close();
							p.waitFor();
							errs=errs.trim()+inps.trim();
							if (!errs.equals(""))
								exerrors.add("%3758%: "+ref_site+", "+parameterfiles[i]+" ("+errs+")\n");
							else
							{
								try
								{
									Thread.sleep(100);
								}
								catch (Exception e){}
								param_done++;
								les1sec=0;
								exist_infile=(new File(infile)).exists();
								while (!exist_infile && les1sec<40)
								{
									try
									{
										Thread.sleep(100);
									}
									catch (Exception e){}
									exist_infile=(new File(infile+"_"+String.valueOf(i)+".txt")).exists();
									les1sec++;
								}
								BufferedReader check_open=null;
								les1sec=0;
								while (check_open==null && les1sec<40)
								{
									try
									{
										Thread.sleep(100);
									}
									catch (Exception e){}
									try
									{
										check_open = new BufferedReader(new FileReader(infile+"_"+String.valueOf(i)+".txt"));
										check_open.close();
										les1sec=100;
									}
									catch (Exception e)
									{
										check_open=null;
									}
									les1sec++;
								}
							}
						}
						existfile=new File(outfile).exists();
						if (existfile) deletedfiles=new File(outfile).delete();
						final_freq.clear();
						if (param_done==parameterfiles.length)
						{
							for (int i=0; i<param_done; i++)
							{
								bufread[i]=new BufferedReader(new FileReader(infile+"_"+String.valueOf(i)+".txt"));
							}
							secvalues=new String[param_done];
							types_found=new String[param_done];
							lemmas_found=new String[param_done];
							term_found=new String[param_done];
							while((line = bufread[0].readLine()) != null)
							{
								secvalues[0]=line;
								values=secvalues[0].split("\t");
								for (int j=0; j<values.length; j++)
								{
									if (j==1 && values[j].indexOf(":")>0) values[j]=values[j].substring(0,values[j].indexOf(":"));
									values[j]=values[j].toLowerCase();
								}
								term_found[0]=values[0];
								types_found[0]=values[1];
								lemmas_found[0]=values[2];
								if (lemmas_found[0].indexOf("|")>=0) lemmas_found[0]=lemmas_found[0].substring(0,lemmas_found[0].indexOf("|"));
								if (gowords.size()>0 && gowords.contains(term_found[0].toLowerCase()))
								{
									if (add_two_terms)
									{
										if (two_terms.size()==2)
										{
											temp_terms=two_terms.get(0)+"_"+two_terms.get(1);
											if (final_freq.get(temp_terms)!=null)
											{
												tfreq=(final_freq.get(temp_terms)).intValue();
												final_freq.put(temp_terms, new Integer(tfreq+1));
											}
											else final_freq.put(temp_terms, new Integer(1));
											two_terms.remove(0);
										}
										if (!term_found[0].equals("")) two_terms.add(term_found[0]);
									}
									if (add_three_terms)
									{
										if (three_terms.size()==3)
										{
											temp_terms=three_terms.get(0)+"_"+three_terms.get(1)+"_"+three_terms.get(2);
											if (final_freq.get(temp_terms)!=null)
											{
												tfreq=(final_freq.get(temp_terms)).intValue();
												final_freq.put(temp_terms, new Integer(tfreq+1));
											}
											else final_freq.put(temp_terms, new Integer(1));
											three_terms.remove(0);
										}
										if (!term_found[0].equals("")) three_terms.add(term_found[0]);
									}
									if (final_freq.get(term_found[0])!=null)
									{
										tfreq=(final_freq.get(term_found[0])).intValue();
										final_freq.put(term_found[0], new Integer(tfreq+1));
									}
									else final_freq.put(term_found[0], new Integer(1));
									for (int i=1; i<param_done; i++)
									{
										bufread[i].readLine();
									}
								}
								else
								{
									real_term="";
									for (int i=1; i<param_done; i++)
									{
										secvalues[i]=bufread[i].readLine();
										values=secvalues[i].split("\t");
										for (int j=0; j<values.length; j++)
										{
											if (j==1 && values[j].indexOf(":")>0) values[j]=values[j].substring(0,values[j].indexOf(":"));
											values[j]=values[j].toLowerCase();
										}
										term_found[i]=values[0];
										types_found[i]=values[1];
										lemmas_found[i]=values[2];
										if (lemmas_found[i].indexOf("|")>=0) lemmas_found[i]=lemmas_found[i].substring(0,lemmas_found[i].indexOf("|"));
									}
									for (int i=0; i<param_done; i++)
									{
										tt=tagsets.get(i);
										if (avoidunknown && lemmas_found[i].equalsIgnoreCase("<unknown>")) lemmas_found[i]="";
										if (real_term.equals("") && tt.size()==0 && !lemmas_found[i].equals("")) real_term=lemmas_found[i];
										if (tt.size()>0)
										{
											if (real_term.equals("") && tt.contains(types_found[i]) && !lemmas_found[i].equals("")) real_term=lemmas_found[i];
										}
									}
									if (!real_term.equals(""))
									{
										if (add_two_terms)
										{
											if (two_terms.size()==2)
											{
												temp_terms=two_terms.get(0)+"_"+two_terms.get(1);
												if (final_freq.get(temp_terms)!=null)
												{
													tfreq=(final_freq.get(temp_terms)).intValue();
													final_freq.put(temp_terms, new Integer(tfreq+1));
												}
												else final_freq.put(temp_terms, new Integer(1));
												two_terms.remove(0);
											}
											two_terms.add(real_term);
										}
										if (add_three_terms)
										{
											if (three_terms.size()==3)
											{
												temp_terms=three_terms.get(0)+"_"+three_terms.get(1)+"_"+three_terms.get(2);
												if (final_freq.get(temp_terms)!=null)
												{
													tfreq=(final_freq.get(temp_terms)).intValue();
													final_freq.put(temp_terms, new Integer(tfreq+1));
												}
												else final_freq.put(temp_terms, new Integer(1));
												three_terms.remove(0);
											}
											three_terms.add(real_term);
										}
										if (final_freq.get(real_term)!=null)
										{
											tfreq=(final_freq.get(real_term)).intValue();
											final_freq.put(real_term, new Integer(tfreq+1));
										}
										else final_freq.put(real_term, new Integer(1));
									}
								}
							}
							for (int i=0; i<param_done; i++)
							{
								bufread[i].close();
								bufread[i]=null;
								existfile=new File(infile+"_"+String.valueOf(i)+".txt").exists();
								if (existfile) deletedfiles=new File(infile+"_"+String.valueOf(i)+".txt").delete();
							}
							if (final_freq.size()>0)
							{
								outvalues[0]=ref_site;
								outvalues[1]="";
								outvalues[2]="";
								outvalues[3]="";
								outvalues[4]="";
								Iterator<String> entries = final_freq.keySet().iterator();
								num_terms=0;
								con_size=0;
								while (entries.hasNext())
								{
								    line= entries.next();
								    tfreq=(final_freq.get(line)).intValue();
								    if (tfreq>mnf)
								    {
										outvalues[1]=outvalues[1]+line+" ";
										outvalues[2]=outvalues[2]+String.valueOf(tfreq)+" ";
										num_terms++;
										con_size=con_size+line.length();
									}
								}
								outvalues[3]=String.valueOf(num_terms);
								outvalues[4]=String.valueOf(con_size);
								if (num_terms>0)
								{
									record_wr++;
									dw.write(outvalues);
								}
							}
						}
					}
				}
				catch (Exception e)
				{
					if (bw!=null)
					{
						try
						{
							bw.close();
						}
						catch (Exception e1){}
					}
					for (int i=0; i<parameterfiles.length; i++)
					{
						if (bufread[i]!=null)
						{
							try
							{
								bufread[i].close();
								existfile=new File(infile+"_"+String.valueOf(i)+".txt").exists();
								if (existfile) deletedfiles=new File(infile+"_"+String.valueOf(i)+".txt").delete();
							}
							catch (Exception e1){}
						}
					}
					exerrors.add("%4074% ("+e.toString()+")\n");
				}
			}
			if (to_treat_sites.size()==0) minfile=-1;
			if (minfile==0) minfile=-1;
			if (vftd.size()>0)
			{
				try
				{
					for (Enumeration<String> en=vftd.keys(); en.hasMoreElements();)
					{
						ref_site=en.nextElement();
						FastTempDataSet tempftd = vftd.get(ref_site);
						tempftd.deletefile();
						tempftd=null;
					}
				}
				catch (Exception e){}
			}
		}
		System.gc();
		Vector<StepResult> result = new Vector<StepResult>();

		vftd.clear();
		vftd=null;

		Keywords.percentage_total=0;
		Keywords.percentage_done=0;

		result.add(new LocalMessageGetter("%3759%: "+String.valueOf(sites_lemmas)+"<br>\n"));
		result.add(new LocalMessageGetter("%3760%: "+String.valueOf(treated_sites.size())+"<br>\n"));

		for (int i=0; i<exerrors.size(); i++)
		{
			result.add(new LocalMessageGetter(exerrors.get(i)));
		}
		if (record_wr==0)
		{
			dw.deletetmp();
			return new Result("%3761%<br>\n", false, result);
		}

		String keyword="Web Scraper Tagger "+dictcontent.getkeyword();
		String description="Web Scraper Tagger "+dictcontent.getdescription();
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
	public LinkedList<GetRequiredParameters> getparameters() {
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"content=", "dict", true, 3750, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"tags=", "dict", false, 3751, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"sw=", "dict", false, 3399, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"gow=", "dict", false, 3701, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 3756, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.trettaggerexe,"file=all", true, 3366,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.parameterfileandtagsets,"multipletext", true, 3752,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 3753, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 3754, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 3755, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.file_encoding,"text", false, 3393,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.minfreq, "text", false, 3764, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.minlength, "text", false, 480, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.maxlength, "text", false, 481, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.limit_temp_files, "text", false, 4072, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.limit_reference, "text", false, 4069, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 4070, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.onlyascii, "checkbox", false, 3341, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.add_two_terms, "checkbox", false, 4077, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.add_three_terms, "checkbox", false, 4078, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.consider_numbers, "checkbox", false, 4079, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.avoidunknown, "checkbox", false, 3765, dep, "", 2));
		String[] depsw ={""};
		depsw[0]=Keywords.dict+"sw";
		parameters.add(new GetRequiredParameters(Keywords.varsw, "vars=all", false, 3400, depsw, "", 2));
		String[] depgw ={""};
		depgw[0]=Keywords.dict+"gow";
		parameters.add(new GetRequiredParameters(Keywords.vargow, "vars=all", false, 3702, depgw, "", 2));
		return parameters;
	}
	/**
	*Return the name of the procedure and the group to which belongs
	*/
	public String[] getstepinfo() {
		String[] info=new String[2];
		info[0]="4168";
		info[1]="3749";
		return info;
	}
	/**
	*Return the printable ascii char
	*/
	private String getAscii(String intext)
	{
		try
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
		catch (Exception e)
		{
			return "";
		}
	}
}
