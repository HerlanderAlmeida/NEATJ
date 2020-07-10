package test;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import net.connection.Connection;
import net.neuron.Neuron;

public class NetworkTest
{
	@Test
	public void testNeurons() throws ReflectiveOperationException
	{
		Neuron[] neurons = new Neuron[8];
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
		neurons[7] = new Neuron(List.of(new Connection(neurons[4], 0.2),
			new Connection(neurons[5], 0.2), new Connection(neurons[6], 0.2)));
		for(int i = 3; i < neurons.length; i++)
		{
			neurons[i].update();
		}
		
		Assertions.assertEquals(1.7, neurons[3].value(), 1e-10);
		Assertions.assertEquals(5, neurons[4].value(), 1e-10);
		Assertions.assertEquals(8, neurons[5].value(), 1e-10);
		Assertions.assertEquals(6.4, neurons[6].value(), 1e-10);
		Assertions.assertEquals(3.88, neurons[7].value(), 1e-10);
	}
	
//	@Test
//	public void testNetworkFromFile()
//	{
//		Network net = new Network("resources/net/test_network.json");
//		try (var reader = new JsonReader(new FileReader("resources/net/test_network.json"));
//			var scanner = new Scanner(new File("resources/net/test_network.json")))
//		{
//			String content = scanner.useDelimiter("\\Z").next();
//			var elem1 = JsonParser.parseString(content);
//			var elem2 = JsonParser.parseString(net.toJson());
//			Assertions.assertEquals(elem1, elem2);
//		}
//		catch(Exception e)
//		{
//			Assertions.assertEquals(true, false);
//		}
//	}
}