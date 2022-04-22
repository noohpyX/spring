package com.wiredbraincoffee.springwebfluxfunctional.handler;

import com.wiredbraincoffee.springwebfluxfunctional.model.Product;
import com.wiredbraincoffee.springwebfluxfunctional.model.ProductEvent;
import com.wiredbraincoffee.springwebfluxfunctional.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

/**
 * This class will implement the handlers for each routes in our API
 *
 * Note: a handler function takes a "ServerRequest" object and return a Mono<ServerResponse> object.
 */
@Component
public class ProductHandler {
    private ProductRepository repository;

    public ProductHandler(ProductRepository repository) {
        this.repository = repository;
    }

    /**
     * URL associated: /products
     * HTTP method associated: GET
     *
     * This method will find all the existing products in database
     *
     * @param request object containing all the info of the request done
     * @return response with the body, content type and HTTP status
     */
    public Mono<ServerResponse> getAllProducts(ServerRequest request) {
        Flux<Product> products = repository.findAll();

        return ServerResponse.ok()
                .contentType(APPLICATION_JSON)
                .body(products, Product.class);
    }

    /**
     * URL associated: /products/{id}
     * HTTP method associated: GET
     *
     * This method will get a single product with the id that we can find in the "ServerRequest" object
     *
     * @param request object containing all the info of the request done
     * @return product found or 404- Not Found in case the product did not exist
     */
    public Mono<ServerResponse> getProduct(ServerRequest request) {
        // Extracting a path variable from the request
        String id = request.pathVariable("id");

        Mono<Product> productMono = this.repository.findById(id);
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        return productMono
                .flatMap(product ->
                        ServerResponse.ok()
                                .contentType(APPLICATION_JSON)
                                .body(fromValue(product)))
                // This operator is similar to "defaultIfEmpty()", but in this case only accepts Publishers
                .switchIfEmpty(notFound);
    }

    /**
     * URL associated: /products
     * HTTP method associated: POST
     *
     * This method will save a new product into database
     *
     * @param request object containing all the info of the request done
     * @return product saved in database and HTTP status
     */
    public Mono<ServerResponse> saveProduct(ServerRequest request) {
        // Extracting the body content and transforming it into a Mono from the request
        Mono<Product> productMono = request.bodyToMono(Product.class);

        return productMono.flatMap(product ->
                ServerResponse.status(HttpStatus.CREATED)
                        .contentType(APPLICATION_JSON)
                        .body(repository.save(product), Product.class));
    }

    /**
     * URL associated: /products/{id}
     * HTTP method associated: PUT
     *
     * This method will update the info of a product based on an id
     *
     * @param request object containing all the info of the request done
     * @return the product updated or 404 - Not Found if the product did not exist in database
     */
    public Mono<ServerResponse> updateProduct(ServerRequest request) {
        // Extracting a path variable from the request
        String id = request.pathVariable("id");

        // Verifying if the product with that id exists
        Mono<Product> existingProductMono = this.repository.findById(id);

        // Extracting the body content and transforming it into a Mono from the request
        Mono<Product> productMono = request.bodyToMono(Product.class);

        // 404 - Not Found response in case a product with the previous id does not exist
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        return productMono.zipWith(existingProductMono,
                (product, existingProduct) ->
                        new Product(existingProduct.getId(), product.getName(), product.getPrice())
        ).flatMap(product ->
                ServerResponse.ok()
                        .contentType(APPLICATION_JSON)
                        .body(repository.save(product), Product.class)
        ).switchIfEmpty(notFound);
    }

    /**
     * URL associated: /products/{id}
     * HTTP method associated: DELETE
     *
     * This method will delete a product based on an id
     *
     * @param request object containing all the info of the request done
     * @return empty content if everything was ok or 404 - Not Found if the product did not exist in database
     */
    public Mono<ServerResponse> deleteProduct(ServerRequest request) {
        // Extracting a path variable from the request
        String id = request.pathVariable("id");

        // Verifying if the product with that id exists
        Mono<Product> productMono = this.repository.findById(id);

        // 404 - Not Found response in case a product with the previous id does not exist
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();

        return productMono
                .flatMap(existingProduct ->
                        ServerResponse.ok()
                                .build(repository.delete(existingProduct))
                )
                .switchIfEmpty(notFound);
    }

    /**
     * URL associated: /products
     * HTTP method associated: DELETE
     *
     * This method will delete all existing products in database
     *
     * @param request object containing all the info of the request done
     * @return empty body with HTTP status 201 - Created
     */
    public Mono<ServerResponse> deleteAllProducts(ServerRequest request) {
        return ServerResponse.ok()
                .build(repository.deleteAll());
    }

    /**
     * URL associated: /products/events
     * HTTP method associated: GET
     *
     * This method simulates a stream of events and lets the server push new data to the page or client at any time
     *
     * @param request object containing all the info of the request done
     * @return Flux with the events generated
     */
    public Mono<ServerResponse> getProductEvents(ServerRequest request) {
        Flux<ProductEvent> eventsFlux = Flux.interval(Duration.ofSeconds(1)).map(val ->
                new ProductEvent(val, "Product Event")
        );

        return ServerResponse.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .body(eventsFlux, ProductEvent.class);
    }
}
