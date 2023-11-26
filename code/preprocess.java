import ch.idsia.credici.IO;
import ch.idsia.credici.model.StructuralCausalModel;
import ch.idsia.credici.model.builder.CausalBuilder;
import ch.idsia.credici.utility.DataUtil;
import ch.idsia.crema.model.graphical.SparseDirectedAcyclicGraph;
import com.opencsv.exceptions.CsvException;
import es.ual.socioeco.data.Preprocess;
import es.ual.socioeco.util.DAGUtils;
import gnu.trove.map.hash.TIntIntHashMap;
import jdk.jshell.spi.ExecutionControl;

import javax.script.ScriptException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class preprocess {

    // Usage: java -cp ./lib/credici_socioeco.jar ./code/preprocess.java
    public static void main(String[] args) throws ExecutionControl.NotImplementedException, InterruptedException, ScriptException, IOException, CsvException, CsvException, CsvException {

        // These paths can be customized
        Path wdir = Path.of(".");
        Path datadir = wdir.resolve("./data");

        // Prefix for output files
        String label = "final";


        // Load the original data
        List dataOriginal = DataUtil.fromCSVtoStrMap(datadir.resolve("fulldataset.csv").toString());
        System.out.println("Loaded "+dataOriginal.size()+" data instances");


        // Define the graph edges
        String arcs ="";
        arcs += "(social_1_Ec001_ActR,social_1_S042_Pdens),(social_1_S007_EGR,social_1_S042_Pdens),(social_1_S013_IME,social_1_Ec001_ActR),(social_1_S015_ODI,social_1_Ec001_ActR),(Topography_Env104_MGU_litt,social_1_S003_MsR),(altEconomyClass_1_Ec109_OcuTradBankServHab,social_1_S003_MsR),(Topography_Env104_MGU_litt,social_1_S013_IME),(altEconomyClass_1_Ec109_OcuTradBankServHab,social_1_S013_IME),(altEconomyClass_1_Ec096_OcuSecunHab,social_1_S013_IME),(social_1_S042_Pdens,alt_LandUse_1_Env093_BUILT),(Topography_Env104_MGU_litt,alt_LandUse_1_Env093_BUILT),(social_1_S042_Pdens,alt_LandUse_1_Env095_HERBCROPS),(Topography_Env104_MGU_litt,alt_LandUse_1_Env095_HERBCROPS),(social_1_S042_Pdens,alt_LandUse_1_Env096_WoodCrops),(Topography_Env104_MGU_litt,alt_LandUse_1_Env096_WoodCrops),(social_1_S042_Pdens,alt_LandUse_1_Env098_Natural),(Topography_Env104_MGU_litt,alt_LandUse_1_Env098_Natural),(social_1_S042_Pdens,alt_LandUse_1_Env097_HETEROGENEO),(Topography_Env104_MGU_litt,alt_LandUse_1_Env097_HETEROGENEO),(social_1_S042_Pdens,alt_LandUse_1_Env094_INTENSIVE),(Topography_Env104_MGU_litt,alt_LandUse_1_Env094_INTENSIVE),(Topography_Env104_MGU_litt,altEconomyClass_1_Ec109_OcuTradBankServHab),(Topography_Env104_MGU_litt,altEconomyClass_1_Ec096_OcuSecunHab),(Topography_Env104_MGU_litt,social_1_S015_ODI),(altEconomyClass_1_Ec109_OcuTradBankServHab,social_1_S015_ODI),(altEconomyClass_1_Ec096_OcuSecunHab,social_1_S015_ODI),(social_1_S013_IME,social_1_S007_EGR),(social_1_S040_Tbnatalidad,social_1_S007_EGR),(social_1_S036_Tbmortalid,social_1_S007_EGR),(social_1_S003_MsR,social_1_S040_Tbnatalidad),(social_1_S015_ODI,social_1_S040_Tbnatalidad),(social_1_S015_ODI,social_1_S036_Tbmortalid)";
        arcs += ",(social_1_S015_ODI,social_1_S042_Pdens)";
        String[] vars = Arrays.stream(arcs.replace("(", "").replace(")", "").split(",")).distinct().toArray(String[]::new);

        // Define the states partitions
        Preprocess p = new Preprocess(dataOriginal)
                .setVars(vars)
                .setPositive("altEconomyClass_1_Ec113_Education", "High_school_or_higher")
                .setPositive("altEconomyClass_1_Ec096_OcuSecunHab","High")
                .setPositive("social_1_S015_ODI","Low")
                .setPositive("social_1_S013_IME","Immigration")
                .setPositive("altEconomyClass_1_Ec109_OcuTradBankServHab","High")
                .setPositive("social_1_S003_MsR","More_females")
                .setPositive("Topography_Env104_MGU_litt","Littoral", "Baetic_Depression")
                .setPositive("social_1_S042_Pdens","High")
                .setPositive("social_1_S46_HDI","High")
                .setPositive("altEconomyClass_1_Ec110_incomeIndex","High")
                .setPositive("econ_Education_1_S044_eduIndex","High")
                .setPositive("social_1_S040_Tbnatalidad","High")
                .setPositive("social_1_S036_Tbmortalid","Low")
                .setPositive("social_1_Ec001_ActR","High")
                .setPositive("social_1_S007_EGR","Increase")
                .setPositive("social_1_S008_DT","Fast_to_doubling_time", "Moderately_to_doubling_time", "Slowly_to_doubling_time")
                .setPositive("alt_LandUse_1_Env098_Natural", "Dominant", "Fair")
                .setPositive("alt_LandUse_1_Env097_HETEROGENEO", "Dominant", "Fair")
                .setPositive("alt_LandUse_1_Env094_INTENSIVE", "Abundant", "Fair")
                .setPositive("alt_LandUse_1_Env095_HERBCROPS", "Dominant", "Fair")
                .setPositive("alt_LandUse_1_Env096_WoodCrops", "Dominant", "Fair")
                .setPositive("alt_LandUse_1_Env093_BUILT", "Abundant", "Fair");




        // Build  and save the model
        int[] endoVarsSizes = p.getDom().values().stream().mapToInt(v -> v.size()).toArray();
        SparseDirectedAcyclicGraph dag = DAGUtils.buildFromNames(arcs, p.getVars());
        StructuralCausalModel model = CausalBuilder.of(dag, endoVarsSizes).build();

        Path targetfile = datadir.resolve(label+"_model.uai").toAbsolutePath();
        System.out.println("Saving: "+targetfile);
        IO.write(model, targetfile.toString());

        // Save the data
        TIntIntHashMap[] data = p.getData().toArray(TIntIntHashMap[]::new);
        targetfile = datadir.resolve(label+"_data.csv").toAbsolutePath();
        System.out.println("Saving: "+targetfile);
        DataUtil.toCSV(targetfile.toString(), data);

        // Save the association between variable names and IDs in the model
        targetfile = datadir.resolve(label+"_varnames.csv").toAbsolutePath();
        DataUtil.toCSV(targetfile.toString(),
                new String[][]{
                        IntStream.range(0, p.getVars().size()).mapToObj(i -> ""+i).toArray(String[]::new),
                        (String[]) p.getVars().toArray(String[]::new)
                });

        System.out.println("Preprocessing finished");

    }
}
