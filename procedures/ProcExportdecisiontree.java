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
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.VariableUtilities;

/**
* This is the procedure that export in an html file the decision tree
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcExportdecisiontree implements RunStep
{
	/**
	* Starts the execution of Proc Exportdecisiontree
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.treefile, Keywords.dict};
		String [] optionalparameters=new String[] {Keywords.useperc, Keywords.separate, Keywords.nounsure, Keywords.nogaininfo, Keywords.replace};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		boolean nogaininfo =(parameters.get(Keywords.nogaininfo)!=null);
		boolean nounsure =(parameters.get(Keywords.nounsure)!=null);
		boolean separate =(parameters.get(Keywords.separate)!=null);
		boolean useperc =(parameters.get(Keywords.useperc)!=null);
		String replace =(String)parameters.get(Keywords.replace);

		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		String outreport=(String)parameters.get(Keywords.treefile);
		if (!outreport.toLowerCase().endsWith(".html"))
			outreport=outreport+".html";

		BufferedWriter fileouthtml=null;
		boolean exist=(new File(outreport)).exists();
		if (exist)
		{
			boolean success = (new File(outreport)).delete();
			if (!success)
				return new Result("%1507%<br>\n", false, null);
		}
		try
		{
			fileouthtml = new BufferedWriter(new FileWriter(outreport, true));
		}
		catch (Exception e)
		{
			return new Result("%1508% ("+outreport+")<br>\n", false, null);
		}

		String content="";
		int totalnodes=0;
		boolean iscorrect=true;
		for (int i=0; i<dict.gettotalvar(); i++)
		{
			String tempname=dict.getvarname(i);
			if (tempname.startsWith("node_"))
			{
				try
				{
					tempname=tempname.substring(5);
					int rif=Integer.parseInt(tempname);
					if (rif>totalnodes)
						totalnodes=rif;
				}
				catch (Exception e)
				{
					iscorrect=false;
				}
			}
		}
		if (!iscorrect)
			return new Result("%1509%<br>\n", false, null);

		totalnodes++;

		String var="";
		for (int i=0; i<totalnodes; i++)
		{
			var=var+" node_"+String.valueOf(i);
		}
		for (int i=0; i<totalnodes; i++)
		{
			var=var+" val_"+String.valueOf(i);
		}
		if (!nogaininfo)
		{
			for (int i=0; i<totalnodes; i++)
			{
				var=var+" gain_"+String.valueOf(i);
			}
		}
		var=var.trim()+" decision freq freqp";

		VariableUtilities varu=new VariableUtilities(dict, null, null, null, var, null);
		if (varu.geterror())
			return new Result(varu.getmessage(), false, null);

		String[] reqvar=varu.getreqvar();

		int[] replacerule=varu.getreplaceruleforsel(replace);

		DataReader data = new DataReader(dict);

		String[] values=null;

		if (!data.open(reqvar, replacerule, true))
			return new Result(data.getmessage(), false, null);

		String testdecision="";
		int paths=0;
		if (!nounsure)
			paths=data.getRecords();
		else
		{
			while (!data.isLast())
			{
				values = data.getRecord();
				double tempfreqp=0;
				try
				{
					tempfreqp=Double.parseDouble(values[values.length-1]);
				}
				catch (Exception en) {}
				testdecision=values[values.length-3];
				if ((tempfreqp==100) && (!testdecision.equals("-")))
					paths++;
			}
			data.close();
			if (!data.open(reqvar, replacerule, true))
				return new Result(data.getmessage(), false, null);
		}

		String[][] totalvalues=new String[paths][totalnodes*2];
		String[][] totaldec=new String[paths][2];

		int pointer=0;
		while (!data.isLast())
		{
			values = data.getRecord();
			double tempfreqp=0;
			try
			{
				tempfreqp=Double.parseDouble(values[values.length-1]);
			}
			catch (Exception en) {}
			boolean loadval=true;
			testdecision=values[values.length-3];
			if ((tempfreqp!=100) && (nounsure))
				loadval=false;
			if ((testdecision.equals("-")) && (nounsure))
				loadval=false;
			if (loadval)
			{
				for (int i=0; i<totalnodes; i++)
				{
					totalvalues[pointer][i*2]=values[i];
				}
				for (int i=0; i<totalnodes; i++)
				{
					totalvalues[pointer][i*2+1]=values[i+totalnodes];
				}
				if (!nogaininfo)
				{
					for (int i=0; i<totalnodes; i++)
					{
						if (!totalvalues[pointer][i*2].equals(""))
							totalvalues[pointer][i*2]=totalvalues[pointer][i*2]+" ("+values[i+totalnodes+totalnodes]+")";
					}
				}
				totaldec[pointer][0]=values[values.length-3];
				if (!useperc)
					totaldec[pointer][1]=values[values.length-2];
				else
					totaldec[pointer][1]=values[values.length-1];
				pointer++;
			}
		}
		data.close();
		content="<html>\n";
		content=content+"<head>\n";
		content=content+"<meta name=\"GENERATOR\" content=\"ADaMSoft Report\">\n";
		content=content+"<title>"+dict.getdescription()+"</title>\n";
		content=content+"</head>\n";
		if (!separate)
		{
			content=content+"<table border=\"1\">\n";
			int[][] checkspan=new int[totalvalues.length][totalvalues[0].length];
			for (int i=0; i<totalvalues[0].length; i++)
			{
				int rowspan=0;
				for (int j=0; j<totalvalues.length; j++)
				{
					for (int k=j; k<totalvalues.length; k++)
					{
						if ((totalvalues[j][i].equals(totalvalues[k][i])) && (!totalvalues[j][i].equals("")))
						{
							rowspan++;
						}
						else
							break;
					}
					checkspan[j][i]=rowspan;

					if (rowspan>1)
					{
						if (i>0)
						{
							int morecheck=1;
							for (int k=j+1; k<j+rowspan; k++)
							{
								if ((totalvalues[j][i-1].equals(totalvalues[k][i-1])))
								{
									morecheck++;
								}
								else
									break;
							}
							if ((checkspan[j][i-1]<morecheck) && (checkspan[j][i-1]>0))
								morecheck=checkspan[j][i-1];
							if ((morecheck)!=rowspan)
							{
								rowspan=morecheck;
								checkspan[j][i]=rowspan;
							}
						}
						for (int k=j+1; k<j+rowspan; k++)
						{
							checkspan[k][i]=-1;
						}
						j=j+rowspan-1;
					}
					rowspan=0;
				}
			}
			int a=0;
			int b=1;
			for (int i=0; i<totalvalues[0].length; i++)
			{
				content=content+"<tr>\n";
				String templine="";
				for (int j=0; j<totalvalues.length; j++)
				{
					if(totalvalues[j][i].equals(""))
						templine=templine+"<td>&nbsp;</td>\n";
					else
					{
						if (checkspan[j][i]==1)
						{
							templine=templine+"<td align=center>";
							if (b==1)
								templine=templine+"<b>";
							templine=templine+escapeHtml(totalvalues[j][i]);
							if (b==1)
								templine=templine+"</b>";
							templine=templine+"</td>\n";
						}
						if (checkspan[j][i]>1)
						{
							templine=templine+"<td colspan="+String.valueOf(checkspan[j][i])+" align=center>";
							if (b==1)
								templine=templine+"<b>";
							templine=templine+escapeHtml(totalvalues[j][i]);
							if (b==1)
								templine=templine+"</b>";
							templine=templine+"</td>\n";
						}
					}
				}
				content=content+templine+"</tr>\n";
				if (b==1)
					b=0;
				if (a==1)
				{
					a=0;
					b=1;
				}
				if (b==0)
				{
					a=1;
				}
			}
			content=content+"<tr>\n";
			for (int i=0; i<totaldec.length; i++)
			{
				if (totaldec[i][0].equals(""))
					content=content+"<td>&nbsp;</td>\n";
				else
					content=content+"<td><b><i>"+escapeHtml(totaldec[i][0]+" ("+totaldec[i][1]+")")+"</i></b></td>\n";
			}
			content=content+"</tr>\n";
			content=content+"</table>\n";
		}
		else
		{
			Hashtable<String, Integer> numdiff=new Hashtable<String, Integer>();
			for (int i=0; i<totaldec.length; i++)
			{
				if (numdiff.get(totaldec[i][0])==null)
					numdiff.put(totaldec[i][0], new Integer(1));
				else
				{
					int tempnum=(numdiff.get(totaldec[i][0])).intValue()+1;
					numdiff.put(totaldec[i][0], new Integer(tempnum));
				}
			}
			for (Enumeration<String> en=numdiff.keys(); en.hasMoreElements();)
			{
				String yval=en.nextElement();
				int tempnum=(numdiff.get(yval)).intValue();
				String[][] totalvaluestemp=new String[tempnum][totalvalues[0].length];
				String[][] totaldectemp=new String[tempnum][2];
				int r=0;
				for (int i=0; i<totalvaluestemp.length; i++)
				{
					for (int j=0; j<totalvaluestemp[0].length; j++)
					{
						totalvaluestemp[i][j]="";
					}
				}
				for (int i=0; i<totaldec.length; i++)
				{
					if (totaldec[i][0].equals(yval))
					{
						for (int j=0; j<totalvalues[0].length; j++)
						{
							totalvaluestemp[r][j]=totalvalues[i][j];
						}
						totaldectemp[r][0]=totaldec[i][0];
						totaldectemp[r][1]=totaldec[i][1];
						r++;
					}
				}
				content=content+"<h1>"+yval+"</h1>\n<br>\n";
				content=content+"<table border=\"1\">\n";
				int[][] checkspan=new int[totalvaluestemp.length][totalvaluestemp[0].length];
				for (int i=0; i<totalvaluestemp[0].length; i++)
				{
					int rowspan=0;
					for (int j=0; j<totalvaluestemp.length; j++)
					{
						for (int k=j; k<totalvaluestemp.length; k++)
						{
							if ((totalvaluestemp[j][i].equals(totalvaluestemp[k][i])) && (!totalvaluestemp[j][i].equals("")))
							{
								rowspan++;
							}
							else
								break;
						}
						checkspan[j][i]=rowspan;
						if (rowspan>1)
						{
							if (i>0)
							{
								int morecheck=1;
								for (int k=j+1; k<j+rowspan; k++)
								{
									if ((totalvaluestemp[j][i-1].equals(totalvaluestemp[k][i-1])))
									{
										morecheck++;
									}
									else
										break;
								}
								if ((checkspan[j][i-1]<morecheck) && (checkspan[j][i-1]>0))
									morecheck=checkspan[j][i-1];
								if ((morecheck)!=rowspan)
								{
									rowspan=morecheck;
									checkspan[j][i]=rowspan;
								}
							}
							for (int k=j+1; k<j+rowspan; k++)
							{
								checkspan[k][i]=-1;
							}
							j=j+rowspan-1;
						}
						rowspan=0;
					}
				}
				int a=0;
				int b=1;
				for (int i=0; i<totalvaluestemp[0].length; i++)
				{
					int realpath=0;
					for (int j=0; j<totalvaluestemp.length; j++)
					{
						if (totalvaluestemp[j][i].equals(""))
							realpath++;
					}
					if (realpath!=totalvaluestemp.length)
					{
						content=content+"<tr>\n";
						String templine="";
						for (int j=0; j<totalvaluestemp.length; j++)
						{

							if(totalvaluestemp[j][i].equals(""))
								templine=templine+"<td>&nbsp;</td>\n";
							else
							{
								if (checkspan[j][i]==1)
								{
									templine=templine+"<td align=center>";
									if (b==1)
										templine=templine+"<b>";
									templine=templine+escapeHtml(totalvaluestemp[j][i]);
									if (b==1)
										templine=templine+"</b>";
									templine=templine+"</td>\n";
								}
								if (checkspan[j][i]>1)
								{
									templine=templine+"<td colspan="+String.valueOf(checkspan[j][i])+" align=center>";
									if (b==1)
										templine=templine+"<b>";
									templine=templine+escapeHtml(totalvaluestemp[j][i]);
									if (b==1)
										templine=templine+"</b>";
									templine=templine+"</td>\n";
								}
							}
						}
						content=content+templine+"</tr>\n";
						if (b==1)
							b=0;
						if (a==1)
						{
							a=0;
							b=1;
						}
						if (b==0)
						{
							a=1;
						}
					}
				}
				content=content+"<tr>\n";
				for (int i=0; i<totaldectemp.length; i++)
				{
					if (totaldectemp[i][0].equals(""))
						content=content+"<td>&nbsp;</td>\n";
					else
						content=content+"<td><b><i>\n"+escapeHtml(totaldectemp[i][0]+" ("+totaldectemp[i][1]+")")+"</i></b></td>\n";
				}
				content=content+"</tr>\n";
				content=content+"</table>\n";
				content=content+"<hr noshade>\n";
			}
		}

		content=content+"</html>\n";

		try
		{
			fileouthtml.write(content);
			content="";
			fileouthtml.close();
			return new Result("%1510% ("+outreport+")<br>\n", true, null);
		}
		catch (Exception e)
		{
			return new Result("%1508% ("+outreport+")<br>\n", false, null);
		}
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 1487, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.treefile, "filesave=.html", true, 1488, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.nounsure, "checkbox", false, 1494, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.separate, "checkbox", false, 1489, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.nogaininfo, "checkbox", false, 1490, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.useperc, "checkbox", false, 1511, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="1462";
		retprocinfo[1]="1506";
		return retprocinfo;
	}
}
