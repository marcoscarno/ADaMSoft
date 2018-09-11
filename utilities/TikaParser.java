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

import java.io.File;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.net.URLConnection;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

import java.util.Vector;
import java.lang.StringBuilder;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.language.LanguageIdentifier;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.Tika;

/**
* This class parse a document by using Tika
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/
public class TikaParser
{
	String msgfilep;
	String msgLang;
	String msgErrorParsing;
	boolean errorParsing;
	boolean cases;
	Vector<Integer> charstodelete;
	Vector<Integer> charstosubwspace;
	String[] charstoreplace;
	String content;
	String newcontent;
	String othermsg;
	boolean replacenewlines;
	boolean identifynewsentences;
	boolean onlyascii;
	boolean nonumbers;
	int tempint_char=0;
	int testbyte;
	InputStream input;
	String workingdir;
	int timeout;
	boolean loadedfromlocal;
	public TikaParser ()
	{
		workingdir="";
		timeout=2000;
		msgfilep="";
		msgLang="";
		cases=false;
		msgErrorParsing="";
		errorParsing=false;
		charstodelete=null;
		charstosubwspace=null;
		content="";
		newcontent="";
		othermsg="";
		charstoreplace=null;
		replacenewlines=false;
		identifynewsentences=false;
		onlyascii=false;
		nonumbers=false;
		charstodelete=new Vector<Integer>();
		charstosubwspace=new Vector<Integer>();
		charstoreplace=null;
		input=null;
		loadedfromlocal=false;
	}
	public void setOnlyascii(boolean onlyascii)
	{
		this.onlyascii=onlyascii;
	}
	public void setNonumbers(boolean nonumbers)
	{
		this.nonumbers=nonumbers;
	}
	public void setCases(boolean cases)
	{
		this.cases=cases;
	}
	public void setReplacenewlines(boolean replacenewlines)
	{
		this.replacenewlines=replacenewlines;
	}
	public void setIdentifynewsentences(boolean identifynewsentences)
	{
		this.identifynewsentences=identifynewsentences;
	}
	public void setCharstodelete(String[] tcharstodelete)
	{
		charstodelete=new Vector<Integer>();
		if (tcharstodelete!=null)
		{
			for (int i=0; i<tcharstodelete.length; i++)
			{
				try
				{
					tempint_char=Integer.parseInt(tcharstodelete[i]);
					if (!charstodelete.contains(new Integer(tempint_char)))
						charstodelete.add(new Integer(tempint_char));
				}
				catch (Exception ec){}
			}
		}
	}
	public void setCharstosubwspace(String[] tcharstosubwspace)
	{
		charstosubwspace=new Vector<Integer>();
		if (tcharstosubwspace!=null)
		{
			for (int i=0; i<tcharstosubwspace.length; i++)
			{
				try
				{
					tempint_char=Integer.parseInt(tcharstosubwspace[i]);
					if (!charstosubwspace.contains(new Integer(tempint_char)))
						charstosubwspace.add(new Integer(tempint_char));
				}
				catch (Exception ec) {}
			}
		}
	}
	public void setCharstoreplace(String[] charstoreplace)
	{
		this.charstoreplace=charstoreplace;
	}
	public String getContent()
	{
		return newcontent;
	}
	public String getMsgFileP()
	{
		return msgfilep;
	}
	public String getMsgLang()
	{
		return msgLang;
	}
	public String getMsgErrorParsing()
	{
		return msgErrorParsing;
	}
	public boolean getErrorParsing()
	{
		return errorParsing;
	}
	public String getOtherMsg()
	{
		return othermsg;
	}
	public void setWorkindDir(String workingdir)
	{
		this.workingdir=workingdir;
	}
	public void setTimeout(int timeout)
	{
		this.timeout=timeout;
	}
	public void parseFile(String filename)
	{
		newcontent="";
		loadedfromlocal=false;
		String localfname="";
		errorParsing=false;
		msgErrorParsing="";
		if((filename.toLowerCase()).startsWith("http") && !workingdir.equals(""))
		{
			URLConnection conn=null;
			double tdoc_size=Double.NaN;
			java.net.URL fileUrl;
			try
			{
				fileUrl =  new java.net.URL(filename);
				conn = fileUrl.openConnection();
				conn.setConnectTimeout(timeout);
				conn.setReadTimeout(timeout);
				conn.connect();
				tdoc_size = (double)conn.getContentLength();
			}
			catch (Exception econn)
			{
				errorParsing=true;
				msgErrorParsing="%3332%\n"+econn.toString()+"<br>\n";
				return;
			}
			if (Double.isNaN(tdoc_size))
			{
				errorParsing=true;
				msgErrorParsing="%3332%<br>\n";
				return;
			}
			byte[] buffer = new byte[1024];
			int bytesRead;
			int totbytes=0;
			localfname=workingdir+"TempHttpFile";
			(new File(localfname)).delete();
			try
			{
				BufferedInputStream inputStream = new BufferedInputStream(conn.getInputStream());
				File tempf = new File(localfname);
				BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempf));
				while ((bytesRead = inputStream.read(buffer)) != -1)
				{
					totbytes+=bytesRead;
					outputStream.write(buffer, 0, bytesRead);
				}
				inputStream.close();
				outputStream.close();
			}
			catch (Exception econn)
			{
				errorParsing=true;
				msgErrorParsing="%3332%\n"+econn.toString()+"<br>\n";
				return;
			}
			if (totbytes<2)
			{
				errorParsing=true;
				msgErrorParsing="%3332%<br>\n";
				return;
			}
			loadedfromlocal=true;
			filename=localfname;
		}
		content="";
		newcontent="";
		msgfilep="";
		msgLang="";
		content="";
		othermsg="";
		input=null;
		try
		{
			java.net.URL fileUrl;
			if((filename.toLowerCase()).startsWith("http"))
				fileUrl =  new java.net.URL(filename);
			else
			{
				File file=new File(filename);
				fileUrl = file.toURI().toURL();
			}
			input = fileUrl.openStream();
			testbyte = input.read(new byte[1]);
			try
			{
				input.close();
			}
			catch (Exception ci)
			{
				testbyte = -1;
			}
			if (testbyte == -1)
			{
				if (loadedfromlocal) (new File(filename)).delete();
				try
				{
					input.close();
				}
				catch (Exception ci) {}
				errorParsing=true;
				msgErrorParsing="%3332%<br>\n";
				return;
			}
			input = fileUrl.openStream();
		}
		catch(Exception e)
		{
			if (loadedfromlocal) (new File(filename)).delete();
			try
			{
				input.close();
			}
			catch (Exception ci) {}
			errorParsing=true;
			msgErrorParsing="%3332%<br>\n"+e.toString()+"<br>\n";
			return;
		}
		try
		{
			newcontent="";
			Tika tika = new Tika();
			tika.setMaxStringLength(-1);
			content=tika.parseToString(input);
			msgfilep="%3326%: "+filename+"<br>\n";
			if (cases) content = content.toLowerCase();
			content=content.replaceAll("\t"," ");
			content=content.replaceAll("\0"," ");
			content=content.replaceAll("\f"," ");
			content=content.replaceAll("\uFFFD","");
			content=content.replaceAll("\u00A0"," ");
			newcontent=content.trim();
			content="";
			input.close();
			input=null;
			if (loadedfromlocal) (new File(filename)).delete();
		}
		catch(OutOfMemoryError ea)
		{
			if (loadedfromlocal) (new File(filename)).delete();
			try
			{
				input.close();
			}
			catch (Exception ci) {}
			errorParsing=true;
			msgErrorParsing="%3332%<br>\n"+ea.toString()+"<br>\n";
			return;
		}
		catch(Exception e)
		{
			if (loadedfromlocal) (new File(filename)).delete();
			try
			{
				input.close();
			}
			catch (Exception ci) {}
			errorParsing=true;
			msgErrorParsing="%3332%<br>\n"+e.toString()+"<br>\n";
			return;
		}
		try
		{
			int charSeparator=0;
			if (charstodelete.size()>0)
			{
				try
				{
					StringBuilder start=new StringBuilder();
					start.append(newcontent);
					StringBuilder end=new StringBuilder();
					int sz = start.length();
					for (int i = 0; i < sz; i++)
					{
						if (!charstodelete.contains(new Integer(start.charAt(i)))) end.append(start.charAt(i));
					}
					newcontent=end.toString();
					start=null;
					newcontent=newcontent.trim();
				}
				catch (Exception ee)
				{
					othermsg=othermsg+"%3336%: "+ee.toString()+"<br>\n";
				}
			}
			if (charstosubwspace.size()>0)
			{
				try
				{
					StringBuilder start=new StringBuilder();
					start.append(newcontent);
					StringBuilder end=new StringBuilder();
					int sz = start.length();
					for (int i = 0; i < sz; i++)
					{
						if (!charstosubwspace.contains(new Integer(start.charAt(i)))) end.append(start.charAt(i));
						if (charstosubwspace.contains(new Integer(start.charAt(i)))) end.append(" ");
					}
					newcontent=end.toString();
					start=null;
					newcontent=newcontent.trim();
				}
				catch (Exception ee)
				{
					othermsg=othermsg+"%3427%: "+ee.toString()+"<br>\n";
				}
			}
			if (charstoreplace!=null)
			{
				String[] parts1=null;
				String[] parts2=null;
				String[] parts3=null;
				StringBuilder from=null;
				StringBuilder to=null;
				StringBuilder orig=new StringBuilder();
				orig.append(newcontent);
				boolean errorinrep=false;
				int index=0;
				for (int i=0; i<charstoreplace.length; i++)
				{
					errorinrep=false;
					try
					{
						parts1=charstoreplace[i].split("=");
						if (parts1.length==2)
						{
							parts2=parts1[0].split(" ");
							parts3=parts1[1].split(" ");
							from=new StringBuilder();
							to=new StringBuilder();
							for (int j=0; j<parts2.length; j++)
							{
								if (!parts2[j].equals(""))
								{
									try
									{
										charSeparator = Integer.parseInt(parts2[j]);
										from.append((char)charSeparator);
									}
									catch (Exception ee)
									{
										errorinrep=true;
									}
								}
							}
							for (int j=0; j<parts3.length; j++)
							{
								if (!parts3[j].equals(""))
								{
									try
									{
										charSeparator = Integer.parseInt(parts3[j]);
										to.append((char)charSeparator);
									}
									catch (Exception ee)
									{
										errorinrep=true;
									}
								}
							}
							if (!errorinrep)
							{
								index = orig.indexOf(from.toString());
								while (index != -1)
								{
									orig.replace(index, index + from.length(), to.toString());
									index += to.length();
									index = orig.indexOf(from.toString(), index);
								}
							}
						}
						else errorinrep=true;
					}
					catch (Exception ee)
					{
						othermsg=othermsg+"%3337%: "+charstoreplace[i]+"<br>\n";
					}
					if (errorinrep) othermsg=othermsg+"%3337%: "+charstoreplace[i]+"<br>\n";
				}
				newcontent=orig.toString();
				newcontent=newcontent.trim();
				content="";
			}
			if (replacenewlines)
			{
				content=newcontent;
				newcontent="";
				StringBuilder from=null;
				StringBuilder to=null;
				StringBuilder orig=new StringBuilder();
				orig.append(content);
				from=new StringBuilder();
				to=new StringBuilder();
				from.append((char)10);
				to.append((char)32);
				to.append((char)60);
				to.append((char)78);
				to.append((char)76);
				to.append((char)62);
				to.append((char)32);
				int index = orig.indexOf(from.toString());
				while (index != -1)
				{
					orig.replace(index, index + from.length(), to.toString());
					index += to.length();
					index = orig.indexOf(from.toString(), index);
				}
				from=new StringBuilder();
				from.append((char)12);
				index = orig.indexOf(from.toString());
				while (index != -1)
				{
					orig.replace(index, index + from.length(), to.toString());
					index += to.length();
					index = orig.indexOf(from.toString(), index);
				}
				from=new StringBuilder();
				from.append((char)13);
				index = orig.indexOf(from.toString());
				while (index != -1)
				{
					orig.replace(index, index + from.length(), to.toString());
					index += to.length();
					index = orig.indexOf(from.toString(), index);
				}
				newcontent=orig.toString();
				newcontent=newcontent.replaceAll("\\s+"," ");
				newcontent=newcontent.trim();
				content="";
			}
			else
			{
				newcontent=newcontent.replaceAll("\r"," ");
				newcontent=newcontent.replaceAll("\n"," ");
				newcontent=newcontent.replaceAll("\\s+"," ");
			}
			String[] split = null;
			if (identifynewsentences)
			{
				content=newcontent;
				newcontent="";
				StringBuilder from=null;
				StringBuilder to=null;
				StringBuilder orig=new StringBuilder();
				for (int j=0; j<content.length(); j++)
				{
					orig.append(content.substring(j,j+1));
				}
				from=new StringBuilder();
				to=new StringBuilder();
				from.append((char)41);
				to.append((char)32);
				to.append((char)60);
				to.append((char)78);
				to.append((char)83);
				to.append((char)62);
				to.append((char)32);
				int index = orig.indexOf(from.toString());
				while (index != -1)
				{
					orig.replace(index, index + from.length(), to.toString());
					index += to.length();
					index = orig.indexOf(from.toString(), index);
				}
				from=new StringBuilder();
				from.append((char)33);
				index = orig.indexOf(from.toString());
				while (index != -1)
				{
					orig.replace(index, index + from.length(), to.toString());
					index += to.length();
					index = orig.indexOf(from.toString(), index);
				}
				from=new StringBuilder();
				from.append((char)46);
				index = orig.indexOf(from.toString());
				while (index != -1)
				{
					orig.replace(index, index + from.length(), to.toString());
					index += to.length();
					index = orig.indexOf(from.toString(), index);
				}
				from=new StringBuilder();
				from.append((char)40);
				index = orig.indexOf(from.toString());
				while (index != -1)
				{
					orig.replace(index, index + from.length(), to.toString());
					index += to.length();
					index = orig.indexOf(from.toString(), index);
				}
				from=new StringBuilder();
				from.append((char)44);
				index = orig.indexOf(from.toString());
				while (index != -1)
				{
					orig.replace(index, index + from.length(), to.toString());
					index += to.length();
					index = orig.indexOf(from.toString(), index);
				}
				from=new StringBuilder();
				from.append((char)58);
				index = orig.indexOf(from.toString());
				while (index != -1)
				{
					orig.replace(index, index + from.length(), to.toString());
					index += to.length();
					index = orig.indexOf(from.toString(), index);
				}
				from=new StringBuilder();
				from.append((char)59);
				index = orig.indexOf(from.toString());
				while (index != -1)
				{
					orig.replace(index, index + from.length(), to.toString());
					index += to.length();
					index = orig.indexOf(from.toString(), index);
				}
				from=new StringBuilder();
				from.append((char)63);
				index = orig.indexOf(from.toString());
				while (index != -1)
				{
					orig.replace(index, index + from.length(), to.toString());
					index += to.length();
					index = orig.indexOf(from.toString(), index);
				}
				from=new StringBuilder();
				from.append((char)91);
				index = orig.indexOf(from.toString());
				while (index != -1)
				{
					orig.replace(index, index + from.length(), to.toString());
					index += to.length();
					index = orig.indexOf(from.toString(), index);
				}
				from=new StringBuilder();
				from.append((char)93);
				index = orig.indexOf(from.toString());
				while (index != -1)
				{
					orig.replace(index, index + from.length(), to.toString());
					index += to.length();
					index = orig.indexOf(from.toString(), index);
				}
				from=new StringBuilder();
				from.append((char)34);
				index = orig.indexOf(from.toString());
				while (index != -1)
				{
					orig.replace(index, index + from.length(), to.toString());
					index += to.length();
					index = orig.indexOf(from.toString(), index);
				}
				newcontent=orig.toString();
				newcontent=newcontent.replaceAll("\\s+"," ");
				newcontent=newcontent.trim();
				content="";
			}
			if (onlyascii || nonumbers)
			{
				StringBuilder start=new StringBuilder();
				start.append(newcontent);
				StringBuilder end=new StringBuilder();
				int sz = start.length();
				boolean consider=true;
				for (int i = 0; i < sz; i++)
				{
					consider=true;
					if (start.charAt(i)!=32)
					{
						if (onlyascii)
						{
							if (checkapo(start.charAt(i)))
							{
								end.append("' ");
								consider=false;
							}
							else if (!isAscii(start.charAt(i))) consider=false;
						}
						if (nonumbers && (isNumber(start.charAt(i)))) consider=false;
					}
					if (consider) end.append(start.charAt(i));
				}
				newcontent=end.toString();
				start=null;
				newcontent=newcontent.replaceAll("\\s+"," ");
				newcontent=newcontent.trim();
				content="";
			}
			newcontent=newcontent.replaceAll("\\s+"," ");
			if (identifynewsentences || replacenewlines)
			{
				split = newcontent.split(" ",-1);
				boolean already=false;
				boolean add=false;
				boolean isdel=false;
				newcontent="";
				for (int i=0; i<split.length; i++)
				{
					add=true;
					isdel=false;
					if (split[i].equals("<NL>") && already) add=false;
					if (split[i].equals("<NS>") && already) add=false;
					if (split[i].equals("<NL>") && !already) already=true;
					if (split[i].equals("<NS>") && !already) already=true;
					if (split[i].equals("<NL>")) isdel=true;
					if (split[i].equals("<NS>")) isdel=true;
					if (!isdel) already=false;
					if (add) newcontent=newcontent+split[i].trim()+" ";
				}
				newcontent=newcontent.replaceAll("\\s+"," ");
			}
			newcontent=newcontent.trim();
			content="";
		}
		catch (Exception eg)
		{
			errorParsing=true;
			msgErrorParsing="%3332%\n"+eg.toString()+"<br>\n";
		}
	}
	public String simpleparser(String filename)
	{
		loadedfromlocal=false;
		String localfname="";
		errorParsing=false;
		msgErrorParsing="";
		if((filename.toLowerCase()).startsWith("http") && !workingdir.equals(""))
		{
			URLConnection conn=null;
			double tdoc_size=Double.NaN;
			java.net.URL fileUrl;
			try
			{
				fileUrl =  new java.net.URL(filename);
				conn = fileUrl.openConnection();
				conn.setConnectTimeout(timeout);
				conn.setReadTimeout(timeout);
				conn.connect();
				tdoc_size = (double)conn.getContentLength();
			}
			catch (Exception econn)
			{
				errorParsing=true;
				msgErrorParsing="%3332%\n"+econn.toString()+"<br>\n";
				return null;
			}
			if (Double.isNaN(tdoc_size))
			{
				errorParsing=true;
				msgErrorParsing="%3332%<br>\n";
				return null;
			}
			if (tdoc_size<2)
			{
				errorParsing=true;
				msgErrorParsing="%3332%<br>\n";
				return null;
			}
			byte[] buffer = new byte[1024];
			int bytesRead;
			localfname=workingdir+"TempHttpFile";
			(new File(localfname)).delete();
			try
			{
				BufferedInputStream inputStream = new BufferedInputStream(conn.getInputStream());
				File tempf = new File(localfname);
				BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempf));
				while ((bytesRead = inputStream.read(buffer)) != -1)
				{
					outputStream.write(buffer, 0, bytesRead);
				}
				inputStream.close();
				outputStream.close();
			}
			catch (Exception econn)
			{
				errorParsing=true;
				msgErrorParsing="%3332%<br>\n"+econn.toString()+"<br>\n";
				return null;
			}
			loadedfromlocal=true;
			filename=localfname;
		}
		Tika tika=null;
		content="";
		newcontent="";
		msgfilep="";
		msgLang="";
		content="";
		othermsg="";
		input=null;
		java.net.URL fileUrl;
		try
		{
			if((filename.toLowerCase()).startsWith("http"))
				fileUrl =  new java.net.URL(filename);
			else
			{
				File file=new File(filename);
				fileUrl = file.toURI().toURL();
			}
			tika = new Tika();
			tika.setMaxStringLength(-1);
			input = fileUrl.openStream();
			testbyte = input.read(new byte[1]);
			try
			{
				input.close();
			}
			catch (Exception ci)
			{
				testbyte = -1;
			}
			if (testbyte == -1)
			{
				if (loadedfromlocal) (new File(filename)).delete();
				try
				{
					input.close();
				}
				catch (Exception ci) {}
				tika=null;
				errorParsing=true;
				msgErrorParsing="%3332%<br>\n";
				return null;
			}
			//input = fileUrl.openStream();
		}
		catch(Exception e)
		{
			if (loadedfromlocal) (new File(filename)).delete();
			try
			{
				input.close();
			}
			catch (Exception ci) {}
			tika=null;
			errorParsing=true;
			msgErrorParsing="%3332%<br>\n"+e.toString()+"<br>\n";
			return null;
		}
		try
		{
			content = tika.parseToString(fileUrl);
			msgfilep="%3326%: "+filename;
			if (cases) content = content.toLowerCase();
			tika=null;
			input.close();
			input=null;
			if (loadedfromlocal) (new File(filename)).delete();
			content=content.replaceAll("\\s+"," ");
			return content;
		}
		catch(OutOfMemoryError ea)
		{
			if (loadedfromlocal) (new File(filename)).delete();
			try
			{
				input.close();
			}
			catch (Exception ci) {}
			tika=null;
			errorParsing=true;
			msgErrorParsing="%3332%<br>\n"+ea.toString()+"<br>\n";
			return null;
		}
		catch(Exception e)
		{
			if (loadedfromlocal) (new File(filename)).delete();
			try
			{
				input.close();
			}
			catch (Exception ci) {}
			tika=null;
			errorParsing=true;
			msgErrorParsing="%3332%<br>\n"+e.toString()+"<br>\n";
			return null;
		}
	}
	/**
	*Return true if it is a printable ascii char
	*/
	private boolean isAscii(char ch)
	{
		if (ch >= 48 && ch <= 57) return true;
		else if (ch == 60)
		{
			if (identifynewsentences) return true;
			else return false;
		}
		else if (ch == 62)
		{
			if (identifynewsentences) return true;
			else return false;
		}
		else if (ch >= 65 && ch <= 90) return true;
		else if (ch >= 97 && ch <= 122) return true;
		else if (ch >= 192 && ch <= 197) return true;
		else if (ch >= 200 && ch <= 207) return true;
		else if (ch >= 210 && ch <= 214) return true;
		else if (ch >= 216 && ch <= 221) return true;
		else if (ch >= 224 && ch <= 246) return true;
		else if (ch >= 249 && ch <= 255) return true;
		else return false;
	}
	/**
	*Return true if it is a number
	*/
	private boolean isNumber(char ch)
	{
		return ch >= 48 && ch <= 57;
	}
	/**
	*Check for the apostrophe
	*/
	private boolean checkapo(char ch)
	{
		if (ch==39) return true;
		else if (ch==96) return true;
		else if (ch==145) return true;
		else if (ch==146) return true;
		else if (ch==180) return true;
		else if (ch==8216) return true;
		else if (ch==8217) return true;
		else return false;
	}
}
