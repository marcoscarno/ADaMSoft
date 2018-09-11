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

package ADaMSoft.dataaccess;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import ADaMSoft.keywords.Keywords;
import corejava.Format;

/**
* This class calls the method that reads a data table according to its data type.<p>
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class DataReader
{
	/**
	*This contains the writing formats for each variable
	*/
	Vector<String> writeformat;
	/**
	*This is the vector of the code labels of the variables that will be read
	*/
	public Vector<CLContainer> codelabel;
	/**
	*This is the vector of the missing data rules of the variables that will be read
	*/
	public Vector<MDContainer> missingdata;
	/**
	*To be discovered
	*/
	String ted;
	/**
	*This is the used data reader
	*/
	DataTableReader dtr;
	/**
	*This is the used data table viewer
	*/
	DataTableViewer dtv;
	/**
	*This refers to the class used to store the pointer to the correct data reader class
	*/
	Class<?> classtoread;
	/**
	*This contains the message related to the data reader
	*/
	String msgDataReader="";
	/**
	*This contains the code label of the variables that will be read
	*/
	Vector<Hashtable<String, String>> actualcodelabel;
	/**
	*This contains the missing data rule of the variables that will be read
	*/
	Vector<Hashtable<String, String>> actualmissingdata;
	/**
	*If set to true, means that the value of the variables will be returned using what is defined into the writeformat
	*/
	boolean istowrite;
	/**
	*Contains the replace rule for each requested variable
	*/
	int[] replace;
	/**
	*Contains the dictionary reader
	*/
	DictionaryReader dr;
	/**
	*This is the type of the data table
	*/
	String tabletype;
	/**
	*This is the reference for the name of the data view
	*/
	String vref;
	/**
	*Initializes the data reader
	*/
	boolean clast;
	String[] varcondition;
	String[] valuescondit;
	int[] typecondition;
	int[] positioncondition;
	boolean outrecord;
	int obstoadd;
	Vector<Integer> posreqvar;
	Vector<Hashtable<String, String>> fixedvariableinfo;
	String[] tempr;
	private ClassLoader classtoexecute;
	public DataReader(DictionaryReader dr)
	{
		this.dr=dr;
		varcondition=null;
		valuescondit=null;
		typecondition=null;
		positioncondition=null;
		obstoadd=0;
		posreqvar=new Vector<Integer>();
		fixedvariableinfo=dr.getfixedvariableinfo();
		clast=false;
	}
	/**
	*Opens the data table in order to read the records.<p>
	*It receives:<p>
	*A String array of the variable names that will be read (if it is null or of dimension 0, all the variables will be used).<p>
	*A integer that contains the replace rule for each requested variable; in particular:<p>
	* 0: means no substitution<p>
	* 1: substitute the code label and the missing data<p>
	* 2: substitute only the code label
	* 3: substitute only the missing data
	*The received boolean, instead, means that, if true, the values of the variables will be replaced according to the writing formats.<p>
	*This class returns true if the data table read methods was correctly opened.
	*/
	public boolean open (String[] vartoread, int replacei, boolean istowrite)
	{
		clast=false;
		posreqvar.clear();
		replace=new int[0];
		if (vartoread==null) replace=new int[fixedvariableinfo.size()];
		else replace=new int[vartoread.length];
		for (int i=0; i<replace.length; i++)
		{
			replace[i]=replacei;
		}
		return open(vartoread, replace, istowrite);
	}
	/**
	*Opens the data table in order to read the records.<p>
	*It receives:<p>
	*A String array of the variable names that will be read (if it is null or of dimension 0, all the variables will be used).<p>
	*An integer array that contains, for each variable, the replace rule; in particular:<p>
	* 0: means no substitution<p>
	* 1: substitute the code label and the missing data<p>
	* 2: substitute only the code label
	* 3: substitute only the missing data
	*The received boolean, instead, means that, if true, the values of the variables will be replaced according to the writing formats.<p>
	*This class returns true if the data table read methods was correctly opened.
	*/
	public boolean open (String[] vartoread, int[] replacef, boolean istowrite)
	{
		clast=false;
		posreqvar.clear();
		this.replace=replacef;
		this.codelabel=new Vector<CLContainer>();
		this.missingdata= new Vector<MDContainer>();
		writeformat=new Vector<String>();
		if (vartoread==null)
			vartoread=new String[0];
		if (replace==null)
			replace=new int[0];
		this.istowrite=istowrite;
		tabletype=dr.getdatatabletype();
		Hashtable<String, String> tableinfo=dr.getdatatableinfo();
		Vector<Hashtable<String, String>> tablevariableinfo=dr.gettablevariableinfo();
		Vector<Hashtable<String, String>> allcodelabel=dr.getcodelabel();
		Vector<Hashtable<String, String>> allmissingdata=dr.getmissingdata();
		actualcodelabel=new Vector<Hashtable<String, String>>();
		actualmissingdata=new Vector<Hashtable<String, String>>();
		if (vartoread.length>0)
		{
			String varsfound="";
			for (int i=0; i<vartoread.length; i++)
			{
				boolean vno=false;
				for (int j=0; j<fixedvariableinfo.size(); j++)
				{
					Hashtable<String, String> currentvar=fixedvariableinfo.get(j);
					String varname=currentvar.get(Keywords.VariableName.toLowerCase());
					if (varname.equalsIgnoreCase(vartoread[i]))
					{
						posreqvar.add(new Integer(j));
						vno=true;
						actualcodelabel.add(allcodelabel.get(j));
						actualmissingdata.add(allmissingdata.get(j));
						writeformat.add(currentvar.get(Keywords.VariableFormat.toLowerCase()));
					}
				}
				if (!vno) varsfound=varsfound+" "+vartoread[i];
			}
			if (!varsfound.equals(""))
			{
				msgDataReader="%1632% ("+varsfound.trim().toUpperCase()+")<br>\n";
				return false;
			}
		}
		else
		{
			actualcodelabel=allcodelabel;
			actualmissingdata=allmissingdata;
			for (int j=0; j<fixedvariableinfo.size(); j++)
			{
				Hashtable<String, String> currentvar=fixedvariableinfo.get(j);
				writeformat.add(currentvar.get(Keywords.VariableFormat.toLowerCase()));
				posreqvar.add(new Integer(j));
			}
		}
		if (replace.length==0)
		{
			replace=new int[posreqvar.size()];
			for (int i=0; i<posreqvar.size(); i++)
			{
				replace[i]=0;
			}
		}
		if (!tabletype.equalsIgnoreCase("view"))
		{
			try
			{
				classtoread= Class.forName(Keywords.SoftwareName+".dataaccess.Read_"+tabletype.toLowerCase());
				dtr = (DataTableReader)classtoread.newInstance();
				setContainers(actualcodelabel, actualmissingdata);
				boolean openresult=dtr.open(tableinfo, fixedvariableinfo, tablevariableinfo);
				if (!openresult)
				{
					msgDataReader=dtr.getMessage();
					Keywords.operationReading=false;
				}
				else
					Keywords.operationReading=true;
				return openresult;
			}
			catch (ClassNotFoundException In)
			{
				Keywords.operationReading=false;
				msgDataReader="%352% ("+tabletype+")<br>\n";
				return false;
			}
			catch (Exception e)
			{
				Keywords.operationReading=false;
				msgDataReader="%353% ("+tabletype+")<br>\n";
				return false;
			}
		}
		else
		{
			Hashtable<String, Object> parview=dr.getparameters();
			ted=(String)parview.get(Keywords.WorkDir);
			byte[] file=(byte[])dr.getviewclass();
			vref=dr.getviewclassref();
			try
			{
				FileOutputStream out = new FileOutputStream(ted+"Read_view"+vref+".class");
				out.write(file);
				out.close();
				file=new byte[0];
				file=null;
			}
			catch (Exception e)
			{
				msgDataReader="%1517%<br>\n";
				return false;
			}
			try
			{
				File fileclass = new File(ted);
				URL url = fileclass.toURI().toURL();
				URL[] urls = new URL[]{url};
				classtoexecute = new URLClassLoader(urls);
				classtoread = classtoexecute.loadClass("Read_view"+vref);
				dtv = (DataTableViewer)classtoread.newInstance();
				setContainers(actualcodelabel, actualmissingdata);
				dtv.setparameters(parview);
				boolean openresult=dtv.open(tableinfo, fixedvariableinfo, tablevariableinfo);
				if (!openresult)
				{
					msgDataReader=dtv.getMessage();
					Keywords.operationReading=false;
				}
				else
					Keywords.operationReading=true;
				return openresult;
			}
			catch (Exception e)
			{
				Keywords.operationReading=false;
				dtv=null;
				classtoread=null;
				msgDataReader="%1518%<br>\n";
				return false;
			}
		}
	}
	/**
	*Receives the optional condition that could be applied on the variables
	*/
	public boolean setcondition(String textcondition)
	{
		if (!textcondition.trim().equals(""))
		{
			try
			{
				if (textcondition.indexOf("AND")>0)
				{
					textcondition=textcondition.replaceAll("AND",";");
				}
				String[] partconditio=textcondition.split(";");
				varcondition=new String[partconditio.length];
				valuescondit=new String[partconditio.length];
				typecondition=new int[partconditio.length];
				for (int i=0; i<partconditio.length; i++)
				{
					partconditio[i]=partconditio[i].trim();
					String[] temppartc=null;
					if (partconditio[i].indexOf("=")>0)
					{
						typecondition[i]=0;
						temppartc=partconditio[i].split("=");
					}
					if (partconditio[i].indexOf("!=")>0)
					{
						typecondition[i]=3;
						temppartc=partconditio[i].split("!=");
					}
					if (partconditio[i].indexOf(">")>0)
					{
						typecondition[i]=1;
						temppartc=partconditio[i].split(">");
					}
					if (partconditio[i].indexOf("<")>0)
					{
						typecondition[i]=-1;
						temppartc=partconditio[i].split("<");
					}
					if (partconditio[i].indexOf(">=")>0)
					{
						typecondition[i]=2;
						temppartc=partconditio[i].split(">=");
					}
					if (partconditio[i].indexOf("<=")>0)
					{
						typecondition[i]=-2;
						temppartc=partconditio[i].split("<=");
					}
					varcondition[i]=temppartc[0].trim();
					valuescondit[i]=temppartc[1].trim();
				}
				positioncondition=new int[partconditio.length];
				for (int i=0; i<partconditio.length; i++)
				{
					boolean varexistcond=false;
					for(int k=0; k<fixedvariableinfo.size(); k++)
					{
						Hashtable<String, String> currentvar=fixedvariableinfo.get(k);
						String tempnamec=(currentvar.get(Keywords.VariableName.toLowerCase())).trim();
						if (tempnamec.toLowerCase().equals(varcondition[i].toLowerCase()))
						{
							positioncondition[i]=k;
							varexistcond=true;
						}
					}
					if (!varexistcond)
					{
						msgDataReader="%2598% ("+varcondition[i].toUpperCase()+")<br>\n";
						return false;
					}
				}
			}
			catch (Exception econd)
			{
				msgDataReader="%2597%<br>\n";
				return false;
			}
		}
		return true;
	}
	/**
	*Returns the message related to an error (that can came or from this method or from the specific read method)
	*/
	public String getmessage()
	{
		return msgDataReader;
	}
	/**
	*Returns the current read record
	*/
	public String[] getRecord()
	{
		Keywords.numread++;
		outrecord=false;
		while (!outrecord)
		{
			if (!tabletype.equalsIgnoreCase("view"))
				tempr=dtr.getRecord();
			else
				tempr=dtv.getRecord();
			outrecord=true;
			if (tempr==null)
			{
				clast=true;
				return null;
			}
			if (varcondition!=null) outrecord=CheckCondition(tempr);
			if (!outrecord && isLast()) return null;
		}
		String[] resval=new String[posreqvar.size()];
		for(int i=0; i<posreqvar.size(); i++)
		{
			resval[i]=tempr[posreqvar.get(i)];
			if(replace[i]==1)
			{
				resval[i]=codelabel.get(i).replace(resval[i]);
				resval[i]=missingdata.get(i).replace(resval[i]);
			}
			if(replace[i]==2)
				resval[i]=codelabel.get(i).replace(resval[i]);
			else if(replace[i]==3)
				resval[i]=missingdata.get(i).replace(resval[i]);
		}
		if (istowrite)
			resval=getwriteformat(resval);
		return resval;
	}
	/**
	*Used to check the values when a condition is required
	*/
	private boolean CheckCondition(String[] tempr)
	{
		obstoadd=0;
		for (int i=0; i<positioncondition.length; i++)
		{
			if (!valuescondit[i].equals("MISSING"))
			{
				if ( (valuescondit[i].startsWith("\"")) && (valuescondit[i].endsWith("\"")) )
				{
					valuescondit[i]=valuescondit[i].replaceAll("\"","");
					if (typecondition[i]!=3)
					{
						if ((typecondition[i]==0) && (valuescondit[i].equalsIgnoreCase(tempr[positioncondition[i]].trim())))
							obstoadd++;
						else if ((typecondition[i]==-2) && (valuescondit[i].equalsIgnoreCase(tempr[positioncondition[i]].trim())))
							obstoadd++;
						else if ((typecondition[i]==2) && (valuescondit[i].equalsIgnoreCase(tempr[positioncondition[i]].trim())))
							obstoadd++;
						else
						{
							int tempcompare=tempr[positioncondition[i]].toUpperCase().compareTo(valuescondit[i].toUpperCase());
							if ((typecondition[i]<0) && (tempcompare<0))
								obstoadd++;
							if ((typecondition[i]>0) && (tempcompare>0))
								obstoadd++;
						}
					}
					else
					{
						if (!valuescondit[i].equalsIgnoreCase(tempr[positioncondition[i]].trim()))
							obstoadd++;
					}
				}
				else
				{
					double connum=Double.NaN;
					double valnum=Double.NaN;
					try
					{
						connum=Double.parseDouble(valuescondit[i]);
						valnum=Double.parseDouble(tempr[positioncondition[i]].trim());
					}
					catch (Exception enumco){}
					if ( (!Double.isNaN(connum)) && (!Double.isNaN(valnum)) )
					{
						if ((typecondition[i]==0) && (connum==valnum))
							obstoadd++;
						else if ((typecondition[i]==1) && (valnum>connum))
							obstoadd++;
						else if ((typecondition[i]==2) && (valnum>=connum))
							obstoadd++;
						else if ((typecondition[i]==-1) && (valnum<connum))
							obstoadd++;
						else if ((typecondition[i]==-2) && (valnum<=connum))
							obstoadd++;
						else if ((typecondition[i]==3) && (valnum!=connum))
							obstoadd++;
					}
					else
					{
						if (typecondition[i]!=3)
						{
							if ((typecondition[i]==0) && (valuescondit[i].equalsIgnoreCase(tempr[positioncondition[i]].trim())))
								obstoadd++;
							else if ((typecondition[i]==-2) && (valuescondit[i].equalsIgnoreCase(tempr[positioncondition[i]].trim())))
								obstoadd++;
							else if ((typecondition[i]==2) && (valuescondit[i].equalsIgnoreCase(tempr[positioncondition[i]].trim())))
								obstoadd++;
							else
							{
								int tempcompare=tempr[positioncondition[i]].toUpperCase().compareTo(valuescondit[i].toUpperCase());
								if ((typecondition[i]<0) && (tempcompare<0))
									obstoadd++;
								if ((typecondition[i]>0) && (tempcompare>0))
									obstoadd++;
							}
						}
						else
						{
							if (!valuescondit[i].equalsIgnoreCase(tempr[positioncondition[i]].trim()))
								obstoadd++;
						}
					}
				}
			}
			else
			{
				if ((tempr[positioncondition[i]].equals("")) && (typecondition[i]==0))
					obstoadd++;
				if ((!tempr[positioncondition[i]].equals("")) && (typecondition[i]==3))
					obstoadd++;
			}
		}
		if (obstoadd!=positioncondition.length) return false;
		return true;
	}
	/**
	*Return the current record in a bidimensional array where the first element is the original value, the second the transformed ones
	*/
	public String[][] getOriginalTransformedRecord()
	{
		Keywords.numread++;
		String[][] resval=new String[posreqvar.size()][2];
		outrecord=false;
		while (!outrecord)
		{
			if (!tabletype.equalsIgnoreCase("view"))
				tempr=dtr.getRecord();
			else
				tempr=dtv.getRecord();
			outrecord=true;
			if (tempr==null)
			{
				clast=true;
				return null;
			}
			if (varcondition!=null) outrecord=CheckCondition(tempr);
			if (!outrecord && isLast()) return null;
		}
		for(int i=0; i<posreqvar.size(); i++)
		{
			resval[i][0]=tempr[posreqvar.get(i)];
			resval[i][1]=tempr[posreqvar.get(i)];
			if(replace[i]==1)
			{
				resval[i][1]=codelabel.get(i).replace(resval[i][1]);
				resval[i][1]=missingdata.get(i).replace(resval[i][1]);
			}
			if(replace[i]==2)
				resval[i][1]=codelabel.get(i).replace(resval[i][1]);
			else if(replace[i]==3)
				resval[i][1]=missingdata.get(i).replace(resval[i][1]);
		}
		if (istowrite) resval=getwriteformatfordouble(resval);
		return resval;
	}
	/**
	*Return true if the last observation was accessed
	*/
	public boolean isLast()
	{
		if (!clast)
		{
			if (!tabletype.equalsIgnoreCase("view"))
				return dtr.isLast();
			else
				return dtv.isLast();
		}
		else return true;
	}
	/**
	*Return true if the last observation was accessed
	*/
	public boolean deletetable()
	{
		if (!tabletype.equalsIgnoreCase("view"))
			return dtr.deletetable();
		else
			return dtv.deletetable();
	}
	/**
	*Close the data table.<p>
	*If it is returned a false, than there was an error closing the data table
	*/
	public boolean close()
	{
		Keywords.operationReading=false;
		boolean cl=true;
		if (!tabletype.equalsIgnoreCase("view"))
			cl=dtr.close();
		else
			cl=dtv.close();
		if ((!cl) && (!tabletype.equalsIgnoreCase("view")))
			msgDataReader=dtr.getMessage();
		if ((!cl) && (tabletype.equalsIgnoreCase("view")))
			msgDataReader=dtv.getMessage();
		if (tabletype.equalsIgnoreCase("view"))
		{
			dtv=null;
			classtoread=null;
			(new File(ted+"Read_view"+vref)).delete();
		}
		return cl;
	}
	/**
	*Returns an int with the number of records in the data table.<p>
	*If such int is equal to 0, than an error can be happened... so check the returned message.<p>
	*Note: such methods must be called before the OPEN
	*/
	public int getRecords()
	{
		String temptabletype=dr.getdatatabletype();
		if (!temptabletype.equalsIgnoreCase("view"))
		{
			Hashtable<String, String> temptableinfo=dr.getdatatableinfo();
			try
			{
				Class<?> tempclas= Class.forName(Keywords.SoftwareName+".dataaccess.Read_"+temptabletype.toLowerCase());
				DataTableReader tempdtr = (DataTableReader)tempclas.newInstance();
				int totrecords=tempdtr.getRecords(temptableinfo);
				if (totrecords==0)
					msgDataReader=tempdtr.getMessage();
				return totrecords;
			}
			catch (ClassNotFoundException In)
			{
				msgDataReader="%352% ("+temptabletype+")<br>\n";
				return 0;
			}
			catch (Exception e)
			{
				msgDataReader="%353% ("+temptabletype+")<br>\n";
				return 0;
			}
		}
		else
		{
			Hashtable<String, Object> parview=dr.getparameters();
			String td=(String)parview.get(Keywords.WorkDir);
			String vreft=dr.getviewclassref();
			byte[] file=(byte[])dr.getviewclass();
			try
			{
				FileOutputStream out = new FileOutputStream(td+"Read_view"+vreft+".class");
				out.write(file);
				out.close();
				file=new byte[0];
				file=null;
			}
			catch (Exception e)
			{
				msgDataReader="%1517%\n";
				return 0;
			}
			try
			{
				File fileclass = new File(td);
				URL url = fileclass.toURI().toURL();
				URL[] urls = new URL[]{url};
				classtoexecute = new URLClassLoader(urls);
				Class<?> tempclas = classtoexecute.loadClass("Read_view"+vreft);
				DataTableViewer tempdtv = (DataTableViewer)tempclas.newInstance();
				tempdtv.setparameters(parview);
				int totrecords=tempdtv.getRecords(null);
				if (totrecords==0)
					msgDataReader=tempdtv.getMessage();
				tempdtv=null;
				tempclas=null;
				(new File(td+"Read_view"+vreft)).delete();
				return totrecords;
			}
			catch (Exception e)
			{
				(new File(td+"Read_view"+vreft)).delete();
				msgDataReader="%1518%<br>\n";
				return 0;
			}
		}
	}
	/**
	*Initializes the container of the code labels and of the missing data
	*/
	protected void setContainers(Vector<Hashtable<String, String>> codelabel, Vector<Hashtable<String, String>> missingdata)
	{
		Iterator<Hashtable<String, String>> it = codelabel.iterator();
		while(it.hasNext())
		{
			Hashtable<String, String> current = it.next();
			Iterator<String> itt = current.keySet().iterator();
			CLContainer tmpCode = new CLContainer();
			while(itt.hasNext())
			{
				String rule = itt.next();
				tmpCode.addRule(rule,current.get(rule));
			}
			this.codelabel.add(tmpCode);
		}
		it = missingdata.iterator();
		while(it.hasNext())
		{
			Hashtable<String, String> current = it.next();
			Iterator<String> itt = current.keySet().iterator();
			MDContainer tmpCode = new MDContainer();
			while(itt.hasNext())
			{
				String rule = itt.next();
				tmpCode.addRule(rule,current.get(rule));
			}
			this.missingdata.add(tmpCode);
		}
	}
	/**
	*Replaces the values according to tha actual writing formats
	*/
	private String[] getwriteformat(String[] record)
	{
		if (writeformat==null)
			return record;
		if (record.length!=writeformat.size())
			return record;
		for(int i=0;i<record.length;i++)
		{
			String writefmt=writeformat.get(i);
			if ((writefmt.length()==Keywords.NUMSuffix.length())&& (writefmt.toUpperCase().startsWith(Keywords.NUMSuffix.toUpperCase())))
			{
				try
				{
					double roundval=Double.valueOf(record[i]).doubleValue();
					Format f=new Format("%f");
					record[i]=f.format(roundval);
					record[i]=record[i].trim();
				}
				catch (Exception ex) {}
			}
			else if ((writefmt.length()>Keywords.NUMSuffix.length())&& (writefmt.toUpperCase().startsWith(Keywords.NUMSuffix.toUpperCase())))
			{
				String num=writefmt.substring(Keywords.NUMSuffix.length());
				if ((num.toUpperCase()).startsWith(Keywords.DTSuffix))
				{
					try
					{
						double roundval=Double.valueOf(record[i]).doubleValue();
						record[i] = DateFormat.getDateTimeInstance().format(new Date((long)roundval));
					}
					catch (Exception ex) {}
				}
				else if ((num.toUpperCase()).startsWith(Keywords.DATESuffix))
				{
					try
					{
						if (System.getProperty("writedatefmt")==null)
						{
							double roundval=Double.valueOf(record[i]).doubleValue();
							record[i] = DateFormat.getDateInstance().format(new Date((long)roundval));
						}
						else
						{
							String fval=System.getProperty("writedatefmt");
							if (fval.equals(""))
							{
								double roundval=Double.valueOf(record[i]).doubleValue();
								record[i] = DateFormat.getDateInstance().format(new Date((long)roundval));
							}
							else
							{
								double roundval=Double.valueOf(record[i]).doubleValue();
								Locale lc= Locale.getDefault();
								SimpleDateFormat sdf = new SimpleDateFormat(fval, lc);
								Calendar cal = Calendar.getInstance();
								long offset = cal.get(Calendar.ZONE_OFFSET);
								record[i]=sdf.format(new Date((long)roundval-offset));
							}
						}
					}
					catch (Exception ex) {}
				}
				else if ((num.toUpperCase()).startsWith(Keywords.TIMESuffix))
				{
					try
					{
						double roundval=Double.valueOf(record[i]).doubleValue();
						record[i] = DateFormat.getTimeInstance().format(new Date((long)roundval));
					}
					catch (Exception ex) {}
				}
				else if ((num.toUpperCase()).startsWith(Keywords.INTSuffix))
				{
					try
					{
						double roundval=Double.valueOf(record[i]).doubleValue();
						Format f=new Format("%f");
						record[i]=f.format(roundval);
						record[i]=record[i].substring(0,record[i].indexOf("."));
						record[i]=record[i].trim();
					}
					catch (Exception ex) {}
				}
				else if ((num.toUpperCase()).startsWith(Keywords.DECSuffix))
				{
					String num1=writefmt.substring(Keywords.NUMSuffix.length()+Keywords.DECSuffix.length());
					try
					{
						double roundval=Double.valueOf(record[i]).doubleValue();
						int numFormatDEC=Integer.parseInt(num1.trim());
						if (numFormatDEC==0)
						{
							record[i]=""+Math.round(roundval);
						}
						else
						{
							String formatType="%."+numFormatDEC+"f";
							Format f=new Format(formatType);
							record[i]=f.format(roundval);
							record[i]=record[i].trim();
						}
					}
					catch (Exception ex) {}
				}
				else if ((num.toUpperCase()).startsWith(Keywords.EXPSuffix))
				{
					try
					{
						double roundval=Double.valueOf(record[i]).doubleValue();
						Format f=new Format("%E");
						record[i]=f.format(roundval);
						record[i]=record[i].trim();
					}
					catch (Exception ex) {}
				}
			}
			else if ((writefmt.length()>Keywords.TEXTSuffix.length())&& (writefmt.toUpperCase().startsWith(Keywords.TEXTSuffix.toUpperCase())))
			{
				String num=writefmt.substring(Keywords.TEXTSuffix.length());
				int numFormatInt=0;
				try
				{
					numFormatInt=Integer.parseInt(num);
					record[i]=record[i].substring(0,numFormatInt);
				}
				catch (Exception ex) {}
			}
		}
		return record;
	}
	/**
	*Replaces the values according to tha actual writing formats
	*/
	private String[][] getwriteformatfordouble(String[][] record)
	{
		if (writeformat==null)
			return record;
		if (record.length!=writeformat.size())
			return record;
		for(int i=0;i<record.length;i++)
		{
			String writefmt=writeformat.get(i);
			if ((writefmt.length()==Keywords.NUMSuffix.length())&& (writefmt.toUpperCase().startsWith(Keywords.NUMSuffix.toUpperCase())))
			{
				try
				{
					double roundval=Double.valueOf(record[i][1]).doubleValue();
					Format f=new Format("%f");
					record[i][1]=f.format(roundval);
					record[i][1]=record[i][1].trim();
				}
				catch (Exception ex) {}
			}
			else if ((writefmt.length()>Keywords.NUMSuffix.length())&& (writefmt.toUpperCase().startsWith(Keywords.NUMSuffix.toUpperCase())))
			{
				String num=writefmt.substring(Keywords.NUMSuffix.length());
				if ((num.toUpperCase()).startsWith(Keywords.DTSuffix))
				{
					try
					{
						double roundval=Double.valueOf(record[i][1]).doubleValue();
						record[i][1] = DateFormat.getDateTimeInstance().format(new Date((long)roundval));
					}
					catch (Exception ex) {}
				}
				else if ((num.toUpperCase()).startsWith(Keywords.DATESuffix))
				{
					try
					{
						if (System.getProperty("writedatefmt")==null)
						{
							double roundval=Double.valueOf(record[i][1]).doubleValue();
							record[i][1] = DateFormat.getDateInstance().format(new Date((long)roundval));
						}
						else
						{
							String fval=System.getProperty("writedatefmt");
							if (fval.equals(""))
							{
								double roundval=Double.valueOf(record[i][1]).doubleValue();
								record[i][1] = DateFormat.getDateInstance().format(new Date((long)roundval));
							}
							else
							{
								double roundval=Double.valueOf(record[i][1]).doubleValue();
								Locale lc= Locale.getDefault();
								SimpleDateFormat sdf = new SimpleDateFormat(fval, lc);
								Calendar cal = Calendar.getInstance();
								long offset = cal.get(Calendar.ZONE_OFFSET);
								record[i][1]=sdf.format(new Date((long)roundval-offset));
							}
						}
					}
					catch (Exception ex) {}
				}
				else if ((num.toUpperCase()).startsWith(Keywords.TIMESuffix))
				{
					try
					{
						double roundval=Double.valueOf(record[i][1]).doubleValue();
						record[i][1] = DateFormat.getTimeInstance().format(new Date((long)roundval));
					}
					catch (Exception ex) {}
				}
				else if ((num.toUpperCase()).startsWith(Keywords.INTSuffix))
				{
					try
					{
						double roundval=Double.valueOf(record[i][1]).doubleValue();
						Format f=new Format("%f");
						record[i][1]=f.format(roundval);
						record[i][1]=record[i][1].substring(0,record[i][1].indexOf("."));
						record[i][1]=record[i][1].trim();
					}
					catch (Exception ex) {}
				}
				else if ((num.toUpperCase()).startsWith(Keywords.DECSuffix))
				{
					String num1=writefmt.substring(Keywords.NUMSuffix.length()+Keywords.DECSuffix.length());
					try
					{
						double roundval=Double.valueOf(record[i][1]).doubleValue();
						int numFormatDEC=Integer.parseInt(num1.trim());
						if (numFormatDEC==0)
						{
							record[i][1]=""+Math.round(roundval);
						}
						else
						{
							String formatType="%."+numFormatDEC+"f";
							Format f=new Format(formatType);
							record[i][1]=f.format(roundval);
							record[i][1]=record[i][1].trim();
						}
					}
					catch (Exception ex) {}
				}
				else if ((num.toUpperCase()).startsWith(Keywords.EXPSuffix))
				{
					try
					{
						double roundval=Double.valueOf(record[i][1]).doubleValue();
						Format f=new Format("%E");
						record[i][1]=f.format(roundval);
						record[i][1]=record[i][1].trim();
					}
					catch (Exception ex) {}
				}
			}
			else if ((writefmt.length()>Keywords.TEXTSuffix.length())&& (writefmt.toUpperCase().startsWith(Keywords.TEXTSuffix.toUpperCase())))
			{
				String num=writefmt.substring(Keywords.TEXTSuffix.length());
				int numFormatInt=0;
				try
				{
					numFormatInt=Integer.parseInt(num);
					record[i][1]=record[i][1].substring(0,numFormatInt);
				}
				catch (Exception ex) {}
			}
		}
		return record;
	}
}
