package hdfs4882;

public class testcode {
	
	public static void main(String[] args) throws InterruptedException{
		testcode inst = new testcode();
		inst.checkLeaseManager();
	}
	
	public void checkLeaseManager() throws InterruptedException{
		FSNamesystem fsn = new FSNamesystem();
		LeaseManager lm = new LeaseManager(fsn);
//		LeaseManagerPatch lm = new LeaseManagerPatch(fsn);
		lm.setLeasePeriod(0,0);

	    //Add some leases to the LeaseManager
	    lm.addLease("holder1", "src1");
	    lm.addLease("holder2", "src2");
	    lm.addLease("holder3", "src3");
	    
	    Thread.sleep(1000);//this is bc all the functions in LeaseManager are synchronized. We force addLease finish before checkLeases
	    
		lm.checkLeases();
	}
}
