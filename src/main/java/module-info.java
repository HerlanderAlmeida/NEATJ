open module avi
{
	exports genetic;
	exports genetic.crossover;
	exports genetic.evaluate;
	exports genetic.function;
	exports genetic.generics;
	exports genetic.genome;
	exports genetic.mutation;
	exports genetic.repopulate;
	exports genetic.selection;
	exports genetic.selection.method;
	exports neat;
	exports network;
	exports network.connection;
	exports network.neuron;

	requires org.junit.jupiter.api;
	requires org.junit.platform.commons;
	requires com.google.gson;
}