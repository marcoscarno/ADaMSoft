/**
* Copyright © 2017 MS
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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Serializable;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.jasypt.encryption.pbe.StandardPBEByteEncryptor;

/**
* Loads a document (an external file)
* @authormarco.scarno@gmail.com
* @date 14/11/2017
*/
public class ADaMSdoc implements Serializable
{
	/**
	 *This is the default serial versio UID
	 */
	private static final long serialVersionUID = 1L;
	/**
	*Contains the name of the document (with extension, without the path)
	*/
	String documentname="";
	/**
	*This is the buffer size
	*/
	static final int buffSize = 1024;
	/**
	*This is the default password if the user don't enter a value for encryptwith
	*/
	final static String password = "ADaMSoft";
	/**
	*This is the buffer
	*/
	private byte[] buffer;
	/**
	*This is the internal buffer of this class
	*/
	public ADaMSdoc(byte[] buffer)
	{
		this.buffer=buffer;
	}
	/**
	*This is the constructor (used if the password is null)
	*/
	public ADaMSdoc(String path) throws Exception
	{
		init(path, password);
	}
	/**
	*This is the constructor (used if the password is not null)
	*/
	public ADaMSdoc(String path, String passPhrase) throws Exception
	{
		init(path, passPhrase);
	}
	/**
	*Reads the file, crypts and compress it
	*/
	private void init (String path, String passPhrase) throws Exception
	{
		if (passPhrase==null) passPhrase="ADaMSoft";
		if (passPhrase.equals("")) passPhrase="ADaMSoft";
		byte[] b = new byte[buffSize];
		documentname=(new File(path)).getPath();
		documentname=documentname.substring(documentname.lastIndexOf(System.getProperty("file.separator"))+1);
		ByteArrayOutputStream baostmp = new ByteArrayOutputStream();
		GZIPOutputStream zip = new GZIPOutputStream(baostmp);
		int count = 0;
		BufferedInputStream in = new BufferedInputStream((new File(path)).toURI().toURL().openStream());
		while ((count = in.read(b)) != -1) {
			zip.write(b, 0, count);
		}
		zip.finish();
		zip.close();
		baostmp.close();
		in.close();
		buffer = baostmp.toByteArray();
		StandardPBEByteEncryptor se=new StandardPBEByteEncryptor();
		se.setPassword(passPhrase);
		se.initialize();
		buffer=se.encrypt(buffer);
	}
	/**
	*Used to retrieve the document (returns a stream)
	*/
	public InputStream getData() throws IOException
	{
		return getData(password);
	}
	/**
	*Used to retrieve the document (returns a stream), considering the password
	*/
	public InputStream getData(String passPhrase)
	{
		if (passPhrase==null) passPhrase="ADaMSoft";
		if (passPhrase.equals("")) passPhrase="ADaMSoft";
		GZIPInputStream out=null;
		try
		{
			StandardPBEByteEncryptor see=new StandardPBEByteEncryptor();
			see.setPassword(passPhrase);
			see.initialize();
			buffer=see.decrypt(buffer);
			ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
			out=new GZIPInputStream(bais);
		}
		catch (Exception e){}
		return out;
	}
	/**
	*Returns a byte array with the zipped document
	*/
	public byte[] getZippedArray()
	{
		return buffer;
	}
	/**
	*Returns the original name (without the path) of the file
	*/
	public String getdocumentname()
	{
		return documentname;
	}
}
