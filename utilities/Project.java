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

package ADaMSoft.utilities;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import java.util.TreeMap;

/**
* Utilities to manage the project status
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/
public class Project
{
	//this structure save the defined path as name(key) and path
	private DefinedPath definedpath;
	//this structure save the definition as name(key) and value
	private NameAndDefinition nameanddefinition;
	//this structure save the defined settings
	private AllSetting allsetting;
	//this structure save the macro steps
	private MacroSteps macrosteps;
	TreeMap<String, String> nameanddefinitionordered=new TreeMap<String, String>();
	public Project()
	{
		definedpath = new DefinedPath();
		nameanddefinition = new NameAndDefinition();
		allsetting = new AllSetting();
		macrosteps = new MacroSteps();
	}
	public void clearMacroStep()
	{
		macrosteps.clear();
	}
	/**
	* Returns the steps associated with a given name
	*/
	public Vector<String> getMacroStep(String namestep)
	{
		namestep=namestep.toLowerCase();
		return macrosteps.get(namestep);
	}
	/**
	* Returns the steps associated with a given name
	*/
	public TreeMap<String, Vector<String>> getAllMacroStep()
	{
		return macrosteps;
	}
	/**
	* Remove the steps associated with a given name
	*/
	public boolean delMacroStep(String namestep)
	{
		namestep=namestep.toLowerCase();
		if (macrosteps.get(namestep)!=null)
		{
			macrosteps.remove(namestep);
			return true;
		}
		else
			return false;
	}
	/**
	* Add a step to the Macro Steps
	*/
	public void addMacroStep(String namestep, Vector<String> statements)
	{
		if (macrosteps==null)
			macrosteps=new MacroSteps();
		try
		{
			namestep=namestep.replaceAll(" ","");
		}
		catch (Exception e){}
		namestep=namestep.toLowerCase();
		macrosteps.put(namestep, statements);
	}
	/**
	*Returns the number of macro steps defined
	*/
	public int getNumMacroStep()
	{
		return macrosteps.size();
	}
	/**
	* Returns the definition associated with namedefinition
	*/
	public String getDefine(String namedefinition)
	{
		return nameanddefinition.get(namedefinition);
	}
	/**
	* Returns all the definitions names that are in the project setting
	*/
	public Vector<String> getDefine()
	{
		return new Vector<String>(nameanddefinition.keySet());
	}
	/**
	*Deletes a path from the list
	*/
	public void delPath(String path)
	{
		path=path.toLowerCase();
		definedpath.remove(path);
	}
	/**
	*Deletes a setting from the list
	*/
	public void delSetting(String name, String type)
	{
		Iterator<SingleSetting> it=allsetting.iterator();
		while(it.hasNext())
		{
			SingleSetting ss = it.next();
			if(ss.name.equalsIgnoreCase(name) && ss.type.equalsIgnoreCase(type))
			{
				allsetting.remove(ss);
				break;
			}
		}
	}
	/**
	*Deletes a define from the list
	*/
	public void delDefine(String name)
	{
		name=name.toLowerCase();
		nameanddefinition.remove(name);
	}
	/**
	* Returns all names of the definitions and their corresponding values that are in the project setting
	*/
	public TreeMap<String, String> getNamesAndDefinitions()
	{
		return nameanddefinition;
	}
	/**
	* Returns all names of the definitions and their corresponding values that are in the project setting
	*/
	public TreeMap<String, String> getOrderedNamesAndDefinitions()
	{
		return nameanddefinition;
	}
	/**
	* Returns the path associated to the pathname
	*/
	public String getPath(String pathname)
	{
		return definedpath.get(pathname.toLowerCase())==null?"":definedpath.get(pathname.toLowerCase());
	}
	/**
	* Returns all the names of the path defined in the project setting
	*/
	public Vector<String> getPaths()
	{
		return new Vector<String>(definedpath.keySet());
	}
	/**
	* Returns all the Path names and the real paths defined in the project setting
	*/
	public TreeMap<String, String> getNamesAndPaths()
	{
		return definedpath;
	}
	/**
	*Returns the parameters and their values associated to a setting, according to its type and to its name.<p>
	*The Hashtable has the following form: parameter, values
	*/
	public Hashtable<String, String> getSetting(String type, String name)
	{
		for (int i=0; i<allsetting.size(); i++)
		{
			SingleSetting ss=allsetting.get(i);
			if ((ss.getType().equalsIgnoreCase(type)) && (ss.getName().equalsIgnoreCase(name)))
				return ss.getParameters();
		}
		return new Hashtable<String, String>();
	}
	/**
	*Returns all the names of the settings that correspond to the type.
	*/
	public Vector<String> getSettingNames(String type)
	{
		Vector<String> output = new Vector<String>();
		for (int i=0; i<allsetting.size(); i++)
		{
			SingleSetting ss=allsetting.get(i);
			if (ss.getType().equalsIgnoreCase(type))
				output.add(ss.getName());
		}
		return output;
	}
	/**
	*Returns all the type of the defined SETTING
	*/
	public Vector<String> getSettingsTypes()
	{
		TreeSet<String> ts = new TreeSet<String>();
		for (int i=0; i<allsetting.size(); i++)
		{
			SingleSetting ss=allsetting.get(i);
			ts.add(ss.getType().toLowerCase());
		}
		return new Vector<String>(ts);
	}
	/**
	* Add a new couple name/path
	*/
	public void addPath(String key, String value)
	{
		definedpath.remove(key.toLowerCase());
		try
		{
			value=value.replaceAll("\\\\","/");
		}
		catch (Exception fs){}
		if ((!value.endsWith(System.getProperty("file.separator"))) && (!(value.toUpperCase()).startsWith("HTTP")))
			value=value+System.getProperty("file.separator");
		if ((!value.endsWith(System.getProperty("file.separator"))) && ((value.toUpperCase()).startsWith("HTTP")))
			value=value+"/";
		definedpath.put(key.toLowerCase(), value);
	}
	/**
	* Add a new singleeSetting
	*/
	public void addSingleSetting(SingleSetting o)
	{
		if(allsetting.contains(o))
		{
			allsetting.remove(o);
			allsetting.add(o);
		}
		else
		{
			allsetting.add(o);
		}
	}
	/**
	* Add a new couple definition/value
	*/
	public void addDefinition(String key, String value)
	{
		nameanddefinition.remove(key.toLowerCase());
		nameanddefinition.put(key.toLowerCase(), value);
	}
	/**
	* Clear all the settings
	*/
	public void clearSettings()
	{
		allsetting.clear();
	}
	/**
	* Clear all the paths
	*/
	public void clearPath()
	{
		TreeMap<String, String> tempPath=new TreeMap<String, String>();
		for (Iterator<String> it = definedpath.keySet().iterator(); it.hasNext();) 
		{
			String namepath = it.next();
			String valuepath=definedpath.get(namepath);
			if (!namepath.equals("work")) tempPath.put(namepath, valuepath);
		}
		definedpath.clear();
		for (Iterator<String> it = tempPath.keySet().iterator(); it.hasNext();) 
		{
			String namepath = it.next();
			String valuepath=definedpath.get(namepath);
			definedpath.put(namepath, valuepath);
		}
		tempPath.clear();
		tempPath=null;
	}
	/**
	* Clear all the defines
	*/
	public void clearDefines()
	{
		TreeMap<String, String> tempPath=new TreeMap<String, String>();
		for (Iterator<String> it = nameanddefinition.keySet().iterator(); it.hasNext();) 
		{
			String namepath = it.next();
			String valuepath=nameanddefinition.get(namepath);
			if (!namepath.equalsIgnoreCase("openeddirectory") && !namepath.equalsIgnoreCase("main_directory") && !namepath.equalsIgnoreCase("workdir"))
				tempPath.put(namepath, valuepath);
		}
		nameanddefinition.clear();
		for (Iterator<String> it = tempPath.keySet().iterator(); it.hasNext();) 
		{
			String namepath = it.next();
			String valuepath=definedpath.get(namepath);
			nameanddefinition.put(namepath, valuepath);
		}
		tempPath.clear();
		tempPath=null;
	}
	/**
	* Returns all the defined settings
	*/
	public Vector<SingleSetting> getAllSettings()
	{
		return (AllSetting)allsetting.clone();
	}
}

class DefinedPath extends TreeMap<String, String>{private static final long serialVersionUID = 1L;}
class NameAndDefinition extends TreeMap<String, String>{private static final long serialVersionUID = 1L;}
class AllSetting extends Vector<SingleSetting>{private static final long serialVersionUID = 1L;}
class MacroSteps extends TreeMap<String, Vector<String>>{private static final long serialVersionUID = 1L;}
