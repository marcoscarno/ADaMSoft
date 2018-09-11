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

/**
* This method gives back the array of variables name sorted according to the requested order
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/
public class SortRequestedVar
{
	/**
	*Returns the string array that corresponds to the sorted variables list, received using an Hashtable of names and positions
	*/
	public static String[] getsorted(String[] varlist, Hashtable<String, Integer> varorder)
	{
		if (varlist==null)
			return varlist;
		if (varorder.size()==0)
			return varlist;
		int pointer=0;
		String[] neworderedlist=new String[varlist.length];
		for (int i=0; i<varlist.length; i++)
		{
			if (varorder.get(varlist[i].toLowerCase())!=null)
			{
				int currentorder=(varorder.get(varlist[i].toLowerCase())).intValue();
				neworderedlist[currentorder-1]=varlist[i];
			}
			else
			{
				neworderedlist[pointer+varorder.size()]=varlist[i];
				pointer++;
			}
		}
		return neworderedlist;
	}
	/**
	*Returns the string array of the the reqvarlist, sorted according to the order in the allvarlist array
	*/
	public static String[] getreqsorted(String[] reqvarlist, String[] allvarlist)
	{
		if (reqvarlist==null)
			return reqvarlist;
		if (reqvarlist.length==0)
			return reqvarlist;
		if (allvarlist==null)
			return reqvarlist;
		if (allvarlist.length==0)
			return reqvarlist;
		if (allvarlist.length<reqvarlist.length)
			return reqvarlist;
		String[] neworderedlist=new String[allvarlist.length];
		for (int i=0; i<reqvarlist.length; i++)
		{
			for (int j=0; j<allvarlist.length; j++)
			{
				if (reqvarlist[i].equalsIgnoreCase(allvarlist[j]))
					neworderedlist[j]=reqvarlist[i];
			}
		}
		int pointer=0;
		for (int j=0; j<neworderedlist.length; j++)
		{
			if (neworderedlist[j]!=null)
			{
				reqvarlist[pointer]=neworderedlist[j];
				pointer++;
			}
		}
		neworderedlist=new String[0];
		neworderedlist=null;
		return reqvarlist;
	}
	/**
	*Returns the integer array of the new order of the reqvarlist sorted according to the order in the allvarlist array
	*/
	public static int[] getreqorder(String[] reqvarlist, String[] allvarlist)
	{
		if (reqvarlist==null)
			return null;
		int[] neworder=new int[reqvarlist.length];
		for(int i=0; i<reqvarlist.length; i++)
		{
			neworder[i]=i;
		}
		if (reqvarlist.length==0)
			return neworder;
		if (allvarlist==null)
			return neworder;
		if (allvarlist.length==0)
			return neworder;
		if (allvarlist.length<reqvarlist.length)
			return neworder;
		String[] neworderedlist=new String[allvarlist.length];
		for (int i=0; i<reqvarlist.length; i++)
		{
			for (int j=0; j<allvarlist.length; j++)
			{
				if (reqvarlist[i].equalsIgnoreCase(allvarlist[j]))
					neworderedlist[j]=reqvarlist[i];
			}
		}
		int pointer=0;
		String[] newreqvarlist=new String[reqvarlist.length];
		for (int j=0; j<neworderedlist.length; j++)
		{
			if (neworderedlist[j]!=null)
			{
				newreqvarlist[pointer]=neworderedlist[j];
				pointer++;
			}
		}
		for (int i=0; i<newreqvarlist.length; i++)
		{
			for (int j=0; j<reqvarlist.length; j++)
			{
				if (reqvarlist[j].equalsIgnoreCase(newreqvarlist[i]))
					neworder[i]=j;
			}
		}
		return neworder;
	}
}
