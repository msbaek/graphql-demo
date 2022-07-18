package com.example.graphqldemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootApplication
public class GraphqlDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(GraphqlDemoApplication.class, args);
        System.out.println("http://localhost:8080/graphiql");
    }
}

@Controller
class CustomerController {
    private final Map<Integer, Customer> db = new ConcurrentHashMap<>();
    private final AtomicInteger id = new AtomicInteger();

    @QueryMapping
    public Flux<Customer> customers() {
        return Flux.fromIterable(db.values());
    }

    @QueryMapping
    public Mono<Customer> customerById(@Argument int id) {
        return Mono.just(new Customer(id, "John Doe"));
    }

    @MutationMapping
    public Mono<Customer> addCustomer(@Argument  String name) {
        var customer = new Customer(id.incrementAndGet(), name);
        db.put(customer.id(), customer);
        return Mono.just(customer);
    }

    @SchemaMapping(typeName = "Customer")
    Mono<Account> account(Customer customer) {
        System.out.println("account called for customer["+customer.id()+"]");
        return Mono.just(new Account(customer.id(), "Account@"+ Instant.now()));
    }

    @SchemaMapping(typeName = "Customer")
    Mono<Profile> profile(Customer customer) {
        System.out.println("profile called for customer["+customer.id()+"]");
        return Mono.just(new Profile(customer.id(), "Profile@"+ Instant.now()));
    }
}

record Customer(int id, String name) {
}

record Account(int id, String name) {
}

record Profile(int id, String name) {
}