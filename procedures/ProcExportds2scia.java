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

import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.keywords.Keywords;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.Iterator;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;


/**
* This is the procedure that creates a data set and the corresponding VARDOM to be used with concord scia
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcExportds2scia implements RunStep
{
	/**
	* Starts the execution of Proc Exportds2scia and returns the corresponding message
	*/
	@SuppressWarnings("resource")
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		Keywords.procedure_error=false;
		String [] requiredparameters=new String[] {Keywords.dict, Keywords.dirsciafiles, Keywords.var, Keywords.projectname};
		String [] optionalparameters=new String[] {Keywords.oldvardom, Keywords.renamevar, Keywords.vartoretain, Keywords.varpresence,
		Keywords.varmissing, Keywords.vartorecode, Keywords.addidentifier, Keywords.replace};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		boolean viewprogress = true;
		boolean addidentifier =(parameters.get(Keywords.addidentifier)!=null);
		String testviewpossible=(String)parameters.get("possiblegui");
		if (!testviewpossible.equalsIgnoreCase("true")) viewprogress=false;
		if (viewprogress) Keywords.percentage_total=100;
 		if (viewprogress) Keywords.percentage_done=1;
 		String oldvardom=(String)parameters.get(Keywords.oldvardom);
 		Hashtable<String, String> oldvardominfo=new Hashtable<String, String>();
 		if (oldvardom!=null)
 		{
			try
			{
				java.net.URL fileovd;
				if((oldvardom.toLowerCase()).startsWith("http"))
					fileovd =  new java.net.URL(oldvardom);
				else
				{
					File file=new File(oldvardom);
					fileovd = file.toURI().toURL();
				}
		        BufferedReader oldvd = new BufferedReader(new InputStreamReader(fileovd.openStream()));
				String strovd;
				String ovname="";
				String ovdom="";
				while ((strovd = oldvd.readLine()) != null)
				{
					if (strovd.length()>11)
					{
						ovname=strovd.substring(0,6);
						ovdom=strovd.substring(11);
						oldvardominfo.put(ovname.trim().toUpperCase(), ovdom.toUpperCase());
					}
				}
				oldvd.close();
			}
			catch (Exception eod)
			{
				Keywords.procedure_error=true;
				return new Result("%3842% ("+eod.toString()+")<br>\n", false, null);
			}
		}
		String dirsciafiles=(String)parameters.get(Keywords.dirsciafiles);
		try
		{
			dirsciafiles=dirsciafiles.replaceAll("\\\\","/");
		}
		catch (Exception fs){}
		boolean okdf=false;
		if ((new File(dirsciafiles)).exists() && (new File(dirsciafiles)).isDirectory())
		{
			okdf=true;
		}
		if (!okdf)
		{
			Keywords.procedure_error=true;
			return new Result("%3784%<br>\n", false, null);
		}
		if (!dirsciafiles.endsWith("/")) dirsciafiles=dirsciafiles+"/";
		String projectname=(String)parameters.get(Keywords.projectname);
		String outdata=dirsciafiles+"Dataset."+projectname;
		String outvardom=dirsciafiles+"VARDOM."+projectname;
		String outadamsnotes=dirsciafiles+projectname+"_ADaMSoft_res.txt";
		boolean fedel=false;
		if((new File(outdata)).exists())
		{
			fedel=(new File(outdata)).delete();
			if (!fedel)
				return new Result("%3794%<br>\n", false, null);
		}
		if((new File(outvardom)).exists())
		{
			fedel=(new File(outvardom)).delete();
			if (!fedel)
				return new Result("%3795%<br>\n", false, null);
		}
		if((new File(outadamsnotes)).exists())
		{
			fedel=(new File(outadamsnotes)).delete();
			if (!fedel)
				return new Result("%3796%<br>\n", false, null);
		}
		BufferedWriter outputdata = null;
		BufferedWriter outputvardom = null;
		BufferedWriter outputadamsnotes = null;
		try
		{
			outputdata = new BufferedWriter(new FileWriter(new File(outdata)));
			outputvardom = new BufferedWriter(new FileWriter(new File(outvardom)));
			outputadamsnotes = new BufferedWriter(new FileWriter(new File(outadamsnotes)));
		}
		catch (Exception ef)
		{
				return new Result("%3797% ("+ef.toString()+")<br>\n", false, null);
		}
		String temp=(String)parameters.get(Keywords.var);
		String[] var=temp.split(" ");
		temp=(String)parameters.get(Keywords.vartoretain);
		String[] vartoretain=null;
		if (temp!=null)
			vartoretain=temp.split(" ");
		temp=(String)parameters.get(Keywords.vartorecode);
		String[] vartorecode=null;
		if (temp!=null)
			vartorecode=temp.split(" ");
		temp=(String)parameters.get(Keywords.varpresence);
		String[] varpresence=null;
		if (temp!=null)
			varpresence=temp.split(" ");
		temp=(String)parameters.get(Keywords.varmissing);
		String[] varmissing=null;
		if (temp!=null)
			varmissing=temp.split(" ");
		temp=(String)parameters.get(Keywords.renamevar);
		String[] renamevar=null;
		if (temp!=null)
			renamevar=temp.split(";");
		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		int nvar=dict.gettotalvar();
		String nv="";
		okdf=false;
		for (int j=0; j<var.length; j++)
		{
			okdf=false;
			for (int i=0; i<nvar; i++)
			{
				if (var[j].equalsIgnoreCase(dict.getvarname(i)))
					okdf=true;
			}
			if (!okdf)
				nv=nv+var[j]+" ";
		}
		if (!nv.equals(""))
			return new Result("%3798% ("+nv.trim()+")<br>\n", false, null);
		if (oldvardominfo.size()>0)
		{
			nv="";
			for (int j=0; j<var.length; j++)
			{
				if (oldvardominfo.get(var[j].toUpperCase())==null)
					nv=nv+var[j].toUpperCase()+" ";
			}
			if (!nv.equals(""))
					result.add(new LocalMessageGetter("%3843%: "+nv+"<br>\n"));
			nv="";
		}
		if (vartoretain!=null)
		{
			String nvr="";
			String nvrv="";
			okdf=false;
			for (int j=0; j<vartoretain.length; j++)
			{
				okdf=false;
				for (int i=0; i<nvar; i++)
				{
					if (vartoretain[j].equalsIgnoreCase(dict.getvarname(i)))
						okdf=true;
				}
				if (!okdf)
					nvr=nvr+vartoretain[j]+" ";
				for (int i=0; i<var.length; i++)
				{
					if (vartoretain[j].equalsIgnoreCase(var[i])) nvrv=nvrv+vartoretain[j]+" ";
				}
			}
			if (!nvr.equals(""))
				return new Result("%3799% ("+nvr.trim()+")<br>\n", false, null);
			if (!nvrv.equals(""))
				return new Result("%3800% ("+nvrv.trim()+")<br>\n", false, null);
		}
		if (vartorecode!=null)
		{
			String nvr="";
			okdf=false;
			for (int i=0; i<vartorecode.length; i++)
			{
				okdf=false;
				for (int j=0; j<var.length; j++)
				{
					if (var[j].equalsIgnoreCase(vartorecode[i])) okdf=true;
				}
				if (!okdf) nvr=nvr+vartorecode[i]+" ";
			}
			if (!nvr.equals(""))
				return new Result("%3801% ("+nvr.trim()+")<br>\n", false, null);
		}
		Vector<String[]> vrv=new Vector<String[]>();
		if (renamevar!=null)
		{
			for (int i=0; i<renamevar.length; i++)
			{
				try
				{
					String[] tvrv=renamevar[i].split("=");
					if (tvrv.length!=2)
						return new Result("%3802% ("+renamevar[i]+")%<br>\n%3803%<br>\n", false, null);
					tvrv[0]=tvrv[0].trim();
					tvrv[1]=tvrv[1].trim();
					vrv.add(tvrv);
				}
				catch (Exception erv)
				{
					return new Result("%3802% ("+renamevar[i]+")%<br>\n%3803%<br>\n", false, null);
				}
			}
			String nvr="";
			okdf=false;
			for (int i=0; i<vrv.size(); i++)
			{
				String[] tvrv=vrv.get(i);
				okdf=false;
				for (int j=0; j<var.length; j++)
				{
					if (var[j].equalsIgnoreCase(tvrv[0])) okdf=true;
				}
				if (!okdf) nvr=nvr+tvrv[0]+" ";
			}
			if (!nvr.equals(""))
				return new Result("%3804% ("+nvr.trim()+")<br>\n", false, null);
		}
		if (varpresence!=null)
		{
			String nvr="";
			okdf=false;
			for (int i=0; i<varpresence.length; i++)
			{
				okdf=false;
				for (int j=0; j<var.length; j++)
				{
					if (var[j].equalsIgnoreCase(varpresence[i])) okdf=true;
				}
				if (!okdf) nvr=nvr+varpresence[i]+" ";
			}
			if (!nvr.equals(""))
				return new Result("%3805% ("+nvr.trim()+")<br>\n", false, null);
		}
		if (varmissing!=null)
		{
			String nvr="";
			okdf=false;
			for (int i=0; i<varmissing.length; i++)
			{
				okdf=false;
				for (int j=0; j<var.length; j++)
				{
					if (var[j].equalsIgnoreCase(varmissing[i])) okdf=true;
				}
				if (!okdf) nvr=nvr+varmissing[i]+" ";
			}
			if (!nvr.equals(""))
				return new Result("%3806% ("+nvr.trim()+")<br>\n", false, null);
		}
		if (varpresence!=null && vartorecode!=null)
		{
			String nvr="";
			okdf=false;
			for (int i=0; i<varpresence.length; i++)
			{
				okdf=false;
				for (int j=0; j<vartorecode.length; j++)
				{
					if (varpresence[i].equalsIgnoreCase(vartorecode[j])) okdf=true;
				}
				if (okdf) nvr=nvr+varpresence[i]+" ";
			}
			if (!nvr.equals(""))
				return new Result("%3807% ("+nvr.trim()+")<br>\n", false, null);
		}
		int dimtotal=var.length;
		if (vartoretain!=null)
			dimtotal=dimtotal+vartoretain.length;
		String[] totalvar=new String[dimtotal];
		for (int i=0; i<var.length; i++)
		{
			totalvar[i]=var[i];
		}
		if (vartoretain!=null)
		{
			for (int i=0; i<vartoretain.length; i++)
			{
				totalvar[var.length+i]=vartoretain[i];
			}
		}
		Vector<String[]> vinfo=new Vector<String[]>();
		Vector<Vector<String>> valactives=new Vector<Vector<String>>();
		Vector<Integer> lenactives=new Vector<Integer>();
		Vector<Integer> reallenactives=new Vector<Integer>();
		for (int i=0; i<var.length; i++)
		{
			Vector<String> tdv=new Vector<String>();
			valactives.add(tdv);
			lenactives.add(new Integer(0));
			String[] tinfo=new String[4];
			tinfo[0]="";
			tinfo[1]=var[i];
			if (tinfo[1].length()>6) tinfo[1]=tinfo[1].substring(0,6);
			tinfo[2]="";
			tinfo[3]="";
			okdf=false;
			if (vartorecode!=null)
			{
				for (int j=0; j<vartorecode.length; j++)
				{
					if (var[i].equalsIgnoreCase(vartorecode[j])) okdf=true;
				}
				if (okdf) tinfo[0]="1";
			}
			okdf=false;
			if (vrv.size()>0)
			{
				String nname="";
				for (int j=0; j<vrv.size(); j++)
				{
					String[] tvrv=vrv.get(j);
					if (var[i].equalsIgnoreCase(tvrv[0])) nname=tvrv[1];
				}
				if (!nname.equals(""))
				{
					tinfo[1]=nname;
					if (tinfo[1].length()>6) tinfo[1]=tinfo[1].substring(0,6);
				}
			}
			okdf=false;
			if (varpresence!=null)
			{
				for (int j=0; j<varpresence.length; j++)
				{
					if (var[i].equalsIgnoreCase(varpresence[j])) okdf=true;
				}
				if (okdf) tinfo[2]="1";
			}
			if (varmissing!=null)
			{
				for (int j=0; j<varmissing.length; j++)
				{
					if (var[i].equalsIgnoreCase(varmissing[j])) okdf=true;
				}
				if (okdf) tinfo[3]="1";
			}
			vinfo.add(tinfo);
		}
		Vector<Integer> lenother=new Vector<Integer>();
		if (vartoretain!=null)
		{
			for (int i=0; i<vartoretain.length; i++)
			{
				lenother.add(new Integer(0));
			}
		}
		DataReader data = new DataReader(dict);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}
		String replace=(String)parameters.get(Keywords.replace);
		int rifrep=0;
		if (replace==null)
			rifrep=0;
		else if (replace.equalsIgnoreCase(Keywords.replaceall))
			rifrep=1;
		else if (replace.equalsIgnoreCase(Keywords.replaceformat))
			rifrep=2;
		else if (replace.equalsIgnoreCase(Keywords.replacemissing))
			rifrep=3;
		if (!data.open(totalvar, rifrep, false))
		{
			return new Result(data.getmessage(), false, null);
		}
		String[] values=null;
		int tempint=0;
		int basevar=vinfo.size();
		int totrecord=0;
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				totrecord++;
				for (int i=0; i<basevar; i++)
				{
					Vector<String> tdv=valactives.get(i);
					if (!tdv.contains(values[i])) tdv.add(values[i]);
					tempint=(lenactives.get(i)).intValue();
					if (values[i].length()>tempint)
						lenactives.set(i, new Integer(values[i].length()));
				}
				if (vartoretain!=null)
				{
					for (int i=0; i<vartoretain.length; i++)
					{
						tempint=(lenother.get(i)).intValue();
						if (values[i+basevar].length()>tempint)
							lenother.set(i, new Integer(values[i+basevar].length()));
					}
				}
			}
		}
		data.close();
		if (viewprogress) Keywords.percentage_done=50;
		Vector<TreeMap<String, String>> realvalactives=new Vector<TreeMap<String, String>>();
		Vector<Hashtable<String, String>> realrecvalactives=new Vector<Hashtable<String, String>>();
		String tempvv="";
		String tempnv="";
		int maxdigit=0;
		int currcode=0;
		try
		{
			outputadamsnotes.write("Number of records found in the input data set= "+totrecord+"\n");
		}
		catch (Exception ef)
		{
			try
			{
				outputdata.close();
			}
			catch (Exception ec) {}
			try
			{
				outputvardom.close();
			}
			catch (Exception ec) {}
			try
			{
				outputadamsnotes.close();
			}
			catch (Exception ec) {}
			return new Result("%Error writing the result file% ("+ef.toString()+")<br>\n", false, null);
		}
		String infofile="";
		String tempstring="";
		try
		{
			infofile="";
			for (int j=0; j<var.length; j++)
			{
				infofile=infofile+var[j]+" ";
			}
			outputadamsnotes.write("List of selected active variables="+infofile.trim()+"\n");
			if (vartoretain!=null)
			{
				infofile="";
				for (int j=0; j<vartoretain.length; j++)
				{
					infofile=infofile+vartoretain[j]+" ";
				}
				outputadamsnotes.write("Additional variables to keep in the output data file="+infofile.trim()+"\n");
			}
			else outputadamsnotes.write("No selection of additional variables to keep in the output data file\n");
			if (vartorecode!=null)
			{
				infofile="";
				for (int j=0; j<vartorecode.length; j++)
				{
					infofile=infofile+vartorecode[j]+" ";
				}
				outputadamsnotes.write("List of active variables to recode="+infofile.trim()+"\n");
			}
			else outputadamsnotes.write("No selection of variables to recode\n");
			if (varpresence!=null)
			{
				infofile="";
				for (int j=0; j<varpresence.length; j++)
				{
					infofile=infofile+varpresence[j]+" ";
				}
				outputadamsnotes.write("List of active variables that have to be verified for the presence of a value="+infofile.trim()+"\n");
			}
			else outputadamsnotes.write("No selection of variables that have to be verified for the presence of a value\n");
			if (varmissing!=null)
			{
				infofile="";
				for (int j=0; j<varmissing.length; j++)
				{
					infofile=infofile+varmissing[j]+" ";
				}
				outputadamsnotes.write("List of active variables that can have a missing value="+infofile.trim()+"\n");
			}
			else outputadamsnotes.write("No selection of variables that can have a missing value\n");
			outputadamsnotes.write("\n");
		}
		catch (Exception ef)
		{
			try
			{
				outputdata.close();
			}
			catch (Exception ec) {}
			try
			{
				outputvardom.close();
			}
			catch (Exception ec) {}
			try
			{
				outputadamsnotes.close();
			}
			catch (Exception ec) {}
			return new Result("%Error writing the result file% ("+ef.toString()+")<br>\n", false, null);
		}
		boolean allmissing=false;
		String tempdiffv="";
		for (int i=0; i<vinfo.size(); i++)
		{
			try
			{
				outputadamsnotes.write("\n");
			}
			catch (Exception ef)
			{
				try
				{
					outputdata.close();
				}
				catch (Exception ec) {}
				try
				{
					outputvardom.close();
				}
				catch (Exception ec) {}
				try
				{
					outputadamsnotes.close();
				}
				catch (Exception ec) {}
				return new Result("%3813% ("+ef.toString()+")<br>\n", false, null);
			}
			TreeMap<String, String> recode=new TreeMap<String, String>();
			Hashtable<String, String> trecode=new Hashtable<String, String>();
			String[] tinfo=vinfo.get(i);
			try
			{
				infofile="Name used in VARDOM for: "+var[i]+"="+tinfo[1]+"\n";
				outputadamsnotes.write(infofile);
				if (oldvardominfo.get(tinfo[1].trim().toUpperCase())!=null)
				{
					infofile="The codes for the variables: "+var[i]+" ("+tinfo[1]+") derive from another VARDOM file and are:"+oldvardominfo.get(tinfo[1].trim().toUpperCase())+"\n";

				}
			}
			catch (Exception ef)
			{
				try
				{
					outputdata.close();
				}
				catch (Exception ec) {}
				try
				{
					outputvardom.close();
				}
				catch (Exception ec) {}
				try
				{
					outputadamsnotes.close();
				}
				catch (Exception ec) {}
				return new Result("%3813% ("+ef.toString()+")<br>\n", false, null);
			}
			allmissing=true;
			Vector<String> tdv=valactives.get(i);
			if (tdv.size()>0)
			{
				for (int t=0; t<tdv.size(); t++)
				{
					if (!tdv.get(t).equals("")) allmissing=false;
				}
			}
			if (allmissing)
			{
				try
				{
					infofile="WARNING: "+var[i]+" does not have valid values\n";
					outputadamsnotes.write(infofile);
				}
				catch (Exception ef)
				{
					try
					{
						outputdata.close();
					}
					catch (Exception ec) {}
					try
					{
						outputvardom.close();
					}
					catch (Exception ec) {}
					try
					{
						outputadamsnotes.close();
					}
					catch (Exception ec) {}
					return new Result("%3813% ("+ef.toString()+")<br>\n", false, null);
				}
			}
			else
			{
				infofile="Different values found for variable: "+var[i]+"=";
				for (int t=0; t<tdv.size(); t++)
				{
					tempdiffv=tdv.get(t);
					if (tempdiffv.equals("")) infofile=infofile+"MISSING ";
					else
						infofile=infofile+tempdiffv+" ";
				}
				try
				{
					outputadamsnotes.write(infofile+"\n");
				}
				catch (Exception ef)
				{
					try
					{
						outputdata.close();
					}
					catch (Exception ec) {}
					try
					{
						outputvardom.close();
					}
					catch (Exception ec) {}
					try
					{
						outputadamsnotes.close();
					}
					catch (Exception ec) {}
					return new Result("%3813% ("+ef.toString()+")<br>\n", false, null);
				}
			}
			tempint=(lenactives.get(i)).intValue();
			if (tinfo[2].equals("1"))
			{
				reallenactives.add(new Integer(1));
				infofile="The variable: "+var[i]+" will be checked for the presence of values";
				try
				{
					outputadamsnotes.write(infofile+"\n");
				}
				catch (Exception ef)
				{
					try
					{
						outputdata.close();
					}
					catch (Exception ec) {}
					try
					{
						outputvardom.close();
					}
					catch (Exception ec) {}
					try
					{
						outputadamsnotes.close();
					}
					catch (Exception ec) {}
					return new Result("%3813% ("+ef.toString()+")<br>\n", false, null);
				}
				realvalactives.add(recode);
				realrecvalactives.add(trecode);
			}
			else
			{
				if (tinfo[0].equals(""))
				{
					for (int t=0; t<tdv.size(); t++)
					{
						tempvv=tdv.get(t);
						if (!tempvv.equals(""))
						{
							tempnv=tempvv;
							if (tempvv.length()<tempint)
							{
								for (int k=0; k<(tempint-tempvv.length()); k++)
								{
									tempnv="0"+tempnv;
								}
							}
							recode.put(tempnv, tempvv);
						}
					}
					for (Iterator<String> it = recode.keySet().iterator(); it.hasNext();)
					{
						tempnv= it.next();
   						tempvv= (String)recode.get(tempnv);
						infofile="Code value for: "+var[i]+"="+tempvv+"="+tempnv+"\n";
						try
						{
							outputadamsnotes.write(infofile);
						}
						catch (Exception ef)
						{
							try
							{
								outputdata.close();
							}
							catch (Exception ec) {}
							try
							{
								outputvardom.close();
							}
							catch (Exception ec) {}
							try
							{
								outputadamsnotes.close();
							}
							catch (Exception ec) {}
							return new Result("%3813% ("+ef.toString()+")<br>\n", false, null);
						}
						trecode.put(tempvv, tempnv);
					}
					realvalactives.add(recode);
					realrecvalactives.add(trecode);
					reallenactives.add(lenactives.get(i));
				}
				if (tinfo[0].equals("1"))
				{
					maxdigit=(String.valueOf(tdv.size())).length();
					reallenactives.add(new Integer(maxdigit));
					currcode=0;
					for (int t=0; t<tdv.size(); t++)
					{
						tempvv=tdv.get(t);
						if (!tempvv.equals(""))
						{
							currcode++;
							tempnv=String.valueOf(currcode);
							tempstring=String.valueOf(currcode);
							tempint=tempstring.length();
							if (tempint<maxdigit)
							{
								for (int k=0; k<(maxdigit-tempint); k++)
								{
									tempnv="0"+tempnv;
								}
							}
							recode.put(tempnv, tempvv);
						}
					}
					for (Iterator<String> it = recode.keySet().iterator(); it.hasNext();)
					{
						tempnv= it.next();
   						tempvv= (String)recode.get(tempnv);
						infofile="Code value for: "+var[i]+"="+tempvv+"="+tempnv+"\n";
						try
						{
							outputadamsnotes.write(infofile);
						}
						catch (Exception ef)
						{
							try
							{
								outputdata.close();
							}
							catch (Exception ec) {}
							try
							{
								outputvardom.close();
							}
							catch (Exception ec) {}
							try
							{
								outputadamsnotes.close();
							}
							catch (Exception ec) {}
							return new Result("%3813% ("+ef.toString()+")<br>\n", false, null);
						}
   						trecode.put(tempvv, tempnv);
					}
					realvalactives.add(recode);
					realrecvalactives.add(trecode);
				}
			}
		}
		if (!data.open(totalvar, rifrep, false))
		{
			return new Result(data.getmessage(), false, null);
		}
		infofile="";
		String refa=String.valueOf(totrecord);
		String refb="";
		int[] start_positions=new int[basevar];
		int[] length_variables=new int[basevar];
		int[] start_positions_ret=null;
		int[] length_variables_ret=null;
		if (vartoretain!=null)
		{
			start_positions_ret=new int[vartoretain.length];
			length_variables_ret=new int[vartoretain.length];
		}
		int position_identifier=0;
		int length_identifier=0;
		boolean added_info=false;
		int currecord=0;
		while (!data.isLast())
		{
			infofile="";
			values = data.getRecord();
			if (values!=null)
			{
				currecord++;
				if (viewprogress)
				{
					Keywords.percentage_done=50+50*currecord/totrecord;
				}
				for (int i=0; i<basevar; i++)
				{
					if (!added_info) start_positions[i]=infofile.length()+1;
					String[] tinfo=vinfo.get(i);
					if (tinfo[2].equals("1"))
					{
						if (values[i].equals("")) infofile=infofile+" ";
						else infofile=infofile+"1";
					}
					else
					{
						Hashtable<String, String> trecode=realrecvalactives.get(i);
						if (values[i].equals(""))
						{
							tempint=(reallenactives.get(i)).intValue();
							for (int j=0; j<tempint; j++)
							{
								infofile=infofile+" ";
							}
						}
						else
						{
							tempnv=trecode.get(values[i]);
							infofile=infofile+tempnv;
						}
					}
					if (!added_info) length_variables[i]=infofile.length()-start_positions[i]+1;
				}
				if (vartoretain!=null)
				{
					for (int i=0; i<vartoretain.length; i++)
					{
						if (!added_info) start_positions_ret[i]=infofile.length()+1;
						tempint=(lenother.get(i)).intValue();
						if (values[i+basevar].length()<tempint)
						{
							for (int j=0; j<(tempint-values[i+basevar].length()); j++)
							{
								infofile=infofile+" ";
							}
						}
						infofile=infofile+values[i+basevar];
						if (!added_info) length_variables_ret[i]=infofile.length()-start_positions_ret[i]+1;
					}
				}
				if (addidentifier)
				{
					if (!added_info) position_identifier=infofile.length()+1;
					refb=String.valueOf(currecord);
					tempint=refa.length()-refb.length();
					for (int j=0; j<tempint; j++)
					{
						infofile=infofile+" ";
					}
					infofile=infofile+refb;
					if (!added_info) length_identifier=infofile.length()-position_identifier+1;
				}
				try
				{
					outputdata.write(infofile+"\n");
				}
				catch (Exception ec) {}
				added_info=true;
			}
		}
		data.close();
		try
		{
			outputdata.close();
		}
		catch (Exception ec) {}
		infofile="";
		String infoadams="";
		String cvn="";
		for (int i=0; i<vinfo.size(); i++)
		{
			infoadams="Position of active variable and length: "+var[i]+"="+String.valueOf(start_positions[i])+"-"+String.valueOf(length_variables[i]);
			String[] tinfo=vinfo.get(i);
			cvn=tinfo[1].trim().toUpperCase();
			infofile=cvn;
			if (infofile.length()<6)
			{
				tempint=6-infofile.length();
				for (int j=0; j<tempint; j++)
				{
					infofile=infofile+" ";
				}
			}
			tempint=start_positions[i];
			tempnv=String.valueOf(tempint);
			if (tempnv.length()<4)
			{
				tempint=4-tempnv.length();
				for (int j=0; j<tempint; j++)
				{
					tempnv="0"+tempnv;
				}
			}
			infofile=infofile+tempnv;
			tempint=length_variables[i];
			tempnv=String.valueOf(tempint);
			infofile=infofile+tempnv;
			if (oldvardominfo.get(cvn)==null)
			{
				if (!tinfo[2].equals("1"))
				{
					TreeMap<String, String> recode=realvalactives.get(i);
					for (Iterator<String> it = recode.keySet().iterator(); it.hasNext();)
					{
						tempnv=it.next();
						tempint=4-tempnv.length();
						for (int j=0; j<tempint; j++)
						{
							tempnv="0"+tempnv;
						}
						infofile=infofile+tempnv;
					}
				}
				else infofile=infofile+"00010001";
				if (tinfo[3].equals("1")) infofile=infofile+"B   B";
			}
			else
			{
				infofile=infofile+oldvardominfo.get(cvn);
			}
			try
			{
				outputvardom.write(infofile+"\n");
				outputadamsnotes.write(infoadams+"\n");
			}
			catch (Exception ec) {}
		}
		if (vartoretain!=null)
		{
			for (int i=0; i<vartoretain.length; i++)
			{
				infoadams="Position of retained variable and length: "+vartoretain[i]+"="+String.valueOf(start_positions_ret[i])+"-"+String.valueOf(length_variables_ret[i]);
				try
				{
					outputadamsnotes.write(infoadams+"\n");
				}
				catch (Exception ec) {}
			}
		}
		if (position_identifier!=0)
		{
			infoadams="Position of IDENTIFIER and length: "+String.valueOf(position_identifier)+"-"+String.valueOf(length_identifier);
			try
			{
				outputadamsnotes.write(infoadams+"\n");
			}
			catch (Exception ec) {}
		}
		try
		{
			outputadamsnotes.close();
			outputvardom.close();
		}
		catch (Exception ec) {}
		result.add(new LocalMessageGetter("%3809%: "+outdata+"<br>\n"));
		result.add(new LocalMessageGetter("%3810%: "+outvardom+"<br>\n"));
		result.add(new LocalMessageGetter("%3811%: "+outadamsnotes+"<br>\n"));
		result.add(new LocalMessageGetter("%3812%\n"));
		return new Result("%3823%", true, result);
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 3781, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dirsciafiles, "dir", true, 3782, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.projectname, "text", true, 3785, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", true, 3783, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.oldvardom, "file=all", false, 3841, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.renamevar, "textvarsws", false, 3786, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 3787, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 3788, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vartoretain, "vars=all", false, 3789, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varpresence, "vars=all", false, 3790, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vartorecode, "vars=all", false, 3791, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varmissing, "vars=all", false, 3792, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.addidentifier, "checkbox", false, 3793, dep, "", 2));
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
		retprocinfo[0]="4167";
		retprocinfo[1]="3780";
		return retprocinfo;
	}
}
