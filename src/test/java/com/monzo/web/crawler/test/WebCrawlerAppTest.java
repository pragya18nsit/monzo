package com.monzo.web.crawler.test;

import com.monzo.web.crawler.model.CrawlerCustomException;
import com.monzo.web.crawler.utils.CrawlerUtils;
import org.junit.Test;

import com.monzo.web.crawler.WebCrawlerApp;

public class WebCrawlerAppTest {

	@Test(expected = CrawlerCustomException.class)
	public void notEnoughArguments() throws CrawlerCustomException {
		String[] args = { "argument" };
		WebCrawlerApp.main(args);
	}

	@Test(expected = CrawlerCustomException.class)
	public void wrongUrl() throws CrawlerCustomException {
		String[] args = { "wrongurl", "5", "true" };
		WebCrawlerApp.main(args);
	}

	@Test(expected = CrawlerCustomException.class)
	public void wrongNumberOfThreads() {
		String[] args = { "https://monzo.com", "-5", "true" };
		WebCrawlerApp.main(args);
	}

	@Test
	public void testUrlTest() {
		String url = "https://adaral.github.io";
		String[] args = { url, "2", "true" };
		WebCrawlerApp.main(args);
	}
	
	@Test
	public void isSameDomainTest() {
		String url = "https://monzo.com";
		String linkUrl = "https://monzo.com/blog/";
		CrawlerUtils.isSameDomain(linkUrl, url);

		url = "https://www.monzo.com";
		linkUrl = "https://www.monzo.com/blog/";
		CrawlerUtils.isSameDomain(linkUrl, url);
	}

	@Test
	public void isSameDomainExceptionTest() {
		String url = "http://monzo.com/h?s=^IXIC";
		CrawlerUtils.isSameDomain(url, url);
	}

	@Test
	public void isUrlValid() {
		CrawlerUtils.isURLValid("https://monzo.com");
	}

}
