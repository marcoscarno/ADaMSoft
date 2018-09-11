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

import java.util.*;
import java.util.zip.*;
import java.io.*;

import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.GenericContainerForHashtable;
import ADaMSoft.utilities.StepUtilities;

/**
* This procedure will create a zip file for a dataset (in which there are the dictionary and the data table).
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class ProcDsexport implements RunStep
{
	private String filename;
	private String tempdir;
	/**
	*Execute the procedure dsexport
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String[] requiredparameters = new String[] {Keywords.dict, Keywords.filename};
		String[] optionalparameters = new String[0];
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);

		filename = (String) parameters.get(Keywords.filename);
		if (!filename.toLowerCase().endsWith(".zip"))
			filename=filename+".zip";
		tempdir=(String)parameters.get(Keywords.WorkDir);
		OutputStream out;
		ZipOutputStream zipOut;
		ZipEntry ze;
		GenericContainerForHashtable filenames=new GenericContainerForHashtable();
		try
		{
			out=new FileOutputStream(filename);
			zipOut = new ZipOutputStream(new BufferedOutputStream(out));
			ze = new ZipEntry("dict0");
			zipOut.putNextEntry(ze);
			tempdir=tempdir+"_dict_";

			File fileforname = new File(dict.getDictPath());
			String dsforname = fileforname.getName();
			try
			{
				dsforname=dsforname.replaceAll(Keywords.DictionaryExtension,"");
			}
			catch (Exception repdic) {}
			filenames.put("dict0", dsforname);

			LocalXMLDictionaryWriter ldxmlw=new LocalXMLDictionaryWriter(tempdir, dict.getkeyword(),
			dict.getdescription(), dict.getauthor(), dict.getfixedvariableinfo(),
			dict.getcodelabel(), dict.getmissingdata());

			String restempw=ldxmlw.action();
			if (!restempw.equals(""))
				return new Result(restempw,false, null);

			BufferedInputStream in = new BufferedInputStream(new File(tempdir).toURI().toURL().openStream());
			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) > -1)
			{
				zipOut.write(buffer, 0, read);
			}
			zipOut.closeEntry();
			in.close();
			(new File(tempdir)).delete();
			ze = new ZipEntry("DataSetNames");
			zipOut.putNextEntry(ze);
			ObjectOutputStream oos = new ObjectOutputStream(zipOut);
			oos.writeObject(filenames);
			zipOut.closeEntry();

		}
		catch (Exception e)
		{
			return new Result("%500%<br>\n",false, null);
		}
		try
		{
			ze = new ZipEntry("table0");
			zipOut.putNextEntry(ze);
			DataReader data = new DataReader(dict);
			int[] subst=new int[dict.gettotalvar()];
			Arrays.fill(subst,0);
			if (!data.open(new String[0],subst,false))
				return new Result(data.getmessage(), false, null);
			String[] line;
			PrintStream ps = new PrintStream(zipOut);
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
			zipOut.finish();
			zipOut.close();
		}
		catch (Exception e)
		{
			return new Result("%508%<br>\n", false, null);
		}
		return new Result("%501% ("+filename+")<br>\n", true, null);
	}
	/**
	*The required parameters for the dstransport procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters = new LinkedList<GetRequiredParameters>();
		String[] dep = { "" };
		parameters.add(new GetRequiredParameters(Keywords.dict + "=","dict", true, 506, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.filename, "filesave=.zip", true, 507, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the procedure name
	*/
	public String[] getstepinfo()
	{
		String[] info = new String[2];
		info[0] = "504";
		info[1] = "505";
		return info;
	}
}
