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
* This class represents a collection of objects that will be considered for a queue characterized by a name and a vector of strings
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/
public class ObjectsForQueue
{
	Vector<String> refnames;
	Vector<Vector<String[]>> objects;
	int pos;
	WorkQueue wq;
	public ObjectsForQueue()
	{
		refnames=new Vector<String>();
		objects=new Vector<Vector<String[]>>();
		wq=new WorkQueue();
	}
	public void addelement(String refn, String[] refobj)
	{
		if (!refnames.contains(refn))
		{
			refnames.add(refn);
			Vector<String[]> tempn=new Vector<String[]>();
			String[] temps=new String[refobj.length];
			for (int i=0; i<refobj.length; i++)
			{
				temps[i]=refobj[i];
			}
			tempn.add(temps);
			objects.add(tempn);
		}
		else
		{
			pos=refnames.indexOf(refn);
			Vector<String[]> tempn=objects.get(pos);
			String[] temps=new String[refobj.length];
			for (int i=0; i<refobj.length; i++)
			{
				temps[i]=refobj[i];
			}
			tempn.add(temps);
			objects.set(pos, tempn);
		}
	}
	public int getelem()
	{
		return refnames.size();
	}
	public void fillqueue()
	{
		for (int i=0; i<refnames.size(); i++)
		{
			ObjectForQueue ofq=new ObjectForQueue(refnames.get(i), objects.get(i));
			wq.addWork(ofq);
		}
	}
	public WorkQueue getqueue()
	{
		return wq;
	}
}
