package com.wiredbraincoffee.springwebfluxfunctional.router;

import com.wiredbraincoffee.springwebfluxfunctional.handler.ProductHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * This class will implement all the routes our API will have
 */
@Component
public class ProductRouter {

    /**
     * Example of how to define the different endpoints of the API using route() and builder pattern.
     * We need to keep on mind to put the most specific routes at the beginning, because Spring will scan sequentially.
     *
     * The problem with this approach is there is a lot of repetition and also some endpoints may not need the predicate.
     *
     * @param handler handler of the routes defined
     * @return object to manage the routes
     */
    RouterFunction<ServerResponse> chainedRoutes(ProductHandler handler) {
        return route()
				.GET("/products/events", accept(MediaType.TEXT_EVENT_STREAM), handler::getProductEvents)
				.GET("/products/{id}", accept(MediaType.APPLICATION_JSON), handler::getProduct)
				.GET("/products", accept(MediaType.APPLICATION_JSON), handler::getAllProducts)
				.PUT("/products/{id}", accept(MediaType.APPLICATION_JSON), handler::updateProduct)
				.POST("/products", contentType(MediaType.APPLICATION_JSON), handler::saveProduct)
				.DELETE("/products/{id}", accept(MediaType.APPLICATION_JSON), handler::deleteProduct)
				.DELETE("/products", accept(MediaType.APPLICATION_JSON), handler::deleteAllProducts)
				.build();
    }

    /**
     * Example of how to define the different endpoints of the API using route() and builder pattern.
     * We need to keep on mind to put the most specific routes at the beginning, because Spring will scan sequentially.
     *
     * The problem with this approach is that it is more complex and difficult to read
     *
     * @param handler handler of the routes defined
     * @return object to manage the routes
     */
    @Bean
    RouterFunction<ServerResponse> nestedRoutes(ProductHandler handler) {
        return route()
                // Base path for all routes
                .path("/products",
                        builder -> builder
                                .nest(
                                        // Contemplated headers. In this case: Accept and Content-Type
                                        accept(MediaType.APPLICATION_JSON)
                                                .or(contentType(MediaType.APPLICATION_JSON))
                                                .or(accept(MediaType.TEXT_EVENT_STREAM)),
                                        nestedBuilder -> nestedBuilder
                                                .GET("/events", handler::getProductEvents)
                                                .GET("/{id}", handler::getProduct)
                                                .GET(handler::getAllProducts)
                                                .PUT("/{id}", handler::updateProduct)
                                                .POST(handler::saveProduct)
                                )
                                /*
                                   DELETE methods don't need the previous headers, so we can place them outside
                                   the scope of "nest()" method
                                 */
                                .DELETE("/{id}", handler::deleteProduct)
                                .DELETE(handler::deleteAllProducts)
                ).build();
    }
}
