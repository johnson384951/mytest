package com.pinyougou.item.listener;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.service.GoodsService;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

/**
 * 消息监听器(生成商品的静态页面)
 *
 * @author lee.siu.wah
 * @version 1.0
 * <p>File Created at 2019-01-13<p>
 */
public class PageMessageListener implements SessionAwareMessageListener<TextMessage> {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Reference(timeout = 10000)
    private GoodsService goodsService;
    // 生成静态存储的目录
    @Value("${pageDir}")
    private String pageDir;

    @Override
    public void onMessage(TextMessage textMessage, Session session) throws JMSException {
        try{
            System.out.println("========PageMessageListener========");
            // 获取消息内容
            String goodsId = textMessage.getText();
            System.out.println("goodsId: " + goodsId);

            // 1. 生成商品的静态页面(html) 把item.ftl 动态页 生成 xxx.html
            // 1.1 获取item.ftl页面对应的 模板对象
            Template template = freeMarkerConfigurer
                    .getConfiguration().getTemplate("/item.ftl");

            // 1.2 创建模板文件需要的数据模型
            Map<String,Object> dataModel = goodsService.getGoods(Long.valueOf(goodsId));

            // 1.3 用数据模型填充模板页面 生成静态的页面
            OutputStreamWriter writer = new OutputStreamWriter(new
                    FileOutputStream(pageDir + goodsId + ".html"),"UTF-8");
            template.process(dataModel, writer);
            writer.close();

        }catch (Exception ex){
            throw new RuntimeException(ex);
        }
    }
}
