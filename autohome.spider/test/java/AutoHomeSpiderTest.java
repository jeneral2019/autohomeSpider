import cn.jeneral.autohome.AutoHomeSpider;
import cn.jeneral.dao.AutoHomeDao;
import cn.jeneral.entity.CarCategory;
import cn.jeneral.util.JsoupUtils;
import cn.jeneral.util.MysqlConnect;
import cn.jeneral.util.SpiderRule;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.testng.annotations.Test;

import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class AutoHomeSpiderTest {

    final String seriesUrl = "https://www.autohome.com.cn/grade/carhtml/";
    final String[] ENG = {"A","B","C","D","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","V","W","X","Y","Z"};

    @Test
    public void spiderTest(){
        String url = "https://www.baidu.com/";
        SpiderRule rule = new SpiderRule(url, null, null, null, SpiderRule.GET);
        Document doc = JsoupUtils.extract(rule);
        System.out.println(doc.toString());
    }

    @Test
    public void mybatisTest() throws IOException {
        String resource = "mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        try (SqlSession session = sqlSessionFactory.openSession()) {
            AutoHomeDao autoHomeDao = session.getMapper(AutoHomeDao.class);
            List<CarCategory> carCategoryList = autoHomeDao.selectById(1L);
            System.out.println(carCategoryList.toString());
        }

    }

    @Test
    public void connectTest() throws IOException {
        AutoHomeDao autoHomeDao = new MysqlConnect().getMapper(AutoHomeDao.class);
        List<CarCategory> carCategoryList = autoHomeDao.selectById(1L);
        System.out.println(carCategoryList.toString());
    }

    @Test
    public void autoHomeTest() throws IOException {
        AutoHomeSpider autoHomeSpider = new AutoHomeSpider();
        autoHomeSpider.autoHomeSpider();
    }

}
