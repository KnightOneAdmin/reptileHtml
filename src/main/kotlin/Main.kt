import cn.hutool.core.date.DateUtil
import com.alibaba.excel.EasyExcel
import entity.DocumentData
import entity.JianShuEntity
import entity.PremiumSiteEntity

import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import utils.FileUtils
import kotlin.collections.ArrayList
import kotlin.concurrent.thread

fun main() {
//    for (i in 0 until 10) {
//        val url = String.format(BASE_URL, i * 25)
//        try {
//            parsePageList(url)
//        } catch (e: Exception) {
//            println(e.printStackTrace())
//        }
//    }
    thread {
//        parseJianShuPage("https://www.jianshu.com/")
        parse549Page("https://549.tv/")
    }
}


fun getHtmlIds(): ArrayList<String> {
    var htmlIds = ArrayList<String>()
    for (index in 27..40) {
        htmlIds.add("term-$index")
    }
    return htmlIds
}


/**
 * 日期、数字或者自定义格式转换
 * <p>
 * 1. 创建excel对应的实体对象 参照{@link ConverterData}
 * <p>
 * 2. 使用{@link ExcelProperty}配合使用注解{@link DateTimeFormat}、{@link NumberFormat}或者自定义注解
 * <p>
 * 3. 直接写即可
 */
fun converterWrite(listData: ArrayList<DocumentData>) {
    val fileName = FileUtils.getPath() + "converterWrite" + ".xlsx"
    println("fileName:$fileName")
    EasyExcel.write(fileName, DocumentData::class.java).sheet("模版").doWrite(listData)
}

fun buildData(title: String, url: String): DocumentData {
    var documentData = DocumentData()
    documentData.title = title
    documentData.url = url
    documentData.date = DateUtil.date()
    return documentData
}


fun buildJianShuData(title: String, url: String, nickName: String, content: String, likeNum: String): JianShuEntity {
    var documentData = JianShuEntity()
    documentData.title = title
    documentData.url = url
    documentData.nickName = nickName
    documentData.likeNum = likeNum
    documentData.content = content
    documentData.date = DateUtil.date()
    return documentData
}


/**
 * 获取界面数据
 */
fun parsePageList(url: String) {
    var connect: Connection = Jsoup.connect(url).timeout(5000)
        .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36")
        .method(Connection.Method.GET)
    val document: Document = connect.get()
    val elements = document.select("div.item")
    var listData = arrayListOf<DocumentData>()
    elements.forEach {
        val title: String = it.select("span.title").first()?.text().toString()
        val url: String = it.select("div.hd > a").first()?.attr("href").toString()
        val images: String = it.select("div.pic.a > img").first()?.attr("src").toString()
//       val images =  it.getElementsByTag("div.pic.a").select("img").first()?.attr("src").toString()
        println("title:$title  url:$url" + "  images:$images")
        listData.add(buildData(title, url))
    }
    converterWrite(listData)
}


/***
 * 简书主页数据
 */
fun parseJianShuPage(url: String) {
    var connect: Connection = Jsoup.connect(url).timeout(5000)
        .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36")
        .method(Connection.Method.GET)
    val document: Document = connect.get()
    val elements = document.select("div.content")
    var listData = arrayListOf<JianShuEntity>()
    elements.forEach {
        val title = it.select("a.title").first()?.text().toString()
        val decUrl = url + it.select("a.title").first()?.attr("href").toString()
        val content = it.select("p.abstract").first()?.text().toString()
        val nickname = it.select("div.meta > a").first()?.text().toString()
        val lickNum = it.select("div.meta > span")[1]?.text().toString()
        listData.add(buildJianShuData(title, decUrl, nickname, content, lickNum))
    }
    converterJianShuWrite(listData)
}


/**
 *
 */
fun parse549Page(url: String) {
    var connect: Connection = Jsoup.connect(url).timeout(5000)
        .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36")
        .method(Connection.Method.GET)
    val document: Document = connect.get()
    val element = document.getElementById("content")
    getPremiumSiteData(element)
}

var typeTitle: String = ""
var title: String = ""
var subTitle: String = ""
var url: String = ""
var logoUrl: String = ""
var markers: String = ""

/**
 *  获取优质站点数据
 */
fun getPremiumSiteData(element: Element?) {
    var listData = arrayListOf<PremiumSiteEntity>()
    getHtmlIds().forEach { _ids ->
        val elements = element?.getElementById(_ids)
        typeTitle = elements?.select("h3")?.text().toString()
        val elements1 = elements?.select("li.url-card")
        elements1?.forEach { it ->
            title = it.getElementsByClass("url-info flex-fill").select("h5").text().toString()
            subTitle = it.getElementsByClass("url-info flex-fill").select("p")[1].text().toString()
            url = it.getElementsByClass("url-body default").select("a").attr("href").toString()
            logoUrl =
                it.getElementsByClass("url-img rounded-circle me-3 d-flex align-items-center justify-content-center")
                    .select("img")?.attr("data-src").toString()
            val elementsByClass = it.getElementsByClass("my-2")
            elementsByClass.forEach { it2 ->
                markers = "${it2.select("span").text()}"
            }
            listData.add(buildPremiumSiteData(typeTitle, title, subTitle, url, logoUrl, markers))
        }
    }
    converterSiteWrite(listData)
}

fun buildPremiumSiteData(
    typeTitle: String,
    title: String,
    subTitle: String,
    url: String,
    logoUrl: String,
    markers: String,
): PremiumSiteEntity {
    var documentData = PremiumSiteEntity()
    documentData.typeTitle = typeTitle
    documentData.title = title
    documentData.subTitle = subTitle
    documentData.siteUrl = url
    documentData.logoUrl = logoUrl
    documentData.markers = markers
    documentData.date = DateUtil.date()
    return documentData
}


/**
 * 日期、数字或者自定义格式转换
 * <p>
 * 1. 创建excel对应的实体对象 参照{@link ConverterData}
 * <p>
 * 2. 使用{@link ExcelProperty}配合使用注解{@link DateTimeFormat}、{@link NumberFormat}或者自定义注解
 * <p>
 * 3. 直接写即可
 */
fun converterJianShuWrite(listData: ArrayList<JianShuEntity>) {
    val path = "converterJianShuWrite" + ".xlsx"
    val fileName = FileUtils.createNewFile(path)
    println("fileName:$fileName")
    EasyExcel.write(fileName, JianShuEntity::class.java).sheet("549").doWrite(listData)
}


fun converterSiteWrite(listData: ArrayList<PremiumSiteEntity>) {
    val path = "converterSiteWrite" + ".xlsx"
    val fileName = FileUtils.createNewFile(path)
    println("fileName:$fileName")
    EasyExcel.write(fileName, PremiumSiteEntity::class.java).sheet("549").doWrite(listData)
}


