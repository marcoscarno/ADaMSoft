/**
* Copyright (c) 2015 MS
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

package ADaMSoft.utilities;

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;

import ADaMSoft.utilities.SortRequestedVar;
/**
* This class contains several utilities for the variables that can be used in a step
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/
public class VariableUtilities
{
	String[] groupvar;
	String[] analysisvar;
	String[] weightvar;
	String[] rowvar;
	String[] colvar;
	String[] allvar;
	String[] reqvar;
	String messageVariableUtilities;
	boolean errorVariableUtilities;
	Vector<Hashtable<String, String>> fixedvariableinfo;
	/**
	* This is the constructor of the methods that contains utilities on the variables.<p>
	* Receive:<p>
	* the names of the grouping variables<p>
	* the names of the analysis variables<p>
	* the name of the weighting variable<p>
	* the name of the row variables<p>
	* the name of the column variables<p>
	*/
	public VariableUtilities(DictionaryReader dict, String varg, String vara, String varw, String varr, String varc)
	{
		Hashtable<String, Integer> orderedgvar=new Hashtable<String, Integer>();
		Hashtable<String, Integer> orderedavar=new Hashtable<String, Integer>();
		Hashtable<String, Integer> orderedrvar=new Hashtable<String, Integer>();
		Hashtable<String, Integer> orderedcvar=new Hashtable<String, Integer>();
		Hashtable<String, Integer> orderedvar=new Hashtable<String, Integer>();
		messageVariableUtilities="";
		errorVariableUtilities=false;
		groupvar=new String[0];
		analysisvar=new String[0];
		weightvar=new String[0];
		rowvar=new String[0];
		colvar=new String[0];
		allvar=new String[0];
		reqvar=new String[0];
		Hashtable<String, String> allrequestedvar=new Hashtable<String, String>();
		if (varg!=null)
		{
			groupvar=varg.split(" ");
			Hashtable<String, String> temp=new Hashtable<String, String>();
			for (int i=0; i<groupvar.length; i++)
			{
				temp.put(groupvar[i].toLowerCase(),"");
				if (orderedgvar.get(groupvar[i].toLowerCase())==null)
				{
					int csize=orderedgvar.size()+1;
					orderedgvar.put(groupvar[i].toLowerCase(), new Integer(csize));
				}
				if (orderedvar.get(groupvar[i].toLowerCase())==null)
				{
					int csize=orderedvar.size()+1;
					orderedvar.put(groupvar[i].toLowerCase(), new Integer(csize));
				}
			}
			groupvar=new String[temp.size()];
			int pointer=0;
			for (Enumeration<String> e = temp.keys() ; e.hasMoreElements() ;)
			{
				groupvar[pointer]=e.nextElement();
				allrequestedvar.put(groupvar[pointer],"");
				pointer++;
			}
		}
		if (varw!=null)
		{
			weightvar=varw.split(" ");
			Hashtable<String, String> temp=new Hashtable<String, String>();
			for (int i=0; i<weightvar.length; i++)
			{
				temp.put(weightvar[i].toLowerCase(),"");
			}
			if (temp.size()>1)
			{
				errorVariableUtilities=true;
				messageVariableUtilities="%595%<br>\n";
				return;
			}
			weightvar=new String[temp.size()];
			int pointer=0;
			for (Enumeration<String> e = temp.keys() ; e.hasMoreElements() ;)
			{
				weightvar[pointer]=e.nextElement();
				allrequestedvar.put(weightvar[pointer],"");
				pointer++;
			}
			boolean varincluded=false;
			for (int i=0; i<weightvar.length; i++)
			{
				for (int j=0; j<groupvar.length; j++)
				{
					if (weightvar[i].equalsIgnoreCase(groupvar[j]))
						varincluded=true;
				}
			}
			if (varincluded)
			{
				errorVariableUtilities=true;
				messageVariableUtilities="%635%<br>\n";
				return;
			}
		}
		if (vara!=null)
		{
			analysisvar=vara.split(" ");
			Hashtable<String, String> temp=new Hashtable<String, String>();
			for (int i=0; i<analysisvar.length; i++)
			{
				temp.put(analysisvar[i].toLowerCase(),"");
				if (orderedavar.get(analysisvar[i].toLowerCase())==null)
				{
					int csize=orderedavar.size()+1;
					orderedavar.put(analysisvar[i].toLowerCase(), new Integer(csize));
				}
				if (orderedvar.get(analysisvar[i].toLowerCase())==null)
				{
					int csize=orderedvar.size()+1;
					orderedvar.put(analysisvar[i].toLowerCase(), new Integer(csize));
				}
			}
			analysisvar=new String[temp.size()];
			int pointer=0;
			for (Enumeration<String> e = temp.keys() ; e.hasMoreElements() ;)
			{
				analysisvar[pointer]=e.nextElement();
				allrequestedvar.put(analysisvar[pointer],"");
				pointer++;
			}
			boolean varincluded=false;
			for (int i=0; i<analysisvar.length; i++)
			{
				for (int j=0; j<groupvar.length; j++)
				{
					if (analysisvar[i].equalsIgnoreCase(groupvar[j]))
						varincluded=true;
				}
			}
			if (varincluded)
			{
				errorVariableUtilities=true;
				messageVariableUtilities="%634%<br>\n";
				return;
			}
			varincluded=false;
			for (int i=0; i<analysisvar.length; i++)
			{
				for (int j=0; j<weightvar.length; j++)
				{
					if (analysisvar[i].equalsIgnoreCase(weightvar[j]))
						varincluded=true;
				}
			}
			if (varincluded)
			{
				errorVariableUtilities=true;
				messageVariableUtilities="%636%<br>\n";
				return;
			}
		}
		if (varr!=null)
		{
			rowvar=varr.split(" ");
			Hashtable<String, String> temp=new Hashtable<String, String>();
			for (int i=0; i<rowvar.length; i++)
			{
				temp.put(rowvar[i].toLowerCase(),"");
				if (orderedrvar.get(rowvar[i].toLowerCase())==null)
				{
					int csize=orderedrvar.size()+1;
					orderedrvar.put(rowvar[i].toLowerCase(), new Integer(csize));
				}
				if (orderedvar.get(rowvar[i].toLowerCase())==null)
				{
					int csize=orderedvar.size()+1;
					orderedvar.put(rowvar[i].toLowerCase(), new Integer(csize));
				}
			}
			rowvar=new String[temp.size()];
			int pointer=0;
			for (Enumeration<String> e = temp.keys() ; e.hasMoreElements() ;)
			{
				rowvar[pointer]=e.nextElement();
				allrequestedvar.put(rowvar[pointer],"");
				pointer++;
			}
			boolean varincluded=false;
			for (int i=0; i<rowvar.length; i++)
			{
				for (int j=0; j<groupvar.length; j++)
				{
					if (rowvar[i].equalsIgnoreCase(groupvar[j]))
						varincluded=true;
				}
			}
			if (varincluded)
			{
				errorVariableUtilities=true;
				messageVariableUtilities="%634%<br>\n";
				return;
			}
			varincluded=false;
			for (int i=0; i<rowvar.length; i++)
			{
				for (int j=0; j<weightvar.length; j++)
				{
					if (rowvar[i].equalsIgnoreCase(weightvar[j]))
						varincluded=true;
				}
			}
			if (varincluded)
			{
				errorVariableUtilities=true;
				messageVariableUtilities="%636%<br>\n";
				return;
			}
		}
		if (varc!=null)
		{
			colvar=varc.split(" ");
			Hashtable<String, String> temp=new Hashtable<String, String>();
			for (int i=0; i<colvar.length; i++)
			{
				temp.put(colvar[i].toLowerCase(),"");
				if (orderedcvar.get(colvar[i].toLowerCase())==null)
				{
					int csize=orderedcvar.size()+1;
					orderedcvar.put(colvar[i].toLowerCase(), new Integer(csize));
				}
				if (orderedvar.get(colvar[i].toLowerCase())==null)
				{
					int csize=orderedvar.size()+1;
					orderedvar.put(colvar[i].toLowerCase(), new Integer(csize));
				}
			}
			colvar=new String[temp.size()];
			int pointer=0;
			for (Enumeration<String> e = temp.keys() ; e.hasMoreElements() ;)
			{
				colvar[pointer]=e.nextElement();
				allrequestedvar.put(colvar[pointer],"");
				pointer++;
			}
			boolean varincluded=false;
			for (int i=0; i<colvar.length; i++)
			{
				for (int j=0; j<groupvar.length; j++)
				{
					if (colvar[i].equalsIgnoreCase(groupvar[j]))
						varincluded=true;
				}
			}
			if (varincluded)
			{
				errorVariableUtilities=true;
				messageVariableUtilities="%634%<br>\n";
				return;
			}
			varincluded=false;
			for (int i=0; i<colvar.length; i++)
			{
				for (int j=0; j<weightvar.length; j++)
				{
					if (colvar[i].equalsIgnoreCase(weightvar[j]))
						varincluded=true;
				}
			}
			if (varincluded)
			{
				errorVariableUtilities=true;
				messageVariableUtilities="%636%<br>\n";
				return;
			}
		}
		//test if the requested variables exists in the dictionary
		fixedvariableinfo=dict.getfixedvariableinfo();
		int existentvariables=0;
		String notexistent="";
		for (Enumeration<String> e = allrequestedvar.keys() ; e.hasMoreElements() ;)
		{
			String reqname=e.nextElement();
			boolean adname=false;
			for (int j=0; j<fixedvariableinfo.size(); j++)
			{
				Hashtable<String, String> tempv=fixedvariableinfo.get(j);
				String exiname=(tempv.get(Keywords.VariableName.toLowerCase())).trim();
				if (reqname.equalsIgnoreCase(exiname))
				{
					existentvariables++;
					adname=true;
				}
			}
			if (!adname)
				notexistent=notexistent+reqname+" ";
		}
		if (existentvariables!=allrequestedvar.size())
		{
			errorVariableUtilities=true;
			messageVariableUtilities="%514%<br>\n"+notexistent.trim()+"<br>\n";
			return;
		}
		allvar=new String[fixedvariableinfo.size()];
		for (int j=0; j<fixedvariableinfo.size(); j++)
		{
			Hashtable<String, String> tempv=fixedvariableinfo.get(j);
			String exiname=(tempv.get(Keywords.VariableName.toLowerCase())).trim();
			allvar[j]=exiname;
		}
		if ((vara==null) && (varr==null) && (varc==null))
		{
			if (allrequestedvar.size()==0)
			{
				analysisvar=new String[allvar.length];
				for (int i=0; i<allvar.length; i++)
				{
					analysisvar[i]=allvar[i];
					allrequestedvar.put(allvar[i],"");
				}
			}
			else
			{
				Vector<String> tempname=new Vector<String>();
				for (int j=0; j<fixedvariableinfo.size(); j++)
				{
					Hashtable<String, String> tempv=fixedvariableinfo.get(j);
					String exiname=(tempv.get(Keywords.VariableName.toLowerCase())).trim();
					boolean notreq=true;
					for (Enumeration<String> e = allrequestedvar.keys() ; e.hasMoreElements() ;)
					{
						String selname=e.nextElement();
						if (exiname.equalsIgnoreCase(selname))
							notreq=false;
					}
					if (notreq)
						tempname.add(exiname);
				}
				if (tempname.size()==0)
				{
					errorVariableUtilities=true;
					messageVariableUtilities="%638%<br>\n";
					return;
				}
				analysisvar=new String[tempname.size()];
				for (int i=0; i<tempname.size(); i++)
				{
					analysisvar[i]=tempname.get(i);
					allrequestedvar.put(analysisvar[i],"");
				}
			}
		}
		reqvar=new String[allrequestedvar.size()];
		int pointer=0;
		for (Enumeration<String> e = allrequestedvar.keys() ; e.hasMoreElements() ;)
		{
			String selname=e.nextElement();
			reqvar[pointer]=selname;
			pointer++;
		}
		reqvar=SortRequestedVar.getsorted(reqvar, orderedvar);
		groupvar=SortRequestedVar.getsorted(groupvar, orderedgvar);
		analysisvar=SortRequestedVar.getsorted(analysisvar, orderedavar);
		rowvar=SortRequestedVar.getsorted(rowvar, orderedrvar);
		colvar=SortRequestedVar.getsorted(colvar, orderedcvar);
	}
	/**
	*Return true in case of error
	*/
	public boolean geterror()
	{
		return errorVariableUtilities;
	}
	/**
	*Return the requested variables
	*/
	public String[] getreqvar()
	{
		return reqvar;
	}
	/**
	*Return the error message
	*/
	public String getmessage()
	{
		return messageVariableUtilities;
	}
	/**
	*Return the grouping variables
	*/
	public String[] getgroupvar()
	{
		return groupvar;
	}
	/**
	*Return the analysis variables
	*/
	public String[] getanalysisvar()
	{
		return analysisvar;
	}
	/**
	*Return the weighting variables
	*/
	public String[] getweightvar()
	{
		return weightvar;
	}
	/**
	*Return the row variables
	*/
	public String[] getrowvar()
	{
		return rowvar;
	}
	/**
	*Return the column variables
	*/
	public String[] getcolvar()
	{
		return colvar;
	}
	/**
	*Return all variables name
	*/
	public String[] getallvar()
	{
		return allvar;
	}
	/**
	*Return the normal parsing rule for all the variables in the dictionary
	*/
	public int[] getnormalruleforall()
	{
		int[] normalrule=new int[fixedvariableinfo.size()];
		for (int i=0; i<allvar.length; i++)
		{
			normalrule[i]=0;
			for (int j=0; j<groupvar.length; j++)
			{
				if (allvar[i].equalsIgnoreCase(groupvar[j]))
					normalrule[i]=1;
			}
			for (int j=0; j<analysisvar.length; j++)
			{
				if (allvar[i].equalsIgnoreCase(analysisvar[j]))
					normalrule[i]=2;
			}
			for (int j=0; j<weightvar.length; j++)
			{
				if (allvar[i].equalsIgnoreCase(weightvar[j]))
					normalrule[i]=3;
			}
		}
		return normalrule;
	}
	/**
	*Return the normal parsing rule for only the selected variables
	*/
	public int[] getnormalruleforsel()
	{
		int[] normalrule=new int[reqvar.length];
		for (int i=0; i<reqvar.length; i++)
		{
			normalrule[i]=0;
			for (int j=0; j<groupvar.length; j++)
			{
				if (reqvar[i].equalsIgnoreCase(groupvar[j]))
					normalrule[i]=1;
			}
			for (int j=0; j<analysisvar.length; j++)
			{
				if (reqvar[i].equalsIgnoreCase(analysisvar[j]))
					normalrule[i]=2;
			}
			for (int j=0; j<weightvar.length; j++)
			{
				if (reqvar[i].equalsIgnoreCase(weightvar[j]))
					normalrule[i]=3;
			}
		}
		return normalrule;
	}
	/**
	*Return the replace rule for all the variables in the dictionary
	*/
	public int[] getreplaceruleforall(String replace)
	{
		int[] replacerule=new int[fixedvariableinfo.size()];
		int rifrep=0;
		if (replace==null)
			rifrep=0;
		else if (replace.equalsIgnoreCase(Keywords.replaceall))
			rifrep=1;
		else if (replace.equalsIgnoreCase(Keywords.replaceformat))
			rifrep=2;
		else if (replace.equalsIgnoreCase(Keywords.replacemissing))
			rifrep=3;
		for (int i=0; i<allvar.length; i++)
		{
			replacerule[i]=rifrep;
			for (int j=0; j<groupvar.length; j++)
			{
				if (allvar[i].equalsIgnoreCase(groupvar[j]))
					replacerule[i]=1;
			}
			for (int j=0; j<weightvar.length; j++)
			{
				if (allvar[i].equalsIgnoreCase(weightvar[j]))
					replacerule[i]=1;
			}
		}
		return replacerule;
	}
	/**
	*Return the replace rule for the selected variables
	*/
	public int[] getreplaceruleforsel(String replace)
	{
		int[] replacerule=new int[reqvar.length];
		int rifrep=0;
		if (replace==null)
			rifrep=0;
		else if (replace.equalsIgnoreCase(Keywords.replaceall))
			rifrep=1;
		else if (replace.equalsIgnoreCase(Keywords.replaceformat))
			rifrep=2;
		else if (replace.equalsIgnoreCase(Keywords.replacemissing))
			rifrep=3;
		for (int i=0; i<reqvar.length; i++)
		{
			replacerule[i]=rifrep;
			for (int j=0; j<groupvar.length; j++)
			{
				if (reqvar[i].equalsIgnoreCase(groupvar[j]))
					replacerule[i]=1;
			}
			for (int j=0; j<weightvar.length; j++)
			{
				if (reqvar[i].equalsIgnoreCase(weightvar[j]))
					replacerule[i]=1;
			}
		}
		return replacerule;
	}
	/**
	*Return the grouping variables parsing rule for all the variables in the dictionary
	*/
	public int[] getgroupruleforall()
	{
		int[] grouprule=new int[fixedvariableinfo.size()];
		for (int i=0; i<allvar.length; i++)
		{
			grouprule[i]=0;
			for (int j=0; j<groupvar.length; j++)
			{
				if (allvar[i].equalsIgnoreCase(groupvar[j]))
					grouprule[i]=1;
			}
		}
		return grouprule;
	}
	/**
	*Return the grouping variables parsing rule for only the selected variables
	*/
	public int[] getgroupruleforsel()
	{
		int[] grouprule=new int[reqvar.length];
		for (int i=0; i<reqvar.length; i++)
		{
			grouprule[i]=0;
			for (int j=0; j<groupvar.length; j++)
			{
				if (reqvar[i].equalsIgnoreCase(groupvar[j]))
					grouprule[i]=1;
			}
		}
		return grouprule;
	}
	/**
	*Return the analysis variables parsing rule for all the variables in the dictionary
	*/
	public int[] getanalysisruleforall()
	{
		int[] analysisrule=new int[fixedvariableinfo.size()];
		for (int i=0; i<allvar.length; i++)
		{
			analysisrule[i]=0;
			for (int j=0; j<analysisvar.length; j++)
			{
				if (allvar[i].equalsIgnoreCase(analysisvar[j]))
					analysisrule[i]=1;
			}
		}
		return analysisrule;
	}
	/**
	*Return the grouping variables parsing rule for only the selected variables
	*/
	public int[] getanalysisruleforsel()
	{
		int[] analysisrule=new int[reqvar.length];
		for (int i=0; i<reqvar.length; i++)
		{
			analysisrule[i]=0;
			for (int j=0; j<analysisvar.length; j++)
			{
				if (reqvar[i].equalsIgnoreCase(analysisvar[j]))
					analysisrule[i]=1;
			}
		}
		return analysisrule;
	}
	/**
	*Return the row variables parsing rule for all the variables in the dictionary
	*/
	public int[] getrowruleforall()
	{
		int[] rowrule=new int[fixedvariableinfo.size()];
		for (int i=0; i<allvar.length; i++)
		{
			rowrule[i]=0;
			for (int j=0; j<rowvar.length; j++)
			{
				if (allvar[i].equalsIgnoreCase(rowvar[j]))
					rowrule[i]=1;
			}
		}
		return rowrule;
	}
	/**
	*Return the row variables parsing rule for only the selected variables
	*/
	public int[] getrowruleforsel()
	{
		int[] rowrule=new int[reqvar.length];
		for (int i=0; i<reqvar.length; i++)
		{
			rowrule[i]=0;
			for (int j=0; j<rowvar.length; j++)
			{
				if (reqvar[i].equalsIgnoreCase(rowvar[j]))
					rowrule[i]=1;
			}
		}
		return rowrule;
	}
	/**
	*Return the columns variables parsing rule for all the variables in the dictionary
	*/
	public int[] getcolruleforall()
	{
		int[] colrule=new int[fixedvariableinfo.size()];
		for (int i=0; i<allvar.length; i++)
		{
			colrule[i]=0;
			for (int j=0; j<colvar.length; j++)
			{
				if (allvar[i].equalsIgnoreCase(colvar[j]))
					colrule[i]=1;
			}
		}
		return colrule;
	}
	/**
	*Return the columns variables parsing rule for only the selected variables
	*/
	public int[] getcolruleforsel()
	{
		int[] colrule=new int[reqvar.length];
		for (int i=0; i<reqvar.length; i++)
		{
			colrule[i]=0;
			for (int j=0; j<colvar.length; j++)
			{
				if (reqvar[i].equalsIgnoreCase(colvar[j]))
					colrule[i]=1;
			}
		}
		return colrule;
	}
	/**
	*Return the weighting variable parsing rule for all the variables in the dictionary
	*/
	public int[] getweightruleforall()
	{
		int[] weightrule=new int[fixedvariableinfo.size()];
		for (int i=0; i<allvar.length; i++)
		{
			weightrule[i]=0;
			for (int j=0; j<weightvar.length; j++)
			{
				if (allvar[i].equalsIgnoreCase(weightvar[j]))
					weightrule[i]=1;
			}
		}
		return weightrule;
	}
	/**
	*Return the weighting variables parsing rule for only the selected variables
	*/
	public int[] getweightruleforsel()
	{
		int[] weightrule=new int[reqvar.length];
		for (int i=0; i<reqvar.length; i++)
		{
			weightrule[i]=0;
			for (int j=0; j<weightvar.length; j++)
			{
				if (reqvar[i].equalsIgnoreCase(weightvar[j]))
					weightrule[i]=1;
			}
		}
		return weightrule;
	}
}
