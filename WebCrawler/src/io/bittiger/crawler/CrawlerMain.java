package io.bittiger.crawler;

 import com.rabbitmq.client.*;

 import java.io.IOException;
 import java.nio.charset.StandardCharsets;
 import java.util.List;
 import java.util.concurrent.TimeoutException;

 import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

 import io.bittiger.ad.Ad;

public class CrawlerMain {
    private final static String IN_QUEUE_NAME = "q_feeds";
    private final static String OUT_QUEUE_NAME = "q_product";
    private final static String ERR_QUEUE_NAME = "q_error";

    private static AmazonCrawler crawler;

    private static Channel outChannel;
    private static Channel errChannel;

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        if(args.length < 1)
        {
            System.out.println("Usage: Crawler <proxyFilePath> ");
            System.exit(0);
        }
        ObjectMapper mapper = new ObjectMapper();
        String proxyFilePath = args[0];

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel inChannel = connection.createChannel();
        inChannel.queueDeclare(IN_QUEUE_NAME, true, false, false, null);
        System.out.println(" [x] Waiting for messages. To exit press CTRL+C ");

        Connection connection2 = factory.newConnection();
        outChannel = connection2.createChannel();
        outChannel.queueDeclare(OUT_QUEUE_NAME, true, false, false, null);

        Connection connection3 = factory.newConnection();
        errChannel = connection3.createChannel();
        errChannel.queueDeclare(ERR_QUEUE_NAME, true, false, false, null);

        crawler = new AmazonCrawler(proxyFilePath, errChannel, ERR_QUEUE_NAME);

        Consumer consumer = new DefaultConsumer(inChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                try {
                    String message = new String(body, "UTF-8");
                    System.out.println(" [x] received '" + message + "'");
                    String[] fields = message.split(",");
                    String query = fields[0].trim();
                    double bidPrice = Double.parseDouble((fields[1].trim()));
                    int campaignId = Integer.parseInt(fields[2].trim());
                    int queryGroupId = Integer.parseInt(fields[3].trim());

                    List<Ad> ads =  crawler.GetAdBasicInfoByQuery(query, bidPrice, campaignId, queryGroupId);
                    for(Ad ad : ads) {
                        String jsonInString = mapper.writeValueAsString(ad);
                        System.out.println(jsonInString);
                        outChannel.basicPublish("", OUT_QUEUE_NAME, null, jsonInString.getBytes(StandardCharsets.UTF_8));
                    }
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        inChannel.basicConsume(IN_QUEUE_NAME, true, consumer);

        crawler.cleanup();
    }
}
