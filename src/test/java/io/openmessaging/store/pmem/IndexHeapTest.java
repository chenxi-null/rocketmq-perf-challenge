package io.openmessaging.store.pmem;

import io.openmessaging.Config;
import io.openmessaging.DefaultMessageQueueImpl;
import io.openmessaging.store.BaseTest;
import io.openmessaging.store.TopicQueueTable;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author chenxi20
 * @date 2021/10/31
 */
class IndexHeapTest extends BaseTest {

    static Config config = Config.getInstance();

    IndexHeap indexHeap = getMQ().getStore().getIndexHeap();
    TopicQueueTable topicQueueTable = getMQ().getStore().getTopicQueueTable();

    String topic1 = "-topic-1-";
    int queueId1 = 1101;
    int msgBlockHandle = 123;

    String topic2 = "-topic-2-";
    int queueId2 = 1102;
    int msgBlockHandle2 = 456;

    @Test
    void test() throws IOException {
        indexHeap.write(topic1, queueId1, 0, msgBlockHandle);
        indexHeap.load(topicQueueTable, topic1, queueId1);
        assertEquals(msgBlockHandle, topicQueueTable.getPmemOffset(topic1, queueId1, 0));

        indexHeap.write(topic2, queueId2, 0, msgBlockHandle2);
        indexHeap.load(topicQueueTable, topic2, queueId2);
        assertEquals(msgBlockHandle2, topicQueueTable.getPmemOffset(topic2, queueId2, 0));
    }

    @Test
    void blockPartitionForOneQueue() throws IOException {
        long origSize = config.getPmemIndexMemoryBlockSize();
        // one item one block
        config.setPmemIndexMemoryBlockSize(16 + 16);
        try {
            // when: write into one block
            indexHeap.write(topic1, queueId1, 0, msgBlockHandle);
            // and: write into another block
            indexHeap.write(topic1, queueId1, 1, msgBlockHandle2);

            // then: check file
            DefaultMessageQueueImpl newMQ = new DefaultMessageQueueImpl();
            TopicQueueTable tmpTable = newMQ.getStore().getIndexHeap().load();
            System.out.println("tmpTable: " + tmpTable);

            assertEquals(msgBlockHandle, tmpTable.getPmemOffset(topic1, queueId1, 0));
            assertEquals(msgBlockHandle2, tmpTable.getPmemOffset(topic1, queueId1, 1));
        } finally {
            config.setPmemIndexMemoryBlockSize(origSize);
        }
    }
}
