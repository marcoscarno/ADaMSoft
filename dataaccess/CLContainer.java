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

import java.util.Vector;
import java.lang.Double;
import ADaMSoft.keywords.Keywords;

/**
* This class contains the classes that are used to represent the code labels rules<p>
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class CLContainer extends RuleContainer
{
	/**
	*Initializes the classes
	*/
	public CLContainer()
	{
		super.container=new Vector<Rule>();
		super.rules=new Vector<String>();
	}
	/**
	*Parse the rule
	*/
	public Rule parseRule(String rule, String value)
	{
		try
		{
			if(rule.equalsIgnoreCase(Keywords.other))
			{
				other = new ORule(value);
				return new NULLRule("");
			}
			else if(rule.toUpperCase().startsWith(Keywords.IGNORECASE))
			{
				int fBracket, lBracket;
				fBracket = rule.indexOf('(');
				lBracket = rule.lastIndexOf(')');
				String key = rule.substring(fBracket+1, lBracket);
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
					if (!vals[1].toUpperCase().endsWith(Keywords.INF))
						uplimit=Double.parseDouble(vals[1]);
					return new INORule(value,lowlimit,uplimit);
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
					if (!vals[1].toUpperCase().endsWith(Keywords.INF))
						uplimit=Double.parseDouble(vals[1]);
					return new IRORule(value,lowlimit,uplimit);
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
					if (!vals[1].toUpperCase().endsWith(Keywords.INF))
						uplimit=Double.parseDouble(vals[1]);
					return new ILORule(value,lowlimit,uplimit);
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
					if (!vals[1].toUpperCase().endsWith(Keywords.INF))
						uplimit=Double.parseDouble(vals[1]);
					return new IBORule(value,lowlimit,uplimit);
				}
			}
			else
			{
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
			return new NULLRule("");
		}
		catch (Exception e)
		{
			return new NULLRule("");
		}
	}
}
/**
* This is the empty rule, do nothing
*/
class NULLRule implements Rule
{
	String value;
	public NULLRule(String replace)
	{
		value = replace;
	}
	public String replace(String value)
	{
		return value;
	}
	public boolean isreplaced()
	{
		return false;
	}
}
/**
* This is the rule OTHER
*/
class ORule implements Rule
{
	String value;
	public ORule(String replace)
	{
		value = replace;
	}
	public String replace(String value)
	{
		return this.value;
	}
	public boolean isreplaced()
	{
		return false;
	}
}
/**
* This is the rule EQUALS
*/
class EQRule implements Rule
{
	String value, key;
	double test;
	boolean ruleisnum;
	boolean replaced;
	public EQRule(String replace, String key)
	{
		replaced=false;
		value = replace;
		this.key = key;
		ruleisnum=false;
	}
	public EQRule(String replace, double test)
	{
		replaced=false;
		value = replace;
		this.test = test;
		ruleisnum=true;
	}
	public String replace(String value)
	{
		replaced=false;
		if (ruleisnum)
		{
			try
			{
				double receivedtest=Double.parseDouble(value);
				if (receivedtest==test)
				{
					replaced=true;
					return this.value;
				}
			}
			catch (Exception ee) {}
		}
		if (value.equals(key))
		{
			replaced=true;
			return this.value;
		}
		return value;
	}
	public boolean isreplaced()
	{
		return replaced;
	}
}
/**
* This is the rule IGNORECASE
*/
class ICRule implements Rule
{
	String value, key;
	boolean replaced;
	public ICRule(String replace, String key)
	{
		replaced=false;
		value = replace;
		this.key = key;
	}
	public String replace(String value)
	{
		replaced=false;
		if (value.equalsIgnoreCase(key))
		{
			replaced=true;
			return this.value;
		}
		return value;
	}
	public boolean isreplaced()
	{
		return replaced;
	}
}
/**
* This is the rule STARTWITH
*/
class SWRule implements Rule
{
	String value, key;
	boolean replaced;
	public SWRule(String replace, String key)
	{
		replaced=false;
		value = replace;
		this.key = key;
	}
	public String replace(String value)
	{
		replaced=false;
		if (value.startsWith(key))
		{
			replaced=true;
			return this.value;
		}
		return value;
	}
	public boolean isreplaced()
	{
		return replaced;
	}
}
/**
* This is the rule ENDWITH
*/
class EWRule implements Rule
{
	String value, key;
	boolean replaced;
	public EWRule(String replace, String key)
	{
		replaced=false;
		value = replace;
		this.key = key;
	}
	public String replace(String value)
	{
		replaced=false;
		if (value.endsWith(key))
		{
			replaced=true;
			return this.value;
		}
		return value;
	}
	public boolean isreplaced()
	{
		return replaced;
	}
}
/**
* This is the interval rule ()
*
*/
class IBORule implements Rule
{
	double inf, sup;
	String replace;
	boolean replaced;
	public IBORule(String replace, double inf, double sup)
	{
		replaced=false;
		this.replace = replace;
		this.inf = inf;
		this.sup = sup;
	}
	public String replace(String value)
	{
		replaced=false;
		try
		{
			double parsedVal = Double.parseDouble(value);
			if ((parsedVal < sup) && (parsedVal > inf))
			{
				replaced=true;
				return replace;
			}
		}
		catch (Exception ee) {}
		return value;
	}
	public boolean isreplaced()
	{
		return replaced;
	}
}
/**
* This is the interval rule (]
*/
class ILORule implements Rule
{
	double inf, sup;
	String replace, key;
	boolean replaced;
	public ILORule(String replace,double inf, double sup)
	{
		replaced=false;
		this.replace = replace;
		this.inf = inf;
		this.sup = sup;
	}
	public String replace(String value)
	{
		replaced=false;
		try
		{
			double parsedVal = Double.parseDouble(value);
			if ((parsedVal <= sup) && (parsedVal > inf))
			{
				replaced=true;
				return replace;
			}
		}
		catch (Exception ee) {}
		return value;
	}
	public boolean isreplaced()
	{
		return replaced;
	}
}
/**
* This is the interval rule [)
*/
class IRORule implements Rule
{
	double inf, sup;
	String replace;
	boolean replaced;
	public IRORule(String replace,double inf, double sup)
	{
		replaced=false;
		this.replace = replace;
		this.inf = inf;
		this.sup = sup;
	}
	public String replace(String value)
	{
		replaced=false;
		try
		{
			double parsedVal = Double.parseDouble(value);
			if ((parsedVal < sup) && (parsedVal >= inf))
			{
				replaced=true;
				return replace;
			}
		}
		catch (Exception ee) {}
		return value;
	}
	public boolean isreplaced()
	{
		return replaced;
	}
}
/**
* This is the interval rule []
*/
class INORule  implements Rule
{
	double inf, sup;
	String replace;
	boolean replaced;
	public INORule(String replace, double inf, double sup)
	{
		replaced=false;
		this.replace = replace;
		this.inf = inf;
		this.sup = sup;
	}
	public String replace(String value)
	{
		replaced=false;
		try
		{
			double parsedVal = Double.parseDouble(value);
			if ((parsedVal <= sup) && (parsedVal >= inf))
			{
				replaced=true;
				return replace;
			}
		}
		catch (Exception ee) {}
		return value;
	}
	public boolean isreplaced()
	{
		return replaced;
	}
}