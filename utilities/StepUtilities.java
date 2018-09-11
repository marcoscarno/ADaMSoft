/**
* Copyright (c) 2015 ADaMSoft
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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
* This this class divides the values in a set of n bin between min and max
* @author marco.scarno@gmail.com
* @date 02/09/2015
*/

public class StepUtilities 
{
	Vector <String> missingparameter;
	String message;
	int existoption;
	int[] selection;
	int numselected;
	boolean checkcorrected;
	public StepUtilities()
	{
		message="";
	}
	public boolean checkParameters(String [] required, String [] optional, Hashtable<String, Object> parameters)
	{
		missingparameter=new Vector<String>();
		message="";
		for (int i=0; i<required.length; i++)
		{
			if (parameters.get(required[i])==null)
				missingparameter.add(required[i]);
		}
		if (missingparameter.size()==0)
			return true;
		message="%75%<br>\n%76%<br>\n";
		for (int i=0; i<missingparameter.size(); i++)
		{
			String[] tempparameter=(missingparameter.get(i)).split("_");
			if (tempparameter.length==1)
				message=message+tempparameter[0].toUpperCase()+";<br>\n";
			if (tempparameter.length==2)
			{
				String settingtype=tempparameter[0];
				message=message+"%77% "+settingtype.toUpperCase()+": "+tempparameter[1].toUpperCase()+";<br>\n";
			}
		}
		message=message+"%78%<br>\n";
		for (int i=0; i<required.length; i++)
		{
			String[] tempparameter=required[i].split("_");
			if (tempparameter.length==1)
				message=message+tempparameter[0].toUpperCase()+";<br>\n";
			if (tempparameter.length==2)
			{
				String settingtype=tempparameter[0];
				message=message+"%77% "+settingtype.toUpperCase()+": "+tempparameter[1].toUpperCase()+";<br>\n";
			}
		}
		message=message+"%79%<br>\n";
		for (int i=0; i<optional.length; i++)
		{
			String[] tempparameter=optional[i].split("_");
			if (tempparameter.length==1)
				message=message+tempparameter[0].toUpperCase()+";<br>\n";
			if (tempparameter.length==2)
			{
				String settingtype=tempparameter[0];
				message=message+"%77% "+settingtype.toUpperCase()+": "+tempparameter[1].toUpperCase()+";<br>\n";
			}
		}
		return false;
	}
	public String getMessage()
	{
		return message;
	}
	public int CheckOption(String [] options, String selected)
	{
		message="";
		existoption=0;
		for (int i=0; i<options.length; i++)
		{
			message=message+options[i].toUpperCase()+" ";
			if (options[i].equalsIgnoreCase(selected))
				existoption=i+1;
		}
		if (existoption==0)
			message="%642%<br>\n"+message.trim()+"<br>\n";
		return existoption;
	}
	public int getOption()
	{
		return existoption;
	}
	public boolean CheckOptions (String [] options, String userselection)
	{
		checkcorrected=true;
		message="";
		selection=new int[options.length];
		String[] selected=(userselection.trim()).split(" ");
		Hashtable<String, String> tempsel=new Hashtable<String, String>();
		for (int i=0; i<selected.length; i++)
		{
			tempsel.put(selected[i].toLowerCase(),"");
		}
		selected=new String[tempsel.size()];
		int pointer=0;
		for (Enumeration<String> e = tempsel.keys() ; e.hasMoreElements() ;)
		{
			selected[pointer]=e.nextElement();
			pointer++;
		}
		numselected=0;
		for (int j=0; j<options.length; j++)
		{
			selection[j]=0;
			for (int i=0; i<selected.length; i++)
			{
				if (options[j].equalsIgnoreCase(selected[i]))
				{
					selection[j]=1;
					numselected++;
				}
			}
		}
		if (numselected!=selected.length)
		{
			message="%661%<br>\n";
			for (int j=0; j<options.length; j++)
			{
				message=message+options[j]+" ";
			}
			message=message+"<br>\n";
			checkcorrected=false;
		}
		return checkcorrected;
	}
	public int getNumselected()
	{
		return numselected;
	}
	public int[] getSelection()
	{
		return selection;
	}
}
