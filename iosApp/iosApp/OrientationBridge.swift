import UIKit

@objc public class OrientationBridge: NSObject {
    @objc public static let shared = OrientationBridge()
    @objc public var mask: UIInterfaceOrientationMask = .all

    @objc public func lockPortrait() {
        mask = .portrait
        UIViewController.attemptRotationToDeviceOrientation()
    }

    @objc public func unlock() {
        mask = .all
        UIViewController.attemptRotationToDeviceOrientation()
    }
}

enum OrientationEvents {
    static let lock   = Notification.Name("GRANDMAPP_LOCK_PORTRAIT")
    static let unlock = Notification.Name("GRANDMAPP_UNLOCK_ORIENTATION")
}
