package com.wiredbraincoffee.springwebfluxfunctional;

import com.wiredbraincoffee.springwebfluxfunctional.model.Product;
import com.wiredbraincoffee.springwebfluxfunctional.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringWebfluxFunctionalApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringWebfluxFunctionalApplication.class, args);
	}

	/**
	 * This method will insert some data in our embedded database using a functional interface, CommandLineRunner.
	 * This interface receives a variable number of string arguments(see args) and we are going to return a lambda
	 * expression.
	 * Inside this lambda expression we are going to add some data to our database
	 * @param operations We only need this argument if we are going to work with a real MongoDB database
	 * @param repository Database object used to perform operations. Injected automatically by Spring
	 * @return
	 */
	@Bean
	CommandLineRunner init(ReactiveMongoOperations operations, ProductRepository repository) {
		return args -> {
			// We don't need to create an id, MongoDB will do it automatically
			Flux<Product> productFlux = Flux.just(
							new Product(null, "Big Latte", 2.99),
							new Product(null, "Big Decaf", 2.49),
							new Product(null, "Green Tea", 1.99))
					.flatMap(repository::save);

			/*
				After we save the previous data, we need to verify that data exists, so we perform a "findAll()" query.
				"thenMany()" operator: this operator allows the first publisher to complete and then execute
				the publisher it receives as an argument.

				Difference between "then()" and "thenMany()":
					- "then()": takes either no parameters or a Mono
					- "thenMany()": takes any publisher(Flux or Mono)
			 */
			productFlux
					.thenMany(repository.findAll())
					.subscribe(System.out::println);

			/*
				In case we are working with a real MongoDB database and need to delete the existing data between
				executions, we need to perform something like this
			 */
            /*operations.collectionExists(Product.class)
                    .flatMap(exists -> exists ? operations.dropCollection(Product.class) : Mono.just(exists))
					.thenMany(v -> operations.createCollection(Product.class))
					.thenMany(productFlux)
					.thenMany(repository.findAll())
					.subscribe(System.out::println);*/
		};
	}

}
