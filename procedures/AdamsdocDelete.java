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

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.StepUtilities;

/**
* Delete an ADaMSoft document
* @author marco.scarno@gmail.com
* @date 12/05/2017
*/
public class AdamsdocDelete implements RunStep
{
	/**
	*Delete an ADaMSoft document
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		Keywords.percentage_total=2;
		Keywords.percentage_done=0;
		String [] requiredparameters=new String[] {Keywords.docname};
		String [] optionalparameters=new String[] {Keywords.docpath};
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			Keywords.percentage_total=0;
			return new Result(steputilities.getMessage(), false, null);
		}
		String documentname=(String)parameters.get(Keywords.docname);
		String path        =(String)parameters.get(Keywords.docpath);
		String workdir     =(String)parameters.get(Keywords.WorkDir);
		documentname=documentname.toLowerCase();
		String firstChar=documentname.substring(0,1);
		documentname = documentname.replaceFirst(firstChar,firstChar.toUpperCase());

		if (path==null)
			path=workdir;
		Keywords.percentage_done=1;
		path=path+documentname;
		if(!path.endsWith(Keywords.DocExtension))
			path=path+Keywords.DocExtension;

		boolean documentdeleted=(new File(path)).delete();
		if (documentdeleted)
		{
			Keywords.percentage_total=0;
			return new Result("%193% ("+path+")<br>\n", true, null);
		}
		else
		{
			Keywords.percentage_total=0;
			return new Result("%194% ("+path+")<br>\n", false, null);
		}
	}
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.docpath+"=", "path", false, 157, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.docname, "text", true,  195, dep, "", 2));
		return parameters;
	}
	public String[] getstepinfo()
	{
		String[] info=new String[2];
		info[0]="196";
		info[1]="3970";
		return info;
	}
}
