/*
 * =============================================================================
 * Simplified BSD License, see http://www.opensource.org/licenses/
 * -----------------------------------------------------------------------------
 * Copyright (c) 2008-2009, Marco Terzer, Zurich, Switzerland
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Swiss Federal Institute of Technology Zurich
 *       nor the names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior written
 *       permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * =============================================================================
 */

package ADaMSoft.algorithms;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import ch.javasoft.metabolic.efm.config.Arithmetic;

/*
general options:
   -arithmetic a1 [a2 [a3]]
     * specifies the arithmetic, one of: fractional, double or bigint
     * a1 is used for the core computation (fractional by default)
     * a2 is used for post-processing and output (fractional by default)
     * a3 is used for pre-processing (fractional by default)
   -zero v1 [v2 [v3]]
     * specifies the value to treat as zero if double arithmetic is used
     * v1, v2 and v3 correspond to arithmetic a1, a2 and a3 above
     * default: 1e-10 for double, 0 for fractional/bigint
     * use NaN for default value
   -maxthreads t
     * maximum number of threads to use, 0 (default)
       to use as many threads as system cores
   -algorithm a
     * algorithm implementation, one of: standard (default), borndie
   -model m
     * variant for standard algorithm, one of: nullspace (default), canonical
   -adj a
     * method to use for adjacent ray enumeration, one of:
       pattern-tree-minzero (default), pattern-tree-rank, pattern-tree-mod-rank,
       rankup-modpi-incore, rankup-modpi-outcore
     * do not use pattern-tree-minzero for for the born/die implementation
   -sortinput o
     * row ordering applied to input matrices, one of:
       LexMin, AbsLexMin, MostZeros, FewestNegPos
     * by default, input rows are not sorted
   -sortinitial o
     * row ordering applied to initial kernel matrix, one of:
       MostZerosOrAbsLexMin (default), MostZeros, AbsLexMin, LexMin, FewestNegPos,
       MostZerosOrFewestNegPos, MostZerosOrLexMin, FewestNegPosOrMostZeros,
       FewestNegPosOrAbsLexMin, Random
   -compression c
     * compression to use, one of: default, off
   -memory m
     * memory model, one of: in-core (default), out-core, sort-out-core
     * for out-core/sort-out-core memory, also set the tmpdir option
   -tmpdir dir
     * directory for temporary files, if out-core memory is used
     * use fast, local drive directory with large capacity

log options:
   -level l -format f
     * l: log level, one of: OFF, SEVERE, WARNING, CONFIG, INFO (default), FINE, FINER, FINEST, ALL
     * f: log format, one of: default, plain

other options:
   --help | -h | -? | ?
     * display this help message
   --version | -v
     * display version information
 */
/**
 * The <code>Options</code> class can be used to set polco computation options
 * if the {@link PolcoAdapter} is used.
 */
public class Options {

	/**
	 * Constructor for options using default values if no options are changed.
	 */
	public Options() {
		super();
		//java -jar polco.jar -kind text -iq ccp6.iq -out text ccp6.txt
		put("-kind", "text");
		put("-iq", "in.iq");
		put("-out", "text", "out.rays");
	}

	private static interface GenericOption {
		/**
		 * Returns the option name
		 * @return option name
		 */
		String getOptionName();
		/**
		 * Returns the option values
		 * @return option values
		 */
		String[] getOptionValues();

	}
	/**
	 * Constants for the {@link #setAlgorithm(Algorithm) algorithm} option. The
	 * default option is {@link #standard}.
	 */
	public static enum Algorithm implements GenericOption {
		/**
		 * Standard double description method algorithm, binary nullspace
		 * approach with bit pattern trees
		 */
		standard,
		/**
		 * Born/die variant of the double description algorithm
		 */
		borndie;

		public String getOptionName() {
			return "-algorithm";
		}

		public String[] getOptionValues() {
			return new String[] {name()};
		}
	};
	/**
	 * Constants for the {@link #setModel(Model) model} option. The default
	 * option is {@link #nullspace}. Affects only the
	 * {@link Algorithm#standard standard} algorithm.
	 */
	public static enum Model implements GenericOption {
		/**
		 * (Binary) nullspace approach, starting with a nullspace basis matrix,
		 * enforcing non-negativity constraints in the iteration phase.
		 */
		nullspace,
		/**
		 * Canonical approach, starting with an identity matrix, enforcing the
		 * equality constraints in the iteration phase
		 */
		canonical;

		public String getOptionName() {
			return "-model";
		}

		public String[] getOptionValues() {
			return new String[] {name()};
		}
	};
	/**
	 * Constants for the {@link #setCompression(Compression) compression}
	 * option. The default option is {@link #standard}.
	 */
	public static enum Compression implements GenericOption {
		/**
		 * Standard compression techniques are used
		 */
		standard {
			@Override
			public String[] getOptionValues() {
				return new String[] {"default"};
			}
		},
		/**
		 * No compression is used
		 */
		off;

