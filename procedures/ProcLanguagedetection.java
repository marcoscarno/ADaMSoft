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
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.File;

import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.dataaccess.FastTempDataSet;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.VariableUtilities;

/**
* This is the procedure that adds an index able to identify the language from some treetagger parameter files
* @author marco.scarno@gmail.com
* @date 15/02/2018
*/
public class ProcLanguagedetection implements RunStep
{
	/**
	*Evaluate the index able to identify the language from some treetagger parameter files
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String[] requiredparameters = new String[] {Keywords.dict, Keywords.var, Keywords.OUT.toLowerCase(), Keywords.trettaggerexe, Keywords.parameterfile};
		String[] optionalparameters = new String[] {Keywords.onlyascii};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		boolean onlyascii = (parameters.get(Keywords.onlyascii) != null);
		String tempvar=(String)parameters.get(Keywords.var.toLowerCase());
		String[] test=tempvar.split(" ");
		if (test.length!=1)
			return new Result("%3349%<br>\n", false, null);
		int position_var=-1;
		String[] var=new String[dict.gettotalvar()];
		for (int i=0; i<dict.gettotalvar(); i++)
		{
			String tempname=dict.getvarname(i);
			var[i]=tempname;
			if (tempname.equalsIgnoreCase(tempvar)) position_var=i;
		}
		if (position_var==-1)
		{
			return new Result("%4250%<br>\n", false, null);
		}
		DataWriter dw = new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);
		String trettaggerexe = (String) parameters.get(Keywords.trettaggerexe);
		try
		{
			trettaggerexe=trettaggerexe.replaceAll("\\\\","/");
		}
		catch (Exception fs){}
		boolean existfile=new File(trettaggerexe).exists();
		if (!existfile)
			return new Result("%4239% ("+trettaggerexe+")<br>\n", false, null);
		String parameterfile = (String) parameters.get(Keywords.parameterfile);
		Vector<String> list_parameters=new Vector<String>();
		Vector<String> file_encoding=new Vector<String>();
		DataSetUtilities dsu=new DataSetUtilities();
		int dimnewvars=0;
		dsu.setreplace(null);
		Hashtable<String, String> temph=new Hashtable<String, String>();
		dsu.defineolddict(dict);
		String parameter_not="";
		try
		{
			String[] list_par_files=parameterfile.split(";");
			for (int i=0; i<list_par_files.length; i++)
			{
				String[] type_parameters=list_par_files[i].split("=");
				try
				{
					type_parameters[0]=type_parameters[0].replaceAll("\\\\","/");
				}
				catch (Exception fs){}
				list_parameters.add(type_parameters[0]);
				existfile=new File(type_parameters[0]).exists();
				if (existfile)
				{
					File fp=new File(type_parameters[0]);
					dsu.addnewvartoolddict("index_language_"+String.valueOf(i), "%4240% "+fp.getName(), Keywords.TEXTSuffix, temph, temph);
				}
				if (!existfile)
					parameter_not=parameter_not+"%4237% ("+type_parameters[0]+")<br>";
				if (type_parameters.length>1)
					file_encoding.add(type_parameters[1]);
				else
					file_encoding.add("");

			}
		}
		catch (Exception ep)
		{
			return new Result("%4236%<br>\n", false, null);
		}
		if (!parameter_not.equals(""))
			return new Result("%4238%<br>"+parameter_not, false, null);
		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);
		String[] newvalues=new String[list_parameters.size()];
		String[] wvalues=null;
		String tempdir=(String)parameters.get(Keywords.WorkDir);
		String outfile=tempdir+"outfile_";
		String infile=tempdir+"infile_";
		Keywords.percentage_total=100;
		Keywords.percentage_done=1;
		DataReader data = new DataReader(dict);
		if (!data.open(var, 0, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		BufferedWriter[] bw = new BufferedWriter[list_parameters.size()];
		for (int i=0; i<list_parameters.size(); i++)
		{
			try
			{
				if (!file_encoding.get(i).equals(""))
					bw[i]=new BufferedWriter(new OutputStreamWriter (new FileOutputStream (outfile+String.valueOf(i)+".txt") , file_encoding.get(i))) ;
				else
					bw[i]=new BufferedWriter(new FileWriter(outfile+String.valueOf(i)+".txt"));
				bw[i].write("Record_0"+"\n");
			}
			catch (Exception e)
			{
				return new Result("%4280%<br>"+e.toString()+"\n", false, null);
			}
		}
		String[] commandstring=new String[7];
		commandstring[0]=trettaggerexe;
		commandstring[1]="-token";
		commandstring[2]="-quiet";
		commandstring[3]="-lemma";
		String line="";
		String inps="";
		String errs="";
		int les1sec=1;
		double current_obs=0;
		double total_obs=0;
		String msg="";
		String[] values;
		String[] res_tree;
		String current_record;
		StringBuilder start=new StringBuilder();
		StringBuilder end=new StringBuilder();
		int sz;
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}
		int cr=0;
		String[] terms;
		try
		{
			while (!data.isLast())
			{
				values = data.getRecord();
				if (values!=null)
				{
					current_record=values[position_var];
					if (!current_record.equals(""))
					{
						if (onlyascii)
						{
							start=new StringBuilder();
							start.append(current_record);
							end=new StringBuilder();
							sz = start.length();
							for (int i = 0; i < sz; i++)
							{
								if (start.charAt(i) >= 97 && start.charAt(i) <= 122) end.append(start.charAt(i));
								else if (start.charAt(i) >= 65 && start.charAt(i) <= 90) end.append(start.charAt(i));
								else if (start.charAt(i) >= 224 && start.charAt(i) <= 246) end.append(start.charAt(i));
								else if (start.charAt(i) >= 249 && start.charAt(i) <= 255) end.append(start.charAt(i));
								else end.append(" ");
							}
							current_record=end.toString();
							start=null;
						}
						current_record=current_record.replaceAll("\\s+"," ");
						terms=current_record.split(" ");
						cr++;
						for (int i=0; i<list_parameters.size(); i++)
						{
							for (int j=0; j<terms.length; j++)
							{
								bw[i].write(terms[j]+"\n");
							}
							bw[i].write("Record_"+String.valueOf(cr)+"\n");
							bw[i].flush();
						}
					}
				}
			}
			data.close();
		}
		catch (Exception ep)
		{
			return new Result("%4241%<br>"+ep.toString()+"<br>", false, null);
		}
		for (int i=0; i<list_parameters.size(); i++)
		{
			try
			{
				bw[i].flush();
				bw[i].close();
			}
			catch (Exception e)
			{
				return new Result("%4280%<br>"+e.toString()+"\n", false, null);
			}
		}
		for (int i=0; i<list_parameters.size(); i++)
		{
			try
			{
				commandstring[4]=list_parameters.get(i);
				commandstring[5]=outfile+String.valueOf(i)+".txt";
				commandstring[6]=infile+String.valueOf(i)+".txt";
				Process p = Runtime.getRuntime().exec(commandstring);
				BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
				BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				line="";
				inps="";
				errs="";
				while ((line = bri.readLine()) != null)
				{
					inps=inps+line+"<br>";
				}
				bri.close();
				while ((line = bre.readLine()) != null)
				{
					errs=errs+line+"<br>";
				}
				bre.close();
				p.waitFor();
				existfile=new File(infile+String.valueOf(i)+".txt").exists();
				les1sec=1;
				while (!existfile && les1sec<200)
				{
					try
					{
						Thread.sleep(100);
					}
					catch (Exception e){}
					existfile=(new File(infile+String.valueOf(i)+".txt")).exists();
					les1sec++;
				}
				if (!existfile)
				{
					if (!inps.trim().equals("")) msg=msg+inps.trim()+"<br>";
					if (!errs.trim().equals("")) msg=msg.trim()+""+errs.trim()+"<br>";
					if (msg.equals("")) msg="%3355%";
						return new Result("%3354%: "+msg.trim()+"<br>\n", false, null);
				}
			}
			catch (Exception e)
			{
				return new Result("%4241%<br>"+e.toString()+"\n", false, null);
			}
		}
		BufferedReader[] bufferedReader=new BufferedReader[list_parameters.size()];
		for (int i=0; i<list_parameters.size(); i++)
		{
			try
			{
				(new File(outfile+String.valueOf(i)+".txt")).delete();
				if (file_encoding.get(i).equals(""))
					bufferedReader[i] = new BufferedReader(new FileReader(infile+String.valueOf(i)+".txt"));
				else
					bufferedReader[i] = new BufferedReader(new InputStreamReader(new FileInputStream(infile+String.valueOf(i)+".txt"), file_encoding.get(i)));
			}
			catch (Exception e)
			{
				return new Result("%4280%<br>"+e.toString()+"\n", false, null);
			}
		}
		if (!data.open(var, 0, false))
			return new Result(data.getmessage(), false, null);
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}
		cr=1;
		try
		{
			while (!data.isLast())
			{
				values = data.getRecord();
				if (values!=null)
				{
					current_record=values[position_var];
					for (int i=0; i<list_parameters.size(); i++)
					{
						line="";
						current_obs=0;
						total_obs=0;
						while((line = bufferedReader[i].readLine()) != null && !line.startsWith("Record_"+String.valueOf(cr)))
						{
							res_tree=line.split("\\t");
							total_obs++;
							if (!res_tree[res_tree.length-1].equalsIgnoreCase("<unknown>")) current_obs++;
						}
						current_obs=100*current_obs/total_obs;
						newvalues[i]=String.valueOf(current_obs);
					}
					cr++;
					wvalues=dsu.getnewvalues(values, newvalues);
					dw.write(wvalues);
				}
			}
			data.close();
		}
		catch (Exception ep)
		{
			return new Result("%4241%<br>"+ep.toString()+"<br>", false, null);
		}
		for (int i=0; i<list_parameters.size(); i++)
		{
			try
			{
				bufferedReader[i].close();
				(new File(infile+String.valueOf(i)+".txt")).delete();
			}
			catch (Exception e)
			{
				return new Result("%4280%<br>"+e.toString()+"\n", false, null);
			}
		}
		Keywords.percentage_done=100;
		Vector<StepResult> result = new Vector<StepResult>();
		String keyword="Language detection "+dict.getkeyword();
		String description="Language detection "+dict.getdescription();
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());
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
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 4242, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 4243, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.var, "var=all", true, 4244, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.trettaggerexe,"file=all", true, 3366,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.parameterfile,"multipletext", true, 4245,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 4246, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 4247, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 4248, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.onlyascii, "checkbox", false, 3341, dep, "", 2));
		return parameters;
	}
	/**
	*Return the name of the procedure and the group to which belongs
	*/
	public String[] getstepinfo() {
		String[] info=new String[2];
		info[0]="4168";
		info[1]="4249";
		return info;
	}
}
