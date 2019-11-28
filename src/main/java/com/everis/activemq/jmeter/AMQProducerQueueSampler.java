package com.everis.activemq.jmeter;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

public class AMQProducerQueueSampler extends AMQProducerTopicSampler {

	@Override
	protected Destination buildDestination(String destinationName, Session session) throws JMSException {

		return session.createQueue(destinationName);
	}
}
