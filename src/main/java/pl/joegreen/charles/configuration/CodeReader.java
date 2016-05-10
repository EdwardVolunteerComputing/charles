package pl.joegreen.charles.configuration;

import java.io.File;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;

import pl.joegreen.charles.PhaseType;

public class CodeReader {

	public static String readCode(ExperimentConfiguration configuration,
			PhaseType phaseType) {
		PhaseParameters phaseConfiguration = configuration
				.getPhaseConfiguration(phaseType);
		List<String> codeFiles = phaseConfiguration.getCodeFiles();
		StringBuilder builder = new StringBuilder();
		builder.append("(function(){ \n");
		builder.append("\n/* Code generated by Charles */\n");

		Stream<File> absoluteCodeFiles = codeFiles.stream().map(
				path -> {
					File file = new File(path);
					if (file.isAbsolute()) {
						return file;
					} else {
						return new File(configuration.getSource()
								.getParentFile(), path);
					}
				});

		absoluteCodeFiles.forEach(file -> {
			try {
				builder.append("\n/* Code from file: " + file.getAbsolutePath()
						+ " */\n");
				// TODO: encoding
				builder.append(FileUtils.readFileToString(file));
			} catch (Exception e) {
				// TODO: handle
				throw new RuntimeException(e);
			}
		});

		builder.append(";\n return ");
		switch (phaseType) {
		case GENERATE:
			builder.append("generate; ");
			break;
		case IMPROVE:
			builder.append("function(input){return improve(input.population, input.parameters)}");
			break;
		case MIGRATE:
			if (configuration.isAsynchronous()) {
				builder.append("function(input){return migrate(input.population, input.pool, input.parameters)}");
			} else {
				builder.append("function(input){return {populations: migrate(input.firstPopulation, input.secondPopulation, input.parameters)}}");
			}
			break;
		}
		builder.append("\n/* End of code generated by Charles */\n");
		builder.append("}())");
		return builder.toString();
	}
}
