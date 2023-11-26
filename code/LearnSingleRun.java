package code.experiments;

import ch.idsia.credici.IO;
import ch.idsia.credici.inference.CausalEMVE;
import ch.idsia.credici.model.StructuralCausalModel;
import ch.idsia.credici.utility.DataUtil;
import ch.idsia.credici.utility.experiments.Terminal;
import ch.idsia.credici.utility.experiments.Watch;
import ch.idsia.crema.utility.RandomUtil;
import com.opencsv.exceptions.CsvException;
import gnu.trove.map.TIntIntMap;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;


// Usage: java -cp ./lib/credici_socioeco.jar ./code/LearnSingleRun.java --data ./data/final_data.csv --maxiter 300 --seed 1234 ./data/final_model.uai


public class LearnSingleRun extends Terminal {

	@CommandLine.Parameters(description = "Model path in UAI format.")
	private String modelPath;

	@CommandLine.Option(names = {"-d", "--data"}, description = "Data path in CSV format.")
	private String dataPath;

	@CommandLine.Option(names = {"-m", "--maxiter"}, description = "Maximum EM internal iterations. Default to 500")
	private int maxIter = 500;

	@CommandLine.Option(names={"-o", "--output"}, description = "Output folder for the results. Default working dir.")
	String output = ".";

	public static void main(String[] args) {
		argStr = String.join(";", args);
		CommandLine.run(new LearnSingleRun(), args);
		if(errMsg!="")
			System.exit(-1);
	}

	@Override
	protected void entryPoint() throws IOException, CsvException, InterruptedException {

		Path wdir = Paths.get(".");
		RandomUtil.setRandomSeed(seed);
		logger.info("Starting logger with seed "+seed);

		String fullpath = wdir.resolve(dataPath).toString();
		TIntIntMap[] data = DataUtil.fromCSV(fullpath.toString());
		logger.info("Loaded data from: "+fullpath);

		// Load model
		fullpath = wdir.resolve(modelPath).toString();
		StructuralCausalModel model = (StructuralCausalModel)IO.readUAI(fullpath);
		logger.info("Loaded model from: "+fullpath);


		String modelName = Path.of(modelPath).getFileName().toString().replace(".uai","");
		String outputModel = modelName+"_mIter"+maxIter+"_"+seed+".uai";
		String outputStats =  modelName+"_mIter"+maxIter+"_"+seed+".csv";

		//double maxllk = Probability.maxLogLikelihood(model,data);
		//logger.info("max-llk:"+maxllk);

		Watch.start();
		CausalEMVE inf = new CausalEMVE(model,data, 1, maxIter);
		StructuralCausalModel m = inf.getInputModels().get(0);
		long time = Watch.stop();
		logger.info("Inference finished in :"+time);


		// store model and statistics
		fullpath = wdir.resolve(output).resolve(outputModel).toString();
		logger.info("Saving precise model at "+fullpath);
		IO.writeUAI(m, fullpath);


		fullpath = wdir.resolve(output).resolve(outputStats).toString();
		logger.info("Saving statistics at at "+fullpath);


		HashMap<String, String> stats = new HashMap<>();
		stats.put("seed", String.valueOf(seed));
		stats.put("time", String.valueOf(time));
		stats.put("iter", String.valueOf(maxIter));



		List<HashMap> res = new ArrayList<>();
		res.add(stats);
		DataUtil.toCSV(fullpath, res);

	}

}
