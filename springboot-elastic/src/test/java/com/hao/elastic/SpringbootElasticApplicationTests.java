package com.hao.elastic;

import com.hao.elastic.entity.Book;
import io.searchbox.client.JestClient;
import io.searchbox.core.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SpringBootTest
class SpringbootElasticApplicationTests {

    @Autowired
    JestClient jestClient;

    @Test
    public void index() {
        List<Book> bookList = new ArrayList<>();
        Book book = new Book();
        book.setId("1");
        book.setName(null);
        book.setContent("hello");
        book.setCreateDate(new Date());
        bookList.add(book);
        //构建一个索引功能，类型为news
//        Index index = new Index.Builder(book).index("jest").type("news").build();
        Bulk.Builder bulk = new Bulk.Builder().defaultIndex("jest").defaultType("news");
        for (Book obj : bookList) {
            Index index = new Index.Builder(obj).build();
            bulk.addAction(index);
        }
        try {
            BulkResult br = jestClient.execute(bulk.build());
            if (br.isSucceeded()) {
                System.out.println("数据索引成功！");
            } else {
                List<BulkResult.BulkResultItem> failedItems = br.getFailedItems();
                for (BulkResult.BulkResultItem item : failedItems) {
                    System.out.println(item.errorReason);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        //include_type_name
        /*System.out.println(index.getRestMethodName());
        try {
            jestClient.execute(index);
            System.out.println("数据索引成功！");
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    @Test
    public void search() {
        //查询表达式
        String json = "{\n" +
               /* "  \"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"must_not\": {\n" +
                "        \"exists\":{\"field\":\"test\"}\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +*/
                "}";
        //构建搜索功能
        Search search = new Search.Builder(json).addIndex("jest").addType("news").build();
        try {
            SearchResult result = jestClient.execute(search);
            System.out.println(result.getJsonString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void contextLoads() throws IOException {
    }

    @Test
    void test() throws IOException {

    }

}
