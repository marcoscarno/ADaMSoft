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
import java.io.Serializable;

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;

/**
* This class contains several utilities for the final variables that will be written in a data set
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/
public class DataSetUtilities implements Serializable
{
	/**
	 * This is the default serial version UID
	 */
	private static final long serialVersionUID = 1L;
	int pointerfornewvar;
	Vector<Integer> newvarposition;
	Vector<Integer> origposition;
	Vector<Hashtable<String, String>> newvar;
	Vector<Hashtable<String, String>> newvarcl;
	Vector<Hashtable<String, String>> newvarmd;
	DictionaryReader olddict;
	boolean replaceaction;
	Vector<String> addedvars;
	/**
	*Initializes the method
	*/
	public DataSetUtilities()
	{
		newvar=new Vector<Hashtable<String, String>>();
		newvarcl=new Vector<Hashtable<String, String>>();
		newvarmd=new Vector<Hashtable<String, String>>();
		pointerfornewvar=0;
		olddict=null;
		newvarposition=new Vector<Integer>();
		replaceaction=false;
		addedvars=new Vector<String>();
		origposition=new Vector<Integer>();
	}
	/**
	*Receive the info about the replace action; this is used in order to change, eventually, the code label in case of replace all or replace codelabel
	*/
	public void setreplace(String replace)
	{
		if (replace==null)
			replaceaction=false;
		else if (replace.equalsIgnoreCase(Keywords.replaceall))
			replaceaction=true;
		else if (replace.equalsIgnoreCase(Keywords.replaceformat))
			replaceaction=true;
		else if (replace.equalsIgnoreCase(Keywords.replacemissing))
			replaceaction=false;
	}
	/**
	*Add a new variable by receiving its name, its label, its writing format, the hashtable of the code labels and of the missing data rules
	*/
	public void addnewvar(String varname, String varlabel, String newvarwf, Hashtable<String, String> tempnewvarcl, Hashtable<String, String> tempnewvarmd)
	{
		addedvars.add(varname);
		Hashtable<String, String> temp=new Hashtable<String, String>();
		temp.put(Keywords.VariableName.toLowerCase(), varname);
		temp.put(Keywords.VariableFormat.toLowerCase(), newvarwf);
		temp.put(Keywords.LabelOfVariable.toLowerCase(), varlabel);
		newvar.add(temp);
		newvarcl.add(tempnewvarcl);
		newvarmd.add(tempnewvarmd);
		pointerfornewvar++;
		newvarposition.add(new Integer(pointerfornewvar));
	}
	/**
	*Add a new variable according to what is defined into another dictionary
	*/
	public void addnewvarfromolddict(DictionaryReader dict, String varname, Hashtable<String, String> cl, Hashtable<String, String> md, String newname)
	{
		Vector<Hashtable<String, String>> fixedvariableinfo=dict.getfixedvariableinfo();
		Vector<Hashtable<String, String>> codelabel=dict.getcodelabel();
		Vector<Hashtable<String, String>> missingdata=dict.getmissingdata();
		for (int i=0; i<fixedvariableinfo.size(); i++)
		{
			Hashtable<String, String> tempvar=fixedvariableinfo.get(i);
			String tvarname=(tempvar.get(Keywords.VariableName.toLowerCase())).trim();
			if (tvarname.equalsIgnoreCase(varname.trim()))
			{
				Hashtable<String, String> newtempi=new Hashtable<String, String>();
				for (Enumeration<String> en=tempvar.keys(); en.hasMoreElements();)
				{
					String code=en.nextElement();
					String value=tempvar.get(code);
					if (!code.equalsIgnoreCase(Keywords.VariableName.toLowerCase()))
					{
						if (!replaceaction)
							newtempi.put(code.toLowerCase(),value);
						else
						{
							if (code.toLowerCase().equals(Keywords.VariableFormat.toLowerCase()))
							{
								if (!code.toLowerCase().startsWith(Keywords.TEXTSuffix.toLowerCase()))
								{
									if (dict.iscodelabeltext(tvarname))
										value=Keywords.TEXTSuffix;
									newtempi.put(code.toLowerCase(),value);
								}
								else
									newtempi.put(code.toLowerCase(),value);
							}
							else
								newtempi.put(code.toLowerCase(),value);
						}
					}
					else
						newtempi.put(code.toLowerCase(), newname);
				}
				newvar.add(newtempi);
				if (cl!=null)
					newvarcl.add(cl);
				else
					newvarcl.add(codelabel.get(i));
				if (md!=null)
					newvarmd.add(md);
				else
					newvarmd.add(missingdata.get(i));
			}
		}
	}
	/**
	*Takes the information for new variables from an existent dictionary
	*/
	public void defineolddict(DictionaryReader dict)
	{
		olddict=dict;
		Vector<Hashtable<String, String>> fixedvariableinfo=dict.getfixedvariableinfo();
		Vector<Hashtable<String, String>> codelabel=dict.getcodelabel();
		Vector<Hashtable<String, String>> missingdata=dict.getmissingdata();

		pointerfornewvar=fixedvariableinfo.size();

		for (int i=0; i<fixedvariableinfo.size(); i++)
		{
			Hashtable<String, String> tempi=fixedvariableinfo.get(i);
			Hashtable<String, String> tempc=codelabel.get(i);
			Hashtable<String, String> tempm=missingdata.get(i);
			Hashtable<String, String> newtempi=new Hashtable<String, String>();
			Hashtable<String, String> newtempc=new Hashtable<String, String>();
			Hashtable<String, String> newtempm=new Hashtable<String, String>();
			for (Enumeration<String> en=tempi.keys(); en.hasMoreElements();)
			{
				String code=en.nextElement();
				String value=tempi.get(code);
				if (!replaceaction)
					newtempi.put(code.toLowerCase(),value);
				else
				{
					if (code.toLowerCase().equals(Keywords.VariableFormat.toLowerCase()))
					{
						if (!code.toLowerCase().startsWith(Keywords.TEXTSuffix.toLowerCase()))
						{
							if (dict.iscodelabeltext(dict.getvarname(i)))
								value=Keywords.TEXTSuffix;
							else
								newtempi.put(code.toLowerCase(),value);
						}
						else
							newtempi.put(code.toLowerCase(),value);
					}
					else
						newtempi.put(code.toLowerCase(),value);
				}
				newtempi.put(code,value);
			}
			for (Enumeration<String> en=tempc.keys(); en.hasMoreElements();)
			{
				String code=en.nextElement();
				String value=tempc.get(code);
				newtempc.put(code,value);
			}
			for (Enumeration<String> en=tempm.keys(); en.hasMoreElements();)
			{
				String code=en.nextElement();
				String value=tempm.get(code);
				newtempm.put(code,value);
			}
			newvar.add(newtempi);
			newvarcl.add(newtempc);
			newvarmd.add(newtempm);
		}
	}
	/**
	*Sets the old dictionary
	*/
	public void setolddict(DictionaryReader dict)
	{
		olddict=dict;
	}
	/**
	*Sets the old dictionary
	*/
	public void setpointerfornewvar(int pointerfornewvar)
	{
		this.pointerfornewvar=pointerfornewvar;
	}
	/**
	*Takes the information for new variables from an existent dictionary, but only for the list of the selected variables (selvar)
	*/
	public void defineolddictwithvar(DictionaryReader dict, String[] selvar)
	{
		Vector<Hashtable<String, String>> fixedvariableinfo=dict.getfixedvariableinfo();
		Vector<Hashtable<String, String>> codelabel=dict.getcodelabel();
		Vector<Hashtable<String, String>> missingdata=dict.getmissingdata();

		for (int j=0; j<selvar.length; j++)
		{
			for (int i=0; i<fixedvariableinfo.size(); i++)
			{
				Hashtable<String, String> tempvar=fixedvariableinfo.get(i);
				Hashtable<String, String> tempcl=codelabel.get(i);
				Hashtable<String, String> tempmd=missingdata.get(i);
				String varname=(tempvar.get(Keywords.VariableName.toLowerCase())).trim();
				if (varname.equalsIgnoreCase(selvar[j].trim()))
				{
					origposition.add(new Integer(i));
					Hashtable<String, String> newtempi=new Hashtable<String, String>();
					for (Enumeration<String> en=tempvar.keys(); en.hasMoreElements();)
					{
						String code=en.nextElement();
						String value=tempvar.get(code);
						if (!replaceaction)
							newtempi.put(code.toLowerCase(),value);
						else
						{
							if (code.toLowerCase().equals(Keywords.VariableFormat.toLowerCase()))
							{
								if (!code.toLowerCase().startsWith(Keywords.TEXTSuffix.toLowerCase()))
								{
									if (dict.iscodelabeltext(dict.getvarname(i)))
										value=Keywords.TEXTSuffix;
									else
										newtempi.put(code.toLowerCase(),value);
								}
								else
									newtempi.put(code.toLowerCase(),value);
							}
							else
								newtempi.put(code.toLowerCase(),value);
						}
						newtempi.put(code,value);
					}
					newvar.add(newtempi);
					newvarcl.add(tempcl);
					newvarmd.add(tempmd);
				}
			}
		}
	}
	/**
	*This method is used to set the position of the new variables in the array that will be written
	*/
	public void setnewvarposition(int reftotvar)
	{
		for (int i=0; i<reftotvar; i++)
		{
			newvarposition.add(new Integer(i));
		}
		pointerfornewvar=reftotvar;
	}
	/**
	*This method is used to substitute (or to add) a new variable to an existent dictionary
	*/
	public void addnewvartoolddict(String varname, String varlabel, String varwf, Hashtable<String, String> tempnewvarcl, Hashtable<String, String> tempnewvarmd)
	{
		addedvars.add(varname);
		Vector<Hashtable<String, String>> fixedvariableinfo=null;
		if (olddict!=null)
			fixedvariableinfo=olddict.getfixedvariableinfo();
		else
		{
			fixedvariableinfo=new Vector<Hashtable<String, String>>();
			for (int i=0; i<newvar.size(); i++)
			{
				Hashtable<String, String> ta=newvar.get(i);
				Hashtable<String, String> tb=new Hashtable<String, String>();
				for (Enumeration<String> en=ta.keys(); en.hasMoreElements();)
				{
					String ttc=en.nextElement();
					String ttv=ta.get(ttc);
					tb.put(ttc, ttv);
				}
				fixedvariableinfo.add(tb);
			}
		}

		boolean exist=false;
		for (int i=0; i<fixedvariableinfo.size(); i++)
		{
			Hashtable<String, String> tempvar=fixedvariableinfo.get(i);
			String oldvarname=(tempvar.get(Keywords.VariableName.toLowerCase())).trim();
			if (oldvarname.equalsIgnoreCase(varname))
			{
				exist=true;
				Hashtable<String, String> temp=new Hashtable<String, String>();
				temp.put(Keywords.VariableName.toLowerCase(), varname);
				temp.put(Keywords.VariableFormat.toLowerCase(), varwf);
				temp.put(Keywords.LabelOfVariable.toLowerCase(), varlabel);
				newvar.set(i, temp);
				newvarcl.set(i, tempnewvarcl);
				newvarmd.set(i, tempnewvarmd);
				newvarposition.add(new Integer(i));
			}
		}
		if (!exist)
		{
			Hashtable<String, String> temp=new Hashtable<String, String>();
			temp.put(Keywords.VariableName.toLowerCase(), varname);
			temp.put(Keywords.VariableFormat.toLowerCase(), varwf);
			temp.put(Keywords.LabelOfVariable.toLowerCase(), varlabel);
			newvar.add(temp);
			newvarcl.add(tempnewvarcl);
			newvarmd.add(tempnewvarmd);
			newvarposition.add(new Integer(pointerfornewvar));
			pointerfornewvar++;
		}
	}
	/**
	*This method is used to empty the code labels
	*/
	public void setempycodelabels()
	{
		for (int i=0; i<newvar.size(); i++)
		{
			boolean isoldvar=true;
			Hashtable<String, String> tempvinfo=newvar.get(i);
			String oldname=(tempvinfo.get(Keywords.VariableName.toLowerCase())).trim();
			for (int j=0; j<addedvars.size(); j++)
			{
				String newname=(addedvars.get(j)).trim();
				if (oldname.equalsIgnoreCase(newname))
					isoldvar=false;
			}
			if(isoldvar)
			{
				Hashtable<String, String> temp=new Hashtable<String, String>();
				newvarcl.set(i, temp);
			}
		}
	}
	/**
	*This method is used to empty the missing data rules
	*/
	public void setempymissingdata()
	{
		for (int i=0; i<newvar.size(); i++)
		{
			boolean isoldvar=true;
			Hashtable<String, String> tempvinfo=newvar.get(i);
			String oldname=(tempvinfo.get(Keywords.VariableName.toLowerCase())).trim();
			for (int j=0; j<addedvars.size(); j++)
			{
				String newname=(addedvars.get(j)).trim();
				if (oldname.equalsIgnoreCase(newname))
					isoldvar=false;
			}
			if(isoldvar)
			{
				Hashtable<String, String> temp=new Hashtable<String, String>();
				newvarmd.set(i, temp);
			}
		}
	}
	/**
	*Returns the variables definitions
	*/
	public Vector<Hashtable<String, String>> getfinalvarinfo()
	{
		return newvar;
	}
	/**
	*Returns the code labels rules
	*/
	public Vector<Hashtable<String, String>> getfinalcl()
	{
		return newvarcl;
	}
	/**
	*Returns the missing data rules
	*/
	public Vector<Hashtable<String, String>> getfinalmd()
	{
		return newvarmd;
	}
	/**
	*Returns the number of the new variables
	*/
	public int gettotalvarnum()
	{
		return newvar.size();
	}
	/**
	*Receives the array of old values, the array of new values and returns the array of the values that can be written in the new data set
	*/
	public String[] getnewvalues(String[] oldvalues, String[] newvalues)
	{
		if (newvarposition.size()==0)
			return newvalues;
		String[] returnedvalues=new String[newvar.size()];
		for (int i=0; i<oldvalues.length; i++)
		{
			returnedvalues[i]=oldvalues[i];
		}
		for (int i=0; i<newvalues.length; i++)
		{
			int newposition=(newvarposition.get(i)).intValue();
			returnedvalues[newposition]=newvalues[i];
		}
		return returnedvalues;
	}
	/**
	*Receives the array of old values, the array of new values and returns the array of the values that can be written in the new data set
	*/
	public String[] getoldnewvalues(String[] oldvalues, String[] newvalues)
	{
		if (newvarposition.size()==0)
			return newvalues;
		String[] returnedvalues=new String[newvar.size()];
		for (int i=0; i<origposition.size(); i++)
		{
			int newposition=(origposition.get(i)).intValue();
			returnedvalues[i]=oldvalues[newposition];
		}
		for (int i=0; i<newvalues.length; i++)
		{
			int newposition=(newvarposition.get(i)).intValue();
			returnedvalues[newposition]=newvalues[i];
		}
		return returnedvalues;
	}
}
