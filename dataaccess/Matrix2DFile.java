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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import ADaMSoft.keywords.Keywords;
/**
* This class implements a random access two-dimensional array by using a temporary file stored on disk (NIO Mapped file implementation)
* @author marco.scarno@gmail.com
* @date 03/09/2015
*/
public class Matrix2DFile implements Serializable, Matrix2D
{
	private static final int DOUBLESIZE=8;
	//number of double contained in the buffer
	private int BLOCKSIZE=1048576;
	private static final long serialVersionUID = 1L;
	protected String workdir;
	protected String message;
	protected String file;
	protected int columnsCount;
	private FileChannel fc;
	private ByteBuffer bb;
	private long bufferLowerBound=0;
	private long bufferUpperBound=0;
	private int rows;
	private int records;
	private RandomAccessFile raf;
	boolean toread=false;
	double value;
	/**
	 * Constructor
	 * @param workdir: Working directory were store the file
	 * @param var: Number of variable, i.e. the number of columns of the array
	 */
	public Matrix2DFile(String workdir, int var)
	{
		toread=false;
		try
		{
			BLOCKSIZE=Integer.parseInt(System.getProperty(Keywords.FileBufferDim));
		}
		catch (Exception exbuf) {}
		columnsCount=var;
		this.workdir=workdir;
		message="";
		rows=0;
		file=workdir+"raf"+Math.random()+".tmp";
		try
		{
			raf = new RandomAccessFile(file,"rw");
			fc = raf.getChannel();
			bufferUpperBound=BLOCKSIZE*DOUBLESIZE;
			bb = fc.map(FileChannel.MapMode.READ_WRITE, bufferLowerBound, BLOCKSIZE*DOUBLESIZE);
		}
		catch (FileNotFoundException e)
		{
			message= "%934%<br>\n";
		}
		catch (IOException e)
		{
			message= "%934%<br>\n";
		}
	}
	/**
	*
	*/
	public String getfilename()
	{
		return file;
	}
	/**
	*Returns the number of rows that were written
	*/
	public int getRows()
	{
		return records;
	}
	/**
	*Returns the number of columns that were written
	*/
	public int getColumns()
	{
		return columnsCount;
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
	 * This method close the stream and cancel the temporary file
	 * @return True if the operation is completed successfully, false otherwise
	 */
	public boolean close()
	{
		try
		{
			fc.force(true);
			bb.flip();
			fc.write(bb);
			raf.seek(0);
			fc.close();
			raf.close();
			bb=null;
			fc=null;
			raf=null;
			System.gc();
			raf = new RandomAccessFile(file,"rw");
			raf.close();
			raf=null;
			System.gc();
			File f = new File(file);
			int trydel=0;
			if(f.exists())
			{
				boolean filedel=f.delete();
				while ((!filedel) && (trydel<10))
				{
					filedel=f.delete();
					trydel++;
				}
				return filedel;
			}
			return true;
		}
		catch (Exception e)
		{
			message="%933%<br>\n";
			return false;
		}
	}
	/**
	 * Append an array to the file, the array must have the same elements of variable var
	 * @param values: The array to append
	 * @return True if the operation is completed successfully, false otherwise
	 */
	public boolean write(double[] values)
	{
		toread=false;
		if(values.length!=columnsCount)
		{
			message="%935%<br>\n";
			return false;
		}
		for(int i=0;i<values.length;i++)
		{
			write(values[i],rows, i);
		}
		rows++;
		records=rows;
		return true;
	}
	/**
	 * This method write a value into the array, but control that the indicies are
	 * compliant whith the current dimention of array
	 * @param value: The value to write
	 * @param column: The column where wrte the value
	 * @param row: The row were write the value
	 * @return True if the operation is completed successfully, false otherwise
	 */
	public boolean safewrite(double value, int row, int column)
	{
		toread=false;
		if(column>columnsCount || row>rows)
		{
			message="%937%<br>\n";
			return false;
		}
		write(value,row, column);
		return true;
	}
	/**
	 * This method write a value into the array, but do not control that the indicies are
	 * compliant whith the current dimention of array
	 * @param value: The value to write
	 * @param column: The column where wrte the value
	 * @param row: The row were write the value
	 * @return True if the operation is completed successfully, false otherwise
	 */
	public boolean write(double value, int row, int column)
	{
		toread=false;
		setBuffer(row, column);
		int bufferPos=(int)(getPhisicalAddress(row, column)-bufferLowerBound);
		bb.putDouble(bufferPos,value);
		if(bufferPos>bb.limit())
		{
			bb.limit(bufferPos);
		}
		if(row>rows)
			rows=row;
		if (row>=records)
			records=row+1;
		return true;
	}
	/**This manage the file buffer
	 * @param row: The row cordinate
	 * @param column: The column coordinate
	 */
	private void setBuffer(int row, int column)
	{
		long offset = getPhisicalAddress(row, column);
		if(!((offset<bufferUpperBound) && (offset>=bufferLowerBound)))
		{
			if (!toread)
			{
				try
				{
					fc.force(true);
					bb.flip();
					fc.write(bb);
					fc.force(true);
					bb=null;
					System.gc();
				}
				catch (IOException e){}
			}
			bufferLowerBound=offset;
			bufferUpperBound=offset+BLOCKSIZE*DOUBLESIZE;
			try
			{
				bb = fc.map(FileChannel.MapMode.READ_WRITE, bufferLowerBound, BLOCKSIZE*DOUBLESIZE);
			}
			catch (IOException e){}
		}
	}
	/**
	 * This method transform the coordinates row/column in a physical file address
	 * @param row: The row cordinate
	 * @param column: The column coordinate
	 * @return The phisical address of the element at (row;column)
	 */
	private long getPhisicalAddress(int row, int column){

		long offset = (columnsCount)*(row)+column+1;
		return offset*DOUBLESIZE;
	}
	/**
	 * This method write a row at specified index, but control that the indicies are
	 * compliant whith the current dimention of array
	 * @param values: The values to write
	 * @param index: The index where write the values
	 * @return True if the operation is completed successfully, false otherwise
	 */
	public boolean safewrite(double[] values, int index)
	{
		toread=false;
		if(index>rows)
		{
			message="%938%<br>\n";
			return false;
		}
		for(int i=0;i<values.length;i++)
		{
			write(values[i],index,i);
		}
		return true;
	}
	/**
	 * This method write a row at specified index, but it does not control that the indicies are
	 * compliant whith the current dimention of array
	 * @param values: The values to write
	 * @param index: The index where write the values
	 * @return True if the operation is completed successfully, false otherwise
	 */
	public boolean write(double[] values, int index)
	{
		toread=false;
		for(int i=0;i<values.length;i++)
		{
			write(values[i],index,i);
		}
		if(index>rows)
		{
			rows=index;
			records=rows+1;
		}
		return true;
	}
	/**
	 * This method read a values at the specified indices
	 * @param column: The column where to read the value
	 * @param row: The row where to read the value
	 * @return The value or NaN if an error occurs
	 */
	public double read(int row, int column)
	{
		setBuffer(row, column);
		int bufferPos=(int)(getPhisicalAddress(row, column)-bufferLowerBound);
		value=bb.getDouble(bufferPos);
		toread=true;
		return value;
	}
	/**
	 * This method read a row of values at the specified indices
	 * @param row: The row were read the value
	 * @return The value read or NaN if an error occurs
	 */
	public double[] readRow(int row)
	{
		double[] buffer = new double[columnsCount];
		for(int i=0;i<columnsCount;i++)
		{
			buffer[i] = read(row,i);
		}
		toread=true;
		return buffer;
	}
}
