package cn.jeneral.autohome;

import cn.jeneral.dao.AutoHomeDao;
import cn.jeneral.entity.CarCategory;
import cn.jeneral.util.JsoupUtils;
import cn.jeneral.util.MysqlConnect;
import cn.jeneral.util.SpiderRule;
import dynamicwait.DWChromeDriver;
import dynamicwait.DWWebDriver;
import dynamicwait.DWWebElement;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author yuxiangfeng
 */
public class AutoHomeSpider {

    final String seriesUrl = "https://www.autohome.com.cn/grade/carhtml/";
    final String[] ENG = {"A","B","C","D","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","V","W","X","Y","Z"};

    private DWWebDriver driver = null;
    private MysqlConnect connect;
    private AutoHomeDao autoHomeDao;

    public AutoHomeSpider() throws IOException{
        connect = new MysqlConnect();
        autoHomeDao = connect.getMapper(AutoHomeDao.class);
    }

    public void autoHomeSpider(){
        getLevel1And2();
        getLevel3();
    }

    public void getLevel1And2(){

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
                carCategory.setAutohomeId(brandId);
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

                        carCategory2.setAutohomeId(seriesId);
                        carCategory2.setSeries(series);
                        carCategory2.setUrl(seriesUrl);

                        carCategoryList.add(carCategory2);
                    }
                }
            }
            autoHomeDao.batchInsert(carCategoryList);
            connect.commit();
        }

    }

    private List<CarCategory> carCategoryList3 = new ArrayList<>();
    public void getLevel3(){

        while (true){
            List<CarCategory> carCategoryList2 = autoHomeDao.selectByLevelAndStatus(2,0);
            if (carCategoryList2.isEmpty()){
                break;
            }

            for (CarCategory carCategory:carCategoryList2){
                carCategoryList3.clear();
                autoHomeDao.modifyStatus(carCategory.getId(),1);
                String url = carCategory.getUrl();
                SpiderRule rule = new SpiderRule(url, null, null, null, SpiderRule.GET);
                Document doc = JsoupUtils.extract(rule);

                carCategory.setCarType(doc.select(".type__item").text());

                if (doc.select(".series-list").size() > 0 ){
                    getLevel3InNormal(carCategory);
                }else if (doc.select(".subnav").size() > 0 ){
                    getLevel3InSpecial(carCategory,doc);
                }else {
                    autoHomeDao.modifyStatus(carCategory.getId(),500);
                }

            }


        }

    }
    public void getLevel3InNormal(CarCategory carCategory){

        if (driver == null){
            driver = new DWChromeDriver();
        }
        driver.get(carCategory.getUrl());

        try {
            List<DWWebElement> listStatusEle = driver.findVisElements(By.cssSelector(".list-stats>li"));
            if (listStatusEle.size() == 0){
                autoHomeDao.modifyStatus(carCategory.getId(),20);
                return;
            }

            for (DWWebElement liELe: listStatusEle){
                //停售款
                if (liELe.getAttribute("class").equals("more-dropdown")){
                    List<DWWebElement> moreListEle = liELe.findElements(By.cssSelector("#haltList>li>a"));
                    for (DWWebElement moreLiEle:moreListEle){
                        Boolean a = moreLiEle.clickForce(By.cssSelector("#specWrap-3>.halt-spec")) != null;
                        String year = moreLiEle.getAttribute("innerHTML");
                        getCarCategory("#specWrap-3",carCategory);
                        year = null;
                    }
                    continue;
                }
                //预售、在售
                String id = liELe.findElement(By.tagName("a")).getAttribute("data-target");
                Boolean a = liELe.clickForce();
                Boolean b = driver.waitEle(By.cssSelector(id+".active"),5L);
                getCarCategory(id,carCategory);
            }

        }catch (Exception e){
            autoHomeDao.modifyStatus(carCategory.getId(),3);
            return;
        }
    }

    final Long quickTime = 5L;
    private void getCarCategory(String id,CarCategory parentCarCategory){

        if (!driver.waitEle(By.cssSelector(id+">dl"),quickTime)) {
            System.out.println(id + "is empty");
            return;
        }

        CarCategory carCategory3 = new CarCategory();
        carCategory3.setLevel(3);
        carCategory3.setParentId(parentCarCategory.getAutohomeId());
        carCategory3.setFirstLetter(parentCarCategory.getFirstLetter());
        carCategory3.setBrand(parentCarCategory.getBrand());
        carCategory3.setSeries(parentCarCategory.getSeries());
        carCategory3.setCarType(parentCarCategory.getCarType());

        DWWebElement specWrap = driver.findElement(By.cssSelector("id"));
        List<DWWebElement> dlEleList = specWrap.findElements(By.tagName("dl"));
        for (DWWebElement dlEle:dlEleList){
            DWWebElement dtEle = dlEle.findElement(By.tagName("dt"));
            DWWebElement engineEle = dtEle.findElement(By.cssSelector("spec-name"));
            String engine = engineEle.getText();
            for (DWWebElement ddEle:dlEle.findElements(By.tagName("dd"))){
                if (id.equals("#specWrap-2")){
                    carCategory3.setYear(ddEle.getAttribute("data-sift1"));
                }else if (id.equals("#specWrap-1")){
                    carCategory3.setYear("-1");
                }
                try {
                    DWWebElement nameEle = ddEle.findElement(By.className("name"));
                    getNameAndSpecId(nameEle,carCategory3);
                }catch (Exception e){
                    System.out.println("nameEle find error"+e.toString());
                    System.out.println("set name = 名字未找到 , specId = -");
                    carCategory3.setName("名字未找到");
                    carCategory3.setSpecId(-1);
                }
                try {
                    carCategory3.setPrecursor(ddEle.findElement(By.className("type-default")).getText());
                }catch (Exception e){
                    System.out.println("set precursor=未知");
                    carCategory3.setPrecursor("未知");
                }

                try {
                    carCategory3.setGear(ddEle.findElement(By.className("type-default"),1).getText());
                }catch (Exception e){
                    System.out.println("set gear=未知");
                    carCategory3.setGear("未知");
                }


                carCategoryList3.add(carCategory3);
            }
        }
    }

    private void getNameAndSpecId(DWWebElement nameEle,CarCategory carCategory3){
        try{
            carCategory3.setName(nameEle.getText());
        }catch (Exception e){
            System.out.println("set name = 名字未找到");
            carCategory3.setName("名字未找到");
        }
        try {
            //获取url https://www.autohome.com.cn/spec/45512/#pvareaid=3454492
            String urlStr = nameEle.getAttribute("href");
            URL url = new URL(urlStr);
            //获取fileStr 如 /spec/45512/
            String fileStr = url.getFile();
            //获取specIdStr 45512
            String specIdStr = fileStr.substring(fileStr.indexOf("/",1)+1,fileStr.length()-1);
            carCategory3.setSpecId(Integer.valueOf(specIdStr));
        }catch (Exception e){
            System.out.println("set specId = -1");
            carCategory3.setSpecId(-1);
        }
    }

    public void getLevel3InSpecial(CarCategory parentCarCategory,Document doc){

        CarCategory carCategory3 = new CarCategory();
        carCategory3.setParentId(parentCarCategory.getAutohomeId());
        carCategory3.setLevel(3);
        carCategory3.setFirstLetter(parentCarCategory.getFirstLetter());
        carCategory3.setBrand(parentCarCategory.getBrand());
        carCategory3.setSeries(parentCarCategory.getSeries());

        Elements carDetails = doc.select(".car_detail");

        for (Element carDetail:carDetails){
            carCategory3.setYear(carDetail.select(".years").text());
            Elements nameEls = carDetail.select(".name_d .name");
            for (Element nameEle :nameEls){
                carCategory3.setName(nameEle.text());
                String href = nameEle.select("a").attr("href");
                String specIdStr = href.substring(href.indexOf("/",1)+1,href.length()-1);
                carCategory3.setSpecId(Integer.valueOf(specIdStr));
                carCategoryList3.add(carCategory3);
            }

        }

        if (!carCategoryList3.isEmpty()){
            autoHomeDao.batchInsert(carCategoryList3);
            autoHomeDao.modifyStatus(parentCarCategory.getAutohomeId(),2);
        }else {
            autoHomeDao.modifyStatus(parentCarCategory.getAutohomeId(),20);
        }
        carCategoryList3.clear();
    }

}
