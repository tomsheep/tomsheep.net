Title: 从一个bug看Linux启动原理
Author: Kang Liu
Date: Wen Jun 8 2011 18:53:00 GMT+0800 (China Standard Time)
Node: v0.4.12

磨合久了就会产生默契。非但人是这样，机器也是如此。前几天刚刚复习过Linux的开机原理，今天我的ubuntu就很配合地卡死在了启动阶段，经过观察分析，活学活用战无不胜的爱谁谁的思想，成功排除了bug，幸甚至哉，歌以咏志。

## Linux启动原理

如果按纪元来分划，系统的启动过程可以分为“kernel前”的蛮荒时代和“kernel后”的文明时代。kernel就是我们的操作系统内核，老强大了，一般以压缩文件的方式被放置在/boot分区下。所谓“kernel前”（我胡诌的），即kernel接管整个启动过程之前，主要经历了BIOS自检->bootloader->initrd几个阶段。而“kernel后”，则主要包括了init进程->相应runlevel的若干启动脚本等。

BIOS在自检过后，一般会做这样一件事情，就是直接通过磁盘寻址加载磁盘的MBR（主引导扇区，512字节） ，而MBR里存放的就是bootloader程序，负责下一步的启动任务。Windows系统有自己的bootloader，没研究过不瞎说了，揣测一下就是系统分区根目录的ntldr（Win XP），boot.ini为其提供配置信息。 Linux中比较常见的bootloader有LILO和grub，grub更流行一些，我们主要分析它。grub0.98之前的版本被称为grub legacy，之后的版本统称为grub2，两者在指令、配置文件上有一些区别，我的ubuntu10.04使用的是grub2.

刚才说了MBR只有512字节，除了bootloader还要存放分区表（64字节）和2字节的magic number，而bootloader的最终目的是加载kernel，要做的事情很多，放不下怎么办？针对Grub而言，它把自己分为了三个stage，MBR里的只是stage1，由它来加载后面的stage1.5。我们刚才说了linux的内核是放在文件系统中的，所以要加载kernel必须得有文件系统支持才行，stage1.5做的就是这个事情（/boot分区中你会看到很多的stage1.5模块，就是根据不同的文件系统支持来划分的）。在stage1.5之后，grub终于可以访问文件系统了，stage2就会读取相应的配置文件，找到kernel和initrd的位置，然后把控制权交给kernel。下面给出一段grub2的配置文件例子

<linux-boot/grub.snippet.conf>

其中linux指令指定了kernel文件的位置，root参数指明根目录对应的设备，你可能会问这不是和前面的set root重复了吗？其实没有，前面的set root是给grub的，让grub切换根目录到相应设备，而这里的root参数是给kernel看的，以让kernel正确地挂载根目录，loop参数比较特殊，这是因为我的ubuntu是wubi安装的，后面再介绍。

initrd是一个临时的根文件系统，被kernel加载到内存中，然后进行一些列驱动的加载。它存在的目的是让kernel保持简洁，因为如果把所有设备驱动都编进kernel，那kernel就太臃肿了，而且不灵活。initrd机制就是在kernel中只保存少量最主要的设备驱动，系统安装时检测硬件，把实际启动需要的驱动编入initrd，这样kernel并不需要修改，保持良好身材。在initrd中的init脚本（就是加载驱动）完成后，kernel有能力挂载真正的根目录了，使出一招始乱终弃……哦不是，偷天换日，挂载刚才root参数指定的分区，把根目录换成它。

后面的故事就是所谓的“kernel后”时代了，启动init进程，pid为1，这个进程是所有进程之祖。接下来就是检查runlevel（一般在/etc/inittab下，但ubuntu在），依次启动相应的脚本集，一般在/etc/rcX.d目录中 ，它们都是指向其他地方的软连接，按照名字中的数字从小到大运行，S开头的表示启动，传start参数，K开头的表示Kill，传stop参数。另外rcS.d目录下的脚本是不管那个runlevel都会运行的。“kernel后”时代的故事不展开说，和今天的主题没有太多关系。

## ubuntu的wubi安装机制

ubuntu从8.04（？）版本后开始支持wubi安装模式，它的主要原理就是用文件镜像来模拟真实的磁盘（就是刚才loop里的root.disk），从而无须单独分区，即可把ubuntu安装到机器上。这个wubi系统除了磁盘是假的（宿主系统中的镜像文件），磁盘IO会有一些overhead外，其余设备都是“真”的，所以并不是什么虚拟机。

那么它是怎么启动的？原理大致是一样的，首先从宿主系统入手，我的是WinXP，C:\boot.ini文件（相当于grub.cfg）中除了正常的WinXP启动项外，还有一项指向了C盘下的wubildr文件，这就是一个chainloader机制，因为windows的bootloader不能直接启动Linux系统，但可以把bootloader“链”到另一个bootloader，这个bootloader可能是其他分区的boot sector，也可能是一个镜像文件（就像这里的wubildr），然后wubildr再去扮演grub的角色，来启动Linux。

要注意的是wubi用的是文件镜像来做磁盘，所以grub要额外做一些事情，上面的配置文件中有一句loopback指令，他就是用来把镜像文件加载为一个虚拟设备，叫做loop0，然后set root为loop0，这样grub就可以去读镜像磁盘中的文件了（比如kernel什么的），而下面linux指令中的loop参数作用是辅助kernel正确挂载根目录——因为/dev/sda6是拓展分区的第二个逻辑分区，再直白一点就是我的WinXP里的E盘，在wubi这个特殊的语境下，他只是存放镜像的分区，不是正确的根目录（正确的应该是E:\ubuntu\disks\root.disk），而loop参数就是进一步告诉kernel根目录在哪里。

## 解决开机错误

说了这么多，终于到正题了，我遇到的问题是开机后ubuntu启动卡在了grub中，直接进入grub命令行，其实问题不大，因为这说明启动已经到了stage2，问题多半是grub配置文件损坏。在命令行里输入相应的配置指令就可以了。ls一下发现loop0设备已经有了，想一下这也是正常的，因为loop0应该在stage1.5就被挂载好，这相当于添加文件系统支持。root=(loop0)设为grub的根目录，然后判断一下root.disk镜像是放在哪个分区的，ls命令就行，发现是/dev/sda6, 给linux指令和initrd指令配好正确的参数，然后键入boot，就欢乐地进入系统了。

进入系统后执行update-grub2命令，会自动检测可启动的系统生成相应grub.cfg（这是grub2的特性，不用手动编写），修复grub，问题解决。做到这里突然想起来去年装好ubuntu后想把镜像文件移到另一个分区中，一直没有成功，现在了解了原理，是不是……没错，按照这个原理，随意把镜像文件放到哪里，都没有问题。

## 总结
本文从ubuntu10.04启动中由于grub配置损坏引起的一个bug出发，介绍了linux的启动原理以及wubi的特殊原理。