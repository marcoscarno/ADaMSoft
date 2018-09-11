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

/**
* This is the procedure that reads the documents with path specified in a data set
* @author marco.scarno@gmail.com
* @date 20/07/2017
*/
public class ProcMultidocreader implements RunStep
{
	boolean cases;
	int maxvalue, minvalue;
	/**
	*Evaluate the frequencies of each word
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Vector<StepResult> result = new Vector<StepResult>();
		String[] requiredparameters = new String[] {Keywords.dict, Keywords.varpathfiles, Keywords.OUT.toLowerCase()};
		String[] optionalparameters = new String[] {Keywords.vargroupby, Keywords.casesensitive,
		Keywords.nonumbers, Keywords.onlyascii, Keywords.charstoreplace, Keywords.charstodelete, Keywords.charstosubwspace};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		String varpathfiles=(String) parameters.get(Keywords.varpathfiles);
		String tvargroupby=(String) parameters.get(Keywords.vargroupby);
		String[] tvarpathfiles=varpathfiles.split(" ");
		if (tvarpathfiles.length!=1)
		{
			return new Result("%3472% ("+varpathfiles+")<br>\n", false, null);
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
		TreeMap<String, Vector<String>> info_docs=new TreeMap<String, Vector<String>>();
		String[] values=null;
		DataReader data = new DataReader(dict);
		if (!data.open(null, 1, false))
		{
			return new Result(data.getmessage(), false, null);
		}
		String gfile="";
		String fname="";
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				fname=values[posvp];
				gfile=fname;
				if (tvargroupby!=null)
				{
					gfile="";
					for (int i=0; i<posvb.length; i++)
					{
						gfile=gfile+values[posvb[i]];
						if (i<posvb.length-1)
						{
							gfile=gfile+"-";
						}
					}
				}
				if (info_docs.get(gfile)==null)
				{
					Vector<String> tempv=new Vector<String>();
					tempv.add(fname);
					info_docs.put(gfile, tempv);
				}
				else
				{
					Vector<String> tempv=info_docs.get(gfile);
					if (!tempv.contains(fname)) tempv.add(fname);
					info_docs.put(gfile, tempv);
				}
			}
		}
		data.close();
		if (info_docs.size()==0)
		{
			return new Result("%3490%<br>\n", false, null);
		}
		boolean onlyascii = (parameters.get(Keywords.onlyascii) != null);
		boolean nonumbers = (parameters.get(Keywords.nonumbers) != null);
		boolean casesensitive = (parameters.get(Keywords.casesensitive) != null);
		String chartoreplace = (String) parameters.get(Keywords.charstoreplace);
		String chartodelete = (String) parameters.get(Keywords.charstodelete);
		String chartosubwspace = (String) parameters.get(Keywords.charstosubwspace);
		String[] charstoreplace=new String[0];
		String[] charstodelete=new String[0];
		String[] charstosubwspace=new String[0];
		if (chartoreplace!=null) charstoreplace=chartoreplace.split(";");
		if (chartodelete!=null) charstodelete=chartodelete.split(" ",-1);
		if (chartosubwspace!=null) charstosubwspace=chartosubwspace.split(" ", -1);
		DataWriter dw = new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
		{
			return new Result(dw.getmessage(), false, null);
		}

		DataSetUtilities dsu=new DataSetUtilities();
		String keyword = "Multi word counter " + dict.getkeyword();
		String description = "Multi word counter " + dict.getdescription();
		String author = (String) parameters.get(Keywords.client_host.toLowerCase());
		String tempdir=(String)parameters.get(Keywords.WorkDir);

		Hashtable<String, String> tempmd=new Hashtable<String, String>();
		dsu.addnewvar("document_reference", "%4183%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("document_content", "%4184%", Keywords.TEXTSuffix, tempmd, tempmd);
		boolean resopen = dw.opendatatable(dsu.getfinalvarinfo());
		if (!resopen)
		{
			result.add(new LocalMessageGetter(dw.getmessage()));
			return new Result("", false, result);
		}
		TikaParser tp=new TikaParser();
		tp.setWorkindDir(tempdir);
		tp.setCases(casesensitive);
		tp.setCharstodelete(charstodelete);
		tp.setCharstosubwspace(charstosubwspace);
		tp.setCharstoreplace(charstoreplace);
		tp.setOnlyascii(onlyascii);
		tp.setNonumbers(nonumbers);
		Iterator<String> it = info_docs.keySet().iterator();
		String[] outvalues=new String[2];
		while (it.hasNext())
		{
			String key = it.next();
			outvalues[0]=key;
			outvalues[1]="";
			Vector<String> fnames=info_docs.get(key);
			{
				for (int i=0; i<fnames.size(); i++)
				{
					fname=fnames.get(i);
					tp.parseFile(fname);
					if (!tp.getErrorParsing())
					{
						outvalues[1]=outvalues[1]+" "+tp.getContent();
					}
					else
					{
						result.add(new LocalMessageGetter(fname+": "+tp.getMsgErrorParsing()));
					}
				}
			}
			dw.write(outvalues);
		}
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
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 3470, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 440, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.varpathfiles, "var=all", true, 3482, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroupby, "vars=all", false, 3484, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 3485, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.casesensitive, "checkbox", false, 467, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.charstodelete, "text", false, 3333, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.charstosubwspace, "text", false, 3426, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.charstoreplace, "longtext", false, 3334, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 3329, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 3335, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.onlyascii, "checkbox", false, 3341, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.nonumbers, "checkbox", false, 3342, dep, "", 2));
		return parameters;
	}
	/**
	*Return the name of the procedure and the group to which belongs
	*/
	public String[] getstepinfo() {
		String[] info=new String[2];
		info[0]="4170";
		info[1]="4185";
		return info;
	}
}
