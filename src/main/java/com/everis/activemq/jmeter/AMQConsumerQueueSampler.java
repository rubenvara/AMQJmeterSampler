package com.everis.activemq.jmeter;

import java.util.Random;

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
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import edu.emory.mathcs.backport.java.util.concurrent.TimeUnit;

public class AMQConsumerQueueSampler extends AMQAbstractSampler {

	private static final Logger LOGGER = LoggingManager.getLoggerForClass();

	private static final String AVERAGE_TAG = "AVERAGE";
	private static final String TIMELIFE_TAG = "TIMELIFE";

	private static final int DEFAULT_AVERAGE = 100;
	private static final int DEFAULT_TIME_LIFE = 250;

	Random random = new Random(100);

	MessageConsumer consumer = null;

	private int media = 0;
	private int timeLife = DEFAULT_TIME_LIFE;

	private static final CircularList<Boolean> circularList = new CircularList<Boolean>(100);

	@Override
	protected void customizeInit(Session session, Destination destination) throws JMSException {

		consumer = session.createConsumer(destination);

	}

	private int getValorNumerico(String valor, int valorDefecto) {
		int valorAux;
		if (valor.matches("[0-9]*")) {
			valorAux = Integer.valueOf(valor);
		} else {
			valorAux = valorDefecto;
			LOGGER.info("Se evalua valor por defecto");
		}
		return valorAux;
	}

	@Override
	protected void customRunTest(SampleResult result) throws JMSException {
		if (consumer != null) {

			Message textMessage = consumer.receive(1000);// Si no hay mensajes se cancela
			if (circularList.hasNext().booleanValue()) {
				sleepABit();
				textMessage.acknowledge();
				LOGGER.info("Mensaje aceptado");
			} else {
				LOGGER.info("Mensaje rechazado");
			}

			if (textMessage != null) {
				if (textMessage instanceof TextMessage) {
					result.setResponseMessage("Se ha recuperado el mensaje \n" + ((TextMessage) textMessage).getText());
				} else {
					result.setResponseMessage("Se ha recuperado un mensaje que no es de TextMessage");
				}
			} else {
				result.setResponseMessage("Se aborta por falta de mensajes");
			}

		} else {
			result.setResponseMessage("No se ha creado el consumidor correctamente");
		}

	}

	private void sleepABit() {
		try {
			TimeUnit.MILLISECONDS.sleep(timeLife);
		} catch (InterruptedException e) {
			e.printStackTrace();
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
		arguments.addArgument(new Argument(AVERAGE_TAG, "50"));
		arguments.addArgument(new Argument(TIMELIFE_TAG, "100"));

		JavaSamplerContext javaSamplerContext = new JavaSamplerContext(arguments);
		test.setupTest(javaSamplerContext);
		for (int i = 0; i < 10; i++) {
			test.runTest(javaSamplerContext);
		}

		test.teardownTest(javaSamplerContext);

	}

	@Override
	boolean getMode() {

		return true;
	}

	protected Destination buildDestination(String destinationName, Session session) throws JMSException {
		return new ActiveMQQueue(destinationName);
		// return session.createQueue(destinationName);
	}

	@Override
	protected void customSetupTest(JavaSamplerContext javaSamplerContext) {
		String valor = getValue(javaSamplerContext, AVERAGE_TAG, String.valueOf(DEFAULT_AVERAGE));
		String aux = getValue(javaSamplerContext, TIMELIFE_TAG, String.valueOf(DEFAULT_TIME_LIFE));
		media = getValorNumerico(valor, DEFAULT_AVERAGE);
		timeLife = getValorNumerico(aux, DEFAULT_TIME_LIFE);
		for (int i = 0; i < 100; i++) {
			circularList.add(Boolean.valueOf(random.nextInt(100) < media));
		}
	}

	@Override
	protected void customArguments(Arguments args) {
		args.addArgument(AVERAGE_TAG, "70");
		args.addArgument(TIMELIFE_TAG, "1000");
	}

}
