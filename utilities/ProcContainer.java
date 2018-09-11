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

import java.util.Vector;
import java.io.Serializable;
/**
* This class contains the names of the procedures
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/
public class ProcContainer implements Serializable
{
	private String name;
	private Vector<String> names;
	private Vector<String> labelnames;
	private static final long serialVersionUID = 1L;
	/**
	*Constructor that receives the name of the group and the vectors of names and labels
	*/
	public ProcContainer (String name, Vector<String> names, Vector<String> labelnames)
	{
		this.name=name;
		this.names=names;
		this.labelnames=labelnames;
	}
	/**
	*Sets the group name
	*/
	public void setGroupName(String name)
	{
		this.name=name;
	}
	/**
	*Gets the group name
	*/
	public String getGroupName()
	{
		return name;
	}
	/**
	*Sets the procedures names
	*/
	public void setProcNames(Vector<String> names)
	{
		this.names=names;
	}
	/**
	*Gets the procedures names
	*/
	public Vector<String> getProcNames()
	{
		return names;
	}
	/**
	*Sets the procedures names
	*/
	public void setLabelNames(Vector<String> labelnames)
	{
		this.labelnames=labelnames;
	}
	/**
	*Gets the procedures names
	*/
	public Vector<String> getLabelNames()
	{
		return labelnames;
	}
}
