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

import java.io.File;
import java.util.TreeMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.TikaParser;
import ADaMSoft.utilities.StepUtilities;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;

/**
* This is the procedure that writes a text file referred to a series of document specified by a data set
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcMultitextextractor implements RunStep
{
	boolean cases;
	int maxvalue, minvalue;
	TreeMap<String, Integer> output;
	/**
	*Write the text contained in a document
	*/
	@SuppressWarnings("resource")
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		String[] requiredparameters = new String[] {Keywords.dict, Keywords.varpathfiles, Keywords.outfile};
		String[] optionalparameters = new String[] {Keywords.varaddinfodoc, Keywords.vargroupby, Keywords.lowcase, Keywords.nonumbers,
		Keywords.onlyascii, Keywords.replacenewlines, Keywords.identifynewsentences, Keywords.charstoreplace, Keywords.charstodelete,
		Keywords.charstosubwspace, Keywords.noaddfileinfo, Keywords.shortmsgs, Keywords.onefilefordoc, Keywords.timeout};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		boolean onlyascii = (parameters.get(Keywords.onlyascii) != null);
		boolean nonumbers = (parameters.get(Keywords.nonumbers) != null);
		boolean shortmsgs = (parameters.get(Keywords.shortmsgs) != null);
		boolean onefilefordoc = (parameters.get(Keywords.onefilefordoc) != null);
		boolean noaddfileinfo = (parameters.get(Keywords.noaddfileinfo) != null);
		String timeout=(String) parameters.get(Keywords.timeout);
		int timeoutmillis=2000;
		if (timeout!=null)
		{
			timeoutmillis=-1;
			try
			{
				timeoutmillis=Integer.parseInt(timeout);
			}
			catch (Exception et) {}
			if (timeoutmillis<0)
			{
				return new Result("%3507%<br>\n", false, null);
			}
		}

		String outfile = (String) parameters.get(Keywords.outfile);
		String filepath=outfile;
		if (!filepath.endsWith(".txt"))
			filepath=filepath+".txt";
		String prefix_file=filepath.substring(0,filepath.lastIndexOf(".txt"));

		boolean replacenewlines = (parameters.get(Keywords.replacenewlines) != null);
		boolean identifynewsentences = (parameters.get(Keywords.identifynewsentences) != null);

		String chartoreplace = (String) parameters.get(Keywords.charstoreplace);
		String chartodelete = (String) parameters.get(Keywords.charstodelete);
		String chartosubwspace = (String) parameters.get(Keywords.charstosubwspace);
		String[] charstoreplace=null;
		String[] charstodelete=null;
		String[] charstosubwspace=null;
		if (chartoreplace!=null) charstoreplace=chartoreplace.split(";");
		if (chartodelete!=null) charstodelete=chartodelete.split(" ");
		if (chartosubwspace!=null) charstosubwspace=chartosubwspace.split(" ");

		cases = (parameters.get(Keywords.lowcase) != null);
		String tempdir=(String)parameters.get(Keywords.WorkDir);
		TikaParser tp=new TikaParser();
		tp.setWorkindDir(tempdir);
		tp.setTimeout(timeoutmillis);
		tp.setCases(cases);
		tp.setReplacenewlines(replacenewlines);
		tp.setIdentifynewsentences(identifynewsentences);
		tp.setCharstodelete(charstodelete);
		tp.setCharstosubwspace(charstosubwspace);
		tp.setCharstoreplace(charstoreplace);
		tp.setOnlyascii(onlyascii);
		tp.setNonumbers(nonumbers);

		String varpathfiles=(String) parameters.get(Keywords.varpathfiles);
		String tvargroupby=(String) parameters.get(Keywords.vargroupby);
		String tvaraddinfodoc=(String) parameters.get(Keywords.varaddinfodoc);
		String[] tvarpathfiles=varpathfiles.split(" ");
		if (tvarpathfiles.length!=1)
		{
			return new Result("%3472% ("+varpathfiles+")<br>\n", false, null);
		}
		int[] posva=null;
		String[] varaddinfodoc=null;
		if (tvaraddinfodoc!=null)
		{
			varaddinfodoc=tvaraddinfodoc.split(" ");
			posva=new int[varaddinfodoc.length];
			for (int i=0; i<posva.length; i++)
			{
				if (varaddinfodoc[i].equalsIgnoreCase(varpathfiles))
				{
					return new Result("%3486% ("+varpathfiles+")<br>\n", false, null);
				}
				posva[i]=-1;
			}
		}
		String[] vargroupby=null;
		int[] posvb=null;
		if (tvargroupby!=null)
		{
			vargroupby=tvargroupby.split(" ");
			posvb=new int[vargroupby.length];
			for (int i=0; i<posvb.length; i++)
			{
				if (vargroupby[i].equalsIgnoreCase(varpathfiles))
				{
					return new Result("%3487% ("+varpathfiles+")<br>\n", false, null);
				}
				posvb[i]=-1;
			}
		}
		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		int posvp=-1;
		for (int i=0; i<dict.gettotalvar(); i++)
		{
			if (varpathfiles.equalsIgnoreCase(dict.getvarname(i))) posvp=i;
			if (tvargroupby!=null)
			{
				for (int j=0; j<vargroupby.length; j++)
				{
					if (vargroupby[j].equalsIgnoreCase(dict.getvarname(i))) posvb[j]=i;
				}
			}
			if (tvaraddinfodoc!=null)
			{
				for (int j=0; j<varaddinfodoc.length; j++)
				{
					if (varaddinfodoc[j].equalsIgnoreCase(dict.getvarname(i))) posva[j]=i;
				}
			}
		}
		if (tvargroupby!=null)
		{
			for (int i=0; i<vargroupby.length; i++)
			{
				if (posvb[i]==-1)
				{
					return new Result("%3488% ("+vargroupby[i]+")<br>\n", false, null);
				}
			}
		}
		if (tvaraddinfodoc!=null)
		{
			for (int i=0; i<varaddinfodoc.length; i++)
			{
				if (posva[i]==-1)
				{
					return new Result("%3489% ("+varaddinfodoc[i]+")<br>\n", false, null);
				}
			}
		}
		TreeMap<String, Vector<String[]>> infofiles=new TreeMap<String, Vector<String[]>>();
		String[] values=null;
		DataReader data = new DataReader(dict);
		if (!data.open(null, 1, false))
		{
			Keywords.procedure_error=true;
			return new Result(data.getmessage(), false, null);
		}
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where))
			{
				return new Result(data.getmessage(), false, null);
			}
		}
		String gfile="";
		String ifile="";
		String fname="";
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				fname=values[posvp];
				gfile="";
				if (tvargroupby!=null)
				{
					for (int i=0; i<posvb.length; i++)
					{
						gfile=gfile+values[posvb[i]];
						if (i<posvb.length-1)
						{
							gfile=gfile+"-";
						}
					}
				}
				else gfile=fname;
				ifile="";
				if (tvaraddinfodoc!=null)
				{
					for (int i=0; i<posva.length; i++)
					{
						ifile=ifile+values[posva[i]];
						if (i<posva.length-1)
						{
							ifile=ifile+" ";
						}
					}
				}
				String[] tinfo=new String[2];
				tinfo[0]=fname;
				tinfo[1]=ifile;
				if (!infofiles.containsKey(gfile))
				{
					Vector<String[]> tv=new Vector<String[]>();
					tv.add(tinfo);
					infofiles.put(gfile, tv);
				}
				else
				{
					Vector<String[]> tv=infofiles.get(gfile);
					tv.add(tinfo);
					infofiles.put(gfile, tv);
				}
			}
		}
		data.close();
		if (infofiles.size()==0)
		{
			return new Result("%3490%<br>\n", false, null);
		}

		if (!onefilefordoc)
		{
			boolean exist=(new File(filepath)).exists();
			if (exist)
			{
				boolean success = (new File(filepath)).delete();
				if (!success)
				{
					return new Result("%3542% ("+filepath+")<br>\n", false, null);
				}
			}
			BufferedWriter out=null;
			try
			{
				out=new BufferedWriter(new FileWriter(filepath));
			}
			catch (Exception ex)
			{
				return new Result("%3543%: "+ex.toString()+"<br>\n", false, null);
			}
			String filecontent="";
			int written=0;
			int file_done=0;
			String fcont="";
			Keywords.percentage_total=infofiles.size();
			for (Iterator<String> f = infofiles.keySet().iterator(); f.hasNext();)
			{
				file_done++;
				Keywords.percentage_done=file_done;
				String tgroup=f.next();
				Vector<String[]> tv=infofiles.get(tgroup);
				filecontent="";
				for (int g=0; g<tv.size(); g++)
				{
					String[] tinfo=tv.get(g);
					fname=tinfo[0];
					tinfo[1]=tinfo[1].replaceAll("\t"," ");
					tp.parseFile(fname);
					if (tp.getErrorParsing()) result.add(new LocalMessageGetter("%3474%: "+fname+"<br>\n"+tp.getMsgErrorParsing()));
					else
					{
						fcont=tp.getContent();
						if (!fcont.trim().equals(""))
						{
							tinfo[1]=tinfo[1].trim();
							try
							{
								tinfo[1]=tinfo[1].replaceAll("\t"," ");
								tinfo[1]=tinfo[1].replaceAll("\0"," ");
								tinfo[1]=tinfo[1].replaceAll("\f"," ");
								tinfo[1]=tinfo[1].replaceAll("\uFFFD","");
								tinfo[1]=tinfo[1].replaceAll("\u00A0"," ");
								tinfo[1]=tinfo[1].replaceAll("\\s+"," ");
							}
							catch (Exception etinfo) {}
							if (!tinfo[1].equals("")) tinfo[1]=" "+tinfo[1];
							filecontent=filecontent.trim()+tinfo[1].trim()+" "+fcont;
							if (!shortmsgs) result.add(new LocalMessageGetter("%3473%: "+fname+"<br>"));
						}
					}
				}
				try
				{
					if (!filecontent.equals(""))
					{
						written++;
						if (noaddfileinfo)
							out.write(filecontent+"\n");
						else
							out.write(tgroup.trim()+"\t"+filecontent+"\n");
					}
				}
				catch (Exception e)
				{
					result.add(new LocalMessageGetter("%3553% ("+tgroup.trim()+")<br>\n"+e.toString()+"<br>\n"));
				}
			}
			try
			{
				out.close();
			}
			catch (Exception e)
			{
				return new Result("%3543%: "+e.toString()+"<br>\n", false, null);
			}
			if (written==0)
			{
				(new File(filepath)).delete();
				return new Result("%3554%<br>\n", false, null);
			}
			return new Result("%3545% ("+outfile+")<br>\n", true, result);
		}
		else
		{
			Vector<String[]> filesinfo=new Vector<String[]>();
			String filecontent="";
			int written=0;
			String fcont="";
			boolean realwritten=false;
			int file_done=0;
			Keywords.percentage_total=infofiles.size();
			for (Iterator<String> f = infofiles.keySet().iterator(); f.hasNext();)
			{
				file_done++;
				Keywords.percentage_done=file_done;
				filepath=prefix_file+"_"+String.valueOf(written+1)+".txt";
				boolean exist=(new File(filepath)).exists();
				if (exist)
				{
					boolean success = (new File(filepath)).delete();
					if (!success)
					{
						return new Result("%3542% ("+filepath+")<br>\n", false, null);
					}
				}
				String tgroup=f.next();
				Vector<String[]> tv=infofiles.get(tgroup);
				filecontent="";
				realwritten=false;
				for (int g=0; g<tv.size(); g++)
				{
					String[] tinfo=tv.get(g);
					fname=tinfo[0];
					tinfo[1]=tinfo[1].replaceAll("\t"," ");
					tp.parseFile(fname);
					if (tp.getErrorParsing()) result.add(new LocalMessageGetter("%3474%: "+fname+"<br>\n"+tp.getMsgErrorParsing()+"<br>"));
					else
					{
						fcont=tp.getContent();
						if (!fcont.equals(""))
						{
							tinfo[1]=tinfo[1].trim();
							try
							{
								tinfo[1]=tinfo[1].replaceAll("\t"," ");
								tinfo[1]=tinfo[1].replaceAll("\0"," ");
								tinfo[1]=tinfo[1].replaceAll("\f"," ");
								tinfo[1]=tinfo[1].replaceAll("\uFFFD","");
								tinfo[1]=tinfo[1].replaceAll("\u00A0"," ");
								tinfo[1]=tinfo[1].replaceAll("\\s+"," ");
							}
							catch (Exception etinfo) {}
							if (!tinfo[1].equals("")) tinfo[1]=" "+tinfo[1];
							filecontent=filecontent.trim()+tinfo[1].trim()+" "+fcont;
							if (!shortmsgs) result.add(new LocalMessageGetter("%3473%: "+fname+"<br>"));
							realwritten=true;
						}
					}
				}
				try
				{
					if (realwritten)
					{
						BufferedWriter out=null;
						try
						{
							out=new BufferedWriter(new FileWriter(filepath));
						}
						catch (Exception ex)
						{
							Keywords.procedure_error=true;
							return new Result("%3543%: "+ex.toString()+"<br>\n", false, null);
						}
						try
						{
							if (noaddfileinfo)
								out.write(filecontent+"\n");
							else
								out.write(tgroup.trim()+"\t"+filecontent+"\n");
						}
						catch (Exception e)
						{
							result.add(new LocalMessageGetter("%3553% ("+tgroup.trim()+")<br>\n"+e.toString()+"<br>\n"));
						}
						out.close();
						String[] ifil=new String[2];
						ifil[0]=String.valueOf(written+1);
						ifil[1]=tgroup.trim();
						filesinfo.add(ifil);
						written++;
					}
				}
				catch (Exception e){}
			}
			if (written==0)
			{
				return new Result("%3554%<br>\n", false, null);
			}
			filepath=prefix_file+"_summary.txt";
			BufferedWriter outsum=null;
			try
			{
				outsum=new BufferedWriter(new FileWriter(filepath));
			}
			catch (Exception ex)
			{
				Keywords.procedure_error=true;
				return new Result("%3543%: "+ex.toString()+"<br>\n", false, null);
			}
			for (int i=0; i<filesinfo.size(); i++)
			{
				String[] tifil=filesinfo.get(i);
				try
				{
					outsum.write(tifil[0]+"\t"+tifil[1]+"\n");
				}
				catch (Exception ee){}
			}
			try
			{
				outsum.close();
			}
			catch (Exception ex) {}
			return new Result("%3687%: "+prefix_file+"<br>\n", true, result);
		}
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters() {
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 3547, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.outfile, "filesave=txt", true,  3541, dep, "", 2));
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.varpathfiles, "var=all", true, 3482, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varaddinfodoc, "vars=all", false, 3483, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroupby, "vars=all", false, 3484, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 3485, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.lowcase, "checkbox", false, 3546, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.charstodelete, "text", false, 3333, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.charstosubwspace, "text", false, 3426, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.charstoreplace, "longtext", false, 3334, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 3329, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 3335, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.onlyascii, "checkbox", false, 3341, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replacenewlines, "checkbox", false, 3397, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 3398, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.identifynewsentences, "checkbox", false, 3395, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 3396, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.nonumbers, "checkbox", false, 3342, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.shortmsgs, "checkbox", false, 3683, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.noaddfileinfo, "checkbox", false, 3684, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.timeout, "text", false, 3699, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.onefilefordoc, "checkbox", false, 3685, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 3686, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		return parameters;
	}
	/**
	*Return the name of the procedure and the group to which belongs
	*/
	public String[] getstepinfo() {
		String[] info=new String[2];
		info[0]="4170";
		info[1]="3549";
		return info;
	}
}
