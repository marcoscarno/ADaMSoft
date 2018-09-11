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

package ADaMSoft.algorithms.frequencies;

import java.util.Vector;

/**
* This class contains the result of the tabulator
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class ResTabulate
{
	private Vector<String> tv;
	private double[] tf;
	/**
	*Insert the vector and the corresponding frequencies
	*/
	public ResTabulate (Vector<String> tv, double[] tf)
	{
		this.tv = tv;
		this.tf = tf;
	}
	/**
	*Gets the Vector element
	*/
	public Vector<String> getVec()
	{
		return tv;
	}
	/**
	*Gets the frequencies element
	*/
	public double[] getFre()
	{
		return tf;
	}
}
