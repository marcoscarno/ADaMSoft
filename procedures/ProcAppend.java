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

import ADaMSoft.utilities.GetRequiredParameters;

import ADaMSoft.keywords.Keywords;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import java.util.Enumeration;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that append a dataset to another if the old one exist otherwise it creates a new data set
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class ProcAppend implements RunStep
{
	/**
	* Starts the execution of Proc Append and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict+"n"};
		String [] optionalparameters=new String[] {Keywords.dict+"o", Keywords.replace};
		Keywords.percentage_total=3;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			Keywords.percentage_total=0;
			return new Result(steputilities.getMessage(), false, null);
		}
		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
		{
			return new Result(dw.getmessage(), false, null);
		}

		Keywords.percentage_done=1;

		String replace=(String)parameters.get(Keywords.replace);
		boolean delcl=false;
		boolean delmd=false;
		if (replace!=null)
		{
			if (replace.equalsIgnoreCase(Keywords.replaceall))
			{
				delcl=true;
				delmd=true;
			}
			else if (replace.equalsIgnoreCase(Keywords.replaceformat))
				delcl=true;
			else if (replace.equalsIgnoreCase(Keywords.replacemissing))
				delmd=true;
		}

		String keyword="Append";
		String description="Append";
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());;

		Vector<Hashtable<String, String>> fixedvariableinfo=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> codelabel=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> missingdata=new Vector<Hashtable<String, String>>();

		String[] listvarnames=null;
		int[] replacerule=null;
		boolean yetwritten=false;

		DictionaryReader dictn = (DictionaryReader)parameters.get(Keywords.dict+"n");

		if (parameters.get(Keywords.dict+"o")!=null)
		{
			Keywords.percentage_done=2;
			DictionaryReader dicto = (DictionaryReader)parameters.get(Keywords.dict+"o");
			keyword=keyword+" "+dicto.getkeyword();
			description=description+" "+dicto.getdescription();
			fixedvariableinfo=dicto.getfixedvariableinfo();
			if (!delcl)
				codelabel=dicto.getcodelabel();
			else
			{
				for (int i=0; i<fixedvariableinfo.size(); i++)
				{
					Hashtable<String, String> tempcl=new Hashtable<String, String>();
					codelabel.add(tempcl);
				}
			}
			if (!delmd)
				missingdata=dicto.getmissingdata();
			else
			{
				for (int i=0; i<fixedvariableinfo.size(); i++)
				{
					Hashtable<String, String> tempmd=new Hashtable<String, String>();
					missingdata.add(tempmd);
				}
			}
			replacerule=new int[fixedvariableinfo.size()];
			listvarnames=new String[fixedvariableinfo.size()];
			int rifrep=0;
			if (replace==null)
				rifrep=0;
			else if (replace.equalsIgnoreCase(Keywords.replaceall))
				rifrep=1;
			else if (replace.equalsIgnoreCase(Keywords.replaceformat))
				rifrep=2;
			else if (replace.equalsIgnoreCase(Keywords.replacemissing))
				rifrep=3;
			int nnvar=dictn.gettotalvar();
			boolean errorinvars=false;
			for (int i=0; i<fixedvariableinfo.size(); i++)
			{
				replacerule[i]=rifrep;
				listvarnames[i]=dicto.getvarname(i);
				boolean rescheck=false;
				for (int j=0; j<nnvar; j++)
				{
					String cnn=dictn.getvarname(j);
					if (listvarnames[i].equalsIgnoreCase(cnn))
						rescheck=true;
				}
				if (!rescheck)
					errorinvars=true;
			}
			if (errorinvars)
			{
				Keywords.percentage_total=0;
				Keywords.percentage_done=0;
				return new Result("%2422%<br>\n", false, null);
			}
			DataReader data = new DataReader(dicto);
			if (!data.open(listvarnames, replacerule, false))
			{
				Keywords.percentage_total=0;
				Keywords.percentage_done=0;
				return new Result(data.getmessage(), false, null);
			}
			if (!dw.opendatatable(fixedvariableinfo))
			{
				Keywords.percentage_total=0;
				Keywords.percentage_done=0;
				return new Result(dw.getmessage(), false, null);
			}
			yetwritten=true;
			String[] values=null;
			while (!data.isLast())
			{
				values = data.getRecord();
				dw.write(values);
			}
			data.close();
		}
		keyword=keyword+" "+dictn.getkeyword();
		description=description+" "+dictn.getdescription();
		if (!yetwritten)
		{
			Keywords.percentage_done=2;
			fixedvariableinfo=dictn.getfixedvariableinfo();
			if (!delcl)
				codelabel=dictn.getcodelabel();
			else
			{
				for (int i=0; i<fixedvariableinfo.size(); i++)
				{
					Hashtable<String, String> tempcl=new Hashtable<String, String>();
					codelabel.add(tempcl);
				}
			}
			if (!delmd)
				missingdata=dictn.getmissingdata();
			else
			{
				for (int i=0; i<fixedvariableinfo.size(); i++)
				{
					Hashtable<String, String> tempmd=new Hashtable<String, String>();
					missingdata.add(tempmd);
				}
			}
			replacerule=new int[fixedvariableinfo.size()];
			listvarnames=new String[fixedvariableinfo.size()];
			int rifrep=0;
			if (replace==null)
				rifrep=0;
			else if (replace.equalsIgnoreCase(Keywords.replaceall))
				rifrep=1;
			else if (replace.equalsIgnoreCase(Keywords.replaceformat))
				rifrep=2;
			else if (replace.equalsIgnoreCase(Keywords.replacemissing))
				rifrep=3;
			for (int i=0; i<fixedvariableinfo.size(); i++)
			{
				replacerule[i]=rifrep;
				listvarnames[i]=dictn.getvarname(i);
			}
			DataReader data = new DataReader(dictn);
			if (!data.open(listvarnames, replacerule, false))
			{
				Keywords.percentage_total=0;
				Keywords.percentage_done=0;
				return new Result(data.getmessage(), false, null);
			}
			if (!dw.opendatatable(fixedvariableinfo))
			{
				Keywords.percentage_total=0;
				Keywords.percentage_done=0;
				return new Result(dw.getmessage(), false, null);
			}
			String[] values=null;
			while (!data.isLast())
			{
				values = data.getRecord();
				dw.write(values);
			}
			data.close();
		}
		else
		{
			if (!delcl)
			{
				for (int i=0; i<listvarnames.length; i++)
				{
					Hashtable<String, String> tempcln=dictn.getcodelabelfromname(listvarnames[i]);
					Hashtable<String, String> tempclo=codelabel.get(i);
					for (Enumeration<String> en=tempcln.keys(); en.hasMoreElements();)
					{
						String co = en.nextElement();
						String la = tempcln.get(co);
						tempclo.put(co, la);
					}
					codelabel.set(i, tempclo);
				}
			}
			if (!delmd)
			{
				for (int i=0; i<listvarnames.length; i++)
				{
					Hashtable<String, String> tempcln=dictn.getmissingdatafromname(listvarnames[i]);
					Hashtable<String, String> tempclo=missingdata.get(i);
					for (Enumeration<String> en=tempcln.keys(); en.hasMoreElements();)
					{
						String co = en.nextElement();
						String la = tempcln.get(co);
						tempclo.put(co, la);
					}
					missingdata.set(i, tempclo);
				}
			}
			DataReader data = new DataReader(dictn);
			if (!data.open(listvarnames, replacerule, false))
			{
				Keywords.percentage_total=0;
				Keywords.percentage_done=0;
				return new Result(data.getmessage(), false, null);
			}
			String[] values=null;
			while (!data.isLast())
			{
				values = data.getRecord();
				dw.write(values);
			}
			data.close();
		}
		Keywords.percentage_done=0;
		Keywords.percentage_total=0;

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
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"o=", "freedict", false, 2424, dep, "", 1));
		parameters.add(new GetRequiredParameters("","note", false, 2426, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"n=", "dict", true, 2425, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 440, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="4162";
		retprocinfo[1]="2423";
		return retprocinfo;
	}
}
