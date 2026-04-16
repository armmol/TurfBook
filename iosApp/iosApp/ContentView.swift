import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            // Let Compose paint edge-to-edge — it handles insets internally
            // via statusBarsPadding() / navigationBarsPadding() modifiers.
            .ignoresSafeArea(.all)
            // Match our dark background so there's no flicker behind the
            // home indicator or in the corners on rounded-display devices.
            .background(Color(red: 0.051, green: 0.122, blue: 0.078))
    }
}
