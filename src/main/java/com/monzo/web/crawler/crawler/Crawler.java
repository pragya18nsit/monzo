package com.monzo.web.crawler.crawler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import com.monzo.web.crawler.model.Page;
import com.monzo.web.crawler.model.Sitemap;
import com.monzo.web.crawler.utils.CrawlerUtils;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jsoup.Connection.Response;

public class Crawler implements Runnable {

	private static final Logger LOG = LoggerFactory.getLogger(Crawler.class);

	private final List<String> visited;

	private final BlockingQueue<Page> queue;

	private final String firstURL;

	private final Sitemap sitemap;

	private final List<String> disallowedURLs;

	private final Boolean showLog;

	/*
	 * Constructor
	 */
	public Crawler(String url, Sitemap s, List<String> visited, BlockingQueue<Page> queue, List<String> disallowedURLs,
			Boolean showLog) {
		this.firstURL = url;
		this.sitemap = s;
		this.queue = queue;
		this.visited = visited;
		this.disallowedURLs = disallowedURLs;
		this.showLog = showLog;
	}

	@SneakyThrows
	@Override
	public void run() {
		/*
		 * While the queue is not empty, poll the element from the head of the queue,
		 * add it to the visited list, and crawl it to get its links
		 */
		while (!queue.isEmpty()) {
			Page page = queue.poll();
			/*
			 * Check if it's been visited already. If it hasn't, crawl it
			 */
			if (!visited.contains(page.getUrl())) {
				visited.add(page.getUrl());
				crawl(page);
			}

		}
	}

	private void crawl(Page page) throws IOException {
		CrawlerUtils.pageCrawler(page.getUrl(), page, sitemap, disallowedURLs, queue, showLog, 0);
	}


}

