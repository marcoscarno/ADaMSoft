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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that export a dictionary in an html file
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class ProcDict2html implements RunStep
{
	boolean nomd;
	boolean nocodelabel;
	/**
	* Starts the execution of Proc Dict2html and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.dict, Keywords.htmlfile};
		String [] optionalparameters=new String[] {Keywords.nomd, Keywords.nocodelabel};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		nomd =(parameters.get(Keywords.nomd)!=null);
		nocodelabel =(parameters.get(Keywords.nocodelabel)!=null);

		DictionaryReader newdr = (DictionaryReader)parameters.get(Keywords.dict);

		String outreport=(String)parameters.get(Keywords.htmlfile);
		if (!outreport.toLowerCase().endsWith(".html"))
			outreport=outreport+".html";

		BufferedWriter fileouthtml=null;
		boolean exist=(new File(outreport)).exists();
		if (exist)
		{
			boolean success = (new File(outreport)).delete();
			if (!success)
				return new Result("%2419%<br>\n", false, null);
		}
		try
		{
			fileouthtml = new BufferedWriter(new FileWriter(outreport, true));
		}
		catch (Exception e)
		{
			return new Result("%2420% ("+outreport+")<br>\n", false, null);
		}

		String content="<html>\n<head>\n<meta name=\"GENERATOR\" content=\"ADaMSoft Exporting tool\">\n";
		String title=newdr.getDictPath();
		try
		{
			title=title.replaceAll("\\\\","/");
		}
		catch (Exception fs){}
		title=title.substring(title.lastIndexOf("/")+1);
		title=title.substring(0,title.indexOf("."));
		content=content+"<title>"+title+"</title>\n</head>\n<body>\n";
		content=content+"<p>&nbsp;</p><center><table border=\"1\"><caption>General information on the data set</caption>\n";
		content=content+"<tr><td>Creation date<td><td>"+newdr.getcreationdate()+"</td></tr>\n";
		content=content+"<tr><td>Author<td><td>"+newdr.getauthor()+"</td></tr>\n";
		content=content+"<tr><td>Data table type<td><td>"+newdr.getdatatabletype()+"</td></tr>\n";
		String desc=newdr.getdescription();
		String keyw=newdr.getkeyword();
		if (desc.equals(""))
			desc="&nbsp;";
		if (keyw.equals(""))
			keyw="&nbsp;";

		content=content+"<tr><td>Description<td><td>"+desc+"</td></tr>\n";
		content=content+"<tr><td>Keyword<td><td>"+keyw+"</td></tr>\n";
		content=content+"</table><hr noshade>\n";

		content=content+"<p>&nbsp;</p><center><table border=\"1\"><caption>Variables in the data set</caption>\n";
		content=content+"<tr>";
		content=content+"<th>Name</th>";
		content=content+"<th>Label</th>";
		content=content+"<th>Writing format</th>";
		content=content+"<th>Code label</th>";
		content=content+"<th>Missing data rules</th>";
		content=content+"</tr>\n";

		Vector<Hashtable<String, String>> newvar=newdr.getfixedvariableinfo();
		Vector<Hashtable<String, String>> codelabel=newdr.getcodelabel();
		Vector<Hashtable<String, String>> missingdata=newdr.getmissingdata();
		boolean ecl=false;
		boolean emd=false;
		for (int i=0; i<newvar.size(); i++)
		{
			Hashtable<String, String> currentvar=newvar.get(i);
			Hashtable<String, String> tempcodelabel=codelabel.get(i);
			Hashtable<String, String> tempmissingdata=missingdata.get(i);
			content=content+"<tr>\n";
			content=content+"<td>"+currentvar.get(Keywords.VariableName.toLowerCase())+"</td>\n";
			content=content+"<td>"+currentvar.get(Keywords.LabelOfVariable.toLowerCase())+"</td>\n";
			content=content+"<td>"+currentvar.get(Keywords.VariableFormat.toLowerCase())+"</td>\n";
			if (tempcodelabel.size()==0)
				content=content+"<td>No</td>\n";
			else
			{
				ecl=true;
				content=content+"<td>Yes</td>\n";
			}
			if (tempmissingdata.size()==0)
				content=content+"<td>No</td>\n";
			else
			{
				emd=true;
				content=content+"<td>Yes</td>\n";
			}
			content=content+"</tr>\n";
		}
		content=content+"</table>\n<hr noshade>\n";
		if (!nocodelabel)
		{
			if (!ecl)
				content=content+"<p>&nbsp;</p>No code label defined for the variables<hr noshade>\n";
			else
			{
				for (int i=0; i<newvar.size(); i++)
				{
					Hashtable<String, String> currentvar=newvar.get(i);
					Hashtable<String, String> tempcodelabel=codelabel.get(i);
					if (tempcodelabel.size()>0)
					{
						String varname=currentvar.get(Keywords.VariableName.toLowerCase());
						content=content+"<p>&nbsp;</p><center><table border=\"1\"><caption>"+varname+"</caption>\n";
						content=content+"<tr>";
						content=content+"<th>Code</th>";
						content=content+"<th>Label</th>";
						content=content+"</tr>\n";
						for (Enumeration<String> e = tempcodelabel.keys() ; e.hasMoreElements() ;)
						{
							String code = e.nextElement();
							String value= tempcodelabel.get(code);
							content=content+"<tr>\n";
							content=content+"<td>"+code+"</td>\n";
							content=content+"<td>"+value+"</td>\n";
							content=content+"</tr>\n";
						}
						content=content+"</table>\n";
						content=content+"<hr width=50%>\n";
						content=content+"<hr noshade>\n";
					}
				}
			}
		}
		if (!nomd)
		{
			if (!emd)
				content=content+"<p>&nbsp;</p>No missing data rules defined for the variables<hr noshade>\n";
			else
			{
				for (int i=0; i<newvar.size(); i++)
				{
					Hashtable<String, String> currentvar=newvar.get(i);
					Hashtable<String, String> tempmissingdata=missingdata.get(i);
					if (tempmissingdata.size()>0)
					{
						String varname=currentvar.get(Keywords.VariableName.toLowerCase());
						content=content+"<p>&nbsp;</p><center><table border=\"1\"><caption>"+varname+"</caption>\n";
						content=content+"<tr>";
						content=content+"<th>Rule</th>";
						content=content+"</tr>\n";
						for (Enumeration<String> e = tempmissingdata.keys() ; e.hasMoreElements() ;)
						{
							String code = e.nextElement();
							content=content+"<tr>\n";
							content=content+"<td>"+code+"</td>\n";
							content=content+"</tr>\n";
						}
						content=content+"</table>\n";
						content=content+"<hr width=50%>\n";
					}
				}
			}
		}
		content=content+"</body>\n</html>";
		try
		{
			fileouthtml.write(content);
			content="";
			fileouthtml.close();
			return new Result("%2421% ("+outreport+")<br>\n", true, null);
		}
		catch (Exception e)
		{
			return new Result("%2420% ("+outreport+")<br>\n", false, null);
		}
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 2415, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.htmlfile, "filesave=.html", true, 2416, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.nomd, "checkbox", false, 2417, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.nocodelabel, "checkbox", false, 2418, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="4166";
		retprocinfo[1]="2414";
		return retprocinfo;
	}
}
