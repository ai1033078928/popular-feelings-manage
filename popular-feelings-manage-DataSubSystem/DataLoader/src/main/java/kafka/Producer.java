package kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class Producer implements Runnable{

    private final KafkaProducer<Integer, String> producer;
    private final String topic;
    private final Properties props = new Properties();

    public Producer(String topic){
        // key-value序列化所用到的类
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        // kafka集群地址
        props.put("bootstrap.servers", "hadoop1:9092");
        producer = new KafkaProducer<Integer, String>(props);
        this.topic = topic;
    }

    @Override
    public void run() {
        int messageNo = 1;
        while (true) {
            String messageStr = new String("Message_" + messageNo);
            System.out.println("Send:" + messageStr);

            producer.send(new ProducerRecord<Integer, String>(topic, messageStr));
            messageNo++;
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 创建main函数运行该java进程
     */
    public static void main(String[] args) {
        Producer producer = new Producer("first_topic");
        producer.run();
    }
}
