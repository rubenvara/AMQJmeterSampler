package com.everis.activemq.jmeter;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

public abstract class AMQAbstractSampler implements JavaSamplerClient {

	private static final Logger LOGGER = LoggingManager.getLoggerForClass();
	
	protected static final String DEFAULT_URL_CONNECTION = "tcp://localhost:61616";
	protected static final String TOKEN_PASSWORD = "PASSWORD";
	protected static final String TOKEN_USUARIO = "USERNAME";
	protected static final String CONNECTION_URL = "connectionUrl";
	protected static final String DESTINATION_NAME = "destinationName";
	protected static final String FILE_OR_FILES = "MessagesContent";
	protected Connection connection;
	protected TextMessage message;
	protected Session session;
	protected Exception errorCondition = null;
	protected boolean errorState;
	protected String errorMessage;

	public AMQAbstractSampler() {
		super();
	}

	public Arguments getDefaultParameters() {
		Arguments args = new Arguments();
		args.addArgument(CONNECTION_URL, DEFAULT_URL_CONNECTION);
		args.addArgument(DESTINATION_NAME, "Queue/Topic name");
		args.addArgument(TOKEN_USUARIO, "username" + "");
		args.addArgument(TOKEN_PASSWORD, "pasword");
		customArguments(args);

		return args;

	}

	protected void customArguments(Arguments args) {

	}

	/**
	 * Called once.
	 * 
	 */
	public void setupTest(JavaSamplerContext javaSamplerContext) {

		String connectionUrl = getValue(javaSamplerContext, CONNECTION_URL, DEFAULT_URL_CONNECTION);
		String username = getValue(javaSamplerContext, TOKEN_USUARIO, "username");
		String password = getValue(javaSamplerContext, TOKEN_PASSWORD, "password");
		String destinationName = getValue(javaSamplerContext, DESTINATION_NAME, "JMeter-test-queue");

		initConnection(connectionUrl, destinationName, username, password);
		customSetupTest(javaSamplerContext);

	}

	protected String getValue(JavaSamplerContext javaSamplerContext, String tag, String defaultValue) {
		String destinationName = javaSamplerContext.getParameter(tag);

		if (destinationName == null || (destinationName != null && destinationName.equals(""))) {
			destinationName = defaultValue;
		}
		return destinationName;
	}

	private void initConnection(String connectionUrl, String destinationName, String username, String password) {
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(connectionUrl);
		try {
			
			connection = connectionFactory.createConnection(username, password);
			connection.start();
			session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
			Destination destination = buildDestination(destinationName, session);
			customizeInit(session, destination);

		} catch (JMSException e) {
			setErrorState(e);
			e.printStackTrace();
		}
	}

	abstract boolean getMode();

	protected void setErrorState(Exception e) {
		errorCondition = e;
		errorState = true;
		errorMessage = e.getMessage();
	}

	abstract protected Destination buildDestination(String destinationName, Session session) throws JMSException;

	/**
	 * Called for every loop (defined in the thread group)
	 */
	public SampleResult runTest(JavaSamplerContext javaSamplerContext) {

		SampleResult result = new SampleResult();
		result.setDataType(SampleResult.TEXT);
		result.setSampleLabel("ActiveMQ Test Result");

		try {

			validate(result);
			// start timer
			result.sampleStart();

			customRunTest(result);
			result.sampleEnd();
			result.setSuccessful(true);
			result.setResponseCodeOK();
		} catch (Exception ex) {
			result.setSuccessful(false);

			result.setResponseMessage(ex.getMessage());
			ex.printStackTrace(System.out);

		}

		return result;
	}

	/**
	 * Only called once
	 */
	public void teardownTest(JavaSamplerContext javaSamplerContext) {
		try {
			customTearDownTest();
			if (connection != null) {
				connection.stop();
				connection.close();
			}

		} catch (JMSException e) {

			e.printStackTrace(System.out);
		}

	}

	protected abstract void customSetupTest(JavaSamplerContext javaSamplerContext);

	protected abstract void customizeInit(final Session session, Destination destination) throws JMSException;

	protected abstract void customRunTest(SampleResult result) throws JMSException;

	protected abstract void customTearDownTest() throws JMSException;

	protected abstract void validate(SampleResult result);

}