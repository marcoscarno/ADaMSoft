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


/**
* This implements the thread that will write the new record
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
class DsWriter extends Thread
{
	WriterQueue wq;
	DataTableWriter datatable;
	String msgdw;
	String tabletype;
	int wsize=0;
	static final Object NO_MORE_RECORD = new Object();
	static final Object TABLE_DELETE = new Object();
	/**
	*Initialize the writer object
	*/
	DsWriter(WriterQueue wq, DataTableWriter datatable, String tabletype)
	{
		this.wq = wq;
		this.datatable=datatable;
		this.tabletype=tabletype;
		msgdw="";
		wq.setWrite();
	}
	/**
	*Runs the current writing job
	*/
	public void run()
	{
		try
		{
			while (true)
			{
				Object tempr=wq.getNewRecord();
				if (tempr==NO_MORE_RECORD)
				{
					boolean check=datatable.close();
					if (check==false)
						msgdw=msgdw+"<br>\n"+datatable.getmessage();
					wq.setendwriting();
					break;
				}
				else if (tempr==TABLE_DELETE)
				{
					datatable.deletetmp();
					wq.setendwriting();
					break;
				}
				else if (tempr!=null)
				{
					String[] record=(String[])tempr;
					try
					{
						boolean check=datatable.writevalues(record);
						if (check==false)
						{
							msgdw=msgdw+"<br>\n"+datatable.getmessage();
							wq.setendwriting();
							break;
						}
					}
					catch (Exception e)
					{
						msgdw=msgdw+"<br>\n"+"%385% ("+tabletype+")<br>\n";
						wq.setendwriting();
						break;
					}
				}
			}
		}
		catch (InterruptedException e)
		{
			wq.setendwriting();
		}
	}
	/**
	*If the string is not empty then an error occours
	*/
	public String getmsgdw()
	{
		return msgdw;
	}
}
