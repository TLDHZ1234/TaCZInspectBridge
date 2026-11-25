# TaCZ Inspect Bridge

一个小型客户端模组，为 TaCZ（Timeless & Classics Guns: Zero）和 LesRaisins Tactical Equipments
（lrtactical）的检视（inspect）动画提供两件事：

1. 通过 `IInspectAccessor` 把当前检视动画的播放进度 / 总时长暴露给其它模组；
2. 检视动画播放期间取消原版视角摇晃（`GameRenderer#bobView`），让检视画面不被镜头晃动干扰。

支持的加载器：

| 加载器 | Minecraft | 依赖 |
| --- | --- | --- |
| Forge | 1.20.1 | TaCZ ≥ 1.1.0、lrtactical ≥ 0.4.0 |
| Fabric | 1.21.1 | TaCZ（Refabricated）≥ 1.1.0、lrtactical ≥ 0.4.0 |
| NeoForge | 1.21.1 | TaCZ ≥ 1.1.0、lrtactical ≥ 0.4.0 |

## 安装

把对应加载器和游戏版本的 jar 放进 `mods/` 目录即可（客户端模组，服务端不需要）：

- `taczinspect-forge-1.20.1-<version>.jar`
- `taczinspect-fabric-1.21.1-<version>.jar`
- `taczinspect-neoforge-1.21.1-<version>.jar`

## 给其它模组用的 API

```java
// 拿到的 AnimationStateContext 实例（GunAnimationStateContext /
// MeleeAnimationStateContext 都继承自它）实现了这个接口：
public interface IInspectAccessor {
    boolean taczinspect$isInspecting();
    float taczinspect$getInspectProgressSeconds(); // 当前检视动画已播放秒数，未检视时 < 0
    float taczinspect$getInspectTotalSeconds();    // 当前检视动画总时长，未检视时 < 0
}
```

`AnimationStateContext` 由 `GunDisplayInstance#getAnimationStateMachine()#getContext()` 或
`MeleeDisplayInstance#getStateMachine()#getContext()` 取得。

## 构建

```bash
./gradlew build              # 三个加载器全部构建
./gradlew -Pmodule=fabric :fabric:build   # 只构建单个加载器
```

Forge 1.20.1 需要 Java 17 工具链，Fabric / NeoForge 1.21.1 需要 Java 21。
推送 tag 或手动触发仓库里的 `Build` workflow 也可以在 GitHub Actions 上构建，产物以 artifact 形式上传。

## 结构

```
common/src/main/java   共享源码（两个 mixin + API 接口），被三个加载器的 sourceSet 直接引用
fabric/  forge/  neoforge/   各加载器的构建脚本、模组元数据和唯一不同的 LrTactical 适配类
mods/                  构建时用作 compileOnly 依赖的第三方 jar
```
