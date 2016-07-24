/**
* Copyright (C) 2008 Happy Fish / YuQing
*
* FastDFS Java Client may be copied only under the terms of the GNU Lesser
* General Public License (LGPL).
* Please visit the FastDFS Home Page http://www.csource.org/ for more detail.
**/

package org.csource.fastdfs;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.csource.common.IniFileReader;
import org.csource.common.MyException;

/**
* Global variables
* @author Happy Fish / YuQing
* @version Version 1.11
*/
public class Configuration
{
	public static int g_connect_timeout; //millisecond
	public static int g_network_timeout; //millisecond
	public static String g_charset;
	public static int g_tracker_http_port;
	public static int g_storage_port;
	public static boolean g_anti_steal_token;  //if anti-steal token
	public static String g_secret_key;   //generage token secret key
	public static TrackerGroup g_tracker_group;
	
	public static final int DEFAULT_CONNECT_TIMEOUT = 5;  //second
	public static final int DEFAULT_NETWORK_TIMEOUT = 30; //second
	
	private Configuration configuration;
	
	public Configuration()
	{
	}
	
	/**
	* load global variables
	* @param conf_filename config filename
	*/
	public Configuration configrue(String conf_filename) throws FileNotFoundException, IOException, MyException
	{
		configuration = new Configuration();
  		IniFileReader iniReader;
  		String[] szTrackerServers;
		String[] parts;
		
		//read config file key/value
		if(conf_filename.contains("classpath:")){
			String fileName = conf_filename.substring("classpath:".length(), conf_filename.length());
			conf_filename = Configuration.class.getClassLoader().getResource(fileName).getFile().substring(1);
		}
  		iniReader = new IniFileReader(conf_filename);

		g_connect_timeout = iniReader.getIntValue("connect_timeout", DEFAULT_CONNECT_TIMEOUT);
  		if (g_connect_timeout < 0)
  		{
  			g_connect_timeout = DEFAULT_CONNECT_TIMEOUT;
  		}
  		g_connect_timeout *= 1000; //millisecond
  		
  		g_network_timeout = iniReader.getIntValue("network_timeout", DEFAULT_NETWORK_TIMEOUT);
  		if (g_network_timeout < 0)
  		{
  			g_network_timeout = DEFAULT_NETWORK_TIMEOUT;
  		}
  		g_network_timeout *= 1000; //millisecond

  		g_charset = iniReader.getStrValue("charset");
  		if (g_charset == null || g_charset.length() == 0)
  		{
  			g_charset = "ISO8859-1";
  		}
  		
  		szTrackerServers = iniReader.getValues("tracker_server");
  		if (szTrackerServers == null)
  		{
  			throw new MyException("item \"tracker_server\" in " + conf_filename + " not found");
  		}
  		
  		InetSocketAddress[] tracker_servers = new InetSocketAddress[szTrackerServers.length];
  		for (int i=0; i<szTrackerServers.length; i++)
  		{
  			parts = szTrackerServers[i].split("\\:", 2);
  			if (parts.length != 2)
  			{
  				throw new MyException("the value of item \"tracker_server\" is invalid, the correct format is host:port");
  			}
  			
  			tracker_servers[i] = new InetSocketAddress(parts[0].trim(), Integer.parseInt(parts[1].trim()));
  		}
  		g_tracker_group = new TrackerGroup(tracker_servers);
  		
  		g_tracker_http_port = iniReader.getIntValue("http.tracker_http_port", 80);
  		g_storage_port = iniReader.getIntValue("storage_port", 23000);
  		g_anti_steal_token = iniReader.getBoolValue("http.anti_steal_token", false);
  		if (g_anti_steal_token)
  		{
  			g_secret_key = iniReader.getStrValue("http.secret_key");
  		}
  		return this.configuration;
	}
	
	/**
	* construct Socket object
	* @param ip_addr ip address or hostname
	* @param port port number
	* @return connected Socket object
	*/
	public static Socket getSocket(String ip_addr, int port) throws IOException
	{
		Socket sock = new Socket();
		sock.setSoTimeout(Configuration.g_network_timeout);
		sock.connect(new InetSocketAddress(ip_addr, port), Configuration.g_connect_timeout);
		return sock;
	}
		
	/**
	* construct Socket object
	* @param addr InetSocketAddress object, including ip address and port
	* @return connected Socket object
	*/
	public static Socket getSocket(InetSocketAddress addr) throws IOException
	{
		Socket sock = new Socket();
		sock.setSoTimeout(Configuration.g_network_timeout);
		sock.connect(addr, Configuration.g_connect_timeout);
		return sock;
	}
	
	public static int getG_connect_timeout()
	{
		return g_connect_timeout;
	}
	
	public static void setG_connect_timeout(int connect_timeout)
	{
		Configuration.g_connect_timeout = connect_timeout;
	}
	
	public static int getG_network_timeout()
	{
		return g_network_timeout;
	}
	
	public static void setG_network_timeout(int network_timeout)
	{
		Configuration.g_network_timeout = network_timeout;
	}
	
	public static String getG_charset()
	{
		return g_charset;
	}
	
	public static void setG_charset(String charset)
	{
		Configuration.g_charset = charset;
	}
	
	public static int getG_tracker_http_port()
	{
		return g_tracker_http_port;
	}
	
	public static void setG_tracker_http_port(int tracker_http_port)
	{
		Configuration.g_tracker_http_port = tracker_http_port;
	}
	
	public static boolean getG_anti_steal_token()
	{
		return g_anti_steal_token;
	}
	
	public static boolean isG_anti_steal_token()
	{
		return g_anti_steal_token;
	}
	
	public static void setG_anti_steal_token(boolean anti_steal_token)
	{
		Configuration.g_anti_steal_token = anti_steal_token;
	}
	
	public static String getG_secret_key()
	{
		return g_secret_key;
	}
	
	public static void setG_secret_key(String secret_key)
	{
		Configuration.g_secret_key = secret_key;
	}
	
	public static TrackerGroup getG_tracker_group()
	{
		return g_tracker_group;
	}
	
	public static void setG_tracker_group(TrackerGroup tracker_group)
	{
		Configuration.g_tracker_group = tracker_group;
	}
	
	public TrackerServer build() throws IOException
	{
		TrackerClient trackerClient = new TrackerClient();
		return trackerClient.getConnection();
	}
}
