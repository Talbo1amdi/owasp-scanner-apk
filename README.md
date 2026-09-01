# اسکنر امنیتی — Security Scanner (Persian, Serverless Android)

یک اسکنر امنیتی فارسی برای اندروید. کاملاً آفلاین و سرورلس — هیچ درخواستی به هیچ سرور خارجی ارسال نمی‌شود (بدون Vercel، بدون هاستینگ، بدون CORS). شبکه از طریق پلاگین بومی ‎`@capacitor-community/http`‎ انجام می‌شود.

A fully serverless Persian (Farsi, RTL) security scanner for Android. It makes **no** requests to any external server (no Vercel, no hosting, no CORS) — network calls go through the native `@capacitor-community/http` plugin.

## ساخت APK / Build the APK

اپک به‌صورت خودکار توسط GitHub Actions ساخته می‌شود. The APK is built automatically by GitHub Actions:

1. Push to `main` — یا از تب **Actions** روی **Build Android APK** → **Run workflow** کلیک کنید (or run the **Build Android APK** workflow manually from the Actions tab).
2. When it finishes, open the run and download the **owasp-scanner-farsi-apk** artifact (a `.apk` file).
3. On Android, tap install and allow **Install unknown apps** when prompted.

## ساختار / Structure

| Path | Purpose |
|---|---|
| `www/index.html` | The full Farsi RTL scanner web app |
| `android/android/` | Capacitor native Android project (generated once, then sync'd) |
| `capacitor.config.json` | Capacitor app config (`appId: ir.owasp.scanner`) |
| `.github/workflows/build-apk.yml` | GitHub Actions APK build workflow |
| `package.json` | Capacitor dependency pins |

## نکات فنی / Technical notes

- Native project is committed so custom Android manifest settings survive (`networkSecurityConfig` cleartext traffic, `android.permission.INTERNET`, `supportsRtl="true"`).
- CI: `setup-node` → `setup-java` (JDK 21, required by Gradle 8.11.1/AGP 8.7.2) → `setup-android` (pre-warms SDK/build tools to avoid dl.google.com timeouts) → `npm install` → `npx cap sync android` → `./gradlew assembleDebug`.