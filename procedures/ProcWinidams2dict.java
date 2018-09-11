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
import java.util.Enumeration;
import java.util.Vector;
import java.util.LinkedList;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.utilities.CheckVarNames;

/**
* This procedure creates a dictionary for a Winidams data set
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcWinidams2dict implements RunStep
{
	/**
	* Creates a dictionary for a Winidams dictionary
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.winidamsdic, Keywords.outdict, Keywords.winidamsdat};
		String [] optionalparameters=new String[0];
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		String winidamsdic = (String) parameters.get(Keywords.winidamsdic);
		String path       = (String) parameters.get(Keywords.outdict);
		String winidamsdat = (String) parameters.get(Keywords.winidamsdat);
		java.net.URL fileUrl;
		try
		{
			if((winidamsdic.toLowerCase()).startsWith("http"))
				fileUrl =  new java.net.URL(winidamsdic);
			else
			{
				File fileTXT=new File(winidamsdic);
				fileUrl = fileTXT.toURI().toURL();
			}
		}
		catch (Exception e)
		{
			return new Result("%571% ("+winidamsdic+")<br>\n", false, null);
		}
		Vector<Hashtable<String, String>> fixedvariableinfo=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> tablevariableinfo=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> codelabel=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> missingdata=new Vector<Hashtable<String, String>>();
		String inputLine="";
		try
		{
	        BufferedReader filebuffered = new BufferedReader(new InputStreamReader(fileUrl.openStream()));
			inputLine = filebuffered.readLine();
			if (!(inputLine.substring(3,4).equals("3")))
			{
				return new Result("%572% ("+winidamsdic+")<br>\n", false, null);
			}
			int totalvar=0;
			while ((inputLine = filebuffered.readLine()) != null)
			{
				if(inputLine.startsWith("T"))
					totalvar++;
			}
			for (int i=0; i<totalvar; i++)
			{
				Hashtable<String, String> temp=new Hashtable<String, String>();
				fixedvariableinfo.add(temp);
				tablevariableinfo.add(temp);
				codelabel.add(temp);
				missingdata.add(temp);
			}
			filebuffered.close();
	        filebuffered = new BufferedReader(new InputStreamReader(fileUrl.openStream()));
	        int varpointer=0;
			Hashtable<String, String> tempcl=new Hashtable<String, String>();
			while ((inputLine = filebuffered.readLine()) != null)
			{
				if(inputLine.startsWith("T"))
				{
					if (tempcl.size()>0)
					{
						Hashtable<String, String> t=new Hashtable<String, String>();
						for (Enumeration<String> e = tempcl.keys() ; e.hasMoreElements() ;)
						{
							String p = e.nextElement();
							String l = tempcl.get(p);
							t.put(p, l);
						}
						codelabel.set(varpointer-1, t);
					}
					tempcl.clear();
					try
					{
						Hashtable<String, String> temp=new Hashtable<String, String>();
						int varIndex;
						varIndex= Integer.parseInt(inputLine.substring(1,5).trim());
						temp.put(Keywords.VariableName.toLowerCase(),"v"+String.valueOf(varIndex));
						temp.put(Keywords.LabelOfVariable.toLowerCase(), inputLine.substring(6,30).trim());
						boolean isNum = inputLine.substring(40,41).equals(" ");
						if (!isNum)
							temp.put(Keywords.VariableFormat.toLowerCase(),"TEXT");
						else
							temp.put(Keywords.VariableFormat.toLowerCase(),"NUM");
						Hashtable<String, String> tempvi=new Hashtable<String, String>();
						int vs=Integer.parseInt(inputLine.substring(31,35).trim());
						int ve=Integer.parseInt(inputLine.substring(35,39).trim());
						tempvi.put(Keywords.FixedFileVariableStart.toLowerCase(), String.valueOf(vs));
						tempvi.put(Keywords.FixedFileVariableEnd.toLowerCase(), String.valueOf(vs+ve));
						int varDec = 0;
						if (isNum && !(inputLine.substring(39,40).equals(" ")))
							varDec = Integer.parseInt(inputLine.substring(39,40));
						String vardec  =String.valueOf(varDec);
						tempvi.put(Keywords.NumberOfDecimals.toLowerCase(),vardec);
						fixedvariableinfo.set(varpointer, temp);
						tablevariableinfo.set(varpointer, tempvi);
						if(isNum)
						{
							double valueMD1=Double.NaN;
							double valueMD2=Double.NaN;
							if(!(inputLine.substring(44,51).trim().equals("")))
							{
								valueMD1 = Double.parseDouble(inputLine.substring(44,51).trim());
								if (varDec>0)
								{
									for (int nd=0; nd<varDec; nd++)
									{
										valueMD1=valueMD1/10.0;
									}
								}
							}
							if(!(inputLine.substring(51,58).trim().equals("")))
							{
								valueMD2 = Double.parseDouble(inputLine.substring(51,58).trim());
								if (varDec>0)
								{
									for (int nd=0; nd<varDec; nd++)
									{
										valueMD2=valueMD2/10.0;
									}
								}
							}
							if ((!Double.isNaN(valueMD1)) || (!Double.isNaN(valueMD2)))
							{
								Hashtable<String, String> md=new Hashtable<String, String>();
								if (!Double.isNaN(valueMD1))
									md.put(String.valueOf(valueMD1),"");
								if (!Double.isNaN(valueMD2))
									md.put(String.valueOf(valueMD2),"");
								missingdata.set(varpointer, md);
							}
						}

					}
					catch (Exception ev)
					{
						filebuffered.close();
						return new Result("%574% ("+winidamsdic+")<br>\n", false, null);
					}
					varpointer++;
				}
				if(inputLine.startsWith("C"))
				{
					String code = inputLine.substring(14,19).trim();
					String formatcode = inputLine.substring(21,29).trim();
					tempcl.put(code, formatcode);
				}
			}
			filebuffered.close();
		}
    	catch (Exception e)
    	{
			return new Result("%573% ("+winidamsdic+")<br>\n", false, null);
		}
		String keyword=winidamsdic;
		String description=winidamsdic;
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());
		String datatabletype=Keywords.fixedtxt;

		Hashtable<String, String> datatableinfo=new Hashtable<String, String>();
		datatableinfo.put(Keywords.DATA.toLowerCase(), winidamsdat);

		String workdir=(String)parameters.get(Keywords.WorkDir);
		if (!CheckVarNames.getResultCheck(fixedvariableinfo, workdir).equals(""))
			return new Result(CheckVarNames.getResultCheck(fixedvariableinfo, workdir), false, null);

		Vector<StepResult> result = new Vector<StepResult>();
		result.add(new LocalDictionaryWriter(path, keyword, description, author, datatabletype,
		datatableinfo, fixedvariableinfo, tablevariableinfo, codelabel, missingdata, null));
		return new Result("", true, result);

	}
	/**
	* Create the LinkedList with the parameters for the procedures
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.outdict+"=", "outdictreport", true, 249, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.winidamsdic, "file=dic", true, 575, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.winidamsdat, "file=dat", true, 576, dep, "", 2));
		return parameters;
	}
	/**
	* Specify the procedure name and the groups to which it belongs
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="247";
		retprocinfo[1]="577";
		return retprocinfo;
	}
}
