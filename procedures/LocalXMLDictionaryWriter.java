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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.helpers.AttributesImpl;

import ADaMSoft.keywords.Keywords;

/**
* This class creates a Dictionary expressed by an XML file
* @author marco.scarno@gmail.com
* @date 13/02/2017
*/
public class LocalXMLDictionaryWriter implements StepResult, Serializable
{
	/**
	 * This is the default static version UID
	 */
	private static final long serialVersionUID = 1L;
	/**
	*This is the path where the dictionary will be saved
	*/
	String path;
	/**
	*These are the keywords associated to the data set
	*/
	String keyword;
	/**
	*This is the description of the data set
	*/
	String description;
	/**
	*This is the information on who creates the dictionary
	*/
	String author;
	/**
	*This vector contains, in each record, an hashtable with the fixed information in the variable.<p>
	*Such information are related to:<p>
	*name, label, writeformat.
	*/
	Vector<Hashtable<String, String>> fixedvariableinfo;
	/**
	*This vector contains, in each record, an hashtable with the code and the label defined on each variable.
	*/
	Vector<Hashtable<String, String>> codelabel;
	/**
	*This vector contains, in each record, an hashtable with the missing data rule defined on each variable.
	*/
	Vector<Hashtable<String, String>> missingdata;
	/**
	*Constructor
	*/
	public LocalXMLDictionaryWriter (String path, String keyword, String description, String author,
	Vector<Hashtable<String, String>> fixedvariableinfo, Vector<Hashtable<String, String>> codelabel,
	Vector<Hashtable<String, String>> missingdata)
	{
		this.path=path;
		this.keyword=keyword;
		this.description=description;
		this.author=author;
		this.fixedvariableinfo=fixedvariableinfo;
		this.codelabel=codelabel;
		this.missingdata=missingdata;
	}
	/*Not used*/
	public void exportOutput()
	{
	}
	public String action()
	{
		System.setProperty("file.encoding", (System.getProperty( "file.encoding" )).toString());
		try
		{
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path, false)));
			StreamResult streamResult = new StreamResult(out);
			SAXTransformerFactory tf = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
			TransformerHandler hd = tf.newTransformerHandler();
			Transformer serializer = hd.getTransformer();
			serializer.setOutputProperty(OutputKeys.ENCODING, (System.getProperty("file.encoding")).toString());
			serializer.setOutputProperty(OutputKeys.INDENT,"yes");
			hd.setResult(streamResult);
			hd.startDocument();
			AttributesImpl atts = new AttributesImpl();
			hd.startElement("","",Keywords.SoftwareName+"_Dictionary",atts);
			hd.startElement("","",Keywords.DataSetInfo,atts);
			if (keyword==null)
				keyword=path;
			if (description==null)
				description=path;
			hd.startElement("","",Keywords.keyword,atts);
			hd.characters(keyword.toCharArray(),0,keyword.length());
			hd.endElement("","",Keywords.keyword);
			hd.startElement("","",Keywords.description,atts);
			hd.characters(description.toCharArray(),0,description.length());
			hd.endElement("","",Keywords.description);
			if (author==null)
			{
				try
				{
		    	    InetAddress addr = InetAddress.getLocalHost();
		    	    author=addr.toString();
		    	}
		    	catch (Exception ex) {}
		    }
			hd.startElement("","",Keywords.author,atts);
			hd.characters(author.toCharArray(),0,author.length());
			hd.endElement("","",Keywords.author);
			hd.endElement("","",Keywords.DataSetInfo);
			hd.startElement("","",Keywords.VariablesList,atts);
			for (int i=0; i<fixedvariableinfo.size(); i++)
			{
				String varpointer=String.valueOf(i);
				Hashtable<String, String> info1=fixedvariableinfo.get(i);
				Hashtable<String, String> cl=codelabel.get(i);
				Hashtable<String, String> md=missingdata.get(i);
				hd.startElement("","",Keywords.Variable,atts);
				hd.startElement("","",Keywords.VariableNumber.toLowerCase(),atts);
				hd.characters(varpointer.toCharArray(),0,varpointer.length());
				hd.endElement("","",Keywords.VariableNumber.toLowerCase());
				for (Enumeration<String> e = info1.keys() ; e.hasMoreElements() ;)
				{
					String Info = e.nextElement();
					String Value= info1.get(Info);
					if (!Info.equalsIgnoreCase(Keywords.VariableNumber))
					{
						hd.startElement("","",Info.toLowerCase(),atts);
						hd.characters(Value.toCharArray(),0,Value.length());
						hd.endElement("","",Info.toLowerCase());
					}
				}
				if (cl.size()>0)
				{
					hd.startElement("","",Keywords.CodeLabels,atts);
					for (Enumeration<String> e = cl.keys() ; e.hasMoreElements() ;)
					{
						String Info =e.nextElement();
						String Value= cl.get(Info);
						Info=Info.trim();
						Value=Value.trim();
						if ((!Info.equals("")) && (!Value.equals("")))
						{
							hd.startElement("","",Keywords.CodeLabel,atts);
							hd.startElement("","",Keywords.Code,atts);
							hd.characters(Info.toCharArray(),0,Info.length());
							hd.endElement("","",Keywords.Code);
							hd.startElement("","",Keywords.LabelVar,atts);
							hd.characters(Value.toCharArray(),0,Value.length());
							hd.endElement("","",Keywords.LabelVar);
							hd.endElement("","",Keywords.CodeLabel);
						}
					}
					hd.endElement("","",Keywords.CodeLabels);
				}
				if (md.size()>0)
				{
					hd.startElement("","",Keywords.MissingDataValues,atts);
					for (Enumeration<String> e = md.keys() ; e.hasMoreElements() ;)
					{
						String Info =e.nextElement();
						Info=Info.trim();
						if (!Info.equals(""))
						{
							hd.startElement("","",Keywords.MissingData,atts);
							hd.startElement("","",Keywords.Rule,atts);
							hd.characters(Info.toCharArray(),0,Info.length());
							hd.endElement("","",Keywords.Rule);
							hd.endElement("","",Keywords.MissingData);
						}
					}
					hd.endElement("","",Keywords.MissingDataValues);
				}
				hd.endElement("","",Keywords.Variable);
			}
			hd.endElement("","",Keywords.VariablesList);
			hd.endElement("","",Keywords.SoftwareName+"_Dictionary");
			hd.endDocument();
			out.close();
		}
		catch (Exception e)
		{
			return "%502%\n"+e.toString()+"<br>\n";
		}
		return "";
	}
}
