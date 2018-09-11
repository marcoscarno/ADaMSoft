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

package ADaMSoft.supervisor;

import java.util.Iterator;
import java.util.TreeMap;

import ADaMSoft.keywords.Keywords;

/**
* Executes the define steps
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class DefineRunner
{
	String message;
	boolean steperror;
	public DefineRunner (String actualstatement)
	{
		message="";
		steperror=false;
		actualstatement=actualstatement.trim();
		String valdefine="";
		try
		{
			valdefine=actualstatement.substring(actualstatement.indexOf(" "));
			valdefine=valdefine.trim();
		}
		catch (Exception ex)
		{
			message=Keywords.Language.getMessage(39)+"<br>\n";
			steperror=true;
			return;
		}
		int type=0;
		try
		{
			valdefine=valdefine.replaceAll("\"","'");
		}
		catch (Exception ed){}
		String [] infodefine;
		String definename="";
		String definevalue="";
		if (valdefine.equalsIgnoreCase(Keywords.clear))
			type=1;
		else if (valdefine.indexOf("=")>0)
		{
			infodefine=valdefine.split("=");
			if (infodefine.length>2)
			{
				String tempdef="";
				for (int i=1; i<infodefine.length; i++)
				{
					tempdef=tempdef+infodefine[i];
				}
				String tempdefv=infodefine[0];
				infodefine=new String[2];
				infodefine[0]=tempdefv;
				infodefine[1]=tempdef;
			}
			if (infodefine.length!=2)
			{
				message=Keywords.Language.getMessage(39)+"<br>\n";
				steperror=true;
				return;
			}
			definename=infodefine[0].toLowerCase();
			if (infodefine[1].equalsIgnoreCase(Keywords.clear))
			{
				type=2;
				if (infodefine[0].equalsIgnoreCase("openeddirectory") || infodefine[0].equalsIgnoreCase("main_directory") || infodefine[0].equalsIgnoreCase("workdir"))
				{
					message=Keywords.Language.getMessage(3975)+"<br>\n";
					steperror=true;
					return;
				}
			}
			else
				definevalue=infodefine[1];
		}
		else
		{
			message=Keywords.Language.getMessage(39)+"<br>\n";
			steperror=true;
			return;
		}
		if (type>0)
		{
			TreeMap<String, String> defineddefinition=Keywords.project.getNamesAndDefinitions();
			if (defineddefinition.size()==0)
			{
				message=Keywords.Language.getMessage(40)+"<br>\n";
				return;
			}
			if (type==2)
			{
				boolean nameexist=false;
				for (Iterator<String> it = defineddefinition.keySet().iterator(); it.hasNext();)
				{
					String actualdefinition = it.next();
					if (actualdefinition.equalsIgnoreCase(definename))
						nameexist=true;
				}
				if (!nameexist)
				{
					message=Keywords.Language.getMessage(41)+"<br>\n";
					return;
				}
			}
		}

		if (type==0)
			Keywords.project.addDefinition(definename, definevalue);
		if (type==1)
			Keywords.project.clearDefines();
		if (type==2)
			Keywords.project.delDefine(definename);

		if (type==0)
			message=message+Keywords.Language.getMessage(43)+"<br>\n";
		if (type==1)
			message=message+Keywords.Language.getMessage(45)+"<br>\n";
		if (type==2)
			message=message+Keywords.Language.getMessage(44)+"<br>\n";
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
