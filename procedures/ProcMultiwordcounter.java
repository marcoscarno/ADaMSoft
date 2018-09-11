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
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.*;

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.TikaParser;

import ADaMSoft.utilities.WorkQueue;
import ADaMSoft.utilities.ObjectsForQueue;
import ADaMSoft.utilities.ObjectForQueue;

/**
* This is the procedure that writes the frequencies of each word in all the texts of files stored in a data set
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcMultiwordcounter implements RunStep
{
	boolean cases;
	int maxvalue, minvalue;
	/**
	*Evaluate the frequencies of each word
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		String[] requiredparameters = new String[] {Keywords.dict, Keywords.varpathfiles, Keywords.OUT.toLowerCase(), Keywords.OUT.toLowerCase()+"doclist"};
		String[] optionalparameters = new String[] {Keywords.varaddinfodoc, Keywords.vargroupby, Keywords.casesensitive,
		Keywords.writeifpresentfor, Keywords.minwords, Keywords.minfreq, Keywords.joinwords, Keywords.withchars,
		Keywords.nonumbers, Keywords.onlyascii, Keywords.minlength, Keywords.maxlength, Keywords.replacenewlines,
		Keywords.identifynewsentences, Keywords.charstoreplace, Keywords.charstodelete, Keywords.charstosubwspace,
		Keywords.dict+"sw", Keywords.varsw, Keywords.dict+"gow", Keywords.vargow, Keywords.docsprefix, Keywords.shortmsgs, Keywords.timeout, Keywords.numthreads};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		String varpathfiles=(String) parameters.get(Keywords.varpathfiles);
		String docsprefix=(String) parameters.get(Keywords.docsprefix);
		if (docsprefix==null)
			docsprefix="doc_";
		else if (docsprefix.indexOf(" ")>0)
		{
			Keywords.procedure_error=true;
			return new Result("%3645% ("+docsprefix+")<br>\n", false, null);
		}
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
					Keywords.procedure_error=true;
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

		ObjectsForQueue ofq=new ObjectsForQueue();
		String[] values=null;
		DataReader data = new DataReader(dict);
		if (!data.open(null, 1, false))
		{
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
		String[] refv=new String[2];
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
				refv[0]=fname;
				refv[1]=ifile;
				ofq.addelement(gfile, refv);
			}
		}
		data.close();
		int allf=ofq.getelem();
		if (allf==0)
		{
			return new Result("%3490%<br>\n", false, null);
		}

		ofq.fillqueue();

		int numthread=2;
		String tmp_numthread=(String) parameters.get(Keywords.numthreads);
		if (tmp_numthread==null) tmp_numthread="";
		if (!tmp_numthread.equals(""))
		{
			numthread=-1;
			try
			{
				double tma=Double.parseDouble(tmp_numthread);
				numthread=(int)tma;
			}
			catch (Exception et) {}
			if (numthread<1) return new Result("%3737%<br>\n", false, null);
			if (numthread>1000) return new Result("%3737%<br>\n", false, null);
		}
		if (allf<numthread) numthread=allf;

		boolean onlyascii = (parameters.get(Keywords.onlyascii) != null);
		boolean nonumbers = (parameters.get(Keywords.nonumbers) != null);
		boolean shortmsgs = (parameters.get(Keywords.shortmsgs) != null);
		DataWriter dw = new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
		{
			return new Result(dw.getmessage(), false, null);
		}

		DataWriter dwdl = new DataWriter(parameters, Keywords.OUT.toLowerCase()+"doclist");
		if (!dwdl.getmessage().equals(""))
		{
			return new Result(dwdl.getmessage(), false, null);
		}

		String minval = (String) parameters.get(Keywords.minlength);
		String maxval = (String) parameters.get(Keywords.maxlength);

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
				stopwords.add(valuessw[0]);
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
				gowords.add(valuesgow[0]);
			}
			datagow.close();
		}

		String minwords = (String) parameters.get(Keywords.minwords);
		int mnw=-1;
		if (minwords!=null)
		{
			mnw=0;
			try
			{
				mnw=Integer.parseInt(minwords);
			}
			catch (Exception e)
			{
				mnw=0;
			}
			if (mnw<1)
			{
				return new Result("%3479% ("+minwords+")<br>\n", false, null);
			}
		}

		String tempwriteifpresentfor = (String) parameters.get(Keywords.writeifpresentfor);
		int writeifpresentfor=0;
		if (tempwriteifpresentfor!=null)
		{
			try
			{
				writeifpresentfor=Integer.parseInt(tempwriteifpresentfor);
			}
			catch (Exception e)
			{
				return new Result("%3698% ("+tempwriteifpresentfor+")<br>\n", false, null);
			}
		}

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

		String joinwords = (String) parameters.get(Keywords.joinwords);
		int wtoj=1;
		if (joinwords!=null)
		{
			wtoj=0;
			try
			{
				wtoj=Integer.parseInt(joinwords);
			}
			catch (Exception e)
			{
				wtoj=0;
			}
		}
		if (wtoj<1)
		{
			return new Result("%3345% ("+joinwords+")<br>\n", false, null);
		}

		boolean replacenewlines = (parameters.get(Keywords.replacenewlines) != null);
		boolean identifynewsentences = (parameters.get(Keywords.identifynewsentences) != null);

		String chartoreplace = (String) parameters.get(Keywords.charstoreplace);
		String chartodelete = (String) parameters.get(Keywords.charstodelete);
		String chartosubwspace = (String) parameters.get(Keywords.charstosubwspace);
		String[] charstoreplace=null;
		String[] charstodelete=null;
		String[] charstosubwspace=null;
		if (chartoreplace!=null) charstoreplace=chartoreplace.split(";");
		if (chartodelete!=null) charstodelete=chartodelete.split(" ",-1);
		if (chartosubwspace!=null) charstosubwspace=chartosubwspace.split(" ", -1);
		minvalue = 0;
		maxvalue = 100;
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
		cases = (parameters.get(Keywords.casesensitive) != null);
		boolean usechars = (parameters.get(Keywords.withchars) != null);
		DataSetUtilities dsu=new DataSetUtilities();
		String keyword = "Multi word counter " + dict.getkeyword();
		String description = "Multi word counter " + dict.getdescription();
		String author = (String) parameters.get(Keywords.client_host.toLowerCase());
		String tempdir=(String)parameters.get(Keywords.WorkDir);

		Hashtable<String, String> tempmd=new Hashtable<String, String>();
		dsu.addnewvar("word", "%1142%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("total_word_presence", "%3688%", Keywords.NUMSuffix+Keywords.INTSuffix, tempmd, tempmd);
		if (usechars)
			dsu.addnewvar("ascii_codes", "%1140%", Keywords.TEXTSuffix, tempmd, tempmd);

		DataSetUtilities dsudl=new DataSetUtilities();
		dsudl.addnewvar("doc_varname", "%3775%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsudl.addnewvar("doc_varlabel", "%3776%", Keywords.TEXTSuffix, tempmd, tempmd);

		Keywords.percentage_total=2*allf;

		Semaphore sem=new Semaphore(1, true);

		WorkQueue queue=ofq.getqueue();

		TreeMap<String, TreeMap<String, Integer>> output = new TreeMap<String, TreeMap<String, Integer>>();
		TreeSet<String> loaded=new TreeSet<String>();
		WordCounterRetriever[] worret = new WordCounterRetriever[numthread];
		for (int i=0; i<worret.length; i++)
		{
			worret[i] = new WordCounterRetriever(queue, sem, tempdir, timeoutmillis, cases, replacenewlines,
			identifynewsentences, charstodelete, charstosubwspace, charstoreplace, onlyascii, nonumbers, shortmsgs, wtoj, mnf, mnw, minvalue, maxvalue);
			worret[i].setOutputMap(output);
			worret[i].setLoaded(loaded);
			worret[i].setGowords(gowords);
			worret[i].setStopwords(stopwords);
			worret[i].start();
		}

		try
		{
			for (int i=0; i<worret.length; i++)
			{
				worret[i].join();
			}
		}
		catch (Exception e){}

		for (int i=0; i<worret.length; i++)
		{
			Vector<String> tempmsg=worret[i].getretmsg();
			for (int j=0; j<tempmsg.size(); j++)
			{
				result.add(new LocalMessageGetter(tempmsg.get(j)+"<br>"));
			}
		}

		int tempint=0;
		Iterator<String> itr=loaded.iterator();
		Vector<String[]> docinforef=new Vector<String[]>();
		while(itr.hasNext())
		{
			String tgroup=itr.next();
			String[] tempdr=new String[2];
			tempdr[0]=docsprefix+String.valueOf(tempint+1);
			tempdr[1]=tgroup;
			docinforef.add(tempdr);
			dsu.addnewvar(docsprefix+String.valueOf(tempint+1), "%3477%: "+tgroup, Keywords.NUMSuffix+Keywords.INTSuffix, tempmd, tempmd);
			tempint++;
		}
		boolean resopen = dw.opendatatable(dsu.getfinalvarinfo());
		if (!resopen)
		{
			result.add(new LocalMessageGetter(dw.getmessage()));
			return new Result("", false, result);
		}

		resopen = dwdl.opendatatable(dsudl.getfinalvarinfo());
		if (!resopen)
		{
			result.add(new LocalMessageGetter(dwdl.getmessage()));
			return new Result("", false, result);
		}

		int def_lun=2+tempint;
		if (usechars) def_lun++;
		String[] out_val=new String[def_lun];
		Set<String> s = output.keySet();
		Iterator<String> it = s.iterator();
		int written=0;
		int startpoint=0;
		int total_times=0;
		Keywords.percentage_total=output.size()*2;
		Keywords.percentage_done=output.size();
		while (it.hasNext())
		{
			Keywords.percentage_done++;
			String key = it.next();
			out_val[0]=key;
			out_val[1]="";
			startpoint=2;
			total_times=0;
			if (usechars)
			{
				out_val[2]="";
				for (int i = 0; i < key.length(); i++)
				{
					out_val[2] += key.codePointAt(i) + " ";
				}
				startpoint=3;
			}
			TreeMap<String, Integer> temout=output.get(key);
			itr=loaded.iterator();
			while(itr.hasNext())
			{
				String tgroup=itr.next();
				if (temout.containsKey(tgroup))
				{
					out_val[startpoint]=String.valueOf((temout.get(tgroup)).intValue());
					total_times=total_times+1;
				}
				else out_val[startpoint]="0";
				startpoint++;
			}
			out_val[1]=String.valueOf(total_times);
			if (total_times>=writeifpresentfor)
			{
				written++;
				dw.write(out_val);
			}
		}
		for (int i=0; i<docinforef.size(); i++)
		{
			dwdl.write(docinforef.get(i));
		}
		System.gc();

		if (written==0)
		{
			dw.deletetmp();
			dwdl.deletetmp();
			result.add(new LocalMessageGetter(("%3481%<br>\n")));
			return new Result("", false, result);
		}
		boolean resclose = dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		resclose = dwdl.close();
		if (!resclose)
			return new Result(dwdl.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfo = dw.getVarInfo();
		Hashtable<String, String> datatableinfo = dw.getTableInfo();
		result.add(new LocalDictionaryWriter(dw.getdictpath(), keyword,description, author,
		dw.gettabletype(), datatableinfo,dsu.getfinalvarinfo(), tablevariableinfo, dsu.getfinalcl(),dsu.getfinalmd(), null));
		Vector<Hashtable<String, String>> tablevariableinfodl = dwdl.getVarInfo();
		Hashtable<String, String> datatableinfodl = dwdl.getTableInfo();
		result.add(new LocalDictionaryWriter(dwdl.getdictpath(), keyword,description, author,
		dwdl.gettabletype(), datatableinfodl,dsudl.getfinalvarinfo(), tablevariableinfodl, dsudl.getfinalcl(),dsudl.getfinalmd(), null));
		return new Result("", true, result);
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters() {
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 3470, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"sw=", "dict", false, 3399, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"gow=", "dict", false, 3701, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 440, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"doclist=", "setting=out", true, 3774, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.varpathfiles, "var=all", true, 3482, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varaddinfodoc, "vars=all", false, 3483, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroupby, "vars=all", false, 3484, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 3485, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.casesensitive, "checkbox", false, 467, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.withchars, "checkbox", false, 468, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.minlength, "text", false, 480, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.maxlength, "text", false, 481, dep, "", 2));
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
		parameters.add(new GetRequiredParameters(Keywords.joinwords, "text", false, 3343, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 3344, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.minwords, "text", false, 3478, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.minfreq, "text", false, 3496, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.docsprefix, "text", false, 3644, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.shortmsgs, "checkbox", false, 3683, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.writeifpresentfor, "text", false, 3697, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.timeout, "text", false, 3699, dep, "", 2));
		String[] depsw ={""};
		depsw[0]=Keywords.dict+"sw";
		parameters.add(new GetRequiredParameters(Keywords.varsw, "vars=all", false, 3400, depsw, "", 2));
		String[] depgw ={""};
		depgw[0]=Keywords.dict+"gow";
		parameters.add(new GetRequiredParameters(Keywords.vargow, "vars=all", false, 3702, depgw, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.numthreads,"text", false, 3738,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 3552, dep, "", 2));
		return parameters;
	}
	/**
	*Return the name of the procedure and the group to which belongs
	*/
	public String[] getstepinfo() {
		String[] info=new String[2];
		info[0]="4176";
		info[1]="3469";
		return info;
	}
}
/**
*This is the thread that retrieves the content
*/
class WordCounterRetriever extends Thread
{
	WorkQueue q;
	Semaphore sem;
	String tempdir, realtext, newcontent, key, fname, descriptor;
	boolean execounter, shortmsgs, to_add, isgoword, spgowords;
	int tempint, luntp, st, tot_word, mnf, numcsw, wtoj, mnw, minvalue, maxvalue, tempintt;
	double total_words;
	String[] tempsub;
	String[] test_parts;
	TikaParser tp;
	TreeMap<String, TreeMap<String, Integer>> output;
	Vector<String> retmsg;
	TreeMap<String, Integer> tempoutput;
	String[] split;
	TreeSet<String> loaded;
	HashSet<String> gowords;
	HashSet<String> stopwords;
	Vector<String[]> subfiles;
	WordCounterRetriever(WorkQueue q, Semaphore sem, String tempdir, int timeoutmillis, boolean cases, boolean replacenewlines,
		boolean identifynewsentences, String[] charstodelete, String[] charstosubwspace, String[] charstoreplace, boolean onlyascii,
		boolean nonumbers, boolean shortmsgs, int wtoj, int mnf, int mnw, int minvalue, int maxvalue)
    {
		this.q=q;
		this.sem=sem;
		execounter=true;
		tp=new TikaParser();
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
		retmsg=new Vector<String>();
		tempoutput = new TreeMap<String, Integer>();
		this.shortmsgs=shortmsgs;
		this.wtoj=wtoj;
		this.mnf=mnf;
		this.mnw=mnw;
		this.minvalue=minvalue;
		this.maxvalue=maxvalue;
		spgowords=false;
	}
	public void setOutputMap(TreeMap<String, TreeMap<String, Integer>> output)
	{
		this.output=output;
	}
	public void setLoaded(TreeSet<String> loaded)
	{
		this.loaded=loaded;
	}
    public Vector<String> getretmsg()
    {
		return retmsg;
	}
	public void setGowords(HashSet<String> gowords)
	{
		this.gowords=gowords;
		if (gowords.size()>0) spgowords=true;
	}
	public void setStopwords(HashSet<String> stopwords)
	{
		this.stopwords=stopwords;
	}
    public void run()
    {
        try
        {
			execounter=true;
			ObjectForQueue cofq;
            while (execounter)
            {
				sem.acquire();
				cofq=q.getWork();
				sem.release();
                if (cofq == null)
                {
					execounter=false;
                    break;
                }
                descriptor=cofq.getname();
                subfiles=cofq.getcontent();
                for (int sf=0; sf<subfiles.size(); sf++)
                {
					tempsub=subfiles.get(sf);
					tp.parseFile(tempsub[0]);
					if (!tp.getErrorParsing())
					{
						fname=tempsub[0];
						if (!shortmsgs) retmsg.add("%3473%: "+fname);
						numcsw=0;
						newcontent=tempsub[1]+" "+tp.getContent();
						split = newcontent.split(" ",-1);
						tempint=0;
						realtext="";
						if (split.length<wtoj)
						{
							retmsg.add("%3475% ("+fname+")\n");
						}
						else
						{
							total_words=0;
							test_parts=null;
							luntp=0;
							st=0;
							to_add=true;
							isgoword=false;
							tot_word=split.length;
							for (int i=0; i<=tot_word; i++)
							{
								realtext="";
								luntp=0;
								st=0;
								to_add=true;
								while (luntp!=wtoj)
								{
									if (i+st<tot_word)
									{
										if (!split[i+st].equals(""))
										{
											realtext=realtext+split[i+st]+" ";
											test_parts=(realtext.trim()).split(" ");
											luntp=test_parts.length;
										}
										st++;
									}
									else
									{
										to_add=false;
										luntp=wtoj;
									}
								}
								realtext=realtext.trim();
								if (to_add)
								{
									if (!realtext.equals(""))
									{
										if (realtext.length()>=minvalue && realtext.length()<=maxvalue)
										{
											if (!stopwords.contains(realtext))
											{
												isgoword=true;
												if (spgowords)
												{
													if (!gowords.contains(realtext))
														isgoword=false;
												}
												if (isgoword)
												{
													total_words++;
													if (!tempoutput.containsKey(realtext))
													{
														tempoutput.put(realtext, new Integer(1));
													}
													else
													{
														tempint=(tempoutput.get(realtext)).intValue();
														tempoutput.put(realtext, new Integer(tempint+1));
													}
												}
											}
										}
									}
								}
								else break;
							}
							if (mnf>0)
							{
								TreeMap<String, Integer> temptempoutput = new TreeMap<String, Integer>();
								Set<String> s = tempoutput.keySet();
								Iterator<String> it = s.iterator();
								key="";
								while (it.hasNext())
								{
									key = it.next();
									tempint = (tempoutput.get(key)).intValue();
									if (tempint>=mnf) temptempoutput.put(key, new Integer(tempint));
								}
								tempoutput.clear();
								s = temptempoutput.keySet();
								it = s.iterator();
								while (it.hasNext())
								{
									key = it.next();
									tempint = (temptempoutput.get(key)).intValue();
									tempoutput.put(key, new Integer(tempint));
								}
								temptempoutput.clear();
								temptempoutput=null;
							}
							if (mnw==-1 && tempoutput.size()>0)
							{
								sem.acquire();
								Set<String> s = tempoutput.keySet();
								Iterator<String> it = s.iterator();
								key="";
								while (it.hasNext())
								{
									numcsw++;
									key = it.next();
									tempint = (tempoutput.get(key)).intValue();
									if (!output.containsKey(key))
									{
										TreeMap<String, Integer> temout=new TreeMap<String, Integer>();
										temout.put(descriptor, new Integer(tempint));
										output.put(key, temout);
									}
									else
									{
										TreeMap<String, Integer> temout=output.get(key);
										if (!temout.containsKey(descriptor))
										{
											temout.put(descriptor, new Integer(tempint));
										}
										else
										{
											tempintt=(temout.get(descriptor)).intValue();
											temout.put(descriptor, new Integer(tempint+tempintt));
										}
										output.put(key, temout);
									}
								}
								loaded.add(descriptor);
								if (!shortmsgs) retmsg.add("%3476%: "+fname+"= "+String.valueOf(numcsw)+"\n");
								sem.release();
							}
							else
							{
								if (tempoutput.size()>=mnw)
								{
									sem.acquire();
									Set<String> s = tempoutput.keySet();
									Iterator<String> it = s.iterator();
									key="";
									while (it.hasNext())
									{
										numcsw++;
										key = it.next();
										tempint = (tempoutput.get(key)).intValue();
										if (!output.containsKey(key))
										{
											TreeMap<String, Integer> temout=new TreeMap<String, Integer>();
											temout.put(descriptor, new Integer(tempint));
											output.put(key, temout);
										}
										else
										{
											TreeMap<String, Integer> temout=output.get(key);
											if (!temout.containsKey(descriptor))
											{
												temout.put(descriptor, new Integer(tempint));
											}
											else
											{
												tempintt=(temout.get(descriptor)).intValue();
												temout.put(descriptor, new Integer(tempint+tempintt));
											}
											output.put(key, temout);
										}
									}
									loaded.add(descriptor);
									if (!shortmsgs) retmsg.add("%3476%: "+fname+"= "+String.valueOf(numcsw)+"\n");
									sem.release();
								}
								else
									retmsg.add("%3480% ("+fname+", "+String.valueOf(tempoutput.size())+")\n");
							}
							tempoutput.clear();
						}
					}
					else
						retmsg.add("%3474%: "+fname+"<br>\n"+tp.getMsgErrorParsing()+"<br>");
				}
                Keywords.percentage_done++;
			}
        }
        catch (InterruptedException e) {}
        catch (Exception ed) {}
    }
}
