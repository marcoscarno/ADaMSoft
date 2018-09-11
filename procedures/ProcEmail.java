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

import java.util.LinkedList;
import java.util.Hashtable;

import ADaMSoft.keywords.Keywords;
import ADaMSoft.utilities.StepUtilities;
import ADaMSoft.utilities.GetRequiredParameters;
import ADaMSoft.utilities.MailCommand;

/**
* This is the procedure that sends an email, eventually with an attached file
* @author marco.scarno@gmail.com
* @date 14/02/2017
*/
public class ProcEmail implements RunStep
{
	/**
	* Starts the execution of Proc Email
	*/
	public Result executionresult(Hashtable<String, Object> parameters)
	{
		String [] requiredparameters=new String[] {Keywords.mailtext, Keywords.mailserver_address, Keywords.to, Keywords.mailserver_port, Keywords.mailserver_username, Keywords.subject};
		String [] optionalparameters=new String[] {Keywords.mailserver_password, Keywords.from, Keywords.attach, Keywords.useauth, Keywords.starttls, Keywords.ssl_enable, Keywords.set_transport};
		Keywords.percentage_total=0;
		Keywords.percentage_done=0;
		StepUtilities steputilities=new StepUtilities();
		if (!steputilities.checkParameters(requiredparameters, optionalparameters, parameters))
		{
			return new Result(steputilities.getMessage(), false, null);
		}

		String mailserver_address=(String)parameters.get(Keywords.mailserver_address);
		String mailserver_port=(String)parameters.get(Keywords.mailserver_port);
		String useauth=(String)parameters.get(Keywords.useauth);
		String starttls=(String)parameters.get(Keywords.starttls);
		String mailserver_username=(String)parameters.get(Keywords.mailserver_username);
		String mailserver_password=(String)parameters.get(Keywords.mailserver_password);
		String ssl_enable=(String)parameters.get(Keywords.ssl_enable);
		String set_transport=(String)parameters.get(Keywords.set_transport);
		String to=(String)parameters.get(Keywords.to);
		String from=(String)parameters.get(Keywords.from);
		if (from==null) from=mailserver_username;
		String subject=(String)parameters.get(Keywords.subject);
		String mailtext=(String)parameters.get(Keywords.mailtext);
		String attach=(String)parameters.get(Keywords.attach);
		try
		{
			MailCommand mailCommand = new MailCommand();
			mailCommand.set_smtp(mailserver_address);
			mailCommand.set_smtp_port(mailserver_port);
			if (useauth!=null)
				mailCommand.set_smtp_auth(true);
			if (starttls!=null)
				mailCommand.set_smtp_starttls(true);
			mailCommand.set_smtp_user(mailserver_username);
			mailCommand.set_smtp_password(mailserver_password);
			if (ssl_enable!=null)
				mailCommand.set_smtp_ssl_enable(true);
			mailCommand.set_from(from);
			if (set_transport!=null)
				mailCommand.set_transport(set_transport);
			mailCommand.initialize();
			String[] addresses=to.split(";");
			for (int i=0; i<addresses.length; i++)
			{
				mailCommand.addTo(addresses[i]);
			}
			mailCommand.setSubject(subject);
			mailCommand.setBody(mailtext);
			mailCommand.prepareMessage();
			if (attach!=null)
				mailCommand.addAttachment(attach);
			mailCommand.sendmessage();
			if (mailCommand.get_sending_error())
				return new Result("%1236%<br>\n"+mailCommand.get_message_MailCommand()+"\n", false, null);
		}
		catch (Exception e)
		{
			return new Result("%1236%<br>\n"+e.toString()+"<br>\n", false, null);
		}
		return new Result("%1248%<br>\n", true, null);
	}
	/**
	*Returns the parameters for the procedure
	*/
	public LinkedList<GetRequiredParameters> getparameters()
	{
		LinkedList<GetRequiredParameters> parameters=new LinkedList<GetRequiredParameters>();
		String[] dep ={""};
		parameters.add(new GetRequiredParameters(Keywords.mailserver+"=" , "setting=mailserver", true, 1242, dep, "", 1));
		parameters.add(new GetRequiredParameters(Keywords.to, "text", true, 1243, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.from, "text", false, 1244, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.subject, "text", true, 1247, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.mailtext, "text",true, 1245, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.attach, "file=all",false, 1246, dep, "", 2));

		parameters.add(new GetRequiredParameters(Keywords.useauth, "checkbox", false, 3981, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.starttls, "checkbox", false, 3982, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.ssl_enable, "checkbox", false, 3983, dep, "", 2));
		parameters.add(new GetRequiredParameters(Keywords.set_transport, "text", false, 3984, dep, "", 2));

		return parameters;
	}
	/**
	*Returns the group and the name of the procedure
	*/
	public String[] getstepinfo()
	{
		String[] retprocinfo=new String[2];
		retprocinfo[0]="4173";
		retprocinfo[1]="1238";
		return retprocinfo;
	}
}
