/**
* Copyright (c) 2016 MS
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

import java.util.LinkedList;

/**
* This class represent the general queue for workers
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/
public class WorkQueue
{
    LinkedList<ObjectForQueue> queue = new LinkedList<ObjectForQueue>();
    boolean forcedstop=false;
    public synchronized void addWork(ObjectForQueue current)
    {
        queue.addLast(current);
        notify();
    }
    public synchronized ObjectForQueue getWork() throws InterruptedException
    {
        if (queue.isEmpty() || forcedstop) return null;
        return queue.removeFirst();
    }
    public synchronized void force_stop()
    {
		forcedstop=true;
    }
}