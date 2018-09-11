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

import java.net.URL;
import java.net.InetAddress;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.TreeSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

import ADaMSoft.keywords.Keywords;

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.procedures.LocalDictionaryWriter;

/**
* This class contains different functions that can be used as utilities inside a broker
* @author marco.scarno@gmail.com
* @date 02/09/2015
*/

public class ADaMSoftWebFunctions
{
	/**
	*Save the String text inside an html file
	*/
	public boolean SAVESTRINGINHTML(String text, String htmlfile)
	{
		try
		{
			try
			{
				htmlfile=htmlfile.replaceAll("\\","/");
			}
			catch (Exception e) {}
			if (!htmlfile.toLowerCase().endsWith(".html"))
				htmlfile=htmlfile+".html";
			File newFile = new File(htmlfile);
			boolean exists = (newFile.exists());
			if (exists)
			{
				boolean resdel=newFile.delete();
				if (!resdel)
					return false;
			}
			FileWriter writer = new FileWriter(newFile);
			PrintWriter outfile = new PrintWriter(writer);
			outfile.println(text);
			outfile.close();
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	/**
	*Transform the current String text for an html file adding the header and the footer
	*/
	public String SETSTRINGFORHTML(String text)
	{
		String headder="<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">\n";
		headder=headder+"<HTML>\n";
		headder=headder+"<HEAD>\n";
		headder=headder+"<TITLE>ADaMSoft</TITLE>\n";
		headder=headder+"</HEAD>\n";
		headder=headder+"<BODY>\n";
		text=headder+text+"</BODY>\n</HTML>\n";
		return text;
	}
	/**
	*Transform the current String text for an html file adding the header and the footer
	*/
	public String ADDHTMLHEADERTOSTRING(String text)
	{
		String headder="<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">\n";
		headder=headder+"<HTML>\n";
		headder=headder+"<HEAD>\n";
		headder=headder+"<TITLE>ADaMSoft</TITLE>\n";
		headder=headder+"</HEAD>\n";
		headder=headder+"<BODY>\n";
		text=headder+text;
		return text;
	}
	/**
	*Transform the current String text for an html file adding the header and the footer
	*/
	public String ADDHTMLFOOTERTOSTRING(String text)
	{
		text=text+"</BODY>\n</HTML>\n";
		return text;
	}
	/**
	*Used to replace special characters in a string to be html compliant
	*/
	public String HTMLCOMPLIANT(String text)
	{
		StringBuffer sb = new StringBuffer();
		int n = text.length();
		for (int i = 0; i < n; i++)
		{
			char c = text.charAt(i);
			int h=(int)c;
			switch (h)
			{
				case 60: sb.append("&lt;"); break;
				case 34: sb.append("&quot;"); break;
				case 38: sb.append("&amp;"); break;
				case 62: sb.append("&gt;"); break;
				case 176: sb.append("&deg;"); break;
				case 177: sb.append("&plusmn;"); break;
				case 224: sb.append("&agrave;"); break;
				case 225: sb.append("&aacute;"); break;
				case 232: sb.append("&egrave;"); break;
				case 233: sb.append("&eacute;"); break;
				case 236: sb.append("&igrave;"); break;
				case 237: sb.append("&iacute;"); break;
				case 242: sb.append("&ograve;"); break;
				case 243: sb.append("&oacute;"); break;
				case 249: sb.append("&ugrave;"); break;
				case 250: sb.append("&uacute;"); break;
				case 10: sb.append("<br>");break;
				default:  sb.append(c); break;
			}
		}
		text=sb.toString();
		try
		{
			text=text.replaceAll("&amp;nb","&nb");
			text=text.replaceAll("%n","<br>");
		}
		catch (Exception es) {}
		return text;
	}
	/**
	*Used to replace special characters in a string to be html compliant
	*/
	public String HTML2VALUE(String text)
	{
		try{text=text.replaceAll("&lt;",String.valueOf((char)60));}catch (Exception ec){}
		try{text=text.replaceAll("&gt;",String.valueOf((char)62));}catch (Exception ec){}
		try{text=text.replaceAll("&amp;",String.valueOf((char)38));}catch (Exception ec){}
		try{text=text.replaceAll("&quot;",String.valueOf((char)34));}catch (Exception ec){}
		return text;
	}
	/**
	*Change the current html String by modifying the default title
	*/
	public String CHANGEDEFAULTTITLEINHTML(String text, String newtitle)
	{
		if (text.indexOf("<TITLE>ADaMSoft</TITLE>")>=0)
		{
			try
			{
				text=text.replaceAll("<TITLE>ADaMSoft</TITLE>","<TITLE>"+HTMLCOMPLIANT(newtitle)+"</TITLE>");
			}
			catch (Exception es) {}
		}
		else if (text.indexOf("<TITLE>")>0)
		{
			String part1=text.substring(0, text.indexOf("<TITLE>"));
			String part2=text.substring(text.indexOf("</TITLE>"));
			text=part1+HTMLCOMPLIANT(newtitle)+part2;
		}
		return text;
	}
	/**
	*Add a link to a style sheet to the current html text
	*/
	public String ADDSTYLESHEET(String text, String stylepath)
	{
		try
		{
			text=text.replaceAll("</HEAD>","<LINK href=\""+stylepath+"\" type=text/css rel=stylesheet></HEAD>");
		}
		catch (Exception es){}
		return text;
	}
	/**
	*Set the bg color to the current html text
	*/
	public String SETBGCOLOR(String text, String bgcolor)
	{
		try
		{
			text=text.replaceAll("<BODY>","<BODY bgColor=#"+bgcolor+">");
		}
		catch (Exception es) {}
		return text;
	}
	/**
	*Returns the parameter value from the broker by verifyng if it is valid
	*/
	public String GETPARVALUE(String parameter)
	{
		if (parameter.startsWith("&"))
			return "";
		return parameter;
	}
	/**
	*Returns the value of a variable inside a data set that corresponds to the value of another variable in the same data set using the replace rule ALL
	*/
	public String GETVALUEFROMDS(String dictionary, String refvarname, String refvarvalue, String refvartouse)
	{
		return GETVALUEFROMDS(dictionary, refvarname, refvarvalue, refvartouse, 1);
	}
	/**
	*Returns the value of a variable inside a data set that corresponds to the value of another variable in the same data set
	*/
	public String GETVALUEFROMDS(String dictionary, String refvarname, String refvarvalue, String refvartouse, int replacerule)
	{
		refvarvalue=GETPARVALUE(refvarvalue);
		try
		{
			DictionaryReader dr=new DictionaryReader(dictionary);
			String formatvar="";
			for (int i=0; i<dr.gettotalvar(); i++)
			{
				if (refvartouse.equalsIgnoreCase(dr.getvarname(i)))
				{
					formatvar=dr.getvarformat(i);
					break;
				}
			}
			if (formatvar.equals(""))
				return null;
			DataReader data = new DataReader(dr);
			int totvar=2;
			if (refvarname.equalsIgnoreCase(refvartouse))
				totvar=1;
			String[] reqvar=new String[totvar];
			reqvar[0]=refvarname;
			if (!refvarname.equalsIgnoreCase(refvartouse))
				reqvar[1]=refvartouse;
			int[] repvar=new int[totvar];
			repvar[0]=replacerule;
			if (!refvarname.equalsIgnoreCase(refvartouse))
				repvar[1]=replacerule;
			if (!data.open(reqvar, repvar, false))
				return null;
			String[] values=null;
			while (!data.isLast())
			{
				values = data.getRecord();
				try
				{
					double t1=Double.parseDouble(values[0]);
					double t2=Double.parseDouble(refvarvalue);
					if (t1==t2)
					{
						String valtoret=values[totvar-1];
						data.close();
						return valtoret;
					}
				}
				catch (Exception e)
				{
					if (values[0].equalsIgnoreCase(refvarvalue))
					{
						String valtoret=values[totvar-1];
						data.close();
						return valtoret;
					}
				}
			}
			data.close();
			return "";
		}
		catch (Exception ee) {}
		return null;
	}
	/**
	*Returns the value of a variable inside a data set that corresponds to the values of two variables in the same data set using the replace rule ALL
	*/
	public String GETVALUEFROMDS(String dictionary, String refvarname1, String refvarvalue1, String refvarname2, String refvarvalue2, String refvartouse)
	{
		return GETVALUEFROMDS(dictionary, refvarname1, refvarvalue1, refvarname2, refvarvalue2, refvartouse, 1);
	}
	/**
	*Returns the value of a variable inside a data set that corresponds to the values of two variables in the same data set
	*/
	public String GETVALUEFROMDS(String dictionary, String refvarname1, String refvarvalue1, String refvarname2, String refvarvalue2, String refvartouse, int replacerule)
	{
		refvarvalue1=GETPARVALUE(refvarvalue1);
		refvarvalue2=GETPARVALUE(refvarvalue2);
		try
		{
			DictionaryReader dr=new DictionaryReader(dictionary);
			String formatvar="";
			for (int i=0; i<dr.gettotalvar(); i++)
			{
				if (refvartouse.equalsIgnoreCase(dr.getvarname(i)))
				{
					formatvar=dr.getvarformat(i);
					break;
				}
			}
			if (formatvar.equals(""))
				return null;
			DataReader data = new DataReader(dr);
			String[] reqvar=new String[3];
			reqvar[0]=refvarname1;
			reqvar[1]=refvarname2;
			reqvar[2]=refvartouse;
			int[] repvar=new int[3];
			repvar[0]=replacerule;
			repvar[1]=replacerule;
			repvar[2]=replacerule;
			if (!data.open(reqvar, repvar, false))
				return null;
			String[] values=null;
			int pointer=0;
			while (!data.isLast())
			{
				values = data.getRecord();
				try
				{
					double t1=Double.parseDouble(values[0]);
					double t2=Double.parseDouble(refvarvalue1);
					if (t1==t2)
						pointer++;
				}
				catch (Exception e)
				{
					if (values[0].equalsIgnoreCase(refvarvalue1))
						pointer++;
				}
				try
				{
					double t1=Double.parseDouble(values[1]);
					double t2=Double.parseDouble(refvarvalue2);
					if (t1==t2)
						pointer++;
				}
				catch (Exception e)
				{
					if (values[1].equalsIgnoreCase(refvarvalue2))
						pointer++;
				}
				if (pointer==2)
				{
					String valtoret=values[2];
					data.close();
					return valtoret;
				}
			}
			data.close();
			return "";
		}
		catch (Exception e) {}
		return null;
	}
	/**
	*Returns a String that contains the content of a file
	*/
	public String GETFILE(String file)
	{
		String retval="";
		try
		{
			try
			{
				file=file.replaceAll("\\","/");
			}
			catch (Exception e) {}
			java.net.URL fileUrl;
			if((file.toLowerCase()).startsWith("http"))
				fileUrl =  new java.net.URL(file);
			else
			{
				File files=new File(file);
				fileUrl = files.toURI().toURL();
			}
	        BufferedReader in = new BufferedReader(new InputStreamReader(fileUrl.openStream()));
        	String str;
        	while ((str = in.readLine()) != null)
        	{
				retval=retval+str;
			}
			in.close();
			return retval;
		}
		catch (Exception e) {}
		return null;
	}
	/**
	*Add the javascript code that can be used to disable the execute button
	*/
	public String ADDDISBUT(String text, String buttonname)
	{
		try
		{
			text=text.replaceAll("</HEAD>","</HEAD>\n<SCRIPT LANGUAGE=\"JavaScript\">\n function disabutt(form){form."+buttonname+".disabled=true }\n</SCRIPT>}\n");
			return text;
		}
		catch (Exception es)
		{
			return null;
		}
	}
	/**
	*Add the form value by receiving the broker path, the method and a boolean that add the function to disable the button
	*/
	public String ADDFORMACTION(String text, String brkpath, String method, boolean disbut)
	{
		try
		{
			text=text+"<FORM action=\""+brkpath+"\"method=\""+method+"\"";
			if (disbut)
				text=text+" onSubmit=\"return disabutt(this)\"";
			text=text+">\n";
			return text;
		}
		catch (Exception e)
		{
			return null;
		}
	}
	/**
	*Add the form value by receiving the broker path, the method and a boolean that add the function to disable the button
	*/
	public String ADDSCRIPTREF(String text, String scriptpath)
	{
		try
		{
			text=text+"<input type=\"hidden\" name=\"_SCRIPT\" value=\""+scriptpath+"\">\n";
			return text;
		}
		catch (Exception e)
		{
			return null;
		}
	}
	/**
	*Returns true if a dictionary exists
	*/
	public boolean CHECKDS(String dictionary)
	{
		try
		{
			DictionaryReader dr=new DictionaryReader(dictionary);
			if (!dr.getmessageDictionaryReader().equals(""))
				return false;
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	/**
	*Returns true if a dictionary exists
	*/
	public boolean CHECKDS(String dsname, String dspath)
	{
	    if (!dspath.endsWith(System.getProperty("file.separator")))
	    	dspath=dspath+System.getProperty("file.separator");
	    return CHECKDS(dspath+dsname);
	}
	/**
	*Returns the number of records from contained into a data set
	*/
	public int GETNUMRECORDSFROMDS(String dictionary)
	{
		try
		{
			DictionaryReader dr=new DictionaryReader(dictionary);
			DataReader data = new DataReader(dr);
			return data.getRecords();
		}
		catch (Exception e) {}
		return 0;
	}
	/**
	*Returns the number of records from contained into a data set
	*/
	public int GETNUMRECORDSFROMDS(String dsname, String dspath)
	{
	    if (!dspath.endsWith(System.getProperty("file.separator")))
	    	dspath=dspath+System.getProperty("file.separator");
	    return GETNUMRECORDSFROMDS(dspath+dsname);
	}
	/**
	*Creates a data set
	*/
	public boolean CREATEDS(String dsname, String dspath, String[] varnames, String[] varlabels, String[] varformats, String keyword, String description, String[] valuestowrite)
	{
		try
		{
			String author="LOCALHOST";
			try
			{
		        InetAddress addr = InetAddress.getLocalHost();
		        author=addr.toString();
		    }
		    catch (Exception ex) {}
		    Hashtable<String, Object> parameters=new Hashtable<String, Object>();
		    if (!dspath.endsWith(System.getProperty("file.separator")))
		    	dspath=dspath+System.getProperty("file.separator");
		    parameters.put("out_dict", dspath);
		    parameters.put("out_data", dspath);
		    parameters.put("out", dsname);
		    parameters.put(Keywords.WorkDir, System.getProperty(Keywords.WorkDir));
			DataWriter dw=new DataWriter(parameters, "out");
			if (!dw.getmessage().equals(""))
				return false;
			DataSetUtilities dsu=new DataSetUtilities();
			Hashtable<String, String> temp=new Hashtable<String, String>();
			for (int i=0; i<varnames.length; i++)
			{
				dsu.addnewvar(varnames[i], varlabels[i], varformats[i], temp, temp);
			}
			if (!dw.opendatatable(dsu.getfinalvarinfo()))
				return false;
			for (int i=0; i<valuestowrite.length; i++)
			{
				valuestowrite[i]=GETPARVALUE(valuestowrite[i]);
			}
			dw.write(valuestowrite);
			boolean resclose=dw.close();
			if (!resclose)
				return false;
			LocalDictionaryWriter ldw=new LocalDictionaryWriter(dw.getdictpath(), keyword, description, author, dw.gettabletype(),
			dw.getTableInfo(), dsu.getfinalvarinfo(), dw.getVarInfo(), dsu.getfinalcl(), dsu.getfinalmd(), null);
			String resw=ldw.action();
			if (resw.startsWith("0"))
				return false;
			return true;
		}
		catch (Exception e) {}
		return false;
	}
	/**
	*Creates a data set
	*/
	public boolean CREATEDS(String dsname, String dspath, String[] varnames, String[] varlabels, String[] varformats, String keyword, String description, Vector<String[]> valuestowrite)
	{
		try
		{
			String author="LOCALHOST";
			try
			{
		        InetAddress addr = InetAddress.getLocalHost();
		        author=addr.toString();
		    }
		    catch (Exception ex) {}
		    Hashtable<String, Object> parameters=new Hashtable<String, Object>();
		    if (!dspath.endsWith(System.getProperty("file.separator")))
		    	dspath=dspath+System.getProperty("file.separator");
		    parameters.put("out_dict", dspath);
		    parameters.put("out_data", dspath);
		    parameters.put("out", dsname);
		    parameters.put(Keywords.WorkDir, System.getProperty(Keywords.WorkDir));
			DataWriter dw=new DataWriter(parameters, "out");
			if (!dw.getmessage().equals(""))
				return false;
			DataSetUtilities dsu=new DataSetUtilities();
			Hashtable<String, String> temp=new Hashtable<String, String>();
			for (int i=0; i<varnames.length; i++)
			{
				dsu.addnewvar(varnames[i], varlabels[i], varformats[i], temp, temp);
			}
			if (!dw.opendatatable(dsu.getfinalvarinfo()))
				return false;
			for (int i=0; i<valuestowrite.size(); i++)
			{
				String[] tempvtw=valuestowrite.get(i);
				for (int j=0; j<tempvtw.length; j++)
				{
					tempvtw[j]=GETPARVALUE(tempvtw[j]);
				}
				dw.write(tempvtw);
			}
			boolean resclose=dw.close();
			if (!resclose)
				return false;
			LocalDictionaryWriter ldw=new LocalDictionaryWriter(dw.getdictpath(), keyword, description, author, dw.gettabletype(),
			dw.getTableInfo(), dsu.getfinalvarinfo(), dw.getVarInfo(), dsu.getfinalcl(), dsu.getfinalmd(), null);
			String resw=ldw.action();
			if (resw.startsWith("0"))
				return false;
			return true;
		}
		catch (Exception e) {}
		return false;
	}
	/**
	*Add a record to a data set
	*/
	public boolean ADDRECORDTODS(String dsname, String dspath, String[] varnames, String[] newvaluestowrite)
	{
		try
		{
		    if (!dspath.endsWith(System.getProperty("file.separator")))
		    	dspath=dspath+System.getProperty("file.separator");
		    Hashtable<String, Object> parameters=new Hashtable<String, Object>();
		    if (!dspath.endsWith(System.getProperty("file.separator")))
		    	dspath=dspath+System.getProperty("file.separator");
		    parameters.put("out_dict", dspath);
		    parameters.put("out_data", dspath);
		    parameters.put("out", dsname);
		    parameters.put(Keywords.WorkDir, System.getProperty(Keywords.WorkDir));
			DataWriter dw=new DataWriter(parameters, "out");
			if (!dw.getmessage().equals(""))
				return false;
			DictionaryReader dr=new DictionaryReader(dspath+dsname);
			String keyword=dr.getkeyword();
			String description=dr.getdescription();
			String author=dr.getauthor();

			int[] varposition=new int[varnames.length];
			for (int j=0; j<varnames.length; j++)
			{
				newvaluestowrite[j]=GETPARVALUE(newvaluestowrite[j]);
				for (int i=0; i<dr.gettotalvar(); i++)
				{
					if (dr.getvarname(i).equalsIgnoreCase(varnames[j]))
						varposition[j]=i;
				}
			}
			DataReader data = new DataReader(dr);
			String[] reqvar=new String[dr.gettotalvar()];
			int[] repvar=new int[dr.gettotalvar()];
			String[] valuestowrite=new String[dr.gettotalvar()];
			for (int i=0; i<dr.gettotalvar(); i++)
			{
				valuestowrite[i]="";
				reqvar[i]=dr.getvarname(i);
				repvar[i]=0;
			}
			if (!data.open(reqvar, repvar, false))
				return false;
			DataSetUtilities dsu=new DataSetUtilities();
			dsu.defineolddict(dr);
			if (!dw.opendatatable(dsu.getfinalvarinfo()))
				return false;
			String[] values=null;
			while (!data.isLast())
			{
				values = data.getRecord();
				dw.write(values);
			}
			data.close();
			for (int i=0; i<varposition.length; i++)
			{
				valuestowrite[varposition[i]]=newvaluestowrite[i];
			}
			dw.write(valuestowrite);
			boolean resclose=dw.close();
			if (!resclose)
				return false;
			LocalDictionaryWriter ldw=new LocalDictionaryWriter(dw.getdictpath(), keyword, description, author, dw.gettabletype(),
			dw.getTableInfo(), dsu.getfinalvarinfo(), dw.getVarInfo(), dsu.getfinalcl(), dsu.getfinalmd(), null);
			String resw=ldw.action();
			if (resw.startsWith("0"))
				return false;
			return true;
		}
		catch (Exception e) {}
		return false;
	}
	/**
	*Modifies the content of a record, that is identified by an array of values referred to the correspondent variables
	*/
	public boolean SETRECORDTODS(String dsname, String dspath, String[] varnames, String[] newvaluestowrite, String[] refvalues, String[] refvarnames, boolean ignorecase)
	{
		try
		{
		    if (!dspath.endsWith(System.getProperty("file.separator")))
		    	dspath=dspath+System.getProperty("file.separator");
		    Hashtable<String, Object> parameters=new Hashtable<String, Object>();
		    if (!dspath.endsWith(System.getProperty("file.separator")))
		    	dspath=dspath+System.getProperty("file.separator");
		    parameters.put("out_dict", dspath);
		    parameters.put("out_data", dspath);
		    parameters.put("out", dsname);
		    parameters.put(Keywords.WorkDir, System.getProperty(Keywords.WorkDir));
			DataWriter dw=new DataWriter(parameters, "out");
			if (!dw.getmessage().equals(""))
				return false;
			DictionaryReader dr=new DictionaryReader(dspath+dsname);
			String keyword=dr.getkeyword();
			String description=dr.getdescription();
			String author=dr.getauthor();

			int[] varposition=new int[varnames.length];
			for (int j=0; j<varnames.length; j++)
			{
				newvaluestowrite[j]=GETPARVALUE(newvaluestowrite[j]);
				for (int i=0; i<dr.gettotalvar(); i++)
				{
					if (dr.getvarname(i).equalsIgnoreCase(varnames[j]))
						varposition[j]=i;
				}
			}

			int[] refvarposition=new int[refvarnames.length];
			for (int j=0; j<refvarnames.length; j++)
			{
				refvalues[j]=GETPARVALUE(refvalues[j]);
				for (int i=0; i<dr.gettotalvar(); i++)
				{
					if (dr.getvarname(i).equalsIgnoreCase(refvarnames[j]))
						refvarposition[j]=i;
				}
			}
			DataReader data = new DataReader(dr);
			String[] reqvar=new String[dr.gettotalvar()];
			int[] repvar=new int[dr.gettotalvar()];
			for (int i=0; i<dr.gettotalvar(); i++)
			{
				reqvar[i]=dr.getvarname(i);
				repvar[i]=0;
			}
			if (!data.open(reqvar, repvar, false))
				return false;
			DataSetUtilities dsu=new DataSetUtilities();
			dsu.defineolddict(dr);
			if (!dw.opendatatable(dsu.getfinalvarinfo()))
				return false;
			String[] values=null;
			int score=0;
			while (!data.isLast())
			{
				values = data.getRecord();
				score=0;
				for (int i=0; i<refvarposition.length; i++)
				{
					if (ignorecase)
					{
						try
						{
							double t1=Double.parseDouble(values[refvarposition[i]]);
							double t2=Double.parseDouble(refvalues[i]);
							if (t1==t2)
								score++;
						}
						catch (Exception e)
						{
							if (values[refvarposition[i]].equalsIgnoreCase(refvalues[i]))
								score++;
						}
					}
					else
					{
						try
						{
							double t1=Double.parseDouble(values[refvarposition[i]]);
							double t2=Double.parseDouble(refvalues[i]);
							if (t1==t2)
								score++;
						}
						catch (Exception e)
						{
							if (values[refvarposition[i]].equals(refvalues[i]))
								score++;
						}
					}
				}
				if (score==refvarposition.length)
				{
					for (int i=0; i<varposition.length; i++)
					{
						values[varposition[i]]=newvaluestowrite[i];
					}
				}
				dw.write(values);
			}
			data.close();
			boolean resclose=dw.close();
			if (!resclose)
				return false;
			LocalDictionaryWriter ldw=new LocalDictionaryWriter(dw.getdictpath(), keyword, description, author, dw.gettabletype(),
			dw.getTableInfo(), dsu.getfinalvarinfo(), dw.getVarInfo(), dsu.getfinalcl(), dsu.getfinalmd(), null);
			String resw=ldw.action();
			if (resw.startsWith("0"))
				return false;
			return true;
		}
		catch (Exception e) {}
		return false;
	}
	/**
	*Deletes a record that is identified by an array of values referred to the correspondent variables
	*/
	public boolean DELRECORDFROMDS(String dsname, String dspath, String[] refvalues, String[] refvarnames, boolean ignorecase)
	{
		try
		{
		    if (!dspath.endsWith(System.getProperty("file.separator")))
		    	dspath=dspath+System.getProperty("file.separator");
		    Hashtable<String, Object> parameters=new Hashtable<String, Object>();
		    if (!dspath.endsWith(System.getProperty("file.separator")))
		    	dspath=dspath+System.getProperty("file.separator");
		    parameters.put("out_dict", dspath);
		    parameters.put("out_data", dspath);
		    parameters.put("out", dsname);
		    parameters.put(Keywords.WorkDir, System.getProperty(Keywords.WorkDir));
			DataWriter dw=new DataWriter(parameters, "out");
			if (!dw.getmessage().equals(""))
				return false;
			DictionaryReader dr=new DictionaryReader(dspath+dsname);
			String keyword=dr.getkeyword();
			String description=dr.getdescription();
			String author=dr.getauthor();

			int[] refvarposition=new int[refvarnames.length];
			for (int j=0; j<refvarnames.length; j++)
			{
				refvalues[j]=GETPARVALUE(refvalues[j]);
				for (int i=0; i<dr.gettotalvar(); i++)
				{
					if (dr.getvarname(i).equalsIgnoreCase(refvarnames[j]))
						refvarposition[j]=i;
				}
			}
			DataReader data = new DataReader(dr);
			String[] reqvar=new String[dr.gettotalvar()];
			int[] repvar=new int[dr.gettotalvar()];
			for (int i=0; i<dr.gettotalvar(); i++)
			{
				reqvar[i]=dr.getvarname(i);
				repvar[i]=0;
			}
			if (!data.open(reqvar, repvar, false))
				return false;
			DataSetUtilities dsu=new DataSetUtilities();
			dsu.defineolddict(dr);
			if (!dw.opendatatable(dsu.getfinalvarinfo()))
				return false;
			String[] values=null;
			int score=0;
			while (!data.isLast())
			{
				values = data.getRecord();
				score=0;
				for (int i=0; i<refvarposition.length; i++)
				{
					if (ignorecase)
					{
						try
						{
							double t1=Double.parseDouble(values[refvarposition[i]]);
							double t2=Double.parseDouble(refvalues[i]);
							if (t1==t2)
								score++;
						}
						catch (Exception e)
						{
							if (values[refvarposition[i]].equalsIgnoreCase(refvalues[i]))
								score++;
						}
					}
					else
					{
						try
						{
							double t1=Double.parseDouble(values[refvarposition[i]]);
							double t2=Double.parseDouble(refvalues[i]);
							if (t1==t2)
								score++;
						}
						catch (Exception e)
						{
							if (values[refvarposition[i]].equals(refvalues[i]))
								score++;
						}
					}
				}
				if (score!=refvarposition.length)
					dw.write(values);
			}
			data.close();
			boolean resclose=dw.close();
			if (!resclose)
				return false;
			LocalDictionaryWriter ldw=new LocalDictionaryWriter(dw.getdictpath(), keyword, description, author, dw.gettabletype(),
			dw.getTableInfo(), dsu.getfinalvarinfo(), dw.getVarInfo(), dsu.getfinalcl(), dsu.getfinalmd(), null);
			String resw=ldw.action();
			if (resw.startsWith("0"))
				return false;
			return true;
		}
		catch (Exception e) {}
		return false;
	}
	/**
	*Returns the record that is identified by an array of values referred to the correspondent variables
	*/
	public String[] GETRECORDFROMDS(String dsname, String dspath, String[] refvalues, String[] refvarnames, boolean ignorecase)
	{
		return GETRECORDFROMDS(dsname, dspath, refvalues, refvarnames, ignorecase, 0);
	}
	/**
	*Returns the record that is identified by an array of values referred to the correspondent variables
	*/
	public String[] GETRECORDFROMDS(String dsname, String dspath, String[] refvalues, String[] refvarnames, boolean ignorecase, int replacerule)
	{
		String[] valtoreturn=null;
		try
		{
		    if (!dspath.endsWith(System.getProperty("file.separator")))
		    	dspath=dspath+System.getProperty("file.separator");
			DictionaryReader dr=new DictionaryReader(dspath+dsname);
			int[] refvarposition=new int[refvarnames.length];
			for (int j=0; j<refvarnames.length; j++)
			{
				refvalues[j]=GETPARVALUE(refvalues[j]);
				for (int i=0; i<dr.gettotalvar(); i++)
				{
					if (dr.getvarname(i).equalsIgnoreCase(refvarnames[j]))
						refvarposition[j]=i;
				}
			}
			DataReader data = new DataReader(dr);
			String[] reqvar=new String[dr.gettotalvar()];
			int[] repvar=new int[dr.gettotalvar()];
			for (int i=0; i<dr.gettotalvar(); i++)
			{
				reqvar[i]=dr.getvarname(i);
				repvar[i]=replacerule;
			}
			if (!data.open(reqvar, repvar, false))
				return null;
			String[] values=null;
			int score=0;
			while (!data.isLast())
			{
				values = data.getRecord();
				score=0;
				for (int i=0; i<refvarposition.length; i++)
				{
					if (ignorecase)
					{
						try
						{
							double t1=Double.parseDouble(values[refvarposition[i]]);
							double t2=Double.parseDouble(refvalues[i]);
							if (t1==t2)
								score++;
						}
						catch (Exception e)
						{
							if (values[refvarposition[i]].equalsIgnoreCase(refvalues[i]))
								score++;
						}
					}
					else
					{
						try
						{
							double t1=Double.parseDouble(values[refvarposition[i]]);
							double t2=Double.parseDouble(refvalues[i]);
							if (t1==t2)
								score++;
						}
						catch (Exception e)
						{
							if (values[refvarposition[i]].equals(refvalues[i]))
								score++;
						}
					}
				}
				if (score==refvarposition.length)
				{
					data.close();
					return values;
				}
			}
			data.close();
		}
		catch (Exception e) {}
		return valtoreturn;
	}
	/**
	*Exports a data set inside an html table
	*/
	public String EXPORTDSINTABLE(String dictname, String dictpath, String[] varnames, int border, boolean putlabel)
	{
	    if (!dictpath.endsWith(System.getProperty("file.separator")))
	    	dictpath=dictpath+System.getProperty("file.separator");
		return EXPORTDSINTABLE(dictpath+dictname, varnames, border, putlabel);
	}
	/**
	*Exports a data set inside an html table
	*/
	public String EXPORTDSINTABLE(String dictionary, int border, boolean putlabel)
	{
		return EXPORTDSINTABLE(dictionary, null, border, putlabel);
	}
	/**
	*Returns all the values inside a data set
	*/
	public String[][] GETDS(String dictionary, String[] varnames, int replacerule)
	{
		String[][] valtoreturn=null;
		try
		{
			DictionaryReader dr=new DictionaryReader(dictionary);
			DataReader data = new DataReader(dr);
			int numobs=data.getRecords();

			String[] reqvar=new String[0];
			int[] repvar=new int[0];
			if (varnames==null)
			{
				reqvar=new String[dr.gettotalvar()];
				repvar=new int[dr.gettotalvar()];
				for (int i=0; i<dr.gettotalvar(); i++)
				{
					reqvar[i]=dr.getvarname(i);
					repvar[i]=replacerule;
				}
			}
			else
			{
				reqvar=new String[varnames.length];
				repvar=new int[varnames.length];
				for (int i=0; i<varnames.length; i++)
				{
					repvar[i]=replacerule;
					reqvar[i]=varnames[i];
				}
			}
			if (!data.open(reqvar, repvar, true))
				return null;
			valtoreturn=new String[numobs][reqvar.length];
			String[] values=null;
			int pointer=0;
			while (!data.isLast())
			{
				values = data.getRecord();
				for (int i=0; i<values.length; i++)
				{
					valtoreturn[pointer][i]=values[i];
				}
				pointer++;
			}
			data.close();
			return valtoreturn;
		}
		catch (Exception e) {}
		return null;
	}
	/**
	*Exports a data set inside an html table
	*/
	public String EXPORTDSINTABLE(String dictionary, String[] varnames, int border, boolean putlabel)
	{
		String valtoreturn=null;
		try
		{
			DictionaryReader dr=new DictionaryReader(dictionary);
			DataReader data = new DataReader(dr);
			String[] reqvar=new String[0];
			int[] repvar=new int[0];
			valtoreturn="<table border=\""+String.valueOf(border)+"\">\n";
			if (putlabel)
				valtoreturn=valtoreturn+"<tr>\n";

			if (varnames==null)
			{
				reqvar=new String[dr.gettotalvar()];
				repvar=new int[dr.gettotalvar()];
				for (int i=0; i<dr.gettotalvar(); i++)
				{
					reqvar[i]=dr.getvarname(i);
					repvar[i]=1;
					if (putlabel)
						valtoreturn=valtoreturn+"<th>"+HTMLCOMPLIANT(dr.getvarlabel(i))+"</th>\n";
				}
			}
			else
			{
				reqvar=new String[varnames.length];
				repvar=new int[varnames.length];
				for (int i=0; i<varnames.length; i++)
				{
					repvar[i]=1;
					if (putlabel)
						valtoreturn=valtoreturn+"<th>"+HTMLCOMPLIANT(dr.getvarlabelfromname(varnames[i]))+"</th>\n";
				}
			}
			if (putlabel)
				valtoreturn=valtoreturn+"</tr>\n";

			if (!data.open(reqvar, repvar, true))
				return null;
			String[] values=null;
			while (!data.isLast())
			{
				values = data.getRecord();
				valtoreturn=valtoreturn+"<tr>\n";
				for (int i=0; i<values.length; i++)
				{
					if (values[i].equals(""))
						values[i]="&nbsp;";
					valtoreturn=valtoreturn+"<td>"+HTMLCOMPLIANT(values[i])+"</td>\n";
				}
				valtoreturn=valtoreturn+"</tr>\n";
			}
			data.close();
			valtoreturn=valtoreturn+"</table>\n";
			return valtoreturn;
		}
		catch (Exception e) {}
		return null;
	}
	/**
	*Adds a table by receiving the text the array of label names and the double array of values
	*/
	public String ADDTABLE(String text, String[] labels, String[][] values, int border)
	{
		text=text+"<table border=\""+String.valueOf(border)+"\">\n";
		text=text+"<tr>\n";
		for (int i=0; i<labels.length; i++)
		{
			if (labels[i]==null)
				labels[i]="&nbsp;";
			if (labels[i].equals(""))
				labels[i]="&nbsp;";
			text=text+"<th>"+HTMLCOMPLIANT(labels[i])+"</th>\n";
		}
		text=text+"</tr>\n";
		for (int i=0; i<values.length; i++)
		{
			text=text+"<tr>\n";
			for (int j=0; j<values[0].length; j++)
			{
				if (values[i][j]==null)
					values[i][j]="";
				if (values[i][j].equals(""))
					values[i][j]="&nbsp;";
				else
					values[i][j]=HTMLCOMPLIANT(values[i][j]);
				text=text+"<td>"+values[i][j]+"</td>\n";
			}
			text=text+"</tr>\n";
		}
		text=text+"</table>\n";
		return text;

	}
	/**
	*Insert a BR inside the received text
	*/
	public String INSERTBR(String text)
	{
		return text+"<br>\n";
	}
	/**
	*Insert a HR inside the received text
	*/
	public String INSERTHR(String text)
	{
		return text+"<hr>\n";
	}
	/**
	*Insert an html link to the text that can optionally be opened in a new window
	*/
	public String INSERTLINK(String text, String link, String linkname, boolean newwindow)
	{
		text=text+"<a href=\""+link+"\"";
		if (newwindow)
			text=text+" target=\"_blank\"";
		text=text+">"+HTMLCOMPLIANT(linkname)+"</a>\n";
		return text;
	}
	/**
	*Insert a list of values inside a text
	*/
	public String INSERTLIST(String text, String[] listval)
	{
		text=text+"<ul>\n";
		for (int i=0; i<listval.length; i++)
		{
			text=text+"<li>"+HTMLCOMPLIANT(listval[i])+"</li>\n";

		}
		text=text+"</ul>\n";
		return text;
	}
	/**
	*Insert an input text area inside a text
	*/
	public String INSERTINPUT(String text, String name)
	{
		text=text+"<input type=\"text\" name=\""+name+"\">\n";
		return text;
	}
	/**
	*Insert a password area inside a text
	*/
	public String INSERTPASSWORD(String text, String name)
	{
		text=text+"<input type=\"password\" name=\""+name+"\">\n";
		return text;
	}
	/**
	*Insert a radio box inside a text by receiving the list of options and of their names
	*/
	public String INSERTRADIO(String text, String name, String[] values, String[] texts)
	{
		for (int i=0; i<values.length; i++)
		{
			text=text+"<input type=\"radio\" name=\""+name+"\" value=\""+values[i]+"\"> "+HTMLCOMPLIANT(texts[i])+"\n";
			if (i<(values.length-1))
				text=text+"<br>\n";
		}
		return text;
	}
	/**
	*Insert a checkbox inside a text by receiving the list of options and of their names
	*/
	public String INSERTCHECKBOX(String text, String value, String name)
	{
		text=text+"<input type=\"checkbox\" name=\""+name+"\" value=\""+value+"\">\n";
		return text;
	}
	/**
	*Insert a list inside a text by receiving the list of options and of their names
	*/
	public String INSERTLIST(String text, String name, String[] values, String[] texts)
	{
		text=text+"<select name=\""+name+"\">\n";
		for (int i=0; i<values.length; i++)
		{
			text=text+"<option value=\""+HTMLCOMPLIANT(values[i])+"\">"+texts[i]+"</option>\n";
		}
		text=text+"</select>\n";
		return text;
	}
	/**
	*Insert the end of the current form inside a text
	*/
	public String ENDFORM(String text)
	{
		if (text!=null)
			return text+"</form>\n";
		else
			return text;

	}
	/**
	*Returns the record that is identified by an array of values referred to the correspondent variables
	*/
	public String[] GETVALUESFROMDS(String dsname, String dspath, String varref, boolean writefmt)
	{
		return GETVALUESFROMDS(dsname, dspath, varref, writefmt, 1);
	}
	/**
	*Returns the record that is identified by an array of values referred to the correspondent variables
	*/
	public String[] GETVALUESFROMDS(String dsname, String dspath, String varref, boolean writefmt, int replacerule)
	{
		String[] valtoreturn=null;
		try
		{
			TreeSet<String> diffvalues=new TreeSet<String>(new StringComparator());
		    if (!dspath.endsWith(System.getProperty("file.separator")))
		    	dspath=dspath+System.getProperty("file.separator");
			DictionaryReader dr=new DictionaryReader(dspath+dsname);
			DataReader data = new DataReader(dr);
			String[] reqvar=new String[1];
			int[] repvar=new int[1];
			reqvar[0]=varref;
			repvar[0]=replacerule;
			if (!data.open(reqvar, repvar, writefmt))
				return null;
			String[] values=null;
			while (!data.isLast())
			{
				values = data.getRecord();
				diffvalues.add(values[0]);
			}
			data.close();
			valtoreturn=new String[diffvalues.size()];
			Iterator<String> idv = diffvalues.iterator();
			int pos=0;
			while(idv.hasNext())
			{
				valtoreturn[pos]=idv.next();
				pos++;
			}
		}
		catch (Exception e) {}
		return valtoreturn;
	}
	/**
	*Returns an array of String that contains all the definitions<p>
	*The first element of the array is the name, the second the value
	*/
	public String[][] GETDEFINES()
	{
		TreeMap<String, String> checkkey=Keywords.project.getNamesAndDefinitions();
		String[][] retdef=new String[checkkey.size()][2];
		int point=0;
		for (Iterator<String> it = checkkey.keySet().iterator(); it.hasNext();) 
		{
			String keyname = it.next();
			retdef[point][0]=keyname;
			retdef[point][1]=checkkey.get(keyname);
			point++;
		}
		return retdef;
	}
	/**
	*Returns an array of String that contains all the definitions<p>
	*The first element of the array is the name, the second the value
	*/
	public String GETDEFINE(String name)
	{
		TreeMap<String, String> checkkey=Keywords.project.getNamesAndDefinitions();
		try
		{
			for (Iterator<String> it = checkkey.keySet().iterator(); it.hasNext();) 
			{
				String keyname = it.next();
				if (keyname.equalsIgnoreCase(name))
					return checkkey.get(keyname);
			}
		}
		catch (Exception e)
		{
			return null;
		}
		return "";
	}
	/**
	*Add a new definition by receiving its new name and its value
	*/
	public void ADDDEFINE(String name, String value)
	{
		Keywords.project.addDefinition(name, value);
	}
	/**
	*Add a new PATH by receiving its new name and its value
	*/
	public void ADDPATH(String name, String value)
	{
		Keywords.project.addPath(name, value);
	}
	/**
	*Returns an array of String that contains all the defined PATHS<p>
	*The first element of the array is the name, the second the value
	*/
	public String[][] GETPATHS()
	{
		TreeMap<String, String> checkkey=Keywords.project.getNamesAndPaths();
		String[][] retdef=new String[checkkey.size()][2];
		int point=0;
		for (Iterator<String> it = checkkey.keySet().iterator(); it.hasNext();) 
		{
			String keyname = it.next();
			retdef[point][0]=keyname;
			retdef[point][1]=checkkey.get(keyname);
			point++;
		}
		return retdef;
	}
	/**
	*Return the path associated to the current name
	*/
	public String GETPATH(String name)
	{
		try
		{
			return Keywords.project.getPath(name);
		}
		catch (Exception e)
		{
			return null;
		}
	}
	/**
	*Return the label of the given variable contained into the specified dictionary
	*/
	public String GETLABEL(String dictionary, String varname)
	{
		String valtoreturn=null;
		try
		{
			DictionaryReader dr=new DictionaryReader(dictionary);
			return dr.getvarlabelfromname(varname);
		}
		catch (Exception e) {}
		return valtoreturn;
	}
	/**
	*Return the number of variables in the dictionary
	*/
	public int GETNUMVARS(String dictionary)
	{
		try
		{
			DictionaryReader dr=new DictionaryReader(dictionary);
			return dr.gettotalvar();
		}
		catch (Exception e) {}
		return 0;
	}
	/**
	*Return the name of the variable that is in the dictionary
	*/
	public String GETVARNAME(String dictionary, int rif)
	{
		try
		{
			DictionaryReader dr=new DictionaryReader(dictionary);
			return dr.getvarname(rif);
		}
		catch (Exception e) {}
		return null;
	}
	/**
	*Return the label of the variable that is in the dictionary
	*/
	public String GETVARLABEL(String dictionary, int rif)
	{
		try
		{
			DictionaryReader dr=new DictionaryReader(dictionary);
			return dr.getvarlabel(rif);
		}
		catch (Exception e) {}
		return null;
	}
	/**
	*Return the writing format of the variable that is in the dictionary
	*/
	public String GETVARFORMAT(String dictionary, int rif)
	{
		try
		{
			DictionaryReader dr=new DictionaryReader(dictionary);
			return dr.getvarformat(rif);
		}
		catch (Exception e) {}
		return null;
	}
	/**
	*Return the keyword associated to the data set
	*/
	public String GETKEYWORD(String dictionary)
	{
		try
		{
			DictionaryReader dr=new DictionaryReader(dictionary);
			return dr.getkeyword();
		}
		catch (Exception e) {}
		return null;
	}
	/**
	*Return the description associated to the data set
	*/
	public String GETDESCRIPTION(String dictionary)
	{
		try
		{
			DictionaryReader dr=new DictionaryReader(dictionary);
			return dr.getdescription();
		}
		catch (Exception e) {}
		return null;
	}
	/**
	*Return the author associated to the data set
	*/
	public String GETAUTHOR(String dictionary)
	{
		try
		{
			DictionaryReader dr=new DictionaryReader(dictionary);
			return dr.getauthor();
		}
		catch (Exception e) {}
		return null;
	}
	/**
	*Return the all the code labels
	*/
	public String[][] GETCODELABEL(String dictionary, int rif)
	{
		try
		{
			DictionaryReader dr=new DictionaryReader(dictionary);
			return dr.getcodelabelfromref(rif);
		}
		catch (Exception e) {}
		return null;
	}
	/**
	*Return the localization of the given town or street, city, country<p>
	*Note that it is necessary to use a valid GOOGLE MAPS key
	*The first element contains the result 200 for ok<p>
	*The second element contains the latitude<p>
	*The third element the longitude<p>
	*The fourth element the
	*/
	public double[] GETLOCALIZATION(String town, String City, String Country, String key)
	{
		double[] position=new double[4];
		try
		{
			String query="http://maps.google.com/maps/geo?q=";
			if (town!=null)
			{
				if (!town.equals(""))
				{
					try
					{
						town=town.replaceAll(" ","+");
					}
					catch (Exception ee) {}
				}
			}
			else
				town="";
			if (City!=null)
			{
				if (!City.equals(""))
				{
					try
					{
						City=City.replaceAll(" ","+");
					}
					catch (Exception ee) {}
				}
			}
			else
				City="";
			if (Country!=null)
			{
				if (!Country.equals(""))
				{
					try
					{
						Country=Country.replaceAll(" ","+");
					}
					catch (Exception ee) {}
				}
			}
			else
				Country="";
			if (!town.equals(""))
				query=query+town;
			if (!City.equals(""))
			{
				if (!town.equals(""))
					query=query+",+";
				query=query+City;
			}
			if (!Country.equals(""))
			{
				if ((!town.equals("")) || (!City.equals("")))
					query=query+",+";
				query=query+Country;
			}
			query=query+"&output=csv&sensor=false&key="+key;
			URL posi = new URL(query);
			BufferedReader buffReader = new BufferedReader(new InputStreamReader(posi.openStream()));
			String pos= buffReader.readLine();
			String[] tp=pos.split(",");
			position[0]=Double.parseDouble(tp[0]);
			position[1]=Double.parseDouble(tp[2]);
			position[2]=Double.parseDouble(tp[3]);
			position[3]=Double.parseDouble(tp[1]);
			buffReader.close();
			return position;
		}
		catch (Exception e)
		{
			return position;
		}
	}
	/**
	*This functions returns a String that contains the map starting info, by receiving the center
	*/
	public String GETMAPSTART(double latcenter, double longcenter, int zoomlevel)
	{
		String returnmap="var map = new GMap2(document.getElementById(\"map_canvas\"));\n";
		returnmap=returnmap+"map.setCenter(new GLatLng("+String.valueOf(latcenter)+","+String.valueOf(longcenter)+"), "+String.valueOf(zoomlevel)+");\n";
		returnmap=returnmap+"map.addControl(new GSmallMapControl());\n";
		returnmap=returnmap+"map.addControl(new GMapTypeControl());\n";
		returnmap=returnmap+"var baseIcon = new GIcon(G_DEFAULT_ICON);\n";
		returnmap=returnmap+"baseIcon.shadow = \"http://www.google.com/mapfiles/shadow50.png\";\n";
		returnmap=returnmap+"baseIcon.iconSize = new GSize(20, 34);\n";
		returnmap=returnmap+"baseIcon.shadowSize = new GSize(37, 34);\n";
		returnmap=returnmap+"baseIcon.iconAnchor = new GPoint(9, 34);\n";
		returnmap=returnmap+"baseIcon.infoWindowAnchor = new GPoint(9, 2);\n";
		return returnmap;
	}
	/**
	*This functions returns a String that contains the function that in a map creates a marker
	*/
	public String GETMARKERFUNCTION()
	{
		String returnmap="function createMarker(point, name, letter){\n";
		returnmap=returnmap+"var letteredIcon = new GIcon(baseIcon);\n";
		returnmap=returnmap+"letteredIcon.image = \"http://www.google.com/mapfiles/marker\"+letter.toUpperCase()+\".png\";\n";
		returnmap=returnmap+"markerOptions = { icon:letteredIcon };\n";
		returnmap=returnmap+"var marker = new GMarker(point, markerOptions);\n";
		returnmap=returnmap+"GEvent.addListener(marker, \"click\", function(){marker.openInfoWindowHtml(\"<b>\"+name+\"</b>\");}); return marker;}\n";
		return returnmap;
	}
	/**
	*This function returns a String that contains a new marker to add
	*/
	public String GETMARKER(String mname, double latcenter, double longcenter, String name, String letter)
	{
		String returnmap="var "+mname+"=new GLatLng("+String.valueOf(latcenter)+","+String.valueOf(longcenter)+");\n";
		returnmap=returnmap+"map.addOverlay(createMarker("+mname+", \""+name+"\",\""+letter+"\"));\n";
		return returnmap;
	}
	/**
	*Create maps with functions
	*/
	public String GETMAPS(String htmlname, String functions, String key, double width, double height)
	{
		String returnmap="<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n";
		returnmap=returnmap+"<html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:v=\"urn:schemas-microsoft-com:vml\">\n";
		returnmap=returnmap+"<head>\n";
		returnmap=returnmap+"<meta http-equiv=\"content-type\" content=\"text/html; charset=utf-8\"/>\n";
		returnmap=returnmap+"<title>"+htmlname+"</title><script src=\"http://maps.google.com/maps?file=api&amp;v=2&amp;key="+key+"\" type=\"text/javascript\"></script>\n";
		returnmap=returnmap+"<script type=\"text/javascript\">\n";
		returnmap=returnmap+"function initialize(){\n";
		returnmap=returnmap+"if (GBrowserIsCompatible()){\n";
		returnmap=returnmap+functions+"\n}} </script>\n";
		returnmap=returnmap+"<body onload=\"initialize()\" onunload=\"GUnload()\">\n";
		returnmap=returnmap+"<div id=\"map_canvas\" style=\"width: "+String.valueOf(width)+"px; height: "+String.valueOf(height)+"px\"></div>\n</body></html>";
		return returnmap;
	}
	public String GETSCRIPT2SORTABLE(String content)
	{
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
		return content;
	}
	/**
	*Add the javascript code that can be used to disable the execute button
	*/
	public String GETSCRIPT2DISBUT(String text, String buttonname)
	{
		text=text+"function disabutt(form){form."+buttonname+".disabled=true }\n";
		return text;
	}
	/**
	*Return a bidimensional array where the first element has the dimension of the total variables the
	*second indicates in the first position the name of the variables the second position there
	*is the label and in the third there is the writing format
	*/
	public String[][] GETVARINFO(String dictionary)
	{
		try
		{
			DictionaryReader dr=new DictionaryReader(dictionary);
			int nv=dr.gettotalvar();
			String[][] retv=new String[nv][3];
			for (int i=0; i<nv; i++)
			{
				retv[i][0]=dr.getvarname(i);
				retv[i][1]=dr.getvarlabel(i);
				retv[i][2]=dr.getvarformat(i);
			}
			return retv;
		}
		catch (Exception e) {}
		return null;
	}
}
