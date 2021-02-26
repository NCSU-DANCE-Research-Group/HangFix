package cassandraIO;

//import sun.misc.Unsafe;



public class FastByteComparisons {
	public static int compareTo(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
	    return LexicographicalComparerHolder.BEST_COMPARER.compareTo(
	        b1, s1, l1, b2, s2, l2);
	}
	
	private interface Comparer<T> {
	    abstract public int compareTo(T buffer1, int offset1, int length1,
	        T buffer2, int offset2, int length2);
	}
	
	private static Comparer<byte[]> lexicographicalComparerJavaImpl() {
	    return LexicographicalComparerHolder.PureJavaComparer.INSTANCE;
	}
	
	private static class LexicographicalComparerHolder {
		static final String UNSAFE_COMPARER_NAME =
		        LexicographicalComparerHolder.class.getName() + "$UnsafeComparer";
		
		static final Comparer<byte[]> BEST_COMPARER = getBestComparer();
		
		static Comparer<byte[]> getBestComparer() {
		      String arch = System.getProperty("os.arch");
		      boolean unaligned = arch.equals("i386") || arch.equals("x86")
		                    || arch.equals("amd64") || arch.equals("x86_64");
		      if (!unaligned)
		        return lexicographicalComparerJavaImpl();
		      try {
		        Class<?> theClass = Class.forName(UNSAFE_COMPARER_NAME);

		        // yes, UnsafeComparer does implement Comparer<byte[]>
		        @SuppressWarnings("unchecked")
		        Comparer<byte[]> comparer =
		          (Comparer<byte[]>) theClass.getEnumConstants()[0];
		        return comparer;
		      } catch (Throwable t) { // ensure we really catch *everything*
		        return lexicographicalComparerJavaImpl();
		      }
		}
		
		private enum PureJavaComparer implements Comparer<byte[]> {
		      INSTANCE;

		      @Override
		      public int compareTo(byte[] buffer1, int offset1, int length1,
		          byte[] buffer2, int offset2, int length2) {
		        // Short circuit equal case
		        if (buffer1 == buffer2 &&
		            offset1 == offset2 &&
		            length1 == length2) {
		          return 0;
		        }
		        int end1 = offset1 + length1;
		        int end2 = offset2 + length2;
		        for (int i = offset1, j = offset2; i < end1 && j < end2; i++, j++) {
		          int a = (buffer1[i] & 0xff);
		          int b = (buffer2[j] & 0xff);
		          if (a != b) {
		            return a - b;
		          }
		        }
		        return length1 - length2;
		      }
		}
	}
	
	
}
