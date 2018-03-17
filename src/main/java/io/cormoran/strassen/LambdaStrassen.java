package io.cormoran.strassen;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Table;
import com.google.common.io.CharStreams;

/**
 * mvn package shade:shade lambda:deploy-lambda
 * 
 * @author Benoit Lacelle
 *
 */
public class LambdaStrassen implements RequestStreamHandler {
	protected static ObjectMapper objectMapper = new ObjectMapper();

	Table<V5, V5, V5> aeToAE = Strassen.aeToAE();

	SetMultimap<V5, List<V5>> preparedPairs = Strassen.preparedPairs();

	SetMultimap<V5, V5> leftToRightGiving0 = Strassen.leftToRightFor0();
	SetMultimap<V5, V5> leftToRightGiving1 = Strassen.leftToRightGiving1();

	public static class LambdaScrapperSpringConfig {

	}

	// Required by AWS Lambda as .handleRequest is not static
	// https://docs.aws.amazon.com/lambda/latest/dg/best-practices.html
	public LambdaStrassen() {
	}

	public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
			throws JsonProcessingException, IOException {
		String inputAsString = CharStreams.toString(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

		context.getLogger().log("Received as command: " + inputAsString);

		QueryIJKL ijkl = objectMapper.readValue(inputAsString, QueryIJKL.class);

		List<List<V5>> count = countForIJKL(ijkl);

		objectMapper.writer().writeValue(outputStream, ImmutableMap.of("count", count));
	}

	public List<List<V5>> countForIJKL(QueryIJKL ijkl) {
		return Strassen.processIJKL(leftToRightGiving0,
				leftToRightGiving1,
				// aeToAE,
				preparedPairs,
				Arrays.asList(ijkl.i, ijkl.j, ijkl.k, ijkl.l)).collect(Collectors.toList());
	}

	public Stream<List<V5>> countForIJKL(QueryIJK ijk) {
		Stream<List<V5>> stream = Strassen.allIJKLAsStream().parallel().filter(
				ijkl -> ijkl.get(0).equals(ijk.i) && ijkl.get(1).equals(ijk.j) && ijkl.get(2).equals(ijk.k));

		return stream.flatMap(ijkl -> Strassen.processIJKL(leftToRightGiving0,
				leftToRightGiving1,
				// aeToAE,
				preparedPairs,
				ijkl));
	}

}
