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

import java.util.LinkedList;

/**
* This is the class that implements the writer queue
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
public class WriterQueue
{
	LinkedList<Object> wqueue = new LinkedList<Object>();
	boolean iswriting=false;
	/**
	*Return the number of waiting objects
	*/
	public synchronized int getwaitingsize()
	{
		return wqueue.size();
	}
	/**
	*Adds a new record
	*/
	public synchronized void addNewRecord(Object or)
	{
		wqueue.addLast(or);
		notify();
	}
	/**
	*Returns the record
	*/
	public synchronized Object getNewRecord() throws InterruptedException
	{
		return wqueue.pollFirst();
	}
	/**
	*Sets that the writing action occours
	*/
	public void setWrite()
	{
		iswriting=true;
	}
	/**
	*Clear the memory
	*/
	public void clearmem()
	{
		wqueue.clear();
		wqueue=null;
	}
	/**
	*Sets an ended job
	*/
	public synchronized void setendwriting()
	{
		iswriting=false;
		notify();
	}
	/**
	*Return the info that the writing job is finished
	*/
	public synchronized boolean testwriting()
	{
		return iswriting;
	}
}
