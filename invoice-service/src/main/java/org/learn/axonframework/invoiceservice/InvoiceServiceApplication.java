package org.learn.axonframework.invoiceservice;

import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.distributed.AnnotationRoutingStrategy;
import org.axonframework.commandhandling.distributed.CommandBusConnector;
import org.axonframework.commandhandling.distributed.CommandRouter;
import org.axonframework.commandhandling.distributed.DistributedCommandBus;
import org.axonframework.extensions.springcloud.commandhandling.SpringCloudCommandRouter;
import org.axonframework.extensions.springcloud.commandhandling.SpringCloudHttpBackupCommandRouter;
import org.axonframework.extensions.springcloud.commandhandling.SpringHttpCommandBusConnector;
import org.axonframework.serialization.Serializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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

	

	//spring cloud settings - distributed command bus
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
    
    // if you don't use Spring Boot Autoconfiguration, you will need to explicitly define the local segment:
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
