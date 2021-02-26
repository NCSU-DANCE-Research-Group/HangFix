package hive13397_18142;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.EnumSet;

import javax.annotation.Nullable;

interface CompressionCodec {

  public enum Modifier {
    /* speed/compression tradeoffs */
    FASTEST,
    FAST,
    DEFAULT,
    /* data sensitivity modifiers */
    TEXT,
    BINARY
  };

  /**
   * Compress the in buffer to the out buffer.
   * @param in the bytes to compress
   * @param out the uncompressed bytes
   * @param overflow put any additional bytes here
   * @return true if the output is smaller than input
   * @throws IOException
   */
  boolean compress(ByteBuffer in, ByteBuffer out, ByteBuffer overflow
                  ) throws IOException;

  /**
   * Decompress the in buffer to the out buffer.
   * @param in the bytes to decompress
   * @param out the decompressed bytes
   * @throws IOException
   */
  void decompress(ByteBuffer in, ByteBuffer out) throws IOException;

  /**
   * Produce a modified compression codec if the underlying algorithm allows
   * modification.
   *
   * This does not modify the current object, but returns a new object if
   * modifications are possible. Returns the same object if no modifications
   * are possible.
   * @param modifiers compression modifiers
   * @return codec for use after optional modification
   */
  CompressionCodec modify(@Nullable EnumSet<Modifier> modifiers);

}
