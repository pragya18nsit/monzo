package com.monzo.web.crawler.crawler;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.monzo.web.crawler.utils.CrawlerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * This class provides a static method that given a URL, looks for the robots.txt, 
 * parses it, and returns a list with all disallowed urls. The following code duplicates
 * some of the URLs. Eg: it will add to the list https://monzo.com/docs/ and https://www.monzo.com/docs
 * This is to make sure that wether the given URL starts with www or not, de disallowed links will
 * not be followed.
 */
public class RobotsParser {

	private static final Logger LOG = LoggerFactory.getLogger(CrawlerManager.class);

	// constants
	private static final String ROBOTS_TXT = "/robots.txt";
	public static final String DISALLOW = "Disallow:";

	public static List<String> checkRobotsTxt(String url) {

		try {

			URI uri = new URI(url);

			/*
			 * Get the protocol and hostname of the URL. Form the full robots.txt URL
			 */
			String protocol = uri.getScheme();
			String hostname = uri.getHost();
			String baseURL = protocol + "://" + hostname;
			/*
			 * If the hostname does not start with www, add it
			 */
			String secondBaseUrl = protocol + "://" + (!hostname.startsWith("www.") ? "www." + hostname : hostname);

			URL robotsURL = new URL(baseURL + ROBOTS_TXT);

			/*
			 * Create a new connection and add a User-Agent to avoid getting 403 errors.
			 * Read the contents of robots.txt and put it in a buffered reader
			 */
			HttpURLConnection connection = (HttpURLConnection) robotsURL.openConnection();
			String userAgent = CrawlerUtils.readPropertiesFile("src/main/resources/config.properties").getProperty("userAgent");
			connection.addRequestProperty("User-Agent", userAgent);
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			/*
			 * Read the buffer and split it into an array by lines.
			 */
			String robots = reader.lines().collect(Collectors.joining("\n"));
			String[] lines = robots.split("\n");

			/*
			 * Form the disallowed URLS, add them to a list and return them
			 */
			List<String> disallowedURLs = new ArrayList<>();
			for (String string : lines) {
				if (string.startsWith(DISALLOW)) {
					String[] line = string.split(" ");
					disallowedURLs.add(baseURL + line[1]);
					disallowedURLs.add(secondBaseUrl + line[1]);
				}
			}

			return disallowedURLs;

		} catch (URISyntaxException e) {
			LOG.error("Error parsing {}. Error message: {}", url, e.getMessage());
		} catch (MalformedURLException e) {
			LOG.error("The robots.txt URL is not valid");
		} catch (IOException e) {
			LOG.error("Error parsing robots.txt. Error messaege: {}", e.getMessage());
		}

		return null;

	}

}
