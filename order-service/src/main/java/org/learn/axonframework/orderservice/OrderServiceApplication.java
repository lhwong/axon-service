package org.learn.axonframework.orderservice;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.distributed.AnnotationRoutingStrategy;
import org.axonframework.commandhandling.distributed.CommandBusConnector;
import org.axonframework.commandhandling.distributed.CommandRouter;
import org.axonframework.commandhandling.distributed.DistributedCommandBus;
import org.axonframework.extensions.springcloud.autoconfig.SpringCloudAutoConfiguration;
import org.axonframework.extensions.springcloud.commandhandling.SpringCloudCommandRouter;
import org.axonframework.extensions.springcloud.commandhandling.SpringCloudHttpBackupCommandRouter;
import org.axonframework.extensions.springcloud.commandhandling.SpringHttpCommandBusConnector;
import org.axonframework.serialization.Serializer;
import org.axonframework.springboot.autoconfig.AxonAutoConfiguration;
import org.learn.axonframework.orderservice.swagger.SwaggerConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@EnableDiscoveryClient
@SpringBootApplication
@Import(SwaggerConfiguration.class)
@EnableScheduling
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

    /*@Bean
    public SagaConfiguration<OrderManagementSaga> orderManagementSagaConfiguration(
    		StreamableKafkaMessageSource springMessageSource, PlatformTransactionManager txManager) {
        return SagaConfiguration.trackingSagaManager(OrderManagementSaga.class, c -> springMessageSource);
    }*/
    
    /*
      @Bean
  public SagaConfiguration<MySaga> mySagaConfiguration(@Autowired KafkaMessageSource<String, Object> kafkaMessageSource) {
    return SagaConfiguration.trackingSagaManager(MySaga.class, "MyProcessor", configuration -> kafkaMessageSource);
  }
     */
   /* 
    @Bean
    public ProducerFactory<String, byte[]> producerFactory(Duration closeTimeout,
            int producerCacheSize,
            Map<String, Object> producerConfiguration,
            ConfirmationMode confirmationMode,
            String transactionIdPrefix) {
		return DefaultProducerFactory.<String, byte[]>builder()
		.closeTimeout(closeTimeout)                 // Defaults to "30" seconds
		.producerCacheSize(producerCacheSize)       // Defaults to "10"; only used for "TRANSACTIONAL" mode
		.configuration(producerConfiguration)       // Hard requirement
		.confirmationMode(confirmationMode)         // Defaults to a Confirmation Mode of "NONE"
		.transactionalIdPrefix(transactionIdPrefix) // Hard requirement when in "TRANSACTIONAL" mode
		.build();
	}*/
    
    /*@Bean
    public StreamableKafkaMessageSource<String, byte[]> streamableKafkaMessageSource(List<String> topics,
            String groupIdPrefix,
            Supplier<String> groupIdSuffixFactory,
            ConsumerFactory<String, byte[]> consumerFactory,
            Fetcher<String, byte[], KafkaEventMessage> fetcher,
            KafkaMessageConverter<String, byte[]> messageConverter,
            int bufferCapacity) {
				return StreamableKafkaMessageSource.<String, byte[]>builder()
				.topics(topics)                                                 // Defaults to a collection of "Axon.Events"
				.groupIdPrefix(groupIdPrefix)                                   // Defaults to "Axon.Streamable.Consumer-"
				.groupIdSuffixFactory(groupIdSuffixFactory)                     // Defaults to a random UUID
				.consumerFactory(consumerFactory)                               // Hard requirement
				.fetcher(fetcher)                                               // Hard requirement
				.messageConverter(messageConverter)                             // Defaults to a "DefaultKafkaMessageConverter"
				.bufferFactory(
				() -> new SortedKafkaMessageBuffer<>(bufferCapacity))   // Defaults to a "SortedKafkaMessageBuffer" with a buffer capacity of "1000"
				.build();
    }
    
    @Autowired
    public void configureStreamableKafkaSource(EventProcessingConfigurer configurer,
    		StreamableKafkaMessageSource<String, ByteArray> streamableKafkaMessageSource) {
        configurer.registerTrackingEventProcessor("ordersaga", c -> streamableKafkaMessageSource);
    }*/



    //spring cloud settings - distributed command bus
    /*@Bean
    public CommandRouter springCloudCommandRouter(DiscoveryClient discoveryClient) {
        return new SpringCloudCommandRouter(discoveryClient, new AnnotationRoutingStrategy());
    }*/
    
    /*@Bean
    public CommandRouter springCloudCommandRouter(DiscoveryClient discoveryClient, Registration localServiceInstance) {
        return SpringCloudCommandRouter.builder()
                                       .discoveryClient(discoveryClient)
                                       .routingStrategy(new AnnotationRoutingStrategy())
                                       .localServiceInstance(localServiceInstance)
                                       .build();
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
    
    @Bean
    @Qualifier("localSegment")
    public CommandBus localSegment() {
        return SimpleCommandBus.builder().build();
    }
    
    @Bean
    public CommandRouter springCloudHttpBackupCommandRouter(
                             DiscoveryClient discoveryClient, 
                             RestTemplate restTemplate,
                             Registration localServiceInstance,                             
                             @Value("${axon.distributed.spring-cloud.fallback-url}") 
                                         String messageRoutingInformationEndpoint) {
        return SpringCloudHttpBackupCommandRouter.builder()
                                                 .discoveryClient(discoveryClient)
                                                 .routingStrategy(new AnnotationRoutingStrategy())
                                                 .restTemplate(restTemplate)
                                                 .localServiceInstance(localServiceInstance)
                                                 .messageRoutingInformationEndpoint(messageRoutingInformationEndpoint)
                                                 .build();
    }
    


}
