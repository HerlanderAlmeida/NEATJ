package utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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

	public static void deleteFile(String filename) throws IOException
	{
		Paths.get(absolute(filename)).toFile().delete();
	}

	public static void addToFile(String filename, String output) throws IOException
	{
		var path = Paths.get(absolute(filename));
		Files.write(path, output.getBytes(), StandardOpenOption.WRITE, StandardOpenOption.APPEND,
			StandardOpenOption.CREATE);
	}

	public static <T> T readObjectFromFile(String filename, Type typeOfT) throws IOException
	{
		var path = Paths.get(absolute(filename));
		try (var reader = Files.newBufferedReader(path))
		{
			return GsonUtils.fromJson(reader, typeOfT);
		}
	}

	public static <T> T readObjectFromFile(String filename, Class<T> classOfT) throws IOException
	{
		var path = Paths.get(absolute(filename));
		try (var reader = Files.newBufferedReader(path))
		{
			return GsonUtils.fromJson(reader, classOfT);
		}
	}

	public static void writeObjectToFile(String filename, Object object) throws IOException
	{
		var output = GsonUtils.toJson(object);
		writeToFile(filename, output);
	}

	public static void writeToFile(String filename, String output) throws IOException
	{
		var path = Paths.get(absolute(filename));
		Files.write(path, output.getBytes());
	}

	private static String absolute(String filename)
	{
		var path = Paths.get(filename);
		if(!path.equals(path.toAbsolutePath()) || !path.toFile().exists())
		{
			if(path.startsWith("/"))
			{
				return System.getProperty("user.dir") + filename;
			}
			else
			{
				return System.getProperty("user.dir") + "/" + filename;
			}
		}
		return filename;
	}
}
