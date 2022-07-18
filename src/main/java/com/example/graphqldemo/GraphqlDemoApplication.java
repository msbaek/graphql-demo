package com.example.graphqldemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Instant;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toMap;

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
        return Mono.justOrEmpty(db.get(id));
    }

    @MutationMapping
    public Mono<Customer> addCustomer(@Argument  String name) {
        var customer = new Customer(id.incrementAndGet(), name);
        db.put(customer.id(), customer);
        return Mono.just(customer);
    }

    @MutationMapping
    public Mono<Customer> updateCustomer(@Argument int id, @Argument String name) {
        var customer = db.get(id);
        customer.updateName(name);
        return Mono.just(customer);
    }

    @MutationMapping
    public Mono<Customer> deleteCustomer(@Argument int id) {
        var customer = db.get(id);
        db.remove(id);
        return Mono.just(customer);
    }

    @BatchMapping
    Map<Customer, Account> account(List<Customer> customers) {
        System.out.println("account for " + customers.size() + " customers");
        // account for 2 customers
        return customers.stream()
                .collect(toMap(c -> c, c -> new Account(c.id(), "Account@"+ Instant.now())));
    }


    @BatchMapping
    Map<Customer, Profile> profile(List<Customer> customers) {
        System.out.println("profile for " + customers.size() + " customers");
        return customers.stream()
                .collect(toMap(c-> c, c -> new Profile(c.id(), "Profile@"+ Instant.now())));
    }
}

class Customer {
    private int id;
    private String name;

    public Customer(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int id() {
        return id;
    }

    public String name() {
        return name;
    }

    void updateName(String name) {
        this.name = name;
    }
}

record Account(int id, String name) {
}

record Profile(int id, String name) {
}