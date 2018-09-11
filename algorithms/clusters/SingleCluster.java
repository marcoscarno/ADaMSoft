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

package ADaMSoft.algorithms.clusters;

import java.util.HashSet;
import java.util.Vector;

/**
* This method contains a single cluster
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class SingleCluster
{
	HashSet<String> name;
	Vector<Double> distances;
	HashSet<String> clustername;
	/**
	*Initialize the cluster element
	*/
	public SingleCluster (HashSet<String> clustername, HashSet<String> name, Vector<Double> distances)
	{
		this.clustername=clustername;
		this.name=name;
		this.distances=distances;
	}
	/**
	*Return the names of the cluster
	*/
	public HashSet<String> getclustername()
	{
		return clustername;
	}
	/**
	*Return the names of the units inside the cluster
	*/
	public HashSet<String> getnames()
	{
		return name;
	}
	/**
	*Return the distance between the two units
	*/
	public Vector<Double> getdistances()
	{
		return distances;
	}
	/**
	*Return the number of the first element inside the cluster
	*/
	public int getnumunit()
	{
		return name.size();
	}
}
