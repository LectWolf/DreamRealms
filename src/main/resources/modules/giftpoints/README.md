# GiftPoints 点券赠送模块

玩家可以通过充值赠送点券给其他在线玩家。

## 功能

- 选择在线玩家作为赠送对象
- 支持微信/支付宝支付
- 可配置金额选项和点券比例
- 支付成功后自动赠送点券

## 依赖

- **SweetCheckout** - 支付系统
- **PlayerPoints** - 点券系统

## 命令

| 命令                                   | 说明         | 权限                     |
| -------------------------------------- | ------------ | ------------------------ |
| `/gift`                                | 打开赠送菜单 | `dreamrealms.giftpoints` |
| `/gift <玩家> <wechat\|alipay> <金额>` | 直接赠送     | `dreamrealms.giftpoints` |

## 权限

| 权限                     | 说明             |
| ------------------------ | ---------------- |
| `dreamrealms.giftpoints` | 使用点券赠送功能 |

## 配置文件

- `settings.yml` - 模块配置（点券比例、支付超时、金额选项）
- `menu/player_select.yml` - 玩家选择菜单
- `menu/payment_select.yml` - 支付方式选择菜单
- `menu/amount_select.yml` - 金额选择菜单
