package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class ResourceUtils
{
	/**
	 * Gets resource from root resources
	 */
	public static String resourceAsString(String fileName) throws IOException
	{
		var classLoader = Thread.currentThread().getContextClassLoader();
		try (var is = classLoader.getResourceAsStream(fileName))
		{
			return resourceAsString(is);
		}
	}

	/**
	 * Gets resource from InputStream <code>is</code>'s resources
	 */
	public static String resourceAsString(InputStream is) throws IOException
	{
		if(is == null)
		{
			return null;
		}
		try (var isr = new InputStreamReader(is); var reader = new BufferedReader(isr))
		{
			return reader.lines().collect(Collectors.joining(System.lineSeparator()));
		}
	}
}
