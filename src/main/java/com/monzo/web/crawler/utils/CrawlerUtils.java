package com.monzo.web.crawler.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;

import com.monzo.web.crawler.model.Page;
import com.monzo.web.crawler.model.Sitemap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Class containing util methods used along the application
 */
public class CrawlerUtils {

	private static final Logger LOG = LoggerFactory.getLogger(CrawlerUtils.class);

	/*
	 * This method compares the hostname of two URLs. Ignores wether they start with
	 * www or not
	 */
	public static boolean isSameDomain(String linkUrl, String url) {

		try {
			URI linkURI = new URI(linkUrl);
			URI uri = new URI(url);

			/*
			 * Delete the trailing www. if exists
			 */
			String linkUriString = linkURI.getHost().startsWith("www.") ? linkURI.getHost().substring(4)
					: linkURI.getHost();
			String uriString = uri.getHost().startsWith("www.") ? uri.getHost().substring(4) : uri.getHost();

			/*
			 * If both hostnames are the same, return true
			 */
			return linkUriString.equals(uriString);

		} catch (URISyntaxException e) {
			LOG.error("Error parsing URL. Message: {}", e.getMessage());
		} catch (Exception e) {
			LOG.error("Error in crawling URL. Message: {}", e.getMessage());
		}

		return false;

	}

	/*
	 * Validates if the URL is valid with Apache Commons URL Validator
	 */
	public static boolean isURLValid(String url) {
		UrlValidator urlValidator = new UrlValidator();
		return urlValidator.isValid(url);
	}

	public static void pageCrawler(String url, Page page, Sitemap sitemap, List<String> disallowedURLs, BlockingQueue<Page> queue, Boolean showLog, Integer retryCount ) {
		retryCount = retryCount + 1;
		if (retryCount == 3){
			return;
		}
		try {
			/*
			 * Get the document and its a href elements with jsoup
			 */

			Connection.Response response = Jsoup.connect(page.getUrl()).timeout(10 * 1000).ignoreContentType(true).execute();

			if (200 == response.statusCode()) {
				Document document = Jsoup.connect(url).timeout(0).ignoreContentType(true).get();
				Elements linksOnPage = document.select("a[href]");
				List<String> links = new ArrayList<>();
				/*
				 * Iterate over all elements
				 */
				for (Element link : linksOnPage) {
					String linkURL = link.attr("abs:href");
					/*
					 * If the link is empty, ignore it
					 */
					if (StringUtils.isEmpty(linkURL))
						continue;
					/*
					 * Delete all trailing characters that are not letters, such as / and #. This is
					 * done to avoid duplicates, as www.monzo.com and www.monzo.com/ would be trated
					 * as different, but will have the same links
					 */
					while (!Character.isLetter(linkURL.charAt(linkURL.length() - 1))) {
						linkURL = linkURL.substring(0, linkURL.length() - 1);
					}
					/*
					 * Check if the current URL is in the disallowed list, if they belong to the
					 * same domain, and if it's not already in the queue
					 */
					if (disallowedURLs.stream().noneMatch(linkURL::startsWith)) {
						/*
						 * If it's not in the queue, create a new page and add it
						 */
						if (CrawlerUtils.isSameDomain(linkURL, url)
								&& queue.stream().map(Page::getUrl).noneMatch(linkURL::equals)) {
							Page linkedPage = new Page();
							linkedPage.setUrl(linkURL);
							queue.add(linkedPage);
						}

						/*
						 * Add the list of links to the page
						 */
						links.add(linkURL);

					}

				}
				/*
				 * Add page to sitemap
				 */
				page.setLinks(links);
				if (showLog)
					LOG.info("Crawled {} Found {} links", page.getUrl(), page.getLinks().size());
				sitemap.addPage(page);
			}
		} catch (IOException e) {
			retryCount = retryCount +1;
			LOG.error("Error parsing {}", page.getUrl());
			LOG.info("Retrial count {}", retryCount);
			LOG.error("Error reading {} Message: {}", page.getUrl(), e.getMessage());
			CrawlerUtils.pageCrawler(url, page, sitemap, disallowedURLs, queue, showLog, retryCount);
		}
	}

	public static Properties readPropertiesFile(String fileName) throws IOException {
		FileInputStream fis = null;
		Properties prop = null;
		try {
			fis = new FileInputStream(fileName);
			prop = new Properties();
			prop.load(fis);
		} catch(FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		} finally {
			fis.close();
		}
		return prop;
	}

}
