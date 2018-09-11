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

import java.io.File;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.AdamsCompliant;
import ADaMSoft.utilities.DataTableInfo;
import ADaMSoft.utilities.GenericContainerForDict;
import ADaMSoft.utilities.GenericContainerForParameters;

/**
* This class reads a Dictionary and contains several methods to retrieve the information that are in it
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
public class DictionaryReader extends AdamsCompliant implements Serializable
{
	/**
	*This is the defult serial version UID
	*/
	private static final long serialVersionUID = 1L;
	/**
	*These are the variable names
	*/
	String[] varnames;
	/**
	*These are the variable labels
	*/
	String[] varlabels;
	/**
	*These are the variable labels
	*/
	String[] varformats;
	/**
	*This is the number of variables;
	*/
	int totalvar;
	/**
	 * This is the path where the dictionary is stored
	 */
	String dictPath="";
	/**
	*This is the creation date
	*/
	String creationdate="";
	/**
	*This is the type of the associated data table
	*/
	String datatabletype="";
	/**
	*These are the keywords associated to the data set
	*/
	String keyword="";
	/**
	*This is the description of the data set
	*/
	String description="";
	/**
	*This is the information on who creates the dictionary
	*/
	String author="";
	/**
	*These hashtable contains the information in the data table
	*/
	DataTableInfo datatableinfo;
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
	*This vector contains, in each record, an hashtable with the label and the code defined on each variable.
	*/
	Vector<Hashtable<String, String>> labelcode;
	/**
	*This vector contains, in each element, the int value that represent the first code that can be used to add new codelabel to the variable
	*/
	int[] labelcodeends;
	/**
	*This vector contains, in each record, an hashtable with the missing data rule defined on each variable.
	*/
	GenericContainerForDict missingdata;
	/**
	*This is the message, if the dictionary cannot be read, that is returned
	*/
	String messageDictionaryReader="";
	/**
	*If true the dictionary is remote
	*/
	boolean remote;
	/**
	*Contains the class required to access to a dataset view
	*/
	Object viewclass;
	/**
	*Contains the reference to the class required to access to a dataset view
	*/
	String viewclassref;
	/**
	*Contains the parameter required by the dataset view
	*/
	GenericContainerForParameters parameterview;
	boolean[] hascodelabel;
	boolean conversion=true;
	/**
	*Receives the path of the dictionary and initializes the different objects that can be retrieved.<p>
	*Returne false if the dictionary cannot be read.<p>
	*/
	public DictionaryReader(String dictionaryfile)
	{
		conversion=true;
		varnames=new String[0];
		varlabels=new String[0];
		varformats=new String[0];
		remote=false;
		viewclass=null;
		parameterview=null;
		viewclassref=null;
		if (dictionaryfile.toUpperCase().startsWith(Keywords.WorkDir.toUpperCase()))
			dictionaryfile=System.getProperty(Keywords.WorkDir)+dictionaryfile.substring(Keywords.WorkDir.length());
		if(!dictionaryfile.toUpperCase().endsWith(Keywords.DictionaryExtension.toUpperCase()))
			dictionaryfile=dictionaryfile+Keywords.DictionaryExtension;
		dictionaryfile = toAdamsFormat(dictionaryfile);
		dictPath=dictionaryfile;
		try
		{
			java.net.URL dictionaryurl;

			if((dictionaryfile.toLowerCase()).startsWith("http"))
				dictionaryurl =  new java.net.URL(dictionaryfile);
			else
			{
				File fileDictionary=new File(dictionaryfile);
				dictionaryurl = fileDictionary.toURI().toURL();
			}

			URLConnection      urlConn;
			urlConn = dictionaryurl.openConnection();
			urlConn.setDoInput(true);
			urlConn.setUseCaches(false);
			ZipInputStream indata= new ZipInputStream(urlConn.getInputStream());
			ZipEntry entry=null;
			entry = indata.getNextEntry();
			ObjectInputStream ois = new ObjectInputStream(indata);

			do
			{
				String nameFile = entry.getName();
				if (nameFile.equalsIgnoreCase(Keywords.remotedict))
				{
					remote=true;
				}
				if (nameFile.equalsIgnoreCase(Keywords.CreationDate))
				{
					creationdate=(String)ois.readObject();
				}
				if (nameFile.equalsIgnoreCase(Keywords.keyword))
				{
					keyword=(String)ois.readObject();
				}
				if (nameFile.equalsIgnoreCase(Keywords.description))
				{
					description=(String)ois.readObject();
				}
				if (nameFile.equalsIgnoreCase(Keywords.author))
				{
					author=(String)ois.readObject();
				}
				if (nameFile.equalsIgnoreCase(Keywords.DataTableType))
				{
					datatabletype=(String)ois.readObject();
				}
				if (nameFile.equalsIgnoreCase(Keywords.DataTableInfo))
				{
					datatableinfo=(DataTableInfo)ois.readObject();
				}
				if (nameFile.equalsIgnoreCase(Keywords.FixedVariablesInfo))
				{
					fixedvariableinfo=(GenericContainerForDict)ois.readObject();
					totalvar=fixedvariableinfo.size();
					varnames=new String[totalvar];
					varlabels=new String[totalvar];
					varformats=new String[totalvar];
					for (int i=0; i<totalvar; i++)
					{
						Hashtable<String, String> tempvar=fixedvariableinfo.get(i);
						varnames[i]=(tempvar.get(Keywords.VariableName.toLowerCase())).trim();
						varlabels[i]=(tempvar.get(Keywords.LabelOfVariable.toLowerCase())).trim();
						varformats[i]=(tempvar.get(Keywords.VariableFormat.toLowerCase())).trim();
					}
				}
				if (nameFile.equalsIgnoreCase("Parameterview"))
				{
					parameterview=(GenericContainerForParameters)ois.readObject();
				}
				if (nameFile.equalsIgnoreCase(Keywords.TableVariablesInfo))
				{
					tablevariableinfo=(GenericContainerForDict)ois.readObject();
				}
				if (nameFile.equalsIgnoreCase(Keywords.CodeLabels))
				{
					codelabel=(GenericContainerForDict)ois.readObject();
					labelcode=new Vector<Hashtable<String, String>>();
					labelcodeends=new int[codelabel.size()];
					hascodelabel=new boolean[codelabel.size()];
					for (int i=0; i<codelabel.size(); i++)
					{
						labelcodeends[i]=99;
						Hashtable<String, String> lc=new Hashtable<String, String>();
						labelcode.add(lc);
						Hashtable<String, String> tlc=codelabel.get(i);
						if (tlc.size()>0)
							hascodelabel[i]=true;
						else
							hascodelabel[i]=false;
					}
				}
				if (nameFile.equalsIgnoreCase(Keywords.MissingDataValues))
				{
					missingdata=(GenericContainerForDict)ois.readObject();
				}
				if (nameFile.equalsIgnoreCase("Read_view"))
				{
					viewclass=ois.readObject();
				}
				if (nameFile.equalsIgnoreCase("Ref_Read_view"))
				{
					viewclassref=(String)ois.readObject();
				}
				indata.closeEntry();
	        } while((entry = indata.getNextEntry()) != null);
			ois.close();
		}
		catch (Exception e)
		{
			messageDictionaryReader=Keywords.Language.getMessage(255)+" ("+dictionaryfile+")\n"+e.toString()+"<br>\n";
			return;
		}
		return;
	}
	/**
	*If this method is called, then the values will not be converted into numbers
	*/
	public void setnoconversion()
	{
		conversion=false;
	}
	/**
	*Creates the label code object
	*/
	public void setlabelcode()
	{
		for (int i=0; i<codelabel.size(); i++)
		{
			Hashtable<String, String> cl=codelabel.get(i);
			Hashtable<String, String> lc=labelcode.get(i);
			lc.clear();
			int tempval=99;
			for (Enumeration<String> en=cl.keys(); en.hasMoreElements();)
			{
				String rule = en.nextElement();
				String val = cl.get(rule);
				if(rule.toUpperCase().startsWith(Keywords.IGNORECASE))
				{
					String key="";
					try
					{
						int fBracket, lBracket;
						fBracket = rule.indexOf('(');
						lBracket = rule.lastIndexOf(')');
						key = rule.substring(fBracket+1, lBracket);
					}
					catch (Exception et) {}
					if (lc.get(val)==null)
						lc.put(val, key);
					else
					{
						if (key.equals(""))
							lc.put(val, key);
						else if (!lc.get(val).equals(""))
							lc.put(val, key);
					}
					if (!key.equals(""))
					{
						try
						{
							int lowlimit=(int)(Double.parseDouble(key));
							if (lowlimit>=tempval)
								tempval=lowlimit+2;
						}
						catch (Exception et) {}
					}
				}
				else if(rule.toUpperCase().startsWith(Keywords.STARTSWITH))
				{
					String key=null;
					try
					{
						int fBracket, lBracket;
						fBracket = rule.indexOf('(');
						lBracket = rule.lastIndexOf(')');
						key = rule.substring(fBracket+1,lBracket);
					}
					catch (Exception et) {}
					if (lc.get(val)==null)
						lc.put(val, key);
					else
					{
						if (key.equals(""))
							lc.put(val, key);
						else if (!lc.get(val).equals(""))
							lc.put(val, key);
					}
					if (!key.equals(""))
					{
						try
						{
							int lowlimit=(int)(Double.parseDouble(key));
							if (lowlimit>=tempval)
								tempval=lowlimit+2;
						}
						catch (Exception et) {}
					}
				}
				else if(rule.toUpperCase().startsWith(Keywords.ENDSWITH))
				{
					String key=null;
					try
					{
						int fBracket, lBracket;
						fBracket = rule.indexOf('(');
						lBracket = rule.lastIndexOf(')');
						key = rule.substring(fBracket+1,lBracket);
					}
					catch (Exception et) {}
					if (lc.get(val)==null)
						lc.put(val, key);
					else
					{
						if (key.equals(""))
							lc.put(val, key);
						else if (!lc.get(val).equals(""))
							lc.put(val, key);
					}
					if (!key.equals(""))
					{
						try
						{
							int lowlimit=(int)(Double.parseDouble(key));
							if (lowlimit>=tempval)
								tempval=lowlimit+2;
						}
						catch (Exception et) {}
					}
				}
				else if(rule.toUpperCase().startsWith("["))
				{
					String values;
					values = rule.replace("[","");
					String key="";
					if(rule.endsWith("]"))
					{
						double uplimit=Double.MAX_VALUE;
						try
						{
							values = values.replace("]","");
							values=values.trim();
							String[] vals = values.split(":");
							double lowlimit=-1.7976931348623157E308;
							if (!vals[0].equalsIgnoreCase("-"+Keywords.INF))
								lowlimit=Double.parseDouble(vals[0]);
							if (!vals[1].equalsIgnoreCase(Keywords.INF))
								uplimit=Double.parseDouble(vals[1]);
							key=String.valueOf((uplimit+lowlimit)/2);
						}
						catch (Exception et) {}
						if (lc.get(val)==null)
							lc.put(val, key);
						else
						{
							if (key.equals(""))
								lc.put(val, key);
							else if (!lc.get(val).equals(""))
								lc.put(val, key);
						}
						if (!key.equals(""))
						{
							try
							{
								int lowlimitn=(int)(uplimit);
								if (lowlimitn>=tempval)
									tempval=lowlimitn+2;
							}
							catch (Exception et) {}
						}
					}
					if(rule.endsWith(")"))
					{
						double uplimit=Double.MAX_VALUE;
						try
						{
							values = values.replace(")","");
							values=values.trim();
							String[] vals = values.split(":");
							double lowlimit=-1.7976931348623157E308;
							if (!vals[0].equalsIgnoreCase("-"+Keywords.INF))
								lowlimit=Double.parseDouble(vals[0]);
							if (!vals[1].equalsIgnoreCase(Keywords.INF))
								uplimit=Double.parseDouble(vals[1]);
							key=String.valueOf((uplimit+lowlimit)/2);
						}
						catch (Exception et) {}
						if (lc.get(val)==null)
							lc.put(val, key);
						else
						{
							if (key.equals(""))
								lc.put(val, key);
							else if (!lc.get(val).equals(""))
								lc.put(val, key);
						}
						if (!key.equals(""))
						{
							try
							{
								int lowlimitn=(int)(uplimit);
								if (lowlimitn>=tempval)
									tempval=lowlimitn+2;
							}
							catch (Exception et) {}
						}
					}
				}
				else if(rule.toUpperCase().startsWith("("))
				{
					String key="";
					String values;
					values = rule.replace("(","");
					if(rule.endsWith("]"))
					{
						double uplimit=Double.MAX_VALUE;
						try
						{
							values = values.replace("]","");
							values=values.trim();
							String[] vals = values.split(":");
							double lowlimit=-1.7976931348623157E308;
							if (!vals[0].equalsIgnoreCase("-"+Keywords.INF))
								lowlimit=Double.parseDouble(vals[0]);
							if (!vals[1].equalsIgnoreCase(Keywords.INF))
								uplimit=Double.parseDouble(vals[1]);
							key=String.valueOf((uplimit+lowlimit)/2);
						}
						catch (Exception et) {}
						if (lc.get(val)==null)
							lc.put(val, key);
						else
						{
							if (key.equals(""))
								lc.put(val, key);
							else if (!lc.get(val).equals(""))
								lc.put(val, key);
						}
						if (!key.equals(""))
						{
							try
							{
								int lowlimitn=(int)(uplimit);
								if (lowlimitn>=tempval)
									tempval=lowlimitn+2;
							}
							catch (Exception et) {}
						}
					}
					if(rule.endsWith(")"))
					{
						double uplimit=Double.MAX_VALUE;
						try
						{
							values = values.replace(")","");
							values=values.trim();
							String[] vals = values.split(":");
							double lowlimit=-1.7976931348623157E308;
							if (!vals[0].equalsIgnoreCase("-"+Keywords.INF))
								lowlimit=Double.parseDouble(vals[0]);
							if (!vals[1].equalsIgnoreCase(Keywords.INF))
								uplimit=Double.parseDouble(vals[1]);
							key=String.valueOf((uplimit+lowlimit)/2);
						}
						catch (Exception et) {}
						if (lc.get(val)==null)
							lc.put(val, key);
						else
						{
							if (key.equals(""))
								lc.put(val, key);
							else if (!lc.get(val).equals(""))
								lc.put(val, key);
						}
						if (!key.equals(""))
						{
							try
							{
								int lowlimitn=(int)(uplimit);
								if (lowlimitn>=tempval)
									tempval=lowlimitn+2;
							}
							catch (Exception et) {}
						}
					}
				}
				else if(!rule.equalsIgnoreCase(Keywords.other))
				{
					try
					{
						double valnum=Double.parseDouble(rule);
						int lowlimitn=(int)valnum;
						if (lowlimitn>=tempval)
							tempval=lowlimitn+2;
					}
					catch (Exception enu){}
					lc.put(val, rule);
				}
			}
			labelcode.set(i, lc);
			labelcodeends[i]=tempval;
		}
	}
	/**
	*Returns the class required to access to a dataset view
	*/
	public Object getviewclass()
	{
		return viewclass;
	}
	/**
	*Returns the name of the view class
	*/
	public String getviewclassref()
	{
		return viewclassref;
	}
	/**
	*Returns the parameters used to access a data view
	*/
	public Hashtable<String, Object> getparameters()
	{
		return parameterview;
	}
	/**
	*Returns the creation date of the dictionary
	*/
	public String getcreationdate()
	{
		return creationdate;
	}
	/**
	*Returns the type of the data table
	*/
	public String getdatatabletype()
	{
		return datatabletype;
	}
	/**
	*Returns the keywords associated to the data set
	*/
	public String getkeyword()
	{
		return keyword;
	}
	/**
	*Returns the description associated to the data set
	*/
	public String getdescription()
	{
		return description;
	}
	/**
	*Returns the author of the data set
	*/
	public String getauthor()
	{
		return author;
	}
	/**
	*Returns an hashtable of keywords and values that describes the data table.<p>
	*These will be passed to the data reader methods in order to access the records.
	*/
	public Hashtable<String, String> getdatatableinfo()
	{
		return datatableinfo;
	}
	/**
	*This vector contains, in each record, an hashtable with the fixed information for the variable.<p>
	*Such information are:<p>
	*name, label, writeformat.
	*/
	public Vector<Hashtable<String, String>> getfixedvariableinfo()
	{
		return fixedvariableinfo;
	}
	/**
	*This vector contains, in each record, an hashtable with the information on the variables that are specific
	*for the type of the data table.
	*/
	public Vector<Hashtable<String, String>> gettablevariableinfo()
	{
		return tablevariableinfo;
	}
	/**
	*This vector contains, in each record, an hashtable with the code and the label defined on each variable.
	*/
	public Vector<Hashtable<String, String>> getcodelabel()
	{
		return codelabel;
	}
	/**
	*This vector contains, in each record, an hashtable with the missing data rule defined on each variable.
	*/
	public Vector<Hashtable<String, String>> getmissingdata()
	{
		return missingdata;
	}
	/**
	*Returns an empty string if the dictionary was read ok, otherwise the error message
	*/
	public String getmessageDictionaryReader()
	{
		return messageDictionaryReader;
	}
	/**
	*Returns the total number of variables
	*/
	public int gettotalvar()
	{
		return totalvar;
	}
	/**
	*Return the name of the rif variable
	*/
	public String getvarname(int rif)
	{
		return varnames[rif];
	}
	/**
	*Return the label of the rif variable
	*/
	public String getvarlabel(int rif)
	{
		return varlabels[rif];
	}
	/**
	*Return the label of the variable with the given name
	*/
	public String getvarlabelfromname(String name)
	{
		for (int i=0; i<totalvar; i++)
		{
			if (varnames[i].equalsIgnoreCase(name.trim()))
				return varlabels[i];
		}
		return "";
	}
	/**
	*Return the writeformat of the rif variable
	*/
	public String getvarformat(int rif)
	{
		return varformats[rif];
	}
	/**
	*Return the writeformat of the variable with the given name
	*/
	public String getvarformatfromname(String name)
	{
		for (int i=0; i<totalvar; i++)
		{
			if (varnames[i].equalsIgnoreCase(name.trim()))
				return varformats[i];
		}
		return "";
	}
	/**
	*Return the codelabel of the variable with the given name
	*/
	public Hashtable<String, String> getcodelabelfromname(String name)
	{
		Hashtable<String, String> cl=new Hashtable<String, String>();
		for (int i=0; i<totalvar; i++)
		{
			if (varnames[i].equalsIgnoreCase(name.trim()))
			{
				cl=codelabel.get(i);
				return cl;
			}
		}
		return cl;
	}
	/**
	*Return the code associated to the label for the variable with the given name<p>
	*It can return null if the label is not defined or if it is multiple
	*/
	public String getlabelcodefromname(String name, String label)
	{
		for (int i=0; i<totalvar; i++)
		{
			if (varnames[i].equalsIgnoreCase(name.trim()))
			{
				Hashtable<String, String> cl=labelcode.get(i);
				if (cl.get(label)!=null)
					return cl.get(label);
				if (conversion)
				{
					try
					{
						double valnum=Double.parseDouble(label);
						for (Enumeration<String> en=cl.keys(); en.hasMoreElements();)
						{
							String par = en.nextElement();
							String val = cl.get(par);
							try
							{
								double parnum=Double.parseDouble(par);
								if (valnum==parnum)
									return val;
							}
							catch (Exception enuu) {}
						}
					}
					catch (Exception enu)
					{
						return null;
					}
				}
			}
		}
		return null;
	}
	/**
	*Returns the next usable code to represent the values not defined int the code labels
	*/
	public int getusablecodefromname(String name)
	{
		for (int i=0; i<totalvar; i++)
		{
			if (varnames[i].equalsIgnoreCase(name.trim()))
			{
				return labelcodeends[i];
			}
		}
		return 0;
	}
	/**
	*Returns the true if the variable name has codelabel, otherwise false
	*/
	public boolean checkhaslabel(String name)
	{
		for (int i=0; i<totalvar; i++)
		{
			if (varnames[i].equalsIgnoreCase(name.trim()))
			{
				return hascodelabel[i];
			}
		}
		return false;
	}
	/**
	*Return the codelabel of the ref variable
	*/
	public String[][] getcodelabelfromref(int ref)
	{
		Hashtable<String, String> cl=fixedvariableinfo.get(ref);
		if (cl.size()==0)
			return null;
		String[][] clret=new String[cl.size()][2];
		int pointer=0;
		for (Enumeration<String> en=cl.keys(); en.hasMoreElements();)
		{
			String par = en.nextElement();
			String val = cl.get(par);
			clret[pointer][0]=par;
			clret[pointer][1]=val;
			pointer++;
		}
		return clret;
	}
	/**
	*Return the missing data rules of the variable with the given name
	*/
	public Hashtable<String, String> getmissingdatafromname(String name)
	{
		Hashtable<String, String> md=new Hashtable<String, String>();
		for (int i=0; i<totalvar; i++)
		{
			if (varnames[i].equalsIgnoreCase(name.trim()))
			{
				md=missingdata.get(i);
				return md;
			}
		}
		return md;
	}
	/**
	*Return true if the codelabel for the given variable are alphanumerical, othewise false
	*Note: this is used to test if the replace all must transform the variable from type NUM to TEXT
	*/
	public boolean iscodelabeltext(String name)
	{
		for (int i=0; i<totalvar; i++)
		{
			if (varnames[i].equalsIgnoreCase(name.trim()))
			{
				if (varformats[i].toLowerCase().startsWith(Keywords.TEXTSuffix.toLowerCase()))
					return true;
				else
				{
					Hashtable<String, String> cl=codelabel.get(i);
					for (Enumeration<String> en=cl.keys(); en.hasMoreElements();)
					{
						String par = en.nextElement();
						String val = cl.get(par);
						try
						{
							Double.parseDouble(val);
						}
						catch (Exception e)
						{
							return true;
						}
					}
					return false;
				}
			}
		}
		return false;
	}
	/**
	*Returns the dictionary name and path
	*/
	public String getDictPath()
	{
		return dictPath;
	}
	/**
	*Returns true if the data table is remote
	*/
	public boolean isRemote()
	{
		return remote;
	}
}