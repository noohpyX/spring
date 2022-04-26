package com.wiredbraincoffee.springwebfluxannotation;

import com.wiredbraincoffee.springwebfluxannotation.controller.ProductController;
import com.wiredbraincoffee.springwebfluxannotation.model.Product;
import com.wiredbraincoffee.springwebfluxannotation.model.ProductEvent;
import com.wiredbraincoffee.springwebfluxannotation.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.FluxExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Class that shows how to test an API without starting the server every time the test runs.
 * To achieve this we need to use:
 *      - @ExtendWith(SpringExtension.class) to use JUnit 5
 *      - @MockBean to mock something. In this case, the repository
 *
 * Using these annotations, only Spring's application context is initialized
 */
@ExtendWith(SpringExtension.class)
class JUnit5ControllerMockTest {
    private WebTestClient client;

    private List<Product> expectedList;

    @MockBean
    private ProductRepository repository;

    /**
     * This method will be executed every time a test is run.
     * Here we are binding the WebTestClient instance to a controller and also getting all the data
     * in the database in a synchronous way
     */
    @BeforeEach
    void beforeEach() {
        this.client =
                WebTestClient
                        .bindToController(new ProductController(repository))
                        .configureClient()
                        .baseUrl("/products")
                        .build();

        this.expectedList = Arrays.asList(
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
        String id = "aaa";

        // Since we are using Mockito, we need to specify what the mock object methods should return
        when(repository.findById(id)).thenReturn(Mono.empty());

        client
                .get()
                .uri("/{id}", id)
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
