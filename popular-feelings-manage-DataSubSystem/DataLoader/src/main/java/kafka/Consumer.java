package kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class Consumer implements Runnable {

    private final KafkaConsumer<Integer, String> consumer;
    private final String topic;
    private final Properties props = new Properties();

    public Consumer(String topic) {
        // key-value序列化所用到的类
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        // kafka集群地址
        props.put("bootstrap.servers", "hadoop1:9092");
        // 自定义消费者所在组 随意
        props.put("group.id", "test");
        // 是否显示最近的消息 如果不配置这不现实最近存储在topic中的数据 只会等待producer发送新的数据
        props.put("auto.offset.reset", "earliest");
        consumer = new KafkaConsumer<>(props);
        this.topic = topic;
    }

    @Override
    public void run() {
        consumer.subscribe(Arrays.asList(topic));
        try {
            while (true) {
                ConsumerRecords<Integer, String> records = consumer.poll(Duration.ofMillis(1000));
                for (ConsumerRecord<Integer, String> record : records) {
                    System.out.println("Receive:offset = " + record.offset() + ", key = " + record.key() + ", value = " + record.value());
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            consumer.close();
        }
    }

    /**
     * 创建main函数运行该java进程
     * @param args
     */
    public static void main(String[] args) {
        Consumer consumer = new Consumer("first_topic");
        consumer.run();
    }
}
