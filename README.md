# DreamRealms

Minecraft Spigot/Paper 插件，基于 [PluginBase](https://github.com/MrXiaoM/PluginBase) 框架开发。

## 功能模块

| 模块     | 说明                     | 状态 |
| -------- | ------------------------ | ---- |
| TimeSync | 服务器时间与现实时间同步 | ✅   |

## 文档

详细文档请查看 [Wiki](https://github.com/LectWolf/DreamRealms/wiki)

## 依赖

- Java 21+
- Spigot/Paper 1.21+
- Vault (可选，经济系统)

## 安装

1. 下载 [最新 Release](https://github.com/LectWolf/DreamRealms/releases)
2. 放入 `plugins/` 目录
3. 重启服务器
4. 编辑 `plugins/DreamRealms/config.yml` 启用需要的模块

## 命令

| 命令           | 说明         | 权限                   |
| -------------- | ------------ | ---------------------- |
| `/dr reload`   | 重载配置     | `dreamrealms.admin`    |
| `/dr timesync` | 时间同步模块 | `dreamrealms.timesync` |

## 构建

```bash
./gradlew build
```

产物位于 `build/libs/`

## 许可证

本项目采用 [GPL-3.0](LICENSE) 许可证。
