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

/**
* This class represents a single object that will be considered for a queue characterized by a name and a vector of strings
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/
public class ObjectForQueue
{
	String name;
	Vector<String[]> content;
	public ObjectForQueue(String name, Vector<String[]> content)
	{
		this.name=name;
		this.content=content;
	}
	public String getname()
	{
		return name;
	}
	public Vector<String[]> getcontent()
	{
		return content;
	}
}
