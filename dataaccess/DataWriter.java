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

package ADaMSoft.dataaccess;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Vector;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.procedures.LocalDictionaryWriter;
import ADaMSoft.procedures.StepResult;
import ADaMSoft.utilities.GetSettingParameters;
import corejava.Format;

/**
* This is the class that writes a new data table
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
public class DataWriter implements StepResult, Serializable
{
	/**
	 * This is the default serial version uid
	 */
	private static final long serialVersionUID = 1L;
	/**
	*This is the message that will be returned
	*/
	public static String messageDataWriter;
	/**
	*The data table methods
	*/
	DataTableWriter datatable;
	/**
	*The name of the class that will write the table
	*/
	Class<?> classtowrite;
	/**
	*The hashtable that contains the information on the data table
	*/
	Hashtable<String, String> tableinfo;
	/**
	*The hashtable that contains other information on the data table (like the sorting variable)
	*/
	Hashtable<String, String> othertableinfo;
	/**
	*This contains the information on the variables
	*/
	Vector<Hashtable<String, String>> fixedvariableinfo;
	/**
	*This is the name of the type of the data table
	*/
	String tabletype;
	/**
	*These are the information on the new dictionary
	*/
	String pathdict;
	String keyword;
	String description;
	String author;
	Vector<Hashtable<String, String>>codelabel;
	Vector<Hashtable<String, String>>missingdata;
	boolean isRemote;
	int recordWritten;
	DsWriter wri;
	WriterQueue wrique;
	int wsize;
	boolean[] ftmtext;
	int[] fmttouse;
	double tv;
	NumberFormat formatter;
	String formatType;
	Format corejavaf;
	/**
	*This receives the parameters (from the interpreter), the suffix of out used by the procedure, a
	*boolean that tells if the data table is created immediatly or if the values will be stored in the memory.
	*/
	public DataWriter(Hashtable<String, Object> parameters, String outsuffix)
	{
		wri=null;
		wrique=null;
		recordWritten=0;
		tableinfo=new Hashtable<String, String>();
		messageDataWriter="";
		outsuffix=outsuffix.toLowerCase();
		LinkedList<GetSettingParameters> tableparameters;
		tabletype=Keywords.SoftwareName.toLowerCase();
		if (parameters.get(outsuffix+Keywords._datatype)!=null)
			tabletype=(String)parameters.get((outsuffix+Keywords._datatype).toLowerCase());
		datatable=null;
		try
		{
			classtowrite= Class.forName(Keywords.SoftwareName+".dataaccess.Write_"+tabletype.toLowerCase());
			datatable = (DataTableWriter)classtowrite.newInstance();
			tableparameters=datatable.initialize();
		}
		catch (ClassNotFoundException In)
		{
			messageDataWriter="%382% ("+tabletype+")<br>\n";
			return;
		}
		catch (Exception e)
		{
			messageDataWriter="%383% ("+tabletype+")<br>\n";
			return;
		}
		Vector<String> missingparameter=new Vector<String>();
		Iterator<GetSettingParameters> itpar = tableparameters.iterator();
		while(itpar.hasNext())
		{
			GetSettingParameters tablepar = itpar.next();
			String parametername=(tablepar.getName()).toLowerCase();
			boolean required=tablepar.isMandatory();
			String parexist=(String)parameters.get(outsuffix+"_"+parametername);
			if ((parexist==null) && (required))
				missingparameter.add(parametername);
			if (parexist!=null)
				tableinfo.put(parametername.toLowerCase(), parexist);
		}
		if (missingparameter.size()>0)
		{
			messageDataWriter="%384% ("+outsuffix+")<br>\n%76%:<br>\n";
			for (int i=0; i<missingparameter.size(); i++)
			{
				messageDataWriter=messageDataWriter+missingparameter.get(i)+"<br>\n";
			}
			return;
		}
		String tablename=(String)parameters.get(outsuffix);
			tableinfo.put(Keywords.tablename, tablename);
		pathdict=(String) parameters.get(outsuffix+"_"+Keywords.dict.toLowerCase());
		if (pathdict==null)
			pathdict=Keywords.WorkDir+tablename;
		else
			pathdict=pathdict+tablename;
		String workdir  =(String)parameters.get(Keywords.WorkDir);
		tableinfo.put(Keywords.WorkDir, workdir);
		return;
	}
	/**
	*Calls the method that opens a data table, by passing to it the fixed variable information (name, label and format).<p>
	*Returns false in case of error.
	*/
	public boolean opendatatable(Vector<Hashtable<String, String>> fixedvariableinfo)
	{
		this.fixedvariableinfo=fixedvariableinfo;
		ftmtext=new boolean[fixedvariableinfo.size()];
		fmttouse=new int[fixedvariableinfo.size()];
		for (int i=0; i<fixedvariableinfo.size(); i++)
		{
			fmttouse[i]=0;
			String wfmt=(fixedvariableinfo.get(i)).get(Keywords.VariableFormat.toLowerCase());
			if ((wfmt.length()==Keywords.NUMSuffix.length()) && (wfmt.toUpperCase().startsWith(Keywords.NUMSuffix.toUpperCase())))
				ftmtext[i]=false;
			else if ((wfmt.length()>Keywords.NUMSuffix.length()) && (wfmt.toUpperCase().startsWith(Keywords.NUMSuffix.toUpperCase())))
			{
				String num=wfmt.substring(Keywords.NUMSuffix.length());
				if ((num.toUpperCase()).startsWith(Keywords.DTSuffix))
					ftmtext[i]=true;
				else if ((num.toUpperCase()).startsWith(Keywords.DATESuffix))
					ftmtext[i]=true;
				else if ((num.toUpperCase()).startsWith(Keywords.TIMESuffix))
					ftmtext[i]=true;
				if ((num.toUpperCase()).startsWith(Keywords.DECSuffix))
				{
					try
					{
						int numFormatDEC=Integer.parseInt(wfmt.substring(Keywords.NUMSuffix.length()+Keywords.DECSuffix.length()));
						if (numFormatDEC==0)
						{
							fmttouse[i]=1;
						}
						else
						{
							fmttouse[i]=-1*numFormatDEC;
						}
					}
					catch (Exception ex) {}
				}
				if ((num.toUpperCase()).startsWith(Keywords.INTSuffix))
					fmttouse[i]=1;
				if ((num.toUpperCase()).startsWith(Keywords.EXPSuffix))
					fmttouse[i]=2;
			}
			else
				ftmtext[i]=true;
		}
		try
		{
			boolean check=datatable.open(tableinfo, fixedvariableinfo);
			if (check==false)
			{
				messageDataWriter=datatable.getmessage();
				return false;
			}
			wrique = new WriterQueue();
		    wri = new DsWriter(wrique, datatable, tabletype);
		    wri.start();
		}
		catch (Exception e)
		{
			messageDataWriter="%383% ("+tabletype+")\n";
			return false;
		}
		Keywords.operationWriting=true;
		return true;
	}
	/**
	*Write the values in the data table or in the buffer (in case writeonremote is false)
	*/
	public boolean write(String[] record)
	{
		recordWritten++;
		Keywords.numwrite++;
		String[] wr=new String[record.length];
		for (int i=0; i<record.length; i++)
		{
			wr[i]=record[i];
			if(record[i]==null)	wr[i]="";
			try
			{
				wr[i]=wr[i].replaceAll("\n", " ").replaceAll("\r", " ").replaceAll("\t", " ").replaceAll("\"","'");
			}
			catch (Exception enl) {}
			if (!ftmtext[i])
			{
				try
				{
					tv=Double.parseDouble(wr[i]);
					if (Double.isNaN(tv))
						wr[i]="";
					else
					{
						if (fmttouse[i]==1)
						{
							NumberFormat formatter = new DecimalFormat("#");
							wr[i]= formatter.format(tv);
						}
						else if (fmttouse[i]==2)
						{
							formatType="%E";
							corejavaf=new Format(formatType);
							wr[i]=corejavaf.format(tv);
						}
						else if (fmttouse[i]<0)
						{
							formatType="%."+String.valueOf(fmttouse[i]*-1)+"f";
							corejavaf=new Format(formatType);
							wr[i]=corejavaf.format(tv);
						}
					}
				}
				catch (Exception e)
				{
					wr[i]="";
				}
			}
		}
		wsize=wrique.getwaitingsize();
		while(wsize>10000)
		{
			try
			{
				Thread.sleep(10);
			}
			catch (Exception e){}
			wsize=wrique.getwaitingsize();
		}
		wrique.addNewRecord(wr);
		return true;
	}
	/**
	*Write the values in the data table or in the buffer (in case writeonremote is false) without approximating the numbers
	*/
	public boolean writenoapprox(String[] record)
	{
		recordWritten++;
		Keywords.numwrite++;
		String[] wr=new String[record.length];
		for (int i=0; i<record.length; i++)
		{
			wr[i]=record[i];
			if(record[i]==null)	wr[i]="";
			try
			{
				wr[i]=wr[i].replaceAll("\n", " ").replaceAll("\r", " ").replaceAll("\t", " ").replaceAll("\"","'");
			}
			catch (Exception enl) {}
			if (!ftmtext[i])
			{
				try
				{
					tv=Double.parseDouble(wr[i]);
					if (Double.isNaN(tv)) wr[i]="";
				}
				catch (Exception e)
				{
					wr[i]="";
				}
			}
		}
		wsize=wrique.getwaitingsize();
		while(wsize>10000)
		{
			try
			{
				Thread.sleep(100);
			}
			catch (Exception e){}
			wsize=wrique.getwaitingsize();
		}
		wrique.addNewRecord(wr);
		return true;
	}
	/**
	*Used to store the dictionary information in case of a local write of the data table, but when the execution is remote
	*/
	public void setdictinfo(String keyword, String description, String author,Vector<Hashtable<String, String>>codelabel, Vector<Hashtable<String, String>>missingdata, Hashtable<String, String> othertableinfo)
	{
		this.othertableinfo=othertableinfo;
		this.keyword=keyword;
		this.description=description;
		this.author=author;
		this.codelabel=codelabel;
		this.missingdata=missingdata;
	}
	public void exportOutput()
	{
	}
	/**
	*Used to write locally the data table in case of writeonremote=false
	*/
	public String action()
	{
		return "";
	}
	/**
	*Delete the temporary file
	*/
	public boolean deletetmp()
	{
		Keywords.operationWriting=false;
		wrique.addNewRecord(DsWriter.TABLE_DELETE);
		boolean checkfinished=wrique.testwriting();
		while (checkfinished)
		{
			try
			{
				Thread.sleep(100);
			}
			catch (Exception eee){}
			checkfinished=wrique.testwriting();
		}
		return true;
	}
	/**
	*Close the data table
	*/
	public boolean close()
	{
		Keywords.operationWriting=false;
		wrique.addNewRecord(DsWriter.NO_MORE_RECORD);
		boolean checkfinished=wrique.testwriting();
		while (checkfinished)
		{
			try
			{
				Thread.currentThread();
				Thread.sleep(100);
			}
			catch (Exception eee){}
			checkfinished=wrique.testwriting();
		}
		String reswri=wri.getmsgdw();
		if (!reswri.equals(""))
		{
			messageDataWriter=reswri;
			return false;
		}
		if (wri!=null)
			wri=null;
		if (wrique!=null)
		{
			wrique.clearmem();
			wrique=null;
		}
		System.gc();
		return true;
	}
	/**
	*Returns the information on the variables that depend from the data table
	*/
	public Vector<Hashtable<String, String>> getVarInfo()
	{
		Vector<Hashtable<String, String>> varinfo=new Vector<Hashtable<String, String>>();
		try
		{
			varinfo=datatable.getVariablesInfo();
		}
		catch (Exception e)
		{
			messageDataWriter="%387%<br>\n";
			return varinfo;
		}
		return varinfo;
	}
	/**
	*Returns the information of the data table
	*/
	public Hashtable<String, String> getTableInfo()
	{
		try
		{
			tableinfo=datatable.getTableInfo();
		}
		catch (Exception e)
		{
			messageDataWriter="%388%<br>\n";
		}
		return tableinfo;
	}
	/**
	*Returns the error message
	*/
	public String getmessage()
	{
		return messageDataWriter;
	}
	/**
	*Returns the path of the dictionary
	*/
	public String getdictpath()
	{
		return pathdict;
	}
	/**
	*Returns the type of the data table
	*/
	public String gettabletype()
	{
		return tabletype;
	}
	/**
	*Returns the number of records written
	*/
	public int getRecordsWritten()
	{
		return recordWritten;
	}
}
