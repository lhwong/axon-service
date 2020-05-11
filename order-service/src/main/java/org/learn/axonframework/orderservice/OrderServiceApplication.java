package org.learn.axonframework.orderservice;

import com.rabbitmq.client.Channel;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.distributed.AnnotationRoutingStrategy;
import org.axonframework.commandhandling.distributed.CommandBusConnector;
import org.axonframework.commandhandling.distributed.CommandRouter;
import org.axonframework.commandhandling.distributed.DistributedCommandBus;
import org.axonframework.config.SagaConfiguration;
import org.axonframework.extensions.amqp.eventhandling.AMQPMessageConverter;
import org.axonframework.extensions.amqp.eventhandling.spring.SpringAMQPMessageSource;
import org.axonframework.serialization.Serializer;
import org.axonframework.springcloud.commandhandling.SpringCloudCommandRouter;
import org.axonframework.springcloud.commandhandling.SpringHttpCommandBusConnector;
import org.learn.axonframework.orderservice.saga.OrderManagementSaga;
import org.learn.axonframework.orderservice.swagger.SwaggerConfiguration;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@EnableDiscoveryClient
@SpringBootApplication
@Import(SwaggerConfiguration.class)
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

    /*@Bean
    public SagaConfiguration<OrderManagementSaga> orderManagementSagaConfiguration(
            SpringAMQPMessageSource springAMQPMessageSource, PlatformTransactionManager txManager) {
        return SagaConfiguration.subscribingSagaManager(OrderManagementSaga.class, c -> springAMQPMessageSource)
                .configureTransactionManager(c -> new SpringTransactionManager(txManager));
    }*/
    
    

    @Bean
    public Exchange orderExchange() {
        return ExchangeBuilder.fanoutExchange("OrderEvents").durable(true).build();
    }

    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable("OrderQueue").build();
    }

    @Bean
    public Binding orderBinding() {
        return BindingBuilder.bind(orderQueue()).to(orderExchange()).with("*").noargs();
    }

    @Bean
    public Queue queryQueue() {
        return QueueBuilder.durable("QueryQueue").build();
    }

    @Bean
    public Binding queryBinding() {
        return BindingBuilder.bind(queryQueue()).to(orderExchange()).with("*").noargs();
    }

    @Autowired
    public void configure(AmqpAdmin admin) {
        admin.declareExchange(orderExchange());

        admin.declareQueue(orderQueue());
        admin.declareBinding(orderBinding());

        admin.declareQueue(queryQueue());
        admin.declareBinding(queryBinding());
    }

    /*@Bean
    public SpringAMQPMessageSource orderEvents(Serializer serializer) {
        return new SpringAMQPMessageSource(new DefaultAMQPMessageConverter(serializer)) {

            @Transactional
            @RabbitListener(queues = "OrderQueue")
            @Override
            public void onMessage(Message message, Channel channel) throws Exception {
                super.onMessage(message, channel);
            }
        };
    }*/
    
    @Bean
    public SpringAMQPMessageSource orderEvents(AMQPMessageConverter messageConverter) {
        return new SpringAMQPMessageSource(messageConverter) {

            @RabbitListener(queues = "OrderQueue")
            @Override
            public void onMessage(Message message, Channel channel) {
                super.onMessage(message, channel);
            }
        };
    }

    //spring cloud settings - distributed command bus
    /*@Bean
    public CommandRouter springCloudCommandRouter(DiscoveryClient discoveryClient) {
        return new SpringCloudCommandRouter(discoveryClient, new AnnotationRoutingStrategy());
    }*/
    @Bean
    public CommandRouter springCloudCommandRouter(DiscoveryClient discoveryClient, Registration localServiceInstance) {
        return SpringCloudCommandRouter.builder()
                                       .discoveryClient(discoveryClient)
                                       .routingStrategy(new AnnotationRoutingStrategy())
                                       .localServiceInstance(localServiceInstance)
                                       .build();
    }

    @Bean
    public RestOperations restTemplate() {
        return new RestTemplate();
    }

    /*@Bean
    public CommandBusConnector springHttpCommandBusConnector(@Qualifier("localSegment") CommandBus localSegment,
                                                             RestOperations restOperations,
                                                             Serializer serializer) {
        return new SpringHttpCommandBusConnector(localSegment, restOperations, serializer);
    }

    @Primary // to make sure this CommandBus implementation is used for autowiring
    @Bean
    public DistributedCommandBus springCloudDistributedCommandBus(CommandRouter commandRouter,
                                                                  CommandBusConnector commandBusConnector) {
        return new DistributedCommandBus(commandRouter, commandBusConnector);
    }

    @Bean(destroyMethod = "shutdown")
    @Qualifier("localSegment")
    public CommandBus localSegment(TransactionManager transactionManager) {
        AsynchronousCommandBus asynchronousCommandBus = new AsynchronousCommandBus();
        asynchronousCommandBus.registerDispatchInterceptor(new BeanValidationInterceptor<>());
        asynchronousCommandBus.registerHandlerInterceptor(new TransactionManagingInterceptor<>(transactionManager));

        return asynchronousCommandBus;
    }*/
    
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
