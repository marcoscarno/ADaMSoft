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
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.GenericContainerForHashtable;

/**
* This procedure will create a zip file with all the datasets that are in a path
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcPathexport implements RunStep
{
	private String filename;
	private String tempdir;
	/**
	*Execute the procedure pathexport
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String[] requiredparameters = new String[] {Keywords.filename};
		String[] optionalparameters = new String[] {Keywords.Path.toLowerCase(), Keywords.dsname};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		filename = (String) parameters.get(Keywords.filename);
		if (!filename.toLowerCase().endsWith(".zip"))
			filename=filename+".zip";
		tempdir=(String)parameters.get(Keywords.WorkDir);
		String path=(String)parameters.get(Keywords.Path.toLowerCase());
		String dsname=(String)parameters.get(Keywords.dsname.toLowerCase());
		String[] dsnames=new String[0];
		if (dsname!=null)
			dsnames=dsname.split(";");
		if (path==null)
			path=tempdir;
		OutputStream out;
		ZipOutputStream zipOut;
		ZipEntry ze;
		GenericContainerForHashtable filenames=new GenericContainerForHashtable();
		try
		{
			out=new FileOutputStream(filename);
			zipOut = new ZipOutputStream(new BufferedOutputStream(out));
			File f = new File(path);
			String[] dicts = f.list();
			if (dicts==null)
				return new Result("%512% ("+path+")<br>\n",false, null);
			for (int d=0; d<dicts.length; d++)
			{
				boolean addds=false;
				if (dicts[d].endsWith(Keywords.DictionaryExtension))
				{
					addds=true;
				}
				if (dsnames.length>0)
				{
					if (addds)
					{
						String checkdsname=dicts[d].replaceAll(Keywords.DictionaryExtension, "");
						boolean foundds=false;
						for (int i=0; i<dsnames.length; i++)
						{
							if (dsnames[i].endsWith("*"))
							{
								String tempdsname=dsnames[i].replaceAll("\\*","");
								if (checkdsname.toUpperCase().startsWith(tempdsname.toUpperCase()))
									foundds=true;
							}
							else
							{
								if (checkdsname.equalsIgnoreCase(dsnames[i]))
									foundds=true;
							}
						}
						if (!foundds)
							addds=false;
					}
				}
				if (addds)
				{
					DictionaryReader dict=new DictionaryReader(path+dicts[d]);
					if (!dict.getmessageDictionaryReader().equals(""))
					{
						zipOut.finish();
						zipOut.close();
						return new Result(dict.getmessageDictionaryReader(), false, null);
					}
					ze = new ZipEntry("dict"+String.valueOf(d));
					File fileforname = new File(dict.getDictPath());
					String dsforname = fileforname.getName();
					try
					{
						dsforname=dsforname.replaceAll(Keywords.DictionaryExtension,"");
					}
					catch (Exception repdic) {}
					filenames.put("dict"+String.valueOf(d), dsforname);
					zipOut.putNextEntry(ze);
					LocalXMLDictionaryWriter ldxmlw=new LocalXMLDictionaryWriter(tempdir+"_dict_"+String.valueOf(d), dict.getkeyword(),
					dict.getdescription(), dict.getauthor(), dict.getfixedvariableinfo(),
					dict.getcodelabel(), dict.getmissingdata());

					String restempw=ldxmlw.action();
					if (!restempw.equals(""))
					{
						zipOut.finish();
						zipOut.close();
						return new Result(restempw,false, null);
					}

					BufferedInputStream in = new BufferedInputStream(new File(tempdir+"_dict_"+String.valueOf(d)).toURI().toURL().openStream());
					byte[] buffer = new byte[1024];
					int read;
					while ((read = in.read(buffer)) > -1)
					{
						zipOut.write(buffer, 0, read);
					}
					zipOut.closeEntry();
					in.close();
					(new File(tempdir+"_dict_"+String.valueOf(d))).delete();
					ze = new ZipEntry("table"+String.valueOf(d));
					zipOut.putNextEntry(ze);
					DataReader data = new DataReader(dict);
					int[] subst=new int[dict.gettotalvar()];
					Arrays.fill(subst,0);
					if (!data.open(new String[0],subst,false))
					{
						zipOut.finish();
						zipOut.close();
						return new Result(data.getmessage(), false, null);
					}
					String[] line;PrintStream ps = new PrintStream(zipOut);
					while (!data.isLast())
					{
						line = data.getRecord();
						for(int i=0; i<line.length-1;i++)
						{
							try
							{
								line[i]=line[i].replaceAll("\n", " ");
							}
							catch (Exception enl) {}
							try
							{
								line[i]=line[i].replaceAll("\t", " ");
							}
							catch (Exception enl) {}
							try
							{
								line[i]=line[i].replaceAll("\r", " ");
							}
							catch (Exception enl) {}
							ps.print((line[i]+"\t"));
						}
						try
						{
							line[line.length-1]=line[line.length-1].replaceAll("\n", " ");
						}
						catch (Exception enl) {}
						ps.print((line[line.length-1]));
						ps.println();
					}
					data.close();
					zipOut.closeEntry();
				}
			}
			ze = new ZipEntry("DataSetNames");
			zipOut.putNextEntry(ze);
			ObjectOutputStream oos = new ObjectOutputStream(zipOut);
			oos.writeObject(filenames);
			zipOut.closeEntry();
			zipOut.finish();
			zipOut.close();
		}
		catch (Exception e)
		{
			return new Result("%513%<br>\n",false, null);
		}
		return new Result("%501% ("+filename+")<br>\n", true, null);
	}
	/**
	*The required parameters for the pathtransport procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters = new LinkedList<GetRequiredParameters>();
		String[] dep = { "" };
		parameters.add(new GetRequiredParameters(Keywords.Path + "=","path", false, 510, dep, "", 1));
		parameters.add(new GetRequiredParameters("","note", false, 511, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.filename, "filesave=.zip", true, 507, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.dsname, "multipletext", false, 2402, dep, "", 2));
		parameters.add(new GetRequiredParameters("", "note", false, 2403, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the procedure name
	*/
	public String[] getstepinfo()
	{
		String[] info = new String[2];
		info[0] = "504";
		info[1] = "509";
		return info;
	}
}