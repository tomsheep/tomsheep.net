Title: ABC of WebSocket
Author: Kang Liu
Date: Sun May 29 2011 01:11:00 GMT+0800 (China Standard Time)
Lang: JS, Python

今天去参加Shaanxi版聚，蛋疼的桥五。坐在车上翻了会html5的postmessage和websocket，前者我曾经接触过，可以用来实现窗口间的跨域通信。发觉Websocket还是个挺简单高效的协议，版聚回来后尝试上手试验了一下。Websocket是为了解决web环境下客户端和服务器端建立全双工连接而引入的规范，最新的协议版本号是76，在这里可以看到。

## Websocket协议

之所以简单是因为它基于tcp，基本上把tcp wrap一下就差不多了。建立一个websocket连接需要客户端和服务器端首先完成一轮握手，值得注意的是它的握手其实是通过HTTP协议进行的Upgrade的，“协商升级” 的方法在互联网协议中比较常见，因为在很多场合都需要考虑向后兼容性。在这里其实websocket完全可以不依靠HTTP，之所以这样设计主要是为了能让HTTP协议和WS协议方便地在同一个服务器上实现。下面看一个完整的Websocket握手（客户端我用的是Chrome12， Server是用python写的一段socket程序，等会介绍）：

Client->Server（request）：

<abc-of-websocket/request.txt>

Server->Client（response）:

<abc-of-websocket/response.txt>

可以看到这其实是一个采用了Upgrade机制的HTTP请求响应对，request中主要注意的是有三个随机的key值，头部有两个，后面body里是长度为8字节的key3（括号里的文字是提示，还有字符间的冒号也是为了看上去清晰才加上的，真正传输是没有的），以此向server发送一个challenge，server需要根据这三个key计算出一个token，在响应中发回给client，以证明自己对request的正常解读。计算方法是这样的：对于key1，抽取其中的数字字符，形成一个整数num，然后除以他自身的空格数spaces，保留整数部分i1; key2如法炮制，得到i2，把i1和i2按照big-endian字符序连接起来，然后再与key3连接，得到一个初始的序列，对这个序列使用md5计算出一个16字节长的摘要，就是所需的token。另外值得注意的是Origin头部，意味着Websocket是支持cross origin的（cross origin是html5中的统一提法，相当于跨域通信，这是个一致性和高的概念。ps：我们这里其实已经跨域了有木有）

在handshake完成后，两方就可以互相发送信息了，websocket的消息格式也很简单，用0×00和0xFF作为消息定界符，分别加在一条消息的头部和尾部。消息主体用utf8编码。

这几乎就是websocket协议最主要的内容了，javascript的api也很简单，主要关注onopen, onmessage, onclose这几个回调函数就行了，用代码实践一下：

## Client端（html+js）：

<abc-of-websocket/client.html>

描述一下大致的功能是：HTML包括一个log区域用以写入信息，script加载后创建一个WebSocket实例并注册回调函数，handshake如果正常完成，则会调用onopen，收到消息时会调用onmessage，连接关闭时调用onclose。

当然光有客户端是不够的，感情需要双方的共同培养。已有的server实现包括netty（java），mod_pywebsocket（apache模块）等。这里用python原生socket来实现一个简易的server

## Server端(python):

<abc-of-websocket/server.py>
 
应该没什么不好理解的，和C的socket API是完全对应的，最主要的功能就是按照协议完成一个handshake，然后把client发来的消息体加一个前缀“you just said”然后再发还给client。我用Chrome12和Firefox4做的测试，表现正常。其中Firefox4默认的websocket功能是关的，好像是发现具体实现有安全问题。在about:config里把network.websocket.enabled和network.websocket.override-security-block都设为true就可以用了。

## 总结 
本文结合一个简易的demo，简要介绍了Websocket协议的内容。