package org.learn.axonframework.queryservice;

import com.rabbitmq.client.Channel;

import org.axonframework.extensions.amqp.eventhandling.AMQPMessageConverter;
import org.axonframework.extensions.amqp.eventhandling.DefaultAMQPMessageConverter;
import org.axonframework.extensions.amqp.eventhandling.spring.SpringAMQPMessageSource;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class QueryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(QueryServiceApplication.class, args);
	}
	
	/*@Bean
    public SpringAMQPMessageSource queryEvents(AMQPMessageConverter messageConverter) {
        return new SpringAMQPMessageSource(messageConverter) {

            @RabbitListener(queues = "QueryQueue")
            @Override
            public void onMessage(Message message, Channel channel) {
                super.onMessage(message, channel);
            }
        };
    }*/
	
	/*@Bean
	public SpringAMQPMessageSource queryEvents(Serializer serializer) {
		return new SpringAMQPMessageSource(new DefaultAMQPMessageConverter(serializer)) {

			@RabbitListener(queues = "QueryQueue")
			@Override
			public void onMessage(Message message, Channel channel){
				super.onMessage(message, channel);
			}
		};
	}*/
}
