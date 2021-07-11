package edu.umd.cs.satune;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

public class Utils {
    public static final String HOME_DIR = System.getProperty("user.home");
    public static final String PROJECT_DIR = HOME_DIR + "/sv";
    public static final DecimalFormat four = new DecimalFormat("#0.0000");
    private static final boolean debug = true;
    public static String logFile;
    public static String BENCH_DIR, TOOL;
    public static String featureFile, cmdPattern, debugStr, saveFile;
    public static Map<String, String[]> featureMap = new HashMap<>();
    public static HashSet<String> forbiddenOpts;

    public static int runVerifier(LinkedHashMap<String, String[]> configMap, String task, String category,
                                  String[] config) {
        String confStr = prepConfigStr(configMap, config);
        String cmd = TOOL + " "
            + cmdPattern.replace("CATEGORY", category).replace("TARGETFILE", task).replace("CONFIG", confStr);
        log(cmd);
        ProcessBuilder pb = new ProcessBuilder("bash", "-c", cmd);
        Map<String, String> env = pb.environment();
        env.put("PATH", "/usr/bin:/usr/local/bin:/opt/local/bin:" + env.get("PATH"));

        try {
            StringBuilder sb = new StringBuilder();
            Process p = pb.start();
            BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
            stdout.lines().forEach(sb::append);
            log(sb.toString());
            int exitCode = p.waitFor();
            p.getInputStream().close();
            p.getOutputStream().close();
            p.getErrorStream().close();
            return getVerifResult(exitCode, sb.toString());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return -99;
    }

    public static String prepConfigStr(LinkedHashMap<String, String[]> configMap, String[] config) {
        StringBuilder dp = new StringBuilder();
        int i = -1;
        if (TOOL.endsWith("bmc")) {
            for (Entry<String, String[]> entry : configMap.entrySet()) {
                i++;
                if (config[i].equals("0") || config[i].equals("off")) continue;
                String opt = entry.getKey();
                if (opt.startsWith("--")) {
                    boolean binaryOption = (config[i].equals("1") || config[i].equals("on")) && entry.getValue().length == 2;
                    dp.append(binaryOption ? opt + " " : opt + " " + config[i] + " ");
                } else dp.append(config[i]).append(" ");
            }
        } else if (TOOL.endsWith("jayhorn")) {
            for (Entry<String, String[]> entry : configMap.entrySet()) {
                i++;
                if (config[i].equals("off")) continue;
                String opt = entry.getKey();
                dp.append(config[i].equals("on") ? opt + " " : opt + " " + config[i] + " ");
            }
        } else if (TOOL.endsWith("symbiotic")) {
            StringBuilder optimize = new StringBuilder("--optimize=");
            for (Entry<String, String[]> entry : configMap.entrySet()) {
                i++;
                if (config[i].equals("off")) continue;
                String opt = entry.getKey();
                if (config[i].equals("on")) {
                    dp.append(opt).append(" ");
                } else if (opt.startsWith("--")) {
                    dp.append(opt).append(" ").append(config[i]).append(" ");
                } else { //optimize before after
                    optimize.append(config[i]).append(",");
                }
            }
            if (optimize.length() > 11) { // something to append
                dp.append(optimize.deleteCharAt(optimize.length() - 1));
            } else {
                dp.append("--no-optimize");
            }
        } else {
            throw new IllegalArgumentException("Unknown tool:" + TOOL);
        }
        return dp.toString();
    }

    public static String[] getFeatures(String task) throws Exception {
        if (featureMap.isEmpty()) {
            Files.lines(Paths.get(featureFile)).forEach(line -> {
                String[] split = line.split(",", 2);
                featureMap.put(split[0], split[1].split(","));
            });
        }
        if (!featureMap.containsKey(task)) throw new Exception("No feature vector for " + task);
        return featureMap.get(task);
    }

    public static String getLabel(int result, boolean verdict) {
        if (result != 0 && result != 10)
            return "UNK";
        else if (verdict)
            return result == 0 ? "TN" : "FP";
        else return result == 0 ? "FN" : "TP";
    }

    private static int getVerifResult(int exitCode, String log) {
        if (TOOL.endsWith("cbmc")) {
            return exitCode;
        } else if (TOOL.endsWith("jbmc")) {
            return log.contains("UNKNOWN") ? -9 : exitCode;
        } else if (TOOL.endsWith("jayhorn")) {
            return log.contains("UNKNOWN") ? -9 : (log.contains("UNSAFE") ? 10 : 0);
        } else if (TOOL.endsWith("symbiotic")) {
            return log.contains("RESULT: true") ? 0 : (log.contains("RESULT: false") ? 10 : -9);
        } else {
            throw new IllegalArgumentException("Unknown tool:" + TOOL);
        }
    }

    public static void log(String log) {
        if (debug) try {
            writeFile(logFile, log + "\n", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeFile(String path, String str, boolean append) throws IOException {
        OutputStreamWriter stadFile1 = new OutputStreamWriter(new FileOutputStream(path, append), StandardCharsets.UTF_8);
        stadFile1.write(str);
        stadFile1.flush();
        stadFile1.close();
    }

    public static LinkedHashMap<String, String[]> readConfigFile(String configSpace) {
        LinkedHashMap<String, String[]> configMap = new LinkedHashMap<>();
        try (Stream<String> stream = Files.lines(Paths.get(configSpace))) {
            stream.forEach(line -> {
                String[] pair = line.split(":");
                configMap.put(pair[0], pair[1].split(","));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return configMap;
    }

    public static String[] readDefaultConfig(String defConfigFile) throws IOException {
        List<String> lines = Files.readAllLines(new File(defConfigFile).toPath(), Charset.defaultCharset());
        String[] configDef = new String[lines.size()];
        for (int i = 0; i < lines.size(); i++)
            configDef[i] = lines.get(i).split(" ")[1];
        return configDef;
    }

    public static void reportSearchEnd(String task, String[] config, int result, double time, double score, double cost,
                                       int configCount, int revisitCount, int acceptCount, int saRunCount, int bestCount,
                                       boolean verdict) {
        Utils.log("Final result:" + getLabel(result, verdict) + "," + task + "," + result + "," + four.format(cost) + ","
            + four.format(score) + "," + four.format(time) + "," + configCount + "," + revisitCount + "," + acceptCount
            + "," + bestCount + "," + saRunCount + "," + Utils.debugStr);
        Utils.log("With config:" + Utils.arr2String(config));
    }

    public static void reportSearchStart(String task, String[] features, double score, double cost, double thresh) {
        Utils.log("Starting search for:" + task);
        Utils.log("Threshold:" + thresh + ":" + ": Initial cost:" + four.format(cost) + ": Initial score:"
            + four.format(score));
        Utils.log("Features: " + Utils.arr2String(features));
    }

    public static void reportVerificationResult(String task, String category, String[] config, int result,
                                                String[] features, double score, double cost, boolean verdict) throws IOException {
        String label = getLabel(result, verdict);
        String labelReg = "50";
        switch (label) {
            case "TN":
                labelReg = "0";
                break;
            case "FP":
                labelReg = "99";
                break;
            case "TP":
                labelReg = "1";
                break;
            case "FN":
                labelReg = "100";
                break;
        }

        String instance = Utils.arr2String(config) + "," + Utils.arr2String(features) + "," + labelReg + "," + category
            + "," + task + "\n";
        Utils.writeFile(saveFile, instance, true);

        Utils.log("Verification result:" + task + "," + result + "," + four.format(cost) + "," + four.format(score));
        Utils.log("With config:" + Utils.arr2String(config));
    }

    public static String arr2String(Object[] arr) {
        StringBuilder sv = new StringBuilder();
        for (Object t : arr)
            sv.append(",").append(t);
        return sv.substring(1);
    }

    public static Options prepareExperimentOptions() {
        Options options = new Options();

        Option searchOpt = new Option(null, "search", true,
            "the search algorithm to use: anneal,astar (default: anneal)");
        searchOpt.setRequired(true);
        options.addOption(searchOpt);

        Option toolOpt = new Option(null, "tool", true, "the subject tool: cbmc,symbiotic,jayhorn,jbmc");
        toolOpt.setRequired(true);
        options.addOption(toolOpt);

        Option neighOpt = new Option(null, "anneal-neighbor-strat", true,
            "The neighbor generation strategy for simulated annealing: base, greedy, conservative (default:base)");
        options.addOption(neighOpt);

        Option modelOpt = new Option(null, "ml-model", true, "regression or classification (deprecated. Value will be ignored)");
        options.addOption(modelOpt);

        Option pcaOpt = new Option(null, "pca", false, "perform principal component analysis");
        options.addOption(pcaOpt);

        Option threshOpt = new Option(null, "threshold", true,
            "The threshold value for the incorrectness score. Should be in the range of [0,1] for classification, and in the range aof min and max numeric values used for regression mapping.");
        threshOpt.setRequired(true);
        threshOpt.setType(Double.class);
        options.addOption(threshOpt);

        Option improveOpt = new Option(null, "improve", false, "Run the improvement experiment");
        options.addOption(improveOpt);

        Option seedOpt = new Option(null, "seed", true,
            "numeric value to be used as the seed of random number generator.");
        seedOpt.setRequired(true);
        seedOpt.setType(Integer.class);
        options.addOption(seedOpt);

        Option helpOpt = new Option("h", "help", false, "Display usage info.");
        options.addOption(helpOpt);

        return options;
    }
}