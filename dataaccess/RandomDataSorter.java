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

package ADaMSoft.dataaccess;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import java.util.Vector;
import java.util.Hashtable;

import ADaMSoft.keywords.Keywords;

/**
* This class randomly sorts a data set (by creating several sorted data sets and, than, by merging all of these in one)
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
public class RandomDataSorter
{
	int maxdatasorted=100;
	Object [] MainValues;
	int sortedfiles=0;
	String tempdir="";
	int totalrecord=0;
	String filesorted;
	ZipInputStream def;
	ObjectInputStream ois;
	String suffixfile="";
	boolean sortingerror=false;
	String message="";
	DataReader data;
	int totalvar;
	/**
	*Receive the dictionary, the var using which the data set will be sorted, their order and the temporary directory
	*/
	public RandomDataSorter(DictionaryReader dict, String tempdir, int[] replace)
	{
		sortingerror=false;
		message="";
		this.tempdir=tempdir;
		java.util.Date dateProcedure=new java.util.Date();
		long timeProcedure=dateProcedure.getTime();
		double addfn=Math.random()*100000;
		int addfni=(int)addfn;
		suffixfile=tempdir+"File"+String.valueOf(timeProcedure)+String.valueOf(addfni);
		boolean iswriting=(new File(suffixfile)).exists();
		while (iswriting)
		{
			timeProcedure=dateProcedure.getTime();
			addfn=Math.random()*100000;
			addfni=(int)addfn;
			suffixfile=tempdir+"File"+String.valueOf(timeProcedure)+String.valueOf(addfni);
			iswriting=(new File(suffixfile)).exists();
		}
		try
		{
			maxdatasorted = Integer.parseInt(System.getProperty(Keywords.MaxDataBuffered));
		}
		catch(NumberFormatException  nfe){}

		Vector<Hashtable<String, String>> fixedvariableinfo=dict.getfixedvariableinfo();

		totalvar=fixedvariableinfo.size();

		String[] vars=new String[totalvar];

		for (int i=0; i<totalvar; i++)
		{
			Hashtable<String, String> tempvar=fixedvariableinfo.get(i);
			String varname=(tempvar.get(Keywords.VariableName.toLowerCase())).trim();
			vars[i]=varname;
		}

		data=new DataReader(dict);
		if (!data.open(vars, replace, false))
		{
			sortingerror=true;
			message=data.getmessage();
		}
	}
	/**
	*Returns true in case of error
	*/
	public boolean geterror()
	{
		return sortingerror;
	}
	/**
	*Returns the error message
	*/
	public String getmessage()
	{
		return message;
	}
	/**
	*Do the sort
	*/
	public void sortdata()
	{
		MainValues=new Object[maxdatasorted];
		int obsreaded=0;
		while (!data.isLast())
		{
			String[] values=data.getRecord();
			if (values==null)
			{
				sortingerror=true;
				message=data.getmessage();
				return;
			}
			MainValues[obsreaded]=values;
			obsreaded++;
			totalrecord++;
			if (obsreaded>=maxdatasorted)
			{
				int resultsortexe=sortinfiles(0, obsreaded-1);
				if (resultsortexe!=0)
				{
					sortingerror=true;
					message="%515%<br>\n";
				}
				obsreaded=0;
				MainValues=null;
				System.gc();
				MainValues=new Object[maxdatasorted];
			}
		}
		if (obsreaded>0)
		{
			int resultsortexe=sortinfiles(0,obsreaded-1);
			if (resultsortexe!=0)
			{
				sortingerror=true;
				message="%515%<br>\n";
			}
			obsreaded=0;
			MainValues=null;
			System.gc();
			MainValues=new Object[maxdatasorted];
		}
		data.close();

		filesorted=suffixfile+".sorted";
        RecordFilesIterator[] rfi = new RecordFilesIterator[sortedfiles];
        ZipOutputStream	out=null;
        ObjectOutputStream oos=null;
		try
		{
			File filezip = new File(filesorted);
			out = new ZipOutputStream(new FileOutputStream(filezip));
			ZipEntry entry = new ZipEntry("Values");
			out.putNextEntry(entry);
			oos = new ObjectOutputStream(out);
	        Object[] currentRecords = new Object[sortedfiles];
	        for (int i = 0; i < currentRecords.length; i++)
	        {
				String filetemp=suffixfile+String.valueOf(i)+".tempsort";
	            rfi[i] = new SerializedObjectsRecordsFileIterator(filetemp);
	            if (rfi[i].hasNext())
	            {
                	currentRecords[i] = rfi[i].next(totalvar);
	            }
	        }
	        Object r=null;
	        while ((r = getMinimum(currentRecords, rfi)) != null)
	        {
				String [] tempwrite=(String[])r;
				for (int j=0; j<tempwrite.length; j++)
				{
					oos.writeObject(tempwrite[j]);
				}
				oos.reset();
        	}
	        for (int i = 0; i < rfi.length; i++)
	        {
            	rfi[i].close();
        	}
	        for (int i = 0; i < currentRecords.length; i++)
	        {
				String filetemp=suffixfile+String.valueOf(i)+".tempsort";
				File filedel=new File(filetemp);
				filedel.delete();
			}
			oos.close();
			out.close();
		}
		catch (Exception e)
		{
			try
			{
		        for (int i = 0; i < rfi.length; i++)
		        {
	            	rfi[i].close();
	        	}
			}
			catch (Exception ef) {}
			try
			{
				oos.close();
				out.close();
			}
			catch (Exception ef) {}
			sortingerror=true;
			message="%516%<br>\n";
		}
		return;
	}
	/**
	*Used to get the minumum value from each part (sorted) to which the data set was splitted
	*/
    public Object getMinimum(Object[] records, RecordFilesIterator[] rfi)
    {
        Object min = null;
        Object result = null;
        int minindex = 0;
        for (int i = 0; i < records.length; i++)
        {
            if ((min == null) && (records[i] != null))
            {
				String [] temp=(String[])records[i];
				min=temp;
				minindex = i;
			}
			else if ((min != null) && (records[i] != null))
			{
				if (compare() < 0)
				{
					String [] temp=(String[])records[i];
					min=temp;
					minindex = i;
				}
            }
        }
		String[] tempres=(String[])records[minindex];
		result=tempres;
        if (min != null)
        {
            if (rfi[minindex].hasNext())
            {
                records[minindex] = rfi[minindex].next(totalvar);
            }
            else
            {
                records[minindex] = null;
            }
        }
        return result;
    }
    /**
    *Sort values and write them in a temporary file
    */
	public int sortinfiles(int from, int to)
	{
		int resultsortexe=0;
		executesort(from, to);
		String filetemp=suffixfile+String.valueOf(sortedfiles)+".tempsort";
		try
		{
			File filezip = new File(filetemp);
			ZipOutputStream	out = new ZipOutputStream(new FileOutputStream(filezip));
			ZipEntry entry = new ZipEntry("Values");
			out.putNextEntry(entry);
			ObjectOutputStream oos = new ObjectOutputStream(out);
			for (int i=0;i<MainValues.length; i++)
			{
				if (MainValues[i]!=null)
				{
					String [] values=(String[])MainValues[i];
					for (int j=0; j<values.length; j++)
					{
						oos.writeObject(values[j]);
					}
				}
			}
			oos.close();
			out.close();
		}
		catch (Exception e)
		{
			resultsortexe=1;
		}
		sortedfiles++;
		return resultsortexe;
	}
	/**
	*Sorts the values
	*/
	public void executesort(int from, int to)
	{
		if (MainValues == null || MainValues.length < 2) return;
		int i = from, j = to;
		do
		{
			while( (i < to) && (compare() < 0) )
				i++;
			while( (j > from) && (compare() > 0) )
				j--;
			if (i < j)
			{
				Object temp = MainValues[i]; MainValues[i] = MainValues[j]; MainValues[j] = temp;
			}
			if (i <= j)
			{
				i++;
				j--;
			}
		}
		while(i <= j);
		if (from < j) executesort(from, j);
		if (i < to) executesort(i, to);
	}
	/**
	*Define the rule for the comparing alghoritm, by considering the variables type
	*/
	public int compare()
	{
		int compareresult=0;
		double anum=Math.random();
		double bnum=Math.random();
		if (anum<bnum)
			compareresult=-1;
		if (anum>bnum)
			compareresult=1;
		return compareresult;
	}
	/**
	*Reads and return the final record of the sorted file
	*/
	public Object[] readrecord(ObjectInputStream ino, ZipInputStream inz)
	{
		Object[] retrecord=new Object[0];
		try
		{
			if (inz.available()!=0)
			{
				retrecord=new Object[totalvar];
				boolean checknull=false;
				for (int v=0; v<totalvar; v++)
				{
					retrecord[v]=ino.readObject();
					if (retrecord[v]==null)
						checknull=true;
				}
				if (checknull)
					retrecord=new Object[0];
			}
		}
		catch (Exception e)
		{
			sortingerror=true;
			message="%517%<br>\n";
		}
		return retrecord;
	}
	/**
	*Return the name of the file that was sorted
	*/
	public String getFile()
	{
		return filesorted;
	}
	/**
	*Deletes all the temporary files
	*/
	public void deletefile()
	{
		try
		{
			for (int i=0; i<sortedfiles; i++)
			{
				String filetemp=suffixfile+String.valueOf(i)+".tempsort";
				File filezipdel = new File(filetemp);
				filezipdel.delete();
			}
		}
		catch (Exception e) {}
		try
		{
			File filezip = new File(filesorted);
			filezip.delete();
		}
		catch (Exception e) {}
	}
	/**
	*Returns the total numbers of sorted records
	*/
	public int getTotalRecords()
	{
		return totalrecord;
	}
	/**
	*Returns the suffix of all the partial sorted files
	*/
	public String getSuffixFile()
	{
		return suffixfile;
	}
	/**
	*Returns the number of the partial sorted files
	*/
	public int getSortedFiles()
	{
		return sortedfiles;
	}
	/**
	*Open the final sorted file
	*/
	public void openFinalFile()
	{
		try
		{
			File filezip = new File(filesorted);
			def = new ZipInputStream(new FileInputStream(filezip));
			def.getNextEntry();
			ois=new ObjectInputStream(def);
		}
		catch (Exception e)
		{
			sortingerror=true;
			message="%518%<br>\n";
		}
	}
	/**
	*Reads and return the final sorted record
	*/
	public Object[] readFinalRecord()
	{
		Object[] retrecord=new Object[0];
		try
		{
			if (def.available()!=0)
			{
				retrecord=new Object[totalvar];
				for (int v=0; v<totalvar; v++)
				{
					retrecord[v]=ois.readObject();
				}
			}
		}
		catch (Exception e)
		{
			sortingerror=true;
			message="%519%<br>\n";
		}
		return retrecord;
	}
	/**
	*Close and delete the final sorted file
	*/
	public void closeFinalFile()
	{
		try
		{
			ois.close();
			def.close();
			File filezip = new File(filesorted);
			filezip.delete();
		}
		catch (Exception e) {}
		MainValues=new Object[0];
		System.gc();
	}
}
