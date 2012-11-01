package tesuji.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class FileUtil
{
	public static InputStream getInputStream(String fileName, Class<?> handler)
	{
		InputStream inputStream = null;
		if (handler!=null)
		{
			inputStream = handler.getResourceAsStream(fileName);
			if (inputStream!=null)
				return inputStream;
		}
		
		Resource resource = new FileSystemResource(fileName);
		File file;
		try
		{
			file = resource.getFile();
			inputStream = new FileInputStream(file);
		}
		catch (IOException exception)
		{
			System.err.println("Couldn't read file: "+fileName);
			System.exit(-1);
		}
		return inputStream;
	}
	
	public static URL getResourceURL(String fileName, Class<?> handler)
	{
		URL url = null;
		if (handler!=null)
		{
			url = handler.getResource(fileName);
			if (url!=null)
				return url;
		}
		
		Resource resource = new FileSystemResource(fileName);
		try
		{
			url = resource.getURL();
		}
		catch (IOException exception)
		{
			System.err.println("Couldn't read file: "+fileName);
			System.exit(-1);
		}
		return url;
	}
}
