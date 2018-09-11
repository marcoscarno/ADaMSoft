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
import java.io.File;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Vector;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.Iterator;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.CheckVarNames;
import ADaMSoft.utilities.StepUtilities;

/**
* This procedure creates a dictionary for a fixed format text file
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcFixedtxt2dict implements RunStep
{
	boolean label=false;
	/**
	* Creates a dictionary for a fixed format text file
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.txtfile, Keywords.outdict, Keywords.field};
		String [] optionalparameters=new String[] {Keywords.numlinestoread, Keywords.codethousanddlm, Keywords.codedecimaldlm};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		String txtfile    = (String) parameters.get(Keywords.txtfile);
		String path       = (String) parameters.get(Keywords.outdict);
		String field      = (String) parameters.get(Keywords.field);
		String numlinestoread= (String) parameters.get(Keywords.numlinestoread);

		String thousanddlm= (String) parameters.get(Keywords.codethousanddlm);
		String decimaldlm= (String) parameters.get(Keywords.codedecimaldlm);

		TreeMap<Integer, Integer> totalFields=new TreeMap<Integer, Integer>();
		try
		{
			String[] fields=field.split(";");
			for (int i=0; i<fields.length; i++)
			{
				String[] parts=fields[i].split("-");
				if (parts.length!=2)
					return new Result("%2087%<br>\n", false, null);
				int start=Integer.parseInt(parts[0].trim());
				int length=Integer.parseInt(parts[1].trim());
				totalFields.put(new Integer(start), new Integer(length));
			}
		}
		catch (Exception e)
		{
			return new Result("%2087%<br>\n", false, null);
		}
		int numl=0;
		if (numlinestoread!=null)
		{
			try
			{
				numl = Integer.parseInt(numlinestoread);
			}
			catch(NumberFormatException ExNum)
			{
				return new Result("%1604%<br>\n", false, null);
			}
		}
		int codethousanddlm=-1;
		if (thousanddlm!=null)
		{
			try
			{
				codethousanddlm = Integer.parseInt(thousanddlm);
	    	    thousanddlm = new Character((char)codethousanddlm).toString();
			}
			catch(NumberFormatException ExNum)
			{
				return new Result("%2144%<br>\n", false, null);
			}
		}
		int codedecimaldlm=-1;
		if (decimaldlm!=null)
		{
			try
			{
				codedecimaldlm = Integer.parseInt(decimaldlm);
				if (codethousanddlm==codedecimaldlm)
					return new Result("%2146%<br>\n", false, null);
	    	    decimaldlm = new Character((char)codedecimaldlm).toString();
			}
			catch(NumberFormatException ExNum)
			{
				return new Result("%2145%<br>\n", false, null);
			}
		}
		java.net.URL fileUrl;
		try
		{
			if((txtfile.toLowerCase()).startsWith("http"))
				fileUrl =  new java.net.URL(txtfile);
			else
			{
				File file=new File(txtfile);
				fileUrl = file.toURI().toURL();
			}
		}
		catch (Exception e)
		{
			return new Result("%245% ("+txtfile+") "+e.toString()+"<br>\n", false, null);
		}

       	String[] labelofvariables=new String[totalFields.size()];
       	String[] variableformat=new String[totalFields.size()];
       	int[] vstart=new int[totalFields.size()];
       	int[] vend=new int[totalFields.size()];
       	for (int i=0; i<totalFields.size(); i++)
       	{
			labelofvariables[i]="v"+String.valueOf(i);
			variableformat[i]=Keywords.NUMSuffix+Keywords.INTSuffix;
		}
		Iterator<Integer> it = totalFields.keySet().iterator();
		int ref=0;
		while(it.hasNext())
		{
			int start=(it.next()).intValue();
			int lun=(totalFields.get(new Integer(start))).intValue();
			vstart[ref]=start;
			vend[ref]=start+lun;
			ref++;
		}

       	boolean stopline=false;
       	String ttest;
       	double testisnum;
		try
		{
	        BufferedReader in = new BufferedReader(new InputStreamReader(fileUrl.openStream()));
        	String str;
        	int actualline=0;
        	while (((str = in.readLine()) != null) && (!stopline))
        	{
				actualline++;
				for (int i=0; i<vstart.length; i++)
				{
					String test=str.substring(vstart[i]-1, vend[i]-1);
					if (!test.equals(""))
					{
						ttest=test;
						try
						{
							if (!thousanddlm.equals(""))
								ttest=ttest.replace(thousanddlm,"");
						}
						catch (Exception e) {}
						try
						{
							if (!decimaldlm.equals(""))
							{
								if (!decimaldlm.equals("."))
									ttest=ttest.replace(decimaldlm,".");
							}
						}
						catch (Exception e) {}
						try
						{
							testisnum=Double.parseDouble(ttest);
							if (!Double.isNaN(testisnum))
							{
								if (!Double.isInfinite(testisnum))
									test=ttest;
							}
						}
						catch (Exception en){}
						if (variableformat[i].equalsIgnoreCase(Keywords.NUMSuffix+Keywords.INTSuffix))
						{
							try
							{
								Double.parseDouble(test.trim());
								if ( (test.indexOf(".")<0) && (test.startsWith("0")) && (test.length()>1) )
									variableformat[i]=Keywords.TEXTSuffix;
								else if ( (test.indexOf(".")<0) && (test.startsWith("0")) && (test.length()==1) )
									variableformat[i]=Keywords.NUMSuffix+Keywords.INTSuffix;
								else if ( (test.indexOf(".")>1) && (test.startsWith("0")) )
									variableformat[i]=Keywords.TEXTSuffix;
								else if ((test.indexOf(".")<0) && (!test.startsWith("0")) )
									variableformat[i]=Keywords.NUMSuffix+Keywords.INTSuffix;
								else
									variableformat[i]=Keywords.NUMSuffix;
							}
							catch (Exception en)
							{
								variableformat[i]=Keywords.TEXTSuffix;
							}
						}
						else if (!variableformat[i].equalsIgnoreCase(Keywords.TEXTSuffix))
						{
							try
							{
								Double.parseDouble(test.trim());
								if ( (test.indexOf(".")<0) && (test.startsWith("0")) && (test.length()>1) )
									variableformat[i]=Keywords.TEXTSuffix;
								else if ( (test.indexOf(".")<0) && (test.startsWith("0")) && (test.length()==1) )
									variableformat[i]=Keywords.NUMSuffix+Keywords.INTSuffix;
								else if ( (test.indexOf(".")>1) && (test.startsWith("0")) )
									variableformat[i]=Keywords.TEXTSuffix;
								else if ((test.indexOf(".")<0) && (!test.startsWith("0")) )
									variableformat[i]=Keywords.NUMSuffix+Keywords.INTSuffix;
								else
									variableformat[i]=Keywords.NUMSuffix;
							}
							catch (Exception en)
							{
								variableformat[i]=Keywords.TEXTSuffix;
							}
						}
					}
				}
				if (numl!=0)
				{
					if (numl==actualline)
						stopline=true;
				}
	        }
			in.close();
		}
		catch (Exception e)
		{
			String msgexc=e.toString();
			return new Result("%2088% ("+txtfile+")<br>\n"+msgexc+"<br>\n", false, null);
		}

		Vector<Hashtable<String, String>> fixedvariableinfo=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> tablevariableinfo=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> codelabel=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> missingdata=new Vector<Hashtable<String, String>>();
		for (int i=0; i<labelofvariables.length; i++)
		{
			Hashtable<String, String> tempfixedvariableinfo=new Hashtable<String, String>();
			Hashtable<String, String> temptablevariableinfo=new Hashtable<String, String>();
			Hashtable<String, String> tempcodelabel=new Hashtable<String, String>();
			Hashtable<String, String> tempmissingdata=new Hashtable<String, String>();
			tempfixedvariableinfo.put(Keywords.VariableName.toLowerCase(),labelofvariables[i]);
			tempfixedvariableinfo.put(Keywords.VariableFormat.toLowerCase(),variableformat[i]);
			tempfixedvariableinfo.put(Keywords.LabelOfVariable.toLowerCase(),labelofvariables[i]);
			temptablevariableinfo.put(Keywords.FixedFileVariableStart.toLowerCase(), String.valueOf(vstart[i]));
			temptablevariableinfo.put(Keywords.FixedFileVariableEnd.toLowerCase(), String.valueOf(vend[i]));
			fixedvariableinfo.add(tempfixedvariableinfo);
			tablevariableinfo.add(temptablevariableinfo);
			codelabel.add(tempcodelabel);
			missingdata.add(tempmissingdata);
		}


		String keyword=txtfile;
		String description=txtfile;
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());
		String datatabletype=Keywords.fixedtxt;

		Hashtable<String, String> datatableinfo=new Hashtable<String, String>();
		datatableinfo.put(Keywords.DATA.toLowerCase(), txtfile);
		if (codethousanddlm!=-1)
			datatableinfo.put(Keywords.codethousanddlm.toLowerCase(), (String) parameters.get(Keywords.codethousanddlm));
		if (codedecimaldlm!=-1)
			datatableinfo.put(Keywords.codedecimaldlm.toLowerCase(), (String) parameters.get(Keywords.codedecimaldlm));

		String workdir=(String)parameters.get(Keywords.WorkDir);
		if (!CheckVarNames.getResultCheck(fixedvariableinfo, workdir).equals(""))
			return new Result(CheckVarNames.getResultCheck(fixedvariableinfo, workdir), false, null);

		Vector<StepResult> result = new Vector<StepResult>();
		result.add(new LocalDictionaryWriter(path, keyword, description, author, datatabletype,
		datatableinfo, fixedvariableinfo, tablevariableinfo, codelabel, missingdata, null));
		return new Result("", true, result);

	}
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.outdict+"=", "outdictreport", true, 249, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.txtfile, "file=all", true, 2084, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.field, "multipletext", true, 2085, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 2086, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.numlinestoread, "text", false, 1603, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.codethousanddlm, "text", false, 2142, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.codedecimaldlm, "text", false, 2143, dep, "", 2));
		return parameters;
	}
	public String[] getstepinfo()
	{
		String[] retstepinfo=new String[2];
		retstepinfo[0]="247";
		retstepinfo[1]="2083";
		return retstepinfo;
	}
}
