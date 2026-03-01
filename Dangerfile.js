// Dangerfile.js — automated PR review rules for Hexodus

const { danger, warn, fail, message } = require("danger");

const pr = danger.github.pr;
const modifiedFiles = danger.git.modified_files;
const createdFiles = danger.git.created_files;
const deletedFiles = danger.git.deleted_files;
const allChangedFiles = [...modifiedFiles, ...createdFiles];

// ── PR size ──────────────────────────────────────────────────────────────────
const totalLines = pr.additions + pr.deletions;
if (totalLines > 800) {
  warn(
    `This PR changes **${totalLines} lines**. Consider splitting it into smaller, focused PRs to make review easier.`
  );
}

// ── PR description ────────────────────────────────────────────────────────────
if (!pr.body || pr.body.trim().length < 20) {
  fail("Please provide a meaningful PR description so reviewers understand the intent of the change.");
}

// ── Source changes without tests ─────────────────────────────────────────────
const sourceFiles = allChangedFiles.filter(
  (f) => f.startsWith("app/src/main/") && f.endsWith(".kt")
);
const testFiles = allChangedFiles.filter(
  (f) => f.includes("src/test/") || f.includes("src/androidTest/")
);
if (sourceFiles.length > 0 && testFiles.length === 0) {
  warn(
    `**${sourceFiles.length} source file(s) modified** but no test files were added or updated. ` +
    "Consider adding unit tests to cover the changes."
  );
}

// ── Kotlin files summary ──────────────────────────────────────────────────────
const kotlinFiles = allChangedFiles.filter((f) => f.endsWith(".kt"));
if (kotlinFiles.length > 0) {
  message(`📦 **${kotlinFiles.length} Kotlin file(s)** changed in this PR.`);
}

// ── Manifest changes ──────────────────────────────────────────────────────────
const manifestChanged = allChangedFiles.some((f) =>
  f.includes("AndroidManifest.xml")
);
if (manifestChanged) {
  warn(
    "**AndroidManifest.xml was modified.** Please verify permissions, services, and providers are intentional."
  );
}

// ── Gradle dependency changes ─────────────────────────────────────────────────
const gradleChanged = allChangedFiles.some((f) => f.endsWith(".gradle") || f.endsWith(".gradle.kts"));
if (gradleChanged) {
  message("🔧 Gradle files were modified. Ensure dependency versions are pinned and compatible.");
}

// ── Sentry/crash handler changes ─────────────────────────────────────────────
const sentryRelated = allChangedFiles.some(
  (f) => f.includes("Sentry") || f.includes("CrashHandler") || f.includes("HexodusApplication")
);
if (sentryRelated) {
  warn(
    "**Crash reporting or application init was modified.** Verify Sentry config is correct and filterShizukuNoise() is not accidentally over-filtering."
  );
}

// ── Workflow changes ──────────────────────────────────────────────────────────
const workflowChanged = allChangedFiles.some((f) => f.startsWith(".github/workflows/"));
if (workflowChanged) {
  warn("**GitHub Actions workflow was modified.** Review CI changes carefully before merging.");
}

// ── TODO/FIXME left in source ─────────────────────────────────────────────────
// (Danger-js doesn't do inline content scanning easily without plugins,
//  so this is a reminder to check rather than automated detection)
message(
  `ℹ️ **Reminder:** check for any leftover \`TODO\`, \`FIXME\`, or \`Log.d\` calls in production paths before merging.`
);
