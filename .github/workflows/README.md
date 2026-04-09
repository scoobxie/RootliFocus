# CI/CD

## What is CI/CD?

**CI (Continuous Integration)** - every time code is pushed, an automated system builds it and runs tests. Catches broken code early, before it reaches users.

**CD (Continuous Delivery/Deployment)** - after CI passes, the system automatically packages and ships the software. No manual "zip this up and upload it" steps.

In this project there's no test suite yet, so the workflow is purely CD: push a version tag → GitHub's servers build the app → two installer files appear on the Releases page automatically.

The workflow file lives in `.github/workflows/` - GitHub detects and runs any `.yml` files there. Each workflow defines a **trigger** (what event starts it), a **runner** (what OS the build machine uses), and a list of **steps** (the actual commands).

---

`release.yml` - triggers on `v*` tags. Builds a fat JAR on a Windows runner (needed for Windows-native JavaFX libs), then produces two release artifacts:

| Artifact | Requires Java | Notes |
|---|---|---|
| `RootlifyFocus-vX.X.X-portable.zip` | Yes (17+) | Extract and run `java -jar RootlifyFocus.jar` |
| `RootlifyFocus-X.X.X.exe` | No | Windows installer, bundles its own JRE |

## Cut a release

```sh
git tag v1.0.0 && git push origin v1.0.0
```

Release appears under the Releases tab with auto-generated notes from commits.
