package com.novel.spider;

import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.pipeline.JsonFilePipeline;

public class Run {
    public static void main(String[] args) {
        Spider spider = Spider.create(new X23usPageProcessor());
        spider.addUrl("https://www.x23us.com/class/2_1.html");
        spider.addPipeline(new JsonFilePipeline());
        spider.thread(10).run();
    }
}
