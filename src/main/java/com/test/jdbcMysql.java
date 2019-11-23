package com.test;

import com.pojo.Book;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class jdbcMysql {
    @Test
    public static List<Book> findBookList(){
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Book> bookList=new ArrayList<>();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            String url="jdbc:mysql://192.168.214.128:3306/lucene?characterEncoding=utf-8";
            String username="root";
            String password="123";
            conn = DriverManager.getConnection(url, username, password);
            String sql="select *  from book";
             ps = conn.prepareStatement(sql);
             rs = ps.executeQuery();
             while (rs.next()){
                 Book book=new Book();
                 book.setId( rs.getInt("id"));
                 book.setName(rs.getString("name"));
                 book.setPrice(rs.getFloat("price"));
                 book.setPic(rs.getString("pic"));
                 book.setDescription(rs.getString("description"));
                 bookList.add(book);
             }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                conn.close();
                ps.close();
                rs.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bookList;
    }

}
