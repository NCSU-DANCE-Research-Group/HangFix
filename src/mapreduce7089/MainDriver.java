package mapreduce7089;


/* Usage: java MainDriver [soot-options] appClass
 */
import java.util.ArrayList;
import java.util.List;

import utils.LoopPathFinder;
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
    
    List<String> classNames = new ArrayList<String>();
    for(int i = 0; i < args.length; i++) {       // parse command line
    	classNames.add(args[i]);
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
    
//    Scene.v().addBasicClass("org.apache.commons.logging.Log",SootClass.SIGNATURES);
//    Scene.v().addBasicClass("java.util.ArrayList",SootClass.SIGNATURES);
//    Scene.v().addBasicClass("java.util.concurrent.CopyOnWriteArrayList",SootClass.SIGNATURES);
//    Scene.v().addBasicClass("java.util.HashMap",SootClass.SIGNATURES);
//    Scene.v().addBasicClass("hadoop2Conf.Configuration$DeprecationDelta",SootClass.SIGNATURES);
//    Scene.v().addBasicClass("java.util.concurrent.atomic.AtomicReference",SootClass.SIGNATURES);
//    Scene.v().addBasicClass("java.util.concurrent.atomic.AtomicReference",SootClass.SIGNATURES);
//    Scene.v().addBasicClass("java.util.regex.Pattern",SootClass.SIGNATURES);


    /* Give control to Soot to process all options,
     * InvokeStaticInstrumenter.internalTransform will get called.
     */
    soot.Main.main(args);
  }
}