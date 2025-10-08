package ru.acs.grandmap.feature.auth

expect fun defaultUseCookies(): Boolean        // wasm=true, android=false
expect fun platformCode(): Int                 // Web=4, Android=1, iOS=2, Desktop=3
expect fun deviceId(): String?
expect fun deviceTitle(): String?