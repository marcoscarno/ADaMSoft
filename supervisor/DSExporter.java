/**
* Copyright (c) ADaMSoft
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

package ADaMSoft.supervisor;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JOptionPane;

import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.gui.MainGUI;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.NewWriteFormat;

/**
* Export a data set in the output area
* @author marco.scarno@gmail.com
* @date 05/09/2019
*/
public class DSExporter
{
	String message;
	boolean steperror;
	public DSExporter (String actualstatement)
	{
		message="";
		steperror=false;
		actualstatement=actualstatement.trim();
		String valds="";
		try
		{
			valds=actualstatement.substring(actualstatement.indexOf(" "));
			valds=valds.trim();
		}
		catch (Exception ex)
		{
			message=Keywords.Language.getMessage(2522);
			steperror=true;
			return;
		}
		String pathname="";
		String dsname=valds;
		if (valds.indexOf(".")>0)
		{
			String[] levelpart=valds.split("\\.");
			pathname=levelpart[0].trim();
			dsname=levelpart[1].trim();
		}
		String path="";
		if (pathname.equals(""))
		{
			path=System.getProperty(Keywords.WorkDir)+dsname;
		}
		if (pathname.equals(""))
			path=System.getProperty(Keywords.WorkDir)+dsname;
		else if (pathname.toUpperCase().startsWith(Keywords.WorkDir.toUpperCase()))
			path=System.getProperty(Keywords.WorkDir)+dsname;
		else
		{
			path=Keywords.project.getPath(pathname);
			if (path==null)
			{
				message=Keywords.Language.getMessage(1062)+" ("+pathname+")";
				steperror=true;
				return;
			}
			else if (path.equalsIgnoreCase(""))
			{
				message=Keywords.Language.getMessage(61)+" ("+pathname+")";
				steperror=true;
				return;
			}
			path=path+dsname;
		}
		try
		{
			boolean isbatch=false;
			if (System.getProperty("isbatch")!=null)
			{
				if (System.getProperty("isbatch").equals("true"))
					isbatch=true;
			}
			boolean waitmsg=true;
			if (System.getProperty("waitmsgforout")!=null)
			{
				if (System.getProperty("waitmsgforout").equals("false"))
					waitmsg=false;
			}
			String OutputFile=System.getProperty("out_outfile");
			DictionaryReader dr=new DictionaryReader(path);
			if (!dr.getmessageDictionaryReader().equals(""))
			{
				message=dr.getmessageDictionaryReader()+")\n";
			}
			String tempdescrip=dr.getdescription();
			if (tempdescrip.length()>39)
				tempdescrip=tempdescrip.substring(0,39)+"(..)";
			String repout="<br><br><table border=\"1\"><caption>"+tempdescrip+"</caption>\n";
			DataReader data=new DataReader(dr);
			Vector<Hashtable<String, String>> var=dr.getfixedvariableinfo();
			int[] replace=new int[var.size()];
			repout=repout+"<tr>";
			int totalcells=var.size()*data.getRecords();
			if (totalcells==0)
			{
				message=Keywords.Language.getMessage(2523)+" ("+path+")";
				steperror=true;
				return;
			}
			if ( (!isbatch) && (totalcells>500) )
			{
				if (waitmsg)
				{
					Object[] optionslg = {Keywords.Language.getMessage(1628), Keywords.Language.getMessage(1629)};
					int largeds =JOptionPane.showOptionDialog(MainGUI.desktop, Keywords.Language.getMessage(1627)+" ("+Keywords.Language.getMessage(1768)+" "+String.valueOf(data.getRecords())+")", Keywords.Language.getMessage(134), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,null, optionslg, optionslg[0]);
					if (largeds==1)
					{
						message=Keywords.Language.getMessage(2524)+" ("+path+")";
						steperror=true;
						return;
					}
				}
			}
			int defdec=-1;
			try
			{
				defdec = Integer.parseInt(System.getProperty(Keywords.numdecimals));
			}
			catch(NumberFormatException  nfe){}
			boolean isselnumasos=false;
			String uselocalefornumbers="false";
			try
			{
				uselocalefornumbers = System.getProperty(Keywords.uselocalefornumbers);
			}
			catch(Exception  nfe){}
			if (uselocalefornumbers==null)
				uselocalefornumbers="false";
			if (uselocalefornumbers.equalsIgnoreCase("true"))
				isselnumasos=true;
			boolean istowrite=true;
			boolean newrep=false;
			if (defdec>-1)
				newrep=true;
			if (isselnumasos)
				newrep=true;
			if (newrep)
				istowrite=false;
			Vector<String> writeformat=new Vector<String>();
			for(int k=0; k<var.size(); k++)
			{
				Hashtable<String, String> currentvar=var.get(k);
				writeformat.add(dr.getvarformatfromname(currentvar.get(Keywords.VariableName.toLowerCase())));
				repout=repout+"<th>"+currentvar.get(Keywords.LabelOfVariable.toLowerCase())+"</th>\n";
				replace[k]=1;
			}
			repout=repout+"</tr>";
			if (!data.open(null, replace, istowrite))
			{
				message=Keywords.Language.getMessage(2523)+" ("+path+")";
				steperror=true;
				return;
			}
			String[] values=null;
			String tempvalue="";
			while (!data.isLast())
			{
				values=data.getRecord();
				if (newrep==true)
				{
					values=NewWriteFormat.getwriteformat(values, writeformat, defdec, isselnumasos);
				}
				repout=repout+"<tr>\n";
				for (int i=0; i<values.length; i++)
				{
					tempvalue=values[i].trim();
					if (tempvalue.equals(""))
						tempvalue="&nbsp;";
					repout=repout+"<td>"+tempvalue+"</td>\n";
				}
				repout=repout+"</tr>\n";
			}
			data.close();
			repout=repout+"</table><br><br>\n";
			if ((isbatch) && (!OutputFile.equals("")))
			{
				try
				{
					Keywords.semwriteOut.acquire();
					BufferedWriter outoutput = new BufferedWriter(new FileWriter(OutputFile, true));
					outoutput.write(repout);
					outoutput.close();
					Keywords.semwriteOut.release();
				}
				catch (Exception exoutput) {}
			}
			else if (!isbatch)
			{
				try
				{
					Keywords.semwriteOut.acquire();
			        BufferedWriter outwriter = new BufferedWriter(new FileWriter(System.getProperty("out_outfile"),true));
			        outwriter.write(repout);
			        outwriter.close();
					Keywords.semwriteOut.release();
				}
				catch (Exception e)
				{
					message=e.toString();
					steperror=true;
					return;
				}
			}
		}
		catch (Exception expo)
		{
			message=expo.toString();
			steperror=true;
			return;
		}
	}
	/**
	*Returns the result
	*/
	public String getMessage()
	{
		return message;
	}
	public boolean getError()
	{
		return steperror;
	}
}
