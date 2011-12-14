Title: 人生的意义在于折腾
Author: Kang Liu
Date: Wed Dec 14 2011 20:00:00 GMT+0800 (China Standard Time)

感冒在家休息。

上周末一激动把博客从wordpress迁移到了NodeJS：域名重定向到二级域名[blog.tomsheep.net][]，过去的站点被扔到了[old.tomsheep.net][]。文章暂时只搬了PV较高的十几篇，其余的看心情陆续处理。我觉得我是一个很二网站管理员，经常因为心血来潮而让站点面目全非。上述这些举措对SEO来说简直就是灾难，而且出来的是个半成品，好在这个博客存在的意义本来就是自娱自乐，多折腾几遍也无关紧要。

## Why NodeJS?
选用Node是因为我想要对网站的细枝末节有更清楚的认识，wordpress对于一个不喜欢做网站只喜欢玩网站的2B站长来说太成熟了。而且Javascript眼看即将成为我的工作及兴趣重心，前后台一致的编程体验也是十分吸引人的特性。真有意思，Java费了血劲也没有做到的事情，最终却被JavaScript做到了。而四年前JS还是我最厌恶的编程语言之一，现在成了我最喜欢的玩具——技术变革真是让普通开发者目不暇接，一觉醒来就可能变成“技术考古学家”，数年的积累换来的只是一个头衔：Expert of some legacy stuff。

## Wheat
目前采用的博客引擎是[Wheat][], 是NodeJS社区领袖[Tim CasWell][]的作品，也是技术博客[howtonode.org][]的引擎（目前博客的样式也暂时借用了howtonode的设计）。这是一个简洁到极致的引擎：Git作为文件系统以及版本控制工具、haml模板布局网页结构、markdown语法用来书写文章，除此再无其他。连博客评论都off load到[disqus.com][], 头像交给[gravatar.com][], 连数据库都不需要——所用的功能和技术结合的都很另类却又很“自然”，也显得相当性感。这也是我没有选用功能更完善的[Bogart][]的原因——太像一个主流博客引擎了。

当然，Wheat本身也存在一些不良好的体验，其中最大的问题是Tim CasWell并没有把它设计为一个“框架”，而只是一个应用，这意味想要拓展或添加功能，就必须做侵入式的修改，维护这样一份代码并且期望日后与新版本的Wheat整合，是一件看上去很麻烦的事情。

## tomsheep.net
在我还没有想好到底应该放一个什么主页之前，主域名tomsheep.net会自动重定向到[blog.tomsheep.net][]。目前博客也欠缺一些基本的功能，比如翻页（居然没有-\_-），比如category，比如友情链接。以后会慢慢补上。

周末还把简历搬到了博客里[About me][], 希望能够看着这份文档随着年岁增长而不断丰富，理想一栏渐渐变为现实。

博客内容也会渐渐丰富，我会敦促自己时常更新——我最喜欢的两份职业，程序员和编剧，其实都需要有好的写作技巧——如果你对博客的话题感兴趣，甚至想要作为作者之一，可以fork这个repo [tomsheep.net on Github][], 添加自己的内容后发pull request给我。


[blog.tomsheep.net]: http://blog.tomsheep.net "言之有误"
[old.tomsheep.net]: http://old.tomsheep.net "旧版本-言之有误"
[Tim CasWell]: http://creationix.com/ "Tim CasWell"
[Wheat]: http://github.com/creationix/wheat "Wheat"
[howtonode.org]: http://howtonode.org "HowToNode"
[disqus.com]: http://disqus.com "DISQUS"
[gravatar.com]: http://www.gravatar.com "Gravatar"
[Bogart]: https://github.com/nrstott/bogart "Bogart"
[About me]: http://blog.tomsheep.net/about "Kang Liu"
[tomsheep.net on Github]: https://github.com/tomsheep/tomsheep.net  "tomsheep.net on Github"