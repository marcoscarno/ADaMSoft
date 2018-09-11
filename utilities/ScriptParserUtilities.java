/**
* Copyright (c) MS
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

import ADaMSoft.keywords.Keywords;

/**
* This class contains several methods that are used in the script parsing
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/
public class ScriptParserUtilities
{
	/**
	*Replaces in the input String the multiple spaces with one
	*/
	public String MultipleSpacesReplacer(String input)
	{
		input=input.trim();
		String firstpart, lastpart;
		while ((input.indexOf(" "))>0)
		{
			firstpart=input.substring(0,input.indexOf(" "));
			lastpart=input.substring((input.indexOf(" "))+1);
			firstpart=firstpart.trim();
			lastpart=lastpart.trim();
			input=firstpart+"_@_"+lastpart;
		}
		input=input.replaceAll("_@_"," ");
		return input;
	}
	/**
	*Gives back a String in which the name, if present in inputstring, is replaced in upper case
	*/
	public String ReplaceUpperNameInString(String inputstring, String name)
	{
		String TempName=inputstring.toLowerCase();
		name=name.toLowerCase();
		int checkvarname=TempName.indexOf(name);
		while (checkvarname>=0)
		{
			if (checkvarname==0)
			{
				inputstring=name.toUpperCase()+inputstring.substring(checkvarname+name.length());
				TempName=name.toUpperCase()+(inputstring.substring(checkvarname+name.length())).toLowerCase();
			}
			if (checkvarname>0)
			{
				inputstring=inputstring.substring(0,checkvarname)+name.toUpperCase()+inputstring.substring(checkvarname+name.length());
				TempName=inputstring.substring(0,checkvarname)+name.toUpperCase()+(inputstring.substring(checkvarname+name.length())).toLowerCase();
			}
			checkvarname=TempName.indexOf(name);
		}
		return inputstring;
	}
	/**
	*Returns the received string, but deletes all the spaces that are between the sign: =
	*/
	public String SpacesBetweenEqualReplacer(String input)
	{
		String[] parProc=input.split("=");
		if (parProc.length>0)
		{
			input="";
			for (int i=0; i<parProc.length; i++)
			{
				input=input+parProc[i].trim();
				if (i<parProc.length-1)
					input=input+"=";
			}
		}
		return input;
	}
	/**
	* This method receives the name of the variables and replace, eventually, this name with the full list of
	* variables if the sign "-" is used to indicate several variables
	*/
	public String VarReplacer(String varnames)
	{
		varnames=varnames.trim();
		varnames=varnames.toUpperCase();
		String iniVar, endVar, FirstVar, LastVar, suffixIni, suffixEnd;
		int inivalue, endvalue;
		while ((varnames!=null) && ((varnames.indexOf("-"))>0))
		{
			iniVar=varnames.substring(0,varnames.indexOf("-"));
			iniVar=iniVar.trim();
			try
			{
				FirstVar=iniVar.substring(iniVar.lastIndexOf(" "));
			}
			catch (Exception ex)
			{
				FirstVar=iniVar;
			}
			endVar=varnames.substring(varnames.indexOf("-")+1);
			endVar=endVar.trim();
			try
			{
				LastVar=endVar.substring(0,endVar.indexOf(" "));
			}
			catch (Exception ex)
			{
				LastVar=endVar;
			}
			FirstVar=FirstVar.trim();
			LastVar=LastVar.trim();
			inivalue=-1;
			endvalue=0;
			int posinival=0;
			int posendval=0;
			for (int i=0; i<FirstVar.length(); i++)
			{
				if (posinival==0)
				{
					try
					{
						inivalue=Integer.parseInt(FirstVar.substring(i));
						posinival=i;
					}
					catch (Exception ex)
					{
						inivalue=-1;
						posinival=0;
					}
				}
			}
			suffixIni=FirstVar.substring(0,posinival);
			for (int i=0; i<LastVar.length(); i++)
			{
				if (posendval==0)
				{
					try
					{
						endvalue=Integer.parseInt(LastVar.substring(i));
						posendval=i;
					}
					catch (Exception ex)
					{
						endvalue=0;
						posendval=0;
					}
				}
			}
			suffixEnd=LastVar.substring(0,posendval);
			if (!suffixEnd.equals(suffixIni))
			{
				varnames="";
			}
			else
			{
				if (endvalue==0) varnames="";
				else if (inivalue==-1) varnames="";
				else
				{
					if (inivalue<endvalue)
					{
						for (int j=inivalue+1; j<endvalue; j++)
						{
							iniVar=iniVar+" "+suffixIni+j;
						}
						varnames=iniVar+" "+endVar;
					}
					else varnames="";
				}
			}
		}
		return varnames;
	}
	/**
	*Checks if the write formats are well written<p>
	*Possibilities:<p>
	*NUM (generic number)<p>
	*NUMDATETIME (date and time)<p>
	*NUMDATE (date )<p>
	*NUMTIME (time)<p>
	*NUMI (integer part)<p>
	*NUMD# (number with # digits of decimals)<p>
	*NUME (Exponential representation of the number)<p>
	*TEXT (a generic text)<p>
	*TEXT# (a text with # characters)<p>
	*Such method returns true if the check went OK, otherwise false
	*/
	public boolean WriteFormatVerifier(String formatname)
	{
		if ((formatname.length()==3)&& (formatname.toUpperCase().startsWith(Keywords.NUMSuffix.toUpperCase())))
			return true;
		if ((formatname.length()>3)&& (formatname.toUpperCase().startsWith(Keywords.NUMSuffix.toUpperCase())))
		{
			String num=formatname.substring(Keywords.NUMSuffix.length());
			if ((num.toUpperCase()).startsWith(Keywords.DTSuffix))
				return true;
			if ((num.toUpperCase()).startsWith(Keywords.TIMESuffix))
				return true;
			if ((num.toUpperCase()).startsWith(Keywords.DATESuffix))
				return true;
			if ((num.toUpperCase()).startsWith(Keywords.INTSuffix))
				return true;
			if ((num.toUpperCase()).startsWith(Keywords.DECSuffix))
				return true;
			if ((num.toUpperCase()).startsWith(Keywords.EXPSuffix))
				return true;
		}
		if ((formatname.length()==4)&& (formatname.toUpperCase().startsWith(Keywords.TEXTSuffix.toUpperCase())))
			return true;
		if ((formatname.length()>4)&& (formatname.toUpperCase().startsWith(Keywords.TEXTSuffix.toUpperCase())))
		{
			return true;
		}
		return false;
	}
}
