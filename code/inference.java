import ch.idsia.credici.inference.CausalMultiVE;
import ch.idsia.credici.model.StructuralCausalModel;
import ch.idsia.credici.utility.DAGUtil;
import ch.idsia.credici.utility.DataUtil;
import ch.idsia.credici.utility.experiments.ResultsManager;
import ch.idsia.credici.utility.experiments.Watch;
import ch.idsia.crema.factor.GenericFactor;
import ch.idsia.crema.factor.convert.VertexToInterval;
import ch.idsia.crema.factor.credal.linear.IntervalFactor;
import ch.idsia.crema.factor.credal.vertex.VertexFactor;
import ch.idsia.crema.model.ObservationBuilder;
import ch.idsia.crema.model.graphical.specialized.BayesianNetwork;
import ch.idsia.crema.utility.ArraysUtil;
import com.opencsv.exceptions.CsvException;
import gnu.trove.map.TIntIntMap;
import jdk.jshell.spi.ExecutionControl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class inference {


    public static CausalMultiVE inf = null;
    public static TIntIntMap[] data = null;
    public static StructuralCausalModel model = null;
    public static BayesianNetwork bnet = null;

    public static void main(String[] args) throws InterruptedException, ExecutionControl.NotImplementedException, IOException, CsvException {

        // Usage:  java -cp ./lib/credici_socioeco.jar ./code/inference.java

        String label = "final";

        // Define the number of models to use
        int numRuns = 100;

        // Effect variables to be considered
        String[] Yvars = {
                "alt_LandUse_1_Env097_HETEROGENEO",
                "alt_LandUse_1_Env098_Natural",
                "alt_LandUse_1_Env094_INTENSIVE",
                "alt_LandUse_1_Env096_WoodCrops",
                "alt_LandUse_1_Env095_HERBCROPS",
                "alt_LandUse_1_Env093_BUILT",
                "social_1_S007_EGR"
        };

        // Define the queries to compute
        String[] queries = {
                "condDiff",
                "ACE",
                "PNS",
                "PN",
                "PS",
                "PNrc",
        };

        Path wdir = Path.of(".");
        Path folder = wdir.resolve(Path.of("output/learning/"));
        System.out.println(folder);

        inf = CausalMultiVE.fromFolder(folder).setLastModelIndex(numRuns - 1);
        model = inf.getInputModels().get(0);
        bnet = model.getEmpiricalNet();

        Path namesfile = (Path) Files.walk(folder).filter(f -> f.toString().endsWith("_varnames.csv")).toArray()[0];
        HashMap varnames = DataUtil.fromCSVtoStrMap(namesfile.toString()).get(0);

        Path datafile = (Path) Files.walk(folder).filter(f -> f.toString().endsWith("_data.csv")).toArray()[0];
        data = DataUtil.fromCSV(datafile.toString());

        List vars = IntStream.range(0, varnames.size()).mapToObj(i -> varnames.get("" + i)).collect(Collectors.toList());


        for (String yname : Yvars) {
            for (String m : queries) {
                System.out.println("\n=====" + m + ": " + yname + "=====");
                int y = vars.indexOf(yname);
                int[] endoAncestors = ArraysUtil.intersection(DAGUtil.ancestors(model.getNetwork(), y), model.getEndogenousVars());

                ResultsManager res = new ResultsManager();
                int idx = 0;

                String filename = datafile.getFileName().toString().replace("_data.csv", yname.substring(yname.lastIndexOf("_")) + "_m" + m + "_x" + numRuns + ".csv");

                Path outputpath = wdir.resolve(Path.of("output/inference/" + filename));
                if (!Files.exists(outputpath)) {
                    for (int x : endoAncestors) {
                        Watch.start();

                        String xname = (String) vars.get(x);
                        double lb = 0;
                        double ub = 0;

                        double bounds[] = query(m, x, y);
                        lb = bounds[0];
                        ub = bounds[1];
                        System.out.println(xname + "->" + yname + " : [" + lb + "," + ub + "]");

                        res.addExperiment(String.valueOf(idx));
                        res.add(String.valueOf(idx), "metric", m);
                        res.add(String.valueOf(idx), "cause", xname);
                        res.add(String.valueOf(idx), "effect", yname);
                        res.add(String.valueOf(idx), "lb", lb);
                        res.add(String.valueOf(idx), "ub", ub);

                        idx++;

                    }


                    System.out.println("Saving results to " + filename);

                    res.save(outputpath.toString());
                    System.gc();
                } else {
                    System.out.println("Output file " + filename + " already exists. Not overwritting.");
                }


            }


        }


    }

    public static double[] query(String type, int x, int y) throws ExecutionControl.NotImplementedException, InterruptedException {
        IntervalFactor res = null;

        double b1 = Double.NaN;
        double b2 = Double.NaN;

        if (!type.startsWith("cond")) {
            if (type.equals("PNS"))
                res = convert(inf.probNecessityAndSufficiency(x, y, 0, 1));
            else if (type.equals("PS"))
                res = convert(inf.probSufficiency(x, y, 0, 1));
            else if (type.equals("PN"))
                res = convert(inf.probNecessity(x, y, 0, 1));
            else if (type.equals("PE"))
                res = convert(inf.probEnablement(x, y, 0, 1));
            else if (type.equals("PD"))
                res = convert(inf.probDisablement(x, y, 0, 1));

            else if (type.equals("ACE"))
                res = convert(inf.averageCausalEffects(x, y, 0, 0, 1));

            else if (type.equals("PNSre"))
                res = convert(inf.probNecessityAndSufficiency(x, y, 0, 1, 1, 0));
            else if (type.equals("PNre"))
                res = convert(inf.probNecessity(x, y, 0, 1, 1, 0));
            else if (type.equals("PEre"))
                res = convert(inf.probEnablement(x, y, 0, 1, 1, 0));
            else if (type.equals("PDre"))
                res = convert(inf.probDisablement(x, y, 0, 1, 1, 0));

            else if (type.equals("PNSrc"))
                res = convert(inf.probNecessityAndSufficiency(x, y, 1, 0, 0, 1));
            else if (type.equals("PNrc"))
                res = convert(inf.probNecessity(x, y, 1, 0, 0, 1));
            else if (type.equals("PErc"))
                res = convert(inf.probEnablement(x, y, 1, 0, 0, 1));
            else if (type.equals("PDrc"))
                res = convert(inf.probDisablement(x, y, 1, 0, 0, 1));
            else if (type.equals("PSrc"))
                res = convert(inf.probSufficiency(x, y, 1, 0, 0, 1));

            b1 = res.getDataLower()[0][0];
            b2 = res.getDataUpper()[0][0];
        } else {
            double pcond = Double.NaN;
            if (type.equals("condTrue")) {
                pcond = condQuery(y, 0, x, 0);

            } else if (type.equals("condFalse")) {
                pcond = condQuery(y, 0, x, 1);

            } else if (type.equals("condDiff")) {
                pcond = condQuery(y, 0, x, 0) - condQuery(y, 0, x, 1);
            }

            b1 = pcond;
            b2 = pcond;

        }


        return new double[]{Math.min(b1, b2), Math.max(b1, b2)};
    }


    public static double condQuery(int y, int yval, int x, int xval) throws InterruptedException {
        return convert(inf.query(y, ObservationBuilder.observe(x, xval))).getDataLower()[0][yval];
    }

    public static IntervalFactor convert(GenericFactor f) {
        if (f instanceof VertexFactor)
            return new VertexToInterval().apply((VertexFactor) f);
        return (IntervalFactor) f;
    }
}
