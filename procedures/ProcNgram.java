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
import java.util.Set;
import java.util.Vector;
import java.net.URL;

import org.apache.tika.Tika;
import org.apache.tika.language.LanguageIdentifier;
import org.apache.tika.metadata.Metadata;

import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that writes the ngrams from a document
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcNgram implements RunStep
{
	boolean cases;
	int maxvalue, minvalue;
	TreeMap<String, Integer> output;
	int nvalue;
	/**
	*Evaluate the frequencies of each word
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean noout=false;
		Vector<StepResult> result = new Vector<StepResult>();
		String[] requiredparameters = new String[] {Keywords.OUT.toLowerCase(), Keywords.infile, Keywords.nvalue};
		String[] optionalparameters = new String[] {Keywords.casesensitive, Keywords.usepercfreq, Keywords.withchars, Keywords.charstosubwspace, Keywords.nonumbers, Keywords.onlyascii, Keywords.charstoreplace, Keywords.charstodelete};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		boolean onlyascii = (parameters.get(Keywords.onlyascii) != null);
		boolean nonumbers = (parameters.get(Keywords.nonumbers) != null);
		boolean usepercfreq = (parameters.get(Keywords.usepercfreq) != null);
		DataWriter dw = new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);
		String filename = (String) parameters.get(Keywords.infile);
		String snvalue=(String) parameters.get(Keywords.nvalue);

		Keywords.percentage_total=100;
		Keywords.percentage_done=0;

		nvalue=0;
		try
		{
			nvalue=Integer.parseInt(snvalue);
		}
		catch (Exception e)
		{
			nvalue=0;
		}
		if (nvalue<2)
			return new Result("%3347% ("+snvalue+")<br>\n", false, null);

		String chartoreplace = (String) parameters.get(Keywords.charstoreplace);
		String chartodelete = (String) parameters.get(Keywords.charstodelete);
		String chartosubwspace = (String) parameters.get(Keywords.charstosubwspace);
		String[] charstoreplace=null;
		String[] charstodelete=null;
		String[] charstosubwspace=null;
		if (chartoreplace!=null) charstoreplace=chartoreplace.split(";");
		if (chartodelete!=null) charstodelete=chartodelete.split(" ");
		if (chartosubwspace!=null) charstosubwspace=chartosubwspace.split(" ");
		output = new TreeMap<String, Integer>();
		cases = (parameters.get(Keywords.casesensitive) != null);
		boolean usechars = (parameters.get(Keywords.withchars) != null);
		String content="";
		DataSetUtilities dsu=new DataSetUtilities();
		String keyword = filename;
		String description = "NGram for " + filename;
		String author = (String) parameters.get(Keywords.client_host.toLowerCase());
		try
		{
			File file = new File(filename);
			URL url = file.toURI().toURL();
			Tika tika = new Tika();
			content = tika.parseToString(file);
			LanguageIdentifier ld = new LanguageIdentifier(content);
			result.add(new LocalMessageGetter("%3326%: "+file+"<br>\n"));
			result.add(new LocalMessageGetter("%3327%: "+ld.getLanguage()+"<br>\n"));
			Metadata met = new Metadata();
			tika.parse(url.openStream(), met);
			result.add(new LocalMessageGetter("%3328%: "+met.get(Metadata.CONTENT_TYPE)+"\n"));
			content=content.replaceAll("\r"," ");
			content=content.replaceAll("\n"," ");
			content=content.replaceAll("\t"," ");
			if (cases) content = content.toLowerCase();
			int charSeparator=0;
			int actualchar=0;
			if (charstodelete!=null)
			{
				for (int i=0; i<charstodelete.length; i++)
				{
					try
					{
						String newcontent="";
						charSeparator = Integer.parseInt(charstodelete[i]);
						for (int j=0; j<content.length(); j++)
						{
							actualchar=content.codePointAt(j);
							if (actualchar!=charSeparator) newcontent=newcontent+Character.toString(content.charAt(j));
						}
						content=newcontent;
						newcontent="";
					}
					catch (Exception ee)
					{
						result.add(new LocalMessageGetter("%3336%: "+charstodelete[i]+"<br>\n"));
					}
				}
			}
			if (charstosubwspace!=null)
			{
				for (int i=0; i<charstosubwspace.length; i++)
				{
					try
					{
						String newcontent="";
						charSeparator = Integer.parseInt(charstosubwspace[i]);
						for (int j=0; j<content.length(); j++)
						{
							actualchar=content.codePointAt(j);
							if (actualchar!=charSeparator) newcontent=newcontent+Character.toString(content.charAt(j));
							else newcontent=newcontent+" ";
						}
						content=newcontent;
						newcontent="";
					}
					catch (Exception ee)
					{
						result.add(new LocalMessageGetter("%3427%: "+charstosubwspace[i]+"<br>\n"));
					}
				}
			}
			if (charstoreplace!=null)
			{
				String[] parts1=null;
				String[] parts2=null;
				String[] parts3=null;
				StringBuilder from=null;
				StringBuilder to=null;
				StringBuilder orig=new StringBuilder();
				for (int j=0; j<content.length(); j++)
				{
					orig.append(content.substring(j,j+1));
				}
				boolean errorinrep=false;
				int index=0;
				for (int i=0; i<charstoreplace.length; i++)
				{
					errorinrep=false;
					try
					{
						parts1=charstoreplace[i].split("=");
						if (parts1.length==2)
						{
							parts2=parts1[0].split(" ");
							parts3=parts1[1].split(" ");
							from=new StringBuilder();
							to=new StringBuilder();
							for (int j=0; j<parts2.length; j++)
							{
								if (!parts2[j].equals(""))
								{
									try
									{
										charSeparator = Integer.parseInt(parts2[j]);
										from.append((char)charSeparator);
									}
									catch (Exception ee)
									{
										errorinrep=true;
									}
								}
							}
							for (int j=0; j<parts3.length; j++)
							{
								if (!parts3[j].equals(""))
								{
									try
									{
										charSeparator = Integer.parseInt(parts3[j]);
										to.append((char)charSeparator);
									}
									catch (Exception ee)
									{
										errorinrep=true;
									}
								}
							}
							if (!errorinrep)
							{
								index = orig.indexOf(from.toString());
								while (index != -1)
								{
									orig.replace(index, index + from.length(), to.toString());
									index += to.length();
									index = orig.indexOf(from.toString(), index);
								}
							}
						}
						else errorinrep=true;
					}
					catch (Exception ee)
					{
						result.add(new LocalMessageGetter("%3337%: "+charstoreplace[i]+"<br>\n"));
					}
					if (errorinrep) result.add(new LocalMessageGetter("%3337%: "+charstoreplace[i]+"<br>\n"));
				}
				content=orig.toString();
			}
			content=content.replaceAll("\\s+"," ");
			int sz = content.length();
			String defcontent="";
			boolean consider=true;
			for (int i = 0; i < sz; i++)
			{
				consider=true;
				if (content.codePointAt(i)==65533) consider=false;
				if (onlyascii && (!isAscii(content.charAt(i)))) consider=false;
				if (nonumbers && (isNumber(content.charAt(i)))) consider=false;
				if (consider) defcontent=defcontent+content.charAt(i);
			}
			int first=0;
			int last=nvalue;
			String ngramm="";
			double totngrams=0;
			while(last<defcontent.length())
			{
				totngrams++;
				ngramm = defcontent.substring(first,last);
				first++;last++;
				Integer frequency = output.get(ngramm);
				if(frequency == null)
					output.put(ngramm,new Integer(1));
				else
					output.put(ngramm, frequency+1);
			}
			Hashtable<String, String> tempmd=new Hashtable<String, String>();
			dsu.addnewvar("ngram", "%1139%", Keywords.TEXTSuffix, tempmd, tempmd);
			if (usechars)
				dsu.addnewvar("ascii_codes", "%1140%", Keywords.TEXTSuffix, tempmd, tempmd);
			if (!usepercfreq)
				dsu.addnewvar("freq", "%1141%", Keywords.NUMSuffix+Keywords.INTSuffix, tempmd, tempmd);
			else
				dsu.addnewvar("freq", "%1141%", Keywords.NUMSuffix, tempmd, tempmd);
			boolean resopen = dw.opendatatable(dsu.getfinalvarinfo());
			if (!resopen)
				return new Result(dw.getmessage(), false, null);
			int def_lun=2;
			if (usechars) def_lun++;
			String[] out_val=new String[def_lun];
			Set<String> s = output.keySet();
			Iterator<String> it = s.iterator();
			int written=0;
			int tempint=0;
			Keywords.percentage_total=output.size();
			Keywords.percentage_done=0;
			while (it.hasNext())
			{
				written++;
				Keywords.percentage_done=written;
				String key = it.next();
				tempint = (output.get(key)).intValue();
				out_val[0]=key;
				if (usechars)
				{
					out_val[1]="";
					for (int i = 0; i < key.length(); i++)
					{
						out_val[1] += key.codePointAt(i) + " ";
					}
					if (!usepercfreq)
						out_val[2]=String.valueOf(tempint);
					else
						out_val[2]=String.valueOf(100*tempint/totngrams);
				}
				else
				{
					if (!usepercfreq)
						out_val[1]=String.valueOf(tempint);
					else
						out_val[1]=String.valueOf(100*tempint/totngrams);
				}
				dw.write(out_val);
			}
			if (written==0)
			{
				dw.deletetmp();
				result.add(new LocalMessageGetter("%3340%\n"));
				noout=true;
			}
		}
		catch (Exception e)
		{
			return new Result("%3332%<br>\n"+e.toString()+"<br>\n", false, null);
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
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 440, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.infile, "file=all", true,  3381, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.nvalue, "text", true, 469, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.casesensitive, "checkbox", false, 467, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.withchars, "checkbox", false, 468, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.charstodelete, "text", false, 3333, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.charstosubwspace, "text", false, 3426, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.charstoreplace, "longtext", false, 3334, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 3329, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 3335, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.onlyascii, "checkbox", false, 3341, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.nonumbers, "checkbox", false, 3342, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.usepercfreq, "checkbox", false, 3348, dep, "", 2));
		return parameters;
	}
	/**
	*Return the name of the procedure and the group to which belongs
	*/
	public String[] getstepinfo() {
		String[] info=new String[2];
		info[0]="4176";
		info[1]="465";
		return info;
	}
	/**
	*Return true if it is a printable ascii char
	*/
	private boolean isAscii(char ch)
	{
		return ch >= 32 && ch < 127;
	}
	/**
	*Return true if it is a number
	*/
	private boolean isNumber(char ch)
	{
		return ch >= 48 && ch < 57;
	}
}
