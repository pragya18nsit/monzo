package com.monzo.web.crawler.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Page {

	private String url;

	private List<String> links;

}
