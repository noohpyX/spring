import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Arrays;
import java.util.Random;

/**
 * Flux: one of the Publisher interface implementations.
 * This implementation allows us to return from 0 to N elements.
 *
 * In these test cases we won't cover error management because they are the same as in Mono.
 */
class FluxTest {

    /**
     * Test case: how to create a simple Flux using "just()" method and seeing every event in the sequence using
     *            "log()" operator.
     * To see the log sequence we need to subscribe to the publisher, so we need to use "subscribe()" method.
     *
     * The difference of "just()" method in Mono and Flux is with Mono we can only pass an argument and with Flux
     * we can pass more than one.
     *
     */
    @Test
    void firstFlux() {
        Flux.just("A", "B", "C")
                .log()
                .subscribe();
    }

    /**
     * Test case: how to create a Flux from an iterable(like a list, for example) using "fromIterable()" method.
     *
     * When we use this method, we cannot use "just()" because the values will be passed to the subscriber in one
     * "onNext" call.
     */
    @Test
    void fluxFromIterable() {
        Flux.fromIterable(Arrays.asList("A", "B", "C"))
                .log()
                .subscribe();
    }

    /**
     * Test case: how to create a Flux from a range using "range()" method.
     *
     * The "range()" method accepts 2 parameters:
     *      - First parameter: starting value
     *      - Second parameter: total number of elements that will be published.
     *
     * Note: the range only accepts integers as values
     */
    @Test
    void fluxFromRange() {
        Flux.range(10, 5)
                .log()
                .subscribe();
    }

    /**
     * Test case: how to create a Flux that emits values starting from 0 and implementing them at specific time
     *            intervals.
     *
     * The "interval()" method publishes elements at regular intervals while running on another thread, and it does not
     * block the main thread, so we cannot see the log sequence.
     * To see the log sequence we need to sleep the main thread.
     *
     * Note: we have to take care using this method in production environments because it will emit values infinitely
     *       since the application will always be running.
     *       To avoid this problem we can use "take()" method to take only the first N values of the Flux. That will
     *       cause the completion of the Flux by cancelling the subscription and not completing it.
     * @throws Exception
     */
    @Test
    void fluxFromInterval() throws Exception {
        Flux.interval(Duration.ofSeconds(1))
                .log()
                .take(2)
                .subscribe();
        //Thread.sleep(5000);
    }

    /**
     * Test case: how to create a Flux from a range and using a backpressure mechanism.
     *
     * We can use the "subscribe()" method that allows a fourth parameter, but depends on Reactor version. This new
     * parameter is a consumer that allows us to do something at the time of the subscription. In this example we are
     * requesting an initial number of elements.
     *
     * If we are using a version of Reactor that does not allow the fourth parameter, we can pass an implementation
     * of the Subscriber interface(BaseSubscriber in our case).
     *
     */
    @Test
    void fluxRequest() {
        Flux.range(1, 5)
                .log()
                //.subscribe(null,
                //      null,
                //      null,
                //      s -> s.request(3)
                //);
                .subscribe(new BaseSubscriber<Integer>() {
                    @Override
                    protected void hookOnSubscribe(Subscription s) {
                        s.request(3L);
                    }
                });
    }

    /**
     * Test case: using a custom subscriber to implement some logic.
     *
     *
     */
    @Test
    void fluxCustomSubscriber() {
        Flux.range(1, 10)
                .log()
                .subscribe(new BaseSubscriber<Integer>() {
                    int elementsToProcess = 3;
                    int counter = 0;

                    // Adding a hook to request only N elements.
                    public void hookOnSubscribe(Subscription subscription) {
                        System.out.println("Subscribed!");
                        request(elementsToProcess);
                    }

                    // This function will be called until all requested elements are published
                    public void hookOnNext(Integer value) {
                        counter++;
                        if(counter == elementsToProcess) {
                            counter = 0;

                            Random r = new Random();
                            elementsToProcess = r.ints(1, 4)
                                    .findFirst().getAsInt();
                            request(elementsToProcess);
                        }
                    }
                });
    }

    /**
     * Test case: how to request a fixed numbers of elements every time.
     *
     * To achieve this, we can use the "limitRate()" operator.
     * In this case, the "request()" method in the log sequence is called every N "onNext()" calls until the Flux
     * completes.
     * The N is set in the argument of "limitRate()" method.
     *
     * The log sequence would be: request(N) -> onNext(from range start to N) -> request(N) ->
     * onNext(from last value before request call to N) -> onComplete().
     */
    @Test
    void fluxLimitRate() {
        Flux.range(1, 5)
                .log()
                .limitRate(3)
                .subscribe();
    }
}
