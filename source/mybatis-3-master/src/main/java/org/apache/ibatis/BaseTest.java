package org.apache.ibatis;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author cristina
 */
public class BaseTest {

  public void test05() {
    try {
      // 读取配置文件
      InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml");
      // 1.SqlSessionFactory 的初始化
      SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
      // 2.获取 SqlSession 对象
      SqlSession sqlSession = sqlSessionFactory.openSession();
      // 3.获取接口的代理对象
      Object mapper = sqlSession.getMapper(Object.class);
      // 4. 查询实现,，MapperProxy 会代理拦截
//      User user = mapper.selectById(1);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
