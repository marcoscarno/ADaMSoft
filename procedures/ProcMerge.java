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

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that merges two datasets
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcMerge implements RunStep
{
	/**
	* Starts the execution of Proc Merge and returns the corresponding message
	*/
	@SuppressWarnings("unused")
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict+"a", Keywords.dict+"b"};
		String [] optionalparameters=new String[] {Keywords.force, Keywords.replace};
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

		DictionaryReader dicta = (DictionaryReader)parameters.get(Keywords.dict+"a");
		DictionaryReader dictb = (DictionaryReader)parameters.get(Keywords.dict+"b");
		String replace=(String)parameters.get(Keywords.replace);
		boolean force=(parameters.get(Keywords.force)!=null);
		String keyword="Merge "+dicta.getkeyword()+" "+dictb.getkeyword();
		String description="Merge "+dicta.getdescription()+" "+dictb.getdescription();
		String author=(String)parameters.get(Keywords.client_host.toLowerCase());

		int[] repa=new int[dicta.gettotalvar()];
		int[] repb=new int[dictb.gettotalvar()];

		int rifrep=0;
		if (replace==null)
			rifrep=0;
		else if (replace.equalsIgnoreCase(Keywords.replaceall))
			rifrep=1;
		else if (replace.equalsIgnoreCase(Keywords.replaceformat))
			rifrep=2;
		else if (replace.equalsIgnoreCase(Keywords.replacemissing))
			rifrep=3;

		if (!force)
		{
			boolean commonvars=false;
			for (int i=0; i<dicta.gettotalvar(); i++)
			{
				String tempnamea=dicta.getvarname(i);
				for (int j=0; j<dictb.gettotalvar(); j++)
				{
					String tempnameb=dictb.getvarname(j);
					if (tempnamea.equalsIgnoreCase(tempnameb))
						commonvars=true;
				}
			}
			if (commonvars)
				return new Result("%1795%<br>\n", false, null);
		}

		DataSetUtilities dsu=new DataSetUtilities();
		int pointvar=0;
		for (int i=0; i<dicta.gettotalvar(); i++)
		{
			repa[i]=0;
			String vname=dicta.getvarname(i);
			Hashtable<String, String> cl=dicta.getcodelabelfromname(vname);
			Hashtable<String, String> md=dicta.getmissingdatafromname(vname);
			if (rifrep==1)
			{
				repa[i]=1;
				cl.clear();
				md.clear();
			}
			if (rifrep==2)
			{
				repa[i]=2;
				cl.clear();
			}
			if (rifrep==3)
			{
				repa[i]=3;
				md.clear();
			}
			if (force)
			{
				vname="dicta_"+vname;
				pointvar++;
			}
			dsu.addnewvar(vname, dicta.getvarlabel(i), dicta.getvarformat(i), cl, md);
		}
		for (int i=0; i<dictb.gettotalvar(); i++)
		{
			repb[i]=0;
			String vname=dictb.getvarname(i);
			Hashtable<String, String> cl=dictb.getcodelabelfromname(vname);
			Hashtable<String, String> md=dictb.getmissingdatafromname(vname);
			if (rifrep==1)
			{
				repb[i]=1;
				cl.clear();
				md.clear();
			}
			if (rifrep==2)
			{
				repb[i]=1;
				cl.clear();
			}
			if (rifrep==3)
			{
				repb[i]=1;
				md.clear();
			}
			if (force)
			{
				vname="dictb_"+vname;
				pointvar++;
			}
			dsu.addnewvar(vname, dictb.getvarlabel(i), dictb.getvarformat(i), cl, md);
		}

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		DataReader dataa = new DataReader(dicta);
		DataReader datab = new DataReader(dictb);

		if (!dataa.open(null, repa, false))
			return new Result(dataa.getmessage(), false, null);

		if (!datab.open(null, repb, false))
			return new Result(datab.getmessage(), false, null);

		String[] valuestowrite=new String[dicta.gettotalvar()+dictb.gettotalvar()];

		int pointa=dicta.gettotalvar();

		boolean toread=true;
		while (toread)
		{
			for (int i=0; i<valuestowrite.length; i++)
				valuestowrite[i]="";
			if (!dataa.isLast())
			{
				String[] values = dataa.getRecord();
				for (int i=0; i<values.length; i++)
					valuestowrite[i]=values[i];
			}
			if (!datab.isLast())
			{
				String[] values = datab.getRecord();
				for (int i=0; i<values.length; i++)
					valuestowrite[i+pointa]=values[i];
			}
			dw.write(valuestowrite);
			if ( (dataa.isLast()) && (datab.isLast()))
				toread=false;
		}
		dataa.close();
		datab.close();

		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
		Hashtable<String, String> datatableinfo=dw.getTableInfo();
		Vector<StepResult> result = new Vector<StepResult>();
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
		parameters.add(new GetRequiredParameters(Keywords.dict+"a=", "dict", true, 536, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.dict+"b=", "dict", true, 537, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 440, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.force, "checkbox", false, 1794, dep, "", 2));
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
		retprocinfo[1]="1793";
		return retprocinfo;
	}
}
