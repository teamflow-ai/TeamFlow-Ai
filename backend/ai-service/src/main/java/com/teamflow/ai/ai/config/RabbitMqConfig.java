package com.teamflow.ai.ai.config;

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
 * Declares the shared exchange topology plus every queue this service actually
 * consumes from.
 *
 * <p>Unlike the producer-only services, each queue here is wired with a
 * dead-letter exchange: a listener failing three times (see
 * {@code spring.rabbitmq.listener.simple.retry} in {@code application.yml}) parks
 * the message on the DLQ for inspection instead of silently dropping it or
 * requeuing forever.
 */
@Configuration
public class RabbitMqConfig {

    @Bean
    public TopicExchange teamflowExchange() {
        return new TopicExchange(MessagingConstants.TOPIC_EXCHANGE, true, false);
    }

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
    public Queue employeeEventsQueue() {
        return durableWithDlx(MessagingConstants.QUEUE_AI_EMPLOYEE_EVENTS);
    }

    @Bean
    public Binding employeeEventsBinding(Queue employeeEventsQueue, TopicExchange teamflowExchange) {
        return BindingBuilder.bind(employeeEventsQueue).to(teamflowExchange).with("employee.#");
    }

    @Bean
    public Queue projectEventsQueue() {
        return durableWithDlx(MessagingConstants.QUEUE_AI_PROJECT_EVENTS);
    }

    @Bean
    public Binding projectEventsBinding(Queue projectEventsQueue, TopicExchange teamflowExchange) {
        return BindingBuilder.bind(projectEventsQueue).to(teamflowExchange).with("project.#");
    }

    @Bean
    public Queue taskEventsQueue() {
        return durableWithDlx(MessagingConstants.QUEUE_AI_TASK_EVENTS);
    }

    @Bean
    public Binding taskEventsBinding(Queue taskEventsQueue, TopicExchange teamflowExchange) {
        return BindingBuilder.bind(taskEventsQueue).to(teamflowExchange).with("task.#");
    }

    @Bean
    public Queue bugEventsQueue() {
        return durableWithDlx(MessagingConstants.QUEUE_AI_BUG_EVENTS);
    }

    @Bean
    public Binding bugEventsBinding(Queue bugEventsQueue, TopicExchange teamflowExchange) {
        return BindingBuilder.bind(bugEventsQueue).to(teamflowExchange).with("bug.#");
    }

    @Bean
    public Queue meetingEventsQueue() {
        return durableWithDlx(MessagingConstants.QUEUE_AI_MEETING_EVENTS);
    }

    @Bean
    public Binding meetingEventsBinding(Queue meetingEventsQueue, TopicExchange teamflowExchange) {
        return BindingBuilder.bind(meetingEventsQueue).to(teamflowExchange).with("meeting.#");
    }

    @Bean
    public Queue leaveEventsQueue() {
        return durableWithDlx(MessagingConstants.QUEUE_AI_LEAVE_EVENTS);
    }

    @Bean
    public Binding leaveEventsBinding(Queue leaveEventsQueue, TopicExchange teamflowExchange) {
        return BindingBuilder.bind(leaveEventsQueue).to(teamflowExchange).with("leave.#");
    }

    @Bean
    public Queue notificationEventsQueue() {
        return durableWithDlx(MessagingConstants.QUEUE_NOTIFICATION_EVENTS);
    }

    @Bean
    public Binding notificationEventsBinding(Queue notificationEventsQueue, TopicExchange teamflowExchange) {
        return BindingBuilder.bind(notificationEventsQueue).to(teamflowExchange).with(MessagingConstants.NOTIFICATION_CREATED);
    }

    @Bean
    public Queue approvalEventsQueue() {
        return durableWithDlx(MessagingConstants.QUEUE_AI_APPROVAL_EVENTS);
    }

    @Bean
    public Binding approvalEventsBinding(Queue approvalEventsQueue, TopicExchange teamflowExchange) {
        return BindingBuilder.bind(approvalEventsQueue).to(teamflowExchange).with("approval.#");
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

    private Queue durableWithDlx(String name) {
        return QueueBuilder.durable(name)
                .withArgument("x-dead-letter-exchange", MessagingConstants.DLX_EXCHANGE)
                .build();
    }
}