		public String getOptionName() {
			return "-compression";
		}

		public String[] getOptionValues() {
			return new String[] {name()};
		}
	};
	/**
	 * Constants for the {@link #setMemory(Memory) memory} option. The default
	 * option is {@link #in_core}.
	 */
	public static enum Memory implements GenericOption {
		/**
		 * In-core heap memory is used to store intermediary rays.
		 */
		in_core,
		/**
		 * Out-core memory is used, meaning that intermediary rays are stored in
		 * files.
		 */
		out_core,
		/**
		 * Out-core memory is used, meaning that intermediary rays are stored in
		 * files. Sorting of arrays is performed in-core, meaning that an index
		 * array is kept in heap memory.
		 */
		sort_out_core;

		public String getOptionName() {
			return "-memory";
		}

		public String[] getOptionValues() {
			return new String[] {name().replace('_', '-')};
		}
	};

	/**
	 * Constants for the {@link #setLogFormat(LogFormat) log format} option. The
	 * default option is {@link #standard}.
	 */
	public static enum LogFormat implements GenericOption {
		/**
		 * Standard log format with time stamp and thread prefix
		 */
		standard,
		/**
		 * Plain log format tracing only the actual log messages
		 */
		plain;

		public String getOptionName() {
			return "-format";
		}
		public String[] getOptionValues() {
			return new String[] {name()};
		}
	};

	private final Map<String, String[]> opts = new LinkedHashMap<String, String[]>();

	/**
	 * Sets the arithmetic used for the core computation, which is fractional by
	 * default.
	 *
	 * @param core	arithmetic to use for core computation
	 */
	public void setArithmetic(Arithmetic core) {
		put("-arithmetic", core.getNiceName());
	}
	/**
	 * Sets the arithmetic used for the core computation and for
	 * post-processing. The default is fractional for both phases.
	 *
	 * @param core	arithmetic to use for core computation
	 * @param post	arithmetic to use for post-processing
	 */
	public void setArithmetic(Arithmetic core, Arithmetic post) {
		put("-arithmetic", core.getNiceName(), post.getNiceName());
	}
	/**
	 * Sets the arithmetic used for the core computation, for post-processing
	 * and for pre-processing. The default is fractional for all phases.
	 *
	 * @param core	arithmetic to use for core computation
	 * @param post	arithmetic to use for post-processing
	 * @param pre	arithmetic to use for pre-processing
	 */
	public void setArithmetic(Arithmetic core, Arithmetic post, Arithmetic pre) {
		put("-arithmetic", core.getNiceName(), post.getNiceName(), pre.getNiceName());
	}

	/**
	 * Sets the value that should be treated as zero if double arithmetic is
	 * used. The zero value are used for the core computation only.
	 * <p>
	 * For exact computation (fractional,bigint,varint), zero is the default and
	 * should be used, for double arithmetic, 1e-10 is the default value. Set
	 * the zero value to {@link Double#NaN NaN} to force use of default values.
	 *
	 * @param zeroCore	zero value for core computation
	 */
	public void setZero(double zeroCore) {
		put("-zero", String.valueOf(Math.abs(zeroCore)));
	}

	/**
	 * Sets the value that should be treated as zero if double arithmetic is
	 * used. The zero value are used for the core computation and for
	 * post-processing, respectively.
	 * <p>
	 * For exact computation (fractional,bigint,varint), zero is the default and
	 * should be used, for double arithmetic, 1e-10 is the default value. Set
	 * the zero value to {@link Double#NaN NaN} to force use of default values.
	 *
	 * @param zeroCore	zero value for core computation
	 * @param zeroPost	zero value for post-processing
	 */
	public void setZero(double zeroCore, double zeroPost) {
		put("-zero", String.valueOf(Math.abs(zeroCore)), String.valueOf(Math.abs(zeroPost)));
	}
	/**
	 * Sets the value that should be treated as zero if double arithmetic is
	 * used. The zero value are used for the core computation, for
	 * post-processing and for pre-processing, respectively.
	 * <p>
	 * For exact computation (fractional,bigint,varint), zero is the default and
	 * should be used, for double arithmetic, 1e-10 is the default value. Set
	 * the zero value to {@link Double#NaN NaN} to force use of default values.
	 *
	 * @param zeroCore	zero value for core computation
	 * @param zeroPost	zero value for post-processing
	 * @param zeroPre	zero value for pre-processing
	 */
	public void setZero(double zeroCore, double zeroPost, double zeroPre) {
		put("-zero", String.valueOf(Math.abs(zeroCore)), String.valueOf(Math.abs(zeroPost)), String.valueOf(Math.abs(zeroPre)));
	}

