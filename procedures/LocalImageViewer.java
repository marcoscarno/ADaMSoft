/**
* Copyright (c) 2018 MS
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

import javax.swing.SwingUtilities;
import java.io.Serializable;
import ADaMSoft.gui.ImageViewer;
import ADaMSoft.utilities.AdamsCompliant;
import ADaMSoft.keywords.Keywords;

/**
 * This class visualize an image
 * @author marco.scarno@gmail.com
 * @date 23/02/2018
 */

public class LocalImageViewer extends AdamsCompliant implements StepResult, Serializable
{
	private static final long serialVersionUID = 1L;
	/**
	*This is the path
	*/
	String path;
	String title;
	/**
	*Constructor
	*/
	public LocalImageViewer(String path, String title)
	{
		this.path=path;
		this.title=title;
	}
	/*Not used*/
	public void exportOutput()
	{
	}
	/**
	*View the image
	*/
	public String action()
	{
		try
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				  public void run()
				  {
						ImageViewer iv=new ImageViewer(path,title);
				  }
			});
		}
		catch (Exception e)
		{
			return "0 %4281%<br>"+e.toString()+"\n";
		}
		return "1 %4282%<br>\n";
	}
}
