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

package ADaMSoft.dataaccess;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URLConnection;
import java.util.Hashtable;
import java.util.Vector;

import ADaMSoft.keywords.Keywords;

/**
* This class sorts a data set (by creating several sorted data sets and, than, by merging all of these in one)
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class DataSorter
{
	private int maxdatasorted=1000;
	private String[][] MainValues;
	private int sortedfiles=0;
	private int totalvar=0;
	private int sortvar=0;
	private int totalrecord=0;
	private String filesorted;
	private ObjectInputStream ois;
	private BufferedInputStream def;
	private String suffixfile="";
	private boolean ascending;
	private boolean sortingerror=false;
	private String message="";
	private DataReader data;
	private ObjectInputStream[] inputStreams;
	private BufferedInputStream[] binputStreams;
	boolean noconversion;
	private Vector<String> files;
	Object[] currentRecords;
	boolean[] varascending;
	boolean isvaglobal;
	/**
	*Receive the dictionary, the var using which the data set will be sorted, their order and the temporary directory
	*/
	public DataSorter(DictionaryReader dict, String[] var, boolean ascending, String tempdir, int[] replace, String where)
	{
		files=new Vector<String>();
		isvaglobal=true;
		noconversion=false;
		sortvar=var.length;
		sortingerror=false;
		message="";
		this.ascending=ascending;
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
			double tempmaxdatasorted = Double.parseDouble(System.getProperty(Keywords.MaxDataBuffered));
			tempmaxdatasorted=tempmaxdatasorted/2;
			maxdatasorted=(int)tempmaxdatasorted;
		}
		catch(NumberFormatException  nfe){}
		Vector<Hashtable<String, String>> fixedvariableinfo=dict.getfixedvariableinfo();
		totalvar=fixedvariableinfo.size();
		String[] newvar=new String[fixedvariableinfo.size()];

		Vector<String> remainvar=new Vector<String>();

		for (int i=0; i<totalvar; i++)
		{
			Hashtable<String, String> tempvar=fixedvariableinfo.get(i);
			String varname=(tempvar.get(Keywords.VariableName.toLowerCase())).trim();
			boolean issel=false;
			for (int j=0; j<var.length; j++)
			{
				if (varname.equalsIgnoreCase(var[j].trim()))
					issel=true;
			}
			if (!issel)
				remainvar.add(varname);
		}
		for (int i=0; i<var.length; i++)
		{
			newvar[i]=var[i];
		}
		for (int i=0; i<remainvar.size(); i++)
		{
			String tempname=remainvar.get(i);
			newvar[i+var.length]=tempname;
		}
		data=new DataReader(dict);
		if (!data.open(newvar, replace, false))
		{
			sortingerror=true;
			message=data.getmessage();
		}
		if (where!=null)
		{
			if (!data.setcondition(where))
			{
				sortingerror=true;
				message=data.getmessage();
			}
		}
	}
	/**
	*Receive the dictionary, the var using which the data set will be sorted, their order and the temporary directory
	*/
	public DataSorter(DictionaryReader dict, String[] var, boolean[] varascending, String tempdir, int[] replace, String where)
	{
		files=new Vector<String>();
		isvaglobal=false;
		noconversion=false;
		sortvar=var.length;
		sortingerror=false;
		message="";
		this.varascending=varascending;
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
			double tempmaxdatasorted = Double.parseDouble(System.getProperty(Keywords.MaxDataBuffered));
			tempmaxdatasorted=tempmaxdatasorted/2;
			maxdatasorted=(int)tempmaxdatasorted;
		}
		catch(NumberFormatException  nfe){}
		Vector<Hashtable<String, String>> fixedvariableinfo=dict.getfixedvariableinfo();
		totalvar=fixedvariableinfo.size();
		String[] newvar=new String[fixedvariableinfo.size()];

		Vector<String> remainvar=new Vector<String>();

		for (int i=0; i<totalvar; i++)
		{
			Hashtable<String, String> tempvar=fixedvariableinfo.get(i);
			String varname=(tempvar.get(Keywords.VariableName.toLowerCase())).trim();
			boolean issel=false;
			for (int j=0; j<var.length; j++)
			{
				if (varname.equalsIgnoreCase(var[j].trim()))
					issel=true;
			}
			if (!issel)
				remainvar.add(varname);
		}
		for (int i=0; i<var.length; i++)
		{
			newvar[i]=var[i];
		}
		for (int i=0; i<remainvar.size(); i++)
		{
			String tempname=remainvar.get(i);
			newvar[i+var.length]=tempname;
		}

		data=new DataReader(dict);
		if (!data.open(newvar, replace, false))
		{
			sortingerror=true;
			message=data.getmessage();
		}
		if (where!=null)
		{
			if (!data.setcondition(where))
			{
				sortingerror=true;
				message=data.getmessage();
			}
		}
	}
	/**
	*Receives from the procedure the value of the maxdatasorted
	*/
	public void setmaxdatasorted(int maxdatasorted)
	{
		this.maxdatasorted=maxdatasorted;
	}
	/**
	*If the arguments is true then the data are not automatically converted
	*/
	public void setconversion(boolean noconversion)
	{
		this.noconversion=noconversion;
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
		MainValues=new String[maxdatasorted][totalvar];
		int obsreaded=0;
		while (!data.isLast())
		{
			String[] values=data.getRecord();
			if (values!=null)
			{
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
					MainValues=new String[0][0];
					MainValues=null;
					MainValues= new String[maxdatasorted][totalvar];
				}
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
		}
		data.close();
		MainValues=new String[0][0];
		MainValues=null;
		System.gc();
		filesorted=suffixfile+".sorted";
        BufferedOutputStream out=null;
        ObjectOutputStream oos=null;
		try
		{
			out = new BufferedOutputStream(new FileOutputStream(filesorted));
			oos = new ObjectOutputStream(out);
	        currentRecords = new Object[sortedfiles];
			inputStreams = new ObjectInputStream[sortedfiles];
			binputStreams = new BufferedInputStream[sortedfiles];
	        for (int i = 0; i < files.size(); i++)
	        {
				File filetemp=new File(files.get(i));
				java.net.URL fileUrl = filetemp.toURI().toURL();
				URLConnection      urlConn;
				urlConn = fileUrl.openConnection();
				urlConn.setDoInput(true);
				urlConn.setUseCaches(true);
				binputStreams[i] = new BufferedInputStream(urlConn.getInputStream());
				inputStreams[i]= new ObjectInputStream(binputStreams[i]);
				currentRecords[i]=inputStreams[i].readObject();
	        }
	        Object[] r=null;
	        int written=0;
	        while ((r = getMinimum()) != null)
	        {
	        	written++;
				oos.writeObject(r);
				if(written>maxdatasorted)
				{
					oos.flush();
					oos.reset();
					written=0;
				}
        	}
	        for (int i = 0; i < inputStreams.length; i++)
	        {
				try
				{
					inputStreams[i].close();
					binputStreams[i].close();
					File filedel=new File(files.get(i));
					filedel.delete();
				}
				catch (Exception eee) {}
        	}
			oos.flush();
			oos.close();
			out.close();
		}
		catch (Exception e)
		{
			try
			{
		        for (int i = 0; i < inputStreams.length; i++)
		        {
					try
					{
						inputStreams[i].close();
						binputStreams[i].close();
						File filedel=new File(files.get(i));
						filedel.delete();
					}
					catch (Exception eee) {}
	        	}
	        	oos.flush();
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
    private Object[] getMinimum()
    {
        Object min = null;
        Object[] result = null;
        int minindex = 0;
        for (int i = 0; i < currentRecords.length; i++)
        {
            if ((min == null) && (currentRecords[i] != null))
            {
				min=currentRecords[i];
				minindex = i;
			}
			else if ((min != null) && (currentRecords[i] != null))
			{
				if (isvaglobal)
				{
					if ((ascending) && (compare(min, currentRecords[i]) > 0))
					{
						min=currentRecords[i];
						minindex = i;
					}
					if ((!ascending) && (compare(min, currentRecords[i]) < 0))
					{
						min=currentRecords[i];
						minindex = i;
					}
				}
				else
				{
					if (comparevect(min, currentRecords[i]) > 0)
					{
						min=currentRecords[i];
						minindex = i;
					}
				}
			}
        }
        if (currentRecords[minindex]==null)
        	return null;
		Object[] tempresult=(Object[])currentRecords[minindex];
		result=new Object[tempresult.length];
		for (int i=0; i<result.length; i++)
			result[i]=tempresult[i];
        if (min != null)
        {
            try
            {
				currentRecords[minindex]=inputStreams[minindex].readObject();
			}
			catch (Exception e)
			{
				currentRecords[minindex] = null;
				try
				{
					inputStreams[minindex].close();
					binputStreams[minindex].close();
				} catch (IOException e1) {}
			}
        }
        return result;
    }
    /**
    *Sort values and write them in a temporary file
    */
	private int sortinfiles(int from, int to)
	{
		int resultsortexe=0;
		executesort(from, to);
		String filetemp=suffixfile+String.valueOf(sortedfiles)+".tempsort";
		try
		{
			files.add(filetemp);
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filetemp));
			ObjectOutputStream oos = new ObjectOutputStream(out);
			for (int i=0;i<MainValues.length; i++)
			{
				if (MainValues[i]!=null && MainValues[i][0]!=null)
				{
					Object[] values=MainValues[i];
					oos.writeObject(values);
					oos.flush();
					oos.reset();
				}
			}
			oos.flush();
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
	private void executesort(int from, int to)
	{
		if (MainValues == null || MainValues.length < 2) return;
		int i = from, j = to;
		Object center = MainValues[(from+to)/2];
		do
		{
			if (isvaglobal)
			{
				if (ascending)
				{
					while( (i < to) && (compare(center, MainValues[i]) > 0) )
						i++;
					while( (j > from) && (compare(center, MainValues[j]) < 0) )
					j--;
				}
				else
				{
					while( (i < to) && (compare(center, MainValues[i]) < 0) )
						i++;
					while( (j > from) && (compare(center, MainValues[j]) > 0) )
						j--;
				}
			}
			else
			{
					while( (i < to) && (comparevect(center, MainValues[i]) > 0) )
						i++;
					while( (j > from) && (comparevect(center, MainValues[j]) < 0) )
					j--;
			}
			if (i < j)
			{
				String[] temp = MainValues[i];
				MainValues[i] = MainValues[j];
				MainValues[j] = temp;
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
	private int compare(Object a, Object b)
	{
		String [] avalues=(String[])a;
		String [] bvalues=(String[])b;
		for (int k=0; k<sortvar; k++)
		{
			int arenum=0;
			double anum=Double.NaN;
			double bnum=Double.NaN;
			if (!noconversion)
			{
				try
				{
					anum=Double.parseDouble(avalues[k]);
					arenum++;
				}
				catch (Exception nonnumber) {}
				try
				{
					bnum=Double.parseDouble(bvalues[k]);
					arenum++;
				}
				catch (Exception nonnumber) {}
			}
			if (arenum!=2)
			{
				if (((avalues[k].trim()).equals("")) && (!(bvalues[k].trim()).equals("")))
					return -1;
				else if ((!(avalues[k].trim()).equals("")) && ((bvalues[k].trim()).equals("")))
					return 1;
				else
				{
					if (avalues[k].compareTo(bvalues[k])<0) return -1;
					if (avalues[k].compareTo(bvalues[k])>0) return 1;
				}
			}
			else
			{
				if ((Double.isNaN(anum)) && (!Double.isNaN(bnum)))
					return -1;
				else if ((!Double.isNaN(anum)) && (Double.isNaN(bnum)))
					return 1;
				else if (anum<bnum)
					return -1;
				else if (anum>bnum)
					return 1;
			}
		}
		return 0;
	}
	/**
	*Define the rule for the comparing alghoritm, by considering the variables type
	*/
	private int comparevect(Object a, Object b)
	{
		String [] avalues=(String[])a;
		String [] bvalues=(String[])b;
		for (int k=0; k<sortvar; k++)
		{
			int arenum=0;
			double anum=Double.NaN;
			double bnum=Double.NaN;
			if (!noconversion)
			{
				try
				{
					anum=Double.parseDouble(avalues[k]);
					arenum++;
				}
				catch (Exception nonnumber) {}
				try
				{
					bnum=Double.parseDouble(bvalues[k]);
					arenum++;
				}
				catch (Exception nonnumber) {}
			}
			if (arenum!=2)
			{
				if (((avalues[k].trim()).equals("")) && (!(bvalues[k].trim()).equals("")))
				{
					if (varascending[k]) return -1;
					else return 1;
				}
				else if ((!(avalues[k].trim()).equals("")) && ((bvalues[k].trim()).equals("")))
				{
					if (varascending[k]) return 1;
					else return -1;
				}
				else
				{
					arenum=avalues[k].compareTo(bvalues[k]);
					if (!varascending[k]) arenum=arenum*-1;
				}
				if (arenum<=-1) return -1;
				if (arenum>=1) return 1;
			}
			else
			{
				if ((Double.isNaN(anum)) && (!Double.isNaN(bnum)))
				{
					if (varascending[k]) return -1;
					else return 1;
				}
				else if ((!Double.isNaN(anum)) && (Double.isNaN(bnum)))
				{
					if (varascending[k]) return 1;
					else return -1;
				}
				else if (anum<bnum)
				{
					if (varascending[k]) return -1;
					else return 1;
				}
				else if (anum>bnum)
				{
					if (varascending[k]) return 1;
					else return -1;
				}
			}
		}
		return 0;
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
		return files.size();
	}
	/**
	*Open the final sorted file
	*/
	public void openFinalFile()
	{
		try
		{
			File filetemp = new File(filesorted);
			java.net.URL fileUrl = filetemp.toURI().toURL();
			URLConnection      urlConn;
			urlConn = fileUrl.openConnection();
			urlConn.setDoInput(true);
			urlConn.setUseCaches(true);
			def = new BufferedInputStream(urlConn.getInputStream());
			ois = new ObjectInputStream(def);
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
				retrecord=(Object[])ois.readObject();
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
		MainValues=new String[0][0];
		MainValues=null;
		System.gc();
	}
}
