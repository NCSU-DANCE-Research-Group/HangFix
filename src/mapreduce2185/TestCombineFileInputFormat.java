package mapreduce2185;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.ArrayList;
import java.util.zip.GZIPOutputStream;

import junit.framework.TestCase;

import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.hdfs.MiniDFSCluster;
import org.apache.hadoop.hdfs.DFSTestUtil;
import org.apache.hadoop.hdfs.DistributedFileSystem;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.TaskType;
import org.apache.hadoop.mapreduce.lib.input.CombineFileRecordReader;
import org.apache.hadoop.mapreduce.lib.input.CombineFileSplit;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.task.TaskAttemptContextImpl;

public class TestCombineFileInputFormat extends TestCase {

  private static final String rack1[] = new String[] {
    "/r1"
  };
  private static final String hosts1[] = new String[] {
    "host1.rack1.com"
  };
  private static final String rack2[] = new String[] {
    "/r2"
  };
  private static final String hosts2[] = new String[] {
    "host2.rack2.com"
  };
  private static final String rack3[] = new String[] {
    "/r3"
  };
  private static final String hosts3[] = new String[] {
    "host3.rack3.com"
  };
  final Path inDir = new Path("/racktesting");
  final Path outputPath = new Path("/output");
  final Path dir1 = new Path(inDir, "/dir1");
  final Path dir2 = new Path(inDir, "/dir2");
  final Path dir3 = new Path(inDir, "/dir3");
  final Path dir4 = new Path(inDir, "/dir4");
  final Path dir5 = new Path(inDir, "/dir5");

  static final int BLOCKSIZE = 1024;
  static final byte[] databuf = new byte[BLOCKSIZE];

  /** Dummy class to extend CombineFileInputFormat*/
  private class DummyInputFormat extends CombineFileInputFormat<Text, Text> {
    @Override
    public RecordReader<Text,Text> createRecordReader(InputSplit split, 
        TaskAttemptContext context) throws IOException {
      return null;
    }
  }

  /** Dummy class to extend CombineFileInputFormat. It allows
   * testing with files having missing blocks without actually removing replicas.
   */
  public static class MissingBlockFileSystem extends DistributedFileSystem {
    String fileWithMissingBlocks;

    @Override
    public void initialize(URI name, Configuration conf) throws IOException {
      fileWithMissingBlocks = "";
      super.initialize(name, conf);
    }

    @Override
    public BlockLocation[] getFileBlockLocations(
        FileStatus stat, long start, long len) throws IOException {
      if (stat.isDir()) {
        return null;
      }
      System.out.println("File " + stat.getPath());
      String name = stat.getPath().toUri().getPath();
      BlockLocation[] locs =
        super.getFileBlockLocations(stat, start, len);
      if (name.equals(fileWithMissingBlocks)) {
        System.out.println("Returing missing blocks for " + fileWithMissingBlocks);
        locs[0] = new BlockLocation(new String[0], new String[0],
            locs[0].getOffset(), locs[0].getLength());
      }
      return locs;
    }

    public void setFileWithMissingBlocks(String f) {
      fileWithMissingBlocks = f;
    }
  }

  private static final String DUMMY_KEY = "dummy.rr.key";

  private static class DummyRecordReader extends RecordReader<Text, Text> {
    private TaskAttemptContext context;
    private CombineFileSplit s;
    private int idx;
    private boolean used;

    public DummyRecordReader(CombineFileSplit split, TaskAttemptContext context,
        Integer i) {
      this.context = context;
      this.idx = i;
      this.s = split;
      this.used = true;
    }

    /** @return a value specified in the context to check whether the
     * context is properly updated by the initialize() method.
     */
    public String getDummyConfVal() {
      return this.context.getConfiguration().get(DUMMY_KEY);
    }

    public void initialize(InputSplit split, TaskAttemptContext context) {
      this.context = context;
      this.s = (CombineFileSplit) split;

      // By setting used to true in the c'tor, but false in initialize,
      // we can check that initialize() is always called before use
      // (e.g., in testReinit()).
      this.used = false;
    }

    public boolean nextKeyValue() {
      boolean ret = !used;
      this.used = true;
      return ret;
    }

    public Text getCurrentKey() {
      return new Text(this.context.getConfiguration().get(DUMMY_KEY));
    }

    public Text getCurrentValue() {
      return new Text(this.s.getPath(idx).toString());
    }

    public float getProgress() {
      return used ? 1.0f : 0.0f;
    }

    public void close() {
    }
  }


  static void writeFile(Configuration conf, Path name,
      short replication, int numBlocks) throws IOException {
    FileSystem fileSys = FileSystem.get(conf);

    FSDataOutputStream stm = fileSys.create(name, true,
                                            conf.getInt("io.file.buffer.size", 4096),
                                            replication, (long)BLOCKSIZE);
    writeDataAndSetReplication(fileSys, name, stm, replication, numBlocks);
  }

