package typeMissingTimeout;


/* Usage: java MainDriver [soot-options] appClass
 */

import utils.LoopTagTransformer;
import utils.MethodTagTransformer;
/* import necessary soot packages */
import soot.*;

public class MainDriver {
  public static void main(String[] args) {

    /* check the arguments */
    if (args.length == 0) {
      System.err.println("Usage: java MainDriver [options] classname");
      System.exit(0);
    }

    MethodTagTransformer methodTags = new MethodTagTransformer();
	methodTags.setMethodPrint(true);
	PackManager.v().getPack("jtp").add(new Transform("jtp.methdTag",methodTags)); //add this transformer during the jap phase
	
	LoopTagTransformer loopTags = new LoopTagTransformer(); //print all statements of a function contains loop		 
	loopTags.setLoopPrint(true);
	PackManager.v().getPack("jtp").add(new Transform("jtp.loopTag",loopTags));
	
//	LoopPathFinder pathFinder = new LoopPathFinder();
////	pathFinder.setOtherBodies(otherClassMethodBodys);
//	PackManager.v().getPack("jtp").add(new Transform("jtp.pathFinder", pathFinder));
	
	/* add a phase to transformer pack by call Pack.add */
    Pack jtp = PackManager.v().getPack("jtp");
    jtp.add(new Transform("jtp.instrumenter", new InvokeStaticInstrumenter()));

    /* Give control to Soot to process all options,
     * InvokeStaticInstrumenter.internalTransform will get called.
     */
    soot.Main.main(args);
  }
}