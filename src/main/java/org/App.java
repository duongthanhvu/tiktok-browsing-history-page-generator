package org;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Hello world!
 */
public final class App {
	private App() {
	}

	/**
	 * Says hello to the world.
	 * 
	 * @param args The arguments of the program.
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		OkHttpClient client = new OkHttpClient().newBuilder()
				.connectionSpecs(Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.COMPATIBLE_TLS))
				.followRedirects(false).followSslRedirects(false).build();
		try (BufferedReader reader = new BufferedReader(new FileReader(args[0]))) {
			String line = reader.readLine();
			int limit = 0;
			List<Map<String, Object>> val = new ArrayList<>();
			while (line != null) {
				System.out.println(line);
				// read next line
				line = reader.readLine();
				Request request = new Request.Builder().url(line).get().build();

				String originalLink;
				try (Response response = client.newCall(request).execute()) {
					// Headers headers = response.headers();
					// headers.forEach(header -> System.out.println(header.getFirst() + " : "
					// +header.getSecond()));
					originalLink = response.header("Location");
					System.out.println("link origin: " + originalLink);
				}

				request = new Request.Builder().url("https://www.tiktok.com/oembed?url=" + originalLink).get().build();

				String content;
				try (Response response = client.newCall(request).execute()) {
					content = response.body().string();
					System.out.println(content);
				}

				// read json file data to String
				//byte[] jsonData = Files.readAllBytes(Paths.get("employee.txt"));

				// create ObjectMapper instance
				ObjectMapper objectMapper = new ObjectMapper();

				// read JSON like DOM Parser
				JsonNode rootNode = objectMapper.readTree(content);
				JsonNode idNode = rootNode.path("thumbnail_url");
				String imgUrl = idNode.asText();
				System.out.println("thumb url: " + imgUrl);

				Map<String, Object> variables = new HashMap<>();
				variables.put("linktiktok", originalLink);
				variables.put("thumbimg", imgUrl);
				val.add(variables);
				
				
				limit++;
				System.out.println("============>   number of record: " + limit);
				Thread.sleep(1000);
				if (limit > 300) {
					break;
				}
			}
			processHTML(val);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void processHTML(List<Map<String, Object>> variables) throws IOException {
		TemplateEngine templateEngine = new TemplateEngine();
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("/templates/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setTemplateMode(TemplateMode.HTML);
        templateEngine.setTemplateResolver(resolver);

        Context context = new Context();
        context.setVariable("val", variables);
		FileWriter fileWriter = new FileWriter("result7.html");
    	PrintWriter printWriter = new PrintWriter(fileWriter);
		templateEngine.process("show.html", context, printWriter);
		fileWriter.close();
	}
}
