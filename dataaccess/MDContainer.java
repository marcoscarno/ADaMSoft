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

import java.util.Vector;

import ADaMSoft.keywords.Keywords;

/**
* This class contains the classes that are used to represent the missing data rules<p>
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
public class MDContainer extends RuleContainer
{
	/**
	*This is the container for the missing data rules
	*/
	public MDContainer()
	{
		container=new Vector<Rule>();
		rules=new Vector<String>();
	}
	/**
	*Parse the rule
	*/
	public Rule parseRule(String rule)
	{
		return parseRule(rule,"");
	}
	/**
	*Parse the rule
	*/
	protected Rule parseRule(String rule, String value)
	{
		try
		{
			if(rule.toUpperCase().startsWith(Keywords.IGNORECASE))
			{
				int fBracket, lBracket;
				fBracket = rule.indexOf('(');
				lBracket = rule.lastIndexOf(')');
				String key = rule.substring(fBracket+1,lBracket);
				return new ICRule(value,key);
			}
			else if(rule.toUpperCase().startsWith(Keywords.STARTSWITH))
			{
				int fBracket, lBracket;
				fBracket = rule.indexOf('(');
				lBracket = rule.lastIndexOf(')');
				String key = rule.substring(fBracket+1,lBracket);
				return new SWRule(value,key);
			}
			else if(rule.toUpperCase().startsWith(Keywords.ENDSWITH))
			{
				int fBracket, lBracket;
				fBracket = rule.indexOf('(');
				lBracket = rule.lastIndexOf(')');
				String key = rule.substring(fBracket+1,lBracket);
				return new EWRule(value,key);
			}
			else if(rule.toUpperCase().startsWith("["))
			{
				String values;
				values = rule.replace("[","");
				if(rule.endsWith("]"))
				{
					values = values.replace("]","");
					values=values.trim();
					String[] vals = values.split(":");
					double lowlimit=-1.7976931348623157E308;
					double uplimit=Double.MAX_VALUE;
					if (!vals[0].equalsIgnoreCase("-"+Keywords.INF))
						lowlimit=Double.parseDouble(vals[0]);
					if (!vals[1].equalsIgnoreCase(Keywords.INF))
						uplimit=Double.parseDouble(vals[1]);
					return new INORule(value, lowlimit,uplimit);
				}
				if(rule.endsWith(")"))
				{
					values = values.replace(")","");
					values=values.trim();
					String[] vals = values.split(":");
					double lowlimit=-1.7976931348623157E308;
					double uplimit=Double.MAX_VALUE;
					if (!vals[0].equalsIgnoreCase("-"+Keywords.INF))
						lowlimit=Double.parseDouble(vals[0]);
					if (!vals[1].equalsIgnoreCase(Keywords.INF))
						uplimit=Double.parseDouble(vals[1]);
					return new INORule(value, lowlimit,uplimit);
				}
			}
			else if(rule.toUpperCase().startsWith("("))
			{
				String values;
				values = rule.replace("(","");
				if(rule.endsWith("]"))
				{
					values = values.replace("]","");
					values=values.trim();
					String[] vals = values.split(":");
					double lowlimit=-1.7976931348623157E308;
					double uplimit=Double.MAX_VALUE;
					if (!vals[0].equalsIgnoreCase("-"+Keywords.INF))
						lowlimit=Double.parseDouble(vals[0]);
					if (!vals[1].equalsIgnoreCase(Keywords.INF))
						uplimit=Double.parseDouble(vals[1]);
					return new ILORule(value, lowlimit,uplimit);
				}
				if(rule.endsWith(")"))
				{
					values = values.replace(")","");
					values=values.trim();
					String[] vals = values.split(":");
					double lowlimit=-1.7976931348623157E308;
					double uplimit=Double.MAX_VALUE;
					if (!vals[0].equalsIgnoreCase("-"+Keywords.INF))
						lowlimit=Double.parseDouble(vals[0]);
					if (!vals[1].equalsIgnoreCase(Keywords.INF))
						uplimit=Double.parseDouble(vals[1]);
					return new IBORule(value, lowlimit,uplimit);
				}
			}
			try
			{
				double test=Double.parseDouble(rule);
				return new EQRule(value, test);
			}
			catch (Exception ee)
			{
				rule=rule.replace("\"","");
				return new EQRule(value, rule);
			}
		}
		catch (Exception e)
		{
			return new NULLRule("");
		}
	}
}
