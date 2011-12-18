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
package tesuji.games.go.main;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import tesuji.core.util.ClassPathLoader;
//import tesuji.games.go.client.GoDataWindow;
import tesuji.games.go.common.GoEngine;
import tesuji.games.go.common.GoMove;
import tesuji.games.gtp.GameEngineGTPSocketConnector;

/**
 * Load an engine 
 */
public class GoEngineGTPSocketMain
	extends GameEngineGTPSocketConnector<GoMove>
{
    public static void main(String[] args)
	{
    	GoEngineGTPSocketMain main = new GoEngineGTPSocketMain();

    	String engineName = "GoEngine";
		
		GoEngine engine = null;

		try
		{
			System.err.println("Load XML");
			Resource resource = new FileSystemResource("GoEngineGTP.xml");
			XmlBeanFactory factory = new XmlBeanFactory(resource);
			BeanDefinition beanDefinition = factory.getBeanDefinition(engineName);
			MutablePropertyValues properties = beanDefinition.getPropertyValues();
			PropertyValue value = properties.getPropertyValue("jar");
			String strValue = value.getValue().toString();
			int beginIndex = strValue.indexOf('[')+1;
			int endIndex = strValue.indexOf(']');
			String jarName = strValue.substring(beginIndex,endIndex);
			System.err.println("Load JAR " + jarName);
//			String jarName = Properties.getString(Properties.JAR_NAME);
			ClassPathLoader.addFile(jarName);

			System.err.println("Load Bean");
			engine = (GoEngine) factory.getBean(engineName);
			
//			System.err.println("Load Class");
//			String engineClassName = Properties.getString(Properties.GO_ENGINE_CLASS);
//			Class<?> engineClass = ClassLoader.getSystemClassLoader().loadClass(engineClassName);
//			engine = (GoEngine) engineClass.newInstance();
		}
		catch (Exception exception)
		{
			System.err.println(exception.getMessage());
			
			//engine = new RandomGoEngine();
		}
//		GoDataWindow.show(main.getEngineName());
		main.init(args);
		main.connectEngine(engine);
	}
}
