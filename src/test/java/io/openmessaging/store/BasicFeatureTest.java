package io.openmessaging.store;

import io.openmessaging.InMemoryImpl;
import io.openmessaging.MessageQueue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author chenxi
 * @date 2021/10/8
 */
class BasicFeatureTest extends BaseTest {

    @Test
    void baseTest() throws InterruptedException {
        doBaseTest(getMQ());
    }

    @Disabled
    @Test
    void baseTest_InMemoryImpl() throws InterruptedException {
        doBaseTest(new InMemoryImpl());
    }

    void doBaseTest(MessageQueue mq) throws InterruptedException {

        writeTestData(mq);

        // topic1: 10001(1, 2, 3), 10002(1), 10003(1)
        // topic2: 10001(1), 10002(1)
        //
        // -- diff msg size:
        // topic3: 12345(1)
        // topic4: 23456(1)
        assertAll(
                () -> {
                    Map<Integer, ByteBuffer> map = mq.getRange("wrong-topic", 10001, 0, 1);
                    assertTrue(map.isEmpty(), "wrong-topic");
                },
                () -> {
                    Map<Integer, ByteBuffer> map = mq.getRange("topic1", 10002, 0, 10);
                    assertEquals(1, map.size(), "fetch all msg, (t1, q2)");
                    assertEquals("content-1-10002_1", toString(map.get(0)));
                },
                () -> {
                    Map<Integer, ByteBuffer> map = mq.getRange("topic1", 10001, 2, 10);
                    assertEquals(1, map.size());
                    assertEquals("content-1-10001_3", toString(map.get(0)));
                },
                () -> {
                    Map<Integer, ByteBuffer> map = mq.getRange("topic1", 10001, 1, 2);
                    assertEquals(2, map.size());
                    assertEquals("content-1-10001_2", toString(map.get(0)));
                    assertEquals("content-1-10001_3", toString(map.get(1)));
                },
                () -> {
                    Map<Integer, ByteBuffer> map = mq.getRange("topic2", 10001, 0, 10);
                    assertEquals(2, map.size());
                    assertEquals("content-2-10001_1", toString(map.get(0)));
                    assertEquals("content-2-10001_2", toString(map.get(1)));
                },
                () -> {
                });
    }
}
