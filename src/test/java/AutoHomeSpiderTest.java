import cn.jeneral.entity.CarCategory;
import cn.jeneral.util.JsoupUtils;
import cn.jeneral.util.SpiderRule;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.testng.annotations.Test;

import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
    }

    @Test
    public void autoHomeTest(){

        List<CarCategory> carCategoryList = new ArrayList<>();
        for (String eng:ENG){
            String url = seriesUrl + eng + ".html";
            SpiderRule rule = new SpiderRule(url, null, null, null, SpiderRule.GET);
            Document doc = JsoupUtils.extract(rule);
            //品牌
            Elements brandEls = doc.select("dl");
            for (Element brandEle : brandEls){
                CarCategory carCategory = new CarCategory();
                String brand = brandEle.select("dt div").select("a").text();
                String brandIdStr = brandEle.attr("id");
                Long brandId = Long.valueOf(brandIdStr);
                carCategory.setBrand(brand);
                carCategory.setId(brandId);
                carCategory.setLevel(1);
                carCategory.setFirstLetter(eng);

                carCategoryList.add(carCategory);

                Elements manEls = brandEle.select(".h3-tit");
                Elements m_manEls = brandEle.select(".rank-list-ul");
                for (int i = 0; i < m_manEls.size(); i++){

                    CarCategory seriesTemplate = new CarCategory();
                    seriesTemplate.setBrand(brand);
                    seriesTemplate.setParentId(brandId);
                    seriesTemplate.setLevel(2);
                    seriesTemplate.setFirstLetter(eng);

                    String manufacturer = manEls.get(i).text();

                    seriesTemplate.setCarManufacturer(manufacturer);

                    Elements seriesEls = m_manEls.get(i).select("h4");
                    for (int j = 0; j < seriesEls.size(); j++){

                        CarCategory carCategory2 = seriesTemplate;

                        String seriesUrl = "https:" + seriesEls.get(j).select("a").attr("href");
                        String series = seriesEls.get(j).select("a").text();
                        String seriesIdStr = seriesUrl.substring(28,seriesUrl.indexOf("/#",1));

                        Long seriesId = Long.valueOf(seriesIdStr);

                        if(seriesUrl == null || manufacturer == null || seriesId == null){
                            System.out.println("==>" + seriesUrl + "  :  " + manufacturer);
                            continue;
                        }

                        carCategory2.setId(seriesId);
                        carCategory2.setSeries(series);
                        carCategory2.setUrl(seriesUrl);

                        carCategoryList.add(carCategory2);
                    }
                }
            }
            System.out.println(carCategoryList);
        }
    }
}
