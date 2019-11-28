package com.everis.activemq.jmeter;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

public class AMQConsumerQueueSampler extends AMQAbstractSampler {

	MessageConsumer consumer;

	@Override
	protected void customizeInit(Session session, Destination destination) throws JMSException {
		
		consumer = session.createConsumer(destination);
		
	}

	@Override
	protected void customRunTest(SampleResult result) throws JMSException {
		if (consumer != null) {
			
			Message textMessage = consumer.receive();
			
			if (textMessage instanceof TextMessage) {
				result.setResponseMessage("Se ha recuperado el mensaje \n" + ((TextMessage) textMessage).getText());
			} else {
				result.setResponseMessage("Se ha recuperado un mensaje que no es de TextMessage");
			}
			
		} else {
			result.setResponseMessage("No se ha creado el consumidor correctamente");
		}

	}

	@Override
	protected void customTearDownTest() throws JMSException {
		if (consumer != null) {
			consumer.close();
		}
	}

	@Override
	protected void validate(SampleResult result) {

		if (consumer == null) {
			result.setResponseMessage("Consumer Not Initialised");
			result.setSuccessful(false);
		}
	}

	public static void main(String[] args) {

		AMQConsumerQueueSampler test = new AMQConsumerQueueSampler();
		Arguments arguments = new Arguments();
		arguments.addArgument(new Argument(DESTINATION_NAME, "Consumer.Patients.Core"));
		arguments.addArgument(new Argument(CONNECTION_URL, DEFAULT_URL_CONNECTION));
		arguments.addArgument(new Argument(TOKEN_USUARIO, "core"));
		arguments.addArgument(new Argument(TOKEN_PASSWORD, "core"));

		JavaSamplerContext javaSamplerContext = new JavaSamplerContext(arguments);
		test.setupTest(javaSamplerContext);
		test.runTest(javaSamplerContext);
		test.runTest(javaSamplerContext);
		test.teardownTest(javaSamplerContext);

	}

	@Override
	boolean getMode() {

		return true;
	}

	protected Destination buildDestination(String destinationName, Session session) throws JMSException {
		return new ActiveMQQueue(destinationName);
//		return session.createQueue(destinationName);
	}

	@Override
	protected void customSetupTest(JavaSamplerContext javaSamplerContext) {

	}

}
