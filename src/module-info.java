module avi
{
	exports temp;
	exports genetic;
	
	requires org.junit.jupiter.api;
	requires com.google.gson;
		opens network to com.google.gson;
		opens network.neuron to com.google.gson;
		opens network.connection to com.google.gson;
}