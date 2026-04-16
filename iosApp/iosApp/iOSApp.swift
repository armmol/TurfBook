import SwiftUI
import UIKit

@main
struct iOSApp: App {

    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate

    var body: some Scene {
        WindowGroup {
            ContentView()
                // Make the window background match our launch screen — no
                // flicker even if Compose takes a frame to paint.
                .background(Color(red: 0.051, green: 0.122, blue: 0.078))
        }
    }
}

final class AppDelegate: NSObject, UIApplicationDelegate {

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        // Force light-content status bar (white clock/icons on our dark bg).
        // Info.plist sets the default; this call ensures it's applied immediately.
        UIApplication.shared.statusBarStyle = .lightContent
        return true
    }
}
