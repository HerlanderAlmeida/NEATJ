package genetic.crossover;

import java.util.function.BinaryOperator;

import genetic.Individual;

public interface CrossoverMethod<T extends Individual> extends BinaryOperator<T>
{
}
