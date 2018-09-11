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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.StepUtilities;

/**
* This is the procedure that copies a file in an url locally
* @author marco.scarno@gmail.com
* @version 1.0.0, rev.: 19/02/17 by marco
*/
public class ProcCopyurl implements RunStep
{
	/**
	* Starts the execution of Proc Copyurl
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.url};
		String [] optionalparameters=new String[] {Keywords.localdir, Keywords.saveas};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}

		String urls=(String)parameters.get(Keywords.url);
		String localdir=(String)parameters.get(Keywords.localdir);
		String saveas=(String)parameters.get(Keywords.saveas);

		if (localdir==null)
			localdir=(String)parameters.get(Keywords.WorkDir);

		try
		{
			localdir=localdir.replaceAll("\\\\","/");
			if (!localdir.endsWith("/"))
				localdir=localdir+"/";
		}
		catch (Exception fs){}

		Keywords.percentage_done=0;
		Keywords.percentage_total=1;

		try
		{
			URL           url  = new URL(urls);
			String fname="";
			if (saveas==null)
				fname=(new File(url.getFile())).getName();
			else
				fname=saveas;
			if (fname.equals(""))
			{
				return new Result("%1273%<br>\n", false, null);
			}
			localdir=localdir+fname;
			DataInputStream in = new DataInputStream(new BufferedInputStream(url.openStream()));
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer=new byte[1024];
			int accessed=0;
			while((accessed=in.read(buffer))!=-1)
			{
				baos.write(buffer,0 , accessed);
			}
			baos.close();
			in.close();
			Keywords.percentage_done=0;
			Keywords.percentage_total=0;
			byte[] resf=baos.toByteArray();
			FileOutputStream fas=new FileOutputStream(localdir);
			fas.write(resf);
			fas.close();
			return new Result("%1275% ("+localdir+")<br>\n", true, null);

		}
		catch (Exception e)
		{
			return new Result("%1274% ("+urls+")\n"+e.toString()+"<br>\n", false, null);
		}
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.url, "text", true, 1271, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.localdir, "text", false, 1272, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.saveas, "text", false, 3270, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="4173";
		retprocinfo[1]="1270";
		return retprocinfo;
	}
}
