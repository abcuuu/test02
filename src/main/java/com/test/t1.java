package com.test;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.File;
import java.io.IOException;

public class t1 {

    @Test  //根据name:java查询
    public void queryIndex() throws IOException {
        FSDirectory directory = FSDirectory.open(new File("D:\\WeGame\\index"));
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher=new IndexSearcher(reader);
        Term term=new Term("name","java");
        //TermQuery(term)是最小的搜索单元
        TopDocs topDocs = indexSearcher.search(new TermQuery(term), 100);
        System.out.println(topDocs.totalHits);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            Document doc = indexSearcher.doc(scoreDoc.doc);
            System.out.println("id:"+doc.get("id")+",name:"+doc.get("name")+",score:"+scoreDoc.score);
        }
        reader.close();
    }

    @Test  //查询所有
    public void queryIndexAll() throws IOException {
        FSDirectory directory = FSDirectory.open(new File("D:\\WeGame\\index"));
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher=new IndexSearcher(reader);
        TopDocs topDocs = indexSearcher.search(new MatchAllDocsQuery(), 100);
        System.out.println(topDocs.totalHits);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            Document doc = indexSearcher.doc(scoreDoc.doc);
            System.out.println("id:"+doc.get("id")+",name:"+doc.get("name")+",score:"+ scoreDoc.score);
        }
        reader.close();
    }

    @Test  //根据价格区间查询所有
    public void queryIndexAllAndPrice() throws IOException {
        FSDirectory directory = FSDirectory.open(new File("D:\\WeGame\\index"));
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher=new IndexSearcher(reader);
        //  field : 域名
        // min : 最小值
        // max : 最大值
        // minInclusive : 是否包含最小值
        // maxInclusive : 是否包含最大值
        // 50 <= price <= 70   true true    语法:   price:[50.0 TO 70.0]
        // 50 < price <= 70   false true    语法:   price:{50.0 TO 70.0]
        // 如果包含某一个值,使用中括号[]
        // 如果不包含某一个值,使用大括号{}
        NumericRangeQuery<Float> rangeQuery = NumericRangeQuery.newFloatRange
                ("price", 50f, 70f, false, true);
        TopDocs topDocs = indexSearcher.search(rangeQuery, 100);
        System.out.println(topDocs.totalHits);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            Document doc = indexSearcher.doc(scoreDoc.doc);
            System.out.println("id:"+doc.get("id")+",name:"+doc.get("name")+",price:"+doc.get("price"));
        }
        reader.close();
    }

    @Test  //根据名字包含apache和价格区间的查询
    public void queryIndexAllAndPriceAndName() throws IOException {
        FSDirectory directory = FSDirectory.open(new File("D:\\WeGame\\index"));
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher=new IndexSearcher(reader);
        BooleanQuery booleanQuery=new BooleanQuery();
        NumericRangeQuery<Float> rangeQuery = NumericRangeQuery.newFloatRange
                ("price", 50f, 70.f, false, true);
        TermQuery termQuery = new TermQuery(new Term("name", "apache"));
        // MUST   : 必须满足该条件,可以理解为SQL语法中的AND
        // MUST_NOT  : 必须不满足该条件,可以理解为SQL语法中的NOT
        // SHOULD  : 应该满足该条件,可以理解为SQL语法中的OR
        // 参数1 : 要被组合进来的查询条件
        // 参数2 : 如何组合查询条件
        // MUST  MUST  两个条件必须都满足,等于获取两个查询条件的交集  语法:   +name:apache +price:[50.0 TO 70.0]
        // MUST  MUST_NOT  必须满足条件1,必须不满足条件2          语法:   +name:apache -price:[50.0 TO 70.0]
        // MUST  SHOULD  必须满足条件1                         语法:    +name:apache price:[50.0 TO 70.0]
        // SHOULD  SHOULD   两个条件的并集                      语法:    name:apache price:[50.0 TO 70.0]
        // MUST_NOT  MUST_NOT   没有任何意义,得到的结果永远是null  语法:   -name:apache -price:[50.0 TO 70.0]
        // SHOULD  MUST_NOT    等价于MUST  MUST_NOT            语法:    name:apache -price:[50.0 TO 70.0]
        booleanQuery.add(rangeQuery, BooleanClause.Occur.MUST);
        booleanQuery.add(termQuery, BooleanClause.Occur.MUST);
        TopDocs topDocs = indexSearcher.search(booleanQuery, 100);
        System.out.println(topDocs.totalHits);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            Document doc = indexSearcher.doc(scoreDoc.doc);
            System.out.println("id:"+doc.get("id")+",name:"+doc.get("name")+",price:"+doc.get("price"));
        }
        reader.close();
    }

    @Test  //把输入的内容进行分词之后查询
    public void queryIndexAllAndIkName() throws Exception {
        FSDirectory directory = FSDirectory.open(new File("D:\\WeGame\\index"));
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher=new IndexSearcher(reader);
        // 参数1 : 要搜索的域
        // 参数2 : 分词器.搜索的分词器一定要和创建索引的时候使用的分词器保持一致
        QueryParser queryParser = new QueryParser("name", new IKAnalyzer());
        // 对查询条件进行分词
        Query query = queryParser.parse("lucene is a java programme");
        //查看query的查询语法
        System.out.println(query);
        TopDocs topDocs = indexSearcher.search(query, 100);
        //查到的数据条数
        System.out.println(topDocs.totalHits);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            Document doc = indexSearcher.doc(scoreDoc.doc);
            System.out.println("id:"+doc.get("id")+",name:"+doc.get("name")+",price:"+doc.get("price"));
        }
        reader.close();
    }

    @Test  //从多个域查询包含java的数据
    public void queryIndexAllAndMuch() throws Exception {
        FSDirectory directory = FSDirectory.open(new File("D:\\WeGame\\index"));
        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher=new IndexSearcher(reader);
        //2个域,name和desc
        String[] fields = new String[]{"name", "desc"};
        // 参数1 : 要搜索的域
        // 参数2 : 分词器.搜索的分词器一定要和创建索引的时候使用的分词器保持一致
        QueryParser queryParser = new MultiFieldQueryParser(fields, new IKAnalyzer());
        // 查询条件
        Query query = queryParser.parse("java");
        //查看query的查询语法
        System.out.println(query);
        TopDocs topDocs = indexSearcher.search(query, 100);
        //查到的数据条数
        System.out.println(topDocs.totalHits);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        for (ScoreDoc scoreDoc : scoreDocs) {
            Document doc = indexSearcher.doc(scoreDoc.doc);
            System.out.println("id:"+doc.get("id")+",name:"+doc.get("name")+",price:"+doc.get("price"));
        }
        reader.close();
    }

}
