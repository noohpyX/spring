package com.wiredbraincoffee.springwebfluxfunctional;

import com.wiredbraincoffee.springwebfluxfunctional.model.Product;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

/**
 * Class to show how to create and manipulate WebTestClient object.
 * This object has an API similar to WebClient and allow us to test web applications using a non-blocking reactive
 * client.
 */
public class WebTestClientDemo {

    /*
       Creating a WebTestClient instance
     */

    // Binding WebTestClient to a controller to test one controller at time
    //WebTestClient clientBindToController = WebTestClient.bindToController(ProductController.class).build();

    // Binding WebTestClient to a router function
    // WebTestClient clientBindToRouter = WebTestClient.bindToRouterFunction(routerFunction).build();

    // Binding WebTestClient to an application context to set up a server from Spring configuration
    // WebTestClient clientBindToApplicationContext = WebTestClient.bindToApplicationContext(applicationContext)
    //        .build();

    // Binding WebTestClient to a sever
    WebTestClient clientBindToServer = WebTestClient.bindToServer().baseUrl("http://localhost:8080").build();


    /*
       Testing with WebTestClient
     */

    /**
     * Example of how to test GET endpoints.
     *      - Call "get()" method of WebTestClient
     *      - Call "exchange()" method to perform the request and get the complete response
     *      - Use "expect...()" methods to test the response
     */
    void testGetMethods() {
        clientBindToServer
                .get()
                .uri("/products")
                .exchange()
                .expectStatus().isOk();
    }

    /**
     * Example of how to test POST endpoints
     *      - Call "post()" method of WebTestClient
     *      - Call "body()" method to insert the body request
     *      - Call "exchange()" method to perform the request and get the complete response
     *      - Use "expect...()" methods to test the response
     */
    void testPostMethods() {
        clientBindToServer
                .post()
                .body(BodyInserters.fromValue(new Product("1", "Big Latte", 2.99)))
                .exchange()
                .expectStatus().isCreated();
    }

    /**
     * Example of how to test PUT endpoints
     *      - Call "put()" method of WebTestClient
     *      - Call "body()" method to insert the body request
     *      - Call "exchange()" method to perform the request and get the complete response
     *      - Use "expect...()" methods to test the response
     */
    void testPutMethods() {
        clientBindToServer
                .put()
                .uri("/products/{id}", "aaa")
                .body(BodyInserters.fromValue(new Product("1", "Big Latte", 2.99)))
                .exchange()
                .expectStatus().isOk();
    }

    /**
     * Example of how to test DELETE endpoints
     *      - Call "delete()" method of WebTestClient
     *      - Call "exchange()" method to perform the request and get the complete response
     *      - Use "expect...()" methods to test the response
     *
     * Note: normally, a DELETE endpoint returns a response with no content. To test it, we can use either
     *       "expectBody(Void.class)" or "expectBody().isEmpty()"
     */
    void testDeleteMethods() {
        clientBindToServer
                .delete()
                .uri("/products/{id}", "aaa")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Void.class);
    }
}
