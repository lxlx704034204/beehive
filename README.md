
##介绍
* beehive是一个JavaEE企业级项目的快速开发的脚手架，提供了底层抽象和通用功能，拿来即用。

### 核心功能
* 通用的DAO、Service、Controller抽象，从CRUD中解放
* ……

### 技术选型

#### 管理
* gradle依赖和项目管理
* git版本控制

#### 后端
* IoC容器 spring
* web框架 springmvc
* servlet 3.0(需要支持servlet3的servlet容器，如tomcat7)

#### 数据库
 * 目前只支持mysql，建议mysql5.5及以上

####
 * 本脚手架会选型技术的最新版本

###支持的浏览器
 * chrome
 * firefox（目前使用firefox 19.0.2测试）
 * opera 12
 * ie7及以上（建议ie9以上，获取更好的体验）
 * 其他浏览器暂时未测试


##如何运行

####1、到es/web/pom.xml修改数据库配置：
*  默认修改：profiles/profile/development下的
*  connection.admin.url
*  connection.username
*  connection.password

####2、到项目的根下(sparrow)
* cd bin
* install.bat 安装jar包到本地仓库（jdk6即可）
* create-db.bat 创建数据库（mysql需要5.5及以上 编码为utf-8）
* refresh-db.bat 创建schema和初始化data
* jetty.bat 启动web应用 默认端口9080 可以到es/web/pom.xml下修改（servlet 2.5即可）
* 系统默认帐户是admin/123456

####3、注意
* 
* 
* 
