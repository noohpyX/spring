package com.wiredbraincoffee.springwebfluxannotation.controller;

import com.wiredbraincoffee.springwebfluxannotation.model.Product;
import com.wiredbraincoffee.springwebfluxannotation.model.ProductEvent;
import com.wiredbraincoffee.springwebfluxannotation.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping("/products")
public class ProductController {

    private ProductRepository repository;

    public ProductController(ProductRepository repository) {
        this.repository = repository;
    }

    /**
     * URL associated: /products
     * HTTP method associated: GET
     *
     * This method will find all the existing products in database, so we are going to return a Flux.
     *
     * @return Flux containing all the existing products
     */
    @GetMapping
    public Flux<Product> getAllProducts() {
        return repository.findAll();
    }

    /**
     * URL associated: /products/{id}
     * HTTP method associated: GET
     *
     * This method will get a single product with the id we receive as parameter, so we are going to return a Mono.
     *
     * @param id product identifier
     * @return Mono containing the product info or an empty response in case that product does not exist
     */
    @GetMapping("{id}")
    public Mono<ResponseEntity<Product>> getProduct(@PathVariable String id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                // This operator will be executed when the performed action returns an empty object.
                // Note: this operator works with objects and no need of Publishers
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * URL associated: /products
     * HTTP method associated: POST
     * HTTP response status: 201 - Created
     *
     * This method will save a new product into database returning a Mono with the object saved
     *
     * @param product Product to be saved in database
     * @return Mono containing the product saved in database
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Product> saveProduct(@RequestBody Product product) {
        return repository.save(product);
    }

    /**
     * URL associated: /products/{id}
     * HTTP method associated: PUT
     *
     * This method will update the info of a product with the id passed.
     *
     * @param id Product id to be updated
     * @param product new info of the product to be updated
     * @return Mono containing the product updated or 404 - Not Found if the product did not exist in database
     */
    @PutMapping("{id}")
    public Mono<ResponseEntity<Product>> updateProduct(@PathVariable(value = "id") String id,
                                                       @RequestBody Product product) {
        return repository.findById(id)
                .flatMap(existingProduct -> {
                    existingProduct.setName(product.getName());
                    existingProduct.setPrice(product.getPrice());
                    return repository.save(existingProduct);
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * URL associated: /products/{id}
     * HTTP method associated: DELETE
     *
     * This method will delete a product with the id passed
     *
     * @param id product id to be deleted
     * @return empty Mono or 404 - Not Found if the product did not exist in database
     */
    @DeleteMapping("{id}")
    public Mono<ResponseEntity<Void>> deleteProduct(@PathVariable(value = "id") String id) {
        return repository.findById(id)
                .flatMap(existingProduct ->
                        repository.delete(existingProduct)
                                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                )
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * URL associated: /products
     * HTTP method associated: DELETE
     *
     * This method will delete all existing products in database
     *
     * @return empty Mono
     */
    @DeleteMapping
    public Mono<Void> deleteAllProducts() {
        return repository.deleteAll();
    }

    /**
     * URL associated: /products/events
     * HTTP method associated: GET
     *
     * This method simulates a stream of events and lets the server push new data to the page or client at any time
     *
     * @return Flux with the events generated
     */
    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ProductEvent> getProductEvents() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(val ->
                        new ProductEvent(val, "Product Event")
                );
    }
}
