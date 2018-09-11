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
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.StepUtilities;

/**
* Create a toc for a path that contains several dictionaries
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class TocDataset implements RunStep
{
	/**
	*If set to false the result data set will be on the client.<p>
	*/
	/**
	*Create a data set with the information on several adams documents stored into a path
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase()};
		String [] optionalparameters=new String[] {Keywords.PATH.toLowerCase()};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);
		String path        =(String)parameters.get(Keywords.PATH.toLowerCase());
		String workdir     =(String)parameters.get(Keywords.WorkDir);
		if (path==null)
			path=workdir;
		if (path.toLowerCase().startsWith("http:"))
			return new Result("%1208% ("+path+")<br>\n", false, null);
		File dir = new File(path);
		String[] children = dir.list();
		Vector<String> dictnames=new Vector<String>();
		if (children == null)
		{
			return new Result("%443% ("+path+")<br>\n", false, null);
		}
		else
		{
			for (int i=0; i<children.length; i++)
			{
				String filename = children[i];
				if (filename.toLowerCase().endsWith(Keywords.DictionaryExtension.toLowerCase()))
					dictnames.add(filename);
			}
		}
		if (dictnames.size()==0)
			return new Result("%444% ("+path+")<br>\n", false, null);
		Vector<String[]> infodict=new Vector<String[]>();
		for (int i=0; i<dictnames.size(); i++)
		{
			String[] information=new String[7];
			information[0]=path;
			String dictn=dictnames.get(i);
			if (dictn.endsWith(Keywords.DictionaryExtension))
				dictn=dictn.substring(0, dictn.indexOf(Keywords.DictionaryExtension));
			information[1]=dictn;
			String dictfile=path+dictnames.get(i);
			DictionaryReader dr=new DictionaryReader(dictfile);
			information[2]=dr.getauthor();
			information[3]=dr.getkeyword();
			information[4]=dr.getdescription();
			information[5]=dr.getcreationdate();
			information[6]=dr.getdatatabletype();
			infodict.add(information);
		}
		String keyword=Keywords.TOC+" "+path;
		String description=Keywords.TOC+" "+path;
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());
		Vector<Hashtable<String, String>> fixedvariableinfo=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> codelabel=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> missingdata=new Vector<Hashtable<String, String>>();
		for (int i=0; i<7; i++)
		{
			String labelvar="";
			Hashtable<String, String> tempfixedvariableinfo=new Hashtable<String, String>();
			Hashtable<String, String> tempcodelabel=new Hashtable<String, String>();
			Hashtable<String, String> tempmissingdata=new Hashtable<String, String>();
			tempfixedvariableinfo.put(Keywords.VariableName.toLowerCase(),"v"+(String.valueOf(i)));
			tempfixedvariableinfo.put(Keywords.VariableFormat.toLowerCase(),Keywords.TEXTSuffix);
			if (i==0)
				labelvar="%1209%";
			if (i==1)
				labelvar="%1210%";
			if (i==2)
				labelvar="%1203%";
			if (i==3)
				labelvar="%1204%";
			if (i==4)
				labelvar="%1205%";
			if (i==6)
				labelvar="%1211%";
			if (i==5)
				labelvar="%1207%";
			tempfixedvariableinfo.put(Keywords.LabelOfVariable.toLowerCase(),labelvar);
			fixedvariableinfo.add(tempfixedvariableinfo);
			codelabel.add(tempcodelabel);
			missingdata.add(tempmissingdata);
		}
		boolean resopen=dw.opendatatable(fixedvariableinfo);
		if (!resopen)
			return new Result(dw.getmessage(), false, null);
		for (int i=0; i<infodict.size(); i++)
		{
			String[] tempinfo=infodict.get(i);
			dw.write(tempinfo);
		}
		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
		Hashtable<String, String> datatableinfo=dw.getTableInfo();
		Vector<StepResult> result = new Vector<StepResult>();
		result.add(new LocalDictionaryWriter(dw.getdictpath(), keyword, description, author, dw.gettabletype(),
		datatableinfo, fixedvariableinfo, tablevariableinfo, codelabel, missingdata, null));
		return new Result("", true, result);
	}
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.PATH.toLowerCase()+"=", "path", false, 445, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 440, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		return parameters;
	}
	public String[] getstepinfo()
	{
		String[] info=new String[2];
		info[0]="441";
		info[1]="442";
		return info;
	}
}
