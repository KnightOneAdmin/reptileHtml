import cn.hutool.core.date.DateUtil
import com.alibaba.excel.EasyExcel
import entity.DocumentData
import entity.JianShuEntity

import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
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
    thread { parseJianShuPage("https://www.jianshu.com/") }
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
        val lickNum = it.select("div.meta > span").get(1)?.text().toString()
        listData.add(buildJianShuData(title, decUrl, nickname, content, lickNum))
    }
    converterJianShuWrite(listData)
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
    EasyExcel.write(fileName, JianShuEntity::class.java).sheet("简书").doWrite(listData)
}



