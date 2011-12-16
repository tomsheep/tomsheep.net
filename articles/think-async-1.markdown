Title: Think Async(1): 从一个简单需求开始
Author: Kang Liu
Date: Fri Dec 16 2011 22:30:00 GMT+0800 (China Standard Time)
Lang: JS

前两天QE找到我，请我帮助完成这样一个需求：将项目框架中基于Ajax请求的Javascript“跨页面调用”封装成同步形式，以便于写出“符合以往编程经验”的automation test。其中的核心在于将异步的“回调”转换为同步的函数调用。

## 背景
程序目前的架构类似于一个“承载多个Web页面的浏览器”，程序逻辑分散在各个页面中，使用Javascript实现。不同页面间的通讯由“浏览器”转发，表现形式为异步的消息通知。举例说明：如果有这样一个transaction，页面A向页面B发送消息“ping”，期待页面B对消息进行处理，完成后向A发送消息“pong”，页面A受到“pong”后执行另一段逻辑。实现起来是这样的： 

页面A：
    
    function step1() {
        // 注册事件 "pong"
        A.addListener("pong", step2);
        // 通知 B
        B.notify("ping", args);
    }
    
    function step2(result) {
        A.removeListener("pong", step2);
        // resume working flow...
        // result就是B发回的结果
    }
    // 触发整个流程
    step1();
    
页面B：

    // 注册事件 "ping"
    B.addListener("ping", processPing);
    
    function processPing(args) {
        // do some stuff...
        // 通知 A
        A.notify("pong", result);
    }

这样实现问题在于，程序逻辑被回调函数拆得四分五裂，对与automation test这样需要清晰执行流的应用场景来说，是难以接受的。QE比较喜欢的同步写法是这样的：

页面A:

    function flow() {
        // step 1
        var result = B.notify("ping", args);
        // step 2
        doSomethingWithResult(result);
    }
    
    flow();
    
页面B行为大致不变略去不写。这里的区别在于，step1中请求页面B并“同步”等待结果，然后恢复执行流。

## 尝试1： Fake sleep
一开始我没有太当回事，想当然认为按照C或者Java的编程经验，无非是一个“睡眠/唤醒”的问题，于是进行了这样的尝试：

添加工具函数：
    
    function sleep(msec) {
        var sleepService = "http://localhost:1234?timeout=" + msec,
            xhr = new XMLHTTPRequest();
            // 利用XHR的同步调用
            xhr.open("GET", sleepService, false);
            xhr.send(null);
    }
    
    function transaction(targetPage, eventType, waitFor, args) {
        // self 即为调用者
        var self = this,
            retValue = null;
            
            callback = function(result) {
                retValue = result;
                self.removeListener(callback);
            };
        
        // 为waitFor类型消息注册监听器
        self.addLisener(waitFor, callback);
        // 向targetPage发消息
        targetPage.notify(eventType, args);
        // 循环等待
        while(!retValue) {
            // 每100毫秒检查一次retValue
            sleep(100);
        }
        // 跳出循环
        return retValue;               
    }
    
这里的sleep调了一个webservice，是因为Javascript生来就是单线程的，没有设计sleep/wait这样的东西，这里又不想用暴力的while循环来吃光CPU，所以试图借用XHR提供的同步方法调用来实现。Webservice很容易实现，就是收到连接，等待指定的时间然后释放连接：
    
    var http = require("http"),
        url = require("url");
        
    http.createServer(function(req, res){
        var query = url.parse(req.url, true).query;
        // 读取参数
        int timeout = parseInt(query.timeout);
        timeout = timeout > 0 ? timeout : 100;
        setTimeout(function(){
            // 时间到，释放连接
            res.writeHead(200, {'Content-Type': 'text/plain'});
            res.end();
        }, timeout);
        
    }).listen(1234);
    
有了这两个工具之后，理想的执行流是:

页面A：
    
    function flow() {
        // step 1
        var result = transaction(B, "ping", "pong", args);
        // step 2
        doSomethingWithResult(result);
    }
    
    flow();
    
页面B仍然基本不变。看上去好像可行，但很遗憾，太simple太naive了，完全没有理解Javascript。问题出在哪里呢？就在工具函数transaction最后的while循环等待上。事实上，这里永远不可能跳出循环，又是因为Javascript是单线程的，而且执行单元最小粒度为一个function，在这个函数没有返回前，其他任何函数是无法“抢占”的，也就是说我们注册的callback在循环跳出前永远不会被执行，而讽刺的是循环跳出的前提是该callback被调用，这就成了一个“鸡生蛋蛋生鸡”的问题。所以，此路不通。

