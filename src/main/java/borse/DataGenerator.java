package borse;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class DataGenerator {
  /**
   * Randomly generated int.
   */

  private static final Random random = new Random();
  

  public DataGenerator() {
  }


  public static synchronized int getData(int min, int max) {
    return random.nextInt(max - min + 1) + min;
  }

}
