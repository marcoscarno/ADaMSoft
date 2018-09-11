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
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.CheckVarNames;
import ADaMSoft.utilities.StringSplitter;


/**
* This procedure creates a dictionary for a delimited text file
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class ProcDlmtxt2dict implements RunStep
{
	boolean label=false;
	/**
	* Creates a dictionary for a delimited text file
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.txtfile, Keywords.outdict};
		String [] optionalparameters=new String[] {Keywords.dlm, Keywords.label, Keywords.uselabelasvarname, Keywords.allvarstext, Keywords.trimchars, Keywords.codethousanddlm, Keywords.codedecimaldlm, Keywords.numlinestoread, Keywords.avoidquotationmarks};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}

		String txtfile    = (String) parameters.get(Keywords.txtfile);
		String path       = (String) parameters.get(Keywords.outdict);
		String dlm        = (String) parameters.get(Keywords.dlm);
		String numlinestoread= (String) parameters.get(Keywords.numlinestoread);

		String trimchars= (String) parameters.get(Keywords.trimchars);

		boolean avoidquotationmarks=(parameters.get(Keywords.avoidquotationmarks)!=null);
		boolean allvarstext=(parameters.get(Keywords.allvarstext)!=null);

		String thousanddlm= (String) parameters.get(Keywords.codethousanddlm);
		String decimaldlm= (String) parameters.get(Keywords.codedecimaldlm);

		label             =(parameters.get(Keywords.label)!=null);
		boolean uselabelasvarname =(parameters.get(Keywords.uselabelasvarname)!=null);
		if ((!label) && (uselabelasvarname))
			return new Result("%1594%<br>\n", false, null);
		if (dlm==null)
			dlm="09";
		int charSeparator;
		try
		{
			charSeparator = Integer.parseInt(dlm);
		}
		catch(NumberFormatException ExNum)
		{
			return new Result("%244%<br>\n", false, null);
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
		if ((label) && (numl==1))
			numl=2;

        String separ = new Character((char)charSeparator).toString();
        StringSplitter sp=new StringSplitter();
        sp.setdlm(separ);
        sp.setQuotationMarks(avoidquotationmarks);
        if (codethousanddlm!=-1)
        	sp.setthousanddlm(thousanddlm);
        if (codedecimaldlm!=-1)
	        sp.setdecimaldlm(decimaldlm);
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
			return new Result("%245% ("+txtfile+")<br>\n", false, null);
		}
		int ncol=-1;
       	boolean checkcol=true;
       	boolean actuallabel=label;
       	Vector<String> labelofvariables=new Vector<String>();
       	Vector<String> variableformat=new Vector<String>();
       	boolean stopline=false;
       	int tempich=0;
       	int temprfc=0;
       	String[] trimc=null;
       	String[] temptemptrimchars=null;
       	StringBuilder start=null;
		try
		{
	        BufferedReader in = new BufferedReader(new InputStreamReader(fileUrl.openStream()));
        	String str;
        	int actualline=0;
        	while (((str = in.readLine()) != null) && (!stopline))
        	{
				actualline++;
				if (!str.trim().equals(""))
				{
					if (trimchars!=null)
					{
						trimc=trimchars.split(";");
						for (int i=0; i<trimc.length; i++)
						{
							temptemptrimchars=(trimc[i].trim()).split(" ");
							for (int j=0; j<temptemptrimchars.length; j++)
							{
								try
								{
									temprfc=Integer.parseInt(temptemptrimchars[j].trim());
									start=new StringBuilder();
									start.append(str);
									StringBuilder end=new StringBuilder();
									int sz = start.length();
									for (int h = 0; h < sz; h++)
									{
										tempich=new Integer(start.charAt(h));
										if (tempich!=temprfc)
											end.append(start.charAt(h));
									}
									str=end.toString();
									start=null;
								}
								catch (Exception ee){}
							}
						}
					}
					try
					{
						str=str.replaceAll("\"\"","'");
					}
					catch (Exception e) {}
					sp.settextwithformat(str);
					String [] dimcol=sp.getvals();
					if (variableformat.size()==0)
					{
						for (int j=0; j<dimcol.length; j++)
						{
							if (allvarstext)
								variableformat.add(Keywords.TEXTSuffix);
							else
								variableformat.add(Keywords.NUMSuffix+Keywords.INTSuffix);
						}
						ncol=dimcol.length;
					}
					if (!actuallabel)
					{
						if (dimcol.length!=variableformat.size())
						{
							String outstring="";
							for (int i=0; i<dimcol.length; i++)
							{
								outstring=outstring+String.valueOf(i+1)+"\t"+dimcol[i];
								if (i<(dimcol.length-1)) outstring=outstring+"\n";
							}
							in.close();
							return new Result("%2122% (%2123%:"+String.valueOf(actualline)+")<br>\n%2908%:<br>\n"+outstring+"<br>\n", false, null);
						}
						for (int i=0; i<dimcol.length; i++)
						{
							String test=variableformat.get(i);
							if (!test.equalsIgnoreCase(Keywords.TEXTSuffix))
							{
								String newf=sp.getformat(i);
								if (!newf.equals(Keywords.NUMSuffix+Keywords.INTSuffix))
									variableformat.set(i, newf);
							}
						}
					}
					else
					{
						for (int i=0; i<dimcol.length; i++)
						{
							labelofvariables.add(dimcol[i].trim());
						}
						actuallabel=false;
					}
					if (dimcol.length!=ncol)
						checkcol=false;
					if (numl!=0)
					{
						if (numl==actualline)
							stopline=true;
					}
				}
	        }
			in.close();
		}
		catch (Exception e)
		{
			String msgexc=e.toString();
			return new Result("%245% ("+txtfile+")<br>\n"+msgexc+"<br>\n", false, null);
		}
		Keywords.percentage_done=2;
		if (!checkcol)
		{
			return new Result("%246% ("+txtfile+")<br>\n", false, null);
		}
		boolean vexist=false;
		String[] varname=new String[ncol];
		if (uselabelasvarname)
		{
			Hashtable<String, String> testvn=new Hashtable<String, String>();
			for (int i=0; i<ncol; i++)
			{
				String tempvname=labelofvariables.get(i);
				String[] tempvnames=tempvname.split(" ");
				if (tempvnames.length>0)
				{
					tempvname="";
					for (int j=0; j<tempvnames.length; j++)
					{
						tempvname=tempvname+tempvnames[j];
					}
				}
				if (testvn.size()>0)
				{
					if (testvn.get(tempvname)!=null)
						vexist=true;
				}
				testvn.put(tempvname, "");
				varname[i]=tempvname;
			}
		}
		if (vexist)
			return new Result("%1595%<br>\n", false, null);

		String keyword=txtfile;
		String description=txtfile;
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());
		String datatabletype=Keywords.dlmfile;
		Hashtable<String, String> datatableinfo=new Hashtable<String, String>();
		datatableinfo.put(Keywords.DATA.toLowerCase(), txtfile);
		datatableinfo.put(Keywords.DLM.toLowerCase(), dlm);
		if (avoidquotationmarks)
			datatableinfo.put(Keywords.avoidquotationmarks, "ON");
		if (trimchars!=null)
			datatableinfo.put(Keywords.trimchars, trimchars);
		if (label)
			datatableinfo.put(Keywords.LABEL.toLowerCase(), "ON");
		if (codethousanddlm!=-1)
			datatableinfo.put(Keywords.codethousanddlm.toLowerCase(), (String) parameters.get(Keywords.codethousanddlm));
		if (codedecimaldlm!=-1)
			datatableinfo.put(Keywords.codedecimaldlm.toLowerCase(), (String) parameters.get(Keywords.codedecimaldlm));
		Vector<Hashtable<String, String>> fixedvariableinfo=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> tablevariableinfo=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> codelabel=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> missingdata=new Vector<Hashtable<String, String>>();

		if (label)
		{
			for (int i=0; i<ncol; i++)
			{
				if (labelofvariables.get(i).equals(""))
					return new Result("%2647%<br>\n", false, null);
			}
			for (int i=0; i<ncol-1; i++)
			{
				for (int j=i+1; j<ncol; j++)
				{
					if (labelofvariables.get(i).equalsIgnoreCase(labelofvariables.get(j)))
						return new Result("%2648% ("+labelofvariables.get(i)+")<br>\n", false, null);
				}
			}
		}

		for (int i=0; i<ncol; i++)
		{
			Hashtable<String, String> tempfixedvariableinfo=new Hashtable<String, String>();
			Hashtable<String, String> temptablevariableinfo=new Hashtable<String, String>();
			Hashtable<String, String> tempcodelabel=new Hashtable<String, String>();
			Hashtable<String, String> tempmissingdata=new Hashtable<String, String>();
			if (!uselabelasvarname)
				tempfixedvariableinfo.put(Keywords.VariableName.toLowerCase(),"v"+(String.valueOf(i)));
			else
			{
				if (varname[i].startsWith("0")) varname[i]="_"+varname[i];
				if (varname[i].startsWith("1")) varname[i]="_"+varname[i];
				if (varname[i].startsWith("2")) varname[i]="_"+varname[i];
				if (varname[i].startsWith("3")) varname[i]="_"+varname[i];
				if (varname[i].startsWith("4")) varname[i]="_"+varname[i];
				if (varname[i].startsWith("5")) varname[i]="_"+varname[i];
				if (varname[i].startsWith("6")) varname[i]="_"+varname[i];
				if (varname[i].startsWith("7")) varname[i]="_"+varname[i];
				if (varname[i].startsWith("8")) varname[i]="_"+varname[i];
				if (varname[i].startsWith("9")) varname[i]="_"+varname[i];
				try
				{
					varname[i]=varname[i].replaceAll(" ","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll("#","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll("\\*","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll("\\+","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll("\\.","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll("-","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll("&","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll("%","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll("\\|","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll("!","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll("\\$","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll("/","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll("\\(","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll("\\)","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll("=","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll("\\?","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll("<","_");
				}
				catch(Exception ename){}
				try
				{
					varname[i]=varname[i].replaceAll(">","_");
				}
				catch(Exception ename){}
				tempfixedvariableinfo.put(Keywords.VariableName.toLowerCase(),varname[i]);
			}
			tempfixedvariableinfo.put(Keywords.VariableFormat.toLowerCase(),variableformat.get(i));
			if (!label)
				tempfixedvariableinfo.put(Keywords.LabelOfVariable.toLowerCase(),"v"+(String.valueOf(i)));
			else
				tempfixedvariableinfo.put(Keywords.LabelOfVariable.toLowerCase(),labelofvariables.get(i));
			fixedvariableinfo.add(tempfixedvariableinfo);
			tablevariableinfo.add(temptablevariableinfo);
			codelabel.add(tempcodelabel);
			missingdata.add(tempmissingdata);
		}
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
		parameters.add(new GetRequiredParameters(Keywords.txtfile, "file=all", true, 250, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.dlm, "text", false, 251, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.numlinestoread, "text", false, 1603, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.label,"checkbox",false,252,dep,"",2));
		parameters.add(new GetRequiredParameters(Keywords.uselabelasvarname, "checkbox", false, 1592, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.allvarstext, "checkbox", false, 3814, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.avoidquotationmarks, "checkbox", false, 2916, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 1593, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.trimchars, "multipletext", false, 2917, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 2918, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.codethousanddlm, "text", false, 2142, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.codedecimaldlm, "text", false, 2143, dep, "", 2));
		return parameters;
	}
	public String[] getstepinfo()
	{
		String[] retstepinfo=new String[2];
		retstepinfo[0]="247";
		retstepinfo[1]="248";
		return retstepinfo;
	}
}
