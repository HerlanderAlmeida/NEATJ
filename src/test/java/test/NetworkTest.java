package test;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import com.google.gson.JsonParser;

import network.Network;
import network.neuron.Neuron;
import utils.ResourceUtils;

public class NetworkTest
{
	/**
	 * This test checks if evaluation can survive a recurrent network
	 * and not die from an infinite loop.
	 */
	@Test
	@Timeout( value = 10, unit = TimeUnit.SECONDS )
	public void testCycle()
	{
		var network = new Network(3, 1, 0, false);
		for(var neuron = 3; neuron < 8; neuron++)
		{
			network.addNeuron(Neuron.newHidden());
		}
		// inputs|outputs|no biases|hidden
		// 0 1 2 |3 | |4 5 6 7 8
		network.evaluate(new double[]
			{ 1, 2, 3 });
		var map = new TreeMap<Integer, List<Integer>>(Comparator.reverseOrder());
		map.put(0, List.of(4, 6, 8));
		map.put(1, List.of(5));
		map.put(2, List.of(6));
		map.put(4, List.of(5));
		map.put(5, List.of(6, 7));
		map.put(6, List.of(4, 5, 7));
		map.put(7, List.of(4, 3));
		map.put(8, List.of(3));
		for(var entry : map.entrySet())
		{
			var from = entry.getKey();
			for(var to : entry.getValue())
			{
				network.addConnection(from, to, -1);
			}
		}
		// 0 -> 4, 6, 8
		// 1 -> 5
		// 2 -> 6
		// 4 -> 5
		// 5 -> 6, 7
		// 6 -> 4, 5, 7
		// 7 -> 4, output
		// 8 -> 3
		// proper order is: 0, 1, 2, [6, 4, 5], 7, 3 with 8 somewhere in the []
		var output = network.evaluate(new double[]
			{ 1, 2, 3 });
		Assertions.assertFalse(output[0] < 0.01 && output[0] > -0.01);
	}

	@Test
	public void testLoop()
	{
		var input = Neuron.newInput();
		var output = Neuron.newOutput();
		var bias = Neuron.newBias();
		var hidden = Neuron.newHidden();
		assertThrows(UnsupportedOperationException.class, () -> input.addInput(input, 0));
		assertThrows(UnsupportedOperationException.class, () -> output.addOutput(output, 0));
		assertThrows(UnsupportedOperationException.class, () -> bias.addInput(bias, 0));
		assertThrows(IllegalArgumentException.class, () -> hidden.addInput(hidden, 0));
		assertThrows(IllegalArgumentException.class, () -> hidden.addOutput(hidden, 0));
	}

	@Test
	public void testNeurons()
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
		for(var neuron = 0; neuron < 3; neuron++)
		{
			neurons[neuron] = Neuron.newInput();
		}
		for(var neuron = 3; neuron < neurons.length; neuron++)
		{
			neurons[neuron] = Neuron.newHidden();
		}
		neurons[0].value(1);
		neurons[1].value(2);
		neurons[2].value(3);
		neurons[0].addOutput(neurons[3], 0.5);
		neurons[1].addOutput(neurons[3], 0.6);
		neurons[0].addOutput(neurons[4], 1);
		neurons[1].addOutput(neurons[4], 2);
		neurons[1].addOutput(neurons[5], 1);
		neurons[2].addOutput(neurons[5], 2);
		neurons[2].addOutput(neurons[6], 1);
		neurons[3].addOutput(neurons[6], 2);
		neurons[4].addOutput(neurons[7], -0.2);
		neurons[5].addOutput(neurons[7], -0.2);
		neurons[6].addOutput(neurons[7], -2);
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
			var net = new Network(ResourceUtils
				.resourceAsString(getClass().getResourceAsStream("/test_network.json")));
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