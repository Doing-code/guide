# Maven快速入门

## Maven概念

### 仓库

所谓仓库，就是用来存储 jar 包的地方。可以是远程仓库、本地仓库、私服等。

### 坐标

Maven中的坐标用于描述仓库中资源的位置。

Maven团队提供的仓库地址`https://repo1.maven.org/maven2/`

Maven的坐标的主要组成：

- **groupId**：定义当前Maven项目隶属组织名称（通常是域名反写，如 org.mybatis）

- **artifactId**：定义当前Maven项目名称（通常是模块名称）

- **version**：定义当前项目版本号

- packaging：定义该项目的打包方式（war、jar）

Maven坐标的作用：唯一性定位资源位置，通过该标识可以将资源的识别与下载工作交给Maven工具来完成。

## 依赖管理

### 依赖配置

```xml
<!-- 设置当前项目依赖的所有 jar 包 -->
<dependencies>
    <!-- 具体的依赖 -->
    <dependency>
        <!-- 依赖隶属组织名称 -->
        <groupId>junit</groupId>
        <!-- 依赖所属项目名称 -->
        <artifactId>junit</artifactId>
        <!-- 依赖版本号 -->
        <version>4.12</version>
    </dependency>
</dependencies>
```

### 依赖传递

模块A依赖模块B。

- 依赖具有传递性：

    - 直接依赖：在当前项目中通过依赖配置建立的依赖关系。
    
    - 间接依赖：`A 依赖 B，B 依赖 C`，C 对于 A 来说就是间接依赖。

依赖传递冲突问题：

- 路径优先：当依赖中出现相同的资源时，层级越浅，优先级越高，层级越深，优先级越低。优先级高的覆盖优先级低的。

- 声明优先：当资源在相同层级被依赖时（间接依赖），配置顺序靠前的覆盖配置顺序靠后的。

- 特殊优先：当在一个项目中配置了相同资源的不同版本，后配置的覆盖先配置的。

### 可选依赖

可选依赖指对外隐藏当前所依赖的资源。简单来说就是，B 依赖 A，但是 A 有一个私服的依赖不想暴露给 B，A 就可以设置可选依赖。

```xml
<dependencies>
    <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <version>4.12</version>
        <!-- 可选依赖 -->
        <optional>true</optional>
    </dependency>
</dependencies>
```

### 排除依赖

排除依赖指主动断开依赖的资源，被排除的资源无需指定版本。简单来说就是不需要。

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    <version>2021.0.5.0</version>
    <exclusions>
        <exclusion>
            <groupId> springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-ribbon</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

### 依赖范围

compile runtime test system provided

依赖的 jar 默认情况下可以任何地方使用，可以通过 scope 标签设置其作用范围。

作用范围：

- 主程序范围有效（main 文件夹范围内）

- 测试程序范围有效（test 文件夹范围内）

- 是否参与打包（package指令范围内）

| scope | 主代码 | 测试代码 | 打包  | 示例 | 
| ---- | ---- | ---- | ----  | ---- | 
| compile（default ） | √ | √ | √ | log4j |
| test | | √ | | junit |
| provided | √ | √ | | servlet-api |
| runtime | | | √ | jdbc |

**依赖范围传递性**：带有依赖范围的资源在进行传递时，作用范围将受到影响。

| | compile | test | provided | runtime |
| ---- | ---- | ---- | ---- | ---- |
| compile | compile | test | provided | runtime |
| test | | | | |
| provided | | | | |
| runtime | runtime | test | provided | runtime |

A 依赖 B，B（第一列） 定义依赖范围为compile，A（第一行） 重新定义依赖范围为 test，A 不受影响。但如果 B 设置其范围为 test，那么对于 A 将不可见。

## 生命周期&插件

mvn执行阶段的命令格式是：`mvn 阶段1 [阶段2] [阶段n]`。如：

- `mvn clean`：调用clean生命周期的clean阶段，实际执行的阶段为 clean 生命周期中的 pre-clean 和 clean 阶段。

