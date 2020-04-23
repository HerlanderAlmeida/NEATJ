package net;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class Network
{
	private static final Gson gson = new Gson();
	
	private int inputUnits;
	private int outputUnits;
	private int biasUnits;
	private List<Link> connections;
	
	public Network(String filename)
	{
		try(var reader = new JsonReader(new BufferedReader(new FileReader(filename))))
		{
			var net = (Network) gson.fromJson(reader, Network.class);
			this.inputUnits = net.inputUnits;
			this.outputUnits = net.outputUnits;
			this.biasUnits = net.biasUnits;
			this.connections = net.connections;
		}
		catch(IOException e)
		{
			var exc = new IllegalStateException("Failed to initialize network!");
			exc.initCause(e);
			throw exc;
		}
	}
	
	public String toJson()
	{
		return gson.toJson(this);
	}
	
	public String toString()
	{
		return toJson();
	}
}
