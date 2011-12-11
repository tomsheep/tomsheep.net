Title: PyV8
Author: Kang Liu
Date: Sun Aug 07 2011 11:03:00 GMT+0800 (China Standard Time)
Lang: C++, Python

v8是Chromium中内嵌的javascript引擎，号称跑的最快，拿着天文望远镜也找不着对手。它为应用程序员提供了接口，可以方便地将v8嵌入到自己的程序中，订制出自己的应用，使native程序和javascript互动，当下炙手可热的nodejs就是一个比较成功的应用。

## Boost.Python
v8的文档非常稀缺，即便暴露给应用程序员用的API也几乎没有文档可循，v8.h中的注释大概是最珍贵的参考资料了。可能Google的工程师们认为，你丫都看不懂源代码，还玩什么v8……这严重伤害了像我这样的低智商码农的感情。不过还好，开源社区总会有一批蛋疼的人，喜欢把各种框架port到各种平台，胶水层上再加胶水，支持各种语言。看看他们写的框架，也算是曲线救国的一种。

贵python界一向格外蛋疼，搜索之下，果然如此，pyv8就是这样的框架，利用v8引擎让python可以直接与javascript互操作。pyv8其实是一个很薄的胶水层，绝大多数的工作只是把v8的外部API包装了一个python壳，使用了Boost.Python中提供的机制，将C++模块导出为Python模块。Boost.Python的使用方法大致如下：

<pyv8-playground/py-boost.cpp>

将这段程序编译为动态链接库，以windows的dll为例，将编译好的dll放入python include路径下（有很多方法，比较方便的是扔到/path_to_python/Lib/site-packages里），把后缀改成pyd，因为python不认dll。然后就可以在python中import这个叫做boostpy的模块了。更多用法见这里[boostpy-usage][]

## PyV8胶水层 

刚才说pyv8其实就是一个wrapper，把v8中暴露给C++的数据结构与API通过Boost.Python这种机制wrap一下再暴露给了python。当然它做了很多语法糖的东西，让python用户用起来更顺手。pyv8的源码很少，分两部分，native部分向python导出了\_PyV8模块，把v8中的Context（JS运行环境）、Isolate（v8实例）、Function（JS函数）、Script（编译后的js source）等等数据结构wrap一层导出；而PyV8.py这个文件在_PyV8的基础在再wrap一遍，变为方便python程序员使用的数据结构，成为最终的PyV8模块。

使用起来很方便，官方给的一个简单的例子：

<pyv8-playground/v8test.py>

这里JSContext就是v8::Context的一个wrapper，代表一个js运行环境，这里传入了一个JSClass对象给JSContext的构造函数，其实就是代表一个js运行环境中的“全局对象”（还记得当年大明湖畔的window对象吗？）JSClass定义在PyV8.py里，并非是v8数据结构的映射，那是如何注册到v8的Context中去的呢？奥妙在此：

<pyv8-playground/pyv8-glue.cpp>

最后一行就是答案，这里m\_context是一个v8::Context的handle，这一句先把global也就是传入的JSClass对象（注意这是python object）包装成v8的Object，然后注入到m\_context的global对象中去，作为`__proto__`属性，这是javascript对象的隐式继承链，关于js的prototype机制就不多做解释了。

## 编译PyV8

编译pyv8需要一定的耐心，不过作为一个苦逼的码农，这点折腾问题不大。

1. 使用的编译环境：  
    * python（我使用2.7.1）
    * scons
    * VS2008

2. 编译boost  
如果没有Boost环境，那首先需要编译boost。源码载到后，按照官网的指示[boost-build][]，boost其实分为很多模块，可以分开编译，这里只用到了boost的智能指针和python模块，选择性编译就行了

        bjam --build-type=complete --with=python --toolset=msvc-9.0

这里用的是vs2008环境编译的，complete是指build出静态链接、静态链接多线程、动态链接、动态链接多线程等多个版本

3. 编译v8。
    * 载到v8的源码： 
        svn checkout http://v8.googlecode.com/svn/trunk/ v8 
    * 不要急着编译，pyv8有一个已知的issue，需要配置v8，打开Exception支持，我不知道不打开会是什么效果，但既然人家这么要求了，本着尽量少折腾自己的原则，还是照做为好。参考这里[v8-build][]

* 用scons编译v8

        scons mode=debug library=shared

    shared表示编译为动态链接库，编译为静态链接库也行，但是体积庞大，外部程序link起来很费时

4.编译pyv8

* 首先要配置一些环境变量，使得编译连接时找得到对应的库，修改setup.py文件，顶部的一些变量换成自己的对应真实路径，包括python、v8、boost等等

* 开始build

        python setup.py build

开始的时候会check你的源码是否是最新版的，需要pysvn支持，但坑爹的是一直打不开这个项目的主页，干脆把这一段注释掉好了。

* 安装

        python setup.py install 

    会使用easy\_install自动打成egg包扔到site-packages目录中去，下面就可以使用pyv8了。

源码中附带了一些demos，读起来方便很多，因为pyv8是对v8的封装，看python而知C++，也真是寂寞的蛋疼。

[boostpy-usage]: http://www.python-cn.cn/yuyanjichu/2011/0707/14106.html
[boost-build]: http://www.boost.org/doc/libs/1_47_0/more/getting_started/windows.html
[v8-build]: http://code.google.com/p/pyv8/wiki/HowToBuild