# leeks
idea插件，查看基金

## 安装  
leeks-1.1.jar 直接在IDEA里面安装  

## 使用  
设置里面找到Leeks选项，输入基金编码，股票编码，逗号分隔，apply。    
（股票编码建议用雪球看网页找，示例：sh600519,sz000001，基金编码zfb上面有，或者天天基金看  
double shift，连按两下shift，输入leeks，找到toolWindow，打开以后默认在下方，自行调节位置  
每次修改，添加基金，都需要点refresh按钮或者重启IDEA。  
更新频率一分钟一次

![da](https://github.com/huage2580/leeks/blob/master/TIM%E6%88%AA%E5%9B%BE20200715180137.jpg)
![dd](https://github.com/huage2580/leeks/blob/master/TIM%E6%88%AA%E5%9B%BE20200715180157.jpg)

## change  
- v1.1   
增加了股票的tab，采用腾讯的行情接口，股票轮询间隔10s  
- v1.2   
支持了港股和美股 示例代码：（sh000001,sh600519,sz000001,hk00700,usAAPL）代码一般可以在各网页端看得到  
- v1.3    
插件由小韭菜更名为Leeks
支持了IDEA 2020.1.3,兼容到`IDEA 2017.3`，修复macOS 行高问题（不确定  
- v1.4.1   
增加了隐蔽模式（全拼音和无色涨跌幅
```$xslt
IntelliJ IDEA 2020.1.3 (Community Edition)
Build #IC-201.8538.31, built on July 7, 2020
Runtime version: 11.0.7+10-b765.64 amd64
VM: OpenJDK 64-Bit Server VM by JetBrains s.r.o.
Windows 10 10.0
GC: G1 Young Generation, G1 Old Generation
Memory: 512M
Cores: 6
Non-Bundled Plugins: com.huage2580.leeks
```
