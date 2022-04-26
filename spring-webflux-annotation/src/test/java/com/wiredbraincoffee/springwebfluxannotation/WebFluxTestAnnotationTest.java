package com.wiredbraincoffee.springwebfluxannotation;


import com.wiredbraincoffee.springwebfluxannotation.controller.ProductController;
import com.wiredbraincoffee.springwebfluxannotation.model.Product;
import com.wiredbraincoffee.springwebfluxannotation.model.ProductEvent;
import com.wiredbraincoffee.springwebfluxannotation.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Class that shows how to test WebFlux controllers.
 * "@WebFluxTest" annotation configured beans provided:
 *      - @Controller
 *      - @JsonComponent
 *      - Converters
 *      - WebFluxConfigurer beans
 *
 * "@WebFluxTest" annotation configured beans not provided:
 *      - @Component
 *      - @Service
 *      - @Repository
 *
 * Using @WebFluxTest will provide a WebTestClient instance, so we can use @Autowired with the WebTestClient object.
 * The WebTestClient instance provided is one by default, so we need to specify the complete PATH in each test
 */
@WebFluxTest(ProductController.class)
class WebFluxTestAnnotationTest {

    @Autowired
    private WebTestClient client;

    private List<Product> expectedList;

    // Mocked because @WebFluxTest does not provide a configuration for it
    @MockBean
    private ProductRepository repository;

    @MockBean
    private CommandLineRunner commandLineRunner;

    /**
     * This method will be executed every time a test is run.
     * Notice we don't need to create a WebTestClient, since @WebFluxTest gives us one by default
     */
    @BeforeEach
    void beforeEach() {
        this.expectedList = List.of(
                new Product("1", "Big Latte", 2.99)
        );
    }

    /**
     * Endpoint tested: /products
     * HTTP method associated: GET
     *
     * In this example we are going to test the endpoint to get all the products.
     */
    @Test
    void testGetAllProducts() {
        // Since we are using Mockito, we need to specify what the mock object methods should return
        when(repository.findAll()).thenReturn(Flux.fromIterable(this.expectedList));

        client
                .get()
                .uri("/products")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBodyList(Product.class)
                .isEqualTo(expectedList);
    }

    /**
     * Endpoint tested: /products/{id}
     * HTTP method associated: GET
     *
     * Test case: product not found when passing an incorrect id
     */
    @Test
    void testProductInvalidIdNotFound() {
        String id = "aaa";

        // Since we are using Mockito, we need to specify what the mock object methods should return
        when(repository.findById(id)).thenReturn(Mono.empty());

        client
                .get()
                .uri("/products/{id}", id)
                .exchange()
                .expectStatus()
                .isNotFound();
    }

    /**
     * Endpoint tested: /products/{id}
     * HTTP method associated: GET
     *
     * Test case: product found when passing a correct id
     */
    @Test
    void testProductIdFound() {
        Product expectedProduct = this.expectedList.get(0);

        // Since we are using Mockito, we need to specify what the mock object methods should return
        when(repository.findById(expectedProduct.getId())).thenReturn(Mono.just(expectedProduct));

        client
                .get()
                .uri("/products/{id}", expectedProduct.getId())
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Product.class)
                .isEqualTo(expectedProduct);
    }

    /**
     * Endpoint tested: /products/events
     * HTTP method associated: GET
     *
     * Test case: how to test the events sent by the server
     */
    @Test
    void testProductEvents() {
        ProductEvent expectedEvent =
                new ProductEvent(0L, "Product Event");

        FluxExchangeResult<ProductEvent> result =
                client.get().uri("/products/events")
                        .accept(MediaType.TEXT_EVENT_STREAM)
                        .exchange()
                        .expectStatus().isOk()
                        .returnResult(ProductEvent.class);

        StepVerifier.create(result.getResponseBody())
                .expectNext(expectedEvent)
                .expectNextCount(2)
                .consumeNextWith(event ->
                        assertEquals(Long.valueOf(3), event.getEventId()))
                .thenCancel()
                .verify();
    }
}