  // Creates the gzip file and return the FileStatus
  static FileStatus writeGzipFile(Configuration conf, Path name,
      short replication, int numBlocks) throws IOException {
    FileSystem fileSys = FileSystem.get(conf);

    GZIPOutputStream out = new GZIPOutputStream(fileSys.create(name, true, conf
        .getInt("io.file.buffer.size", 4096), replication, (long) BLOCKSIZE));
    writeDataAndSetReplication(fileSys, name, out, replication, numBlocks);
    return fileSys.getFileStatus(name);
  }

  private static void writeDataAndSetReplication(FileSystem fileSys, Path name,
      OutputStream out, short replication, int numBlocks) throws IOException {
    for (int i = 0; i < numBlocks; i++) {
      out.write(databuf);
    }
    out.close();
    DFSTestUtil.waitReplication(fileSys, name, replication);
  }
 
  /**
   * Test that CFIF can handle missing blocks.
   */
  public void testMissingBlocks() throws IOException {
    String namenode = null;
    MiniDFSCluster dfs = null;
    FileSystem fileSys = null;
    String testName = "testMissingBlocks";
    try {
      Configuration conf = new Configuration();
      conf.set("fs.hdfs.impl", MissingBlockFileSystem.class.getName());
      conf.setBoolean("dfs.replication.considerLoad", false);
      dfs = new MiniDFSCluster(conf, 1, true, rack1, hosts1);
      dfs.waitActive();

      namenode = (dfs.getFileSystem()).getUri().getHost() + ":" +
                 (dfs.getFileSystem()).getUri().getPort();

      fileSys = dfs.getFileSystem();
      if (!fileSys.mkdirs(inDir)) {
        throw new IOException("Mkdirs failed to create " + inDir.toString());
      }

      Path file1 = new Path(dir1 + "/file1");
      writeFile(conf, file1, (short)1, 1);
      // create another file on the same datanode
      Path file5 = new Path(dir5 + "/file5");
      writeFile(conf, file5, (short)1, 1);

      ((MissingBlockFileSystem)fileSys).setFileWithMissingBlocks(file1.toUri().getPath());
      // split it using a CombinedFile input format
      DummyInputFormat inFormat = new DummyInputFormat();
      Job job = Job.getInstance(conf);
      FileInputFormat.setInputPaths(job, dir1 + "," + dir5);
      List<InputSplit> splits = inFormat.getSplits(job);
      System.out.println("Made splits(Test0): " + splits.size());
      for (InputSplit split : splits) {
        System.out.println("File split(Test0): " + split);
      }
      assertEquals(splits.size(), 1);
      CombineFileSplit fileSplit = (CombineFileSplit) splits.get(0);
      assertEquals(2, fileSplit.getNumPaths());
      assertEquals(1, fileSplit.getLocations().length);
      assertEquals(file1.getName(), fileSplit.getPath(0).getName());
      assertEquals(0, fileSplit.getOffset(0));
      assertEquals(BLOCKSIZE, fileSplit.getLength(0));
      assertEquals(file5.getName(), fileSplit.getPath(1).getName());
      assertEquals(0, fileSplit.getOffset(1));
      assertEquals(BLOCKSIZE, fileSplit.getLength(1));
      assertEquals(hosts1[0], fileSplit.getLocations()[0]);

    } finally {
      if (dfs != null) {
    	try {
			Thread.sleep(60000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        dfs.shutdown();
      }
    }
  }

  static class TestFilter implements PathFilter {
    private Path p;

    // store a path prefix in this TestFilter
    public TestFilter(Path p) {
      this.p = p;
    }

    // returns true if the specified path matches the prefix stored
    // in this TestFilter.
    public boolean accept(Path path) {
      if (path.toString().indexOf(p.toString()) == 0) {
        return true;
      }
      return false;
    }

    public String toString() {
      return "PathFilter:" + p;
    }
  }

  /*
   * Prints out the input splits for the specified files
   */
  private void splitRealFiles(String[] args) throws IOException {
    Configuration conf = new Configuration();
    Job job = Job.getInstance();
    FileSystem fs = FileSystem.get(conf);
    if (!(fs instanceof DistributedFileSystem)) {
      throw new IOException("Wrong file system: " + fs.getClass().getName());
    }
    int blockSize = conf.getInt("dfs.block.size", 128 * 1024 * 1024);

    DummyInputFormat inFormat = new DummyInputFormat();
    for (int i = 0; i < args.length; i++) {
      FileInputFormat.addInputPaths(job, args[i]);
    }
    inFormat.setMinSplitSizeRack(blockSize);
    inFormat.setMaxSplitSize(10 * blockSize);

    List<InputSplit> splits = inFormat.getSplits(job);
    System.out.println("Total number of splits " + splits.size());
    for (int i = 0; i < splits.size(); ++i) {
      CombineFileSplit fileSplit = (CombineFileSplit) splits.get(i);
      System.out.println("Split[" + i + "] " + fileSplit);
    }
  }

//  public static void main(String[] args) throws Exception{
//
//    // if there are some parameters specified, then use those paths
//    if (args.length != 0) {
//      TestCombineFileInputFormat test = new TestCombineFileInputFormat();
//      test.splitRealFiles(args);
//    } else {
//      TestCombineFileInputFormat test = new TestCombineFileInputFormat();
//      test.testSplitPlacement();
//    }
//  }
}
