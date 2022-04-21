import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Test class to show the most common operators used when we work with Flux and Mono
 */
class OperatorTest {

    /**
     * Test case: how to use "map()" operator.
     *
     * This operator let us transform an element into another.
     * The transformation of each element is performed by a synchronous function, so we don't need to return a Flux
     * or Mono.
     */
    @Test
    void map() {
        Flux.range(1, 5)
                .map(i -> i * 10)
                .subscribe(System.out::println);
    }

    /**
     * Test case: how to use "flatMap()" operator.
     *
     * This operator is similar to "map()", but the difference is that the transformation of each element is performed
     * by an asynchronous function.
     * In this case, we need to return a Flux or Mono
     *
     * Another thing to mention is, in the end, this operator will flatten all the publishers into a single one.
     */
    @Test
    void flatMap() {
        Flux.range(1, 5)
                .flatMap(i -> Flux.range(i*10, 2))
                .subscribe(System.out::println);
    }

    /**
     * Test case: how to use "flatMapMany()" operator.
     *
     * This operator only applies to Mono, and let us transform a Mono into a Flux
     */
    @Test
    void flatMapMany() {
        Mono.just(3)
                .flatMapMany(i -> Flux.range(1, i))
                .subscribe(System.out::println);
    }

    /**
     * Test case: how to use "concat()" operator.
     *
     * In this example we are concatenating two Flux into a single one.
     *
     * We can use the static method of Flux, "concat()", to concatenate any number of publishers, or we can use
     * "concatWith()" method on a Mono or Flux instance.
     *
     * If we use "concatWith()", we are always going to have a Flux as a result, because the concatenation will have at
     * least two elements.
     *
     * Note: the concatenation is done in a sequential way, so the order is preserved.
     * @throws InterruptedException
     */
    @Test
    void concat() throws InterruptedException {
        Flux<Integer> oneToFive = Flux.range(1, 5)
                // Delays the publishing of each element by a given duration
                .delayElements(Duration.ofMillis(200));
        Flux<Integer> sixToTen = Flux.range(6, 5)
                .delayElements(Duration.ofMillis(400));

        // Example of static method Flux.concat()
        Flux.concat(oneToFive, sixToTen)
                .subscribe(System.out::println);

        // Example of "concatWith()" method on a Flux instance
        //oneToFive.concatWith(sixToTen)
        //      .subscribe(System.out::println);

        // Both Flux work in parallel, so we need to give time to see the action
        //Thread.sleep(4000);
    }


    /**
     * Test case: how to use "merge()" operator.
     *
     * In this example we are merging two Flux into a single one.
     *
     * We can use the static method of Flux, "merge()", to merge any number of publishers, or we can use
     * "mergeWith()" method on a Mono or Flux instance.
     *
     * If we use "mergeWith()", we are always going to have a Flux as a result, because the merging will have at least
     * two elements.
     *
     * Note: merge does not sequentially combine the elements, so the order is not preserved.
     * @throws InterruptedException
     */
    @Test
    void merge() throws InterruptedException  {
        Flux<Integer> oneToFive = Flux.range(1, 5)
                .delayElements(Duration.ofMillis(200));
        Flux<Integer> sixToTen = Flux.range(6, 5)
                .delayElements(Duration.ofMillis(400));

        // Example of static method Flux.concat()
        Flux.merge(oneToFive, sixToTen)
                .subscribe(System.out::println);

        // Example of "concatWith()" method on a Flux instance
        //oneToFive.mergeWith(sixToTen)
        //        .subscribe(System.out::println);

        // Both Flux work in parallel, so we need to give time to see the action
        //Thread.sleep(4000);
    }

    /**
     * Test case: how to use "zip()" operator.
     *
     * This operator let us combine two or more publishers(Flux or Mono) by waiting for all the sources to emit one
     * element and combine these elements into an output value according to the provided function.
     *
     * We can use the static method of Flux or Mono, "zip()", to combine any number of publishers, or we can use
     * "zipWith()" method on a Mono or Flux instance.
     */
    @Test
    void zip()  {
        Flux<Integer> oneToFive = Flux.range(1, 5);
        Flux<Integer> sixToTen = Flux.range(6, 5);

        Flux.zip(oneToFive, sixToTen,
                        (item1, item2) -> item1 + ", " + + item2)
                .subscribe(System.out::println);

        //oneToFive.zipWith(sixToTen)
        //        .subscribe(System.out::println);
    }
}
