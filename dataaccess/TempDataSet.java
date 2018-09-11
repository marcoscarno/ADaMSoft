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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import ADaMSoft.keywords.Keywords;
/**
* This class implemnts a random access two-dimensional array of String by using a temporary compressed file stored on disk (in the temporary directory)
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
public class TempDataSet implements Serializable
{
	private int BLOCKSIZE=1048576;
	private static final long serialVersionUID = 1L;
	int totalrecord;
	RandomAccessFile raf;
	ZipOutputStream out=null;
	ObjectOutputStream oos=null;
	ZipEntry entry=null;
	String message;
	ByteArrayOutputStream baos;
	FileChannel fc;
	String tempfilename;
	boolean writingerror;
	ObjectInputStream ois;
	ZipInputStream indata;
	int var;
	int readrecord;
	private String[][] MainValues;
	boolean last;
	private boolean ascending=true;
	String filesorted;
	private ObjectInputStream[] inputStreams;
	private int sortedfiles=0;
	private Vector<RandomAccessFile> files;
	int sortingvar;
	/**
	 * Constructor
	 * @param workdir: Working directory were store the file
	 * @param var: Number of variable, i.e. the number of columns of the array
	 */
	public TempDataSet(String workdir, int var)
	{
		last=false;
		message="";
		this.var=var;
		writingerror=false;
		totalrecord=0;
		readrecord=0;
		tempfilename=workdir+"raf"+Math.random()+".tmp";
		try
		{
			BLOCKSIZE=Integer.parseInt(System.getProperty(Keywords.FileBufferDim));
		}
		catch (Exception exbuf) {}
		try
		{
			raf= new RandomAccessFile(tempfilename,"rw");
			fc = raf.getChannel();
			baos= new ByteArrayOutputStream();
			out = new ZipOutputStream(baos);
			entry = new ZipEntry("Values");
			out.putNextEntry(entry);
			oos = new ObjectOutputStream(out);
		}
		catch (Exception e)
		{
			writingerror=true;
			message="%1799% ("+tempfilename+")<br>\n";
		}
	}
	/**
	*Return true in case of error
	*/
	public boolean geterror()
	{
		return writingerror;
	}
	/**
	*Writes the values
	*/
	public boolean write(String[] values)
	{
		if (writingerror)
			return false;
		try
		{
			totalrecord++;
			for(int i=0;i<values.length;i++)
			{
				if(values[i]==null)
					values[i]="";
				if(baos.size()>BLOCKSIZE)
				{
					oos.flush();
					ByteBuffer bb = ByteBuffer.wrap(baos.toByteArray());
					fc.write(bb);
					baos.reset();
				}
				oos.writeObject(values[i].trim());
			}
			oos.reset();
			return true;
		}
		catch (Exception e)
		{
			try
			{
				oos.close();
			}
			catch (Exception ed) {}
			message="%1800%<br>\n";
			writingerror=true;
			return false;
		}
	}
	/**
	*Return the number of records when a variable represents a record weight
	*/
	public double getweight(int wvar)
	{
		double w=0;
		String[] values=new String[var];
		while (!isLast())
		{
			values=read();
			try
			{
				double retval=Double.parseDouble(values[wvar]);
				if ( (!Double.isNaN(retval)) || (!Double.isInfinite(retval)) )
					w=w+retval;
			}
			catch (Exception en){}
		}
		endread();
		return w;
	}
	/**
	*Return the number of records when a variable represents a record weight (under the condition that the variable rifvar is not missing)
	*/
	public double getweight(int wvar, int rifvar)
	{
		double w=0;
		String[] values=new String[var];
		while (!isLast())
		{
			values=read();
			if (!values[rifvar].equals(""))
			{
				try
				{
					double retval=Double.parseDouble(values[wvar]);
					if ( (!Double.isNaN(retval)) || (!Double.isInfinite(retval)) )
						w=w+retval;
				}
				catch (Exception en){}
			}
		}
		endread();
		return w;
	}
	/**
	*Sort the data set according to the variable that is in the sortingvar position
	*/
	@SuppressWarnings("resource")
	public boolean sortdata(int sortingvar)
	{
		sortedfiles=0;
		files= new Vector<RandomAccessFile>();
		this.sortingvar=sortingvar;
		int maxdatasorted=1000;
		try
		{
			maxdatasorted = Integer.parseInt(System.getProperty(Keywords.MaxDataBuffered));
		}
		catch(NumberFormatException  nfe){}
		MainValues=new String[maxdatasorted][var];
		int obsreaded=0;
		while (!isLast())
		{
			String[] values=read();
			MainValues[obsreaded]=values;
			obsreaded++;
			if (obsreaded>=maxdatasorted)
			{
				sortinfiles(0, obsreaded-1);
				obsreaded=0;
				MainValues=null;
				System.gc();
				MainValues= new String[maxdatasorted][var];
			}
		}
		if (obsreaded>0)
		{
			sortinfiles(0,obsreaded-1);
			MainValues=null;
			System.gc();
		}
		endread();
		System.gc();

		totalrecord=0;
		readrecord=0;

		try
		{
			try
			{
				fc = (new FileOutputStream(tempfilename)).getChannel();
				baos= new ByteArrayOutputStream();
				out = new ZipOutputStream(baos);
				entry = new ZipEntry("Values");
				out.putNextEntry(entry);
				oos = new ObjectOutputStream(out);
			}
			catch (Exception e)
			{
				message="%1805%<br>\n";
				return false;
			}


	        String[][] currentRecords = new String[sortedfiles][var];
			inputStreams=new ObjectInputStream[sortedfiles];

	        for (int i = 0; i < sortedfiles; i++)
	        {
				RandomAccessFile raf=files.get(i);
				raf.seek(0);
				ZipInputStream zip = new ZipInputStream(Channels.newInputStream(raf.getChannel()));
				while(!zip.getNextEntry().getName().equalsIgnoreCase("Values"));
				inputStreams[i]= new ObjectInputStream(zip);
				for(int j=0;j<var;j++)
				{
					currentRecords[i][j]=inputStreams[i].readObject().toString();
				}
	        }
	        String[] r=null;
	        while ((r = getMinimum(currentRecords)) != null)
	        {
				write(r);
        	}
	        for (int i = 0; i < currentRecords.length; i++)
	        {
				String filetemp=tempfilename+".tempsort"+String.valueOf(i);
				File filedel=new File(filetemp);
				filedel.delete();
			}
	        finalizeWrite();

		}
		catch (Exception e)
		{
			try
			{
		        for (int i = 0; i < inputStreams.length; i++)
		        {
	            	inputStreams[i].close();
	        	}
		        finalizeWrite();
			}
			catch (Exception ef) {}
			message="%1805%<br>\n";
			return false;
		}
		return true;
	}
	/**
	*Returns the name of the file
	*/
	public String getfilename()
	{
		return tempfilename;
	}
	/**
	*Returns the number of rows that were written
	*/
	public int getRows()
	{
		return totalrecord;
	}
	/**
	*Returns the number of columns that were written
	*/
	public int getColumns()
	{
		return var;
	}
	/**
	 * This method return a message if an error occurs
	 * @return The (eventual) error message
	 */
	public String getMessage()
	{
		return message;
	}
	/**
	 * This method close the stream after all the records were written
	 * @return True if the operation is completed successfully, false otherwise
	 */
	public boolean finalizeWrite()
	{
		if (writingerror)
		{
			message="%1800%<br>\n";
			try
			{
				out.closeEntry();
				baos.close();
				fc.close();
			}
			catch (Exception e) {}
			return false;
		}
		try
		{
			oos.flush();
			out.closeEntry();
			out.finish();
			out.flush();
			ByteBuffer bb = ByteBuffer.wrap(baos.toByteArray());
			fc.write(bb);
			if (totalrecord==0)
			{
				message="%1801%<br>\n";
				return false;
			}
			fc.close();
			entry=null;
			System.gc();
			return true;
		}
		catch (Exception e)
		{
			message="%1803%<br>\n";
		}
		return false;
	}
	/**
	*Opens the temporary data set
	*/
	private void opentemp()
	{
		try
		{
			indata= new ZipInputStream(new FileInputStream(tempfilename));
			while (!indata.getNextEntry().getName().equalsIgnoreCase("Values"));
			ois= new ObjectInputStream(indata);
			readrecord=0;
		}
		catch (Exception e)
		{
			message="%1802%<br>\n";
		}
	}
	/**
	 * Append an array to the file, the array must have the same elements of variable var
	 * @param values: The array to append
	 * @return True if the operation is completed successfully, false otherwise
	 */
	public String[] read()
	{
		if (ois==null)
			opentemp();

		String[] temprecord=new String[var];
		for (int i=0; i<var; i++)
		{
			temprecord[i]="";
		}
		if (readrecord<totalrecord)
		{
			try
			{
				for (int i=0; i<var; i++)
				{
					temprecord[i]=(ois.readObject()).toString();
				}
			}
			catch (Exception e){}
		}
		if (readrecord==totalrecord-1)
			last=true;
		readrecord++;
		return temprecord;
	}
	/**
	*Return true if this is the ast record of the data set
	*/
	public boolean isLast()
	{
		return last;
	}
	/**
	*Close the file and returns false in case of error
	*/
	public boolean deletetempdata()
	{
		try
		{
			(new File(tempfilename)).delete();
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	/**
	*Close the file and returns false in case of error
	*/
	public void endread()
	{
		try
		{
			ois.close();
			indata.close();
			ois=null;
			last=false;
		}
		catch (Exception e) {}
	}
	/**
	*Sort values and write them in a temporary file
	*/
	private int sortinfiles(int from, int to)
	{
		int resultsortexe=0;
		executesort(from, to);
		String filetemp=tempfilename+".tempsort"+String.valueOf(sortedfiles);
		try
		{
			RandomAccessFile raf = new RandomAccessFile(filetemp,"rw");
			files.add(raf);
			ZipOutputStream	out = new ZipOutputStream(Channels.newOutputStream(raf.getChannel()));
			ZipEntry entry = new ZipEntry("Values");
			out.putNextEntry(entry);
			ObjectOutputStream oos = new ObjectOutputStream(out);
			for (int i=0;i<MainValues.length; i++)
			{
				if (MainValues[i]!=null && MainValues[i][0]!=null)
				{
					String [] values=MainValues[i];
					for (int j=0; j<values.length; j++)
					{
						oos.writeObject(values[j]);
					}
					oos.flush();
					oos.reset();
				}
			}
			out.closeEntry();
			out.finish();
		}
		catch (Exception e)
		{
			resultsortexe=1;
		}
		sortedfiles++;
		return resultsortexe;
	}
	/**
	*Used to get the minumum value from each part (sorted) to which the data set was splitted
	*/
    private String[] getMinimum(String[][] records)
    {
        String[] min = null;
        String[] result = null;
        int minindex = 0;
        for (int i = 0; i < records.length; i++)
        {
            if ((min == null) && (records[i] != null))
            {
				String [] temp=records[i];
				min=temp;
				minindex = i;
			}
			else if ((min != null) && (records[i] != null))
			{
				if ((ascending) && (compare(min, records[i]) > 0))
				{
					String [] temp=records[i];
					min=temp;
					minindex = i;
				}
				if ((!ascending) && (compare(min, records[i]) < 0))
				{
					String [] temp=records[i];
					min=temp;
					minindex = i;
				}
            }
        }
		String[] tempres=records[minindex];
		result=tempres;
        if (min != null)
        {
            try
            {
				String[] tmp = new String[var];
					for(int i=0; i<tmp.length;i++)
					{
						Object obj = inputStreams[minindex].readObject();
						if(obj!=null)
						{
							tmp[i]=obj.toString();
						}
						else
						{
							tmp[i]=null;
						}
					}
					records[minindex]=tmp;
				}
				catch (Exception e)
				{
					records[minindex] = null;
					try
					{
						inputStreams[minindex].close();
						inputStreams[minindex]=null;
					} catch (IOException e1) {}
				}
        }
        return result;
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
		int compareresult=0;
		compareresult=0;
		double anum=Double.NaN;
		double bnum=Double.NaN;
		int arenum=0;
		try
		{
			anum=Double.parseDouble(avalues[sortingvar]);
			arenum++;
		}
		catch (Exception nonnumber) {}
		try
		{
			bnum=Double.parseDouble(bvalues[sortingvar]);
			arenum++;
		}
		catch (Exception nonnumber) {}
		if (arenum!=2)
		{
			if (((avalues[sortingvar].trim()).equals("")) && (!(bvalues[sortingvar].trim()).equals("")))
				compareresult=-1;
			else if ((!(avalues[sortingvar].trim()).equals("")) && ((bvalues[sortingvar].trim()).equals("")))
				compareresult=1;
			else
				compareresult=avalues[sortingvar].compareTo(bvalues[sortingvar]);
			if (compareresult<=-1)
				compareresult=-1;
			if (compareresult>=1)
				compareresult=1;
		}
		else
		{
			if ((Double.isNaN(anum)) && (!Double.isNaN(bnum)))
				compareresult=-1;
			else if ((!Double.isNaN(anum)) && (Double.isNaN(bnum)))
				compareresult=1;
			else if (anum<bnum)
				compareresult=-1;
			else if (anum>bnum)
				compareresult=1;
		}
		return compareresult;
	}
}
