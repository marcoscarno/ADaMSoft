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

package ADaMSoft.algorithms;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.utilities.VectorStringComparator;
import ADaMSoft.utilities.VectorStringComparatorNoC;

/**
* This method stores all the different modalities of the grouping variables
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class VarGroupModalities
{
	boolean nullnumber;
	Vector<Vector<String>> finalmodalities;
	Vector<Vector<String>> finalcodes;
	Vector<Hashtable<String, String>> groupcodelabels;
	Vector<Hashtable<String, String>> codelabels;
	TreeSet<Vector<String>> checkgroup;
	TreeSet<Vector<String>> testcheckgroup;
	String[] usedvars;
	DictionaryReader dr;
	boolean useorigcl;
	boolean orderbycode;
	boolean noconversion;
	/**
	*Initialise the main Objects, that will contains the different modalities of the grouping variables
	*/
	public VarGroupModalities()
	{
		useorigcl=false;
		usedvars=null;
		orderbycode=false;
		checkgroup=new TreeSet<Vector<String>>(new VectorStringComparator());
		testcheckgroup=new TreeSet<Vector<String>>(new VectorStringComparatorNoC());
		groupcodelabels=new Vector<Hashtable<String, String>>();
		codelabels=new Vector<Hashtable<String, String>>();
		finalmodalities=new Vector<Vector<String>>();
		finalcodes=new Vector<Vector<String>>();
		nullnumber=false;
		noconversion=false;
	}
	/**
	*This method is called when it is required the attempt to transform the values in numbers
	*/
	public void conversion()
	{
		noconversion=false;
		checkgroup=new TreeSet<Vector<String>>(new VectorStringComparator());
	}
	/**
	*This method is called when it is not required the attempt to transform the values in numbers
	*/
	public void noconversion()
	{
		noconversion=true;
		checkgroup=new TreeSet<Vector<String>>(new VectorStringComparatorNoC());
	}
	/**
	*If this method is called then the final values will be ordered by considering the codes
	*/
	public void setorderbycode()
	{
		orderbycode=true;
	}
	/**
	*These array contains the names of the used variables
	*/
	public void setvarnames(String[] usedvars)
	{
		this.usedvars=usedvars;
	}
	/**
	*Here the dictionary is passed to the method
	*/
	public void setdictionary(DictionaryReader dr)
	{
		this.dr=dr;
		if (usedvars!=null)
			useorigcl=true;
	}
	/**
	* Update the modalities
	*/
	public void updateModalities(Vector<String> groupval)
	{
		Vector<String> newg=new Vector<String>();
		for (int i=0; i<groupval.size(); i++)
		{
			newg.add(groupval.get(i));
		}
		String test=newg.get(0);
		nullnumber=false;
		if (test==null)
			nullnumber=true;
		if (!testcheckgroup.contains(newg))
		{
			checkgroup.add(newg);
			testcheckgroup.add(newg);
		}
	}
	/**
	*Finalize the calculation of the modalities and of the code labels to associate to each variable
	*/
	public void calculate()
	{
		if (nullnumber)
		{
			Vector<String> tempv=new Vector<String>();
			tempv.add(null);
			finalmodalities.add(tempv);
			return;
		}
		if (useorigcl)
		{
			dr.setlabelcode();
			if (noconversion)
				dr.setnoconversion();
		}
		int[] lastrefcl=null;
		boolean[] hascl=null;
		if (useorigcl)
		{
			lastrefcl=new int[usedvars.length];
			hascl=new boolean[usedvars.length];
			for (int i=0; i<usedvars.length; i++)
			{
				lastrefcl[i]=dr.getusablecodefromname(usedvars[i]);
				hascl[i]=dr.checkhaslabel(usedvars[i]);
			}
		}
		Iterator<Vector<String>> itv=checkgroup.iterator();
		boolean containercreated=false;
		String actualsize="";
		while(itv.hasNext())
		{
			Vector<String> tempv=itv.next();
			if (!containercreated)
			{
				for (int i=0; i<tempv.size(); i++)
				{
					Hashtable<String, String> tempgc=new Hashtable<String, String>();
					groupcodelabels.add(tempgc);
				}
				containercreated=true;
			}
			finalmodalities.add(tempv);
			Vector<String> tempfinalcodes=new Vector<String>();
			for (int i=0; i<tempv.size(); i++)
			{
				Hashtable<String, String> tempgc=groupcodelabels.get(i);
				String tempvv=tempv.get(i);
				if (tempgc.get(tempvv)==null)
				{
					if (!useorigcl)
					{
						actualsize=String.valueOf(tempgc.size()+1);
						tempgc.put(tempvv, actualsize);
						tempfinalcodes.add(actualsize);
					}
					else
					{
						if (!hascl[i])
						{
							tempgc.put(tempvv, tempvv);
							tempfinalcodes.add(tempvv);
						}
						else
						{
							String lc=dr.getlabelcodefromname(usedvars[i], tempvv);
							if (lc==null)
							{
								lc=String.valueOf(lastrefcl[i]);
								lastrefcl[i]=lastrefcl[i]+1;
							}
							else if (lc.equals(""))
							{
								lc=String.valueOf(lastrefcl[i]);
								lastrefcl[i]=lastrefcl[i]+1;
							}
							tempgc.put(tempvv, lc);
							tempfinalcodes.add(lc);
						}
					}
				}
				else
				{
					tempfinalcodes.add(tempgc.get(tempvv));
				}
				groupcodelabels.set(i, tempgc);
			}
			finalcodes.add(tempfinalcodes);
		}
		if (orderbycode)
		{
			if (finalcodes.size()>1)
			{
				executesort(0, finalcodes.size()-1);
			}
		}
		checkgroup.clear();
		checkgroup=null;
		for (int i=0; i<groupcodelabels.size(); i++)
		{
			Hashtable<String, String> tempgc=groupcodelabels.get(i);
			Hashtable<String, String> newcl=new Hashtable<String, String>();
			for (Enumeration<String> e = tempgc.keys() ; e.hasMoreElements() ;)
			{
				String templabel=e.nextElement();
				String tempcode=tempgc.get(templabel);
				newcl.put(tempcode, templabel);
			}
			codelabels.add(newcl);
		}
	}
	/**
	* Return the total number of modalities
	*/
	public int getTotal()
	{
		if (nullnumber)
			return 0;
		else
			return finalmodalities.size();
	}
	/**
	*Return the i-th modalities
	*/
	public String[] getstringmodalities(int i)
	{
		if (nullnumber)
			return new String[0];
		Vector<String> tempmodalities=finalmodalities.get(i);
		String[] modalities=new String[tempmodalities.size()];
		for (int j=0; j<tempmodalities.size(); j++)
		{
			modalities[j]=tempmodalities.get(j);
		}
		return modalities;
	}
	/**
	*Returns the vector of the i-th modalities
	*/
	public Vector<String> getvectormodalities(int i)
	{
		Vector<String> tempmodalities=new Vector<String>();
		if (nullnumber)
			tempmodalities.add(null);
		else
			tempmodalities=finalmodalities.get(i);
		return tempmodalities;
	}
	/**
	*Returns the code labels for the grouping variables
	*/
	public Vector<Hashtable<String, String>> getgroupcodelabels()
	{
		return codelabels;
	}
	/**
	*Returns the code of the rifgroupvar grouping variables, for its value=label
	*/
	public String getcode(int rifgroupvar, String label)
	{
		if (nullnumber)
			return null;
		Hashtable<String, String> groupvalues=groupcodelabels.get(rifgroupvar);
		return groupvalues.get(label);
	}
	/**
	*Returns the vectors of all the possibile modalities for each grouping variable
	*/
	public Vector<Vector<String>> getfinalmodalities()
	{
		return finalmodalities;
	}
	/**
	*This is the routine used to sort the different values according to the code values
	*/
	private void executesort(int from, int to)
	{
		int i = from, j = to;
		Vector<String> center = new Vector<String>();
		for (int k=0; k<finalcodes.get((from+to)/2).size(); k++)
		{
			center.add((finalcodes.get((from+to)/2)).get(k));
		}
		do
		{
			while( (i < to) && (compare(center, finalcodes.get(i)) > 0) )
				i++;
			while( (j > from) && (compare(center, finalcodes.get(j)) < 0) )
				j--;
			if (i < j)
			{
				Vector<String> tempi = new Vector<String>();
				Vector<String> tempj = new Vector<String>();
				for (int k=0; k<finalcodes.get(i).size(); k++)
				{
					tempi.add((finalcodes.get(i)).get(k));
				}
				for (int k=0; k<finalcodes.get(j).size(); k++)
				{
					tempj.add((finalcodes.get(j)).get(k));
				}
				finalcodes.set(i, tempj);
				finalcodes.set(j, tempi);
				Vector<String> tempif = new Vector<String>();
				Vector<String> tempjf = new Vector<String>();
				for (int k=0; k<finalmodalities.get(i).size(); k++)
				{
					tempif.add((finalmodalities.get(i)).get(k));
				}
				for (int k=0; k<finalmodalities.get(j).size(); k++)
				{
					tempjf.add((finalmodalities.get(j)).get(k));
				}
				finalmodalities.set(i, tempjf);
				finalmodalities.set(j, tempif);
			}
			if (i <= j)
			{
				i++;
				j--;
			}
		}
		while(i <= j);
		if (from < j) executesort(from, j);
		if (i < to) executesort(i, to);
	}
	/**
	*Defines the rule for the comparing alghoritm applied to the Vector that contains the different values
	*/
	public int compare(Vector<String> ta, Vector<String> tb)
	{
		if((ta==null) && (tb==null))
			return 0;
		if(ta==null)
			return 1;
		if(tb==null)
			return -1;
		int sizea=ta.size();
		int sizeb=tb.size();
		if (sizeb<sizea)
			sizea=sizeb;
		for (int i=0; i<sizea; i++)
		{
			int resultcheck=0;
			String a=ta.get(i);
			String b=tb.get(i);
			resultcheck=compareref(a,b);
			if (resultcheck!=0)
				return resultcheck;
		}
		if (ta.size()<tb.size())
			return -1;
		else if (ta.size()>tb.size())
			return 1;
		else
			return 0;
	}
	/**
	*Define the rule for the String comparison
	*/
	private int compareref(String a, String b)
	{
		if ((a==null) && (b!=null))
			return 1;
		else if ((a!=null) && (b==null))
			return -1;
		else if ((a==null) && (b==null))
			return 0;
		double anum=Double.NaN;
		double bnum=Double.NaN;
		try
		{
			anum=Double.parseDouble(a);
			bnum=Double.parseDouble(b);
		}
		catch (Exception nonnumber) {}
		if ((!Double.isNaN(anum)) && (!Double.isNaN(bnum)))
		{
			if (anum>bnum)
				return 1;
			else if(anum<bnum)
				return -1;
			else
				return 0;
		}
		else
		{
			return a.compareTo(b);
		}
	}
}
