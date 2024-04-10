# Counterfactual Reasoning with Probabilistic Graphical Models for Analyzing Socioecological Systems

This bundle contains the manuscript submitted to the _Environmetrics_ and entitled  "Counterfactual Reasoning with Probabilistic Graphical Models for Analyzing Socioecological Systems".
The organisation  is the following:

- _code_: Java files for replicating the case of study.
- _data_: datasets and models used in the case of study. This includes the original data and the processed data and model.
- _lib_: packages needed for running the code.
- _output_: output of learning (set of precise SCMs) and inference (csv files with the query results).


## Setup

The code has been tested with **Java openjdk 12.0.1**. For checking The Java version running in your system use the
following command:

```bash
$ java --version
```

```
openjdk 12.0.1 2019-04-16
OpenJDK Runtime Environment (build 12.0.1+12)
OpenJDK 64-Bit Server VM (build 12.0.1+12, mixed mode, sharing)
```


## Running the code

Here we illustrate how to do all the tasks for counterfactual reasoning. First `code/inference.java` allows to preprocess
the original dataset and to build the partially-defined SCM used in the paper. This basically creates files `final_model.uai`
and `final_data.csv` at data folder. For running it:

```bash
java -cp ./lib/credici_socioeco.jar ./code/preprocess.java
```

Afterwards, a java terminal script for learning is provided at `code/LearnSingleRun.java`. Note that this allows to run
a single EM run and it can be used for any model.  For details about its usage, run:

```bash
java -cp ./lib/credici_socioeco.jar ./code/LearnSingleRun.java --help
```

```bash
Usage: <main class> [-hq] [--debug] [-d=<dataPath>] [-l=<logfile>]
                    [-m=<maxIter>] [-o=<output>] [-s=<seed>] <modelPath>
      <modelPath>           Model path in UAI format.
      --debug               Debug flag. Defaults to false
  -d, --data=<dataPath>     Data path in CSV format.
  -h, --help                Display a help message
  -l, --logfile=<logfile>   Output file for the logs.
  -m, --maxiter=<maxIter>   Maximum EM internal iterations. Default to 500
  -o, --output=<output>     Output folder for the results. Default working dir.
  -q, --quiet               Controls if log messages are printed to standard output.
  -s, --seed=<seed>         Random seed. If not specified, it is randomly selected.

```

For instance, with the data and model from the case of study run:

```bash
java -cp ./lib/credici_socioeco.jar ./code/LearnSingleRun.java --data ./data/final_data.csv --maxiter 300 --seed 1234 ./data/final_model.uai
```

```bash
[2023-11-26T12:28:34.551755][INFO][seed1234] Set up logging
[2023-11-26T12:28:34.554098][INFO][seed1234] args: --data;./data/final_data.csv;--maxiter;300;--seed;1234;./data/final_model.uai
[2023-11-26T12:28:34.554560][INFO][seed1234] Starting logger with seed 1234
[2023-11-26T12:28:34.595258][INFO][seed1234] Loaded data from: ././data/final_data.csv
[2023-11-26T12:28:34.621941][INFO][seed1234] Loaded model from: ././data/final_model.uai
 [...]
```


Finally, the last task involves running the desired queries. For those in the case of study, run:

```bash
java -cp ./lib/credici_socioeco.jar ./code/inference.java
```
