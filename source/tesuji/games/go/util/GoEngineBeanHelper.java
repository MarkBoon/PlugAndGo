/**
 * Project: Tesuji Go Framework.<br>
 * <br>
 * <font color="#CC6600"><font size=-1> Copyright (c) 1985-2006 Mark Boon<br>
 * All rights reserved.<br>
 * <br>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so,
 * provided that the above copyright notice(s) and this permission notice appear
 * in all copies of the Software and that both the above copyright notice(s) and
 * this permission notice appear in supporting documentation.<br>
 * <br>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR
 * IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.<br>
 * <br>
 * <font color="#00000"><font size=+1>
 * 
 */
package tesuji.games.go.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import tesuji.core.util.ClassPathLoader;
import tesuji.games.go.common.GoEngine;

public class GoEngineBeanHelper
{
	public static Hashtable<String, GoEngine> getGoEngineBeans(String initialXmlFile, String ... engineName)
		throws Exception
	{
		Logger logger = Logger.getRootLogger();
		Hashtable<String, GoEngine> engineList = new Hashtable<String, GoEngine>();

		Resource resource = new FileSystemResource(initialXmlFile);
		File file = resource.getFile();
		file.getAbsolutePath();
		File absoluteFile = file.getAbsoluteFile();
		File directory = absoluteFile.getParentFile();
		FilenameFilter filter = new FilenameFilter()
			{
				public boolean accept(File dir, String name)
				{
					return name.endsWith(".xml");
				}					
			};
		logger.debug("Read xml from directory "+directory.getName());
		File[] xmlFiles = directory.listFiles(filter);
		logger.info("Found "+xmlFiles.length+" xml files");
		for (File xmlFile : xmlFiles)
		{
			try
			{
				Resource xmlResource = new FileSystemResource(xmlFile);
				logger.debug("Loading file "+xmlFile.getName());
				XmlBeanFactory factory = new XmlBeanFactory(xmlResource);
				String[] beanNames = factory.getBeanDefinitionNames();
				for (String bean : beanNames)
				{
					if (engineName==null || containsName(engineName, bean))
					{
						try
						{
							logger.debug("Loading bean "+bean);
							BeanDefinition beanDefinition = factory.getBeanDefinition(bean);
							if (beanDefinition==null)
								throw new Exception("Couldn't find '"+bean+"' definition in XML files.");
							MutablePropertyValues properties = beanDefinition.getPropertyValues();
							PropertyValue value = properties.getPropertyValue("jar");
							if (value!=null)
							{
								String strValue = value.getValue().toString();
								int beginIndex = strValue.indexOf('[')+1;
								int endIndex = strValue.indexOf(']');
								String jarName = strValue.substring(beginIndex,endIndex);
								ClassPathLoader.addFile(jarName);
								GoEngine newEngine = (GoEngine) factory.getBean(bean);
								engineList.put(bean,newEngine);
							}
							else if (bean.equals("SocketEngine"))
							{
								GoEngine newEngine = (GoEngine) factory.getBean(bean);
								engineList.put(bean,newEngine);
							}
						}
						catch (Exception exception)
						{
							// Just keep going.
							logger.error("Unexpected exception "+exception.getClass().getName()+": "+exception.getMessage());
							//exception.printStackTrace();
						}
					}
				}
			}
			catch (Exception exception)
			{
				// Just keep going
				logger.error("Unexpected exception "+exception.getClass().getName()+": "+exception.getMessage());
				//exception.printStackTrace();
			}
		}
		
		return engineList;
	}
	
	private static boolean containsName(String[] names, String name)
	{
		for (String n : names)
			if (n.equals(name))
				return true;
		return false;
	}
	
	public static GoGameProperties getGoGameProperties(String xmlFile)
		throws Exception
	{
		Logger logger = Logger.getRootLogger();

		try
		{
			Resource xmlResource = new FileSystemResource(xmlFile);
			logger.debug("Loading file "+xmlFile);
			XmlBeanFactory factory = new XmlBeanFactory(xmlResource);
			GoGameProperties gameProperties = (GoGameProperties) factory.getBean("goGameProperties");
			return gameProperties;
		}
		catch (Exception exception)
		{
			// Just keep going
			logger.error("Unexpected exception "+exception.getClass().getName()+": "+exception.getMessage());
			//exception.printStackTrace();
			return null;
		}
	}
	
	public static String selectEngineFromList(Hashtable<String,GoEngine> engineList)
	{
		String[] names = new String[engineList.size()];
		int index = 0;
		Enumeration<String> enumeration = engineList.keys();
		while (enumeration.hasMoreElements())
			names[index++] = enumeration.nextElement();
		Arrays.sort(names);
		engineList.keys();
		String selectedEngine = 
			(String) JOptionPane.showInputDialog(null,"Select an engine","Select an engine",JOptionPane.QUESTION_MESSAGE,null,names,names[0]);
		return selectedEngine;
	}
}
