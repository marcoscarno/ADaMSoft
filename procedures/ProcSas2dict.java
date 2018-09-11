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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.ByteOrder;
import java.util.Hashtable;
import java.util.LinkedList;
import ADaMSoft.utilities.StepUtilities;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.GetRequiredParameters;


/**
* This procedure creates a dictionary for a SAS data set
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcSas2dict implements RunStep
{
	private byte[] SUBH_ROWSIZE;
	private byte[] SUBH_COLSIZE;
	private byte[] SUBH_COLTEXT;
	private byte[] SUBH_COLATTR;
	private byte[] SUBH_COLNAME;
	private byte[] SUBH_COLLABS;
	private byte[] MAGIC;
	@SuppressWarnings("unused")
	private File sas_file;
	private String errormsg;
	private boolean read_error;
	private String CHARSET_NAME = "windows-1252";
	@SuppressWarnings("unused")
	private String sasRelease;
	@SuppressWarnings("unused")
	private String sasHost;
	private int pageSize;
	private int pageCount;
	private List<SasSubHeader> subHeaders;
	private List<Integer> columnOffsets;
	private List<Integer> columnLengths;
	private FileInputStream is = null;
	private Vector<String> columnTypes;
	private Vector<String> columnLabels;
	private Vector<String> columnNames;
	@SuppressWarnings("unused")
	private int rowCount = 0;
	@SuppressWarnings("unused")
	private int row_count = -1;
	@SuppressWarnings("unused")
	private int row_count_fp = -1;
	@SuppressWarnings("unused")
	private int row_length = -1;
	private int col_count = -1;
	private int pageNumber;
	/**
	* Creates a dictionary for a delimited text file
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.sasdataset, Keywords.outdict};
		String [] optionalparameters=new String[] {};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}
		String sasdataset    = (String) parameters.get(Keywords.sasdataset);
		String path       = (String) parameters.get(Keywords.outdict);
		errormsg="";
		read_error=false;
		pageNumber=0;
		columnTypes=new Vector<String>();
		columnLabels=new Vector<String>();
		columnNames=new Vector<String>();
		SUBH_ROWSIZE = toBytes(0xf7, 0xf7, 0xf7, 0xf7);
		SUBH_COLSIZE = toBytes(0xf6, 0xf6, 0xf6, 0xf6);
		SUBH_COLTEXT = toBytes(0xFD, 0xFF, 0xFF, 0xFF);
		SUBH_COLATTR = toBytes(0xFC, 0xFF, 0xFF, 0xFF);
		SUBH_COLNAME = toBytes(0xFF, 0xFF, 0xFF, 0xFF);
		SUBH_COLLABS = toBytes(0xFE, 0xFB, 0xFF, 0xFF);
		MAGIC = toBytes(0x0, 0x0, 0x0, 0x0, 0x0,
		0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0xc2, 0xea, 0x81, 0x60, 0xb3,
		0x14, 0x11, 0xcf, 0xbd, 0x92, 0x8, 0x0, 0x9, 0xc7, 0x31, 0x8c,
		0x18, 0x1f, 0x10, 0x11);
		try
		{
			is = new FileInputStream(sasdataset);
			readHeader(is);
			if (read_error) return new Result(errormsg, false, null);
			readVarInfo();
			if (read_error) return new Result(errormsg, false, null);
		}
		catch (Exception e)
		{
			if (read_error) return new Result("%3066% ("+sasdataset+")<br>\n"+e.toString()+"<br>\n", false, null);
			read_error=true;
			errormsg=e.toString();
		}
		finally
		{
			if (is != null)
			{
				try
				{
					is.close();
				}
				catch (IOException e) {}
			}
		}
		String keyword=sasdataset;
		String description=sasdataset;
		String author=(String) parameters.get(Keywords.client_host.toLowerCase());
		String datatabletype=Keywords.sas;
		Hashtable<String, String> datatableinfo=new Hashtable<String, String>();
		datatableinfo.put(Keywords.DATA.toLowerCase(), sasdataset);
		Vector<Hashtable<String, String>> fixedvariableinfo=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> tablevariableinfo=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> codelabel=new Vector<Hashtable<String, String>>();
		Vector<Hashtable<String, String>> missingdata=new Vector<Hashtable<String, String>>();
		for (int i=0; i<columnNames.size(); i++)
		{
			Hashtable<String, String> tempfixedvariableinfo=new Hashtable<String, String>();
			Hashtable<String, String> temptablevariableinfo=new Hashtable<String, String>();
			Hashtable<String, String> tempcodelabel=new Hashtable<String, String>();
			Hashtable<String, String> tempmissingdata=new Hashtable<String, String>();
			tempfixedvariableinfo.put(Keywords.VariableName.toLowerCase(),columnNames.get(i));
			tempfixedvariableinfo.put(Keywords.LabelOfVariable.toLowerCase(),columnLabels.get(i));
			if (columnTypes.get(i).equals("NUMERIC")) tempfixedvariableinfo.put(Keywords.VariableFormat.toLowerCase(),Keywords.NUMSuffix);
			else tempfixedvariableinfo.put(Keywords.VariableFormat.toLowerCase(),Keywords.TEXTSuffix);
			fixedvariableinfo.add(tempfixedvariableinfo);
			tablevariableinfo.add(temptablevariableinfo);
			codelabel.add(tempcodelabel);
			missingdata.add(tempmissingdata);
		}
		Vector<StepResult> result = new Vector<StepResult>();
		result.add(new LocalDictionaryWriter(path, keyword, description, author, datatabletype,
		datatableinfo, fixedvariableinfo, tablevariableinfo, codelabel, missingdata, null));
		return new Result("", true, result);
	}
	/**
	*Used by the method
	*/
	private byte[] toBytes(int... arr)
	{
		if (arr == null) return null;
		byte[] result = new byte[arr.length];
		for (int i = 0; i < result.length; i++)
		{
			result[i] = (byte) arr[i];
		}
		return result;
	}
	/**
	*Used by the method
	*/
	@SuppressWarnings("unused")
	private boolean isMagicNumber(int[] data)
	{
		return isMagicNumber(toBytes(data));
	}
	/**
	*Used by the method
	*/
	private boolean isMagicNumber(byte[] data)
	{
		return isIdentical(data, MAGIC);
	}
	/**
	*Used by the method
	*/
	private boolean isIdentical(byte[] data, byte[] expected)
	{
		if (data == null)
		{
			return false;
		}
		byte[] comparedBytes;
		if (data.length > expected.length)
		{
			comparedBytes = Arrays.copyOf(data, expected.length);
		}
		else
		{
			comparedBytes = data;
		}
		return Arrays.equals(expected, comparedBytes);
	}
	/**
	*Used by the method in order to read the info on the variables
	*/
	@SuppressWarnings("unused")
	private void readVarInfo()
	{
		subHeaders = new ArrayList<SasSubHeader>();
		columnOffsets = new ArrayList<Integer>();
		columnLengths = new ArrayList<Integer>();
		rowCount = 0;
		row_count = -1;
		row_count_fp = -1;
		row_length = -1;
		col_count = -1;
		try
		{
			for (pageNumber = 0; pageNumber < pageCount; pageNumber++)
			{
				byte[] pageData = new byte[pageSize];
				int read = is.read(pageData);
				if (read == -1)
				{
					break;
				}
				byte pageType = readByte(pageData, 17);
				switch (pageType)
				{
					case 0:
					case 1:
					case 2:
						break;
					case 4:
						break;
					default:
					{
						read_error=true;
						errormsg="%3063%<br>\n";
						return;
					}
				}
				if (pageType == 0 || pageType == 2)
				{
					int subhCount = readInt(pageData, 20);
					for (int subHeaderNumber = 0; subHeaderNumber < subhCount; subHeaderNumber++)
					{
						int base = 24 + subHeaderNumber * 12;
						int offset = readInt(pageData, base);
						int length = readInt(pageData, base + 4);
						if (length > 0)
						{
							byte[] rawData = readBytes(pageData, offset, length);
							byte[] signatureData = readBytes(rawData, 0, 4);
							SasSubHeader subHeader = new SasSubHeader(rawData, signatureData);
							subHeaders.add(subHeader);
						}
					}
				}
				if ((pageType == 1 || pageType == 2))
				{
					SasSubHeader rowSize = getSubHeader(SUBH_ROWSIZE, "ROWSIZE");
					row_length = readInt(rowSize.getRawData(), 20);
					row_count = readInt(rowSize.getRawData(), 24);
					int col_count_7 = readInt(rowSize.getRawData(), 36);
					row_count_fp = readInt(rowSize.getRawData(), 60);
					SasSubHeader colSize = getSubHeader(SUBH_COLSIZE, "COLSIZE");
					int col_count_6 = readInt(colSize.getRawData(), 4);
					col_count = col_count_6;
					SasSubHeader colText = getSubHeader(SUBH_COLTEXT, "COLTEXT");
					List<SasSubHeader> colAttrHeaders = getSubHeaders(SUBH_COLATTR, "COLATTR");
					SasSubHeader colAttr;
					if (colAttrHeaders.isEmpty())
					{
						read_error=true;
						errormsg="%3064%\n";
						return;
					}
					else if (colAttrHeaders.size() == 1)
					{
						colAttr = colAttrHeaders.get(0);
					}
					else
					{
						colAttr = spliceColAttrSubHeaders(colAttrHeaders);
					}
					SasSubHeader colName = getSubHeader(SUBH_COLNAME, "COLNAME");
					List<SasSubHeader> colLabels = getSubHeaders(SUBH_COLLABS, "COLLABS");
					if (!colLabels.isEmpty() && colLabels.size() != col_count)
					{
						read_error=true;
						errormsg="%3065%<br>\n";
						return;
					}
					for (int i = 0; i < col_count; i++)
					{
						int base = 12 + i * 8;
						String columnName;
						if (colName!=null)
						{
							byte amd = readByte(colName.getRawData(), base);
							if (amd == 0)
							{
								int off = readShort(colName.getRawData(), base + 2) + 4;
								int len = readShort(colName.getRawData(), base + 4);
								columnName = readString(colText.getRawData(), off, len);
							}
							else
							{
								columnName = "COL" + i;
							}
						}
						else columnName = "COL" + i;
						String label=null;
						if (colLabels != null && !colLabels.isEmpty())
						{
							base = 42;
							byte[] rawData = colLabels.get(i).getRawData();
							int off = readShort(rawData, base) + 4;
							short len = readShort(rawData, base + 2);
							if (len > 0)
							{
								label = readString(colText.getRawData(), off, len);
							}
							else
							{
								label = null;
							}
						}
						else
						{
							label=null;
						}
						if (label==null) columnLabels.add(columnName);
						else columnLabels.add(label);
						columnNames.add(columnName);
						base = 12 + i * 12;
						int offset = readInt(colAttr.getRawData(), base);
						columnOffsets.add(offset);
						int length = readInt(colAttr.getRawData(), base + 4);
						columnLengths.add(length);
						short columnTypeCode = readShort(colAttr.getRawData(), base + 10);
						if (columnTypeCode==1) columnTypes.add("NUMERIC");
						else columnTypes.add("CHARACTER");
					}
					return;
				}
			}
		}
		catch (Exception gene)
		{
			read_error=true;
			errormsg="%3066%\n"+gene.toString()+"<br>\n";
		}
	}
	/**
	*Used by the method
	*/
	private SasSubHeader spliceColAttrSubHeaders(List<SasSubHeader> colAttrHeaders)
	{
		int colAttrHeadersSize = colAttrHeaders.size();
		byte[] result = readBytes(colAttrHeaders.get(0).getRawData(), 0, colAttrHeaders.get(0).getRawData().length - 8);
		for (int i = 1; i < colAttrHeadersSize; i++)
		{
			byte[] rawData = colAttrHeaders.get(i).getRawData();
			result = concat(result, readBytes(rawData, 12, rawData.length - 20));
		}
		return new SasSubHeader(result, null);
	}
	/**
	*Used by the method
	*/
	private List<SasSubHeader> getSubHeaders(byte[] signature, String name)
	{
		List<SasSubHeader> result = new ArrayList<SasSubHeader>();
		for (SasSubHeader subHeader : subHeaders)
		{
			byte[] signatureData = subHeader.getSignatureData();
			if (isIdentical(signatureData, signature)) result.add(subHeader);
		}
		return result;
	}
	/**
	*Used by the method
	*/
	private SasSubHeader getSubHeader(byte[] signature, String name)
	{
		List<SasSubHeader> result = getSubHeaders(signature, name);
		if (result.isEmpty())
		{
			read_error=true;
			errormsg="%3071%<br>\n";
			return null;
		}
		else if (result.size() != 1)
		{
			read_error=true;
			errormsg="%3071%<br>\n";
			return null;
		}
		return result.get(0);
	}
	/**
	*Used by the method
	*/
	private void readHeader(InputStream is)
	{
		byte[] header = new byte[1024];
		try
		{
			int read = is.read(header);
			if (read != 1024)
			{
				read_error=true;
				errormsg="%3067%<br>\n";
				return;
			}
			if (!isMagicNumber(header))
			{
				read_error=true;
				errormsg="%3068%<br>\n";
				return;
			}
			pageSize = readInt(header, 200);
			if (pageSize < 0)
			{
				read_error=true;
				errormsg="%3069%<br>\n";
			}
			pageCount = readInt(header, 204);
			if (pageCount < 1)
			{
				read_error=true;
				errormsg="%3070%<br>\n";
			}
			sasRelease = readString(header, 216, 8);
			sasHost = readString(header, 224, 8);
		}
		catch (Exception e)
		{
				read_error=true;
				errormsg="%3071%<br>\n";
		}
	}
	/**
	*Used by the method
	*/
	private String readString(byte[] buffer, int off, int len)
	{
		byte[] subset = readBytes(buffer, off, len);
		String str = getString(subset, CHARSET_NAME);
		return str;
	}
	/**
	*Used by the method
	*/
	private String getString(byte[] bytes, String encoding)
	{
		try
		{
			InputStreamReader reader = new InputStreamReader(new ByteArrayInputStream(bytes), encoding);
			char[] chars = new char[bytes.length * 2];
			int read = reader.read(chars);
			chars = Arrays.copyOf(chars, read);
			return new String(chars);
		}
		catch (Exception e)
		{
			read_error=true;
			errormsg=e.toString();
		}
		return null;
	}
	/**
	*Used by the method
	*/
	private byte readByte(byte[] buffer, int off)
	{
		ByteBuffer bb = ByteBuffer.wrap(buffer);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		return bb.get(off);
	}
	/**
	*Used by the method
	*/
	private int readInt(byte[] buffer, int off)
	{
		ByteBuffer bb = ByteBuffer.wrap(buffer);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		return bb.getInt(off);
	}
	/**
	*Used by the method
	*/
	private double readDouble(byte[] buffer, int off)
	{
		ByteBuffer bb = ByteBuffer.wrap(buffer);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		return bb.getDouble(off);
	}
	/**
	*Used by the method
	*/
	private byte[] readBytes(byte[] data, int off, int len)
	{
		if (data.length < off + len)
		{
			errormsg="readBytes failed! data.length: " + data.length + ", off: " + off + ", len: " + len;
			read_error=true;
			return null;
		}
		byte[] subset = new byte[len];
		System.arraycopy(data, off, subset, 0, len);
		return subset;
	}
	/**
	*Used by the method
	*/
	private short readShort(byte[] buffer, int off)
	{
		ByteBuffer bb = ByteBuffer.wrap(buffer);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		return bb.getShort(off);
	}
	/**
	*Used by the method
	*/
	@SuppressWarnings("unused")
	private Number readNumber(byte[] buffer, int off, int len)
	{
		if (len == 1) return readByte(buffer, off);
		else if (len == 2) return readShort(buffer, off);
		else if (len == 4) return readInt(buffer, off);
		else if (len == 8) return readDouble(buffer, off);
		else
		{
			return null;
		}
	}
	/**
	*Used by the method
	*/
	private byte[] concat(byte[] arr1, byte[] arr2)
	{
		byte[] result = new byte[arr1.length + arr2.length];
		System.arraycopy(arr1, 0, result, 0, arr1.length);
		System.arraycopy(arr2, 0, result, arr1.length, arr2.length);
		return result;
	}
	/**
	*Returns the parameter needed by the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.outdict+"=", "outdictreport", true, 249, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.sasdataset, "file=sas7bdat", true, 3073, dep, "", 2));
		return parameters;
	}
	/**
	*Returns the info on the step
	*/
	public String[] getstepinfo()
	{
		String[] retstepinfo=new String[2];
		retstepinfo[0]="247";
		retstepinfo[1]="3072";
		return retstepinfo;
	}
}
class SasSubHeader
{
	private final byte[] _rawData;
	private final byte[] _signatureData;
	public SasSubHeader(byte[] rawData, byte[] signatureData)
	{
		_rawData = rawData;
		_signatureData = signatureData;
	}
	public byte[] getSignatureData()
	{
		return _signatureData;
	}
	public byte[] getRawData()
	{
		return _rawData;
	}
}