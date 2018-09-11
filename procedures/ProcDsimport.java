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
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ObjectInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.XMLDictionaryReader;
import ADaMSoft.utilities.GenericContainerForHashtable;

/**
* This procedure extract the data set(s) contained in a compressed file
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcDsimport implements RunStep
{
	private String filename;
	/**
	*Execute the procedure
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean useoriginalnames=false;
		String[] requiredparameters = new String[] {Keywords.OUT.toLowerCase(), Keywords.filename};
		String[] optionalparameters = new String[] {Keywords.useoriginalnames};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		useoriginalnames=(parameters.get(Keywords.useoriginalnames)!=null);
		filename = (String) parameters.get(Keywords.filename);
		String mainname=(String)parameters.get(Keywords.OUT.toLowerCase());
		String tempdir=(String)parameters.get(Keywords.WorkDir);
		GenericContainerForHashtable filenames=null;
		try
		{
			ZipFile zf = new ZipFile(new File(filename));
			Enumeration<? extends ZipEntry> zipEntries = zf.entries();
			LinkedList<String> entries  = new LinkedList<String>();
			while(zipEntries.hasMoreElements())
			{
				String entryname=zipEntries.nextElement().getName();
				if (entryname.equalsIgnoreCase("DataSetNames") && (useoriginalnames))
				{
					ObjectInputStream ois = new ObjectInputStream(zf.getInputStream(zf.getEntry(entryname)));
					filenames=(GenericContainerForHashtable)ois.readObject();
					ois.close();
				}
				else
					entries.add(entryname);
			}
			Collections.sort(entries);
			while(!entries.isEmpty() && !entries.getFirst().startsWith("dict"))
			{
				entries.removeFirst();
			}
			if(entries.isEmpty())
			{
				zf.close();
				return new Result("%543%<br>\n",false, null);
			}
			String zName;
			byte[] buffer = new byte[1024];
			Vector<StepResult> result = new Vector<StepResult>();
			while((zName=entries.getFirst()).toLowerCase().startsWith("dict"))
			{
				entries.removeFirst();
				BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(tempdir+"tempdict"));
				BufferedInputStream bis =new BufferedInputStream(zf.getInputStream(zf.getEntry(zName)));
				int read;
				while((read =bis.read(buffer))!=-1)
				{
					fileOut.write(buffer,0,read);
				}
				fileOut.close();
				XMLDictionaryReader dict = new XMLDictionaryReader(tempdir+"tempdict");
				if (!(dict.getmessageDictionaryReader()).equals(""))
					return new Result(dict.getmessageDictionaryReader(), false, null);

				String suffix = zName.replaceAll("dict","");
				String parsubstitution=mainname+suffix;
				if (filenames!=null)
				{
					parameters.put(Keywords.OUT.toLowerCase(), filenames.get(zName));
				}
				else
					parameters.put(Keywords.OUT.toLowerCase(), parsubstitution);
				DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
				if (!dw.getmessage().equals(""))
					return new Result(dw.getmessage(), false, null);

				ZipEntry ze = zf.getEntry("table"+suffix);
				if(ze == null)
				{
					new File(tempdir+"tempdict").delete();
					return new Result("%544% ("+zName+")<br>\n",false, null);
				}
				Vector<Hashtable<String, String>> fixedvariableinfo=dict.getfixedvariableinfo();

				BufferedReader br = new BufferedReader(new InputStreamReader(zf.getInputStream(ze)));

				if (!dw.opendatatable(fixedvariableinfo))
					return new Result(dw.getmessage(), false, null);
				String readl="";
				while((readl=br.readLine())!=null)
				{
					String[] values = readl.split("\t",-1);
					dw.write(values);
				}
				boolean resclose=dw.close();
				if (!resclose)
					return new Result(dw.getmessage(), false, null);
				Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
				Hashtable<String, String> datatableinfo=dw.getTableInfo();
				result.add(new LocalDictionaryWriter(dw.getdictpath(), dict.getkeyword(), dict.getdescription(), dict.getauthor(), dw.gettabletype(),
				datatableinfo, fixedvariableinfo, tablevariableinfo, dict.getcodelabel(), dict.getmissingdata(), null));
				new File(tempdir+"tempdict").delete();
			}
			zf.close();
			return new Result("", true, result);
		}
		catch (Exception e)
		{
			return new Result("%548%<br>\n"+e.toString()+"<br>\n", false, null);
		}
	}
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters = new LinkedList<GetRequiredParameters>();
		String[] dep = { "" };
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 546, dep, "", 1));
		parameters.add(new GetRequiredParameters("", "note", false, 2279, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.filename, "file=.zip", true, 547, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.useoriginalnames, "checkbox", false, 2277, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 2278, dep, "", 2));
		return parameters;
	}

	public String[] getstepinfo()
	{
		String[] info = new String[2];
		info[0] = "504";
		info[1] = "545";
		return info;
	}

}
