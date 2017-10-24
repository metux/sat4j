package org.sat4j.sat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.sat4j.pb.OptToPBSATAdapter;
import org.sat4j.pb.PBSolverHandle;
import org.sat4j.pb.PseudoOptDecorator;
import org.sat4j.pb.SolverFactory;
import org.sat4j.pb.constraints.pb.ConflictMapReduceByGCD;
import org.sat4j.pb.constraints.pb.ConflictMapReduceByPowersOf2;
import org.sat4j.pb.constraints.pb.ConflictMapReduceToCard;
import org.sat4j.pb.constraints.pb.ConflictMapReduceToClause;
import org.sat4j.pb.constraints.pb.IWeakeningStrategy;
import org.sat4j.pb.constraints.pb.PostProcessToCard;
import org.sat4j.pb.constraints.pb.PostProcessToClause;
import org.sat4j.pb.core.PBSolverCP;
import org.sat4j.pb.reader.OPBReader2012;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.DotSearchTracing;

/**
 * A specific launcher for experimenting various settings in the PB solvers with
 * Jakob Nordstrom's group @ KTH.
 * 
 * @author leberre
 * @since 2.3.6
 */
public class KTHLauncher {

    public static Options createCLIOptions() {
        Options options = new Options();
        options.addOption("cl", "coeflim", true,
                "coefficient limit triggering division");
        options.addOption("cls", "coeflim-small", true,
                "coefficient limit for divided constraints");
        options.addOption("fb", "find-best-divisor-when-dividing-for-overflow",
                true, "rounding coefficient. Legal values are true or false.");
        options.addOption("wr", "when-resolve", true,
                "behavior when performing conflict analysis. Legal values are skip or always.");
        options.addOption("rr", "round-reason", true,
                "Rounding strategy during conflict analysis. Legal values are divide-v1, divide-unless-equal, divide-unless-divisor, round-to-gcd, or never.");
        options.addOption("rwp", "rounding-weaken-priority", true,
                "Which literals are removed to ensure conflicting constraints. Legal values are any, satisfied, unassigned");
        options.addOption("tlc", "type-of-learned-constraint", true,
                "Type of constraints learned. Legal values are general-linear, cardinality, clause");
        options.addOption("wni", "weaken-nonimplied", true,
                "Remove literals that are not implied/propagated by the assignment at the backjump level. Legal values are true or false.");
        options.addOption("division", "division-strategy", true,
                "Detect if all the coefficients can be divided by a common number. Legal values are two, gcd or none.");
        options.addOption("dot", "dot-output", true,
                "Output the search as a dot file");
        Option op = options.getOption("coeflim");
        op.setArgName("limit");
        op = options.getOption("coeflim-small");
        op.setArgName("limit");
        op = options.getOption("when-resolve");
        op.setArgName("strategy");
        op = options.getOption("find-best-divisor-when-dividing-for-overflow");
        op.setArgName("boolean");
        op = options.getOption("round-reason");
        op.setArgName("strategy");
        op = options.getOption("rounding-weaken-priority");
        op.setArgName("strategy");
        op = options.getOption("type-of-learned-constraint");
        op.setArgName("type");
        op = options.getOption("weaken-nonimplied");
        op.setArgName("boolean");
        op = options.getOption("dot-output");
        op.setArgName("filename");
        return options;
    }

    public static void log(String str) {
        System.out.println("c " + str);
    }

