package net.betaengine;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

public class SquareRootStream {
    interface Stream<A> extends Function<Integer, A> {
        boolean isEmpty();
        A head();
        Stream<A> tail();
        
        default Stream<A> filter(Predicate<A> p) {
            if (isEmpty()) return this;
            else if (p.test(head())) return cons(head(), () -> tail().filter(p));
            else return tail().filter(p);
        }
        
        default <R> Stream<R> map(Function<A, R> mapper) {
            if (isEmpty()) return empty();
            else return cons(mapper.apply(head()), () -> tail().map(mapper));
        }
        
        @Override
        default A apply(Integer i) {
            if (i == 0) return head();
            else return tail().apply(i - 1);
        }
        
        static <B> Stream<B> cons(B hd, Supplier<Stream<B>> tl) {
            return new Stream<B>() {
                @Override
                public boolean isEmpty() { return false; }

                @Override
                public B head() { return hd; }
                
                private Supplier<Stream<B>> lazyTail = Suppliers.memoize(tl);

                @Override
                public Stream<B> tail() { return lazyTail.get(); }
            };
        }
        
        static Stream<Void> EMPTY = new Stream<Void>() {
            @Override
            public boolean isEmpty() { return true; }

            @Override
            public Void head() { throw new NoSuchElementException("empty.head"); }

            @Override
            public Stream<Void> tail() { throw new NoSuchElementException("empty.tail"); }
        };
        
        @SuppressWarnings("unchecked")
        static <B> Stream<B> empty() {
            return (Stream<B>)EMPTY;
        }
    }
    
static int total = 0;
    private Stream<Double> sqrtStream(double x) {
//        Function<Double, Double> improve = guess -> (guess + x / guess) / 2;
        Function<Double, Double> improve = guess -> {
            total++;
            return (guess + x / guess) / 2;
        };
        
        // The atomic reference here is a way to get around the problem that we can't refer to guesses on the RHS of our assignment
        // to guesses itself - the compiler would complain that "The local variable guesses may not have been initialized."
        //
        // In Scala the `lazy val` gets around this ordering problem.
        //
        // What's important is that all references to guesses refer to the same value. So is the memoization aspect of `lazy val`
        // important in this case?
        //
        // Under the covers `lazy val` is implemented as a method call with a backing variable so it's important there that the method
        // call remembers its result in the backing variable (rather than recalculating it) - see http://stackoverflow.com/a/23856501
        //
        // We do capture guesses in the closure created for the reference.set line. So we do hang onto the value in order to use it
        // again later but I wouldn't call this memoization in the sense of "an optimization technique used primarily to speed up
        // computer programs by storing the results of expensive function calls and returning the cached result when the same inputs
        // occur again." (Wikipedia).
        //
        // In any case here it's clear that the reference to guesses in the reference.set step will not result in guesses being
        // reevaluated, i.e. a new Stream.cons(1d, ...) being created.
        //
        // So the crucial laziness is the lazy Stream.tail.
        //
        AtomicReference<Supplier<Stream<Double>>> reference = new AtomicReference<>();
        Stream<Double> guesses = Stream.cons(1d, () -> reference.get().get());
        
        reference.set(() -> guesses.map(improve));
        
        return guesses;
    }

    // A horrible non-functional implementation of take(n).toList.
    private <R> List<R> take(Stream<R> s, int count) {
        List<R> result = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            result.add(s.head());
            s = s.tail();
        }
        
        return result;
    }
    
    private void run() {
        Stream<Double> sqrtStream = sqrtStream(4);
        
        System.out.println(take(sqrtStream, 7));
        System.out.println(total);
    }
    
    public static void main(String[] args) {
        new SquareRootStream().run();
    }
}
