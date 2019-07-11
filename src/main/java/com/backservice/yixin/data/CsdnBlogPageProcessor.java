package com.backservice.yixin.data;

import com.backservice.yixin.data.dto.CsdnBlogDto;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import javax.servlet.http.Cookie;
import java.util.List;
import java.util.Set;

public class CsdnBlogPageProcessor implements PageProcessor {
    private String pageText = null;
    public static Integer count = 0;
    private static String username = "yx0628";// 设置csdn用户名
    private Site site = Site.me().setCycleRetryTimes(5).setRetryTimes(5).
            setSleepTime(1000).setTimeOut(3*60*1000);
    private Set<Cookie> cookies;
    public String getPageText(){
        return pageText;
    }
    @Override
    public void process(Page page) {
// 列表页
        System.out.println("pagessss"+page.getHtml());
        System.out.println("pagesssssss"+page.getRawText());
        if (page.getUrl().regex("https://blog\\.csdn\\.net/" + username + "/article/details/").match()) {
            // 添加所有文章页
            page.addTargetRequests(page.getHtml().xpath("//div[@id='article_list']").links()// 限定文章列表获取区域
                    .regex("/" + username + "/article/details/\\d+")
                    .replace("/" + username + "/", "https://blog.csdn.net/" + username + "/")// 巧用替换给把相对url转换成绝对url
                    .all());
            // 添加其他列表页
            page.addTargetRequests(page.getHtml().xpath("//div[@id='papelist']").links()// 限定其他列表页获取区域
                    .regex("/" + username + "/article/list/\\d+")
                    .replace("/" + username + "/", "https://blog.csdn.net/" + username + "/")// 巧用替换给把相对url转换成绝对url
                    .all());
            // 文章页
        } else {
            count++;// 文章数量加1
            // 用CsdnBlog类来存抓取到的数据，方便存入数据库
            CsdnBlogDto csdnBlog = new CsdnBlogDto();
            // 设置编号
            csdnBlog.setId(Long.valueOf(
                    page.getUrl().regex("https://blog\\.csdn\\.net/" + username + "/article/details/(\\d+)").get()));
            // 设置标题
            csdnBlog.setTitle(
                    page.getHtml().xpath("//div[@class='article_title']//span[@class='link_title']/a/text()").get());
            // 设置日期
            csdnBlog.setDate(
                    page.getHtml().xpath("//div[@class='article_r']/span[@class='link_postdate']/text()").get());
            // 设置标签（可以有多个，用,来分割）
            csdnBlog.setTags(listToString(page.getHtml()
                    .xpath("//div[@class='article_l']/span[@class='link_categories']/a/allText()").all()));
            // 设置类别（可以有多个，用,来分割）
            csdnBlog.setCategory(
                    listToString(page.getHtml().xpath("//div[@class='category_r']/label/span/text()").all()));
            // 设置阅读人数
            csdnBlog.setView(Integer.parseInt(page.getHtml().xpath("//div[@class='article_r']/span[@class='link_view']")
                    .regex("(\\d+)人阅读").get()));
            // 设置评论人数
            csdnBlog.setComments(Integer.parseInt(page.getHtml()
                    .xpath("//div[@class='article_r']/span[@class='link_comments']").regex("\\((\\d+)\\)").get()));
            // 设置是否原创
            csdnBlog.setCopyright(page.getHtml().regex("bog_copyright").match() ? 1 : 0);
            // 把对象输出控制台
            System.out.println("title"+csdnBlog.getTitle());
        }
    }

    @Override
    public Site getSite() {
        return site;
    }

    public static void main(String[] args) {
        long startTime, endTime;
        startTime = System.currentTimeMillis();
        // 从用户博客首页开始抓，开启5个线程，启动爬虫
        Spider.create(new CsdnBlogPageProcessor()).addUrl("https://blog.csdn.net/" + username).thread(1).run();
        endTime = System.currentTimeMillis();
    }

    public static String listToString(List<String> stringList) {
        if (stringList == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        boolean flag = false;
        for (String string : stringList) {
            if (flag) {
                result.append(",");
            } else {
                flag = true;
            }
            result.append(string);
        }
        return result.toString();
    }
}
