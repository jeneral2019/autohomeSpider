package cn.jeneral.util;

import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;

public class MysqlConnect {

    public SqlSession session;
    final String resource = "mybatis-config.xml";



    public MysqlConnect() throws IOException {
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        session = sqlSessionFactory.openSession();
    }

    public <T> T getMapper(Class<T> TClass){
        T TDao = (T) session.getMapper(TClass);
        return  TDao;
    }

    public void commit(){
        session.commit();
    }

}
