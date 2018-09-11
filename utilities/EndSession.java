/**
* Copyright (c) 2018 MS
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Iterator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import ADaMSoft.keywords.Keywords;

/**
* This class deletes all the work directories
* @author marco.scarno@gmail.com
* @date 13/06/2018
*/
public class EndSession
{
	String message;
	boolean successf;
	public EndSession()
	{
		message="";
		successf=true;
		try
		{
			Iterator<String> iter = Keywords.SQL_DB.iterator();
			while (iter.hasNext())
			{
				Class.forName("org.hsqldb.jdbcDriver").newInstance();
				Connection conn = DriverManager.getConnection("jdbc:hsqldb:file:"+iter.next());
				Statement stmt= conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
				stmt.execute("SHUTDOWN");
				stmt.close();
				conn.close();
			}
		}
		catch (Exception e){}
		boolean localresult=true;
		boolean remoteresult=true;
		String WORK_DIR=System.getProperty(Keywords.WorkDir);
		String encodingLocale=(System.getProperty( "file.encoding" )).toString();
		System.setProperty("file.encoding", encodingLocale);
		try
		{
			File dir = new File(WORK_DIR);
			visitAllDirsAndFiles(dir);
			visitAllDirs(dir);
			if (!successf)
				localresult=false;
			boolean successd = (new File(WORK_DIR)).exists();
			if (successd)
			{
				successd = (new File(WORK_DIR)).delete();
				if (!successd)
					localresult=false;
			}
		}
		catch(Exception e)
		{
			localresult=false;
		}
		if (!localresult)
			message=message+Keywords.Language.getMessage(70)+"\n";
		successf=false;
		if (localresult && remoteresult)
			successf=true;
		String gui_error=System.getProperty("gui_error");
		if (!gui_error.equals(""))
		{
			try
			{
				String sCurrentLine;
				BufferedReader brge = new BufferedReader(new FileReader(gui_error));
				String egui="";
				while ((sCurrentLine = brge.readLine()) != null)
				{
					if (!sCurrentLine.equals(""))
						egui=egui+sCurrentLine;
				}
				brge.close();
				if (egui.equals("ERRORS IN GUI"))
					(new File(gui_error)).delete();

			} catch (Exception e) {}
		}
	}
	public boolean getresult()
	{
		return successf;
	}
	private void visitAllDirsAndFiles(File dir)
	{
		if (dir.isDirectory())
		{
			String[] children = dir.list();
			for (int i=0; i<children.length; i++)
			{
				visitAllDirsAndFiles(new File(dir, children[i]));
			}
		}
		else
		{
			boolean locsuccessf = dir.delete();
			if (!locsuccessf)
				successf=false;
		}
	}
	private void visitAllDirs(File dir)
	{
		if (dir.isDirectory())
		{
			String[] children = dir.list();
			if (children.length==0)
			{
				boolean locsuccessf = dir.delete();
				if (!locsuccessf)
					successf=false;
			}
			else
			{
	            for (int i=0; i<children.length; i++)
	            {
					visitAllDirs(new File(dir, children[i]));
				}
			}
		}
    }
	public String getMessage()
	{
		return message;
	}
}
