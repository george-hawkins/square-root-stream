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
        
        AtomicReference<Supplier<Stream<Double>>> reference = new AtomicReference<>();
        // That both guesses and tail 
        Supplier<Stream<Double>> guesses = Suppliers.memoize(() -> Stream.cons(1d, () -> reference.get().get()));
        
        reference.set(Suppliers.memoize(() -> guesses.get().map(improve)));
        
        return guesses.get();
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
