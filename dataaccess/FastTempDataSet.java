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
import java.io.Serializable;
import java.net.URLConnection;
import java.util.Vector;

import ADaMSoft.keywords.Keywords;
/**
* This class implemnts a temporary data set for a fast access to its records
* @author marco.scarno@gmail.com
* @date 20/06/2017
*/
public class FastTempDataSet implements Serializable
{
	private static final long serialVersionUID = 1L;
	BufferedOutputStream bisr;
	ObjectOutputStream oos=null;
	int totalrecord;
	String message;
	String tempfilename;
	boolean writingerror;
	ObjectInputStream dis;
	BufferedInputStream bis;
	String[][] MainValues=null;
	String[] tempvalues=null;
	int sortvar=0;
	int numvars, maxdatasorted, charvars;
	Vector<String> sortedfiles;
	String workdir, filesorted, suffixfile;
	private ObjectInputStream[] inputStreams;
	private BufferedInputStream[] binputStreams;
	Object[] currentRecords;
	BufferedInputStream defs;
	ObjectInputStream oiss;
	int stateftds;
	boolean first_sort_num, reverse_order;
	/**
	 * Constructor
	 * @param workdir: Working directory were store the file
	 * @param var: Number of variable, i.e. the number of columns of the array
	 */
	public FastTempDataSet(String workdir)
	{
		first_sort_num=false;
		reverse_order=false;
		stateftds=0;
		this.workdir=workdir;
		message="";
		writingerror=false;
		totalrecord=0;
		tempfilename=workdir+"ftf"+Math.random()+".tmp";
		boolean iswriting=(new File(tempfilename)).exists();
		while (iswriting)
		{
			tempfilename=workdir+"ftf"+Math.random()+".tmp";
			iswriting=(new File(tempfilename)).exists();
		}
		try
		{
			bisr = new BufferedOutputStream(new FileOutputStream(tempfilename));
			oos = new ObjectOutputStream(bisr);
		}
		catch (Exception e)
		{
			writingerror=true;
			message="%2824% ("+tempfilename+")<br>\n";
		}
		sortedfiles=new Vector<String>();
	}
	/**
	*Return true in case of error
	*/
	public boolean geterror()
	{
		return writingerror;
	}
	/**
	*Returns the current message
	*/
	public String getmessage()
	{
		return message;
	}
	/**
	*Writes the values
	*/
	public boolean write(String[] values)
	{
		stateftds=1;
		if (writingerror)
			return false;
		try
		{
			oos.writeObject(values);
			oos.flush();
			bisr.flush();
			oos.reset();
			totalrecord++;
			return true;
		}
		catch (Exception e)
		{
			try
			{
				oos.flush();
				bisr.flush();
				oos.close();
				bisr.close();
				oos=null;
				bisr=null;
			}
			catch (Exception ed) {}
			message="%2825% ("+e.toString()+")<br>\n";
			writingerror=true;
			return false;
		}
	}
	/**
	*Force the close of all the files
	*/
	public void forceClose()
	{
		try
		{
			if (oos!=null)
			{
				oos.flush();
				bisr.flush();
				oos.close();
				bisr.close();
				oos=null;
				bisr=null;
			}
		}
		catch (Exception ed) {}
		try
		{
			if (bis!=null)
			{
				dis.close();
				bis.close();
				dis=null;
				bis=null;
			}
		}
		catch (Exception ed) {}
		(new File(tempfilename)).delete();
	}
	/**
	*Closes the stream that was opened to write the records
	*/
	public boolean endwrite()
	{
		stateftds=2;
		try
		{
			oos.flush();
			oos.close();
			bisr.close();
			oos=null;
			bisr=null;
		}
		catch (Exception e)
		{
			message="%2826% ("+e.toString()+")<br>\n";
			return false;
		}
		return true;
	}
	/**
	*Opens the stream in order to read the records
	*/
	public boolean opentoread()
	{
		stateftds=3;
		try
		{
			File filetemp=new File(tempfilename);
			java.net.URL fileUrl = filetemp.toURI().toURL();
			URLConnection      urlConn;
			urlConn = fileUrl.openConnection();
			urlConn.setDoInput(true);
			urlConn.setUseCaches(false);
			bis = new BufferedInputStream(urlConn.getInputStream());
			dis = new ObjectInputStream(bis);
		}
		catch (Exception e)
		{
			if (dis!=null)
			{
				try
				{
					dis.close();
				}
				catch (Exception ee) {}
			}
			if (bis!=null)
			{
				try
				{
					bis.close();
				}
				catch (Exception ee) {}
			}
			dis=null;
			bis=null;
			message="%2827% ("+e.toString()+")<br>\n";
			return false;
		}
		return true;
	}
	/**
	*Returns the number of records written
	*/
	public int getrecords()
	{
		return totalrecord;
	}
	/**
	*Reads the values from the data set
	*/
	public String[] read()
	{
		stateftds=4;
		try
		{
			String[] tempvalues=(String[])dis.readObject();
			return tempvalues;
		}
		catch (Exception e)
		{
			return null;
		}
	}
	/**
	*Reads the values from the data set and return them as double
	*/
	public double[] readdouble()
	{
		stateftds=5;
		try
		{
			String[] tempvalues=(String[])dis.readObject();
			double[] tempdval=new double[tempvalues.length];
			for (int i=0; i<tempvalues.length; i++)
			{
				tempdval[i]=Double.NaN;
				try
				{
					tempdval[i]=Double.parseDouble(tempvalues[i]);
				}
				catch (Exception co) {}
			}
			return tempdval;
		}
		catch (Exception e)
		{
			return null;
		}
	}
	/**
	*Closes the stream that was opened in order to read the records
	*/
	public boolean endread()
	{
		stateftds=6;
		try
		{
			if (dis!=null) dis.close();
			if (bis!=null) bis.close();
			dis=null;
			bis=null;
		}
		catch (Exception e)
		{
			message="%2844% ("+e.toString()+")<br>\n";
			return false;
		}
		return true;
	}
	/**
	*Closes the file opened for reading and deletes it and returns false in case of error
	*/
	public boolean deletefile()
	{
		try
		{
			if (dis!=null) dis.close();
			if (bis!=null) bis.close();
			dis=null;
			bis=null;
			(new File(tempfilename)).delete();
		}
		catch (Exception e)
		{
			message="%2828% ("+e.toString()+")<br>\n";
			return false;
		}
		return true;
	}
	/**
	*Sorts the data file
	*/
	public boolean sortwith(int numvars, int charvars, int totalvar)
	{
		java.util.Date dateProcedure=new java.util.Date();
		long timeProcedure=dateProcedure.getTime();
		double addfn=Math.random()*100000;
		int addfni=(int)addfn;
		suffixfile=workdir+"File"+String.valueOf(timeProcedure)+String.valueOf(addfni);
		boolean iswriting=(new File(suffixfile)).exists();
		while (iswriting)
		{
			timeProcedure=dateProcedure.getTime();
			addfn=Math.random()*100000;
			addfni=(int)addfn;
			suffixfile=workdir+"File"+String.valueOf(timeProcedure)+String.valueOf(addfni);
			iswriting=(new File(suffixfile)).exists();
		}
		this.numvars=numvars;
		this.charvars=charvars;
		sortvar=numvars+charvars;
		try
		{
			maxdatasorted = Integer.parseInt(System.getProperty(Keywords.MaxDataBuffered));
		}
		catch(NumberFormatException  nfe){}
		MainValues=new String[maxdatasorted][totalvar];
		if (!opentoread()) return false;
		int obsreaded=0;
		int currentobs=0;
		while (currentobs<totalrecord)
		{
			tempvalues=read();
			if (tempvalues!=null)
			{
				MainValues[obsreaded]=tempvalues;
				obsreaded++;
				if (obsreaded>=maxdatasorted)
				{
					int resultsortexe=fastsort(0, obsreaded-1);
					MainValues=new String[0][0];
					MainValues=null;
					if (resultsortexe!=0)
					{
						message="%515%<br>\n";
						return false;
					}
					obsreaded=0;
					MainValues= new String[maxdatasorted][totalvar];
				}
			}
			currentobs++;
		}
		if (obsreaded>0)
		{
			int resultsortexe=fastsort(0,obsreaded-1);
			MainValues=new String[0][0];
			MainValues=null;
			if (resultsortexe!=0)
			{
				message="%515%<br>\n";
				return false;
			}
			obsreaded=0;
		}
		endread();
		filesorted=suffixfile+".sorted";
        BufferedOutputStream out=null;
        ObjectOutputStream oos=null;
		try
		{
			out = new BufferedOutputStream(new FileOutputStream(filesorted));
			oos = new ObjectOutputStream(out);
	        currentRecords = new Object[sortedfiles.size()];
			inputStreams = new ObjectInputStream[sortedfiles.size()];
			binputStreams = new BufferedInputStream[sortedfiles.size()];
	        for (int i = 0; i < sortedfiles.size(); i++)
	        {
				File filetemp=new File(sortedfiles.get(i));
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
	        while ((r = getMinimumFromPart()) != null)
	        {
	        	written++;
				oos.writeObject(r);
				if(written>totalrecord)
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
					File filedel=new File(sortedfiles.get(i));
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
						File filedel=new File(sortedfiles.get(i));
						filedel.delete();
					}
					catch (Exception eee) {}
	        	}
	        	oos.flush();
				oos.close();
				out.close();
			}
			catch (Exception ef) {}
			message="%516%\n";
			return false;
		}
		return true;
	}
	 /**
    *Sort values and write them in a temporary file
    */
	private int fastsort(int from, int to)
	{
		executememsort(from, to);
		String filetemp=suffixfile+String.valueOf(sortedfiles.size())+".tempsort";
		try
		{
			sortedfiles.add(filetemp);
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
			return 1;
		}
		return 0;
	}
	/**
	*Sorts the values
	*/
	private void executememsort(int from, int to)
	{
		if (MainValues == null) return;
		if (to<2) return;
		int i = from, j = to;
		Object center = MainValues[(from+to)/2];
		do
		{
			while( (i < to) && (intcompare(center, MainValues[i]) < 0) )
				i++;
			while( (j > from) && (intcompare(center, MainValues[j]) > 0) )
				j--;
			if (i <= j)
			{
				String[] temp = MainValues[i];
				MainValues[i] = MainValues[j];
				MainValues[j] = temp;
				i++;
				j--;
			}
		}
		while(i <= j);
		if (from < j) executememsort(from, j);
		if (i < to) executememsort(i, to);
	}
	public void reverse_num()
	{
		reverse_order=true;
	}
	public void first_sort_num()
	{
		first_sort_num=true;
	}
	/**
	*Define the rule for the comparing alghoritm, by considering the variables type
	*/
	private int intcompare(Object a, Object b)
	{
		String [] avalues=(String[])a;
		String [] bvalues=(String[])b;
		if (!first_sort_num)
		{
			for (int k=0; k<charvars; k++)
			{
				if (((avalues[k+numvars].trim()).equals("")) && (!(bvalues[k+numvars].trim()).equals("")))
					return 1;
				else if ((!(avalues[k+numvars].trim()).equals("")) && ((bvalues[k+numvars].trim()).equals("")))
					return -1;
				else
				{
					if (avalues[k+numvars].compareTo(bvalues[k+numvars])<0) return 1;
					if (avalues[k+numvars].compareTo(bvalues[k+numvars])>0) return -1;
				}
			}
			for (int k=0; k<numvars; k++)
			{
				double anum=Double.NaN;
				double bnum=Double.NaN;
				try
				{
					anum=Double.parseDouble(avalues[k]);
				}
				catch (Exception nonnumber) {}
				try
				{
					bnum=Double.parseDouble(bvalues[k]);
				}
				catch (Exception nonnumber) {}
				if ((Double.isNaN(anum)) && (!Double.isNaN(bnum)))
					return -1;
				else if ((!Double.isNaN(anum)) && (Double.isNaN(bnum)))
					return 1;
				else if (anum<bnum)
					return -1;
				else if (anum>bnum)
					return 1;
			}
			return 0;
		}
		else
		{
			if (!reverse_order)
			{
				for (int k=0; k<numvars; k++)
				{
					double anum=Double.NaN;
					double bnum=Double.NaN;
					try
					{
						anum=Double.parseDouble(avalues[k+charvars]);
					}
					catch (Exception nonnumber) {}
					try
					{
						bnum=Double.parseDouble(bvalues[k+charvars]);
					}
					catch (Exception nonnumber) {}
					if ((Double.isNaN(anum)) && (!Double.isNaN(bnum)))
						return 1;
					else if ((!Double.isNaN(anum)) && (Double.isNaN(bnum)))
						return -1;
					else if (anum<bnum)
						return 1;
					else if (anum>bnum)
						return -1;
				}
			}
			else
			{
				for (int k=0; k<numvars; k++)
				{
					double anum=Double.NaN;
					double bnum=Double.NaN;
					try
					{
						anum=Double.parseDouble(avalues[k+charvars]);
					}
					catch (Exception nonnumber) {}
					try
					{
						bnum=Double.parseDouble(bvalues[k+charvars]);
					}
					catch (Exception nonnumber) {}
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
			for (int k=0; k<charvars; k++)
			{
				if (((avalues[k].trim()).equals("")) && (!(bvalues[k].trim()).equals("")))
					return 1;
				else if ((!(avalues[k].trim()).equals("")) && ((bvalues[k].trim()).equals("")))
					return -1;
				else
				{
					if (avalues[k].compareTo(bvalues[k])<0) return 1;
					if (avalues[k].compareTo(bvalues[k])>0) return -1;
				}
			}
			return 0;
		}
	}
	/*
	*Used to get the minumum value from each part (sorted) to which the data set was splitted
	*/
    private Object[] getMinimumFromPart()
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
				if (intcompare(min, currentRecords[i]) < 0)
				{
					min=currentRecords[i];
					minindex = i;
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
	*Open the final sorted file
	*/
	public boolean openSortedFile()
	{
		try
		{
			File filetemp = new File(filesorted);
			java.net.URL fileUrl = filetemp.toURI().toURL();
			URLConnection      urlConn;
			urlConn = fileUrl.openConnection();
			urlConn.setDoInput(true);
			urlConn.setUseCaches(true);
			defs = new BufferedInputStream(urlConn.getInputStream());
			oiss = new ObjectInputStream(defs);
			return true;
		}
		catch (Exception e)
		{
			message="%518%<br>\n";
			return false;
		}
	}
	/**
	*Reads and return the final sorted record
	*/
	public Object[] readSortedRecord()
	{
		Object[] retrecord=null;
		try
		{
			if (defs.available()!=0)
			{
				retrecord=(Object[])oiss.readObject();
			}
		}
		catch (Exception e)
		{
			message="%519%<br>\n";
			return null;
		}
		return retrecord;
	}
	/**
	*Close and delete the final sorted file
	*/
	public void closeSortedFile()
	{
		try
		{
			oiss.close();
			defs.close();
			File filezip = new File(filesorted);
			filezip.delete();
		}
		catch (Exception e) {}
		MainValues=new String[0][0];
		MainValues=null;
	}
	/**
	*Deletes the intermediate files incase of errors
	*/
	public void deleteIntermediateFiles()
	{
		if (inputStreams!=null)
		{
			if (inputStreams.length>0)
			{
		        for (int i = 0; i < inputStreams.length; i++)
		        {
					try
					{
						inputStreams[i].close();
						binputStreams[i].close();
						File filedel=new File(sortedfiles.get(i));
						filedel.delete();
					}
					catch (Exception eee) {}
	        	}
			}
		}
		if (oiss!=null)
		{
			try
			{
				oiss.close();
				defs.close();
			}
			catch (Exception e) {}
		}
		try
		{
			File filezip = new File(filesorted);
			filezip.delete();
		}
		catch (Exception e) {}
		MainValues=new String[0][0];
		MainValues=null;
	}
}
