# AI 客服中间件 · Docker 环境部署指南

本项目提供了 `docker-compose.yml` 文件，用于一键启动所有依赖的基础设施数据库环境（MySQL、Redis、PGVector）。

无论是在本地开发调试，还是在云服务器上进行生产环境部署，使用 Docker 都可以极大简化环境配置过程。以下是两种场景下的使用指南。

## 场景一：本地 Windows 开发环境

在开发阶段，我们使用 Docker 启动这些数据库，为后端的 Spring Boot 应用提供服务。

### 1. 前置准备
* 确保你的 Windows 电脑已安装 **Docker Desktop** 并且正在运行。
* 推荐安装数据库连接工具（如：Navicat, DBeaver, DataGrip）方便导入初始表结构。

### 2. 启动基础环境容器
打开 PowerShell 或 CMD，进入项目根目录：
```powershell
cd D:\java\project\aiCustomerService
docker-compose up -d
```
首次运行会拉取镜像，耗时视网络而定。启动成功后，会在根目录下自动生成一个 `docker` 文件夹用于挂载本地数据卷（防数据丢失）。

*你也可以使用 `docker-compose down` 来停止并移除容器，但数据会保留在刚才生成的 `docker` 文件夹内。*

### 3. 初始化数据库结构
环境启动后，需要初始化数据库的表结果。请使用你喜欢的 GUI 客户端进行连接并导入 SQL 面板：

#### **服务 A：MySQL 主数据库**
*   **连接地址**：`127.0.0.1:3306`
*   **用户名**：`root`
*   **密码**：`root`
*   **目标库名**：`ai_cs`（容器已自动创建）
*   **操作**：连接到该库，执行项目中附带的 `sql/init.sql` 脚本。

#### **服务 B：PostgreSQL + PGVector (向量库)**
*   **连接地址**：`127.0.0.1:5432`
*   **用户名**：`root`
*   **密码**：`root`
*   **目标库名**：`ai_cs_vector`（容器已自动创建）
*   **操作**：连接到该库，执行项目中附带的 `sql/pgvector_init.sql` 脚本。

---

## 场景二：云服务器 Linux 生产环境

在生产环境部署时，我们建议将中间件直接放在云端运行，并重置默认的弱密码保障安全。

### 1. 前置准备
在你的云服务器（推荐 Ubuntu/CentOS 等环境）上安装 Docker 与 Docker Compose：
```bash
# Ubuntu 一键安装示例
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo apt install docker-compose
```

### 2. 上传配置与修改（安全注意）
将本地的 `docker-compose.yml` 和 `sql` 文件夹上传到服务器某目录（例如 `/opt/ai-cs/`）。

⚠️ **【非常重要】上线前必须修改文件中的默认密码：**
使用 `vim docker-compose.yml` 编辑修改：
* MySQL 环境变量 `MYSQL_ROOT_PASSWORD`
* Redis command 中的 `--requirepass`
* PGVector 环境变量 `POSTGRES_PASSWORD`

*(注意：记得把修改后的这些新密码同步更新到 Spring Boot 的 `application-prod.yml` 里面！)*

### 3. 后台启动所有容器
进入服务器对应的目录，运行：
```bash
cd /opt/ai-cs/
docker-compose up -d
```

### 4. 导入初始数据 (命令行方式)
如果你的服务器不允许公网 IP 直接连接数据库（为安全推荐仅内网互通），可以通过执行 `docker exec` 直接将 SQL 灌入：

**初始化 MySQL：**
```bash
cat sql/init.sql | docker exec -i aics-mysql mysql -uroot -p<你修改后的密码> ai_cs
```

**初始化 PGVector：**
```bash
cat sql/pgvector_init.sql | docker exec -i aics-pgvector psql -U root -d ai_cs_vector
```

### 5. 部署应用程序本身
完成以上数据库部署后：
1. 将打包好的 Java `ai-cs-server.jar` 上传到服务器运行：`java -jar ai-cs-server.jar`
2. 将构建好的前端 Vue 静态资源（dist文件夹）使用 Nginx 进行代理部署即可。

---

**附录：默认对外暴露端口矩阵**

| 服务分类 | 服务名 | 默认宿主机端口 | 是否需要开放云服务器防火墙/安全组？ |
| :--- | :--- | :--- | :--- |
| 主数据库 | MySQL 8.0 | `3306` | 否（仅留给本地/安全组互访验证） |
| 缓存/会话 | Redis 7.0 | `6379` | 否 |
| 向量库 | PGVector | `5432` | 否 |
| 后端 API | Server | `9900` | **是**（视反向代理配置也可不开放暴露本机反代） |
| 页面代理 | Nginx | `80/443` | **是** |
