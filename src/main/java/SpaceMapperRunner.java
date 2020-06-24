import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class SpaceMapperRunner {

    public static void main(String[] args) throws Exception {

        String [] files = {"src/main/resources/lc_pn_faults.js"};
        List<Path> inputPaths = new ArrayList<>(args.length);
        for ( String arg : files ) {
            Path fn = Paths.get(arg);
            if ( Files.exists(fn) ) {
                inputPaths.add(fn);
            } else {
                System.err.printf("Input file '%s' does not exist (absolute path: '%s')\n", arg, fn.toAbsolutePath().toString());
                System.exit(2);
            }
        }
        StateSpaceMapper mpr = new StateSpaceMapper();
        inputPaths.stream().forEach( mpr::addFile );

        mpr.mapSpace("lc_pn_faults.fsm");
        System.out.println("// done");
    }

}
