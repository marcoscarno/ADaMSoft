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

import ADaMSoft.dataaccess.DictionaryReader;
import ADaMSoft.dataaccess.DataReader;
import ADaMSoft.dataaccess.DataWriter;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.DataSetUtilities;
import ADaMSoft.utilities.VectorStringComparator;
import ADaMSoft.utilities.StringComparator;
import ADaMSoft.utilities.VectorStringComparatorNoC;
import ADaMSoft.utilities.StringComparatorNoC;

import ADaMSoft.keywords.Keywords;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.LinkedList;
import java.util.Vector;
import java.util.TreeSet;

/**
* This is the procedure that transpose a dataset
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcTranspose implements RunStep
{
	/**
	* Starts the execution of Proc Transpose and returns the corresponding message
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		boolean novgconvert=false;
		boolean noidconvert=false;
		String [] requiredparameters=new String[] {Keywords.OUT.toLowerCase(), Keywords.dict};
		String [] optionalparameters=new String[] {Keywords.var, Keywords.where, Keywords.vargroup, Keywords.varid, Keywords.replace, Keywords.novgconvert, Keywords.noidconvert};
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

		novgconvert =(parameters.get(Keywords.novgconvert)!=null);
		noidconvert =(parameters.get(Keywords.noidconvert)!=null);
		DictionaryReader dict = (DictionaryReader)parameters.get(Keywords.dict);
		String vartemp=(String)parameters.get(Keywords.var.toLowerCase());
		String vartempg=(String)parameters.get(Keywords.vargroup.toLowerCase());
		String vartempi=(String)parameters.get(Keywords.varid.toLowerCase());
		String[] vgname=new String[0];
		if (vartempg!=null)
		{
			vgname=(vartempg.trim()).split(" ");
		}

		if (vartempi!=null)
		{
			String[] testvar=vartempi.split(" ");
			if (testvar.length>1)
				return new Result("%1336%<br>\n", false, null);
		}
		if (vartemp==null)
		{
			vartemp="";
			for (int i=0; i<dict.gettotalvar(); i++)
			{
				boolean toadd=true;
				if (vartempg!=null)
				{
					for (int j=0; j<vgname.length; j++)
					{
						if (vgname[j].equalsIgnoreCase(dict.getvarname(i)))
							toadd=false;
					}
				}
				if (vartempi!=null)
				{
					if (vartempi.trim().equalsIgnoreCase(dict.getvarname(i)))
						toadd=false;
				}
				if (toadd)
					vartemp=vartemp+" "+dict.getvarname(i);
			}
			vartemp=vartemp.trim();
		}
		String[] tempvarname=vartemp.split(" ");
		int numberofvars=tempvarname.length;
		boolean invarisg=false;
		boolean invarisd=false;
		String vartouse=vartemp;
		for (int i=0; i<tempvarname.length; i++)
		{
			if (vartempg!=null)
			{
				for (int j=0; j<vgname.length; j++)
				{
					if ((tempvarname[i].trim()).equalsIgnoreCase(vgname[j]))
						invarisg=true;
				}
				if ((tempvarname[i].trim()).equalsIgnoreCase(vartempg.trim()))
					invarisg=true;
			}
			if (vartempi!=null)
			{
				if ((tempvarname[i].trim()).equalsIgnoreCase(vartempi.trim()))
					invarisd=true;
			}
		}
		if (invarisg)
			return new Result("%2059%<br>\n", false, null);
		if (invarisd)
			return new Result("%2072%<br>\n", false, null);

		if (vartempi!=null)
		{
			vartouse=vartempi+" "+vartouse;
		}
		vartouse=vartouse.trim();

		String tgvno="";
		for (int i=0; i<vgname.length; i++)
		{
			tgvno=tgvno+" "+vgname[i];
		}
		vartouse=tgvno.trim()+" "+vartouse;
		vartouse=vartouse.trim();
		String[] varstouse=vartouse.split(" ");
		String replace =(String)parameters.get(Keywords.replace);
		int[] replacerule=new int[varstouse.length];
		int rifrep=0;
		if (replace==null)
			rifrep=0;
		else if (replace.equalsIgnoreCase(Keywords.replaceall))
			rifrep=1;
		else if (replace.equalsIgnoreCase(Keywords.replaceformat))
			rifrep=2;
		else if (replace.equalsIgnoreCase(Keywords.replacemissing))
			rifrep=3;
		for (int i=0; i<varstouse.length; i++)
		{
			replacerule[i]=rifrep;
		}

		if (vgname.length>0)
		{
			for (int i=0; i<vgname.length; i++)
			{
				replacerule[i]=1;
			}
		}

		String keyword="Transpose "+dict.getkeyword();
		String description="Transpose "+dict.getdescription();
		String author=dict.getauthor();

		DataReader data = new DataReader(dict);

		String[] values=null;
		TreeMap<Vector<String>, TreeSet<String>> idvar=null;
		TreeMap<Vector<String>, Integer> groupvar=null;
		if (novgconvert)
		{
			idvar=new TreeMap<Vector<String>, TreeSet<String>>(new VectorStringComparatorNoC());
			groupvar=new TreeMap<Vector<String>, Integer>(new VectorStringComparatorNoC());
		}
		else
		{
			idvar=new TreeMap<Vector<String>, TreeSet<String>>(new VectorStringComparator());
			groupvar=new TreeMap<Vector<String>, Integer>(new VectorStringComparator());
		}

		int posvi=vgname.length;
		int totalobs=0;
		String existidvar="";
		if (!data.open(varstouse, replacerule, false))
			return new Result(data.getmessage(), false, null);
		String where=(String)parameters.get(Keywords.where.toLowerCase());
		if (where!=null)
		{
			if (!data.setcondition(where)) return new Result(data.getmessage(), false, null);
		}
		while (!data.isLast())
		{
			values = data.getRecord();
			if (values!=null)
			{
				totalobs++;
				if (existidvar.equals(""))
				{
					if (vartempg!=null)
					{
						Vector<String> temp=new Vector<String>();
						for (int i=0; i<vgname.length; i++)
						{
							temp.add(values[i]);
						}
						if (groupvar.get(temp)==null)
							groupvar.put(temp, new Integer(1));
						else
						{
							int tv=groupvar.get(temp).intValue();
							groupvar.put(temp, new Integer(tv+1));
						}
					}
					if (vartempi!=null)
					{
						Vector<String> temp=new Vector<String>();
						if (vartempg!=null)
						{
							for (int i=0; i<vgname.length; i++)
							{
								temp.add(values[i]);
							}
						}
						else
						{
							String t="";
							temp.add(t);
						}
						TreeSet<String> ttidvar=idvar.get(temp);
						if (ttidvar==null)
						{
							if (!noidconvert)
								ttidvar=new TreeSet<String>(new StringComparator());
							else
								ttidvar=new TreeSet<String>(new StringComparatorNoC());
						}
						if (ttidvar.contains(values[posvi]))
							existidvar=values[posvi];
						else
							ttidvar.add(values[posvi]);
						idvar.put(temp, ttidvar);
					}
				}
			}
		}
		data.close();
		if (!existidvar.equals(""))
			return new Result("%2076% ("+existidvar+")<br>\n", false, null);

		String[][] valuestouse=null;
		String[] valuestowrite=null;
		DataSetUtilities dsu=new DataSetUtilities();
		dsu.setreplace(replace);
		Hashtable<String, String> tempmd=new Hashtable<String, String>();
		Hashtable<String, String> clvar=new Hashtable<String, String>();
		boolean dataopened=false;
		if ((vartempg==null) && (vartempi==null))
		{
			try
			{
				for (int i=0; i<tempvarname.length; i++)
				{
					clvar.put(tempvarname[i], dict.getvarlabelfromname(tempvarname[i]));
				}
				dsu.addnewvar("var", "%2073%", Keywords.TEXTSuffix, clvar, tempmd);
				for (int i=0; i<totalobs; i++)
				{
					dsu.addnewvar("obs"+String.valueOf(i+1), "%2074% "+String.valueOf(i+1), Keywords.TEXTSuffix, tempmd, tempmd);
				}
				if (!dw.opendatatable(dsu.getfinalvarinfo()))
					return new Result(dw.getmessage(), false, null);
				valuestouse=new String[numberofvars][totalobs];
				if (!data.open(varstouse, replacerule, false))
					return new Result(data.getmessage(), false, null);
				dataopened=true;
				int pointerobs=0;
				while (!data.isLast())
				{
					values = data.getRecord();
					for (int i=0; i<values.length; i++)
					{
						valuestouse[i][pointerobs]=values[i];
					}
					pointerobs++;
				}
				data.close();
				valuestowrite=new String[totalobs+1];
				for (int i=0; i<tempvarname.length; i++)
				{
					valuestowrite[0]=tempvarname[i];
					for (int j=0; j<totalobs; j++)
					{
						valuestowrite[j+1]=valuestouse[i][j];
					}
					dw.write(valuestowrite);
				}
			}
			catch (Exception e)
			{
				valuestouse=null;
				if (dataopened)
					data.close();
				System.gc();
				return new Result("%2075%<br>\n", false, null);
			}
		}
		else if ((vartempg==null) && (vartempi!=null))
		{
			try
			{
				for (int i=0; i<tempvarname.length; i++)
				{
					clvar.put(tempvarname[i], dict.getvarlabelfromname(tempvarname[i]));
				}
				dsu.addnewvar("ref", "%2073%", Keywords.TEXTSuffix, clvar, tempmd);
				Vector<String> temp=new Vector<String>();
				String t="";
				temp.add(t);
				TreeSet<String> ttidvar=idvar.get(temp);
				Hashtable<String, Integer> positionvar=new Hashtable<String, Integer>();
				Iterator<String> itt = ttidvar.iterator();
				while(itt.hasNext())
				{
					String tvname=itt.next();
					positionvar.put(tvname,new Integer(positionvar.size()+1));
					dsu.addnewvar("obs"+String.valueOf(positionvar.size()), tvname, Keywords.TEXTSuffix, tempmd, tempmd);
				}
				if (!dw.opendatatable(dsu.getfinalvarinfo()))
					return new Result(dw.getmessage(), false, null);

				valuestouse=new String[numberofvars][totalobs];

				if (!data.open(varstouse, replacerule, false))
					return new Result(data.getmessage(), false, null);
				dataopened=true;
				while (!data.isLast())
				{
					values = data.getRecord();
					int position=(positionvar.get(values[0])).intValue();
					for (int i=1; i<numberofvars+1; i++)
					{
						valuestouse[i-1][position-1]=values[i];
					}
				}
				data.close();
				valuestowrite=new String[totalobs+1];
				for (int i=0; i<tempvarname.length; i++)
				{
					valuestowrite[0]=tempvarname[i];
					for (int j=0; j<totalobs; j++)
					{
						valuestowrite[j+1]=valuestouse[i][j];
					}
					dw.write(valuestowrite);
				}
			}
			catch (Exception e)
			{
				valuestouse=null;
				if (dataopened)
					data.close();
				System.gc();
				return new Result("%2075%<br>\n", false, null);
			}
		}
		else if ((vartempg!=null) && (vartempi==null))
		{
			try
			{
				for (int i=0; i<vgname.length; i++)
				{
					dsu.addnewvar("g_"+vgname[i], dict.getvarlabelfromname(vgname[i]), Keywords.TEXTSuffix, tempmd, tempmd);
				}
				for (int i=0; i<tempvarname.length; i++)
				{
					clvar.put(tempvarname[i], dict.getvarlabelfromname(tempvarname[i]));
				}
				dsu.addnewvar("ref", "%2073%", Keywords.TEXTSuffix, clvar, tempmd);
				int maxvn=0;
				Iterator<Vector<String>> igv = groupvar.keySet().iterator();
				while(igv.hasNext())
				{
					Vector<String> gv=igv.next();
					int gvnv=(groupvar.get(gv)).intValue();
					if (gvnv>maxvn)
						maxvn=gvnv;
				}
				for (int i=0; i<maxvn; i++)
				{
					dsu.addnewvar("obs"+String.valueOf(i+1), "%2074% "+String.valueOf(i+1), Keywords.TEXTSuffix, tempmd, tempmd);
				}
				if (!dw.opendatatable(dsu.getfinalvarinfo()))
					return new Result(dw.getmessage(), false, null);

				igv = groupvar.keySet().iterator();
				while(igv.hasNext())
				{
					Vector<String> gv=igv.next();
					int gvnv=(groupvar.get(gv)).intValue();
					valuestouse=new String[numberofvars][gvnv];
					if (!data.open(varstouse, replacerule, false))
						return new Result(data.getmessage(), false, null);
					dataopened=true;
					int pointerobs=0;
					boolean toinsert=true;
					while (!data.isLast())
					{
						values = data.getRecord();
						toinsert=true;
						for (int i=0; i<gv.size(); i++)
						{
							if (!values[i].equals(gv.get(i)))
								toinsert=false;
						}
						if (toinsert)
						{
							for (int i=gv.size(); i<values.length; i++)
							{
								valuestouse[i-gv.size()][pointerobs]=values[i];
							}
							pointerobs++;
						}
					}
					data.close();
					dataopened=false;
					valuestowrite=new String[maxvn+1+vgname.length];
					for (int i=0; i<tempvarname.length; i++)
					{
						for (int j=0; j<gv.size(); j++)
						{
							valuestowrite[j]=gv.get(j);
						}
						valuestowrite[vgname.length]=tempvarname[i];
						for (int j=0; j<gvnv; j++)
						{
							valuestowrite[j+1+vgname.length]=valuestouse[i][j];
						}
						dw.write(valuestowrite);
					}
					valuestouse=null;
				}
			}
			catch (Exception e)
			{
				valuestouse=null;
				if (dataopened)
					data.close();
				System.gc();
				return new Result("%2075%<br>\n", false, null);
			}
		}
		else
		{
			try
			{
				for (int i=0; i<vgname.length; i++)
				{
					dsu.addnewvar("g_"+vgname[i], dict.getvarlabelfromname(vgname[i]), Keywords.TEXTSuffix, tempmd, tempmd);
				}
				for (int i=0; i<tempvarname.length; i++)
				{
					clvar.put(tempvarname[i], dict.getvarlabelfromname(tempvarname[i]));
				}
				dsu.addnewvar("ref", "%2073%", Keywords.TEXTSuffix, clvar, tempmd);
				Hashtable<String, Integer> positionvar=new Hashtable<String, Integer>();
				TreeSet<String> deftidvar=new TreeSet<String>();
				Iterator<Vector<String>> igv = groupvar.keySet().iterator();
				int maxvn=0;
				while(igv.hasNext())
				{
					Vector<String> gv=igv.next();
					int gvnv=(groupvar.get(gv)).intValue();
					if (gvnv>maxvn)
						maxvn=gvnv;
					TreeSet<String> ttidvar=idvar.get(gv);
					Iterator<String> iv = ttidvar.iterator();
					while(iv.hasNext())
					{
						String checki=iv.next();
						if (!deftidvar.contains(checki))
							deftidvar.add(checki);
					}
				}
				Iterator<String> itv = deftidvar.iterator();
				int refpoin=1;
				while(itv.hasNext())
				{
					String tvname=itv.next();
					positionvar.put(tvname,new Integer(refpoin));
					dsu.addnewvar("obs"+String.valueOf(refpoin), tvname, Keywords.TEXTSuffix, tempmd, tempmd);
					refpoin++;
				}
				if (!dw.opendatatable(dsu.getfinalvarinfo()))
					return new Result(dw.getmessage(), false, null);

				igv = groupvar.keySet().iterator();
				maxvn=positionvar.size();
				while(igv.hasNext())
				{
					Vector<String> gv=igv.next();
					valuestouse=new String[numberofvars][positionvar.size()];
					for (int i=0; i<numberofvars; i++)
					{
						for (int j=0; j<maxvn; j++)
						{
							valuestouse[i][j]="";
						}
					}
					if (!data.open(varstouse, replacerule, false))
						return new Result(data.getmessage(), false, null);
					dataopened=true;
					boolean toinsert=true;
					while (!data.isLast())
					{
						values = data.getRecord();
						toinsert=true;
						for (int i=0; i<gv.size(); i++)
						{
							if (!values[i].equals(gv.get(i)))
								toinsert=false;
						}
						if (toinsert)
						{
							int position=(positionvar.get(values[posvi])).intValue();
							for (int i=0; i<numberofvars; i++)
							{
								valuestouse[i][position-1]=values[i+posvi+1];
							}
						}
					}
					data.close();
					dataopened=false;
					valuestowrite=new String[positionvar.size()+1+vgname.length];
					for (int i=0; i<tempvarname.length; i++)
					{
						for (int j=0; j<gv.size(); j++)
						{
							valuestowrite[j]=gv.get(j);
						}
						valuestowrite[vgname.length]=tempvarname[i];
						for (int j=0; j<maxvn; j++)
						{
							valuestowrite[j+1+vgname.length]=valuestouse[i][j];
						}
						dw.write(valuestowrite);
					}
					valuestouse=null;
				}
			}
			catch (Exception e)
			{
				valuestouse=null;
				if (dataopened)
					data.close();
				System.gc();
				return new Result("%2075%<br>\n", false, null);
			}
		}

		boolean resclose=dw.close();
		if (!resclose)
			return new Result(dw.getmessage(), false, null);
		Vector<Hashtable<String, String>> tablevariableinfo=dw.getVarInfo();
		Hashtable<String, String> datatableinfo=dw.getTableInfo();
		Vector<StepResult> result = new Vector<StepResult>();
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
		parameters.add(new GetRequiredParameters(Keywords.dict+"=", "dict", true, 541, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.OUT.toLowerCase()+"=", "setting=out", true, 440, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.viewout, "checkbox", false, 1612, dep, "", 1));
		dep = new String[1];
		dep[0]=Keywords.dict;
		parameters.add(new GetRequiredParameters(Keywords.var, "vars=all", false, 2069, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.vargroup, "vars=all", false, 655, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.varid, "vars=all", false, 2070, dep, "", 2));
		dep = new String[0];
		parameters.add(new GetRequiredParameters(Keywords.where,"text", false, 2805,dep,"",2));
		parameters.add(new GetRequiredParameters("", "note", false, 2806, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.novgconvert, "checkbox", false, 2227, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.noidconvert, "checkbox", false, 2252, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.replace, "listsingle=530_NULL,531_"+Keywords.replaceall+",532_"+Keywords.replaceformat+",533_"+Keywords.replacemissing,false, 534, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="4166";
		retprocinfo[1]="2068";
		return retprocinfo;
	}
}