## 尝试2：Backend支持
看来一个“纯Javascript”的同步实现是不太可行了，那就寻求“浏览器”——也就是我们的应用程序backend的帮助吧，即把刚才transaction工具改变成两个Javascript与“浏览器”的交互接口，`ping(targetPage, eventType, args)`，和`pong(transactionId, result)`。他们的行为是：

1. 让“浏览器”将eventType消息转发给targetPage，后台生成一个transactionID，这个ID也是随着args一起发给targetPage。“浏览器”不马上释放调用者的链接，而是以transaction ID为键值保存这个连接。
2. 当targetPage处理完消息，调用pong返回结果。浏览器查找对应transaction ID的连接，把结果交还给transaction的发起者。

这样做看上去应该是行的通的，无非是实现的复杂并且丑陋一些而已。然而，又很遗憾，这个尝试也没能成功。瓶颈出在了我们“浏览器”的架构上。由于我们的“浏览器”目前是一个单进程模型，而且只有一个UI线程。不同页面的rendering都是跑在这个线程里的。而且由于不可抗因素的限制，在目前的引擎下，页面中的Javascript（Web Worker另当别论）必须运行在UI线程里。这样就导致页面A在调用同步接口ping的时候，事实上会hang住整个UI线程，那么，页面B里的逻辑也就无从执行了。

## 尝试3：控制流伪同步
经过这样的折腾后，我终于放弃了在Javascript里实现“**同步transaction**”这样的逆天行为。在勉强说服QE接受“**使用异步机制模拟同步控制流**”这样的解决方案之后，这个问题终于变得没那么复杂了。目前有很多完成类似工作的JS工具库，原理各有不同，我首先想到的是[老赵][]同志整天在微博上宣传的[Jscex][]，扫了一眼介绍后被震住了，直觉告诉我太heavy了，在没有详细了解到底背后干了些啥之前还是别自找麻烦。为了尽快摆脱这个问题，选用简洁易懂的[flow-js][]交出一个workaround：

工具：

    function transaction(targetPage, eventType, waitFor, args, next) {
         var self = this;
            
            callback = function(result) {
                self.removeListener(callback);
                // call back for flow-js
                if(next) next(null, result);    
            };
        
        // 为waitFor类型消息注册监听器
        self.addLisener(waitFor, callback);
        // 向targetPage发消息
        targetPage.notify(eventType, args);
    }
    
执行流：

    var case1 = flow.define(
        function(args) {
            // step 1            
            transaction(B, "ping", "pong", args, this);           
        },function(err, result) {
            // step 2
            foo("bar", this);            
        },function(err, result) {
            // step 3
            // ...
            // done
        }
    )
    
    case1();

现在，工作流被定义为case1，`flow.define`接受任意多个函数作为参数，这些函数即为这个工作流的不同步骤，后一个步骤依赖于前一步的返回值。步骤之间的分界点就是“异步任务”，比如，这里的transaction，但注意这次transaction里多了一个参数next，flow传实参的时候传的是this，这是[flow-js][]里做的封装，有点[call/cc][]的意思，有机会再详细说明。现在只要知道当异步工作完成后，是通过这个回调来使整个flow继续进行的。在case1里我多加了一个step，是为了说明一般地用[flow-js][]写一个异步任务的方式。foo的实现如下：

    function foo(args, next) {
        setTimeout(function(){
            if(next)next(null, "foobar");
        }, 2000);
    }
    
这样就把异步任务“嵌入”到了工作流当中。

## Future Work
当然，这样虽然把工作流汇集到了一个貌似同步的序列里，但写法上仍然不是非常直观，而且“步骤”是被异步任务而非逻辑需要分割开来的，多少有些反直觉。在接下来一段时间里，我会持续关注这个问题，并把学习经验分享在这里和感兴趣的同学讨论。初步估计涉及的内容会涵盖回调，[promise][]，[coroutine][], [call/cc][], [CPS][], [Monad][]等等，涉及的实现大致会有[flow-js][], [Step][], [conductor][], [javascript-fibers][], [Jscex][]等等。


[flow-js]: https://github.com/willconant/flow-js
[老赵]: http://blog.zhaojie.me
[Jscex]: https://github.com/Jeffreyzhao/jscex
[Step]: https://github.com/creationix/experiments/blob/master/step.js
[conductor]: http://github.com/creationix/conductor
[promise]: https://github.com/kriszyp/node-promise
[call/cc]: http://en.wikipedia.org/wiki/Call-with-current-continuation
[CPS]: http://en.wikipedia.org/wiki/Continuation_passing_style
[javascript-fibers]: https://github.com/randallmorey/javascript-fibers
[Monad]: <http://en.wikipedia.org/wiki/Monad_(functional_programming)>
[coroutine]: http://en.wikipedia.org/wiki/Coroutine