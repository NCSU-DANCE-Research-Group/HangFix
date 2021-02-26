package hdfs4882;

import hdfs4882.LeaseManager.Lease;

import java.io.IOException;
import org.apache.hadoop.fs.UnresolvedLinkException;
import org.apache.hadoop.hdfs.protocol.AlreadyBeingCreatedException;
import org.apache.hadoop.hdfs.protocol.Block;
//import org.apache.hadoop.hdfs.server.blockmanagement.BlockInfo;
//import org.apache.hadoop.hdfs.server.blockmanagement.BlockInfoUnderConstruction;
import org.apache.hadoop.hdfs.server.common.HdfsServerConstants.BlockUCState;
import org.apache.hadoop.hdfs.server.namenode.FSClusterStats;
//import org.apache.hadoop.hdfs.server.namenode.LeaseManager.Lease;
import org.apache.hadoop.hdfs.server.namenode.NameNodeMXBean;
import org.apache.hadoop.hdfs.server.namenode.Namesystem;
//import org.apache.hadoop.hdfs.server.namenode.FSNamesystem.SafeModeInfo;
//import org.apache.hadoop.hdfs.server.namenode.FSNamesystem.SafeModeMonitor;
import org.apache.hadoop.hdfs.server.namenode.metrics.FSNamesystemMBean;
import org.apache.hadoop.security.AccessControlException;

public class FSNamesystem implements Namesystem, FSClusterStats, FSNamesystemMBean, NameNodeMXBean {
	
//	static final Log LOG = LogFactory.getLog(FSNamesystem.class);
//	FSDirectory dir;
//	
//	private UserGroupInformation fsOwner;
//	private String supergroup;
//	
//	PermissionStatus createFsOwnerPermissions(FsPermission permission) {
//	    return new PermissionStatus(fsOwner.getShortUserName(), supergroup, permission);
//	}
//	
//	private BlockManager blockManager;
//	private volatile SafeModeInfo safeMode;  // safe mode information
//	
//	LeaseManager leaseManager = new LeaseManager(this); 
	FSNamesystem(){
		
	}
	
	boolean internalReleaseLeasePatch(hdfs4882.LeaseManagerPatch.Lease leaseToCheck, String src, 
		      String recoveryLeaseHolder) throws AlreadyBeingCreatedException, 
		      IOException, UnresolvedLinkException {
		return false;
	}
	
