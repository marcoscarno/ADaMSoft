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
import java.io.FileWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.AdamsCompliant;
import ADaMSoft.utilities.DataTableInfo;
import ADaMSoft.utilities.GenericContainerForDict;
import ADaMSoft.utilities.GenericContainerForParameters;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.utilities.MessagesReplacer;
import ADaMSoft.utilities.NewWriteFormat;

import ADaMSoft.gui.MainGUI;
/**
* This class creates a Dictionary
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class LocalDictionaryWriter extends AdamsCompliant implements StepResult, Serializable
{
	/**
	 * This is the default static version UID
	 */
	private static final long serialVersionUID = 1L;
	/**
	*This is the path where the dictionary will be saved
	*/
	String path;
	/**
	*These are the keywords associated to the data set
	*/
	String keyword;
	/**
	*This is the description of the data set
	*/
	String description;
	/**
	*This is the information on who creates the dictionary
	*/
	String author;
	/**
	*This contains the type of the data table
	*/
	String datatabletype;
	/**
	*These hashtable contains the information in the data table
	*/
	DataTableInfo datatableinfo;
	/**
	*These hashtable contains the information in the data table
	*/
	DataTableInfo othertableinfo;
	/**
	*This vector contains, in each record, an hashtable with the fixed information in the variable.<p>
	*Such information are related to:<p>
	*name, label, writeformat.
	*/
	GenericContainerForDict fixedvariableinfo;
	/**
	*This vector contains, in each record, an hashtable with the information on the variables that are specific
	*for the type of the data table.
	*/
	GenericContainerForDict tablevariableinfo;
	/**
	*This vector contains, in each record, an hashtable with the code and the label defined on each variable.
	*/
	GenericContainerForDict codelabel;
	/**
	*This vector contains, in each record, an hashtable with the missing data rule defined on each variable.
	*/
	GenericContainerForDict missingdata;
	/**
	*Contains the info that the data set must be exported in the output file
	*/
	boolean exportoutput;
	/**
	*Contains the reference to the class file name
	*/
	String viewclassref;
	/**
	*Contains the class required to access to a dataset view
	*/
	Object viewclass;
	/**
	*Contains the parameter required by the dataset view
	*/
	GenericContainerForParameters parameterview;
	/**
	*Number of records written
	*/
	int	recordWritten;
	/**
	*Constructor
	*/
	public LocalDictionaryWriter (String path, String tkeyword, String tdescription, String author, String datatabletype,
	Hashtable<String, String> datatableinfo, Vector<Hashtable<String, String>> fixedvariableinfo,
	Vector<Hashtable<String, String>> tablevariableinfo, Vector<Hashtable<String, String>> codelabel,
	Vector<Hashtable<String, String>> missingdata, Hashtable<String, String> othertableinfo)
	{
		recordWritten=-1;
		exportoutput=false;
		viewclass=null;
		this.path=path;
		if (tkeyword.length()>150)
			tkeyword=tkeyword.substring(0,149);
		if (tdescription.length()>150)
			tdescription=tdescription.substring(0,149);
		this.keyword=tkeyword;
		this.description=tdescription;
		this.author=author;
		this.datatabletype=datatabletype;
		if(datatableinfo!=null )
		{
			this.datatableinfo=new DataTableInfo();
			this.datatableinfo.putAll(datatableinfo);
		}
		if(fixedvariableinfo!=null )
		{
			this.fixedvariableinfo=new GenericContainerForDict();
			this.fixedvariableinfo.addAll(fixedvariableinfo);
		}
		if(tablevariableinfo!=null )
		{
			this.tablevariableinfo=new GenericContainerForDict();
			this.tablevariableinfo.addAll(tablevariableinfo);
		}
		if(codelabel!=null )
		{
			this.codelabel=new GenericContainerForDict();
			this.codelabel.addAll(codelabel);
		}
		if(missingdata!=null )
		{
			this.missingdata=new GenericContainerForDict();
			this.missingdata.addAll(missingdata);
		}
		if(othertableinfo!=null )
		{
			this.othertableinfo=new DataTableInfo();
			this.othertableinfo.putAll(othertableinfo);
		}
	}
	/**
	*Change the path of the data table specified in a dictionary
	*/
	public void setnewpath(String newpath)
	{
		String currentpath=datatableinfo.get(Keywords.DATA.toLowerCase());
		File tempfiletochange = new File(currentpath);
		currentpath=newpath+tempfiletochange.getName();
  		datatableinfo.put(Keywords.DATA.toLowerCase(), currentpath);
	}
	public void setRecordWritten(int recordWritten)
	{
		this.recordWritten=recordWritten;
	}
	/**
	*Receive the info that the dictionary should be exported in the output window
	*/
	public void exportOutput()
	{
		this.exportoutput=true;
	}
	/**
	*Receive the class required to access to a dataset view
	*/
	public void setviewclass(Object viewclass)
	{
		this.viewclass=viewclass;
	}
	/**
	*Receive the class required to access to a dataset view
	*/
	public void setviewclassref(String viewclassref)
	{
		this.viewclassref=viewclassref;
	}
	/**
	*Receive the class required to access to a dataset view
	*/
	public void setviewparameter(Hashtable<String, Object> parameterview)
	{
		this.parameterview=new GenericContainerForParameters();
		this.parameterview.putAll(parameterview);
	}
	/**
	*Writes the dictionary
	*/
	public String action()
	{
		if(fixedvariableinfo!=null )
		{
			for(int i=0;i<this.fixedvariableinfo.size();i++)
			{
				Hashtable<String, String> tempinfo=this.fixedvariableinfo.get(i);
				tempinfo.put(Keywords.LabelOfVariable.toLowerCase(), MessagesReplacer.replaceMessages(tempinfo.get(Keywords.LabelOfVariable.toLowerCase())));
				tempinfo.put(Keywords.VariableNumber.toLowerCase(), String.valueOf(i));
			}
		}
		if(codelabel!=null )
		{
			for(int i=0;i<this.codelabel.size();i++)
			{
				Hashtable<String, String> tempinfo=this.codelabel.get(i);
				if (tempinfo.size()>0)
				{
					for (Enumeration<String> en=tempinfo.keys(); en.hasMoreElements();)
					{
						String code=en.nextElement();
						String label=tempinfo.get(code);
						tempinfo.put(code, MessagesReplacer.replaceMessages(label));
					}
				}
			}
		}

		if (path.startsWith(Keywords.WorkDir))
			path=System.getProperty(Keywords.WorkDir)+path.substring(Keywords.WorkDir.length());
		if (!path.endsWith(Keywords.DictionaryExtension))
			path=path+Keywords.DictionaryExtension;
		try
		{
			path = toAdamsFormat(path);
			File filezip = new File(path);
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(filezip));
			Object[] paramDate = new Object[]{new java.util.Date(), new java.util.Date(0)};
			String ActualDate = MessageFormat.format("{0}", paramDate);

			ZipEntry entry = new ZipEntry(Keywords.CreationDate);
			out.putNextEntry(entry);
			ObjectOutputStream oos = new ObjectOutputStream(out);
			oos.writeObject(ActualDate);
			out.closeEntry();

			if (keyword==null)
				keyword=path;
			if (description==null)
				description=path;

			entry = new ZipEntry(Keywords.keyword);
			out.putNextEntry(entry);
			oos.writeObject(keyword);
			out.closeEntry();

			entry = new ZipEntry(Keywords.description);
			out.putNextEntry(entry);
			oos.writeObject(description);
			out.closeEntry();

			if (author==null)
			{
				try
				{
		    	    InetAddress addr = InetAddress.getLocalHost();
		    	    author=addr.toString();
		    	}
		    	catch (Exception ex)
		    	{
					author="LOCALHOST";
				}
		    }

			entry = new ZipEntry(Keywords.author);
			out.putNextEntry(entry);
			oos.writeObject(author);
			out.closeEntry();

			entry = new ZipEntry(Keywords.DataTableType);
			out.putNextEntry(entry);
			oos.writeObject(datatabletype);
			out.closeEntry();

			if (othertableinfo!=null)
			{
				for (Enumeration<String> e = othertableinfo.keys() ; e.hasMoreElements() ;)
				{
					String temppar = e.nextElement();
					String tempval = othertableinfo.get(temppar);
					datatableinfo.put(temppar, tempval);
				}
			}

			if (viewclass!=null)
			{
				entry = new ZipEntry("Read_view");
				out.putNextEntry(entry);
				oos.writeObject(viewclass);
				out.closeEntry();
				entry = new ZipEntry("Ref_Read_view");
				out.putNextEntry(entry);
				oos.writeObject(viewclassref);
				out.closeEntry();
			}

			if (parameterview!=null)
			{
				if (parameterview.size()>0)
				{
					entry = new ZipEntry("Parameterview");
					out.putNextEntry(entry);
					oos.writeObject(parameterview);
					out.closeEntry();
				}
			}

			if (datatableinfo!=null)
			{
				if (datatableinfo.size()>0)
				{
					if (datatableinfo.get(Keywords.SORTED.toLowerCase())!=null)
						datatableinfo.remove(Keywords.SORTED.toLowerCase());
					entry = new ZipEntry(Keywords.DataTableInfo);
					out.putNextEntry(entry);
					oos.writeObject(datatableinfo);
					out.closeEntry();
				}
			}

			if (fixedvariableinfo!=null)
			{
				if (fixedvariableinfo.size()>0)
				{
					entry = new ZipEntry(Keywords.FixedVariablesInfo);
					out.putNextEntry(entry);
					oos.writeObject(fixedvariableinfo);
					out.closeEntry();
				}
			}

			if (tablevariableinfo!=null)
			{
				if (tablevariableinfo.size()>0)
				{
					entry = new ZipEntry(Keywords.TableVariablesInfo);
					out.putNextEntry(entry);
					oos.writeObject(tablevariableinfo);
					out.closeEntry();
				}
			}

			entry = new ZipEntry(Keywords.CodeLabels);
			out.putNextEntry(entry);
			oos.writeObject(codelabel);
			out.closeEntry();

			entry = new ZipEntry(Keywords.MissingDataValues);
			out.putNextEntry(entry);
			oos.writeObject(missingdata);
			out.closeEntry();
			out.close();
			oos.close();
			if (exportoutput)
			{
				try
				{
					DictionaryReader dr=new DictionaryReader(path);
					String tempdescrip=dr.getdescription();
					if (tempdescrip.length()>39)
						tempdescrip=tempdescrip.substring(0,39)+"(..)";
					String repout="<table border=\"1\"><caption>"+tempdescrip+"</caption>\n";
					DataReader data=new DataReader(dr);
					Vector<Hashtable<String, String>> var=dr.getfixedvariableinfo();
					int[] replace=new int[var.size()];
					repout=repout+"<tr>";
					String test_batch=System.getProperty("isbatch");
					int totalcells=var.size()*data.getRecords();
					if (totalcells==0)
						return "1 %503%"+" ("+path+")<br>\n";
					if (test_batch.equalsIgnoreCase("false") && totalcells>500)
					{
						Object[] optionslg = {Keywords.Language.getMessage(1628), Keywords.Language.getMessage(1629)};
						int largeds =JOptionPane.showOptionDialog(MainGUI.desktop, Keywords.Language.getMessage(1627)+" ("+Keywords.Language.getMessage(1768)+" "+String.valueOf(data.getRecords())+")", Keywords.Language.getMessage(134), JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,null, optionslg, optionslg[0]);
						if (largeds==1)
							return "1 %503%"+" ("+path+")<br>\n";
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
						return "1 %503%"+" ("+path+")<br>\n";
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
					repout=repout+"</table><br><p>&nbsp;</p>\n";
					Keywords.semwriteOut.acquire();
			        BufferedWriter outwriter = new BufferedWriter(new FileWriter(System.getProperty("out_outfile"),true));
			        outwriter.write(repout);
			        outwriter.close();
					Keywords.semwriteOut.release();
				}
				catch (Exception expo){}
			}
		}
		catch (Exception ex)
		{
			return "0 %502%"+" ("+path+")<br>\n";
		}
		if (recordWritten==-1)
			return "1 %503%"+" ("+path+")<br>\n";
		else
		{
			return "1 %503%"+" ("+path+")<br>\n%2339%: "+String.valueOf(recordWritten)+"<br>\n";
		}
	}
}