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
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.DataSetUtilities;

import ADaMSoft.keywords.Keywords;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import java.util.Enumeration;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.File;

/**
* This is the procedure that execute R code
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcRcaller implements RunStep
{
	Vector<String> createdfiles;
	String tempdir;
	/**
	* Starts the execution of Proc Rcaller and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.rcmd};
		String [] optionalparameters=new String[] {Keywords.rbatchoptions, Keywords.rcode, Keywords.noviewrout, Keywords.rfile, Keywords.dict+"ToR", Keywords.OUT.toLowerCase()+"FromR"};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		Vector<String> listofoutdict=new Vector<String>();
		Vector<String> listofindict=new Vector<String>();
		for (Enumeration<String> en=parameters.keys(); en.hasMoreElements();)
		{
			String keyname=(String)en.nextElement();
			if (keyname.toLowerCase().startsWith((Keywords.dict+"ToR").toLowerCase())) listofoutdict.add(keyname);
			if (keyname.toLowerCase().startsWith((Keywords.OUT.toLowerCase()+"FromR").toLowerCase())) listofindict.add(keyname);
		}

		boolean noviewrout =(parameters.get(Keywords.noviewrout)!=null);
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		String rcode=(String)parameters.get(Keywords.rcode);
		String rfile=(String)parameters.get(Keywords.rfile);
		String code2submit="";
		if (rcode==null && rfile==null)
			return new Result("%3244%<br>\n", false, null);
		if (rcode!=null && rfile!=null)
			return new Result("%3244%<br>\n", false, null);
		tempdir=(String)parameters.get(Keywords.WorkDir);
		Vector<StepResult> results = new Vector<StepResult>();
		if (listofoutdict.size()==0) results.add(new LocalMessageGetter("%3255%<br>\n"));
		if (listofindict.size()==0) results.add(new LocalMessageGetter("%3256%<br>\n"));
		createdfiles=new Vector<String>();
		int numerrors=0;
		for (int i=0; i<listofoutdict.size(); i++)
		{
			String filename=(listofoutdict.get(i)).substring(7);
			code2submit=code2submit+filename+"<-read.table(\""+tempdir+filename+".txt\", header = TRUE, sep='\\t')\n";
			DictionaryReader dict = (DictionaryReader)parameters.get(listofoutdict.get(i));
			String actualmsg="%3257%: "+filename+" ("+dict.getDictPath()+")<br>\n";
			BufferedWriter fileout = null;
			try
			{
				fileout = new BufferedWriter(new FileWriter(tempdir+filename+".txt", false));
				createdfiles.add(tempdir+filename+".txt");
				int numvar=dict.gettotalvar();
				for (int j=0; j<numvar; j++)
				{
					String labname="";
					labname=dict.getvarname(j);
					fileout.write(labname);
					if (j<(numvar-1))
						fileout.write("\t");
				}
				fileout.write("\n");
				String[] values=null;
				DataReader data = new DataReader(dict);
				if (!data.open(null, 0, false))
				{
					numerrors++;
					actualmsg=actualmsg+"%3258%: "+data.getmessage()+"<br>\n";
					if (fileout!=null) fileout.close();
				}
				else
				{
					while (!data.isLast())
					{
						values = data.getRecord();
						for (int j=0; j<values.length; j++)
						{
							fileout.write(values[j]);
							if (j<(numvar-1))
								fileout.write("\t");
						}
						fileout.write("\n");
					}
					data.close();
					fileout.close();
					actualmsg=actualmsg+"%3259%<br>\n";
				}
			}
			catch (Exception e)
			{
				numerrors++;
				if (fileout!=null)
				{
					try
					{
						fileout.close();
					}
					catch (Exception ecl) {}
				}
				actualmsg=actualmsg+"%3258%: "+e.toString()+"<br>\n";
			}
			results.add(new LocalMessageGetter(actualmsg+"<br>\n"));
		}
		if (numerrors>0)
		{
			deletealltempfiles();
			return new Result("", false, results);
		}
		if (rcode!=null)
		{
			String[] partrcode=rcode.split(";");
			for (int i=0; i<partrcode.length; i++)
			{
				code2submit=code2submit+partrcode[i]+"\n";
			}
		}
		else
		{
			String actualmsg="%3260% ("+rfile+")<br>\n";
			BufferedReader br=null;
			try
			{
				br = new BufferedReader(new FileReader(rfile));
				String line = "";
				while ((line = br.readLine()) != null)
				{
					if (!(line.trim()).equals(""))
					{
						actualmsg=actualmsg+line+"<br>\n";
						code2submit=code2submit+line+"<br>\n";
					}
				}
				br.close();
			}
			catch (Exception ef)
			{
				if (br!=null)
				{
					try
					{
						br.close();
					}
					catch (Exception ecl) {}
				}
				actualmsg=actualmsg+"%3261%: "+ef.toString()+"<br>\n";
				results.add(new LocalMessageGetter(actualmsg+"<br>\n"));
				deletealltempfiles();
				return new Result("", false, results);
			}
			results.add(new LocalMessageGetter(actualmsg+"<br>\n"));
		}
		for (int i=0; i<listofindict.size(); i++)
		{
			String filename=(listofindict.get(i)).substring(8);
			code2submit=code2submit+"write.table("+filename+",\""+tempdir+filename+".txt\", quote = FALSE, col.names = TRUE, row.names=FALSE, sep='\\t')\n";
		}
		BufferedWriter filesr = null;
		try
		{
			filesr = new BufferedWriter(new FileWriter(tempdir+"script.R", false));
			filesr.write(code2submit);
			filesr.close();
		}
		catch (Exception esr)
		{
			if (filesr!=null)
			{
				try
				{
					filesr.close();
				}
				catch (Exception ecl) {}
			}
			results.add(new LocalMessageGetter("%3262% ("+esr.toString()+")<br>\n"));
			deletealltempfiles();
			return new Result("", false, results);
		}
		String inps="";
		String errs="";
		try
		{
			String commandstring=(String)parameters.get(Keywords.rcmd);
			String[] arraycmd=new String[6];
			String rbatchoptions=(String)parameters.get(Keywords.rbatchoptions);
			if (rbatchoptions!=null)
			{
				rbatchoptions=rbatchoptions.trim();
				String[] tempacmd=rbatchoptions.split(" ");
				arraycmd=new String[6+tempacmd.length];
				for (int i=0; i<tempacmd.length; i++)
				{
					arraycmd[1+i]=tempacmd[i];
				}
			}
			arraycmd[0]=commandstring;
			arraycmd[arraycmd.length-1]=tempdir+"script.R";
			arraycmd[arraycmd.length-2]="--no-restore";
			arraycmd[arraycmd.length-3]="--no-save";
			arraycmd[arraycmd.length-4]="BATCH";
			arraycmd[arraycmd.length-5]="CMD";
			Process p = Runtime.getRuntime().exec(arraycmd);
			BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line="";
			while ((line = bri.readLine()) != null)
			{
				inps=inps+line+"\n";
			}
			bri.close();
			while ((line = bre.readLine()) != null)
			{
				errs=errs+line+"\n";
			}
			bre.close();
			p.waitFor();
		}
		catch (Exception err)
		{
			results.add(new LocalMessageGetter("%3263%: "+err.toString()+"<br>\n"));
			deletealltempfiles();
			return new Result("", false, results);
		}
		if (!noviewrout && new File(tempdir+"script.Rout").exists())
		{
			BufferedReader br=null;
			String outmsg="";
			try
			{
				br = new BufferedReader(new FileReader(tempdir+"script.Rout"));
				String line = "";
				while ((line = br.readLine()) != null)
				{
					outmsg=outmsg+line+"<br>\n";
				}
				br.close();
				if (!outmsg.equals("")) results.add(new LocalOutputGetter(outmsg));
			}
			catch (Exception esr)
			{
				results.add(new LocalMessageGetter("%3264%: "+esr.toString()+"<br>\n"));
			}
		}
		if (!inps.trim().equals("")) results.add(new LocalMessageGetter("%3265%: "+inps+"<br>\n"));
		if (!errs.trim().equals("")) results.add(new LocalMessageGetter("%3266%: "+errs+"<br>\n"));
		for (int j=0; j<listofindict.size(); j++)
		{
			String filename=(listofindict.get(j)).substring(8);
			String msgload="";
			DataWriter dw=new DataWriter(parameters, listofindict.get(j));
			DataSetUtilities dsu=new DataSetUtilities();
			boolean errorinloading=false;
			if (!dw.getmessage().equals(""))
			{
				msgload=dw.getmessage();
				errorinloading=true;
			}
			else
			{
				try
				{
					File file=new File(tempdir+filename+".txt");
					createdfiles.add(tempdir+filename+".txt");
					java.net.URL fileUrl=file.toURI().toURL();
					BufferedReader in = new BufferedReader(new InputStreamReader(fileUrl.openStream()));
					String str;
					int actualline=0;
					Hashtable<String, String> tempmd=new Hashtable<String, String>();
					String[] varvalues=null;
					String[] varnames=new String[0];
					while ( (str = in.readLine() ) != null)
					{
						if (actualline==0)
						{
							varnames=str.split("\\t");
							for (int i=0; i<varnames.length; i++)
							{
								dsu.addnewvar(varnames[i], varnames[i], Keywords.NUMSuffix, tempmd, tempmd);
							}
							if (!dw.opendatatable(dsu.getfinalvarinfo()))
							{
								errorinloading=true;
								msgload=dw.getmessage();
							}
						}
						if (actualline>0 && !errorinloading)
						{
							varvalues=str.split("\\t");
							if (varvalues.length==varnames.length)
							{
								dw.write(varvalues);
							}
							else
							{
								errorinloading=true;
								msgload="%3268% ("+String.valueOf(actualline)+")<br>\n";
							}
						}
						actualline++;
					}
				}
				catch (Exception e)
				{
					msgload=e.toString()+"<br>\n";
					errorinloading=true;
				}
			}
			if (errorinloading)
			{
				try
				{
					dw.deletetmp();
				}
				catch (Exception er) {}
				results.add(new LocalMessageGetter("%3269%: "+filename+"<br>\n"+msgload+"<br>"));
			}
			else
			{
				boolean resclose=dw.close();
				if (!resclose) results.add(new LocalMessageGetter("%3269%: "+filename+"<br>\n"+dw.getmessage()));
				else
				{
					Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
					Hashtable<String, String> datatableinfo=dw.getTableInfo();
					results.add(new LocalDictionaryWriter(dw.getdictpath(), "R OUT"+filename, "R OUT"+filename, author, dw.gettabletype(),
					datatableinfo, dsu.getfinalvarinfo(), tablevariableinfo, dsu.getfinalcl(), dsu.getfinalmd(), null));
				}
			}
		}
		deletealltempfiles();
		return new Result("", true, results);
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"ToR", "multipledict", false, 3243, dep, "3248", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"FromR", "multiplesettingout", false, 3245, dep, "3249", 1));
		parameters.add(new GetRequiredParameters(Keywords.rcmd,"file=all", true, 3253,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.rbatchoptions,"text", false, 3254,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.rcode,"longtext", false, 3251,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.rfile,"file=all", false, 3252,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.noviewrout,"checkbox", false, 3267,dep,"",2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="4174";
		retprocinfo[1]="3237";
		return retprocinfo;
	}
	/**
	*Delete all the temporary files
	*/
	public void deletealltempfiles()
	{
		for (int i=0; i<createdfiles.size(); i++)
		{
			(new File(createdfiles.get(i))).delete();
		}
		(new File(tempdir+"script.R")).delete();
		(new File(tempdir+"script.Rout")).delete();
	}
}
