package compress87;


	public interface ArchiveEntry {

	    /** The name of the entry in the archive. May refer to a file or directory or other item */
		public String getName();
		
		/** The (uncompressed) size of the entry. May be -1 (SIZE_UNKNOWN) if the size is unknown */
		public long getSize();
		
		/** Special value indicating that the size is unknown */
		public static final long SIZE_UNKNOWN = -1;
		
		/** True if the entry refers to a directory */
		public boolean isDirectory();
	}


