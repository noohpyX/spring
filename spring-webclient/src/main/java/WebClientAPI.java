import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * WebClient configuration class. In this class we are going to create a WebClient and also see how to
 * make requests with it.
 *
 * The WebClient is a client for making HTTP requests using Reactor and Netty, and, for that reason provides a
 * non-blocking reactive API that accepts and returns publishers.
 */
public class WebClientAPI {
    private WebClient webClient;

    // How to create a WebClient

    // Default implementation
    private WebClient defaultWebClient = WebClient.create();
    // Implementation with base URL
    private WebClient baseUrlWebClient = WebClient.create("https://localhost:8080");
    // Implementation using Builder Pattern. With this option we can configure more things, like headers, cookies..
    private WebClient builderPatterWebClient = WebClient.builder()
            .baseUrl("https://localhost:8080")
            .defaultHeader(HttpHeaders.USER_AGENT, "Spring 5")
            .build();
    // Creation of another builder with the same properties of the original instance to modify them or add new ones.
    // We are going to use this method when we want to work with another WebClient, because the object is inmutable
    private WebClient mutatedWebClient = baseUrlWebClient.mutate()
            .baseUrl("https://localhost:8081")
            .build();

    public WebClientAPI() {
        // Creation of WebClient using the static method "create()"
        //this.webClient = WebClient.create("http://localhost:8080/products");

        // Creation of WebClient using the Builder Pattern
        this.webClient =WebClient.builder()
                .baseUrl("http://localhost:8080/products")
                .build();
    }

    public static void main(String args[]) {
        WebClientAPI api = new WebClientAPI();

        api.postNewProduct()
                .thenMany(api.getAllProducts())
                .take(1)
                .flatMap(p -> api.updateProduct(p.getId(), "White Tea", 0.99))
                .flatMap(p -> api.deleteProduct(p.getId()))
                .thenMany(api.getAllProducts())
                .thenMany(api.getAllEvents())
                .subscribeOn(Schedulers.newSingle("myThread"))
                .subscribe(System.out::println);

        /*
            Need it if we want to see the logs in console, because the sequence above is executed asynchronously.
            If you want to avoid using try-catch block, you can use "subscribeOn()" method. This method will create
            a non-daemon thread and will block the application from exiting until this new thread pool is not shut down.
         */
        // Need it if we want to see the logs in console, because the sequence above is executed asynchronously
        //try {
        //    Thread.sleep(5000);
        //} catch(Exception e) { }
    }

    /**
     * Example of how to do a POST request with WebClient. Things to keep on mind:
     *      - HTTP method involved(GET, POST, PUT, DELETE...)
     *      - URI of the endpoint we want to call
     *      - Headers accepted by the endpoint
     *      - Content type returned by the endpoint: JSON, XML, streams...
     *      - Body type of the request, in case the API needs JSON, XML...
     *
     * How to read the response:
     *      - Using "retrieve()" method. After calling retrieve we can transform the body to a publisher(Mono or Flux).
     *        We can also register custom functions that will be executed when a given status is not with the response.
     *      - Using "exchangeToFlux()" or "exchangeToMono()" methods. These methods take a function that receives an
     *        argument of type ClientResponse, which we can use to access to the response's status and headers and also,
     *        consume the response body.
     *
     * Note: remember we need to use "subscribe()" to start the process
     * @return
     */
    private Mono<ResponseEntity<Product>> postNewProduct() {
        return webClient
                .post()
                .body(Mono.just(new Product(null, "Jasmine Tea", 1.99)), Product.class)
                .exchangeToMono(response -> response.toEntity(Product.class))
                .doOnSuccess(o -> System.out.println("**********POST " + o));
    }

    /**
     * Example of how to do a GET request with WebClient. Things to keep on mind:
     *      - HTTP method involved(GET, POST, PUT, DELETE...)
     *      - URI of the endpoint we want to call
     *      - Headers accepted by the endpoint
     *      - Content type returned by the endpoint: JSON, XML, streams...
     *      - Body type of the request, in case the API needs JSON, XML...
     *
     * How to read the response:
     *      - Using "retrieve()" method. After calling retrieve we can transform the body to a publisher(Mono or Flux).
     *        We can also register custom functions that will be executed when a given status is not with the response.
     *      - Using "exchangeToFlux()" or "exchangeToMono()" methods. These methods take a function that receives an
     *        argument of type ClientResponse, which we can use to access to the response's status and headers and also,
     *        consume the response body.
     *
     * Note: remember we need to use "subscribe()" to start the process
     * @return
     */
    private Flux<Product> getAllProducts() {
        return webClient
                .get()
                .retrieve()
                .bodyToFlux(Product.class)
                .doOnNext(o -> System.out.println("**********GET: " + o));
    }

    /**
     * Example of how to do a PUT request with WebClient. Things to keep on mind:
     *      - HTTP method involved(GET, POST, PUT, DELETE...)
     *      - URI of the endpoint we want to call
     *      - Headers accepted by the endpoint
     *      - Content type returned by the endpoint: JSON, XML, streams...
     *      - Body type of the request, in case the API needs JSON, XML...
     *
     * How to read the response:
     *      - Using "retrieve()" method. After calling retrieve we can transform the body to a publisher(Mono or Flux).
     *        We can also register custom functions that will be executed when a given status is not with the response.
     *      - Using "exchangeToFlux()" or "exchangeToMono()" methods. These methods take a function that receives an
     *        argument of type ClientResponse, which we can use to access to the response's status and headers and also,
     *        consume the response body.
     *
     * Note: remember we need to use "subscribe()" to start the process
     * @param id
     * @param name
     * @param price
     * @return
     */
    private Mono<Product> updateProduct(String id, String name, double price) {
        return webClient
                .put()
                .uri("/{id}", id)
                .body(Mono.just(new Product(null, name, price)), Product.class)
                .retrieve()
                .bodyToMono(Product.class)
                .doOnSuccess(o -> System.out.println("**********UPDATE " + o));
    }

    /**
     * Example of how to do a DELETE request with WebClient. Things to keep on mind:
     *      - HTTP method involved(GET, POST, PUT, DELETE...)
     *      - URI of the endpoint we want to call
     *      - Headers accepted by the endpoint
     *      - Content type returned by the endpoint: JSON, XML, streams...
     *      - Body type of the request, in case the API needs JSON, XML...
     *
     * How to read the response:
     *      - Using "retrieve()" method. After calling retrieve we can transform the body to a publisher(Mono or Flux).
     *        We can also register custom functions that will be executed when a given status is not with the response.
     *      - Using "exchangeToFlux()" or "exchangeToMono()" methods. These methods take a function that receives an
     *        argument of type ClientResponse, which we can use to access to the response's status and headers and also,
     *        consume the response body.
     *
     * Note: remember we need to use "subscribe()" to start the process
     * @param id
     * @return
     */
    private Mono<Void> deleteProduct(String id) {
        return webClient
                .delete()
                .uri("/{id}", id)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(o -> System.out.println("**********DELETE " + o));
    }

    private Flux<ProductEvent> getAllEvents() {
        return webClient
                .get()
                .uri("/events")
                .retrieve()
                .bodyToFlux(ProductEvent.class);
    }
}
