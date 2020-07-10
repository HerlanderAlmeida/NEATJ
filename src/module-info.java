module avi
{
	exports temp;
	exports genetic;
	
	requires org.junit.jupiter.api;
	requires com.google.gson;
		opens net to com.google.gson;
		opens net.neuron to com.google.gson;
		opens net.connection to com.google.gson;
}