	/**
	 * Sets the maximum number of threads to use. Set the value to 0
	 * (the default) to use as many threads as system CPU cores.
	 *
	 * @param maxThreads	the maximum number of threads to use, or 0 to use as
	 * 						many threads as system CPU cores.
	 */
	public void setMaxThreads(int maxThreads) {
		put("-maxthreads", String.valueOf(maxThreads));
	}

	/**
	 * Sets the algorithm implementation that should be used. Consider the
	 * {@link Algorithm} constants for suitable values.
	 *
	 * @param algorithm the algorithm implementation to use
	 */
	public void setAlgorithm(Algorithm algorithm) {
		put(algorithm);
	}

	/**
	 * Sets the model to use for the {@link Algorithm#standard standard}
	 * algorithm. Consider the {@link Model} constants for suitable values.
	 *
	 * @param model the model for the standard algorithm
	 */
	public void setModel(Model model) {
		put(model);
	}

	/**
	 * Sets the adjacency test method to use. The following values are
	 * supported:<br>
	 * pattern-tree-minzero (default), pattern-tree-rank, pattern-tree-mod-rank,
	 * rankup-modpi-incore, rankup-modpi-outcore
	 * <p>
	 * <b>Note:</b><br>
	 * Do not use pattern-tree-minzero for for the
	 * {@link Algorithm#borndie borndie} algorithm implementation.
	 *
	 * @param adj the adjacency test method to use
	 */
	public void setAdjacencyMethod(String adj) {
		put("-adj", adj);
	}

	/**
	 * Sets row ordering applied to input matrices. The following values are
	 * supported:<br>
	 * LexMin, AbsLexMin, MostZeros, FewestNegPos
	 * <p>
     * By default, input rows are not sorted.
	 */
	public void setSortInput(String sorting) {
		put("sortinput", sorting);
	}
	/**
	 * Sets row ordering applied to the initial kernel matrix. The following
	 * values are supported:<br>
	 * MostZerosOrAbsLexMin (default), MostZeros, AbsLexMin, LexMin,
	 * FewestNegPos, MostZerosOrFewestNegPos, MostZerosOrLexMin,
	 * FewestNegPosOrMostZeros, FewestNegPosOrAbsLexMin, Random
	 * <p>
     * By default, MostZerosOrAbsLexMin is used.
	 */
	public void setSortInitial(String sorting) {
		put("sortinput", sorting);
	}
	/**
	 * Sets the compression to use for the input matrices. Consider the
	 * {@link Compression} constants for suitable values.
	 *
	 * @param compression the compression strategy
	 */
	public void setCompression(Compression compression) {
		put(compression);
	}
	/**
	 * Sets the memory to used to store intermediary rays. Consider the
	 * {@link Memory} constants for suitable values.
	 *
	 * @param memory the memory used to store intermediary rays
	 */
	public void setMemory(Memory memory) {
		put(memory);
	}

	/**
	 * Sets the directory for temporary files, if out-core {@link Memory} is
	 * used. It is recommended to use a fast, local drive directory with large
	 * capacity.
	 */
	public void setTmpDir(File tmpDir) {
		put("-tmpdir", tmpDir.getAbsolutePath());
	}

	/**
	 * Sets the log level to use. Consider the {@link Level} constants for
	 * suitable values.
	 *
	 * @param level log level
	 */
	public void setLoglevel(Level level) {
		put("-level", level.getName());
	}

	/**
	 * Sets the log format to use. Consider the {@link LogFormat} constants for
	 * suitable values.
	 *
	 * @param format log output format
	 */
	public void setLogFormat(LogFormat format) {
		put(format);
	}

	/**
	 * Sets the log file. All logging output will be written to that file. By
	 * default, log output is written to the standard error and output stream.
	 * If you set the file to {@code null}, the default logging to the error
	 * and output stream is used.
	 *
	 * @param file 	the log file, or {@code null} to log to standard error and
	 * 				output stream (the default).
	 */
	public void setLogFile(File file) {
		if (file == null) {
			opts.remove("-log");
		}
		else {
			put("-log", "file", file.getAbsolutePath());
		}
	}

	private void put(GenericOption option) {
		put(option.getOptionName(), option.getOptionValues());
	}
	private void put(String optionName, String... optionValues) {
		opts.put(optionName, optionValues);
	}

	/**
	 * Merges {@code this} options with {@code other}. For conflicting options,
	 * the values of {@code this} class are preferred.
	 *
	 * @param other the options to merge with
	 * @return the merged options object
	 */
	public Options merge(Options other) {
		final Options merge = new Options();
		merge.opts.putAll(other.opts);
		merge.opts.putAll(this.opts);
		return merge;
	}

	/**
	 * Converts the options into an args list as usually passed to the main
	 * method of the polco program.
	 * @return the args list as used in the main method
	 */
	protected String[] toArgs() {
		final List<String> list = new ArrayList<String>();
		for (final String key : opts.keySet()) {
			list.add(key);
			list.addAll(Arrays.asList(opts.get(key)));
		}
		return list.toArray(new String[list.size()]);
	}
}
