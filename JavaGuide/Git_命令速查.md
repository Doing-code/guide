# Git

```text
# 一般来说，用这几个命令即可

git status

git stash

git pull --rebase

git stash pop

git add .

git commit -m "写注释"

git push origin master

git reset HEAD xxx  撤销某个 add 文件
```


## Git工作流程图

![](https://github.com/Doing-code/guide/blob/main/image/git_Git工作流程图.png)

1. clone：克隆。从远程仓库中克隆代码到本地仓库。

2. checkout：检出。从本地仓库中选一个仓库分支然后进行修订。

3. add：添加。在提交前先将代码提交到暂存区。

4. commit：提交。将暂存区的代码提交到本地仓库。本地仓库中保存修改的历史版本。

5. fetch：抓取。从远程仓库抓取到本地仓库，不进行任何的合并动作，一般使用较少。

6. pull：拉取。从远程仓库中拉取代码到本地库，自动进行合并（merge）。然后保存到工作区，相当于fetch+merge。

7. push：推送。修改完成后，将本地仓库的代码推送到远程仓库。

## Git常用命令

### 基础操作指令

![](https://github.com/Doing-code/guide/blob/main/image/git_基础操作指令.png)

#### 查看修改状态

- 作用：查看暂存区、工作区的修改的状态。

- 命令形式：`git status`。

```bash
$ touch file.txt

$ git status
On branch master

No commits yet

Untracked files:
  (use "git add <file>..." to include in what will be committed)
        file.txt

nothing added to commit but untracked files present (use "git add" to track)

$ git add file.txt

$ git status
On branch master

No commits yet

Changes to be committed:
  (use "git rm --cached <file>..." to unstage)
        new file:   file.txt

$ git commit -m "add file.txt"
[master (root-commit) 4157bd3] add file.txt
 1 file changed, 0 insertions(+), 0 deletions(-)
 create mode 100644 file.txt

$ git status
On branch master
nothing to commit, working tree clean
```

#### 添加工作区到暂存区

- 作用：添加工作区的一个或多个文件的修改到暂存区。

- 命令形式：git add 单个文件名|通配符。如将所有修改添加到暂存区`git add .`。

#### 提交暂存区到本地仓库

- 作用：提交暂存区内容到本地仓库的当前分支。

- 命令形式：`git commit -m "注释"`。

#### 查看提交日志

- 作用：查看提交记录。

- 命令形式：`git log [option]`。
  
  - option：
    
    - `--all`：显示所有分支
    
    - `--pretty=oneline`：将提交信息显示为一行。
    
    - `--abbrev-commit`：输出简短的commitId。
    
    - `--graph`：用字符以图形方式展示分支变化（提交、合并...）。

#### 版本回退

- 作用：版本切换。

- 命令形式：`git reset --hard commitId`。

commitId 可以使用`git log`指令查看。

通过`git reflog`指令可以查看 已经删除的提交记录。

`git reset HEAD~1`撤销已经提交的commit到未提交状态。

其中HEAD~1表示将HEAD指针向前移动1个commit，即撤销上一个commit。执行完该命令后，之前的commit将被撤销到暂存区，代码仍然保留在本地工作区。

#### ignore

```bash
$ touch .ignore
*.txt # 表示忽略所有以 txt 结尾的文件
```

### 分支

几乎所有的版本控制系统都以某种形式支持分支。使用分支意味着你可以把你的工作从开发主线上分离开来，进行Bug修复、开发新的功能等，避免影响开发主线。

#### 查看本地分支

- 命令：`git branch`。

#### 创建本地分支

- 命令：`git branch branch_name`。

#### 切换分支

- 命令：`git checkout branch_name`。

- 命令：`git checkout -b branch_name`，`-b`表示如果切换的分支不存在则创建。

切换分支前先提交本地的修改。

#### 合并分支

- 作用：一个分支上的提交可以合并到另一个分支。

- 命令：`git　merge　branch_name`。

如果在master分支上执行`git　merge branch_name`，表示将其他分支branch_name的更改合并到master分支。

#### 删除分支

只能删除除当前分支以外的其它分支。

- `git branch -d branch_name`：删除分支时，需要安全检查。

- `git branch -D branch_name`：强制删除，不做任何检查。

#### 解决冲突

当两个分支对同一个文件的同一处地方的修改会存在冲突。这时就需要手动解决冲突，解决冲突的步骤如下：

1. 护理文件中冲突的地方。

2. 将解决完冲突的文件假如暂存区。

3. 提交到仓库。

```text
<<<<<<< HEAD
update count=3
=======
update count=2
>>>>>>> dev
```

`HEAD`和`==`范围之间的是master分支冲突内容，`==`和`dev`范围之间的是dev分支冲突内容。如果想保留master分支内容，则删除dev相关内容，否则删除master相关内容。

```text
update count=5
```

#### 开发中分支使用原则与流程

- master（生产）分支：线上分支，主分支，中小规模项目作用线上运行的应用对应的分支；

- develop（开发）分支：从master创建的分支，一般作为开发部门的主要开发分支。如果没有其它并行开发不同期上线要求，都可以在此分支上进行开发，阶段开发完成后，需要合并到master分支，准备上线。

- feature/xxx分支：从develop创建的分支，一般是同期并行开发，但不同期上限时所创建的分支，该分支的研发任务完成后需合并到develop分支。

- hotfix/xxx分支：从master派生的分支，一般作为线上bug修复使用，修复完成需要合并到master、test、develop分支。

- 其它分支，如test、pre等分支。在此不再详述。

## 远程仓库

### 添加远程仓库

此操作时先初始化本地仓库，然后与远程仓库进行关联。

- 命令：`git remote add <远程仓库名称> <远程仓库路径>`。
  
  - 远程仓库名称：默认时origin，取决于远程仓库服务器设置。
  
  - 仓库路径：远程仓库服务器URL。

```bash
$ git remote add origin https://github.com/xxx/xxx/xxx.git
```

### 查看远程仓库

- 命令：`git remote`。

```bash
$ git remote
origin
```

### 推送到远程仓库

- 命令：`git push [-f] [--set-upstream] [远程名称 [本地分支名][:远程分支名]]`
  
  - `-f`：强制更新。
  
  - 如果远程分支和本地分支名称相同，则可以只写本地分支`git push origin master`。
  
  - `--set-upstream`表示推送到远程的同时并建立本地和远端分支的关联关系。`git push --set-upstream origin master`
  
  - 如果当前分支已经和远端分支关联，则可以省略分支名称和远程名称。`git push` 将master分支推送到已关联的远程分支。

### 本地分支与远程分支的关联

- 作用：查看本地分支与远程分支的关联关系

- 命令：`git branch -vv`。

### 从远程仓库克隆

- 作用：将远程仓库clone到本地。

- 命令：`git clone <远程仓库地址> [本地目录]`
  
  - 本地目录可以省略，会在当前文件生成一个目录。

### 从远程仓库中抓取和拉取

远程分支和本地分支一样，可以进行merge操作，需要先把远程仓库更新到本地后再进行操作。

- fetch命令：`git fetch [remote name] [branch name]`
  
  - 抓取命令就是将仓库中的修改都抓取到本地，但不会进行合并。
  
  - 如果不指定`[remote name] [branch name]`，则默认抓取与本地仓库关联的远程仓库的分支。

- pull命令：`git pull [remote name] [branch name]`
  
  - 拉取命令就是将远程仓库的修改拉到本地并自动进行合并，等同于fetch+merge。
  
  - 如果不指定`[remote name] [branch name]`，则默认拉取与本地仓库关联的远程仓库的分支，并更新当前分支。

### 解决合并冲突

在pull拉取分支时，A、B修改了同一文件的同一处位置的代码，所以会导致代码冲突。

远程分支也是分支，所以合并时冲突的解决方式也和解决本地分支冲突相同。

## 附录

### 获取本地仓库

```bash
$ git init
Initialized empty Git repository in A:/etc/test_git/.git/
```