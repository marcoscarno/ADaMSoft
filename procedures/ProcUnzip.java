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
import ADaMSoft.utilities.StepUtilities;

import java.util.zip.ZipInputStream;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;

/**
* This is the procedure that decompress a zip file
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcUnzip implements RunStep
{
	/**
	* Starts the execution of Proc Unzip
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		int BUFFER = 2048;
		Vector<StepResult> result = new Vector<StepResult>();
		String [] requiredparameters=new String[] {Keywords.zipfile};
		String [] optionalparameters=new String[] {Keywords.dirref, Keywords.replacefile, Keywords.filestoselect};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}

		boolean replace =(parameters.get(Keywords.replacefile)!=null);

		String filestoselect=(String)parameters.get(Keywords.filestoselect);
		if (filestoselect==null)
			filestoselect="";

		String zipfile=(String)parameters.get(Keywords.zipfile);
		if (!zipfile.endsWith(".zip"))
			zipfile=zipfile+".zip";

		String dirref=(String)parameters.get(Keywords.dirref);
		String workdir=(String)parameters.get(Keywords.WorkDir);
		boolean istemp=false;
		if (dirref==null)
		{
			dirref=workdir;
			istemp=true;
		}

		try
		{
			dirref=dirref.replaceAll("\\\\","/");
		}
		catch (Exception fs){}
		if (!dirref.endsWith("/"))
			dirref=dirref+"/";

		try
		{
			FileInputStream fis = new FileInputStream(zipfile);
			ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
			ZipEntry entry;
			while((entry = zis.getNextEntry()) != null)
			{
				int count;
				byte data[] = new byte[BUFFER];
				File tempfilename=new File(entry.getName());
				String tempfname=tempfilename.getName();
				boolean dounzip=true;
				if (!filestoselect.equals(""))
				{
					if (tempfname.toUpperCase().indexOf(filestoselect.toUpperCase())<0)
						dounzip=false;
				}
				boolean existsdest = (new File(dirref+tempfname)).exists();
				if (existsdest)
				{
					if ((!replace) && (dounzip))
					{
						dounzip=false;
						result.add(new LocalMessageGetter("%2308% ("+dirref+tempfname+")<br>\n"));
					}
				}
				if (dounzip)
				{
					FileOutputStream fos = new FileOutputStream(dirref+tempfname);
					BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);
					while ((count = zis.read(data, 0, BUFFER))!= -1)
					{
						dest.write(data, 0, count);
					}
					dest.flush();
					dest.close();
					fos.close();
					result.add(new LocalMessageGetter("%2303% ("+dirref+tempfname+")<br>\n"));
				}
			}
			zis.close();
			fis.close();
		}
		catch (Exception e)
		{
			return new Result("%2302%<br>\n", false, null);
		}
		return new Result("%2304% ("+zipfile+")<br>\n", true, result);
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.zipfile, "filesave=.zip", true, 2305, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.dirref, "dir", false, 2306, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.filestoselect, "text", false, 2728, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replacefile, "checkbox", false, 2307, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="4173";
		retprocinfo[1]="2301";
		return retprocinfo;
	}
}
