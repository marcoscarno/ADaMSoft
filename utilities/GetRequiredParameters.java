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

/**
* This class iterates over the parameters of a procedure and gives back the information on them
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/
public class GetRequiredParameters
{
	/**
	*This is the name of the parameter
	*/
	private String name;
	/**
	*This is the type of the parameter used to build the graphical interface
	*/
	private String type;
	/**
	*If true means that the parameter is mandatory
	*/
	private boolean mandatory;
	/**
	*This is the label that specifies the parameter meaning in the graphical user interface
	*/
	private int label;
	/**
	*This String array contains the names of the dictionaries that will be used to build the variables selection
	*/
	private String[] dependences;
	/**
	*This contains the default value for a parameter
	*/
	private Object defaultValue;
	/**
	*If 0 means that the parameter will appear on the Step specifications, else is a simple parameter
	*/
	private int level;
	/**
	*An internal counter
	*/
	private int counter;
	/**
	*Set a new parameter
	*/
	public GetRequiredParameters (String name, String type, boolean mandatory, int label, String[] dependences, Object defaultValue, int level)
	{
		counter=0;
		this.name = name;
		this.type = type;
		this.mandatory = mandatory;
		this.label = label;
		this.dependences = dependences;
		this.defaultValue = defaultValue;
		this.level = level;
	}
	/**
	*Gets the default value
	*/
	public Object getDefaultValue()
	{
		return defaultValue;
	}
	/**
	*Gets the dependencies
	*/
	public String[] getDependences()
	{
		return dependences;
	}
	/**
	*Checks if other dependencies are defined
	*/
	public boolean hasNextDependance()
	{
		return counter<dependences.length?true:false;
	}
	/**
	*Gets the next dependency
	*/
	public String nextDependence()
	{
		if(counter<dependences.length)
		{
			return dependences[counter++];
		}
		else return null;
	}
	/**
	*Gets the label of the parameter
	*/
	public int getLabel()
	{
		return label;
	}
	/**
	*Gets the level of the parameter
	*/
	public int getLevel()
	{
		return level;
	}
	/**
	*Returns true if the parameter is mandatory
	*/
	public boolean isMandatory()
	{
		return mandatory;
	}
	/**
	*Returns the name of the parameter
	*/
	public String getName()
	{
		return name;
	}
	/**
	*Returns the type of the parameter
	*/
	public String getType()
	{
		return type;
	}
}
