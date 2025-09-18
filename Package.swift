// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "CapacitorNativeAudioNotification",
    platforms: [.iOS(.v14)],
    products: [
        .library(
            name: "CapacitorNativeAudioNotification",
            targets: ["NativeAudioNotificationPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "7.0.0")
    ],
    targets: [
        .target(
            name: "NativeAudioNotificationPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/NativeAudioNotificationPlugin"),
        .testTarget(
            name: "NativeAudioNotificationPluginTests",
            dependencies: ["NativeAudioNotificationPlugin"],
            path: "ios/Tests/NativeAudioNotificationPluginTests")
    ]
)