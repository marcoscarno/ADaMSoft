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

package ADaMSoft.procedures;

import java.io.*;
import java.util.LinkedList;
import java.util.Hashtable;
import java.util.Vector;
import java.util.zip.ZipEntry;

import java.util.zip.ZipOutputStream;
import java.util.zip.ZipInputStream;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.utilities.GetRequiredParameters;

/**
* This is the procedure that compress into a zip file a single file or the content of a directory
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcZip implements RunStep
{
	/**
	* Starts the execution of Proc Zip
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		byte[] buf = new byte[1024];
		Vector<StepResult> result = new Vector<StepResult>();
		String [] requiredparameters=new String[] {Keywords.zipfile};
		String [] optionalparameters=new String[] {Keywords.dirref, Keywords.fileref, Keywords.replacezipfile, Keywords.addtozip};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}

		boolean replace =(parameters.get(Keywords.replacezipfile)!=null);
		boolean addtozip =(parameters.get(Keywords.addtozip)!=null);

		String zipfile=(String)parameters.get(Keywords.zipfile);
		if (!zipfile.endsWith(".zip"))
			zipfile=zipfile+".zip";

		String dirref=(String)parameters.get(Keywords.dirref);
		String fileref=(String)parameters.get(Keywords.fileref);

		if ((dirref==null) && (fileref==null))
			return new Result("%2293%<br>\n", false, null);

		if ((dirref!=null) && (fileref!=null))
			return new Result("%2293%<br>\n", false, null);

		if(dirref!=null)
		{
			if (!dirref.endsWith(System.getProperty("file.separator")))
				dirref=dirref+System.getProperty("file.separator");
		}


		String workdir=(String)parameters.get(Keywords.WorkDir);
		String tempzipfile=workdir+"Temp.zip";
		{
			boolean exists = (new File(tempzipfile)).exists();
			if (exists)
			{
				 boolean success = (new File(tempzipfile)).delete();
				 if (!success)
				 	return new Result("%2288%<br>\n", false, null);
			}
		}
		boolean exists=false;
		exists = (new File(zipfile)).exists();
		if (exists)
		{
			boolean iserr=true;
			if (replace)
			{
				iserr=false;
				addtozip=false;
				boolean success = (new File(zipfile)).delete();
				if (!success)
					return new Result("%2289%<br>\n", false, null);
			}
			if (addtozip)
				iserr=false;
			if (iserr)
				return new Result("%2287%<br>\n", false, null);
		}
		else
			addtozip=false;
		ZipOutputStream out = null;
		try
		{
			if (fileref!=null)
			{
				exists = (new File(fileref)).exists();
				if (!exists)
					return new Result("%2290%<br>\n", false, null);
				out = new ZipOutputStream(new FileOutputStream(tempzipfile));
				File tempfile = new File(fileref);
				String filename = tempfile.getName();
				InputStream in = new FileInputStream(fileref);
				out.putNextEntry(new ZipEntry(filename));
				int len;
				while ((len = in.read(buf)) > 0)
				{
					out.write(buf, 0, len);
				}
				in.close();
				if (addtozip)
				{
					ZipInputStream zin = new ZipInputStream(new FileInputStream(zipfile));
					ZipEntry entry = zin.getNextEntry();
					while (entry != null)
					{
						String name = entry.getName();
						if (!name.equalsIgnoreCase(filename))
						{
							out.putNextEntry(new ZipEntry(name));
							while ((len = zin.read(buf)) > 0)
							{
								out.write(buf, 0, len);
							}
						}
						entry = zin.getNextEntry();
					}
					zin.close();
				}
				out.close();
			}
			else
			{
				File dir = new File(dirref);
				String[] children = dir.list();
				if (children==null)
					return new Result("%2286%<br>\n", false, null);
				Vector<String> realfile=new Vector<String>();
				Vector<String> realnames=new Vector<String>();
				for (int i=0; i<children.length; i++)
				{
					File tempfile = new File(dirref+children[i]);
					if (tempfile.isFile())
					{
						realfile.add(dirref+children[i]);
						realnames.add(children[i]);
					}
				}
				if (realfile.size()==0)
					return new Result("%2286%<br>\n", false, null);
				out = new ZipOutputStream(new FileOutputStream(tempzipfile));
				for (int i=0; i<realfile.size(); i++)
				{
					InputStream in = new FileInputStream(realfile.get(i));
					out.putNextEntry(new ZipEntry(realnames.get(i)));
					int len;
					while ((len = in.read(buf)) > 0)
					{
						out.write(buf, 0, len);
					}
					in.close();
				}
				if (addtozip)
				{
					ZipInputStream zin = new ZipInputStream(new FileInputStream(zipfile));
					ZipEntry entry = zin.getNextEntry();
					while (entry != null)
					{
						String name = entry.getName();
						boolean toadd=true;
						for (int i=0; i<realnames.size(); i++)
						{
							if (name.equalsIgnoreCase(realnames.get(i)))
								toadd=false;
						}
						if (toadd)
						{
							int len;
							out.putNextEntry(new ZipEntry(name));
							while ((len = zin.read(buf)) > 0)
							{
								out.write(buf, 0, len);
							}
						}
						entry = zin.getNextEntry();
					}
					zin.close();
				}
				out.close();
			}
		}
		catch (Exception e)
		{
			return new Result("%2291%<br>\n", false, null);
		}
		try
		{
			InputStream in = new FileInputStream(tempzipfile);
			OutputStream outt = new FileOutputStream(zipfile);
			int len;
			while ((len = in.read(buf)) > 0)
			{
				outt.write(buf, 0, len);
			}
			in.close();
			outt.close();
		}
		catch (Exception ee)
		{
			return new Result("%2294%<br>\n", false, null);
		}
		(new File(tempzipfile)).delete();
		return new Result("%2295%<br>\n", true, null);
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.zipfile, "filesave=.zip", true, 2296, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.fileref, "file=all", false, 2297, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.dirref, "dir", false, 2298, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replacezipfile, "checkbox", false, 2299, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.addtozip, "checkbox", false, 2300, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="4173";
		retprocinfo[1]="2292";
		return retprocinfo;
	}
}