- `mvn clean install`：执行了两个阶段 clean 和 install。clean 位于 clean 生命周期中，install 位于 default 生命周期中。

    - 所以这个命令会先从 clean 生命周期中的 pre-clean 阶段开始执行一直到 clean 生命周期的 clean 阶段。
    
    - 然后会继续从 default 生命周期的 validate 阶段开始执行一直到 default 生命周期的 install 阶段。

每个生命周期中的后面的阶段会依赖于前面的阶段，当执行某个阶段的时候，会先执行其前面的阶段。

### 构建生命周期

Maven对项目构建的生命周期划分为3部分：

- clean：清理工作。

- default：核心工作，例如编译、测试、打包、部署等。

- site：产生报告，发布站点等。

#### clean生命周期

> 目的是清理项目

- pre-clean：执行一些需要在clean之前完成的工作。

- clean：移除所有上一次构建生成的文件。

- post-clean：执行一些需要在clean之后完成的工作。

#### default生命周期

> 主要被用于构建应用

| 生命周期 | 说明 | 功能 |
| ---- | ---- | ---- |
| validate | 校验 | 校验项目是否正确并且所有必要的信息可以完成项目的构建过程 |
| initialize | 初始化 | 初始化构建状态，比如设置属性值 |
| generate-sources | 生成源代码 | 生成包含在编译阶段中的任何源代码 |
| process-sources | 处理源代码 | 处理源代码，比如说，过滤任意值 |
| generate-resources | 生成资源文件 | 生成将会包含在项目包中的资源文件 |
| process-resources | 编译 | 复制和处理资源到目标目录，为打包阶段最好准备 |
| compile | 处理类文件 | 编译项目的源代码 |
| process-classes | 处理类文件 | 处理编译生成的文件，比如说对Java class文件做字节码改善优化 |
| generate-test-sources | 生成测试源代码 | 生成包含在编译阶段中的任何测试源代码 |
| process-test-sources | 处理测试源代码 | 处理测试源代码，比如说，过滤任意值 |
| generate-test-resources | 生成测试源文件 | 为测试创建资源文件 |
| process-test-resources | 处理测试源文件 | 复制和处理测试资源到目标目录 |
| test-compile | 编译测试源码 | 编译测试源代码到测试目标目录 |
| process-test-classes | 处理测试类文件 | 处理测试源码编译生成的文件 |
| test | 测试 | 使用合适的单元测试框架运行测试（Juint是其中之一） |
| prepare-package | 准备打包 | 在实际打包之前，执行任何的必要的操作为打包做准备 |
| package | 打包 | 将编译后的代码打包成可分发格式的文件，比如JAR、WAR或者EAR文件 |
| pre-integration-test | 集成测试前 | 在执行集成测试前进行必要的动作。比如说，搭建需要的环境 |
| integration-test | 集成测试 | 处理和部署项目到可以运行集成测试环境中 |
| post-integration-test | 集成测试后 | 在执行集成测试完成后进行必要的动作。比如说，清理集成测试环境 |
| verify | 验证 | 运行任意的检查来验证项目包有效且达到质量标准 |
| install | 安装 | 安装项目包到本地仓库，这样项目包可以用作其他本地项目的依赖 |
| deploy | 部署 | 将最终的项目包复制到远程仓库中与其他开发者和项目共享 |

#### site生命周期

- pre-site：执行一些需要在生成站点文档之前完成的工作。

- site：生成项目的站点文档。

- post-site：执行一些需要在生成站点文档之后完成的工作，并为部署做准备。

- site-deploy：将生成的站点文档部署到特定的服务器上。

### 插件

- 插件与生命周期内的阶段绑定，当执行到对应的生命周期时执行对应的插件功能。

- 默认情况下，Maven工具会在各个生命周期中绑定预设的功能。

- 通过插件可以自定义功能。

