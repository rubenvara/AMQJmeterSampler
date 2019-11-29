package com.everis.activemq.jmeter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.commons.io.FileUtils;
import org.apache.jmeter.config.Argument;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

/**
 * Very Simple Queue Producer sending messages to given queue.
 * 
 * 
 * TODO: - do better logging
 * 
 */

public class AMQProducerTopicSampler extends AMQAbstractSampler {

	MessageProducer messageProducer;

	CircularList<TextMessage> queue = new CircularList(10);

	private List files = new ArrayList();

	protected void customizeInit(final Session session, Destination destination) throws JMSException {
		// create a producer
		messageProducer = session.createProducer(destination);
	}

	public static void main(String[] args) {

		AMQProducerTopicSampler test = new AMQProducerTopicSampler();
		Arguments arguments = new Arguments();
		arguments.addArgument(new Argument(DESTINATION_NAME, "VirtualTopic.Patients"));
		arguments.addArgument(new Argument(CONNECTION_URL, DEFAULT_URL_CONNECTION));
		arguments.addArgument(new Argument(TOKEN_USUARIO, "mirthconnect"));
		arguments.addArgument(new Argument(TOKEN_PASSWORD, "mirthconnect"));
		arguments.addArgument(new Argument(FILE_OR_FILES, "C:\\Users\\rvaravar\\Desktop\\SANIDAD\\test"));
		JavaSamplerContext javaSamplerContext = new JavaSamplerContext(arguments);
		test.setupTest(javaSamplerContext);
		test.runTest(javaSamplerContext);
		test.runTest(javaSamplerContext);
		test.teardownTest(javaSamplerContext);

	}

	@Override
	protected void customArguments(Arguments args) {
		args.addArgument(FILE_OR_FILES, "Path to file or dir (only *.xml files are sending)");
	}

	@Override
	protected void customTearDownTest() throws JMSException {
		if (messageProducer != null) {
			messageProducer.close();
		}
	}

	@Override
	protected void validate(SampleResult result) {

		if (messageProducer == null) {
			result.setResponseMessage("MessageProducer Not Initialised");
			result.setSuccessful(false);
		}
	}

	@Override
	protected void customRunTest(SampleResult result) throws JMSException {
		messageProducer.send(queue.hasNext());
	}

	@Override
	protected void customSetupTest(JavaSamplerContext javaSamplerContext) {
		String files = getValue(javaSamplerContext, FILE_OR_FILES, "File or dir url");
		File baseFile = new File(files);

		if (baseFile.isDirectory()) {
			Iterator<File> listFiles = FileUtils.iterateFiles(baseFile, new String[] { "xml" }, false);
			listFiles.forEachRemaining(new Consumer<File>() {
				public void accept(File t) {
					createAndAddFiles(session, t);
				}
			});
		} else {
			if (baseFile.exists()) {
				createAndAddFiles(session, baseFile);
			} else {
				try {
					message.setText("Contenido del mensaje");
				} catch (JMSException e) {
					setErrorState(e);
				}
			}
		}
	}

	private void createAndAddFiles(Session session, File baseFile) {
		try {
			final TextMessage fileMessage = session.createTextMessage();
			fileMessage.setText(FileUtils.readFileToString(baseFile));
			queue.add(fileMessage);
		} catch (JMSException | IOException e) {
			setErrorState(e);
		}
	}

	@Override
	boolean getMode() {

		return false;
	}

	@Override
	protected Destination buildDestination(String destinationName, Session session) throws JMSException {

		return session.createTopic(destinationName);
	}
}
