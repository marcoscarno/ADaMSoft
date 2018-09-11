/**
* Copyright © 2006-2013 CINECA
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

import java.util.Hashtable;
import java.util.Vector;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.AddCodeLabelVerifier;
import ADaMSoft.utilities.SingleSetting;

/**
* Executes the setting steps
* @author marco.scarno@cineca.it
* @version 1.0.0, rev.: 01/07/13 by marco
*/
public class SettingRunner
{
	String message;
	boolean steperror;
	public SettingRunner (Vector<String> KeywordValue)
	{
		message="";
		steperror=false;
		String type="";
		String name="";
		Hashtable<String, String> parameter=new Hashtable<String, String>();
		boolean isformat=false;
		int typeaction=0;
		for (int i=0; i<KeywordValue.size(); i++)
		{
			String actualvalue=KeywordValue.get(i);
			actualvalue=actualvalue.trim();
			String firstpart="";
			try
			{
				firstpart=actualvalue.substring(0,actualvalue.indexOf(" "));
				firstpart=firstpart.trim();
			}
			catch (Exception e)	{}
			if (firstpart.equalsIgnoreCase(Keywords.SETTING))
			{
				try
				{
					type=actualvalue.substring(actualvalue.indexOf(" "));
					type=type.trim();
				}
				catch (Exception e)
				{
					message=Keywords.Language.getMessage(47)+"<br>\n";
					steperror=true;
					return;
				}
				if (type.equalsIgnoreCase(Keywords.clear))
					typeaction=1;
				if (type.equalsIgnoreCase(Keywords.CODELABEL))
					isformat=true;
			}
			if (firstpart.equalsIgnoreCase(Keywords.Name))
			{
				try
				{
					name=actualvalue.substring(actualvalue.indexOf(" "));
					name=name.trim();
				}
				catch (Exception e)
				{
					message=Keywords.Language.getMessage(48)+"<br>\n";
					steperror=true;
					return;
				}
			}
			if ((!actualvalue.equalsIgnoreCase(Keywords.END)) && (typeaction==0))
			{
				if ((actualvalue.equalsIgnoreCase(Keywords.clear)) && (name.equalsIgnoreCase("")))
					typeaction=2;
				if ((actualvalue.equalsIgnoreCase(Keywords.clear)) && (!name.equalsIgnoreCase("")))
					typeaction=3;
			}
		}
		if (typeaction==0)
		{
			for (int i=0; i<KeywordValue.size(); i++)
			{
				String actualvalue=KeywordValue.get(i);
				actualvalue=actualvalue.trim();
				String firstpart="";
				try
				{
					firstpart=actualvalue.substring(0,actualvalue.indexOf(" "));
					firstpart=firstpart.trim();
				}
				catch (Exception e)
				{
					firstpart=actualvalue;
				}
				if ((!firstpart.equalsIgnoreCase(Keywords.SETTING)) && (!firstpart.equalsIgnoreCase(Keywords.Name)) && (!actualvalue.equalsIgnoreCase(Keywords.END)))
				{
					if (!isformat)
					{
						String secondpart="";
						try
						{
							secondpart=actualvalue.substring(actualvalue.indexOf(" "));
							secondpart=secondpart.trim();
						}
						catch (Exception e) {}
						parameter.put(firstpart.toLowerCase(), secondpart);
					}
					else
					{
						String[] codelabels=actualvalue.split("=");
						if (codelabels.length<2)
						{
							message=Keywords.Language.getMessage(49)+"<br>\n";
							steperror=true;
							return;
						}
						String label="";
						if (codelabels.length==2)
							label=codelabels[1];
						else
						{
							for (int j=1; j<codelabels.length; j++)
							{
								label=label+codelabels[j];
								if (i<codelabels.length-1)
									label=label+"=";
							}
						}
						AddCodeLabelVerifier aclv=new AddCodeLabelVerifier(codelabels[0]);
						if (!aclv.getMessage().equals(""))
						{
							message=aclv.getMessage();
							steperror=true;
							return;
						}
						parameter.put(Keywords.Code.toLowerCase()+"_"+codelabels[0].trim(), label.trim());
					}
				}
			}
		}
		Vector<SingleSetting> allsetting=Keywords.project.getAllSettings();
		if ((allsetting.size()==0) && (typeaction==1))
		{
			message=Keywords.Language.getMessage(54)+"<br>\n";
			steperror=true;
			return;
		}
		if (typeaction!=1)
		{
			if (typeaction==0)
			{
				SingleSetting ss = new SingleSetting(type, name);
				ss.addParameter(parameter);
				Keywords.project.addSingleSetting(ss);
			}
			else
			{
				Vector<String> types = Keywords.project.getSettingsTypes();
				for (int i=0; i<types.size(); i++)
				{
					String actualtype=types.get(i);
					if (typeaction==2)
					{
						Vector<?> names = Keywords.project.getSettingNames(actualtype);
						for (int j=0; j<names.size(); j++)
						{
							String actualvalue=(String)names.get(j);
							if (actualtype.equalsIgnoreCase(type))
							{
								Keywords.project.delSetting(actualvalue, actualtype);
							}
						}
					}
					if (typeaction==3)
					{
						Vector<?> names = Keywords.project.getSettingNames(actualtype);
						for (int j=0; j<names.size(); j++)
						{
							String actualvalue=(String)names.get(j);
							Keywords.project.delSetting(actualvalue,actualtype);
						}
					}
				}
			}
		}
		if (typeaction==0)
			message=message+Keywords.Language.getMessage(55)+"<br>\n";
		if (typeaction==1)
		{
			Keywords.project.clearSettings();
			message=message+Keywords.Language.getMessage(56)+"<br>\n";
		}
		if (typeaction==2)
			message=message+Keywords.Language.getMessage(57)+"<br>\n";
		if (typeaction==3)
			message=message+Keywords.Language.getMessage(58)+"<br>\n";
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
