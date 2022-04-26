package com.wiredbraincoffee.springwebfluxannotation;

import com.wiredbraincoffee.springwebfluxannotation.model.Product;
import com.wiredbraincoffee.springwebfluxannotation.model.ProductEvent;
import com.wiredbraincoffee.springwebfluxannotation.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

/**
 * Class that shows how to test an API starting a server.
 * To achieve this we need to use @SpringBootTest annotation and bind the WebTestClient instance to an application
 * context.
 */
@SpringBootTest
class WebTestClientBindToApplicationContextTest {
    private WebTestClient client;

    private List<Product> expectedList;

    @Autowired
    private ProductRepository repository;

    @Autowired
    private ApplicationContext context;

    /**
     * This method will be executed every time a test is run.
     * Here we are binding the WebTestClient instance to an application context and also getting all the data
     * in the database in a synchronous way
     */
    @BeforeEach
    void beforeEach() {
        this.client =
                WebTestClient
                        .bindToApplicationContext(context)
                        .configureClient()
                        .baseUrl("/products")
                        .build();

        /*
            Calling "block()" method will convert an asynchronous call into a synchronous one.
            We need to use it, se we can have data to test
         */
        this.expectedList =
                repository.findAll().collectList().block();
    }

    /**
     * Endpoint tested: /products
     * HTTP method associated: GET
     *
     * In this example we are going to test the endpoint to get all the products.
     */
    @Test
    void testGetAllProducts() {
        client
                .get()
                .uri("/")
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
        client
                .get()
                .uri("/aaa")
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
        Product expectedProduct = expectedList.get(0);
        client
                .get()
                .uri("/{id}", expectedProduct.getId())
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
                client.get().uri("/events")
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
