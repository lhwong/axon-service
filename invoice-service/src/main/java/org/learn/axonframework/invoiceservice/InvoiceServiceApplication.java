package org.learn.axonframework.invoiceservice;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.distributed.AnnotationRoutingStrategy;
import org.axonframework.commandhandling.distributed.CommandBusConnector;
import org.axonframework.commandhandling.distributed.CommandRouter;
import org.axonframework.commandhandling.distributed.DistributedCommandBus;
import org.axonframework.serialization.Serializer;
import org.axonframework.springcloud.commandhandling.SpringCloudCommandRouter;
import org.axonframework.springcloud.commandhandling.SpringHttpCommandBusConnector;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@EnableDiscoveryClient
@SpringBootApplication
public class InvoiceServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InvoiceServiceApplication.class, args);
	}

	@Bean
	public Exchange invoiceExchange() {
		return ExchangeBuilder.fanoutExchange("InvoiceEvents").durable(true).build();
	}

	// order AMQP queue

	@Bean
	public Queue orderQueue() {
		return QueueBuilder.durable("OrderQueue").build();
	}

	@Bean
	public Binding orderBinding() {
		return BindingBuilder.bind(orderQueue()).to(invoiceExchange()).with("*").noargs();
	}

	// query AMQP queue

	@Bean
	public Queue queryQueue() {
		return QueueBuilder.durable("QueryQueue").build();
	}

	@Bean
	public Binding queryBinding() {
		return BindingBuilder.bind(queryQueue()).to(invoiceExchange()).with("*").noargs();
	}

	@Autowired
	public void configure(AmqpAdmin admin) {
		admin.declareExchange(invoiceExchange());

		admin.declareQueue(orderQueue());
		admin.declareBinding(orderBinding());

		admin.declareQueue(queryQueue());
		admin.declareBinding(queryBinding());
	}
	
	@Bean
    public RestOperations restTemplate() {
        return new RestTemplate();
    }

	//spring cloud settings - distributed command bus
	@Bean
    public CommandRouter springCloudCommandRouter(DiscoveryClient discoveryClient, Registration localServiceInstance) {
        return SpringCloudCommandRouter.builder()
                                       .discoveryClient(discoveryClient)
                                       .routingStrategy(new AnnotationRoutingStrategy())
                                       .localServiceInstance(localServiceInstance)
                                       .build();
    }

	@Bean
    public CommandBusConnector springHttpCommandBusConnector(
                        @Qualifier("localSegment") CommandBus localSegment,
                        RestOperations restOperations,
                        Serializer serializer) {
        return SpringHttpCommandBusConnector.builder()
                                            .localCommandBus(localSegment)
                                            .restOperations(restOperations)
                                            .serializer(serializer)
                                            .build();
    }

    @Primary // to make sure this CommandBus implementation is used for autowiring
    @Bean
    public DistributedCommandBus springCloudDistributedCommandBus(
                         CommandRouter commandRouter, 
                         CommandBusConnector commandBusConnector) {
        return DistributedCommandBus.builder()
                                    .commandRouter(commandRouter)
                                    .connector(commandBusConnector)
                                    .build();
    }
}
