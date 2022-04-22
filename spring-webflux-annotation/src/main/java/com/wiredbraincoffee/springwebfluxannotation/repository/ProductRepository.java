package com.wiredbraincoffee.springwebfluxannotation.repository;

import com.wiredbraincoffee.springwebfluxannotation.model.Product;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/**
 * We need to extend "ReactiveMongoRepository" because we are working in a reactive way.
 * Also, we specify that we will work with objects of type "Product" and the identifier of those objects
 * is a String type.
 *
 * To see the identifier type, we only need to search in the object we are going to work with a field with
 * "@Id" annotation
 *
 * Note: the operations done with this repository return Flux or Mono
 */
public interface ProductRepository
        extends ReactiveMongoRepository<Product, String>  {
}
