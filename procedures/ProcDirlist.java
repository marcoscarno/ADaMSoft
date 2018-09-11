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

import java.io.File;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.GetRequiredParameters;

/**
* This is the procedure that lists all the files and all the directories of a given path and write such data into a data set
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class ProcDirlist implements RunStep
{
	Vector<String[]> dircontent;
	/**
	* Starts the execution of Proc Listdir
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean nosubdir=false;
		dircontent=new Vector<String[]>();
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.directory};
		String [] optionalparameters=new String[] {Keywords.filterfile, Keywords.nosubdir};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}

		nosubdir=(parameters.get(Keywords.nosubdir)!=null);

		String filterfile=(String)parameters.get(Keywords.filterfile);

		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);

		String directory=(String)parameters.get(Keywords.directory);
		File tempmaindir = new File(directory);
		try
		{
			directory=tempmaindir.getCanonicalPath();
		}
		catch (Exception e){}

		String keyword="Listdir "+directory;
		String description="Listdir "+directory;
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());

		boolean exists = (new File(directory)).exists();
		if (!exists)
			return new Result("%1252% ("+directory+")<br>\n", false, null);
		try
		{
			File maindir = new File(directory);
			if (!nosubdir)
			{
				listdir(maindir);
			}
			else
			{
				String[] children = maindir.list();
				if (children != null)
				{
					for (int i=0; i<children.length; i++)
					{
						String filename = children[i];
						File test=new File(maindir, filename);
						if (!test.isDirectory())
						{
					        String[] infodir=new String[2];
					        String dirtemp=directory+File.separator+filename;
							try
        					{
								dirtemp=dirtemp.replaceAll("\\\\","/");
							}
							catch (Exception er){}
					        infodir[0]=dirtemp;
							infodir[1]="2";
							dircontent.add(infodir);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			return new Result("%1251%\n", false, null);
		}
		DataSetUtilities dsu=new DataSetUtilities();

		Hashtable<String, String> tempmd=new Hashtable<String, String>();

		Hashtable<String, String> tempcl=new Hashtable<String, String>();
		tempcl.put("1","%2332%");
		tempcl.put("2","%2333%");

		dsu.addnewvar("name", "%1253%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("type", "%2331%", Keywords.TEXTSuffix, tempcl, tempmd);
		dsu.addnewvar("filename", "%2335%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("dirref", "%2336%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("filenamenoext", "%2337%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("fileext", "%2338%", Keywords.TEXTSuffix, tempmd, tempmd);
		dsu.addnewvar("filesize", "%3695%", Keywords.NUMSuffix, tempmd, tempmd);

		if (!dw.opendatatable(dsu.getfinalvarinfo()))
			return new Result(dw.getmessage(), false, null);

		String[] valuestowrite=new String[7];

		for (int i=0; i<dircontent.size(); i++)
		{
			String[] tempname=dircontent.get(i);
			boolean writefile=true;
			if (filterfile!=null)
			{
				if (tempname[0].toLowerCase().indexOf(filterfile.toLowerCase())<0)
					writefile=false;
			}
			if (writefile)
			{
				valuestowrite[0]="";
				valuestowrite[1]="";
				valuestowrite[2]="";
				valuestowrite[3]="";
				valuestowrite[4]="";
				valuestowrite[5]="";
				valuestowrite[6]="";
				valuestowrite[0]=tempname[0];
				valuestowrite[1]=tempname[1];
				if (tempname[1].equals("2"))
				{
					try
					{
						File temp=new File(tempname[0]);
						valuestowrite[2]=temp.getName();
						valuestowrite[3]=temp.getParent()+File.separator;
						try
       					{
							valuestowrite[3]=valuestowrite[3].replaceAll("\\\\","/");
						}
						catch (Exception er){}
						try
						{
							valuestowrite[4]=valuestowrite[2].substring(0, valuestowrite[2].lastIndexOf("."));
						}
						catch (Exception er){}
						try
						{
							valuestowrite[5]=valuestowrite[2].substring(valuestowrite[2].lastIndexOf(".")+1);
						}
						catch (Exception er){}
						try
						{
							valuestowrite[6]=String.valueOf(temp.length());
						}
						catch (Exception er){}
					}
					catch (Exception e){}
				}
				dw.write(valuestowrite);
			}
		}
		Vector<StepResult> result = new Vector<StepResult>();
		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
		Hashtable<String, String> datatableinfo=dw.getTableInfo();
		result.add(new LocalDictionaryWriter(dw.getdictpath(), keyword, description, author, dw.gettabletype(),
		datatableinfo, dsu.getfinalvarinfo(), tablevariableinfo, dsu.getfinalcl(), dsu.getfinalmd(), null));
		return new Result("", true, result);
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 1254, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.directory, "text", true, 1255, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.filterfile, "text", false, 1256, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 1257, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.nosubdir, "checkbox", false, 1258, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="4173";
		retprocinfo[1]="1250";
		return retprocinfo;
	}
	private void listdir(File dir)
	{
		if (dir.isDirectory())
		{
			String[] children = dir.list();
			if (children!=null)
			{
				for (int i=0; i<children.length; i++)
				{
					listdir(new File(dir, children[i]));
        		}
        	}
        }
        String[] infodir=new String[2];
        String dirtemp=dir.toString();
        try
        {
			dirtemp=dirtemp.replaceAll("\\\\","/");
		}
		catch (Exception er){}
        infodir[0]=dirtemp;
        if (dir.isDirectory()) infodir[1]="1";
        else infodir[1]="2";
        dircontent.add(infodir);
	}
}
