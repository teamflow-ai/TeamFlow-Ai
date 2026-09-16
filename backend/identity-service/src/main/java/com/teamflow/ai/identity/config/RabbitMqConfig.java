package com.teamflow.ai.identity.config;

import com.teamflow.ai.common.constant.MessagingConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the shared event topology.
 *
 * <p>Producers own exchange declaration; consumers own their own queues. Both are
 * declared idempotently, so start-up order between services does not matter.
 *
 * <p>Messages are serialized as JSON rather than Java serialization so that the
 * three services stay independently deployable and a payload can be inspected in
 * the RabbitMQ console during a demo.
 */
@Configuration
public class RabbitMqConfig {

    @Bean
    public TopicExchange teamflowExchange() {
        return new TopicExchange(MessagingConstants.TOPIC_EXCHANGE, true, false);
    }

    /** Terminus for messages that exhaust their retry budget. */
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(MessagingConstants.DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(MessagingConstants.DLQ_QUEUE).build();
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with("#");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