    public static void main(String[] args) {
        log("SAT4J PB solver for KTH experiments");
        URL url = KTHLauncher.class.getResource("/sat4j.version");
        if (url != null) {
            BufferedReader in = null;
            try {
                in = new BufferedReader(
                        new InputStreamReader(url.openStream()));
                log("version " + in.readLine()); //$NON-NLS-1$
            } catch (IOException e) {
                log("c ERROR: " + e.getMessage());
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        log("c ERROR: " + e.getMessage());
                    }
                }
            }
        }
        CommandLineParser parser = new PosixParser();
        Options options = createCLIOptions();
        if (args.length == 0) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("KTHLauncher", options);
            return;
        }
        try {
            CommandLine line = parser.parse(options, args);
            PBSolverCP solver = SolverFactory.newCuttingPlanes();
            solver.setNoRemove(true);
            solver.setSkipAllow(false);
            if (line.hasOption("division-strategy")) {
                String value = line.getOptionValue("division-strategy");
                switch (value.toLowerCase()) {
                case "two":
                    solver.setConflictFactory(ConflictMapReduceByPowersOf2.factory());
                    break;
                case "gcd":
                    solver.setConflictFactory(ConflictMapReduceByGCD.factory());
                    break; 
                case "none":
                    break;
                default:
                    log(value
                            + " is not a supported value for option division");
                    return;
                }
            }

            if (line.hasOption("type-of-learned-constraint")) {
                String value = line
                        .getOptionValue("type-of-learned-constraint");
                switch (value) {
                case "general-linear": // default case, do nothing
                    break;
                case "cardinality":
                    solver.setPostprocess(PostProcessToCard.instance());
                    break;
                case "clause":
                    solver.setPostprocess(PostProcessToClause.instance());
                    break;
                default:
                    log(value
                            + " is not a supported value for option type-of-learned-constraint");
                    return;
                }
            }
            if (line.hasOption("when-resolve")) {
                String value = line.getOptionValue("when-resolve");
                switch (value) {
                case "always":
                    solver.setSkipAllow(false);
                    break;
                case "skip":
                    solver.setSkipAllow(true);
                    break;
                default:
                    log(value
                            + " is not a supported value for option when-resolve");
                    return;
                }
            }
            if (line.hasOption("round-reason")) {
                String value = line.getOptionValue("round-reason");
                switch (value) {
                case "never": 
                    // by default
                    break;
                case "clausal":
                    solver.setConflictFactory(ConflictMapReduceToClause.factory());
                    break;
                case "cardinality":
                    solver.setConflictFactory(ConflictMapReduceToCard.factory());
                    break;
                case "divide-v1":
                case "divide-unless-equal":
                case "divide-unless-divisor":
                case "round-to-gcd":
                default:
                    log(value
                            + " is not a supported value for option round-reason");
                    return;
                }
            }
            // validate that block-size has been set
            if (line.hasOption("coeflim")) {
                log("coeflim option is unsupported at the moment");
                return;
            }

            if (line.hasOption("coeflim-small")) {
                log("coeflim-small option is unsupported at the moment");
                return;
            }
            if (line.hasOption(
                    "find-best-divisor-when-dividing-for-overflow")) {
                log("find-best-divisor-when-dividing-for-overflow option is unsupported at the moment");
                return;
            }
            if (line.hasOption("rounding-weaken-priority")) {
                String value = line.getOptionValue("rounding-weaken-priority");
                switch (value) {
                case "unassigned": 
                    solver.setWeakeningStrategy(IWeakeningStrategy.UNASSIGNED_FIRST);
                    break;
                case "satisfied":
                    solver.setWeakeningStrategy(IWeakeningStrategy.SATISFIED_FIRST);
                    break;
                case "any":
                    solver.setWeakeningStrategy(IWeakeningStrategy.ANY);
                    break;
                default:
                    log(value
                            + " is not a supported value for option rounding-weaken-priority");
                    return;
                }
            }
            if (line.hasOption("weaken-nonimplied")) {
                String value = line.getOptionValue("weaken-nonimplied");
                switch (value) {
                case "false": 
                    // by default
                    break;
                case "true":
                case "round":
                default:
                    log(value
                            + " is not a supported value for option weaken-nonimplied");
                    return;
                }
            }
            System.out.println(solver.toString("c "));
            String[] leftArgs = line.getArgs();
            if (leftArgs.length == 0) {
                System.err.println("Missing filename");
                return;
            }
            String filename = leftArgs[leftArgs.length - 1];
            PBSolverHandle handle = new PBSolverHandle(
                    new PseudoOptDecorator(solver));
            OPBReader2012 reader = new OPBReader2012(handle);
            OptToPBSATAdapter optimizer = new OptToPBSATAdapter(handle);
            try {
                reader.parseInstance(filename);
                if (line.hasOption("dot-output")) {
                        String dotfilename = line.getOptionValue("dot-output");
                        if (dotfilename != null) {
                            DotSearchTracing<String> dotTracing = new DotSearchTracing<>(dotfilename);
                            solver.setSearchListener(dotTracing);
                            dotTracing.setMapping(reader.getMapping());
                        }
                 }
                if (optimizer.isSatisfiable()) {
                    if (optimizer.isOptimal()) {
                        System.out.println("s OPTIMUM FOUND");
                    } else {
                        System.out.println("s SATISFIABLE");
                    }
                    System.out.println(
                            "v " + reader.decode(optimizer.model()) + " 0");
                } else {
                    System.out.println("s UNSATISFIABLE");
                }
                optimizer.printStat(System.out, "c ");
            } catch (TimeoutException e) {
                System.out.println("s UNKNOWN");
            } catch (ParseFormatException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ContradictionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (ParseException exp) {
            System.out.println("Unexpected exception:" + exp.getMessage());
        }
    }
}
