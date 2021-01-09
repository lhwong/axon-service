package org.learn.axonframework.queryservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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
