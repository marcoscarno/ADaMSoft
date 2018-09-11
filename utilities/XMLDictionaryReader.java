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
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ADaMSoft.keywords.Keywords;

/**
* This class reads a Dictionary expressed into an XML file and contains several methods to retrieve the information that are in it
* @author marco.scarno@gmail.com
* @date 07/09/2015
*/
public class XMLDictionaryReader
{
	/**
	*This is the creation date
	*/
	String creationdate="";
	/**
	*These are the keywords associated to the data set
	*/
	String keyword="";
	/**
	*This is the description of the data set
	*/
	String description="";
	/**
	*This is the information on who creates the dictionary
	*/
	String author="";
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
	*This is the message, if the dictionary cannot be read, that is returned
	*/
	String messageDictionaryReader="";
	/**
	*Receives the path of the dictionary and initializes the different object that can be retrieved.<p>
	*Returne false if the dictionary cannot be read.<p>
	*/
	public XMLDictionaryReader(String dictionaryfile)
	{
		messageDictionaryReader="";
		String encodingLocale=(System.getProperty( "file.encoding" )).toString();
		System.setProperty("file.encoding", encodingLocale);
		try
		{
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			java.net.URL dictionaryurl;
			InputStream dictionarystream=null;
			File fileDictionary=new File(dictionaryfile);
			dictionaryurl = fileDictionary.toURI().toURL();
			try
			{
				dictionarystream = dictionaryurl.openStream();
			}
			catch(Exception exs)
			{
				messageDictionaryReader=Keywords.Language.getMessage(255)+" ("+dictionaryfile+")<br>\n";
				return;
			}
			Document doc = docBuilder.parse (dictionarystream);
			doc.getDocumentElement().normalize ();

			NodeList datasetinfo = doc.getElementsByTagName(Keywords.DataSetInfo);
			Node nodedsinfo = (datasetinfo.item(0));
			NodeList childdsinfo = nodedsinfo.getChildNodes();
			for (int i = 0; i < childdsinfo.getLength(); i++)
			{
				Node kiddsinfo = childdsinfo.item(i);
				if (kiddsinfo.getNodeType()== Node.ELEMENT_NODE)
				{
					try
					{
						String info=kiddsinfo.getNodeName();
						String value=(doc.getElementsByTagName(info).item(0).getChildNodes().item(0)).getNodeValue().trim();
						if (info.equalsIgnoreCase(Keywords.description))
							description=value;
						if (info.equalsIgnoreCase(Keywords.keyword))
							keyword=value;
						if (info.equalsIgnoreCase(Keywords.CreationDate))
							creationdate=value;
						if (info.equalsIgnoreCase(Keywords.author))
							author=value;
					}
					catch(Exception ex){}
				}
			}

			NodeList variables = doc.getElementsByTagName(Keywords.VariablesList);
			if (variables.getLength() !=1)
			{
				messageDictionaryReader=Keywords.Language.getMessage(256)+" ("+dictionaryfile+")<br>\n";
				docBuilder=null;
				return;
			}
			fixedvariableinfo=new Vector<Hashtable<String, String>>();
			codelabel=new Vector<Hashtable<String, String>>();
			missingdata=new Vector<Hashtable<String, String>>();
			NodeList nodevariable= doc.getElementsByTagName(Keywords.Variable);
			int totalvariables = nodevariable.getLength();
			for(int i=0; i<totalvariables; i++)
			{
				Node variablenode=nodevariable.item(i);
				Hashtable<String, String> tempfixedvariableinfo=new Hashtable<String, String>();
				Hashtable<String, String> tempcodelabel=new Hashtable<String, String>();
				Hashtable<String, String> tempmissingdata=new Hashtable<String, String>();
				String name=(doc.getElementsByTagName(Keywords.VariableName.toLowerCase()).item(i).getChildNodes().item(0)).getNodeValue().trim();
				String format=(doc.getElementsByTagName(Keywords.VariableFormat.toLowerCase()).item(i).getChildNodes().item(0)).getNodeValue().trim();
				String label=(doc.getElementsByTagName(Keywords.LabelOfVariable.toLowerCase()).item(i).getChildNodes().item(0)).getNodeValue().trim();
				String varnum=(doc.getElementsByTagName(Keywords.VariableNumber.toLowerCase()).item(i).getChildNodes().item(0)).getNodeValue().trim();
				tempfixedvariableinfo.put(Keywords.VariableName.toLowerCase(), name);
				tempfixedvariableinfo.put(Keywords.VariableFormat.toLowerCase(), format);
				tempfixedvariableinfo.put(Keywords.LabelOfVariable.toLowerCase(), label);
				tempfixedvariableinfo.put(Keywords.VariableNumber.toLowerCase(), varnum);
				NodeList childvariable = variablenode.getChildNodes();
				for (int j = 0; j < childvariable.getLength(); j++)
				{
					Node kid = childvariable.item(j);
					try
					{
						if (kid.getNodeType() == Node.ELEMENT_NODE)
						{
							String tagname = kid.getNodeName().trim();
							if(tagname.equalsIgnoreCase(Keywords.CodeLabels))
							{
								Element clelement = (Element)variablenode;
								NodeList listformat = clelement.getElementsByTagName(Keywords.CodeLabel);
								if (listformat.getLength()>0)
								{
									for(int k=0; k<listformat.getLength(); k++)
									{
										Node nodeFormat= listformat.item(k);
										Element elementFormat=(Element)nodeFormat;
										try
										{
											String tempcode=(elementFormat.getElementsByTagName(Keywords.Code).item(0).getChildNodes().item(0)).getNodeValue().trim();
											String tempvalue=(elementFormat.getElementsByTagName(Keywords.LabelVar).item(0).getChildNodes().item(0)).getNodeValue().trim();
											tempcodelabel.put(tempcode, tempvalue);
										}
										catch (Exception eclnull) {}
									}
								}
							}
							else if(tagname.equalsIgnoreCase(Keywords.MissingDataValues))
							{
								Element mdelement = (Element)variablenode;
								NodeList listmissing= mdelement.getElementsByTagName(Keywords.MissingData);
								if (listmissing.getLength()>0)
								{
									for(int k=0;k<listmissing.getLength();k++)
									{
										Node nodeMissing= listmissing.item(k);
										Element elementMissing=(Element)nodeMissing;
										try
										{
											String tempmd=(elementMissing.getElementsByTagName(Keywords.Rule).item(0).getChildNodes().item(0)).getNodeValue().trim();
											tempmissingdata.put(tempmd,"");
										}
										catch (Exception emdnull) {}
									}
								}
							}
						}
					}
					catch(DOMException ex)
					{
						messageDictionaryReader=Keywords.Language.getMessage(256)+" ("+dictionaryfile+")\n";
						docBuilder=null;
						return;
					}
				}
				fixedvariableinfo.add(i, tempfixedvariableinfo);
				codelabel.add(i, tempcodelabel);
				missingdata.add(i, tempmissingdata);
			}
		}
		catch (Exception e)
		{
			messageDictionaryReader=Keywords.Language.getMessage(255)+" ("+dictionaryfile+")<br>\n";
			return;
		}
		return;
	}
	/**
	*Returns the creation date of the dictionary
	*/
	public String getcreationdate()
	{
		return creationdate;
	}
	/**
	*Returns the keywords associated to the data set
	*/
	public String getkeyword()
	{
		return keyword;
	}
	/**
	*Returns the description associated to the data set
	*/
	public String getdescription()
	{
		return description;
	}
	/**
	*Returns the author of the data set
	*/
	public String getauthor()
	{
		return author;
	}
	/**
	*This vector contains, in each record, an hashtable with the fixed information for the variables<p>
	*Such information are:<p>
	*name, label, writeformat.
	*/
	public Vector<Hashtable<String, String>> getfixedvariableinfo()
	{
		return fixedvariableinfo;
	}
	/**
	*This vector contains, in each record, an hashtable with the code and the label defined on each variable.
	*/
	public Vector<Hashtable<String, String>> getcodelabel()
	{
		return codelabel;
	}
	/**
	*This vector contains, in each record, an hashtable with the missing data rule defined on each variable.
	*/
	public Vector<Hashtable<String, String>> getmissingdata()
	{
		return missingdata;
	}
	/**
	*Returns an empty string if the dictionary was read ok, otherwise the error message
	*/
	public String getmessageDictionaryReader()
	{
		return messageDictionaryReader;
	}
}
