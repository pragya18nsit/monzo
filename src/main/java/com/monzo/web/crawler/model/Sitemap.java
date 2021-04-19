package com.monzo.web.crawler.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

@Getter
public class Sitemap {

	private List<Page> pages = new ArrayList<>();

	public void addPage(Page page) {
		this.pages.add(page);
	}

}
