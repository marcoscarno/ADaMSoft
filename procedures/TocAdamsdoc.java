/**
* Copyright (c) 2015 MS
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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.utilities.GetRequiredParameters;

/**
* Create a toc for a path that contains several adams documents
v*/
public class TocAdamsdoc implements RunStep
{
	/**
	*If set to false the result data set will be on the client.<p>
	*/
	/**
	*Create a data set with the information on several adams documents stored into a path
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase()};
		String [] optionalparameters=new String[] {Keywords.docpath};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		DataWriter dw=new DataWriter(parameters, Keywords.OUT.toLowerCase());
		if (!dw.getmessage().equals(""))
			return new Result(dw.getmessage(), false, null);
		String path        =(String)parameters.get(Keywords.docpath);
		String workdir     =(String)parameters.get(Keywords.WorkDir);
		if (path==null)
			path=workdir;
		if (path.toLowerCase().startsWith("http:"))
			return new Result("%1208% ("+path+")<br>\n", false, null);
		File dir = new File(path);
		String[] children = dir.list();
		Vector<String> docnames=new Vector<String>();
		if (children == null)
		{
			return new Result("%434% ("+path+")<br>\n", false, null);
		}
		else
		{
			for (int i=0; i<children.length; i++)
			{
				String filename = children[i];
				if (filename.toLowerCase().endsWith(Keywords.DocExtension.toLowerCase()))
					docnames.add(filename);
			}
		}
		if (docnames.size()==0)
			return new Result("%435% ("+path+")<br>\n", false, null);
		Vector<String[]> infodoc=new Vector<String[]>();
		for (int i=0; i<docnames.size(); i++)
		{
			String[] information=new String[8];
			information[0]=path;
			String docn=docnames.get(i);
			if (docn.endsWith(Keywords.DocExtension))
				docn=docn.substring(0, docn.indexOf(Keywords.DocExtension));
			information[1]=docn;
			String docfile=path+docnames.get(i);
			try
			{
				java.net.URL documentfile=(new File(docfile)).toURI().toURL();
				URLConnection urlConn = documentfile.openConnection();
				urlConn.setDoInput(true);
				urlConn.setUseCaches(false);
				ZipInputStream indata= new ZipInputStream(urlConn.getInputStream());
				ZipEntry entry;
				String [] temp=new String[0];
				while ((entry = indata.getNextEntry()) != null)
				{
					String nameFile = entry.getName();
					if (nameFile.equalsIgnoreCase("Info"))
					{
						BufferedReader filebuffered= new BufferedReader(new InputStreamReader(indata));
						String line=filebuffered.readLine();
						temp=line.split("\t");
					}
		        }
				indata.close();
		        information[2]=temp[0];
		        information[3]=temp[1];
		        information[4]=temp[2];
		        information[5]=temp[4];
		        information[6]=temp[3];
		        information[7]=temp[5];
				infodoc.add(information);
			}
			catch (Exception e)
			{
				return new Result("%438% ("+docfile+")<br>\n", false, null);
			}
		}
		String keyword=Keywords.TOC+" "+path;
		String description=Keywords.TOC+" "+path;
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());
		Vector<Hashtable<String, String>> fixedvariableinfo=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> codelabel=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> missingdata=new Vector<Hashtable<String, String>>();
		for (int i=0; i<8; i++)
		{
			String labelvar="";
			Hashtable<String, String> tempfixedvariableinfo=new Hashtable<String, String>();
			Hashtable<String, String> tempcodelabel=new Hashtable<String, String>();
			Hashtable<String, String> tempmissingdata=new Hashtable<String, String>();
			tempfixedvariableinfo.put(Keywords.VariableName.toLowerCase(),"v"+(String.valueOf(i)));
			tempfixedvariableinfo.put(Keywords.VariableFormat.toLowerCase(),Keywords.TEXTSuffix);
			if (i==0)
				labelvar="%1200%";
			else if (i==1)
				labelvar="%1201%";
			else if (i==2)
				labelvar="%1203%";
			else if (i==3)
				labelvar="%1204%";
			else if (i==4)
				labelvar="%1205%";
			else if (i==6)
			{
				labelvar="%1206%";
				tempcodelabel.put("0", "%363%");
				tempcodelabel.put("1", "%362%");
			}
			else if (i==7)
				labelvar="%1300%";
			else if (i==5)
				labelvar="%1207%";
			tempfixedvariableinfo.put(Keywords.LabelOfVariable.toLowerCase(),labelvar);
			fixedvariableinfo.add(tempfixedvariableinfo);
			codelabel.add(tempcodelabel);
			missingdata.add(tempmissingdata);
		}
		boolean resopen=dw.opendatatable(fixedvariableinfo);
		if (!resopen)
			return new Result(dw.getmessage(), false, null);
		for (int i=0; i<infodoc.size(); i++)
		{
			String[] tempinfo=infodoc.get(i);
			dw.write(tempinfo);
		}
		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
		Hashtable<String, String> datatableinfo=dw.getTableInfo();
		Vector<StepResult> result = new Vector<StepResult>();
		result.add(new LocalDictionaryWriter(dw.getdictpath(), keyword, description, author, dw.gettabletype(),
		datatableinfo, fixedvariableinfo, tablevariableinfo, codelabel, missingdata, null));
		return new Result("", true, result);
	}
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.docpath+"=", "path", false, 439, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 440, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		return parameters;
	}
	public String[] getstepinfo()
	{
		String[] info=new String[2];
		info[0]="436";
		info[1]="437";
		return info;
	}
}
