package com.novel.spider;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.util.HashMap;
import java.util.Map;

public class X23usPageProcessor implements PageProcessor {

    private Site site = Site.me().addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36").setRetryTimes(3).setSleepTime(1000);

    /**
     * 分类URL
     */
    private static final String CATEGORY_PAGE_URL = "htt(p|ps)://www\\.x23us\\.com/class/\\d+_\\d+.html";

    /**
     * 简介URL
     */
    private static final String SUMMARY_PAGE_URL = "htt(p|ps)://www\\.x23us\\.com/book/\\d+";

    /**
     * 目录URL
     */
    private static final String CATELOG_PAGE_URL = "htt(p|ps)://www\\.x23us\\.com/html/\\d+/\\d+/$";

    /**
     * 章节URL
     */
    private static final String CHAPTER_PAGE_URL = "htt(p|ps)://www\\.x23us\\.com/html/\\d+/\\d+/\\d+.html";

    public Site getSite() {
        return site;
    }

    public void process(Page page) {
        Selectable url = page.getUrl();
        if (url.regex(CATEGORY_PAGE_URL).match()) {
            processCategoryPage(page);
        } else if (url.regex(SUMMARY_PAGE_URL).match()) {
            processSummaryPage(page);
        } else if (url.regex(CATELOG_PAGE_URL).match()) {
            processCatelogPage(page);
        } else if (url.regex(CHAPTER_PAGE_URL).match()) {
            processChapterPage(page);
        } else {
            page.setSkip(true);
        }
    }

    /**
     * 处理分类页面
     *
     * @param page
     */
    public void processCategoryPage(Page page) {
        Selectable html = page.getHtml().xpath("//*[@id=\"content\"]/dd[1]/table/tbody");
        page.putField("type", "category");
        // 小说简介页面url
        page.addTargetRequests(html.regex(String.format("(%s)", SUMMARY_PAGE_URL)).all());
        // 小说章节目录页面url
        page.addTargetRequests(html.regex("(htt(p|ps)://www\\.x23us\\.com/html/\\d+/\\d+/)").all());
        // 获取下一页url
        String nextPageUrl = page.getHtml().xpath("//div[@id='pageLink']/a[@class='next']/@href").toString();
        if (nextPageUrl != null) {
            page.addTargetRequest(nextPageUrl);
        }
    }

    /**
     * 处理简介页面
     *
     * @param page
     */
    public void processSummaryPage(Page page) {
        Html html = page.getHtml();
        Map<Object, Object> novel = new HashMap<Object, Object>();
        Selectable category = html.xpath("//*[@id=\"at\"]/tbody/tr[1]/td[1]/a");
        String process = html.xpath("//*[@id=\"at\"]/tbody/tr[1]/td[3]/text()").toString();
        if (process.contains("连载")) {
            novel.put("process", 1);
        } else if (process.contains("完成")) {
            novel.put("process", 2);
        } else {
            novel.put("process", 3);
        }

        novel.put("id", page.getUrl().regex(String.format("(%s)", SUMMARY_PAGE_URL)).toString());
        novel.put("name", html.xpath("//h1/text()").toString().split(" ")[0]);
        novel.put("cover_url", html.xpath("//*[@id=\"content\"]/dd[2]/div[1]/a/img/@src").toString());
        novel.put("introduction", html.xpath("//*[@id=\"content\"]/dd[4]/p[2]/text()").toString());
        novel.put("tags", html.xpath("//*[@id=\"content\"]/dd[4]/p[5]/u").all().toString().replace(",", ""));
        novel.put("latest_chapter", html.xpath("//*[@id=\"content\"]/dd[4]/p[6]/a/text()").toString());
        novel.put("author_name", html.xpath("//*[@id=\"at\"]/tbody/tr[1]/td[2]/text()").toString());
        novel.put("category_id", category.xpath("/a/@href").regex("/class/(\\d+)_\\d+\\.html").toString());
        novel.put("category_name", category.xpath("/a/text()").toString());
        novel.put("collection_number", html.xpath("//*[@id=\"at\"]/tbody/tr[2]/td[1]/text()").toString());
        novel.put("total_words_number", html.xpath("//*[@id=\"at\"]/tbody/tr[2]/td[2]/text()").toString().replace("字", ""));
        novel.put("latest_updated_at", html.xpath("//*[@id=\"at\"]/tbody/tr[2]/td[3]/text()").toString());
        novel.put("total_click_number", html.xpath("//*[@id=\"at\"]/tbody/tr[3]/td[1]/text()").toString());
        novel.put("month_click_number", html.xpath("//*[@id=\"at\"]/tbody/tr[3]/td[2]/text()").toString());
        novel.put("week_click_number", html.xpath("//*[@id=\"at\"]/tbody/tr[3]/td[3]/text()").toString());
        novel.put("total_recommend_number", html.xpath("//*[@id=\"at\"]/tbody/tr[4]/td[1]/text()").toString());
        novel.put("month_recommend_number", html.xpath("//*[@id=\"at\"]/tbody/tr[4]/td[2]/text()").toString());
        novel.put("week_recommend_number", html.xpath("//*[@id=\"at\"]/tbody/tr[4]/td[3]/text()").toString());

        page.putField("type", "summary");
        page.putField("novel", novel);
    }

    /**
     * 处理目录页面
     *
     * @param page
     */
    public void processCatelogPage(Page page) {
        page.addTargetRequests(page.getHtml().xpath("//table[@id='at']").links().all());
	page.setSkip(true);
    }

    /**
     * 处理章节页面
     *
     * @param page
     */
    public void processChapterPage(Page page) {
        page.putField("type", "chapter");
        page.putField("title", page.getHtml().xpath("//h1/text()").toString());
        page.putField("content", page.getHtml().xpath("//dd[@id='contents']/text()").toString());
    }
}
