/**
* Copyright © 2006-2010 CASPUR
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
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.NewWriteFormat;

/**
* Export one or more data sets in an html file
* @author mscarno@aspur.it
* @version 1.0.0, rev.: 25/09/10 by marco
*/
public class ReportHtml implements RunStep
{
	/**
	*Export a data set into an html file
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean sortable=false;
		boolean nocheckhtml=false;
		Object tempinfo=parameters.get("reportinfo");
		@SuppressWarnings("unchecked")
		Hashtable<Integer,?> reportinfo=(Hashtable<Integer,?>)tempinfo;

		int maxds=0;
		for (Enumeration<Integer> e = reportinfo.keys() ; e.hasMoreElements() ;)
		{
			int rifds = ((Integer)e.nextElement()).intValue();
			if (rifds>maxds)
				maxds=rifds;
		}
		String reportname=(String)parameters.get("reportname");
		String reportpath=(String)parameters.get("reportpath");
		sortable = (parameters.get(Keywords.sortable) != null);
		nocheckhtml = (parameters.get(Keywords.nocheckhtml) != null);
		if (reportpath.equals(""))
			reportpath=(String)parameters.get(Keywords.WorkDir);
		BufferedWriter fileouthtml=null;

		String outreport=reportpath+reportname+".html";
		boolean exist=(new File(outreport)).exists();
		if (exist)
		{
			boolean success = (new File(outreport)).delete();
			if (!success)
				return new Result("%482%\n", false, null);
		}
		try
		{
			fileouthtml = new BufferedWriter(new FileWriter(outreport, true));
		}
		catch (Exception e)
		{
			return new Result("%483%\n", false, null);
		}
		Object generalinfo=reportinfo.get(new Integer(0));
		@SuppressWarnings("rawtypes")
		Hashtable general=(Hashtable)generalinfo;

		String headerfile=null;
		try
		{
			headerfile=(String)general.get(Keywords.headerfile);
		}
		catch (Exception exe) {}

		String footerfile=null;
		try
		{
			footerfile=(String)general.get(Keywords.footerfile);
		}
		catch (Exception exe) {}

		String header="";
		if (headerfile!=null)
		{
			try
			{
				try
				{
					headerfile=headerfile.replaceAll("\\","/");
				}
				catch (Exception e) {}
				java.net.URL fileUrl;
				if((headerfile.toLowerCase()).startsWith("http"))
					fileUrl =  new java.net.URL(headerfile);
				else
				{
					File files=new File(headerfile);
					fileUrl = files.toURI().toURL();
				}
		        BufferedReader infh = new BufferedReader(new InputStreamReader(fileUrl.openStream()));
	        	String str;
	        	while ((str = infh.readLine()) != null)
	        	{
					header=header+str;
				}
				infh.close();
			}
			catch (Exception efh)
			{
				return new Result("%2283%\n"+efh.toString()+"\n", false, null);
			}
		}
		String footer="";
		if (footerfile!=null)
		{
			try
			{
				try
				{
					footerfile=footerfile.replaceAll("\\","/");
				}
				catch (Exception e) {}
				java.net.URL fileUrl;
				if((footerfile.toLowerCase()).startsWith("http"))
					fileUrl =  new java.net.URL(footerfile);
				else
				{
					File files=new File(footerfile);
					fileUrl = files.toURI().toURL();
				}
		        BufferedReader inff = new BufferedReader(new InputStreamReader(fileUrl.openStream()));
	        	String str;
	        	while ((str = inff.readLine()) != null)
	        	{
					footer=footer+str;
				}
				inff.close();
			}
			catch (Exception efh)
			{
				return new Result("%2284%\n"+efh.toString()+"\n", false, null);
			}
		}

		String htmltitle=null;
		try
		{
			htmltitle=(String)general.get(Keywords.htmltitle);
		}
		catch (Exception exe) {}
		String csspath=null;
		try
		{
			csspath=(String)general.get(Keywords.csspath);
		}
		catch (Exception exe) {}
		String background=null;
		try
		{
			background=(String)general.get(Keywords.background);
		}
		catch (Exception exe) {}
		String div=null;
		try
		{
			div=(String)general.get(Keywords.div);
		}
		catch (Exception exe) {}
		boolean insdiv=false;
		if (div!=null)
		{
			if ((div.trim()).equalsIgnoreCase("ON"))
				insdiv=true;
		}
		String content="";
		if (header.equals(""))
		{
			content=content+"<html>\n";
			content=content+"<head>\n";
			content=content+"<meta name=\"GENERATOR\" content=\"ADaMSoft Report\">\n";
			if (htmltitle!=null)
				content=content+"<title>"+htmltitle+"</title>\n";
			if (csspath!=null)
				content=content+"<LINK href=\""+csspath+"\" type=text/css rel=stylesheet>\n";
			if (sortable)
			{
				content=content+"<SCRIPT LANGUAGE=\"JavaScript\">\n";
				content=content+"var sortedOn = -1;\n";
				content=content+"function setDataType(cValue){\n";
				content=content+"	var isDate = new Date(cValue);\n";
				content=content+"	if (isDate == \"NaN\"){\n";
				content=content+"		if (isNaN(cValue)){\n";
				content=content+"			cValue = cValue.toUpperCase();\n";
				content=content+"			return cValue;}\n";
				content=content+"		else{\n";
				content=content+"			var myNum;\n";
				content=content+"			myNum = String.fromCharCode(48 + cValue.length) + cValue;\n";
				content=content+"			return myNum;}}\n";
				content=content+"	else{\n";
				content=content+"		var myDate = new String();\n";
				content=content+"		myDate = isDate.getFullYear() + \" \" ;\n";
				content=content+"		myDate = myDate + isDate.getMonth() + \" \";\n";
				content=content+"		myDate = myDate + isDate.getDate(); + \" \";\n";
				content=content+"		myDate = myDate + isDate.getHours(); + \" \";\n";
				content=content+"		myDate = myDate + isDate.getMinutes(); + \" \";\n";
				content=content+"		myDate = myDate + isDate.getSeconds();\n";
				content=content+"		return myDate;}}\n";
				content=content+"function sortTable(col, tableToSort){\n";
				content=content+"	var iCurCell = col + tableToSort.cols;\n";
				content=content+"	var totalRows = tableToSort.rows.length;\n";
				content=content+"	var bSort = 0;\n";
				content=content+"	var colArray = new Array();\n";
				content=content+"	var oldIndex = new Array();\n";
				content=content+"	var indexArray = new Array();\n";
				content=content+"	var bArray = new Array();\n";
				content=content+"	var newRow;\n";
				content=content+"	var newCell;\n";
				content=content+"	var i;\n";
				content=content+"	var c;\n";
				content=content+"	var j;\n";
				content=content+"	for (i=1; i < tableToSort.rows.length; i++){\n";
				content=content+"		colArray[i - 1] = setDataType(tableToSort.cells(iCurCell).innerText);\n";
				content=content+"		iCurCell = iCurCell + tableToSort.cols;}\n";
				content=content+"    for (i=0; i < colArray.length; i++){\n";
				content=content+"		bArray[i] = colArray[i];}\n";
				content=content+"	 if ((sortedOn>-1) && (col == sortedOn)){\n";
				content=content+"		colArray.reverse();}\n";
				content=content+"	else{\n";
				content=content+"		sortedOn=col;\n";
				content=content+"		colArray.sort();}\n";
				content=content+"	for (i=0; i < colArray.length; i++){\n";
				content=content+"		indexArray[i] = (i+1);\n";
				content=content+"		for(j=0; j < bArray.length; j++){\n";
				content=content+"			if (colArray[i] == bArray[j]){\n";
				content=content+"				for (c=0; c<i; c++){\n";
				content=content+"					if ( oldIndex[c] == (j+1) ){\n";
				content=content+"						bSort = 1;}}\n";
				content=content+"				if (bSort == 0){\n";
				content=content+"					oldIndex[i] = (j+1);}\n";
				content=content+"				bSort = 0;}}}\n";
				content=content+"	for (i=0; i<oldIndex.length; i++){\n";
				content=content+"		newRow = tableToSort.insertRow();\n";
				content=content+"		for (c=0; c<tableToSort.cols; c++){\n";
				content=content+"			newCell = newRow.insertCell();\n";
				content=content+"			newCell.innerHTML = tableToSort.rows(oldIndex[i]).cells(c).innerHTML;}}\n";
				content=content+"	for (i=1; i<totalRows; i++){\n";
				content=content+"		tableToSort.moveRow((tableToSort.rows.length -1),1);}\n";
				content=content+"	for (i=1; i<totalRows; i++){\n";
				content=content+"		tableToSort.deleteRow();}}\n";
				content=content+"</script>\n";
			}
			content=content+"</head><body>\n";
			if (background!=null)
				content=content+"<body BGCOLOR =\""+background+"\">\n";
		}
		else
		{
			content=header;
			if (htmltitle!=null)
			{
				try
				{
					content=content.replaceAll("TABLE OF RESULTS",htmltitle);
				}
				catch (Exception etitle){}
			}
		}
		try
		{
			fileouthtml.write(content);
			content="";
		}
		catch (Exception e)
		{
			return new Result("%484%\n", false, null);
		}
		try
		{
			for (int i=1; i<=maxds; i++)
			{
				Object tempdsinfo=reportinfo.get(new Integer(i));
				@SuppressWarnings("rawtypes")
				Hashtable dsinfo=(Hashtable)tempdsinfo;
				DictionaryReader dr=(DictionaryReader)dsinfo.get(Keywords.dict);
				String[] selectedvar=(String[])dsinfo.get(Keywords.var);
				String replace=(String)dsinfo.get(Keywords.replace);
				String nocaption=(String)dsinfo.get(Keywords.nocaption);
				boolean caption=true;
				if (nocaption!=null)
				{
					if (nocaption.equalsIgnoreCase("NO"))
						caption=false;
				}
				String borderwidth=(String)dsinfo.get(Keywords.borderwidth);
				String tbackground=(String)dsinfo.get(Keywords.background);
				String cellspacing=(String)dsinfo.get(Keywords.cellspacing);
				String cellpadding=(String)dsinfo.get(Keywords.cellpadding);
				String width=(String)dsinfo.get(Keywords.width);
				String height=(String)dsinfo.get(Keywords.height);
				String tablealign=(String)dsinfo.get(Keywords.tablealign);
				String numalign=(String)dsinfo.get(Keywords.numalign);
				String textalign=(String)dsinfo.get(Keywords.textalign);

				String firstrowanchor=(String)dsinfo.get(Keywords.firstrowanchor);
				String firstcolanchor=(String)dsinfo.get(Keywords.firstcolanchor);
				String cellanchor=(String)dsinfo.get(Keywords.cellanchor);

				content=content+"<table ";
				if (borderwidth!=null)
					content=content+" border=\""+borderwidth+"\"";
				if (tbackground!=null)
					content=content+" bgcolor=\""+tbackground+"\"";
				if (cellspacing!=null)
					content=content+" cellspacing=\""+cellspacing+"\"";
				if (cellpadding!=null)
					content=content+" cellpadding=\""+cellpadding+"\"";
				if (width!=null)
					content=content+" width=\""+width+"\"";
				if (height!=null)
					content=content+" height=\""+height+"\"";
				if (tablealign!=null)
					content=content+" align=\""+tablealign+"\"";
				DataReader data=new DataReader(dr);
				int totalvar=0;
				Vector<String> writeformat=new Vector<String>();
				if (selectedvar==null)
				{
					totalvar=dr.gettotalvar();
					selectedvar=new String[totalvar];
					for (int j=0; j<totalvar; j++)
					{
						selectedvar[j]=dr.getvarname(j);
						writeformat.add(dr.getvarformat(j));
					}
				}
				else
				{
					totalvar=selectedvar.length;
					for (int j=0; j<totalvar; j++)
					{
						writeformat.add(dr.getvarformatfromname(selectedvar[j]));
					}
				}
				if (sortable)
					content=content+" name=\"rsTable"+String.valueOf(i)+"\" id=rsTable"+String.valueOf(i)+" cols="+String.valueOf(totalvar)+"\n";
				content=content+">\n";
				if (caption)
				{
					String captionalign=(String)dsinfo.get(Keywords.captionalign);
					String captionfont=(String)dsinfo.get(Keywords.captionfont);
					String captionsize=(String)dsinfo.get(Keywords.captionsize);
					String captioncolor=(String)dsinfo.get(Keywords.captioncolor);
					content=content+"<caption>";
					if (captionalign==null)
						captionalign="";
					if (captionfont==null)
						captionfont="";
					if (captionsize==null)
						captionsize="";
					if (captioncolor==null)
						captioncolor="";
					if ((!captionalign.equals("")) || (!captionfont.equals("")) || (!captionsize.equals("")) || (!captioncolor.equals("")))
					{
						content=content+"<font ";
						if (captionalign!=null)
							content=content+"align=\""+captionalign+"\" ";
						if (captionfont!=null)
							content=content+"face=\""+captionfont+"\" ";
						if (captionsize!=null)
							content=content+"size=\""+captionsize+"\" ";
						if (captioncolor!=null)
							content=content+"color=\""+captioncolor+"\" ";
						content=content+">\n";
					}
					content=content+dr.getdescription();
					content=content+"</caption>\n";
				}
				int[] replacerule=new int[totalvar];
				if (replace==null)
				{
					for (int j=0; j<totalvar; j++)
					{
						replacerule[j]=1;
					}
				}
				else if (replace.equalsIgnoreCase(Keywords.replaceall))
					{
					for (int j=0; j<totalvar; j++)
					{
						replacerule[j]=1;
					}
				}
				else if (replace.equalsIgnoreCase(Keywords.replaceformat))
				{
					for (int j=0; j<totalvar; j++)
					{
						replacerule[j]=2;
					}
				}
				else if (replace.equalsIgnoreCase(Keywords.replacemissing))
				{
					for (int j=0; j<totalvar; j++)
					{
						replacerule[j]=3;
					}
				}
				else
				{
					for (int j=0; j<totalvar; j++)
					{
						replacerule[j]=1;
					}
				}

				String uselocalefornumbers=(String)dsinfo.get(Keywords.uselocalefornumbers);
				String numdecimals=(String)dsinfo.get(Keywords.numdecimals);
				int defdec=-1;
				if (numdecimals!=null)
				{
					try
					{
						defdec = Integer.parseInt(numdecimals);
					}
					catch(NumberFormatException  nfe){}
				}
				boolean isselnumasos=false;
				if (uselocalefornumbers==null)
					uselocalefornumbers="";
				if (uselocalefornumbers.equalsIgnoreCase("YES"))
					isselnumasos=true;

				boolean istowrite=true;
				boolean newrep=false;
				if (defdec>-1)
					newrep=true;
				if (isselnumasos)
					newrep=true;
				if (newrep)
					istowrite=false;

				if (!data.open(selectedvar, replacerule, istowrite))
				{
					fileouthtml.close();
					return new Result(data.getmessage(), false, null);
				}

				String labelcolor=(String)dsinfo.get(Keywords.labelcolor);
				String labelhalign=(String)dsinfo.get(Keywords.labelhalign);
				String labelvalign=(String)dsinfo.get(Keywords.labelvalign);
				String labelfont=(String)dsinfo.get(Keywords.labelfont);
				String labelsize=(String)dsinfo.get(Keywords.labelsize);
				String labelbgcolor=(String)dsinfo.get(Keywords.labelbgcolor);

				String varcolor=(String)dsinfo.get(Keywords.varcolor);
				String varhalign=(String)dsinfo.get(Keywords.varhalign);
				String varvalign=(String)dsinfo.get(Keywords.varvalign);
				String varfont=(String)dsinfo.get(Keywords.varfont);
				String varsize=(String)dsinfo.get(Keywords.varsize);
				String varbgcolor=(String)dsinfo.get(Keywords.varbgcolor);

				content=content+"<tr>\n";
				for (int j=0; j<totalvar; j++)
				{
					content=content+"<th ";
					if (labelbgcolor!=null)
						content=content+" bgcolor=\""+labelbgcolor+"\" ";
					if (labelhalign!=null)
						content=content+" align=\""+labelhalign+"\" ";
					if (labelvalign!=null)
						content=content+" valign=\""+labelvalign+"\" ";
					content=content+">";
					if (sortable)
						content=content+"<A href=\"javascript:sortTable("+String.valueOf(j)+", rsTable"+String.valueOf(i)+");\">\n";
					if (labelcolor==null)
						labelcolor="";
					if (labelfont==null)
						labelfont="";
					if (labelsize==null)
						labelsize="";
					if ((!labelcolor.equals("")) || (!labelfont.equals("")) || (!labelsize.equals("")))
					{
						content=content+"<font ";
						if (labelfont!=null)
							content=content+"face=\""+labelfont+"\" ";
						if (labelsize!=null)
							content=content+"size=\""+labelsize+"\" ";
						if (labelcolor!=null)
							content=content+"color=\""+labelcolor+"\" ";
						content=content+">\n";
					}
					if (firstrowanchor!=null)
					{
						content=content+"<A ";
						firstrowanchor=firstrowanchor.trim();
						boolean fqm=false;
						if (firstrowanchor.indexOf("href=")>=0)
						{
							if (firstrowanchor.indexOf("?")>0)
							{
								String firstapart=firstrowanchor.substring(0,firstrowanchor.indexOf("?")+1);
								String lastapart=firstrowanchor.substring(firstrowanchor.indexOf("?")+1);
								fqm=true;
								content=content+firstapart+"row="+escapeHtml(dr.getvarlabelfromname(selectedvar[j]))+"&"+escapeHtml(lastapart)+">\n";
							}
						}
						if (!fqm)
							content=content+firstrowanchor+">\n";
					}
					if (!nocheckhtml)
						content=content+escapeHtml(dr.getvarlabelfromname(selectedvar[j]));
					else
						content=content+dr.getvarlabelfromname(selectedvar[j]);
					if ((!labelcolor.equals("")) || (!labelfont.equals("")) || (!labelsize.equals("")))
						content=content+"</font>";
					if (firstrowanchor!=null)
						content=content+"</a>\n";
					if (sortable)
						content=content+"</a>\n";
					content=content+"</th>\n";
				}
				content=content+"</tr>\n";
				fileouthtml.write(content);
				content="";
				while (!data.isLast())
				{
					content=content+"<tr>\n";
					String[] values=data.getRecord();
					if (values==null)
					{
						fileouthtml.close();
						return new Result(data.getmessage(), false, null);
					}
					if (newrep==true)
					{
						values=NewWriteFormat.getwriteformat(values, writeformat, defdec, isselnumasos);
					}
					for (int j=0; j<values.length; j++)
					{
						if (values[j].trim().equals(""))
							values[j]="&nbsp;";
						content=content+"<td ";
						if (varbgcolor!=null)
							content=content+" bgcolor=\""+varbgcolor+"\" ";
						if (varhalign!=null)
							content=content+" align=\""+varhalign+"\" ";
						if (varvalign!=null)
							content=content+" valign=\""+varvalign+"\" ";
						if ((numalign!=null) && (writeformat.get(j).toUpperCase().startsWith(Keywords.NUMSuffix)))
							content=content+" align=\""+numalign+"\" ";
						if ((textalign!=null) && (writeformat.get(j).toUpperCase().startsWith(Keywords.TEXTSuffix)))
							content=content+" align=\""+textalign+"\" ";
						content=content+">";
						if (varcolor==null)
							varcolor="";
						if (varfont==null)
							varfont="";
						if (varsize==null)
							varsize="";
						if ((!varcolor.equals("")) || (!varfont.equals("")) || (!varsize.equals("")))
						{
							content=content+"<font ";
							if (varfont!=null)
								content=content+"face=\""+varfont+"\" ";
							if (varsize!=null)
								content=content+"size=\""+varsize+"\" ";
							if (varcolor!=null)
								content=content+"color=\""+varcolor+"\" ";
							content=content+">\n";
						}
						if (j==0)
						{
							if (firstcolanchor!=null)
							{
								content=content+"<A ";
								firstcolanchor=firstcolanchor.trim();
								boolean fqm=false;
								if (firstcolanchor.indexOf("href=")>=0)
								{
									if (firstcolanchor.indexOf("?")>0)
									{
										String firstapart=firstcolanchor.substring(0,firstcolanchor.indexOf("?")+1);
										String lastapart=firstcolanchor.substring(firstcolanchor.indexOf("?")+1);
										fqm=true;
										content=content+firstapart+"col="+escapeHtml(values[j])+"&"+escapeHtml(lastapart)+">\n";
									}
								}
								if (!fqm)
									content=content+firstcolanchor+">\n";
							}
							else
							{
								if (cellanchor!=null)
								{
									content=content+"<A ";
									cellanchor=cellanchor.trim();
									boolean fqm=false;
									if (cellanchor.indexOf("href=")>=0)
									{
										if (cellanchor.indexOf("?")>0)
										{
											String firstapart=cellanchor.substring(0,cellanchor.indexOf("?")+1);
											String lastapart=cellanchor.substring(cellanchor.indexOf("?")+1);
											fqm=true;
											content=content+firstapart+"row="+escapeHtml(dr.getvarlabelfromname(selectedvar[j]))+"&col="+escapeHtml(values[0].trim())+"&val="+escapeHtml(values[j])+"&"+escapeHtml(lastapart)+">\n";
										}
									}
									if (!fqm)
										content=content+cellanchor+">\n";
								}
							}
						}
						else
						{
							if (cellanchor!=null)
							{
								content=content+"<A ";
								cellanchor=cellanchor.trim();
								boolean fqm=false;
								if (cellanchor.indexOf("href=")>=0)
								{
									if (cellanchor.indexOf("?")>0)
									{
										String firstapart=cellanchor.substring(0,cellanchor.indexOf("?")+1);
										String lastapart=cellanchor.substring(cellanchor.indexOf("?")+1);
										fqm=true;
										content=content+firstapart+"row="+escapeHtml(dr.getvarlabelfromname(selectedvar[j]))+"&col="+escapeHtml(values[0].trim())+"&val="+escapeHtml(values[j])+"&"+lastapart+">\n";
									}
								}
								if (!fqm)
									content=content+cellanchor+">\n";
							}
						}
						if (!nocheckhtml)
							content=content+escapeHtml(values[j].trim());
						else
							content=content+values[j].trim();
						if (j==0)
						{
							if (firstcolanchor!=null)
							{
								content=content+"</a>\n";
							}
							else
							{
								if (cellanchor!=null)
								{
									content=content+"</a>\n";
								}
							}
						}
						else
						{
							if (cellanchor!=null)
							{
								content=content+"</A>\n";
							}
						}
						if ((!varcolor.equals("")) || (!varfont.equals("")) || (!varsize.equals("")))
							content=content+"</font>";
						content=content+"</td>\n";
					}
					content=content+"</tr>\n";
					fileouthtml.write(content);
					content="";
				}
				data.close();
				content=content+"</table>\n";
				if (insdiv)
					content=content+"<hr noshade>\n";
				content=content+"<p>&nbsp;</p>\n";
			}
			if (footer.equals(""))
			{
				content=content+"</body>\n";
				content=content+"</html>\n";
			}
			else
				content=content+footer;
			fileouthtml.write(content);
			content="";
			fileouthtml.close();
			return new Result("%711% ("+outreport+")\n", true, null);
		}
		catch (Exception e)
		{
			return new Result("%484%\n", false, null);
		}
	}
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.outreport+"=", "outdictreport", true, 450, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.htmllayout+"=" , "setting=htmllayout", false, 451, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.sortable, "checkbox", false, 2280, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.nocheckhtml, "checkbox", false, 2285, dep, "", 1));
		return parameters;
	}
	public String[] getstepinfo()
	{
		String[] info=new String[2];
		info[0]="428";
		info[1]="428";
		return info;
	}
}
