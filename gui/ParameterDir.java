/**
* Copyright © 2006-2009 CASPUR
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

package ADaMSoft.gui;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import ADaMSoft.keywords.Keywords;

/**
* This class adds the possibility to select an external file in the GUI related to
* the procedure parameters selection
* @author marco.scarno@caspur.it
* @version 1.0.0, rev.: 19/02/09 by marco
*/
public class ParameterDir implements ActionListener
{
	JButton fileselection;
	JTextField nameselectedfile;
	String parametername;
	/**
	*Create a GUI for file selection
	*/
	public ParameterDir (String parametername, int messagecode, JPanel actualpanel, boolean required)
	{
		this.parametername=parametername;
		JPanel panelfile=new JPanel(new GridLayout(1,2));
		fileselection=new JButton(Keywords.Language.getMessage(messagecode));
		fileselection.addActionListener(this);
		fileselection.setToolTipText(parametername);
		panelfile.add(fileselection);
		nameselectedfile=new JTextField();
		nameselectedfile.setToolTipText(parametername);
		panelfile.add(nameselectedfile);
		Border parameterborder = BorderFactory.createCompoundBorder(BorderFactory.createRaisedBevelBorder(), BorderFactory.createLoweredBevelBorder());
		panelfile.setBorder(parameterborder);
		if (!required)
			panelfile.setBackground(Color.LIGHT_GRAY);
		actualpanel.add(panelfile);
	}
	/**
	*Returns the name of the selected file in a string form
	*/
	public String toString()
	{
		String value=nameselectedfile.getText();
		if (!value.equals(""))
			value=parametername+" "+value;
		return value;
	}
	/**
	*Checks the type of the selected file
	*/
	public void actionPerformed(ActionEvent e)
	{
		String lastopeneddir=(String)System.getProperty("lastOpenedDirectory");
		JFileChooser sb = new JFileChooser(new File(lastopeneddir));
		sb.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		try
		{
			sb.showOpenDialog(null);
			File openfile= sb.getSelectedFile();
			String filename=openfile.getAbsolutePath();
			lastopeneddir = openfile.getPath();
			try
			{
				lastopeneddir=lastopeneddir.replaceAll("\\\\","/");
			}
			catch (Exception fs){}
			if (!lastopeneddir.endsWith(System.getProperty("file.separator")))
				lastopeneddir=lastopeneddir+System.getProperty("file.separator");
			System.setProperty("lastOpenedDirectory", lastopeneddir);
			nameselectedfile.setText(filename);
		}
		catch (Exception ec)
		{
			return;
		}
	}
}