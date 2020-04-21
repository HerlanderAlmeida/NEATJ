package test;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import net.connection.MultiplierConnection;
import net.neuron.Neuron;

public class NetworkTest
{
	@Test
	public void testNeurons() throws Exception
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
		neurons[0].setValue(1);
		neurons[1] = new Neuron();
		neurons[1].setValue(2);
		neurons[2] = new Neuron();
		neurons[2].setValue(3);
		var ctor = MultiplierConnection.class.getDeclaredConstructor(Neuron.class, double.class);
		ctor.setAccessible(true);
		neurons[3] = new Neuron(List.of(ctor.newInstance(neurons[0], 0.5), ctor.newInstance(neurons[1], 0.6)));
		neurons[4] = new Neuron(List.of(ctor.newInstance(neurons[0], 1), ctor.newInstance(neurons[1], 2)));
		neurons[5] = new Neuron(List.of(ctor.newInstance(neurons[1], 1), ctor.newInstance(neurons[2], 2)));
		neurons[6] = new Neuron(List.of(ctor.newInstance(neurons[2], 1), ctor.newInstance(neurons[3], 2)));
		neurons[7] = new Neuron(List.of(ctor.newInstance(neurons[4], 0.2),
			ctor.newInstance(neurons[5], 0.2), ctor.newInstance(neurons[6], 0.2)));
		ctor.setAccessible(false);
		for(int i = 3; i < neurons.length; i++)
		{
			neurons[i].update();
		}
		
		Assertions.assertEquals(1.7, neurons[3].getValue(), 1e-10);
		Assertions.assertEquals(5, neurons[4].getValue(), 1e-10);
		Assertions.assertEquals(8, neurons[5].getValue(), 1e-10);
		Assertions.assertEquals(6.4, neurons[6].getValue(), 1e-10);
		Assertions.assertEquals(3.88, neurons[7].getValue(), 1e-10);
	}
}