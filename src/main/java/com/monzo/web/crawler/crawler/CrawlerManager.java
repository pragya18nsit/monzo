package com.monzo.web.crawler.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import com.monzo.web.crawler.model.Page;
import com.monzo.web.crawler.model.Sitemap;
import com.monzo.web.crawler.utils.CrawlerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrawlerManager {

	private static final Logger LOG = LoggerFactory.getLogger(CrawlerManager.class);

	private Sitemap sitemap;

	private final ExecutorService executor;

	private final List<String> visited;

	private final BlockingQueue<Page> queue;

	private String url;

	private int numberOfThreads;

	private List<String> disallowedURLs;

	private Boolean showLog;

	/*
	 * Constructor
	 */
	public CrawlerManager(String url, int numberOfThreads, List<String> disallowedURLs, Sitemap sitemap,
			Boolean showLog) {
		/*
		 * Create a new executor with a pool with the number of threads provided
		 */
		this.executor = Executors.newFixedThreadPool(numberOfThreads);
		/*
		 * Create a new thread safe queue where to put the Pages to crawl and a
		 * synchronized list where to put the URLs already visited
		 */
		this.queue = new LinkedBlockingQueue<>();
		this.visited = Collections.synchronizedList(new ArrayList<>());
		this.url = url;
		this.numberOfThreads = numberOfThreads;
		this.disallowedURLs = disallowedURLs;
		this.sitemap = sitemap;
		this.showLog = showLog;
	}

	/*
	 * This method starts the crawling proccess
	 */
	public Sitemap startCrawling() {

		LOG.info("Starting to crawl {} with {} threads", url, numberOfThreads);

		/*
		 * Start the queue so it consists of more than one element
		 */
		startListAndQueue(url);

		/*
		 * Create as many crawlers as number of threads
		 */

		List<Runnable> runnables = new ArrayList<>();
		for (int i = 0; i < numberOfThreads; i++) {
			runnables.add(new Crawler(url, sitemap, visited, queue, disallowedURLs, showLog));
		}

		/*
		 * Run runnables asynchronously and wait for all of them to finish
		 */

		CompletableFuture<?>[] futures = runnables.stream().map(r -> CompletableFuture.runAsync(r, executor))
				.toArray(CompletableFuture[]::new);
		CompletableFuture.allOf(futures).join();

		/*
		 * Shutdown the executor and return the sitemap object with all Pages
		 */
		executor.shutdown();
		return sitemap;
	}

	/*
	 * This method initiates the queue. As we start with only one url, this will add
	 * all links of the first URL to the queue so the threads can crawl different
	 * URLs
	 */
	private void startListAndQueue(String url) {

		/*
		 * Create a new Page object and set its URL
		 */
		Page page = new Page();
		page.setUrl(url);
		visited.add(url);
		CrawlerUtils.pageCrawler(url, page, sitemap, disallowedURLs, queue, showLog, 0);

	}

}
