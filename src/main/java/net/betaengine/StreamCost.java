package net.betaengine;

import java.util.stream.IntStream;

// This class demonstrates that unlike the Scala Stream class the Java Stream class has no heavy upfront creation cost.
// Unlike Scala where the definition on stream below would cause the first element of the stream to be found (even if it was later never consumed)
// the Java version will only start triggering calls to isPrime once you start consuming values from it.
public class StreamCost {
    private boolean isPrime(int n) {
        System.out.println("Checking if " + n + " is a prime.");
        return IntStream.range(2, n).allMatch(x -> n % x != 0);
    }
    
    private void run() {
        IntStream stream = IntStream.range(1000, 10001).filter(this::isPrime);
        
        System.out.println("Finding second prime...");

        stream.skip(1).findFirst().ifPresent(n -> System.out.println("Second prime: " + n));
    }

    public static void main(String[] args) {
        new StreamCost().run();
    }
}
