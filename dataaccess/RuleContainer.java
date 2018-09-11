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

/**
* This class work with the replacement rules
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
public abstract class RuleContainer
{
	protected Vector<Rule> container;
	protected Vector<String> rules;
	Rule other = null;
	/**
	*Add a rule
	*/
	public void addRule(String rule, String value)
	{
		container.add(parseRule(rule,value));
		rules.add(rule);
	}
	/**
	*Add a rule at int position
	*/
	public void addRuleAt(String rule, String value, int pos)
	{
		container.add(pos,parseRule(rule,value));
		rules.add(pos,rule);
	}
	/**
	*Do the replacement
	*/
	public String replace(String value)
	{
		String modified = value;
		for(int i=0;i<container.size();i++)
		{
			modified=container.get(i).replace(modified);
			if (container.get(i).isreplaced())
				return modified;
		}
		if(other!=null)
		{
			modified = other.replace(value);
		}
		return modified;
	}
	/**
	*Delete rule
	*/
	public void deleteRule(String rule)
	{
		int pos=rules.indexOf(rule);
		if(pos!=-1)
		{
			container.remove(pos);
			rules.remove(pos);
		}
	}
	/**
	*Delete a rule at int position
	*/
	public void deleteRuleAt(int pos)
	{
		container.remove(pos);
	}
	/**
	*Delete all the rules
	*/
	public void deleteAllRules()
	{
		container.clear();
	}
	/**
	*Returns the number of the rules
	*/
	public int size()
	{
		return container.size();
	}
	abstract protected Rule parseRule(String rule, String value);
}
/**
*This is the rule interface
*/
interface Rule
{
	public String replace(String value);
	public boolean isreplaced();
}