	/**
	   * Move a file that is being written to be immutable.
	   * @param src The filename
	   * @param lease The lease for the client creating the file
	   * @param recoveryLeaseHolder reassign lease to this holder if the last block
	   *        needs recovery; keep current holder if null.
	   * @throws AlreadyBeingCreatedException if file is waiting to achieve minimal
	   *         replication;<br>
	   *         RecoveryInProgressException if lease recovery is in progress.<br>
	   *         IOException in case of an error.
	   * @return true  if file has been successfully finalized and closed or 
	   *         false if block recovery has been initiated
	   */
	  boolean internalReleaseLease(Lease lease, String src, 
	      String recoveryLeaseHolder) throws AlreadyBeingCreatedException, 
	      IOException, UnresolvedLinkException {
//	    LOG.info("Recovering lease=" + lease + ", src=" + src);
//	    assert !isInSafeMode();
//	    assert hasWriteLock();
//	    INodeFile iFile = dir.getFileINode(src);
//	    if (iFile == null) {
//	      final String message = "DIR* NameSystem.internalReleaseLease: "
//	        + "attempt to release a create lock on "
//	        + src + " file does not exist.";
//	      NameNode.stateChangeLog.warn(message);
//	      throw new IOException(message);
//	    }
//	    if (!iFile.isUnderConstruction()) {
//	      final String message = "DIR* NameSystem.internalReleaseLease: "
//	        + "attempt to release a create lock on "
//	        + src + " but file is already closed.";
//	      NameNode.stateChangeLog.warn(message);
//	      throw new IOException(message);
//	    }
//
//	    INodeFileUnderConstruction pendingFile = (INodeFileUnderConstruction) iFile;
//	    int nrBlocks = pendingFile.numBlocks();
//	    BlockInfo[] blocks = pendingFile.getBlocks();
//
//	    int nrCompleteBlocks;
//	    BlockInfo curBlock = null;
//	    for(nrCompleteBlocks = 0; nrCompleteBlocks < nrBlocks; nrCompleteBlocks++) {
//	      curBlock = blocks[nrCompleteBlocks];
//	      if(!curBlock.isComplete())
//	        break;
//	      assert blockManager.checkMinReplication(curBlock) :
//	              "A COMPLETE block is not minimally replicated in " + src;
//	    }
//
//	    // If there are no incomplete blocks associated with this file,
//	    // then reap lease immediately and close the file.
//	    if(nrCompleteBlocks == nrBlocks) {
//	      finalizeINodeFileUnderConstruction(src, pendingFile);
//	      NameNode.stateChangeLog.warn("BLOCK*"
//	        + " internalReleaseLease: All existing blocks are COMPLETE,"
//	        + " lease removed, file closed.");
//	      return true;  // closed!
//	    }

//	    // Only the last and the penultimate blocks may be in non COMPLETE state.
//	    // If the penultimate block is not COMPLETE, then it must be COMMITTED.
//	    if(nrCompleteBlocks < nrBlocks - 2 ||
//	       nrCompleteBlocks == nrBlocks - 2 &&
//	         curBlock.getBlockUCState() != BlockUCState.COMMITTED) {
//	      final String message = "DIR* NameSystem.internalReleaseLease: "
//	        + "attempt to release a create lock on "
//	        + src + " but file is already closed.";
//	      NameNode.stateChangeLog.warn(message);
//	      throw new IOException(message);
//	    }

	    // no we know that the last block is not COMPLETE, and
	    // that the penultimate block if exists is either COMPLETE or COMMITTED
//	    BlockInfoUnderConstruction lastBlock = pendingFile.getLastBlock();
//	    BlockUCState lastBlockState = lastBlock.getBlockUCState();
	    BlockUCState lastBlockState = BlockUCState.COMPLETE;
//	    BlockInfo penultimateBlock = pendingFile.getPenultimateBlock();
//	    boolean penultimateBlockMinReplication;
//	    BlockUCState penultimateBlockState;
//	    if (penultimateBlock == null) {
//	      penultimateBlockState = BlockUCState.COMPLETE;
//	      // If penultimate block doesn't exist then its minReplication is met
//	      penultimateBlockMinReplication = true;
//	    } else {
//	      penultimateBlockState = BlockUCState.COMMITTED;
//	      penultimateBlockMinReplication = 
//	        blockManager.checkMinReplication(penultimateBlock);
//	    }
	    BlockUCState penultimateBlockState = BlockUCState.COMMITTED;
	    assert penultimateBlockState == BlockUCState.COMPLETE ||
	           penultimateBlockState == BlockUCState.COMMITTED :
	           "Unexpected state of penultimate block in " + src;

	    switch(lastBlockState) {
	    case COMPLETE:
	      assert false : "Already checked that the last block is incomplete";
	      break;
//	    case COMMITTED:
//	      // Close file if committed blocks are minimally replicated
//	      if(penultimateBlockMinReplication &&
//	          blockManager.checkMinReplication(lastBlock)) {
//	        finalizeINodeFileUnderConstruction(src, pendingFile);
//	        NameNode.stateChangeLog.warn("BLOCK*"
//	          + " internalReleaseLease: Committed blocks are minimally replicated,"
//	          + " lease removed, file closed.");
//	        return true;  // closed!
//	      }
//	      // Cannot close file right now, since some blocks 
//	      // are not yet minimally replicated.
//	      // This may potentially cause infinite loop in lease recovery
//	      // if there are no valid replicas on data-nodes.
//	      String message = "DIR* NameSystem.internalReleaseLease: " +
//	          "Failed to release lease for file " + src +
//	          ". Committed blocks are waiting to be minimally replicated." +
//	          " Try again later.";
//	      NameNode.stateChangeLog.warn(message);
//	      throw new AlreadyBeingCreatedException(message);
//	    case UNDER_CONSTRUCTION:
//	    case UNDER_RECOVERY:
//	      // setup the last block locations from the blockManager if not known
//	      if(lastBlock.getNumExpectedLocations() == 0)
//	        lastBlock.setExpectedLocations(blockManager.getNodes(lastBlock));
//	      // start recovery of the last block for this file
//	      long blockRecoveryId = nextGenerationStamp();
//	      lease = reassignLease(lease, src, recoveryLeaseHolder, pendingFile);
//	      lastBlock.initializeBlockRecovery(blockRecoveryId);
//	      leaseManager.renewLease(lease);
//	      // Cannot close file right now, since the last block requires recovery.
//	      // This may potentially cause infinite loop in lease recovery
//	      // if there are no valid replicas on data-nodes.
//	      NameNode.stateChangeLog.warn(
//	                "DIR* NameSystem.internalReleaseLease: " +
//	                "File " + src + " has not been closed." +
//	               " Lease recovery is in progress. " +
//	                "RecoveryId = " + blockRecoveryId + " for block " + lastBlock);
//	      break;
	    }
	    return false;
	  }
	@Override
	public void readLock() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void readUnlock() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean hasReadLock() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void writeLock() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void writeUnlock() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean hasWriteLock() {
		// TODO Auto-generated method stub
		return true;
	}
	@Override
	public boolean hasReadOrWriteLock() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void checkSafeMode() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean isInSafeMode() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean isInStartupSafeMode() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean isPopulatingReplQueues() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void incrementSafeBlockCount(int replication) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void decrementSafeBlockCount(Block b) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public long getUsed() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public long getFree() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public long getTotal() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public String getSafemode() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean isUpgradeFinalized() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public long getNonDfsUsedSpace() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public float getPercentUsed() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public float getPercentRemaining() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public long getBlockPoolUsedSpace() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public float getPercentBlockPoolUsed() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public long getTotalBlocks() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public long getTotalFiles() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public long getNumberOfMissingBlocks() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public int getThreads() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public String getLiveNodes() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getDeadNodes() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getDecomNodes() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getClusterId() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getFSState() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public long getBlocksTotal() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public long getCapacityTotal() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public long getCapacityRemaining() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public long getCapacityUsed() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public long getFilesTotal() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public long getPendingReplicationBlocks() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public long getUnderReplicatedBlocks() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public long getScheduledReplicationBlocks() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public int getNumLiveDataNodes() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public int getNumDeadDataNodes() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public int getTotalLoad() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public boolean isRunning() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void checkSuperuserPrivilege() throws AccessControlException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public String getBlockPoolId() {
		// TODO Auto-generated method stub
		return null;
	}

	  
	  /**
	   * SafeModeInfo contains information related to the safe mode.
	   * <p>
	   * An instance of {@link SafeModeInfo} is created when the name node
	   * enters safe mode.
	   * <p>
	   * During name node startup {@link SafeModeInfo} counts the number of
	   * <em>safe blocks</em>, those that have at least the minimal number of
	   * replicas, and calculates the ratio of safe blocks to the total number
	   * of blocks in the system, which is the size of blocks in
	   * {@link FSNamesystem#blockManager}. When the ratio reaches the
	   * {@link #threshold} it starts the {@link SafeModeMonitor} daemon in order
	   * to monitor whether the safe mode {@link #extension} is passed.
	   * Then it leaves safe mode and destroys itself.
	   * <p>
	   * If safe mode is turned on manually then the number of safe blocks is
	   * not tracked because the name node is not intended to leave safe mode
	   * automatically in the case.
	   *
	   * @see ClientProtocol#setSafeMode(HdfsConstants.SafeModeAction)
	   * @see SafeModeMonitor
	   */
//	  class SafeModeInfo {
//	    // configuration fields
//	    /** Safe mode threshold condition %.*/
//	    private double threshold;
//	    /** Safe mode minimum number of datanodes alive */
//	    private int datanodeThreshold;
//	    /** Safe mode extension after the threshold. */
//	    private int extension;
//	    /** Min replication required by safe mode. */
//	    private int safeReplication;
//	    /** threshold for populating needed replication queues */
//	    private double replQueueThreshold;
//	      
//	    // internal fields
//	    /** Time when threshold was reached.
//	     * 
//	     * <br>-1 safe mode is off
//	     * <br> 0 safe mode is on, but threshold is not reached yet 
//	     */
//	    private long reached = -1;  
//	    /** Total number of blocks. */
//	    int blockTotal; 
//	    /** Number of safe blocks. */
//	    private int blockSafe;
//	    /** Number of blocks needed to satisfy safe mode threshold condition */
//	    private int blockThreshold;
//	    /** Number of blocks needed before populating replication queues */
//	    private int blockReplQueueThreshold;
//	    /** time of the last status printout */
//	    private long lastStatusReport = 0;
//	    /** flag indicating whether replication queues have been initialized */
//	    private boolean initializedReplQueues = false;
//	    /** Was safemode entered automatically because available resources were low. */
//	    private boolean resourcesLow = false;
//	    
//	    /**
//	     * Creates SafeModeInfo when the name node enters
//	     * automatic safe mode at startup.
//	     *  
//	     * @param conf configuration
//	     */
//	    private SafeModeInfo(Configuration conf) {
//	      this.threshold = conf.getFloat(DFS_NAMENODE_SAFEMODE_THRESHOLD_PCT_KEY,
//	          DFS_NAMENODE_SAFEMODE_THRESHOLD_PCT_DEFAULT);
//	      if(threshold > 1.0) {
//	        LOG.warn("The threshold value should't be greater than 1, threshold: " + threshold);
//	      }
//	      this.datanodeThreshold = conf.getInt(
//	        DFS_NAMENODE_SAFEMODE_MIN_DATANODES_KEY,
//	        DFS_NAMENODE_SAFEMODE_MIN_DATANODES_DEFAULT);
//	      this.extension = conf.getInt(DFS_NAMENODE_SAFEMODE_EXTENSION_KEY, 0);
//	      this.safeReplication = conf.getInt(DFS_NAMENODE_REPLICATION_MIN_KEY, 
//	                                         DFS_NAMENODE_REPLICATION_MIN_DEFAULT);
//	      // default to safe mode threshold (i.e., don't populate queues before leaving safe mode)
//	      this.replQueueThreshold = 
//	        conf.getFloat(DFS_NAMENODE_REPL_QUEUE_THRESHOLD_PCT_KEY,
//	                      (float) threshold);
//	      this.blockTotal = 0; 
//	      this.blockSafe = 0;
//	    }
//
//	    /**
//	     * Creates SafeModeInfo when safe mode is entered manually, or because
//	     * available resources are low.
//	     *
//	     * The {@link #threshold} is set to 1.5 so that it could never be reached.
//	     * {@link #blockTotal} is set to -1 to indicate that safe mode is manual.
//	     * 
//	     * @see SafeModeInfo
//	     */
//	    private SafeModeInfo(boolean resourcesLow) {
//	      this.threshold = 1.5f;  // this threshold can never be reached
//	      this.datanodeThreshold = Integer.MAX_VALUE;
//	      this.extension = Integer.MAX_VALUE;
//	      this.safeReplication = Short.MAX_VALUE + 1; // more than maxReplication
//	      this.replQueueThreshold = 1.5f; // can never be reached
//	      this.blockTotal = -1;
//	      this.blockSafe = -1;
//	      this.reached = -1;
//	      this.resourcesLow = resourcesLow;
//	      enter();
//	      reportStatus("STATE* Safe mode is ON.", true);
//	    }
//	      
//	    /**
//	     * Check if safe mode is on.
//	     * @return true if in safe mode
//	     */
//	    private synchronized boolean isOn() {
//	      try {
//	        assert isConsistent() : " SafeMode: Inconsistent filesystem state: "
//	          + "Total num of blocks, active blocks, or "
//	          + "total safe blocks don't match.";
//	      } catch(IOException e) {
//	        System.err.print(StringUtils.stringifyException(e));
//	      }
//	      return this.reached >= 0;
//	    }
//	      
//	    /**
//	     * Check if we are populating replication queues.
//	     */
//	    private synchronized boolean isPopulatingReplQueues() {
//	      return initializedReplQueues;
//	    }
//
//	    /**
//	     * Enter safe mode.
//	     */
//	    private void enter() {
//	      this.reached = 0;
//	    }
//	      
//	    /**
//	     * Leave safe mode.
//	     * <p>
//	     * Switch to manual safe mode if distributed upgrade is required.<br>
//	     * Check for invalid, under- & over-replicated blocks in the end of startup.
//	     */
//	    private synchronized void leave(boolean checkForUpgrades) {
//	      if(checkForUpgrades) {
//	        // verify whether a distributed upgrade needs to be started
//	        boolean needUpgrade = false;
//	        try {
//	          needUpgrade = upgradeManager.startUpgrade();
//	        } catch(IOException e) {
//	          FSNamesystem.LOG.error("IOException in startDistributedUpgradeIfNeeded", e);
//	        }
//	        if(needUpgrade) {
//	          // switch to manual safe mode
//	          safeMode = new SafeModeInfo(false);
//	          return;
//	        }
//	      }
//	      // if not done yet, initialize replication queues
//	      if (!isPopulatingReplQueues()) {
//	        initializeReplQueues();
//	      }
//	      long timeInSafemode = now() - systemStart;
//	      NameNode.stateChangeLog.info("STATE* Leaving safe mode after " 
//	                                    + timeInSafemode/1000 + " secs.");
//	      NameNode.getNameNodeMetrics().setSafeModeTime((int) timeInSafemode);
//	      
//	      if (reached >= 0) {
//	        NameNode.stateChangeLog.info("STATE* Safe mode is OFF."); 
//	      }
//	      reached = -1;
//	      safeMode = null;
//	      final NetworkTopology nt = blockManager.getDatanodeManager().getNetworkTopology();
//	      NameNode.stateChangeLog.info("STATE* Network topology has "
//	          + nt.getNumOfRacks() + " racks and "
//	          + nt.getNumOfLeaves() + " datanodes");
//	      NameNode.stateChangeLog.info("STATE* UnderReplicatedBlocks has "
//	          + blockManager.numOfUnderReplicatedBlocks() + " blocks");
//	    }
//
//	    /**
//	     * Initialize replication queues.
//	     */
//	    private synchronized void initializeReplQueues() {
//	      LOG.info("initializing replication queues");
//	      if (isPopulatingReplQueues()) {
//	        LOG.warn("Replication queues already initialized.");
//	      }
//	      long startTimeMisReplicatedScan = now();
//	      blockManager.processMisReplicatedBlocks();
//	      initializedReplQueues = true;
//	      NameNode.stateChangeLog.info("STATE* Replication Queue initialization "
//	          + "scan for invalid, over- and under-replicated blocks "
//	          + "completed in " + (now() - startTimeMisReplicatedScan)
//	          + " msec");
//	    }
//
//	    /**
//	     * Check whether we have reached the threshold for 
//	     * initializing replication queues.
//	     */
//	    private synchronized boolean canInitializeReplQueues() {
//	      return blockSafe >= blockReplQueueThreshold;
//	    }
//	      
//	    /** 
//	     * Safe mode can be turned off iff 
//	     * the threshold is reached and 
//	     * the extension time have passed.
//	     * @return true if can leave or false otherwise.
//	     */
//	    private synchronized boolean canLeave() {
//	      if (reached == 0)
//	        return false;
//	      if (now() - reached < extension) {
//	        reportStatus("STATE* Safe mode ON.", false);
//	        return false;
//	      }
//	      return !needEnter();
//	    }
//	      
//	    /** 
//	     * There is no need to enter safe mode 
//	     * if DFS is empty or {@link #threshold} == 0
//	     */
//	    private boolean needEnter() {
//	      return (threshold != 0 && blockSafe < blockThreshold) ||
//	        (getNumLiveDataNodes() < datanodeThreshold) ||
//	        (!nameNodeHasResourcesAvailable());
//	    }
//	      
//	    /**
//	     * Check and trigger safe mode if needed. 
//	     */
//	    private void checkMode() {
//	      if (needEnter()) {
//	        enter();
//	        // check if we are ready to initialize replication queues
//	        if (canInitializeReplQueues() && !isPopulatingReplQueues()) {
//	          initializeReplQueues();
//	        }
//	        reportStatus("STATE* Safe mode ON.", false);
//	        return;
//	      }
//	      // the threshold is reached
//	      if (!isOn() ||                           // safe mode is off
//	          extension <= 0 || threshold <= 0) {  // don't need to wait
//	        this.leave(true); // leave safe mode
//	        return;
//	      }
//	      if (reached > 0) {  // threshold has already been reached before
//	        reportStatus("STATE* Safe mode ON.", false);
//	        return;
//	      }
//	      // start monitor
//	      reached = now();
//	      smmthread = new Daemon(new SafeModeMonitor());
//	      smmthread.start();
//	      reportStatus("STATE* Safe mode extension entered.", true);
//
//	      // check if we are ready to initialize replication queues
//	      if (canInitializeReplQueues() && !isPopulatingReplQueues()) {
//	        initializeReplQueues();
//	      }
//	    }
//	      
//	    /**
//	     * Set total number of blocks.
//	     */
//	    private synchronized void setBlockTotal(int total) {
//	      this.blockTotal = total;
//	      this.blockThreshold = (int) (blockTotal * threshold);
//	      this.blockReplQueueThreshold = 
//	        (int) (((double) blockTotal) * replQueueThreshold);
//	      checkMode();
//	    }
//	      
//	    /**
//	     * Increment number of safe blocks if current block has 
//	     * reached minimal replication.
//	     * @param replication current replication 
//	     */
//	    private synchronized void incrementSafeBlockCount(short replication) {
//	      if ((int)replication == safeReplication)
//	        this.blockSafe++;
//	      checkMode();
//	    }
//	      
//	    /**
//	     * Decrement number of safe blocks if current block has 
//	     * fallen below minimal replication.
//	     * @param replication current replication 
//	     */
//	    private synchronized void decrementSafeBlockCount(short replication) {
//	      if (replication == safeReplication-1)
//	        this.blockSafe--;
//	      checkMode();
//	    }
//
//	    /**
//	     * Check if safe mode was entered manually or automatically (at startup, or
//	     * when disk space is low).
//	     */
//	    private boolean isManual() {
//	      return extension == Integer.MAX_VALUE && !resourcesLow;
//	    }
//
//	    /**
//	     * Set manual safe mode.
//	     */
//	    private synchronized void setManual() {
//	      extension = Integer.MAX_VALUE;
//	    }
//
//	    /**
//	     * Check if safe mode was entered due to resources being low.
//	     */
//	    private boolean areResourcesLow() {
//	      return resourcesLow;
//	    }
//
//	    /**
//	     * Set that resources are low for this instance of safe mode.
//	     */
//	    private void setResourcesLow() {
//	      resourcesLow = true;
//	    }
//
//	    /**
//	     * A tip on how safe mode is to be turned off: manually or automatically.
//	     */
//	    String getTurnOffTip() {
//	      if(reached < 0)
//	        return "Safe mode is OFF.";
//	      String leaveMsg = "";
//	      if (areResourcesLow()) {
//	        leaveMsg = "Resources are low on NN. Safe mode must be turned off manually";
//	      } else {
//	        leaveMsg = "Safe mode will be turned off automatically";
//	      }
//	      if(isManual()) {
//	        if(upgradeManager.getUpgradeState())
//	          return leaveMsg + " upon completion of " + 
//	            "the distributed upgrade: upgrade progress = " + 
//	            upgradeManager.getUpgradeStatus() + "%";
//	        leaveMsg = "Use \"hdfs dfsadmin -safemode leave\" to turn safe mode off";
//	      }
//
//	      if(blockTotal < 0)
//	        return leaveMsg + ".";
//
//	      int numLive = getNumLiveDataNodes();
//	      String msg = "";
//	      if (reached == 0) {
//	        if (blockSafe < blockThreshold) {
//	          msg += String.format(
//	            "The reported blocks %d needs additional %d"
//	            + " blocks to reach the threshold %.4f of total blocks %d.",
//	            blockSafe, (blockThreshold - blockSafe) + 1, threshold, blockTotal);
//	        }
//	        if (numLive < datanodeThreshold) {
//	          if (!"".equals(msg)) {
//	            msg += "\n";
//	          }
//	          msg += String.format(
//	            "The number of live datanodes %d needs an additional %d live "
//	            + "datanodes to reach the minimum number %d.",
//	            numLive, (datanodeThreshold - numLive) + 1 , datanodeThreshold);
//	        }
//	        msg += " " + leaveMsg;
//	      } else {
//	        msg = String.format("The reported blocks %d has reached the threshold"
//	            + " %.4f of total blocks %d.", blockSafe, threshold, 
//	            blockTotal);
//
//	        if (datanodeThreshold > 0) {
//	          msg += String.format(" The number of live datanodes %d has reached "
//	                               + "the minimum number %d.",
//	                               numLive, datanodeThreshold);
//	        }
//	        msg += " " + leaveMsg;
//	      }
//	      if(reached == 0 || isManual()) {  // threshold is not reached or manual       
//	        return msg + ".";
//	      }
//	      // extension period is in progress
//	      return msg + " in " + Math.abs(reached + extension - now()) / 1000
//	          + " seconds.";
//	    }
//
//	    /**
//	     * Print status every 20 seconds.
//	     */
//	    private void reportStatus(String msg, boolean rightNow) {
//	      long curTime = now();
//	      if(!rightNow && (curTime - lastStatusReport < 20 * 1000))
//	        return;
//	      NameNode.stateChangeLog.info(msg + " \n" + getTurnOffTip());
//	      lastStatusReport = curTime;
//	    }
//
//	    @Override
//	    public String toString() {
//	      String resText = "Current safe blocks = " 
//	        + blockSafe 
//	        + ". Target blocks = " + blockThreshold + " for threshold = %" + threshold
//	        + ". Minimal replication = " + safeReplication + ".";
//	      if (reached > 0) 
//	        resText += " Threshold was reached " + new Date(reached) + ".";
//	      return resText;
//	    }
//	      
//	    /**
//	     * Checks consistency of the class state.
//	     * This is costly and currently called only in assert.
//	     */
//	    private boolean isConsistent() throws IOException {
//	      if (blockTotal == -1 && blockSafe == -1) {
//	        return true; // manual safe mode
//	      }
//	      int activeBlocks = blockManager.getActiveBlockCount();
//	      return (blockTotal == activeBlocks) ||
//	        (blockSafe >= 0 && blockSafe <= blockTotal);
//	    }
//	  }

}
