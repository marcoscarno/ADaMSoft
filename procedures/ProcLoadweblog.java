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
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.BufferedInputStream;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;

import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.GetRequiredParameters;


/**
* This is the procedure that loads a web log file
* @author marco.scarno@gmail.com
* @date 19/02/2017
*/
public class ProcLoadweblog implements RunStep
{
	/**
	* Starts the execution of Proc Loadweblog and returns the corresponding message
	*/
	@SuppressWarnings("unchecked")
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		boolean delonend=false;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.logfile, Keywords.structure};
		String [] optionalparameters=new String[] {Keywords.dict, Keywords.chartodelete, Keywords.charstartquery, Keywords.delonend, Keywords.noupdate, Keywords.firstrow};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		Keywords.percentage_total=4;
		Keywords.percentage_done=0;
		boolean noupdate =(parameters.get(Keywords.noupdate)!=null);
		delonend =(parameters.get(Keywords.delonend)!=null);
		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		String fr=(String) parameters.get(Keywords.firstrow);
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		int firstrow=0;
		if (fr!=null)
		{
			try
			{
				firstrow=Integer.parseInt(fr);
			}
			catch (Exception e){}
		}

		String logfile    = (String) parameters.get(Keywords.logfile);

		String keyword="LoadWebLog "+logfile;
		String description="LoadWebLog "+logfile;
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());
		String structure=(String)parameters.get(Keywords.structure);

		String chartodelete=(String)parameters.get(Keywords.chartodelete);
		String[] chars=new String[0];
		try
		{
			if (chartodelete!=null)
			{
				chars=chartodelete.split(" ");
			}
			if (chars.length>0)
			{
				for (int i=0; i<chars.length; i++)
				{
					chars[i]=chars[i].trim();
					Integer.parseInt(chars[i]);
				}
			}
		}
		catch (Exception e)
		{
			return new Result("%1047% ("+e.toString()+")<br>\n", false, null);
		}

		String[] parts=structure.split("/");
		for (int i=0; i<parts.length; i++)
		{
			parts[i]=parts[i].trim();
		}
		Vector<String> varnames=new Vector<String>();
		Vector<int[]> separator=new Vector<int[]>();
		boolean isquery=false;
		String[] pps=null;
		try
		{
			for (int i=0; i<parts.length; i=i+2)
			{
				if (!varnames.contains(parts[i].toLowerCase()))
					varnames.add(parts[i].toLowerCase());
				else
					return new Result("%3452% ("+parts[i].toUpperCase()+")<br>\n", false, null);
				if (parts[i].equalsIgnoreCase(Keywords.query))
					isquery=true;
				if (i<parts.length-1)
				{
					pps=(parts[i+1].trim()).split(" ");
					int[] ps=new int[pps.length];
					for (int j=0; j<pps.length; j++)
					{
						try
						{
							ps[j] = Integer.parseInt(pps[j].trim());
						}
						catch (Exception e)
						{
							return new Result("%1053% ("+parts[i]+"/"+parts[i+1]+" :"+pps[j]+")<br>\n", false, null);
						}
					}
					separator.add(ps);
				}
			}
		}
		catch (Exception e)
		{
			return new Result("%1052% ("+e.toString()+")<br>\n", false, null);
		}
		String tempcsq=(String)parameters.get(Keywords.charstartquery);
		if ((isquery) && (tempcsq==null))
			return new Result("%1050%<br>\n", false, null);

		int charstartquery=-1;

		if (tempcsq!=null)
		{
			try
			{
				charstartquery = Integer.parseInt(tempcsq);
			}
			catch (Exception ee)
			{
				return new Result("%1051%<br>\n", false, null);
			}
		}

		java.net.URL fileUrl;
		try
		{
			if((logfile.toLowerCase()).startsWith("http"))
				fileUrl =  new java.net.URL(logfile);
			else
			{
				File file=new File(logfile);
				fileUrl = file.toURI().toURL();
			}
		}
		catch (Exception e)
		{
			return new Result("%1041% ("+logfile+")<br>\n", false, null);
		}
		String tempdir=(String)parameters.get(Keywords.WorkDir);

		Vector<String> allvars=new Vector<String>();

		boolean isdict=(parameters.get(Keywords.dict)!=null);

		DictionaryReader dict=null;

		int[] replacerule=new int[0];

		if (isdict)
		{
			dict = (DictionaryReader)parameters.get(Keywords.dict);
			int numberoldvars=dict.gettotalvar();
			replacerule=new int[numberoldvars];
			for (int i=0; i<numberoldvars; i++)
			{
				replacerule[i]=0;
				String varlabel=dict.getvarlabel(i);
				allvars.add(varlabel.toLowerCase());
			}
		}

		for (int i=0; i<varnames.size(); i++)
		{
			if (!allvars.contains(varnames.get(i).toLowerCase())) allvars.add(varnames.get(i).toLowerCase());
		}

		Random generator = new Random();
		int randomIndex = generator.nextInt(100);
		java.util.Date dateProcedure=new java.util.Date();
		long timeProcedure=dateProcedure.getTime();
		String tempfile=tempdir+String.valueOf(timeProcedure)+String.valueOf(randomIndex);

		BufferedOutputStream bisr = null;
		ObjectOutputStream oos = null;

		try
		{
			bisr = new BufferedOutputStream(new FileOutputStream(tempfile));
			oos = new ObjectOutputStream(bisr);
		}
		catch (Exception e)
		{
			try
			{
				oos.close();
				bisr.close();
			}
			catch (Exception ee) {}
			(new File(tempfile)).delete();
			return new Result("%1042%<br>\n", false, null);
		}
		int records=0;
		int effrecords=0;
		Keywords.percentage_done=1;
		try
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(fileUrl.openStream()));
			String str;
			int charSeparator = 0;
			String actualname=null;
			String queryref=null;
			String remainingquery=null;
			String[] queryparts=null;
			String[] tempparts=null;

			int actualrec=0;
			String newcontent="";
			int actualchar=0;
			int[] actualsepar=new int[0];
			int pointersep=0;
			int refpointer=0;
			int lastchar=0;
			int pointstr=0;
			boolean actualquery=false;
			while ((str = in.readLine()) != null)
			{
				actualrec++;
				if (actualrec<firstrow)
					str="";
				if (str.length()>3048)
					str="";
				str=str.trim();
				str=str.replaceAll("&quot;","\"");
				str=str.replaceAll("&lt;","<");
				str=str.replaceAll("&gt;",">");
				if (str.startsWith("#"))
					str="";
				if (!str.equals(""))
				{
					records++;
					if (chars.length!=0)
					{
						for (int i=0; i<chars.length; i++)
						{
							try
							{
								newcontent="";
								charSeparator = Integer.parseInt(chars[i]);
								for (int j=0; j<str.length(); j++)
								{
									actualchar=str.codePointAt(j);
									if (actualchar!=charSeparator) newcontent=newcontent+Character.toString(str.charAt(j));
								}
								str=newcontent;
								newcontent="";
							}
							catch (Exception ee) {}
						}
					}
					Hashtable<String, String> actualvalues=new Hashtable<String, String>();
					str=str.replaceAll("&amp;","&");
					try
					{
						for (int i=0; i<separator.size(); i++)
						{
							actualsepar=separator.get(i);
							pointersep=0;
							refpointer=0;
							newcontent="";
							pointstr=0;
							while (pointersep!=actualsepar.length)
							{
								actualchar=str.codePointAt(pointstr);
								if (actualchar!=actualsepar[refpointer])
								{
									newcontent=newcontent+Character.toString(str.charAt(pointstr));
									pointersep=0;
									lastchar=pointstr;
								}
								if (actualchar==actualsepar[refpointer])
								{
									pointersep++;
									refpointer++;
								}
								pointstr++;
								if (pointstr>=str.length()) pointersep=actualsepar.length;
							}
							actualname=varnames.get(i);
							str=str.substring(lastchar+actualsepar.length+1);
							if (!actualname.equalsIgnoreCase(Keywords.query))
							{
								if (!newcontent.trim().equals("")) actualvalues.put(actualname.toLowerCase(), newcontent.trim());
							}
							else
							{
								actualquery=false;
								lastchar=0;
								for (int j=0; j<newcontent.length(); j++)
								{
									actualchar=newcontent.codePointAt(j);
									if (actualchar==charstartquery)
									{
										actualquery=true;
										lastchar=j;
									}
								}
								if (actualquery)
								{
									queryref=newcontent.substring(0, lastchar+1);
									if (!queryref.trim().equals("")) actualvalues.put(actualname, queryref.trim());
									remainingquery=newcontent.substring(lastchar+1);
									queryparts=remainingquery.split("&");
									for (int j=0; j<queryparts.length; j++)
									{
										tempparts=queryparts[j].split("=");
										if (tempparts.length==2)
										{
											if (tempparts[0].length()<30)
											{
												if (allvars.indexOf(Keywords.query.toLowerCase()+"_"+tempparts[0].toLowerCase())<0)
												{
													allvars.add(Keywords.query.toLowerCase()+"_"+tempparts[0].toLowerCase());
												}
												actualvalues.put(Keywords.query.toLowerCase()+"_"+tempparts[0].toLowerCase(), tempparts[1].trim());
											}
										}
									}
								}
								else
								{
									if (!newcontent.trim().equals("")) actualvalues.put(actualname.toLowerCase(), newcontent.trim());
								}
							}
						}
						if (!str.equals(""))
						{
							actualvalues.put(varnames.get(varnames.size()-1), str.trim());
						}
						effrecords++;
						oos.writeObject(actualvalues);
						oos.reset();
					}
					catch (Exception ee) {}
				}
			}
			try
			{
				oos.close();
				bisr.close();
			}
			catch (Exception e) {}
			in.close();
		}
		catch (Exception ee)
		{
			try
			{
				oos.close();
				bisr.close();
			}
			catch (Exception eee) {}
			(new File(tempfile)).delete();
			return new Result("%1043% ("+logfile+")<br>\n"+ee.toString()+"<br>\n", false, null);
		}
		Keywords.percentage_done=2;
		if (records>0)
			result.add(new LocalMessageGetter("%3681%: "+String.valueOf(records)+"<br>\n"));
		if (effrecords>0)
			result.add(new LocalMessageGetter("%3453%: "+String.valueOf(effrecords)+"<br>\n"));

		DataSetUtilities dsu=new DataSetUtilities();
		for (int i=0; i<allvars.size(); i++)
		{
			Hashtable<String, String> tempmd=new Hashtable<String, String>();
			Hashtable<String, String> tempcl=new Hashtable<String, String>();
			if (isdict)
			{
				for (int j=0; j<dict.gettotalvar(); j++)
				{
					String label=dict.getvarlabel(j).toLowerCase();
					if (label.equalsIgnoreCase(allvars.get(i).toLowerCase()))
					{
						tempcl=dict.getcodelabelfromname(dict.getvarname(j));
						break;
					}
				}
			}
			dsu.addnewvar("v"+String.valueOf(i), allvars.get(i), Keywords.TEXTSuffix, tempcl, tempmd);
		}
		if (!dw.opendatatable(dsu.getfinalvarinfo()))
		{
			(new File(tempfile)).delete();
			return new Result(dw.getmessage(), false, null);
		}

		String[] outvalues=new String[allvars.size()];

		if ((isdict) && (noupdate))
			result.add(new LocalMessageGetter("%3454%<br>\n"));

		if ((isdict) && (!noupdate))
		{
			int readrecords=0;
			String[] values=null;
			DataReader data = new DataReader(dict);
			if (!data.open(null, replacerule, false))
				return new Result(data.getmessage(), false, null);
			for (int i=0; i<outvalues.length; i++)
			{
				outvalues[i]="";
			}
			while (!data.isLast())
			{
				readrecords++;
				values = data.getRecord();
				if (values!=null)
				{
					for (int i=0; i<values.length; i++)
					{
						outvalues[i]=values[i];
					}
					dw.write(outvalues);
				}
			}
			data.close();
			if (readrecords>0)
				result.add(new LocalMessageGetter("%3455% ("+String.valueOf(readrecords)+")<br>\n"));
		}
		try
		{
			File filetmp=new File(tempfile);
			java.net.URL fileUrlw =  filetmp.toURI().toURL();
			URLConnection urlConn;
			urlConn = fileUrlw.openConnection();
			urlConn.setDoInput(true);
			urlConn.setUseCaches(false);
			BufferedInputStream bis = new BufferedInputStream(urlConn.getInputStream());
			ObjectInputStream dis = new ObjectInputStream(bis);
			Hashtable<String,String> tempvalues=new Hashtable<String,String>();
			for (int i=0; i<records; i++)
			{
				try
				{
					tempvalues=(Hashtable<String,String>)dis.readObject();
					for (int j=0; j<allvars.size(); j++)
					{
						outvalues[j]="";
						if (tempvalues.get(allvars.get(j).toLowerCase())!=null)
						{
							outvalues[j]=tempvalues.get(allvars.get(j).toLowerCase());
						}
					}
					dw.write(outvalues);
				}
				catch (Exception exe){}
			}
			dis.close();
			bis.close();

		}
		catch (Exception e)
		{
			dw.deletetmp();
			try
			{
				(new File(tempfile)).delete();
			}
			catch (Exception eee) {}
			return new Result("%1143%<br>\n", false, null);
		}
		Keywords.percentage_done=3;
		try
		{
			(new File(tempfile)).delete();
		}
		catch (Exception eee) {}

		if ((delonend) && (!(logfile.toLowerCase()).startsWith("http")))
		{
			(new File(logfile)).delete();
		}
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;

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
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 440, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", false, 1038, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.logfile, "file=all", true, 1039, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.structure, "text", true, 1040, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 1044, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.chartodelete, "text", false, 1045, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 1046, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.charstartquery, "text", false, 1048, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 1049, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.noupdate, "checkbox", false, 1054, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.delonend, "checkbox", false, 1369, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.firstrow, "text", false, 1370, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="4182";
		retprocinfo[1]="1037";
		return retprocinfo;
	}
}
