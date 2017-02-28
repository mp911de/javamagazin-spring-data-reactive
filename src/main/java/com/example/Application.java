/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example;

import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.CollectionOptions;
import org.springframework.data.mongodb.core.ReactiveMongoOperations;

/**
 * @author Mark Paluch
 */
@SpringBootApplication
@Import(AppConfig.class)
public class Application implements CommandLineRunner {

	private final ReactiveMongoOperations mongoOperations;

	public Application(ReactiveMongoOperations mongoOperations) {
		this.mongoOperations = mongoOperations;
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Override
	public void run(String... strings) throws Exception {

		Person walter = new Person("Walter", "White");
		Person jesse = new Person("Jesse", "Pinkman");

		mongoOperations.dropCollection(Person.class) //
				.then(mongoOperations.insertAll(Arrays.asList(walter, jesse)).then()) //
				.subscribe();

		mongoOperations.dropCollection(LoginEvent.class) //
				.then(mongoOperations.createCollection(LoginEvent.class, //
						new CollectionOptions(10000, 1000, true))) //
				.subscribe();

		Stream<String> generate = Stream.generate(() -> UUID.randomUUID().toString());
		Flux<String> streams = Flux.fromStream(generate);
		Flux<Long> intervals = Flux.interval(Duration.ofSeconds(1));

		Flux.zip(intervals, streams) //
				.map(t2 -> new LoginEvent(t2.getT1(), t2.getT2(), "remote-" + t2.getT2())) //
				.flatMap(mongoOperations::insert) //
				.subscribe();
	}
}
