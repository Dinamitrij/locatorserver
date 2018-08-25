package lv.div.locator.mqueue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Stateless;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

/**
 * Sending information about current safe zone
 */
@Stateless
public class ZoneDataMQSender extends GenericQueue {

    private Channel channel;
    private Connection connection;
    private HashMap<String, Object> defaultQueueArguments;

    @PostConstruct
    public void init() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("ssss");
        factory.setVirtualHost("ddd");
        factory.setUsername("asdasd");
        factory.setPassword("asdasdasdsa");
        factory.setAutomaticRecoveryEnabled(true);
        //Recommended settings
        factory.setRequestedHeartbeat(30);
        factory.setConnectionTimeout(30000);

//   		jsonHelper = new JsonHelper();

        try {
            connection = factory.newConnection();
            channel = connection.createChannel();

            defaultQueueArguments = getDefaultQueueArguments();

            channel.addShutdownListener(new ShutdownListener() {
                @Override
                public void shutdownCompleted(ShutdownSignalException e) {
                    //System.out.println("RabbitMQ[channel] Shutdown!");
                    //System.out.print(e);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void shutdown() {
        try {
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Data sender
     *
     * @param exchangeName
     * @param queueName    Mobile device ID
     * @param routingKey   Mobile device ID
     * @param textToSend
     *
     * @throws Exception
     */
    public void sendData(String exchangeName, String queueName, String routingKey, String textToSend) {

        boolean durable = true;    //durable - RabbitMQ will never lose the queue if a crash occurs
        boolean exclusive = false;  //exclusive - if queue only will be used by one connection
        boolean autoDelete = false; //autodelete - queue is deleted when last consumer unsubscribes

        // MLSFences!!!!!

        try {
            channel.exchangeDeclare(exchangeName, "direct", durable);
            channel.queueDeclare(queueName, durable, exclusive, autoDelete, defaultQueueArguments);
            channel.queueBind(queueName, exchangeName, routingKey);

            final byte[] textData = textToSend.getBytes();
            channel.basicPublish(exchangeName, routingKey, MessageProperties.PERSISTENT_TEXT_PLAIN, textData);
        } catch (IOException e) {
            System.out.println("ERROR SENDING DATA TO RABBIT_MQ!!!");
            e.printStackTrace();
        }

    }
}
