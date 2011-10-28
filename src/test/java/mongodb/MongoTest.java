package mongodb;

import java.io.IOException;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

class DummyOutputStream extends OutputStream {
    public void write(int b) throws IOException {
    }
}

public class MongoTest {

    private static final String TEST_COLL = "t";
    private static final String TEST_DB = "repl2";
    private static final int THREAD_SIZE = 20;
    static Logger logger = Logger.getLogger(MongoTest.class);

    static class MongoFSTask implements Runnable {

        private static final AtomicLong idGen = new AtomicLong(1);

        private GridFS fs;
        private RandBytes rb = new RandBytes();
        private long rand = 0;
        private final Random r = new Random();
        private final long stopTime;

        public MongoFSTask(GridFS fs, long duration) {
            this.fs = fs;
            stopTime = duration + System.currentTimeMillis();
        }

        public void run() {
            logger.info("start");
            while (true) {
                long id = idGen.getAndIncrement();
                GridFSInputFile file = fs
                        .createFile(rb.get(r.nextInt(500) + 30));
                file.setFilename("id-" + id);
                file.save();
                try {
                    TimeUnit.MILLISECONDS.sleep(r.nextInt(100));
                } catch (InterruptedException ignore) {
                    logger.error(ignore.getMessage(), ignore);
                }

                if (id % 5 == 0) {
                    try {
                        rand = r.nextInt() % id;
                    } catch (Exception e1) {

                        e1.printStackTrace();
                    }
                    String filename = "id-" + rand;
                    GridFSDBFile f = fs.findOne(filename);
                    if (f != null) {
                        logger.info("find file " + filename);
                        try {
                            IOUtils.copy(f.getInputStream(),
                                    new DummyOutputStream());
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                        }
                    } else {
                        logger.info("fail to find file " + filename + ", " + id);
                    }
                }

                if (stopTime < System.currentTimeMillis())
                    break;
            }
        }
    }

    private static class MongoTask implements Runnable {

        private final long start;
        private final long maxTime;
        private final DBCollection coll;

        public MongoTask(long start, final long maxtime, DBCollection coll) {
            this.start = start;
            this.maxTime = maxtime;
            this.coll = coll;
        }

        private static final String ALPHA = "abcdefghijklmnopqrstuvwxyz"
                + "1234567890";
        private static final Random r = new Random();

        private static String getRandomString() {
            int length = r.nextInt(20);
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < length; ++i) {
                sb.append(ALPHA.charAt(r.nextInt(ALPHA.length())));
            }
            return sb.toString();
        }

        @Override
        public void run() {
            long i = 0;
            outerloop: while (true) {
                String str = getRandomString();
                while (true) {
                    try {
                        long begin = System.currentTimeMillis();

                        BasicDBObject doc = new BasicDBObject();
                        doc.put("test", str);
                        doc.put("index", i);
                        coll.insert(doc);
                        ++i;

                        boolean find = coll.findOne(doc) != null;
                        long duration = System.currentTimeMillis() - begin;

                        logger.info("success! data count: " + i + "; time: "
                                + duration + "ms; find it: " + find);
                        if (System.currentTimeMillis() - start > maxTime)
                            break outerloop;

                        break;
                    } catch (MongoException ignore) {
                        logger.fatal(ignore.getMessage(), ignore);
                    }
                }
            }
        }

    }

    DBCollection coll;
    GridFS fs;

    @Before
    public void setup() throws UnknownHostException {
        List<ServerAddress> addrs = new ArrayList<ServerAddress>();
        // addrs.add(new ServerAddress("192.168.0.150"));
        // addrs.add(new ServerAddress("192.168.0.12"));
        addrs.add(new ServerAddress("127.0.0.1"));
        Mongo mongo = new Mongo(addrs);
        mongo.slaveOk();

        mongo.dropDatabase(TEST_DB);
        DB db = mongo.getDB(TEST_DB);
        coll = db.getCollection(TEST_COLL);
        fs = new GridFS(db);

    }

    @Test
    public void testMongoFS() throws InterruptedException {
        ExecutorService exec = Executors.newFixedThreadPool(THREAD_SIZE);

        final long duration = TimeUnit.MINUTES.toMillis(5);

        for (int i = 0; i < THREAD_SIZE; ++i) {
            exec.submit(new MongoFSTask(fs, duration));
        }
        exec.shutdown();
        exec.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
    }

    @Test
    public void testReplication() throws UnknownHostException,
            InterruptedException {
        ExecutorService exec = Executors.newFixedThreadPool(THREAD_SIZE);

        long start = System.currentTimeMillis();
        final long duration = TimeUnit.MINUTES.toMillis(20);

        for (int i = 0; i < THREAD_SIZE; ++i) {
            exec.submit(new MongoTask(start, duration, coll));
        }
        exec.shutdown();
        exec.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
    }
}
