package com.hao.elastic;

import com.hao.elastic.entity.Book;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class SpringbootElasticApplicationTests {

    @Autowired
    JestClient jestClient;

    @Test
    public void index() {
//        Map<String, Object> data = new HashMap<>();
        Book book = new Book();
        book.setId("1");
        book.setName(null);
        book.setContent("hello");
//        data.put("id", 1);
//        data.put("content", "hello");
//        data.put("test", null);
        //构建一个索引功能，类型为news
        Index index = new Index.Builder(book).index("jest").type("news").build();
//        PutTemplate putTemplate = new PutTemplate.Builder("my_returnreport", book).build();
        System.out.println(index.getRestMethodName());
        try {
            jestClient.execute(index);
            System.out.println("数据索引成功！");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void search() {
        //查询表达式
        String json = "{\n" +
                "  \"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"must_not\": {\n" +
                "        \"exists\":{\"field\":\"test\"}\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
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
