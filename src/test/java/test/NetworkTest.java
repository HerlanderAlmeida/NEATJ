package test;

import java.util.List;
import java.util.Scanner;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonParser;

import network.Network;
import network.connection.Connection;
import network.neuron.Neuron;
import utils.ResourceUtils;

public class NetworkTest
{
	@Test
	public void testNeurons() throws ReflectiveOperationException
	{
		var neurons = new Neuron[8];
		// first 3 neurons are input neurons, with values 1, 2, and 3
		// 0 and 1 will be connected to 3 with weights 0.5 and 0.6
		// neuron 0 = 1
		// neuron 1 = 2
		// neuron 2 = 3
		// neuron 3 = 1.7
		// neurons 0 and 1 will be connected to 4 with weights 1 and 2
		// neurons 1 and 2 will be connected to 5 with weights 1 and 2
		// neurons 2 and 3 will be connected to 6 with weights 1 and 2
		// neuron 4 = 5
		// neuron 5 = 8
		// neuron 6 = 6.4
		// neurons 4, 5, and 6 will be connected to 7 with weights 0.2
		// neuron 7 = 3.88
		neurons[0] = new Neuron();
		neurons[0].value(1);
		neurons[1] = new Neuron();
		neurons[1].value(2);
		neurons[2] = new Neuron();
		neurons[2].value(3);
		neurons[3] = new Neuron(List.of(new Connection(neurons[0], 0.5), new Connection(neurons[1], 0.6)));
		neurons[4] = new Neuron(List.of(new Connection(neurons[0], 1), new Connection(neurons[1], 2)));
		neurons[5] = new Neuron(List.of(new Connection(neurons[1], 1), new Connection(neurons[2], 2)));
		neurons[6] = new Neuron(List.of(new Connection(neurons[2], 1), new Connection(neurons[3], 2)));
		neurons[7] = new Neuron(List.of(new Connection(neurons[4], -0.2),
			new Connection(neurons[5], -0.2), new Connection(neurons[6], -2)));
		for(var i = 3; i < neurons.length; i++)
		{
			neurons[i].update();
		}

		Assertions.assertTrue(neurons[3].value() > 0.9);
		Assertions.assertTrue(neurons[4].value() > 0.9);
		Assertions.assertTrue(neurons[5].value() > 0.9);
		Assertions.assertTrue(neurons[6].value() > 0.9);
		Assertions.assertTrue(neurons[7].value() < -0.9);
	}

	@Test
	public void testNetworkFromFile()
	{
		try (var scanner = new Scanner(getClass().getResourceAsStream("/test_network.json")))
		{
			var net = new Network(ResourceUtils.resourceAsString(getClass().getResourceAsStream("/test_network.json")));
			var content = scanner.useDelimiter("\\Z").next();
			var elem1 = JsonParser.parseString(content);
			var elem2 = JsonParser.parseString(net.toJson());
			Assertions.assertEquals(elem1, elem2);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			Assertions.assertEquals(true, false);
		}
	}
}