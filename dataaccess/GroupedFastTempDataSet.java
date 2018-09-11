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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.URLConnection;
import java.util.Vector;
/**
* This class implements a grouped temporary data set for a fast access to its records
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class GroupedFastTempDataSet implements Serializable
{
	private static final long serialVersionUID = 1L;
	Vector<BufferedOutputStream> bisr;
	Vector<ObjectOutputStream> oos;
	int totalrecord;
	String message;
	String mainfilename, tempfilename;
	boolean writingerror;
	ObjectInputStream dis;
	BufferedInputStream bis;
	RandomAccessFile recordfile;
	int numgv;
	int ng;
	String cfile;
	int rescheck=0;
	String line;
	int filetouse;
	String workdir;
	int currentgroup;
	char testchar;
	BufferedOutputStream mainfilebos;
	ObjectOutputStream mainfileoos;
	ObjectInputStream mainfileois;
	BufferedInputStream mainfilebis;
	String[] scurrentgroup;
	int currentretugroup;
	String filetorename;
	/**
	 * Constructor
	 * @param workdir: Working directory were store the file
	 * @param var: Number of variable, i.e. the number of columns of the array
	 */
	public GroupedFastTempDataSet(String workdir, int numgv)
	{
		currentretugroup=0;
		dis=null;
		bis=null;
		currentgroup=0;
		this.workdir=workdir;
		this.numgv=numgv;
		ng=0;
		message="";
		writingerror=false;
		totalrecord=0;
		mainfilename=workdir+"gftds"+Math.random()+".tmp";
		if ( (new File(mainfilename)).exists()) (new File(mainfilename)).delete();
		try
		{
			recordfile=new RandomAccessFile(new File(mainfilename+"rec"), "rw");
			bisr=new Vector<BufferedOutputStream>();
			oos=new Vector<ObjectOutputStream>();
		}
		catch (Exception e)
		{
			writingerror=true;
			message="%2930%:<br>\n"+e.toString()+" ("+mainfilename+")<br>\n";
		}
	}
	/**
	*Reinitializes the possibility to read the groups
	*/
	public void reinitgroups()
	{
		currentretugroup=0;
	}
	/**
	*Return the current String array of the next group
	*/
	public String[] getnextgroup()
	{
		if (numgv==0) return null;
		try
		{
			java.net.URL fileUrl = (new File(mainfilename)).toURI().toURL();
			URLConnection      urlConn;
			urlConn = fileUrl.openConnection();
			urlConn.setDoInput(true);
			urlConn.setUseCaches(false);
			mainfilebis=new BufferedInputStream(urlConn.getInputStream());
			mainfileois=new ObjectInputStream(mainfilebis);
			recordfile.seek(0);
			for (int i=0; i<ng; i++)
			{
				totalrecord=recordfile.readInt();
				if (i==currentretugroup)
				{
					scurrentgroup=(String[])mainfileois.readObject();
					currentretugroup++;
					return scurrentgroup;
				}
				scurrentgroup=(String[])mainfileois.readObject();
				line=(String)mainfileois.readObject();
			}
			mainfileois.close();
			mainfilebis.close();
			mainfileois=null;
			mainfilebis=null;
		}
		catch (Exception e)
		{
			if (mainfilebis!=null)
			{
				try
				{
					mainfileois.close();
					mainfilebis.close();
					mainfileois=null;
					mainfilebis=null;
				}
				catch (Exception ee) {}
			}
		}
		return null;
	}
	/**
	*Return the number of groups
	*/
	public int getnumg()
	{
		return ng;
	}
	/**
	*Return true in case of error
	*/
	public boolean geterror()
	{
		return writingerror;
	}
	/**
	*Returns the current message
	*/
	public String getmessage()
	{
		return message;
	}
	/**
	*Looks if the group already exists
	*/
	private int checkgroup(String[] groups)
	{
		if (ng==0) return -1;
		try
		{
			if (numgv==0)
			{
				recordfile.seek(0);
				totalrecord=recordfile.readInt();
				return 0;
			}
			java.net.URL fileUrl = (new File(mainfilename)).toURI().toURL();
			URLConnection      urlConn;
			urlConn = fileUrl.openConnection();
			urlConn.setDoInput(true);
			urlConn.setUseCaches(false);
			mainfilebis=new BufferedInputStream(urlConn.getInputStream());
			mainfileois=new ObjectInputStream(mainfilebis);
			recordfile.seek(0);
			for (int i=0; i<ng; i++)
			{
				scurrentgroup=(String[])mainfileois.readObject();
				rescheck=0;
				if (scurrentgroup!=null)
				{
					for (int j=0; j<numgv; j++)
					{
						if (scurrentgroup[j].equals(groups[j])) rescheck++;
					}
				}
				if (rescheck==groups.length)
				{
					totalrecord=recordfile.readInt();
					mainfileois.close();
					mainfilebis.close();
					mainfileois=null;
					mainfilebis=null;
					return i;
				}
				line=(String)mainfileois.readObject();
				totalrecord=recordfile.readInt();
			}
			mainfileois.close();
			mainfilebis.close();
			mainfileois=null;
			mainfilebis=null;
		}
		catch (Exception e)
		{
			if (mainfilebis!=null)
			{
				try
				{
					mainfileois.close();
					mainfilebis.close();
					mainfileois=null;
					mainfilebis=null;
				}
				catch (Exception ee) {}
			}
		}
		return -1;
	}
	/**
	*Looks if the group already exists
	*/
	private int createNewFastFile(String[] groups)
	{
		totalrecord=0;
		tempfilename=workdir+"ftf"+Math.random()+".tmp";
		while (new File(tempfilename).exists())
		{
			tempfilename=workdir+"ftds"+Math.random()+".tmp";
		}
		try
		{
			bisr.add(new BufferedOutputStream(new FileOutputStream(tempfilename)));
			oos.add(new ObjectOutputStream(bisr.get(bisr.size()-1)));
			filetorename=mainfilename+"tmp";
			mainfilebos=new BufferedOutputStream(new FileOutputStream(filetorename, true));
			mainfileoos=new ObjectOutputStream(mainfilebos);
			if (ng>0)
			{
				java.net.URL fileUrl = (new File(mainfilename)).toURI().toURL();
				URLConnection      urlConn;
				urlConn = fileUrl.openConnection();
				urlConn.setDoInput(true);
				urlConn.setUseCaches(false);
				mainfilebis=new BufferedInputStream(urlConn.getInputStream());
				mainfileois=new ObjectInputStream(mainfilebis);
				for (int i=0; i<ng; i++)
				{
					if (numgv>0) scurrentgroup=(String[])mainfileois.readObject();
					line=(String)mainfileois.readObject();
					if (numgv>0) mainfileoos.writeObject(scurrentgroup);
					mainfileoos.writeObject(line);
				}
				mainfileois.close();
				mainfilebis.close();
				mainfileois=null;
				mainfilebis=null;
			}
			if (numgv>0) mainfileoos.writeObject(groups);
			mainfileoos.writeObject(tempfilename);
			mainfileoos.flush();
			mainfilebos.flush();
			mainfileoos.close();
			mainfilebos.close();
			mainfileoos=null;
			mainfilebos=null;
			(new File(mainfilename)).delete();
			(new File(filetorename)).renameTo(new File(mainfilename));
			ng++;
			return ng-1;
		}
		catch (Exception enf)
		{
			message=enf.toString();
			if (mainfilebos!=null)
			{
				try
				{
					mainfileoos.flush();
					mainfilebos.flush();
					mainfileoos.close();
					mainfilebos.close();
					mainfileoos=null;
					mainfilebos=null;
				}
				catch (Exception e) {}
			}
			if (mainfilebis!=null)
			{
				try
				{
					mainfileois.close();
					mainfilebis.close();
					mainfileois=null;
					mainfilebis=null;
				}
				catch (Exception e) {}
			}
			writingerror=true;
		}
		return -2;
	}
	/**
	*Looks if the group already exists
	*/
	private void writeTotRecords(int reffile)
	{
		try
		{
			recordfile.seek(0);
			for (int i=0; i<ng; i++)
			{
				if (i<reffile) recordfile.readInt();
				else if (i==reffile) recordfile.writeInt(totalrecord);
				else break;
			}
		}
		catch (Exception eio) {}
	}
	/**
	*Writes the values
	*/
	public boolean write(String[] groups, String[] values)
	{
		if (writingerror)
			return false;
		filetouse=checkgroup(groups);
		if (filetouse==-1) filetouse=createNewFastFile(groups);
		if (filetouse<0)
		{
			message="%2931%:<br>\n"+message+"<br>\n";
			return false;
		}
		BufferedOutputStream bisrt=bisr.get(filetouse);
		ObjectOutputStream oost=oos.get(filetouse);
		try
		{
			oost.writeObject(values);
			oost.flush();
			bisrt.flush();
			oost.reset();
			totalrecord++;
			writeTotRecords(filetouse);
			return true;
		}
		catch (Exception e)
		{
			try
			{
				oost.flush();
				bisrt.flush();
				oost.close();
				bisrt.close();
			}
			catch (Exception ed) {}
			message="%2932%:<br>\n"+e.toString()+"<br>\n";
			writingerror=true;
			return false;
		}
	}
	/**
	*Closes the stream that was opened to write the records
	*/
	public boolean endwrite()
	{
		for (int i=0; i<bisr.size(); i++)
		{
			BufferedOutputStream bisrt=bisr.get(i);
			ObjectOutputStream oost=oos.get(i);
			try
			{
				oost.flush();
				oost.close();
				bisrt.close();
			}
			catch (Exception e)
			{
				message="%2933%:<br>\n"+e.toString()+"<br>\n";
				return false;
			}
		}
		return true;
	}
	/**
	*Opens the stream in order to read the records
	*/
	public boolean opentoread(String[] groups)
	{
		if (dis!=null)
		{
			try
			{
				dis.close();
				bis.close();
				dis=null;
				bis=null;
			}
			catch (Exception e) {}
		}
		cfile="";
		totalrecord=-1;
		try
		{
			java.net.URL fileUrl = (new File(mainfilename)).toURI().toURL();
			URLConnection      urlConn;
			urlConn = fileUrl.openConnection();
			urlConn.setDoInput(true);
			urlConn.setUseCaches(false);
			mainfilebis=new BufferedInputStream(urlConn.getInputStream());
			mainfileois=new ObjectInputStream(mainfilebis);
			recordfile.seek(0);
			if (numgv>0)
			{
				for (int i=0; i<ng; i++)
				{
					scurrentgroup=(String[])mainfileois.readObject();
					rescheck=0;
					if (scurrentgroup!=null)
					{
						for (int j=0; j<numgv; j++)
						{
							if (scurrentgroup[j].equals(groups[j])) rescheck++;
						}
					}
					if (rescheck==groups.length)
					{
						totalrecord=recordfile.readInt();
						cfile=(String)mainfileois.readObject();
						break;
					}
					else line=(String)mainfileois.readObject();
					recordfile.readInt();
				}
			}
			else
			{
				cfile=(String)mainfileois.readObject();
				totalrecord=recordfile.readInt();
			}
			mainfileois.close();
			mainfilebis.close();
			mainfileois=null;
			mainfilebis=null;
		}
		catch (Exception e)
		{
			if (mainfilebis!=null)
			{
				try
				{
					mainfileois.close();
					mainfilebis.close();
					mainfileois=null;
					mainfilebis=null;
				}
				catch (Exception ee) {}
			}
		}
		if (!cfile.equals(""))
		{
			try
			{
				File filetemp=new File(cfile);
				java.net.URL fileUrl = filetemp.toURI().toURL();
				URLConnection      urlConn;
				urlConn = fileUrl.openConnection();
				urlConn.setDoInput(true);
				urlConn.setUseCaches(false);
				bis=new BufferedInputStream(urlConn.getInputStream());
				dis=new ObjectInputStream(bis);
			}
			catch (Exception e)
			{
				message="%2934%:<br>\n"+e.toString()+"<br>\n";
				return false;
			}
			return true;
		}
		message="%2934%<br>\n";
		return false;
	}
	/**
	*Returns the number of records written
	*/
	public int getrecords()
	{
		return totalrecord;
	}
	/**
	*Reads the values from the data set
	*/
	public String[] read()
	{
		try
		{
			String[] tempvalues=(String[])dis.readObject();
			return tempvalues;
		}
		catch (Exception e)
		{
			return null;
		}
	}
	/**
	*Reads the values from the data set and return them as double
	*/
	public double[] readdouble()
	{
		try
		{
			String[] tempvalues=(String[])dis.readObject();
			double[] tempdval=new double[tempvalues.length];
			for (int i=0; i<tempvalues.length; i++)
			{
				tempdval[i]=Double.NaN;
				try
				{
					tempdval[i]=Double.parseDouble(tempvalues[i]);
				}
				catch (Exception co) {}
			}
			return tempdval;
		}
		catch (Exception e)
		{
			return null;
		}
	}
	/**
	*Return true if the stream to read os opened
	*/
	public boolean isreading()
	{
		if (dis!=null) return true;
		else return false;
	}
	/**
	*Closes the stream that was opened in order to read the records
	*/
	public boolean endread()
	{
		try
		{
			dis.close();
			bis.close();
			dis=null;
			bis=null;
		}
		catch (Exception e)
		{
			message="%2935%<br>\n"+e.toString()+"<br>\n";
			return false;
		}
		return true;
	}
	/**
	*Closes the file opened for reading and deletes it and returns false in case of error
	*/
	public boolean deletefile()
	{
		try
		{
			java.net.URL fileUrl = (new File(mainfilename)).toURI().toURL();
			URLConnection      urlConn;
			urlConn = fileUrl.openConnection();
			urlConn.setDoInput(true);
			urlConn.setUseCaches(false);
			mainfilebis=new BufferedInputStream(urlConn.getInputStream());
			mainfileois=new ObjectInputStream(mainfilebis);
			if (numgv>0)
			{
				for (int i=0; i<ng; i++)
				{
					scurrentgroup=(String[])mainfileois.readObject();
					cfile=(String)mainfileois.readObject();
					(new File(cfile)).delete();
				}
			}
			mainfileois.close();
			mainfilebis.close();
			mainfileois=null;
			mainfilebis=null;
			recordfile.close();
			recordfile=null;
			(new File(mainfilename)).delete();
			(new File(mainfilename+"rec")).delete();
		}
		catch (Exception e)
		{
			message="%2936%<br>\n"+e.toString()+"<br>\n";
			return false;
		}
		return true;
	}
}
