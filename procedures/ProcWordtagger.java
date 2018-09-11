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
* This is the procedure that tags the different words by using TreeTagger
* @author marco.scarno@gmail.com
* @date 15/02/2018
*/
public class ProcWordtagger implements RunStep
{
	/**
	*Evaluate the frequencies of each word
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String[] requiredparameters = new String[] {Keywords.dict, Keywords.var, Keywords.OUT.toLowerCase(), Keywords.trettaggerexe, Keywords.parameterfile};
		String[] optionalparameters = new String[] {Keywords.addprob, Keywords.secparameterfile, Keywords.file_encoding, Keywords.maxterms};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		boolean addprob = (parameters.get(Keywords.addprob) != null);
		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String tempvar=(String)parameters.get(Keywords.var.toLowerCase());
		String[] test=tempvar.split(" ");
		if (test.length!=1)
			return new Result("%3349%<br>\n", false, null);

		DataWriter dw = new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);
		String trettaggerexe = (String) parameters.get(Keywords.trettaggerexe);
		String parameterfile = (String) parameters.get(Keywords.parameterfile);
		String secparameterfile=(String) parameters.get(Keywords.secparameterfile);
		String file_encoding=(String) parameters.get(Keywords.file_encoding);

		String tempmaxwords =(String)parameters.get(Keywords.maxterms);
		int maxterms=0;
		if (tempmaxwords!=null)
		{
			try
			{
				maxterms=Integer.parseInt(tempmaxwords);
			}
			catch (Exception e)
			{
				maxterms=-1;
			}
			if (maxterms<0)
				return new Result("%4105%<br>\n", false, null);
		}

		String tempdir=(String)parameters.get(Keywords.WorkDir);
		String outfile=tempdir+"outfile";
		String infile=tempdir+"infile";
		String secinfile=tempdir+"secinfile";
		boolean existfile=new File(outfile+"_1.txt").exists();
		boolean deletedfiles=true;
		if (existfile) deletedfiles=new File(outfile+"_1.txt").delete();
		if (!deletedfiles)
			return new Result("%3350% ("+outfile+"_1.txt<br>", false, null);
		existfile=new File(infile+"_1.txt").exists();
		if (existfile) deletedfiles=new File(infile+"_1.txt").delete();
		if (!deletedfiles)
			return new Result("%3350% ("+infile+"_1.txt)<br>", false, null);

		if (secparameterfile!=null)
		{
			existfile=new File(secinfile+"_1.txt").exists();
			if (existfile) deletedfiles=new File(secinfile+"_1.txt").delete();
			if (!deletedfiles)
				return new Result("%3350% ("+secinfile+"_1.txt)<br>", false, null);
		}

		VariableUtilities varu=new VariableUtilities(dict, null, tempvar, null, null, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);
		String[] var=varu.getanalysisvar();
		DataReader data = new DataReader(dict);
		if (!data.open(var, 0, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}
		Keywords.percentage_total=100;
		Keywords.percentage_done=1;
		int validgroup=0;
		BufferedWriter bw = null;
		int n_parts=1;
		int totdatarec=0;
		int current_maxterms=0;
		int ref_outf=1;
		boolean multiple=false;
		int total_terms=0;
		String[] parts=null;
		String[] values=null;
		boolean are_missing=false;
		try
		{
			if (file_encoding!=null)
				bw=new BufferedWriter(new OutputStreamWriter (new FileOutputStream (outfile+"_"+String.valueOf(ref_outf)+".txt") , file_encoding)) ;
			else
				bw=new BufferedWriter(new FileWriter(outfile+"_"+String.valueOf(ref_outf)+".txt"));
			while (!data.isLast())
			{
				values = data.getRecord();
				if (values!=null)
				{
					totdatarec++;
					values[0]=values[0].trim();
					if (values[0].equals("")) are_missing=true;
					if (values[0].indexOf(" ")>0)
					{
						parts=values[0].split(" ");
						n_parts=parts.length;
						bw.write("_STARTWORDS\n");
						current_maxterms++;
						total_terms++;
						for (int i=0; i<parts.length; i++)
						{
							if (!parts[i].trim().equals(""))
							{
								bw.write(parts[i]+"\n");
								current_maxterms++;
								total_terms++;
							}
						}
						bw.write("_ENDWORDS\n");
						current_maxterms++;
						total_terms++;
						multiple=true;
					}
					else
					{
						if (!values[0].trim().equals(""))
						{
							bw.write(values[0]+"\n");
							current_maxterms++;
							total_terms++;
						}
					}
					if (maxterms>0)
					{
						if (current_maxterms>maxterms)
						{
							bw.flush();
							bw.close();
							bw=null;
							ref_outf++;
							current_maxterms=0;
							if (file_encoding!=null)
								bw=new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile+"_"+String.valueOf(ref_outf)+".txt"), file_encoding));
							else
								bw=new BufferedWriter(new FileWriter(outfile+"_"+String.valueOf(ref_outf)+".txt"));

						}
					}
				}
			}
			data.close();
			if (bw!=null)
			{
				bw.flush();
				bw.close();
				bw=null;
			}
			if (total_terms==0)
			{
				new File(outfile+"_"+String.valueOf(ref_outf)+".txt").delete();
				return new Result("%3351%<br>\n", false, null);
			}
		}
		catch (Exception e)
		{
			if (bw!=null)
			{
				try
				{
					bw.close();
					bw=null;
				}
				catch (Exception ee){}
			}
			new File(outfile+"_"+String.valueOf(ref_outf)+".txt").delete();
			return new Result("%3352% ("+e.toString()+")<br>\n", false, null);
		}
		if (n_parts>500)
		{
			new File(outfile+"_"+String.valueOf(ref_outf)+".txt").delete();
			return new Result("%4106% ("+String.valueOf(n_parts)+")<br>\n", false, null);
		}
		if (current_maxterms==0)
		{
			new File(outfile+"_"+String.valueOf(ref_outf)+".txt").delete();
			ref_outf=ref_outf-1;
		}
		if (are_missing)
		{
			for (int i=0; i<ref_outf; i++)
			{
				new File(outfile+"_"+String.valueOf(i+1)+".txt").delete();
			}
			return new Result("%4134%<br>\n", false, null);
		}
		Keywords.percentage_done=2;
		String inps="";
		String errs="";
		int les1sec=1;
		String line="";
		String msg="";
		try
		{
			for (int i=0; i<ref_outf; i++)
			{
				String[] commandstring=new String[7];
				if (addprob) commandstring=new String[8];
				commandstring[0]=trettaggerexe;
				commandstring[1]="-token";
				commandstring[2]="-quiet";
				commandstring[3]="-lemma";
				commandstring[4]=parameterfile;
				commandstring[5]=outfile+"_"+String.valueOf(i+1)+".txt";
				commandstring[6]=infile+"_"+String.valueOf(i+1)+".txt";
				if (addprob)
				{
					commandstring[4]="-proto-with-prob";
					commandstring[5]=parameterfile;
					commandstring[6]=outfile+"_"+String.valueOf(i+1)+".txt";;
					commandstring[7]=infile+"_"+String.valueOf(i+1)+".txt";
				}
				Process p = Runtime.getRuntime().exec(commandstring);
				BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
				BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
				line="";
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
				existfile=new File(infile+"_"+String.valueOf(i+1)+".txt").exists();
				les1sec=1;
				while (!existfile && les1sec<200)
				{
					try
					{
						Thread.sleep(100);
					}
					catch (Exception e){}
					existfile=(new File(infile+"_"+String.valueOf(i+1)+".txt")).exists();
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
		}
		catch (Exception err)
		{
			return new Result("%3353%: "+err.toString()+"<br>\n", false, null);
		}
		Keywords.percentage_done=3;
		if (secparameterfile!=null)
		{
			try
			{
				for (int i=0; i<ref_outf; i++)
				{
					String[] commandstring=new String[7];
					if (addprob) commandstring=new String[8];
					commandstring[0]=trettaggerexe;
					commandstring[1]="-token";
					commandstring[2]="-quiet";
					commandstring[3]="-lemma";
					commandstring[4]=secparameterfile;
					commandstring[5]=outfile+"_"+String.valueOf(i+1)+".txt";
					commandstring[6]=secinfile+"_"+String.valueOf(i+1)+".txt";
					if (addprob)
					{
						commandstring[4]="-proto-with-prob";
						commandstring[5]=secparameterfile;
						commandstring[6]=outfile+"_"+String.valueOf(i+1)+".txt";
						commandstring[7]=secinfile+"_"+String.valueOf(i+1)+".txt";
					}
					Process p = Runtime.getRuntime().exec(commandstring);
					BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
					BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
					line="";
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
					existfile=new File(secinfile+"_"+String.valueOf(i+1)+".txt").exists();
					les1sec=1;
					while (!existfile && les1sec<200)
					{
						try
						{
							Thread.sleep(100);
						}
						catch (Exception e){}
						existfile=(new File(secinfile+"_"+String.valueOf(i+1)+".txt")).exists();
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
			}
			catch (Exception err)
			{
				return new Result("%3353%: "+err.toString()+"<br>\n", false, null);
			}
		}
		Keywords.percentage_done++;
		for (int i=0; i<ref_outf; i++)
		{
			new File(outfile+"_"+String.valueOf(i+1)+".txt").delete();
		}
		BufferedReader bufferedReader=null;
		BufferedReader secbufferedReader=null;
		int ref_dim=0;
		if (n_parts==1)
		{
			ref_dim=2;
			if (addprob) ref_dim=3;
		}
		else
		{
			for (int i=0; i<n_parts; i++)
			{
				ref_dim=ref_dim+2;
				if (addprob) ref_dim=ref_dim+1;
			}
		}
		FastTempDataSet fdt=null;
		FastTempDataSet fdts=null;
		String[] values_temp=new String[ref_dim];
		try
		{
			fdt=new FastTempDataSet(tempdir);
			int controls_terms=0;
			Vector<String> terms_done=new Vector<String>();
			boolean start_multiple=false;
			boolean add_current=true;
			for (int i=0; i<ref_outf; i++)
			{
				start_multiple=false;
				if (file_encoding==null)
					bufferedReader = new BufferedReader(new FileReader(infile+"_"+String.valueOf(i+1)+".txt"));
				else
					bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(infile+"_"+String.valueOf(i+1)+".txt"), file_encoding));
				start_multiple=false;
				while((line = bufferedReader.readLine()) != null)
				{
					add_current=true;
					if (line.indexOf("_STARTWORDS")>=0)
					{
						add_current=false;
						start_multiple=true;
					}
					if (line.indexOf("_ENDWORDS")>=0)
					{
						add_current=false;
						start_multiple=false;
					}
					if (add_current)
					{
						terms_done.add(line.trim());
					}
					if (!start_multiple)
					{
						for (int j=0; j<ref_dim; j++)
						{
							values_temp[j]="";
						}
						validgroup=0;
						for (int j=0; j<terms_done.size(); j++)
						{
							parts=(terms_done.get(j)).split("\\t");
							values_temp[validgroup]=parts[1].toUpperCase();
							validgroup++;
							values_temp[validgroup]=parts[2].toLowerCase();
							if (values_temp[validgroup].indexOf("|")>0)
								values_temp[validgroup]=values_temp[validgroup].substring(0,values_temp[validgroup].indexOf("|"));
							validgroup++;
							if (addprob)
							{
								values_temp[validgroup]=parts[3].toLowerCase();
								validgroup++;
							}
						}
						fdt.write(values_temp);
						terms_done.clear();
					}
					controls_terms++;
				}
				bufferedReader.close();
			}
			fdt.endwrite();
			if (controls_terms!=total_terms)
			{
				if (bufferedReader!=null)
				{
					try
					{
						bufferedReader.close();
					}
					catch (Exception ee){}
				}
				fdt.deletefile();
				return new Result("%3356%<br>\n", false, null);
			}
			if (secparameterfile!=null)
			{
				fdts=new FastTempDataSet(tempdir);
				controls_terms=0;
				for (int i=0; i<ref_outf; i++)
				{
					if (file_encoding==null)
						secbufferedReader = new BufferedReader(new FileReader(secinfile+"_"+String.valueOf(i+1)+".txt"));
					else
						secbufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(secinfile+"_"+String.valueOf(i+1)+".txt"), file_encoding));
					start_multiple=false;
					while((line = secbufferedReader.readLine()) != null)
					{
						add_current=true;
						if (line.indexOf("_STARTWORDS")>=0)
						{
							add_current=false;
							start_multiple=true;
						}
						if (line.indexOf("_ENDWORDS")>=0)
						{
							add_current=false;
							start_multiple=false;
						}
						if (add_current)
						{
							terms_done.add(line.trim());
						}
						if (!start_multiple)
						{
							for (int j=0; j<ref_dim; j++)
							{
								values_temp[j]="";
							}
							validgroup=0;
							for (int j=0; j<terms_done.size(); j++)
							{
								parts=(terms_done.get(j)).split("\\t");
								values_temp[validgroup]=parts[1].toUpperCase();
								validgroup++;
								values_temp[validgroup]=parts[2].toLowerCase();
								validgroup++;
								if (addprob)
								{
									values_temp[validgroup]=parts[3].toLowerCase();
									validgroup++;
								}
							}
							fdts.write(values_temp);
							terms_done.clear();
						}
						controls_terms++;
					}
					secbufferedReader.close();
				}
				fdts.endwrite();
				if (controls_terms!=total_terms)
				{
					fdt.deletefile();
					fdts.deletefile();
					if (secbufferedReader!=null)
					{
						try
						{
							secbufferedReader.close();
						}
						catch (Exception ee){}
					}
					return new Result("%3356%<br>\n", false, null);
				}
			}
		}
		catch (Exception e)
		{
			fdt.deletefile();
			if (fdts!=null) fdts.deletefile();
			if (bufferedReader!=null)
			{
				try
				{
					bufferedReader.close();
				}
				catch (Exception ee){}
			}
			if (secbufferedReader!=null)
			{
				try
				{
					secbufferedReader.close();
				}
				catch (Exception ee){}
			}
			return new Result("%3360% ("+e.toString()+")<br>\n", false, null);
		}
		for (int i=0; i<ref_outf; i++)
		{
			new File(infile+"_"+String.valueOf(i+1)+".txt").delete();
			if (secparameterfile!=null) new File(secinfile+"_"+String.valueOf(i+1)+".txt").delete();
		}
		DataSetUtilities dsu=new DataSetUtilities();
		int dimnewvars=0;
		try
		{
			dsu.setreplace(null);
			Hashtable<String, String> temph=new Hashtable<String, String>();
			dsu.defineolddict(dict);
			if (n_parts==1)
			{
				dimnewvars=2;
				dsu.addnewvartoolddict("type", "%3357%", Keywords.TEXTSuffix, temph, temph);
				dsu.addnewvartoolddict("lemma", "%3358%", Keywords.TEXTSuffix, temph, temph);
				if (addprob)
				{
					dimnewvars=3;
					dsu.addnewvartoolddict("proto_probs", "%3359%", Keywords.TEXTSuffix, temph, temph);
				}
				if (secparameterfile!=null)
				{
					dimnewvars=dimnewvars*2;
					dsu.addnewvartoolddict("type_sec", "%3371%", Keywords.TEXTSuffix, temph, temph);
					dsu.addnewvartoolddict("lemma_sec", "%3372%", Keywords.TEXTSuffix, temph, temph);
					if (addprob)
						dsu.addnewvartoolddict("proto_probs_sec", "%3373%", Keywords.TEXTSuffix, temph, temph);
				}
			}
			else
			{
				dimnewvars=0;
				for (int i=0; i<n_parts; i++)
				{
					dsu.addnewvartoolddict("type_"+String.valueOf(i+1), "%3357% ("+String.valueOf(i+1)+")", Keywords.TEXTSuffix, temph, temph);
					dsu.addnewvartoolddict("lemma_"+String.valueOf(i+1), "%3358% ("+String.valueOf(i+1)+")", Keywords.TEXTSuffix, temph, temph);
					dimnewvars=dimnewvars+2;
					if (addprob)
					{
						dsu.addnewvartoolddict("proto_probs_"+String.valueOf(i+1), "%3359% ("+String.valueOf(i+1)+")", Keywords.TEXTSuffix, temph, temph);
						dimnewvars=dimnewvars+1;
					}
				}
				if (secparameterfile!=null)
				{
					for (int i=0; i<n_parts; i++)
					{
						dimnewvars=dimnewvars+2;
						dsu.addnewvartoolddict("type_sec_"+String.valueOf(i+1), "%3371% ("+String.valueOf(i+1)+")", Keywords.TEXTSuffix, temph, temph);
						dsu.addnewvartoolddict("lemma_sec_"+String.valueOf(i+1), "%3372% ("+String.valueOf(i+1)+")", Keywords.TEXTSuffix, temph, temph);
						if (addprob)
						{
							dsu.addnewvartoolddict("proto_probs_sec_"+String.valueOf(i+1), "%3373% ("+String.valueOf(i+1)+")", Keywords.TEXTSuffix, temph, temph);
							dimnewvars=dimnewvars+1;
						}
					}
				}
			}
			if (!dw.opendatatable(dsu.getfinalvarinfo()))
				return new Result(dw.getmessage(), false, null);
			data = new DataReader(dict);
			if (!data.open(null, 0, false))
				return new Result(data.getmessage(), false, null);
			String[] newvalues=new String[dimnewvars];
			String[] wvalues=null;
			if (fdt!=null) fdt.opentoread();
			if (fdts!=null) fdts.opentoread();
			int trec=0;
			if (fdt!=null) trec=fdt.getrecords();
			if (fdts!=null) trec=fdts.getrecords();
			String[] tempnrec=null;
			while (!data.isLast())
			{
				values = data.getRecord();
				if (values!=null)
				{
					Keywords.percentage_done++;
					if (fdt!=null)
					{
						tempnrec=fdt.read();
						for (int i=0; i<tempnrec.length; i++)
						{
							newvalues[i]=tempnrec[i];
						}
					}
					if (fdts!=null)
					{
						tempnrec=fdts.read();
						for (int i=0; i<tempnrec.length; i++)
						{
							newvalues[i+tempnrec.length]=tempnrec[i];
						}
					}
					wvalues=dsu.getnewvalues(values, newvalues);
					dw.write(wvalues);
				}
			}
			data.close();
		}
		catch (Exception e)
		{
			try
			{
				dw.deletetmp();
				data.close();
			}
			catch (Exception ee) {}
			return new Result("%3361% ("+e.toString()+")<br>\n", false, null);
		}
		Keywords.percentage_done++;
		if (fdt!=null)
		{
			fdt.endread();
			fdt.deletefile();
			fdt=null;
		}
		if (fdts!=null)
		{
			fdts.endread();
			fdts.deletefile();
			fdts=null;
		}
		Vector<StepResult> result = new Vector<StepResult>();
		String keyword="Word TreeTagger "+dict.getkeyword();
		String description="Word TreeTagger "+dict.getdescription();
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
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 3363, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 3364, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.var, "var=all", true, 3365, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.trettaggerexe,"file=all", true, 3366,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.parameterfile,"file=all", true, 3367,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.secparameterfile,"file=all", false, 3369,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.file_encoding,"text", false, 3393,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.maxterms,"text", false, 4104,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.addprob, "checkbox", false, 3368, dep, "", 2));
		return parameters;
	}
	/**
	*Return the name of the procedure and the group to which belongs
	*/
	public String[] getstepinfo() {
		String[] info=new String[2];
		info[0]="4168";
		info[1]="3362";
		return info;
	}
}