```xml
<build>
    <plugins>
        <plugin>
            <!-- 插件坐标，这个插件专门负责将项目源文件打成包 -->
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-source-plugin</artifactId>
            <version>2.2.1</version>
            <executions>
                <excution>
                    <goals>
                        <!-- 当goal属性的值为test-jar时，是对测试代码打包 -->
                        <goal>jar</goal>
                    </goals>
                    <!-- 在哪个阶段（生命周期）执行插件 -->
                    <phase>generate-test-resources</phase>
                </excution>
            </executions>
        </plugin>
    </plugins>
</build>
```

## 聚合

> 聚合用于快速构建 Maven 工程，一次性构建多个项目/模块.

聚合本身也是个模块，称为聚合模块。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>...</groupId>
    <artifactId>...</artifactId>
    <!-- 打包方式为 pom -->
    <packaging>pom</packaging>
    <version>0.0.1</version>

    <!-- 定义当前模块进行构建操作时，关联的其它模块名称 -->
    <modules>
        <!-- 具体工程名称 -->
        <module>A-模块</module>
        <module>B-模块</module>
        <module>C-模块</module>
        <module>D-模块</module>
        <module>E-模块</module>
        <module>F-模块</module>
    </modules>

</project>
```

参与聚合操作的模块最终执行顺序与模块间的依赖关系有关，和配置顺序无关。A 依赖 B，则需要先等待 B 构建完，才能开始构建 A。

## 继承

> 依赖管理

父工程管理整个项目的依赖版本，而子工程需要依赖时，还是需要定义依赖坐标，但无需指定版本，版本从父工程中继承。

父工程只是声明依赖，子工程按需引入。

```xml
<dependencyManagement>

    <dependencies>

        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

在子工程中配置父工程的坐标

```xml
<project>
    <parent>
        <artifactId>Lottery</artifactId>
        <groupId>cn.forbearance.lottery</groupId>
        <version>0.0.1</version>
        <relativePath/>
    </parent>
    
    <dependencies>
    
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

## 属性

> 自定义属性、内置属性、setting属性、Java系统属性、环境变量属性.

```xml
<project>
    <properties>
        <jdk.version>1.8</jdk.version>
        <sourceEncoding>UTF-8</sourceEncoding>
        <junit.version>4.12</junit.version>
    </properties>

<dependencies>
    
    <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <version>${junit.version}</version>
        <scope>test</scope>
    </dependency>
</dependencies>
</project>
```

## 版本管理

工程版本号约定规范：

- 主版本.次版本.增量版本.里程碑版本

- 主版本：表示项目重大架构的变更。

- 次版本：表示有较大地功能增加和变化，或全面系统地修复漏洞。

- 增量版本：表示有重大漏斗的修复。

- 里程碑版本：表明一个版本的里程碑（内部版本）。这样的斑斑同下一个正式版本相比，相对来说不是很稳定，有待更多的测试。

## 资源配置

> 在使用 MyBatis 时，其 mapper 映射文件就需要配置这个资源配置

配置文件引用pom属性，调用格式为`${定义的属性标签名}`，如`${user.name}`。

```xml
<project>
    <properties>
        <user.name>Jack</user.name>
    </properties>
    <build>
        <resources>
            <resource>
                <!-- 资源文件路径 -->
                <directory>src/main/resource</directory>
                <!-- 开启加载 pom 属性过滤功能 -->
                <filtering>true</filtering>
            </resource>
        </resources>
    
        <!-- 测试资源文件 -->
        <testResources>
            <testResource>
                <directory>src/test/resources</directory>
                <filtering>true</filtering>
            </testResource>
        </testResources>
    </build>
</project>
```

## 多环境开发配置

> 如何使用？使用 mvn 命令：`mvn install -P env_pron`

```xml
<profiles>
    <profile>
        <!-- 环境名称 -->
        <id>env_pron</id>
        <!-- 不同环境使用不同的属性 -->
        <properties>
            <user.name>Jack</user.name>
        </properties>
        <!-- 默认生效环境 -->
        <activation>
            <activeByDefault>true</activeByDefault>
        </activation>
    </profile>

    <profile>
        <id>env_dev</id>
        <properties>
            <user.name>Tommy</user.name>
        </properties>
    </profile>
</profiles>
```

## 跳过测试

## 私服