import UIKit

class AppDelegate: NSObject, UIApplicationDelegate {

    func application(
        _ application: UIApplication,
        supportedInterfaceOrientationsFor window: UIWindow?
    ) -> UIInterfaceOrientationMask {
        OrientationBridge.shared.mask
    }

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        NotificationCenter.default.addObserver(forName: OrientationEvents.lock, object: nil, queue: .main) { _ in
            OrientationBridge.shared.lockPortrait()
        }
        NotificationCenter.default.addObserver(forName: OrientationEvents.unlock, object: nil, queue: .main) { _ in
            OrientationBridge.shared.unlock()
        }
        return true
    }
}
