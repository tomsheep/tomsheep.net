Title: 我所知道的Server Push
Author: Kang Liu
Date: Fri May 27 2011 03:55:00 GMT+0800 (China Standard Time)
Lang: HTML5, JS

由于大一时web课上得实在纠结，对于web技术一直存在抗拒心理，导致常年处于白痴状态，半年前去SAP实习算是为我二次启蒙，让我发觉这个领域相当广阔大有可为实在是知识青年上山下乡接受再教育的不二之选。最近几天正好接触到服务器“推”，总结一下相关技术：

## 无连接的HTTP

HTTP协议是整个Web的基础，它是一个无连接协议，基于请求/响应模型，不具备类似TCP一样的全双工能力，为了实现服务器端向客户端实时推送数据，必须采取一些hacks，或避开HTTP，或建立在HTTP之上。前者主要依靠富客户端技术如Flash、Java Applet、Silverlight等提供的socket机制，通过浏览器插件给予支持。这种方式实现简单、较为可靠，但削弱了web应用的灵活性，而且可能会陷入跨平台的窘境，下图阐述了这个深刻的道理：

![悲剧的闪电侠](/server-push/flash.jpg)  


所以，基于原生HTTP协议的解决方案更为靠谱。HTTP1.1版本中增加了“HTTP长连接”，利用同一个tcp连接处理多个http请求和响应，也叫HTTP Keep-alive, 为push技术的高效实现提供了重要的底层原语。

轮询是一种简单粗暴的解决方案，客户端在一定的频率下向服务器发送请求，询问服务器“是不是想给我推点啥”，高频率的轮询会造成大量的无用traffic，而低频率的轮询又往往有数据延时之虞。严格地讲，传统的轮询并不能归入“推”的行列。

## Comet

十月革命一声炮响，给我们送来了Comet技术。（我比较讨厌这些一眼看上去看不出究竟是什么的装逼名词，彗星，彗星你妹啊）和Ajax一样，Comet其实是一个umbrella term，是一篮子技术的统称，提供低时延的服务器到客户端浏览器的数据传送。现有的Comet技术可以笼统地分为两大类，一类是基于长轮询的，比如XHR loog polling和Script tag long polling（CometP）。另一类是流式的，主要有XHR Streaming，forever frame等。绝大多数的Comet技术依赖于long-lived HTTP连接，需要服务器的特殊实现，因此造成了技术普及推广的小小障碍。下面介绍几种主流的Comet技术：

1. Forever Frame  
在页面加载之后，DOM中生成一个隐藏的iframe，通过其src与服务器建立持久连接。通常服务器向浏览器推送一系列脚本，通常是一些callback functions的调用，在浏览器中运行，服务器与浏览器通过HTTP1.1的chunked encoding机制（服务器可以在不知道整个response有多长的情况下就开始会送响应，响应数据呈chunk序列，包在不闭合的html文档中）进行数据传送，浏览器接收后对渐进地对chunk数据进行处理。经过观察人人网的在线聊天工具采用的就是这种技术。

2. XHR long polling  
和传统的轮询不同，长轮询并不以一定的频率向服务器发送询问请求，而是采取这样的策略：客户端发起一个连接，服务器不马上返回，而是hold住这个连接，直到有数据要“推”给客户端时才发送响应；客户端收到响应之后马上再发起一个新的请求，如此反复。减少了传统轮询的无谓开销，只在服务器有话说时产生流量。通常长轮询服务器端会设置超时，在一段时间内连接双发均表示沉默则强制发回响应，以免产生大量的死链接。

3. CometP  
又叫做Script tag long-polling，名字起的这么没个性，一看就不是什么新鲜玩意儿。它是long polling和jsonp的结合，用来支持跨域（cross-domain）请求。浏览器的安全限制规定脚本只能和处在同一域的server进行交互，否则视为不安全的（这是不是地域歧视？），但这样的无差别攻击，在打击了一小撮不法分子的同时，严重伤害了淳朴善良的外地务工好青年们，合理的cross-domain请求应该被允许。为了达到这个目的，苦逼的web工程师们想出了各种蛋疼的方法，比如找北京人结婚……啊，不是，jsonp就是其中之一。他的原理是，script标签的src是可以跨域的，那我们让服务器发回一个可运行的javascript对象，通常是对客户端已注册的回调函数的调用，就可以实现跨域了（进个城容易么）。而所谓的CometP，就是long polling + jsonp.

4. BOSH（Bidirectional-streams Over Synchronous HTTP）  
这是我毕设中涉及到的一个部分，BOSH是XMPP的一个拓展协议[XEP-0124][]，通过HTTP支持XMPP的全双工连接。我不确定它是不是可以归为Comet技术，因为Comet这个坑爹的名词含义太广边界模糊，而BOSH本质上有何主要的Comet技术很相似，所以放在这里也无妨。首先BOSH的架构是这样的：

<server-push/bosh.txt>

Connection Manager（CM）是Client和Server之间的桥梁。Client和CM通过HTTP交流，CM和Server通过各种各样可能的方式交流（有很多不同的实现），我们只关心Client和CM之间的HTTP连接技术。

BOSH同时支持传统轮询和长轮询模式（我想这就是所谓的degrade gracefully设计吧），因此可以很好的兼容受限的运行环境（比如mobile浏览器等），这一点来说一般的Comet技术就不容易做到，此外，与Comet不同，BOSH的CM返回完整的HTTP response，完美兼容缓存代理。（协议原文如此，不过按照我的理解，如果Comet是一个比较宽泛的、为了解决同一类问题而提出的技术概念，具体的实现有很多种，甚至BOSH都可以算所其中之一。也许我主观把Comet放大了？其实我说了我压根没摸准这个概念的边界）

其实，Comet现在仍然处于后娘养的状态，没有成为Web标准的一部分，因此推广起来有一定的阻力。眼看着Ajax登堂入室，自己这么多年还没捞着个名分，心里肯定着急啊。HTML5新加入了两个规范，可以原生支持服务器“推”，这就是Websocket和server-sent events：Websocket是一个支持在浏览器和服务器之间建立全双工连接的协议，据说目前各个浏览器支持的都不好，有空可以试试。Server-sent events被Comet拥趸们视为Comet的未来，它允许浏览器与服务器建立一个持久的连接，并且实时地从服务器接收“事件”，这些事件是和DOM API关联的哦亲。

## 总结
本文介绍了我所知道的主要server push技术：包括富客户端技术、传统轮询、Comet技术（XHR long polling、CometP、隐藏iframe等）、BOSH、以及还在发展的HTML5标准中Websocket和Server-sent events规范。

其实，说了这么多，不得感慨于这样一个这样的事实：标准才是web的王道，其余都是扯淡。什么server push，什么cross-domain，什么Comet，什么Ajax（这个已经扶正了），全是蹩脚的hacks。我等屁民，箪食壶浆以迎HTML5。

[XEP-0124]: http://xmpp.org/extensions/xep-0124.html