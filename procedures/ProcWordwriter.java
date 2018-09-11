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
import java.util.LinkedList;
import java.util.Vector;
import java.util.HashSet;

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.TikaParser;


/**
* This is the procedure that writes each word in a text or in a word document
* @author marco.scarno@gmail.com
* @date 03/04/2017
*/
public class ProcWordwriter implements RunStep
{
	boolean cases;
	int maxvalue, minvalue;
	TreeMap<String, Integer> output;
	/**
	*Evaluate the frequencies of each word
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean noout=false;
		Vector<StepResult> result = new Vector<StepResult>();
		String[] requiredparameters = new String[] {Keywords.OUT.toLowerCase(), Keywords.infile};
		String[] optionalparameters = new String[] {Keywords.casesensitive, Keywords.joinwords, Keywords.withchars, Keywords.writealsoifempty, Keywords.nonumbers, Keywords.onlyascii, Keywords.minlength, Keywords.maxlength, Keywords.replacenewlines, Keywords.identifynewsentences, Keywords.charstoreplace, Keywords.charstodelete,
		Keywords.charstosubwspace, Keywords.dict+"sw", Keywords.varsw, Keywords.timeout};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		boolean onlyascii = (parameters.get(Keywords.onlyascii) != null);
		boolean nonumbers = (parameters.get(Keywords.nonumbers) != null);
		boolean writealsoifempty = (parameters.get(Keywords.writealsoifempty) != null);
		DataWriter dw = new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);
		String filename = (String) parameters.get(Keywords.infile);
		String minval = (String) parameters.get(Keywords.minlength);
		String maxval = (String) parameters.get(Keywords.maxlength);
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
			if (timeoutmillis<0) return new Result("%3507%<br>\n", false, null);
		}

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
			return new Result("%3345% ("+joinwords+")<br>\n", false, null);

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
		minvalue = 0;
		maxvalue = 100;
		output = new TreeMap<String, Integer>();
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
		if (minvalue<0) return new Result("%3325%<br>\n", false, null);
		if (maxvalue<0) return new Result("%3325%<br>\n", false, null);
		if (minvalue>maxvalue) return new Result("%3325%<br>\n", false, null);
		cases = (parameters.get(Keywords.casesensitive) != null);
		boolean usechars = (parameters.get(Keywords.withchars) != null);
		String newcontent="";
		DataSetUtilities dsu=new DataSetUtilities();
		String keyword = filename;
		String description = "Word writer for " + filename;
		String author = (String) parameters.get(Keywords.client_host.toLowerCase());
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
		tp.parseFile(filename);
		if (tp.getErrorParsing()) return new Result(tp.getMsgErrorParsing(), false, null);
		result.add(new LocalMessageGetter(tp.getMsgFileP()));
		if (!tp.getMsgLang().equals("")) result.add(new LocalMessageGetter(tp.getMsgLang()));
		try
		{
			int numcsw=0;
			newcontent=tp.getContent();
			String[] split = newcontent.split(" ",-1);
			String realtext="";
			if (split.length<wtoj) return new Result("%3346%<br>\n", false, null);
			Hashtable<String, String> tempmd=new Hashtable<String, String>();
			dsu.addnewvar("word", "%1142%", Keywords.TEXTSuffix, tempmd, tempmd);
			if (usechars)
				dsu.addnewvar("ascii_codes", "%1140%", Keywords.TEXTSuffix, tempmd, tempmd);
			boolean resopen = dw.opendatatable(dsu.getfinalvarinfo());
			if (!resopen)
				return new Result(dw.getmessage(), false, null);
			int def_lun=1;
			if (usechars) def_lun++;
			String[] out_val=new String[def_lun];
			int written=0;
			String[] test_parts=null;
			int luntp=0;
			int st=0;
			boolean to_add=true;
			int tot_word=split.length;
			Keywords.percentage_total=split.length;
			Keywords.percentage_done=0;
			for (int i=0; i<=split.length; i++)
			{
				Keywords.percentage_done=written;
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
								written++;
								out_val[0]=realtext;
								if (usechars)
								{
									out_val[1]="";
									for (int h = 0; h < realtext.length(); h++)
									{
										out_val[1] += realtext.codePointAt(h) + " ";
									}
								}
								dw.write(out_val);
							}
							else
								numcsw++;
						}
					}
				}
				else break;
			}
			if (written==0 && !writealsoifempty)
			{
				dw.deletetmp();
				result.add(new LocalMessageGetter("%3340%<br>\n"));
				noout=true;
			}
			if (written==0 && writealsoifempty)
			{
				out_val[0]="NO_VALID_WORD";
				if (usechars) out_val[1]="";
				dw.write(out_val);
				result.add(new LocalMessageGetter("%3429%<br>\n"));
			}
			if (numcsw>0)
				result.add(new LocalMessageGetter("%3403%= "+String.valueOf(numcsw)+"<br>\n"));
		}
		catch (Exception e)
		{
			dw.deletetmp();
			return new Result("%3332%\n"+e.toString()+"<br>\n", false, null);
		}
		result.add(new LocalMessageGetter("%3338%<br>\n"));
		if (!noout)
		{
			boolean resclose = dw.close();
			if (!resclose)
				return new Result(dw.getmessage(), false, null);
			Vector<Hashtable<String, String>> tablevariableinfo = dw.getVarInfo();
			Hashtable<String, String> datatableinfo = dw.getTableInfo();
			result.add(new LocalDictionaryWriter(dw.getdictpath(), keyword,description, author,
			dw.gettabletype(), datatableinfo,dsu.getfinalvarinfo(), tablevariableinfo, dsu.getfinalcl(),dsu.getfinalmd(), null));
		}
		return new Result("", true, result);
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters() {
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"sw=", "dict", false, 3399, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 440, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.infile, "file=all", true,  4172, dep, "", 2));
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
		parameters.add(new GetRequiredParameters(Keywords.writealsoifempty, "checkbox", false, 3428, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.timeout, "text", false, 3699, dep, "", 2));
		String[] depsw ={""};
		depsw[0]=Keywords.dict+"sw";
		parameters.add(new GetRequiredParameters(Keywords.varsw, "vars=all", false, 3400, depsw, "", 2));
		return parameters;
	}
	/**
	*Return the name of the procedure and the group to which belongs
	*/
	public String[] getstepinfo() {
		String[] info=new String[2];
		info[0]="4170";
		info[1]="4171";
		return info;
	}
}
