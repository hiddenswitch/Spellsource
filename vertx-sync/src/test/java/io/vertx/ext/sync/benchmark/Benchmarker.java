package io.vertx.ext.sync.benchmark;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class Benchmarker {

  private final long numItsReport;

  public Benchmarker(long numItsReport) {
    this.numItsReport = numItsReport;
  }

  private long count;
  private long resTot;
  private long start;

  public void iterDone(int res) {
    if (count == 0) {
      start = System.currentTimeMillis();
    }
    count++;
    resTot += res;
    if (count == numItsReport) {
      long end = System.currentTimeMillis();
      double rate = 1000 * (double)numItsReport/ (end - start);
      System.out.println("Rate is: " + rate + " iterations per second. r: " + resTot);
      count = 0;
    }
  }


}
