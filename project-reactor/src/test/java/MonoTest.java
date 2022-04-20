import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

/**
 * Mono: one of the Publisher interface implementations.
 * This implementation allows us to return from 0 to 1 elements.
 */
class MonoTest {

    /**
     * Test case: how to create a simple Mono using "just()" method.
     * This method only accepts one argument, because we are working with Mono
     */
    @Test
    void firstMono() {
        Mono.just("A");
    }

    /**
     * Test case: how to create a Mono using "just()" method and seeing every event in the sequence
     *            using "log()" operator.
     * The "log()" operator uses logging frameworks as Log4j, SLF4J... but, if there are no logging frameworks then
     * it logs to console.
     *
     * To see the log sequence we need to subscribe to the publisher, so we need to use "subscribe()" method.
     *
     * If we want to do something with the published element, we can pass a lambda expression or method reference
     * inside the "subscribe()" method. In this case we are printing the value
     */
    @Test
    void monoWithConsumer() {
        Mono.just("A")
                .log()
                .subscribe(System.out::println);
    }

    /**
     * Test case: how to create a Mono using "just()" method and how to do something when we subscribe, an error
     *            happens...
     *
     *  "doOnSubscribe()" method: this is executed after the subscription is made.
     *  "doOnRequest()" method: this is executed before the request method.
     *  "doOnSuccess()" method: this is executed when the Mono completes successfully
     */
    @Test
    void monoWithDoOn() {
        Mono.just("A")
                .log()
                .doOnSubscribe(subs -> System.out.println("Subscribed: " + subs))
                .doOnRequest(request -> System.out.println("Request: " + request))
                .doOnSuccess(complete -> System.out.println("Complete: " + complete))
                .subscribe(System.out::println);
    }

    /**
     * Test case: how to create an empty Mono with "empty()" method. These types of Monos don't publish any value.
     *
     * Empty Monos are useful to emulate the "void" return in traditional programming.
     * In this case the "onNext()" method is never called, no value is printed and only "onComplete()" method
     * is called.
     */
    @Test
    void emptyMono() {
        Mono.empty()
                .log()
                .subscribe(System.out::println);
    }

    /**
     * Test case: how to create an empty Mono with "empty()" method and using consumers to do something.
     *
     * In this case we are using the "subscribe" implementation that takes all arguments: the first is a consumer,
     * the second is an error consumer and the third is a complete consumer.
     *
     * We use the last argument to do something because we are working with an empty Mono that does not publish any
     * value and only "onComplete()" method is called.
     */
    @Test
    void emptyCompleteConsumerMono() {
        Mono.empty()
                .log()
                .subscribe(System.out::println,
                        null,
                        () -> System.out.println("Done")
                );
    }

    /**
     * Test case: how to manage unchecked exceptions using "error()" method.
     *
     * In this example the subscription is done and then an error is thrown. The error is thrown after the "request"
     * is done and then a stack trace is printed
     */
    @Test
    void errorRuntimeExceptionMono() {
        Mono.error(new RuntimeException())
                .log()
                .subscribe();
    }

    /**
     * Test case: how to manage checked exceptions using "error()" method.
     *
     * In traditional programming, we would need a try-catch statement, but in reactive programming it does not apply.
     *
     * In this example the subscription is done and then an error is thrown. The error is thrown after the "request"
     * is done and then a stack trace is printed
     */
    @Test
    void errorExceptionMono() {
        Mono.error(new Exception())
                .log()
                .subscribe();
    }

    /**
     * Test case: how to access the exception thrown using a consumer.
     *
     * To do this, we need to use another implementation of "subscribe()" method. In this case, we are using the one
     * that takes two arguments, since the second argument is an error consumer.
     *
     * With this example, we are simulating a traditional try-catch statement
     */
    @Test
    void errorConsumerMono() {
        Mono.error(new Exception())
                .log()
                .subscribe(System.out::println,
                        e -> System.out.println("Error: " + e)
                );
    }

    /**
     * Test case: how to access the exception thrown using "doOnError()" method.
     *
     * Using "doOnError()" method has a similar effect as using a "subscribe()" implementation.
     */
    @Test
    void errorDoOnErrorMono() {
        Mono.error(new Exception())
                .doOnError(e -> System.out.println("Error: " + e))
                .log()
                .subscribe();
    }

    /**
     * Test case: how to return a Mono when an exception is thrown using "onErrorResume()" method.
     *
     * Using "onErrorResume()" method simulates catching the exception, swallowing the exception and then returning
     * a new Mono.
     */
    @Test
    void errorOnErrorResumeMono() {
        Mono.error(new Exception())
                .onErrorResume(e -> {
                    System.out.println("Caught: " + e);
                    return Mono.just("B");
                })
                .log()
                .subscribe();
    }

    /**
     * Test case: how to return a using "onErrorReturn()" method.
     *
     * We can use "onErrorReturn()" method when we want to return a value instead of a Mono.
     */
    @Test
    void errorOnErrorReturnMono() {
        Mono.error(new Exception())
                .onErrorReturn("B")
                .log()
                .subscribe();
    }
}
