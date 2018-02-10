## OverView

fade服务器后台:<br>
Fade是一款含有时间元素的轻博客社交应用，每一条帖子有剩余时间限制，当剩余时间为零时，帖子将从信息流中消失，用户可对内容进行续一秒或减一秒的操作。目前该应用的平台包括了安卓以及微信小程序，已在本校学生群体中推广试用。


## Requirements

* tomcat服务器
* eclipse
* mysql数据库
* redis数据库
* solr搜索服务器

## Details
* 采用Spring MVC+Spring+Mybatis、Restful风格、tomcat+Mysql+redis搭建服务器端。实现用户登录注册、用户发帖、对帖子“续秒/减秒”操作、帖子分页加载、剩余时间倒计时等基础功能；
* Redis结合Spring的拦截器制定了token验证机制，只有header携带有token信息的请求才能通过拦截器，提高服务器的安全性；
* Websocket使得服务器可以主动将消息通知发送给前端用户。
* 采用Solr作为搜索服务器，IkAnalyzer作为中文分词器，通过关键字可以快速检索用户以及帖子。
* 大量使用Redis作为缓存，减小磁盘IO压力
* 定时收集用户行为偏好信息，更新到Preference表中，作为mahout基于用户的协同过滤推荐算法的数据集。


## Screenshot

* 首页<br/>
![img](https://github.com/huanglu20124/ImgRespository/blob/master/fade/首页.jpg?raw=true)

* 发布页<br/>
![img](https://github.com/huanglu20124/ImgRespository/blob/master/fade/发布页.jpg?raw=true)

* 详情页<br/>
![img](https://github.com/huanglu20124/ImgRespository/blob/master/fade/详情页.jpg?raw=true)

* 个人页<br/>
![img](https://github.com/huanglu20124/ImgRespository/blob/master/fade/个人页.jpg?raw=true)

## Author

| Author | E-mail |
| :------:  | :------: |
| huanglu | 845758437@qq.com |
