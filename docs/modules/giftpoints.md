# GiftPoints 点券赠送模块

玩家可以通过充值赠送点券给其他玩家。

## 功能

- 选择在线玩家作为赠送目标
- 支持微信/支付宝支付
- 可配置金额选项
- 支付成功后自动发放点券给目标玩家

## 命令

| 命令                                   | 说明               | 权限                     |
| -------------------------------------- | ------------------ | ------------------------ |
| `/gift`                                | 打开玩家选择菜单   | `dreamrealms.giftpoints` |
| `/gift <玩家> <wechat\|alipay> <金额>` | 直接赠送点券给玩家 | `dreamrealms.giftpoints` |

## 权限

| 权限                     | 说明         |
| ------------------------ | ------------ |
| `dreamrealms.giftpoints` | 使用赠送功能 |

## 软依赖

- **SweetCheckout** - 支付插件 (必需)
- **PlayerPoints** - 点券插件 (必需)

## 配置文件

- `modules/giftpoints/settings.yml` - 模块配置
- `modules/giftpoints/menu/` - 菜单配置

### 配置示例

```yaml
# 调试模式
debug: false

# 点券比例 (1 元 = 10 点券)
points-scale: 10

# 支付超时时间 (秒)
payment-timeout: 300

# 支付方式
payment:
  wechat: true
  alipay: true

# 金额选项
amount-options:
  - 1
  - 5
  - 10
  - 20
  - 50
  - 100
```
